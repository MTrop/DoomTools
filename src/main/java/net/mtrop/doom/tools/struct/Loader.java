/*******************************************************************************
 * Copyright (c) 2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A loading and caching mechanism.
 * The loader is provided a set of {@link LoaderFunction}s.
 * <p>The policy for each function is: each loader takes a {@link String} and either returns a corresponding Object, 
 * or null if the provided name does not correspond to a loaded Object. If no functions return an Object, it is
 * added to a set of non-existent Objects by name. 
 * <p> All of this object's functions should be assumed to be thread-safe.
 * @author Matthew Tropiano
 * @param <T> the object type returned/stored.
 */
public class Loader<T>
{
	/** The set of paths not loaded. */
	private Set<String> errorNames;
	/** A map of paths to loaded objects. */
	private Map<String, T> nameMap;
	/** Loader functions. */
	private LoaderFunction<T>[] loaderFunctions;
	/** The loader listener. */
	private LoaderErrorListener<T> errorListener;
	
	/** Thread pool for async loads. */
	private AtomicReference<ThreadPoolExecutor> threadPool;

	/**
	 * A loader function for loading an object via a name.
	 * @param <T> the object type.
	 */
	@FunctionalInterface
	public interface LoaderFunction<T>
	{
		/**
		 * Loads an object using a name.
		 * @param name the name to use for loading an object.
		 * @return a loaded object or null if not loaded.
		 * @throws Exception if an exception occurs.
		 */
		T load(String name) throws Exception;
	}
	
	/**
	 * A loader listener function for listening for errors on loaders.
	 * @param <T> 
	 */
	@FunctionalInterface
	public interface LoaderErrorListener<T>
	{
		/**
		 * Called when an exception happens in a {@link LoaderFunction#load(String)} call.
		 * @param function the function that produced the error.
		 * @param exception the exception that was created.
		 */
		void onLoaderError(LoaderFunction<T> function, Throwable exception);
	}
	
	/**
	 * Creates a loader that always returns a single object. 
	 * @param <T> The object type returned.
	 * @param object the object to always return.
	 * @return a new loader function that uses the working directory as a root.
	 */
	public static <T> LoaderFunction<T> createStaticLoader(T object)
	{
		return (unused) -> object;
	}

	/**
	 * Creates an object loader that treats the object name as a path to a file from the working directory. 
	 * @param <T> The object type returned.
	 * @param fileLoader the function that loads the source file (after it is confirmed to be a non-directory file that exists).
	 * @return a new loader function that uses the working directory as a root.
	 */
	public static <T> LoaderFunction<T> createFileLoader(Function<File, T> fileLoader)
	{
		return createFileLoader(new File("."), fileLoader);
	}

	/**
	 * Creates an object loader that treats the object name as a path to a file. 
	 * @param <T> The object type returned.
	 * @param directoryPath the root directory path for loading images.
	 * @param fileLoader the function that loads the source file (after it is confirmed to be a non-directory file that exists).
	 * @return a new loader function that uses the provided directory as a root.
	 * @throws IllegalArgumentException if the provided file is null or not a directory. 
	 */
	public static <T> LoaderFunction<T> createFileLoader(final String directoryPath, Function<File, T> fileLoader)
	{
		return createFileLoader(new File(directoryPath), fileLoader);
	}

	/**
	 * Creates an object loader that treats the object name as a path to a file. 
	 * @param <T> The object type returned.
	 * @param directory the root directory for loading images.
	 * @param fileLoader the function that loads the source file (after it is confirmed to be a non-directory file that exists).
	 * @return a new loader function that uses the provided directory as a root.
	 * @throws IllegalArgumentException if the provided file is null or not a directory. 
	 */
	public static <T> LoaderFunction<T> createFileLoader(final File directory, Function<File, T> fileLoader)
	{
		if (directory == null || directory.isDirectory())
			throw new IllegalArgumentException("Provided file is not a directory.");
		
		return (path) ->
		{
			File file = new File(directory.getPath() + "/" + path);
			if (!file.exists() || file.isDirectory())
				return null;
			return fileLoader.apply(file);
		};
	}
	
	/**
	 * Creates an object loader that treats the object name as a path to a classpath resource (from the current classloader). 
	 * @param <T> The object type returned.
	 * @param fileLoader the function that loads the source stream (after it is confirmed to exist).
	 * @return a new loader function that uses the provided string as a resource path prefix.
	 * @throws IllegalArgumentException if the provided file is null or not a directory. 
	 */
	public static <T> LoaderFunction<T> createResourceLoader(Function<InputStream, T> fileLoader)
	{
		return createResourceLoader(Thread.currentThread().getContextClassLoader(), "", fileLoader);
	}
	
