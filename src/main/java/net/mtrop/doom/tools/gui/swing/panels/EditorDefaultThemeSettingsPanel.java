package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.EditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel.EditorThemeType;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.util.EnumUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A panel that manages the default editor theme settings. 
 * @author Matthew Tropiano
 */
public class EditorDefaultThemeSettingsPanel extends JPanel 
{
	private static final long serialVersionUID = -2628032968866995717L;
	
	private DoomToolsLanguageManager language;
	private EditorSettingsManager settings;
	
	private Map<String, EditorThemeType> friendlyNameMap;
	
	private JFormField<String> themeField;
	
	private String themeName;
	
	
	/**
	 * Creates a new panel.
	 */
	public EditorDefaultThemeSettingsPanel()
	{
		this.language = DoomToolsLanguageManager.get();
		this.settings = EditorSettingsManager.get();
		
		this.friendlyNameMap = EnumUtils.createMap(EditorThemeType.class, (i, e) -> e.getFriendlyName());
		
		this.themeName = settings.getEditorThemeName();

		List<String> themes = new ArrayList<>(friendlyNameMap.keySet());
		
		themeField = comboField(comboBox(comboBoxModel(themes), (c, i) -> {
			themeName = friendlyNameMap.get((String)i).name();
		}));
		themeField.setValue(EditorThemeType.THEME_MAP.getOrDefault(settings.getEditorThemeName(), EditorThemeType.DEFAULT).getFriendlyName());
		
		containerOf(this, borderLayout(),
			node(BorderLayout.CENTER, form(language.getInteger("texteditor.settings.label.width", 180))
				.addField(language.getText("texteditor.settings.theme"), themeField)
				.addField(buttonField(button(language.getText("texteditor.settings.reset"), (c, e) -> resetSettings())))
			)
		);
	}

	/**
	 * Resets the settings.
	 */
	public void resetSettings()
	{
		themeField.setValue(EditorThemeType.DEFAULT.getFriendlyName());
	}
	
	/**
	 * Commits the current settings state to storage.
	 */
	public void commitSettings()
	{
		settings.setEditorThemeName(themeName);
	}
	
}
