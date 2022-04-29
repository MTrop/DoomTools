/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

/**
 * String manipulation functions.
 * @author Matthew Tropiano
 */
public final class StringUtils
{
	private StringUtils() {}
	
	/**
	 * Makes a new String with escape sequences in it.
	 * @param s	the original string.
	 * @return the new one with escape sequences in it.
	 */
	public static String withEscChars(String s)
	{
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < s.length(); i++)
			switch (s.charAt(i))
			{
				case '\0':
					out.append("\\0");
					break;
				case '\b':
					out.append("\\b");
					break;
				case '\t':
					out.append("\\t");
					break;
				case '\n':
					out.append("\\n");
					break;
				case '\f':
					out.append("\\f");
					break;
				case '\r':
					out.append("\\r");
					break;
				case '\\':
					out.append("\\\\");
					break;
				case '"':
					if (i != 0 && i != s.length() - 1)
						out.append("\\\"");						
					else
						out.append("\"");
					break;
				default:
					out.append(s.charAt(i));
					break;
			}
		return out.toString();
	}

	/**
	 * Escapes a string so that it can be input safely into a URL string.
	 * @param inString the input string.
	 * @return the escaped string.
	 */
	public static String urlEscape(String inString)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inString.length(); i++)
		{
			char c = inString.charAt(i);
			if (!((c >= 0x30 && c <= 0x39) || (c >= 0x41 && c <= 0x5a) || (c >= 0x61 && c <= 0x7a)))
				sb.append(String.format("%%%02x", (short)c));
			else
				sb.append(c);
			i++;
		}
		return sb.toString();
	}

	/**
	 * Decodes a URL-encoded string.
	 * @param inString the input string.
	 * @return the unescaped string.
	 */
	public static String urlUnescape(String inString)
	{
		StringBuilder sb = new StringBuilder();
		char[] chars = new char[2];
		int x = 0;
		
		final int STATE_START = 0;
		final int STATE_DECODE = 1;
		int state = STATE_START;
		
		for (int i = 0; i < inString.length(); i++)
		{
			char c = inString.charAt(i);
			
			switch (state)
			{
				case STATE_START:
					if (c == '%')
					{
						x = 0;
						state = STATE_DECODE;
					}
					else
						sb.append(c);
					break;
				case STATE_DECODE:
					chars[x++] = c;
					if (x == 2)
					{
						int v = 0;
						try {
							v = Integer.parseInt(new String(chars), 16);
							sb.append((char)(v & 0x0ff));
					} catch (NumberFormatException e) {
							sb.append('%').append(chars[0]).append(chars[1]);
						}
						state = STATE_START;
					}
					break;
			}
		}
		
		if (state == STATE_DECODE)
		{
			sb.append('%');
			for (int n = 0; n < x; n++)
				sb.append(chars[n]);
		}
		
		return sb.toString();
	}

	/**
	 * Adds a character sequence from the start of a string if it does not start with the sequence.
	 * @param input the input string to change.
	 * @param sequence the sequence to potentially add.
	 * @return a modified string or the input string if no modification.
	 */
	public static String addStartingSequence(String input, String sequence)
	{
		if (!input.startsWith(sequence))
			return sequence + input;
		else
			return input;
	}

	/**
	 * Adds a character sequence from the end of a string if it does not end with the sequence.
	 * @param input the input string to change.
	 * @param sequence the sequence to potentially add.
	 * @return a modified string or the input string if no modification.
	 */
	public static String addEndingSequence(String input, String sequence)
	{
		if (!input.endsWith(sequence))
			return input + sequence;
		else
			return input;
	}

	/**
	 * Removes a character sequence from the start of a string if it starts with the sequence.
	 * @param input the input string to change.
	 * @param sequence the sequence to potentially remove.
	 * @return a modified string or the input string if no modification.
	 */
	public static String removeStartingSequence(String input, String sequence)
	{
		if (input.startsWith(sequence))
			return input.substring(sequence.length());
		else
			return input;
	}

	/**
	 * Removes a character sequence from the end of a string if it exists.
	 * @param input the input string to change.
	 * @param sequence the sequence to potentially remove.
	 * @return a modified string or the input string if no modification.
	 */
	public static String removeEndingSequence(String input, String sequence)
	{
		if (input.endsWith(sequence))
			return input.substring(0, input.length() - sequence.length());
		else
			return input;
	}

	/**
	 * Joins an array of strings into one string, with a separator between them.
	 * @param separator the separator to insert between strings. Can be empty or null.
	 * @param strings the strings to join.
	 * @return a string of all joined strings and separators.
	 */
	public static String joinStrings(String separator, String... strings)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strings.length; i++)
		{
			sb.append(strings[i]);
			if (i < strings.length - 1)
				sb.append(separator);
		}
		return sb.toString();
	}

	/**
	 * Joins an array of strings into one string, with a separator between them.
	 * @param startIndex the starting index in the string array.
	 * @param separator the separator to insert between strings. Can be empty or null.
	 * @param strings the strings to join.
	 * @return a string of all joined strings and separators.
	 */
	public static String joinStrings(int startIndex, String separator, String... strings)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = startIndex; i < strings.length; i++)
		{
			sb.append(strings[i]);
			if (!ObjectUtils.isEmpty(separator) && i < strings.length - 1)
				sb.append(separator);
		}
		return sb.toString();
	}

	/**
	 * Joins elements in an <code>Iterable&lt;String&gt;</code> into one string, with a separator between them.
	 * @param separator the separator to insert between strings. Can be empty or null.
	 * @param strings the strings to join.
	 * @return a string of all joined strings and separators.
	 */
	public static String joinStrings(String separator, Iterable<String> strings)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = strings.iterator();
		while(it.hasNext())
		{
			sb.append(it.next());
			if (it.hasNext())
				sb.append(separator);
		}
		return sb.toString();
	}

	/**
	 * Prints a message out to standard out, word-wrapped
	 * to a set column width (in characters). The width cannot be
	 * 1 or less or this does nothing. This will also turn any whitespace
	 * character it encounters into a single space, regardless of speciality.
	 * @param message the output message.
	 * @param width the width in characters.
	 * @return the ending column for subsequent calls.
	 */
	public static int printWrapped(CharSequence message, int width)
	{
		return printWrapped(System.out, message, width);
	}

	/**
	 * Prints a message out to a PrintStream, word-wrapped
	 * to a set column width (in characters). The width cannot be
	 * 1 or less or this does nothing. This will also turn any whitespace
	 * character it encounters into a single space, regardless of speciality.
	 * @param out the print stream to use. 
	 * @param message the output message.
	 * @param width the width in characters.
	 * @return the ending column for subsequent calls.
	 */
	public static int printWrapped(PrintStream out, CharSequence message, int width)
	{
		return printWrapped(out, message, 0, width);
	}

	/**
	 * Prints a message out to a PrintStream, word-wrapped
	 * to a set column width (in characters). The width cannot be
	 * 1 or less or this does nothing. This will also turn any whitespace
	 * character it encounters into a single space, regardless of speciality.
	 * @param out the print stream to use. 
	 * @param message the output message.
	 * @param startColumn the starting column.
	 * @param width the width in characters.
	 * @return the ending column for subsequent calls.
	 */
	public static int printWrapped(PrintStream out, CharSequence message, int startColumn, int width)
	{
		return printWrapped(out, message, 0, 0, width);
	}

	/**
	 * Prints a message out to a PrintStream, word-wrapped
	 * to a set column width (in characters). The width cannot be
	 * 1 or less or this does nothing. This will also turn any whitespace
	 * character it encounters into a single space, regardless of specialty.
	 * @param out the print stream to use. 
	 * @param message the output message.
	 * @param startColumn the starting column.
	 * @param indent the indent line.
	 * @param width the width in characters.
	 * @return the ending column for subsequent calls.
	 */
	public static int printWrapped(PrintStream out, CharSequence message, int startColumn, int indent, int width)
	{
		if (width <= 1) return startColumn;
		
		StringBuilder token = new StringBuilder();
		StringBuilder line = new StringBuilder();
		int ln = startColumn;
		int tok = 0;
		for (int j = 0; j < indent; j++)
			line.append(' ');
		for (int i = 0; i < message.length(); i++)
		{
			char c = message.charAt(i);
			if (c == '\n')
			{
				line.append(token);
				ln += token.length();
				token.delete(0, token.length());
				tok = 0;
				out.println(line.toString());
				line.delete(0, line.length());
				ln = 0;
				for (int j = 0; j < indent; j++)
					line.append(' ');
			}
			else if (Character.isWhitespace(c))
			{
				line.append(token);
				ln += token.length();
				if (ln < width-1)
				{
					line.append(' ');
					ln++;
				}
				token.delete(0, token.length());
				tok = 0;
			}
			else if (c == '-')
			{
				line.append(token);
				ln += token.length();
				line.append('-');
				ln++;
				token.delete(0, token.length());
				tok = 0;
			}
			else if (ln + token.length() + 1 > width-1)
			{
				out.println(line.toString());
				line.delete(0, line.length());
				ln = 0;
				for (int j = 0; j < indent; j++)
					line.append(' ');
				token.append(c);
				tok++;
			}
			else
			{
				token.append(c);
				tok++;
			}
		}
		
		String linestr = line.toString();
		if (line.length() > 0)
			out.print(linestr);
		if (token.length() > 0)
			out.print(token.toString());
		
		return ln + tok;
	}

	/**
	 * Gets a full String representation of a Throwable type,
	 * including a line-by-line breakdown of the stack trace.
	 * @param t the throwable to render into a string.
	 * @return a multi-line string of the exception, similar to the stack dump.
	 */
	public static String getExceptionString(Throwable t)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(t.getClass().getName()+": "+t.getLocalizedMessage());
		sb.append('\n');
		for (StackTraceElement ent : t.getStackTrace())
		{
			sb.append(ent.toString());
			sb.append('\n');
		}
		if (t.getCause() != null)
		{
			sb.append("...Caused by:\n");
			sb.append(getExceptionString(t.getCause()));
		}
		return sb.toString();
	}

	/**
	 * Gets a full String representation of an exception as the JVM dumps it.
	 * @param t the throwable to render into a string.
	 * @return a multi-line string of the exception, similar to the stack dump.
	 * @see #getExceptionString(Throwable)
	 */
	public static String getJREExceptionString(Throwable t)
	{
		StringWriter sw;
		PrintWriter pw = new PrintWriter(sw = new StringWriter(), true);
		t.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}

}
