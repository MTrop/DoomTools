/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui;

import java.awt.Container;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.gui.managers.AppCommon;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;

/**
 * Interface for assembling the GUI part of a DoomTools application instance and 
 * listening to environment events.
 * Can be created as a JFrame or a JInternalFrame.
 * <p> Because of how workspace saving works, all applications must have a default constructor.
 * @author Matthew Tropiano
 */
public abstract class DoomToolsApplicationInstance
{
	/** Doom Tools GUI Utility singleton. */
	protected final DoomToolsGUIUtils utils;
	/** Doom Tools Language singleton. */
	protected final DoomToolsLanguageManager language;
	/** Application Commons singleton. */
	protected final AppCommon appCommon;
	
	/** The listener set on all apps. */
	private DoomToolsApplicationListener listener;
	
	protected DoomToolsApplicationInstance()
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.appCommon = AppCommon.get();
		this.listener = null; // set later
	}
	
	/**
	 * @return this application's name.
	 */
	public abstract String getTitle();
	
	/**
	 * Fetches this application instance's content pane.
	 * This is only called once, and is intended to be the function that constructs
	 * the main component for the application's GUI.
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's content pane.
	 */
	public abstract Container createContentPane();

	/**
	 * Sets the application listener.
	 * Called when the application gets attached to a window, but before it is shown.
	 * <p> This should NEVER be called by the application itself.
	 * @param receiver the receiver instance.
	 */
	public final void setApplicationListener(DoomToolsApplicationListener receiver)
	{
		this.listener = receiver;
	}
	
	/**
	 * Fetches this application instance's window frame icons.
	 * This is only called once, and is intended to be the function that gets the application's icon.
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's icon, or <code>null</code> for default.
	 */
	public Icon getIcon()
	{
		return null;
	}

	/**
	 * Fetches this application instance's menu bar, if used as a desktop frame.
	 * This is only called once per application creation, and is intended to be the function that constructs
	 * the menu bar for the application's GUI.
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's menu bar. May return <code>null</code> for no bar.
	 */
	public JMenuBar createDesktopMenuBar()
	{
		return null;
	}

	/**
	 * Fetches this application instance's menu bar, if used as an internal frame.
	 * This is only called once per application creation, and is intended to be the function that constructs
	 * the menu bar for the application's GUI.
	 * <p> This should NEVER be called by the application itself.
	 * @return the application's menu bar. May return <code>null</code> for no bar.
	 */
	public JMenuBar createInternalMenuBar()
	{
		return null;
	}

	/**
	 * Attempts to close this application programmatically.
	 */
	public void attemptClose()
	{
		if (listener != null)
			listener.attemptClose();
	}
	
	/**
	 * Starts a new application.
	 * @param instance the application instance to start.
	 */
	public void startApplication(DoomToolsApplicationInstance instance)
	{
		if (listener != null)
			listener.startApplication(instance);
	}
	
	/**
	 * @return the parent application container component.
	 */
	public Container getApplicationContainer()
	{
		return listener != null ? listener.getApplicationContainer() : null;
	}
	
	/**
	 * Puts the current state of this application into a state object for persisting workspaces. 
	 * It is up to the application to figure out what to store.
	 * By default, this returns an empty map.
	 * <p> DO NOT store the content pane bounds - this is stored on the workspace.
	 * <p> Applications that override this method should call the parent method via <code>super</code> first!
	 * <p> This should NEVER be called by the application itself.
	 * @return this application's state properties (if any - can be null).
	 * @see #setApplicationState(Map)
	 */
	public Map<String, String> getApplicationState()
	{
		return new TreeMap<>();
	}

	/**
	 * Sets this application's state via a mapping of keys and values, presumably associated with the application. 
	 * Called before it is started, if restoring from a workspace.
	 * Applications that override this method should call the parent method via <code>super</code> first!
	 * <p> This should NEVER be called by the application itself.
	 * @param state the state object.
	 * @see #getApplicationState()
	 */
	public void setApplicationState(Map<String, String> state)
	{
		// Do nothing.
	}

	/**
	 * Called when the application is created but not made visible yet.
	 * If any resizing needs to be done to the frame that houses this application or any other
	 * tasks before it is shown, this would be the place to do it.
	 * <p> Assume that the application is still headless during this method.
	 * <p> This should NEVER be called by the application itself.
	 * @param frame the source object that was "created" - which is usually a window container of some kind.
	 */
	public void onCreate(Object frame)
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is opened/made visible.
	 * The parent frame that houses this application is made visible by the time this is called.
	 * <p> This should NEVER be called by the application itself.
	 * @param frame the source object that "opened" - which is usually a window container of some kind.
	 */
	public void onOpen(Object frame)
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is closed (after {@link #shouldClose()} is called and returns true).
	 * The parent frame that houses this application is hidden by the time this is called.
	 * <p> This should NEVER be called by the application itself.
	 * @param frame the source object that "closed" - which is usually a window container of some kind.
	 */
	public void onClose(Object frame)
	{
		// Do nothing.
	}

	/**
	 * Called when the application is about to close. 
	 * By default, this returns true. You may ask the user if they wish to close it, here.
	 * <p> This should NEVER be called by the application itself.
	 * @return true if the application should close, false if not.
	 */
	public boolean shouldClose()
	{
		return true;
	}

	/**
	 * Called when the application is minimized.
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onMinimize()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is restored from iconification.
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onRestore()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is focused.
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onFocus()
	{
		// Do nothing.
	}
	
	/**
	 * Called when the application is unfocused.
	 * <p> This should NEVER be called by the application itself.
	 */
	public void onBlur()
	{
		// Do nothing.
	}

}
