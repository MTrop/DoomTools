package net.mtrop.doom.tools.doommake.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.OSUtils;
import net.mtrop.doom.tools.struct.SingletonProvider;

/**
 * DoomMake GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class DoomMakeSettings 
{
	/** DoomMake Config folder base. */
    public static final String APPDATA_PATH = OSUtils.getApplicationSettingsPath() + "/DoomMake/";
	/** Settings filename. */
    private static final String SETTINGS_FILENAME = "settings.properties";
    /** Configuration file. */
    private static final File CONFIG_FILE = new File(APPDATA_PATH + SETTINGS_FILENAME);

    /** Logger. */
    private static final Logger LOG = DoomMakeLogger.getLogger(DoomMakeSettings.class); 
    
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomMakeSettings> INSTANCE = new SingletonProvider<>(() -> 
    {
    	DoomMakeSettings out = new DoomMakeSettings();
		if (!CONFIG_FILE.exists())
			LOG.infof("No settings file %s - using defaults.", CONFIG_FILE.getPath());
		else
			out.loadSettings();
		
		return out;
    });
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomMakeSettings get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
    private static final String DOOMMAKE_PATH_LAST_PROJECT = "doommake.path.lastProject";
    private static final String DOOMMAKE_PATH_SLADE = "doommake.path.slade";
    private static final String DOOMMAKE_PATH_VSCODE = "doommake.path.vscode";

	private Properties properties;
	
	private DoomMakeSettings()
	{
		this.properties = new Properties();
	}
	
	private void loadSettings()
	{
		try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) 
		{
			properties.load(fis);
			LOG.infof("Loaded settings from %s", CONFIG_FILE.getPath());
		}
		catch (FileNotFoundException e) 
		{
			LOG.errorf(e, "Could not load settings file from %s", CONFIG_FILE.getPath());
		} 
		catch (IOException e) 
		{
			LOG.errorf(e, "Could not load settings file from %s", CONFIG_FILE.getPath());
		}
	}
	
	private void saveSettings()
	{
		if (!Common.createPathForFile(CONFIG_FILE))
			return;
		
		try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE))
		{
			properties.store(fos, "Created by DoomMake " + Common.getVersionString("doommake"));
		} 
		catch (FileNotFoundException e) 
		{
			LOG.errorf(e, "Could not write settings to %s", CONFIG_FILE.getPath());
		} 
		catch (IOException e) 
		{
			LOG.errorf(e, "Could not write settings to %s", CONFIG_FILE.getPath());
		}
	}
	
	/**
	 * Sets the last project directory opened.
	 * @param lastProjectDirectory the last project directory.
	 */
	public void setLastProjectDirectory(File lastProjectDirectory) 
	{
		properties.setProperty(DOOMMAKE_PATH_LAST_PROJECT, lastProjectDirectory.getAbsolutePath());
		saveSettings();
	}

	/**
	 * @return the last project directory opened.
	 */
	public File getLastProjectDirectory() 
	{
		String path = properties.getProperty(DOOMMAKE_PATH_LAST_PROJECT);
		return path != null ? new File(path) : null;
	}

	/**
	 * Sets the path to SLADE.
	 * @param path the executable path.
	 */
	public void setPathToSlade(File path) 
	{
		properties.setProperty(DOOMMAKE_PATH_SLADE, path.getAbsolutePath());
		saveSettings();
	}

	/**
	 * @return the executable path to SLADE.
	 */
	public File getPathToSlade() 
	{
		String path = properties.getProperty(DOOMMAKE_PATH_SLADE);
		return path != null ? new File(path) : null;
	}

	/**
	 * Sets the path to VSCode.
	 * @param path the executable path.
	 */
	public void setPathToVSCode(File path) 
	{
		properties.setProperty(DOOMMAKE_PATH_VSCODE, path.getAbsolutePath());
		saveSettings();
	}

	/**
	 * @return the executable path to VSCode.
	 */
	public File getPathToVSCode() 
	{
		String path = properties.getProperty(DOOMMAKE_PATH_VSCODE);
		return path != null ? new File(path) : null;
	}

}
