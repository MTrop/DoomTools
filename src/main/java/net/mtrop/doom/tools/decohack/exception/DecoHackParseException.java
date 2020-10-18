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
