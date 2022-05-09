package net.mtrop.doom.tools.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.fife.ui.rsyntaxtextarea.Theme;

import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.gui.DoomToolsConstants.Paths;
import net.mtrop.doom.tools.gui.apps.swing.panels.MultiFileEditorPanel.EditorViewSettings;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

/**
 * Doom Tools settings handler.
 * @author Matthew Tropiano
 */
public class DoomToolsSettings
{
    private static final String EDITOR_VIEW = ".editor.view";
    private static final String WINDOW_X = ".window.x";
    private static final String WINDOW_Y = ".window.y";
    private static final String WINDOW_WIDTH = ".window.width";
    private static final String WINDOW_HEIGHT = ".window.height";
    private static final String WINDOW_MAXIMIZED = ".window.max";

    private File propertiesFile;
    private Logger logger;
    private Properties properties;
	
    /**
     * 
     * @param propertiesFilePath
     * @param logger
     */
	public DoomToolsSettings(File propertiesFilePath, Logger logger)
	{
		this.propertiesFile = propertiesFilePath;
		this.properties = new Properties();
		this.logger = logger;
		loadProperties();
	}
	
	/**
	 * Fetches a file relative to the settings path.
	 * NOTE: The file/dir may not actually exist!
	 * @param path the desired path.
	 * @return a File representing that new path.
	 */
	protected static File getConfigFile(String path)
	{
		return new File(Paths.APPDATA_PATH + path);
	}
	
	private void loadProperties()
	{
		try (FileInputStream fis = new FileInputStream(propertiesFile)) 
		{
			properties.load(fis);
			logger.infof("Loaded settings from %s", propertiesFile.getPath());
		}
		catch (FileNotFoundException e) 
		{
			logger.warnf("Could not load settings file from %s", propertiesFile.getPath());
		} 
		catch (IOException e) 
		{
			logger.errorf(e, "Could not load settings file from %s", propertiesFile.getPath());
		}
	}
	
