/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * HTTP Utilities.
 * <p>All of the HTTP functions are <em>synchronous</em>. If you want to make them asynchronous,
 * you will have to wire them up to your favorite asynchronous task library.
 * @author Matthew Tropiano
 */
public final class HTTPUtils
{
	/** HTTP Method: GET. */
	public static final String HTTP_METHOD_GET = "GET"; 
	/** HTTP Method: HEAD. */
	public static final String HTTP_METHOD_HEAD = "HEAD";
	/** HTTP Method: DELETE. */
	public static final String HTTP_METHOD_DELETE = "DELETE"; 
	/** HTTP Method: OPTIONS. */
	public static final String HTTP_METHOD_OPTIONS = "OPTIONS"; 
	/** HTTP Method: TRACE. */
	public static final String HTTP_METHOD_TRACE = "TRACE";
	/** HTTP Method: POST. */
	public static final String HTTP_METHOD_POST = "POST";
	/** HTTP Method: PUT. */
	public static final String HTTP_METHOD_PUT = "PUT";

	/** Default timeout in milliseconds. */
	public static final AtomicInteger DEFAULT_TIMEOUT_MILLIS = new AtomicInteger(5000); 
	/** Thread pool for async requests. */
	private static final AtomicReference<ThreadPoolExecutor> HTTP_THREAD_POOL = new AtomicReference<ThreadPoolExecutor>(null);
	
	// Not instantiable.
	private HTTPUtils() {}
	
	private static final Charset UTF8 = Charset.forName("utf-8");
	private static final String[] VALID_HTTP = new String[]{"http", "https"};
	private static final byte[] URL_RESERVED = "!#$%&'()*+,/:;=?@[]".getBytes(UTF8);
	private static final byte[] URL_UNRESERVED = "-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~".getBytes(UTF8);
	private static final ThreadLocal<SimpleDateFormat> ISO_DATE = ThreadLocal.withInitial(()->
	{
		SimpleDateFormat out = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		out.setTimeZone(TimeZone.getTimeZone("GMT"));
		return out;
	});
	
	/**
	 * The default executor to use for sending async requests.
	 */
	private static class HTTPThreadPoolExecutor extends ThreadPoolExecutor
	{
		private static final String THREADNAME = "HTTPRequestWorker-";
		
		private static final int CORE_SIZE = 0;
		private static final int MAX_SIZE = 20;
		private static final long KEEPALIVE = 5L;
		private static final TimeUnit KEEPALIVE_UNIT = TimeUnit.SECONDS;

		private static final AtomicLong REQUEST_ID = new AtomicLong(0L);
		
		private HTTPThreadPoolExecutor()
		{
			super(CORE_SIZE, MAX_SIZE, KEEPALIVE, KEEPALIVE_UNIT, new LinkedBlockingQueue<>(), (runnable) -> 
			{
				Thread out = new Thread(runnable);
				out.setName(THREADNAME + REQUEST_ID.getAndIncrement());
				out.setDaemon(true);
				return out;
			});
		}
	}
	
	/**
	 * A single instance of a spawned, asynchronous executable task.
	 * Note that this class is a type of {@link RunnableFuture} - this can be used in places
	 * that {@link Future}s can also be used.
	 * @param <T> the result type.
	 */
	public static abstract class HTTPRequestFuture<T> implements RunnableFuture<T>
	{
		// Source request.
		protected HTTPRequest request;
		protected HTTPResponse response;
		protected AtomicBoolean cancelSwitch;
	
		// Locks
		private Object waitMutex;
	
		// State
		private Thread executor;
		private boolean done;
		private boolean running;
		private Throwable exception;
		private T finishedResult;
	
		private HTTPRequestFuture(HTTPRequest request)
		{
			this.request = request;
			this.response = null;
			this.cancelSwitch = new AtomicBoolean(false);
			this.waitMutex = new Object();
	
			this.executor = null;
			this.done = false;
			this.running = false;
			this.exception = null;
			this.finishedResult = null;
		}
		
		@Override
		public final void run()
		{
			executor = Thread.currentThread();
			running = true;
	
			try {
				finishedResult = execute();
			} catch (Throwable e) {
				exception = e;
			}
			
			running = false;
			done = true;
			executor = null;
			synchronized (waitMutex)
			{
				waitMutex.notifyAll();
			}
		}
	
		/**
		 * Checks if this task instance is being worked on by a thread.
		 * @return true if so, false if not.
		 */
		public final boolean isRunning()
		{
			return running;
		}
	
		@Override
		public final boolean isDone()
		{
			return done;
		}
	
		/**
		 * Gets the thread that is currently executing this future.
		 * If this is done or waiting for execution, this returns null.
		 * @return the executor thread, or null.
		 */
		public final Thread getExecutor()
		{
			return executor;
		}
		
		/**
		 * Makes the calling thread wait indefinitely for this task instance's completion.
		 * @throws InterruptedException if the current thread was interrupted while waiting.
		 */
		public final void waitForDone() throws InterruptedException
		{
			while (!isDone())
			{
				liveLockCheck();
				synchronized (waitMutex)
				{
					waitMutex.wait();
				}
			}
		}

		/**
		 * Makes the calling thread wait for this task instance's completion for, at most, the given interval of time.
		 * @param time the time to wait.
		 * @param unit the time unit of the timeout argument.
		 * @throws InterruptedException if the current thread was interrupted while waiting.
		 */
		public final void waitForDone(long time, TimeUnit unit) throws InterruptedException
		{
			if (!isDone())
			{
				liveLockCheck();
				synchronized (waitMutex)
				{
					unit.timedWait(waitMutex, time);
				}
			}
		}

		/**
		 * Gets the response encapsulation from a call, waiting for the call to be in a "done" state.
		 * <p> If the call errored out or was cancelled before the request was sent, this will be null.
		 * <p> <strong>NOTE:</strong> The response may be open if this call is supposed to return an open response to be read by the application.
		 * @return the response, or null if no response was captured due to an error.
		 * @see #join()
		 * @see #getException()
		 * @see #result()
		 */
		public final HTTPResponse getResponse()
		{
			join();
			return response;
		}

		/**
		 * Gets the exception thrown as a result of this instance completing, making the calling thread wait for its completion.
		 * @return the exception thrown by the encapsulated task, or null if no exception.
		 */
		public final Throwable getException()
		{
			join();
			return exception;
		}

		@Override
		public final T get() throws InterruptedException, ExecutionException
		{
			liveLockCheck();
			waitForDone();
			if (isCancelled())
				throw new CancellationException("task was cancelled");
			if (getException() != null)
				throw new ExecutionException(getException());
			return finishedResult;
		}

		@Override
		public final T get(long time, TimeUnit unit) throws TimeoutException, InterruptedException, ExecutionException
		{
			liveLockCheck();
			waitForDone(time, unit);
			if (!isDone())
				throw new TimeoutException("wait timed out");
			if (isCancelled())
				throw new CancellationException("task was cancelled");
			if (getException() != null)
				throw new ExecutionException(getException());
			return finishedResult;
		}

		/**
		 * Performs a {@link #get()} and on success, calls the success function.
		 * If an exception would have happened, the success function is not called.
		 * @param <R> the return type after the function call.
		 * @param onSuccess the function to call on success with the result object, returning the return object. 
		 * 		If null when this would be called, this returns null.
		 * @return the result from the success function, or null if an exception happened.
		 */
		public final <R> R getAndThen(Function<T, R> onSuccess)
		{
			return getAndThen(onSuccess, null);
		}

		/**
		 * Performs a {@link #get()} and on success, calls the success function, or calls the exception function on an exception.
		 * @param <R> the return type after the function.
		 * @param onSuccess the function to call on success with the result object, returning the return object. 
		 * 		If null when this would be called, this returns null.
		 * @param onException the function to call on exception. If null when this would be called, this returns null.
		 * @return the result from the success function, or the result from the exception function if an exception happened.
		 */
		public final <R> R getAndThen(Function<T, R> onSuccess, Function<Throwable, R> onException)
		{
			try {
				return onSuccess.apply(get());
			} catch (Exception e) {
				return onException != null ? onException.apply(exception) : null;
			}
		}

		/**
		 * Performs a {@link #get()} and on success, calls the success function.
		 * If an exception would have happened, the success function is not called.
		 * @param <R> the return type after the function call.
		 * @param time the maximum time to wait.
		 * @param unit the time unit of the timeout argument.
		 * @param onSuccess the function to call on success with the result object, returning the return object. 
		 * 		If null when this would be called, this returns null.
		 * @return the result from the success function, or null if an exception happened or a timeout occurred.
		 */
		public final <R> R getAndThen(long time, TimeUnit unit, Function<T, R> onSuccess)
		{
			return getAndThen(time, unit, onSuccess, null, null);
		}

		/**
		 * Performs a {@link #get()} and on success, calls the success function.
		 * If an exception would have happened, the success function is not called.
		 * @param <R> the return type after the function call.
		 * @param time the maximum time to wait.
		 * @param unit the time unit of the timeout argument.
		 * @param onSuccess the function to call on success with the result object, returning the return object. 
		 * 		If null when this would be called, this returns null.
		 * @param onTimeout the function to call on wait timeout. First Parameter is the timeout in milliseconds. If null when this would be called, this returns null.
		 * @return the result from the success function, or null if an exception happened.
		 */
		public final <R> R getAndThen(long time, TimeUnit unit, Function<T, R> onSuccess, Function<Long, R> onTimeout)
		{
			return getAndThen(time, unit, onSuccess, onTimeout, null);
		}

		/**
		 * Performs a {@link #get()} and on success, calls the success function, or calls the exception function on an exception.
		 * @param <R> the return type after the function.
		 * @param time the maximum time to wait.
		 * @param unit the time unit of the timeout argument.
		 * @param onSuccess the function to call on success with the result object, returning the return object.
		 * @param onTimeout the function to call on wait timeout. First Parameter is the timeout in milliseconds. If null when this would be called, this returns null.
		 * @param onException the function to call on exception. If null when this would be called, this returns null.
		 * @return the result from the success function, or the result from the exception function if an exception happened.
		 */
		public final <R> R getAndThen(long time, TimeUnit unit, Function<T, R> onSuccess, Function<Long, R> onTimeout, Function<Throwable, R> onException)
		{
			try {
				return onSuccess != null ? onSuccess.apply(get(time, unit)) : null;
			} catch (TimeoutException e) {
				return onTimeout != null ? onTimeout.apply(TimeUnit.MILLISECONDS.convert(time, unit)) : null;
			} catch (Exception e) {
				return onException != null ? onException.apply(exception) : null;
			}
		}

