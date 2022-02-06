package net.mtrop.doom.tools.gui.testapp;

import java.awt.Container;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

/**
 * Test application.
 * @author Matthew Tropiano
 */
public class TestApplication implements DoomToolsApplicationInstance
{
	private Container contentPane;
	
	@Override
	public String getName()
	{
		return "Test Application";
	}

	@Override
	public Container createContentPane() 
	{
		return contentPane = containerOf(node(label("Hello")));
	}

	@Override
	public boolean shouldClose() 
	{
		return SwingUtils.yesTo(contentPane, "Quit?");
	}
	
}
