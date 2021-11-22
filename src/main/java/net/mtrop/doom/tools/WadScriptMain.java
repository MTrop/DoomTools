/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import com.blackrook.rookscript.Script;
import com.blackrook.rookscript.ScriptAssembler;
import com.blackrook.rookscript.ScriptEnvironment;
import com.blackrook.rookscript.ScriptInstance;
import com.blackrook.rookscript.ScriptInstanceBuilder;
import com.blackrook.rookscript.ScriptValue;
import com.blackrook.rookscript.ScriptValue.ErrorType;
import com.blackrook.rookscript.desktop.functions.DesktopFunctions;
import com.blackrook.rookscript.desktop.functions.ImageFunctions;
import com.blackrook.rookscript.exception.ScriptExecutionException;
import com.blackrook.rookscript.exception.ScriptParseException;
import com.blackrook.rookscript.functions.MathFunctions;
import com.blackrook.rookscript.functions.RegexFunctions;
import com.blackrook.rookscript.functions.SystemFunctions;
import com.blackrook.rookscript.functions.ZipFunctions;
import com.blackrook.rookscript.functions.common.BufferFunctions;
import com.blackrook.rookscript.functions.common.ErrorFunctions;
import com.blackrook.rookscript.functions.common.ListFunctions;
import com.blackrook.rookscript.functions.common.MapFunctions;
import com.blackrook.rookscript.functions.common.MiscFunctions;
import com.blackrook.rookscript.functions.common.StringFunctions;
import com.blackrook.rookscript.functions.io.DataIOFunctions;
import com.blackrook.rookscript.functions.io.FileIOFunctions;
import com.blackrook.rookscript.functions.io.StreamingIOFunctions;
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionType.Usage;
import com.blackrook.rookscript.lang.ScriptFunctionType.Usage.ParameterUsage;
import com.blackrook.rookscript.lang.ScriptFunctionType.Usage.TypeUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;
import com.blackrook.rookscript.resolvers.ScriptVariableResolver;
import com.blackrook.rookscript.resolvers.variable.DefaultVariableResolver;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.struct.HTMLWriter;
import net.mtrop.doom.tools.wadscript.DoomMapFunctions;
import net.mtrop.doom.tools.wadscript.PK3Functions;
import net.mtrop.doom.tools.wadscript.UtilityFunctions;
import net.mtrop.doom.tools.wadscript.WadFunctions;

import com.blackrook.rookscript.functions.DateFunctions;
import com.blackrook.rookscript.functions.DigestFunctions;
import com.blackrook.rookscript.functions.FileSystemFunctions;
import com.blackrook.rookscript.functions.JSONFunctions;
import com.blackrook.rookscript.functions.PrintFunctions;

/**
 * Main class for executing scripts.
 * @author Matthew Tropiano
 */
public final class WadScriptMain
{
	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String ROOKSCRIPT_VERSION = Common.getVersionString("rookscript");
	private static final String ROOKSCRIPT_DESKTOP_VERSION = Common.getVersionString("rookscript-desktop");
	private static final String VERSION = Common.getVersionString("wadscript");

	private static final int ERROR_NONE = 0;
	private static final int ERROR_IOERROR = 1;
	private static final int ERROR_INTERNAL = 2;
	private static final int ERROR_BAD_SCRIPT = 4;
	private static final int ERROR_BAD_SCRIPT_ENTRY = 5;
	private static final int ERROR_SCRIPT_EXECUTION_ERROR = 6;
	private static final int ERROR_SCRIPT_RETURNED_ERROR = 7;
	private static final int ERROR_SCRIPT_INSTANCE_EXECUTION = 8;
	private static final int ERROR_SCRIPT_NOT_STARTED = 9;

	private static final String SWITCH_VERSION1 = "--version";
	private static final String SWITCH_HELP1 = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_FUNCHELP1 = "--function-help";
	private static final String SWITCH_FUNCHELP2 = "--function-help-markdown";
	private static final String SWITCH_FUNCHELP3 = "--function-help-html";
	private static final String SWITCH_FUNCHELP4 = "--function-help-html-div";
	private static final String SWITCH_DISASSEMBLE1 = "--disassemble";
	private static final String SWITCH_ENTRY1 = "--entry";
	private static final String SWITCH_ENTRY2 = "-e";
	private static final String SWITCH_RUNAWAYLIMIT1 = "--runaway-limit";
	private static final String SWITCH_ACTIVATIONDEPTH1 = "--activation-depth";
	private static final String SWITCH_STACKDEPTH1 = "--stack-depth";
	private static final String SWITCH_SEPARATOR = "--";
	private static final String SWITCH_SEPARATORBASH = "--X";
	