		/**
		 * Attempts to return the result of this instance, making the calling thread wait for its completion.
		 * <p>This is for convenience - this is like calling {@link #get()}, except it will only throw an
		 * encapsulated {@link RuntimeException} with an exception that {@link #get()} would throw as a cause.
		 * @return the result. Can be null if no result is returned, or this was cancelled before the return.
		 * @throws RuntimeException if a call to {@link #get()} instead of this would throw an exception.
		 * @throws IllegalStateException if the thread processing this future calls this method.
		 */
		public final T result()
		{
			liveLockCheck();
			try {
				waitForDone();
			} catch (InterruptedException e) {
				throw new RuntimeException("wait was interrupted", getException());
			}
			if (getException() != null)
				throw new RuntimeException("exception on result", getException());
			return finishedResult;
		}

		/**
		 * Attempts to return the result of this instance, without waiting for its completion.
		 * <p>If {@link #isDone()} is false, this is guaranteed to return <code>null</code>.
		 * @return the current result, or <code>null</code> if not finished.
		 */
		public final T resultNonBlocking()
		{
			return finishedResult;
		}

		/**
		 * Makes the calling thread wait until this task has finished, returning nothing.
		 * This differs from {@link #waitForDone()} such that it eats a potential {@link InterruptedException}.
		 * @throws IllegalStateException if the thread processing this future calls this method.
		 */
		public final void join()
		{
			try {
				waitForDone();
			} catch (Exception e) {
				// Eat exception.
			}
		}

		/**
	     * Convenience method for: <code>cancel(false)</code>.
	     * @return {@code false} if the task could not be cancelled,
	     * typically because it has already completed normally;
	     * {@code true} otherwise
	     */
		public final boolean cancel()
		{
			return cancel(false);
		}
	
		@Override
		public final boolean cancel(boolean mayInterruptIfRunning)
		{
			if (isDone())
			{
				return false;
			}
			else
			{
				if (mayInterruptIfRunning && executor != null)
					executor.interrupt();
				cancelSwitch.set(true);
				return true;
			}
		}
	
		@Override
		public final boolean isCancelled() 
		{
			return cancelSwitch.get();
		}
		
		// Checks for livelocks.
		private void liveLockCheck()
		{
			if (executor == Thread.currentThread())
				throw new IllegalStateException("Attempt to make executing thread wait for this result.");
		}
		
		/**
		 * Executes this instance's callable payload.
		 * @return the result from the execution.
		 * @throws Throwable for any exception that may occur.
		 */
		protected abstract T execute() throws Throwable;
	
		/** Sends request and gets a response. */
		private static class Response extends HTTPRequestFuture<HTTPResponse>
		{
			private Response(HTTPRequest request)
			{
				super(request);
			}
	
			@Override
			protected HTTPResponse execute() throws Throwable
			{
				return response = request.send(cancelSwitch);
			}
		}
		
		/** Sends request and gets an object. */
		private static class ObjectResponse<T> extends HTTPRequestFuture<T>
		{
			private HTTPReader<T> reader;
			
			private ObjectResponse(HTTPRequest request, HTTPReader<T> reader)
			{
				super(request);
				this.reader = reader;
			}
	
			@Override
			protected T execute() throws Throwable
			{
				try (HTTPResponse resp = (response = request.send(cancelSwitch)))
				{
					return resp.read(reader, cancelSwitch);
				}
			}
		}
	}

	/**
	 * Interface for monitoring change in a data transfer.
	 */
	@FunctionalInterface
	public interface TransferMonitor
	{
		/**
		 * Called when a series of bytes move from one place to another. 
		 * @param current the current amount in bytes.
		 * @param max the maximum amount/target in bytes, if any.
		 */
		void onProgressChange(long current, Long max);
		
		/**
		 * A transfer monitor that does nothing.
		 */
		static TransferMonitor NULL_MONITOR = (current, max) -> {};
	}
	
	/**
	 * Interface for reading an HTTPResponse from a URL call.
	 * @param <R> the return type.
	 */
	@FunctionalInterface
	public interface HTTPReader<R>
	{
		/**
		 * An HTTP Reader that reads byte content and returns a decoded String.
		 * Gets the string contents of the response, decoded using the response's charset.
		 * <p> If the read is cancelled, this returns null.
		 */
		static HTTPReader<String> STRING_CONTENT_READER = (response, cancelSwitch, monitor) ->
		{
			String charset;
			if ((charset = response.getCharset()) == null)
				throw new UnsupportedEncodingException("No charset specified.");
			
			char[] c = new char[16384];
			StringBuilder sb = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(response.getContentStream(), charset);
	
			int buf = 0;
			while (!cancelSwitch.get() && (buf = reader.read(c)) >= 0) 
				sb.append(c, 0, buf);
			
			return cancelSwitch.get() ? null : sb.toString();
		};
		
		/**
		 * An HTTP Reader that reads the response body as byte content and returns a byte array.
		 * <p> If the read is cancelled, this returns null.
		 */
		static HTTPReader<byte[]> BYTE_CONTENT_READER = (response, cancelSwitch, monitor) ->
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			relay(response.getContentStream(), bos, 8192, response.getLength(), cancelSwitch, monitor);
			return cancelSwitch.get() ? null : bos.toByteArray();
		};
		
		/**
		 * An HTTP Reader that reads byte content and puts it in a temporary file.
		 * Gets the string contents of the response, decoded using the response's charset.
		 * <p> If the read is cancelled, the file is closed and deleted, and this returns null.
		 */
		static HTTPReader<File> TEMPORARY_FILE_READER = (response, cancelSwitch, monitor) ->
		{
			File out = File.createTempFile("httpTempRead", null);
			try (FileOutputStream fos = new FileOutputStream(out))
			{
				relay(response.getContentStream(), fos, 8192, response.getLength(), cancelSwitch, monitor);
			}
			if (cancelSwitch.get())
			{
				out.delete();
				return null;
			}
			else
			{
				return out;
			}
		};
		
		/**
		 * Called to read the HTTP response from an HTTP call.
		 * <p> The policy of this reader is to keep reading from the open response stream until
		 * the end is reached.
		 * @param response the response object.
		 * @return the returned decoded object.
		 * @throws IOException if a read error occurs.
		 */
		default R onHTTPResponse(HTTPResponse response) throws IOException
		{
			return onHTTPResponse(response, new AtomicBoolean(false), TransferMonitor.NULL_MONITOR);
		}
		
		/**
		 * Called to read the HTTP response from an HTTP call.
		 * <p> The policy of this reader is to keep reading from the open response stream until
		 * the end is reached.
		 * @param response the response object.
		 * @param monitor the transfer monitor to use to monitor the read.
		 * @return the returned decoded object.
		 * @throws IOException if a read error occurs.
		 */
		default R onHTTPResponse(HTTPResponse response, TransferMonitor monitor) throws IOException
		{
			return onHTTPResponse(response, new AtomicBoolean(false), monitor);
		}
		
		/**
		 * Called to read the HTTP response from an HTTP call.
		 * <p> The policy of this reader is to keep reading from the open response stream until
		 * the end is reached.
		 * @param response the response object.
		 * @param cancelSwitch the cancel switch. Set to <code>true</code> to attempt to cancel.
		 * @return the returned decoded object.
		 * @throws IOException if a read error occurs.
		 */
		default R onHTTPResponse(HTTPResponse response, AtomicBoolean cancelSwitch) throws IOException
		{
			return onHTTPResponse(response, cancelSwitch, TransferMonitor.NULL_MONITOR);
		}
		
		/**
		 * Called to read the HTTP response from an HTTP call.
		 * <p> The policy of this reader is to keep reading from the open response stream until
		 * the end is reached or until the cancel switch is set to true (from outside the reading
		 * mechanism). There is no policy for what to return upon canceling this reader's read.
		 * @param response the response object.
		 * @param cancelSwitch the cancel switch. Set to <code>true</code> to attempt to cancel.
		 * @param monitor the transfer monitor to use to monitor the read.
		 * @return the returned decoded object.
		 * @throws IOException if a read error occurs.
		 */
		R onHTTPResponse(HTTPResponse response, AtomicBoolean cancelSwitch, TransferMonitor monitor) throws IOException;
		
