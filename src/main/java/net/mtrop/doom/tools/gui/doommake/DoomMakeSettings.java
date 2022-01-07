package net.mtrop.doom.tools.gui.doommake;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.DoomToolsLogger;
import net.mtrop.doom.tools.gui.doommake.DoomMakeConstants.Paths;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.SingletonProvider;

/**
 * DoomMake GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class DoomMakeSettings 
{
    /** Settings filename. */
    private static final String SETTINGS_FILENAME = "settings.properties";
    /** Configuration file. */
    private static final File CONFIG_FILE = new File(Paths.APPDATA_PATH + SETTINGS_FILENAME);

    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomMakeSettings.class); 
    
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
			LOG.infof("Saved DoomMake settings to %s.", CONFIG_FILE.getPath());
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
	 * @param path the last project directory.
	 */
	public void setLastProjectDirectory(File path) 
	{
		properties.setProperty(DOOMMAKE_PATH_LAST_PROJECT, path != null ? path.getAbsolutePath() : "");
		saveSettings();
	}

	/**
	 * @return the last project directory opened.
	 */
	public File getLastProjectDirectory() 
	{
		String path = properties.getProperty(DOOMMAKE_PATH_LAST_PROJECT);
		return path != null && path.length() >= 0 ? new File(path) : null;
	}

	/**
	 * Sets the path to SLADE.
	 * @param path the executable path.
	 */
	public void setPathToSlade(File path) 
	{
		properties.setProperty(DOOMMAKE_PATH_SLADE, path != null ? path.getAbsolutePath() : "");
		saveSettings();
	}

	/**
	 * @return the executable path to SLADE.
	 */
	public File getPathToSlade() 
	{
		String path = properties.getProperty(DOOMMAKE_PATH_SLADE);
		return path != null && path.length() >= 0 ? new File(path) : null;
	}

	/**
	 * Sets the path to VSCode.
	 * @param path the executable path.
	 */
	public void setPathToVSCode(File path) 
	{
		properties.setProperty(DOOMMAKE_PATH_VSCODE, path != null ? path.getAbsolutePath() : "");
		saveSettings();
	}

	/**
	 * @return the executable path to VSCode.
	 */
	public File getPathToVSCode() 
	{
		String path = properties.getProperty(DOOMMAKE_PATH_VSCODE);
		return path != null && path.length() >= 0 ? new File(path) : null;
	}

}
