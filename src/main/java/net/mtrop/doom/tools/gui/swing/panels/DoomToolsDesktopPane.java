package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
	private Map<DoomToolsApplicationInstance, JInternalFrame> instances;
	/** The background image on the desktop pane. */
	private ImageIcon backgroundImage;
	
	public DoomToolsDesktopPane()
	{
		this.instances = new HashMap<>();
		this.backgroundImage = ComponentFactory.icon(IMAGES.getImage("background-logo.png"));
		int imageWidth = backgroundImage.getIconWidth() + 100;
		setBackground(Color.DARK_GRAY);
		setPreferredSize(new Dimension(imageWidth, (int)(imageWidth * 0.75)));
	}
	
	@Override
	protected void paintComponent(Graphics g) 
	{
		// Paint pane.
		super.paintComponent(g);

		// Paint background image on top.
		int x, y;
		int width = getWidth();
		int height = getHeight();
		int imageWidth = backgroundImage.getIconWidth();
		int imageHeight = backgroundImage.getIconHeight();
        
        // scale with aspect if needed
        if (width < imageWidth || height < imageHeight)
        {
    		double panelWidthRatio = ((double)width) / height;
    		double imageWidthRatio = ((double)imageWidth) / imageHeight;
    		double imageHeightRatio = ((double)imageHeight) / imageWidth;
            if (panelWidthRatio < imageWidthRatio)
            {
            	imageWidth = width;
            	imageHeight = (int)(width * imageHeightRatio);
            	x = 0;
            	y = (height - imageHeight) / 2;
            }
            else
            {
            	imageWidth = (int)(height * imageWidthRatio);
            	imageHeight = height;
            	x = (width - imageWidth) / 2;
            	y = 0;
            }
        }
        else
        {
    		x = (width - imageWidth) / 2;
            y = (height - imageHeight) / 2;
        }
        
		g.drawImage(backgroundImage.getImage(), x, y, imageWidth, imageHeight, null);
	}
	
	/**
	 * Adds a frame to this pane for an application instance.
	 * @param instance the application instance to use.
	 * @return the new frame.
	 */
	public JInternalFrame addApplicationFrame(final DoomToolsApplicationInstance instance)
	{
		DoomToolsApplicationInternalFrame frame = new DoomToolsApplicationInternalFrame(instance);
		instances.put(instance, frame);
		frame.addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameClosing(InternalFrameEvent e) 
			{
				attemptCloseApplication(instance);
			}
		});
		add(frame);
		return frame;
	}
	
	private void attemptCloseApplication(DoomToolsApplicationInstance instance)
	{
		if (!instances.containsKey(instance))
			return;
		
		if (instance.shouldClose())
			closeApplication(instance);
	}

	private void closeApplication(DoomToolsApplicationInstance instance)
	{
		if (!instances.containsKey(instance))
			return;
		
		JInternalFrame frame = instances.remove(instance); 
		frame.setVisible(false);
		frame.dispose();
		instance.onClose();
	}
	
	/**
	 * Closes all workspace applications.
	 */
	public void clearWorkspace()
	{
		Set<DoomToolsApplicationInstance> instanceSet = new TreeSet<>(instances.keySet()); // copy set
		for (DoomToolsApplicationInstance instance : instanceSet)
			attemptCloseApplication(instance);
	}
	
}
