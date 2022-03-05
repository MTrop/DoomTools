package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsApplicationStarter;
import net.mtrop.doom.tools.gui.DoomToolsImageManager;
import net.mtrop.doom.tools.gui.DoomToolsLogger;
import net.mtrop.doom.tools.gui.swing.DoomToolsApplicationInternalFrame;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.ComponentFactory;

/**
 * The main desktop pane for all DoomTools applications.
 * @author Matthew Tropiano
 */
public class DoomToolsDesktopPane extends JDesktopPane 
{
	private static final long serialVersionUID = -4832880176282917356L;

    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomToolsDesktopPane.class); 

	/** Image manager. */
	private DoomToolsImageManager images;
	
	/** All application instances. */
	private Map<DoomToolsApplicationInstance, JInternalFrame> instances;
	/** List of frames. */
	private List<JInternalFrame> frames;
	
	/** The background image on the desktop pane. */
	private ImageIcon backgroundImage;
	/** A movement listener that keeps windows in the desktop area. */
	private ComponentListener frameMovementListener;
	
	public DoomToolsDesktopPane()
	{
		this.images = DoomToolsImageManager.get();
		this.instances = new HashMap<>();
		this.frames = new ArrayList<>();
		this.backgroundImage = ComponentFactory.icon(images.getImage("background-logo.png"));
		this.frameMovementListener = new ComponentAdapter() 
		{
			@Override
			public void componentMoved(ComponentEvent e) 
			{
				correctFramePosition((JInternalFrame)e.getComponent());
			}
			
			@Override
			public void componentResized(ComponentEvent e) 
			{
				correctFramePosition((JInternalFrame)e.getComponent());
			}
		};
		
		
		int imageWidth = backgroundImage.getIconWidth() + 100;
		setBackground(Color.DARK_GRAY);
		setPreferredSize(new Dimension(imageWidth, (int)(imageWidth * 0.75)));
		addComponentListener(new ComponentAdapter() 
		{
			@Override
			public void componentResized(ComponentEvent e) 
			{
				correctAllFramePositions();
			}
		});
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
	 * @param starter 
	 * @return the new frame.
	 */
	public JInternalFrame addApplicationFrame(final DoomToolsApplicationInstance instance, final DoomToolsApplicationStarter starter)
	{
		DoomToolsApplicationInternalFrame frame = new DoomToolsApplicationInternalFrame(instance, starter);
		instances.put(instance, frame);
		frames.add(frame);
		frame.addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameClosing(InternalFrameEvent e) 
			{
				attemptCloseApplication(instance);
			}
		});
		frame.addComponentListener(frameMovementListener);
		add(frame);
		LOG.infof("Started application: %s", instance.getClass().getSimpleName());
		return frame;
	}
	
	/**
	 * Minimizes (iconifies) all windows in the workspace.
	 */
	public void minimizeWorkspace()
	{
		for (Map.Entry<DoomToolsApplicationInstance, JInternalFrame> entry : instances.entrySet())
		{
			try {
				entry.getValue().setIcon(true);
			} catch (PropertyVetoException e) {
				LOG.warnf("Frame for %s refused iconify: %s", entry.getKey(), e.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * Restores (deiconifies) all windows in the workspace.
	 */
	public void restoreWorkspace()
	{
		for (Map.Entry<DoomToolsApplicationInstance, JInternalFrame> entry : instances.entrySet())
		{
			try {
				entry.getValue().setIcon(false);
			} catch (PropertyVetoException e) {
				LOG.warnf("Frame for %s refused deiconify: %s", entry.getKey(), e.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * Cascades all windows in the workspace.
	 */
	public void cascadeWorkspace()
	{
		int i = 0;
		final int CASCADE_STEP = 24;
		
		final TreeSet<JInternalFrame> reorderedSet = new TreeSet<>((a, b) -> a.hasFocus() ? 1 : 0);
		reorderedSet.addAll(instances.entrySet().stream()
			.map((e)->e.getValue())
			.collect(Collectors.toList())
		);
		
		for (Map.Entry<DoomToolsApplicationInstance, JInternalFrame> entry : instances.entrySet())
		{
			JInternalFrame frame = entry.getValue();
			frame.setBounds(CASCADE_STEP * i, CASCADE_STEP * i, frame.getWidth(), frame.getHeight());
			i++;
		}
	}
	
	/**
	 * Closes all workspace applications.
	 */
	public void clearWorkspace()
	{
		Set<DoomToolsApplicationInstance> instanceSet = new HashSet<>(instances.keySet()); // copy set
		for (DoomToolsApplicationInstance instance : instanceSet)
			attemptCloseApplication(instance);
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
		frames.remove(frame);
		frame.setVisible(false);
		frame.dispose();
		instance.onClose();
		LOG.infof("Closed application: %s", instance.getClass().getSimpleName());
	}
	
	private void correctAllFramePositions()
	{
		for (int i = 0; i < frames.size(); i++)
			correctFramePosition(frames.get(i));
	}
	
	private void correctFramePosition(JInternalFrame frame)
	{
		int currentX = frame.getX();
		int currentY = frame.getY();
		int currentWidth = frame.getWidth();
		int currentHeight = frame.getHeight();

		if (frame.getWidth() > getWidth())
			return;
		if (frame.getHeight() > getHeight())
			return;
		
		if (frame.getX() < 0)
			frame.setBounds(0, currentY, currentWidth, currentHeight);
		else if (frame.getX() + frame.getWidth() > getWidth())
			frame.setBounds(getWidth() - frame.getWidth(), currentY, currentWidth, currentHeight);

		if (frame.getY() < 0)
			frame.setBounds(currentX, 0, currentWidth, currentHeight);
		else if (frame.getY() + frame.getHeight() > getHeight())
			frame.setBounds(currentX, getHeight() - frame.getHeight(), currentWidth, currentHeight);
	}
	
}