	private static final Resolver[] RESOLVERS = 
	{
		new Resolver("Common", MiscFunctions.createResolver()),
		new Resolver("Printing/Logging", PrintFunctions.createResolver()),
		new Resolver("String", StringFunctions.createResolver()),
		new Resolver("List / Set", ListFunctions.createResolver()),
		new Resolver("Map", MapFunctions.createResolver()),
		new Resolver("Buffer", BufferFunctions.createResolver()),
		new Resolver("Error", ErrorFunctions.createResolver()),
		new Resolver("Math", MathFunctions.createResolver()),
		new Resolver("RegEx", RegexFunctions.createResolver()),
		new Resolver("Date / Time", DateFunctions.createResolver()),
		new Resolver("File System", FileSystemFunctions.createResolver()),
		new Resolver("File I/O", FileIOFunctions.createResolver()),
		new Resolver("Zip Files / GZIP Streams", ZipFunctions.createResolver()),
		new Resolver("Stream I/O", StreamingIOFunctions.createResolver()),
		new Resolver("Data I/O", DataIOFunctions.createResolver()),
		new Resolver("Digest", DigestFunctions.createResolver()),
		new Resolver("JSON", JSONFunctions.createResolver()),
		new Resolver("System", SystemFunctions.createResolver()),
		new Resolver("Images", ImageFunctions.createResolver()),
		new Resolver("Desktop", "DESKTOP", DesktopFunctions.createResolver()),
		new Resolver("WADs", WadFunctions.createResolver()),
		new Resolver("PK3/PKEs", PK3Functions.createResolver()),
		new Resolver("Doom / Hexen / ZDoom / UDMF Maps", "MAP", DoomMapFunctions.createResolver()),
		new Resolver("Utilities", "UTIL", UtilityFunctions.createResolver())
	};

	private static final Scope[] SCOPES = 
	{
		new Scope("GLOBAL", new DefaultVariableResolver())
	};
	
	private static class Resolver
	{
		private String sectionName;
		private String namespace;
		private ScriptFunctionResolver resolver;
		
		private Resolver(String sectionName, ScriptFunctionResolver resolver)
		{
			this.sectionName = sectionName;
			this.namespace = null;
			this.resolver = resolver;
		}

		private Resolver(String sectionName, String namespace, ScriptFunctionResolver resolver)
		{
			this.sectionName = sectionName;
			this.namespace = namespace;
			this.resolver = resolver;
		}
	}

	private static class Scope
	{
		private String scopeName;
		private ScriptVariableResolver variableResolver;

		private Scope(String scopeName, ScriptVariableResolver variableResolver)
		{
			this.scopeName = scopeName;
			this.variableResolver = variableResolver;
		}
	}
	
	private interface UsageRendererType
	{
		/**
		 * Called on render start.
		 * @throws IOException if a write error occurs.
		 */
		void startRender() throws IOException;
		
		/**
		 * Starts the table of contents.
		 * @param sections the section name list.
		 * @throws IOException if a write error occurs.
		 */
		void startTableOfContents(String[] sections) throws IOException;

		/**
		 * Finishes the table of contents.
		 * @throws IOException if a write error occurs.
		 */
		void finishTableOfContents() throws IOException;
		
		/**
		 * Starts rendering a section break.
		 * @param title the section title.
		 * @throws IOException if a write error occurs.
		 */
		void startSection(String title) throws IOException;

		/**
		 * Starts rendering a single function usage doc.
		 * @param namespace the function namespace.
		 * @param functionName the function name.
		 * @param parameterNames the parameters on the function render (can be null).
		 * @throws IOException if a write error occurs.
		 */
		void startFunction(String namespace, String functionName, String[] parameterNames) throws IOException;

		/**
		 * Starts rendering a usage doc.
		 * @param usage the usage to render.
		 * @throws IOException if a write error occurs.
		 */
		void startUsage(Usage usage) throws IOException;

		/**
		 * Finishes rendering a usage doc.
		 * @param usage the usage to render.
		 * @throws IOException if a write error occurs.
		 */
		void finishUsage(Usage usage) throws IOException;

		/**
		 * Finishes rendering a function break.
		 * @param namespace the function namespace.
		 * @param functionName the function name.
		 * @throws IOException if a write error occurs.
		 */
		void finishFunction(String namespace, String functionName) throws IOException;

		/**
		 * Finishes rendering a section break.
		 * @param title the section title.
		 * @throws IOException if a write error occurs.
		 */
		void finishSection(String title) throws IOException;

		/**
		 * Called on render finish.
		 * @throws IOException if a write error occurs.
		 */
		void finishRender() throws IOException;
		
	}
	
	private static class UsageTextRenderer implements UsageRendererType
	{
		private static final String NEWLINE_INDENT = "\n            ";
		
		private PrintStream out;
		
		private UsageTextRenderer(PrintStream out) 
		{
			this.out = out;
		}
		
		@Override
		public void startRender()
		{
			// Do nothing
		}

		@Override
		public void startTableOfContents(String[] sections) throws IOException
		{
			printSectionHeader("Table of Contents");
			for (int i = 0; i < sections.length; i++)
				out.println("[" + getTitleCode(sections[i]) + "] " + sections[i]);
		}

		@Override
		public void finishTableOfContents() throws IOException 
		{
			out.println();
			out.println();
		}

		@Override
		public void startSection(String title) 
		{
			printSectionHeader("[" + getTitleCode(title) + "] " + title);
		}

		@Override
		public void finishSection(String title) throws IOException
		{
			out.println();
		}

		@Override
		public void startFunction(String namespace, String functionName, String[] parameterNames)
		{
			if (parameterNames == null)
			{
				if (namespace != null)
					out.append(namespace + "::");
				out.append(functionName).append("(...)").println();
				out.println();
				return;
			}
			
			if (namespace != null)
				out.append(namespace + "::");
			
			out.append(functionName).append('(');
			for (int i = 0; i < parameterNames.length; i++)
			{
				out.append(parameterNames[i]);
				if (i < parameterNames.length - 1)
					out.append(", ");
			}
			out.append(')').print('\n');
		}

