package net.mtrop.doom.tools.gui.doommake.swing.panels;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.gui.DoomToolsSettingsManager;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormPanel.LabelSide;

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
	
	/** Settings singleton. */
	private static final DoomToolsSettingsManager settings = DoomToolsSettingsManager.get();
	
	/**
	 * Creates the settings panel.
	 */
	public DoomMakeSettingsPanel()
	{
		containerOf(this, dimension(450, 100),
			node(BorderLayout.NORTH, form(LabelSide.LEFT, LabelJustification.LEFT, 96)
				.addField("Path to VSCode", fileField(
					settings.getPathToVSCode(), 
					(currentFile) -> chooseFile(this, "Path to VSCode Executable", currentFile, "Open", FileFilters.EXECUTABLES), 
					(value) -> settings.setPathToVSCode(value)
				))
				.addField("Path to SLADE3", fileField(
					settings.getPathToSlade(), 
					(currentFile) -> chooseFile(this, "Path to SLADE Executable", currentFile, "Open", FileFilters.EXECUTABLES), 
					(value) -> settings.setPathToSlade(value)
				))
			),
			node(BorderLayout.CENTER, containerOf())
		);
	}
	
}
