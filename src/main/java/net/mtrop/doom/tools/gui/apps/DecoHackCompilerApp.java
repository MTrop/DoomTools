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
import net.mtrop.doom.tools.gui.apps.data.PatchExportSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.DecoHackCompilerSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DecoHackExportPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The DECOHack compiler application.
 * @author Matthew Tropiano
 */
public class DecoHackCompilerApp extends DoomToolsApplicationInstance
{
    // Singletons

	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private DecoHackCompilerSettingsManager settings;
	private DoomToolsEditorProvider editorProvider;
	private AppCommon appCommon;
	
	// Referenced Components
	
	private JFormField<File> sourceFileField;
	private JFormField<Charset> charsetField;
	private DecoHackExportPanel exportPanel;
	private DoomToolsStatusPanel statusPanel;

	/**
	 * Create a new DECOHack application.
	 */
	public DecoHackCompilerApp() 
	{
		this(null);
	}
	
	/**
	 * Create a new DECOHack application.
	 * @param sourcePath the source file path.
	 */
	public DecoHackCompilerApp(String sourcePath) 
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = DecoHackCompilerSettingsManager.get();
		this.editorProvider = DoomToolsEditorProvider.get();
		this.appCommon = AppCommon.get();
		
		File scriptFile;
		PatchExportSettings settings;
		if (sourcePath != null)
		{
			scriptFile = FileUtils.canonizeFile(new File(sourcePath));
			settings = new PatchExportSettings(scriptFile.getParentFile());
		}
		else
		{
			scriptFile = null;
			settings = new PatchExportSettings();
		}
		
		this.sourceFileField = fileField(
			scriptFile, 
			(current) -> chooseFile(
				getApplicationContainer(),
				language.getText("decohack.export.source.browse.title"), 
				current, 
				language.getText("decohack.export.source.browse.accept"),
				utils.getDecoHackFileFilter() 
			),
			(selected) -> {
				if (selected != null)
					exportPanel.setPatchOutput(new File(selected.getParent() + File.separator + "dehacked.deh"));
				else
					exportPanel.setPatchOutput(null);
			}
		);

		this.charsetField = comboField(comboBox(editorProvider.getAvailableCommonCharsets()));
		this.charsetField.setValue(Charset.defaultCharset());
		
		this.exportPanel = new DecoHackExportPanel(settings);
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle() 
	{
		return language.getText("decohack.compiler.title");
	}

	@Override
	public Container createContentPane() 
	{
		return containerOf(dimension(400, 200), borderLayout(0, 4), 
			node(BorderLayout.NORTH, utils.createFormField(form(language.getInteger("decohack.export.label.width")),
				utils.formField("decohack.export.source", sourceFileField),
				utils.formField("decohack.export.charset", charsetField)
			)),
			node(BorderLayout.CENTER, exportPanel),
			node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(flowLayout(Flow.TRAILING), 
					node(utils.createButtonFromLanguageKey("decohack.export.choice.export", (c, e) -> onRun())
				))),
				node(BorderLayout.SOUTH, statusPanel)
			))
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("decohack.menu.file",
				utils.createItemFromLanguageKey("decohack.menu.file.item.exit", (c, e) -> attemptClose())
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
		Charset encoding = charsetField.getValue(); 
		File patchOut = exportPanel.getPatchOutput();
		File sourceOut = exportPanel.getSourceOutput();
		boolean budget = exportPanel.getBudget();
		
		if (sourceFile != null)
			state.put("export.source", sourceFile.getAbsolutePath());
		if (encoding != null)
			state.put("export.charset", encoding.displayName());
		if (patchOut != null)
			state.put("export.patch", patchOut.getAbsolutePath());
		if (sourceOut != null)
			state.put("export.outsource", sourceOut.getAbsolutePath());
		
		state.put("export.budget", String.valueOf(budget));
		
		return state;
	}
	
	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		Function<String, File> parseFile = (input) -> ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input));
		sourceFileField.setValue(ValueUtils.parse(state.get("export.source"), parseFile));
		charsetField.setValue(ValueUtils.parse(state.get("export.charset"), (value) -> Charset.forName(value)));
		exportPanel.setPatchOutput(ValueUtils.parse(state.get("export.patch"), parseFile));
		exportPanel.setSourceOutput(ValueUtils.parse(state.get("export.outsource"), parseFile));
		exportPanel.setBudget(ValueUtils.parseBoolean(state.get("export.budget"), false));
	}
	
	// ====================================================================

	private void onRun()
	{
		File scriptFile = sourceFileField.getValue();
		Charset encoding = charsetField.getValue();
		PatchExportSettings exportSettings = new PatchExportSettings();
		exportSettings.setOutputFile(exportPanel.getPatchOutput());
		exportSettings.setSourceOutputFile(exportPanel.getSourceOutput());
		exportSettings.setOutputBudget(exportPanel.getBudget());
		appCommon.onExecuteDecoHack(getApplicationContainer(), statusPanel, scriptFile, encoding, exportSettings);
	}

}