		@Override
		public void startUsage(Usage usage) throws IOException 
		{
			out.append("    ").println(usage.getInstructions().replace("\n", NEWLINE_INDENT));
			if (!usage.getParameterInstructions().isEmpty()) for (ParameterUsage pu : usage.getParameterInstructions())
			{
				out.append("    ").append(pu.getParameterName()).println(":");
				for (TypeUsage tu : pu.getTypes())
					renderTypeUsage(tu);
			}
			
			out.append("    ").println("Returns:");
			for (TypeUsage tu : usage.getReturnTypes())
				renderTypeUsage(tu);
			out.println();
		}

		@Override
		public void finishUsage(Usage usage) throws IOException 
		{
			// Do nothing.
		}

		@Override
		public void finishFunction(String namespace, String functionName) throws IOException 
		{
			// Do nothing.
		}

		@Override
		public void finishRender() 
		{
			// Do nothing.
		}

		private void printSectionHeader(String title)
		{
			out.println("=================================================================");
			out.println("==== " + title);
			out.println("=================================================================");
			out.println();
		}

		private String getTitleCode(String title)
		{
			String code = title.replaceAll("([^a-zA-Z])+", "");
			return code.substring(0, Math.min(code.length(), 4)).toUpperCase();
		}

		private void renderTypeUsage(TypeUsage tu)
		{
			out.append("        (").append(tu.getType() != null 
					? (tu.getType().name() + (tu.getSubType() != null ? ":" + tu.getSubType() : "")) 
					: "ANY"
				).append(") ").println(tu.getDescription().replace("\n", NEWLINE_INDENT));
		}

	}
	
	private static class UsageMarkdownRenderer implements UsageRendererType
	{
		final String NEWLINE_INDENT = "\n        - ";

		private PrintStream out;
		
		private UsageMarkdownRenderer(PrintStream out)
		{
			this.out = out;
		}
		
		@Override
		public void startRender()
		{
			// Do nothing.
		}

		@Override
		public void startTableOfContents(String[] sections) throws IOException
		{
			out.println("# Table of Contents");
			out.println();

			for (int i = 0; i < sections.length; i++)
			{
				out.print(i + 1);
				out.append(". [").append(sections[i]).append("](#").append(getTitleCode(sections[i])).append(")").println();				
			}
		}

		@Override
		public void finishTableOfContents() throws IOException 
		{
			out.println();
		}

		@Override
		public void startSection(String title)
		{
			out.append("# ").append(title).append("<span id=\"").append(getTitleCode(title)).println("\"></span>");
			out.println();
		}
		
		private void renderTypeUsage(TypeUsage tu)
		{
			out.append("- `").append(tu.getType() != null ? tu.getType().name() : "ANY").append("` ");
			out.append((tu.getSubType() != null ? "*" + tu.getSubType() + "*" : ""));
			out.println();
			out.append("    - ").println(tu.getDescription().replace("\n", NEWLINE_INDENT));
		}
		
		@Override
		public void startFunction(String namespace, String functionName, String[] parameterNames)
		{
			if (parameterNames == null)
			{
				out.append("## ");
				if (namespace != null)
					out.append(namespace + "::");
				out.append(functionName).append("(...)").println();
				out.println();
				return;
			}
			
			out.append("## ");
			if (namespace != null)
				out.append(namespace + "::");
			out.append(functionName).append('(');
			for (int i = 0; i < parameterNames.length; i++)
			{
				out.append(parameterNames[i]);
				if (i < parameterNames.length - 1)
					out.append(", ");
			}
			out.append(')');
			out.println();
		}

		@Override
		public void startUsage(Usage usage) throws IOException 
		{
			out.println(usage.getInstructions().replace("\n", NEWLINE_INDENT));
			out.println();
			if (!usage.getParameterInstructions().isEmpty())
			{
				for (ParameterUsage pu : usage.getParameterInstructions())
				{
					out.append("**").append(pu.getParameterName()).append("**").println(":");
					out.println();
					for (TypeUsage tu : pu.getTypes())
						renderTypeUsage(tu);
					out.println();
				}
				out.println();
			}

			out.append("**Returns**").println(":");
			out.println();
			for (TypeUsage tu : usage.getReturnTypes())
				renderTypeUsage(tu);
			out.println();
		}

		@Override
		public void finishUsage(Usage usage) throws IOException 
		{
			out.println();
		}

		@Override
		public void finishFunction(String namespace, String functionName) throws IOException 
		{
			out.println();
		}

		@Override
		public void finishSection(String title) throws IOException 
		{
			out.println();
		}

		@Override
		public void finishRender()
		{
			// Do nothing.
		}

		private String getTitleCode(String title)
		{
			return title.replaceAll("([^a-zA-Z])+", "-").toLowerCase();
		}

	}
	
	private static class UsageMarkdownHTML implements UsageRendererType
	{
		private HTMLWriter htmlout;
		private boolean contentOnly;
		private String mainTitle;
		
		private UsageMarkdownHTML(PrintStream out, String mainTitle, boolean contentOnly)
		{
			this.mainTitle = mainTitle;
			this.contentOnly = contentOnly;
			this.htmlout = new HTMLWriter(new PrintWriter(out), 
				HTMLWriter.Options.PRETTY, 
				HTMLWriter.Options.SLASHES_IN_SINGLE_TAGS
			);
		}
		
