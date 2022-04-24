package net.mtrop.doom.tools.gui.managers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.fife.ui.rsyntaxtextarea.Theme;

import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.DoomToolsConstants.Paths;
import net.mtrop.doom.tools.gui.apps.swing.editors.MultiFileEditorPanel.EditorViewSettings;
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

    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsSettingsManager.class); 
    
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

    private static final String EDITOR_VIEW = ".editor.view";

	/* ==================================================================== */

    private Properties properties;
	
	private DoomToolsSettingsManager()
	{
		this.properties = new Properties();
		loadProperties();
	}
	
	private void loadProperties()
	{
		File propertiesFile = getConfigFile(SETTINGS_FILENAME);
		try (FileInputStream fis = new FileInputStream(propertiesFile)) 
		{
			properties.load(fis);
			LOG.infof("Loaded settings from %s", propertiesFile.getPath());
		}
		catch (FileNotFoundException e) 
		{
			LOG.errorf(e, "Could not load settings file from %s", propertiesFile.getPath());
		} 
		catch (IOException e) 
		{
			LOG.errorf(e, "Could not load settings file from %s", propertiesFile.getPath());
		}
	}
	
	private void saveProperties()
	{
		File propertiesFile = getConfigFile(SETTINGS_FILENAME);
		if (!Common.createPathForFile(propertiesFile))
			return;
		
		try (FileOutputStream fos = new FileOutputStream(propertiesFile))
		{
			properties.store(fos, "Created by DoomTools " + Version.DOOMTOOLS);
			LOG.infof("Saved DoomTools settings to %s.", propertiesFile.getPath());
		} 
		catch (FileNotFoundException e) 
		{
			LOG.errorf(e, "Could not write settings to %s", propertiesFile.getPath());
		} 
		catch (IOException e) 
		{
			LOG.errorf(e, "Could not write settings to %s", propertiesFile.getPath());
		}
	}

	/**
	 * Fetches a file relative to the settings path.
	 * NOTE: The file/dir may not actually exist!
	 * @param path the desired path.
	 * @return a File representing that new path.
	 */
	public File getConfigFile(String path)
	{
		return new File(Paths.APPDATA_PATH + SETTINGS_FILENAME);
	}
	
	/**
	 * Sets the theme to use for the GUI.
	 * @param name the name of the theme.
	 */
	public void setThemeName(String name)
	{
		properties.setProperty(DOOMTOOLS_THEME, name);
		saveProperties();
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
		saveProperties();
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
	 * @param keyName an associated key.
	 * @param path the last path.
	 */
	public void setLastPath(String keyName, File path) 
	{
		properties.setProperty(DOOMTOOLS_LAST_PATH + "." + keyName, path != null ? path.getAbsolutePath() : "");
		saveProperties();
	}

	/**
	 * @param keyName an associated key.
	 * @return the last path touched.
	 */
	public File getLastPath(String keyName) 
	{
		String path = properties.getProperty(DOOMTOOLS_LAST_PATH + "." + keyName);
		return path != null && path.length() >= 0 ? new File(path) : null;
	}

	/**
	 * Sets the last project directory opened.
	 * @param path the last project directory.
	 */
	public void setLastProjectDirectory(File path) 
	{
		properties.setProperty(DOOMMAKE_PATH_LAST_PROJECT, path != null ? path.getAbsolutePath() : "");
		saveProperties();
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
		saveProperties();
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
		saveProperties();
	}

	/**
	 * @return the executable path to VSCode.
	 */
	public File getPathToVSCode() 
	{
		String path = properties.getProperty(DOOMMAKE_PATH_VSCODE);
		return path != null && path.length() >= 0 ? new File(path) : null;
	}

	/**
	 * Saves a set of editor view settings.
	 * @param subsetName the editor sub-set name.
	 * @param viewSettings the settings object.
	 */
	public void setEditorViewSettings(String subsetName, EditorViewSettings viewSettings)
	{
		String prefix = subsetName + EDITOR_VIEW;
		
		properties.setProperty(prefix + ".tabsize", String.valueOf(viewSettings.getTabSize()));
		properties.setProperty(prefix + ".softtabs", String.valueOf(viewSettings.isTabsEmulated()));
		properties.setProperty(prefix + ".wrapping", String.valueOf(viewSettings.isLineWrap()));
		properties.setProperty(prefix + ".wrapwords", String.valueOf(viewSettings.isWrapStyleWord()));
		
		saveProperties();
	}
	
	/**
	 * Gets a set of editor view settings.
	 * @param subsetName the editor sub-set name.
	 * @return the settings object.
	 */
	public EditorViewSettings getEditorViewSettings(String subsetName)
	{
		EditorViewSettings out = new EditorViewSettings();
		String prefix = subsetName + EDITOR_VIEW;
		
		out.setTabSize(ValueUtils.parseInt(properties.getProperty(prefix + ".tabsize"), out.getTabSize()));
		out.setTabsEmulated(ValueUtils.parseBoolean(properties.getProperty(prefix + ".softtabs"), out.isTabsEmulated()));
		out.setLineWrap(ValueUtils.parseBoolean(properties.getProperty(prefix + ".wrapping"), out.isLineWrap()));
		out.setWrapStyleWord(ValueUtils.parseBoolean(properties.getProperty(prefix + ".wrapwords"), out.isWrapStyleWord()));
		
		return out;
	}
	
	/**
	 * Sets an editor theme's settings.
	 * @param subsetName the editor sub-set name.
	 * @param editorTheme the theme to store.
	 */
	public void setEditorTheme(String subsetName, Theme editorTheme)
	{
		// TODO: Save as properties since the Theme export stuff is bugged.
	}
	
	/**
	 * Sets an editor theme's settings.
	 * @param subsetName the editor sub-set name.
	 * @return the corresponding theme, or null if no corresponding theme.
	 */
	public Theme getEditorTheme(String subsetName)
	{
		// TODO: Load from properties since the Theme export stuff is bugged.
		return null;
	}
	
	private static String fontToString(Font font)
	{
		return font.getName() + ":" + font.getStyle() + ":" + font.getSize();
	}

	private static Font stringToFont(String font)
	{
		String[] segs = font.split(":");
		String name = segs[0];
		int style = ValueUtils.parseInt(segs[1], Font.PLAIN);
		int size = ValueUtils.parseInt(segs[2], 12);
		return new Font(name, style, size);
	}

	private static String colorToString(Color c) 
	{
        StringBuilder sb = new StringBuilder(Integer.toHexString(c.getRGB() & 0x00ffffff));
		while (sb.length() < 6) 
            sb.insert(0, "0");
		return sb.toString();
	}
	
	private static Color stringToColor(String str)
	{
		return new Color(Integer.parseInt(str, 16));
	}
	
}
