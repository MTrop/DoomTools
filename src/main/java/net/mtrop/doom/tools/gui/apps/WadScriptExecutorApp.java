package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.apps.data.ExecutionSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.WadScriptSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.WadScriptExecuteWithArgsPanel;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;

import static javax.swing.BorderFactory.*;

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

	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private WadScriptSettingsManager settings;
	private AppCommon appCommon;
	
	// Referenced Components
	
	private JFormField<File> sourceFileField;
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
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = WadScriptSettingsManager.get();
		this.appCommon = AppCommon.get();
		
		File scriptFile;
		ExecutionSettings settings;
		if (scriptPath != null)
		{
			scriptFile = new File(scriptPath).getAbsoluteFile();
			settings = new ExecutionSettings(scriptFile.getParentFile());
		}
		else
		{
			scriptFile = null;
			settings = new ExecutionSettings();
		}
		
		this.sourceFileField = fileField(
			scriptFile, 
			(current) -> chooseFile(
				getApplicationContainer(),
				language.getText("wadscript.run.stdin.browse.title"), 
				current, 
				language.getText("wadscript.run.stdin.browse.accept") 
			),
			(selected) -> {
				if (selected != null)
					executePanel.setWorkingDirectory(selected.getParentFile());
				else
					executePanel.setWorkingDirectory(null);
			}
		);
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
		return containerOf(dimension(400, 300), createEmptyBorder(8, 8, 8, 8), borderLayout(0, 4), 
			node(BorderLayout.NORTH, form(language.getInteger("wadscript.run.withargs.label.width"))
				.addField(language.getText("wadscript.run.withargs.source"), sourceFileField)
			),
			node(BorderLayout.CENTER, executePanel),
			node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(flowLayout(Flow.TRAILING), 
					node(button(language.getText("wadscript.run.withargs.choice.run"), (c, e) -> onRun())
				))),
				node(BorderLayout.SOUTH, statusPanel)
			))
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wadscript.menu.file",
				utils.createItemFromLanguageKey("wadscript.menu.file.item.exit", (c, e) -> attemptClose())
			)
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return null;
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
		File workingDirectory = executePanel.getWorkingDirectory();
		File standardInPath = executePanel.getStandardInPath();
		String entryPoint = executePanel.getEntryPoint();
		String[] args = executePanel.getArgs();
		
		if (sourceFile != null)
			state.put("executor.source", sourceFile.getAbsolutePath());
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
		// TODO: Finish this.
	}
	
	// ====================================================================

	private void onRun()
	{
		File scriptFile = sourceFileField.getValue();
		ExecutionSettings executionSettings = new ExecutionSettings();
		executionSettings.setWorkingDirectory(executePanel.getWorkingDirectory());
		executionSettings.setStandardInPath(executePanel.getStandardInPath());
		executionSettings.setEntryPoint(executePanel.getEntryPoint());
		executionSettings.setArgs(executePanel.getArgs());
		appCommon.onExecuteWadScriptWithSettings(getApplicationContainer(), statusPanel, scriptFile, executionSettings);
	}

}
