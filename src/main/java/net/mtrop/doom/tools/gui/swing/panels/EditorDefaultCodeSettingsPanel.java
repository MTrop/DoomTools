package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.EditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel.EditorCodeSettings;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;

/**
 * A panel that manages the default editor code settings. 
 * @author Matthew Tropiano
 */
public class EditorDefaultCodeSettingsPanel extends JPanel 
{
	private static final long serialVersionUID = -1887947424844741478L;
	
	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private EditorSettingsManager settings;
	
	private EditorCodeSettings codeSettings;
	private EditorCodeSettings defaultSettings;
	
	private JFormField<Boolean> marginLineField;
	private JFormField<Integer> marginLinePositionField;
	private JFormField<Boolean> roundedSelectionEdgesField;
	private JFormField<Boolean> highlightCurrentLineField;
	private JFormField<Boolean> autoIndentEnabledField;
	private JFormField<Boolean> bracketMatchingEnabledField;
	private JFormField<Boolean> animateBracketMatchingField;
	private JFormField<Boolean> showMatchedBracketPopupField;
	private JFormField<Boolean> paintMatchedBracketPairField;
	private JFormField<Boolean> codeFoldingEnabledField;
	private JFormField<Boolean> closeCurlyBracesField;
	private JFormField<Boolean> closeMarkupTagsField;
	private JFormField<Boolean> eolMarkersVisibleField;
	private JFormField<Boolean> highlightSecondaryLanguagesField;
	private JFormField<Boolean> useFocusableTipsField;
	private JFormField<Boolean> clearWhitespaceLinesEnabledField;
	private JFormField<Boolean> whitespaceVisibleField;
	private JFormField<Boolean> paintTabLinesField;
	private JFormField<Boolean> markOccurrencesField;
	private JFormField<Boolean> markAllOnOccurrenceSearchesField;
	private JFormField<Integer> markOccurrencesDelayField;
	private JFormField<Boolean> paintMarkOccurrencesBorderField;
	private JFormField<Boolean> useSelectedTextColorField;
	private JFormField<Integer> parserDelayField;
	private JFormField<Boolean> hyperlinksEnabledField;
	
	
	/**
	 * Creates a new panel.
	 */
	public EditorDefaultCodeSettingsPanel()
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = EditorSettingsManager.get();
		
		this.codeSettings = settings.getDefaultEditorCodeSettings();
		this.defaultSettings = new EditorCodeSettings();

