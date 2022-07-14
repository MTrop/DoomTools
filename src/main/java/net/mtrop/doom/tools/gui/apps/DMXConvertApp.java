package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.settings.DMXConvertSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DMXConvertSettingsPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.FileListPanel;

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
	private FileListPanel inputFiles;
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
    	
		this.inputFiles = new FileListPanel(getLanguage().getText("dmxconv.input"), 
			ListSelectionMode.MULTIPLE_INTERVAL, false, true, 
			(files) -> {
				if (files != null && files.length > 0)
					settings.setLastTouchedFile(files[files.length - 1]);
			},
			() -> settings.getLastTouchedFile()
		);
		
		this.outputDirectory = fileField(
			(current) -> getUtils().chooseDirectory(
				getApplicationContainer(), 
				getLanguage().getText("dmxconv.outputdir.browse.title"), 
				getLanguage().getText("dmxconv.outputdir.browse.accept"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile
			)
		);
		
		JRadioButton normalButton = getUtils().createRadioButtonFromLanguageKey("dmxconv.programs.nopreference", true);
		JRadioButton ffmpegOnly = getUtils().createRadioButtonFromLanguageKey("dmxconv.programs.ffmpegonly", false);
		JRadioButton jspiOnly = getUtils().createRadioButtonFromLanguageKey("dmxconv.programs.jspionly", false);
		
		group(normalButton, ffmpegOnly, jspiOnly);
		
		this.normalConversionField = radioField(normalButton);
		this.ffmpegConversionField = radioField(ffmpegOnly);
		this.jspiConversionField = radioField(jspiOnly);
		
		this.statusPanel = new DoomToolsStatusPanel();
    }
    
	@Override
	public String getTitle() 
	{
		return getLanguage().getText("dmxconv.title");
	}

	@Override
	public Container createContentPane()
	{
		return containerOf(dimension(350, 300), borderLayout(0, 4),
			node(BorderLayout.CENTER, inputFiles),
			node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(borderLayout(0, 4),
					node(BorderLayout.NORTH, getUtils().createTitlePanel(getLanguage().getText("dmxconv.programs"), containerOf(gridLayout(1, 3, 0, 4),
						node(normalConversionField), node(ffmpegConversionField), node(jspiConversionField)
					))),
					node(BorderLayout.CENTER, getUtils().createForm(form(getLanguage().getInteger("dmxconv.label.width")),
						getUtils().formField("dmxconv.outputdir", outputDirectory)
					)),
					node(BorderLayout.SOUTH, containerOf(flowLayout(Flow.TRAILING), 
						node(getUtils().createButtonFromLanguageKey("dmxconv.convert", (c, e) -> onDoConversion())
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
		DoomToolsGUIUtils utils = getUtils();
		
		return menuBar(
			utils.createMenuFromLanguageKey("dmxconv.menu.file",
				utils.createItemFromLanguageKey("dmxconv.menu.file.item.settings", (c, e) -> openSettings()),
				separator(),
				utils.createItemFromLanguageKey("dmxconv.menu.file.item.exit", (c, e) -> attemptClose())
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
		statusPanel.setSuccessMessage(getLanguage().getText("dmxconv.status.message.ready"));
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
		File[] inputFile = inputFiles.getFiles();
		File outputDir = outputDirectory.getValue();
		File ffmpegPath = settings.getFFmpegPath();
		Boolean ffmpegOnly = 
			normalConversionField.getValue() ? null :
			ffmpegConversionField.getValue() ? true :
			jspiConversionField.getValue() ? false :
			null;
		
		getCommon().onExecuteDMXConv(getApplicationContainer(), statusPanel, inputFile, ffmpegPath, ffmpegOnly, outputDir);
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		DoomToolsGUIUtils utils = getUtils();
	
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (c, e) -> onHelpChangelog())
		); 
	}

	// Open settings.
	private void openSettings()
	{
		modal(
			getApplicationContainer(),
			getLanguage().getText("dmxconv.settings.title"),
			new DMXConvertSettingsPanel()
		).openThenDispose();
	}

	private void onHelpChangelog()
	{
		getUtils().createHelpModal(getUtils().helpResource("docs/changelogs/CHANGELOG-dmxconv.md")).open();
	}
	
	
	
}
