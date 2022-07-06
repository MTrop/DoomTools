package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.File;

import net.mtrop.doom.tools.gui.apps.data.ExportSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;


/**
 * DECOHack export panel.
 * @author Matthew Tropiano
 */
public class DecoHackExportPanel extends MultiFileEditorPanel
{
	private static final long serialVersionUID = -6601542918464394270L;

	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;

	private JFormField<File> patchOutputField;
	private JFormField<File> sourceOutputField;
	private JFormField<Boolean> budgetField;
	
	public DecoHackExportPanel(ExportSettings settings)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		
		final File patchOutputFile = settings.getOutputFile();
		final File sourceOutputFile = settings.getSourceOutputFile();
		final boolean budget = settings.isOutputBudget();
		
		this.patchOutputField = fileField(
			patchOutputFile, 
			(current) -> chooseFile(
				this,
				language.getText("decohack.export.patch.browse.title"), 
				current, 
				language.getText("decohack.export.patch.browse.accept") 
			)
		);
		
		this.sourceOutputField = fileField(
			sourceOutputFile, 
			(current) -> chooseFile(
				this,
				language.getText("decohack.export.outsource.browse.title"), 
				current, 
				language.getText("decohack.export.outsource.browse.accept") 
			)
		);
		
		this.budgetField = checkBoxField(checkBox(budget));
		
		containerOf(this,
			node(BorderLayout.NORTH, utils.createFormField(form(language.getInteger("decohack.export.label.width")),
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

