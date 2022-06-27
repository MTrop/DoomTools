package net.mtrop.doom.tools.gui.managers;

import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;

import net.mtrop.doom.tools.gui.DoomToolsSettings;
import net.mtrop.doom.tools.struct.SingletonProvider;


/**
 * DECOHack Compiler GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class DecoHackCompilerSettingsManager extends DoomToolsSettings
{
	/** Settings filename. */
    private static final String SETTINGS_FILENAME = "decohack-compiler.properties";

    /** The instance encapsulator. */
    private static final SingletonProvider<DecoHackCompilerSettingsManager> INSTANCE = new SingletonProvider<>(() -> new DecoHackCompilerSettingsManager());
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DecoHackCompilerSettingsManager get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
    private static final String PATH_LAST_FILE = "path.lastFile";

	/* ==================================================================== */

	private DecoHackCompilerSettingsManager()
	{
		super(getConfigFile(SETTINGS_FILENAME), DoomToolsLogger.getLogger(DecoHackCompilerSettingsManager.class));
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

}
