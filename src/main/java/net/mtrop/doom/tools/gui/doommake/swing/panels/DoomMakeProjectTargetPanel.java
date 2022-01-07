package net.mtrop.doom.tools.gui.doommake.swing.panels;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JPanel;
import javax.swing.ListModel;

import net.mtrop.doom.tools.gui.doommake.DoomMakeProjectHelper;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;


/**
 * A DoomMake panel for a single directory.
 * @author Matthew Tropiano
 */
public class DoomMakeProjectTargetPanel extends JPanel
{
	private static final long serialVersionUID = 3576857310145843126L;

	private DoomMakeProjectHelper helper;
	
	/** The project directory. */
	private File projectDirectory;
	/** Target list. */
	private ListModel<String> targetList;

	/**
	 * Creates a new project directory.
	 * @param projectDirectory the project directory.
	 */
	public DoomMakeProjectTargetPanel(File projectDirectory)
	{
		this.helper = DoomMakeProjectHelper.get();
		this.projectDirectory = projectDirectory;
		this.targetList = listModel(Collections.emptyList());
		
		// TODO: Make container.
	}
	
	
	
}
