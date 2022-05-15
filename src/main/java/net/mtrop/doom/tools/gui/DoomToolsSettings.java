package net.mtrop.doom.tools.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.KeyStroke;

import org.fife.ui.rsyntaxtextarea.Theme;

import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.gui.DoomToolsConstants.Paths;
import net.mtrop.doom.tools.gui.apps.swing.panels.MultiFileEditorPanel.EditorAutoCompleteSettings;
import net.mtrop.doom.tools.gui.apps.swing.panels.MultiFileEditorPanel.EditorCodeSettings;
import net.mtrop.doom.tools.gui.apps.swing.panels.MultiFileEditorPanel.EditorViewSettings;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

/**
 * Doom Tools settings handler.
 * @author Matthew Tropiano
 */
public class DoomToolsSettings
{
	private static final String EDITOR_VIEW = ".editor.view";
    private static final String EDITOR_CODE = ".editor.code";
    private static final String EDITOR_AUTOCOMPLETE = ".editor.autocomplete";
    private static final String EDITOR_THEME = ".editor.theme";

    private static final String VIEW_TABSIZE = ".tabsize";
	private static final String VIEW_SOFTTABS = ".softtabs";
	private static final String VIEW_WRAPPING = ".wrapping";
	private static final String VIEW_WRAPWORDS = ".wrapwords";
	
	private static final String CODE_MARGIN_LINE_ENABLED = ".marginLineEnabled";
	private static final String CODE_MARGIN_LINE_POSITION = ".marginLinePosition";
	private static final String CODE_ROUNDED_SELECTION_EDGES = ".roundedSelectionEdges";
	private static final String CODE_PAINT_MARK_OCCURRENCES_BORDER = ".paintMarkOccurrencesBorder";
	private static final String CODE_HIGHLIGHT_CURRENT_LINE = ".highlightCurrentLine";
	private static final String CODE_PAINT_MATCHED_BRACKET_PAIR = ".paintMatchedBracketPair";
	private static final String CODE_MARK_OCCURRENCES_DELAY = ".markOccurrencesDelay";
	private static final String CODE_MARK_ALL_ON_OCCURRENCE_SEARCHES = ".markAllOnOccurrenceSearches";
	private static final String CODE_MARK_OCCURRENCES = ".markOccurrences";
	private static final String CODE_PAINT_TAB_LINES = ".paintTabLines";
	private static final String CODE_WHITESPACE_VISIBLE = ".whitespaceVisible";
	private static final String CODE_USE_FOCUSABLE_TIPS = ".useFocusableTips";
	private static final String CODE_SHOW_MATCHED_BRACKET_POPUP = ".showMatchedBracketPopup";
	private static final String CODE_HIGHLIGHT_SECONDARY_LANGUAGES = ".highlightSecondaryLanguages";
	private static final String CODE_EOL_MARKERS_VISIBLE = ".eolMarkersVisible";
	private static final String CODE_FOLDING_ENABLED = ".codeFoldingEnabled";
	private static final String CODE_CLOSE_MARKUP_TAGS = ".closeMarkupTags";
	private static final String CODE_CLOSE_CURLY_BRACES = ".closeCurlyBraces";
	private static final String CODE_CLEAR_WHITESPACE_LINES_ENABLED = ".clearWhitespaceLinesEnabled";
	private static final String CODE_BRACKET_MATCHING_ENABLED = ".bracketMatchingEnabled";
	private static final String CODE_AUTO_INDENT_ENABLED = ".autoIndentEnabled";
	private static final String CODE_ANIMATE_BRACKET_MATCHING = ".animateBracketMatching";
	private static final String CODE_USE_SELECTED_TEXT_COLOR = ".useSelectedTextColor";
	private static final String CODE_PARSER_DELAY = ".parserDelay";
	private static final String CODE_HYPERLINKS_ENABLED = ".hyperlinksEnabled";
	private static final String CODE_LINK_SCANNING_MASK = ".linkScanningMask";

	private static final String AUTOCOMPLETE_CHOICES_WINDOW_SIZE_WIDTH = ".choicesWindowSizeWidth";
	private static final String AUTOCOMPLETE_CHOICES_WINDOW_SIZE_HEIGHT = ".choicesWindowSizeHeight";
	private static final String AUTOCOMPLETE_DESCRIPTION_WINDOW_SIZE_HEIGHT = ".descriptionWindowSizeHeight";
	private static final String AUTOCOMPLETE_TRIGGER_KEY = ".triggerKey";
	private static final String AUTOCOMPLETE_AUTO_COMPLETE_ENABLED = ".autoCompleteEnabled";
	private static final String AUTOCOMPLETE_AUTO_COMPLETE_SINGLE_CHOICES = ".autoCompleteSingleChoices";
	private static final String AUTOCOMPLETE_AUTO_ACTIVATION_ENABLED = ".autoActivationEnabled";
	private static final String AUTOCOMPLETE_SHOW_DESC_WINDOW = ".showDescWindow";
	private static final String AUTOCOMPLETE_DESCRIPTION_WINDOW_SIZE_WIDTH = ".descriptionWindowSizeWidth";
	private static final String AUTOCOMPLETE_PARAMETER_DESCRIPTION_TRUNCATE_THRESHOLD = ".parameterDescriptionTruncateThreshold";
	
