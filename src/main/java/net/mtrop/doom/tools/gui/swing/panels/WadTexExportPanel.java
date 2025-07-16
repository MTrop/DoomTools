/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.apps.data.WadTexExportSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.WadTexSettingsManager;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;


/**
 * WadTex export panel.
 * @author Matthew Tropiano
 */
public class WadTexExportPanel extends JPanel
{
	private static final long serialVersionUID = -8764784978986700706L;
	
	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	
	private JFormField<File> outputWADField;
	private JFormField<String> nameOverrideField;
	private JFormField<Boolean> appendModeField;
	private JFormField<Boolean> forceStrifeField;

	public WadTexExportPanel(WadTexExportSettings exportSettings)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		
		final File outputWAD = exportSettings.getOutputWAD();
		final String nameOverride = exportSettings.getNameOverride();
		final boolean appendMode = exportSettings.getAppendMode();
		final boolean forceStrife = exportSettings.getForceStrife();
		
		final WadTexSettingsManager settings = WadTexSettingsManager.get();
		
		this.outputWADField = fileField(
			outputWAD, 
			(current) -> utils.chooseFile(
				this,
				language.getText("wadtex.export.wad.browse.title"), 
				language.getText("wadtex.export.wad.browse.accept"),
				() -> current != null ? current : settings.getLastTouchedFile(),
				settings::setLastTouchedFile,
				utils.createWADFileFilter()
			)
		);
		
		this.nameOverrideField = stringField(nameOverride);
		this.appendModeField = checkBoxField(checkBox(appendMode));
		this.forceStrifeField = checkBoxField(checkBox(forceStrife));
		
		containerOf(this,
			node(BorderLayout.NORTH, utils.createForm(form(language.getInteger("wadtex.export.label.width")),
				utils.formField("wadtex.export.wad", outputWADField),
				utils.formField("wadtex.export.name", nameOverrideField),
				utils.formField("wadtex.export.append", appendModeField),
				utils.formField("wadtex.export.strife", forceStrifeField)
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
		
	public String getNameOverride() 
	{
		return nameOverrideField.getValue();
	}
	
	public void setNameOverride(String value) 
	{
		nameOverrideField.setValue(value);
	}
		
	public boolean getAppendMode() 
	{
		return appendModeField.getValue();
	}
	
	public void setAppendMode(boolean value) 
	{
		appendModeField.setValue(value);
	}
		
	public boolean getForceStrife() 
	{
		return forceStrifeField.getValue();
	}
	
	public void setForceStrife(boolean value) 
	{
		forceStrifeField.setValue(value);
	}
		
}

