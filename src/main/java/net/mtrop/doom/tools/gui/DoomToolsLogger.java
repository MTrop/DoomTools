package net.mtrop.doom.tools.gui;

import java.io.File;
import java.io.IOException;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.doommake.DoomMakeConstants.Paths;
import net.mtrop.doom.tools.struct.LoggingFactory;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;

/**
 * DoomMake GUI logger singleton.
 * @author Matthew Tropiano
 */
public final class DoomToolsLogger 
{
    /** Logging filename. */
    private static final String LOG_FILENAME = "doommake.log";
    /** Configuration file. */
    private static final File LOG_FILE = new File(Paths.APPDATA_PATH + LOG_FILENAME);

    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsLogger> INSTANCE = new SingletonProvider<>(() -> new DoomToolsLogger()); 

	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsLogger get()
	{
		return INSTANCE.get();
	}

	/**
	 * Fetches a logger for a class.
	 * @param clazz the class.
	 * @return a new logger.
	 */
	public static Logger getLogger(Class<?> clazz)
	{
		return get().loggingFactory.getLogger(clazz);
	}
	
	/* ==================================================================== */
	
	private LoggingFactory loggingFactory;
	
	private DoomToolsLogger()
	{
		this.loggingFactory = LoggingFactory.createConsoleLoggingFactory();
		try {
			if (LOG_FILE.exists())
				LOG_FILE.delete();
			if (!Common.createPathForFile(LOG_FILE))
				return;
			this.loggingFactory.addDriver(new LoggingFactory.FileLogger(LOG_FILE)); 
		} catch (IOException e) {
			// Do nothing.
		}
	}
	
}
