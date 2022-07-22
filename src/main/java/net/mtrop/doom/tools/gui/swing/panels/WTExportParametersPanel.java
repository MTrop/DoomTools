/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.WTExportSettingsManager;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.ListSelectionMode;
import net.mtrop.doom.tools.struct.util.FileUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * A panel for WTexScan applications.
 * @author Matthew Tropiano
 */
public class WTExportParametersPanel extends JPanel 
{
	private static final long serialVersionUID = -475091825704502422L;

	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private WTExportSettingsManager settings;
	
	private FileListPanel fileListField;
	
	private JFormField<File> baseWadField;
	private JFormField<File> outputWadField;
	private JFormField<Boolean> createField;
	private JFormField<Boolean> addField;

	private JFormField<String> nullTextureField;
	private JFormField<Boolean> noAnimatedField;
	private JFormField<Boolean> noSwitchesField;
	
	public WTExportParametersPanel()
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = WTExportSettingsManager.get();

		final FileFilter wadFilter = utils.createWADFileFilter();
		
		this.fileListField = new FileListPanel(language.getText("wtexport.files.label"), 
			ListSelectionMode.MULTIPLE_INTERVAL, true, true, 
			(files) -> {
				if (files != null && files.length > 0)
					settings.setLastTouchedFile(files[files.length - 1]);
			},
			() -> settings.getLastTouchedFile()
		);
		this.fileListField.setFileFilter(wadFilter);

		this.baseWadField = fileField(
			(current) -> utils.chooseFile(
				this, 
				language.getText("wtexport.basewad.browse.title"), 
				language.getText("wtexport.basewad.browse.choose"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile,
				(filter, input) -> (filter == wadFilter ? FileUtils.addMissingExtension(input, "wad") : input),
				wadFilter
			)
		);
		
		this.outputWadField = fileField(
			(current) -> utils.chooseFile(
				this, 
				language.getText("wtexport.outwad.browse.title"), 
				language.getText("wtexport.outwad.browse.choose"), 
				settings::getLastTouchedFile, 
				settings::setLastTouchedFile,
				(filter, input) -> (filter == wadFilter ? FileUtils.addMissingExtension(input, "wad") : input),
				wadFilter
			)
		);
		
		JRadioButton createButton = radio(language.getText("wtexport.create"), true);
		JRadioButton addButton = radio(language.getText("wtexport.add"), false);

		group(createButton, addButton);
		
		this.createField = radioField(createButton);
		this.addField = radioField(addButton);
		
		this.nullTextureField = stringField(true);
		this.noAnimatedField = checkBoxField(checkBox(false));
		this.noSwitchesField = checkBoxField(checkBox(false));
		
		containerOf(this,
			borderLayout(0, 4),
			node(BorderLayout.CENTER, fileListField),
			node(BorderLayout.SOUTH, containerOf(
				node(utils.createForm(form(language.getInteger("wtexport.label.width")),
					utils.formField("wtexport.basewad", baseWadField),
					utils.formField("wtexport.outwad", outputWadField),
					utils.formField(panelField(containerOf(gridLayout(1, 2),
						node(createButton),
						node(addButton)
					))),
					utils.formField("wtexport.nulltex", nullTextureField),
					utils.formField("wtexport.noanim", noAnimatedField),
					utils.formField("wtexport.noswitch", noSwitchesField)
				)
			))
		));
	}
	
	public File[] getTextureWads() 
	{
		return fileListField.getFiles();
	}
	
	public void setTextureWads(File[] value) 
	{
		fileListField.setFiles(value);
	}
	
	public File getBaseWad() 
	{
		return baseWadField.getValue();
	}
	
	public void setBaseWad(File value) 
	{
		baseWadField.setValue(value);
	}
	
	public File getOutputWad() 
	{
		return outputWadField.getValue();
	}
	
	public void setOutputWad(File value) 
	{
		outputWadField.setValue(value);
	}
	
	public boolean getCreate() 
	{
		return createField.getValue();
	}
	
	public void setCreate(boolean value) 
	{
		createField.setValue(value);
		addField.setValue(!value);
	}
	
	public String getNullTexture() 
	{
		return nullTextureField.getValue();
	}
	
	public void setNullTexture(String value) 
	{
		nullTextureField.setValue(value);
	}

	public boolean getNoAnimated() 
	{
		return noAnimatedField.getValue();
	}
	
	public void setNoAnimated(boolean value) 
	{
		noAnimatedField.setValue(value);
	}
	
	public boolean getNoSwitches() 
	{
		return noSwitchesField.getValue();
	}
	
	public void setNoSwitches(boolean value) 
	{
		noSwitchesField.setValue(value);
	}
	
}
