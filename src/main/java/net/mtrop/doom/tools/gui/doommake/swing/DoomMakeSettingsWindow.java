package net.mtrop.doom.tools.gui.doommake.swing;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.doommake.swing.panels.DoomMakeSettingsPanel;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

/**
 * A window for setting global DoomMake Settings.
 * Changing each setting automatically saves them.
 * @author Matthew Tropiano
 */
public class DoomMakeSettingsWindow extends JFrame
{
	private static final long serialVersionUID = 7920577573661915653L;

	public DoomMakeSettingsWindow()
	{
		setIconImages(DoomToolsGUIUtils.get().getWindowIcons());
		setTitle("DoomMake Settings");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(containerOf(new BorderLayout(), 
			node(BorderLayout.CENTER, new DoomMakeSettingsPanel())
		));
		pack();
		setResizable(false);
	}
}
