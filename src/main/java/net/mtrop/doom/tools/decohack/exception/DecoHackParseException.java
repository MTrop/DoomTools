/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.exception;

/**
 * Thrown when a DECOHack script can't be parsed.
 * @author Matthew Tropiano
 */
public class DecoHackParseException extends RuntimeException
{
	private static final long serialVersionUID = -6079154418417970785L;

	public DecoHackParseException(String message) 
	{
		super(message);
	}

	public DecoHackParseException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
}
