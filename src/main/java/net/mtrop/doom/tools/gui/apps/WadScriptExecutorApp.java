/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.apps.data.ScriptExecutionSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.settings.WadScriptExecutorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.WadScriptExecuteWithArgsPanel;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.chooseFile;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The WadScript executor application.
 * @author Matthew Tropiano
 */
public class WadScriptExecutorApp extends DoomToolsApplicationInstance
{
    // Singletons

	private WadScriptExecutorSettingsManager settings;
	private DoomToolsEditorProvider editorProvider;
	
	// Referenced Components
	
	private JFormField<File> sourceFileField;
	private JFormField<Charset> charsetField;
	private WadScriptExecuteWithArgsPanel executePanel;
	private DoomToolsStatusPanel statusPanel;

	/**
	 * Create a new WadScript application.
	 */
	public WadScriptExecutorApp() 
	{
		this(null);
	}
	
	/**
	 * Create a new WadScript application.
	 * @param scriptPath the script path.
	 */
	public WadScriptExecutorApp(String scriptPath) 
	{
		this.settings = WadScriptExecutorSettingsManager.get();
		this.editorProvider = DoomToolsEditorProvider.get();
		
		File scriptFile;
		ScriptExecutionSettings settings;
		if (scriptPath != null)
		{
			scriptFile = FileUtils.canonizeFile(new File(scriptPath));
			settings = new ScriptExecutionSettings(scriptFile.getParentFile());
		}
		else
		{
			scriptFile = null;
			settings = new ScriptExecutionSettings();
		}
		
		this.sourceFileField = fileField(
			scriptFile, 
			(current) -> chooseFile(
				getApplicationContainer(),
				language.getText("wadscript.run.source.browse.title"), 
				current, 
				language.getText("wadscript.run.source.browse.accept"),
				utils.createWadScriptFileFilter()
			),
			(selected) -> {
				if (selected != null)
					executePanel.setWorkingDirectory(selected.getParentFile());
				else
					executePanel.setWorkingDirectory(null);
			}
		);
		
		this.charsetField = comboField(comboBox(editorProvider.getAvailableCommonCharsets()));
		this.charsetField.setValue(Charset.defaultCharset());
		
		this.executePanel = new WadScriptExecuteWithArgsPanel(settings);
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle() 
	{
		return language.getText("wadscript.executor.title");
	}

	@Override
	public Container createContentPane() 
	{
		return containerOf(dimension(400, 400), borderLayout(0, 4), 
			node(BorderLayout.NORTH, utils.createForm(form(language.getInteger("wadscript.run.withargs.label.width")),
				utils.formField("wadscript.run.withargs.source", sourceFileField),
				utils.formField("wadscript.run.withargs.charset", charsetField)
			)),
			node(BorderLayout.CENTER, executePanel),
			node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(flowLayout(Flow.TRAILING), 
					node(utils.createButtonFromLanguageKey("wadscript.run.withargs.choice.run", (i) -> onRun()))
				)),
				node(BorderLayout.SOUTH, statusPanel)
			))
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wadscript.menu.file",
				utils.createItemFromLanguageKey("wadscript.menu.file.item.exit", (i) -> attemptClose())
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
		statusPanel.setSuccessMessage(language.getText("wadscript.status.message.ready"));
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
	
	@Override
	public Map<String, String> getApplicationState() 
	{
		Map<String, String> state = super.getApplicationState();
		
		File sourceFile = sourceFileField.getValue();
		Charset encoding = charsetField.getValue(); 
		
		File workingDirectory = executePanel.getWorkingDirectory();
		File standardInPath = executePanel.getStandardInPath();
		String entryPoint = executePanel.getEntryPoint();
		String[] args = executePanel.getArgs();
		
		if (sourceFile != null)
			state.put("executor.source", sourceFile.getAbsolutePath());
		if (encoding != null)
			state.put("executor.charset", encoding.displayName());
		if (workingDirectory != null)
			state.put("executor.workdir", workingDirectory.getAbsolutePath());
		if (standardInPath != null)
			state.put("executor.stdin", standardInPath.getAbsolutePath());
		state.put("executor.entryPoint", entryPoint);
		state.put("executor.args", String.valueOf(args.length));
		for (int a = 0; a < args.length; a++)
			state.put("executor.args." + a, args[a]);
		
		return state;
	}
	
	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		String[] args = new String[ValueUtils.parseInt(state.get("executor.args"), 0)];
		for (int a = 0; a < args.length; a++)
			args[a] = state.getOrDefault("executor.args." + a, "");
		
		final Function<String, File> parseFile = (value) -> ObjectUtils.isEmpty(value) ? null : FileUtils.canonizeFile(new File(value));

		sourceFileField.setValue(ValueUtils.parse(state.get("executor.source"), parseFile));
		charsetField.setValue(ValueUtils.parse(state.get("executor.charset"), (value) -> Charset.forName(value)));
		
		executePanel.setWorkingDirectory(ValueUtils.parse(state.get("executor.workdir"), parseFile));
		executePanel.setStandardInPath(ValueUtils.parse(state.get("executor.stdin"), parseFile));
		executePanel.setEntryPoint(state.get("executor.entryPoint"));
		executePanel.setArgs(args);
	}
	
	
	// ====================================================================

	private void onRun()
	{
		File scriptFile = sourceFileField.getValue();
		Charset encoding = charsetField.getValue();
		ScriptExecutionSettings executionSettings = new ScriptExecutionSettings();
		executionSettings.setWorkingDirectory(executePanel.getWorkingDirectory());
		executionSettings.setStandardInPath(executePanel.getStandardInPath());
		executionSettings.setEntryPoint(executePanel.getEntryPoint());
		executionSettings.setArgs(executePanel.getArgs());
		appCommon.onExecuteWadScript(getApplicationContainer(), statusPanel, scriptFile, encoding, executionSettings);
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
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-wadscript.md")).open();
	}

}
