package net.mtrop.doom.tools.gui.doommake.swing.panels;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.struct.swing.FormFactory.JFormPanel.LabelJustification;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormPanel.LabelSide;
import net.mtrop.doom.tools.gui.doommake.DoomMakeSettings;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;

import java.io.File;

/**
 * A DoomMake panel for common settings.
 * @author Matthew Tropiano
 */
public class DoomMakeSettingsPanel extends JPanel
{
	private static final long serialVersionUID = 3657842361863713721L;
	
	private static final FileFilter EXE_FILTER = new FileFilter() 
	{
		@Override
		public String getDescription() 
		{
			return "Executables";
		}
		
		@Override
		public boolean accept(File f) 
		{
			return f.canExecute();
		}
	};
	
	/** Settings singleton. */
	private DoomMakeSettings settings;
	
	/**
	 * Creates the settings panel.
	 */
	public DoomMakeSettingsPanel()
	{
		this.settings = DoomMakeSettings.get();
		
		containerOf(this, new BoxLayout(this, BoxLayout.X_AXIS),
			node(form(LabelSide.LEFT, LabelJustification.LEFT, 96)
				.addField("Path to VSCode", fileField(
					settings.getPathToVSCode(), 
					(currentFile) -> SwingUtils.file(this, "Path to VSCode Executable", currentFile, "Open", EXE_FILTER), 
					(value) -> settings.setPathToVSCode(value)
				))
				.addField("Path to SLADE3", fileField(
					settings.getPathToSlade(), 
					(currentFile) -> SwingUtils.file(this, "Path to SLADE Executable", currentFile, "Open", EXE_FILTER), 
					(value) -> settings.setPathToSlade(value)
				))
			)
		);
	}
	
}
