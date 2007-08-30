/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.UAContentFilter;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.HelpEvaluationContext;

/**
 * Class for finding a topic in a set of TOCs based on its href.  Some of this code was
 * refactored from the TocData class
 */

public class TopicFinder {
	
	private ITopic[] foundTopicPath;
	private int selectedToc;
	private IToc[] tocs;
	
	public TopicFinder(String topicHref, IToc[] tocs) {
		this.tocs = tocs;
		if (topicHref != null && topicHref.length() > 0) {
			int index = -1;
			do {
				selectedToc = findTocContainingTopic(topicHref);
				
				ITopic topic = findTopic(UrlUtil.getHelpURL(topicHref));
				if (topic != null && selectedToc >= 0) {
					foundTopicPath = getTopicPathInToc(topic, tocs[selectedToc]);
				}
				// if no match has been found, check if there is an anchor
				if (foundTopicPath == null && topicHref != null) {
					index = topicHref.indexOf('#');
					if (index != -1)
						topicHref = topicHref.substring(0, index);
				}
				// if there was an anchor, search again without it
			} while (foundTopicPath == null && index != -1);
	    } else {
	    	selectedToc = -1;
			foundTopicPath = null;
	    }	
	}

    public ITopic[] getTopicPath() {
		return foundTopicPath;
	}
    
    public int getSelectedToc() {
    	return selectedToc;
    }

	/*
     * Finds a path of ITopics in the given IToc to the given topic. If the
     * toc doesn't contain the topic, returns null.
     */
	private ITopic[] getTopicPathInToc(ITopic topicToFind, IToc toc) {
		if (topicToFind.getLabel().equals(toc.getLabel())) {
			return new ITopic[0];
		}
		ITopic topics[] = toc.getTopics();
		if (topics != null) {
			for (int i = 0; i < topics.length; ++i) {
				// returns path in reverse order
				List reversePath = getTopicPathInTopic(topicToFind, topics[i]);
				if (reversePath != null) {
					// reverse and return
					ITopic[] path = new ITopic[reversePath.size()];
					for (int j = 0; j < path.length; ++j) {
						path[j] = (ITopic) reversePath.get((path.length - 1)
								- j);
					}
					return path;
				}
			}
		}
		return null;
	}

	/*
	 * Finds the topic in the given topic sub-tree. Returns a path of ITopics to
	 * that topic in reverse order (from the topic up).
	 */
	private List getTopicPathInTopic(ITopic topicToFind, ITopic topic) {
		if (topic.getLabel().equals(topicToFind.getLabel())) {
			// found it. start the list to be created recursively
			List path = new ArrayList();
			path.add(topic);
			return path;
		} else {
			ITopic[] subtopics = topic.getSubtopics();
			for (int i = 0; i < subtopics.length; ++i) {
				List path = getTopicPathInTopic(topicToFind, subtopics[i]);
				if (path != null) {
					// it was in a subtopic.. add to the path and return
					path.add(topic);
					return path;
				}
			}
		}
		return null;
	}

	/**
	 * Finds a TOC that contains specified topic
	 * 
	 * @param topic
	 *            the topic href
	 * @return -1 if the toc is not found
	 */
	private int findTocContainingTopic(String topic) {
		if (topic == null || topic.equals("")) //$NON-NLS-1$
			return -1;

		int index = topic.indexOf("/topic/"); //$NON-NLS-1$
		if (index != -1) {
			topic = topic.substring(index + 6);
		} else {
			// auto-generated nav urls, e.g. "/help/nav/0_1_5"
			index = topic.indexOf("/nav/"); //$NON-NLS-1$
			if (index != -1) {
				// first number is toc index
				String nav = topic.substring(index + 5);
				String book;
				index = nav.indexOf('_');
				if (index == -1) {
					book = nav;
				} else {
					book = nav.substring(0, index);
				}

				try {
					return Integer.parseInt(book);
				} catch (Exception e) {
					// shouldn't happen
				}
			}
		}
		index = topic.indexOf('?');
		if (index != -1)
			topic = topic.substring(0, index);

		if (topic == null || topic.equals("")) //$NON-NLS-1$
			return -1;

		// try to find in enabled tocs first
		for (int i = 0; i < tocs.length; i++)
			if (isEnabled(tocs[i])) {
				if (tocs[i].getTopic(topic) != null) {
					return i;
				}
				ITopic tocTopic = tocs[i].getTopic(null);
				if (tocTopic != null && topic.equals(tocTopic.getHref())) {
					return i;
				}
			}
		// try disabled tocs second
		for (int i = 0; i < tocs.length; i++)
			if (!isEnabled(tocs[i]))
				if (tocs[i].getTopic(topic) != null)
					return i;
		// nothing found
		return -1;
	}

	/**
	 * Finds topic in a TOC
	 * 
	 * @return ITopic or null
	 */
	private ITopic findTopic(String topic) {

		int index = topic.indexOf("/topic/"); //$NON-NLS-1$
		if (index != -1) {
			topic = topic.substring(index + 6);
		} else {
			// auto-generated nav urls, e.g. "/help/nav/0_1_5"
			index = topic.indexOf("/nav/"); //$NON-NLS-1$
			if (index != -1) {
				String nav = topic.substring(index + 5);
				StringTokenizer tok = new StringTokenizer(nav, "_"); //$NON-NLS-1$
				try {
					// first number is toc index
					index = Integer.parseInt(tok.nextToken());
					ITopic current = tocs[index].getTopic(null);
					while (tok.hasMoreTokens()) {
						index = Integer.parseInt(tok.nextToken());
						current = current.getSubtopics()[index];
					}
					return current;
				} catch (Exception e) {
					// shouldn't happen
				}
			}
		}
		index = topic.indexOf('?');
		if (index != -1)
			topic = topic.substring(0, index);

		if (topic == null || topic.equals("")) //$NON-NLS-1$
			return null;

		if (selectedToc < 0)
			return null;
		IToc selectedToc2 = tocs[selectedToc];
		if (selectedToc2 == null)
			return null;
		ITopic selectedTopic = selectedToc2.getTopic(topic);
		if (selectedTopic != null) {
			return selectedTopic;
		}

		ITopic tocTopic = selectedToc2.getTopic(null);
		if (tocTopic != null && topic.equals(tocTopic.getHref())) {
			return tocTopic;
		}
		return null;
	}

	/**
	 * Check if given TOC is visible and non empty
	 * 
	 * @param toc
	 * @return true if TOC should be visible
	 */
	private boolean isEnabled(IToc toc) {
		boolean tocEnabled = HelpBasePlugin.getActivitySupport().isEnabled(
				toc.getHref())
				&& !UAContentFilter.isFiltered(toc, HelpEvaluationContext
						.getContext());
		if (!tocEnabled)
			return false;
		ITopic[] topics = toc.getTopics();
		for (int i = 0; i < topics.length; i++) {
			if (EnabledTopicUtils.isEnabled(topics[i])) {
				return true;
			}
		}
		return false;
	}
}
