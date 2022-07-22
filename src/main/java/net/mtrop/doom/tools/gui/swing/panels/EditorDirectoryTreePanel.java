/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.swing.panels.DirectoryTreePanel.DirectoryTreeListener;
import net.mtrop.doom.tools.struct.util.FileUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * Directory tree panel for editor.
 * @author Matthew Tropiano
 */
public class EditorDirectoryTreePanel extends JPanel
{
	private static final long serialVersionUID = 6433958661826724298L;

	// =======================================================================

	private DoomToolsIconManager icons;
	
	private JLabel textLabel;
	private DirectoryTreePanel treePanel;
	
	private File rootDirectory;

	/**
	 * Creates a new root panel.
	 */
	public EditorDirectoryTreePanel()
	{
		this(null);
	}
	
	/**
	 * Creates a new root panel.
	 * @param rootDirectory the root directory. Can be null.
	 */
	public EditorDirectoryTreePanel(File rootDirectory)
	{
		this.icons = DoomToolsIconManager.get();
		
		this.textLabel = label();
		this.treePanel = new DirectoryTreePanel(FileUtils.NULL_FILE);

		JButton resetButton = button(icons.getImage("folder.png"), (b) -> onResetTop());

		containerOf(this, borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(4, 0),
				node(BorderLayout.LINE_START, resetButton),
				node(BorderLayout.CENTER, textLabel)
			)),
			node(BorderLayout.CENTER, this.treePanel)
		);

		setRootDirectory(rootDirectory);
	}
	
	/**
	 * Set label text.
	 * @param text the new text.
	 */
	public void setLabel(String text)
	{
		textLabel.setText(text);
	}
	
	/**
	 * Sets the root directory.
	 * @param rootDirectory the root directory of the tree.
	 */
	public void setRootDirectory(File rootDirectory)
	{
		this.rootDirectory = rootDirectory;
		if (rootDirectory != null && rootDirectory.isDirectory())
		{
			treePanel.setRootDirectory(rootDirectory);
			treePanel.setVisible(true);
		}
		else
		{
			treePanel.setVisible(false);
			treePanel.setTemporaryRootDirectory(FileUtils.NULL_FILE);
		}
	}
	
	/**
	 * Sets the root directory temporarily.
	 * @param rootDirectory the root directory of the tree.
	 */
	public void setTemporaryRootDirectory(File rootDirectory)
	{
		treePanel.setTemporaryRootDirectory(rootDirectory);
		treePanel.setVisible(true);
	}
	
	/**
	 * @return the current root directory.
	 */
	public File getRootDirectory() 
	{
		return rootDirectory;
	}
	
	/**
	 * Sets the listener on the tree panel.
	 * @param listener the tree listener.
	 */
	public void setListener(DirectoryTreeListener listener)
	{
		treePanel.setDirectoryTreeListener(listener);
	}
	
	/**
	 * Sets the selected file in the file path.
	 * @param file the file to select.
	 */
	public void setSelectedFile(File file)
	{
		treePanel.setSelectedFile(file);
	}

	private void onResetTop()
	{
		if (rootDirectory != null)
			treePanel.setRootDirectory(rootDirectory);
	}
	
}
