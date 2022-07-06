package net.mtrop.doom.tools.gui.managers.settings;

import java.io.File;

import net.mtrop.doom.tools.gui.DoomToolsSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.struct.SingletonProvider;


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
	
    private static final String DOOMMAKE_PATH_LAST_PROJECT = "path.lastProject";
    private static final String DOOMMAKE_PATH_SLADE = "path.slade";
    private static final String DOOMMAKE_PATH_IDE = "path.vscode";
    private static final String DOOMMAKE_PATH_MAPEDITOR = "path.map.editor";

	/* ==================================================================== */

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
	
}
