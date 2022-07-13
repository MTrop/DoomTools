package net.mtrop.doom.tools.gui.managers.settings;

import java.io.File;

import net.mtrop.doom.tools.gui.DoomToolsSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.struct.SingletonProvider;


/**
 * DMXConvert GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class DMXConvertSettingsManager extends DoomToolsSettings
{
	/** Settings filename. */
    private static final String SETTINGS_FILENAME = "dmxconv.properties";

    /** The instance encapsulator. */
    private static final SingletonProvider<DMXConvertSettingsManager> INSTANCE = new SingletonProvider<>(() -> new DMXConvertSettingsManager());
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DMXConvertSettingsManager get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
    private static final String PATH_LAST_FILE = "path.lastFile";
    private static final String PATH_FFMPEG = "path.ffmpeg";

	/* ==================================================================== */

	private DMXConvertSettingsManager()
	{
		super(getConfigFile(SETTINGS_FILENAME), DoomToolsLogger.getLogger(DMXConvertSettingsManager.class));
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
	 * Sets the path to FFmpeg.
	 * @param path the file.
	 */
	public void setFFmpegPath(File path) 
	{
		setFile(PATH_FFMPEG, path);
		commit();
	}

	/**
	 * @return the path to FFmpeg.
	 */
	public File getFFmpegPath() 
	{
		return getFile(PATH_FFMPEG);
	}

}
