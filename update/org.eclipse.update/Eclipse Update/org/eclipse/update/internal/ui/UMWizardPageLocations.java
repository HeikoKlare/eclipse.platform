package org.eclipse.update.internal.ui;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.core.internal.boot.update.*;
import org.eclipse.swt.custom.*;
import java.net.*;
import java.util.TreeSet;

/**
 * Presents discovery URL locations for selection.
 */

public class UMWizardPageLocations extends WizardPage implements ModifyListener, SelectionListener
{
	protected boolean  _bInitialized              = false;
	protected boolean  _bUpdateMode               = false;
	protected UMWizard _wizard                    = null;
	protected Table    _tablePredefinedLocations  = null;
	protected Table    _tableAdditionalLocations  = null;
	protected Button   _buttonAdd                 = null;
	protected Button   _buttonRemove              = null;
	protected Button   _buttonAdditionalLocations = null;
	protected Button   _buttonPredefinedLocations = null;
	protected Text     _textAdditionalLocation    = null;
	protected Text     _textAdditionalDescription = null;
/**
 * ScriptNewScriptWizardPage1 constructor comment.
 * @param name java.lang.String
 */
public UMWizardPageLocations(UMWizard wizard, String strName, boolean bUpdateMode) {
	super(strName);
	_wizard = wizard;
	_bUpdateMode = bUpdateMode;

	if (bUpdateMode == false) {
		this.setTitle(UpdateManagerStrings.getString("S_New_Components"));
		this.setDescription(UpdateManagerStrings.getString("S_Specify_search_locations_for_new_components"));
	}
	else {
		this.setTitle(UpdateManagerStrings.getString("S_New_Component_Updates"));
		this.setDescription(UpdateManagerStrings.getString("S_Select_locations_to_update_from"));
	}
}
/**
 * createContents method comment.
 */
public void createControl(Composite compositeParent) {
	// Content
	//--------
	Composite compositeContent = new Composite(compositeParent, SWT.NULL);

	GridLayout layout = new GridLayout();
	compositeContent.setLayout(layout);

	GridData gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace = true;
	compositeContent.setLayoutData(gridData);

	// Top composite
	//--------------
	Composite compositeTop = new Composite(compositeContent, SWT.NULL);

	layout = new GridLayout();
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	compositeTop.setLayout(layout);

	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace = true;
	compositeTop.setLayoutData(gridData);

	// Checkbox: Predefined Locations
	//-------------------------------
	_buttonPredefinedLocations = new Button(compositeTop, SWT.CHECK);
	_buttonPredefinedLocations.setText(UpdateManagerStrings.getString("S_Predefined_Locations"));
	gridData = new GridData();
	gridData.verticalAlignment = GridData.FILL;
	gridData.widthHint = 500;
	_buttonPredefinedLocations.setLayoutData(gridData);
	_buttonPredefinedLocations.setSelection(true);
	_buttonPredefinedLocations.addSelectionListener(this);

	// Table: Pre-specified Locations
	//------------------------------
	_tablePredefinedLocations = new Table(compositeTop, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

	String[] columnTitles = new String[] { UpdateManagerStrings.getString("S_Location"), UpdateManagerStrings.getString("S_Description")};
	int[] iColumnWeight = { 50, 50 };
	TableLayout layoutTable = new TableLayout();

	for (int i = 0; i < columnTitles.length; i++) {
		TableColumn tableColumn = new TableColumn(_tablePredefinedLocations, SWT.NONE);
		tableColumn.setText(columnTitles[i]);
		ColumnLayoutData cLayout = new ColumnWeightData(iColumnWeight[i], true);
		layoutTable.addColumnData(cLayout);
	}

	_tablePredefinedLocations.setLinesVisible(true);
	_tablePredefinedLocations.setHeaderVisible(true);
	_tablePredefinedLocations.setLayout(layoutTable);
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.heightHint = 100;
	_tablePredefinedLocations.setLayoutData(gridData);
	_tablePredefinedLocations.addSelectionListener(this);

	// Composite: Bottom
	//------------------
	Composite compositeBottom = new Composite(compositeContent, SWT.NULL);

	layout = new GridLayout();
	layout.numColumns = 1;
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	compositeBottom.setLayout(layout);

	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	compositeBottom.setLayoutData(gridData);

	// Separator
	//----------
	Label labelSeparator = new Label(compositeBottom, SWT.NULL);
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.horizontalSpan = 1;
	labelSeparator.setLayoutData(gridData);

	// Checkbox: Additional Locations
	//-------------------------------
	_buttonAdditionalLocations = new Button(compositeBottom, SWT.CHECK);
	_buttonAdditionalLocations.setText(UpdateManagerStrings.getString("S_Additional_Locations"));
	gridData = new GridData();
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.horizontalSpan = 1;
	_buttonAdditionalLocations.setLayoutData(gridData);
	_buttonAdditionalLocations.setSelection(true);
	_buttonAdditionalLocations.addSelectionListener(this);

	// Table: Additional Locations
	//----------------------------
	_tableAdditionalLocations = new Table(compositeBottom, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

	columnTitles = new String[] { UpdateManagerStrings.getString("S_Location"), UpdateManagerStrings.getString("S_Description")};
	layoutTable = new TableLayout();

	for (int i = 0; i < columnTitles.length; i++) {
		TableColumn tableColumn = new TableColumn(_tableAdditionalLocations, SWT.NONE);
		tableColumn.setText(columnTitles[i]);
		ColumnLayoutData cLayout = new ColumnWeightData(iColumnWeight[i], true);
		layoutTable.addColumnData(cLayout);
	}

	_tableAdditionalLocations.setLinesVisible(true);
	_tableAdditionalLocations.setHeaderVisible(true);
	_tableAdditionalLocations.setLayout(layoutTable);
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.horizontalSpan = 1;
	gridData.heightHint = 100;
	_tableAdditionalLocations.setLayoutData(gridData);
	_tableAdditionalLocations.addSelectionListener(this);

	// Composite for three rows
	// If this isn't here, the buttons are too wide
	//---------------------------------------------
	Composite compositeBottomContent = new Composite(compositeBottom, SWT.NULL);

	layout = new GridLayout();
	layout.numColumns = 3;
	layout.marginWidth = 0;
	layout.marginHeight = 0;

	compositeBottomContent.setLayout(layout);

	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace = true;
	compositeBottomContent.setLayoutData(gridData);

	// Label: Uniform Resource Locator
	//--------------------------------
	Label label = new Label(compositeBottomContent, SWT.NULL);
	label.setText(UpdateManagerStrings.getString("S_Uniform_Resource_Locator") + ":");
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	label.setLayoutData(gridData);

	// Text: Location
	//----------------
	_textAdditionalLocation = new Text(compositeBottomContent, SWT.BORDER);
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	_textAdditionalLocation.setLayoutData(gridData);
	_textAdditionalLocation.addModifyListener(this);

	// Button: Remove
	//---------------
	_buttonRemove = new Button(compositeBottomContent, SWT.PUSH);
	_buttonRemove.setText(UpdateManagerStrings.getString("S_Remove"));
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	_buttonRemove.setLayoutData(gridData);
	_buttonRemove.setEnabled(false);
	_buttonRemove.addSelectionListener(this);

	// Label: Description
	//-------------------
	label = new Label(compositeBottomContent, SWT.NULL);
	label.setText(UpdateManagerStrings.getString("S_Description") + ":");
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	label.setLayoutData(gridData);

	// Text: Location
	//----------------
	_textAdditionalDescription = new Text(compositeBottomContent, SWT.BORDER);
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	_textAdditionalDescription.setLayoutData(gridData);

	// Button: Add
	//------------
	_buttonAdd = new Button(compositeBottomContent, SWT.PUSH);
	_buttonAdd.setText(UpdateManagerStrings.getString("S_Add"));
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	_buttonAdd.setLayoutData(gridData);
	_buttonAdd.addSelectionListener(this);
	_buttonAdd.setEnabled(false);

	setControl(compositeContent);
}
/**
 */
public URLNamePair[] getSelectedAdditionalLocations() {
	
	URLNamePair[] urlNamePairs = null;
	
	if (_buttonAdditionalLocations.getSelection() == true) {
	    
		TableItem[] tableItemsSelected = _tableAdditionalLocations.getSelection();

		urlNamePairs = new URLNamePair[tableItemsSelected.length];

		for (int i = 0; i < tableItemsSelected.length; ++i) {
			urlNamePairs[i] = new URLNamePair();
			urlNamePairs[i]._setURL(tableItemsSelected[i].getText(0));
			urlNamePairs[i]._setName(tableItemsSelected[i].getText(1));
		}
	}
	
	else {
		urlNamePairs = new URLNamePair[0];
	}

	return urlNamePairs;
}
/**
 */
public URLNamePair[] getSelectedPredefinedLocations() {
	
	URLNamePair[] urlNamePairs = null;

	if (_buttonPredefinedLocations.getSelection() == true) {
	    
		TableItem[] tableItemsSelected = _tablePredefinedLocations.getSelection();

		urlNamePairs = new URLNamePair[tableItemsSelected.length];

		for (int i = 0; i < tableItemsSelected.length; ++i) {
			urlNamePairs[i] = new URLNamePair();
			urlNamePairs[i]._setURL(tableItemsSelected[i].getText(0));
			urlNamePairs[i]._setName(tableItemsSelected[i].getText(1));
		}
	}

	else {
		urlNamePairs = new URLNamePair[0];
	}

	return urlNamePairs;
}
/**
 * Obtains a list of registered component URLs from the local update registry.
 * Obtains a list of bookmarked URLs from the persistent data.
 * Creates a tree for all of the URLs.
 */
protected void initializeContent() {

	if (_bInitialized == true) {
		return;
	}

	// Obtain downloadable component descriptors
	//------------------------------------------
	IURLNamePair[] urlsPredefined = null;

	if (_bUpdateMode == false)
		urlsPredefined = _wizard._updateManager.getLocalDiscoveryURLs();
	else {
		// Update all
		//-----------
		if (_wizard._manifestDescriptors == null) {
			urlsPredefined = _wizard._updateManager.getLocalUpdateURLs();
		}

		// Update user selected items
		//---------------------------
		else {
			// Obtain update URLs from each manifest descriptor
			// Place them in a set for uniqueness
			//-------------------------------------------------
			TreeSet setURLs = new TreeSet(new UpdateManagerURLComparator());

			IURLNamePair[] urlUpdates = null;

			for (int i = 0; i < _wizard._manifestDescriptors.length; ++i) {

				if (_wizard._manifestDescriptors[i] instanceof IProductDescriptor) {
					urlUpdates = ((IProductDescriptor) _wizard._manifestDescriptors[i]).getUpdateURLs();
					for (int j = 0; j < urlUpdates.length; ++j) {
						setURLs.add(urlUpdates[j]);
					}
				}

				else if (_wizard._manifestDescriptors[i] instanceof IComponentDescriptor) {
					urlUpdates = ((IComponentDescriptor) _wizard._manifestDescriptors[i]).getUpdateURLs();
					for (int j = 0; j < urlUpdates.length; ++j) {
						setURLs.add(urlUpdates[j]);
					}
				}
			}

			Object objArray[] = setURLs.toArray();
			urlsPredefined = new IURLNamePair[objArray.length];
			System.arraycopy(objArray, 0, urlsPredefined, 0, objArray.length);
		}
	}

	// Registered URLs
	//----------------
	for (int i = 0; i < urlsPredefined.length; ++i) {
		TableItem tableItem = null;

		if (urlsPredefined[i].getURL() != null) {
			tableItem = new TableItem(_tablePredefinedLocations, SWT.NULL);
			tableItem.setText(0, urlsPredefined[i].getURL().toExternalForm());
		}

		if (tableItem != null && urlsPredefined[i].getLabel() != null) {
			tableItem.setText(1, urlsPredefined[i].getLabel());
		}
	}

	if (_bUpdateMode == true)
		_tablePredefinedLocations.selectAll();

	// Bookmark strings
	//-----------------
	URLNamePair[] pairs = null;

	if (_bUpdateMode == false)
		pairs = _wizard._dialog._properties.getDiscoveryBookmarks();
	else
		pairs = _wizard._dialog._properties.getUpdateBookmarks();

	if (pairs.length > 0) {
		TableItem tableItem = null;
		for (int i = 0; i < pairs.length; ++i) {
			tableItem = new TableItem(_tableAdditionalLocations, SWT.NULL);
			if (pairs[i]._getURL() != null)
				tableItem.setText(0, pairs[i]._getURL());
			if (pairs[i]._getName() != null)
				tableItem.setText(1, pairs[i]._getName());
		}

		_buttonRemove.setEnabled(true);
	}

	// Page complete
	//--------------
	setPageComplete(_tablePredefinedLocations.getSelectionCount() > 0 || _tableAdditionalLocations.getSelectionCount() > 0);

	_bInitialized = true;

	return;
}
/**
 * modifyText method comment.
 */
public void modifyText(org.eclipse.swt.events.ModifyEvent e) {

	String strText = _textAdditionalLocation.getText();

	// Check for valid URL
	//--------------------
	try {
		String strURL = strText;
		strURL = strURL.replace(java.io.File.separatorChar,'/');

		new URL(strURL);
	}
	catch (MalformedURLException ex) {
		// Disable add button
		//-------------------
		_buttonAdd.setEnabled(false);
		return;
	}

	// Check if URL already exists in the lists
	//-----------------------------------------
	String strURL = null;

	TableItem[] items = _tableAdditionalLocations.getItems();

	for (int i = 0; i < items.length; ++i) {
		if (strText.equals(items[i].getText(0)) == true) {
			// Disable adding
			//---------------
			_buttonAdd.setEnabled(false);
			return;
		}
	}

	_buttonAdd.setEnabled(true);
}
/**
 * 
 */
public void setVisible(boolean bVisible) {

	super.setVisible(bVisible);
	
	if (bVisible == true) {
		initializeContent();
	}
}
/**
 * Sent when default selection occurs in the control.
 * <p>
 * For example, on some platforms default selection occurs
 * in a List when the user double-clicks an item.
 * </p>
 *
 * @param e an event containing information about the default selection
 */
public void widgetDefaultSelected(SelectionEvent e) {	
}
/**
 * widgetSelected method comment.
 */
public void widgetSelected(SelectionEvent e) {
	
	// Button: Add additional location
	//--------------------------------
	if (e.widget == _buttonAdd) {

		// The URL is guarenteed to be valid here
		//---------------------------------------
		try {
			String strURL = _textAdditionalLocation.getText();
			strURL = strURL.replace(java.io.File.separatorChar,'/');
			
			URL url = new URL(strURL);
			_textAdditionalLocation.setText(url.toExternalForm());

			TableItem tableItem = new TableItem(_tableAdditionalLocations, SWT.NULL);
			tableItem.setText(0, url.toExternalForm());
			tableItem.setText(1, _textAdditionalDescription.getText());

			URLNamePair pair = new URLNamePair();
			pair._setURL(url.toExternalForm());
			pair._setName(_textAdditionalDescription.getText());
			if (_bUpdateMode == true)
				_wizard._dialog._properties.addUpdateBookmark(pair);
			else
				_wizard._dialog._properties.addDiscoveryBookmark(pair);

			_tableAdditionalLocations.setSelection(_tableAdditionalLocations.getItemCount() - 1);
			_buttonAdd.setEnabled(false);
			_buttonRemove.setEnabled(true);
		}
		catch (MalformedURLException ex) {
		}
	}

	// Button: Remove additional location
	//-----------------------------------
	else if (e.widget == _buttonRemove) {

		int[] iaSelected = _tableAdditionalLocations.getSelectionIndices();
		TableItem[] itemsSelected = _tableAdditionalLocations.getSelection();

		URLNamePair pair = null;

		for (int i = 0; i < itemsSelected.length; ++i) {

			pair = new URLNamePair();
			pair._setURL(itemsSelected[i].getText(0));
			pair._setName(itemsSelected[i].getText(1));

			if (_bUpdateMode == true)
				_wizard._dialog._properties.removeUpdateBookmark(pair);
			else
				_wizard._dialog._properties.removeDiscoveryBookmark(pair);
		}

		_tableAdditionalLocations.remove(iaSelected);

		if (_tableAdditionalLocations.getItemCount() == 0) {
			_buttonRemove.setEnabled(false);
			_buttonAdd.setEnabled(_textAdditionalLocation.getText().length() > 0);
		}
	}

	// Table: Additional locations selection
	//--------------------------------------
	else if (e.widget == _tableAdditionalLocations) {
		if (_tableAdditionalLocations.getSelectionCount() > 0) {
			_textAdditionalLocation.setText(_tableAdditionalLocations.getItem(_tableAdditionalLocations.getSelectionIndex()).getText(0));
			_textAdditionalDescription.setText(_tableAdditionalLocations.getItem(_tableAdditionalLocations.getSelectionIndex()).getText(1));
			_buttonAdd.setEnabled(false);
			_buttonRemove.setEnabled(true);
		}
		else {
			_textAdditionalLocation.setText("");
			_textAdditionalDescription.setText("");
			_buttonAdd.setEnabled(false);
			_buttonRemove.setEnabled(false);
		}
	}

	// Checkbox: Additional locations
	//-------------------------------
	else if (e.widget == _buttonAdditionalLocations) {
		boolean bEnable = _buttonAdditionalLocations.getSelection();
		_tableAdditionalLocations.setEnabled(bEnable);
		_textAdditionalDescription.setEnabled(bEnable);
		_textAdditionalLocation.setEnabled(bEnable);

		// Remove button
		//--------------
		if (bEnable == true && _tableAdditionalLocations.getSelectionCount() > 0)
			_buttonRemove.setEnabled(true);
		else
			_buttonRemove.setEnabled(false);

		// Add button
		//-----------
		modifyText(null);
	}

	// Checkbox: Predefined locations
	//-------------------------------
	else if (e.widget == _buttonPredefinedLocations) {
		boolean bEnable = _buttonPredefinedLocations.getSelection();
		_tablePredefinedLocations.setEnabled(bEnable);
	}

	// Page complete
	//--------------
	int iNumberOfSelected = 0;

	if (_buttonPredefinedLocations.getSelection() == true)
		iNumberOfSelected += _tablePredefinedLocations.getSelectionCount();

	if (_buttonAdditionalLocations.getSelection() == true)
		iNumberOfSelected += _tableAdditionalLocations.getSelectionCount();

	setPageComplete(iNumberOfSelected > 0);

	// Always signify changed selection to next page
	//----------------------------------------------
	UMWizardPageInstallable pageInstallable = (UMWizardPageInstallable) _wizard.getPage("installable");
	pageInstallable._bRefreshRequired = true;
	
	return;
}
}
