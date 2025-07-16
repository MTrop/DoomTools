/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.apps.data.DefSwAniExportSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.DoomToolsSettingsManager;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;


/**
 * DEFSWANI export panel.
 * @author Matthew Tropiano
 */
public class DefSwAniExportPanel extends JPanel
{
	private static final long serialVersionUID = -6601542918464394270L;

	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;

	private JFormField<File> outputWADField;
	private JFormField<Boolean> outputSourceField;
	
	public DefSwAniExportPanel(DefSwAniExportSettings exportSettings)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		
		final File outputWAD = exportSettings.getOutputWAD();
		final boolean outputSource = exportSettings.isOutputSource();

		final DoomToolsSettingsManager settings = DoomToolsSettingsManager.get();
		
		this.outputWADField = fileField(
			outputWAD, 
			(current) -> utils.chooseFile(
				this,
				language.getText("wswantbl.export.browse.title"), 
				language.getText("wswantbl.export.browse.accept"),
				() -> current != null ? current : settings.getLastFileSave(),
				settings::setLastFileSave,
				utils.createWADFileFilter()
			)
		);
		
		this.outputSourceField = checkBoxField(checkBox(outputSource));
		
		containerOf(this,
			node(BorderLayout.NORTH, utils.createForm(form(language.getInteger("wswantbl.export.label.width")),
				utils.formField("wswantbl.export.wad", outputWADField),
				utils.formField("wswantbl.export.outsource", outputSourceField)
			))
		);
	}
	
	public File getOutputWAD() 
	{
		return outputWADField.getValue();
	}
	
	public void setOutputWAD(File value) 
	{
		outputWADField.setValue(value);
	}
		
	public boolean getOutputSource() 
	{
		return outputSourceField.getValue();
	}
	
	public void setOutputSource(boolean value) 
	{
		outputSourceField.setValue(value);
	}
	
}

