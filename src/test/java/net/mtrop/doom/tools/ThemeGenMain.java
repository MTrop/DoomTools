package net.mtrop.doom.tools;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.UIDefaults;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.IntelliJTheme;

import net.mtrop.doom.tools.struct.HTMLWriter;
import net.mtrop.doom.tools.struct.HTMLWriter.Options;
import net.mtrop.doom.tools.struct.util.IOUtils;

/**
 * Generates Text Editor themes using FlatLaf themes.
 */
@SuppressWarnings("unused")
public final class ThemeGenMain 
{
	private static final String[] GUI_TYPE_PATHS = {
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Arc Dark.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Atom One Dark.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Atom One Light.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Dracula.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/GitHub.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/GitHub Dark.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Light Owl.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Material Darker.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Material Deep Ocean.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Material Lighter.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Material Oceanic.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Material Palenight.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Monokai Pro.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Moonlight.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Night Owl.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Solarized Dark.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/material-theme-ui-lite/Solarized Light.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/arc_theme_dark_orange.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/arc_theme_dark.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/arc-theme.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/arc-theme-orange.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Carbon.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Cobalt_2.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Cyan.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/DarkFlatTheme.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/DarkPurple.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Dracula.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Gradianto_dark_fuchsia.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Gradianto_deep_ocean.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Gradianto_midnight_blue.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Gradianto_Nature_Green.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Gray.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/gruvbox_dark_hard.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/HiberbeeDark.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/HighContrast.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/LightFlatTheme.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/MaterialTheme.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Monocai.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Monokai_Pro.default.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/nord.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/one_dark.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/SolarizedDark.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/SolarizedLight.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Spacegray.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/vuesion_theme.theme.json",
		"com/formdev/flatlaf/intellijthemes/themes/Xcode-Dark.theme.json"
	};
	
	public static void main(String[] args) throws Exception
	{
//		Set<String> outSet = null;
//		UIDefaults defaults = null;
		for (String res : GUI_TYPE_PATHS)
		{
			FlatLaf theme;
			try (InputStream in = IOUtils.openResource(res))
			{
				theme = IntelliJTheme.createLaf(new IntelliJTheme(in));
			}
			String outPath = ("src/main/resources/gui/editor/themes/" + theme.getName() + ".xml").replace(" ", "-").toLowerCase();
			generateXMLTheme(theme, outPath);
			
			/*
			defaults = theme.getDefaults();
			if (outSet == null)
			{
				outSet = new TreeSet<>();
				for (Object key : defaults.keySet())
					outSet.add(String.valueOf(key));
			}
			else
			{
				String[] keys = outSet.toArray(new String[outSet.size()]);
				for (String key : keys)
					if (!defaults.containsKey(key))
						outSet.remove(key);
			}
			*/
			System.out.println(theme.getName().replace(" ", "_").toUpperCase() + "(" + "\"" + theme.getName() + "\", \""+ outPath.substring(outPath.indexOf("gui/editor/themes/")) + "\", " + theme.isDark() + "),");
		}
//		for (String key : outSet)
//			System.out.println(key + "=" + (defaults.get(key) == null ? "null" : defaults.get(key).getClass().getSimpleName()));
	}
	
	static final String HEXALPHA = "0123456789ABCDEF";
	
	private static String colorToString(Color color)
	{
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		
		StringBuilder sb = new StringBuilder(6);
		sb.append(HEXALPHA.charAt((r & 0xf0) >>> 4));
		sb.append(HEXALPHA.charAt((r & 0x0f)));
		sb.append(HEXALPHA.charAt((g & 0xf0) >>> 4));
		sb.append(HEXALPHA.charAt((g & 0x0f)));
		sb.append(HEXALPHA.charAt((b & 0xf0) >>> 4));
		sb.append(HEXALPHA.charAt((b & 0x0f)));
		return sb.toString();
	}

	private static Color colorAdjust(Color color, boolean darkTheme)
	{
		return darkTheme ? colorDarken(color) : colorLighten(color);
	}
	
	private static Color colorLighten(Color color)
	{
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		
		return new Color(Math.min(r + 32, 255), Math.min(g + 32, 255), Math.min(b + 32, 255));
	}
	
	private static Color colorDarken(Color color)
	{
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		
		return new Color(Math.max(r - 32, 0), Math.max(g - 32, 0), Math.max(b - 32, 0));
	}
	
