/*******************************************************************************
 * Copyright (c) 2020-2026 Matt Tropiano
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A single process wrapper because Processes are the worst to deal with.
 * This Process builder is abstracted as a {@link Callable}, so it and other instances
 * can be added to an executor.
 * <p> If the same output reference is provided for more than one stream (STDOUT and STDERR),
 * This will merge the two streams and write to them synchronously. For <b>files</b> however,
 * you'll have to create a {@link FileOutputStream} for it to take advantage.
 * @author Matthew Tropiano
 */
public class ProcessCallable implements Future<Integer>, Callable<Integer>
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

	/* ==================================================================== */
	
	// Builder	
	
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
	private ProcessCallable pipeToCallable;
	
	// Run state

	private final Object waitMutex;
	private volatile boolean cancelled;
	private volatile boolean done;
	private volatile Process runningProcess;
	private PipeThread outThread;
	private PipeThread errThread;
	private PipeThread inThread;
	private volatile Exception exception;
	private volatile Integer finishedResult;

	private ProcessCallable(String callName)
	{
		setCallName(callName);
		
		this.command = new LinkedList<>();
		this.envMap = new HashMap<>();
		this.workingDirectory = null;
		this.stdOutRedirector = (process) -> new InputToOutputStreamThread(process.getInputStream(), OUTPUTSTREAM_NULL, stdOutListener);
		this.stdErrRedirector = (process) -> new InputToOutputStreamThread(process.getErrorStream(), OUTPUTSTREAM_NULL, stdErrListener);
		this.stdInRedirector =  (process) -> new InputToOutputStreamThread(INPUTSTREAM_NULL, process.getOutputStream(), stdInListener);
		this.stdOutListener = null;
		this.stdErrListener = null;
		this.stdInListener = null;
		this.pipeToCallable = null;
		
		this.waitMutex = new Object();
		this.cancelled = false;
		this.done = false;
		this.runningProcess = null;
		this.outThread = null;
		this.errThread = null;
		this.inThread = null;
		this.exception = null;
		this.finishedResult = null;
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
	 * @return this callable's call name. 
	 */
	public String getCallName()
	{
		return callName;
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
	 * Adds a series of command line arguments.
	 * Command line arguments are sent in the order added.
	 * @param args the command arguments to add.
	 * @return this, for chaining.
	 */
	public ProcessCallable args(String ... args)
	{
		for (int i = 0; i < args.length; i++)
			arg(args[i]);
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
		stdOutRedirector = (process) -> new InputToOutputStreamThread(process.getInputStream(), stream, stdOutListener); 
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
		stdOutRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getInputStream()), stream, stdOutListener); 
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
		stdOutRedirector = (process) -> new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getInputStream())), stream, stdOutListener); 
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
		stdOutRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getInputStream(), encoding), stream, stdOutListener); 
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
		stdOutRedirector = (process) -> new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getInputStream(), encoding)), stream, stdOutListener); 
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
		stdOutRedirector = (process) -> new InputToOutputStreamThread(process.getInputStream(), new FileOutputStream(target), stdOutListener);
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
		stdOutRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getInputStream()), new OutputStreamWriter(new FileOutputStream(target), outCharset), stdOutListener);
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
		stdOutRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getInputStream(), processCharset), new OutputStreamWriter(new FileOutputStream(target), outCharset), stdOutListener);
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
		stdErrRedirector = (process) -> new InputToOutputStreamThread(process.getErrorStream(), stream, stdErrListener); 
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
		stdErrRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getErrorStream()), stream, stdErrListener); 
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
		stdErrRedirector = (process) -> new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getErrorStream())), stream, stdErrListener); 
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
		stdErrRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getErrorStream(), encoding), stream, stdErrListener); 
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
		stdErrRedirector = (process) -> new LineReaderToWriterThread(new BufferedReader(new InputStreamReader(process.getErrorStream(), encoding)), stream, stdErrListener); 
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
		stdErrRedirector = (process) -> new InputToOutputStreamThread(process.getErrorStream(), new FileOutputStream(target), stdErrListener);
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
		stdErrRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getErrorStream()), new OutputStreamWriter(new FileOutputStream(target), outCharset), stdErrListener);
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
		stdErrRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(process.getErrorStream(), processCharset), new OutputStreamWriter(new FileOutputStream(target), outCharset), stdErrListener);
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
		stdInRedirector = (process) -> new InputToOutputStreamThread(stream, process.getOutputStream(), stdInListener);
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
		stdInRedirector = (process) -> new ReaderToWriterThread(stream, new OutputStreamWriter(process.getOutputStream()), stdInListener);
		return this;
	}
	
	/**
	 * Redirects standard in.
	 * @param sourceFile the file to read from to pipe into the process's input.
	 * @return this, for chaining.
	 */
	public ProcessCallable setIn(final File sourceFile)
	{
		final File src = sourceFile == null ? NULL_FILE : sourceFile;
		stdInRedirector = (process) -> new InputToOutputStreamThread(new FileInputStream(src), process.getOutputStream(), stdInListener);
		return this;
	}
	
	/**
	 * Redirects standard in.
	 * @param sourceCharset the charset encoding of the file source.
	 * @param sourceFile the file to read from to pipe into the process's input.
	 * @return this, for chaining.
	 */
	public ProcessCallable setIn(final Charset sourceCharset, final File sourceFile)
	{
		final File src = sourceFile == null ? NULL_FILE : sourceFile;
		stdInRedirector = (process) -> new ReaderToWriterThread(new InputStreamReader(new FileInputStream(src), sourceCharset), new OutputStreamWriter(process.getOutputStream()), stdInListener);
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
	 * Redirects the STDOUT of this ProcessCallable to the STDIN of another ProcessCallable, piping the output to the input.
	 * The ProcessCallable passed in is also executed at the same time as this ProcessCallable, when {@link #call()} is called.
	 * @param target the target ProcessCallable. 
	 * @return this, for chaining.
	 */
	public ProcessCallable pipeOutTo(ProcessCallable target)
	{
		BufferedRing ring = new BufferedRing(8192); 
		InputStream pipeIn = ring.getInputStream();
		OutputStream pipeOut = ring.getOutputStream();
		setOut(pipeOut);
		target.setIn(pipeIn);
		pipeToCallable = target;
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
		
		// fix empty args.
		for (int i = 0; i < cmd.length; i++)
		{
			if (cmd[i].length() == 0)
				cmd[i] = "\"\"";
		}
		
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
	 * Executes and attaches all relevant streams and processes to the Process.
	 * @throws Exception if the process could not be started.
	 */
	private void executePipeline() throws Exception
	{
		// start pipe targets.
		if (pipeToCallable != null)
			pipeToCallable.executePipeline();
		
		runningProcess = exec();
		outThread = stdOutRedirector != null ? stdOutRedirector.getThread(runningProcess) : null;
		errThread = stdErrRedirector != null ? stdErrRedirector.getThread(runningProcess) : null;
		inThread =  stdInRedirector  != null ? stdInRedirector.getThread(runningProcess)  : null;

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
	}
	
	/**
	 * Waits for the pipeline to finish, returning the last result in the pipeline.
	 * @throws InterruptedException if a thread is interrupted during wait.
	 */
	private Integer waitForPipeline() throws InterruptedException
	{
		int out = runningProcess.waitFor();
		if (pipeToCallable != null)
			return pipeToCallable.waitForPipeline();
		return out;
	}
	
	/**
	 * Cancels the whole pipeline.
	 * @param mayInterruptIfRunning if true, forcibly cancel it.
	 * @return true if one or more processes were cancelled before they finished.
	 */
	private boolean cancelPipeline(boolean mayInterruptIfRunning)
	{
		if (runningProcess != null)
		{
			if (mayInterruptIfRunning)
				runningProcess.destroyForcibly();
			else
				runningProcess.destroy();
			cancelled = true;
		}
		
		if (pipeToCallable != null)
			return pipeToCallable.cancelPipeline(mayInterruptIfRunning);
		else
			return cancelled;
	}

	/**
	 * Cleans up execution for this process.
	 * @throws InterruptedException if a thread is interrupted on join.
	 */
	private void cleanUpPipeline() throws InterruptedException
	{
		runningProcess = null;
	
		if (outThread != null)
			outThread.join();
		if (errThread != null)
			errThread.join();
		if (inThread != null)
			inThread.join();
		
		if (pipeToCallable != null)
			pipeToCallable.cleanUpPipeline();
	}

	/**
	 * Computes a result, or throws an exception if unable to do so.
	 * <p> This will create the process, bind the redirects, and wait for its completion and
	 * thread termination, returning the Process result.
	 * @return the computed result of the process, or null if cancelled.
	 * @throws Exception if unable to compute a result
	 */
	@Override
	public final Integer call() throws Exception 
	{
		if (runningProcess != null)
			throw new IllegalStateException("Process \"" + callName + "\" was already started.");
		
		executePipeline();
		
		try {
			finishedResult = waitForPipeline();
		} catch (InterruptedException t) {
			exception = t;
		} finally {
			cleanUpPipeline();
			done = true;
			synchronized (waitMutex)
			{
				waitMutex.notifyAll();
			}
		}
		
		if (exception != null)
			throw exception;
		else
			return finishedResult;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public Integer get() throws InterruptedException, ExecutionException
	{
		while (!isDone())
		{
			synchronized (waitMutex)
			{
				if (!isDone())
					waitMutex.wait();
			}
		}
		
		if (exception != null)
			throw new ExecutionException(exception);
		
		return finishedResult;
	}

	@Override
	public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		if (!isDone())
		{
			synchronized (waitMutex)
			{
				unit.timedWait(waitMutex, timeout);
				if (!isDone())
					throw new TimeoutException("Wait for process completion timed out!");
			}
		}
		return get();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) 
	{
		return cancelPipeline(mayInterruptIfRunning);
	}

	/**
	 * Cancels this process.
	 * @return true if this process was cancelled, false if not (because it was already terminated).
	 * @see #cancel(boolean)
	 */
	public boolean cancel() 
	{
		return cancel(false);
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
	 * This buffer is for piped streams, such that shared data is not lost when one stream closes.
	 */
	private static class BufferedRing
	{
		private Object waitMutex;
		private byte[] buffer;
		private int size;
		private int readidx;
		private int writeidx;
		private boolean inClosed;
		private boolean outClosed;
		private InputStream inputStream;
		private OutputStream outputStream;
		
		public BufferedRing(int capacity)
		{
			this.waitMutex = new Object();
			this.buffer = new byte[capacity];
			this.readidx = 0;
			this.writeidx = 0;
			this.inClosed = false;
			this.outClosed = false;
			
			this.inputStream = new InputStream() 
			{
				@Override
				public int read() throws IOException 
				{
					synchronized (waitMutex)
					{
						if (inClosed)
							throw new IOException("stream is closed");
						
						if (readidx == buffer.length)
							readidx = 0;
						if (size == 0) // buffer empty.
						{
							if (outClosed) // nothing coming
								return -1;
							else
							{
								awaitRead();
								if (outClosed)
									return -1;
							}
						}
						
						int out = buffer[readidx++];
						size--;
						return out;
					}
				}
				
				@Override
				public void close() throws IOException 
				{
					synchronized (waitMutex)
					{
						inClosed = true;
					}
				}
				
			};
			
			this.outputStream = new OutputStream() 
			{
				@Override
				public void write(int b) throws IOException 
				{
					synchronized (waitMutex)
					{
						if (outClosed)
							throw new IOException("stream is closed");

						if (writeidx == buffer.length)
							writeidx = 0;
						if (size == buffer.length) // buffer full.
							expandBuffer();
						buffer[writeidx++] = (byte)b;
						size++;
						waitMutex.notify();
					}
				}
				
				@Override
				public void close() throws IOException 
				{
					synchronized (waitMutex)
					{
						outClosed = true;
						waitMutex.notify();
					}
				}
			};
		}
		
		private void expandBuffer()
		{
			byte[] newbuf = new byte[buffer.length * 2];
			int segment = buffer.length - readidx;
			System.arraycopy(buffer, readidx, newbuf, 0, segment);
			System.arraycopy(buffer, 0, newbuf, segment, buffer.length - segment);
			writeidx = buffer.length;
			readidx = 0;
			buffer = newbuf;
		}
		
		private void awaitRead()
		{
			while (size == 0 && !outClosed)
			{
				synchronized (waitMutex)
				{
					try {
						waitMutex.wait();
					} catch (InterruptedException e) {
						try {
							inputStream.close();
						} catch (IOException e1) {
							// Eat exception.
						}
					}
				}
			}
		}
		
		public InputStream getInputStream() 
		{
			return inputStream;
		}
		
		public OutputStream getOutputStream()
		{
			return outputStream;
		}
		
	}
	
	/**
	 * BufferedReader to Writer stream thread.
	 * Transfers characters buffered at one line at a time until the reader reaches the end.
	 * <p> The thread terminates if the reader is terminated.
	 */
	private static class LineReaderToWriterThread extends PipeThread
	{
		protected BufferedReader sourceReader;
		protected PrintStream targetPrintStream;
		private Object targetMutex;
		
		private LineReaderToWriterThread(BufferedReader reader, StreamExceptionListener listener)
		{
			super("LineReaderToWriterThread", listener);
			this.sourceReader = reader;
			this.targetMutex = new Object();
		}
		
		/**
		 * Creates a new thread that, when started, transfers characters one line at a time until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the BufferedReader to read from.
		 * @param stream the PrintStream to write to.
		 */
		private LineReaderToWriterThread(BufferedReader reader, PrintStream stream, StreamExceptionListener listener)
		{
			this(reader, listener);
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
		
		@Override
		protected void close() throws IOException 
		{
			sourceReader.close();
			targetPrintStream.close();
		}
		
	}

	/**
	 * Reader to Writer stream thread.
	 * Transfers characters until the source stream is closed.
	 * <p> The thread terminates if the reader is terminated.
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
		private ReaderToWriterThread(Reader reader, Writer writer, StreamExceptionListener listener)
		{
			super("ReaderToWriterThread", listener);
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
		
		@Override
		protected void close() throws IOException 
		{
			sourceReader.close();
			targetWriter.close();
		}
		
	}

	/**
	 * Input to Output stream thread.
	 * Transfers until the source stream is closed.
	 * <p> The thread terminates if the input stream is terminated.
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
		private InputToOutputStreamThread(InputStream sourceStream, OutputStream targetStream, StreamExceptionListener listener)
		{
			super("InputToOutputStreamThread", listener);
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
		
		@Override
		protected void close() throws IOException 
		{
			sourceStream.close();
			targetStream.close();
		}
		
	}

	private static abstract class PipeThread extends Thread
	{
		private StreamExceptionListener listener;
		
		protected PipeThread(String name, StreamExceptionListener listener)
		{
			super(name);
			this.listener = listener;
		}
		
		@Override
		public final void run() 
		{
			try {
				relay();
			} catch (IOException e) {
				if (listener != null)
					listener.onException(e);
			} finally {
				try {
					close();
				} catch (IOException e) {
					if (listener != null)
						listener.onException(e);
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
		protected abstract void close() throws IOException;

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

