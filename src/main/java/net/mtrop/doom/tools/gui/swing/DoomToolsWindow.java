package net.mtrop.doom.tools.gui.swing;

import javax.swing.JFrame;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.DoomToolsImageManager;

import java.awt.Image;
import java.util.Arrays;

/**
 * A common DoomMake window.
 * Sets up tedious window stuff.
 * @author Matthew Tropiano
 */
public abstract class DoomToolsWindow extends JFrame 
{
	private static final long serialVersionUID = -7160047525149992542L;

	private static final Image ICON16 = DoomToolsImageManager.get().getImage("doomtools-logo-16.png"); 
	private static final Image ICON32 = DoomToolsImageManager.get().getImage("doomtools-logo-32.png"); 
	private static final Image ICON48 = DoomToolsImageManager.get().getImage("doomtools-logo-48.png"); 
	private static final Image ICON64 = DoomToolsImageManager.get().getImage("doomtools-logo-64.png"); 
	private static final Image ICON96 = DoomToolsImageManager.get().getImage("doomtools-logo-96.png"); 
	private static final Image ICON128 = DoomToolsImageManager.get().getImage("doomtools-logo-128.png"); 
	
	/**
	 * Creates the window and fills in some defaults.
	 */
	protected DoomToolsWindow()
	{
		setTitle("DoomMake v" + Common.getVersionString("doommake"));
		setIconImages(Arrays.asList(ICON128, ICON96, ICON64, ICON48, ICON32, ICON16));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}
