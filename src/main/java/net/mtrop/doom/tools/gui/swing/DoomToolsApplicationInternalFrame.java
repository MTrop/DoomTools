/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing;

import static javax.swing.BorderFactory.createEmptyBorder;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.containerOf;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.node;

import java.awt.Container;

import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.mtrop.doom.tools.gui.DoomToolsApplicationListener;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsApplicationStarter;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;

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
	 * @param starter the application starter stub for other applications.
	 */
	public DoomToolsApplicationInternalFrame(final DoomToolsApplicationInstance instance, final DoomToolsApplicationStarter starter)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.instance = instance;

		Icon appIcon = instance.getIcon();
		
		setTitle(instance.getTitle());
		setFrameIcon(appIcon != null ? appIcon : utils.getWindowIcon());
		setJMenuBar(instance.createInternalMenuBar());
		setContentPane(containerOf(createEmptyBorder(8, 8, 8, 8),
			node(instance.createContentPane())
		));
		setResizable(true);
		setIconifiable(true);
		setClosable(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameOpened(InternalFrameEvent e) 
			{
				instance.onOpen(e.getSource());
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
		instance.setApplicationListener(new DoomToolsApplicationListener() 
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
		instance.onCreate(this);
	}
	
	// Attempt to close.
	private void closeMe()
	{
		dispatchEvent(new InternalFrameEvent(this, InternalFrameEvent.INTERNAL_FRAME_CLOSING));
	}
	
	/**
	 * @return the instance for this frame.
	 */
	public DoomToolsApplicationInstance getInstance() 
	{
		return instance;
	}
	
}
