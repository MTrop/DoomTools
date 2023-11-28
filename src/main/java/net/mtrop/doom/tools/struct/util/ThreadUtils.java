/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.util;

/**
 * Simple threading utility functions.
 * @author Matthew Tropiano
 */
public final class ThreadUtils
{
	/**
	 * Calls <code>Thread.sleep()</code> but in an encapsulated try
	 * to avoid catching InterruptedException. Convenience
	 * method for making the current thread sleep when you don't
	 * care if it's interrupted or not and want to keep code neat.
	 * @param millis the amount of milliseconds to sleep.
	 * @see #sleep(long)
	 */
	public static void sleep(long millis)
	{
		try {Thread.sleep(millis);} catch (InterruptedException e) {}
	}

	/**
	 * Calls <code>Thread.sleep()</code> but in an encapsulated try
	 * to avoid catching InterruptedException. Convenience
	 * method for making the current thread sleep when you don't
	 * care if it's interrupted or not and want to keep code neat.
	 * @param millis the amount of milliseconds to sleep.
	 * @param nanos the amount of additional nanoseconds to sleep.
	 * @see #sleep(long, int)
	 */
	public static void sleep(long millis, int nanos)
	{
		try {Thread.sleep(millis, nanos);} catch (InterruptedException e) {}
	}

}
