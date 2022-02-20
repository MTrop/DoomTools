/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A single process Runnawrapper because Processes are the worst to deal with.
 * This Process builder is abstracted as a {@link Callable}, so it and other instances
 * can be added to an executor.
 * <p> If the same output reference is provided for more than one stream (STDOUT and STDERR),
 * This will merge the two streams and write to them synchronously. For <b>files</b> however,
 * you'll have to create a {@link FileOutputStream} for it to take advantage.
 * @author Matthew Tropiano
 */
public class ProcessCallable implements Callable<Integer>
{
	private static final AtomicLong INSTANCE_ID = new AtomicLong(0L); 
	
	private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows");
	
	/** A null outputstream. */
	private static final OutputStream OUTPUTSTREAM_NULL = new OutputStream() 
	{
		@Override
		public void write(int b) throws IOException
		{
			// Do nothing.
		}
		
		@Override
		public void write(byte[] b) throws IOException
		{
			// Do nothing.
		}
	
		@Override
		public void write(byte[] b, int off, int len) throws IOException 
		{
			// Do nothing.
		}
	};
	
	/** A null inputstream. */
	private static final InputStream INPUTSTREAM_NULL = new InputStream() 
	{
		@Override
		public int read() throws IOException
		{
			return -1;
		}
	};

	/** A null writer. */
	private static final Writer WRITER_NULL = new Writer()
	{
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException 
		{
			// Do nothing.
		}
	
		@Override
		public void flush() throws IOException 
		{
			// Do nothing.
		}
	
		@Override
		public void close() throws IOException 
		{
			// Do nothing.
		}
	};
	
	/** A null reader. */
	private static final Reader READER_NULL = new Reader()
	{
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException 
		{
			return -1;
		}

		@Override
		public void close() throws IOException 
		{
			// Do nothing.
		}
	};

	/** A null printstream. */
	private static final PrintStream PRINTSTREAM_NULL = new PrintStream(OUTPUTSTREAM_NULL);
	
	/** A null file. */
	private static final File NULL_FILE = new File(System.getProperty("os.name").contains("Windows") ? "NUL" : "/dev/null");

	private static final PipeThreadCreator NULLOUT_REDIRECTOR =
		(process) -> new InputToOutputStreamThread(process.getInputStream(), OUTPUTSTREAM_NULL);
	private static final PipeThreadCreator NULLERR_REDIRECTOR =
		(process) -> new InputToOutputStreamThread(process.getErrorStream(), OUTPUTSTREAM_NULL);
	private static final PipeThreadCreator NULLIN_REDIRECTOR =
		(process) -> new InputToOutputStreamThread(INPUTSTREAM_NULL, process.getOutputStream());
	
	/* ==================================================================== */
		
	private String callName;
	private Deque<String> command;
	private Map<String, Object> envMap;
	private File workingDirectory;
	private PipeThreadCreator stdOutRedirector;
	private PipeThreadCreator stdErrRedirector;
	private PipeThreadCreator stdInRedirector;
	private StreamExceptionListener stdOutListener;
	private StreamExceptionListener stdErrListener;
	private StreamExceptionListener stdInListener;

	private ProcessCallable(String callName)
	{
		setCallName(callName);
		
		this.command = new LinkedList<>();
		this.envMap = new HashMap<>();
		this.workingDirectory = null;
		this.stdOutRedirector = NULLOUT_REDIRECTOR;
		this.stdErrRedirector = NULLERR_REDIRECTOR;
		this.stdInRedirector = NULLIN_REDIRECTOR;
		this.stdOutListener = null;
		this.stdErrListener = null;
		this.stdInListener = null;
	}
	
	/**
	 * Creates a new ProcessCallable from a command.
	 * @param command the command name or path to a command.
	 * @param args additional optional arguments.
	 * @return a new {@link ProcessCallable}
	 */
	public static ProcessCallable create(String command, String ... args)
	{
		ProcessCallable out = new ProcessCallable(command);
		out.arg(command);
		for (int i = 0; i < args.length; i++)
			out.arg(args[i]);
		return out;
	}
	
