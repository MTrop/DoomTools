package net.mtrop.doom.tools.gui.swing.adapters;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputListener;

/**
 * A combined adapter for mouse movements and presses.
 * @author Matthew Tropiano
 */
public class MouseControlAdapter implements MouseInputListener, MouseWheelListener
{
	public MouseControlAdapter()
	{
		// Do nothing.
	}
	
	@Override
	public void mouseClicked(MouseEvent e) 
	{
		// Do nothing.
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		// Do nothing.
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		// Do nothing.
	}

	@Override
	public void mouseEntered(MouseEvent e) 
	{
		// Do nothing.
	}

	@Override
	public void mouseExited(MouseEvent e) 
	{
		// Do nothing.
	}

	@Override
	public void mouseDragged(MouseEvent e) 
	{
		// Do nothing.
	}

	@Override
	public void mouseMoved(MouseEvent e) 
	{
		// Do nothing.
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		// Do nothing.
	}
	
}

