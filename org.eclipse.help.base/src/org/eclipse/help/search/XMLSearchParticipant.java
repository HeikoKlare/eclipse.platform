/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.search;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.dynamic.ExtensionHandler;
import org.eclipse.help.internal.dynamic.IncludeHandler;
import org.eclipse.help.internal.dynamic.NodeHandler;
import org.eclipse.help.internal.dynamic.NodeReader;
import org.eclipse.help.internal.dynamic.XMLProcessor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An abstract search participants for adding XML documents to the Lucene search index. Subclass it
 * and implement or override protected methods to handle parsing of the document.
 * 
 * @since 3.2
 */
public abstract class XMLSearchParticipant extends LuceneSearchParticipant {
	private Stack stack = new Stack();
	private SAXParser parser;
	private XMLProcessor processor;
	private boolean hasFilters;

	/**
	 * Class that implements this interface is used to store data obtained during the parsing phase.
	 */
	protected interface IParsedXMLContent {

		/**
		 * Returns the locale of the index.
		 * 
		 * @return the locale string
		 */
		String getLocale();

		/**
		 * Sets the title of the parsed document for indexing.
		 * 
		 * @param title
		 *            the document title
		 */
		void setTitle(String title);

		/**
		 * Sets the optional summary of the parsed document that can be later rendered for the
		 * search hits.
		 * 
		 * @param summary
		 *            the short document summary
		 */
		void addToSummary(String summary);

		/**
		 * Adds the text to the content buffer for indexing.
		 * 
		 * @param text
		 *            the text to add to the document content buffer
		 */
		void addText(String text);
	}

	private static class ParsedXMLContent implements IParsedXMLContent {
		private StringBuffer buffer = new StringBuffer();
		private StringBuffer summary = new StringBuffer();
		private String title;
		private String locale;
		private static int SUMMARY_LENGTH = 200;

		public ParsedXMLContent(String locale) {
			this.locale = locale;
		}

		public String getLocale() {
			return locale;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void addToSummary(String text) {
			if (summary.length() >= SUMMARY_LENGTH)
				return;
			if (summary.length() > 0)
				summary.append(" "); //$NON-NLS-1$
			summary.append(text);
			if (summary.length() > SUMMARY_LENGTH)
				summary.delete(SUMMARY_LENGTH, summary.length());
		}

		public void addText(String text) {
			if (buffer.length() > 0)
				buffer.append(" "); //$NON-NLS-1$
			buffer.append(text);
		}

		public Reader newContentReader() {
			return new StringReader(buffer.toString());
		}

		public String getSummary() {
			// if the summary starts with the title, trim that part off.
			String summaryStr = summary.toString();
			if (title != null && summaryStr.length() >= title.length()) {
				String header = summaryStr.substring(0, title.length());
				if (header.equalsIgnoreCase(title)) {
					return summaryStr.substring(title.length()).trim();
				}
			}
			return summaryStr;
		}

		public String getTitle() {
			return title;
		}
	}

	private class XMLHandler extends DefaultHandler {

		public ParsedXMLContent data;

