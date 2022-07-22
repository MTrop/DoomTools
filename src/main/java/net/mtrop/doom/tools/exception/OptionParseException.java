/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.exception;

/**
 * Thrown when command line options can't be parsed.
 * @author Matthew Tropiano
 */
public class OptionParseException extends Exception
{
	private static final long serialVersionUID = 54765625841709782L;

	public OptionParseException(String message) 
	{
		super(message);
	}

	public OptionParseException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
}
