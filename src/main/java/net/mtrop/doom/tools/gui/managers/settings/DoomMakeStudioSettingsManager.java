/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
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
 * Doom Make GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class DoomMakeStudioSettingsManager extends DoomToolsSettings
{
	/** Settings filename. */
    private static final String SETTINGS_FILENAME = "doommake-studio.properties";

    /** The instance encapsulator. */
    private static final SingletonProvider<DoomMakeStudioSettingsManager> INSTANCE = new SingletonProvider<>(() -> new DoomMakeStudioSettingsManager());
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomMakeStudioSettingsManager get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
    private static final String DOOMMAKE_PATH_LAST_PROJECT = "path.lastProject";
    private static final String SIDEBAR_WIDTH = "sidebar.width";
    private static final String TREE_HEIGHT = "tree.height";

	/* ==================================================================== */

	private DoomMakeStudioSettingsManager()
	{
		super(getConfigFile(SETTINGS_FILENAME), DoomToolsLogger.getLogger(DoomMakeStudioSettingsManager.class));
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
		setFile(DOOMMAKE_PATH_LAST_PROJECT, path);
		commit();
	}

	/**
	 * @return the last project directory opened.
	 */
	public File getLastProjectDirectory() 
	{
		return getFile(DOOMMAKE_PATH_LAST_PROJECT);
	}

	/**
	 * Sets sidebar panel width.
	 * @param width the width in pixels.
	 */
	public void setHorizontalDividerWidth(int width)
	{
		setInteger(SIDEBAR_WIDTH, width);
		commit();
	}
	
	/**
	 * Gets sidebar panel width.
	 * @return the width.
	 */
	public int getHorizontalDividerWidth()
	{
		return getInteger(SIDEBAR_WIDTH, 250);
	}
		
	/**
	 * Sets tree panel height.
	 * @param width the width in pixels.
	 */
	public void setVerticalDividerHeight(int width)
	{
		setInteger(TREE_HEIGHT, width);
		commit();
	}
	
	/**
	 * Gets tree panel height.
	 * @return the width.
	 */
	public int getVerticalDividerHeight()
	{
		return getInteger(TREE_HEIGHT, 350);
	}
	
	
	
	
		
	

}