		public XMLHandler(ParsedXMLContent data) {
			this.data = data;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			stack.push(qName);
			handleStartElement(qName, attributes, data);
			if (attributes.getValue("filter") != null || qName.equalsIgnoreCase("filter")) { //$NON-NLS-1$ //$NON-NLS-2$
				hasFilters = true;
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			handleEndElement(qName, data);
			String top = (String) stack.peek();
			if (top != null && top.equals(qName))
				stack.pop();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
		 */
		public void startDocument() throws SAXException {
			XMLSearchParticipant.this.handleStartDocument(data);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
		 */
		public void endDocument() throws SAXException {
			XMLSearchParticipant.this.handleEndDocument(data);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#processingInstruction(java.lang.String,
		 *      java.lang.String)
		 */
		public void processingInstruction(String target, String pidata) throws SAXException {
			handleProcessingInstruction(target, data);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] characters, int start, int length) throws SAXException {
			if (length == 0)
				return;
			StringBuffer buff = new StringBuffer();
			for (int i = 0; i < length; i++) {
				buff.append(characters[start + i]);
			}
			String text = buff.toString().trim();
			if (text.length() > 0)
				handleText(text, data);
		}
	}

	/**
	 * Called when the element has been started.
	 * 
	 * @param name
	 *            the element name
	 * @param attributes
	 *            the element attributes
	 * @param data
	 *            data the parser content data to update
	 */
	protected abstract void handleStartElement(String name, Attributes attributes, IParsedXMLContent data);

	/**
	 * Called when the element has been ended.
	 * 
	 * @param name
	 *            the name of the XML element
	 * @param data
	 *            data the parser content data to update
	 */
	protected abstract void handleEndElement(String name, IParsedXMLContent data);

	/**
	 * Called when the XML document has been started.
	 * 
	 * @param data
	 *            data the parser content data to update
	 */
	protected void handleStartDocument(IParsedXMLContent data) {
	}

	/**
	 * Called when the XML document has been ended.
	 * 
	 * @param data
	 *            data the parser content data to update
	 */
	protected void handleEndDocument(IParsedXMLContent data) {
	}

	/**
	 * Called when a processing instruction has been encountered.
	 * 
	 * @param type
	 *            the instruction data
	 * @param data
	 *            the parser content data to update
	 */
	protected void handleProcessingInstruction(String type, IParsedXMLContent data) {
	}

	/**
	 * Called when element body text has been encountered. Use 'getElementStackPath()' to determine
	 * the element in question.
	 * 
	 * @param text
	 *            the body text
	 * @param data
	 *            the parser content data to update
	 */
	protected abstract void handleText(String text, IParsedXMLContent data);

	/*
	 * @see LuceneSearchParticipant#addDocument(String, String, URL, String, Document)
	 */
	public IStatus addDocument(ISearchIndex index, String pluginId, String name, URL url, String id,
			Document doc) {
		InputStream stream = null;
		try {
			if (parser == null) {
				parser = SAXParserFactory.newInstance().newSAXParser();
			}
			stack.clear();
			hasFilters = false;
			ParsedXMLContent parsed = new ParsedXMLContent(index.getLocale());
			XMLHandler handler = new XMLHandler(parsed);
			stream = url.openStream();
			stream = preprocess(stream, name, index.getLocale());
			parser.parse(stream, handler);
			doc.add(Field.Text("contents", parsed.newContentReader())); //$NON-NLS-1$
			doc.add(Field.Text("exact_contents", parsed //$NON-NLS-1$
					.newContentReader()));
			String title = parsed.getTitle();
			if (title != null)
				addTitle(title, doc);
			String summary = parsed.getSummary();
			if (summary != null)
				doc.add(Field.UnIndexed("summary", summary)); //$NON-NLS-1$
			if (hasFilters) {
				doc.add(Field.UnIndexed("filters", "true")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, IStatus.ERROR,
					"Exception occurred while adding document " + name //$NON-NLS-1$
							+ " to index.", //$NON-NLS-1$
					e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
				stream = null;
			}
		}
	}

	/**
	 * Returns the name of the element that is currently at the top of the element stack.
	 * 
	 * @return the name of the element that is currently at the top of the element stack
	 */

	protected String getTopElement() {
		return (String) stack.peek();
	}

	/**
	 * Returns the full path of the current element in the stack separated by the '/' character.
	 * 
	 * @return the path to the current element in the stack.
	 */
	protected String getElementStackPath() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < stack.size(); i++) {
			if (i > 0)
				buf.append("/"); //$NON-NLS-1$
			buf.append((String) stack.get(i));
		}
		return buf.toString();
	}
	
	/**
	 * <p>
	 * Pre-processes the given document input stream for the given document name and locale.
	 * This implementation will resolve dynamic content that is applicable to searching,
	 * e.g. includes and extensions, but not filters. Subclasses may override to do their
	 * own pre-processing.
	 * </p>
	 * <p>
	 * For performance, implementations that handle documents that do not support dynamic
	 * content should subclass and return the original stream.
	 * </p>
	 * 
	 * @param in the input stream for the document content
	 * @param name the name of the document as it appears in the index
	 * @param locale the locale code, e.g. "en_US"
	 * @return the processed content
	 * @since 3.3
	 */
	protected InputStream preprocess(InputStream in, String name, String locale) {
		if (processor == null) {
			NodeReader reader = new NodeReader();
			reader.setIgnoreWhitespaceNodes(true);
			processor = new XMLProcessor(new NodeHandler[] {
				new IncludeHandler(reader, locale),
				new ExtensionHandler(reader, locale)
			});
		}
		try {
			return processor.process(in, name, null);
		}
		catch (Throwable t) {
			String msg = "An error occured while pre-processing user assistance document \"" + name + "\" for search indexing"; //$NON-NLS-1$ //$NON-NLS-2$
			HelpBasePlugin.logError(msg, t);
			return in;
		}
	}
}
