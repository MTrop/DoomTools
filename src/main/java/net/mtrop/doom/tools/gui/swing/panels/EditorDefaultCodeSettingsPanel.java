package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.EditorSettingsManager;
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
	
	private DoomToolsLanguageManager language;
	private EditorSettingsManager settings;
	
	private EditorCodeSettings codeSettings;
	
	/**
	 * Creates a new panel.
	 */
	public EditorDefaultCodeSettingsPanel()
	{
		this.language = DoomToolsLanguageManager.get();
		this.settings = EditorSettingsManager.get();
		
		this.codeSettings = settings.getDefaultEditorCodeSettings();

		containerOf(this, borderLayout(),
			node(BorderLayout.CENTER, form(language.getInteger("texteditor.settings.label.width", 180))
				.addField(language.getText("texteditor.settings.code.marginline"), checkBoxField(checkBox(codeSettings.isMarginLineEnabled())))
				.addField(language.getText("texteditor.settings.code.marginlinepos"), integerField(codeSettings.getMarginLinePosition()))
				.addField(language.getText("texteditor.settings.code.roundedselection"), checkBoxField(checkBox(codeSettings.isRoundedSelectionEdges())))
				.addField(language.getText("texteditor.settings.code.hilitecurline"), checkBoxField(checkBox(codeSettings.isHighlightCurrentLine())))
				.addField(language.getText("texteditor.settings.code.autoindent"), checkBoxField(checkBox(codeSettings.isAutoIndentEnabled())))
				.addField(language.getText("texteditor.settings.code.bracketmatch"), checkBoxField(checkBox(codeSettings.isBracketMatchingEnabled())))
				.addField(language.getText("texteditor.settings.code.animbracketmatch"), checkBoxField(checkBox(codeSettings.isAnimateBracketMatching())))
				.addField(language.getText("texteditor.settings.code.showmatchedbracketpopup"), checkBoxField(checkBox(codeSettings.isShowMatchedBracketPopup())))
				.addField(language.getText("texteditor.settings.code.paintmatchedbracketpair"), checkBoxField(checkBox(codeSettings.isPaintMatchedBracketPair())))
				.addField(language.getText("texteditor.settings.code.codefolding"), checkBoxField(checkBox(codeSettings.isCodeFoldingEnabled())))
				.addField(language.getText("texteditor.settings.code.closecurlies"), checkBoxField(checkBox(codeSettings.isCloseCurlyBraces())))
				.addField(language.getText("texteditor.settings.code.closemarkuptags"), checkBoxField(checkBox(codeSettings.isCloseMarkupTags())))
				.addField(language.getText("texteditor.settings.code.eolvisible"), checkBoxField(checkBox(codeSettings.isEOLMarkersVisible())))
				.addField(language.getText("texteditor.settings.code.hilitesecondarylang"), checkBoxField(checkBox(codeSettings.isHighlightSecondaryLanguages())))
				.addField(language.getText("texteditor.settings.code.focustips"), checkBoxField(checkBox(codeSettings.isUseFocusableTips())))
				.addField(language.getText("texteditor.settings.code.clearwhitespace"), checkBoxField(checkBox(codeSettings.isClearWhitespaceLinesEnabled())))
				.addField(language.getText("texteditor.settings.code.whitespacevisible"), checkBoxField(checkBox(codeSettings.isWhitespaceVisible())))
				.addField(language.getText("texteditor.settings.code.painttablines"), checkBoxField(checkBox(codeSettings.isPaintTabLines())))
				.addField(language.getText("texteditor.settings.code.markoccur"), checkBoxField(checkBox(codeSettings.isMarkOccurrences())))
				.addField(language.getText("texteditor.settings.code.markallonsearches"), checkBoxField(checkBox(codeSettings.isMarkAllOnOccurrenceSearches())))
				.addField(language.getText("texteditor.settings.code.markoccurdelay"), integerField(codeSettings.getMarkOccurrencesDelay()))
				.addField(language.getText("texteditor.settings.code.paintmarkoccurborder"), checkBoxField(checkBox(codeSettings.isPaintMarkOccurrencesBorder())))
				.addField(language.getText("texteditor.settings.code.useselectedtextcolor"), checkBoxField(checkBox(codeSettings.isUseSelectedTextColor())))
				.addField(language.getText("texteditor.settings.code.parserdelay"), integerField(codeSettings.getParserDelay()))
				.addField(language.getText("texteditor.settings.code.hyperlinksenabled"), checkBoxField(checkBox(codeSettings.isHyperlinksEnabled())))
				//.addField(language.getText("texteditor.settings.code."), integerField(codeSettings.getLinkScanningMask()))
			)
		);
	}

	/**
	 * Commits the current settings state to storage.
	 */
	public void commitSettings()
	{
		settings.setDefaultEditorCodeSettings(codeSettings);
	}
	
}
