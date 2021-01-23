/*******************************************************************************
 * Copyright (c) 2019-2020 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A lexer that scans for specific directives and affects the stream.
 * <ul>
 * <li><code>#include "file path"</code> - Pushes a file onto the stream stack.</li>
 * <li><code>#define MACRO ...tokens... </code> - Defines a macro and the string to replace them with. Defines are case-insensitive.</li>
 * <li><code>#undefine MACRO</code> - Undefines a macro.</li>
 * <li><code>#ifdef MACRO</code> - Includes the next set of lines (until <code>#endif</code>) if the provided macro is defined.</li>
 * <li><code>#ifndef MACRO</code> - Includes the next set of lines (until <code>#endif</code>) if the provided macro is NOT defined.</li>
 * <li><code>#endif</code> - Ends an "if" directive block.</li>
 * <li><code>#else</code> - Block that is used if an "if" block does not succeed.</li>
 * </ul>
 * @author Matthew Tropiano
 * @see Lexer
 */
public class PreprocessorLexer extends Lexer
{
	/** Preprocessor directive - Bang. */
	public static final String DIRECTIVE_BANG = "!";
	/** Preprocessor directive - Include. */
	public static final String DIRECTIVE_INCLUDE = "include";
	/** Preprocessor directive - Define. */
	public static final String DIRECTIVE_DEFINE = "define";
	/** Preprocessor directive - Undefine. */
	public static final String DIRECTIVE_UNDEFINE = "undefine";
	/** Preprocessor directive - If Defined. */
	public static final String DIRECTIVE_IFDEF = "ifdef";
	/** Preprocessor directive - If Undefined. */
	public static final String DIRECTIVE_IFNDEF = "ifndef";
	/** Preprocessor directive - End If. */
	public static final String DIRECTIVE_ENDIF = "endif";
	/** Preprocessor directive - Else. */
	public static final String DIRECTIVE_ELSE = "else";
	
	/** The singular instance for the default includer. */
	public static final DefaultIncluder DEFAULT_INCLUDER = new DefaultIncluder();
	
	/** 
	 * Default includer to use when none specified.
	 * This includer can either pull from the classpath, URIs, or files.
	 * <p>
	 * <ul>
	 * <li>Paths that start with {@code classpath:} are parsed as resource paths in the current classpath.</li>
	 * <li>
	 * 		Else, the path is interpreted as a file path, with the following search order:
	 * 		<ul>
	 * 			<li>Relative to parent of source stream.</li>
	 * 			<li>As is.</li>
	 * 		</ul>
	 * </li>
	 * </ul> 
	 */
	public static class DefaultIncluder implements Includer
	{
		private static final String CLASSPATH_PREFIX = "classpath:";
		private static final boolean IS_WINDOWS;
		
		static
		{
			IS_WINDOWS = System.getProperty("os.name").contains("Windows");
		}
		
		// cannot be instantiated outside of this class.
		private DefaultIncluder(){}

		@Override
		public String getIncludeResourcePath(String streamName, String path) throws IOException
		{
			if (IS_WINDOWS && streamName.contains("\\")) // check for Windows paths.
				streamName = streamName.replace('\\', '/');
			
			String streamParent = null;
			int lidx = -1; 
			if ((lidx = streamName.lastIndexOf('/')) >= 0)
				streamParent = streamName.substring(0, lidx + 1);

			if (path.startsWith(CLASSPATH_PREFIX))
			{
				return path;
			}
			else if (streamParent != null && streamParent.startsWith(CLASSPATH_PREFIX))
			{
				return (streamParent != null ? streamParent : "") + path;
			}
			else
			{
				File f = null;
				if (streamParent != null)
				{
					f = new File(streamParent + path);
					if (f.exists())
						return f.getPath();
					else
						return path;
				}
				else
				{
					return path;
				}
			}
		}
		
		@Override
		public InputStream getIncludeResource(String path) throws IOException
		{
			if (path.startsWith(CLASSPATH_PREFIX))
				return openResource(path.substring(CLASSPATH_PREFIX.length()));
			else
				return new FileInputStream(new File(path));
		}