	private static final String THEME_BASE_FONT = ".baseFont";
	private static final String THEME_BG_COLOR = ".bgColor";
	private static final String THEME_CARET_COLOR = ".caretColor";
	private static final String THEME_SELECTION_FG = ".selectionFG";
	private static final String THEME_SELECTION_BG = ".selectionBG";
	private static final String THEME_USE_SELECTION_FG = ".useSelectionFG";
	private static final String THEME_CURRENT_LINE_NUMBER_COLOR = ".currentLineNumberColor";
	private static final String THEME_CURRENT_LINE_HIGHLIGHT = ".currentLineHighlight";
	private static final String THEME_FADE_CURRENT_LINE_HIGHLIGHT = ".fadeCurrentLineHighlight";
	private static final String THEME_ACTIVE_LINE_RANGE_COLOR = ".activeLineRangeColor";
	private static final String THEME_GUTTER_BORDER_COLOR = ".gutterBorderColor";
	private static final String THEME_GUTTER_BACKGROUND_COLOR = ".gutterBackgroundColor";
	private static final String THEME_SECONDARY_LANGUAGES = ".secondaryLanguages.";
	private static final String THEME_HYPERLINK_FG = ".hyperlinkFG";
	private static final String THEME_MATCHED_BRACKET_ANIMATE = ".matchedBracketAnimate";
	private static final String THEME_MATCHED_BRACKET_HIGHLIGHT_BOTH = ".matchedBracketHighlightBoth";
	private static final String THEME_MATCHED_BRACKET_BG = ".matchedBracketBG";
	private static final String THEME_MATCHED_BRACKET_FG = ".matchedBracketFG";
	private static final String THEME_MARK_OCCURRENCES_BORDER = ".markOccurrencesBorder";
	private static final String THEME_MARK_OCCURRENCES_COLOR = ".markOccurrencesColor";
	private static final String THEME_MARK_ALL_HIGHLIGHT_COLOR = ".markAllHighlightColor";
	private static final String THEME_MARGIN_LINE_COLOR = ".marginLineColor";
	private static final String THEME_TAB_LINE_COLOR = ".tabLineColor";
	private static final String THEME_ICON_ROW_HEADER_INHERITS_GUTTER_BG = ".iconRowHeaderInheritsGutterBG";
	private static final String THEME_SELECTION_ROUNDED_EDGES = ".selectionRoundedEdges";
	private static final String THEME_LINE_NUMBER_COLOR = ".lineNumberColor";
	private static final String THEME_LINE_NUMBER_FONT = ".lineNumberFont";
	private static final String THEME_LINE_NUMBER_FONT_SIZE = ".lineNumberFontSize";
	private static final String THEME_FOLD_INDICATOR_FG = ".foldIndicatorFG";
	private static final String THEME_FOLD_BG = ".foldBG";
	private static final String THEME_ARMED_FOLD_BG = ".armedFoldBG";
	
    private static final String WINDOW_X = ".window.x";
    private static final String WINDOW_Y = ".window.y";
    private static final String WINDOW_WIDTH = ".window.width";
    private static final String WINDOW_HEIGHT = ".window.height";
    private static final String WINDOW_MAXIMIZED = ".window.max";

    private File propertiesFile;
    private Logger logger;
    private Properties properties;
	
    /**
     * 
     * @param propertiesFilePath
     * @param logger
     */
	public DoomToolsSettings(File propertiesFilePath, Logger logger)
	{
		this.propertiesFile = propertiesFilePath;
		this.properties = new Properties();
		this.logger = logger;
		loadProperties();
	}
	
	/**
	 * Fetches a file relative to the settings path.
	 * NOTE: The file/dir may not actually exist!
	 * @param path the desired path.
	 * @return a File representing that new path.
	 */
	protected static File getConfigFile(String path)
	{
		return new File(Paths.APPDATA_PATH + path);
	}
	
	private void loadProperties()
	{
		try (FileInputStream fis = new FileInputStream(propertiesFile)) 
		{
			properties.load(fis);
			logger.infof("Loaded settings from %s", propertiesFile.getPath());
		}
		catch (FileNotFoundException e) 
		{
			logger.warnf("Could not load settings file from %s", propertiesFile.getPath());
		} 
		catch (IOException e) 
		{
			logger.errorf(e, "Could not load settings file from %s", propertiesFile.getPath());
		}
	}
	
