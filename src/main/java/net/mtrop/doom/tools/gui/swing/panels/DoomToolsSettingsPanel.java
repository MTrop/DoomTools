package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.doommake.swing.panels.DoomMakeSettingsPanel;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

/**
 * The settings panel for all of DoomTools (and individual apps).
 * @author Matthew Tropiano
 */
public class DoomToolsSettingsPanel extends JPanel
{
	private static final long serialVersionUID = 678866619269686755L;

	/**
	 * Creates the settings panel.
	 */
	public DoomToolsSettingsPanel()
	{
		containerOf(this, new BorderLayout(),
			node(BorderLayout.CENTER, tabs(TabPlacement.LEFT,
				tab("DoomTools", containerOf()),
				tab("DoomMake", containerOf(createEmptyBorder(4, 4, 4, 4),
					node(new DoomMakeSettingsPanel())
				))
			))
		);
	}
	
}
