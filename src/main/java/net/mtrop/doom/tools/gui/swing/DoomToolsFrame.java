package net.mtrop.doom.tools.gui.swing;

import javax.swing.JFrame;

import net.mtrop.doom.tools.DoomToolsMain;
import net.mtrop.doom.tools.gui.DoomToolsImageManager;

import java.awt.Image;
import java.util.Arrays;

/**
 * A common DoomTools window.
 * Sets up tedious window stuff.
 * @author Matthew Tropiano
 */
public abstract class DoomToolsFrame extends JFrame 
{
	private static final long serialVersionUID = -7160047525149992542L;

	private static final DoomToolsImageManager IMAGES = DoomToolsImageManager.get();
	
	private static final Image ICON16  = IMAGES.getImage("doomtools-logo-16.png"); 
	private static final Image ICON32  = IMAGES.getImage("doomtools-logo-32.png"); 
	private static final Image ICON48  = IMAGES.getImage("doomtools-logo-48.png"); 
	private static final Image ICON64  = IMAGES.getImage("doomtools-logo-64.png"); 
	private static final Image ICON96  = IMAGES.getImage("doomtools-logo-96.png"); 
	private static final Image ICON128 = IMAGES.getImage("doomtools-logo-128.png"); 
	
	/**
	 * Creates the window and fills in some defaults.
	 */
	protected DoomToolsFrame()
	{
		setTitle("DoomTools v" + DoomToolsMain.getVersion());
		setIconImages(Arrays.asList(ICON128, ICON96, ICON64, ICON48, ICON32, ICON16));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
}
