/*******************************************************************************
 * Copyright (c) 2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.util.function.Supplier;

/**
 * An atomic-operation class that holds a single instance of a class type,
 * and ensures that no other thread can access it while it constructs.
 * @author Matthew Tropiano
 * @param <T> the singleton type.
 */
public class SingletonProvider<T>
{
	/** The creator function. */
	private Supplier<T> creator;
	/** The encapsulated instance. */
	private T instance;
	
	/** Constructing thread (state). */
	private Thread constructorThread;

	/**
	 * Creates a singleton instance provider.
	 * @param creator the creator function for supplying the singleton instance.
	 */
	public SingletonProvider(Supplier<T> creator)
	{
		this.creator = creator;
		this.instance = null;
		this.constructorThread = null;
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

			if (constructorThread == Thread.currentThread())
				throw new IllegalStateException("Re-entrance detected - singleton already under construction.");
			
			try {
				constructorThread = Thread.currentThread();
				return instance = creator.get();
			} finally {
				constructorThread = null;
			}
		}
	}
	
}
