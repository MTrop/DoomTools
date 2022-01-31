package net.mtrop.doom.tools.gui.doommake;

import java.awt.Container;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;

/**
 * The DoomMake New Project application.
 * @author Matthew Tropiano
 */
public class DoomMakeProjectApp implements DoomToolsApplicationInstance
{
	@Override
	public String getName() 
	{
		return "DoomMake - New Project";
	}

	@Override
	public Container getContentPane() 
	{
		// TODO Auto-generated method stub
		return containerOf(
			node(label("STUFF"))
		);
	}

}
