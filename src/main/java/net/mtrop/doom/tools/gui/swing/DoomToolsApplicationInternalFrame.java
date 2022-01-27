package net.mtrop.doom.tools.gui.swing;

import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsGUIUtils;

/**
 * A single internal application frame for a DoomTools application.
 * @author Matthew Tropiano
 */
public class DoomToolsApplicationInternalFrame extends JInternalFrame 
{
	private static final long serialVersionUID = 7311434945898035762L;

	/** Image manager. */
	private DoomToolsGUIUtils utils;
	/** The application instance. */
	private DoomToolsApplicationInstance instance;
	
	/**
	 * Creates an application frame from an application instance.
	 * @param instance the instance to use.
	 */
	public DoomToolsApplicationInternalFrame(DoomToolsApplicationInstance instance)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.instance = instance;

		Icon appIcon = instance.getIcon();
		
		setTitle(instance.getName());
		setFrameIcon(appIcon != null ? appIcon : utils.getWindowIcon());
		setJMenuBar(instance.getInternalMenuBar());
		setContentPane(instance.getContentPane());
		setResizable(true);
		setIconifiable(true);
		setClosable(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameOpened(InternalFrameEvent e) 
			{
				instance.onOpen();
			}
			
			@Override
			public void internalFrameActivated(InternalFrameEvent e) 
			{
				instance.onFocus();
			}

			@Override
			public void internalFrameDeactivated(InternalFrameEvent e) 
			{
				instance.onBlur();
			}

			@Override
			public void internalFrameIconified(InternalFrameEvent e) 
			{
				instance.onMinimize();
			}
			
			@Override
			public void internalFrameDeiconified(InternalFrameEvent e) 
			{
				instance.onRestore();
			}
		});
		pack();
	}
	
	/**
	 * @return the instance for this frame.
	 */
	public DoomToolsApplicationInstance getInstance() 
	{
		return instance;
	}
	
}
