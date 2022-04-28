package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain.Theme;
import net.mtrop.doom.tools.gui.apps.swing.panels.DoomMakeSettingsPanel;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsSettingsManager;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;

/**
 * The settings panel for all of DoomTools (and individual apps).
 * @author Matthew Tropiano
 */
public class DoomToolsSettingsPanel extends JPanel
{
	private static final long serialVersionUID = 678866619269686755L;

	/** Language singleton. */
	private final DoomToolsLanguageManager language;
	/** Settings singleton. */
	private final DoomToolsSettingsManager settings;
	
	/**
	 * Creates the settings panel.
	 */
	public DoomToolsSettingsPanel()
	{
		this.language = DoomToolsLanguageManager.get();
		this.settings = DoomToolsSettingsManager.get();
		
		containerOf(this, new BorderLayout(),
			node(BorderLayout.CENTER, tabs(TabPlacement.LEFT,
				tab("DoomTools", containerOf(createEmptyBorder(4, 4, 4, 4),
					node(createMainPanel())
				)),
				tab("DoomMake", containerOf(createEmptyBorder(4, 4, 4, 4),
					node(new DoomMakeSettingsPanel())
				))
			))
		);
	}
	
	// Create main panel.
	private Container createMainPanel()
	{
		List<String> themes = Arrays.asList(
			Theme.LIGHT.name(),
			Theme.DARK.name(),
			Theme.INTELLIJ.name(),
			Theme.DARCULA.name()
		);
		
		JFormField<String> themeField = comboField(comboBox(comboBoxModel(themes), (c, i) -> {
			settings.setThemeName((String)i);
		}));
		themeField.setValue(settings.getThemeName());
		
		return containerOf(
			node(BorderLayout.NORTH, form(96)
				.addField(language.getText("doomtools.settings.theme"), themeField)
			),
			node(BorderLayout.CENTER, containerOf()),
			node(BorderLayout.SOUTH, containerOf(createEmptyBorder(4, 4, 4, 4),
				node(BorderLayout.CENTER, wrappedLabel(language.getText("doomtools.settings.theme.notice")))
			))
		);
	}
	
}
