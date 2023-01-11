/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
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
import net.mtrop.doom.tools.gui.apps.data.WadTexExportSettings;
import net.mtrop.doom.tools.gui.managers.settings.WadTexCompilerSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.WadTexExportPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;
import net.mtrop.doom.util.NameUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The WADTex compiler application.
 * @author Matthew Tropiano
 */
public class WadTexCompilerApp extends DoomToolsApplicationInstance
{
    // Singletons

	private WadTexCompilerSettingsManager settings;
	
	// Referenced Components
	
	private JFormField<File> sourceFileField;
	private WadTexExportPanel exportPanel;
	private DoomToolsStatusPanel statusPanel;

	/**
	 * Create a new WADTex application.
	 */
	public WadTexCompilerApp() 
	{
		this(null);
	}
	
	/**
	 * Create a new WADTex application.
	 * @param sourcePath the source file path.
	 */
	public WadTexCompilerApp(String sourcePath) 
	{
		this.settings = WadTexCompilerSettingsManager.get();
		
		File scriptFile;
		WadTexExportSettings exportSettings = new WadTexExportSettings();
		if (sourcePath != null)
		{
			scriptFile = FileUtils.canonizeFile(new File(sourcePath));
			exportSettings.setNameOverride(NameUtils.toValidEntryName(scriptFile.getName()));
		}
		else
		{
			scriptFile = null;
			exportSettings.setNameOverride("TEXTURE1");
		}
		
		this.sourceFileField = fileField(
			scriptFile, 
			(current) -> {
				current = current != null ? current : settings.getLastTouchedFile();
				return chooseFile(
					getApplicationContainer(),
					language.getText("wadtex.export.source.browse.title"), 
					current, 
					language.getText("wadtex.export.source.browse.accept"),
					utils.createTextFileFilter()
				);
			},
			(selected) -> settings.setLastTouchedFile(selected)
		);

		this.exportPanel = new WadTexExportPanel(exportSettings);
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle() 
	{
		return language.getText("wadtex.compiler.title");
	}

	@Override
	public Map<String, String> getApplicationState() 
	{
		Map<String, String> state = super.getApplicationState();
		
		File sourceFile = sourceFileField.getValue();
		
		File outWADFile = exportPanel.getOutputWAD();
		String nameOverride = exportPanel.getNameOverride();
		boolean appendMode = exportPanel.getAppendMode();
		boolean forceStrife = exportPanel.getForceStrife();
		
		if (sourceFile != null)
			state.put("export.source", sourceFile.getAbsolutePath());
		if (outWADFile != null)
			state.put("export.outwad", outWADFile.getAbsolutePath());
		if (nameOverride != null)
			state.put("export.name", nameOverride);
		state.put("export.append", String.valueOf(appendMode));
		state.put("export.strife", String.valueOf(forceStrife));
		
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		Function<String, File> parseFile = (input) -> ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input));
		
		sourceFileField.setValue(ValueUtils.parse(state.get("export.source"), parseFile));
		exportPanel.setOutputWAD(ValueUtils.parse(state.get("export.outwad"), parseFile));
		exportPanel.setNameOverride(state.get("export.name"));
		exportPanel.setAppendMode(ValueUtils.parseBoolean(state.get("export.append"), false));
		exportPanel.setForceStrife(ValueUtils.parseBoolean(state.get("export.strife"), false));
	}

	@Override
	public Container createContentPane() 
	{
		return containerOf(dimension(400, 200), borderLayout(0, 4), 
			node(BorderLayout.NORTH, utils.createForm(form(language.getInteger("wadtex.export.label.width")),
				utils.formField("wswantbl.export.source", sourceFileField)
			)),
			node(BorderLayout.CENTER, exportPanel),
			node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(flowLayout(Flow.TRAILING), 
					node(utils.createButtonFromLanguageKey("wadtex.export.choice.export", (i) -> onRun())
				))),
				node(BorderLayout.SOUTH, statusPanel)
			))
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wadtex.menu.file",
				utils.createItemFromLanguageKey("wadtex.menu.file.item.exit", (i) -> attemptClose())
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
		statusPanel.setSuccessMessage(language.getText("wadtex.status.message.ready"));
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

	
	// ====================================================================

	private void onRun()
	{
		File scriptFile = sourceFileField.getValue();
		WadTexExportSettings exportSettings = new WadTexExportSettings();
		exportSettings.setOutputWAD(exportPanel.getOutputWAD());
		exportSettings.setNameOverride(exportPanel.getNameOverride());
		exportSettings.setAppendMode(exportPanel.getAppendMode());
		exportSettings.setForceStrife(exportPanel.getForceStrife());
		
		if (scriptFile == null)
		{
			SwingUtils.error(language.getText("wadtex.error.nosource"));
			sourceFileField.requestFocus();
			return;
		}
		
		if (exportSettings.getOutputWAD() == null)
		{
			SwingUtils.error(language.getText("wadtex.error.notarget"));
			return;
		}
		
		appCommon.onExecuteWadTex(getApplicationContainer(), statusPanel, scriptFile, exportSettings);
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
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-wadtex.md")).open();
	}

}