		/**
		 * Creates an HTTPReader that returns a File with the response body content written to it.
		 * The target File will be created or overwritten.
		 * @param targetFile the target file.
		 * @return a reader for the file.
		 */
		static HTTPReader<File> createFileDownloader(final File targetFile)
		{
			return (response, cancelSwitch, monitor) ->
			{
				try (FileOutputStream fos = new FileOutputStream(targetFile))
				{
					relay(response.getContentStream(), fos, 8192, response.getLength(), cancelSwitch, monitor);
				}
				if (cancelSwitch.get())
				{
					targetFile.delete();
					return null;
				}
				else
				{
					return targetFile;
				}
			};
		}
		
	}

	/**
	 * Content body abstraction.
	 */
	public interface HTTPContent
	{
		/**
		 * @return the content MIME-type of this content.
		 */
		String getContentType();
	
		/**
		 * @return the encoded charset of this content (can be null if not text).
		 */
		String getCharset();
	
		/**
		 * @return the encoding type of this content (like GZIP or somesuch).
		 */
		String getEncoding();
	
		/**
		 * @return the length of the content in bytes.
		 */
		long getLength();
	
		/**
		 * @return an input stream for the data.
		 * @throws IOException if the stream can't be opened.
		 * @throws SecurityException if the OS forbids opening it.
		 */
		InputStream getInputStream() throws IOException;
		
	}

	/**
	 * Multipart form data.
	 */
	public static class MultipartFormContent implements HTTPContent
	{
		private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	
		private static final byte[] CRLF;
		
		private static final byte[] DISPOSITION_HEADER;
		private static final byte[] DISPOSITION_NAME;
		private static final byte[] DISPOSITION_NAME_END;
		private static final byte[] DISPOSITION_FILENAME;
		private static final byte[] DISPOSITION_FILENAME_END;

		private static final byte[] TYPE_HEADER;
		private static final byte[] ENCODING_HEADER;

		private static final int BLUEPRINT_BOUNDARY_START = 0;
		private static final int BLUEPRINT_PART = 1;
		private static final int BLUEPRINT_BOUNDARY_MIDDLE = 2;
		private static final int BLUEPRINT_BOUNDARY_END = 3;

		static
		{
			CRLF = "\r\n".getBytes(UTF8);
			DISPOSITION_HEADER = "Content-Disposition: form-data".getBytes(UTF8);
			DISPOSITION_NAME = "; name=\"".getBytes(UTF8);
			DISPOSITION_NAME_END = "\"".getBytes(UTF8);
			DISPOSITION_FILENAME = "; filename=\"".getBytes(UTF8);
			DISPOSITION_FILENAME_END = "\"".getBytes(UTF8);
			TYPE_HEADER = "Content-Type: ".getBytes(UTF8);
			ENCODING_HEADER = "Content-Transfer-Encoding: ".getBytes(UTF8);
		}
		
		/** The form part first boundary. */
		private byte[] boundaryFirst;
		/** The form part middle boundary. */
		private byte[] boundaryMiddle;
		/** The form part ending boundary. */
		private byte[] boundaryEnd;
		
		/** List of Parts. */
		private List<Part> parts;
		/** Total length. */
		private long length;
		
		private MultipartFormContent() 
		{
			this.parts = new LinkedList<>();
			String boundaryText = generateBoundary();
			
			this.boundaryFirst = ("--" + boundaryText + "\r\n").getBytes(UTF8);
			this.boundaryMiddle = ("\r\n--" + boundaryText + "\r\n").getBytes(UTF8);
			this.boundaryEnd = ("\r\n--" + boundaryText + "--").getBytes(UTF8);
			
			// account for start and end boundary at least.
			this.length = boundaryFirst.length + boundaryEnd.length;
		}

		private static String generateBoundary()
		{
			Random r = new Random();
			StringBuilder sb = new StringBuilder();
			int dashes = r.nextInt(15) + 10;
			int letters = r.nextInt(24) + 16;
			while (dashes-- > 0)
				sb.append('-');
			while (letters-- > 0)
				sb.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
			return sb.toString();
		}
		
		// Adds a part and calculates change in length.
		private void addPart(final Part p)
		{
			boolean hadOtherParts = !parts.isEmpty();
			parts.add(p);
			length += p.getLength();
			if (hadOtherParts)
				length += boundaryMiddle.length;
		}
		
		/**
		 * Adds a single field to this multipart form.
		 * @param name the field name.
		 * @param value the value.
		 * @return itself, for chaining.
		 * @throws IOException if the data can't be encoded.
		 */
		public MultipartFormContent addField(String name, String value) throws IOException
		{
			return addTextPart(name, null, value);
		}
		
		/**
		 * Adds a single text part to this multipart form.
		 * @param name the field name.
		 * @param mimeType the mimeType of the text part.
		 * @param text the text data.
		 * @return itself, for chaining.
		 * @throws IOException if the data can't be encoded.
		 */
		public MultipartFormContent addTextPart(String name, final String mimeType, final String text) throws IOException
		{
			return addDataPart(name, mimeType, null, null, text.getBytes(UTF8));
		}
		
		/**
		 * Adds a file part to this multipart form.
		 * The name of the file is passed along.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
		 * @param data the file data.
		 * @return itself, for chaining.
		 * @throws IllegalArgumentException if data is null, the file cannot be found, or the file is a directory.
		 */
		public MultipartFormContent addFilePart(String name, String mimeType, final File data)
		{
			return addFilePart(name, mimeType, data.getName(), data);
		}
		
		/**
		 * Adds a file part to this multipart form.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
		 * @param fileName the file name to send (overridden).
		 * @param data the file data.
		 * @return itself, for chaining.
		 * @throws IllegalArgumentException if data is null, the file cannot be found, or the file is a directory.
		 */
		public MultipartFormContent addFilePart(String name, final String mimeType, final String fileName, final File data)
		{
			if (data == null)
				throw new IllegalArgumentException("data cannot be null.");
			if (!data.exists())
				throw new IllegalArgumentException("File " + data.getPath() + " cannot be found.");
			if (data.isDirectory())
				throw new IllegalArgumentException("File " + data.getPath() + " cannot be a directory.");
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
			try {
				// Content Disposition Line
				bos.write(DISPOSITION_HEADER);
				bos.write(DISPOSITION_NAME);
				bos.write(name.getBytes(UTF8));
				bos.write(DISPOSITION_NAME_END);
				bos.write(DISPOSITION_FILENAME);
				bos.write(fileName.getBytes(UTF8));
				bos.write(DISPOSITION_FILENAME_END);
				bos.write(CRLF);

				// Content Type Line
				bos.write(TYPE_HEADER);
				bos.write(mimeType.getBytes(UTF8));
				bos.write(CRLF);

				// Blank line for header end.
				bos.write(CRLF);
				
				// ... data follows here.
			} catch (IOException e) {
				// should never happen.
				throw new RuntimeException(e);
			}

			final byte[] headerBytes = bos.toByteArray();
			
			addPart(new Part(headerBytes, new PartData() 
			{
				@Override
				public long getDataLength() 
				{
					return data.length();
				}
	
				@Override
				public InputStream getInputStream() throws IOException
				{
					return new FileInputStream(data);
				}
			}));
			return this;
		}
		
		/**
		 * Adds a byte data part to this multipart form.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
		 * @param dataIn the input data.
		 * @return itself, for chaining.
		 */
		public MultipartFormContent addDataPart(String name, String mimeType, byte[] dataIn)
		{
			return addDataPart(name, mimeType, null, null, dataIn);
		}
	
		/**
		 * Adds a byte data part to this multipart form as though it came from a file.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
		 * @param fileName the name of the file, as though this were originating from a file (can be null, for "no file").
		 * @param dataIn the input data.
		 * @return itself, for chaining.
		 */
		public MultipartFormContent addDataPart(String name, String mimeType, String fileName, byte[] dataIn)
		{
			return addDataPart(name, mimeType, null, fileName, dataIn);
		}
		
		/**
		 * Adds a byte data part (translated as text) to this multipart form as though it came from a file.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
		 * @param encoding the encoding type name for the data sent, like 'base64' or 'gzip' or somesuch (can be null to signal no encoding type).
		 * @param fileName the name of the file, as though this were originating from a file (can be null, for "no file").
		 * @param dataIn the input data.
		 * @return itself, for chaining.
		 */
		public MultipartFormContent addDataPart(String name, final String mimeType, final String encoding, final String fileName, final byte[] dataIn)
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
			try {
				// Content Disposition Line
				bos.write(DISPOSITION_HEADER);
				bos.write(DISPOSITION_NAME);
				bos.write(name.getBytes(UTF8));
				bos.write(DISPOSITION_NAME_END);
				if (fileName != null)
				{
					bos.write(DISPOSITION_FILENAME);
					bos.write(fileName.getBytes(UTF8));
					bos.write(DISPOSITION_FILENAME_END);
				}
				bos.write(CRLF);

				// Content Type Line
				if (mimeType != null)
				{
					bos.write(TYPE_HEADER);
					bos.write(mimeType.getBytes(UTF8));
					bos.write(CRLF);
				}

				// Content transfer encoding
				if (encoding != null)
				{
					bos.write(ENCODING_HEADER);
					bos.write(encoding.getBytes(UTF8));
					bos.write(CRLF);
				}
				
				// Blank line for header end.
				bos.write(CRLF);
				
				// ... data follows here.
			} catch (IOException e) {
				// should never happen.
				throw new RuntimeException(e);
			}

			final byte[] headerBytes = bos.toByteArray();
			
			addPart(new Part(headerBytes, new PartData() 
			{
				@Override
				public long getDataLength() 
				{
					return dataIn.length;
				}
	
				@Override
				public InputStream getInputStream()
				{
					return new ByteArrayInputStream(dataIn);
				}
			}));
			return this;
		}

		@Override
		public String getContentType()
		{
			return "multipart/form-data";
		}

		@Override
		public String getCharset()
		{
			return "utf-8";
		}

		@Override
		public String getEncoding()
		{
			return null;
		}

		@Override
		public long getLength()
		{
			return length;
		}

		@Override
		public InputStream getInputStream() throws IOException
		{
			return new MultiformInputStream();
		}
		
		/**
		 * Part data.
		 */
		private interface PartData
		{
			/**
			 * @return the content length of this part in bytes.
			 */
			long getDataLength();
			
			/**
			 * @return an open input stream to read from this part.
			 * @throws IOException if an I/O error occurs on read.
			 */
			InputStream getInputStream() throws IOException;
		}
		
		/**
		 * A single form part.
		 */
		private static class Part
		{
			private byte[] headerbytes;
			private PartData data;
			
			private Part(final byte[] headerbytes, final PartData data)
			{
				this.headerbytes = headerbytes;
				this.data = data;
			}
			
			/**
			 * @return the boundary-plus-header bytes that make up the start of this part.
			 */
			public byte[] getPartHeaderBytes()
			{
				return headerbytes;
			}

			/**
			 * @return the full length of this part, plus headers, in bytes.
			 */
			public long getLength()
			{
				return getPartHeaderBytes().length + data.getDataLength();
			}

			/**
			 * @return an open input stream for reading from this form.
			 * @throws IOException if an input stream could not be opened.
			 */
			public InputStream getInputStream() throws IOException
			{
				return new PartInputStream();
			}

			private class PartInputStream extends InputStream
			{
				private boolean readHeader;
				private InputStream currentStream;
				
				private PartInputStream()
				{
					this.readHeader = false;
					this.currentStream = new ByteArrayInputStream(headerbytes);
				}
				
				@Override
				public int read() throws IOException
				{
					if (currentStream == null)
						return -1;
					
					int out;
					if ((out = currentStream.read()) < 0)
					{
						currentStream.close();
						if (!readHeader)
						{
							currentStream = data.getInputStream();
							readHeader = true;
						}
						else
							currentStream = null;
						return read();
					}
					else
						return out;
				}
				
				@Override
				public void close() throws IOException
				{
					if (currentStream != null)
					{
						currentStream.close();
						currentStream = null;
					}
					super.close();
				}
			}
		}

		private class MultiformInputStream extends InputStream
		{	
			private int[] blueprint;
			private int currentBlueprint;
			private Iterator<Part> streamIterator;
			private Part currentPart;
			private InputStream currentStream;
			
			private MultiformInputStream() throws IOException
			{
				this.streamIterator = parts.iterator();
				this.currentBlueprint = 0;
				this.currentPart = null;
				this.currentStream = null;
				this.blueprint = new int[parts.isEmpty() ? 0 : parts.size() * 2 + 1];
				
				if (blueprint.length > 0)
				{
					this.blueprint[0] = BLUEPRINT_BOUNDARY_START;
					for (int i = 1; i < blueprint.length; i += 2)
					{
						this.blueprint[i] = BLUEPRINT_PART;
						this.blueprint[i + 1] = i + 1 < blueprint.length - 1 ? BLUEPRINT_BOUNDARY_MIDDLE : BLUEPRINT_BOUNDARY_END;
					}
				}
				nextStream();
			}
		
			private void nextStream() throws IOException
			{
				if (currentBlueprint >= blueprint.length)
				{
					currentPart = null;
					currentStream = null;
				}
				else switch (blueprint[currentBlueprint++])
				{
					case BLUEPRINT_BOUNDARY_START:
						currentStream = new ByteArrayInputStream(boundaryFirst);
						break;
					case BLUEPRINT_PART:
						currentPart = streamIterator.hasNext() ? streamIterator.next() : null;
						currentStream = currentPart != null ? currentPart.getInputStream() : null;
						break;
					case BLUEPRINT_BOUNDARY_MIDDLE:
						currentStream = new ByteArrayInputStream(boundaryMiddle);
						break;
					case BLUEPRINT_BOUNDARY_END:
						currentStream = new ByteArrayInputStream(boundaryEnd);
						break;
				}
			}
			
			@Override
			public int read() throws IOException
			{
				if (currentStream == null)
					return -1;
				
				int out;
				if ((out = currentStream.read()) < 0)
				{
					nextStream();
					return read();
				}
				
				return out;
			}
			
			@Override
			public void close() throws IOException
			{
				if (currentStream != null)
					currentStream.close();
				
				currentStream = null;
				currentPart = null;
				streamIterator = null;
				super.close();
			}
		}
	}

	private static class BlobContent implements HTTPContent
	{
		private String contentType;
		private String contentEncoding;
		private byte[] data;
		
		private BlobContent(String contentType, String contentEncoding, byte[] data)
		{
			this.contentType = contentType;
			this.contentEncoding = contentEncoding;
			this.data = data;
		}
		
		@Override
		public String getContentType()
		{
			return contentType;
		}
		
		@Override
		public String getCharset()
		{
			return null;
		}
		
		@Override
		public String getEncoding()
		{
			return contentEncoding;
		}
		
		@Override
		public long getLength()
		{
			return data.length;
		}
		
		@Override
		public InputStream getInputStream() throws IOException
		{
			return new ByteArrayInputStream(data);
		}
		
	}

	private static class TextContent extends BlobContent
	{
		private String contentCharset;
	
		private TextContent(String contentType, String contentCharset, String contentEncoding, byte[] data)
		{
			super(contentType, contentEncoding, data);
			this.contentCharset = contentCharset;
		}
		
		@Override
		public String getCharset()
		{
			return contentCharset;
		}
		
	}

	private static class FileContent implements HTTPContent
	{
		private String contentType;
		private String encodingType;
		private String charsetType;
		private File file;
		
		private FileContent(String contentType, String encodingType, String charsetType, File file)
		{
			if (file == null)
				throw new IllegalArgumentException("file cannot be null.");
			if (!file.exists())
				throw new IllegalArgumentException("File " + file.getPath() + " cannot be found.");
	
			this.contentType = contentType;
			this.encodingType = encodingType;
			this.charsetType = charsetType;
			this.file = file;
		}
		
		@Override
		public String getContentType()
		{
			return contentType;
		}
	
		@Override
		public String getCharset()
		{
			return charsetType;
		}
	
		@Override
		public String getEncoding()
		{
			return encodingType;
		}
	
		@Override
		public long getLength()
		{
			return file.length();
		}
	
		@Override
		public InputStream getInputStream() throws IOException
		{
			return new FileInputStream(file);
		}
		
	}

	private static class FormContent extends TextContent
	{
		private FormContent(HTTPParameters parameters)
		{
			super("x-www-form-urlencoded", Charset.defaultCharset().displayName(), null, parameters.toString().getBytes());
		}
	}

	/**
	 * HTTP Cookie object.
	 */
	public static class HTTPCookie
	{
		public enum SameSiteMode
		{
			STRICT,
			LAX,
			NONE;
		}

		private String key;
		private String value;
		private List<String> flags;
		
		private HTTPCookie(String key, String value)
		{
			this.key = key;
			this.value = value;
			this.flags = new LinkedList<>();
		}
		
		/**
		 * Sets the cookie expiry date. 
		 * @param date the expiry date.
		 * @return this, for chaining.
		 */
		public HTTPCookie expires(Date date)
		{
			flags.add("Expires=" + date(date));
			return this;
		}
		
		/**
		 * Sets the cookie expiry date. 
		 * @param dateMillis the expiry date in milliseconds since the Epoch.
		 * @return this, for chaining.
		 */
		public HTTPCookie expires(long dateMillis)
		{
			flags.add("Expires=" + date(dateMillis));
			return this;
		}
		
		/**
		 * Sets the cookie maximum age in seconds. 
		 * @param seconds the time in seconds.
		 * @return this, for chaining.
		 */
		public HTTPCookie maxAge(long seconds)
		{
			flags.add("Max-Age=" + seconds);
			return this;
		}
		
		/**
		 * Sets the cookie's relevant domain. 
		 * @param value the domain value.
		 * @return this, for chaining.
		 */
		public HTTPCookie domain(String value)
		{
			flags.add("Domain=" + value);
			return this;
		}
		
		/**
		 * Sets the cookie's relevant subpath. 
		 * @param value the path value.
		 * @return this, for chaining.
		 */
		public HTTPCookie path(String value)
		{
			flags.add("Path=" + value);
			return this;
		}

		/**
		 * Sets the cookie for only secure travel. 
		 * @return this, for chaining.
		 */
		public HTTPCookie secure()
		{
			flags.add("Secure");
			return this;
		}

		/**
		 * Sets the cookie for only top-level HTTP requests (not JS/AJAX). 
		 * @return this, for chaining.
		 */
		public HTTPCookie httpOnly()
		{
			flags.add("HttpOnly");
			return this;
		}

		/**
		 * Sets the cookie for a Same Site type. 
		 * @param mode the SameSite mode.
		 * @return this, for chaining.
		 */
		public HTTPCookie sameSite(SameSiteMode mode)
		{
			String name = mode.name();
			flags.add("SameSite=" + name.charAt(0) + name.substring(1).toLowerCase());
			return this;
		}

		/**
		 * @return the cookie string to add to header values.
		 */
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(key).append('=').append(toURLEncoding(value));
			for (String flag : flags)
				sb.append("; ").append(flag);
			return sb.toString();
		}
	}
	
	/**
	 * HTTP headers object.
	 */
	public static class HTTPHeaders
	{
		private Map<String, String> map;
		
		private HTTPHeaders()
		{
			this.map = new HashMap<>(); 
		}

		/**
		 * Copies this header object.
		 * @return a copy of this header.
		 */
		public HTTPHeaders copy()
		{
			HTTPHeaders out = new HTTPHeaders();
			out.merge(this);
			return out;
		}
		
		/**
		 * Adds the header entries from another set of headers to this one.
		 * Existing names are overwritten.
		 * @param headers the input headers.
		 * @return this, for chaining.
		 */
		public HTTPHeaders merge(HTTPHeaders headers)
		{
			for (Map.Entry<String, String> entry : headers.map.entrySet())
				this.setHeader(entry.getKey(), entry.getValue());
			return this;
		}

		/**
		 * Sets a header.
		 * @param header the header name.
		 * @param value the header value.
		 * @return this, for chaining.
		 */
		public HTTPHeaders setHeader(String header, String value)
		{
			map.put(header, value);
			return this;
		}

	}
	
	/**
	 * HTTP Parameters object.
	 */
	public static class HTTPParameters
	{
		private Map<String, List<String>> map;
		
		private HTTPParameters()
		{
			this.map = new HashMap<>(); 
		}

		/**
		 * Copies this parameters object.
		 * @return a copy of this parameter object.
		 */
		public HTTPParameters copy()
		{
			HTTPParameters out = new HTTPParameters();
			out.merge(this);
			return out;
		}

		/**
		 * Adds the parameter entries from another set of parameters to this one.
		 * Parameters are added, not replaced.
		 * @param parameters the input parameters.
		 * @return this, for chaining.
		 * @see #addParameter(String, Object)
		 */
		public HTTPParameters merge(HTTPParameters parameters)
		{
			for (Map.Entry<String, List<String>> entry : parameters.map.entrySet())
				for (String value : entry.getValue())
					this.addParameter(entry.getKey(), value);
			return this;
		}

		/**
		 * Adds/creates a parameter.
		 * @param key the parameter name.
		 * @param value the parameter value.
		 * @return this, for chaining.
		 */
		public HTTPParameters addParameter(String key, Object value)
		{
			List<String> list;
			if ((list = map.get(key)) == null)
				map.put(key, (list = new LinkedList<>()));
			list.add(String.valueOf(value));
			return this;
		}

		/**
		 * Sets/resets a parameter and its values.
		 * If the parameter is already set, it is replaced.
		 * @param key the parameter name.
		 * @param value the parameter value.
		 * @return this, for chaining.
		 */
		public HTTPParameters setParameter(String key, Object value)
		{
			List<String> list;
			map.put(key, (list = new LinkedList<>()));
			list.add(String.valueOf(value));
			return this;
		}

		/**
		 * Sets/resets a parameter and its values.
		 * If the parameter is already set, it is replaced.
		 * @param key the parameter name.
		 * @param values the parameter values.
		 * @return this, for chaining.
		 */
		public HTTPParameters setParameter(String key, Object... values)
		{
			List<String> list;
			map.put(key, (list = new LinkedList<>()));
			for (Object v : values)
				list.add(String.valueOf(v));
			return this;
		}
		
		/**
		 * @return the parameter string to add to form content or URLs.
		 */
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, List<String>> entry : map.entrySet())
			{
				String key = entry.getKey();
				for (String value : entry.getValue())
				{
					if (sb.length() > 0)
						sb.append('&');
					sb.append(toURLEncoding(key));
					sb.append('=');
					sb.append(toURLEncoding(value));
				}
			}
			return sb.toString();
		}
	}
	
	/**
	 * Response from an HTTP call.
	 */
	public static class HTTPResponse implements AutoCloseable
	{
		private HTTPRequest request;
		
		private Map<String, List<String>> headers;
		
		private int statusCode;
		private String statusMessage;
		
		private Long length;
		private InputStream contentStream;
		private String charset;
		private String contentTypeHeader;
		private String contentType;
		private String encoding;
		
		private String contentDisposition;
		private String filename;
		
		private HTTPResponse(HTTPRequest request, HttpURLConnection conn, String defaultResponseCharset) throws IOException
		{
			this.request = request;
			this.statusCode = conn.getResponseCode();
			this.statusMessage = conn.getResponseMessage();

			long conlen = conn.getContentLengthLong();
			this.length = conlen < 0 ? null : conlen;
			this.encoding = conn.getContentEncoding();
			this.contentTypeHeader = conn.getContentType();

			int mimeEnd = contentTypeHeader.indexOf(';');
			
			this.charset = null;
			this.contentType = contentTypeHeader.substring(0, mimeEnd >= 0 ? mimeEnd : contentTypeHeader.length()).trim();
			
			int charsetindex;
			if ((charsetindex = contentTypeHeader.toLowerCase().indexOf("charset=")) >= 0)
			{
				int endIndex = contentTypeHeader.indexOf(";", charsetindex);
				if (endIndex >= 0)
					charset = contentTypeHeader.substring(charsetindex + "charset=".length(), endIndex).trim();
				else
					charset = contentTypeHeader.substring(charsetindex + "charset=".length()).trim();
				if (charset.startsWith("\"")) // remove surrounding quotes
					charset = charset.substring(1, charset.length() - 1); 
			}
			
			if (charset == null)
				charset = defaultResponseCharset;
			
			this.filename = null;
			this.contentDisposition = null;
			this.contentStream = null;
			
			this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet())
				if (entry.getKey() != null)
					this.headers.put(entry.getKey(), entry.getValue());
			this.headers = Collections.unmodifiableMap(this.headers);
			
			// content disposition?
			if ((contentDisposition = conn.getHeaderField("content-disposition")) != null)
			{
				int fileNameIndex;
				if ((fileNameIndex = contentDisposition.toLowerCase().indexOf("filename=")) >= 0)
				{
					int endIndex = contentDisposition.indexOf(";", fileNameIndex);
					if (endIndex >= 0)
						filename = contentDisposition.substring(fileNameIndex + "filename=".length(), endIndex).trim();
					else
						filename = contentDisposition.substring(fileNameIndex + "filename=".length()).trim();
					if (filename.startsWith("\"")) // remove surrounding quotes
						filename = filename.substring(1, filename.length() - 1); 
				}
			}
			
			if (statusCode >= 400) 
				contentStream = conn.getErrorStream();
			else 
				contentStream = conn.getInputStream();
		}
		
		/**
		 * @return the request used to get the response.
		 */
		public HTTPRequest getRequest() 
		{
			return request;
		}
		
		/**
		 * @return the headers on the response.
		 */
		public Map<String, List<String>> getHeaders()
		{
			return headers;
		}
		
		/**
		 * Gets the last specified header value of the response for a header name. 
		 * @param name the header name.
		 * @return the value, or null if no header by that name.
		 */
		public String getHeader(String name)
		{
			List<String> headerList = getHeaderValues(name);
			return headerList != null ? headerList.get(headerList.size() - 1) : null;
		}

		/**
		 * Gets the list of header values for a header name.
		 * @param name the header name.
		 * @return the list of values, or null if no header by that name.
		 */
		public List<String> getHeaderValues(String name)
		{
			return headers.get(name);
		}

		/**
		 * @return the response status code.
		 */
		public int getStatusCode()
		{
			return statusCode;
		}
	
		/**
		 * @return the response status message.
		 */
		public String getStatusMessage()
		{
			return statusMessage;
		}
	
		/**
		 * Fetches the list of previous redirect addresses (if any) plus the final address read, in the order contacted.
		 * If no redirects occurred, this returns a list with a single address.
		 * @return an immutable list of URLs.
		 */
		public List<String> getRedirectHistory()
		{
			List<String> out = new ArrayList<>((request.redirectedURLs != null ? request.redirectedURLs.size() : 0) + 1);
			if (request.redirectedURLs != null)
				out.addAll(request.redirectedURLs);
			out.add(request.url);
			return Collections.unmodifiableList(out);
		}

		/**
		 * @return true if and only if the response status code is between 100 and 199, inclusive, false otherwise.
		 */
		public boolean isInformational()
		{
			return statusCode / 100 == 1;
		}

		/**
		 * @return true if and only if the response status code is between 200 and 299, inclusive, false otherwise.
		 */
		public boolean isSuccess()
		{
			return statusCode / 100 == 2;
		}

		/**
		 * @return true if and only if the response status code is between 300 and 399, inclusive, false otherwise.
		 */
		public boolean isRedirect()
		{
			return statusCode / 100 == 3;
		}

		/**
		 * Checks if the this response can be automatically actioned upon in terms of resolving server-directed redirects.
		 * This object can build a redirect request if the status code is NOT 300, 304, or 305, since that requires more of an intelligent decision.
		 * <p> NOTE: This will not redirect a request that has a redirect in the content body (for instance, an HTML meta tag).
		 * It must be a status code and <code>Location</code> header.
		 * @return true if and only if the response status code has defined redirect guidelines, false otherwise.
		 * @see #buildRedirect()
		 */
		public boolean isAutoRedirectable()
		{
			return (
				statusCode == 301
				|| statusCode == 302
				|| statusCode == 303
				|| statusCode == 307
				|| statusCode == 308
			) && getHeader("Location") != null;
		}

		/**
		 * @return true if and only if the response status code is between 400 and 599, inclusive, false otherwise.
		 */
		public boolean isError()
		{
			int range = statusCode / 100;
			return range == 4 || range == 5;
		}

		/**
		 * @return the response's content length (if reported).
		 */
		public Long getLength() 
		{
			return length;
		}
	
		/**
		 * @return an open input stream for reading the response's content.
		 */
		public InputStream getContentStream() 
		{
			return contentStream;
		}
		
		/**
		 * Convenience function for transferring the entirety of the content 
		 * stream to another.
		 * @param out the output stream.
		 * @return the amount of bytes moved.
		 * @throws IOException if an I/O error occurs during transfer.
		 */
		public final long relayContent(OutputStream out) throws IOException
		{
			return relayContent(out, null);
		}
		
		/**
		 * Convenience function for transferring the entirety of the content 
		 * stream to another, monitoring the progress as it goes.
		 * <p>Equivalent to: <code>return relay(getContentStream(), out, 8192, getLength(), monitor);</code>
		 * @param out the output stream.
		 * @param monitor the optional monitor. Can be null.
		 * @return the amount of bytes moved.
		 * @throws IOException if an I/O error occurs during transfer.
		 */
		public final long relayContent(OutputStream out, TransferMonitor monitor) throws IOException
		{
			return relay(getContentStream(), out, 8192, getLength(), null, monitor);
		}
		
		/**
		 * Reads this response with an HTTPReader and returns the read result.
		 * @param <T> the reader return type - the desired object type.
		 * @param reader the reader.
		 * @return the resultant object.
		 * @throws IOException if a read error occurs.
		 */
		public <T> T read(HTTPReader<T> reader) throws IOException
		{
			return reader.onHTTPResponse(this);
		}
		
		/**
		 * Reads this response with an HTTPReader and returns the read result.
		 * @param <T> the reader return type - the desired object type.
		 * @param reader the reader.
		 * @param monitor the transfer monitor to use to monitor the read.
		 * @return the resultant object.
		 * @throws IOException if a read error occurs.
		 */
		public <T> T read(HTTPReader<T> reader, TransferMonitor monitor) throws IOException
		{
			return reader.onHTTPResponse(this, monitor);
		}
		
		/**
		 * Reads this response with an HTTPReader and returns the read result.
		 * @param <T> the reader return type - the desired object type.
		 * @param reader the reader.
		 * @param cancelSwitch the cancel switch. Set to <code>true</code> to attempt to cancel.
		 * @return the resultant object.
		 * @throws IOException if a read error occurs.
		 */
		public <T> T read(HTTPReader<T> reader, AtomicBoolean cancelSwitch) throws IOException
		{
			return reader.onHTTPResponse(this, cancelSwitch);
		}
		
		/**
		 * Reads this response with an HTTPReader and returns the read result.
		 * @param <T> the reader return type - the desired object type.
		 * @param reader the reader.
		 * @param cancelSwitch the cancel switch. Set to <code>true</code> to attempt to cancel.
		 * @param monitor the transfer monitor to use to monitor the read.
		 * @return the resultant object.
		 * @throws IOException if a read error occurs.
		 */
		public <T> T read(HTTPReader<T> reader, AtomicBoolean cancelSwitch, TransferMonitor monitor) throws IOException
		{
			return reader.onHTTPResponse(this, cancelSwitch, monitor);
		}
		
		/**
		 * @return the response's charset. can be null.
		 */
		public String getCharset()
		{
			return charset;
		}
	
		/**
		 * @return the response's content type. can be null.
		 */
		public String getContentType()
		{
			return contentType;
		}
	
		/**
		 * @return the response's content type (full unparsed header). can be null.
		 */
		public String getContentTypeHeader()
		{
			return contentTypeHeader;
		}
	
		/**
		 * @return the response's encoding. can be null.
		 */
		public String getEncoding()
		{
			return encoding;
		}
	
		/**
		 * @return the content disposition. if present, usually "attachment". Can be null.
		 */
		public String getContentDisposition() 
		{
			return contentDisposition;
		}

		/**
		 * @return the content filename. Set if content disposition is "attachment". Can be null.
		 * @see #getContentDisposition()
		 */
		public String getFilename() 
		{
			return filename;
		}

		/**
		 * Builds a request that fulfills a redirect, but only if this response is a redirect status.
		 * @return a new request that would fulfill a redirect response.
		 * @throws IllegalStateException if this response is not a redirect, or a 300, 304, or 305 
		 * 		status code that would not warrant a remote redirect nor specific handling, or if a redirect loop has been detected.
		 * @throws IllegalArgumentException if the URL string from the server is malformed.
		 */
		public HTTPRequest buildRedirect()
		{
			if (!isRedirect())
				throw new IllegalStateException("Response is not a redirect type.");
			
			String location = getHeader("Location");
			if (location == null)
				throw new IllegalStateException("Response provided no redirect location.");
			
			HTTPRequest out;
			
			switch (statusCode)
			{
				case 300: // Multiple Choice
					throw new IllegalStateException("Response status code 300 not automatically redirectable. Handle locally.");
				case 304: // Not Modified
					throw new IllegalStateException("Response redirects to local cache (code 304). Handle locally.");
				case 305: // Use Proxy
					throw new IllegalStateException("Response status code unsupported: 305 Use Proxy");
				
				default:
				case 301: // Moved Permanently
				case 302: // Found
				case 307: // Temporary Redirect
				case 308: // Permanent Redirect
					out = getRequest().copyRedirect(location);
					break;
					
				case 303: // See Other (Force GET)
					out = getRequest().copyRedirect(HTTP_METHOD_GET, location);
					break;
			}
			
			return out;
		}
		
		@Override
		public void close()
		{
			HTTPUtils.close(contentStream);
		}
	}

	/**
	 * A request builder, as an alternative to calling the "http" methods directly.
	 */
	public static class HTTPRequest
	{
		/** HTTP Method. */
		private String method;
		/** Target URL string. */
		private String url;
		/** HTTP Headers. */
		private HTTPHeaders headers;
		/** HTTP Query Parameters. */
		private HTTPParameters parameters;
		/** List of cookies. */
		private List<HTTPCookie> cookies;
		/** Request timeout milliseconds. */
		private int timeoutMillis;
		/** Default charset encoding (on response). */
		private String defaultCharsetEncoding;
		/** HTTP Content Body. */
		private HTTPContent content;
		/** Upload Transfer Monitor. */
		private TransferMonitor monitor;
		/** Auto-redirect if possible? */
		private boolean autoRedirect;
		/** Set of previous URLs from redirects. */
		private List<String> redirectedURLs;
		
		private HTTPRequest()
		{
			this.method = null;
			this.url = null;
			this.headers = HTTPUtils.headers();
			this.parameters = HTTPUtils.parameters();
			this.cookies = new LinkedList<>();
			this.timeoutMillis = DEFAULT_TIMEOUT_MILLIS.get();
			this.defaultCharsetEncoding = null;
			this.content = null;
			this.monitor = null;
			this.autoRedirect = true;
			this.redirectedURLs = null;
		}
		
		// Checks if a URL pattern is valid.
		// Returns the passed-in URL, unchanged.
		private static String checkURL(String url)
		{
			return checkURL(url, false);
		}
		
		// Checks if a URL pattern is valid.
		// Returns the passed-in URL, unchanged.
		private static String checkURL(String url, boolean fromServer)
		{
			try {
				new URL(url);
				return url;
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(fromServer ? "Redirect URL from server is malformed." : "URL is malformed.", e);
			}
		}
		
		/**
		 * Starts a new request builder.
		 * @param method the HTTP method.
		 * @param url the target URL.
		 * @return an {@link HTTPRequest}.
		 * @throws IllegalArgumentException if the URL string is malformed.
		 */
		public static HTTPRequest create(String method, String url)
		{
			HTTPRequest out = new HTTPRequest();
			out.method = method;
			out.url = checkURL(url);
			return out;
		}
		
		/**
		 * Starts a GET request builder.
		 * This will also auto-redirect on an actionable redirect status
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 * @throws IllegalArgumentException if the URL string is malformed.
		 */
		public static HTTPRequest get(String url)
		{
			return create(HTTP_METHOD_GET, url);
		}

		/**
		 * Starts a HEAD request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 * @throws IllegalArgumentException if the URL string is malformed.
		 */
		public static HTTPRequest head(String url)
		{
			return create(HTTP_METHOD_HEAD, url);
		}

		/**
		 * Starts a DELETE request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 * @throws IllegalArgumentException if the URL string is malformed.
		 */
		public static HTTPRequest delete(String url)
		{
			return create(HTTP_METHOD_DELETE, url);
		}

		/**
		 * Starts a OPTIONS request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 * @throws IllegalArgumentException if the URL string is malformed.
		 */
		public static HTTPRequest options(String url)
		{
			return create(HTTP_METHOD_OPTIONS, url);
		}

		/**
		 * Starts a TRACE request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 * @throws IllegalArgumentException if the URL string is malformed.
		 */
		public static HTTPRequest trace(String url)
		{
			return create(HTTP_METHOD_TRACE, url);
		}

		/**
		 * Starts a PUT request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 * @throws IllegalArgumentException if the URL string is malformed.
		 */
		public static HTTPRequest put(String url)
		{
			return create(HTTP_METHOD_PUT, url);
		}

		/**
		 * Starts a POST request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 * @throws IllegalArgumentException if the URL string is malformed.
		 */
		public static HTTPRequest post(String url)
		{
			return create(HTTP_METHOD_POST, url);
		}

		/**
		 * Makes a deep copy of this request, such that
		 * changes to this one do not affect the original 
		 * (the content and monitor, however, if any, is reference-copied). 
		 * @return a new HTTPRequest that is a copy of this one.
		 */
		public HTTPRequest copy()
		{
			HTTPRequest out = new HTTPRequest();
			out.method = this.method;
			out.url = this.url;
			out.headers = this.headers.copy();
			out.parameters = this.parameters.copy();
			out.cookies = new LinkedList<>(this.cookies);
			out.timeoutMillis = this.timeoutMillis;
			out.defaultCharsetEncoding = this.defaultCharsetEncoding;
			out.content = this.content;
			out.monitor = this.monitor;
			if (redirectedURLs == null)
				out.redirectedURLs = null;
			else
				out.redirectedURLs = new LinkedList<>(out.redirectedURLs);
			return out;
		}

		/**
		 * Copies this request object, but preps this as though it came from a redirect.
		 * The new URL is set (method is preserved), but the previous URL on this request is preserved in a set of previous URLs in order to
		 * detect redirect loops.
		 * @param newURL the new URL (usually from a <code>"Location"</code> header value).
		 * @return a new request that is a copy of this one, but with a new method and URL.
		 * @throws IllegalStateException if the new URL has already been seen previously in earlier redirects.
		 * @throws IllegalArgumentException if the URL string is malformed.
		 */
		public HTTPRequest copyRedirect(String newURL)
		{
			return copyRedirect(null, newURL);
		}
		
		/**
		 * Copies this request object, but preps this as though it came from a redirect.
		 * The new URL and method are set, but the previous URL on this request is preserved in a set of previous URLs in order to
		 * detect redirect loops.
		 * @param newMethod the new HTTP method. If not {@value #HTTP_METHOD_POST} nor {@value #HTTP_METHOD_PUT}, the body is automatically discarded.
		 * @param newURL the new URL (usually from a <code>"Location"</code> header value).
		 * @return a new request that is a copy of this one, but with a new method and URL.
		 * @throws IllegalStateException if the new URL has already been seen previously in earlier redirects.
		 * @throws IllegalArgumentException if the URL string is malformed.
		 */
		public HTTPRequest copyRedirect(String newMethod, String newURL)
		{
			HTTPRequest out = copy();
			if (out.redirectedURLs == null)
				out.redirectedURLs = new LinkedList<>();
			if (out.redirectedURLs.contains(out.url))
				throw new IllegalStateException("Redirect loop detected - " + out.url + " wad already visited.");
			out.redirectedURLs.add(out.url);
			out.url = checkURL(newURL, true);
			if (newMethod != null)
				out.method = newMethod;
			if (!(out.method.equalsIgnoreCase(HTTP_METHOD_POST) || out.method.equalsIgnoreCase(HTTP_METHOD_PUT)))
				out.content(null);
			return out;
		}
		
		/**
		 * Replaces the headers on this request.
		 * The headers are copied, such that future alterations to the headers passed in
		 * are not affected if that headers object is changed later.
		 * @param headers the new headers.
		 * @return this request, for chaining.
		 * @see HTTPUtils#headers(java.util.Map.Entry...)
		 */
		public HTTPRequest headers(HTTPHeaders headers)
		{
			Objects.requireNonNull(headers);
			this.headers = headers.copy();
			return this;
		}
		
		/**
		 * Adds/replaces headers to/on the headers on this request.
		 * @param headers the source header map.
		 * @return this request, for chaining.
		 * @see HTTPHeaders#merge(HTTPHeaders)
		 */
		public HTTPRequest addHeaders(HTTPHeaders headers)
		{
			Objects.requireNonNull(headers);
			this.headers.merge(headers);
			return this;
		}
		
		/**
		 * Sets/replaces a header on the headers on this request.
		 * @param header the header name.
		 * @param value the header value.
		 * @return this request, for chaining.
		 * @see HTTPHeaders#merge(HTTPHeaders)
		 */
		public HTTPRequest setHeader(String header, String value)
		{
			this.headers.setHeader(header, value);
			return this;
		}
		
		/**
		 * Replaces the parameters on this request.
		 * The parameters are copied, such that future alterations to the parameters passed in
		 * are not affected if that parameters object is changed later.
		 * @param parameters the new parameters.
		 * @see HTTPUtils#parameters(java.util.Map.Entry...)
		 * @return this request, for chaining.
		 */
		public HTTPRequest parameters(HTTPParameters parameters)
		{
			Objects.requireNonNull(parameters);
			this.parameters = parameters.copy();
			return this;
		}
		
		/**
		 * Adds/replaces parameters to/on the headers on this request.
		 * @param parameters the source parameter map.
		 * @return this request, for chaining.
		 * @see HTTPHeaders#merge(HTTPHeaders)
		 */
		public HTTPRequest addParameters(HTTPParameters parameters)
		{
			Objects.requireNonNull(parameters);
			this.parameters.merge(parameters);
			return this;
		}
		
		/**
		 * Sets/resets a parameter and its values on this request.
		 * If the parameter is already set, it is replaced.
		 * @param key the parameter name.
		 * @param value the parameter value.
		 * @return this request, for chaining.
		 */
		public HTTPRequest setParameter(String key, Object value)
		{
			this.parameters.setParameter(key, value);
			return this;
		}

		/**
		 * Sets/resets a parameter and its values on this request.
		 * If the parameter is already set, it is replaced.
		 * @param key the parameter name.
		 * @param values the parameter values.
		 * @return this request, for chaining.
		 */
		public HTTPRequest setParameter(String key, Object... values)
		{
			this.parameters.setParameter(key, values);
			return this;
		}
		
		/**
		 * Adds a cookie to this request.
		 * @param cookie the cookie to add.
		 * @return this request, for chaining.
		 * @see HTTPUtils#cookie(String, String)
		 */
		public HTTPRequest addCookie(HTTPCookie cookie)
		{
			this.cookies.add(cookie);
			return this;
		}
		
		/**
		 * Sets the content body for this request.
		 * @param content the content. Can be null for no content body.
		 * @return this request, for chaining.
		 * @see #createByteContent(String, byte[])
		 * @see #createByteContent(String, String, byte[])
		 * @see #createTextContent(String, String)
		 * @see #createFileContent(String, File)
		 * @see #createFileContent(String, String, File)
		 * @see #createFormContent(HTTPParameters)
		 * @see #createMultipartContent()
		 */
		public HTTPRequest content(HTTPContent content) 
		{
			this.content = content;
			return this;
		}
		
		/**
		 * Sets the timeout for this request.
		 * If this is not set, the current default, {@link HTTPUtils#DEFAULT_TIMEOUT_MILLIS}, is the default.
		 * @param timeoutMillis the timeout in milliseconds.
		 * @return this request, for chaining.
		 */
		public HTTPRequest timeout(int timeoutMillis) 
		{
			this.timeoutMillis = timeoutMillis;
			return this;
		}

		/**
		 * Sets the default charset encoding to use on the response if none is sent back
		 * from the server.
		 * @param defaultCharsetEncoding the new encoding name.
		 * @return this request, for chaining.
		 */
		public HTTPRequest defaultCharsetEncoding(String defaultCharsetEncoding) 
		{
			this.defaultCharsetEncoding = defaultCharsetEncoding;
			return this;
		}
		
		/**
		 * Sets the upload monitor callback for the request.
		 * @param monitor the monitor to call.
		 * @return this request, for chaining.
		 */
		public HTTPRequest uploadMonitor(TransferMonitor monitor) 
		{
			this.monitor = monitor;
			return this;
		}
		
		/**
		 * Sets if auto-redirecting happens.
		 * All requests 
		 * Note that not every redirect can be changed into an actionable redirect.
		 * @param autoRedirect true 
		 * @return this request, for chaining.
		 */
		public HTTPRequest setAutoRedirect(boolean autoRedirect) 
		{
			this.autoRedirect = autoRedirect;
			return this;
		}
		
		/**
		 * Sends this request and gets an open response.
		 * <p>
		 * Best used in a try-with-resources block so that the response input stream auto-closes 
		 * (but not the connection, which stays alive if possible), like so:
		 * <pre><code>
		 * try (HTTPResponse response = request.send())
		 * {
		 *     // ... read response ...
		 * }
		 * </code></pre>
		 * @return an HTTPResponse object.
		 * @throws IOException if an error happens during the read/write.
		 * @throws SocketTimeoutException if the socket read times out.
		 * @throws ProtocolException if the request method is incorrect, or not an HTTP URL.
		 */
		public HTTPResponse send() throws IOException
		{
			return send((AtomicBoolean)null);
		}

		/**
		 * Sends this request and gets an open response.
		 * <p>
		 * Best used in a try-with-resources block so that the response input stream auto-closes 
		 * (but not the connection, which stays alive if possible), like so:
		 * <pre><code>
		 * AtomicBoolean cancel = new AtomicBoolean(false);
		 * try (HTTPResponse response = request.send(cancel))
		 * {
		 *     // ... read response ...
		 * }
		 * </code></pre>
		 * @param cancelSwitch the cancel switch. Set to <code>true</code> to attempt to cancel.
		 * @return an HTTPResponse object.
		 * @throws IOException if an error happens during the read/write.
		 * @throws SocketTimeoutException if the socket read times out.
		 * @throws ProtocolException if the request method is incorrect, or not an HTTP URL.
		 */
		public HTTPResponse send(AtomicBoolean cancelSwitch) throws IOException
		{
			HTTPResponse out;
			HTTPRequest current = this;
			while (true)
			{
				out = httpFetch(current, cancelSwitch);
				if (!autoRedirect || !out.isAutoRedirectable())
					break;
				else
				{
					current = out.buildRedirect();
					out.close(); // close open response.
				}
			}
			return out;
		}

		/**
		 * Sends this request and gets a decoded response via an {@link HTTPReader}.
		 * <p>
		 * The response input stream auto-closes after read (but not the connection, which stays alive if possible).
		 * @param <T> the return type.
		 * @param reader the reader to use to read the response.
		 * @return the decoded object from the response.
		 * @throws IOException if an error happens during the read/write.
		 * @throws SocketTimeoutException if the socket read times out.
		 * @throws ProtocolException if the requestMethod is incorrect, or not an HTTP URL.
		 */
		public <T> T send(HTTPReader<T> reader) throws IOException
		{
			return send(null, reader);
		}

		/**
		 * Sends this request and gets a decoded response via an {@link HTTPReader}.
		 * <p>
		 * The response input stream auto-closes after read (but not the connection, which stays alive if possible).
		 * @param <T> the return type.
		 * @param cancelSwitch the cancel switch. Set to <code>true</code> to attempt to cancel.
		 * @param reader the reader to use to read the response.
		 * @return the decoded object from the response.
		 * @throws IOException if an error happens during the read/write.
		 * @throws SocketTimeoutException if the socket read times out.
		 * @throws ProtocolException if the requestMethod is incorrect, or not an HTTP URL.
		 */
		public <T> T send(AtomicBoolean cancelSwitch, HTTPReader<T> reader) throws IOException
		{
			try (HTTPResponse response = send(cancelSwitch))
			{
				return response.read(reader, cancelSwitch != null ? cancelSwitch : new AtomicBoolean(false));
			}
		}

		/**
		 * Sends this request and gets an open response.
		 * <p>
		 * The eventual return is best used with a try-with-resources block so that the response input stream auto-closes 
		 * (but not the connection, which stays alive if possible), like so:
		 * <pre><code>
		 * HTTPRequestFuture<HTTPResponse> future = request.sendAsync();
		 * 
		 * // ... code ...
		 *  
		 * try (HTTPResponse response = future.get())
		 * {
		 *     // ... read response ...
		 * }
		 * catch (ExecutionException | InterruptedException e)
		 * {
		 *     // ... 
		 * }
		 * </code></pre>
		 * @return a future for inspecting later, containing the open response object.
		 */
		public HTTPRequestFuture<HTTPResponse> sendAsync()
		{
			HTTPRequestFuture<HTTPResponse> out = new HTTPRequestFuture.Response(this);
			fetchExecutor().execute(out);
			return out;
		}

		/**
		 * Sends this request and gets a decoded response via an {@link HTTPReader}.
		 * <p>
		 * The response input stream auto-closes after read (but not the connection, which stays alive if possible).
		 * <pre><code>
		 * HTTPRequestFuture<String> future = request.sendAsync(HTTPReader.STRING_CONTENT_READER);
		 * 
		 * // ... code ...
		 *  
		 * try
		 * {
		 *     String content = future.get();
		 * }
		 * catch (ExecutionException | InterruptedException e)
		 * {
		 *     // ... 
		 * }
		 * </code></pre>
		 * @param <T> the return type.
		 * @param reader the reader to use to read the response.
		 * @return a future for inspecting later, containing the decoded object.
		 */
		public <T> HTTPRequestFuture<T> sendAsync(HTTPReader<T> reader)
		{
			HTTPRequestFuture<T> out = new HTTPRequestFuture.ObjectResponse<T>(this, reader);
			fetchExecutor().execute(out);
			return out;
		}

	}
	
	/**
	 * Makes an HTTP-acceptable ISO date string from a Date.
	 * @param date the date to format.
	 * @return the resultant string.
	 */
	public static String date(Date date)
	{
		return ISO_DATE.get().format(date);
	}
	
	/**
	 * Makes an HTTP-acceptable ISO date string from a Date, represented in milliseconds since the Epoch.
	 * @param dateMillis the millisecond date to format.
	 * @return the resultant string.
	 */
	public static String date(long dateMillis)
	{
		return ISO_DATE.get().format(new Date(dateMillis));
	}
	
	/**
	 * Makes a comma-space-separated list of values.
	 * @param values the values to join together.
	 * @return the resultant string.
	 */
	public static String list(String ... values)
	{
		return join(", ", values);
	}
	
	/**
	 * Joins a list of values into one string, placing a joiner between all of them.
	 * @param joiner the joining string.
	 * @param values the values to join together.
	 * @return the resultant string.
	 */
	public static String join(String joiner, String ... values)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++)
		{
			sb.append(values[i]);
			if (i < values.length - 1)
				sb.append(joiner);
		}
		return sb.toString();
	}
	
	/**
	 * Makes a "Value; Parameter" string.
	 * @param value the value.
	 * @param parameter the value parameter.
	 * @return a string that is equivalent to: <code>String.valueOf(value) + "; " + String.valueOf(parameter)</code>.
	 */
	public static String valueParam(String value, String parameter)
	{
		return String.valueOf(value) + "; " + String.valueOf(parameter);
	}
	
	/**
	 * Makes a "Key=Value" string.
	 * @param key the key.
	 * @param value the value.
	 * @return a string that is equivalent to: <code>String.valueOf(key) + '=' + String.valueOf(value)</code>.
	 */
	public static String keyValue(String key, String value)
	{
		return String.valueOf(key) + '=' + String.valueOf(value);
	}
	
	/**
	 * Creates a simple key-value pair.
	 * @param key the pair key.
	 * @param value the pair value (converted to a string via {@link String#valueOf(Object)}).
	 * @return a new pair.
	 */
	public static Map.Entry<String, String> entry(String key, Object value)
	{
		return new AbstractMap.SimpleEntry<>(key, String.valueOf(value));
	}
	
	/**
	 * Starts a new {@link HTTPCookie} object.
	 * @param key the cookie name.
	 * @param value the cookie value. 
	 * @return a new cookie object.
	 */
	public static HTTPCookie cookie(String key, String value)
	{
		return new HTTPCookie(key, value);
	}
	
	/**
	 * Starts a new {@link HTTPHeaders} object.
	 * @param entries the list of entries to add.
	 * @return a new header object.
	 * @see #entry(String, String)
	 * @see HTTPHeaders#setHeader(String, String)
	 */
	@SafeVarargs
	public static HTTPHeaders headers(Map.Entry<String, String> ... entries)
	{
		HTTPHeaders out = new HTTPHeaders();
		for (int i = 0; i < entries.length; i++)
		{
			Map.Entry<String, String> entry = entries[i];
			out.setHeader(entry.getKey(), entry.getValue());
		}
		return out;
	}
	
	/**
	 * Starts a new {@link HTTPParameters} object.
	 * Duplicate parameters are added.
	 * @param entries the list of entries to add.
	 * @return a new parameters object.
	 * @see #entry(String, String)
	 * @see HTTPParameters#addParameter(String, Object)
	 */
	@SafeVarargs
	public static HTTPParameters parameters(Map.Entry<String, String> ... entries)
	{
		HTTPParameters out = new HTTPParameters();
		for (int i = 0; i < entries.length; i++)
		{
			Map.Entry<String, String> entry = entries[i];
			out.addParameter(entry.getKey(), entry.getValue());
		}
		return out;
	}
	
	/**
	 * Creates a text blob content body for an HTTP request.
	 * @param contentType the data's content type.
	 * @param text the text data.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createTextContent(String contentType, String text)
	{
		try {
			return new TextContent(contentType, "utf-8", null, text.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("JVM does not support the UTF-8 charset [INTERNAL ERROR].");
		}
	}

	/**
	 * Creates a byte blob content body for an HTTP request.
	 * @param contentType the data's content type.
	 * @param bytes the byte data.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createByteContent(String contentType, byte[] bytes)
	{
		return new BlobContent(contentType, null, bytes);
	}

	/**
	 * Creates a byte blob content body for an HTTP request.
	 * @param contentType the data's content type.
	 * @param contentEncoding the data's encoding type (like gzip or what have you, can be null for none).
	 * @param bytes the byte data.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createByteContent(String contentType, String contentEncoding, byte[] bytes)
	{
		return new BlobContent(contentType, contentEncoding, bytes);
	}

	/**
	 * Creates a file-based content body for an HTTP request.
	 * <p>Note: This is NOT form-data content! See {@link MultipartFormContent} for that.
	 * @param contentType the file's content type.
	 * @param file the file to read from on send.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createFileContent(String contentType, File file)
	{
		return new FileContent(contentType, null, null, file);
	}

	/**
	 * Creates a file-based content body for an HTTP request.
	 * <p>Note: This is NOT form-data content! See {@link MultipartFormContent} for that.
	 * @param contentType the file's content type.
	 * @param encodingType the data encoding type for the file's payload (e.g. "gzip" or "base64").
	 * @param file the file to read from on send.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createFileContent(String contentType, String encodingType, File file)
	{
		return new FileContent(contentType, encodingType, null, file);
	}

	/**
	 * Creates a file-based content body for an HTTP request, presumably a text file.
	 * The default system text encoding is presumed.
	 * <p>Note: This is NOT form-data content! See {@link MultipartFormContent} for that.
	 * @param contentType the file's content type.
	 * @param file the file to read from on send.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createTextFileContent(String contentType, File file)
	{
		return new FileContent(contentType, null, Charset.defaultCharset().name(), file);
	}

	/**
	 * Creates file based content body for an HTTP request, presumably a text file encoded
	 * with a specific charset.
	 * <p>Note: This is NOT form-data content! See {@link MultipartFormContent} for that.
	 * @param contentType the file's content type.
	 * @param charsetType the charset name for the file's payload (e.g. "utf-8" or "ascii").
	 * @param file the file to read from on send.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createTextFileContent(String contentType, String charsetType, File file)
	{
		return new FileContent(contentType, null, charsetType, file);
	}

	/**
	 * Creates a WWW form, URL encoded content body for an HTTP request.
	 * <p>Note: This is NOT mulitpart form-data content! 
	 * See {@link MultipartFormContent} for mixed file attachments and fields.
	 * @param keyValueMap the map of key to value.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createFormContent(HTTPParameters keyValueMap)
	{
		return new FormContent(keyValueMap);
	}

	/**
	 * Creates a WWW form, URL encoded content body for an HTTP request.
	 * @return a content object representing the content.
	 * @see MultipartFormContent
	 */
	public static MultipartFormContent createMultipartContent()
	{
		return new MultipartFormContent();
	}

	/**
	 * Sets the default timeout to use for requests (if not overridden).
	 * @param timeoutMillis the timeout in milliseconds. A timeout of 0 is indefinite.
	 * @throws IllegalArgumentException if timeoutMillis is less than 0.
	 */
	public static void setDefaultTimeout(int timeoutMillis)
	{
		if (timeoutMillis < 0)
			throw new IllegalArgumentException("timeout cannot be less than 0");
		DEFAULT_TIMEOUT_MILLIS.set(timeoutMillis);
	}
	
	/**
	 * Sets the ThreadPoolExecutor to use for asynchronous requests.
	 * The previous executor, if any, is forcibly shut down.
	 * @param executor the thread pool executor to use for async request handling.
	 * @see ThreadPoolExecutor#shutdownNow()
	 */
	public static void setAsyncExecutor(ThreadPoolExecutor executor)
	{
		ThreadPoolExecutor old;
		if ((old = HTTP_THREAD_POOL.getAndSet(executor)) != null)
			old.shutdownNow();
	}
	
	/**
	 * Gets the content from a opening an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * If the response/stream is closed afterward, it will not close the connection (which may be pooled).
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpFetch(request, cancelSwitch))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param request the request object.
	 * @param cancelSwitch the cancel switch. Set to <code>true</code> to attempt to cancel. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @throws ProtocolException if the requestMethod is incorrect, or not an HTTP URL.
	 */
	private static HTTPResponse httpFetch(HTTPRequest request, AtomicBoolean cancelSwitch) throws IOException
	{
		String requestMethod = request.method;

		Objects.requireNonNull(requestMethod, "request method is null");
		URL url = new URL(urlParams(request.url, request.parameters));
		
		if (Arrays.binarySearch(VALID_HTTP, url.getProtocol()) < 0)
			throw new ProtocolException("This is not an HTTP URL.");
	
		HTTPHeaders headers = request.headers; 
		HTTPContent content = request.content; 
		String defaultResponseCharset = request.defaultCharsetEncoding;
		int socketTimeoutMillis = request.timeoutMillis; 
		TransferMonitor uploadMonitor = request.monitor;

		cancelSwitch = cancelSwitch != null ? cancelSwitch : new AtomicBoolean(false);
		
		// Check cancellation.
		if (cancelSwitch.get())
		{
			return null;
		}

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setReadTimeout(socketTimeoutMillis);
		conn.setRequestMethod(requestMethod);
		
		if (headers != null) for (Map.Entry<String, String> entry : headers.map.entrySet())
			conn.setRequestProperty(entry.getKey(), entry.getValue());
		for (HTTPCookie cookie : request.cookies)
			conn.addRequestProperty("Set-Cookie", cookie.toString());
	
		// set up body data.
		if (content != null)
		{
			long uploadLen = content.getLength();
			conn.setFixedLengthStreamingMode(content.getLength());
			conn.setRequestProperty("Content-Type", content.getContentType() == null ? "application/octet-stream" : content.getContentType());
			if (content.getEncoding() != null)
				conn.setRequestProperty("Content-Encoding", content.getEncoding());
			conn.setDoOutput(true);
			try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream()))
			{
				relay(content.getInputStream(), dos, 8192, uploadLen, cancelSwitch, uploadMonitor);
			}
		}
	
		// Check cancellation.
		if (cancelSwitch.get())
		{
			close(conn.getOutputStream());
			close(conn.getErrorStream());
			close(conn.getInputStream());
			return null;
		}
		
		return new HTTPResponse(request, conn, defaultResponseCharset);
	}
	
	// Fetches or creates the thread executor.
	private static ThreadPoolExecutor fetchExecutor()
	{
		ThreadPoolExecutor out;
		if ((out = HTTP_THREAD_POOL.get()) != null)
			return out;
		synchronized (HTTP_THREAD_POOL)
		{
			if ((out = HTTP_THREAD_POOL.get()) != null)
				return out;
			setAsyncExecutor(out = new HTTPThreadPoolExecutor());
		}
		return out;
	}

	private static final char[] HEX_NYBBLE = "0123456789ABCDEF".toCharArray();

	private static void writePercentChar(StringBuilder target, byte b)
	{
		target.append('%');
		target.append(HEX_NYBBLE[(b & 0x0f0) >> 4]);
		target.append(HEX_NYBBLE[b & 0x00f]);
	}
	
	private static String toURLEncoding(String s)
	{
		StringBuilder sb = new StringBuilder();
		byte[] bytes = s.getBytes(UTF8);
		for (int i = 0; i < s.length(); i++)
		{
			byte b = bytes[i];
			if (Arrays.binarySearch(URL_UNRESERVED, b) >= 0)
				sb.append((char)b);
			else if (Arrays.binarySearch(URL_RESERVED, b) >= 0)
				writePercentChar(sb, b);
			else
				writePercentChar(sb, b);
		}
		return sb.toString();
	}
	
	private static String urlParams(String url, HTTPParameters params)
	{
		return url + (params.map.isEmpty() ? "" : (url.indexOf('?') >= 0 ? '&' : '?') + params.toString()); 
	}
	
	/**
	 * Reads from an input stream, reading in a consistent set of data
	 * and writing it to the output stream. The read/write is buffered
	 * so that it does not bog down the OS's other I/O requests.
	 * This method finishes when the end of the source stream is reached.
	 * Note that this may block if the input stream is a type of stream
	 * that will block if the input stream blocks for additional input.
	 * This method is thread-safe.
	 * @param in the input stream to grab data from.
	 * @param out the output stream to write the data to.
	 * @param bufferSize the buffer size for the I/O. Must be &gt; 0.
	 * @param maxLength the maximum amount of bytes to relay, or null for no max.
	 * @param cancelSwitch the cancel switch. Set to <code>true</code> to attempt to cancel.
	 * @param monitor the transfer monitor to call on changes.
	 * @return the total amount of bytes relayed.
	 * @throws IOException if a read or write error occurs.
	 */
	private static long relay(InputStream in, OutputStream out, int bufferSize, Long maxLength, AtomicBoolean cancelSwitch, TransferMonitor monitor) throws IOException
	{
		long total = 0;
		int buf = 0;
			
		byte[] RELAY_BUFFER = new byte[bufferSize];
		
		while (!cancelSwitch.get() && (buf = in.read(RELAY_BUFFER, 0, Math.min(maxLength == null ? Integer.MAX_VALUE : (int)Math.min(maxLength, Integer.MAX_VALUE), bufferSize))) > 0)
		{
			out.write(RELAY_BUFFER, 0, buf);
			total += buf;
			if (monitor != null)
				monitor.onProgressChange(total, maxLength);
			if (maxLength != null && maxLength >= 0)
				maxLength -= buf;
		}
		return total;
	}
	
	/**
	 * Attempts to close an {@link AutoCloseable} object.
	 * If the object is null, this does nothing.
	 * @param c the reference to the AutoCloseable object.
	 */
	private static void close(AutoCloseable c)
	{
		if (c == null) return;
		try { c.close(); } catch (Exception e){}
	}

}