		this.marginLineField = checkBoxField(checkBox(codeSettings.isMarginLineEnabled(),
			(v) -> codeSettings.setMarginLineEnabled(v)
		));
		this.marginLinePositionField = integerField(codeSettings.getMarginLinePosition(),
			(v) -> codeSettings.setMarginLinePosition(v)
		);
		this.roundedSelectionEdgesField = checkBoxField(checkBox(codeSettings.isRoundedSelectionEdges(),
			(v) -> codeSettings.setRoundedSelectionEdges(v)
		));
		this.highlightCurrentLineField = checkBoxField(checkBox(codeSettings.isHighlightCurrentLine(),
			(v) -> codeSettings.setHighlightCurrentLine(v)
		));
		this.autoIndentEnabledField = checkBoxField(checkBox(codeSettings.isAutoIndentEnabled(),
			(v) -> codeSettings.setAutoIndentEnabled(v)
		));
		this.bracketMatchingEnabledField = checkBoxField(checkBox(codeSettings.isBracketMatchingEnabled(),
			(v) -> codeSettings.setBracketMatchingEnabled(v)
		));
		this.animateBracketMatchingField = checkBoxField(checkBox(codeSettings.isAnimateBracketMatching(),
			(v) -> codeSettings.setAnimateBracketMatching(v)
		));
		this.showMatchedBracketPopupField = checkBoxField(checkBox(codeSettings.isShowMatchedBracketPopup(),
			(v) -> codeSettings.setShowMatchedBracketPopup(v)
		));
		this.paintMatchedBracketPairField = checkBoxField(checkBox(codeSettings.isPaintMatchedBracketPair(),
			(v) -> codeSettings.setPaintMatchedBracketPair(v)
		));
		this.codeFoldingEnabledField = checkBoxField(checkBox(codeSettings.isCodeFoldingEnabled(),
			(v) -> codeSettings.setCodeFoldingEnabled(v)
		));
		this.closeCurlyBracesField = checkBoxField(checkBox(codeSettings.isCloseCurlyBraces(),
			(v) -> codeSettings.setCloseCurlyBraces(v)
		));
		this.closeMarkupTagsField = checkBoxField(checkBox(codeSettings.isCloseMarkupTags(),
			(v) -> codeSettings.setCloseMarkupTags(v)
		));
		this.eolMarkersVisibleField = checkBoxField(checkBox(codeSettings.isEOLMarkersVisible(),
			(v) -> codeSettings.setEOLMarkersVisible(v)
		));
		this.highlightSecondaryLanguagesField = checkBoxField(checkBox(codeSettings.isHighlightSecondaryLanguages(),
			(v) -> codeSettings.setHighlightSecondaryLanguages(v)
		));
		this.useFocusableTipsField = checkBoxField(checkBox(codeSettings.isUseFocusableTips(),
			(v) -> codeSettings.setUseFocusableTips(v)
		));
		this.clearWhitespaceLinesEnabledField = checkBoxField(checkBox(codeSettings.isClearWhitespaceLinesEnabled(),
			(v) -> codeSettings.setClearWhitespaceLinesEnabled(v)
		));
		this.whitespaceVisibleField = checkBoxField(checkBox(codeSettings.isWhitespaceVisible(),
			(v) -> codeSettings.setWhitespaceVisible(v)
		));
		this.paintTabLinesField = checkBoxField(checkBox(codeSettings.isPaintTabLines(),
			(v) -> codeSettings.setPaintTabLines(v)
		));
		this.markOccurrencesField = checkBoxField(checkBox(codeSettings.isMarkOccurrences(),
			(v) -> codeSettings.setMarkOccurrences(v)
		));
		this.markAllOnOccurrenceSearchesField = checkBoxField(checkBox(codeSettings.isMarkAllOnOccurrenceSearches(),
			(v) -> codeSettings.setMarkAllOnOccurrenceSearches(v)
		));
		this.markOccurrencesDelayField = integerField(codeSettings.getMarkOccurrencesDelay(),
			(v) -> codeSettings.setMarkOccurrencesDelay(v)
		);
		this.paintMarkOccurrencesBorderField = checkBoxField(checkBox(codeSettings.isPaintMarkOccurrencesBorder(),
			(v) -> codeSettings.setPaintMarkOccurrencesBorder(v)
		));
		this.useSelectedTextColorField = checkBoxField(checkBox(codeSettings.isUseSelectedTextColor(),
			(v) -> codeSettings.setUseSelectedTextColor(v)
		));
		this.parserDelayField = integerField(codeSettings.getParserDelay(),
			(v) -> codeSettings.setParserDelay(v)
		);
		this.hyperlinksEnabledField = checkBoxField(checkBox(codeSettings.isHyperlinksEnabled(),
			(v) -> codeSettings.setHyperlinksEnabled(v)
		));

