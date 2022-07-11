package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.apps.data.MergeSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.WadMergeExecutorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.WadMergeExecuteWithArgsPanel;
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
public class WadMergeExecutorApp extends DoomToolsApplicationInstance
{
    // Singletons

	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private WadMergeExecutorSettingsManager settings;
	private DoomToolsEditorProvider editorProvider;
	private AppCommon appCommon;
	
	// Referenced Components
	
	private JFormField<File> sourceFileField;
	private JFormField<Charset> charsetField;
	private WadMergeExecuteWithArgsPanel executePanel;
	private DoomToolsStatusPanel statusPanel;

	/**
	 * Create a new WadScript application.
	 */
	public WadMergeExecutorApp() 
	{
		this(null);
	}
	
	/**
	 * Create a new WadScript application.
	 * @param scriptPath the script path.
	 */
	public WadMergeExecutorApp(String scriptPath) 
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = WadMergeExecutorSettingsManager.get();
		this.editorProvider = DoomToolsEditorProvider.get();
		this.appCommon = AppCommon.get();
		
		File scriptFile;
		MergeSettings settings;
		if (scriptPath != null)
		{
			scriptFile = FileUtils.canonizeFile(new File(scriptPath));
			settings = new MergeSettings(scriptFile.getParentFile());
		}
		else
		{
			scriptFile = null;
			settings = new MergeSettings();
		}
		
		this.sourceFileField = fileField(
			scriptFile, 
			(current) -> chooseFile(
				getApplicationContainer(),
				language.getText("wadmerge.run.source.browse.title"), 
				current, 
				language.getText("wadmerge.run.source.browse.accept"),
				utils.getWadMergeFileFilter() 
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
		
		this.executePanel = new WadMergeExecuteWithArgsPanel(settings);
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle() 
	{
		return language.getText("wadmerge.executor.title");
	}

	@Override
	public Container createContentPane() 
	{
		return containerOf(dimension(400, 400), borderLayout(0, 4), 
			node(BorderLayout.NORTH, utils.createFormField(form(language.getInteger("wadmerge.run.withargs.label.width")),
				utils.formField("wadmerge.run.withargs.source", sourceFileField),
				utils.formField("wadmerge.run.withargs.charset", charsetField)
			)),
			node(BorderLayout.CENTER, executePanel),
			node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(flowLayout(Flow.TRAILING), 
					node(utils.createButtonFromLanguageKey("wadmerge.run.withargs.choice.run", (c, e) -> onRun()))
				)),
				node(BorderLayout.SOUTH, statusPanel)
			))
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wadmerge.menu.file",
				utils.createItemFromLanguageKey("wadmerge.menu.file.item.exit", (c, e) -> attemptClose())
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
		statusPanel.setSuccessMessage(language.getText("wadmerge.status.message.ready"));
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
		String[] args = executePanel.getArgs();
		
		if (sourceFile != null)
			state.put("executor.source", sourceFile.getAbsolutePath());
		if (encoding != null)
			state.put("executor.charset", encoding.displayName());
		if (workingDirectory != null)
			state.put("executor.workdir", workingDirectory.getAbsolutePath());
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
		executePanel.setArgs(args);
	}
	
	
	// ====================================================================

	private void onRun()
	{
		File scriptFile = sourceFileField.getValue();
		Charset encoding = charsetField.getValue();
		MergeSettings executionSettings = new MergeSettings();
		executionSettings.setWorkingDirectory(executePanel.getWorkingDirectory());
		executionSettings.setArgs(executePanel.getArgs());
		appCommon.onExecuteWadMerge(getApplicationContainer(), statusPanel, scriptFile, encoding, executionSettings);
	}

}
