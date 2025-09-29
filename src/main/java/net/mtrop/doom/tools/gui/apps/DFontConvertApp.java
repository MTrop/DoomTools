/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.DoomFontConvertMain;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.settings.DImageConvertSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * DImageConverter application.
 * @author Matthew Tropiano
 */
public class DFontConvertApp extends DoomToolsApplicationInstance 
{
	private DImageConvertSettingsManager settings;

	/** Status message. */
	private DoomToolsStatusPanel statusPanel;
	
	public DFontConvertApp()
	{
		this.settings = DImageConvertSettingsManager.get();
		
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle() 
	{
		return language.getText("dfonconv.title");
	}

	@Override
	public Container createContentPane()
	{
		DoomToolsGUIUtils utils = DoomToolsGUIUtils.get();
		return containerOf(dimension(350, 242), borderLayout(0, 4),
			node(BorderLayout.CENTER, containerOf()),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("dfonconv.menu.file",
				utils.createItemFromLanguageKey("dfonconv.menu.file.item.exit", (i) -> attemptClose())
			),
			createHelpMenu()
		);
	}

	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(createHelpMenu());
	}

	@Override
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState();
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		Function<String, File> parseFile = (input) -> ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input));

	}

	@Override
	public void onCreate(Object frame) 
	{
		if (frame instanceof JFrame)
		{
			JFrame f = (JFrame)frame;
			Rectangle bounds = settings.getBounds();
			boolean maximized = settings.getBoundsMaximized();
			f.setBounds(bounds);
			if (maximized)
				f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
	}

	@Override
	public void onOpen(Object frame) 
	{
		statusPanel.setSuccessMessage(language.getText("dfonconv.status.message.ready"));
	}

	@Override
	public boolean shouldClose(Object frame, boolean fromWorkspaceClear) 
	{
		return fromWorkspaceClear || SwingUtils.yesTo(language.getText("doomtools.application.close"));
	}

	@Override
	public void onClose(Object frame) 
	{
		if (frame instanceof JFrame)
		{
			JFrame f = (JFrame)frame;
			settings.setBounds(f);
		}
	}

	private void onDoConversion() 
	{
		
		appCommon.onExecuteDFONConv(getApplicationContainer(), statusPanel);
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.help", (i) -> onHelp()),
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog())
		); 
	}

	private void onHelp()
	{
		utils.createHelpModal(utils.helpProcess(DoomFontConvertMain.class, "--help")).open();
	}

	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-dfonconv.md")).open();
	}
	
}
