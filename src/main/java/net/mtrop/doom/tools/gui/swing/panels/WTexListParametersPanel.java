/*******************************************************************************
 * Copyright (c) 2020-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import net.mtrop.doom.tools.gui.managers.AppCommon.TexScanOutputMode;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.settings.WTexListSettingsManager;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * A panel for WTexList applications.
 * @author Matthew Tropiano
 */
public class WTexListParametersPanel extends JPanel 
{
	private static final long serialVersionUID = 7244729529611529270L;
	
	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private WTexListSettingsManager settings;
	
	private FileListPanel fileListField;
	private JFormField<Boolean> outputTexturesField;
	private JFormField<Boolean> outputFlatsField;
	private JFormField<Boolean> outputBothField;
	private JFormField<Boolean> noCommentMessagesField;
	
	public WTexListParametersPanel()
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = WTexListSettingsManager.get();
		
		this.fileListField = new FileListPanel(language.getText("wtexlist.files.label"), 
			ListSelectionMode.MULTIPLE_INTERVAL, false, true, 
			(files) -> {
				if (files != null && files.length > 0)
					settings.setLastTouchedFile(files[files.length - 1]);
			},
			() -> settings.getLastTouchedFile()
		);
		this.fileListField.setFileFilter(utils.createWADArchiveFilter());
		
		JRadioButton textureButton = radio(false);
		JRadioButton flatButton = radio(false);
		JRadioButton bothButton = radio(true);

		group(textureButton, flatButton, bothButton);

		this.outputTexturesField = radioField(textureButton);
		this.outputFlatsField = radioField(flatButton);
		this.outputBothField = radioField(bothButton);
		this.noCommentMessagesField = checkBoxField(checkBox(false));
		
		containerOf(this,
			node(BorderLayout.CENTER, fileListField),
			node(BorderLayout.SOUTH, containerOf(
				borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(				
					createTitledBorder(createLineBorder(Color.GRAY), language.getText("wtexlist.outtype.label"), TitledBorder.LEADING, TitledBorder.TOP), 
					node(containerOf(createEmptyBorder(4, 4, 4, 4),
						node(utils.createForm(form(language.getInteger("wtexscan.label.width")),
							utils.formField("wtexlist.textures", outputTexturesField),
							utils.formField("wtexlist.flats", outputFlatsField),
							utils.formField("wtexlist.both", outputBothField)
						))
					))
				)),
				node(BorderLayout.SOUTH, containerOf(
					createTitledBorder(createLineBorder(Color.GRAY), language.getText("wtexlist.other.label"), TitledBorder.LEADING, TitledBorder.TOP), 
					node(containerOf(createEmptyBorder(4, 4, 4, 4),
						node(utils.createForm(form(language.getInteger("wtexlist.label.width")),
							utils.formField("wtexlist.nomsgs", noCommentMessagesField)
						))
					))
				))
			))
		);
	}
	
	public File[] getFiles()
	{
		return fileListField.getFiles();
	}
	
	public void setFiles(File[] files)
	{
		fileListField.setFiles(files);
	}
	
	public TexScanOutputMode getOutputMode() 
	{
		return 
			outputTexturesField.getValue() ? TexScanOutputMode.TEXTURES : 
			outputFlatsField.getValue() ? TexScanOutputMode.FLATS : 
			outputBothField.getValue() ? TexScanOutputMode.BOTH : 
			null 
		;
	}
	
	public void setOutputMode(TexScanOutputMode mode) 
	{
		outputTexturesField.setValue(mode == TexScanOutputMode.TEXTURES);
		outputFlatsField.setValue(mode == TexScanOutputMode.FLATS);
		outputBothField.setValue(mode == TexScanOutputMode.BOTH);
	}
	
	public boolean getNoCommentMessages() 
	{
		return noCommentMessagesField.getValue();
	}
	
	public void setNoCommentMessages(boolean value) 
	{
		noCommentMessagesField.setValue(value);
	}
	
}
