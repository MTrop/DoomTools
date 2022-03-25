package net.mtrop.doom.tools.gui.managers;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.DoomToolsConstants.Paths;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.util.ValueUtils;

/**
 * DoomMake GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class DoomToolsSettingsManager 
{
    /** Settings filename. */
    private static final String SETTINGS_FILENAME = "settings.properties";
    /** Configuration file. */
    private static final File CONFIG_FILE = new File(Paths.APPDATA_PATH + SETTINGS_FILENAME);

    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsSettingsManager.class); 
    
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsSettingsManager> INSTANCE = new SingletonProvider<>(() -> 
    {
    	DoomToolsSettingsManager out = new DoomToolsSettingsManager();
		if (!CONFIG_FILE.exists())
			LOG.infof("No settings file %s - using defaults.", CONFIG_FILE.getPath());
		else
			out.loadSettings();
		
		return out;
    });
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsSettingsManager get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
    private static final String DOOMTOOLS_THEME = "doomtools.theme";
    private static final String DOOMTOOLS_WINDOW_X = "doomtools.window.x";
    private static final String DOOMTOOLS_WINDOW_Y = "doomtools.window.y";
    private static final String DOOMTOOLS_WINDOW_WIDTH = "doomtools.window.width";
    private static final String DOOMTOOLS_WINDOW_HEIGHT = "doomtools.window.height";
    private static final String DOOMTOOLS_WINDOW_MAXIMIZED = "doomtools.window.max";
    private static final String DOOMTOOLS_LAST_PATH = "doomtools.lastPath";
    private static final String DOOMMAKE_PATH_LAST_PROJECT = "doommake.path.lastProject";
    private static final String DOOMMAKE_PATH_SLADE = "doommake.path.slade";
    private static final String DOOMMAKE_PATH_VSCODE = "doommake.path.vscode";

	private Properties properties;
	
	private DoomToolsSettingsManager()
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
			properties.store(fos, "Created by DoomTools " + Version.DOOMTOOLS);
			LOG.infof("Saved DoomTools settings to %s.", CONFIG_FILE.getPath());
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
	 * Sets the theme to use for the GUI.
	 * @param name the name of the theme.
	 */
	public void setThemeName(String name)
	{
		properties.setProperty(DOOMTOOLS_THEME, name);
		saveSettings();
	}
	
	/**
	 * @return the theme to use for the GUI.
	 */
	public String getThemeName()
	{
		return properties.getProperty(DOOMTOOLS_THEME, "LIGHT");
	}
	
	/**
	 * Sets the main DoomTools window bounds.
	 * @param x 
	 * @param y 
	 * @param width 
	 * @param height 
	 * @param maximized if window was maximized
	 */
	public void setWindowBounds(int x, int y, int width, int height, boolean maximized) 
	{
		properties.setProperty(DOOMTOOLS_WINDOW_X, String.valueOf(x));
		properties.setProperty(DOOMTOOLS_WINDOW_Y, String.valueOf(y));
		properties.setProperty(DOOMTOOLS_WINDOW_WIDTH, String.valueOf(width));
		properties.setProperty(DOOMTOOLS_WINDOW_HEIGHT, String.valueOf(height));
		properties.setProperty(DOOMTOOLS_WINDOW_MAXIMIZED, String.valueOf(maximized));
		saveSettings();
	}

	/**
	 * Gets the main DoomTools window bounds.
	 * @return the bounds.
	 */
	public Rectangle getWindowBounds()
	{
		return new Rectangle(
			ValueUtils.parseInt(properties.getProperty(DOOMTOOLS_WINDOW_X), 0),
			ValueUtils.parseInt(properties.getProperty(DOOMTOOLS_WINDOW_Y), 0),
			ValueUtils.parseInt(properties.getProperty(DOOMTOOLS_WINDOW_WIDTH), 720),
			ValueUtils.parseInt(properties.getProperty(DOOMTOOLS_WINDOW_HEIGHT), 480)
		);
	}
	
	/**
	 * @return if the main DoomTools window should be maximized.
	 */
	public boolean getWindowMaximized()
	{
		return ValueUtils.parseBoolean(properties.getProperty(DOOMTOOLS_WINDOW_MAXIMIZED), false);
	}
	
	/**
	 * Sets the last path touched.
	 * @param path the last path.
	 */
	public void setLastPath(File path) 
	{
		properties.setProperty(DOOMTOOLS_LAST_PATH, path != null ? path.getAbsolutePath() : "");
		saveSettings();
	}

	/**
	 * @return the last path touched.
	 */
	public File getLastPath() 
	{
		String path = properties.getProperty(DOOMTOOLS_LAST_PATH);
		return path != null && path.length() >= 0 ? new File(path) : null;
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
