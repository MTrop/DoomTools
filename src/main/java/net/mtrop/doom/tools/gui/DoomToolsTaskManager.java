package net.mtrop.doom.tools.gui;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import net.mtrop.doom.tools.struct.AsyncFactory;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.AsyncFactory.Instance;
import net.mtrop.doom.tools.struct.AsyncFactory.InstanceListener;

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
	
	/** Async factory. */
	private AsyncFactory asyncFactory;
	
	private DoomToolsTaskManager()
	{
		this.asyncFactory = new AsyncFactory(0, 4, 5, TimeUnit.SECONDS);
	}

	/**
	 * Spawns a new asynchronous task from a {@link Runnable}.
	 * @param runnable the callable to use.
	 * @return the new instance.
	 */
	public Instance<Void> spawn(Runnable runnable)
	{
		return asyncFactory.spawn(createListener(), runnable);
	}
	
	/**
	 * Spawns a new asynchronous task from a {@link Callable}.
	 * @param <T> the return type for the future.
	 * @param callable the callable to use.
	 * @return the new instance.
	 */
	public <T> Instance<T> spawn(Callable<T> callable)
	{
		return asyncFactory.spawn(createListener(), callable);
	}

	private <T> InstanceListener<T> createListener()
	{
		return new InstanceListener<T>()
		{
			@Override
			public void onStart(Instance<T> instance) 
			{
				LOG.infof("Started task.");
			}

			@Override
			public void onEnd(Instance<T> instance)
			{
				LOG.infof("Finished task.");
			}
		};
	}
	
}
