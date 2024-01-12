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
 * WadScript GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class DecoHackSettingsManager extends DoomToolsSettings
{
	/** Settings filename. */
    private static final String SETTINGS_FILENAME = "decohack.properties";

    /** The instance encapsulator. */
    private static final SingletonProvider<DecoHackSettingsManager> INSTANCE = new SingletonProvider<>(() -> new DecoHackSettingsManager());
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DecoHackSettingsManager get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
    private static final String PATH_LAST_FILE = "path.lastFile";
    private static final String EXPORT_PATH_LAST_FILE = "path.export.lastFile";
    private static final String EXPORT_SOURCE_PATH_LAST_FILE = "path.export.source.lastFile";
    private static final String TREE_WIDTH = "tree.width";

	/* ==================================================================== */

	private DecoHackSettingsManager()
	{
		super(getConfigFile(SETTINGS_FILENAME), DoomToolsLogger.getLogger(DecoHackSettingsManager.class));
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
	 * Sets tree panel width.
	 * @param width the width in pixels.
	 */
	public void setTreeWidth(int width)
	{
		setInteger(TREE_WIDTH, width);
		commit();
	}
	
	/**
	 * Gets tree panel width.
	 * @return the width.
	 */
	public int getTreeWidth()
	{
		return getInteger(TREE_WIDTH, 250);
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

}
