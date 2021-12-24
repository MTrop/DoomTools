package net.mtrop.doom.tools.doommake.gui.swing.panels;

import java.io.File;

import javax.swing.JPanel;
import javax.swing.ListModel;

/**
 * A DoomMake panel for a single directory.
 * @author Matthew Tropiano
 */
public class DoomMakeProjectPanel extends JPanel
{
	private static final long serialVersionUID = 3576857310145843126L;

	/** The project directory. */
	private File projectDirectory;
	
	/** Target list. */
	private ListModel<String> targetList;

	/**
	 * 
	 * @param projectDirectory
	 */
	public DoomMakeProjectPanel(File projectDirectory)
	{
		
	}
	
}
