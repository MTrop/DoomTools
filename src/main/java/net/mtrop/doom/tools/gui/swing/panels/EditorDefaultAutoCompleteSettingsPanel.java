package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.EditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel.EditorAutoCompleteSettings;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;

/**
 * A panel that manages the default editor auto-complete settings. 
 * @author Matthew Tropiano
 */
public class EditorDefaultAutoCompleteSettingsPanel extends JPanel 
{
	private static final long serialVersionUID = -6173939721307568911L;
	
	private DoomToolsLanguageManager language;
	private EditorSettingsManager settings;
	
	private EditorAutoCompleteSettings autoCompleteSettings;
	
	/**
	 * Creates a new panel.
	 */
	public EditorDefaultAutoCompleteSettingsPanel()
	{
		this.language = DoomToolsLanguageManager.get();
		this.settings = EditorSettingsManager.get();
		
		this.autoCompleteSettings = settings.getDefaultEditorAutoCompleteSettings();

		containerOf(this, borderLayout(),
			node(BorderLayout.CENTER, form(language.getInteger("texteditor.settings.label.width", 180))
				.addField(language.getText("texteditor.settings.autocomp.enable"), checkBoxField(checkBox(autoCompleteSettings.isAutoCompleteEnabled())))
				.addField(language.getText("texteditor.settings.autocomp.autoinsertsingle"), checkBoxField(checkBox(autoCompleteSettings.isAutoCompleteSingleChoices())))
				.addField(language.getText("texteditor.settings.autocomp.autoactivate"), checkBoxField(checkBox(autoCompleteSettings.isAutoActivationEnabled())))
				.addField(language.getText("texteditor.settings.autocomp.choices.width"), integerField(autoCompleteSettings.getChoicesWindowSizeWidth()))
				.addField(language.getText("texteditor.settings.autocomp.choices.height"), integerField(autoCompleteSettings.getChoicesWindowSizeHeight()))
				.addField(language.getText("texteditor.settings.autocomp.showdesc"), checkBoxField(checkBox(autoCompleteSettings.isShowDescWindow())))
				.addField(language.getText("texteditor.settings.autocomp.description.width"), integerField(autoCompleteSettings.getDescriptionWindowSizeWidth()))
				.addField(language.getText("texteditor.settings.autocomp.description.height"), integerField(autoCompleteSettings.getDescriptionWindowSizeHeight()))
				.addField(language.getText("texteditor.settings.autocomp.triggerkey"), keyStrokeField(autoCompleteSettings.getTriggerKey()))
				.addField(language.getText("texteditor.settings.autocomp.paramtruncate"), integerField(autoCompleteSettings.getParameterDescriptionTruncateThreshold()))
			)
		);
	}

	/**
	 * Commits the current settings state to storage.
	 */
	public void commitSettings()
	{
		settings.setDefaultEditorAutoCompleteSettings(autoCompleteSettings);
	}
	
}
