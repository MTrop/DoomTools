/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.DoomToolsConstants;
import net.mtrop.doom.tools.gui.apps.data.MergeSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * Panel for gathering options for WadScript execution.
 * @author Matthew Tropiano
 */
public class WadMergeExecuteWithArgsPanel extends JPanel
{
	private static final long serialVersionUID = -5380481388722995023L;

	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;

	private JFormField<File> workingDirFileField;
	private JFormField<Boolean> verboseOutputField;

	private JFormField<Integer> numArgsField; 
	private Container argsFieldPanel;
	private List<JFormField<String>> argsFieldList;
	private List<Component> argsComponentList;
	
	public WadMergeExecuteWithArgsPanel()
	{
		this(new MergeSettings());
	}
	
	public WadMergeExecuteWithArgsPanel(MergeSettings mergeSettings)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		
		final File workingDirectory = mergeSettings.getWorkingDirectory();
		final String[] initArgs = mergeSettings.getArgs();
		
		this.workingDirFileField = fileField(
			workingDirectory, 
			(current) -> chooseDirectory(
				this,
				language.getText("wadmerge.run.workdir.browse.title"), 
				current, 
				language.getText("wadmerge.run.workdir.browse.accept"), 
				DoomToolsConstants.FileFilters.DIRECTORIES
			)
		);
		
		this.verboseOutputField = checkBoxField(checkBox(mergeSettings.getVerboseOutput()));
		
		this.numArgsField = integerField(initArgs.length, this::adjustFields);
		this.argsFieldPanel = containerOf(createEmptyBorder(4, 8, 4, 8), gridLayout(0, 1, 0, 4));
		this.argsFieldList = new ArrayList<>(Math.max(initArgs.length, 4));
		this.argsComponentList = new ArrayList<>(Math.max(initArgs.length, 4));
		
		containerOf(this,
			node(BorderLayout.NORTH, utils.createForm(form(language.getInteger("wadmerge.run.withargs.label.width")),
				utils.formField("wadmerge.run.withargs.workdir", workingDirFileField),
				utils.formField("wadmerge.run.withargs.verbose", verboseOutputField),
				utils.formField("wadmerge.run.withargs.argfield", numArgsField)
			)),
			node(BorderLayout.CENTER, dimension(320, 128), scroll(containerOf(
				node(BorderLayout.NORTH, argsFieldPanel),
				node(BorderLayout.CENTER, containerOf())
			)))
		);
		adjustFields(initArgs.length);
		SwingUtils.invoke(() -> {
			for (int i = 0; i < initArgs.length; i++)
				this.argsFieldList.get(i).setValue(initArgs[i]);
		});
	}
	
	private void adjustFields(Integer newLen)
	{
		final int start = argsFieldList.size();
		newLen = Math.max(newLen, 0);
		
		if (newLen < start)
		{
			while (argsFieldList.size() > newLen)
			{
				int idx = argsFieldList.size() - 1;
				argsFieldList.remove(idx);
				argsFieldPanel.remove(argsComponentList.remove(idx));
			}
		}
		else if (start < newLen)
		{
			while (argsFieldList.size() < newLen)
			{
				int idx = argsFieldList.size();
				JFormField<String> argField = stringField();
				Container container = containerOf(
					node(BorderLayout.LINE_START, dimension(48, 20), label(String.valueOf(idx))),
					node(BorderLayout.CENTER, argField)
				);
				argsFieldList.add(argField);
				argsFieldPanel.add(container);
				argsComponentList.add(container);
			}
		}
		// Refresh panel.
		SwingUtils.invoke(() -> {
			argsFieldPanel.revalidate();
		});
	}
	
	/**
	 * @param workDir the working directory. 
	 */
	public void setWorkingDirectory(File workDir)
	{
		workingDirFileField.setValue(workDir);
	}

	/**
	 * @return the selected working directory.
	 */
	public File getWorkingDirectory()
	{
		return workingDirFileField.getValue();
	}

	/**
	 * @param verbose the new verbose output state.
	 */
	public void setVerboseOutput(boolean verbose) 
	{
		verboseOutputField.setValue(verbose);
	}
	
	/**
	 * @return the verbose output setting.
	 */
	public Boolean getVerboseOutput() 
	{
		return verboseOutputField.getValue();
	}
	
	/**
	 * @param args an array of the entered arguments.
	 */
	public void setArgs(final String ... args)
	{
		numArgsField.setValue(args.length);
		SwingUtils.invoke(() -> {
			int size = argsFieldList.size();
			for (int i = 0; i < size; i++) 
				argsFieldList.get(i).setValue(args[i]);
		});
	}
	
	/**
	 * @return an array of the entered arguments.
	 */
	public String[] getArgs()
	{
		int size = argsFieldList.size();
		List<String> argList = new ArrayList<>(size);
		for (JFormField<String> field : argsFieldList)
			argList.add(field.getValue());
		return argList.toArray(new String[size]);
	}
	
}
