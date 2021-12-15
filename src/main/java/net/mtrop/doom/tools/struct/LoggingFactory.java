/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Some kind of logger for logging messages.
 * @author Matthew Tropiano
 */
public class LoggingFactory
{
	/** Logging levels. */
	public static enum LogLevel
	{
		FATAL,
		SEVERE,
		ERROR,
		WARNING,
		INFO,
		DEBUG;
	}
	
	/**
	 * Logger interface for all log writing.
	 */
	public interface Logger
	{
		/**
		 * Sets the individual logging level for this logger.
		 * @param loglevel the desired logging level or null to defer to the parent factory level.
		 */
		public void setLoggingLevel(LogLevel loglevel);
	
		/**
		 * Gets the current individual logging level for this logger.
		 * @return the current level. can be null, meaning this defers to the parent factory's log level.
		 */
		public LogLevel getLoggingLevel();
	
		/**
		 * Outputs a FATAL log message.
		 * @param message the object to convert to a string to dump.
		 */
		public void fatal(Object message);
	
		/**
		 * Outputs a FATAL log message, formatted.
		 * A newline is always appended to the end of the message.
		 * @param formatString the formatting string to use to render the args.
		 * @param args the additional parameters for the formatter.
		 */
		public void fatalf(String formatString, Object... args);
	
		/**
		 * Outputs a FATAL log message.
		 * @param t the throwable to print along with the message.
		 * @param message the object to convert to a string to dump.
		 */
		public void fatal(Throwable t, Object message);
	
		/**
		 * Outputs a FATAL log message, formatted.
		 * A newline is always appended to the end of the message.
		 * @param t the throwable to print along with the message.
		 * @param formatString the formatting string to use to render the args.
		 * @param args the additional parameters for the formatter.
		 */
		public void fatalf(Throwable t, String formatString, Object... args);
	
		/**
		 * Outputs a SEVERE log message.
		 * @param message the object to convert to a string to dump.
		 */
		public void severe(Object message);
		
		/**
		 * Outputs a SEVERE log message, formatted.
		 * A newline is always appended to the end of the message.
		 * @param formatString the formatting string to use to render the args.
		 * @param args the additional parameters for the formatter.
		 */
		public void severef(String formatString, Object... args);	
		
		/**
		 * Outputs a SEVERE log message.
		 * @param t the throwable to print along with the message.
		 * @param message the object to convert to a string to dump.
		 */
		public void severe(Throwable t, Object message);
		
		/**
		 * Outputs a SEVERE log message, formatted.
		 * A newline is always appended to the end of the message.
		 * @param t the throwable to print along with the message.
		 * @param formatString the formatting string to use to render the args.
		 * @param args the additional parameters for the formatter.
		 */
		public void severef(Throwable t, String formatString, Object... args);	
		
		/**
		 * Outputs a ERROR log message.
		 * @param message the object to convert to a string to dump.
		 */
		public void error(Object message);
		
		/**
		 * Outputs a ERROR log message, formatted.
		 * A newline is always appended to the end of the message.
		 * @param formatString the formatting string to use to render the args.
		 * @param args the additional parameters for the formatter.
		 */
		public void errorf(String formatString, Object... args);	
		
		/**
		 * Outputs a ERROR log message.
		 * @param t the throwable to print along with the message.
		 * @param message the object to convert to a string to dump.
		 */
		public void error(Throwable t, Object message);
		
		/**
		 * Outputs a ERROR log message, formatted.
		 * A newline is always appended to the end of the message.
		 * @param t the throwable to print along with the message.
		 * @param formatString the formatting string to use to render the args.
		 * @param args the additional parameters for the formatter.
		 */
		public void errorf(Throwable t, String formatString, Object... args);	
		
		/**
		 * Outputs a WARNING log message.
		 * @param message the object to convert to a string to dump.
		 */
		public void warn(Object message);
		
		/**
		 * Outputs a WARNING log message, formatted.
		 * A newline is always appended to the end of the message.
		 * @param formatString the formatting string to use to render the args.
		 * @param args the additional parameters for the formatter.
		 */
		public void warnf(String formatString, Object... args);	
		
