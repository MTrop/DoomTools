package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;

import net.mtrop.doom.tools.DoomImageConvertMain;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.AppCommon.GraphicsMode;
import net.mtrop.doom.tools.gui.managers.settings.DImageConvertSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * DImageConverter application.
 * @author Matthew Tropiano
 */
public class DImageConvertApp extends DoomToolsApplicationInstance 
{
	private DImageConvertSettingsManager settings;

	/** Input file. */
	private JFormField<File> inputFileField;
	/** Output file. */
	private JFormField<File> outputFileField;
	/** Recursive. */
	private JFormField<Boolean> recursiveField;
	/** Palette source file. */
	private JFormField<File> paletteSourceField;
	/** Graphics mode. */
	private JFormField<Boolean> graphicModeField;
	/** Flats mode. */
	private JFormField<Boolean> flatsModeField;
	/** Colormap mode. */
	private JFormField<Boolean> colormapModeField;
	/** Palette mode. */
	private JFormField<Boolean> paletteModeField;
	/** Info file name. */
	private JFormField<String> infoFileNameField;
	
    /** Status message. */
    private DoomToolsStatusPanel statusPanel;
    
    public DImageConvertApp()
    {
    	this.settings = DImageConvertSettingsManager.get();
    	
		this.inputFileField = fileField(
			(current) -> getUtils().chooseFileOrDirectory(
				getApplicationContainer(), 
				getLanguage().getText("dimgconv.inputfile.browse.title"), 
				getLanguage().getText("dimgconv.inputfile.browse.accept"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile
			)
		);
		
		this.recursiveField = checkBoxField(checkBox(false));
		
		this.paletteSourceField = fileField(
			(current) -> getUtils().chooseFile(
				getApplicationContainer(), 
				getLanguage().getText("dimgconv.palette.browse.title"), 
				getLanguage().getText("dimgconv.palette.browse.accept"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile
			)
		);

		JRadioButton graphicButton = getUtils().createRadioButtonFromLanguageKey("dimgconv.button.graphics", true);
		JRadioButton flatButton = getUtils().createRadioButtonFromLanguageKey("dimgconv.button.flats", false);
		JRadioButton colormapButton = getUtils().createRadioButtonFromLanguageKey("dimgconv.button.colormaps", false);
		JRadioButton paletteButton = getUtils().createRadioButtonFromLanguageKey("dimgconv.button.palettes", false);

		group(graphicButton, flatButton, colormapButton, paletteButton);
		
		this.graphicModeField = radioField(graphicButton);
		this.flatsModeField = radioField(flatButton);
		this.colormapModeField = radioField(colormapButton);
		this.paletteModeField = radioField(paletteButton);
		
		this.infoFileNameField = stringField(true);
		
		this.outputFileField = fileField(
			(current) -> getUtils().chooseFileOrDirectory(
				getApplicationContainer(), 
				getLanguage().getText("dimgconv.outputfile.browse.title"), 
				getLanguage().getText("dimgconv.outputfile.browse.accept"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile
			)
		);

		this.statusPanel = new DoomToolsStatusPanel();
    }
    
	@Override
	public String getTitle() 
	{
		return getLanguage().getText("dimgconv.title");
	}

	@Override
	public Container createContentPane()
	{
		DoomToolsGUIUtils utils = DoomToolsGUIUtils.get();
		return containerOf(dimension(350, 242), borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, utils.createForm(form(getLanguage().getInteger("dimgconv.label.width")),
					utils.formField("dimgconv.inputfile", inputFileField),
					utils.formField("dimgconv.recurse", recursiveField),
					utils.formField("dimgconv.palette", paletteSourceField),
					utils.formField("dimgconv.metafile", infoFileNameField),
					utils.formField("dimgconv.modes", panelField(containerOf(gridLayout(2, 2), 
						node(graphicModeField), node(flatsModeField),
						node(colormapModeField), node(paletteModeField)
					))),
					utils.formField("dimgconv.outputfile", outputFileField)
				)),
				node(BorderLayout.SOUTH, containerOf(flowLayout(Flow.TRAILING),
					node(utils.createButtonFromLanguageKey("dimgconv.convert", (b) -> onDoConversion()))
				))
			)),
			node(BorderLayout.CENTER, containerOf()),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		DoomToolsGUIUtils utils = getUtils();
		
		return menuBar(
			utils.createMenuFromLanguageKey("dimgconv.menu.file",
				utils.createItemFromLanguageKey("dimgconv.menu.file.item.exit", (i) -> attemptClose())
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
		// TODO: Finish this.
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		// TODO: Finish this.
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
		File inputFile = inputFileField.getValue();
		File outputFile = outputFileField.getValue();
		boolean recursive = recursiveField.getValue();
		File paletteSource = paletteSourceField.getValue(); 
		GraphicsMode mode = 
			graphicModeField.getValue() ? GraphicsMode.GRAPHICS :
			flatsModeField.getValue() ? GraphicsMode.FLATS :
			colormapModeField.getValue() ? GraphicsMode.COLORMAPS :
			paletteModeField.getValue() ? GraphicsMode.PALETTES :
			null;
		String infoFileName = infoFileNameField.getValue();
		
		getCommon().onExecuteDImgConv(getApplicationContainer(), statusPanel, inputFile, outputFile, recursive, paletteSource, mode, infoFileName);
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		DoomToolsGUIUtils utils = getUtils();
	
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.help", (i) -> onHelp()),
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog())
		); 
	}

	private void onHelp()
	{
		getUtils().createHelpModal(getUtils().helpProcess(DoomImageConvertMain.class, "--help")).open();
	}

	private void onHelpChangelog()
	{
		getUtils().createHelpModal(getUtils().helpResource("docs/changelogs/CHANGELOG-dimgconv.md")).open();
	}
	
}
