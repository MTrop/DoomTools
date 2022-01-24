package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsImageManager;
import net.mtrop.doom.tools.gui.swing.DoomToolsApplicationInternalFrame;
import net.mtrop.doom.tools.struct.swing.ComponentFactory;

/**
 * The main desktop pane for all DoomTools applications.
 * @author Matthew Tropiano
 */
public class DoomToolsDesktopPane extends JDesktopPane 
{
	private static final long serialVersionUID = -4832880176282917356L;

	/** Image manager. */
	private static final DoomToolsImageManager IMAGES = DoomToolsImageManager.get();

	/** All application instances. */
	private Set<DoomToolsApplicationInstance> instances;
	/** The background image on the desktop pane. */
	private ImageIcon backgroundImage;
	
	public DoomToolsDesktopPane()
	{
		this.instances = new HashSet<>();
		this.backgroundImage = ComponentFactory.icon(IMAGES.getImage("background-logo.png"));
		setBackground(Color.DARK_GRAY);
		setPreferredSize(new Dimension(backgroundImage.getIconWidth(), backgroundImage.getIconHeight()));
	}
	
	@Override
	protected void paintComponent(Graphics g) 
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		int x = (getWidth() - backgroundImage.getIconWidth()) / 2;
        int y = (getHeight() - backgroundImage.getIconHeight()) / 2;
		g2d.drawImage(backgroundImage.getImage(), x, y, null);
	}
	
	/**
	 * Adds a frame to this pane for an application instance.
	 * @param instance the application instance to use.
	 * @return the new frame.
	 */
	public JInternalFrame addFrame(final DoomToolsApplicationInstance instance)
	{
		DoomToolsApplicationInternalFrame frame = new DoomToolsApplicationInternalFrame(instance);
		instances.add(instance);
		frame.addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameClosed(InternalFrameEvent e) 
			{
				instances.remove(instance);
			}
		});
		return frame;
	}
	
}
