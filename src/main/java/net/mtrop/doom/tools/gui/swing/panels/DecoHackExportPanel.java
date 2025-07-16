/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.apps.data.PatchExportSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.DecoHackSettingsManager;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;


/**
 * DECOHack export panel.
 * @author Matthew Tropiano
 */
public class DecoHackExportPanel extends JPanel
{
	private static final long serialVersionUID = -6601542918464394270L;

	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;

	private JFormField<File> patchOutputField;
	private JFormField<File> sourceOutputField;
	private JFormField<Boolean> budgetField;
	
	public DecoHackExportPanel(PatchExportSettings exportSettings)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		
		final File patchOutputFile = exportSettings.getOutputFile();
		final File sourceOutputFile = exportSettings.getSourceOutputFile();
		final boolean budget = exportSettings.isOutputBudget();
		
		final DecoHackSettingsManager settings = DecoHackSettingsManager.get();
		
		this.patchOutputField = fileField(
			patchOutputFile, 
			(current) -> utils.chooseFile(
				this,
				language.getText("decohack.export.patch.browse.title"), 
				language.getText("decohack.export.patch.browse.accept"),
				() -> current != null ? current : settings.getLastExportFile(),
				settings::setLastExportFile
			)
		);
		
		this.sourceOutputField = fileField(
			sourceOutputFile, 
			(current) -> utils.chooseFile(
				this,
				language.getText("decohack.export.outsource.browse.title"), 
				language.getText("decohack.export.outsource.browse.accept"),
				() -> current != null ? current : settings.getLastExportSourceFile(),
				settings::setLastExportSourceFile
			)
		);
		
		this.budgetField = checkBoxField(checkBox(budget));
		
		containerOf(this,
			node(BorderLayout.NORTH, utils.createForm(form(language.getInteger("decohack.export.label.width")),
				utils.formField("decohack.export.patch", patchOutputField),
				utils.formField("decohack.export.outsource", sourceOutputField),
				utils.formField("decohack.export.budget", budgetField)
			))
		);
	}
	
	public File getPatchOutput() 
	{
		return patchOutputField.getValue();
	}
	
	public void setPatchOutput(File value) 
	{
		patchOutputField.setValue(value);
	}
		
	public File getSourceOutput() 
	{
		return sourceOutputField.getValue();
	}
	
	public void setSourceOutput(File value) 
	{
		sourceOutputField.setValue(value);
	}
	
	public boolean getBudget() 
	{
		return budgetField.getValue();
	}
	
	public void setBudget(boolean value) 
	{
		budgetField.setValue(value);
	}
	
}

