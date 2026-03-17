/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers.settings;

import java.io.File;

import net.mtrop.doom.tools.gui.DoomToolsSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.EnumUtils;


/**
 * Doom Make GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class DoomMakeSettingsManager extends DoomToolsSettings
{
	/** Settings filename. */
	private static final String SETTINGS_FILENAME = "doommake.properties";

	/** The instance encapsulator. */
	private static final SingletonProvider<DoomMakeSettingsManager> INSTANCE = new SingletonProvider<>(() -> new DoomMakeSettingsManager());
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomMakeSettingsManager get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
	private static final String PATH_LAST_FILE = "path.lastFile";
	private static final String DOOMMAKE_PATH_LAST_PROJECT = "path.lastProject";
	private static final String DOOMMAKE_PATH_SLADE = "path.slade";
	private static final String DOOMMAKE_PATH_IDE = "path.vscode";
	private static final String DOOMMAKE_PATH_MAPEDITOR = "path.map.editor";
	private static final String DOOMMAKE_SOUND_BUILD_SUCCESS = "sound.build.success";
	private static final String DOOMMAKE_SOUND_BUILD_FAILURE = "sound.build.failure";

	/* ==================================================================== */

	public enum SoundType
	{
		NONE
		{
			@Override
			public void play() 
			{
				// Do nothing.
			}
		},
		
		DEFAULT
		{
			@Override
			public void play() 
			{
				SwingUtils.soundDefault();
			}
		},

		ASTERISK
		{
			@Override
			public void play() 
			{
				SwingUtils.soundAsterisk();
			}
		},

		EXCLAMATION
		{
			@Override
			public void play() 
			{
				SwingUtils.soundExclamation();
			}
		},

		QUESTION
		{
			@Override
			public void play() 
			{
				SwingUtils.soundQuestion();
			}
		},
		;
		
		/**
		 * Plays the sound.
		 */
		public abstract void play();
	}
	
	private DoomMakeSettingsManager()
	{
		super(getConfigFile(SETTINGS_FILENAME), DoomToolsLogger.getLogger(DoomMakeSettingsManager.class));
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
	 * Sets the path to SLADE.
	 * @param path the executable path.
	 */
	public void setPathToSlade(File path) 
	{
		setFile(DOOMMAKE_PATH_SLADE, path);
		commit();
	}

	/**
	 * @return the executable path to SLADE.
	 */
	public File getPathToSlade() 
	{
		return getFile(DOOMMAKE_PATH_SLADE);
	}

	/**
	 * Sets the path to IDE.
	 * @param path the executable path.
	 */
	public void setPathToIDE(File path) 
	{
		setFile(DOOMMAKE_PATH_IDE, path);
		commit();
	}

	/**
	 * @return the executable path to IDE.
	 */
	public File getPathToIDE() 
	{
		return getFile(DOOMMAKE_PATH_IDE);
	}
	
	/**
	 * Sets the path to the map editor.
	 * @param path the executable path.
	 */
	public void setPathToMapEditor(File path) 
	{
		setFile(DOOMMAKE_PATH_MAPEDITOR, path);
		commit();
	}

	/**
	 * @return the executable path to the map editor.
	 */
	public File getPathToMapEditor() 
	{
		return getFile(DOOMMAKE_PATH_MAPEDITOR);
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
	 * Sets the build success sound.
	 * @param type the build success sound.
	 */
	public void setBuildSuccessSound(SoundType type)
	{
		setString(DOOMMAKE_SOUND_BUILD_SUCCESS, type.name());
		commit();
	}
	
	/**
	 * @return the build success sound.
	 */
	public SoundType getBuildSuccessSound()
	{
		String sound = getString(DOOMMAKE_SOUND_BUILD_SUCCESS);
		return sound != null ? EnumUtils.getEnumInstance(sound, SoundType.class) : SoundType.NONE;
	}
	
	/**
	 * Sets the build failure sound.
	 * @param type the build failure sound.
	 */
	public void setBuildFailureSound(SoundType type)
	{
		setString(DOOMMAKE_SOUND_BUILD_FAILURE, type.name());
		commit();
	}
	
	/**
	 * @return the build failure sound.
	 */
	public SoundType getBuildFailureSound()
	{
		String sound = getString(DOOMMAKE_SOUND_BUILD_FAILURE);
		return sound != null ? EnumUtils.getEnumInstance(sound, SoundType.class) : SoundType.NONE;
	}
	
}
