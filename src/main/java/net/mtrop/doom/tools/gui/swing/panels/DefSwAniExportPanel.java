package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.apps.data.DefSwAniExportSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
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
	
	public DefSwAniExportPanel(DefSwAniExportSettings settings)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		
		final File outputWAD = settings.getOutputWAD();
		final boolean outputSource = settings.isOutputSource();
		
		this.outputWADField = fileField(
			outputWAD, 
			(current) -> chooseFile(
				this,
				language.getText("wswantbl.export.browse.title"), 
				current, 
				language.getText("wswantbl.export.browse.accept"),
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

