/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;
import static org.eclipse.core.tests.harness.FileSystemHelper.getRandomLocation;
import static org.eclipse.core.tests.harness.FileSystemHelper.getTempDir;
import static org.eclipse.core.tests.resources.ResourceTestUtil.createTestMonitor;
import static org.eclipse.core.tests.resources.ResourceTestUtil.waitForBuild;
import static org.eclipse.core.tests.resources.ResourceTestUtil.waitForRefresh;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.rules.ExternalResource;

/**
 * Restores a clean workspace with a default description and an empty resource
 * tree after test execution. Ensures that test finished in a minutes.
 */
public class WorkspaceTestRule extends ExternalResource {
	/**
	 * Set of FileStore instances that must be deleted when the test is complete
	 *
	 * @see #getTempStore
	 */
	private final Set<IFileStore> storesToDelete = new HashSet<>();

	@Override
	protected void before() throws Exception {
		assertNotNull("Workspace was not setup", getWorkspace());
		FreezeMonitor.expectCompletionInAMinute();
		waitForRefresh();
	}

	@Override
	protected void after() {
		boolean wasJobMangerSuspended = resumeJobManagerIfNecessary();
		try {
			restoreCleanWorkspace();
		} finally {
			FreezeMonitor.done();
			assertWorkspaceFolderEmpty();
			assertFalse("This test stopped the JobManager, which could have affected other tests.", //
					wasJobMangerSuspended);
		}
	}

	private void restoreCleanWorkspace() {
		IllegalStateException exception = new IllegalStateException("Failures when cleaning up workspace");
		try {
			restoreWorkspaceDescription();
		} catch (CoreException e) {
			exception.addSuppressed(e);
		}
		// Wait for any build job that may still be executed
		waitForBuild();
		try {
			getWorkspace().run((IWorkspaceRunnable) monitor -> {
				getWorkspace().getRoot().delete(true, true, createTestMonitor());
				// clear stores in workspace runnable to avoid interaction with resource jobs
				for (IFileStore element : storesToDelete) {
					clear(element);
				}
				storesToDelete.clear();
			}, null);
		} catch (CoreException e) {
			exception.addSuppressed(e);
		}
		try {
			getWorkspace().save(true, null);
		} catch (CoreException e) {
			exception.addSuppressed(e);
		}
		// don't leak builder jobs, since they may affect subsequent tests
		waitForBuild();
		if (exception.getSuppressed().length != 0) {
			throw exception;
		}
	}

	private void restoreWorkspaceDescription() throws CoreException {
		getWorkspace().setDescription(Workspace.defaultWorkspaceDescription());
	}

	private void assertWorkspaceFolderEmpty() {
		final String metadataDirectoryName = ".metadata";
		File workspaceLocation = getWorkspace().getRoot().getLocation().toFile();
		File[] remainingFilesInWorkspace = workspaceLocation
				.listFiles(file -> !file.getName().equals(metadataDirectoryName));
		assertArrayEquals("There are unexpected contents in the workspace folder", new File[0],
				remainingFilesInWorkspace);
	}

	private boolean resumeJobManagerIfNecessary() {
		if (Job.getJobManager().isSuspended()) {
			Job.getJobManager().resume();
			return true;
		}
		return false;
	}

	/**
	 * Returns a temporary file store.
	 */
	public IFileStore getTempStore() {
		IFileStore store = EFS.getLocalFileSystem()
				.getStore(getRandomLocation(getTempDir()));
		deleteOnTearDown(store);
		return store;
	}

	/**
	 * Returns a FileStore instance backed by storage in a temporary location. The
	 * returned store will not exist, but will belong to an existing parent. The
	 * tearDown method in this class will ensure the location is deleted after the
	 * test is completed.
	 */
	public void deleteOnTearDown(IPath path) {
		storesToDelete.add(EFS.getLocalFileSystem().getStore(path));
	}

	/**
	 * Ensures that the given store is deleted during test tear down.
	 */
	public void deleteOnTearDown(IFileStore store) {
		storesToDelete.add(store);

	}

	private void clear(IFileStore store) throws CoreException {
		store.delete(EFS.NONE, null);
	}

}