	/**
	 * Creates an object loader that treats the object name as a path to a classpath resource (from the current classloader). 
	 * @param <T> The object type returned.
	 * @param prefix the root path for loading from classpath resources.
	 * @param fileLoader the function that loads the source stream (after it is confirmed to exist).
	 * @return a new loader function that uses the provided string as a resource path prefix.
	 * @throws IllegalArgumentException if the provided file is null or not a directory. 
	 */
	public static <T> LoaderFunction<T> createResourceLoader(final String prefix, Function<InputStream, T> fileLoader)
	{
		return createResourceLoader(Thread.currentThread().getContextClassLoader(), prefix, fileLoader);
	}
	
	/**
	 * Creates an object loader that treats the object name as a path to a classpath resource. 
	 * @param <T> The object type returned.
	 * @param loader the classloader to use.
	 * @param prefix the root path for loading from classpath resources.
	 * @param fileLoader the function that loads the source stream (after it is confirmed to exist).
	 * @return a new loader function that uses the provided string as a resource path prefix.
	 * @throws IllegalArgumentException if the provided file is null or not a directory. 
	 */
	public static <T> LoaderFunction<T> createResourceLoader(final ClassLoader loader, final String prefix, Function<InputStream, T> fileLoader)
	{
		return (path) ->
		{
			InputStream in = null;
			try {
				in = loader.getResourceAsStream(prefix + path);
				return fileLoader.apply(in);
			} finally {
				if (in != null) in.close();
			}
		};
	}
	
	/**
	 * Creates a new object loader.
	 * @param functions the functions to use for loading images, in the order specified.
	 * @throws IllegalArgumentException if functions is length 0.
	 */
	@SafeVarargs
	public Loader(LoaderFunction<T> ... functions)
	{
		if (functions.length == 0)
			throw new IllegalArgumentException("No provided loaders.");
		
		this.threadPool = new AtomicReference<ThreadPoolExecutor>(null);
		this.errorNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		this.nameMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.loaderFunctions = Arrays.copyOf(functions, functions.length);
		this.errorListener = null;
	}

	/**
	 * Sets the error listener function.
	 * @param listener the listener to set.
	 * @see LoaderErrorListener
	 */
	public void setErrorListener(LoaderErrorListener<T> listener)
	{
		this.errorListener = listener;
	}

	/**
	 * Sets the ThreadPoolExecutor to use for asynchronous loading.
	 * @param executor the thread pool executor to use for async loading.
	 */
	public void setAsyncExecutor(ThreadPoolExecutor executor)
	{
		synchronized (threadPool)
		{
			threadPool.set(executor);
		}
	}

	/**
	 * Explicitly sets an object to use for a given name.
	 * The name is case-insensitive.
	 * @param name the name of the object.
	 * @param object the corresponding object.
	 * @return this loader.
	 */
	public Loader<T> setObject(String name, T object)
	{
		synchronized (object) 
		{
			nameMap.put(name, object);
		}
		return this;
	}

	/**
	 * Get or load an object by name.
	 * The name is case-insensitive.
	 * @param name the name of the object.
	 * @return the corresponding loaded object, or null if not loaded.
	 */
	public T getObject(String name)
	{
		if (errorNames.contains(name))
			return null;
		if (nameMap.containsKey(name))
			return nameMap.get(name);
		
		T object = null;
		
		synchronized (loaderFunctions)
		{
			// Early outs for waiting threads.
			if (errorNames.contains(name))
				return null;
			if (nameMap.containsKey(name))
				return nameMap.get(name);
			
			for (int i = 0; i < loaderFunctions.length; i++)
			{
				try
				{
					if ((object = loaderFunctions[i].load(name)) != null)
						break;
				} 
				catch (Exception e) 
				{
					if (errorListener != null)
						errorListener.onLoaderError(loaderFunctions[i], e);
					continue;
				}
			}
		}
		
		if (object == null)
		{
			synchronized (errorNames)
			{
				errorNames.add(name);
			}
		}
		else
		{
			synchronized (nameMap)
			{
				nameMap.put(name, object);
			}
		}
		
		return object;
	}
	
	/**
	 * Get or load an object by name asynchronously.
	 * The name is case-insensitive.
	 * If a thread pool executor has not been set, a default one is created.
	 * @param name the name of the object.
	 * @return the corresponding loaded object, or null if not loaded.
	 * @see #getObject(String)
	 * @see #setAsyncExecutor(ThreadPoolExecutor)
	 */
	public LoaderFuture getObjectAsync(String name)
	{
		LoaderFuture out;
		fetchExecutor().execute(out = new LoaderFuture(name));
		return out;
	}
	
	/**
	 * @return the set of cached object names.
	 * @see Map#keySet()
	 */
	public Set<String> getCachedNameSet()
	{
		return nameMap.keySet();
	}

	/**
	 * Removes a cached object from the cache, and clears a name from the error names set.
	 * @param name the name of the object to remove.
	 * @return true if the file was removed from cache, false if it was never cached.
	 */
	public boolean remove(String name)
	{
		synchronized (errorNames)
		{
			errorNames.remove(name);
		}
		synchronized (nameMap)
		{
			return nameMap.remove(name) != null;
		}
	}
	
