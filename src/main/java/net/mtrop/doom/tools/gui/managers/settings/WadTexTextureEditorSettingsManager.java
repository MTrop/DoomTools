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
 * WadTex Texture Editor GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class WadTexTextureEditorSettingsManager extends DoomToolsSettings
{
	/** Settings filename. */
	private static final String SETTINGS_FILENAME = "wadtex-texture.properties";

	/** The instance encapsulator. */
	private static final SingletonProvider<WadTexTextureEditorSettingsManager> INSTANCE = new SingletonProvider<>(() -> new WadTexTextureEditorSettingsManager());
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static WadTexTextureEditorSettingsManager get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
	private static final String PATH_LAST_FILE = "path.lastFile";
	private static final String PATH_LAST_WAD_OPEN = "path.lastWAD.open";

	/* ==================================================================== */

	private WadTexTextureEditorSettingsManager()
	{
		super(getConfigFile(SETTINGS_FILENAME), DoomToolsLogger.getLogger(WadTexTextureEditorSettingsManager.class));
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

}
