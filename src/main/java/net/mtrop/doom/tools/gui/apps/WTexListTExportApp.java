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
import net.mtrop.doom.tools.gui.managers.settings.WTexListSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.WTExportParameterFieldsPanel;
import net.mtrop.doom.tools.gui.swing.panels.WTexListParametersPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The WTexList-WTexport pipe GUI application.
 * @author Matthew Tropiano
 */
public class WTexListTExportApp extends DoomToolsApplicationInstance
{
	private WTexListSettingsManager settings;
	
    /** Parameter panel. */
    private WTexListParametersPanel texListParametersPanel;
    /** Parameter panel. */
    private WTExportParameterFieldsPanel texportParameterFieldsPanel;
    /** Status message. */
    private DoomToolsStatusPanel statusPanel;
    
	/**
	 * Creates a new WTexScan app instance.
	 */
	public WTexListTExportApp() 
	{
		this.settings = WTexListSettingsManager.get();
		
		this.texListParametersPanel = new WTexListParametersPanel();
		this.texportParameterFieldsPanel = new WTExportParameterFieldsPanel();
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle()
	{
		return language.getText("wlisttex.title");
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState();

		File[] files = texListParametersPanel.getFiles();
		TexScanOutputMode mode = texListParametersPanel.getOutputMode();
		boolean noMessages = texListParametersPanel.getNoCommentMessages();

		state.put("wtexlist.files.count", String.valueOf(files.length));
		for (int i = 0; i < files.length; i++) 
			state.put("wtexlist.files." + i, files[i].getAbsolutePath());
			
		state.put("wtexlist.mode", mode.name());
		state.put("wtexlist.nomessages", String.valueOf(noMessages));
		
		File baseWad = texportParameterFieldsPanel.getBaseWad();
		File outputWad = texportParameterFieldsPanel.getOutputWad();
		boolean create = texportParameterFieldsPanel.getCreate();
		String nullTexture = texportParameterFieldsPanel.getNullTexture();
		boolean noAnim = texportParameterFieldsPanel.getNoAnimated();
		boolean noSwitch = texportParameterFieldsPanel.getNoSwitches();

		state.put("wtexport.basewad", baseWad.getAbsolutePath());
		state.put("wtexport.outputwad", outputWad.getAbsolutePath());
		state.put("wtexport.create", String.valueOf(create));
		state.put("wtexport.nulltexture", nullTexture != null ? nullTexture : "");
		state.put("wtexport.noanim", String.valueOf(noAnim));
		state.put("wtexport.noswitch", String.valueOf(noSwitch));

		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		int texScanCount = ValueUtils.parseInt(state.get("wtexlist.files.count"), 0);

		File[] texScanFiles = new File[texScanCount];
		
		final Function<String, File> fileParse = (input) -> ObjectUtils.isEmpty(input) ? null : new File(input);
		final Function<String, TexScanOutputMode> modeParse = (input) -> EnumUtils.getEnumInstance(input, TexScanOutputMode.class);
		
		for (int i = 0; i < texScanFiles.length; i++) 
			texScanFiles[i] = ValueUtils.parse(state.get("wtexscan.files." + i), fileParse);

		texListParametersPanel.setFiles(texScanFiles);
		texListParametersPanel.setOutputMode(ValueUtils.parse(state.get("wtexlist.mode"), modeParse));
		texListParametersPanel.setNoCommentMessages(ValueUtils.parseBoolean(state.get("wtexlist.nomessages"), false));
		
		int texportCount = ValueUtils.parseInt(state.get("wtexport.files.count"), 0);
		File[] texportFiles = new File[texportCount];
		for (int i = 0; i < texportFiles.length; i++) 
			texportFiles[i] = ValueUtils.parse(state.get("wtexport.files." + i), fileParse);

		texListParametersPanel.setFiles(texportFiles);
		texportParameterFieldsPanel.setBaseWad(ValueUtils.parse(state.get("wtexport.basewad"), fileParse));
		texportParameterFieldsPanel.setOutputWad(ValueUtils.parse(state.get("wtexport.outputwad"), fileParse));
		texportParameterFieldsPanel.setCreate(ValueUtils.parseBoolean(state.get("wtexport.create"), false));
		texportParameterFieldsPanel.setNullTexture(ValueUtils.parse(state.get("wtexport.nulltexture"), (input) -> (ObjectUtils.isEmpty(input) ? null : input)));
		texportParameterFieldsPanel.setNoAnimated(ValueUtils.parseBoolean(state.get("wtexport.noanim"), false));
		texportParameterFieldsPanel.setNoSwitches(ValueUtils.parseBoolean(state.get("wtexport.noswitch"), false));
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wlisttex.menu.file",
				utils.createItemFromLanguageKey("wlisttex.menu.file.item.exit", (i) -> attemptClose())
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
		return containerOf(dimension(640, 450), borderLayout(0, 4),
			node(BorderLayout.CENTER, containerOf(gridLayout(1, 2, 4, 0),
				node(utils.createTitlePanel(language.getText("wtexlist.title"), texListParametersPanel)),
				node(containerOf(borderLayout(),
					node(BorderLayout.NORTH, utils.createTitlePanel(language.getText("wtexport.title"), texportParameterFieldsPanel)),
					node(BorderLayout.CENTER, containerOf())
				))
			)),
			node(BorderLayout.SOUTH, containerOf(
				node(BorderLayout.CENTER, statusPanel),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.TRAILING),
					node(utils.createButtonFromLanguageKey("wlisttex.button.start", (i) -> onDoPipe()))
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
		statusPanel.setSuccessMessage(language.getText("wlisttex.status.message.ready"));
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
	
	private void onDoPipe() 
	{
		File[] files = texListParametersPanel.getFiles();
		TexScanOutputMode mode = texListParametersPanel.getOutputMode();
		boolean noMessages = texListParametersPanel.getNoCommentMessages();
		
		File baseWad = texportParameterFieldsPanel.getBaseWad();
		File outputWad = texportParameterFieldsPanel.getOutputWad();
		boolean create = texportParameterFieldsPanel.getCreate();
		String nullTexture = texportParameterFieldsPanel.getNullTexture();
		boolean noAnim = texportParameterFieldsPanel.getNoAnimated();
		boolean noSwitch = texportParameterFieldsPanel.getNoSwitches();
		
		if (outputWad == null)
		{
			SwingUtils.info(getApplicationContainer(), language.getText("wtexport.nooutwad.message"));
			return;
		}
		
		if (outputWad.exists() && create)
		{
			if (SwingUtils.noTo(getApplicationContainer(), language.getText("wtexport.overwrite.message", outputWad.getName())))
				return;
		}

		appCommon.onExecuteWTexListToWTExport(getApplicationContainer(), statusPanel, 
			files, 
			mode, 
			noMessages,
			baseWad, 
			outputWad, 
			create,
			noAnim,
			noSwitch,
			nullTexture
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
		utils.createHelpModal(
			utils.helpResource("docs/changelogs/CHANGELOG-wtexlist.md"),
			utils.helpResource("docs/changelogs/CHANGELOG-wtexport.md")
		).open();
	}
	
}
