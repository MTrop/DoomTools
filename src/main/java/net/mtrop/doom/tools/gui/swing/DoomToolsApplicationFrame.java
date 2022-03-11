package net.mtrop.doom.tools.gui.swing;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsApplicationControlReceiver;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsApplicationStarter;
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
	 * @param starter the application starter stub for other applications.
	 */
	public DoomToolsApplicationFrame(final DoomToolsApplicationInstance instance, final DoomToolsApplicationStarter starter)
	{
		this.instance = instance;
		setIconImages(DoomToolsGUIUtils.get().getWindowIcons());
		setTitle(instance.getName());
		setJMenuBar(instance.createDesktopMenuBar());
		setContentPane(instance.createContentPane());
		setLocationByPlatform(true);
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
		instance.setApplicationControlReceiver(new DoomToolsApplicationControlReceiver() 
		{
			@Override
			public void attemptClose() 
			{
				closeMe();
			}

			@Override
			public Container getApplicationContainer()
			{
				return getContentPane();
			}

			@Override
			public void startApplication(DoomToolsApplicationInstance instance)
			{
				starter.startApplication(instance);
			}
		});
		pack();
		setMinimumSize(getSize());
	}
	
	// Attempt to close.
	private void closeMe()
	{
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	/**
	 * @return the instance for this frame.
	 */
	public DoomToolsApplicationInstance getInstance() 
	{
		return instance;
	}
	
}
