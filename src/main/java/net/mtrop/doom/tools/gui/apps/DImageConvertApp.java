package net.mtrop.doom.tools.gui.apps;

import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.DoomImageConvertMain;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.AppCommon.GraphicsMode;
import net.mtrop.doom.tools.gui.managers.settings.DImageConvertSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DMXConvertSettingsPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.util.FileUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.modal;


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
    	
		FileFilter wadFilter = getUtils().getWADFileFilter();
		
		this.inputFileField = fileField(
			(current) -> getUtils().chooseFileOrDirectory(
				getApplicationContainer(), 
				getLanguage().getText("dimgconv.inputfile.browse.title"), 
				getLanguage().getText("dimgconv.inputfile.browse.accept"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile,
				(filter, input) -> (filter == wadFilter ? FileUtils.addMissingExtension(input, "wad") : input),
				wadFilter
			)
		);
		
		this.outputFileField = fileField(
			(current) -> getUtils().chooseFileOrDirectory(
				getApplicationContainer(), 
				getLanguage().getText("dimgconv.outputfile.browse.title"), 
				getLanguage().getText("dimgconv.outputfile.browse.accept"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile,
				(filter, input) -> (filter == wadFilter ? FileUtils.addMissingExtension(input, "wad") : input),
				wadFilter
			)
		);

		// TODO: Finish this.
		
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
		// TODO: Finish this.
		return containerOf();
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
			utils.createItemFromLanguageKey("doomtools.menu.help.item.help", (c, e) -> onHelp()),
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

	private void onHelp()
	{
		getUtils().createHelpModal(getUtils().helpProcess(DoomImageConvertMain.class, "--help")).open();
	}

	private void onHelpChangelog()
	{
		getUtils().createHelpModal(getUtils().helpResource("docs/changelogs/CHANGELOG-dimgconv.md")).open();
	}
	
	
	
}