		/**
		 * Outputs a INFO log message.
		 * @param message the object to convert to a string to dump.
		 */
		public void info(Object message);
		
		/**
		 * Outputs a INFO log message, formatted.
		 * A newline is always appended to the end of the message.
		 * @param formatString the formatting string to use to render the args.
		 * @param args the additional parameters for the formatter.
		 */
		public void infof(String formatString, Object... args);	
		
		/**
		 * Outputs a DEBUG log message.
		 * @param message the object to convert to a string to dump.
		 */
		public void debug(Object message);
		
		/**
		 * Outputs a DEBUG log message, formatted.
		 * A newline is always appended to the end of the message.
		 * @param formatString the formatting string to use to render the args.
		 * @param args the additional parameters for the formatter.
		 */
		public void debugf(String formatString, Object... args);	
		
	}

	/**
	 * A logging output driver that outputs messages to a log when it receives a set of inputs. 
	 */
	public interface Driver
	{
		/**
		 * Processes a logging message.
		 * @param time the time that this message was logged.
		 * @param level the logging level.
		 * @param source the source of the message.
		 * @param message the message to object.
		 * @param throwable the throwable to output along with the message.
		 */
		public void log(Date time, LogLevel level, String source, String message, Throwable throwable);
		
	}

	/** Out queue. */
	private Queue<LogObject> outQueue;
	
	/** Stream to send logs out to. */
	private Queue<Driver> drivers;
	/** This logging factory's logging level. */
	private LogLevel loggingLevel;
	/** Logger thread. */
	private LoggerThread loggerThread;
	
	/**
	 * Creates a new logging factory.
	 * The starting logging level is {@link LogLevel#DEBUG}.
	 * @param drivers the logging driver to use for directing output.
	 */
	public LoggingFactory(Driver... drivers)
	{
		this(LogLevel.DEBUG, drivers);
	}
	
	/**
	 * Creates a new logging factory.
	 * @param drivers the logging driver to use for directing output.
	 * @param level the starting logging level.
	 */
	public LoggingFactory(LogLevel level, Driver... drivers)
	{
		this.drivers = new LinkedList<Driver>();
		this.outQueue = new LinkedList<LogObject>(); 
		this.loggingLevel = level;

		addDriver(drivers);
	}
	
	/**
	 * A convenience method for creating a Console-appending logger factory.
	 * <p>Equivalent to: <code>new LoggingFactory(LogLevel.DEBUG, new ConsoleLogger())</code></p>
	 * @return the new logging factory.
	 */
	public static LoggingFactory createConsoleLoggingFactory()
	{
		return new LoggingFactory(LogLevel.DEBUG, new ConsoleLogger());
	}

	/**
	 * A convenience method for creating a Console-appending logger for a particular source by name.
	 * <p>Equivalent to: <code>createConsoleLoggingFactory().getLogger(name)</code></p>
	 * <p>It may be better, thread-resource-wise to create a single factory if you are logging multiple sources/classes through it.</p>
	 * @param name the source name of this logger.
	 * @return the new logger.
	 * @see #getLogger(String)
	 */
	public static Logger createConsoleLoggerFor(String name)
	{
		return createConsoleLoggingFactory().getLogger(name);
	}
	
	/**
	 * A convenience method for creating a Console-appending logger for a particular source by class name.
	 * <p>Equivalent to: <code>createConsoleLoggingFactory().getLogger(clazz)</code></p>
	 * <p>It may be better, thread-resource-wise to create a single factory if you are logging multiple sources/classes through it.</p>
	 * @param clazz the class to create the logger for.
	 * @return the new logger.
	 * @see #getLogger(Class)
	 */
	public static Logger createConsoleLoggerFor(Class<?> clazz)
	{
		return createConsoleLoggingFactory().getLogger(clazz);
	}
	
	/**
	 * A convenience method for creating a Console-appending logger for a particular source by class name.
	 * <p>Equivalent to: <code>createConsoleLoggingFactory().getLogger(clazz, fullyQualified)</code></p>
	 * <p>It may be better, thread-resource-wise to create a single factory if you are logging multiple sources/classes through it.</p>
	 * @param clazz the class to create the logger for.
	 * @param fullyQualified if true, the output name is a fully-qualified name.
	 * @return the new logger.
	 * @see #getLogger(Class, boolean)
	 */
	public static Logger createConsoleLoggerFor(Class<?> clazz, boolean fullyQualified)
	{
		return createConsoleLoggingFactory().getLogger(clazz, fullyQualified);
	}
	
