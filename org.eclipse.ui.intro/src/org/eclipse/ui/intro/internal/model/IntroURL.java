/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.intro.internal.model;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.intro.internal.*;
import org.eclipse.ui.intro.internal.parts.*;
import org.eclipse.ui.intro.internal.presentations.*;
import org.eclipse.ui.intro.internal.util.*;

/**
 * An intro url. An intro URL is a valid http url, with org.eclipse.ui.intro as
 * a host. This class holds all logic to execute Intro URL commands, ie: an
 * Intro URL knows how to execute itself.
 */
public class IntroURL {

    /**
     * Intro URL constants.
     */
    public static final String INTRO_PROTOCOL = "http";
    public static final String INTRO_HOST_ID = "org.eclipse.ui.intro";

    /**
     * Constants that represent Intro URL actions.
     */
    public static final String SET_STANDBY = "setStandbyMode";
    public static final String CLOSE = "close";
    public static final String SHOW_HELP_TOPIC = "showHelpTopic";
    public static final String SHOW_HELP = "showHelp";
    public static final String OPEN_BROWSER = "openBrowser";
    public static final String RUN_ACTION = "runAction";
    public static final String SHOW_PAGE = "showPage";

    /**
     * Constants that represent valid action keys.
     */
    public static final String KEY_ID = "id";
    public static final String KEY_PLUGIN_ID = "pluginId";
    public static final String KEY_CLASS = "class";
    public static final String KEY_STANDBY = "standby";
    public static final String KEY_INPUT_ID = "inputId";

    private String action = null;
    private Properties parameters = null;

    /**
     * Prevent creation. Must be created through an IntroURLParser. This
     * constructor assumed we have a valid intro url.
     * 
     * @param url
     */
    IntroURL(String action, Properties parameters) {
        this.action = action;
        this.parameters = parameters;
    }

    /**
     * Executes whatever valid Intro action is embedded in this Intro URL.
     *  
     */
    public void execute() {
        // check to see if we have a custom action
        // if (action.indexOf("/") != -1)
        //   handleCustomAction();

        // check for all Intro actions.
        if (action.equals(CLOSE))
            closeIntro();

        else if (action.equals(SET_STANDBY))
            handleStandbyStateChanged(getParameter(KEY_STANDBY),
                    getParameter(KEY_PLUGIN_ID), getParameter(KEY_CLASS),
                    getParameter(KEY_INPUT_ID));

        else if (action.equals(SHOW_HELP))
            // display the full Help System.
            showHelp();

        else if (action.equals(SHOW_HELP_TOPIC))
            // display a Help System Topic.
            showHelpTopic(getParameter(KEY_ID));

        else if (action.equals(RUN_ACTION))
            // run an Intro action. Get the pluginId and the class keys.
            runAction(getParameter(KEY_PLUGIN_ID), getParameter(KEY_CLASS));

        else if (action.equals(SHOW_PAGE))
            // display an Intro Page.
            showPage(getParameter(KEY_ID));
    }

    private void closeIntro() {
        // Relies on Workbench.
        PlatformUI.getWorkbench().closeIntro(
                PlatformUI.getWorkbench().findIntro());
    }

    private void handleStandbyStateChanged(String standbyState,
            String pluginId, String standbyContentClassName, String inputId) {

        boolean standby = standbyState.equals("true") ? true : false;
        setStandbyState(standby);

        if (standby) {
            // now handle standby content.
            Object standbyContentObject = createClassInstance(pluginId,
                    standbyContentClassName);
            try {
                // we know we have a customizable part.
                CustomizableIntroPart introPart = (CustomizableIntroPart) IntroPlugin
                        .getDefault().getIntroModelRoot().getPresentation()
                        .getIntroPart();
                StandbyPart standbyPart = introPart.getStandbyPart();
                if (standbyContentObject instanceof IStandbyContentPart) {
                    IStandbyContentPart contentPart = (IStandbyContentPart) standbyContentObject;
                    standbyPart.addStandbyContentPart(contentPart);
                    standbyPart.setTopControl(contentPart.getClass().getName());
                }
                // set the input in all cases because we want to set it to null
                // if we have a simple standby URL, with no plugin and class
                // ids.
                standbyPart.setInput(inputId);
            } catch (Exception e) {
                Logger.logError("Could not create standby content for: "
                        + standbyContentClassName + " in " + pluginId, e);
                return;
            }
        }
    }


    /**
     * Set the Workbench Intro Part state.
     * 
     * @param state
     */
    private void setStandbyState(boolean state) {
        // rely on model to get part.
        IIntroPart introPart = IntroPlugin.getDefault().getIntroModelRoot()
                .getPresentation().getIntroPart();
        // should rely on Workbench api.
        PlatformUI.getWorkbench().setIntroStandby(introPart, state);
    }



    /**
     * Run an action
     */
    private void runAction(String pluginId, String className) {

        Object actionObject = createClassInstance(pluginId, className);
        try {
            if (actionObject instanceof IIntroAction) {
                IIntroAction introAction = (IIntroAction) actionObject;
                IIntroSite site = IntroPlugin.getDefault().getIntroModelRoot()
                        .getPresentation().getIntroPart().getIntroSite();
                introAction.initialize(site, parameters);
                introAction.run();
            } else if (actionObject instanceof IAction) {
                IAction action = (IAction) actionObject;
                action.run();
            } else if (actionObject instanceof IActionDelegate) {
                final IActionDelegate delegate = (IActionDelegate) actionObject;
                if (delegate instanceof IWorkbenchWindowActionDelegate)
                    ((IWorkbenchWindowActionDelegate) delegate).init(PlatformUI
                            .getWorkbench().getActiveWorkbenchWindow());
                Action proxy = new Action(this.action) {

                    public void run() {
                        delegate.run(this);
                    }
                };
                proxy.run();
            }
        } catch (Exception e) {
            Logger.logError("Could not run action: " + className, e);
            return;
        }
    }

    private Object createClassInstance(String pluginId, String className) {
        if (pluginId == null | className == null)
            // quick exits.
            return null;

        IPluginDescriptor desc = Platform.getPluginRegistry()
                .getPluginDescriptor(pluginId);
        if (desc == null)
            // quick exit.
            return null;

        Class aClass;
        Object aObject;
        try {
            aClass = desc.getPluginClassLoader().loadClass(className);
            aObject = aClass.newInstance();
            return aObject;
        } catch (Exception e) {
            Logger.logError("Could not instantiate: " + className + " in "
                    + pluginId, e);
            return null;
        }
    }

    /**
     * Open a help topic.
     */
    private void showHelpTopic(String href) {
        // WorkbenchHelp takes care of error handling.
        WorkbenchHelp.displayHelpResource(href);
    }

    /**
     * Open the help system.
     */
    private void showHelp() {
        WorkbenchHelp.displayHelp();
    }

    /**
     * Display an Intro Page.
     */
    private void showPage(String pageId) {
        // set the current page id in the model. This will triger a listener
        // event to the UI.
        IntroModelRoot modelRoot = IntroPlugin.getDefault().getIntroModelRoot();
        modelRoot.setCurrentPageId(pageId);
    }

    private void handleCustomAction() {
        // REVISIT:
    }

    /**
     * @return Returns the action imbedded in this URL.
     */
    public String getAction() {
        return action;
    }

    /**
     * Return a parameter defined in the Intro URL. Returns null if the
     * parameter is not defined.
     * 
     * @param parameterId
     * @return
     */
    public String getParameter(String parameterId) {
        return parameters.getProperty(parameterId);
    }

}
