/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor.support;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ant.ui.internal.editor.AntEditorCompletionProcessor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.w3c.dom.Element;

public class TestTextCompletionProcessor extends AntEditorCompletionProcessor {
    
    public TestTextCompletionProcessor() {
        cursorPosition = 10;
    }
    
    public ICompletionProposal[] getAttributeProposals(
        String aTaskName,
        String aPrefix) {
        return super.getAttributeProposals(aTaskName, aPrefix);
    }
    
    /**
     * Returns always 10.
     */
    public int getCursorPosition(ITextEditor textEditor) {
        return 10;
    }

    public Element findChildElementNamedOf(
        Element anElement,
        String aChildElementName) {
        return super.findChildElementNamedOf(anElement, aChildElementName);
    }

    public ICompletionProposal[] getTaskProposals(String aWholeDocumentString,
        Element aParentTaskElement,
        String aPrefix) {
        return super.getTaskProposals(aWholeDocumentString, aParentTaskElement, aPrefix);
    }

    public int determineProposalMode(
        String aWholeDocumentString,
        int aCursorPosition,
        String aPrefix) {
        return super.determineProposalMode(
            aWholeDocumentString,
            aCursorPosition,
            aPrefix);
    }

    public Element findParentElement(
        String aWholeDocumentString,
        int aLineNumber,
        int aColumnNumber) {
        return super.findParentElement(
            aWholeDocumentString,
            aLineNumber,
            aColumnNumber);
    }

    public String getPrefixFromDocument(
        String aDocumentText,
        int anOffset) {
        return super.getPrefixFromDocument(aDocumentText, anOffset);
    }

    public ICompletionProposal[] getPropertyProposals(
        String aDocumentText,
        String aPrefix, int aCursorPosition) {
        return super.getPropertyProposals(aDocumentText, aPrefix, aCursorPosition);
    }

	File editedFile;

    /**
     * Returns the edited File that org.eclipse.ui.externaltools.internal.ant.editorfore or a temporary 
     * file, which only serves as a dummy.
     * @see org.eclipse.ui.externaltools.internal.ant.editor.AntEditorCompletionProcessor#getEditedFile()
     */
	public File getEditedFile() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", null);
        } catch (IOException e) {
            TestCase.fail(e.getMessage());
        }
        tempFile.deleteOnExit();
        return tempFile;
    }
    

	public void setEditedFile(File aFile) {
		editedFile = aFile;
	}

	public void setLineNumber(int aLineNumber) {
    	lineNumber = aLineNumber;
    }

	public void setColumnNumber(int aColumnNumber) {
    	columnNumber = aColumnNumber;
    }

}