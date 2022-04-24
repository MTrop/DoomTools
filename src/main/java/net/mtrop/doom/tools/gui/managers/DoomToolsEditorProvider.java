package net.mtrop.doom.tools.gui.managers;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.common.Common;

/**
 * Utility singleton for generating {@link RSyntaxTextArea} instances.
 * @author Matthew Tropiano
 */
public final class DoomToolsEditorProvider 
{
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsEditorProvider> INSTANCE = new SingletonProvider<>(() -> new DoomToolsEditorProvider());
    
    private static final Map<String, String> EXTENSION_TO_STYLE_MAP = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) 
    {
		private static final long serialVersionUID = -1713345708437194651L;
		{
			put("asc", SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT);
			put("as2", SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT);
			put("as3", SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT);
			put("asm", SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
			put("bbc", SyntaxConstants.SYNTAX_STYLE_BBCODE);
			put("c", SyntaxConstants.SYNTAX_STYLE_C);
			put("cljs", SyntaxConstants.SYNTAX_STYLE_CLOJURE);
			put("cpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
			put("cs", SyntaxConstants.SYNTAX_STYLE_CSHARP);
			put("css", SyntaxConstants.SYNTAX_STYLE_CSS);
			put("csv", SyntaxConstants.SYNTAX_STYLE_CSV);
			put("d", SyntaxConstants.SYNTAX_STYLE_D);
			put("dockerfile", SyntaxConstants.SYNTAX_STYLE_DOCKERFILE);
			put("dart", SyntaxConstants.SYNTAX_STYLE_DART);
			put("dtd", SyntaxConstants.SYNTAX_STYLE_DTD);
			put("fortran", SyntaxConstants.SYNTAX_STYLE_FORTRAN);
			put("go", SyntaxConstants.SYNTAX_STYLE_GO);
			put("groovy", SyntaxConstants.SYNTAX_STYLE_GROOVY);
			put("hosts", SyntaxConstants.SYNTAX_STYLE_HOSTS);
			put("htaccess", SyntaxConstants.SYNTAX_STYLE_HTACCESS);
			put("htm", SyntaxConstants.SYNTAX_STYLE_HTML);
			put("html", SyntaxConstants.SYNTAX_STYLE_HTML);
			put("ini", SyntaxConstants.SYNTAX_STYLE_INI);
			put("java", SyntaxConstants.SYNTAX_STYLE_JAVA);
			put("js", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
			put("json", SyntaxConstants.SYNTAX_STYLE_JSON);
			put("jsp", SyntaxConstants.SYNTAX_STYLE_JSP);
			put("kt", SyntaxConstants.SYNTAX_STYLE_KOTLIN);
			put("tex", SyntaxConstants.SYNTAX_STYLE_LATEX);
			put("lss", SyntaxConstants.SYNTAX_STYLE_LESS);
			put("lsp", SyntaxConstants.SYNTAX_STYLE_LISP);
			put("lua", SyntaxConstants.SYNTAX_STYLE_LUA);
			put("makefile", SyntaxConstants.SYNTAX_STYLE_MAKEFILE);
			put("md", SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
			put("mxml", SyntaxConstants.SYNTAX_STYLE_MXML);
			put("nsis", SyntaxConstants.SYNTAX_STYLE_NSIS);
			put("pl", SyntaxConstants.SYNTAX_STYLE_PERL);
			put("php", SyntaxConstants.SYNTAX_STYLE_PHP);
			put("properties", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
			put("py", SyntaxConstants.SYNTAX_STYLE_PYTHON);
			put("rb", SyntaxConstants.SYNTAX_STYLE_RUBY);
			put("sas", SyntaxConstants.SYNTAX_STYLE_SAS);
			put("sca", SyntaxConstants.SYNTAX_STYLE_SCALA);
			put("sql", SyntaxConstants.SYNTAX_STYLE_SQL);
			put("tcl", SyntaxConstants.SYNTAX_STYLE_TCL);
			put("ts", SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT);
			put("sh", SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
			put("vb", SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC);
			put("vbs", SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC);
			put("bat", SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
			put("cmd", SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
			put("xml", SyntaxConstants.SYNTAX_STYLE_XML);
			put("yml", SyntaxConstants.SYNTAX_STYLE_YAML);
			put("yaml", SyntaxConstants.SYNTAX_STYLE_YAML);
		}
    };

	/**
	 * @return the singleton instance of this object.
	 */
	public static DoomToolsEditorProvider get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
	public DoomToolsEditorProvider()
	{
		initCustomLanguages();
	}
	
	private static void initCustomLanguages()
	{
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory)TokenMakerFactory.getDefaultInstance();
		//atmf.putMapping("text/x-wadscript", "fully.qualified.classNameOfYourSyntaxStyle");
	}
	
	/**
	 * Creates a text area using the file's name as a basis for type.
	 * @param file the file to use to find a type from.
	 * @return a new {@link RSyntaxTextArea}.
	 */
	public String getStyleByFile(File file)
	{
		String ext = Common.getFileExtension(file);
		if (Common.isEmpty(ext))
			ext = file.getName();
		
		String styleName;
		if ((styleName = EXTENSION_TO_STYLE_MAP.get(ext)) == null)
			styleName = SyntaxConstants.SYNTAX_STYLE_NONE;

		return styleName;
	}
	
}
