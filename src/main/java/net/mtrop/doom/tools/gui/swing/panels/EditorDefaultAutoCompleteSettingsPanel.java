package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.EditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel.EditorAutoCompleteSettings;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;

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
	
	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private EditorSettingsManager settings;
	
	private EditorAutoCompleteSettings autoCompleteSettings;
	private EditorAutoCompleteSettings defaultSettings;

	private JFormField<Boolean> autoCompleteEnabledField;
	private JFormField<Boolean> autoCompleteSingleChoicesField;
	private JFormField<Boolean> autoActivationEnabledField;
	private JFormField<Integer> autoActivationDelayField;
	private JFormField<Integer> choicesWindowSizeWidthField;
	private JFormField<Integer> choicesWindowSizeHeightField;
	private JFormField<Boolean> showDescWindowField;
	private JFormField<Integer> descriptionWindowSizeWidthField;
	private JFormField<Integer> descriptionWindowSizeHeightField;
	private JFormField<KeyStroke> triggerKeyField;
	private JFormField<Integer> parameterDescriptionTruncateThresholdField;

	
	/**
	 * Creates a new panel.
	 */
	public EditorDefaultAutoCompleteSettingsPanel()
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = EditorSettingsManager.get();
		
		this.autoCompleteSettings = settings.getDefaultEditorAutoCompleteSettings();
		this.defaultSettings = new EditorAutoCompleteSettings();

		this.autoCompleteEnabledField = checkBoxField(checkBox(autoCompleteSettings.isAutoCompleteEnabled(),
			(v) -> autoCompleteSettings.setAutoCompleteEnabled(v)
		));
		this.autoCompleteSingleChoicesField = checkBoxField(checkBox(autoCompleteSettings.isAutoCompleteSingleChoices(),
			(v) -> autoCompleteSettings.setAutoCompleteSingleChoices(v)
		));
		this.autoActivationEnabledField = checkBoxField(checkBox(autoCompleteSettings.isAutoActivationEnabled(),
			(v) -> autoCompleteSettings.setAutoActivationEnabled(v)
		));
		this.autoActivationDelayField = integerField(autoCompleteSettings.getAutoActivationDelay(),
			(v) -> autoCompleteSettings.setAutoActivationDelay(v)
		);
		this.choicesWindowSizeWidthField = integerField(autoCompleteSettings.getChoicesWindowSizeWidth(), 
			(v) -> autoCompleteSettings.setChoicesWindowSizeWidth(v)
		);
		this.choicesWindowSizeHeightField = integerField(autoCompleteSettings.getChoicesWindowSizeHeight(), 
			(v) -> autoCompleteSettings.setChoicesWindowSizeHeight(v)
		);
		this.showDescWindowField = checkBoxField(checkBox(autoCompleteSettings.isShowDescWindow(),
			(v) -> autoCompleteSettings.setShowDescWindow(v)
		));
		this.descriptionWindowSizeWidthField = integerField(autoCompleteSettings.getDescriptionWindowSizeWidth(), 
			(v) -> autoCompleteSettings.setDescriptionWindowSizeWidth(v)
		);
		this.descriptionWindowSizeHeightField = integerField(autoCompleteSettings.getDescriptionWindowSizeHeight(), 
			(v) -> autoCompleteSettings.setDescriptionWindowSizeHeight(v)
		);
		this.triggerKeyField = keyStrokeField(autoCompleteSettings.getTriggerKey(), 
			(v) -> {autoCompleteSettings.setTriggerKey(v);}
		);
		this.parameterDescriptionTruncateThresholdField = integerField(autoCompleteSettings.getParameterDescriptionTruncateThreshold(), 
			(v) -> autoCompleteSettings.setParameterDescriptionTruncateThreshold(v)
		);
		
		containerOf(this, borderLayout(),
			node(BorderLayout.CENTER, utils.createFormField(form(language.getInteger("texteditor.settings.label.width", 180)),
				utils.formField("texteditor.settings.autocomp.enable", autoCompleteEnabledField),
				utils.formField("texteditor.settings.autocomp.autoinsertsingle", autoCompleteSingleChoicesField),
				utils.formField("texteditor.settings.autocomp.autoactivate", autoActivationEnabledField),
				utils.formField("texteditor.settings.autocomp.autoactivatedelay", autoActivationDelayField),
				utils.formField("texteditor.settings.autocomp.choices.width", choicesWindowSizeWidthField),
				utils.formField("texteditor.settings.autocomp.choices.height", choicesWindowSizeHeightField),
				utils.formField("texteditor.settings.autocomp.showdesc", showDescWindowField),
				utils.formField("texteditor.settings.autocomp.description.width", descriptionWindowSizeWidthField),
				utils.formField("texteditor.settings.autocomp.description.height", descriptionWindowSizeHeightField),
				utils.formField("texteditor.settings.autocomp.triggerkey", triggerKeyField),
				utils.formField("texteditor.settings.autocomp.paramtruncate", parameterDescriptionTruncateThresholdField)
				).addField(buttonField(button(language.getText("texteditor.settings.reset"), (c, e) -> resetSettings()))
			))
		);
	}

	/**
	 * Resets the settings.
	 */
	public void resetSettings()
	{
		autoCompleteEnabledField.setValue(defaultSettings.isAutoCompleteEnabled());
		autoCompleteSingleChoicesField.setValue(defaultSettings.isAutoCompleteSingleChoices());
		autoActivationEnabledField.setValue(defaultSettings.isAutoActivationEnabled());
		autoActivationDelayField.setValue(defaultSettings.getAutoActivationDelay());
		choicesWindowSizeWidthField.setValue(defaultSettings.getChoicesWindowSizeWidth());
		choicesWindowSizeHeightField.setValue(defaultSettings.getChoicesWindowSizeHeight());
		showDescWindowField.setValue(defaultSettings.isShowDescWindow());
		descriptionWindowSizeWidthField.setValue(defaultSettings.getDescriptionWindowSizeWidth());
		descriptionWindowSizeHeightField.setValue(defaultSettings.getDescriptionWindowSizeHeight());
		triggerKeyField.setValue(defaultSettings.getTriggerKey());
		parameterDescriptionTruncateThresholdField.setValue(defaultSettings.getParameterDescriptionTruncateThreshold());
	}
	
	/**
	 * Commits the current settings state to storage.
	 */
	public void commitSettings()
	{
		settings.setDefaultEditorAutoCompleteSettings(autoCompleteSettings);
	}
	
}
