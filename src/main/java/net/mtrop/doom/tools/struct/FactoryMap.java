/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A map that holds singleton values and {@link Supplier}s that will generate missing entries.
 * All "gets" may trigger generating a supplier, which is called on get to get the first instance
 * of the corresponding object.
 * <p> This map is thread-safe on each key - if the same key is being fetched and is being constructed,
 * the fetching threads will wait until the Supplier completes.
 * <p> The key type best used is one that has a well-defined equality function.
 * @author Matthew Tropiano
 * @param <K> the key type.
 * @param <V> the value type. 
 */
public abstract class FactoryMap<K, V> 
{
	private static final int DEFAULT_CAPACITY = 8;
	private static final float DEFAULT_REHASH = 0.75f;
	
	/** Key map, for locking. */
	private Map<K, WeakReference<K>> keyMap;
	
	/** Reference map. */
	private Map<K, V> refMap;
	
	/**
	 * Creates a new FactoryMap.
	 */
	public FactoryMap()
	{
		this(DEFAULT_CAPACITY, DEFAULT_REHASH);
	}
	
	/**
	 * Creates a new FactoryMap with an initial capacity.
	 * @param capacity the map's initial capacity.
	 */
	public FactoryMap(int capacity)
	{
		this(capacity, DEFAULT_REHASH);
	}
	
	/**
	 * Creates a new FactoryMap with an initial capacity and rehash ratio.
	 * @param capacity the map's initial capacity.
	 * @param rehash the map's rehashing ratio.
	 */
	public FactoryMap(int capacity, float rehash)
	{
		this.keyMap = new HashMap<>(capacity, rehash);
		this.refMap = new HashMap<>(capacity, rehash);
	}
	
	/**
	 * Gets the corresponding value for a key.
	 * May require a fetch if it has not been fetched before.
	 * @param key the key.
	 * @return the corresponding value.
	 */
	public V get(K key)
	{
		V out;
		if ((out = refMap.get(key)) != null)
			return out;

		try {
			synchronized (uniqueKey(key))
			{
				// Early out.
				if ((out = refMap.get(key)) != null)
					return out;
				
				Supplier<V> supplier = getSupplierForKey(key);
				synchronized (refMap)
				{
					refMap.put(key, out = (supplier != null ? supplier.get() : null));
				}
				return out;
			}
		} finally {
			releaseKey(key);
		}
	}
	
	/**
	 * Removes a value that corresponds to a key.
	 * That next fetch will force a re-fetch.
	 * @param key the key.
	 */
	protected void remove(K key)
	{
		synchronized (refMap)
		{
			refMap.remove(key);
		}
	}

	/**
	 * Called to get a supplier for a specific key.
	 * This supplier is executed right afterward to get the value.
	 * @param key the key.
	 * @return a Supplier to use for fetching the value.
	 */
	protected abstract Supplier<V> getSupplierForKey(K key);

	/**
	 * Gets the unique reference for a specific value instance.
	 * This method is thread-safe.
	 * @param value the value to use.
	 * @return a common reference for the provided value.
	 * @see #equals(Object)
	 * @see #hashCode()
	 */
	private K uniqueKey(K value)
	{
		WeakReference<K> out;
		synchronized (keyMap)
		{
			if ((out = keyMap.get(value)) == null)
				keyMap.put(value, out = new WeakReference<K>(value));
		}
		return out.get();
	}
	
	/**
	 * Removes the reference for a value.
	 * If the entry does not exist, this does nothing.
	 * This method is thread-safe.
	 * @param value the value to use.
	 */
	private void releaseKey(K value)
	{
		if (!keyMap.containsKey(value))
			return;
		
		synchronized (keyMap) 
		{
			keyMap.remove(value);
		}
	}
	
}
