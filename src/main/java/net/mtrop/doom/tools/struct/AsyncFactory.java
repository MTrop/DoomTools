/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Factory for creating a thread pool that handles asynchronous tasks.
 * <p>This class uses an internal thread pool to facilitate asynchronous operations. All {@link Instance}s that the
 * "spawn" methods return are handles to the running routine, and are equivalent to {@link Future}s.
 * @author Matthew Tropiano
 */
public final class AsyncFactory
{
	/** Default amount of core threads. */
	public static final int DEFAULT_CORE_SIZE = 0;
	
	/** Default amount of max threads. */
	public static final int DEFAULT_MAX_SIZE = 20;

	/** Default keepalive time. */
	public static final long DEFAULT_KEEPALIVE_TIME = 30L;

	/** Default keepalive time unit. */
	public static final TimeUnit DEFAULT_KEEPALIVE_TIMEUNIT = TimeUnit.SECONDS;

	/** The thread name prefix. */
	public static final String DEFAULT_THREADNAME_PREFIX = "AsyncFactoryThread-";

	/** Async factory counter. */
	private static final AtomicLong AsyncFactoryID = new AtomicLong(0L);

	// No Process Error Listeners
	private static final ProcessStreamErrorListener[] NO_LISTENERS = new ProcessStreamErrorListener[0];


	/** Process ids. */
	private AtomicLong processId;
	/** Thread pool. */
	private ThreadPoolExecutor threadPool;
	
	
	/**
	 * Creates an AsyncFactory and new underlying thread pool with default values.
	 */
	public AsyncFactory()
	{
		this(
			DEFAULT_CORE_SIZE,
			DEFAULT_MAX_SIZE,
			DEFAULT_KEEPALIVE_TIME,
			DEFAULT_KEEPALIVE_TIMEUNIT
		);	
	}
	
	/**
	 * Creates an AsyncFactory and new underlying thread pool.
	 * @param coreSize the core amount of threads.
	 */
	public AsyncFactory(int coreSize)
	{
		this(
			DEFAULT_THREADNAME_PREFIX + AsyncFactoryID.getAndIncrement() + "-",
			coreSize, 
			coreSize, 
			DEFAULT_KEEPALIVE_TIME, 
			DEFAULT_KEEPALIVE_TIMEUNIT
		);	
	}
	
	/**
	 * Creates an AsyncFactory and new underlying thread pool.
	 * @param coreSize the core amount of threads.
	 * @param maxSize the maximum amount of threads to create.
	 * @param keepAlive the keep-alive time for the threads past the core amount that are idle.
	 * @param keepAliveTimeUnit the time unit for the keep-alive.
	 */
	public AsyncFactory(int coreSize, int maxSize, long keepAlive, TimeUnit keepAliveTimeUnit)
	{
		this(
			DEFAULT_THREADNAME_PREFIX + AsyncFactoryID.getAndIncrement() + "-",
			coreSize, 
			maxSize, 
			keepAlive, 
			keepAliveTimeUnit
		);	
	}
	
	/**
	 * Creates an AsyncFactory and new underlying thread pool.
	 * @param threadNamePrefix the name prefix for each thread in the pool.
	 * @param coreSize the core amount of threads.
	 * @param maxSize the maximum amount of threads to create.
	 * @param keepAlive the keep-alive time for the threads past the core amount that are idle.
	 * @param keepAliveTimeUnit the time unit for the keep-alive.
	 */
	public AsyncFactory(String threadNamePrefix, int coreSize, int maxSize, long keepAlive, TimeUnit keepAliveTimeUnit)
	{
		this(
			coreSize, 
			maxSize, 
			keepAlive, 
			keepAliveTimeUnit,
			new LinkedBlockingQueue<Runnable>(),
			new DefaultThreadFactory(threadNamePrefix)
		);	
	}
	
