/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A single instance of a spawned, potentially asynchronous executable task.
 * Note that this class is a type of {@link RunnableFuture} - this can be used in places
 * that {@link Future}s can also be used.
 * <p>
 * The functions that may be attached to an InstancedFuture as listeners/subscribers are called in this guaranteed order:
 * <ul>
 * <li>{@link InstanceListener#onStart(InstancedFuture)}</li>
 * <li>{@link #call()}</li>
 * <li>subscription.onResult</li>
 * <li>subscription.onError</li>
 * <li>subscription.onComplete</li>
 * <li>{@link InstanceListener#onEnd(InstancedFuture)}</li>
 * </ul>
 * After those functions get called, any threads waiting for a fetcher function, 
 * i.e. {@link #get()}, {@link #result()}, {@link #waitForDone()}, {@link #join()}, can continue.
 * @param <T> the result type.
 */
public abstract class InstancedFuture<T> implements RunnableFuture<T>
{
	private static final AtomicLong DEFAULT_THREADFACTORY_ID = new AtomicLong(0L);
	private static final ThreadFactory DEFAULT_THREADFACTORY = 
		(runnable) -> new Thread(runnable, "InstancedFuture-" + DEFAULT_THREADFACTORY_ID.getAndIncrement());
	
	// Locks
	private Object waitMutex;

	// Fields
	private InstanceListener<T> listener;
	private Subscription<T> subscription;
	
	// State
	private Throwable exception;
	private T finishedResult;

	private volatile Thread executor;
	private volatile boolean running;
	private volatile boolean done;
	
	/**
	 * Creates a new InstancedFuture.
	 * @param listener the listener for instance activity.
	 * @param subscription the subscription to attach, if any.
	 */
	protected InstancedFuture(InstanceListener<T> listener, Subscription<T> subscription)
	{
		this.waitMutex = new Object();
		
		this.listener = listener;
		this.subscription = subscription;
		
		this.exception = null;
		this.finishedResult = null;

		this.executor = null;
		this.running = false;
		this.done = false;
	}
	
	/**
	 * Runs a runnable in a new, started Thread.
	 * The Thread is named <code>InstancedFuture-id</code> and is not a daemon thread.
	 * @param runnable the runnable to execute.
	 * @return an instanced future.
	 */
	public static InstancedFuture<Void> spawn(Runnable runnable)
	{
		return spawn(asCallable(null, runnable));
	}
	
	/**
	 * Runs a runnable in a new, started Thread.
	 * The Thread is created with the provided ThreadFactory.
	 * @param threadFactory the factory for creating the new thread.
	 * @param runnable the runnable to execute.
	 * @return an instanced future.
	 */
	public static InstancedFuture<Void> spawn(ThreadFactory threadFactory, Runnable runnable)
	{
		return spawn(threadFactory, asCallable(null, runnable));
	}
	
	/**
	 * Runs a runnable via an {@link Executor}.
	 * @param executor the executor to add the Callable to.
	 * @param runnable the runnable to execute.
	 * @return an instanced future.
	 */
	public static InstancedFuture<Void> spawn(Executor executor, Runnable runnable)
	{
		return spawn(executor, asCallable(null, runnable));
	}
	
	/**
	 * Runs a runnable in a new, started Thread.
	 * The Thread is named <code>InstancedFuture-id</code> and is not a daemon thread.
	 * @param <T> the return type.
	 * @param result the result to return on a successful execution of the provided Runnable.
	 * @param runnable the runnable to execute.
	 * @return an instanced future.
	 */
	public static <T> InstancedFuture<T> spawn(T result, Runnable runnable)
	{
		return spawn(DEFAULT_THREADFACTORY, asCallable(result, runnable));
	}
	
	/**
	 * Runs a runnable in a new, started Thread.
	 * The Thread is created with the provided ThreadFactory.
	 * @param <T> the return type.
	 * @param threadFactory the factory for creating the new thread.
	 * @param result the result to return on a successful execution of the provided Runnable.
	 * @param runnable the runnable to execute.
	 * @return an instanced future.
	 */
	public static <T> InstancedFuture<T> spawn(ThreadFactory threadFactory, T result, Runnable runnable)
	{
		return spawn(threadFactory, asCallable(result, runnable));
	}
	
	/**
	 * Runs a runnable via an {@link Executor}.
	 * @param <T> the return type.
	 * @param executor the executor to add the Callable to.
	 * @param result the result to return on a successful execution of the provided Runnable.
	 * @param runnable the runnable to execute.
	 * @return an instanced future.
	 */
	public static <T> InstancedFuture<T> spawn(Executor executor, T result, Runnable runnable)
	{
		return spawn(executor, asCallable(result, runnable));
	}
	
	/**
	 * Runs a callable in a new, started Thread.
	 * The Thread is named <code>InstancedFuture-id</code> and is not a daemon thread.
	 * @param <T> the Callable return type.
	 * @param callable the Callable to execute.
	 * @return an instanced future.
	 */
	public static <T> InstancedFuture<T> spawn(Callable<T> callable)
	{
		return instance(callable).spawn();
	}
	
	/**
	 * Runs a callable in a new, started Thread.
	 * The Thread is created with the provided ThreadFactory.
	 * @param <T> the Callable return type.
	 * @param threadFactory the factory for creating the new thread.
	 * @param callable the Callable to execute.
	 * @return an instanced future.
	 */
	public static <T> InstancedFuture<T> spawn(ThreadFactory threadFactory, Callable<T> callable)
	{
		return instance(callable).spawn(threadFactory);
	}
	
	/**
	 * Runs a callable via an {@link Executor}.
	 * @param <T> the Callable return type.
	 * @param executor the executor to add the Callable to.
	 * @param callable the Callable to execute.
	 * @return an instanced future.
	 */
	public static <T> InstancedFuture<T> spawn(Executor executor, Callable<T> callable)
	{
		return instance(callable).spawn(executor);
	}
	
	/**
	 * Creates a new instance builder.
	 * @param runnable the runnable to execute.
	 * @return a new instance builder.
	 */
	public static Builder<Void> instance(Runnable runnable)
	{
		return instance(asCallable(null, runnable));
	}

	/**
	 * Creates a new instance builder.
	 * @param <T> the return type.
	 * @param result the result to return on a successful execution of the provided Runnable.
	 * @param runnable the runnable to execute.
	 * @return a new instance builder.
	 */
	public static <T> Builder<T> instance(T result, Runnable runnable)
	{
		return instance(asCallable(result, runnable));
	}

	/**
	 * Creates a new instance builder.
	 * @param <T> the Callable return type.
	 * @param callable the Callable that gets called in the instance.
	 * @return a new instance builder.
	 */
	public static <T> Builder<T> instance(Callable<T> callable)
	{
		return new Builder<>(callable);
	}

	/**
	 * Waits for all of the provided instances to complete, then continues execution.
	 * @param <T> the return type of each of the instances.
	 * @param instances the list of instances.
	 */
	@SafeVarargs
	public static <T> void join(InstancedFuture<T> ... instances)
	{
		for (int i = 0; i < instances.length; i++)
			instances[i].join();
	}

	@Override
	public final void run()
	{
		done = false;
		running = true;
		executor = Thread.currentThread();

		exception = null;
		finishedResult = null;

		try 
		{
			if (listener != null)
				listener.onStart(this);
			
			try {
				finishedResult = call();
			} catch (Throwable e) {
				exception = e;
			}

			if (subscription != null)
				subscription.emit(finishedResult, exception);
			
			if (listener != null)
				listener.onEnd(this);
		} 
		catch (Throwable e) 
		{
			throw new RuntimeException("Subscription or listener function threw an uncaught exception.", e);
		} 
		finally 
		{
			executor = null;
			running = false;
			done = true;
			
			synchronized (waitMutex)
			{
				waitMutex.notifyAll();
			}
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
				if (!isDone())
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
		} catch (InterruptedException e) {
			// Eat exception.
		}
	}

    /**
     * Convenience method for: <code>cancel(false)</code>.
     * @return {@code false} if the task could not be cancelled,
     * typically because it has already completed normally;
     * {@code true} otherwise
     * @see #cancel(boolean)
     * @see Future#cancel(boolean)
     */
	public final boolean cancel()
	{
		return cancel(false);
	}

	/**
	 * Called by {@link #run()} to get the value to return.
	 * @return the result of the call. 
	 * @throws Exception if an exception occurs.
	 */
	protected abstract T call() throws Exception;
	
	// Wrap Runnable to Callable.
	private static <T> Callable<T> asCallable(T result, Runnable runnable)
	{
		return () -> { runnable.run(); return result; };
	}
	
	// Checks for livelocks.
	private void liveLockCheck()
	{
		if (executor == Thread.currentThread())
			throw new IllegalStateException("Attempt to make executing thread wait for this result.");
	}
	
	/**
	 * Builder class for {@link InstancedFuture}s.
	 * @param <T> the return type of the encapsulated Callable.
	 */
	public static final class Builder<T>
	{
		private Callable<T> callable;
		private InstanceListener<T> listener;
		private Consumer<T> onResult;
		private Consumer<Throwable> onError;
		private Runnable onComplete;
		
		private Builder(Callable<T> callable)
		{
			this.callable = callable;
		}
		
		/**
		 * Adds a listener to this instance.
		 * @param listener the listener to add.
		 * @return this builder.
		 */
		public Builder<T> listener(InstanceListener<T> listener)
		{
			this.listener = listener;
			return this;
		}

		/**
		 * Subscribes a set of functions to execute on the completion of this instance.
		 * @param onResult the function to call with the result value.
		 * @param onComplete the function to call after the result or exception is processed.
		 * @return this builder.
		 * @see #onResult(Consumer)
		 * @see #onComplete(Runnable)
		 */
		public Builder<T> subscribe(Consumer<T> onResult, Runnable onComplete)
		{
			return onResult(onResult).onComplete(onComplete);
		}
		
		/**
		 * Subscribes a set of functions to execute on the completion of this instance.
		 * @param onResult the function to call with the result value.
		 * @param onError the function to call with the exception/throwable.
		 * @return this builder.
		 * @see #onResult(Consumer)
		 * @see #onError(Consumer)
		 */
		public Builder<T> subscribe(Consumer<T> onResult, Consumer<Throwable> onError)
		{
			return onResult(onResult).onError(onError);
		}
		
		/**
		 * Subscribes a set of functions to execute on the completion of this instance.
		 * @param onResult the function to call with the result value.
		 * @param onError the function to call with the exception/throwable.
		 * @param onComplete the function to call after the result or exception is processed.
		 * @return this builder.
		 * @see #onResult(Consumer)
		 * @see #onError(Consumer)
		 * @see #onComplete(Runnable)
		 */
		public Builder<T> subscribe(Consumer<T> onResult, Consumer<Throwable> onError, Runnable onComplete)
		{
			return onResult(onResult).onError(onError).onComplete(onComplete);
		}
		
		/**
		 * Sets the function to call when the instance completes successfully. 
		 * @param onResult the function to call with the result value.
		 * @return this builder.
		 */
		public Builder<T> onResult(Consumer<T> onResult)
		{
			this.onResult = onResult;
			return this;
		}
		
		/**
		 * Sets the function to call when the instance throws an exception. 
		 * @param onError the function to call with the exception/throwable.
		 * @return this builder.
		 */
		public Builder<T> onError(Consumer<Throwable> onError)
		{
			this.onError = onError;
			return this;
		}
		
		/**
		 * Sets the function to always call when the instance finishes (after result or exception). 
		 * @param onComplete the function to call after the result or exception is processed.
		 * @return this builder.
		 * @see #onResult(Consumer)
		 * @see #onError(Consumer)
		 */
		public Builder<T> onComplete(Runnable onComplete)
		{
			this.onComplete = onComplete;
			return this;
		}
		
		/**
		 * Spawns a runnable in a new, started Thread.
		 * The Thread is named <code>InstancedFuture-id</code> and is not a daemon thread.
		 * @return an instanced future.
		 */
		public InstancedFuture<T> spawn()
		{
			return spawn(DEFAULT_THREADFACTORY);
		}

		/**
		 * Spawns a runnable in a new, started Thread.
		 * The Thread is created with the provided ThreadFactory.
		 * @param threadFactory the factory for creating the new thread.
		 * @return an instanced future.
		 */
		public InstancedFuture<T> spawn(ThreadFactory threadFactory)
		{
			InstancedFuture<T> out;
			(threadFactory.newThread(out = new Created<>(callable, listener, new Subscription<>(onResult, onError, onComplete)))).start();
			return out;
		}
		
		/**
		 * Spawns a runnable in a new, started Thread.
		 * The Thread is created with the provided ThreadFactory.
		 * @param executor the factory for creating the new thread.
		 * @return an instanced future.
		 */
		public InstancedFuture<T> spawn(Executor executor)
		{
			InstancedFuture<T> out;
			executor.execute(out = new Created<>(callable, listener, new Subscription<>(onResult, onError, onComplete)));
			return out;
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
	 * A listener interface for all instances.
	 * The purpose of this listener is to report on instances starting or ending,
	 * mostly for monitoring behavior in a work queue or executor.
	 * @param <T> instance return type.
	 */
	public static interface InstanceListener<T>
	{
		/**
		 * Called on Instance start. 
		 * NOTE: The Instance, is this state, is NOT safe to inspect via blocking methods, and {@link InstancedFuture#isDone()} is guaranteed to return false.
		 * @param instance the instance that this is attached to.
		 */
		void onStart(InstancedFuture<T> instance);
	
		/**
		 * Called on Instance end.
		 * The Instance is safe to inspect, and {@link InstancedFuture#isDone()} is guaranteed to return true.
		 * @param instance the instance that this is attached to.
		 */
		void onEnd(InstancedFuture<T> instance);
	}

	/**
	 * Subscription class type.
	 * @param <T> the Future return type.
	 */
	private static class Subscription<T>
	{
		private Consumer<T> onResult;
		private Consumer<Throwable> onError;
		private Runnable onComplete;
		
		private Subscription(Consumer<T> onResult, Consumer<Throwable> onError, Runnable onComplete)
		{
			this.onResult = onResult;
			this.onError = onError;
			this.onComplete = onComplete;
		}
		
		private void emit(T result, Throwable exception)
		{
			if (exception != null)
			{
				if (onError != null)
					onError.accept(exception);
			}
			else 
			{ 
				if (onResult != null)
					onResult.accept(result);
			}
			
			if (onComplete != null)
				onComplete.run();
		}
	}

	/** Under-the-covers instance. */
	private static class Created<T> extends InstancedFuture<T>
	{
		private Callable<T> callable;

		public Created(Callable<T> callable, InstanceListener<T> listener, Subscription<T> subscription) 
		{
			super(listener, subscription);
			this.callable = Objects.requireNonNull(callable, "Callable cannot be null.");
		}

		@Override
		protected T call() throws Exception
		{
			return callable.call();
		}
		
		/**
		 * Cancels this task using the policy of the implemented method, {@link Future#cancel(boolean)},
		 * HOWEVER, this will be cancelled if and ONLY if the encapsulated task is of type {@link Cancellable}.
		 */
		@Override
		public final boolean cancel(boolean mayInterruptIfRunning)
		{
			if (isDone())
			{
				return false;
			}
			else if (callable instanceof Cancellable)
			{
				((Cancellable<T>)callable).cancel();
				
				Thread executor = getExecutor();
				if (mayInterruptIfRunning && executor != null)
					executor.interrupt();
				join();
				return true;
			}
			else
			{
				return false;
			}
		}

		@Override
		public final boolean isCancelled() 
		{
			return (callable instanceof Cancellable) && ((Cancellable<T>)callable).isCancelled();
		}
		
	}
	
}

