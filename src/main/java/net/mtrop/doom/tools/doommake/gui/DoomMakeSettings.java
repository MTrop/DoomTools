package net.mtrop.doom.tools.doommake.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.OSUtils;

/**
 * DoomMake GUI settings singleton.
 * @author Matthew Tropiano
 */
public final class DoomMakeSettings 
{
    /** DoomMake Config folder base. */
    public static final String APPDATA_PATH = OSUtils.getApplicationSettingsPath() + "/DoomMake/";

    /** The instance encapsulator. */
	private static final AtomicReference<DoomMakeSettings> INSTANCE = new AtomicReference<>(null);
    /** Settings filename. */
    private static final String SETTINGS_FILENAME = "settings.properties";
    /** Configuration file. */
    private static final File CONFIG_FILE = new File(APPDATA_PATH + SETTINGS_FILENAME);
    /** Logger. */
    private static final Logger LOG = DoomMakeLogger.getLogger(DoomMakeSettings.class); 
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomMakeSettings get()
	{
		DoomMakeSettings out;
		if ((out = INSTANCE.get()) != null)
			return out;
		
		synchronized (INSTANCE) 
		{
			// short-circuit.
			if ((out = INSTANCE.get()) != null)
				return out;
			
			out = new DoomMakeSettings();
			
			if (!CONFIG_FILE.exists())
			{
				LOG.infof("No settings file %s - using defaults.", CONFIG_FILE.getPath());
			}
			else
			{
				out.loadSettings();
			}
			
			return out;
		}
	}
	
	/* ==================================================================== */
	
	private Properties properties;
	
	private DoomMakeSettings()
	{
		this.properties = new Properties();
	}
	
	public void setLastProjectDirectory(String lastProjectDirectory) 
	{
		properties.setProperty("doommake.path.lastProject", lastProjectDirectory);
		saveSettings();
	}
	
	public String getLastProjectDirectory() 
	{
		return properties.getProperty("doommake.path.lastProject");
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
	
}
