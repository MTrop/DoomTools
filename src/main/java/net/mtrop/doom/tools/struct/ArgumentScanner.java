package net.mtrop.doom.tools.struct;

/**
 * A token scanner that is argument-aware.
 * @author Matthew Tropiano
 */
public class ArgumentScanner extends TokenScanner
{
	private static final ThreadLocal<StringBuilder> BUILDER = ThreadLocal.withInitial(()->new StringBuilder());

	/** Arguments. */
	private String[] arguments;

	public ArgumentScanner(String[] args, String str)
	{
		super(str);
		this.arguments = args;
	}

	/**
	 * Returns the string argument of a certain index. 
	 * @param index the index.
	 * @return the corresponding string, or empty string if not valid.
	 */
	public String getArgument(int index)
	{
		if (index < 0 || index >= arguments.length)
			return "$" + index;
		else
			return arguments[index];
	}
	
	@Override
	public String nextToken() 
	{
		String token = super.nextToken();
		if (token == null)
			return null;
		
		StringBuilder sb = BUILDER.get();
		sb.delete(0, sb.length());
		
		final int STATE_START = 0;
		final int STATE_ARG_START = 1;
		final int STATE_ARG = 2;
		int state = STATE_START;
		int argIndex = 0;
		for (int i = 0; i < token.length(); i++)
		{
			char c = token.charAt(i);
			switch (state)
			{
				case STATE_START:
				{
					if (c == '$')
					{
						argIndex = 0;
						state = STATE_ARG_START;
					}
					else
					{
						sb.append(c);
					}
				}
				break;
				
				case STATE_ARG_START:
				{
					if (c == '$')
					{
						sb.append('$');
						state = STATE_START;
					}
					else if (c >= '0' && c <= '9')
					{
						argIndex *= 10;
						argIndex += (c - '0');
						state = STATE_ARG;
					}
					else
					{
						sb.append('$').append(c);
						state = STATE_START;
					}
				}
				break;
				
				case STATE_ARG:
				{
					if (c >= '0' && c <= '9')
					{
						argIndex *= 10;
						argIndex += (c - '0');
						state = STATE_ARG;
					}
					else if (c == '$')
					{
						sb.append(getArgument(argIndex));
						state = STATE_ARG_START;
					}
					else
					{
						sb.append(getArgument(argIndex));
						sb.append(c);
						state = STATE_START;
					}
				}
				break;
			}
		}
		
		if (state == STATE_ARG)
			sb.append(getArgument(argIndex));
		else if (state == STATE_ARG_START)
			sb.append('$');
		
		return sb.toString();
	}
	
}
