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
import javax.swing.JRadioButton;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.settings.DMXConvertSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DMXConvertSettingsPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.FileListPanel;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.modal;


/**
 * DMX converter application.
 * @author Matthew Tropiano
 */
public class DMXConvertApp extends DoomToolsApplicationInstance 
{
	private DMXConvertSettingsManager settings;

	/** Input sound files. */
	private FileListPanel inputFileField;
	/** Output directory. */
	private JFormField<File> outputDirectory;
	/** Normal conversion. */
	private JFormField<Boolean> normalConversionField;
	/** FFmpeg conversion. */
	private JFormField<Boolean> ffmpegConversionField;
	/** JSPI conversion. */
	private JFormField<Boolean> jspiConversionField;
    /** Status message. */
    private DoomToolsStatusPanel statusPanel;
    
    public DMXConvertApp()
    {
    	this.settings = DMXConvertSettingsManager.get();
    	
		this.inputFileField = new FileListPanel(language.getText("dmxconv.input"), 
			ListSelectionMode.MULTIPLE_INTERVAL, false, true, 
			(files) -> {
				if (files != null && files.length > 0)
					settings.setLastTouchedFile(files[files.length - 1]);
			},
			() -> settings.getLastTouchedFile()
		);
		
		this.outputDirectory = fileField(
			(current) -> utils.chooseDirectory(
				getApplicationContainer(), 
				language.getText("dmxconv.outputdir.browse.title"), 
				language.getText("dmxconv.outputdir.browse.accept"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile
			)
		);
		
		JRadioButton normalButton = utils.createRadioButtonFromLanguageKey("dmxconv.programs.nopreference", true);
		JRadioButton ffmpegOnly = utils.createRadioButtonFromLanguageKey("dmxconv.programs.ffmpegonly", false);
		JRadioButton jspiOnly = utils.createRadioButtonFromLanguageKey("dmxconv.programs.jspionly", false);
		
		group(normalButton, ffmpegOnly, jspiOnly);
		
		this.normalConversionField = radioField(normalButton);
		this.ffmpegConversionField = radioField(ffmpegOnly);
		this.jspiConversionField = radioField(jspiOnly);
		
		this.statusPanel = new DoomToolsStatusPanel();
    }
    
	@Override
	public String getTitle() 
	{
		return language.getText("dmxconv.title");
	}

	@Override
	public Container createContentPane()
	{
		return containerOf(dimension(350, 300), borderLayout(0, 4),
			node(BorderLayout.CENTER, inputFileField),
			node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(borderLayout(0, 4),
					node(BorderLayout.NORTH, utils.createTitlePanel(language.getText("dmxconv.programs"), containerOf(gridLayout(1, 3, 0, 4),
						node(normalConversionField), node(ffmpegConversionField), node(jspiConversionField)
					))),
					node(BorderLayout.CENTER, utils.createForm(form(language.getInteger("dmxconv.label.width")),
						utils.formField("dmxconv.outputdir", outputDirectory)
					)),
					node(BorderLayout.SOUTH, containerOf(flowLayout(Flow.TRAILING), 
						node(utils.createButtonFromLanguageKey("dmxconv.convert", (i) -> onDoConversion())
					)))
				)),
				node(BorderLayout.CENTER, containerOf()),
				node(BorderLayout.SOUTH, statusPanel)
			))
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("dmxconv.menu.file",
				utils.createItemFromLanguageKey("dmxconv.menu.file.item.settings", (i) -> openSettings()),
				separator(),
				utils.createItemFromLanguageKey("dmxconv.menu.file.item.exit", (i) -> attemptClose())
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
		
		File[] inputFiles = inputFileField.getFiles();
		File outputFile = outputDirectory.getValue();
		Boolean conversionType = getConversionType();
		
		state.put("files.length", String.valueOf(inputFiles.length));
		for (int i = 0; i < inputFiles.length; i++) 
			state.put("files." + i, inputFiles[i].getAbsolutePath());
		
		state.put("output", outputFile.getAbsolutePath());
		
		if (conversionType != null)
			state.put("conversiontype", String.valueOf(conversionType));

		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		Function<String, File> parseFile = (input) -> ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input));
		Function<String, Boolean> parseBoolean = (input) -> ObjectUtils.isEmpty(input) ? null : ValueUtils.parseBoolean(input, false);

		File[] inputFiles = new File[ValueUtils.parseInt(state.get("files.length"), 0)];
		for (int i = 0; i < inputFiles.length; i++)
			inputFiles[i] = ValueUtils.parse(state.get("files." + i), parseFile);
		File outputFile = ValueUtils.parse(state.get("output"), parseFile);
		Boolean conversionType = ValueUtils.parse(state.get("conversiontype"), parseBoolean);

		inputFileField.setFiles(inputFiles);
		outputDirectory.setValue(outputFile);
		
		normalConversionField.setValue(conversionType == null);
		ffmpegConversionField.setValue(conversionType == Boolean.TRUE);
		jspiConversionField.setValue(conversionType == Boolean.FALSE);
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
		statusPanel.setSuccessMessage(language.getText("dmxconv.status.message.ready"));
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
		File[] inputFile = inputFileField.getFiles();
		File outputDir = outputDirectory.getValue();
		File ffmpegPath = settings.getFFmpegPath();
		Boolean ffmpegOnly = getConversionType();
		
		appCommon.onExecuteDMXConv(getApplicationContainer(), statusPanel, inputFile, ffmpegPath, ffmpegOnly, outputDir);
	}

	private Boolean getConversionType()
	{
		return 
			normalConversionField.getValue() ? null :
			ffmpegConversionField.getValue() ? true :
			jspiConversionField.getValue() ? false :
			null;
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog())
		); 
	}

	// Open settings.
	private void openSettings()
	{
		modal(
			getApplicationContainer(),
			language.getText("dmxconv.settings.title"),
			new DMXConvertSettingsPanel()
		).openThenDispose();
	}

	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-dmxconv.md")).open();
	}
	
	
	
}
