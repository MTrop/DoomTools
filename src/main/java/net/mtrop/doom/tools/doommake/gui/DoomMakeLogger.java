package net.mtrop.doom.tools.doommake.gui;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.struct.LoggingFactory;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;

/**
 * DoomMake GUI logger singleton.
 * @author Matthew Tropiano
 */
public final class DoomMakeLogger 
{
    /** The instance encapsulator. */
	private static final AtomicReference<DoomMakeLogger> INSTANCE = new AtomicReference<>(null);
    /** Logging filename. */
    private static final String LOG_FILENAME = "doommake.log";
    /** Configuration file. */
    private static final File LOG_FILE = new File(DoomMakeSettings.APPDATA_PATH + LOG_FILENAME);
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomMakeLogger get()
	{
		DoomMakeLogger out;
		if ((out = INSTANCE.get()) != null)
			return out;
		
		synchronized (INSTANCE) 
		{
			// short-circuit.
			if ((out = INSTANCE.get()) != null)
				return out;
			INSTANCE.set(out = new DoomMakeLogger());
			return out;
		}
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
	
	private DoomMakeLogger()
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
