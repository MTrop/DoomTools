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
import javax.swing.JRadioButton;

import net.mtrop.doom.tools.DoomImageConvertMain;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.AppCommon.GraphicsMode;
import net.mtrop.doom.tools.gui.managers.settings.DImageConvertSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

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
			(current) -> utils.chooseFileOrDirectory(
				getApplicationContainer(), 
				language.getText("dimgconv.inputfile.browse.title"), 
				language.getText("dimgconv.inputfile.browse.accept"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile
			)
		);
		
		this.recursiveField = checkBoxField(checkBox(false));
		
		this.paletteSourceField = fileField(
			(current) -> utils.chooseFile(
				getApplicationContainer(), 
				language.getText("dimgconv.palette.browse.title"), 
				language.getText("dimgconv.palette.browse.accept"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile
			)
		);

		JRadioButton graphicButton = utils.createRadioButtonFromLanguageKey("dimgconv.button.graphics", true);
		JRadioButton flatButton = utils.createRadioButtonFromLanguageKey("dimgconv.button.flats", false);
		JRadioButton colormapButton = utils.createRadioButtonFromLanguageKey("dimgconv.button.colormaps", false);
		JRadioButton paletteButton = utils.createRadioButtonFromLanguageKey("dimgconv.button.palettes", false);

		group(graphicButton, flatButton, colormapButton, paletteButton);
		
		this.graphicModeField = radioField(graphicButton);
		this.flatsModeField = radioField(flatButton);
		this.colormapModeField = radioField(colormapButton);
		this.paletteModeField = radioField(paletteButton);
		
		this.infoFileNameField = stringField(true);
		
		this.outputFileField = fileField(
			(current) -> utils.chooseFileOrDirectory(
				getApplicationContainer(), 
				language.getText("dimgconv.outputfile.browse.title"), 
				language.getText("dimgconv.outputfile.browse.accept"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile
			)
		);

		this.statusPanel = new DoomToolsStatusPanel();
    }
    
	@Override
	public String getTitle() 
	{
		return language.getText("dimgconv.title");
	}

	@Override
	public Container createContentPane()
	{
		DoomToolsGUIUtils utils = DoomToolsGUIUtils.get();
		return containerOf(dimension(350, 242), borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, utils.createForm(form(language.getInteger("dimgconv.label.width")),
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
		
		File inputFile = inputFileField.getValue();
		File outputFile = outputFileField.getValue();
		boolean recursive = recursiveField.getValue();
		File paletteSource = paletteSourceField.getValue();
		GraphicsMode graphicsMode = getGraphicsMode();
		String infoFile = infoFileNameField.getValue();

		if (inputFile != null)
			state.put("input", inputFile.getAbsolutePath());
		if (outputFile != null)
			state.put("output", outputFile.getAbsolutePath());
		if (paletteSource != null)
			state.put("palettesource", paletteSource.getAbsolutePath());
		
		state.put("recursive", String.valueOf(recursive));
		state.put("mode", String.valueOf(graphicsMode.name()));
		state.put("infofilename", infoFile);
		
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		Function<String, File> parseFile = (input) -> ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input));

		inputFileField.setValue(ValueUtils.parse(state.get("input"), parseFile));
		outputFileField.setValue(ValueUtils.parse(state.get("output"), parseFile));
		recursiveField.setValue(ValueUtils.parseBoolean(state.get("recursive"), false));
		paletteSourceField.setValue(ValueUtils.parse(state.get("palettesource"), parseFile));
		
		GraphicsMode mode = EnumUtils.getEnumInstance(state.get("mode"), GraphicsMode.class);
		graphicModeField.setValue(mode == GraphicsMode.GRAPHICS);
		flatsModeField.setValue(mode == GraphicsMode.FLATS);
		colormapModeField.setValue(mode == GraphicsMode.COLORMAPS);
		paletteModeField.setValue(mode == GraphicsMode.PALETTES);
		
		infoFileNameField.setValue(state.get("infofilename"));
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
	public boolean shouldClose(Object frame) 
	{
		return SwingUtils.yesTo(language.getText("doomtools.application.close"));
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
		GraphicsMode mode = getGraphicsMode();
		String infoFileName = infoFileNameField.getValue();
		
		appCommon.onExecuteDImgConv(getApplicationContainer(), statusPanel, inputFile, outputFile, recursive, paletteSource, mode, infoFileName);
	}

	private GraphicsMode getGraphicsMode()
	{
		return 
			graphicModeField.getValue() ? GraphicsMode.GRAPHICS :
			flatsModeField.getValue() ? GraphicsMode.FLATS :
			colormapModeField.getValue() ? GraphicsMode.COLORMAPS :
			paletteModeField.getValue() ? GraphicsMode.PALETTES :
			null;
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.help", (i) -> onHelp()),
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog())
		); 
	}

	private void onHelp()
	{
		utils.createHelpModal(utils.helpProcess(DoomImageConvertMain.class, "--help")).open();
	}

	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-dimgconv.md")).open();
	}
	
}
