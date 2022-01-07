package net.mtrop.doom.tools.gui.swing;

import java.awt.BorderLayout;

import net.mtrop.doom.tools.gui.doommake.swing.panels.DoomMakeSettingsPanel;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

/**
 * A window for setting global DoomMake Settings.
 * Changing each setting automatically saves them.
 * @author Matthew Tropiano
 */
public class DoomMakeSettingsWindow extends DoomToolsWindow 
{
	private static final long serialVersionUID = 7920577573661915653L;

	public DoomMakeSettingsWindow()
	{
		setTitle("DoomMake Settings");
		containerOf(this, new BorderLayout(), 
			node(BorderLayout.CENTER, new DoomMakeSettingsPanel())
		);
		pack();
		setResizable(false);
	}
}
