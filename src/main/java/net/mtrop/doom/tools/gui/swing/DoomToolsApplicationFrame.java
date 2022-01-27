package net.mtrop.doom.tools.gui.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsGUIUtils;

/**
 * A single application frame for a DoomTools application.
 * @author Matthew Tropiano
 */
public class DoomToolsApplicationFrame extends JFrame 
{
	private static final long serialVersionUID = -3110415734317195898L;
	
	/** The application instance. */
	private DoomToolsApplicationInstance instance;
	
	/**
	 * Creates an application frame from an application instance.
	 * @param instance the instance to use.
	 */
	public DoomToolsApplicationFrame(final DoomToolsApplicationInstance instance)
	{
		this.instance = instance;
		setIconImages(DoomToolsGUIUtils.get().getWindowIcons());
		setTitle(instance.getName());
		setJMenuBar(instance.getMenuBar());
		setContentPane(instance.getContentPane());
		setResizable(true);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e) 
			{
				instance.onOpen();
			}
			
			@Override
			public void windowGainedFocus(WindowEvent e) 
			{
				instance.onFocus();
			}
			
			@Override
			public void windowLostFocus(WindowEvent e) 
			{
				instance.onBlur();
			}
			
			@Override
			public void windowIconified(WindowEvent e) 
			{
				instance.onMinimize();
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) 
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
