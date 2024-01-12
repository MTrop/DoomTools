/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers.settings;

import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;

import net.mtrop.doom.tools.gui.DoomToolsSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.struct.SingletonProvider;


/**
 * DoomTools GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class DoomToolsSettingsManager extends DoomToolsSettings
{
	/** Settings filename. */
    private static final String SETTINGS_FILENAME = "settings.properties";

    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsSettingsManager> INSTANCE = new SingletonProvider<>(() -> new DoomToolsSettingsManager());
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsSettingsManager get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
    private static final String DOOMTOOLS_THEME = "theme";
    private static final String PATH_LAST_PROJECT = "path.lastProject";
    private static final String PATH_LAST_SAVE = "path.lastSave";

	/* ==================================================================== */

	private DoomToolsSettingsManager()
	{
		super(getConfigFile(SETTINGS_FILENAME), DoomToolsLogger.getLogger(DoomToolsSettingsManager.class));
	}
	
	/**
	 * Sets the theme to use for the GUI.
	 * @param name the name of the theme.
	 */
	public void setThemeName(String name)
	{
		setString(DOOMTOOLS_THEME, name);
		commit();
	}
	
	/**
	 * @return the theme to use for the GUI.
	 */
	public String getThemeName()
	{
		return getString(DOOMTOOLS_THEME, "LIGHT");
	}
	
	/**
	 * Sets window bounds.
	 * @param window the window to get size from.
	 */
	public void setBounds(Frame window)
	{
		setFrameBounds("default", window.getX(), window.getY(), window.getWidth(), window.getHeight(), (window.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0);
		commit();
	}
	
	/**
	 * Gets window bounds.
	 * @return the bounds.
	 */
	public Rectangle getBounds()
	{
		return getFrameBounds("default");
	}
	
	/**
	 * @return if the main DoomTools window should be maximized.
	 */
	public boolean getBoundsMaximized()
	{
		return getFrameMaximized("default");
	}

	/**
	 * Sets the last project directory opened.
	 * @param path the last project directory.
	 */
	public void setLastProjectDirectory(File path) 
	{
		setFile(PATH_LAST_PROJECT, path);
		commit();
	}

	/**
	 * @return the last project directory opened.
	 */
	public File getLastProjectDirectory() 
	{
		return getFile(PATH_LAST_PROJECT);
	}

	/**
	 * Sets the last file saved.
	 * @param path the last file saved.
	 */
	public void setLastFileSave(File path) 
	{
		setFile(PATH_LAST_SAVE, path);
		commit();
	}

	/**
	 * @return the last file saved.
	 */
	public File getLastFileSave() 
	{
		return getFile(PATH_LAST_SAVE);
	}

}
