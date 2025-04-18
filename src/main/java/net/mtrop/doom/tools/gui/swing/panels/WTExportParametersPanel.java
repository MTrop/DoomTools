/*******************************************************************************
 * Copyright (c) 2020-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.WTExportSettingsManager;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.ListSelectionMode;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * A panel for WTexport applications.
 * @author Matthew Tropiano
 */
public class WTExportParametersPanel extends JPanel 
{
	private static final long serialVersionUID = -475091825704502422L;

	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private WTExportSettingsManager settings;
	
	private FileListPanel fileListField;
	
	private WTExportParameterFieldsPanel fieldsPanel;
	
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

		this.fieldsPanel = new WTExportParameterFieldsPanel();
		
		containerOf(this,
			borderLayout(0, 4),
			node(BorderLayout.CENTER, fileListField),
			node(BorderLayout.SOUTH, fieldsPanel)
		);
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
		return fieldsPanel.getBaseWad();
	}
	
	public void setBaseWad(File value) 
	{
		fieldsPanel.setBaseWad(value);
	}
	
	public File getOutputWad() 
	{
		return fieldsPanel.getOutputWad();
	}
	
	public void setOutputWad(File value) 
	{
		fieldsPanel.setOutputWad(value);
	}
	
	public boolean getCreate() 
	{
		return fieldsPanel.getCreate();
	}
	
	public void setCreate(boolean value) 
	{
		fieldsPanel.setCreate(value);
	}
	
	public String getNullTexture() 
	{
		return fieldsPanel.getNullTexture();
	}
	
	public void setNullTexture(String value) 
	{
		fieldsPanel.setNullTexture(value);
	}

	public boolean getNoAnimated() 
	{
		return fieldsPanel.getNoAnimated();
	}
	
	public void setNoAnimated(boolean value) 
	{
		fieldsPanel.setNoAnimated(value);
	}
	
	public boolean getNoSwitches() 
	{
		return fieldsPanel.getNoSwitches();
	}
	
	public void setNoSwitches(boolean value) 
	{
		fieldsPanel.setNoSwitches(value);
	}
	
}
