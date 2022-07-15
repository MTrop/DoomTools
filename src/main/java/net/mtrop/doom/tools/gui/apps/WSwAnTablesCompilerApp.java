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
import net.mtrop.doom.tools.gui.apps.data.DefSwAniExportSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.settings.WSwAnTablesCompilerSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DefSwAniExportPanel;
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
public class WSwAnTablesCompilerApp extends DoomToolsApplicationInstance
{
    // Singletons

	private WSwAnTablesCompilerSettingsManager settings;
	
	// Referenced Components
	
	private JFormField<File> sourceFileField;
	private DefSwAniExportPanel exportPanel;
	private DoomToolsStatusPanel statusPanel;

	/**
	 * Create a new DECOHack application.
	 */
	public WSwAnTablesCompilerApp() 
	{
		this(null);
	}
	
	/**
	 * Create a new DECOHack application.
	 * @param sourcePath the source file path.
	 */
	public WSwAnTablesCompilerApp(String sourcePath) 
	{
		this.settings = WSwAnTablesCompilerSettingsManager.get();
		
		File scriptFile;
		DefSwAniExportSettings settings = new DefSwAniExportSettings();
		if (sourcePath != null)
			scriptFile = FileUtils.canonizeFile(new File(sourcePath));
		else
			scriptFile = null;
		
		this.sourceFileField = fileField(
			scriptFile, 
			(current) -> chooseFile(
				getApplicationContainer(),
				getLanguage().getText("wswantbl.export.source.browse.title"), 
				current, 
				getLanguage().getText("wswantbl.export.source.browse.accept"),
				getUtils().createDEFSWANIFileFilter()
			)
		);

		this.exportPanel = new DefSwAniExportPanel(settings);
		this.statusPanel = new DoomToolsStatusPanel();
	}
	
	@Override
	public String getTitle() 
	{
		return getLanguage().getText("wswantbl.compiler.title");
	}

	@Override
	public Container createContentPane() 
	{
		DoomToolsGUIUtils utils = getUtils();
		
		return containerOf(dimension(400, 150), borderLayout(0, 4), 
			node(BorderLayout.NORTH, utils.createForm(form(getLanguage().getInteger("wswantbl.export.label.width")),
				utils.formField("wswantbl.export.source", sourceFileField)
			)),
			node(BorderLayout.CENTER, exportPanel),
			node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(flowLayout(Flow.TRAILING), 
					node(utils.createButtonFromLanguageKey("wswantbl.export.choice.export", (i) -> onRun())
				))),
				node(BorderLayout.SOUTH, statusPanel)
			))
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		DoomToolsGUIUtils utils = getUtils();
		
		return menuBar(
			utils.createMenuFromLanguageKey("wswantbl.menu.file",
				utils.createItemFromLanguageKey("wswantbl.menu.file.item.exit", (i) -> attemptClose())
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
		statusPanel.setSuccessMessage(getLanguage().getText("wswantbl.status.message.ready"));
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
		File outWADFile = exportPanel.getOutputWAD();
		boolean outputSource = exportPanel.getOutputSource();
		
		if (sourceFile != null)
			state.put("export.source", sourceFile.getAbsolutePath());
		if (outWADFile != null)
			state.put("export.outwad", outWADFile.getAbsolutePath());
		state.put("export.outputsource", String.valueOf(outputSource));
		
		return state;
	}
	
	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		Function<String, File> parseFile = (input) -> ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input));
		sourceFileField.setValue(ValueUtils.parse(state.get("export.source"), parseFile));
		exportPanel.setOutputWAD(ValueUtils.parse(state.get("export.outwad"), parseFile));
		exportPanel.setOutputSource(ValueUtils.parseBoolean(state.get("export.outputsource"), false));
	}
	
	// ====================================================================

	private void onRun()
	{
		File scriptFile = sourceFileField.getValue();
		DefSwAniExportSettings exportSettings = new DefSwAniExportSettings();
		exportSettings.setOutputWAD(exportPanel.getOutputWAD());
		exportSettings.setOutputSource(exportPanel.getOutputSource());
		getCommon().onExecuteWSwAnTbl(getApplicationContainer(), statusPanel, scriptFile, exportSettings);
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		DoomToolsGUIUtils utils = getUtils();
	
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog())
		); 
	}

	private void onHelpChangelog()
	{
		getUtils().createHelpModal(getUtils().helpResource("docs/changelogs/CHANGELOG-wswantbl.md")).open();
	}

}
