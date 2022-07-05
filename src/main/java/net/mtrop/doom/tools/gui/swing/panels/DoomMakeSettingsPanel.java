package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.DoomMakeSettingsManager;

import static javax.swing.BorderFactory.createEmptyBorder;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;

/**
 * A DoomMake panel for common settings.
 * @author Matthew Tropiano
 */
public class DoomMakeSettingsPanel extends JPanel
{
	private static final long serialVersionUID = 3657842361863713721L;
	
	/** Language singleton. */
	private final DoomToolsLanguageManager language;
	/** Settings singleton. */
	private final DoomMakeSettingsManager settings;
	
	/**
	 * Creates the settings panel.
	 */
	public DoomMakeSettingsPanel()
	{
		this.language = DoomToolsLanguageManager.get();
		this.settings = DoomMakeSettingsManager.get();
		
		containerOf(this, dimension(450, 100),
			node(BorderLayout.NORTH, form(96)
				.addField(language.getText("doommake.settings.vscode.label"), fileField(
					settings.getPathToVSCode(), 
					(currentFile) -> chooseFile(
						this, 
						language.getText("doommake.settings.vscode.title"), 
						currentFile, 
						language.getText("doommake.settings.browse.open"),
						FileFilters.EXECUTABLES
					), 
					(value) -> settings.setPathToVSCode(value)
				))
				.addField(language.getText("doommake.settings.slade.label"), fileField(
					settings.getPathToSlade(), 
					(currentFile) -> chooseFile(
						this, 
						language.getText("doommake.settings.slade.title"), 
						currentFile, 
						language.getText("doommake.settings.browse.open"),
						FileFilters.EXECUTABLES
					), 
					(value) -> settings.setPathToSlade(value)
				))
			),
			node(BorderLayout.CENTER, containerOf()),
			node(BorderLayout.SOUTH, containerOf(createEmptyBorder(4, 4, 4, 4),
				node(BorderLayout.CENTER, wrappedLabel(language.getText("doommake.settings.notice")))
			))
		);
	}
	
}