	/**
	 * Creates a new ProcessCallable from a path to an executable image.
	 * @param commandPath the path to an executable image.
	 * @param args additional optional arguments.
	 * @return a new {@link ProcessCallable}
	 */
	public static ProcessCallable create(File commandPath, String ... args)
	{
		String name = commandPath.getName();
		ProcessCallable out = new ProcessCallable(name);
		out.arg(commandPath.getPath());
		for (int i = 0; i < args.length; i++)
			out.arg(args[i]);
		return out;
	}
	
	/**
	 * Creates a new ProcessCallable that, for convenience, starts a call to Windows CMD.
	 * This is useful for calling CMD batch files in Windows.
	 * <p> This is equivalent to: <code>create("cmd").arg("/c").arg(command)</code> 
	 * @param command the command name or path to a command.
	 * @param args additional optional arguments.
	 * @return a new {@link ProcessCallable}
	 */
	public static ProcessCallable cmd(String command, String ... args)
	{
		ProcessCallable out = new ProcessCallable(command);
		out.arg("cmd").arg("/c").arg(command);
		for (int i = 0; i < args.length; i++)
			out.arg(args[i]);
		return out;
	}
	
	/**
	 * Creates a new ProcessCallable that, for convenience, starts a call to Windows CMD if on 
	 * Windows, or just a command otherwise. In UNIX-likes, this is moot - all executables have a header
	 * for finding a host program.
	 * @param command the command name or path to a command.
	 * @param args additional optional arguments.
	 * @return a new {@link ProcessCallable}
	 * @see #cmd(String, String...)
	 * @see #create(String, String...)
	 */
	public static ProcessCallable shell(String command, String ... args)
	{
		return IS_WINDOWS ? cmd(command, args) : create(command, args); 
	}
	
	/**
	 * Creates a new ProcessCallable that, for convenience, starts a call to Windows CMD if on 
	 * Windows, or just a command otherwise. In UNIX-likes, this is moot - all executables have a header
	 * for finding a host program.
	 * @param commandPath the path to an executable image or batch file.
	 * @param args additional optional arguments.
	 * @return a new {@link ProcessCallable}
	 * @see #cmd(String, String...)
	 * @see #create(File, String...)
	 */
	public static ProcessCallable shell(File commandPath, String ... args)
	{
		return IS_WINDOWS ? cmd(commandPath.getPath(), args) : create(commandPath, args); 
	}
	
	/**
	 * Creates a process that is prepopulated with the commands and initial arguments to execute
	 * the current Java process as a new process with the same classpath (<code>System.getProperty("java.class.path")</code>). 
	 * @param mainClass the main class to run.
	 * @param jvmOptions the list of switches to pass to the JVM (NOT PROGRAM ARGUMENTS).
	 * @return a new {@link ProcessCallable}
	 */
	public static ProcessCallable java(Class<?> mainClass, String ... jvmOptions)
	{
		return java(System.getProperty("java.class.path"), mainClass, jvmOptions);
	}
	
	/**
	 * Creates a process that is prepopulated with the commands and initial arguments to execute
	 * the current Java process as a new process. 
	 * @param classPath the class path to use.
	 * @param mainClass the main class to run.
	 * @param jvmOptions the list of switches to pass to the JVM (NOT PROGRAM ARGUMENTS).
	 * @return a new {@link ProcessCallable}
	 */
	public static ProcessCallable java(String classPath, Class<?> mainClass, String ... jvmOptions)
	{
		ProcessCallable out = new ProcessCallable("Java-" + mainClass.getSimpleName());
		out.arg(new File(System.getProperty("java.home") + File.separator + "bin" + File.separator + (IS_WINDOWS ? "java.exe" : "java")).getPath());
		for (int i = 0; i < jvmOptions.length; i++)
			out.arg(jvmOptions[i]);
		out.arg("-cp");
		out.arg(classPath);
		out.arg(mainClass.getCanonicalName());
		return out;
	}
	
	/**
	 * Sets the callable's "call name".
	 * This is used in thread names spawned by this callable for tracking and debugging purposes.
	 * @param callName the new call name.
	 * @return this, for chaining.
	 */
	public ProcessCallable setCallName(String callName)
	{
		this.callName = Objects.requireNonNull(callName, "Command/call name cannot be null.");
		return this;
	}
	
