/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Relaunches the last run-mode launch
 * 
 * This menu item appears in the main 'Run' menu
 * 
 * @see RelaunchLastAction
 * @see DebugLastAction
 * @see ProfileLastAction
 */
public class RunLastAction extends RelaunchLastAction {

	/**
	 * @see RelaunchLastAction#getMode()
	 */
	public String getMode() {
		return ILaunchManager.RUN_MODE;
	}	
	
	/**
	 * @see org.eclipse.debug.internal.ui.actions.LaunchDropDownAction#getLaunchGroupId()
	 */
	public String getLaunchGroupId() {
		return IDebugUIConstants.ID_RUN_LAUNCH_GROUP;
	}

	/**
	 * @see org.eclipse.debug.internal.ui.actions.RelaunchLastAction#getText()
	 */
	protected String getText() {
		if(DebugUIPlugin.getDefault().getContextLaunchingResourceManager().isContextLaunchEnabled()) {
			return ActionMessages.RunLastAction_1;
		}
		else {
			return ActionMessages.RunLastAction_0;
		}
	}

	/**
	 * @see org.eclipse.debug.internal.ui.actions.RelaunchLastAction#getTooltipText()
	 */
	protected String getTooltipText() {
		return ""; //$NON-NLS-1$
	}	
}
