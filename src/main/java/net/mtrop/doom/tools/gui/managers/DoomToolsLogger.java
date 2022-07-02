package net.mtrop.doom.tools.gui.managers;

import java.io.File;
import java.io.IOException;

import net.mtrop.doom.tools.gui.DoomToolsConstants.Paths;
import net.mtrop.doom.tools.struct.LoggingFactory;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;

/**
 * DoomTools GUI logger singleton.
 * @author Matthew Tropiano
 */
public final class DoomToolsLogger 
{
    /** Logging filename. */
    private static final String LOG_FILENAME = "doomtools.log";
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
			if (!FileUtils.createPathForFile(LOG_FILE))
				return;
			this.loggingFactory.addDriver(new LoggingFactory.FileLogger(LOG_FILE)); 
		} catch (IOException e) {
			// Do nothing.
		}
	}
	
}