		private static InputStream openResource(String pathString)
		{
			return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathString);
		}
		
	};
	
	/** Lambda interface that returns a string. */
	@FunctionalInterface
	public interface StringProvider
	{
		String get();
	}

	/**
	 * An interface that allows the user to resolve a resource by path when the
	 * PreprocessorLexer parses it.
	 */
	public interface Includer
	{
		/**
		 * Returns a full path for a path when the parser needs a resource.
		 * @param streamName the current name of the stream. This includer may use this to
		 * 		procure a relative path.
		 * @param path the stream path from the include directive.
		 * @return the path to a possible resource, or null if no possible path is available.
		 * @throws IOException if an error occurs procuring a potential stream.
		 */
		String getIncludeResourcePath(String streamName, String path) throws IOException;
	
		/**
		 * Returns an open {@link InputStream} for a path when the parser needs a resource.
		 * By default, this attempts to open a file at the provided path.
		 * @param path the resolved stream path from the include directive.
		 * @return an open {@link InputStream} for the requested resource, or null if not found.
		 * @throws IOException if an error occurs opening a stream.
		 */
		InputStream getIncludeResource(String path) throws IOException;
	
	}

	/** Is this at the beginning of a line? */
	private boolean lineBeginning;
	/** Map for define token to macro string. */
	private Map<String, StringProvider> macroMap;
	/** Latest IF clause result. */
	private Deque<Boolean> ifStack;
	/** Includer that defines how to find a file. */
	private Includer includer;

	/** List of errors. */
	private List<String> errors;

	/**
	 * Creates a new preprocessor lexer around a String, that will be wrapped into a StringReader.
	 * This will also assign this lexer a default name.
	 * @param kernel the lexer kernel to use for defining how to parse the input text.
	 * @param in the string to read from.
	 */
	public PreprocessorLexer(Kernel kernel, String in)
	{
		this(kernel, null, in);
	}
	
	/**
	 * Creates a new preprocessor lexer around a String, that will be wrapped into a StringReader.
	 * @param kernel the lexer kernel to use for defining how to parse the input text.
	 * @param name the name of this lexer.
	 * @param in the reader to read from.
	 */
	public PreprocessorLexer(Kernel kernel, String name, String in)
	{
		this(kernel, name, new StringReader(in));
	}
	
	/**
	 * Creates a new preprocessor lexer around a reader.
	 * This will also assign this lexer a default name.
	 * @param kernel the kernel to use for this lexer.
	 * @param in the reader to read from.
	 */
	public PreprocessorLexer(Kernel kernel, Reader in)
	{
		this(kernel, null, in);
	}
	
	/**
	 * Creates a new preprocessor lexer around a reader.
	 * @param kernel the kernel to use for this lexer.
	 * @param name the name of this lexer.
	 * @param in the reader to read from.
	 */
	public PreprocessorLexer(Kernel kernel, String name, Reader in)
	{
		super(kernel, name, in);
		this.lineBeginning = true;
		this.macroMap = new HashMap<>();
		this.includer = DEFAULT_INCLUDER;
		this.errors = new LinkedList<>();
		this.ifStack = new LinkedList<>();
	}

	/**
	 * Sets the primary includer to use for resolving included streams.
	 * @param includer the includer to use.
	 */
	public void setIncluder(Includer includer)
	{
		if (includer == null)
			throw new IllegalArgumentException("includer can not be null"); 
		this.includer = includer;
	}
	
	/**
	 * Adds a define macro to this lexer.
	 * @param macro the macro identifier.
	 * @param tokenString the string to push onto the lexer.
	 */
	public void addDefine(String macro, String tokenString)
	{
		macroMap.put(macro.toLowerCase(), ()->tokenString);
	}
	
	/**
	 * Adds a define macro to this lexer.
	 * @param macro the macro identifier.
	 * @param tokenProvider a string-producing lambda function.
	 */
	public void addDefine(String macro, StringProvider tokenProvider)
	{
		macroMap.put(macro.toLowerCase(), tokenProvider);
	}
	
	@Override
	public Token nextToken() throws IOException
	{
		Token token = super.nextToken();
		if (token == null)
			return null;
		
		String macro = token.getLexeme().toLowerCase();
		if (macroMap.containsKey(macro))
		{
			pushStream(getCurrentStreamName() + ":" + macro, new StringReader(macroMap.get(macro).get()));
			return nextToken();
		}
		return token;
	}
	
	protected String getInfoLine(String streamName, int lineNumber, String token, String message)
	{
		StringBuilder sb = new StringBuilder();
		if (streamName != null)
			sb.append("(").append(streamName).append(") ");
		sb.append("Line ").append(lineNumber);
		if (token != null)
			sb.append(", Token \"").append(token).append("\"");
		sb.append(": ");
		sb.append(message);
		return sb.toString();
	}

	@Override
	protected char readChar() throws IOException
	{
		while (true)
		{
			char c = super.readChar();
			
			if (c == END_OF_LEXER)
			{
				lineBeginning = true;
				return c;
			}
			
			if (c == END_OF_STREAM)
			{
				lineBeginning = true;
				return c;
			}
			
			if (c == '\n')
			{
				lineBeginning = true;
				if (ifStack.isEmpty() || ifStack.peek())
					return c;
			}
			else if (lineBeginning && c == '#')
			{
				lineBeginning = false;
				preprocess();
				return readChar();
			}
			else if (lineBeginning && Character.isWhitespace(c))
			{
				// keep lineBeginning
				if (ifStack.isEmpty() || ifStack.peek())
					return c;
			}
			else
			{
				lineBeginning = false;
				if (ifStack.isEmpty() || ifStack.peek())
					return c;
			}
		}
	}
	
	// Start preprocess.
	protected void preprocess() throws IOException
	{
		String streamName = getCurrentStreamName();
		int lineNumber = getCurrentLineNumber();
		
		StringBuilder sb = new StringBuilder();
		final int STATE_START = 0;
		final int STATE_READ = 1;
		final int STATE_ESCAPE = 2;
		int state = STATE_START;
		boolean breakloop = false;
		
		while (!breakloop)
		{
			char c = super.readChar();
			switch (state)
			{
				case STATE_START:
				{
					if (c == END_OF_LEXER)
						breakloop = true;
					else if (c == END_OF_STREAM)
						breakloop = true;
					else if (c == '\n')
						breakloop = true;
					else if (c == '\\')
						state = STATE_ESCAPE;
					else if (Character.isWhitespace(c))
					{
						// eat character.
					}
					else
					{
						sb.append(c);
						state = STATE_READ;
					}
				}
				break;
				
				case STATE_READ:
				{
					if (c == END_OF_LEXER)
						breakloop = true;
					else if (c == END_OF_STREAM)
						breakloop = true;
					else if (c == '\n')
					{
						lineBeginning = true;
						breakloop = true;
					}
					else if (c == '\\')
						state = STATE_ESCAPE;
					else
						sb.append(c);
				}
				break;
				
				case STATE_ESCAPE:
				{
					if (c == END_OF_LEXER)
						breakloop = true;
					else if (c == END_OF_STREAM)
						breakloop = true;
					else if (c == '\n')
					{
						sb.append('\n');
						state = STATE_READ;
					}
					else if (Character.isWhitespace(c))
					{
						// eat character.
					}
					else
					{
						sb.append('\\');
						state = STATE_READ;
					}
				}
				break;
			}
		}
		
		processDirectiveLine(streamName, lineNumber, sb.toString());
		if (!errors.isEmpty()) 
		{
			StringBuilder msg = new StringBuilder();
			Iterator<String> it = errors.iterator();
			while (it.hasNext())
			{
				msg.append(it.next());
				if (it.hasNext())
					msg.append('\n');
			}
			throw new PreprocessorException(msg.toString());
		}
	}
	
	/**
	 * Called when a full directive is read and needs to be processed.
	 * @param streamName the stream name.
	 * @param lineNumber the line number.
	 * @param directiveLine the directive line to process.
	 */
	protected void processDirectiveLine(String streamName, int lineNumber, String directiveLine)
	{
		// skip hashbangs
		if (directiveLine.startsWith(DIRECTIVE_BANG))
			return;

		DirectiveParser parser = new DirectiveParser();
		parser.reset();
		
		String directiveName = parser.scanNext(directiveLine);

		// #EndIf
		if (directiveName.equalsIgnoreCase(DIRECTIVE_ENDIF))
		{
			if (ifStack.isEmpty())
				errors.add(getInfoLine(streamName, lineNumber, directiveName, "#endif encountered without an #if"));
			else
				ifStack.poll();
		}
		// #Else
		else if (directiveName.equalsIgnoreCase(DIRECTIVE_ELSE))
		{
			if (ifStack.isEmpty())
				errors.add(getInfoLine(streamName, lineNumber, directiveName, "#else encountered without an #if"));
			else
				ifStack.push(!ifStack.poll());
		}
		// #Include
		else if (directiveName.equalsIgnoreCase(DIRECTIVE_INCLUDE))
		{
			String path = parser.scanNext(directiveLine);
			
			String includePath;
			InputStream includeIn;
			try {
				includePath = includer.getIncludeResourcePath(streamName, path);
				if (includePath == null)
					errors.add(getInfoLine(streamName, lineNumber, null, "Could not resolve path: \"" + path + "\""));
				includeIn = includer.getIncludeResource(includePath);
				if (includeIn == null)
					errors.add(getInfoLine(streamName, lineNumber, null, "Could not resolve path: \"" + includePath + "\""));
				else
					pushStream(includePath, new InputStreamReader(includeIn));
				
			} catch (IOException e) {
				errors.add(getInfoLine(streamName, lineNumber, null, "Could not resolve path. "+ e.getMessage()));
			}
		}
		// #Define
		else if (directiveName.equalsIgnoreCase(DIRECTIVE_DEFINE))
		{
			String defineToken = parser.scanNext(directiveLine);
			
			if (defineToken == null)
				errors.add(getInfoLine(streamName, lineNumber, null, "Expected macro identifier after #define"));
			else if (parser.state == DirectiveParser.STATE_STRING)
				errors.add(getInfoLine(streamName, lineNumber, null, "Expected identifier type token after #define, not string."));

			String data = parser.getRest(directiveLine).trim();
			
			macroMap.put(defineToken.toLowerCase(), ()->data);
		}
		// #Undefine
		else if (directiveName.equalsIgnoreCase(DIRECTIVE_UNDEFINE))
		{
			String defineToken = parser.scanNext(directiveLine);
			
			if (defineToken == null)
				errors.add(getInfoLine(streamName, lineNumber, defineToken, "Expected macro identifier after #define"));
			else if (parser.state == DirectiveParser.STATE_STRING)
				errors.add(getInfoLine(streamName, lineNumber, defineToken, "Expected identifier type token after #define, not string."));
			
			macroMap.remove(defineToken.toLowerCase());
		}
		// #IfDef
		else if (directiveName.equalsIgnoreCase(DIRECTIVE_IFDEF))
		{
			String defineToken = parser.scanNext(directiveLine);
			
			if (defineToken == null)
				errors.add(getInfoLine(streamName, lineNumber, defineToken, "Expected macro identifier after #define"));
			else if (parser.state == DirectiveParser.STATE_STRING)
				errors.add(getInfoLine(streamName, lineNumber, defineToken, "Expected identifier type token after #define, not string."));

			ifStack.push(macroMap.containsKey(defineToken.toLowerCase()));
		}
		// #IfNDef
		else if (directiveName.equalsIgnoreCase(DIRECTIVE_IFNDEF))
		{
			String defineToken = parser.scanNext(directiveLine);
			
			if (defineToken == null)
				errors.add(getInfoLine(streamName, lineNumber, defineToken, "Expected macro identifier after #define"));
			else if (parser.state == DirectiveParser.STATE_STRING)
				errors.add(getInfoLine(streamName, lineNumber, defineToken, "Expected identifier type token after #define, not string."));

			ifStack.push(!macroMap.containsKey(defineToken.toLowerCase()));
		}
		else
		{
			errors.add(getInfoLine(streamName, lineNumber, directiveName, "Not a valid directive: "+directiveName));
		}
	}
	
	// Parser state.
	private static class DirectiveParser
	{
		private static final int STATE_START = 0;
		private static final int STATE_TOKEN = 1;
		private static final int STATE_STRING = 2;
		private static final int STATE_STRING_ESCAPE = 3;
		
		private int index = 0;
		
		private int state = STATE_START;
		private StringBuilder tokenBuilder = new StringBuilder();
		
		void reset()
		{
			tokenBuilder.delete(0, tokenBuilder.length());
			state = STATE_START;
		}
		
		int getChar(String line)
		{
			if (index >= line.length())
				return -1;
			return line.charAt(index++);
		}
		
		String getRest(String line)
		{
			return line.substring(index);
		}
		
		String scanNext(String line)
		{
			reset();
			boolean breakloop = false;
			int x;
			while (!breakloop && (x = getChar(line)) > -1)
			{
				char c = (char)x;
				switch (state)
				{
					case DirectiveParser.STATE_START:
					{
						if (c == '"')
						{
							state = STATE_STRING;
						}
						else if (!Character.isWhitespace(c))
						{
							tokenBuilder.append(c);
							state = STATE_TOKEN;
						}
					}
					break;
					
					case DirectiveParser.STATE_TOKEN:
					{
						if (!Character.isWhitespace(c))
							tokenBuilder.append(c);
						else
							breakloop = true;
					}
					break;

					case DirectiveParser.STATE_STRING:
					{
						if (c == '"')
							breakloop = true;
						else if (c == '\\')
							state = STATE_STRING_ESCAPE;
						else
							tokenBuilder.append(c);
					}
					break;

					case DirectiveParser.STATE_STRING_ESCAPE:
					{
						switch(c)
						{
							case '0':
								tokenBuilder.append('\0');
								break;
								
							case 'b':
								tokenBuilder.append('\b');
								break;
								
							case 't':
								tokenBuilder.append('\t');
								break;
								
							case 'n':
								tokenBuilder.append('\n');
								break;
								
							case 'f':
								tokenBuilder.append('\f');
								break;
								
							case 'r':
								tokenBuilder.append('\r');
								break;
								
							case '\\':
								tokenBuilder.append('\\');
								break;

							// unicode char
							case 'u':
							{
								StringBuilder sb = new StringBuilder();
								for (int i = 0; i < 4; i++)
								{
									x = getChar(line);
									if (x < 0)
										break;
									
									c = (char)x;
									if (!isHexDigit(c))
									{
										tokenBuilder.append("\\u").append(sb);
										tokenBuilder.delete(0, tokenBuilder.length());
										state = STATE_STRING;
									}
									else
										sb.append(c);
								}
								
								if (tokenBuilder.length() > 0)
									tokenBuilder.append((char)(Integer.parseInt(sb.toString(), 16) & 0x0ffff));
							}
							break;
							
							// ascii char
							case 'x':
							{
								StringBuilder sb = new StringBuilder();
								for (int i = 0; i < 2; i++)
								{
									x = getChar(line);
									if (x < 0)
										break;
									
									c = (char)x;
									if (!isHexDigit((char)c))
									{
										tokenBuilder.append("\\x").append(sb);
										tokenBuilder.delete(0, tokenBuilder.length());
										state = STATE_STRING;
									}
									else
										sb.append(c);
								}
								
								if (tokenBuilder.length() > 0)
									tokenBuilder.append((char)(Integer.parseInt(sb.toString(), 16) & 0x0ff));
							}
							break;
						}
						
						state = STATE_STRING;
					}
					break;
				}
			}
		
			return tokenBuilder.toString();
		}
		
		boolean isHexDigit(char c)
		{
			return (c >= 0x0030 && c <= 0x0039) || 
				(c >= 0x0041 && c <= 0x0046) || 
				(c >= 0x0061 && c <= 0x0066);
		}
		
	}
	
	/**
	 * Thrown on preprocessor error. 
	 */
	public static class PreprocessorException extends RuntimeException
	{
		private static final long serialVersionUID = -1574421632209457335L;

		private PreprocessorException(String message)
		{
			super(message);
		}
	}
		
}