	/**
	 * Saves the settings to storage.
	 */
	protected void commit()
	{
		if (!FileUtils.createPathForFile(propertiesFile))
			return;
		
		try (FileOutputStream fos = new FileOutputStream(propertiesFile))
		{
			properties.store(fos, "Created by DoomTools " + Version.DOOMTOOLS);
			logger.infof("Saved settings to %s.", propertiesFile.getPath());
		} 
		catch (FileNotFoundException e) 
		{
			logger.errorf(e, "Could not write settings to %s", propertiesFile.getPath());
		} 
		catch (IOException e) 
		{
			logger.errorf(e, "Could not write settings to %s", propertiesFile.getPath());
		}
	}

	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setString(String keyName, String value)
	{
		properties.setProperty(keyName, value);
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected String getString(String keyName, String defaultValue)
	{
		return properties.getProperty(keyName, defaultValue);
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected String getString(String keyName)
	{
		return getString(keyName, null);
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setInteger(String keyName, int value)
	{
		properties.setProperty(keyName, String.valueOf(value));
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected int getInteger(String keyName, int defaultValue)
	{
		String value = getString(keyName);
		return ValueUtils.parseInt(value, defaultValue);
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected Integer getInteger(String keyName)
	{
		return ValueUtils.parseInt(getString(keyName));
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setBoolean(String keyName, boolean value)
	{
		properties.setProperty(keyName, String.valueOf(value));
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected boolean getBoolean(String keyName, boolean defaultValue)
	{
		String value = getString(keyName);
		return ValueUtils.parseBoolean(value, defaultValue);
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected Boolean getBoolean(String keyName)
	{
		return ValueUtils.parseBoolean(getString(keyName));
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setFile(String keyName, File value)
	{
		properties.setProperty(keyName, value != null ? value.getAbsolutePath() : "");
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected File getFile(String keyName, File defaultValue)
	{
		String value = getString(keyName);
		return value != null && value.trim().length() > 0 ? new File(value) : defaultValue;
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected File getFile(String keyName)
	{
		return getFile(keyName, null);
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setColor(String keyName, Color value)
	{
		if (value == null)
		{
			properties.setProperty(keyName, "");
		}
		else
		{
	        StringBuilder sb = new StringBuilder(Integer.toHexString(value.getRGB() & 0xffffffff));
			while (sb.length() < 8) 
	            sb.insert(0, "0");
			properties.setProperty(keyName, sb.toString());
		}
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected Color getColor(String keyName, Color defaultValue)
	{
		String value = getString(keyName);
		
		if (value != null && value.trim().length() > 0)
		{
			try {
				long argb = Long.parseLong(value, 16);
				return new Color(
					(int)((argb & 0x000ff0000L) >> 16),
					(int)((argb & 0x00000ff00L) >> 8),
					(int)(argb & 0x0000000ffL),
					(int)((argb & 0x0ff000000L) >> 24)
				);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		else
		{
			return defaultValue;
		}
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected Color getColor(String keyName)
	{
		return getColor(keyName, null);
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setFont(String keyName, Font value)
	{
		if (value != null)
			properties.setProperty(keyName, value.getName() + ":" + value.getStyle() + ":" + value.getSize());
		else
			properties.setProperty(keyName, "");
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected Font getFont(String keyName, Font defaultValue)
	{
		String value = getString(keyName);
		if (value != null && value.trim().length() > 0)
		{
			String[] segs = value.split(":");
			String name = segs[0];
			int style = ValueUtils.parseInt(segs[1], Font.PLAIN);
			int size = ValueUtils.parseInt(segs[2], 12);
			return new Font(name, style, size);
		}
		return defaultValue;
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected Font getFont(String keyName)
	{
		return getFont(keyName, null);
	}
		
	/**
	 * Sets a value.
	 * @param keyName the key name.
	 * @param value the value.
	 */
	protected void setKeyStroke(String keyName, KeyStroke value)
	{
		properties.setProperty(keyName, value != null ? value.toString() : "");
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @param defaultValue the default value if not found.
	 * @return the corresponding value, or the default value if not found.
	 */
	protected KeyStroke getKeyStroke(String keyName, KeyStroke defaultValue)
	{
		String value = getString(keyName);
		if (value != null && value.trim().length() > 0)
			return KeyStroke.getKeyStroke(value);
		return defaultValue;
	}
	
	/**
	 * Gets a value.
	 * @param keyName the key name.
	 * @return the corresponding value.
	 */
	protected KeyStroke getKeyStroke(String keyName)
	{
		return getKeyStroke(keyName, null);
	}
		
	/**
	 * Sets window bounds.
	 * @param keyName an associated key.
	 * @param window the window to get size from.
	 */
	protected void setFrameBounds(String keyName, Frame window)
	{
		setFrameBounds(
			keyName, 
			window.getX(), 
			window.getY(), 
			window.getWidth(), 
			window.getHeight(), 
			(window.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0
		);
	}
	
	/**
	 * Sets window bounds.
	 * @param keyName an associated key.
	 * @param x 
	 * @param y 
	 * @param width 
	 * @param height 
	 * @param maximized if window was maximized
	 */
	protected void setFrameBounds(String keyName, int x, int y, int width, int height, boolean maximized) 
	{
		properties.setProperty(keyName + WINDOW_X, String.valueOf(x));
		properties.setProperty(keyName + WINDOW_Y, String.valueOf(y));
		properties.setProperty(keyName + WINDOW_WIDTH, String.valueOf(width));
		properties.setProperty(keyName + WINDOW_HEIGHT, String.valueOf(height));
		properties.setProperty(keyName + WINDOW_MAXIMIZED, String.valueOf(maximized));
	}

	/**
	 * Gets window bounds.
	 * @param keyName an associated key.
	 * @return the bounds.
	 */
	protected Rectangle getFrameBounds(String keyName)
	{
		return new Rectangle(
			ValueUtils.parseInt(properties.getProperty(keyName + WINDOW_X), 0),
			ValueUtils.parseInt(properties.getProperty(keyName + WINDOW_Y), 0),
			ValueUtils.parseInt(properties.getProperty(keyName + WINDOW_WIDTH), 720),
			ValueUtils.parseInt(properties.getProperty(keyName + WINDOW_HEIGHT), 480)
		);
	}
	
	/**
	 * @param keyName an associated key.
	 * @return if the main DoomTools window should be maximized.
	 */
	protected boolean getFrameMaximized(String keyName)
	{
		return ValueUtils.parseBoolean(properties.getProperty(keyName + WINDOW_MAXIMIZED), false);
	}
	
	/**
	 * Saves a set of editor view settings.
	 * @param subsetName the editor sub-set name.
	 * @param viewSettings the settings object.
	 */
	protected void setEditorViewSettings(String subsetName, EditorViewSettings viewSettings)
	{
		String prefix = subsetName + EDITOR_VIEW;
		setInteger(prefix + VIEW_TABSIZE, viewSettings.getTabSize());
		setBoolean(prefix + VIEW_SOFTTABS, viewSettings.isTabsEmulated());
		setBoolean(prefix + VIEW_WRAPPING, viewSettings.isLineWrap());
		setBoolean(prefix + VIEW_WRAPWORDS, viewSettings.isWrapStyleWord());
	}
	
	/**
	 * Gets a set of editor view settings.
	 * @param subsetName the editor sub-set name.
	 * @return the settings object.
	 */
	protected EditorViewSettings getEditorViewSettings(String subsetName)
	{
		EditorViewSettings out = new EditorViewSettings();
		String prefix = subsetName + EDITOR_VIEW;
		out.setTabSize(getInteger(prefix + VIEW_TABSIZE, out.getTabSize()));
		out.setTabsEmulated(getBoolean(prefix + VIEW_SOFTTABS, out.isTabsEmulated()));
		out.setLineWrap(getBoolean(prefix + VIEW_WRAPPING, out.isLineWrap()));
		out.setWrapStyleWord(getBoolean(prefix + VIEW_WRAPWORDS, out.isWrapStyleWord()));
		return out;
	}
	
	/**
	 * Saves a set of editor code settings.
	 * @param subsetName the editor sub-set name.
	 * @param codeSettings the settings object.
	 */
	protected void setEditorCodeSettings(String subsetName, EditorCodeSettings codeSettings)
	{
		String prefix = subsetName + EDITOR_CODE;
		setBoolean(prefix + CODE_MARGIN_LINE_ENABLED, codeSettings.isMarginLineEnabled());
		setInteger(prefix + CODE_MARGIN_LINE_POSITION, codeSettings.getMarginLinePosition());
		setBoolean(prefix + CODE_ROUNDED_SELECTION_EDGES, codeSettings.isRoundedSelectionEdges());
		setBoolean(prefix + CODE_HIGHLIGHT_CURRENT_LINE, codeSettings.isHighlightCurrentLine());
		setBoolean(prefix + CODE_ANIMATE_BRACKET_MATCHING, codeSettings.isAnimateBracketMatching());
		setBoolean(prefix + CODE_AUTO_INDENT_ENABLED, codeSettings.isAutoIndentEnabled());
		setBoolean(prefix + CODE_BRACKET_MATCHING_ENABLED, codeSettings.isBracketMatchingEnabled());
		setBoolean(prefix + CODE_CLEAR_WHITESPACE_LINES_ENABLED, codeSettings.isClearWhitespaceLinesEnabled());
		setBoolean(prefix + CODE_CLOSE_CURLY_BRACES, codeSettings.isCloseCurlyBraces());
		setBoolean(prefix + CODE_CLOSE_MARKUP_TAGS, codeSettings.isCloseMarkupTags());
		setBoolean(prefix + CODE_FOLDING_ENABLED, codeSettings.isCodeFoldingEnabled());
		setBoolean(prefix + CODE_EOL_MARKERS_VISIBLE, codeSettings.isEOLMarkersVisible());
		setBoolean(prefix + CODE_HIGHLIGHT_SECONDARY_LANGUAGES, codeSettings.isHighlightSecondaryLanguages());
		setBoolean(prefix + CODE_SHOW_MATCHED_BRACKET_POPUP, codeSettings.isShowMatchedBracketPopup());
		setBoolean(prefix + CODE_USE_FOCUSABLE_TIPS, codeSettings.isUseFocusableTips());
		setBoolean(prefix + CODE_WHITESPACE_VISIBLE, codeSettings.isWhitespaceVisible());
		setBoolean(prefix + CODE_PAINT_TAB_LINES, codeSettings.isPaintTabLines());
		setBoolean(prefix + CODE_MARK_OCCURRENCES, codeSettings.isMarkOccurrences());
		setBoolean(prefix + CODE_MARK_ALL_ON_OCCURRENCE_SEARCHES, codeSettings.isMarkAllOnOccurrenceSearches());
		setInteger(prefix + CODE_MARK_OCCURRENCES_DELAY, codeSettings.getMarkOccurrencesDelay());
		setBoolean(prefix + CODE_PAINT_MATCHED_BRACKET_PAIR, codeSettings.isPaintMatchedBracketPair());
		setBoolean(prefix + CODE_PAINT_MARK_OCCURRENCES_BORDER, codeSettings.isPaintMarkOccurrencesBorder());
		setBoolean(prefix + CODE_USE_SELECTED_TEXT_COLOR, codeSettings.isUseSelectedTextColor());
		setInteger(prefix + CODE_PARSER_DELAY, codeSettings.getParserDelay());
		setBoolean(prefix + CODE_HYPERLINKS_ENABLED, codeSettings.isHyperlinksEnabled());
		setInteger(prefix + CODE_LINK_SCANNING_MASK, codeSettings.getLinkScanningMask());
	}
	
	/**
	 * Gets a set of editor code settings.
	 * @param subsetName the editor sub-set name.
	 * @return the settings object.
	 */
	protected EditorCodeSettings getEditorCodeSettings(String subsetName)
	{
		EditorCodeSettings out = new EditorCodeSettings();
		String prefix = subsetName + EDITOR_CODE;
		out.setMarginLineEnabled(getBoolean(prefix + CODE_MARGIN_LINE_ENABLED, out.isMarginLineEnabled()));
		out.setMarginLinePosition(getInteger(prefix + CODE_MARGIN_LINE_POSITION, out.getMarginLinePosition()));
		out.setRoundedSelectionEdges(getBoolean(prefix + CODE_ROUNDED_SELECTION_EDGES, out.isRoundedSelectionEdges()));
		out.setHighlightCurrentLine(getBoolean(prefix + CODE_HIGHLIGHT_CURRENT_LINE, out.isHighlightCurrentLine()));
		out.setAnimateBracketMatching(getBoolean(prefix + CODE_ANIMATE_BRACKET_MATCHING, out.isAnimateBracketMatching()));
		out.setAutoIndentEnabled(getBoolean(prefix + CODE_AUTO_INDENT_ENABLED, out.isAutoIndentEnabled()));
		out.setBracketMatchingEnabled(getBoolean(prefix + CODE_BRACKET_MATCHING_ENABLED, out.isBracketMatchingEnabled()));
		out.setClearWhitespaceLinesEnabled(getBoolean(prefix + CODE_CLEAR_WHITESPACE_LINES_ENABLED, out.isClearWhitespaceLinesEnabled()));
		out.setCloseCurlyBraces(getBoolean(prefix + CODE_CLOSE_CURLY_BRACES, out.isCloseCurlyBraces()));
		out.setCloseMarkupTags(getBoolean(prefix + CODE_CLOSE_MARKUP_TAGS, out.isCloseMarkupTags()));
		out.setCodeFoldingEnabled(getBoolean(prefix + CODE_FOLDING_ENABLED, out.isCodeFoldingEnabled()));
		out.setEOLMarkersVisible(getBoolean(prefix + CODE_EOL_MARKERS_VISIBLE, out.isEOLMarkersVisible()));
		out.setHighlightSecondaryLanguages(getBoolean(prefix + CODE_HIGHLIGHT_SECONDARY_LANGUAGES, out.isHighlightSecondaryLanguages()));
		out.setShowMatchedBracketPopup(getBoolean(prefix + CODE_SHOW_MATCHED_BRACKET_POPUP, out.isShowMatchedBracketPopup()));
		out.setUseFocusableTips(getBoolean(prefix + CODE_USE_FOCUSABLE_TIPS, out.isUseFocusableTips()));
		out.setWhitespaceVisible(getBoolean(prefix + CODE_WHITESPACE_VISIBLE, out.isWhitespaceVisible()));
		out.setPaintTabLines(getBoolean(prefix + CODE_PAINT_TAB_LINES, out.isPaintTabLines()));
		out.setMarkOccurrences(getBoolean(prefix + CODE_MARK_OCCURRENCES, out.isMarkOccurrences()));
		out.setMarkAllOnOccurrenceSearches(getBoolean(prefix + CODE_MARK_ALL_ON_OCCURRENCE_SEARCHES, out.isMarkAllOnOccurrenceSearches()));
		out.setMarkOccurrencesDelay(getInteger(prefix + CODE_MARK_OCCURRENCES_DELAY, out.getMarkOccurrencesDelay()));
		out.setPaintMatchedBracketPair(getBoolean(prefix + CODE_PAINT_MATCHED_BRACKET_PAIR, out.isPaintMatchedBracketPair()));
		out.setPaintMarkOccurrencesBorder(getBoolean(prefix + CODE_PAINT_MARK_OCCURRENCES_BORDER, out.isPaintMarkOccurrencesBorder()));
		out.setUseSelectedTextColor(getBoolean(prefix + CODE_USE_SELECTED_TEXT_COLOR, out.isUseSelectedTextColor()));
		out.setParserDelay(getInteger(prefix + CODE_PARSER_DELAY, out.getParserDelay()));
		out.setHyperlinksEnabled(getBoolean(prefix + CODE_HYPERLINKS_ENABLED, out.isHyperlinksEnabled()));
		out.setLinkScanningMask(getInteger(prefix + CODE_LINK_SCANNING_MASK, out.getLinkScanningMask()));
		return out;
	}
	
	/**
	 * Saves a set of editor auto-complete settings.
	 * @param subsetName the editor sub-set name.
	 * @param codeSettings the settings object.
	 */
	protected void setEditorAutoCompleteSettings(String subsetName, EditorAutoCompleteSettings codeSettings)
	{
		String prefix = subsetName + EDITOR_AUTOCOMPLETE;
		setInteger(prefix + AUTOCOMPLETE_CHOICES_WINDOW_SIZE_WIDTH, codeSettings.getChoicesWindowSizeWidth());
		setInteger(prefix + AUTOCOMPLETE_CHOICES_WINDOW_SIZE_HEIGHT, codeSettings.getChoicesWindowSizeHeight());
		setInteger(prefix + AUTOCOMPLETE_DESCRIPTION_WINDOW_SIZE_WIDTH, codeSettings.getDescriptionWindowSizeWidth());
		setInteger(prefix + AUTOCOMPLETE_DESCRIPTION_WINDOW_SIZE_HEIGHT, codeSettings.getDescriptionWindowSizeHeight());
		setKeyStroke(prefix + AUTOCOMPLETE_TRIGGER_KEY, codeSettings.getTriggerKey());
		setBoolean(prefix + AUTOCOMPLETE_AUTO_COMPLETE_ENABLED, codeSettings.isAutoCompleteEnabled());
		setBoolean(prefix + AUTOCOMPLETE_AUTO_COMPLETE_SINGLE_CHOICES, codeSettings.isAutoCompleteSingleChoices());
		setBoolean(prefix + AUTOCOMPLETE_AUTO_ACTIVATION_ENABLED, codeSettings.isAutoActivationEnabled());
		setBoolean(prefix + AUTOCOMPLETE_SHOW_DESC_WINDOW, codeSettings.isShowDescWindow());
		setInteger(prefix + AUTOCOMPLETE_PARAMETER_DESCRIPTION_TRUNCATE_THRESHOLD, codeSettings.getParameterDescriptionTruncateThreshold());
	}
	
	/**
	 * Gets a set of editor auto-complete settings.
	 * @param subsetName the editor sub-set name.
	 * @return the settings object.
	 */
	protected EditorAutoCompleteSettings getEditorAutoCompleteSettings(String subsetName)
	{
		EditorAutoCompleteSettings out = new EditorAutoCompleteSettings();
		String prefix = subsetName + EDITOR_AUTOCOMPLETE;
		out.setChoicesWindowSizeWidth(getInteger(prefix + AUTOCOMPLETE_CHOICES_WINDOW_SIZE_WIDTH, out.getChoicesWindowSizeWidth()));
		out.setChoicesWindowSizeHeight(getInteger(prefix + AUTOCOMPLETE_CHOICES_WINDOW_SIZE_HEIGHT, out.getChoicesWindowSizeHeight()));
		out.setDescriptionWindowSizeWidth(getInteger(prefix + AUTOCOMPLETE_DESCRIPTION_WINDOW_SIZE_WIDTH, out.getDescriptionWindowSizeWidth()));
		out.setDescriptionWindowSizeHeight(getInteger(prefix + AUTOCOMPLETE_DESCRIPTION_WINDOW_SIZE_HEIGHT, out.getDescriptionWindowSizeHeight()));
		out.setTriggerKey(getKeyStroke(prefix + AUTOCOMPLETE_TRIGGER_KEY, out.getTriggerKey()));
		out.setAutoCompleteEnabled(getBoolean(prefix + AUTOCOMPLETE_AUTO_COMPLETE_ENABLED, out.isAutoCompleteEnabled()));
		out.setAutoCompleteSingleChoices(getBoolean(prefix + AUTOCOMPLETE_AUTO_COMPLETE_SINGLE_CHOICES, out.isAutoCompleteSingleChoices()));
		out.setAutoActivationEnabled(getBoolean(prefix + AUTOCOMPLETE_AUTO_ACTIVATION_ENABLED, out.isAutoActivationEnabled()));
		out.setShowDescWindow(getBoolean(prefix + AUTOCOMPLETE_SHOW_DESC_WINDOW, out.isShowDescWindow()));
		out.setParameterDescriptionTruncateThreshold(getInteger(prefix + AUTOCOMPLETE_PARAMETER_DESCRIPTION_TRUNCATE_THRESHOLD, out.getParameterDescriptionTruncateThreshold()));
		return out;
	}
	
	/**
	 * Sets an editor theme's settings.
	 * @param subsetName the editor sub-set name.
	 * @param editorTheme the theme to store.
	 */
	protected void setEditorTheme(String subsetName, Theme editorTheme)
	{
		String prefix = subsetName + EDITOR_THEME;
		setFont(prefix + THEME_BASE_FONT, editorTheme.baseFont);
		setColor(prefix + THEME_BG_COLOR, editorTheme.bgColor);
		setColor(prefix + THEME_CARET_COLOR, editorTheme.caretColor);
		setBoolean(prefix + THEME_USE_SELECTION_FG, editorTheme.useSelectionFG);
		setColor(prefix + THEME_SELECTION_FG, editorTheme.selectionFG);
		setColor(prefix + THEME_SELECTION_BG, editorTheme.selectionBG);
		setBoolean(prefix + THEME_SELECTION_ROUNDED_EDGES, editorTheme.selectionRoundedEdges);
		setColor(prefix + THEME_CURRENT_LINE_HIGHLIGHT, editorTheme.currentLineHighlight);
		setBoolean(prefix + THEME_FADE_CURRENT_LINE_HIGHLIGHT, editorTheme.fadeCurrentLineHighlight);
		setColor(prefix + THEME_TAB_LINE_COLOR, editorTheme.tabLineColor);
		setColor(prefix + THEME_MARGIN_LINE_COLOR, editorTheme.marginLineColor);
		setColor(prefix + THEME_MARK_ALL_HIGHLIGHT_COLOR, editorTheme.markAllHighlightColor);
		setColor(prefix + THEME_MARK_OCCURRENCES_COLOR, editorTheme.markOccurrencesColor);
		setBoolean(prefix + THEME_MARK_OCCURRENCES_BORDER, editorTheme.markOccurrencesBorder);
		setColor(prefix + THEME_MATCHED_BRACKET_FG, editorTheme.matchedBracketFG);
		setColor(prefix + THEME_MATCHED_BRACKET_BG, editorTheme.matchedBracketBG);
		setBoolean(prefix + THEME_MATCHED_BRACKET_HIGHLIGHT_BOTH, editorTheme.matchedBracketHighlightBoth);
		setBoolean(prefix + THEME_MATCHED_BRACKET_ANIMATE, editorTheme.matchedBracketAnimate);
		setColor(prefix + THEME_HYPERLINK_FG, editorTheme.hyperlinkFG);
		
		for (int i = 0; i < editorTheme.secondaryLanguages.length; i++)
			setColor(prefix + THEME_SECONDARY_LANGUAGES + i, editorTheme.secondaryLanguages[i]);
		
		setColor(prefix + THEME_GUTTER_BACKGROUND_COLOR, editorTheme.gutterBackgroundColor);
		setColor(prefix + THEME_GUTTER_BORDER_COLOR, editorTheme.gutterBorderColor);
		setColor(prefix + THEME_ACTIVE_LINE_RANGE_COLOR, editorTheme.activeLineRangeColor);
		setBoolean(prefix + THEME_ICON_ROW_HEADER_INHERITS_GUTTER_BG, editorTheme.iconRowHeaderInheritsGutterBG);
		setColor(prefix + THEME_LINE_NUMBER_COLOR, editorTheme.lineNumberColor);
		setColor(prefix + THEME_CURRENT_LINE_NUMBER_COLOR, editorTheme.currentLineNumberColor);
		setString(prefix + THEME_LINE_NUMBER_FONT, editorTheme.lineNumberFont);
		setInteger(prefix + THEME_LINE_NUMBER_FONT_SIZE, editorTheme.lineNumberFontSize);
		setColor(prefix + THEME_FOLD_INDICATOR_FG, editorTheme.foldIndicatorFG);
		setColor(prefix + THEME_FOLD_BG, editorTheme.foldBG);
		setColor(prefix + THEME_ARMED_FOLD_BG, editorTheme.armedFoldBG);
	}
	
	/**
	 * Sets an editor theme's settings.
	 * @param targetTheme the target theme.
	 * @param subsetName the editor sub-set name.
	 * @return the corresponding theme, or null if no corresponding theme.
	 */
	protected Theme getEditorTheme(Theme targetTheme, String subsetName)
	{
		String prefix = subsetName + EDITOR_THEME;
		targetTheme.baseFont = getFont(prefix + THEME_BASE_FONT, targetTheme.baseFont);
		targetTheme.bgColor = getColor(prefix + THEME_BG_COLOR, targetTheme.bgColor);
		targetTheme.caretColor = getColor(prefix + THEME_CARET_COLOR, targetTheme.caretColor);
		targetTheme.useSelectionFG = getBoolean(prefix + THEME_USE_SELECTION_FG, targetTheme.useSelectionFG);
		targetTheme.selectionFG = getColor(prefix + THEME_SELECTION_FG, targetTheme.selectionFG);
		targetTheme.selectionBG = getColor(prefix + THEME_SELECTION_BG, targetTheme.selectionBG);
		targetTheme.selectionRoundedEdges = getBoolean(prefix + THEME_SELECTION_ROUNDED_EDGES, targetTheme.selectionRoundedEdges);
		targetTheme.currentLineHighlight = getColor(prefix + THEME_CURRENT_LINE_HIGHLIGHT, targetTheme.currentLineHighlight);
		targetTheme.fadeCurrentLineHighlight = getBoolean(prefix + THEME_FADE_CURRENT_LINE_HIGHLIGHT, targetTheme.fadeCurrentLineHighlight);
		targetTheme.tabLineColor = getColor(prefix + THEME_TAB_LINE_COLOR, targetTheme.tabLineColor);
		targetTheme.marginLineColor = getColor(prefix + THEME_MARGIN_LINE_COLOR, targetTheme.marginLineColor);
		targetTheme.markAllHighlightColor = getColor(prefix + THEME_MARK_ALL_HIGHLIGHT_COLOR, targetTheme.markAllHighlightColor);
		targetTheme.markOccurrencesColor = getColor(prefix + THEME_MARK_OCCURRENCES_COLOR, targetTheme.markOccurrencesColor);
		targetTheme.markOccurrencesBorder = getBoolean(prefix + THEME_MARK_OCCURRENCES_BORDER, targetTheme.markOccurrencesBorder);
		targetTheme.matchedBracketFG = getColor(prefix + THEME_MATCHED_BRACKET_FG, targetTheme.matchedBracketFG);
		targetTheme.matchedBracketBG = getColor(prefix + THEME_MATCHED_BRACKET_BG, targetTheme.matchedBracketBG);
		targetTheme.matchedBracketHighlightBoth = getBoolean(prefix + THEME_MATCHED_BRACKET_HIGHLIGHT_BOTH, targetTheme.matchedBracketHighlightBoth);
		targetTheme.matchedBracketAnimate = getBoolean(prefix + THEME_MATCHED_BRACKET_ANIMATE, targetTheme.matchedBracketAnimate);
		targetTheme.hyperlinkFG = getColor(prefix + THEME_HYPERLINK_FG, targetTheme.hyperlinkFG);
		
		for (int i = 0; i < targetTheme.secondaryLanguages.length; i++)
			targetTheme.secondaryLanguages[i] = getColor(prefix + THEME_SECONDARY_LANGUAGES + i, targetTheme.secondaryLanguages[i]);
		
		targetTheme.gutterBackgroundColor = getColor(prefix + THEME_GUTTER_BACKGROUND_COLOR, targetTheme.gutterBackgroundColor);
		targetTheme.gutterBorderColor = getColor(prefix + THEME_GUTTER_BORDER_COLOR, targetTheme.gutterBorderColor);
		targetTheme.activeLineRangeColor = getColor(prefix + THEME_ACTIVE_LINE_RANGE_COLOR, targetTheme.activeLineRangeColor);
		targetTheme.iconRowHeaderInheritsGutterBG = getBoolean(prefix + THEME_ICON_ROW_HEADER_INHERITS_GUTTER_BG, targetTheme.iconRowHeaderInheritsGutterBG);
		targetTheme.lineNumberColor = getColor(prefix + THEME_LINE_NUMBER_COLOR, targetTheme.lineNumberColor);
		targetTheme.currentLineNumberColor = getColor(prefix + THEME_CURRENT_LINE_NUMBER_COLOR, targetTheme.currentLineNumberColor);
		targetTheme.lineNumberFont = getString(prefix + THEME_LINE_NUMBER_FONT, targetTheme.lineNumberFont);
		targetTheme.lineNumberFontSize = getInteger(prefix + THEME_LINE_NUMBER_FONT_SIZE, targetTheme.lineNumberFontSize);
		targetTheme.foldIndicatorFG = getColor(prefix + THEME_FOLD_INDICATOR_FG, targetTheme.foldIndicatorFG);
		targetTheme.foldBG = getColor(prefix + THEME_FOLD_BG, targetTheme.foldBG);
		targetTheme.armedFoldBG = getColor(prefix + THEME_ARMED_FOLD_BG, targetTheme.armedFoldBG);
		return targetTheme;
	}
	
}