	/**
	 * Adds a command line argument.
	 * Command line arguments are sent in the order added.
	 * @param arg the command argument to add.
	 * @return this, for chaining.
	 */
	public ProcessCallable arg(String arg)
	{
		command.add(arg);
		return this;
	}
	
	/**
	 * Sets all environment variables from the current {@link System#getenv()}.
	 * This is entirely unnecessary for most cases, as setting zero environment variables
	 * will automatically inherit this program's environment.
	 * @return this, for chaining.
	 */
	public ProcessCallable copyEnv()
	{
		for (Map.Entry<String, String> entry : System.getenv().entrySet())
			envMap.put(entry.getKey(), entry.getValue());
		return this;
	}

	/**
	 * Sets an environment variable for the spawned process.
	 * @param variable the environment variable.
	 * @param value the value for the variable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setEnv(String variable, Object value)
	{
		envMap.put(variable, value);
		return this;
	}
	
	/**
	 * Sets an environment variable for the spawned process.
	 * @param directory the working directory.
	 * @return this, for chaining.
	 * @throws IllegalArgumentException if the provided file is not a directory, or it doesn't exist.
	 */
	public ProcessCallable setWorkingDirectory(File directory)
	{
		if (!directory.isDirectory())
			throw new IllegalArgumentException("The provided file is not a directory: " + directory.getPath());
		workingDirectory = directory;
		return this;
	}
	
	/**
	 * Redirects standard out to another destination.
	 * @param newOut the new output stream to redirect STDOUT to. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setOut(final OutputStream newOut)
	{
		final OutputStream stream = newOut == null ? OUTPUTSTREAM_NULL : newOut;
		stdOutRedirector = (process) -> new InputToOutputStreamThread(process.getInputStream(), stream); 
		return this;
	}
	
	/**
	 * Redirects standard out to another destination.
	 * The program's STDOUT is assumed to be in native encoding.
	 * @param newOut the new writer to redirect STDOUT to. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setOut(final Writer newOut)
	{
		final Writer stream = newOut == null ? WRITER_NULL : newOut;
		stdOutRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getInputStream()), stream); 
		return this;
	}
	
	/**
	 * Redirects standard out to another destination.
	 * The program's STDOUT is assumed to be in native encoding.
	 * @param newOut the new print stream to redirect STDOUT to. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setOut(final PrintStream newOut)
	{
		final PrintStream stream = newOut == null ? PRINTSTREAM_NULL : newOut;
		stdOutRedirector = (process) -> new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getInputStream())), stream); 
		return this;
	}
	
	/**
	 * Redirects standard out to another destination.
	 * @param encoding the process output encoding.
	 * @param newOut the new writer to redirect STDOUT to. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setOut(final Charset encoding, final Writer newOut)
	{
		final Writer stream = newOut == null ? WRITER_NULL : newOut;
		stdOutRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getInputStream(), encoding), stream); 
		return this;
	}
	
	/**
	 * Redirects standard out to another destination.
	 * @param encoding the process output encoding.
	 * @param newOut the new print stream to redirect STDOUT to. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setOut(final Charset encoding, final PrintStream newOut)
	{
		final PrintStream stream = newOut == null ? PRINTSTREAM_NULL : newOut;
		stdOutRedirector = (process) -> new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getInputStream(), encoding)), stream); 
		return this;
	}
	
	/**
	 * Redirects standard out to another destination.
	 * @param outFile the output file.
	 * @return this, for chaining.
	 * @throws FileNotFoundException if outFile exists and is a directory. 
	 */
	public ProcessCallable setOut(final File outFile) throws FileNotFoundException
	{
		if (outFile.isDirectory())
			throw new FileNotFoundException("output file is a directory");
		
		final File target = outFile == null ? NULL_FILE : outFile;
		stdOutRedirector = (process) -> new InputToOutputStreamThread(process.getInputStream(), new FileOutputStream(target)) 
		{
			@Override
			public void afterClose() throws IOException
			{
				targetStream.close();
			}
		};
		return this;
	}
	
