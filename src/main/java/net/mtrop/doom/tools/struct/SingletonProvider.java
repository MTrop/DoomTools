/*******************************************************************************
 * Copyright (c) 2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * An atomic-operation class that holds a single instance of a class type,
 * and ensures that no other thread can access it while it constructs, and that circular
 * references do not occur with other constructing {@link SingletonProvider} objects.
 * @author Matthew Tropiano
 * @param <T> the singleton type.
 */
public class SingletonProvider<T> implements Supplier<T>
{
	/** Set of currently-constructing singletons. */
	private static final Set<SingletonProvider<?>> CONSTRUCTING = new HashSet<>();
	
	// Registers that this is constructing.
	private static void registerConstruction(SingletonProvider<?> provider)
	{
		synchronized (CONSTRUCTING)
		{
			if (CONSTRUCTING.contains(provider))
				throw new IllegalStateException("Circular reference/crossover detected - singleton already under construction.");
			CONSTRUCTING.add(provider);
		}
	}
	
	// De-Registers that this is constructing.
	private static void deregisterConstruction(SingletonProvider<?> provider)
	{
		synchronized (CONSTRUCTING)
		{
			CONSTRUCTING.remove(provider);
		}
	}
	
	/* ==================================================================== */
	
	/** The creator function. */
	private Supplier<T> creator;
	/** The encapsulated instance. */
	private volatile T instance;
	
	/**
	 * Creates a singleton instance provider.
	 * @param creator the creator function for supplying the singleton instance.
	 */
	public SingletonProvider(Supplier<T> creator)
	{
		this.creator = creator;
		this.instance = null;
	}
	
	/**
	 * Fetches or constructs this singleton.
	 * <p> If the instance has not been made yet, the calling thread will be allowed to
	 * construct, set, and return the singleton. All other fetches will not require lock acquisition.
	 * <p> This method will also detect re-entrance by the constructing thread in order to catch circular
	 * dependencies.
	 * @return the singleton instance.
	 * @throws IllegalStateException if this is already being constructed by this thread, detecting a circular reference.
	 */
	public T get()
	{
		if (instance != null)
			return instance;
		
		synchronized (this)
		{
			// Early out for waiting threads.
			if (instance != null)
				return instance;

			try {
				registerConstruction(this);
				return instance = creator.get();
			} finally {
				deregisterConstruction(this);
			}
		}
	}
	
}
