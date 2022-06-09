package net.mtrop.doom.tools.gui.managers;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

import net.mtrop.doom.tools.gui.managers.parsing.DecoHackCompletionProvider;
import net.mtrop.doom.tools.gui.managers.parsing.DecoHackTokenMaker;
import net.mtrop.doom.tools.gui.managers.parsing.DoomMakeCompletionProvider;
import net.mtrop.doom.tools.gui.managers.parsing.RookScriptCompletionProvider;
import net.mtrop.doom.tools.gui.managers.parsing.RookScriptTokenMaker;
import net.mtrop.doom.tools.gui.managers.parsing.WadMergeCompletionProvider;
import net.mtrop.doom.tools.gui.managers.parsing.WadMergeTokenMaker;
import net.mtrop.doom.tools.gui.managers.parsing.WadScriptCompletionProvider;
import net.mtrop.doom.tools.struct.FactoryMap;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;

import static org.fife.ui.rsyntaxtextarea.SyntaxConstants.*;


/**
 * Utility singleton for generating {@link RSyntaxTextArea} instances.
 * @author Matthew Tropiano
 */
public final class DoomToolsEditorProvider 
{
	public static final String SYNTAX_STYLE_DECOHACK =   "text/decohack";
	public static final String SYNTAX_STYLE_ROOKSCRIPT = "text/rookscript";
	public static final String SYNTAX_STYLE_WADMERGE =   "text/wadmerge";
	public static final String SYNTAX_STYLE_WADSCRIPT =  "text/wadscript";
	public static final String SYNTAX_STYLE_DOOMMAKE =   "text/doommake";
	
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsEditorProvider> INSTANCE = new SingletonProvider<>(() -> new DoomToolsEditorProvider());