	private static void generateXMLTheme(FlatLaf theme, String path) throws IOException
	{
		UIDefaults defaults = theme.getDefaults();
		
		try (
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(path))));
			HTMLWriter html = new HTMLWriter(writer, Options.PRETTY, Options.SLASHES_IN_SINGLE_TAGS);
		){
			writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>").append("\n");
			writer.append("<!DOCTYPE RSyntaxTheme SYSTEM \"theme.dtd\">").append("\n");
			
			writer.append("\n");

			html.push("RSyntaxTheme", HTMLWriter.attribute("version", "1.0"));
				html.tag("background", 
					HTMLWriter.attribute("color", colorToString(defaults.getColor("TextField.background")))
				);
				html.tag("caret", 
					HTMLWriter.attribute("color", colorToString(defaults.getColor("TextField.caretForeground")))
				);
				html.tag("selection", 
					HTMLWriter.attribute("fg", colorToString(defaults.getColor("TextField.selectionForeground"))),
					HTMLWriter.attribute("useFG", "false"),
					HTMLWriter.attribute("bg", colorToString(defaults.getColor("TextField.selectionBackground")))
				);
				html.tag("currentLineHighlight", 
					HTMLWriter.attribute("color", colorToString(defaults.getColor("TextField.light"))),
					HTMLWriter.attribute("fade", "false")
				);
				html.tag("marginLine", 
					HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("TextField.foreground"), theme.isDark())))
				);
				html.tag("markAllHighlight", 
					HTMLWriter.attribute("color", colorToString(colorLighten(defaults.getColor("TextField.highlight"))))
				);
				html.tag("markOccurrencesHighlight", 
					HTMLWriter.attribute("color", colorToString(colorLighten(defaults.getColor("TextField.highlight")))),
					HTMLWriter.attribute("border", "false")
				);
				html.tag("matchedBracket", 
					HTMLWriter.attribute("fg", colorToString(defaults.getColor("TextField.foreground"))),
					HTMLWriter.attribute("bg", colorToString(colorLighten(defaults.getColor("TextField.highlight")))),
					HTMLWriter.attribute("highlightBoth", "false"),
					HTMLWriter.attribute("animate", "true")
				);
				html.tag("hyperlinks", 
					HTMLWriter.attribute("fg", colorToString(defaults.getColor("Component.linkColor")))
				);
				html.push("secondaryLanguages");
					html.tag("language", 
						HTMLWriter.attribute("index", "1"),
						HTMLWriter.attribute("bg", colorToString(defaults.getColor("CheckBox.icon.background")))
					);
					html.tag("language", 
						HTMLWriter.attribute("index", "2"),
						HTMLWriter.attribute("bg", colorToString(colorAdjust(defaults.getColor("CheckBox.icon.background"), !theme.isDark())))
					);
					html.tag("language", 
						HTMLWriter.attribute("index", "3"),
						HTMLWriter.attribute("bg", colorToString(colorAdjust(colorAdjust(defaults.getColor("CheckBox.icon.background"), !theme.isDark()), !theme.isDark())))
					);
				html.pop();
				
				writer.append("\n");

				html.tag("gutterBorder", 
					HTMLWriter.attribute("color", colorToString(defaults.getColor("EditorPane.inactiveBackground")))
				);
				html.tag("lineNumbers", 
					HTMLWriter.attribute("fg", colorToString(defaults.getColor("EditorPane.foreground"))),
					HTMLWriter.attribute("currentFG", colorToString(defaults.getColor("EditorPane.selectionForeground")))
				);
				html.tag("foldIndicator", 
					HTMLWriter.attribute("fg", colorToString(defaults.getColor("Tree.icon.closedColor"))),
					HTMLWriter.attribute("armedFg", colorToString(colorAdjust(defaults.getColor("Tree.icon.closedColor"), theme.isDark()))),
					HTMLWriter.attribute("iconBg", colorToString(defaults.getColor("Tree.icon.openColor")))
				);
				html.tag("iconRowHeader", 
					HTMLWriter.attribute("activeLineRange", colorToString(colorLighten(defaults.getColor("Tree.icon.closedColor"))))
				);
				
				writer.append("\n");

				html.push("tokenStyles");
					html.tag("style", 
						HTMLWriter.attribute("token", "IDENTIFIER"),
						HTMLWriter.attribute("fg", colorToString(defaults.getColor("TextField.foreground"))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "RESERVED_WORD"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("Button.selectedBackground"), !theme.isDark()))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "RESERVED_WORD_2"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("Button.selectedBackground"), !theme.isDark()))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "ANNOTATION"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("Button.selectedBackground")))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "COMMENT_DOCUMENTATION"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("ComboBox.foreground")))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "true")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "COMMENT_EOL"),
						HTMLWriter.attribute("fg", colorToString(defaults.getColor("ComboBox.foreground"))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "true")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "COMMENT_MULTILINE"),
						HTMLWriter.attribute("fg", colorToString(defaults.getColor("ComboBox.foreground"))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "true")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "COMMENT_KEYWORD"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("ComboBox.foreground"), !theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "true")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "COMMENT_MARKUP"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("ComboBox.foreground")))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "true")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "DATA_TYPE"),
						HTMLWriter.attribute("fg", colorToString(defaults.getColor("Button.selectedBackground"))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "FUNCTION"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("Button.selectedBackground")))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "LITERAL_BOOLEAN"),
						HTMLWriter.attribute("fg", colorToString(defaults.getColor("Component.accentColor"))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "LITERAL_NUMBER_DECIMAL_INT"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("Component.accentColor"), !theme.isDark()))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "LITERAL_NUMBER_FLOAT"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("Component.accentColor"), !theme.isDark()))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "LITERAL_NUMBER_HEXADECIMAL"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("Component.accentColor"), !theme.isDark()))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "LITERAL_STRING_DOUBLE_QUOTE"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("Component.accentColor"), !theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "LITERAL_CHAR"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("Component.accentColor"), !theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "LITERAL_BACKQUOTE"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("Component.accentColor"), !theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "MARKUP_TAG_DELIMITER"),
						HTMLWriter.attribute("fg", colorToString(defaults.getColor("TextField.foreground"))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "MARKUP_TAG_NAME"),
						HTMLWriter.attribute("fg", colorToString(defaults.getColor("TextField.foreground"))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "MARKUP_TAG_ATTRIBUTE"),
						HTMLWriter.attribute("fg", colorToString(colorDarken(defaults.getColor("TextField.foreground")))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "MARKUP_TAG_ATTRIBUTE_VALUE"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("Component.accentColor")))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "MARKUP_COMMENT"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("TextField.foreground"), theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "MARKUP_DTD"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("TextField.foreground"), theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "MARKUP_PROCESSING_INSTRUCTION"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("TextField.foreground"), theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "MARKUP_CDATA"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("TextField.foreground"), theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "MARKUP_CDATA_DELIMITER"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("TextField.foreground"), theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "MARKUP_ENTITY_REFERENCE"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("TextField.foreground")))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "OPERATOR"),
						HTMLWriter.attribute("fg", colorToString(defaults.getColor("TextField.foreground"))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "PREPROCESSOR"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("TextField.foreground")))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "REGEX"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("Button.selectedBackground")))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "SEPARATOR"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("TextField.foreground"), theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "VARIABLE"),
						HTMLWriter.attribute("fg", colorToString(defaults.getColor("TextField.foreground"))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "WHITESPACE"),
						HTMLWriter.attribute("fg", colorToString(colorAdjust(defaults.getColor("TextField.foreground"), theme.isDark()))),
						HTMLWriter.attribute("bold", "false"),
						HTMLWriter.attribute("italic", "false")
					);

					writer.append("\n");

					html.tag("style", 
						HTMLWriter.attribute("token", "ERROR_IDENTIFIER"),
						HTMLWriter.attribute("fg", colorToString(defaults.getColor("TextField.foreground"))),
						HTMLWriter.attribute("bg", colorToString(defaults.getColor("Actions.Red"))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "ERROR_NUMBER_FORMAT"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("Component.accentColor")))),
						HTMLWriter.attribute("bg", colorToString(defaults.getColor("Actions.Red"))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "ERROR_STRING_DOUBLE"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("Component.accentColor")))),
						HTMLWriter.attribute("bg", colorToString(defaults.getColor("Actions.Red"))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
					html.tag("style", 
						HTMLWriter.attribute("token", "ERROR_CHAR"),
						HTMLWriter.attribute("fg", colorToString(colorLighten(defaults.getColor("Component.accentColor")))),
						HTMLWriter.attribute("bg", colorToString(defaults.getColor("Actions.Red"))),
						HTMLWriter.attribute("bold", "true"),
						HTMLWriter.attribute("italic", "false")
					);
				html.pop();

			html.pop();
		} 
	}
	
}