	/**
	 * Adds a logging driver or drivers from this factory.
	 * @param drivers the drivers to add.
	 */
	public void addDriver(Driver... drivers)
	{
		for (Driver d : drivers)
			this.drivers.add(d);
	}
	
	/**
	 * Removes a logging driver or drivers from this factory.
	 * @param drivers the drivers to remove.
	 */
	public void removeDriver(Driver... drivers)
	{
		for (Driver d : drivers)
			this.drivers.remove(d);
	}
	
	/**
	 * Returns the current logging level.
	 * Anything logged using {@link Logger}s generated by this factory is tested
	 * against the current logging level. If the logging level of the message is less than or equal
	 * to the current logging level, it is logged.
	 * @return the current logging level.
	 */
	public LogLevel getLoggingLevel()
	{
		return loggingLevel;
	}
	
	/**
	 * Returns the current logging level.
	 * Anything logged using {@link Logger}s generated by this factory is tested
	 * against the current logging level. If the logging level of the message is less than or equal
	 * to the current logging level, it is logged.
	 * @param level the new logging level.
	 */
	public void setLoggingLevel(LogLevel level)
	{
		this.loggingLevel = level;
	}
	
	/**
	 * Creates a new Logger for outputting logs.
	 * This logger uses the logging level and driver defined on this logging factory.
	 * @param name the source name.
	 * @return a logger to call to output logging to.
	 */
	public Logger getLogger(String name)
	{
		return new LoggerDelegate(name);
	}
	
	/**
	 * Creates a new Logger for outputting logs, using the name of the class as a source name.
	 * This logger uses the logging level and driver defined on this logging factory.
	 * @param clz the class to use.
	 * @return a logger to call to output logging to.
	 */
	public Logger getLogger(Class<?> clz)
	{
		return getLogger(clz, false);
	}
	
	/**
	 * Creates a new Logger for outputting logs, using the name of the class as a source name.
	 * This logger uses the logging level and driver defined on this logging factory.
	 * @param clz the class to use.
	 * @param fullyQualified if true, use the fully-qualified name. 
	 * @return a logger to call to output logging to.
	 */
	public Logger getLogger(Class<?> clz, boolean fullyQualified)
	{
		return new LoggerDelegate(fullyQualified ? clz.getName() : clz.getSimpleName());
	}
	
	// These logging methods are set up this way to not incur potentially expensive string processing until
	// we are sure they will be logged!
	
	/**
	 * Adds a log message to the logger queue.
	 * @param level the target logging level.
	 * @param source the source name.
	 * @param throwable the throwable to dump, if any.
	 * @param message the message to output.
	 */
	private void addLog(LogLevel level, LogLevel localLevel, String source, Throwable throwable, Object message)
	{
		if (!checkLoggingLevel(level, localLevel))
			return;
		
		synchronized (outQueue)
		{
			outQueue.add(new LogObject(new Date(), level, source, String.valueOf(message), throwable));
			if (loggerThread == null || !loggerThread.isAlive())
				(loggerThread = new LoggerThread()).start();
			outQueue.notify();
			try {Thread.sleep(0L);} catch (InterruptedException e) {}
		}
	}