	/**
	 * Removes a set of cached objects from the cache, and clears the provided names from the error names set.
	 * @param nameSet the set of names.
	 * @return the amount of objects removed.
	 * @see #remove(String)
	 */
	public int remove(Collection<String> nameSet)
	{
		int out = 0;
		for (String name : nameSet)
			out += (remove(name) ? 1 : 0);
		return out;
	}
		
	/**
	 * Clears all object names that produced an error, so that they can be reloaded.
	 */
	public void clearErrorNames()
	{
		synchronized (errorNames) 
		{
			errorNames.clear();
		}
	}
	
	/**
	 * Clears all cached objects and errors.
	 */
	public void clear()
	{
		synchronized (nameMap)
		{
			synchronized (nameMap)
			{
				errorNames.clear();
				nameMap.clear();
			}
		}
	}
	
	// Fetches or creates the thread executor.
	private ThreadPoolExecutor fetchExecutor()
	{
		ThreadPoolExecutor out;
		if ((out = threadPool.get()) != null)
			return out;
		synchronized (threadPool)
		{
			if ((out = threadPool.get()) != null)
				return out;
			setAsyncExecutor(out = new LoaderThreadPoolExecutor());
		}
		return out;
	}

	/**
	 * The default executor to use for sending async requests.
	 */
	private static class LoaderThreadPoolExecutor extends ThreadPoolExecutor
	{
		private static final String THREADNAME = "LoaderRequestWorker-";
		
		private static final int CORE_SIZE = 0;
		private static final int MAX_SIZE = 20;
		private static final long KEEPALIVE = 5L;
		private static final TimeUnit KEEPALIVE_UNIT = TimeUnit.SECONDS;

		private static final AtomicLong REQUEST_ID = new AtomicLong(0L);
		
		private LoaderThreadPoolExecutor()
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
	 * A single instance of a spawned, potentially asynchronous executable task.
	 * Note that this class is a type of {@link RunnableFuture} - this can be used in places
	 * that {@link Future}s can also be used.
	 */
	public class LoaderFuture implements RunnableFuture<T>
	{
		// Locks
		private Object waitMutex;

		// State
		private String name;
		private Thread executor;
		private boolean done;
		private boolean running;
		protected boolean cancelled;
		private Throwable exception;
		private T finishedResult;

		/**
		 * Creates a new InstancedFuture.
		 * @param name the loader name.
		 */
		public LoaderFuture(String name)
		{
			this.cancelled = false;
			this.waitMutex = new Object();

			this.name = name;
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
			done = false;
			running = true;
			exception = null;
			finishedResult = null;

			try {
				finishedResult = execute();
			} catch (Throwable e) {
				exception = e;
			}
			
			executor = null;
			running = false;
			done = true;
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
				synchronized (waitMutex)
				{
					unit.timedWait(waitMutex, time);
				}
			}
		}

		/**
		 * Gets the exception thrown as a result of this instance completing, making the calling thread wait for its completion.
		 * @return the exception thrown by the encapsulated task, or null if no exception.
		 */
		public final Throwable getException()
		{
			if (!isDone())
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
		 * @param <R> the return type after the function.
		 * @param onSuccess the function to call on success with the result object, returning the return object.
		 * @return the result from the success function, or null if an exception happened.
		 */
		public final <R> R getAndThen(Function<T, R> onSuccess)
		{
			try {
				return onSuccess.apply(get());
			} catch (Exception e) {
				// Do nothing.
				return null;
			}
		}
		
		/**
		 * Performs a {@link #get()} and on success, calls the success function.
		 * If an exception would have happened, the success function is not called, and the exception function is called.
		 * @param <R> the return type after the function.
		 * @param onSuccess the function to call on success with the result object, returning the return object.
		 * @param onException the function to call on exception.
		 * @return the result from the success function, or the result from the exception function if an exception happened.
		 */
		public final <R> R getAndThen(Function<T, R> onSuccess, Function<Throwable, R> onException)
		{
			try {
				return onSuccess.apply(get());
			} catch (Exception e) {
				return onException.apply(exception);
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
		 * @throws IllegalStateException if the thread processing this future calls this method.
		 */
		public final void join()
		{
			try {
				result();
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
				cancelled = true;
				join();
				return true;
			}
		}

		@Override
		public final boolean isCancelled() 
		{
			return cancelled;
		}
		
		/**
		 * Executes this instance's callable payload.
		 * @return the result from the execution.
		 * @throws Throwable for any exception that may occur.
		 */
		protected T execute() throws Throwable
		{
			return getObject(name);
		}

		// Checks for livelocks.
		private void liveLockCheck()
		{
			if (executor == Thread.currentThread())
				throw new IllegalStateException("Attempt to make executing thread wait for this result.");
		}

	}

}
