package net.mtrop.doom.tools.gui.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.SingletonProvider;

/**
 * DoomTools GUI pub-sub layer.
 * @author Matthew Tropiano
 */
public final class DoomToolsMessenger 
{
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsMessenger.class); 
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsMessenger> INSTANCE = new SingletonProvider<>(() -> new DoomToolsMessenger());

	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsMessenger get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	/** Subscriber list map for broadcasting message object. */
	private Map<String, List<Consumer<Object>>> subscriberListMap;
	/** Broadcasting lock. */
	private ReentrantReadWriteLock.ReadLock readLock;
	/** Subscriber lock. */
	private ReentrantReadWriteLock.WriteLock writeLock;
	
	private DoomToolsMessenger()
	{
		this.subscriberListMap = new HashMap<>();
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		this.readLock = lock.readLock();
		this.writeLock = lock.writeLock();
	}

	/**
	 * Subscribes a consumer to a channel.
	 * @param channel the channel name.
	 * @param listener the listener to add.
	 */
	public void subscribe(String channel, Consumer<Object> listener)
	{
		try {
			writeLock.lock();
			List<Consumer<Object>> list;
			if ((list = subscriberListMap.get(channel)) == null)
				subscriberListMap.put(channel, list = new LinkedList<>());
			list.add(listener);
		} finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * Unsubscribes a consumer from a channel.
	 * @param channel the channel name.
	 * @param listener the listener to remove.
	 */
	public void unsubscribe(String channel, Consumer<Object> listener)
	{
		try {
			writeLock.lock();
			List<Consumer<Object>> list;
			if ((list = subscriberListMap.get(channel)) != null)
			{
				list.remove(listener);
				if (list.isEmpty())
					subscriberListMap.remove(channel);
			}
		} finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * Publishes a message to a channel.
	 * @param channel the target channel.
	 * @param message the message to publish.
	 */
	public void publish(String channel, Object message)
	{
		try {
			readLock.lock();
			List<Consumer<Object>> list;
			if ((list = subscriberListMap.get(channel)) != null)
			{
				for (Consumer<Object> consumer : list)
				{
					try {
						consumer.accept(message);
					} catch (Throwable t) {
						LOG.error(t, "A message receiver threw an exception.");
					}
				}
			}
		} finally {
			readLock.unlock();
		}
	}
	
}
