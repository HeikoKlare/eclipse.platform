/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

 
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IVariableValueEditor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action for changing the value of primitives and <code>String</code> variables.
 * This action will attempt to delegate the editing operation to a registered
 * variable value editor, if any is provided for the variable's debug model.
 * @see org.eclipse.debug.ui.actions.IVariableValueEditor
 */
public class ChangeVariableValueAction extends SelectionProviderAction {
    
	protected IVariable fVariable;
    private VariablesView fView;
    private boolean fEditing= false;
    /**
     * Mapping of debug model identifiers to variable value editors.
     * The keys in this map are always Strings (model ids).
     * The values in the map are IConfigurationElements at startup,
     * which are replaced by IVariableValueEditors as the editors
     * are instantiated (editors are loaded lazily, then cached).
     */
    private Map fEditorMap= new HashMap();
	
	public ChangeVariableValueAction(VariablesView view) {
		super(view.getViewer(), ActionMessages.getString("ChangeVariableValue.title")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("ChangeVariableValue.toolTipText")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_CHANGE_VARIABLE_VALUE));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_CHANGE_VARIABLE_VALUE));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_CHANGE_VARIABLE_VALUE));
		WorkbenchHelp.setHelp(
			this,
			IDebugHelpContextIds.CHANGE_VALUE_ACTION);
		fView= view;
		loadVariableEditors();
	}
	
	/**
	 * Loads contributors to the org.eclipse.debug.ui.variableValueEditors extension point,
	 * for use when the user runs this action.
	 */
	private void loadVariableEditors() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_VARIABLE_VALUE_EDITORS);
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            String modelId = element.getAttribute("modelId"); //$NON-NLS-1$
            if (modelId != null) {
                fEditorMap.put(modelId, element);
            }
        }
	}
	
	/**
	 * Edit the variable value with an inline text editor.  
	 */
	protected void doActionPerformed(final IVariable variable) {
	    Shell shell = fView.getViewSite().getShell();
	    
		// If a previous edit is still in progress, don't start another		
	    if (fEditing) {
	        return;
	    }
	    fEditing= true;
		fVariable = variable;
	    if (!delegateEdit(shell)) {
	        doDefaultEdit(shell);
	    }
		fEditing= false;
	}
	
	/**
	 * Attempts to edit the variable by delegating to anyone who's
	 * contributed a variable value editor via extension. Returns
	 * <code>true</code> if a delegate handled the edit, <code>false</code>
	 * if the variable still needs to be edited.
	 * 
     * @param shell a shell for prompting the user
     * @return whether or not a delegate attempted to edit the variable
     */
    private boolean delegateEdit(Shell shell) {
        String modelIdentifier = fVariable.getModelIdentifier();
        Object object = fEditorMap.get(modelIdentifier);
        IVariableValueEditor editor= null;
        if (object instanceof IVariableValueEditor) {
            editor= (IVariableValueEditor) object;
        } else if (object instanceof IConfigurationElement) {
            try {
                editor = (IVariableValueEditor) ((IConfigurationElement) object).createExecutableExtension("class"); //$NON-NLS-1$
                fEditorMap.put(modelIdentifier, editor);
            } catch (CoreException e) {
                // If an exception occurs, loading the extension, just log it and
                // return false to use the default editor.
                DebugUIPlugin.log(e);
            }
        }
        if (editor != null) {
            return editor.editVariable(fVariable, shell);
        }
        return false;
    }

    /**
     * Edits the variable using the default variable editor
     * @param shell a shell for prompting the user
     */
    protected void doDefaultEdit(Shell shell) {
	    String name= ""; //$NON-NLS-1$
		String value= ""; //$NON-NLS-1$
		try {
			name= fVariable.getName();
			value= fVariable.getValue().getValueString();
		} catch (DebugException exception) {
			DebugUIPlugin.errorDialog(shell, ActionMessages.getString("ChangeVariableValue.errorDialogTitle"),ActionMessages.getString("ChangeVariableValue.errorDialogMessage"), exception);	//$NON-NLS-2$ //$NON-NLS-1$
			return;
		}
		ChangeVariableValueInputDialog inputDialog= new ChangeVariableValueInputDialog(shell, ActionMessages.getString("ChangeVariableValue.1"), MessageFormat.format(ActionMessages.getString("ChangeVariableValue.2"), new String[] {name}), value, new IInputValidator() { //$NON-NLS-1$ //$NON-NLS-2$
			/**
			 * Returns an error string if the input is invalid
			 */
			public String isValid(String input) {
				try {
					if (fVariable.verifyValue(input)) {
						return null; // null means valid
					}
				} catch (DebugException exception) {
					return ActionMessages.getString("ChangeVariableValue.3"); //$NON-NLS-1$
				}
				return ActionMessages.getString("ChangeVariableValue.4"); //$NON-NLS-1$
			}
		});
		
		inputDialog.open();
		String newValue= inputDialog.getValue();
		if (newValue != null) {
			// null value means cancel was pressed
			try {
				fVariable.setValue(newValue);
				getSelectionProvider().setSelection(new StructuredSelection(fVariable));
			} catch (DebugException de) {
				DebugUIPlugin.errorDialog(shell, ActionMessages.getString("ChangeVariableValue.errorDialogTitle"),ActionMessages.getString("ChangeVariableValue.errorDialogMessage"), de);	//$NON-NLS-2$ //$NON-NLS-1$
			}
		}
	}
		
	/**
	 * Updates the enabled state of this action based
	 * on the selection
	 */
	protected void update(IStructuredSelection sel) {
		Iterator iter= sel.iterator();
		if (iter.hasNext()) {
			Object object= iter.next();
			if (object instanceof IValueModification) {
				IValueModification varMod= (IValueModification)object;
				if (!varMod.supportsValueModification()) {
					setEnabled(false);
					return;
				}
				setEnabled(!iter.hasNext());
				return;
			}
		}
		setEnabled(false);
	}

	/**
	 * @see IAction#run()
	 */
	public void run() {
		Iterator iterator= getStructuredSelection().iterator();
		doActionPerformed((IVariable)iterator.next());
	}
	
	/**
	 * @see SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection sel) {
		update(sel);
	}
}