	/**
	 * Adds a log message to the logger queue.
	 * @param level the target logging level.
	 * @param source the source name.
	 * @param throwable the throwable to dump, if any.
	 * @param message the message to output.
	 * @param args the parameterized arguments for formatting.
	 */
	private void addLogF(LogLevel level, LogLevel localLevel, String source, Throwable throwable, String message, Object ...args)
	{
		if (!checkLoggingLevel(level, localLevel))
			return;
		
		synchronized (outQueue)
		{
			outQueue.add(new LogObject(new Date(), level, source, String.format(message, args), throwable));
			if (loggerThread == null || !loggerThread.isAlive())
				(loggerThread = new LoggerThread()).start();
			outQueue.notify();
			try {Thread.sleep(0L);} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * Checks if the logging level allows for a log statement to be logged.
	 * If the localLevel is not provided, this uses the parent logger's level.
	 * @param level the level for the logged statement.
	 * @param localLevel the local logger's log level to check against, if any.
	 * @return true if the log entry passes, false if it doesn't.
	 */
	private boolean checkLoggingLevel(LogLevel level, LogLevel localLevel)
	{
		if (localLevel != null)
		{
			if (level.ordinal() > localLevel.ordinal())
				return false;
		}
		else if (loggingLevel != null)
		{
			if (level.ordinal() > loggingLevel.ordinal())
				return false;
		}
		
		return true;
	}
	
	private static void close(AutoCloseable c)
	{
		if (c == null) return;
		try { c.close(); } catch (Exception e){}
	}

	/**
	 * Logger queue object.
	 */
	private static class LogObject
	{
		Date time;
		LogLevel level;
		String source;
		String message;
		Throwable throwable;
		
		private LogObject(Date time, LogLevel level, String source, String message, Throwable throwable)
		{
			this.time = time;
			this.level = level;
			this.source = source;
			this.message = message;
			this.throwable = throwable;
		}
	}

	/**
	 * Delegate class that accepts logging input.
	 */
	private class LoggerDelegate implements Logger
	{
		/** The source of the message. */
		private LogLevel localLevel;
		/** The source of the message. */
		private String source;

		public LoggerDelegate(String source)
		{
			this.localLevel = null;
			this.source = source;
		}

		@Override
		public void setLoggingLevel(LogLevel loglevel) 
		{
			localLevel = loglevel;
		}

		@Override
		public LogLevel getLoggingLevel() 
		{
			return localLevel;
		}
		
		@Override
		public void fatal(Object message)
		{
			addLog(LogLevel.FATAL, localLevel, source, null, String.valueOf(message));
		}

		@Override
		public void fatalf(String formatString, Object... args)
		{
			addLogF(LogLevel.FATAL, localLevel, source, null, formatString, args);
		}

		@Override
		public void fatal(Throwable t, Object message)
		{
			addLog(LogLevel.FATAL, localLevel, source, t, String.valueOf(message));
		}

		@Override
		public void fatalf(Throwable t, String formatString, Object... args)
		{
			addLogF(LogLevel.FATAL, localLevel, source, t, formatString, args);
		}

		@Override
		public void severe(Object message)
		{
			addLog(LogLevel.SEVERE, localLevel, source, null, String.valueOf(message));
		}

		@Override
		public void severef(String formatString, Object... args)
		{
			addLogF(LogLevel.SEVERE, localLevel, source, null, formatString, args);
		}

		@Override
		public void severe(Throwable t, Object message)
		{
			addLog(LogLevel.SEVERE, localLevel, source, t, String.valueOf(message));
		}

		@Override
		public void severef(Throwable t, String formatString, Object... args)
		{
			addLogF(LogLevel.SEVERE, localLevel, source, t, formatString, args);
		}

		@Override
		public void error(Object message)
		{
			addLog(LogLevel.ERROR, localLevel, source, null, String.valueOf(message));
		}

		@Override
		public void errorf(String formatString, Object... args)
		{
			addLogF(LogLevel.ERROR, localLevel, source, null, formatString, args);
		}

		@Override
		public void error(Throwable t, Object message)
		{
			addLog(LogLevel.ERROR, localLevel, source, t, String.valueOf(message));
		}

		@Override
		public void errorf(Throwable t, String formatString, Object... args)
		{
			addLogF(LogLevel.ERROR, localLevel, source, t, formatString, args);
		}

		@Override
		public void warn(Object message)
		{
			addLog(LogLevel.WARNING, localLevel, source, null, String.valueOf(message));
		}

		@Override
		public void warnf(String formatString, Object... args)
		{
			addLogF(LogLevel.WARNING, localLevel, source, null, formatString, args);
		}

		@Override
		public void info(Object message)
		{
			addLog(LogLevel.INFO, localLevel, source, null, String.valueOf(message));
		}

		@Override
		public void infof(String formatString, Object... args)
		{
			addLogF(LogLevel.INFO, localLevel, source, null, formatString, args);
		}

		@Override
		public void debug(Object message)
		{
			addLog(LogLevel.DEBUG, localLevel, source, null, String.valueOf(message));
		}

		@Override
		public void debugf(String formatString, Object... args)
		{
			addLogF(LogLevel.DEBUG, localLevel, source, null, formatString, args);
		}

	}
	
	/**
	 * The thread that reads the output queue and dumps stuff. 
	 */
	private class LoggerThread extends Thread
	{
		private LoggerThread()
		{
			setName("LoggerThread-"+drivers.getClass().getSimpleName());
			setDaemon(false);
		}
		
		@Override
		public void run()
		{
			while (true)
			{
				try {
					
					LogObject logobj = null;
					synchronized (outQueue)
					{
						if (outQueue.isEmpty())
							outQueue.wait(100);
						if (outQueue.isEmpty())
							break;
						logobj = outQueue.poll();
					}
					
					for (Driver d : drivers)
						d.log(logobj.time, logobj.level, logobj.source, logobj.message, logobj.throwable);
					
				} catch (Throwable e) {
					e.printStackTrace(System.err);
				}
			}
		}
	}

	/**
	 * A standard logger for outputting to a print stream.
	 * @author Matthew Tropiano
	 */
	public static class PrintStreamLogger implements Driver
	{
		/** The print stream to output to. */
		private PrintStream out;
		
		/**
		 * Creates a new print stream logger.
		 * @param out the {@link PrintStream} to output to.
		 */
		public PrintStreamLogger(PrintStream out)
		{
			this.out = out;
		}
		
		@Override
		public void log(Date time, LogLevel level, String source, String message, Throwable throwable)
		{
			if (out == null)
				return;
			
			out.println(String.format("[%tF %tT.%tL] (%s) %s: %s", time, time, time, source, level.name(), message));
			if (throwable != null)
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				throwable.printStackTrace(pw);
				pw.flush();
				pw.close();
				close(sw);
				out.println(sw);
			}
		}
	
	}

	/**
	 * A logger driver that outputs to Standard Out.
	 */
	public static class ConsoleLogger extends PrintStreamLogger
	{
		/**
		 * Creates a new console logger.
		 */
		public ConsoleLogger()
		{
			super(System.out);
		}
	}

	/**
	 * A logging driver that writes to a text file.
	 */
	public static class FileLogger implements Driver
	{
		/** Mutex for set and write. */
		private Object MUTEX;
		/** The current PrintWriter to write to. */
		private PrintWriter writer;
		/** The current File to write to. */
		private File file;
		
		/**
		 * Creates a new file logger the writes to a specific file.
		 * @param logFile the file to write to.
		 * @throws IOException if the file could not be opened.
		 */
		public FileLogger(File logFile) throws IOException
		{
			MUTEX = new Object();
			setFile(logFile);
		}
		
		/**
		 * Sets the log file to a new file.
		 * The previous file is closed.
		 * @param logFile the file to write to.
		 * @throws IOException if the file could not be opened.
		 */
		protected void setFile(File logFile) throws IOException
		{
			synchronized (MUTEX)
			{
				if (file != null)
				{
					close(writer);
					closeFile(file);
					writer = null;
					file = null;
				}
				
				file = logFile;
				writer = new PrintWriter(new FileOutputStream(file), true);
			}
		}
		
		/**
		 * Called after the writer to the previous file is closed
		 * on a file switch via {@link #setFile(File)}
		 * @param closeFile the file that was closed.
		 * @throws IOException if the file could not be closed cleanly.
		 */
		protected void closeFile(File closeFile) throws IOException
		{
			// Does nothing by default.
		}
		
		@Override
		public void log(Date time, LogLevel level, String source, String message, Throwable throwable)
		{
			if (writer == null)
				return;
			
			synchronized (MUTEX)
			{
				writer.println(String.format("[%tF %tT.%tL] (%s) %s: %s", time, time, time, source, level.name(), message));
				if (throwable != null)
					throwable.printStackTrace(writer);
			}
		}
	}

}
