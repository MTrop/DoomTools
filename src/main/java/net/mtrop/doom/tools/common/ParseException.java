package net.mtrop.doom.tools.common;

public class ParseException extends Exception
{
	private static final long serialVersionUID = 6625142122356075134L;

	public ParseException()
	{
		super();
	}

	public ParseException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ParseException(String message)
	{
		super(message);
	}

	public ParseException(Throwable cause)
	{
		super(cause);
	}

}
