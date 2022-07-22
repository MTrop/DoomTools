/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.exception;

/**
 * Thrown when some kind of error happens in a utility 
 * and I can't be bothered to make it more descriptive.
 * @author Matthew Tropiano
 */
public class UtilityException extends Exception
{
	private static final long serialVersionUID = -264135629022785043L;

	public UtilityException(String message) 
	{
		super(message);
	}

	public UtilityException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
}
