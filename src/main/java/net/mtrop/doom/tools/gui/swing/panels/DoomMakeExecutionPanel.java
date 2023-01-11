/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.mtrop.doom.tools.doommake.AutoBuildAgent;
import net.mtrop.doom.tools.doommake.AutoBuildAgent.Listener;
import net.mtrop.doom.tools.gui.managers.AppCommon;
import net.mtrop.doom.tools.gui.managers.DoomMakeProjectHelper;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomMakeProjectHelper.ProcessCallException;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The DoomMake New Project application.
 * @author Matthew Tropiano
 */
public class DoomMakeExecutionPanel extends JPanel
{
	private static final long serialVersionUID = -6262181847728947185L;

	private static final String STATE_PROJECT_DIRECTORY = "execution.projectDirectory";

    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomMakeExecutionPanel.class); 

    private static final String[] NO_ARGS = new String[0];

    // Singletons

    /** Language. */
    private DoomToolsLanguageManager language;
    /** Project helper. */
    private DoomMakeProjectHelper helper;
    /** Project helper. */
    private AppCommon appCommon;

	// Components
	
    /** Targets component. */
    private DoomMakeProjectTargetListPanel listPanel;
    /** Checkbox for flagging auto-build. */
    private JCheckBox autoBuildCheckbox;
    /** Target run action. */
    private Action targetRunAction;

	// Fields
    
    /** Project directory. */
    private File projectDirectory;
    /** Status messages. */
    private DoomToolsStatusPanel statusPanel;
    /** Logging panel override. */
    private DoomToolsTextOutputPanel outputPanel;

    // State
    
    /** Current target. */
    private String currentTarget;
    /** Auto build agent. */
    private AutoBuildAgent autoBuildAgent;

    /**
	 * Creates a new panel from a project directory.
     * @param statusPanel the status panel to talk to.
     * @param targetDirectory 
     * @param outputPanel optional output override panel.
     * @param ideMode if true, omit the IDE button.
	 */
	public DoomMakeExecutionPanel(DoomToolsStatusPanel statusPanel, File targetDirectory, DoomToolsTextOutputPanel outputPanel, boolean ideMode)
	{
		this.language = DoomToolsLanguageManager.get();
		this.helper = DoomMakeProjectHelper.get();
		this.appCommon = AppCommon.get();

		this.listPanel = new DoomMakeProjectTargetListPanel(
			Collections.emptySortedSet(),
			(target) -> setCurrentTarget(target), 
			(target) -> { 
				setCurrentTarget(target);
				runCurrentTarget();
			}
		);
		this.autoBuildCheckbox = checkBox(language.getText("doommake.project.autobuild"), false, (v) -> {
			if (v)
				startAgent();
			else
				shutDownAgent();
		});
		
		this.targetRunAction = actionItem(language.getText("doommake.project.buildaction"), (e) -> runCurrentTarget());

		this.statusPanel = statusPanel;
		this.statusPanel.setSuccessMessage(language.getText("doommake.project.build.message.ready"));

		this.outputPanel = outputPanel;
		
		this.projectDirectory = targetDirectory;
		
		this.currentTarget = null;
		this.autoBuildAgent = null;

		DoomMakeProjectControlPanel control = new DoomMakeProjectControlPanel(projectDirectory, this, ideMode);
		refreshTargets();
		
		containerOf(this,
			node(containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(borderLayout(4, 0),
					node(BorderLayout.CENTER, label(language.getText("doommake.project.targets"))),
					node(BorderLayout.EAST, control)
				)),
				node(BorderLayout.CENTER, containerOf(borderLayout(0, 4),
					node(BorderLayout.CENTER, containerOf(
						node(scroll(listPanel))
					)),
					node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
						node(BorderLayout.CENTER, autoBuildCheckbox),
						node(BorderLayout.EAST, button(targetRunAction))
					))
				))
			))
		);
	}
	
	/**
	 * Checks if a directory is a project directory.
	 * @param directory the directory to check.
	 * @return true if it is, false if not.
	 */
	public static boolean isProjectDirectory(File directory)
	{
		if (!directory.isDirectory())
			return false;
		if (!(new File(directory.getAbsolutePath() + File.separator + "doommake.script")).exists())
			return false;
		return true;
	}
	
	/**
	 * Refreshes the targets.
	 */
	public void refreshTargets()
	{
		String absolutePath = projectDirectory.getAbsolutePath();
		try {
			listPanel.refreshTargets(helper.getProjectTargets(projectDirectory));
			LOG.infof("Targets refreshed for %s", absolutePath);
		} catch (FileNotFoundException e) {
			SwingUtils.error(this, language.getText("doommake.project.targets.error.nodirectory", absolutePath));
			LOG.errorf("Project directory does not exist: %s", absolutePath);
		} catch (ProcessCallException e) {
			SwingUtils.error(this, language.getText("doommake.project.targets.error.gettargets", absolutePath));
			LOG.errorf("Could not invoke `doommake --targets` in %s", absolutePath);
		}
	}

	/**
	 * Saves this component's state to a state map.
	 * @param prefix the key prefix
	 * @param state the output state map.
	 */
	public void saveState(String prefix, Map<String, String> state)
	{
		state.put(prefix + "." + STATE_PROJECT_DIRECTORY, projectDirectory.getAbsolutePath());
	}

	/**
	 * Loads this component's state from a state map.
	 * @param prefix the key prefix
	 * @param state the input state map.
	 */
	public void loadState(String prefix, Map<String, String> state)
	{
		this.projectDirectory = state.containsKey(prefix + "." + STATE_PROJECT_DIRECTORY) ? new File(state.get(prefix + "." + STATE_PROJECT_DIRECTORY)) : null;
	}

	/**
	 * Shuts down the agent, if running.
	 */
	public void shutDownAgent()
	{
		if (autoBuildAgent == null)
			return;

		autoBuildAgent.shutDown();
		autoBuildAgent = null;
	}
	
	// Starts the agent.
	private void startAgent()
	{
		if (autoBuildAgent != null)
			throw new IllegalStateException("INTERNAL ERROR: Start agent while agent running!");
		
		autoBuildAgent = new AutoBuildAgent(projectDirectory, new Listener() 
		{
			@Override
			public void onAgentStarted() 
			{
				LOG.info("Agent started: " + projectDirectory.getPath());
				updateTargetsEnabled(false);
			}
			
			@Override
			public void onAgentStartupException(String message, Exception exception) 
			{
				LOG.error("Agent startup error: " + message);
			}

			@Override
			public void onAgentStopped() 
			{
				LOG.info("Agent stopped: " + projectDirectory.getPath());
				updateTargetsEnabled(true);
			}

			@Override
			public void onAgentStoppedException(String message, Exception exception) 
			{
				LOG.error("Agent stop error: " + message);
			}

			@Override
			public void onBuildPrepared() 
			{
				LOG.info("Change detected. Build prepared.");
			}

			@Override
			public void onBuildStart() 
			{
				LOG.info("Build started.");
			}

			@Override
			public void onBuildEnd(int result) 
			{
				LOG.info("Build ended.");
			}
			
			@Override
			public int callBuild(String target)
			{
				try {
					statusPanel.setActivityMessage(language.getText("doommake.project.build.message.running", target));
					int result = appCommon.callDoomMake(projectDirectory, target, true, NO_ARGS, null, null, null).get();
					if (result != 0)
						statusPanel.setErrorMessage(language.getText("doommake.project.build.message.error"));
					else
						statusPanel.setSuccessMessage(language.getText("doommake.project.build.message.success"));
					return result;
				} catch (InterruptedException e) {
					LOG.warn("DoomMake call interrupted!");
					return -1;
				} catch (ExecutionException e) {
					LOG.error(e, "Exception occurred on DoomMake call!");
					return -1;
				}
			}
			
		});
		
		autoBuildAgent.start();
	}

	/**
	 * Sets the current target to execute.
	 * @param target the new target.
	 */
	private void setCurrentTarget(String target)
	{
		currentTarget = target;
		updateTargetsEnabled(currentTarget != null);
	}
	
	/**
	 * Runs the current target.
	 */
	private void runCurrentTarget()
	{
		// execution failsafe
		if (!targetRunAction.isEnabled())
			return;
		if (autoBuildAgent != null && autoBuildAgent.isRunning())
			return;
		if (currentTarget == null)
			return;
		
		appCommon.onExecuteDoomMake(this, outputPanel != null ? outputPanel : new DoomToolsTextOutputPanel(), statusPanel, outputPanel != null, projectDirectory, null, currentTarget, NO_ARGS, false,
			()->updateTargetsEnabled(false), ()->updateTargetsEnabled(true)
		);
	}

	private void updateTargetsEnabled(boolean enabled)
	{
		final boolean state = enabled && (autoBuildAgent == null || !autoBuildAgent.isRunning());
		SwingUtils.invoke(() -> {
			listPanel.setEnabled(state);
			targetRunAction.setEnabled(state);
		});
	}

}
