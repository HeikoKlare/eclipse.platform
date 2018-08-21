/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
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
 *     John Cortell (Freescale) - update javadoc tags (Bug 292301)
 *******************************************************************************/
package org.eclipse.debug.internal.core.commands;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IRequest;

/**
 * @since 3.3
 */
public class Request implements IRequest {

	private IStatus fStatus;
	private boolean fCanceled = false;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IRequest#done()
	 */
	@Override
	public void done() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IRequest#getStatus()
	 */
	@Override
	public IStatus getStatus() {
		return fStatus;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IRequest#setStatus(org.eclipse.core.runtime.IStatus)
	 */
	@Override
	public void setStatus(IStatus status) {
		fStatus = status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IRequest#cancel()
	 */
	@Override
	public synchronized void cancel() {
		fCanceled = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IRequest#isCanceled()
	 */
	@Override
	public synchronized boolean isCanceled() {
		return fCanceled;
	}

}
