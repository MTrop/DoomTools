package net.mtrop.doom.tools.gui.apps.swing.panels;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.apps.swing.panels.MultiFileEditorPanel.EditorViewSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.EditorSettingsManager;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;

/**
 * A panel that manages the default editor view settings. 
 * @author Matthew Tropiano
 */
public class EditorDefaultViewSettingsPanel extends JPanel 
{
	private static final long serialVersionUID = 6434971725684191370L;
	
	private DoomToolsLanguageManager language;
	private EditorSettingsManager settings;
	
	private EditorViewSettings viewSettings;
	
	/**
	 * Creates a new panel.
	 */
	public EditorDefaultViewSettingsPanel()
	{
		this.language = DoomToolsLanguageManager.get();
		this.settings = EditorSettingsManager.get();
		
		this.viewSettings = settings.getDefaultEditorViewSettings();

		final JFormField<Integer> tabSizeField = spinnerField(spinner(spinnerModel(viewSettings.getTabSize(), 2, 8, 1)));
		final JFormField<Boolean> hardTabsField = checkBoxField(checkBox(viewSettings.isTabsEmulated()));
		final JFormField<Boolean> lineWrapField = checkBoxField(checkBox(viewSettings.isLineWrap()));
		final JFormField<Boolean> wordWrapField = checkBoxField(checkBox(viewSettings.isWrapStyleWord()));
		
		containerOf(this, borderLayout(),
			node(BorderLayout.CENTER, form(136)
				.addField(language.getText("texteditor.settings.view.tabsize"), tabSizeField)
				.addField(language.getText("texteditor.settings.view.hardtabs"), hardTabsField)
				.addField(language.getText("texteditor.settings.view.linewrap"), lineWrapField)
				.addField(language.getText("texteditor.settings.view.wordwrap"), wordWrapField)
			)
		);
	}

	/**
	 * Commits the current settings state to storage.
	 */
	public void commitSettings()
	{
		settings.setDefaultEditorViewSettings(viewSettings);
	}
	
}