	/**
	 * Creates an AsyncFactory and new underlying thread pool.
	 * @param coreSize the core amount of threads.
	 * @param maxSize the maximum amount of threads to create.
	 * @param keepAlive the keep-alive time for the threads past the core amount that are idle.
	 * @param keepAliveTimeUnit the time unit for the keep-alive.
	 * @param workQueue the work queue for all incoming work that can't be allocated to a thread.
	 * @param threadFactory the thread factory to use.
	 */
	public AsyncFactory(int coreSize, int maxSize, long keepAlive, TimeUnit keepAliveTimeUnit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
	{
		this(new ThreadPoolExecutor(
			coreSize, 
			maxSize, 
			keepAlive, 
			keepAliveTimeUnit,
			workQueue,
			threadFactory
		));	
	}
	
	/**
	 * Creates an AsyncFactory that wraps an existing thread pool.
	 * @param threadPool the thread pool to wrap.
	 */
	public AsyncFactory(ThreadPoolExecutor threadPool)
	{
		this.processId = new AtomicLong(0L);
		this.threadPool = threadPool;
	}

	/**
	 * Spawns a Process, returning its return value.
	 * @param process the process to monitor - it should already be started.
	 * @return the new instance.
	 */
	public Instance<Integer> spawn(Process process)
	{
		return spawn(process, null, null, null, NO_LISTENERS);
	}
	
	/**
	 * Spawns a Process with Standard Input attached, returning its return value.
	 * <p>This will spawn a Runnable for each provided stream, which will each be responsible for piping data into the process and
	 * reading from it. The runnables terminate when the streams close. The streams also do not attach if the I/O is redirected
	 * ({@link Process#getOutputStream()} returns <code>null</code>).
	 * <p>It is important to close the input stream, or else the process may hang, waiting forever for input.
	 * @param process the process to monitor - it should already be started.
	 * @param stdin the Standard IN stream. If null, no input is provided.
	 * @param stdout the Standard OUT/ERROR stream. If null, no output is provided.
	 * @param listeners the error listeners to add, if any.
	 * @return the new instance.
	 */
	public Instance<Integer> spawn(Process process, InputStream stdin, OutputStream stdout, ProcessStreamErrorListener ... listeners)
	{
		return spawn(process, stdin, stdout, stdout, listeners);
	}
	
	/**
	 * Spawns a Process with the attached streams, returning its return value.
	 * <p>This will spawn a Runnable for each provided stream, which will each be responsible for piping data into the process and
	 * reading from it. The runnables terminate when the streams close. The streams also do not attach if the I/O is redirected
	 * ({@link Process#getInputStream()}, {@link Process#getErrorStream()}, or {@link Process#getOutputStream()} return <code>null</code>).
	 * <p>If the end of the provided input stream is reached or an error occurs, the pipe into the process is closed.
	 * @param process the process to monitor - it should already be started.
	 * @param stdin the Standard IN stream. If null, no input is provided.
	 * @param stdout the Standard OUT stream. If null, no output is provided.
	 * @param stderr the Standard ERROR stream. If null, no error output is provided.
	 * @param listeners the error listeners to add, if any.
	 * @return the new instance.
	 */
	public Instance<Integer> spawn(
		Process process, 
		final InputStream stdin,
		final OutputStream stdout, 
		final OutputStream stderr, 
		final ProcessStreamErrorListener ... listeners
	)
	{
		final long id = processId.getAndIncrement();
		final OutputStream stdInPipe = process.getOutputStream();
		final InputStream stdOutPipe = process.getInputStream();
		final InputStream stdErrPipe = process.getErrorStream();
		
		// Standard In
		(new PipeInToOutThread(id + "-In", stdin, stdInPipe, (e) -> {
			for (int i = 0; i < listeners.length; i++)
				listeners[i].onStreamError(ProcessStreamErrorListener.StreamType.STDIN, e);
		})).start();

		// Standard Out
		(new PipeInToOutThread(id + "Out", stdOutPipe, stdout, (e) -> {
			for (int i = 0; i < listeners.length; i++)
				listeners[i].onStreamError(ProcessStreamErrorListener.StreamType.STDOUT, e);
		})).start();
		
		// Standard Error
		(new PipeInToOutThread(id + "Error", stdErrPipe, stderr, (e) -> {
			for (int i = 0; i < listeners.length; i++)
				listeners[i].onStreamError(ProcessStreamErrorListener.StreamType.STDERR, e);
		})).start();
		
		Instance<Integer> out = new ProcessInstance(process);
		threadPool.execute(out);
		return out;
	}
	
	/**
	 * Spawns a new asynchronous task from a {@link Runnable}.
	 * @param listener the listener to immediately attach.
	 * @param runnable the runnable to use.
	 * @return the new instance.
	 */
	public Instance<Void> spawn(InstanceListener<Void> listener, Runnable runnable)
	{
		return spawn(listener, (Void)null, runnable);
	}

	/**
	 * Spawns a new asynchronous task from a {@link Runnable}.
	 * @param runnable the runnable to use.
	 * @return the new instance.
	 */
	public Instance<Void> spawn(Runnable runnable)
	{
		return spawn((Void)null, runnable);
	}

	/**
	 * Spawns a new asynchronous task from a {@link Runnable}.
	 * @param <T> the return type for the future.
	 * @param result the result to set on completion.
	 * @param runnable the runnable to use.
	 * @return the new instance.
	 */
	public <T> Instance<T> spawn(T result, Runnable runnable)
	{
		Instance<T> out = new RunnableInstance<T>(runnable, result);
		threadPool.execute(out);
		return out;
	}

	/**
	 * Spawns a new asynchronous task from a {@link Runnable}.
	 * @param <T> the return type for the future.
	 * @param listener the listener to immediately attach.
	 * @param result the result to set on completion.
	 * @param runnable the runnable to use.
	 * @return the new instance.
	 */
	public <T> Instance<T> spawn(InstanceListener<T> listener, T result, Runnable runnable)
	{
		Instance<T> out = new RunnableInstance<T>(runnable, result);
		out.setListener(listener);
		threadPool.execute(out);
		return out;
	}

	/**
	 * Spawns a new asynchronous task from a {@link Callable}.
	 * @param <T> the return type for the future.
	 * @param callable the callable to use.
	 * @return the new instance.
	 */
	public <T> Instance<T> spawn(Callable<T> callable)
	{
		Instance<T> out = new CallableInstance<T>(callable);
		threadPool.execute(out);
		return out;
	}

	/**
	 * Spawns a new asynchronous task from a {@link Callable}.
	 * @param <T> the return type for the future.
	 * @param listener the listener to immediately attach.
	 * @param callable the callable to use.
	 * @return the new instance.
	 */
	public <T> Instance<T> spawn(InstanceListener<T> listener, Callable<T> callable)
	{
		Instance<T> out = new CallableInstance<T>(callable);
		out.setListener(listener);
		threadPool.execute(out);
		return out;
	}

	/**
	 * Spawns a new asynchronous task from a {@link Cancellable}.
	 * <p>Note: {@link Monitorable}s are also Cancellables.
	 * @param <T> the return type for the future.
	 * @param cancellable the cancellable to use.
	 * @return the new instance.
	 */
	public <T> Instance<T> spawn(Cancellable<T> cancellable)
	{
		Instance<T> out = new CancellableInstance<T>(cancellable);
		threadPool.execute(out);
		return out;
	}

	/**
	 * Spawns a new asynchronous task from a {@link Cancellable}.
	 * <p>Note: {@link Monitorable}s are also Cancellables.
	 * @param <T> the return type for the future.
	 * @param listener the listener to immediately attach.
	 * @param cancellable the cancellable to use.
	 * @return the new instance.
	 */
	public <T> Instance<T> spawn(InstanceListener<T> listener, Cancellable<T> cancellable)
	{
		Instance<T> out = new CancellableInstance<T>(cancellable);
		out.setListener(listener);
		threadPool.execute(out);
		return out;
	}

	/**
	 * @return the active thread count.
	 * @see ThreadPoolExecutor#getActiveCount()
	 */
	public int getActiveCount()
	{
		return threadPool.getActiveCount();
	}
	
	/**
	 * @return the completed task count.
	 * @see ThreadPoolExecutor#getCompletedTaskCount()
	 */
	public long getCompletedTaskCount()
	{
		return threadPool.getCompletedTaskCount();
	}
	
	/**
	 * @return the task count.
	 * @see ThreadPoolExecutor#getTaskCount()
	 */
	public long getTaskCount()
	{
		return threadPool.getTaskCount();
	}
	
	/**
	 * @return the largest pool size.
	 * @see ThreadPoolExecutor#getLargestPoolSize()
	 */
	public int getLargestPoolSize()
	{
		return threadPool.getLargestPoolSize();
	}
	
	/**
	 * @return the maximum pool size.
	 * @see ThreadPoolExecutor#getMaximumPoolSize()
	 */
	public int getMaximumPoolSize()
	{
		return threadPool.getMaximumPoolSize();
	}
	
	/**
	 * Attempts to shut down the thread pool.
	 * @see ThreadPoolExecutor#shutdown()
	 */
	public void shutDown()
	{
		threadPool.shutdown();
	}
	
	/**
	 * Attempts to shut down the thread pool.
	 * @return the list of runnables in the pool.
	 * @see ThreadPoolExecutor#shutdownNow()
	 */
	public List<Runnable> shutDownNow()
	{
		return threadPool.shutdownNow();
	}
	
	private static int relay(InputStream in, OutputStream out) throws IOException
	{
		int total = 0;
		int buf = 0;
			
		byte[] RELAY_BUFFER = new byte[8192];
		
		while ((buf = in.read(RELAY_BUFFER)) > 0)
		{
			out.write(RELAY_BUFFER, 0, buf);
			total += buf;
		}
		return total;
	}

	/**
	 * A listener interface for all instances.
	 * @param <T> instance return type.
	 */
	public static interface InstanceListener<T>
	{
		/**
		 * Called on Instance start. 
		 * NOTE: The Instance, is this state, is NOT safe to inspect via blocking methods, and {@link Instance#isDone()} is guaranteed to return false.
		 * @param instance the instance that this is attached to.
		 */
		void onStart(Instance<T> instance);

		/**
		 * Called on Instance end.
		 * The Instance is safe to inspect, and {@link Instance#isDone()} is guaranteed to return true.
		 * @param instance the instance that this is attached to.
		 */
		void onEnd(Instance<T> instance);
	}
	
	/**
	 * A listener interface for {@link Monitorable} tasks. 
	 */
	@FunctionalInterface
	public static interface MonitorableListener
	{
		/**
		 * Called when the task reports a progress or message change in any way.
		 * @param indeterminate if true, progress is indeterminate - calculating it may result in a bad value.
		 * @param current the current progress.
		 * @param maximum the maximum progress.
		 * @param message the message.
		 */
		void onProgressChange(boolean indeterminate, double current, double maximum, String message);
	}

	/**
	 * A listener interface for {@link Process} tasks. 
	 */
	@FunctionalInterface
	public static interface ProcessStreamErrorListener
	{
		/** The stream type that produced the error. */
		enum StreamType
		{
			STDIN,
			STDOUT,
			STDERR
		}
		
		/**
		 * Called when a Process task reports an error.
		 * @param type the {@link StreamType}.
		 * @param exception the exception that happened.
		 */
		void onStreamError(StreamType type, Exception exception);
	}

	/**
	 * A {@link Cancellable} that can listen for changes/progress reported by it.
	 * @param <T> the result type.
	 */
	public static abstract class Monitorable<T> extends Cancellable<T>
	{
		private String message;
		private double currentProgress;
		private double maxProgress;
		private MonitorableListener listener;

		/**
		 * Creates the Monitorable.
		 */
		public Monitorable()
		{
			super();
			this.currentProgress = 0.0;
			this.maxProgress = 0.0;
			this.listener = null;
			this.message = null;
		}

		/**
		 * Sets the listener on this Monitorable.
		 * @param listener the listener to set.
		 */
		public final void setListener(MonitorableListener listener)
		{
			this.listener = listener;
		}

		private static boolean streql(String a, String b)
		{
			return a == b || (a != null && a.equals(b)) || (b != null && b.equals(a));
		}

		/**
		 * Sets the current progress message on this task.
		 * If the provided value is different from the current value, this will alert the listener.
		 * @param message the new current message.
		 */
		public final void setMessage(String message)
		{
			if (!streql(this.message, message))
			{
				this.message = message;
				changed();
			}
		}

		/**
		 * @return the current progress message on this task.
		 */
		public final String getMessage()
		{
			return message;
		}

		/**
		 * Sets the current progress on this task.
		 * If the provided value is different from the current value, this will alert the listener.
		 * @param current the new current progress.
		 */
		public final void setCurrentProgress(double current)
		{
			if (current != this.currentProgress)
			{
				this.currentProgress = current;
				changed();
			}
		}

		/**
		 * @return the current progress on this task.
		 */
		public double getCurrentProgress()
		{
			return currentProgress;
		}
		
		/**
		 * Sets the max progress on this task.
		 * If the provided value is different from the current value, this will alert the listener.
		 * @param max the new maximum progress.
		 */
		public final void setMaxProgress(double max)
		{
			if (max != this.maxProgress)
			{
				this.maxProgress = max;
				changed();
			}
		}

		/**
		 * @return the max progress on this task.
		 */
		public double getMaxProgress() 
		{
			return maxProgress;
		}
		
		/**
		 * Sets the progress on this task.
		 * If any of the provided values are different, this will alert the listener.
		 * @param message the progress message.
		 * @param current the current progress.
		 * @param max the maximum progress.
		 */
		public final void setProgress(String message, double current, double max)
		{
			if (!streql(this.message, message) || current != this.currentProgress || max != this.maxProgress)
			{
				this.message = message;
				this.currentProgress = current;
				this.maxProgress = max;
				changed();
			}
		}

		/**
		 * Sets the progress to "indeterminate."
		 */
		public final void setIndeterminate()
		{
			setProgress(message, 0.0, 0.0);
		}

		/**
		 * Checks if the progress is considered "indeterminate."
		 * This just checks if calculating the progress will result in a value that is not calculable.
		 * @return true if so, false if not.
		 * @see #setIndeterminate()
		 */
		public final boolean isIndeterminate()
		{
			return maxProgress == 0.0;
		}

		// Called on progress change.
		private final void changed()
		{	
			if (listener != null)
				listener.onProgressChange(isIndeterminate(), currentProgress, maxProgress, message);
		}

	}

	/**
	 * A {@link Callable} that can be flagged for cancellation.
	 * The code in {@link #call()} must check to see if it has been cancelled in order to
	 * honor the request, but does not need to guarantee it. 
	 * @param <T> the result type.
	 */
	public static abstract class Cancellable<T> implements Callable<T>
	{
		private boolean isCancelled;

		/**
		 * Creates the Cancellable.
		 */
		public Cancellable()
		{
			isCancelled = false;
		}

		/**
		 * Flags this Cancellable for cancellation.
		 */
		public void cancel()
		{
			isCancelled = true;
		}
		
		/**
		 * Checks if this has been flagged for cancellation.
		 * @return true if so, false if not.
		 * @see #cancel()
		 */
		public final boolean isCancelled()
		{
			return isCancelled;
		}

		@Override
		public abstract T call() throws Exception;

	}

	/**
	 * A single instance of a spawned, asynchronous executable task.
	 * Note that this class is a type of {@link RunnableFuture} - this can be used in places
	 * that {@link Future}s can also be used.
	 * @param <T> the result type.
	 */
	public static abstract class Instance<T> implements RunnableFuture<T>
	{
		protected Thread executor;
		
		private InstanceListener<T> listener;

		// === Locks
		private Object waitMutex;
		private Object listenerMutex;
		
		// === State
		private boolean done;
		private boolean running;
		
		// === Results
		private Throwable exception;
		private T finishedResult;
	
		private Instance()
		{
			this.executor = null;
			
			this.waitMutex = new Object();
			this.listenerMutex = new Object();

			this.done = false;
			this.running = false;
			this.exception = null;
			this.finishedResult = null;
		}
		
		/**
		 * Sets the instance listener.
		 * @param listener the listener to set.
		 */
		public final void setListener(InstanceListener<T> listener)
		{
			synchronized (listenerMutex)
			{
				this.listener = listener;
			}
		}
		
		@Override
		public final void run()
		{
			executor = Thread.currentThread();
			running = true;
			synchronized (listenerMutex)
			{
				if (listener != null)
					listener.onStart(this);
			}
			
			try {
				finishedResult = execute();
			} catch (Throwable e) {
				exception = e;
			}
			
			running = false;
			done = true;
			synchronized (waitMutex)
			{
				waitMutex.notifyAll();
			}
			synchronized (listenerMutex)
			{
				if (listener != null)
					listener.onEnd(this);
			}
			executor = null;
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
		 * Attempts to return the result of this instance, making the calling thread wait for its completion.
		 * <p>This is for convenience - this is like calling {@link #get()}, except it will only throw an
		 * encapsulated {@link RuntimeException} with an exception that {@link #get()} would throw as a cause.
		 * @return the result. Can be null if no result is returned, or this was cancelled before the return.
		 * @throws RuntimeException if a call to {@link #get()} instead of this would throw an exception.
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
		 * @return the result, or <code>null</code> if not finished.
		 */
		public final T resultNonBlocking()
		{
			return finishedResult;
		}
	
		/**
		 * Makes the calling thread wait until this task has finished, returning nothing.
		 */
		public final void join()
		{
			try {
				result();
			} catch (Exception e) {
				// Eat exception.
			}
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
	
	}

	/**
	 * The thread factory used for the Thread Pool.
	 * Makes daemon threads that start with the name <code>"AsyncUtilsThread-"</code>.
	 */
	private static class DefaultThreadFactory implements ThreadFactory
	{
		private AtomicLong threadId;
		private String threadNamePrefix;

		private DefaultThreadFactory(String threadNamePrefix)
		{
			this.threadId = new AtomicLong(0L);
			this.threadNamePrefix = threadNamePrefix;
		}

		@Override
		public Thread newThread(Runnable r)
		{
			Thread out = new Thread(r);
			out.setName(threadNamePrefix + threadId.getAndIncrement());
			out.setDaemon(true);
			out.setPriority(Thread.NORM_PRIORITY);
			return out;
		}
		
	}

	/**
	 * A thread that just pipes input to output.
	 */
	private static class PipeInToOutThread extends Thread
	{
		private InputStream in;
		private OutputStream out;
		private Consumer<IOException> exceptionConsumer;
		
		private PipeInToOutThread(String suffix, InputStream in, OutputStream out, Consumer<IOException> exceptionConsumer)
		{
			setDaemon(false);
			setName("ProcessPipe-" + suffix);
			this.in = in;
			this.out = out;
			this.exceptionConsumer = exceptionConsumer;
		}
		
		@Override
		public void run() 
		{
			try {
				relay(in, out);
			} catch (IOException e) {
				exceptionConsumer.accept(e);
			}
		}
	}

	/**
	 * Process encapsulation. 
	 */
	private static class ProcessInstance extends Instance<Integer>
	{
		private Process process;
		private boolean cancelled;

		private ProcessInstance(Process process) 
		{
			this.process = process;
			this.cancelled = false;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) 
		{
			process.destroy();
			cancelled = true;
			return true;
		}

		@Override
		public boolean isCancelled() 
		{
			return cancelled;
		}

		@Override
		protected Integer execute() throws Exception 
		{
			process.waitFor();
			return process.exitValue();
		}
		
	}
	
	/**
	 * Cancellable encapsulation. 
	 * @param <T> result type.
	 */
	private static class CancellableInstance<T> extends Instance<T>
	{
		private Cancellable<T> cancellable;
		
		private CancellableInstance(Cancellable<T> cancellable)
		{
			this.cancellable = cancellable;
		}

		@Override
		protected T execute() throws Exception
		{
			return cancellable.call();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			if (isDone())
				return false;
			if (mayInterruptIfRunning)
				executor.interrupt();
			cancellable.cancel();
			return true;
		}

		@Override
		public boolean isCancelled()
		{
			return cancellable.isCancelled();
		}

	}

	/**
	 * Callable encapsulation. 
	 * @param <T> result type.
	 */
	private static class CallableInstance<T> extends Instance<T>
	{
		private Callable<T> callable;

		private CallableInstance(Callable<T> callable)
		{
			this.callable = callable;
		}

		@Override
		protected T execute() throws Exception
		{
			return callable.call();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			// Do nothing.
			return false;
		}

		@Override
		public boolean isCancelled()
		{
			return false;
		}

	}

	/**
	 * Runnable encapsulation. 
	 * @param <T> result type.
	 */
	private static class RunnableInstance<T> extends Instance<T>
	{
		private Runnable runnable;
		private T result;

		private RunnableInstance(Runnable runnable, T result)
		{
			this.runnable = runnable;
			this.result = result;
		}

		@Override
		protected T execute() throws Exception
		{
			runnable.run();
			return result;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			// Do nothing.
			return false;
		}

		@Override
		public boolean isCancelled()
		{
			return false;
		}

	}

}
