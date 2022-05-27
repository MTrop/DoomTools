package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.EditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel.EditorViewSettings;

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

		containerOf(this, borderLayout(),
			node(BorderLayout.CENTER, form(language.getInteger("texteditor.settings.label.width", 180))
				.addField(language.getText("texteditor.settings.view.tabsize"), spinnerField(spinner(spinnerModel(viewSettings.getTabSize(), 2, 8, 1))))
				.addField(language.getText("texteditor.settings.view.hardtabs"), checkBoxField(checkBox(viewSettings.isTabsEmulated())))
				.addField(language.getText("texteditor.settings.view.linewrap"), checkBoxField(checkBox(viewSettings.isLineWrap())))
				.addField(language.getText("texteditor.settings.view.wordwrap"), checkBoxField(checkBox(viewSettings.isWrapStyleWord())))
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