    // For specific files.
    private static final Map<String, String> FILENAME_TO_STYLE_MAP = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) 
    {
		private static final long serialVersionUID = -2713345708437194651L;
		{
			put("doommake.script",      SYNTAX_STYLE_DOOMMAKE);
			put("doommake-lib.script",  SYNTAX_STYLE_DOOMMAKE);
			put("doommake-init.script", SYNTAX_STYLE_DOOMMAKE);
			put("dockerfile",           SYNTAX_STYLE_DOCKERFILE);
			put("makefile",             SYNTAX_STYLE_MAKEFILE);
			put("hosts",                SYNTAX_STYLE_HOSTS);
		}
    };
    
    private static final Map<String, String> EXTENSION_TO_STYLE_MAP = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) 
    {
		private static final long serialVersionUID = -1713345708437194651L;
		{
			put("asc",        SYNTAX_STYLE_ACTIONSCRIPT);
			put("as2",        SYNTAX_STYLE_ACTIONSCRIPT);
			put("as3",        SYNTAX_STYLE_ACTIONSCRIPT);
			put("asm",        SYNTAX_STYLE_ASSEMBLER_X86);
			put("bbc",        SYNTAX_STYLE_BBCODE);
			put("c",          SYNTAX_STYLE_C);
			put("cljs",       SYNTAX_STYLE_CLOJURE);
			put("cpp",        SYNTAX_STYLE_CPLUSPLUS);
			put("cs",         SYNTAX_STYLE_CSHARP);
			put("css",        SYNTAX_STYLE_CSS);
			put("csv",        SYNTAX_STYLE_CSV);
			put("d",          SYNTAX_STYLE_D);
			put("dart",       SYNTAX_STYLE_DART);
			put("dtd",        SYNTAX_STYLE_DTD);
			put("fortran",    SYNTAX_STYLE_FORTRAN);
			put("go",         SYNTAX_STYLE_GO);
			put("groovy",     SYNTAX_STYLE_GROOVY);
			put("htaccess",   SYNTAX_STYLE_HTACCESS);
			put("htm",        SYNTAX_STYLE_HTML);
			put("html",       SYNTAX_STYLE_HTML);
			put("ini",        SYNTAX_STYLE_INI);
			put("java",       SYNTAX_STYLE_JAVA);
			put("js",         SYNTAX_STYLE_JAVASCRIPT);
			put("json",       SYNTAX_STYLE_JSON);
			put("jsp",        SYNTAX_STYLE_JSP);
			put("kt",         SYNTAX_STYLE_KOTLIN);
			put("tex",        SYNTAX_STYLE_LATEX);
			put("lss",        SYNTAX_STYLE_LESS);
			put("lsp",        SYNTAX_STYLE_LISP);
			put("lua",        SYNTAX_STYLE_LUA);
			put("md",         SYNTAX_STYLE_MARKDOWN);
			put("mxml",       SYNTAX_STYLE_MXML);
			put("nsis",       SYNTAX_STYLE_NSIS);
			put("pl",         SYNTAX_STYLE_PERL);
			put("php",        SYNTAX_STYLE_PHP);
			put("properties", SYNTAX_STYLE_PROPERTIES_FILE);
			put("py",         SYNTAX_STYLE_PYTHON);
			put("rb",         SYNTAX_STYLE_RUBY);
			put("sas",        SYNTAX_STYLE_SAS);
			put("sca",        SYNTAX_STYLE_SCALA);
			put("sql",        SYNTAX_STYLE_SQL);
			put("tcl",        SYNTAX_STYLE_TCL);
			put("txt",        SYNTAX_STYLE_NONE);
			put("ts",         SYNTAX_STYLE_TYPESCRIPT);
			put("sh",         SYNTAX_STYLE_UNIX_SHELL);
			put("vb",         SYNTAX_STYLE_VISUAL_BASIC);
			put("vbs",        SYNTAX_STYLE_VISUAL_BASIC);
			put("bat",        SYNTAX_STYLE_WINDOWS_BATCH);
			put("cmd",        SYNTAX_STYLE_WINDOWS_BATCH);
			put("xml",        SYNTAX_STYLE_XML);
			put("yml",        SYNTAX_STYLE_YAML);
			put("yaml",       SYNTAX_STYLE_YAML);
			
			// Custom
			put("dh",         SYNTAX_STYLE_DECOHACK);
			put("script",     SYNTAX_STYLE_DOOMMAKE);
			put("rscript",    SYNTAX_STYLE_ROOKSCRIPT);
			put("wadmerge",   SYNTAX_STYLE_WADMERGE);
			put("wadm",       SYNTAX_STYLE_WADMERGE);
			put("wscr",       SYNTAX_STYLE_WADSCRIPT);
			put("wscript",    SYNTAX_STYLE_WADSCRIPT);
			put("wsx",        SYNTAX_STYLE_WADSCRIPT);
		}
    };

    private static final Map<String, String> NAME_TO_STYLE_MAP = Collections.unmodifiableMap(new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) 
    {
		private static final long serialVersionUID = -2069191041359249343L;
		{
			put("Plain Text",         SYNTAX_STYLE_NONE);
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
			put("ActionScript",       SYNTAX_STYLE_ACTIONSCRIPT);
			put("Assembler (x86)",    SYNTAX_STYLE_ASSEMBLER_X86);
			put("BBCode",             SYNTAX_STYLE_BBCODE);
			put("C",                  SYNTAX_STYLE_C);
			put("Clojure",            SYNTAX_STYLE_CLOJURE);
			put("C++",                SYNTAX_STYLE_CPLUSPLUS);
			put("C#",                 SYNTAX_STYLE_CSHARP);
			put("CSS",                SYNTAX_STYLE_CSS);
			put("CSV",                SYNTAX_STYLE_CSV);
			put("D",                  SYNTAX_STYLE_D);
			put("Dockerfile",         SYNTAX_STYLE_DOCKERFILE);
			put("Dart",               SYNTAX_STYLE_DART);
			put("DTD",                SYNTAX_STYLE_DTD);
			put("Fortran",            SYNTAX_STYLE_FORTRAN);
			put("Golang",             SYNTAX_STYLE_GO);
			put("Groovy",             SYNTAX_STYLE_GROOVY);
			put("Etc/Hosts",          SYNTAX_STYLE_HOSTS);
			put("HTAccess",           SYNTAX_STYLE_HTACCESS);
			put("HTML",               SYNTAX_STYLE_HTML);
			put("INI",                SYNTAX_STYLE_INI);
			put("Java",               SYNTAX_STYLE_JAVA);
			put("JavaScript",         SYNTAX_STYLE_JAVASCRIPT);
			put("JSON",               SYNTAX_STYLE_JSON);
			put("JSP",                SYNTAX_STYLE_JSP);
			put("Kotlin",             SYNTAX_STYLE_KOTLIN);
			put("LaTeX",              SYNTAX_STYLE_LATEX);
			put("LESS",               SYNTAX_STYLE_LESS);
			put("Lisp",               SYNTAX_STYLE_LISP);
			put("Lua",                SYNTAX_STYLE_LUA);
			put("Makefile",           SYNTAX_STYLE_MAKEFILE);
			put("Markdown",           SYNTAX_STYLE_MARKDOWN);
			put("MXML",               SYNTAX_STYLE_MXML);
			put("Nullsoft Installer", SYNTAX_STYLE_NSIS);
			put("Perl",               SYNTAX_STYLE_PERL);
			put("PHP",                SYNTAX_STYLE_PHP);
			put("Properties",         SYNTAX_STYLE_PROPERTIES_FILE);
			put("Python",             SYNTAX_STYLE_PYTHON);
			put("Ruby",               SYNTAX_STYLE_RUBY);
			put("SAS",                SYNTAX_STYLE_SAS);
			put("Scala",              SYNTAX_STYLE_SCALA);
			put("SQL",                SYNTAX_STYLE_SQL);
			put("TCL",                SYNTAX_STYLE_TCL);
			put("TypeScript",         SYNTAX_STYLE_TYPESCRIPT);
			put("Bash",               SYNTAX_STYLE_UNIX_SHELL);
			put("VisualBasic",        SYNTAX_STYLE_VISUAL_BASIC);
			put("CMD/BAT",            SYNTAX_STYLE_WINDOWS_BATCH);
			put("XML",                SYNTAX_STYLE_XML);
			put("YAML",               SYNTAX_STYLE_YAML);
		}
    });
    
    private static final Set<Charset> COMMON_CHARSETS = Collections.unmodifiableSet(new TreeSet<Charset>() 
    {
		private static final long serialVersionUID = -4915381545931926947L;
		{
			if (OSUtils.isWindows())
				add(Charset.forName("Windows-1252"));
			add(Charset.defaultCharset());
			add(Charset.forName("US-ASCII"));
			add(Charset.forName("UTF-8"));
			add(Charset.forName("UTF-16"));
			add(Charset.forName("UTF-16LE"));
			add(Charset.forName("UTF-16BE"));
			add(Charset.forName("ISO-8859-1")); // Web ISO Standard
			add(Charset.forName("IBM437"));     // IBM PC Extended ASCII (with the graphics and shading)
			add(Charset.forName("GB18030"));    // Simplified Chinese
			add(Charset.forName("Big5"));       // Traditional Chinese
			add(Charset.forName("Shift-JIS"));  // Japanese
			add(Charset.forName("EUC-JP"));     // Japanese (ASCII-Safe)
			add(Charset.forName("EUC-KR"));     // Korean
			add(Charset.forName("KOI8-R"));     // Cyrillic
		}
    });
    
    private static final Map<String, Supplier<CompletionProvider>> COMPLETION_PROVIDERS = Collections.unmodifiableMap(new TreeMap<String, Supplier<CompletionProvider>>() 
    {
		private static final long serialVersionUID = 1638202185490860804L;
		{
			put(SYNTAX_STYLE_WADMERGE,   () -> new WadMergeCompletionProvider());
			put(SYNTAX_STYLE_ROOKSCRIPT, () -> new RookScriptCompletionProvider());
			put(SYNTAX_STYLE_WADSCRIPT,  () -> new WadScriptCompletionProvider());
			put(SYNTAX_STYLE_DOOMMAKE,   () -> new DoomMakeCompletionProvider());
			put(SYNTAX_STYLE_DECOHACK,   () -> new DecoHackCompletionProvider());
		}
    });
    
    private static final FactoryMap<String, CompletionProvider> FACTORY = new FactoryMap<String, CompletionProvider>()
    {
		@Override
		protected Supplier<CompletionProvider> getSupplierForKey(String key)
		{
			return COMPLETION_PROVIDERS.getOrDefault(key, () -> new DefaultCompletionProvider());
		}
    };
    
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
		
		tokenMakers.putMapping(SYNTAX_STYLE_DOOMMAKE,   RookScriptTokenMaker.class.getName());
		tokenMakers.putMapping(SYNTAX_STYLE_WADSCRIPT,  RookScriptTokenMaker.class.getName());
		tokenMakers.putMapping(SYNTAX_STYLE_ROOKSCRIPT, RookScriptTokenMaker.class.getName());
		tokenMakers.putMapping(SYNTAX_STYLE_WADMERGE,   WadMergeTokenMaker.class.getName());
		tokenMakers.putMapping(SYNTAX_STYLE_DECOHACK,   DecoHackTokenMaker.class.getName());
		
		foldManager.addFoldParserMapping(SYNTAX_STYLE_DOOMMAKE,   new CurlyFoldParser());
		foldManager.addFoldParserMapping(SYNTAX_STYLE_WADSCRIPT,  new CurlyFoldParser());
		foldManager.addFoldParserMapping(SYNTAX_STYLE_ROOKSCRIPT, new CurlyFoldParser());
		foldManager.addFoldParserMapping(SYNTAX_STYLE_DECOHACK,   new CurlyFoldParser());
		
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
	public Map<String, String> getAvailableLanguageMap()
	{
		return NAME_TO_STYLE_MAP;
	}
	
	/**
	 * Gets a reference to an immutable map of language name to style name mapping for other languages.
	 * This is for drop-down or pop-up menus and the like.
	 * @return the map.
	 */
	public Map<String, String> getOtherAvailableLanguageMap()
	{
		return OTHER_NAME_TO_STYLE_MAP;
	}

	/**
	 * @return the common charsets.
	 */
	public Set<Charset> getAvailableCommonCharsets()
	{
		return COMMON_CHARSETS;
	}
	
	/**
	 * Creates a text area using the file's name as a basis for type.
	 * @param file the file to use to find a type from.
	 * @return a new {@link RSyntaxTextArea}.
	 */
	public String getStyleByFile(File file)
	{
		String styleName;

		String fileName = file.getName();
		if ((styleName = FILENAME_TO_STYLE_MAP.get(fileName)) != null)
			return styleName;
		
		String ext = FileUtils.getFileExtension(fileName);
		if ((styleName = EXTENSION_TO_STYLE_MAP.get(ext)) != null)
			return styleName;

		return SYNTAX_STYLE_NONE;
	}

	/**
	 * Gets a corresponding auto-completion engine by a style name.
	 * @param styleName the style name.
	 * @return a new auto-completion instance for a style.
	 */
	public AutoCompletion createAutoCompletionByStyle(String styleName)
	{
		return new AutoCompletion(getProviderByStyle(styleName));
	}

	// Style name.
	private CompletionProvider getProviderByStyle(String styleName)
	{
		return FACTORY.get(styleName);
	}

}