		@Override
		public void startRender() throws IOException
		{
			if (!contentOnly)
			{
				htmlout.start();
				htmlout.push("head");
				htmlout.tag("title", mainTitle);
				try (Reader reader = Common.openResourceReader("wadscript/doc-style.css"))
				{
					htmlout.push("style", HTMLWriter.attribute("type", "text/css"))
						.html(reader)
					.pop();
				}
				htmlout.pop();
				htmlout.push("body");
			}
			htmlout.push("div", HTMLWriter.classes("script-docs"));
			htmlout.push("div", HTMLWriter.classes("main-title"))
				.tag("h1", mainTitle, HTMLWriter.classes("main-title-name"))
			.pop();
		}

		@Override
		public void startTableOfContents(String[] sections) throws IOException 
		{
			htmlout.push("div", HTMLWriter.id("top"), HTMLWriter.classes("table-of-contents"));
			htmlout.push("div", HTMLWriter.classes("toc-title"))
				.push("h1", HTMLWriter.classes("toc-name"))
					.text("Table of Contents")
				.pop()
			.pop();
			
			htmlout.push("div", HTMLWriter.classes("toc-content"));
			for (int i = 0; i < sections.length; i++)
			{
				htmlout.tag("a", sections[i], HTMLWriter.href("#category-"+getTitleCode(sections[i])), HTMLWriter.classes("toc-link"));
				if (i < sections.length - 1)
					htmlout.text(" | ");
			}
			htmlout.pop();
		}

		@Override
		public void finishTableOfContents() throws IOException 
		{
			htmlout.pop();
		}

		@Override
		public void startSection(String title) throws IOException
		{
			String titleCode = getTitleCode(title);
			htmlout.push("div", HTMLWriter.id("category-" + titleCode), HTMLWriter.classes("category-section"));
			
			htmlout.push("div", HTMLWriter.classes("category-title"), HTMLWriter.attribute("section-code", titleCode))
				.push("h1", HTMLWriter.classes("category-name"))
					.text(title)
					.tag("a", "TOP", HTMLWriter.href("#top"), HTMLWriter.classes("category-navigation"))
				.pop()
			.pop();
			
			htmlout.push("div", HTMLWriter.id("category-content-" + titleCode), HTMLWriter.classes("category-content"));
		}

		@Override
		public void startFunction(String namespace, String functionName, String[] parameterNames) throws IOException 
		{
			String functionNameCode = getFunctionNameCode(namespace, functionName);
			htmlout.push("div", HTMLWriter.id("function-" + functionNameCode), HTMLWriter.classes("function-section"));
			
			htmlout.push("div", HTMLWriter.classes("function-title"), HTMLWriter.attribute("section-code", functionNameCode))
				.push("h2", HTMLWriter.classes("function-name"))
					.text((namespace != null ? namespace.toUpperCase() + "::" : "") + functionName.toUpperCase())
					.push("span", HTMLWriter.classes("function-parameters"));

					StringBuilder sb = new StringBuilder("(");
					for (int i = 0; i < parameterNames.length; i++)
					{
						sb.append(parameterNames[i]);
						if (i < parameterNames.length - 1)
							sb.append(", ");
					}
					sb.append(")");

					htmlout.text(sb.toString()).pop();
					htmlout.tag("a", "TOP", HTMLWriter.href("#top"), HTMLWriter.classes("function-navigation"))
				.pop()
			.pop();
			
			htmlout.push("div", HTMLWriter.id("function-content-" + functionNameCode), HTMLWriter.classes("function-content"));
		}

		@Override
		public void startUsage(Usage usage) throws IOException
		{
			htmlout.push("div", HTMLWriter.classes("function-description"))
				.tag("p", usage.getInstructions())
			.pop();
			
			for (ParameterUsage paramUsage : usage.getParameterInstructions()) 
				writeParameterUsage(paramUsage);
			writeParameterReturn(usage.getReturnTypes());
		}

		private void writeParameterUsage(ParameterUsage paramUsage) throws IOException
		{
			htmlout.push("div", HTMLWriter.classes("parameter-section"));
			htmlout.push("div", HTMLWriter.classes("parameter-title"))
				.push("h4", HTMLWriter.classes("parameter-name"))
					.text(paramUsage.getParameterName())
				.pop()
			.pop();
			
			writeParameterTypeUsage(paramUsage.getTypes());
			htmlout.pop();
		}

		private void writeParameterReturn(List<TypeUsage> parameterUsageTypes) throws IOException
		{
			htmlout.push("div", HTMLWriter.classes("parameter-section"));
			htmlout.push("div", HTMLWriter.classes("parameter-title", "return"))
				.push("h4", HTMLWriter.classes("parameter-name"))
					.text("RETURNS")
				.pop()
			.pop();
			
			writeParameterTypeUsage(parameterUsageTypes);
			htmlout.pop();
		}
		
		private void writeParameterTypeUsage(List<TypeUsage> parameterUsageTypes) throws IOException
		{
			htmlout.push("div", HTMLWriter.classes("parameter-content"));
			for (TypeUsage typeUsage : parameterUsageTypes)
			{
				htmlout.push("div", HTMLWriter.classes("parameter-usage"))
					.push("div", HTMLWriter.classes("parameter-type"));
				
					String typeName = typeUsage.getType() != null ? typeUsage.getType().name() : "ANY";
					String subTypeName = typeUsage.getSubType();
					String description = typeUsage.getDescription();
					
					htmlout.tag("span", typeName, HTMLWriter.classes("parameter-type-name"));
					if (subTypeName != null)
						htmlout.tag("span", subTypeName, HTMLWriter.classes("parameter-subtype-name"));

					htmlout.pop();
				
				if (description != null)
				{
					htmlout.push("div", HTMLWriter.classes("parameter-description"))
						.push("p")
							.html(description.replace("\n", "<br/>"))
						.pop()
					.pop();
				}
				htmlout.pop();
			}
			htmlout.pop();
		}
		