	/**
	 * Saves the settings to storage.
	 */
	protected void commit()
	{
		if (!FileUtils.createPathForFile(propertiesFile))
			return;
		
		try (FileOutputStream fos = new FileOutputStream(propertiesFile))
		{
			properties.store(fos, "Created by DoomTools " + Version.DOOMTOOLS);
			logger.infof("Saved settings to %s.", propertiesFile.getPath());
		} 
		catch (FileNotFoundException e) 
		{
			logger.errorf(e, "Could not write settings to %s", propertiesFile.getPath());
		} 
		catch (IOException e) 
		{
			logger.errorf(e, "Could not write settings to %s", propertiesFile.getPath());
		}
	}

	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setString(String keyName, String value)
	{
		properties.setProperty(keyName, value);
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected String getString(String keyName, String defaultValue)
	{
		return properties.getProperty(keyName, defaultValue);
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected String getString(String keyName)
	{
		return getString(keyName, null);
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setInteger(String keyName, int value)
	{
		properties.setProperty(keyName, String.valueOf(value));
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected int getInteger(String keyName, int defaultValue)
	{
		String value = getString(keyName);
		return ValueUtils.parseInt(value, defaultValue);
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected Integer getInteger(String keyName)
	{
		return ValueUtils.parseInt(getString(keyName));
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setBoolean(String keyName, boolean value)
	{
		properties.setProperty(keyName, String.valueOf(value));
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected boolean getBoolean(String keyName, boolean defaultValue)
	{
		String value = getString(keyName);
		return ValueUtils.parseBoolean(value, defaultValue);
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected Boolean getBoolean(String keyName)
	{
		return ValueUtils.parseBoolean(getString(keyName));
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setFile(String keyName, File value)
	{
		properties.setProperty(keyName, value != null ? value.getAbsolutePath() : "");
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected File getFile(String keyName, File defaultValue)
	{
		String value = getString(keyName);
		return value != null && value.trim().length() > 0 ? new File(value) : defaultValue;
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected File getFile(String keyName)
	{
		return getFile(keyName, null);
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setColor(String keyName, Color value)
	{
        StringBuilder sb = new StringBuilder(Integer.toHexString(value.getRGB() & 0x00ffffff));
		while (sb.length() < 6) 
            sb.insert(0, "0");
		properties.setProperty(keyName, sb.toString());
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected Color getColor(String keyName, Color defaultValue)
	{
		String value = getString(keyName);
		return value != null ? new Color(Integer.parseInt(value, 16)) : defaultValue;
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected Color getColor(String keyName)
	{
		return getColor(keyName, null);
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setFont(String keyName, Font value)
	{
		properties.setProperty(keyName, value.getName() + ":" + value.getStyle() + ":" + value.getSize());
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected Font getFont(String keyName, Font defaultValue)
	{
		String value = getString(keyName);
		if (value != null)
		{
			String[] segs = value.split(":");
			String name = segs[0];
			int style = ValueUtils.parseInt(segs[1], Font.PLAIN);
			int size = ValueUtils.parseInt(segs[2], 12);
			return new Font(name, style, size);
		}
		return defaultValue;
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected Font getFont(String keyName)
	{
		return getFont(keyName, null);
	}
		
	/**
	 * Sets window bounds.
	 * @param keyName an associated key.
	 * @param window the window to get size from.
	 */
	protected void setFrameBounds(String keyName, Frame window)
	{
		setFrameBounds(
			keyName, 
			window.getX(), 
			window.getY(), 
			window.getWidth(), 
			window.getHeight(), 
			(window.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0
		);
	}
	
	/**
	 * Sets window bounds.
	 * @param keyName an associated key.
	 * @param x 
	 * @param y 
	 * @param width 
	 * @param height 
	 * @param maximized if window was maximized
	 */
	protected void setFrameBounds(String keyName, int x, int y, int width, int height, boolean maximized) 
	{
		properties.setProperty(keyName + WINDOW_X, String.valueOf(x));
		properties.setProperty(keyName + WINDOW_Y, String.valueOf(y));
		properties.setProperty(keyName + WINDOW_WIDTH, String.valueOf(width));
		properties.setProperty(keyName + WINDOW_HEIGHT, String.valueOf(height));
		properties.setProperty(keyName + WINDOW_MAXIMIZED, String.valueOf(maximized));
	}

	/**
	 * Gets window bounds.
	 * @param keyName an associated key.
	 * @return the bounds.
	 */
	protected Rectangle getFrameBounds(String keyName)
	{
		return new Rectangle(
			ValueUtils.parseInt(properties.getProperty(keyName + WINDOW_X), 0),
			ValueUtils.parseInt(properties.getProperty(keyName + WINDOW_Y), 0),
			ValueUtils.parseInt(properties.getProperty(keyName + WINDOW_WIDTH), 720),
			ValueUtils.parseInt(properties.getProperty(keyName + WINDOW_HEIGHT), 480)
		);
	}
	
	/**
	 * @param keyName an associated key.
	 * @return if the main DoomTools window should be maximized.
	 */
	protected boolean getFrameMaximized(String keyName)
	{
		return ValueUtils.parseBoolean(properties.getProperty(keyName + WINDOW_MAXIMIZED), false);
	}
	
	/**
	 * Saves a set of editor view settings.
	 * @param subsetName the editor sub-set name.
	 * @param viewSettings the settings object.
	 */
	protected void setEditorViewSettings(String subsetName, EditorViewSettings viewSettings)
	{
		String prefix = subsetName + EDITOR_VIEW;
		setInteger(prefix + ".tabsize", viewSettings.getTabSize());
		setBoolean(prefix + ".softtabs", viewSettings.isTabsEmulated());
		setBoolean(prefix + ".wrapping", viewSettings.isLineWrap());
		setBoolean(prefix + ".wrapwords", viewSettings.isWrapStyleWord());
		commit();
	}
	
	/**
	 * Gets a set of editor view settings.
	 * @param subsetName the editor sub-set name.
	 * @return the settings object.
	 */
	protected EditorViewSettings getEditorViewSettings(String subsetName)
	{
		EditorViewSettings out = new EditorViewSettings();
		String prefix = subsetName + EDITOR_VIEW;
		out.setTabSize(getInteger(prefix + ".tabsize", out.getTabSize()));
		out.setTabsEmulated(getBoolean(prefix + ".softtabs", out.isTabsEmulated()));
		out.setLineWrap(getBoolean(prefix + ".wrapping", out.isLineWrap()));
		out.setWrapStyleWord(getBoolean(prefix + ".wrapwords", out.isWrapStyleWord()));
		return out;
	}
	
	/**
	 * Sets an editor theme's settings.
	 * @param subsetName the editor sub-set name.
	 * @param editorTheme the theme to store.
	 */
	protected void setEditorTheme(String subsetName, Theme editorTheme)
	{
		// TODO: Save as properties since the Theme export stuff is bugged.
	}
	
	/**
	 * Sets an editor theme's settings.
	 * @param subsetName the editor sub-set name.
	 * @return the corresponding theme, or null if no corresponding theme.
	 */
	protected Theme getEditorTheme(String subsetName)
	{
		// TODO: Load from properties since the Theme export stuff is bugged.
		return null;
	}
	
}