	/**
	 * Redirects standard out to another destination.
	 * The program's STDOUT is assumed to be in native encoding.
	 * @param outCharset the output encoding for the file.
	 * @param outFile the output file.
	 * @return this, for chaining.
	 * @throws FileNotFoundException if outFile exists and is a directory. 
	 */
	public ProcessCallable setOut(Charset outCharset, final File outFile) throws FileNotFoundException
	{
		if (outFile.isDirectory())
			throw new FileNotFoundException("output file is a directory");
		
		final File target = outFile == null ? NULL_FILE : outFile;
		stdOutRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getInputStream()), new OutputStreamWriter(new FileOutputStream(target), outCharset)) 
		{
			@Override
			public void afterClose() throws IOException
			{
				targetWriter.close();
			}
		};
		return this;
	}
	
	/**
	 * Redirects standard out to another destination.
	 * @param processCharset the process's expected output charset.
	 * @param outCharset the output encoding for the file.
	 * @param outFile the output file.
	 * @return this, for chaining.
	 * @throws FileNotFoundException if outFile exists and is a directory. 
	 */
	public ProcessCallable setOut(Charset processCharset, Charset outCharset, final File outFile) throws FileNotFoundException
	{
		if (outFile.isDirectory())
			throw new FileNotFoundException("output file is a directory");
		
		final File target = outFile == null ? NULL_FILE : outFile;
		stdOutRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getInputStream(), processCharset), new OutputStreamWriter(new FileOutputStream(target), outCharset)) 
		{
			@Override
			public void afterClose() throws IOException
			{
				targetWriter.close();
			}
		};
		return this;
	}
	
	/**
	 * Redirects standard error to another destination.
	 * @param newErr the new output stream to redirect STDERR to. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setErr(final OutputStream newErr)
	{
		final OutputStream stream = newErr == null ? OUTPUTSTREAM_NULL : newErr;
		stdErrRedirector = (process) -> new InputToOutputStreamThread(process.getErrorStream(), stream); 
		return this;
	}
	
	/**
	 * Redirects standard error to another destination.
	 * @param newErr the new writer to redirect STDERR to. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setErr(final Writer newErr)
	{
		final Writer stream = newErr == null ? WRITER_NULL : newErr;
		stdErrRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getErrorStream()), stream); 
		return this;
	}
	
	/**
	 * Redirects standard error to another destination.
	 * @param newErr the new print stream to redirect STDERR to. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setErr(final PrintStream newErr)
	{
		final PrintStream stream = newErr == null ? PRINTSTREAM_NULL : newErr;
		stdErrRedirector = (process) -> new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getErrorStream())), stream); 
		return this;
	}
	
	/**
	 * Redirects standard error to another destination.
	 * @param encoding the process output encoding.
	 * @param newErr the new writer to redirect STDERR to. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setErr(final Charset encoding, final Writer newErr)
	{
		final Writer stream = newErr == null ? WRITER_NULL : newErr;
		stdErrRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getErrorStream(), encoding), stream); 
		return this;
	}
	
	/**
	 * Redirects standard error to another destination.
	 * @param encoding the process output encoding.
	 * @param newErr the new print stream to redirect STDERR to. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setErr(final Charset encoding, final PrintStream newErr)
	{
		final PrintStream stream = newErr == null ? PRINTSTREAM_NULL : newErr;
		stdErrRedirector = (process) -> new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getErrorStream(), encoding)), stream); 
		return this;
	}
	
	/**
	 * Redirects standard error to another destination.
	 * @param outFile the output file.
	 * @return this, for chaining.
	 * @throws FileNotFoundException if outFile exists and is a directory. 
	 */
	public ProcessCallable setErr(final File outFile) throws FileNotFoundException
	{
		if (outFile.isDirectory())
			throw new FileNotFoundException("output file is a directory");
		
		final File target = outFile == null ? NULL_FILE : outFile;
		stdErrRedirector = (process) -> new InputToOutputStreamThread(process.getErrorStream(), new FileOutputStream(target)) 
		{
			@Override
			public void afterClose() throws IOException
			{
				targetStream.close();
			}
		};
		return this;
	}
	
	/**
	 * Redirects standard error to another destination.
	 * The program's STDERR is assumed to be in native encoding.
	 * @param outCharset the output encoding for the file.
	 * @param outFile the output file.
	 * @return this, for chaining.
	 * @throws FileNotFoundException if outFile exists and is a directory. 
	 */
	public ProcessCallable setErr(Charset outCharset, final File outFile) throws FileNotFoundException
	{
		if (outFile.isDirectory())
			throw new FileNotFoundException("output file is a directory");
		
		final File target = outFile == null ? NULL_FILE : outFile;
		stdErrRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getErrorStream()), new OutputStreamWriter(new FileOutputStream(target), outCharset)) 
		{
			@Override
			public void afterClose() throws IOException
			{
				targetWriter.close();
			}
		};
		return this;
	}
	
	/**
	 * Redirects standard error to another destination.
	 * @param processCharset the process's expected output charset.
	 * @param outCharset the output encoding for the file.
	 * @param outFile the output file.
	 * @return this, for chaining.
	 * @throws FileNotFoundException if outFile exists and is a directory. 
	 */
	public ProcessCallable setErr(Charset processCharset, Charset outCharset, final File outFile) throws FileNotFoundException
	{
		if (outFile.isDirectory())
			throw new FileNotFoundException("output file is a directory");
		
		final File target = outFile == null ? NULL_FILE : outFile;
		stdErrRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getErrorStream(), processCharset), new OutputStreamWriter(new FileOutputStream(target), outCharset)) 
		{
			@Override
			public void afterClose() throws IOException
			{
				targetWriter.close();
			}
		};
		return this;
	}
	
	/**
	 * Redirects standard in.
	 * @param newIn the new input stream to redirect STDIN from. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setIn(final InputStream newIn)
	{
		final InputStream stream = newIn == null ? INPUTSTREAM_NULL : newIn;
		stdInRedirector = (process) -> new InputToOutputStreamThread(stream, process.getOutputStream());
		return this;
	}
	
	/**
	 * Redirects standard in.
	 * @param newIn the new input stream to redirect STDIN from. Null is acceptable.
	 * @return this, for chaining.
	 */
	public ProcessCallable setIn(final Reader newIn)
	{
		final Reader stream = newIn == null ? READER_NULL : newIn;
		stdInRedirector = (process) -> new ReaderToWriterThread(stream, new OutputStreamWriter(process.getOutputStream()));
		return this;
	}
	
	/**
	 * Redirects standard in.
	 * @param sourceFile the file to read from to pipe into the process's input.
	 * @return this, for chaining.
	 */
	public ProcessCallable setIn(final File sourceFile)
	{
		stdInRedirector = (process) -> new InputToOutputStreamThread(new FileInputStream(sourceFile), process.getOutputStream()) 
		{
			@Override
			public void afterClose() throws IOException
			{
				sourceStream.close();
			}
		};
		return this;
	}
	
	/**
	 * Sets the listener to use for exceptions that may occur on STDOUT streaming. 
	 * @param listener the listener to use.
	 * @return this, for chaining.
	 */
	public ProcessCallable setOutListener(StreamExceptionListener listener) 
	{
		this.stdOutListener = listener;
		return this;
	}

	/**
	 * Sets the listener to use for exceptions that may occur on STDERR streaming. 
	 * @param listener the listener to use.
	 * @return this, for chaining.
	 */
	public ProcessCallable setErrListener(StreamExceptionListener listener) 
	{
		this.stdErrListener = listener;
		return this;
	}

	/**
	 * Sets the listener to use for exceptions that may occur on STDIN streaming. 
	 * @param listener the listener to use.
	 * @return this, for chaining.
	 */
	public ProcessCallable setInListener(StreamExceptionListener listener) 
	{
		this.stdInListener = listener;
		return this;
	}

	/**
	 * Redirects the STDOUT of this ProcessCallable to the STDIN
	 * of another ProcessCallable, piping the output to the input.
	 * @param target the target ProcessCallable. 
	 * @return this, for chaining.
	 */
	public ProcessCallable pipeOutTo(ProcessCallable target)
	{
		PipedOutputStream pipeOut = new PipedOutputStream();
		PipedInputStream pipeIn = new PipedInputStream();
		this.setOut(pipeOut);
		target.setIn(pipeIn);
		try {
			pipeOut.connect(pipeIn);
		} catch (IOException e) {
			// Not possible. Only thrown if already connected.
		}
		return this;
	}
	
	/**
	 * Convenience function for:
	 * <pre><code>
	 * processCallable.setOut(System.out);
	 * processCallable.setErr(System.err);
	 * </code></pre>
	 * @return this, for chaining.
	 */
	public ProcessCallable inheritOut()
	{
		setOut(System.out);
		setErr(System.err);
		return this;
	}
	
	/**
     * Computes a result, or throws an exception if unable to do so.
     * <p> This will create the process, bind the redirects, and wait for its completion and
     * thread termination, returning the Process result.
     * @return computed result
     * @throws Exception if unable to compute a result
     */
	@Override
	public final Integer call() throws Exception 
	{
		Process process = exec();
		
		PipeThread outThread = stdOutRedirector != null ? stdOutRedirector.getThread(process) : null;
		PipeThread errThread = stdErrRedirector != null ? stdErrRedirector.getThread(process) : null;
		PipeThread inThread =  stdInRedirector  != null ? stdInRedirector.getThread(process)  : null;

		long instanceId = INSTANCE_ID.getAndIncrement();

		if (outThread != null)
		{
			outThread.setName(callName + "-out-" + outThread.getName() + "-" + instanceId);
			outThread.start();
		}
		if (errThread != null)
		{
			errThread.setName(callName + "-err-" + errThread.getName() + "-" + instanceId);
			errThread.start();
		}
		if (inThread != null)
		{
			inThread.setName(callName + "-in-" + inThread.getName() + "-" + instanceId);
			inThread.start();
		}

		int out = process.waitFor();
		
		if (outThread != null)
		{
			outThread.join();
			if (stdOutListener != null)
				stdOutListener.onException(outThread.getException());
		}
		if (errThread != null)
		{
			errThread.join();
			if (stdErrListener != null)
				stdErrListener.onException(errThread.getException());
		}
		if (inThread != null)
		{
			inThread.join();
			if (stdInListener != null)
				stdInListener.onException(inThread.getException());
		}
		
		return out;
	}
	
	/**
	 * Executes this process, returning a reference to it, but <b>not attaching
	 * any of the provided redirects.</b>
	 * <p> This is useful for spawning processes that do not require an active 
	 * monitor or input transfer that will eventually be orphaned by this program.
	 * @return the process created as a result.
	 * @throws IOException if the process could not be started.
	 */
	public Process exec() throws IOException
	{
		String[] cmd = command.toArray(new String[command.size()]);
		String[] env = envMap.isEmpty() ? null : new String[envMap.size() * 2];
		if (env != null)
		{
			int i = 0;
			for (Map.Entry<String, Object> entry : envMap.entrySet())
			{
				env[i++] = entry.getKey();
				env[i++] = String.valueOf(entry.getValue());
			}
		}
	
		return Runtime.getRuntime().exec(cmd, env, workingDirectory);
	}
	
	/**
	 * A listener that this callable uses when an {@link IOException} occurs on
	 * one of the Threads that this creates to pipe the standard streams to and from the opened Process. 
	 */
	@FunctionalInterface
	public interface StreamExceptionListener
	{
		/**
		 * Called when an IOException occurs.
		 * @param exception the exception that occurred.
		 */
		void onException(IOException exception);
	}
	
	/**
	 * BufferedReader to Writer stream thread.
	 * Transfers characters buffered at one line at a time until the source stream is closed.
	 * <p> The thread terminates if the reader is closed. The target writer is not closed.
	 */
	private static class LineReaderToWriterThread extends PipeThread
	{
		protected BufferedReader sourceReader;
		protected PrintStream targetPrintStream;
		private Object targetMutex;
		
		private LineReaderToWriterThread(BufferedReader reader)
		{
			super("LineReaderToWriterThread");
			this.sourceReader = reader;
			this.targetMutex = new Object();
		}
		
		/**
		 * Creates a new thread that, when started, transfers characters one line at a time until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the BufferedReader to read from.
		 * @param stream the PrintStream to write to.
		 */
		public LineReaderToWriterThread(BufferedReader reader, PrintStream stream)
		{
			this(reader);
			this.targetPrintStream = stream;
		}
	
		@Override
		public void relay() throws IOException 
		{
			String line;
			while ((line = sourceReader.readLine()) != null)
			{
				synchronized (targetMutex)
				{
					targetPrintStream.println(line);
					targetPrintStream.flush();
				}
			}
		}
		
		/**
		 * Called after the source stream hits EOF or closes,
		 * but before the Thread terminates.
		 * <p> Does nothing by default.
		 */
		public void afterClose()
		{
			// Do nothing by default.
		}
		
	}

	/**
	 * Reader to Writer stream thread.
	 * Transfers characters until the source stream is closed.
	 * <p> The thread terminates if the reader is closed. The target writer is not closed.
	 */
	private static class ReaderToWriterThread extends PipeThread
	{
		protected Reader sourceReader;
		protected Writer targetWriter;
		
		/**
		 * Creates a new thread that, when started, transfers characters until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the Reader to read from.
		 * @param writer the Writer to write to.
		 */
		private ReaderToWriterThread(Reader reader, Writer writer)
		{
			super("ReaderToWriterThread");
			this.sourceReader = reader;
			this.targetWriter = writer;
		}
	
		@Override
		public void relay() throws IOException
		{
			int buf;
			char[] buffer = new char[8192]; 
			while ((buf = sourceReader.read(buffer)) > 0)
			{
				synchronized (targetWriter)
				{
					targetWriter.write(buffer, 0, buf);
					targetWriter.flush();
				}
			}
		}
		
		/**
		 * Called after the source stream hits EOF or closes,
		 * but before the Thread terminates.
		 * <p> Does nothing by default.
		 * @throws IOException if an IOException occurs.
		 */
		public void afterClose() throws IOException
		{
			// Do nothing by default.
		}
		
	}

	/**
	 * Input to Output stream thread.
	 * Transfers until the source stream is closed.
	 * <p> The thread terminates if the input stream is closed. The target output stream is not closed.
	 */
	private static class InputToOutputStreamThread extends PipeThread
	{
		protected InputStream sourceStream;
		protected OutputStream targetStream;
		
		/**
		 * Creates a new thread that, when started, transfers bytes until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param sourceStream the InputStream to read from. 
		 * @param targetStream the OutputStream to write to.
		 */
		private InputToOutputStreamThread(InputStream sourceStream, OutputStream targetStream)
		{
			super("InputToOutputStreamThread");
			this.sourceStream = sourceStream;
			this.targetStream = targetStream;
		}
		
		@Override
		protected void relay() throws IOException
		{
			int buf;
			byte[] buffer = new byte[8192]; 
			while ((buf = sourceStream.read(buffer)) > 0)
			{
				synchronized (targetStream)
				{
					targetStream.write(buffer, 0, buf);
					targetStream.flush();
				}
			}
		}
		
	}

	private static abstract class PipeThread extends Thread
	{
		private IOException exception;

		protected PipeThread(String name)
		{
			super(name);
			this.exception = null;
		}
		
		@Override
		public final void run() 
		{
			try {
				relay();
			} catch (IOException e) {
				exception = e;
			} finally {
				try {
					afterClose();
				} catch (IOException e) {
					exception = e;
				}
			}
		}
		
		/**
		 * Performs the transfer until end of stream.
		 * @throws IOException if an IO Exception occurs. 
		 */
		protected abstract void relay() throws IOException;
		
		/**
		 * Called after the source stream hits EOF or closes,
		 * but before the Thread terminates.
		 * <p> Does nothing by default.
		 * @throws IOException if an IO Exception occurs. 
		 */
		protected void afterClose() throws IOException
		{
			// Do nothing by default.
		}

		/**
		 * @return the exception thrown during streaming, if any.
		 */
		public IOException getException()
		{
			return exception;
		}
		
	}
	
	@FunctionalInterface
	private interface PipeThreadCreator
	{
		/**
		 * Creates a PipeThread for a stream in a process.
		 * @param process the process to wrap.
		 * @return a new thread.
		 * @throws Exception if an exception occurs creating a thread.
		 */
		PipeThread getThread(Process process) throws Exception;
	}
	
}