		containerOf(this, borderLayout(),
			node(BorderLayout.CENTER, utils.createForm(form(language.getInteger("texteditor.settings.label.width", 180)), 
				utils.formField("texteditor.settings.code.marginline", marginLineField),
				utils.formField("texteditor.settings.code.marginlinepos", marginLinePositionField),
				utils.formField("texteditor.settings.code.roundedselection", roundedSelectionEdgesField),
				utils.formField("texteditor.settings.code.hilitecurline", highlightCurrentLineField),
				utils.formField("texteditor.settings.code.autoindent", autoIndentEnabledField),
				utils.formField("texteditor.settings.code.bracketmatch", bracketMatchingEnabledField),
				utils.formField("texteditor.settings.code.animbracketmatch", animateBracketMatchingField),
				utils.formField("texteditor.settings.code.showmatchedbracketpopup", showMatchedBracketPopupField),
				utils.formField("texteditor.settings.code.paintmatchedbracketpair", paintMatchedBracketPairField),
				utils.formField("texteditor.settings.code.codefolding", codeFoldingEnabledField),
				utils.formField("texteditor.settings.code.closecurlies", closeCurlyBracesField),
				utils.formField("texteditor.settings.code.closemarkuptags", closeMarkupTagsField),
				utils.formField("texteditor.settings.code.eolvisible", eolMarkersVisibleField),
				utils.formField("texteditor.settings.code.hilitesecondarylang", highlightSecondaryLanguagesField),
				utils.formField("texteditor.settings.code.focustips", useFocusableTipsField),
				utils.formField("texteditor.settings.code.clearwhitespace", clearWhitespaceLinesEnabledField),
				utils.formField("texteditor.settings.code.whitespacevisible", whitespaceVisibleField),
				utils.formField("texteditor.settings.code.painttablines", paintTabLinesField),
				utils.formField("texteditor.settings.code.markoccur", markOccurrencesField),
				utils.formField("texteditor.settings.code.markallonsearches", markAllOnOccurrenceSearchesField),
				utils.formField("texteditor.settings.code.markoccurdelay", markOccurrencesDelayField),
				utils.formField("texteditor.settings.code.paintmarkoccurborder", paintMarkOccurrencesBorderField),
				utils.formField("texteditor.settings.code.useselectedtextcolor", useSelectedTextColorField),
				utils.formField("texteditor.settings.code.parserdelay", parserDelayField),
				utils.formField("texteditor.settings.code.hyperlinksenabled", hyperlinksEnabledField)
				).addField(buttonField(button(language.getText("texteditor.settings.reset"), (b) -> resetSettings())))
			)
		);
	}

	/**
	 * Resets the settings.
	 */
	public void resetSettings()
	{
		marginLineField.setValue(defaultSettings.isMarginLineEnabled());
		marginLinePositionField.setValue(defaultSettings.getMarginLinePosition());
		roundedSelectionEdgesField.setValue(defaultSettings.isRoundedSelectionEdges());
		highlightCurrentLineField.setValue(defaultSettings.isHighlightCurrentLine());
		autoIndentEnabledField.setValue(defaultSettings.isAutoIndentEnabled());
		bracketMatchingEnabledField.setValue(defaultSettings.isBracketMatchingEnabled());
		animateBracketMatchingField.setValue(defaultSettings.isAnimateBracketMatching());
		showMatchedBracketPopupField.setValue(defaultSettings.isShowMatchedBracketPopup());
		paintMatchedBracketPairField.setValue(defaultSettings.isPaintMatchedBracketPair());
		codeFoldingEnabledField.setValue(defaultSettings.isCodeFoldingEnabled());
		closeCurlyBracesField.setValue(defaultSettings.isCloseCurlyBraces());
		closeMarkupTagsField.setValue(defaultSettings.isCloseMarkupTags());
		eolMarkersVisibleField.setValue(defaultSettings.isEOLMarkersVisible());
		highlightSecondaryLanguagesField.setValue(defaultSettings.isHighlightSecondaryLanguages());
		useFocusableTipsField.setValue(defaultSettings.isUseFocusableTips());
		clearWhitespaceLinesEnabledField.setValue(defaultSettings.isClearWhitespaceLinesEnabled());
		whitespaceVisibleField.setValue(defaultSettings.isWhitespaceVisible());
		paintTabLinesField.setValue(defaultSettings.isPaintTabLines());
		markOccurrencesField.setValue(defaultSettings.isMarkOccurrences());
		markAllOnOccurrenceSearchesField.setValue(defaultSettings.isMarkAllOnOccurrenceSearches());
		markOccurrencesDelayField.setValue(defaultSettings.getMarkOccurrencesDelay());
		paintMarkOccurrencesBorderField.setValue(defaultSettings.isPaintMarkOccurrencesBorder());
		useSelectedTextColorField.setValue(defaultSettings.isUseSelectedTextColor());
		parserDelayField.setValue(defaultSettings.getParserDelay());
		hyperlinksEnabledField.setValue(defaultSettings.isHyperlinksEnabled());
	}
	
	/**
	 * Commits the current settings state to storage.
	 */
	public void commitSettings()
	{
		settings.setDefaultEditorCodeSettings(codeSettings);
	}
	
}
