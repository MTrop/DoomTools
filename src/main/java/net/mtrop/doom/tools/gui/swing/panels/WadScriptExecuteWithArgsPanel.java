package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.DoomToolsConstants;
import net.mtrop.doom.tools.gui.apps.data.ExecutionSettings;
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
public class WadScriptExecuteWithArgsPanel extends JPanel
{
	private static final long serialVersionUID = 3311704543488697542L;
	
	private DoomToolsLanguageManager language;

	private JFormField<File> workingDirFileField;
	private JFormField<File> standardInPathField;
	private JFormField<String> entryPointField;

	private JFormField<Integer> numArgsField; 
	private Container argsFieldPanel;
	private List<JFormField<String>> argsFieldList;
	private List<Component> argsComponentList;
	
	public WadScriptExecuteWithArgsPanel()
	{
		this(new ExecutionSettings());
	}
	
	public WadScriptExecuteWithArgsPanel(ExecutionSettings executionSettings)
	{
		this.language = DoomToolsLanguageManager.get();
		
		final File workingDirectory = executionSettings.getWorkingDirectory();
		final File standardInPath = executionSettings.getStandardInPath();
		final String entryPoint = executionSettings.getEntryPoint();
		final String[] initArgs = executionSettings.getArgs();
		
		this.workingDirFileField = fileField(
			workingDirectory, 
			(current) -> chooseDirectory(
				this,
				language.getText("wadscript.run.workdir.browse.title"), 
				current, 
				language.getText("wadscript.run.workdir.browse.accept"), 
				DoomToolsConstants.FileFilters.DIRECTORIES
			)
		);
		
		this.standardInPathField = fileField(
			standardInPath, 
			(current) -> chooseFile(
				this,
				language.getText("wadscript.run.stdin.browse.title"), 
				current, 
				language.getText("wadscript.run.stdin.browse.accept") 
			)
		);
		
		this.entryPointField = stringField(entryPoint);

		this.numArgsField = integerField(initArgs.length, this::adjustFields);
		this.argsFieldPanel = containerOf(createEmptyBorder(4, 8, 4, 8), gridLayout(0, 1, 0, 4));
		this.argsFieldList = new ArrayList<>(Math.max(initArgs.length, 4));
		this.argsComponentList = new ArrayList<>(Math.max(initArgs.length, 4));
		
		containerOf(this,
			node(BorderLayout.NORTH, form(language.getInteger("wadscript.run.withargs.label.width"))
				.addField(language.getText("wadscript.run.withargs.workdir"), workingDirFileField)
				.addField(language.getText("wadscript.run.withargs.stdin"), standardInPathField)
				.addField(language.getText("wadscript.run.withargs.entrypoint"), entryPointField)
				.addField(language.getText("wadscript.run.withargs.argfield"), numArgsField)
			),
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
	 * @param stdIn the path to standard in input, or null if no input.
	 */
	public void setStandardInPath(File stdIn)
	{
		standardInPathField.setValue(stdIn);
	}

	/**
	 * @return the path to standard in input, or null if no input.
	 */
	public File getStandardInPath()
	{
		return standardInPathField.getValue();
	}

	/**
	 * @param entry the entry point to use.
	 */
	public void setEntryPoint(String entry)
	{
		entryPointField.setValue(entry);
	}
	
	/**
	 * @return the entry point to use.
	 */
	public String getEntryPoint()
	{
		return entryPointField.getValue();
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
