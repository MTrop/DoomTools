package net.mtrop.doom.tools.gui.managers;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

import net.mtrop.doom.tools.gui.managers.tokenizers.RookScriptTokenMaker;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;


/**
 * Utility singleton for generating {@link RSyntaxTextArea} instances.
 * @author Matthew Tropiano
 */
public final class DoomToolsEditorProvider 
{
	public static final String SYNTAX_STYLE_DECOHACK = "text/decohack";
	public static final String SYNTAX_STYLE_ROOKSCRIPT = "text/rookscript";
	public static final String SYNTAX_STYLE_WADMERGE = "text/wadmerge";
	public static final String SYNTAX_STYLE_WADSCRIPT = "text/wadscript";
	public static final String SYNTAX_STYLE_DOOMMAKE = "text/doommake";
	
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsEditorProvider> INSTANCE = new SingletonProvider<>(() -> new DoomToolsEditorProvider());
    
    private static final Map<String, String> EXTENSION_TO_STYLE_MAP = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) 
    {
		private static final long serialVersionUID = -1713345708437194651L;
		{
			put("asc",        SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT);
			put("as2",        SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT);
			put("as3",        SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT);
			put("asm",        SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
			put("bbc",        SyntaxConstants.SYNTAX_STYLE_BBCODE);
			put("c",          SyntaxConstants.SYNTAX_STYLE_C);
			put("cljs",       SyntaxConstants.SYNTAX_STYLE_CLOJURE);
			put("cpp",        SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
			put("cs",         SyntaxConstants.SYNTAX_STYLE_CSHARP);
			put("css",        SyntaxConstants.SYNTAX_STYLE_CSS);
			put("csv",        SyntaxConstants.SYNTAX_STYLE_CSV);
			put("d",          SyntaxConstants.SYNTAX_STYLE_D);
			put("dockerfile", SyntaxConstants.SYNTAX_STYLE_DOCKERFILE);
			put("dart",       SyntaxConstants.SYNTAX_STYLE_DART);
			put("dtd",        SyntaxConstants.SYNTAX_STYLE_DTD);
			put("fortran",    SyntaxConstants.SYNTAX_STYLE_FORTRAN);
			put("go",         SyntaxConstants.SYNTAX_STYLE_GO);
			put("groovy",     SyntaxConstants.SYNTAX_STYLE_GROOVY);
			put("hosts",      SyntaxConstants.SYNTAX_STYLE_HOSTS);
			put("htaccess",   SyntaxConstants.SYNTAX_STYLE_HTACCESS);
			put("htm",        SyntaxConstants.SYNTAX_STYLE_HTML);
			put("html",       SyntaxConstants.SYNTAX_STYLE_HTML);
			put("ini",        SyntaxConstants.SYNTAX_STYLE_INI);
			put("java",       SyntaxConstants.SYNTAX_STYLE_JAVA);
			put("js",         SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
			put("json",       SyntaxConstants.SYNTAX_STYLE_JSON);
			put("jsp",        SyntaxConstants.SYNTAX_STYLE_JSP);
			put("kt",         SyntaxConstants.SYNTAX_STYLE_KOTLIN);
			put("tex",        SyntaxConstants.SYNTAX_STYLE_LATEX);
			put("lss",        SyntaxConstants.SYNTAX_STYLE_LESS);
			put("lsp",        SyntaxConstants.SYNTAX_STYLE_LISP);
			put("lua",        SyntaxConstants.SYNTAX_STYLE_LUA);
			put("makefile",   SyntaxConstants.SYNTAX_STYLE_MAKEFILE);
			put("md",         SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
			put("mxml",       SyntaxConstants.SYNTAX_STYLE_MXML);
			put("nsis",       SyntaxConstants.SYNTAX_STYLE_NSIS);
			put("pl",         SyntaxConstants.SYNTAX_STYLE_PERL);
			put("php",        SyntaxConstants.SYNTAX_STYLE_PHP);
			put("properties", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
			put("py",         SyntaxConstants.SYNTAX_STYLE_PYTHON);
			put("rb",         SyntaxConstants.SYNTAX_STYLE_RUBY);
			put("sas",        SyntaxConstants.SYNTAX_STYLE_SAS);
			put("sca",        SyntaxConstants.SYNTAX_STYLE_SCALA);
			put("sql",        SyntaxConstants.SYNTAX_STYLE_SQL);
			put("tcl",        SyntaxConstants.SYNTAX_STYLE_TCL);
			put("ts",         SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT);
			put("sh",         SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
			put("vb",         SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC);
			put("vbs",        SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC);
			put("bat",        SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
			put("cmd",        SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
			put("xml",        SyntaxConstants.SYNTAX_STYLE_XML);
			put("yml",        SyntaxConstants.SYNTAX_STYLE_YAML);
			put("yaml",       SyntaxConstants.SYNTAX_STYLE_YAML);
			
			// Custom
			put("dh",         SYNTAX_STYLE_DECOHACK);
			put("script",     SYNTAX_STYLE_DOOMMAKE);
			put("rscript",    SYNTAX_STYLE_ROOKSCRIPT);
			put("wadmerge",   SYNTAX_STYLE_WADMERGE);
			put("wadm",       SYNTAX_STYLE_WADMERGE);
			put("wadm",       SYNTAX_STYLE_WADMERGE);
			put("wscript",    SYNTAX_STYLE_WADSCRIPT);
			put("wsx",        SYNTAX_STYLE_WADSCRIPT);
		}
    };

    private static final Map<String, String> NAME_TO_STYLE_MAP = Collections.unmodifiableMap(new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) 
    {
		private static final long serialVersionUID = -2069191041359249343L;
		{
			put("Plain Text",         SyntaxConstants.SYNTAX_STYLE_NONE);
			put("DECOHack",           SYNTAX_STYLE_DECOHACK);
			put("DoomMake",           SYNTAX_STYLE_DOOMMAKE);
			put("RookScript",         SYNTAX_STYLE_ROOKSCRIPT);
			put("WadMerge",           SYNTAX_STYLE_WADMERGE);
			put("WadScript",          SYNTAX_STYLE_WADSCRIPT);
		}
    });
    
    private static final Map<String, String> OTHER_NAME_TO_STYLE_MAP = Collections.unmodifiableMap(new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) 
    {
		private static final long serialVersionUID = -2069191041359249342L;
		{
			put("ActionScript",       SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT);
			put("Assembler (x86)",    SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
			put("BBCode",             SyntaxConstants.SYNTAX_STYLE_BBCODE);
			put("C",                  SyntaxConstants.SYNTAX_STYLE_C);
			put("Clojure",            SyntaxConstants.SYNTAX_STYLE_CLOJURE);
			put("C++",                SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
			put("C#",                 SyntaxConstants.SYNTAX_STYLE_CSHARP);
			put("CSS",                SyntaxConstants.SYNTAX_STYLE_CSS);
			put("CSV",                SyntaxConstants.SYNTAX_STYLE_CSV);
			put("D",                  SyntaxConstants.SYNTAX_STYLE_D);
			put("Dockerfile",         SyntaxConstants.SYNTAX_STYLE_DOCKERFILE);
			put("Dart",               SyntaxConstants.SYNTAX_STYLE_DART);
			put("DTD",                SyntaxConstants.SYNTAX_STYLE_DTD);
			put("Fortran",            SyntaxConstants.SYNTAX_STYLE_FORTRAN);
			put("Golang",             SyntaxConstants.SYNTAX_STYLE_GO);
			put("Groovy",             SyntaxConstants.SYNTAX_STYLE_GROOVY);
			put("Etc/Hosts",          SyntaxConstants.SYNTAX_STYLE_HOSTS);
			put("HTAccess",           SyntaxConstants.SYNTAX_STYLE_HTACCESS);
			put("HTML",               SyntaxConstants.SYNTAX_STYLE_HTML);
			put("INI",                SyntaxConstants.SYNTAX_STYLE_INI);
			put("Java",               SyntaxConstants.SYNTAX_STYLE_JAVA);
			put("JavaScript",         SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
			put("JSON",               SyntaxConstants.SYNTAX_STYLE_JSON);
			put("JSP",                SyntaxConstants.SYNTAX_STYLE_JSP);
			put("Kotlin",             SyntaxConstants.SYNTAX_STYLE_KOTLIN);
			put("LaTeX",              SyntaxConstants.SYNTAX_STYLE_LATEX);
			put("LESS",               SyntaxConstants.SYNTAX_STYLE_LESS);
			put("Lisp",               SyntaxConstants.SYNTAX_STYLE_LISP);
			put("Lua",                SyntaxConstants.SYNTAX_STYLE_LUA);
			put("Makefile",           SyntaxConstants.SYNTAX_STYLE_MAKEFILE);
			put("Markdown",           SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
			put("MXML",               SyntaxConstants.SYNTAX_STYLE_MXML);
			put("Nullsoft Installer", SyntaxConstants.SYNTAX_STYLE_NSIS);
			put("Perl",               SyntaxConstants.SYNTAX_STYLE_PERL);
			put("PHP",                SyntaxConstants.SYNTAX_STYLE_PHP);
			put("Properties",         SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
			put("Python",             SyntaxConstants.SYNTAX_STYLE_PYTHON);
			put("Ruby",               SyntaxConstants.SYNTAX_STYLE_RUBY);
			put("SAS",                SyntaxConstants.SYNTAX_STYLE_SAS);
			put("Scala",              SyntaxConstants.SYNTAX_STYLE_SCALA);
			put("SQL",                SyntaxConstants.SYNTAX_STYLE_SQL);
			put("TCL",                SyntaxConstants.SYNTAX_STYLE_TCL);
			put("TypeScript",         SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT);
			put("Bash",               SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
			put("VisualBasic",        SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC);
			put("CMD/BAT",            SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
			put("XML",                SyntaxConstants.SYNTAX_STYLE_XML);
			put("YAML",               SyntaxConstants.SYNTAX_STYLE_YAML);
		}
    });
    
    private static final Set<Charset> COMMON_CHARSETS = Collections.unmodifiableSet(new TreeSet<Charset>() 
    {
		private static final long serialVersionUID = -4915381545931926947L;
		{
			if (OSUtils.isWindows())
				add(Charset.forName("Windows-1252"));
			add(Charset.defaultCharset());
			add(Charset.forName("ASCII"));
			add(Charset.forName("UTF-8"));
			add(Charset.forName("UTF-16"));
		}
    });
    
    private static final Set<Charset> OTHER_CHARSETS = Collections.unmodifiableSet(new TreeSet<Charset>() 
    {
		private static final long serialVersionUID = -6067321778091486285L;
		{
			for (Charset cs : Charset.availableCharsets().values())
			{
				if (COMMON_CHARSETS.contains(cs))
					continue;
				else
					add(cs);
			}
		}
    });
    
	/**
	 * @return the singleton instance of this object.
	 */
	public static DoomToolsEditorProvider get()
	{
		return INSTANCE.get();
	}
	
	private static volatile boolean initLanguages = false;
	
	// Adds all the custom stuff.
	private static void initCustomLanguages()
	{
		if (initLanguages)
			return;
		
		AbstractTokenMakerFactory tokenMakers = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
		FoldParserManager foldManager = FoldParserManager.get();
		
		/*
		tokenMakers.putMapping(SYNTAX_STYLE_WADSCRIPT, RookScriptTokenMaker.class.getName());
		foldManager.addFoldParserMapping(SYNTAX_STYLE_WADSCRIPT, new CurlyFoldParser());
		*/
		
		// TODO: Add the rest.
		
		initLanguages = true;
	}

	/* ==================================================================== */
	
	public DoomToolsEditorProvider()
	{
		initCustomLanguages();
	}

	/**
	 * Gets a reference to an immutable map of language name to style name mapping.
	 * This is for drop-down or pop-up menus and the like.
	 * @return the map.
	 */
	public final Map<String, String> getAvailableLanguageMap()
	{
		return NAME_TO_STYLE_MAP;
	}
	
	/**
	 * Gets a reference to an immutable map of language name to style name mapping for other languages.
	 * This is for drop-down or pop-up menus and the like.
	 * @return the map.
	 */
	public final Map<String, String> getOtherAvailableLanguageMap()
	{
		return OTHER_NAME_TO_STYLE_MAP;
	}

	/**
	 * @return the common charsets.
	 */
	public final Set<Charset> getAvailableCommonCharsets()
	{
		return COMMON_CHARSETS;
	}
	
	/**
	 * @return the uncommon charsets.
	 */
	public final Set<Charset> getAvailableOtherCharsets()
	{
		return OTHER_CHARSETS;
	}
	
	/**
	 * Creates a text area using the file's name as a basis for type.
	 * @param file the file to use to find a type from.
	 * @return a new {@link RSyntaxTextArea}.
	 */
	public String getStyleByFile(File file)
	{
		String ext = FileUtils.getFileExtension(file);
		if (ObjectUtils.isEmpty(ext))
			ext = file.getName();
		
		String styleName;
		if ((styleName = EXTENSION_TO_STYLE_MAP.get(ext)) == null)
			styleName = SyntaxConstants.SYNTAX_STYLE_NONE;

		return styleName;
	}

}
