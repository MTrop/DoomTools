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
 * WadTex GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class WadTexSettingsManager extends DoomToolsSettings
{
	/** Settings filename. */
    private static final String SETTINGS_FILENAME = "wadtex.properties";

    /** The instance encapsulator. */
    private static final SingletonProvider<WadTexSettingsManager> INSTANCE = new SingletonProvider<>(() -> new WadTexSettingsManager());
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static WadTexSettingsManager get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
    private static final String PATH_LAST_FILE = "path.lastFile";
    private static final String PATH_LAST_WAD_OPEN = "path.lastWAD.open";
    private static final String EXPORT_PATH_LAST_FILE = "path.export.lastFile";
    private static final String EXPORT_SOURCE_PATH_LAST_FILE = "path.export.source.lastFile";
    private static final String SHOW_BUDGET = "show.budget";

	/* ==================================================================== */

	private WadTexSettingsManager()
	{
		super(getConfigFile(SETTINGS_FILENAME), DoomToolsLogger.getLogger(WadTexSettingsManager.class));
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
	 * Sets the last file opened or saved.
	 * @param path the file.
	 */
	public void setLastTouchedFile(File path) 
	{
		setFile(PATH_LAST_FILE, path);
		commit();
	}

	/**
	 * @return the last file opened or saved.
	 */
	public File getLastTouchedFile() 
	{
		return getFile(PATH_LAST_FILE);
	}

	/**
	 * Sets the last WAD opened.
	 * @param path the file.
	 */
	public void setLastOpenedWAD(File path) 
	{
		setFile(PATH_LAST_WAD_OPEN, path);
		commit();
	}

	/**
	 * @return the last WAD opened.
	 */
	public File getLastOpenedWAD() 
	{
		return getFile(PATH_LAST_WAD_OPEN);
	}

	/**
	 * Sets the last file opened or saved on export.
	 * @param path the file.
	 */
	public void setLastExportFile(File path) 
	{
		setFile(EXPORT_PATH_LAST_FILE, path);
		commit();
	}

	/**
	 * @return the last file opened or saved on export.
	 */
	public File getLastExportFile() 
	{
		return getFile(EXPORT_PATH_LAST_FILE);
	}

	/**
	 * Sets the last file opened or saved on export for source.
	 * @param path the file.
	 */
	public void setLastExportSourceFile(File path) 
	{
		setFile(EXPORT_SOURCE_PATH_LAST_FILE, path);
		commit();
	}

	/**
	 * @return the last file opened or saved on export for source.
	 */
	public File getLastExportSourceFile() 
	{
		return getFile(EXPORT_SOURCE_PATH_LAST_FILE);
	}

	/**
	 * Sets the last state of "showing the budget".
	 * @param enabled the enabled state.
	 */
	public void setShowBudgetDefault(boolean enabled) 
	{
		setBoolean(SHOW_BUDGET, enabled);
		commit();
	}

	/**
	 * @return the last state of "showing the budget".
	 */
	public boolean getShowBudgetDefault() 
	{
		return getBoolean(SHOW_BUDGET);
	}

}
