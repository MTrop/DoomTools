package net.mtrop.doom.tools.gui.managers;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.InstancedFuture.InstanceListener;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.SingletonProvider;

/**
 * DoomMake GUI logger singleton.
 * @author Matthew Tropiano
 */
public final class DoomToolsTaskManager 
{
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsTaskManager.class); 
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsTaskManager> INSTANCE = new SingletonProvider<>(() -> new DoomToolsTaskManager());
	
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsTaskManager get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	/** Thread pool. */
	private Executor executor;
	
	private DoomToolsTaskManager()
	{
		this.executor = Executors.newFixedThreadPool(8, new DefaultThreadFactory("DoomToolsThread"));
	}

	/**
	 * Spawns a new asynchronous task from a {@link Runnable}.
	 * @param runnable the callable to use.
	 * @return the new instance.
	 */
	public InstancedFuture<Void> spawn(Runnable runnable)
	{
		return spawn(() -> { runnable.run(); return null; });
	}
	
	/**
	 * Spawns a new asynchronous task from a {@link Callable}.
	 * @param <T> the return type for the future.
	 * @param callable the callable to use.
	 * @return the new instance.
	 */
	public <T> InstancedFuture<T> spawn(Callable<T> callable)
	{
		return InstancedFuture.instance(callable)
			.listener(createListener())
			.spawn(executor);
	}

	private <T> InstanceListener<T> createListener()
	{
		return new InstanceListener<T>()
		{
			@Override
			public void onStart(InstancedFuture<T> instance) 
			{
				LOG.infof("Started task.");
			}

			@Override
			public void onEnd(InstancedFuture<T> instance)
			{
				LOG.infof("Finished task.");
			}
		};
	}
	
	/**
	 * The thread factory used for the Thread Pool.
	 * Makes non-daemon threads that start with the name <code>"AsyncUtilsThread-"</code>.
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
			out.setDaemon(false);
			out.setPriority(Thread.NORM_PRIORITY);
			return out;
		}
		
	}

}