		@Override
		public void finishUsage(Usage usage) throws IOException 
		{
			// Do nothing.
		}

		@Override
		public void finishFunction(String namespace, String functionName) throws IOException 
		{
			htmlout.pop().pop();
		}

		@Override
		public void finishSection(String title) throws IOException
		{
			htmlout.pop().pop();
		}

		@Override
		public void finishRender() throws IOException
		{
			Common.close(htmlout);
		}

		private String getTitleCode(String title)
		{
			return title.replaceAll("([^a-zA-Z])+", "-").toLowerCase();
		}

		private String getFunctionNameCode(String namespace, String functionName)
		{
			return namespace != null ? getTitleCode(namespace + "-" + functionName) : getTitleCode(functionName);
		}

	}
	
	public enum Mode
	{
		VERSION,
		HELP,
		FUNCTIONHELP,
		FUNCTIONHELP_MARKDOWN,
		FUNCTIONHELP_HTML,
		FUNCTIONHELP_HTML_DIV,
		DISASSEMBLE,
		EXECUTE;
	}
	
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		private InputStream stdin;
		private Mode mode;
		private String docsTitle;
		private File scriptFile;
		private String entryPointName;
		private Integer runawayLimit;
		private Integer activationDepth;
		private Integer stackDepth;
		private List<Object> parameterList;
		private List<Object> argList;
		private List<Resolver> resolvers;
		private List<Scope> scopes;
		
		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.stdin = null;
			this.mode = Mode.EXECUTE;
			this.docsTitle = "WadScript Functions";
			this.scriptFile = null;
			this.entryPointName = "main";
			this.runawayLimit = 0;
			this.activationDepth = 256;
			this.stackDepth = 2048;
			this.parameterList = new LinkedList<>();
			this.argList = new LinkedList<>();
			this.resolvers = new LinkedList<>();
			this.scopes = new LinkedList<>();
		}

		public Options setStdout(OutputStream out) 
		{
			this.stdout = new PrintStream(out, true);;
			return this;
		}
		
		public Options setStderr(OutputStream err) 
		{
			this.stderr = new PrintStream(err, true);
			return this;
		}

		public Options setStdin(InputStream stdin) 
		{
			this.stdin = stdin;
			return this;
		}
		
		public Options setScriptFile(File scriptFile) 
		{
			this.scriptFile = scriptFile;
			return this;
		}
		
		public Options setMode(Mode mode) 
		{
			this.mode = mode;
			return this;
		}
		
		public Options setDocsTitle(String docsTitle) 
		{
			this.docsTitle = docsTitle;
			return this;
		}
		
		public Options setEntryPointName(String entryPointName) 
		{
			this.entryPointName = entryPointName;
			return this;
		}
		
		public Options setRunawayLimit(Integer runawayLimit)
		{
			this.runawayLimit = runawayLimit;
			return this;
		}
		
		public Options setActivationDepth(Integer activationDepth)
		{
			this.activationDepth = activationDepth;
			return this;
		}
		
		public Options setStackDepth(Integer stackDepth)
		{
			this.stackDepth = stackDepth;
			return this;
		}
		
		public Options addEntryParameterArg(Object arg)
		{
			this.parameterList.add(arg);
			return this;
		}
		
		public Options addArg(Object arg)
		{
			this.argList.add(arg);
			return this;
		}
		
		public Options addResolver(String sectionName, ScriptFunctionResolver resolver)
		{
			this.resolvers.add(new Resolver(sectionName, resolver));
			return this;
		}
		
		public Options addResolver(String sectionName, String namespace, ScriptFunctionResolver resolver)
		{
			this.resolvers.add(new Resolver(sectionName, namespace, resolver));
			return this;
		}
		
		public Options addScope(String scopeName, ScriptVariableResolver variableResolver)
		{
			this.scopes.add(new Scope(scopeName, variableResolver));
			return this;
		}
		
	}

	private static class Context
	{
		private Options options;
		
		private Context(Options options)
		{
			this.options = options;
		}
		
		private int call()
		{
			if (options.mode == null)
			{
				splash(options.stdout);
				return ERROR_NONE;
			}

			if (options.mode == Mode.VERSION)
			{
				splash(options.stdout);
				return ERROR_NONE;
			}

			if (options.mode == Mode.HELP)
			{
				usage(options.stdout);
				printHelp(options.stdout);
				return ERROR_NONE;
			}
			
			if (options.mode == Mode.FUNCTIONHELP)
			{
				try {
					printFunctionHelp(new UsageTextRenderer(options.stdout), options.resolvers);
					return ERROR_NONE;
				} catch (IOException e) {
					options.stderr.println("ERROR: " + e.getLocalizedMessage());
					return ERROR_IOERROR;
				} catch (Exception e) {
					options.stderr.println("Internal ERROR: " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
					return ERROR_INTERNAL;
				}
			}
			
			if (options.mode == Mode.FUNCTIONHELP_MARKDOWN)
			{
				try {
					printFunctionHelp(new UsageMarkdownRenderer(options.stdout), options.resolvers);
					return ERROR_NONE;
				} catch (IOException e) {
					options.stderr.println("ERROR: " + e.getLocalizedMessage());
					return ERROR_IOERROR;
				} catch (Exception e) {
					options.stderr.println("Internal ERROR: " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
					return ERROR_INTERNAL;
				}
			}
			
			if (options.mode == Mode.FUNCTIONHELP_HTML)
			{
				try {
					printFunctionHelp(new UsageMarkdownHTML(options.stdout, options.docsTitle, false), options.resolvers);
					return ERROR_NONE;
				} catch (IOException e) {
					options.stderr.println("ERROR: " + e.getLocalizedMessage());
					return ERROR_IOERROR;
				} catch (Exception e) {
					options.stderr.println("Internal ERROR: " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
					return ERROR_INTERNAL;
				}
			}
			
			if (options.mode == Mode.FUNCTIONHELP_HTML_DIV)
			{
				try {
					printFunctionHelp(new UsageMarkdownHTML(options.stdout, options.docsTitle, true), options.resolvers);
					return ERROR_NONE;
				} catch (IOException e) {
					options.stderr.println("ERROR: " + e.getLocalizedMessage());
					return ERROR_IOERROR;
				} catch (Exception e) {
					options.stderr.println("Internal ERROR: " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
					return ERROR_INTERNAL;
				}
			}
			
			if (options.scriptFile == null)
			{
				options.stderr.println("ERROR: Bad script file.");
				return ERROR_BAD_SCRIPT;
			}
			if (!options.scriptFile.exists())
			{
				options.stderr.println("ERROR: Script file does not exist: " + options.scriptFile);
				return ERROR_BAD_SCRIPT;
			}
			if (options.scriptFile.isDirectory())
			{
				options.stderr.println("ERROR: Bad script file. Is directory.");
				return ERROR_BAD_SCRIPT;
			}
		
			ScriptInstance instance;
			
			try 
			{
				ScriptInstanceBuilder builder = ScriptInstance.createBuilder()
					.withSource(options.scriptFile)
					.withEnvironment(ScriptEnvironment.create(options.stdout, options.stderr, options.stdin))
					.withScriptStack(options.activationDepth, options.stackDepth)
					.withRunawayLimit(options.runawayLimit);

				// ============ Add Functions =============
				
				for (int i = 0; i < RESOLVERS.length; i++)
				{
					if (i == 0)
					{
						if (RESOLVERS[i].namespace != null)
							builder.withFunctionResolver(RESOLVERS[i].namespace, RESOLVERS[i].resolver);
						else
							builder.withFunctionResolver(RESOLVERS[i].resolver);
					}
					else 
					{
						if (RESOLVERS[i].namespace != null)
							builder.andFunctionResolver(RESOLVERS[i].namespace, RESOLVERS[i].resolver);
						else
							builder.andFunctionResolver(RESOLVERS[i].resolver);
					} 
				}
				
				for (Resolver resolver : options.resolvers)
				{
					if (resolver.namespace != null)
						builder.andFunctionResolver(resolver.namespace, resolver.resolver);
					else
						builder.andFunctionResolver(resolver.resolver);
				}
				
				// ============== Add Scopes ==============

				for (int i = 0; i < SCOPES.length; i++)
				{
					if (i == 0)
					{
						builder.withScope(SCOPES[i].scopeName, SCOPES[i].variableResolver);
					}
					else 
					{
						builder.andScope(SCOPES[i].scopeName, SCOPES[i].variableResolver);
					} 
				}
				
				for (Scope scope : options.scopes)
				{
					builder.andScope(scope.scopeName, scope.variableResolver);
				}
				
				instance = builder.createInstance();
				
			} 
			catch (ScriptInstanceBuilder.BuilderException e) 
			{
				Throwable cause = e.getCause();
				if (cause instanceof ScriptParseException)
				{
					options.stderr.println("Script ERROR: " + cause.getLocalizedMessage());
					return ERROR_SCRIPT_INSTANCE_EXECUTION;
				}
				else if (cause != null)
				{
					options.stderr.println("ERROR: Script could not be started: " + cause.getLocalizedMessage());
					return ERROR_SCRIPT_INSTANCE_EXECUTION;
				}
				else
				{
					options.stderr.println("ERROR: " + e.getLocalizedMessage());
					return ERROR_SCRIPT_NOT_STARTED;
				}
			}
			
			if (options.mode == Mode.DISASSEMBLE)
			{
				options.stdout.println("Disassembly of \"" + options.scriptFile + "\":");
				doDisassemble(options.stdout, instance);
				return ERROR_NONE;
			}
		
			if (options.mode == Mode.EXECUTE)
			{
				if (options.entryPointName == null)
				{
					options.stderr.println("ERROR: Bad entry point.");
					return ERROR_BAD_SCRIPT;
				}
				if (options.activationDepth == null)
				{
					options.stderr.println("ERROR: Bad activation depth.");
					return ERROR_BAD_SCRIPT;
				}
				if (options.stackDepth == null)
				{
					options.stderr.println("ERROR: Bad stack depth.");
					return ERROR_BAD_SCRIPT;
				}
				if (options.runawayLimit == null)
				{
					options.stderr.println("ERROR: Bad runaway limit.");
					return ERROR_BAD_SCRIPT;
				}
				
				Script.Entry entryPoint;
				
				if ((entryPoint = instance.getScript().getScriptEntry(options.entryPointName)) == null)
				{
					options.stderr.println("ERROR: Entry point not found: " + options.entryPointName);
					return ERROR_BAD_SCRIPT_ENTRY;
				}

				int i = 0;
				Object[] entryParams = new Object[options.parameterList.size() + 1];
				for (Object obj : options.parameterList)
					entryParams[i++] = obj;
				entryParams[i] = options.argList;
				
				try {
					ScriptValue retval = ScriptValue.create(null);
					
					if (entryPoint.getParameterCount() > 0)
						instance.call(options.entryPointName, entryParams);
					else
						instance.call(options.entryPointName);

					instance.popStackValue(retval);
					
					if (retval.isError())
					{
						ErrorType error = retval.asObjectType(ErrorType.class);
						options.stderr.println("ERROR: [" + error.getType() + "]: " + error.getLocalizedMessage());
						return ERROR_SCRIPT_RETURNED_ERROR;
					}
					return retval.asInt();
				} catch (ScriptExecutionException e) {
					options.stderr.println("Script ERROR: " + e.getLocalizedMessage());
					e.printStackTrace(options.stderr);
					return ERROR_SCRIPT_EXECUTION_ERROR;
				} catch (ClassCastException e) {
					options.stderr.println("Script return ERROR: " + e.getLocalizedMessage());
					return ERROR_SCRIPT_EXECUTION_ERROR;
				}
			}
			
			options.stderr.println("ERROR: Bad mode - INTERNAL ERROR.");
			return -1;
		}

		private void doDisassemble(PrintStream out, ScriptInstance instance)
		{
			StringWriter sw = new StringWriter();
			try {
				ScriptAssembler.disassemble(instance.getScript(), new PrintWriter(sw));
			} catch (IOException e) {
				// Do nothing.
			}
			out.print(sw);
		}

		private void printHelp(PrintStream out)
		{
			out.println("[filename]:");
			out.println("    The script filename.");
			out.println();
			out.println("[switches]:");
			out.println("    --help, -h                   Prints this help.");
			out.println("    --version                    Prints the version of this utility.");
			out.println("    --function-help              Prints all available function usages.");
			out.println("    --function-help-markdown     Prints all available function usages in");
			out.println("                                     Markdown format.");
			out.println("    --function-help-html         Prints all available function usages in");
			out.println("                                     HTML format.");
			out.println("    --function-help-html-div     Prints all available function usages in");
			out.println("                                     HTML format, but just the content.");
			out.println("    --disassemble                Prints the disassembly for this script");
			out.println("                                     and exits.");
			out.println("    --entry [name]               Use a different entry point named [name].");
			out.println("                                     Default: \"main\"");
			out.println("    --runaway-limit [num]        Sets the runaway limit (in operations)");
			out.println("                                     before the soft protection on infinite");
			out.println("                                     loops triggers. 0 is no limit.");
			out.println("                                     Default: 0");
			out.println("    --activation-depth [num]     Sets the activation depth to [num].");
			out.println("                                     Default: 256");
			out.println("    --stack-depth [num]          Sets the stack value depth to [num].");
			out.println("                                     Default: 2048");
			out.println("    --                           All tokens after this one are interpreted");
			out.println("                                     literally as args for the script.");
			out.println("                                     Normally, all unrecognized switches");
			out.println("                                     become arguments to the script. This");
			out.println("                                     forces the alternate interpretation.");
			out.println("    --X                          Bash script special: [DEPRECATED]");
			out.println("                                     First argument after this is the script");
			out.println("                                     file, and every argument after are args");
			out.println("                                     to pass to the script.");
			out.println();
			out.println("Scopes");
			out.println("------");
			out.println("All scripts can access a scope called `global` that serves as a common variable");
			out.println("scope for sharing values outside of functions or for data that you would want");
			out.println("to initialize once.");
		}

		private void printFunctionUsages(UsageRendererType renderer, String sectionName, String namespace, ScriptFunctionResolver resolver) throws IOException
		{
			renderer.startSection(sectionName);
			for (ScriptFunctionType sft : resolver.getFunctions())
			{
				List<String> parameters = null;
				Usage usage = sft.getUsage();
				if (usage != null)
				{
					parameters = new LinkedList<>();
					for (ParameterUsage pu : usage.getParameterInstructions())
						parameters.add(pu.getParameterName());
				}
				
				renderer.startFunction(namespace, sft.name(), parameters != null ? parameters.toArray(new String[parameters.size()]) : null);
				if (usage != null)
				{
					renderer.startUsage(usage);
					renderer.finishUsage(usage);
				}
				renderer.finishFunction(namespace, sft.name());
			}
			renderer.finishSection(sectionName);
		}

		private void printFunctionHelp(UsageRendererType renderer, List<Resolver> additionalResolvers) throws IOException
		{
			renderer.startRender();

			List<String> resolverName = new LinkedList<>();
			List<Resolver> resolverList = new LinkedList<>();
			for (int i = 0; i < RESOLVERS.length; i++)
			{
				resolverName.add(RESOLVERS[i].sectionName);
				resolverList.add(RESOLVERS[i]);
			}
			for (Resolver r : additionalResolvers)
			{
				resolverName.add(r.sectionName);
				resolverList.add(r);
			}

			renderer.startTableOfContents(resolverName.toArray(new String[resolverName.size()]));
			renderer.finishTableOfContents();
			for (Resolver r : resolverList)
				printFunctionUsages(renderer, r.sectionName, r.namespace, r.resolver);
			
			renderer.finishRender();
		}
		
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param out the standard output print stream.
	 * @param err the standard error print stream. 
	 * @param in the standard in input stream.
	 * @param args the argument args.
	 * @return the parsed options.
	 * @throws OptionParseException if a parse exception occurs.
	 */
	public static Options options(PrintStream out, PrintStream err, InputStream in, String ... args) throws OptionParseException
	{
		Options options = new Options();
		options.stdout = out;
		options.stderr = err;
		options.stdin = in;
		
		final int STATE_START = 0;
		final int STATE_ARGS = 1;
		final int STATE_BASH_FILE = 2;
		final int SWITCHES = 10;
		final int STATE_SWITCHES_ENTRY = SWITCHES + 0;
		final int STATE_SWITCHES_ACTIVATION = SWITCHES + 1;
		final int STATE_SWITCHES_STACK = SWITCHES + 2;
		final int STATE_SWITCHES_RUNAWAY = SWITCHES + 3;
		int state = STATE_START;
		
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			switch (state)
			{
				case STATE_START:
				{
					if (SWITCH_HELP1.equalsIgnoreCase(arg) || SWITCH_HELP2.equalsIgnoreCase(arg))
					{
						options.mode = Mode.HELP;
						return options;
					}
					else if (SWITCH_VERSION1.equalsIgnoreCase(arg))
						options.mode = Mode.VERSION;
					else if (SWITCH_DISASSEMBLE1.equalsIgnoreCase(arg))
						options.mode = Mode.DISASSEMBLE;
					else if (SWITCH_FUNCHELP1.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP;
					else if (SWITCH_FUNCHELP2.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP_MARKDOWN;
					else if (SWITCH_FUNCHELP3.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP_HTML;
					else if (SWITCH_FUNCHELP4.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP_HTML_DIV;
					else if (SWITCH_ENTRY1.equalsIgnoreCase(arg) || SWITCH_ENTRY2.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_ENTRY;
					else if (SWITCH_RUNAWAYLIMIT1.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_RUNAWAY;
					else if (SWITCH_ACTIVATIONDEPTH1.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_ACTIVATION;
					else if (SWITCH_STACKDEPTH1.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_STACK;
					else if (SWITCH_SEPARATOR.equalsIgnoreCase(arg))
						state = STATE_ARGS;
					else if (SWITCH_SEPARATORBASH.equalsIgnoreCase(arg))
						state = STATE_BASH_FILE;
					else if (options.scriptFile == null)
						options.scriptFile = new File(arg);
					else
						options.argList.add(arg);
				}
				break;
				
				case STATE_SWITCHES_ENTRY:
				{
					arg = arg.trim();
					options.entryPointName = arg.length() > 0 ? arg : null;
				}
				break;
				
				case STATE_SWITCHES_ACTIVATION:
				{
					int n;
					try {
						n = Integer.parseInt(arg);
						options.activationDepth = n > 0 ? n : null;
					} catch (NumberFormatException e) {
						options.activationDepth = null;
						throw new OptionParseException("Activation depth needs to be a number greater than 0.");
					}
					state = STATE_START;
				}
				break;
				
				case STATE_SWITCHES_STACK:
				{
					int n;
					try {
						n = Integer.parseInt(arg);
						options.stackDepth = n > 0 ? n : null;
					} catch (NumberFormatException e) {
						options.stackDepth = null;
						throw new OptionParseException("Stack depth needs to be a number greater than 0.");
					}
					state = STATE_START;
				}
				break;
				
				case STATE_SWITCHES_RUNAWAY:
				{
					int n;
					try {
						n = Integer.parseInt(arg);
						options.runawayLimit = n > 0 ? n : null;
					} catch (NumberFormatException e) {
						options.runawayLimit = null;
						throw new OptionParseException("Runaway limit needs to be a number greater than 0.");
					}
					state = STATE_START;
				}
				break;
				
				case STATE_ARGS:
				{
					options.argList.add(arg);
				}
				break;

				case STATE_BASH_FILE:
				{
					options.scriptFile = new File(arg);
					state = STATE_ARGS;
				}
				break;			
			}
		}
		
		if (state == STATE_SWITCHES_ENTRY)
			throw new OptionParseException("ERROR: Expected entry point name after switches.");
		if (state == STATE_SWITCHES_ACTIVATION)
			throw new OptionParseException("ERROR: Expected number after activation depth switch.");
		if (state == STATE_SWITCHES_STACK)
			throw new OptionParseException("ERROR: Expected number after stack depth switch.");
		if (state == STATE_SWITCHES_RUNAWAY)
			throw new OptionParseException("ERROR: Expected number after runaway limit switch.");
		
		return options;
	}

	/**
	 * Calls the utility using a set of options.
	 * @param options the options to call with.
	 * @return the error code.
	 */
	public static int call(Options options)
	{
		return (new Context(options)).call();
	}
	
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			splash(System.out);
			usage(System.out);
			System.exit(-1);
			return;
		}

		try {
			System.exit(call(options(System.out, System.err, System.in, args)));
		} catch (OptionParseException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * Prints the splash.
	 * @param out the print stream to print to.
	 */
	private static void splash(PrintStream out)
	{
		out.println("WadScript v" + VERSION + " by Matt Tropiano");
		out.println("(using DoomStruct v" + DOOM_VERSION + ", RookScript v" + ROOKSCRIPT_VERSION + ", RookScript-Desktop v" + ROOKSCRIPT_DESKTOP_VERSION + ")");
	}

	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: wadscript [filename] [switches | scriptargs]");
		out.println("                 [--help | -h | --version]");
		out.println("                 [--function-help]");
		out.println("                 [--disassemble] [filename]");
	}
	
}

