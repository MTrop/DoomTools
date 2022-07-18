package net.mtrop.doom.tools.gui.managers.settings;

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

	/* ==================================================================== */

	private DoomMakeStudioSettingsManager()
	{
		super(getConfigFile(SETTINGS_FILENAME), DoomToolsLogger.getLogger(DoomMakeStudioSettingsManager.class));
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

}
