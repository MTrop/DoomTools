/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

/**
 * One stop shop for environment variable value fetching.
 * @author Matthew Tropiano
 */
public final class Environment 
{
	/**
	 * Gets the path that DoomTools is running from.
	 * If null, this variable may not have been set - you are running from an IDE.
	 * @return the corresponding value.
	 * @throws SecurityException if the variable could not be retrieved.
	 */
	public static String getDoomToolsPath()
	{
		return System.getenv("DOOMTOOLS_PATH");
	}
	
	/**
	 * Gets the path of the JAR that DoomTools is running from.
	 * If null, this variable may not have been set - you are running from an IDE.
	 * @return the corresponding value.
	 * @throws SecurityException if the variable could not be retrieved.
	 */
	public static String getDoomToolsJarPath()
	{
		return System.getenv("DOOMTOOLS_JAR");
	}
		
}
