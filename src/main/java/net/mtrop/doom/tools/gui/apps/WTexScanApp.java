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

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.AppCommon.TexScanOutputMode;
import net.mtrop.doom.tools.gui.managers.settings.WTexScanSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.WTexScanParametersPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.menuBar;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The WTexScanApp GUI application.
 * @author Matthew Tropiano
 */
public class WTexScanApp extends DoomToolsApplicationInstance
{
	private WTexScanSettingsManager settings;
	
	/** Parameter panel. */
	private WTexScanParametersPanel parametersPanel;
	/** Status message. */
	private DoomToolsStatusPanel statusPanel;
	
	/**
	 * Creates a new WTexScan app instance.
	 */
	public WTexScanApp() 
	{
		this.settings = WTexScanSettingsManager.get();
		
		this.parametersPanel = new WTexScanParametersPanel();
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle()
	{
		return language.getText("wtexscan.title");
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState();

		File[] files = parametersPanel.getFiles();
		TexScanOutputMode mode = parametersPanel.getOutputMode();
		boolean noSkies = parametersPanel.getSkipSkies();
		boolean noMessages = parametersPanel.getNoCommentMessages();
		String mapName = parametersPanel.getMapName();

		state.put("files.count", String.valueOf(files.length));
		for (int i = 0; i < files.length; i++) 
			state.put("files." + i, files[i].getAbsolutePath());
			
		state.put("mode", mode.name());
		state.put("noskies", String.valueOf(noSkies));
		state.put("nomessages", String.valueOf(noMessages));
		state.put("mapname", mapName != null ? mapName : "");
		
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		int count = ValueUtils.parseInt(state.get("files.count"), 0);

		File[] files = new File[count];
		
		final Function<String, File> fileParse = (input) -> new File(input);
		final Function<String, TexScanOutputMode> modeParse = (input) -> EnumUtils.getEnumInstance(input, TexScanOutputMode.class);
		
		for (int i = 0; i < files.length; i++) 
			files[i] = ValueUtils.parse(state.get("files." + i), fileParse);

		parametersPanel.setFiles(files);
		parametersPanel.setOutputMode(ValueUtils.parse(state.get("mode"), modeParse));
		parametersPanel.setSkipSkies(ValueUtils.parseBoolean(state.get("noskies"), false));
		parametersPanel.setNoCommentMessages(ValueUtils.parseBoolean(state.get("nomessages"), false));
		parametersPanel.setMapName(state.get("mapname"));
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wtexscan.menu.file",
				utils.createItemFromLanguageKey("wtexscan.menu.file.item.exit", (i) -> attemptClose())
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
	public Container createContentPane()
	{
		return containerOf(dimension(320, 450), borderLayout(0, 4),
			node(BorderLayout.CENTER, parametersPanel),
			node(BorderLayout.SOUTH, containerOf(
				node(BorderLayout.CENTER, statusPanel),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.TRAILING),
					node(utils.createButtonFromLanguageKey("wtexscan.button.start", (i) -> onScanMaps()))
				))
			))
		);
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
		statusPanel.setSuccessMessage(language.getText("wtexscan.status.message.ready"));
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
	
	private void onScanMaps() 
	{
		File[] files = parametersPanel.getFiles();
		TexScanOutputMode mode = parametersPanel.getOutputMode();
		boolean noSkies = parametersPanel.getSkipSkies();
		boolean noMessages = parametersPanel.getNoCommentMessages();
		String mapName = parametersPanel.getMapName();
		
		appCommon.onExecuteWTexScan(getApplicationContainer(), statusPanel, 
			files, 
			mode, 
			noSkies, 
			noMessages,
			mapName
		);	
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog())
		); 
	}

	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-wtexscan.md")).open();
	}
	
}
