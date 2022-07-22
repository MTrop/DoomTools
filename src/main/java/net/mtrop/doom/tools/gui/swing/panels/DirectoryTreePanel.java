/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.swing.DropMode;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomToolsTaskManager;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.ClipboardUtils;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.swing.ModalFactory.Modal;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * A panel that shows a directory tree.
 * @author Matthew Tropiano
 */
public class DirectoryTreePanel extends JPanel
{
	private static final long serialVersionUID = 5496698746549135647L;
	
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DirectoryTreePanel.class); 

	private static final Comparator<FileNode> CHILD_COMPARATOR;

	static
	{
		CHILD_COMPARATOR = (a, b) -> FileUtils.getFileListComparator().compare(a.file, b.file);
	}
	
	// =======================================================================

	private static final File[] NO_FILES = new File[0];
	
	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private DoomToolsTaskManager tasks;
	
	// Components

	/** The file tree itself. */
	private File rootDirectory;
	/** The file tree itself. */
	private FileTree fileTree;
	/** The file tree listener. */
	private DirectoryTreeListener directoryTreeListener;
	/** The file popup menu. */
	private JPopupMenu singleFilePopupMenu;
	/** The directory popup menu. */
	private JPopupMenu singleDirectoryPopupMenu;
	/** The multi-file popup menu (only files). */
	private JPopupMenu multiFilePopupMenu;
	
	// Special keys
	
	private KeyStroke copyKeyStroke;
	private KeyStroke pasteKeyStroke;
	private KeyStroke deleteKeyStroke;
	private KeyStroke refreshKeystroke;

	/**
	 * Creates a file tree panel.
	 */
	public DirectoryTreePanel()
	{
		this(null, false, null);
	}
	
	/**
	 * Creates a file tree panel.
	 * @param rootDirectory the root directory.
	 */
	public DirectoryTreePanel(File rootDirectory)
	{
		this(rootDirectory, false, null);
	}
	
	/**
	 * Creates a file tree panel.
	 * @param rootDirectory the root directory.
	 * @param directoryTreeListener the tree listener to use.
	 */
	public DirectoryTreePanel(File rootDirectory, DirectoryTreeListener directoryTreeListener)
	{
		this(rootDirectory, false, directoryTreeListener);
	}
	
	/**
	 * Creates a file tree panel.
	 * @param rootDirectory the root directory.
	 * @param readOnly if the tree is read-only (not editable).
	 * @param directoryTreeListener the tree listener to use.
	 */
	public DirectoryTreePanel(File rootDirectory, boolean readOnly, DirectoryTreeListener directoryTreeListener)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.tasks = DoomToolsTaskManager.get();
		
		this.rootDirectory = rootDirectory != null ? FileUtils.canonizeFile(rootDirectory) : null;
		this.directoryTreeListener = directoryTreeListener;

		this.singleFilePopupMenu = createSingleFilePopupMenu();
		this.singleDirectoryPopupMenu = createSingleDirectoryPopupMenu();
		this.multiFilePopupMenu = createMultiFilePopupMenu();
		
		this.copyKeyStroke = language.getKeyStroke("texteditor.action.copy.keystroke");
		this.pasteKeyStroke = language.getKeyStroke("texteditor.action.paste.keystroke");
		this.deleteKeyStroke = language.getKeyStroke("texteditor.action.delete.keystroke");
		this.refreshKeystroke = language.getKeyStroke("dirtree.popup.menu.item.refresh.keystroke");

		FileTree tree = new FileTree(rootDirectory, readOnly);
		FileTreeListener treeListener = new FileTreeListener();
		tree.addTreeSelectionListener(treeListener);
		tree.addTreeWillExpandListener(treeListener);
		tree.addTreeExpansionListener(treeListener);
		
		FileTreeInputListener inputListener = new FileTreeInputListener();
		tree.addKeyListener(inputListener);
		tree.addMouseListener(inputListener);

		this.fileTree = tree;
		
		containerOf(this,
			node(BorderLayout.CENTER, scroll(tree))
		);
	}
	
	private JPopupMenu createSingleFilePopupMenu()
	{
		return popupMenu(
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.open", (i) -> onOpenFiles()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.rename.file", (i) -> onRenameSelectedFile()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.refresh", (i) -> onRefreshSelectedFiles()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.reveal", (i) -> onRevealSelectedFileInSystem()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.terminal", (i) -> onOpenTerminalHere()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy", (i) -> onCopySelectedFiles()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path", (i) -> onCopySelectedFilePath()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path.rel", (i) -> onCopySelectedRelativeFilePath()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.paste", (i) -> onPasteClipboardFilesIntoSelectedDirectory()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.delete", (i) -> onDeleteSelectedFiles())
		);
	}
	
	private JPopupMenu createSingleDirectoryPopupMenu()
	{
		return popupMenu(
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.new.file", (i) -> onAddNewFile()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.new.directory", (i) -> onAddNewDirectory()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.rename.directory", (i) -> onRenameSelectedFile()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.refresh", (i) -> onRefreshSelectedFiles()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.reveal", (i) -> onRevealSelectedFileInSystem()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.terminal", (i) -> onOpenTerminalHere()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.root", (i) -> onMakeSelectedDirectoryRoot()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy", (i) -> onCopySelectedFiles()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path", (i) -> onCopySelectedFilePath()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path.rel", (i) -> onCopySelectedRelativeFilePath()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.paste", (i) -> onPasteClipboardFilesIntoSelectedDirectory()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.delete", (i) -> onDeleteSelectedFiles())
		);
	}
	
	private JPopupMenu createMultiFilePopupMenu()
	{
		return popupMenu(
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.open", (i) -> onOpenFiles()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy", (i) -> onCopySelectedFiles()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path", (i) -> onCopySelectedFilePath()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path.rel", (i) -> onCopySelectedRelativeFilePath()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.delete", (i) -> onDeleteSelectedFiles())
		);
	}
	
	/**
	 * Sets this tree's directory tree listener.
	 * @param directoryTreeListener the listener to set.
	 */
	public void setDirectoryTreeListener(DirectoryTreeListener directoryTreeListener) 
	{
		this.directoryTreeListener = directoryTreeListener;
	}
	
	/**
	 * Refresh the tree.
	 */
	public void refresh()
	{
		((FileTreeModel)fileTree.getModel()).reload();
	}
	
	/**
	 * Sets the new root directory (temporarily).
	 * @param rootDirectory
	 */
	public void setRootDirectory(File rootDirectory) 
	{
		this.rootDirectory = FileUtils.canonizeFile(rootDirectory);
		fileTree.setRootDirectory(rootDirectory);
	}
	
	/**
	 * Sets the new root directory (temporarily).
	 * @param rootDirectory
	 */
	public void setTemporaryRootDirectory(File rootDirectory) 
	{
		fileTree.setRootDirectory(rootDirectory);
	}
	
	/**
	 * Sets the new root directory to the parent directory, if it has a parent.
	 */
	public void setRootDirectoryParent() 
	{
		File parent = rootDirectory.getParentFile();
		if (parent != null)
			fileTree.setRootDirectory(parent);
	}
	
	/**
	 * Selects a file in the tree, if it is in the tree.
	 * @param filePath the file path.
	 */
	public void setSelectedFile(File filePath)
	{
		TreePath path = getTreePathForFile(filePath);
		if (path != null)
			fileTree.setSelectionPath(path);
	}
	
	/**
	 * Selects a set of file paths in the tree, if they are in the tree.
	 * @param filePaths the file paths.
	 */
	public void setSelectedFiles(File ... filePaths)
	{
		List<TreePath> treePathList = new LinkedList<TreePath>();
		for (int i = 0; i < filePaths.length; i++)
		{
			TreePath path = getTreePathForFile(filePaths[i]); 
			if (path != null)
				treePathList.add(path);
		}
		
		fileTree.setSelectionPaths(treePathList.toArray(new TreePath[treePathList.size()]));
	}
	
	/**
	 * @return the first selected file, or null if no files selected.
	 */
	public synchronized File getSelectedFile()
	{
		File[] files = getSelectedFiles();
		return files.length > 0 ? files[files.length - 1] : null;
	}
	
	/**
	 * Gets all currently selected files.
	 * @return the array of currently selected files. Can be empty, but not null.
	 */
	public synchronized File[] getSelectedFiles()
	{
		Set<File> fileSet = new TreeSet<>(FileUtils.getFileListComparator());
		TreePath[] selectionPaths = fileTree.getSelectionPaths();
		if (selectionPaths != null)
		{
			for (TreePath path : selectionPaths)
				fileSet.add(((FileNode)path.getLastPathComponent()).file);
			return fileSet.toArray(new File[fileSet.size()]);
		}
		else
		{
			return NO_FILES;
		}
	}
	
	/**
	 * @return true if one of the currently selected files is a directory.
	 */
	private synchronized boolean directoryIsSelected()
	{
		for (File file : getSelectedFiles())
		{
			if (file.isDirectory())
				return true;
		}
		return false;
	}
	
	
	// Gets the tree path for a file path.
	// This may need neatening up at some point 
	private TreePath getTreePathForFile(File filePath)
	{
		filePath = FileUtils.canonizeFile(filePath);

		FileNode currentNode = (FileNode)((FileTreeModel)fileTree.getModel()).getRoot();
		
		String rootPath = currentNode.toString();
		String filePathString = filePath.getPath();

		String testPath;
		int separatorIndex = 0;
		do {
			separatorIndex = filePathString.indexOf(File.separator, separatorIndex);
			if (separatorIndex < 0)
				return null;
			testPath = filePathString.substring(0, separatorIndex);
			separatorIndex += File.separator.length();
		} while (!rootPath.equalsIgnoreCase(testPath));
		
		List<FileNode> treeNodes = new LinkedList<>();
		treeNodes.add(currentNode);
		
		do {
			separatorIndex = filePathString.indexOf(File.separator, separatorIndex);
			File nextFile;
			if (separatorIndex > 0)
			{
				nextFile = new File(filePathString.substring(0, separatorIndex));
				separatorIndex += File.separator.length();
			}
			else
			{
				nextFile = new File(filePathString);
			}

			int index = currentNode.getIndex(new FileNode(currentNode, nextFile));
			if (index >= 0)
			{
				FileNode nextNode = (FileNode)currentNode.getChildAt(index);
				treeNodes.add(nextNode);
				currentNode = nextNode;
			}
			else
			{
				return null;
			}

		} while (separatorIndex > 0);
		
		return new TreePath(treeNodes.toArray(new Object[treeNodes.size()]));
	}
	
	private String getNewName(File targetDirectory)
	{
		int i = 1;
		String newFilePath;
		String newFileName;
		do {
			newFileName = "New File " + (i++);
			newFilePath = targetDirectory.getPath() + File.separator + newFileName;
		} while (new File(newFilePath).exists());
		
		JFormField<String> nameField = stringField(newFileName, true);
		
		// Get new name.
		Boolean okay = utils.createModal(
			language.getText("dirtree.modal.new.file.title"), 
			containerOf(
				node(BorderLayout.NORTH, label(language.getText("dirtree.modal.new.file.message"))),
				node(BorderLayout.SOUTH, nameField)
			), 
			utils.createChoiceFromLanguageKey("doomtools.ok", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", false)
		).openThenDispose();
		
		if (okay)
			return nameField.getValue();
		else
			return null;
	}

	/**
	 * Adds a new file under a target directory.
	 * @param targetDirectory the parent directory.
	 */
	private void onAddNewFile()
	{
		File targetDirectory = getSelectedFile();

		String newFileName = getNewName(targetDirectory);
		if (newFileName == null)
			return;
		
		String newFilePath = targetDirectory.getPath() + File.separator + newFileName;
		
		try {
			File newFile = new File(newFilePath);
			if (!newFile.exists() && FileUtils.touch(newFile))
			{
				FileNode node = (FileNode)fileTree.getSelectionPath().getLastPathComponent();
				node.insert(new FileNode(node, newFile), 0);
				reloadNode(node);
			}
		} catch (IOException e) {
			SwingUtils.error(this, language.getText("dirtree.newfile.error.ioerror", e.getLocalizedMessage()));
		} catch (SecurityException e) {
			SwingUtils.error(this, language.getText("dirtree.newfile.error.security", e.getLocalizedMessage()));
		}
	}

	/**
	 * Adds a new directory under a target directory.
	 * @param targetDirectory the parent directory.
	 */
	private void onAddNewDirectory()
	{
		File targetDirectory = getSelectedFile();

		String newFileName = getNewName(targetDirectory);
		if (newFileName == null)
			return;
		
		String newFilePath = targetDirectory.getPath() + File.separator + newFileName;
		
		try {
			File newFile = new File(newFilePath);
			if (newFile.mkdir())
			{
				FileNode node = (FileNode)fileTree.getSelectionPath().getLastPathComponent();
				node.insert(new FileNode(node, newFile), 0);
				reloadNode(node);
			}
		} catch (SecurityException e) {
			SwingUtils.error(this, language.getText("dirtree.newfile.error.security", e.getLocalizedMessage()));
		}
	}

	/**
	 * Renames the currently selected file.
	 */
	private void onRenameSelectedFile()
	{
		File file = getSelectedFile();
		if (file != null)
			onRenameFile(file);
	}

	/**
	 * Renames a file in the tree.
	 * @param fileToRename the file to rename.
	 */
	private void onRenameFile(File fileToRename)
	{
		fileTree.startEditingAtPath(getTreePathForFile(FileUtils.canonizeFile(fileToRename)));
	}

	/**
	 * Refreshes the selected file.
	 */
	private void onRefreshSelectedFiles()
	{
		for (TreePath path : fileTree.getSelectionPaths())
			reloadNode((FileNode)path.getLastPathComponent());
	}

	/**
	 * Prepares to copy files in the tree.
	 */
	private void onCopySelectedFiles()
	{
		File[] copied = getSelectedFiles();
		ClipboardUtils.sendFilesToClipboard(copied);
		if (directoryTreeListener != null)
			directoryTreeListener.onFilesCopied(copied);
	}

	/**
	 * Prepares to copy file path in the tree.
	 */
	private void onCopySelectedFilePath()
	{
		File[] copied = getSelectedFiles();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (File file : copied)
		{
			if (!first)
				sb.append("\n");
			sb.append(file.getAbsolutePath());
			first = false;
		}

		ClipboardUtils.sendStringToClipboard(sb.toString());
		if (directoryTreeListener != null)
			directoryTreeListener.onFilesCopied(copied);
	}

	/**
	 * Prepares to copy relative file path in the tree.
	 */
	private void onCopySelectedRelativeFilePath()
	{
		File[] copied = getSelectedFiles();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (File file : copied)
		{
			if (!first)
				sb.append("\n");
			
			if (file.equals(rootDirectory))
				sb.append(".");
			else
				sb.append(file.getAbsolutePath().substring(rootDirectory.getAbsolutePath().length() + 1));
			first = false;
		}

		ClipboardUtils.sendStringToClipboard(sb.toString());
		if (directoryTreeListener != null)
			directoryTreeListener.onFilesCopied(copied);
	}

	/**
	 * Prepares to copy files into the tree from the clipboard.
	 */
	private void onPasteClipboardFilesIntoSelectedDirectory()
	{
		File directory = getSelectedFile();
		if (!directory.isDirectory())
			return;
		
		File[] sourceFiles = ClipboardUtils.getFilesFromClipboard();
		
		if (sourceFiles != null && sourceFiles.length > 0)
			onPasteFiles(directory, sourceFiles);
	}

	/**
	 * Prepares to copy files into the tree from clipboard.
	 */
	private void onPasteFiles(File parent, File[] filesToPaste)
	{
		int fileCount = countFilesToCopy(filesToPaste);
		
		String dialogtitle = language.getText("dirtree.modal.paste.title");
		String dialogLabel = language.getText("dirtree.modal.paste.message", fileCount, parent.getAbsolutePath());
		
		// Copy and skip/overwrite.
		Boolean overwrite = utils.createModal(dialogtitle, containerOf(node(BorderLayout.CENTER, wrappedLabel(dialogLabel))), 
			utils.createChoiceFromLanguageKey("dirtree.modal.paste.choice.copyskip", (Boolean)false),
			utils.createChoiceFromLanguageKey("dirtree.modal.paste.choice.copyover", (Boolean)true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)null)
		).openThenDispose();
		
		if (overwrite == null)
			return;

		doFileRelocate(parent, filesToPaste, overwrite, "dirtree.newfile.modal.copy.title", "dirtree.newfile.copy.result");
		
		if (directoryTreeListener != null)
			directoryTreeListener.onFilesCopied(filesToPaste);
	}

	/**
	 * Prepares to copy files into the tree from drag-n-drop source.
	 */
	private void onDroppedFiles(File[] filesToPaste)
	{
		int fileCount = countFilesToCopy(filesToPaste);
		File parent = ((FileNode)fileTree.getDropLocation().getPath().getLastPathComponent()).file;
		if (!parent.isDirectory())
			parent = parent.getParentFile();
		
		String dialogtitle = language.getText("dirtree.modal.drop.title");
		String dialogLabel = language.getText("dirtree.modal.drop.message", fileCount, parent.getAbsolutePath());
		
		// Copy and skip/overwrite.
		Boolean overwrite = utils.createModal(dialogtitle, containerOf(node(BorderLayout.CENTER, wrappedLabel(dialogLabel))), 
			utils.createChoiceFromLanguageKey("dirtree.modal.drop.choice.copyskip", (Boolean)false),
			utils.createChoiceFromLanguageKey("dirtree.modal.drop.choice.copyover", (Boolean)true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)null)
		).openThenDispose();
		
		if (overwrite == null)
			return;

		doFileRelocate(parent, filesToPaste, overwrite, "dirtree.newfile.modal.copy.title", "dirtree.newfile.copy.result");
	}

	/**
	 * Prepares to delete files in the tree.
	 */
	private void onDeleteSelectedFiles()
	{
		File[] selectedFiles = getSelectedFiles();

		if (SwingUtils.noTo(language.getText("dirtree.delete", selectedFiles.length)))
			return;

		File[] out = NO_FILES;
		
		for (int i = 0; i < selectedFiles.length; i++)
		{
			if (FileUtils.filePathEquals(selectedFiles[i], rootDirectory))
				continue;
			
			out = ArrayUtils.joinArrays(out, FileUtils.deleteDirectory(selectedFiles[i], true));
			
			File parent = selectedFiles[i].getParentFile();
			if (parent == null)
				continue;
			TreePath path = getTreePathForFile(parent);
			if (path == null)
				reloadNode((FileNode)((FileTreeModel)fileTree.getModel()).getRoot());
			else
				reloadNode((FileNode)path.getLastPathComponent());
		}
		
		SwingUtils.info(language.getText("dirtree.delete.result", out.length));
		if (directoryTreeListener != null)
			directoryTreeListener.onFilesDeleted(out);
	}

	/**
	 * Prepares to reveal a file in the system explorer.
	 */
	private void onRevealSelectedFileInSystem()
	{
		File selected = getSelectedFile();
		if (selected != null)
			Common.openInSystemBrowser(selected);
	}

	/**
	 * Prepares to open a directory in a shell.
	 */
	private void onOpenTerminalHere()
	{
		File selected = getSelectedFile();
		if (selected != null)
		{
			if (selected.isDirectory())
				Common.openTerminalAtDirectory(selected);
			else
				Common.openTerminalAtDirectory(selected.getParentFile());
		}
	}

	/**
	 * Sets a selected directory as the new root.
	 */
	private void onMakeSelectedDirectoryRoot()
	{
		File selected = getSelectedFile();
		if (selected != null && selected.isDirectory())
			setTemporaryRootDirectory(selected);
	}

	/**
	 * Prepares to open selected files (confirms all).
	 */
	private void onOpenFiles()
	{
		for (File file : getSelectedFiles())
			if (directoryTreeListener != null)
				directoryTreeListener.onFileConfirmed(file);
	}

	private void selectNone()
	{
		fileTree.clearSelection();
	}

	private void reloadNode(FileNode node)
	{
		((FileTreeModel)fileTree.getModel()).reload(node);
	}

	private void doFileRelocate(File parent, File[] filesToPaste, Boolean overwrite, String titleKey, String resultKey) 
	{
		final AtomicBoolean cancelSwitch = new AtomicBoolean(false);
		final AtomicInteger result = new AtomicInteger(0);
		
		final int fileCount = countFilesToCopy(filesToPaste);
		
		JLabel fileLabel = label();
		JProgressBar progressBar = progressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(fileCount);
		progressBar.setPreferredSize(dimension(100, 20));
		
		final Modal<Void> modal = modal(language.getText(titleKey), containerOf(dimension(200, 64), gridLayout(2, 1, 0, 4),
			node(BorderLayout.NORTH, fileLabel),
			node(BorderLayout.SOUTH, progressBar)
		));
		
		InstancedFuture<Void> copyTask = tasks.spawn(createCopyTask(parent, filesToPaste, overwrite, cancelSwitch, result, (file) -> {
			fileLabel.setText(file != null ? file.getName() + "..." : "");
			progressBar.setValue(result.get());
			if (result.get() == fileCount)
				modal.dispose();
		}));
		
		modal.openThenDispose(); // on close, continue
		cancelSwitch.set(true);  // cancel to stop task if not over.
		copyTask.result();       // join task
	
		SwingUtils.info(language.getText(resultKey, result.get()));
		
		reloadNode((FileNode)getTreePathForFile(parent).getLastPathComponent());
	}

	private int countFilesToCopy(File[] toCopy)
	{
		int out = 0;
		for (int i = 0; i < toCopy.length; i++)
		{
			File file = toCopy[i];
			if (file.isDirectory())
				out += countFilesToCopy(file.listFiles());
			out++;
		}
		return out;
	}
	
	private Runnable createCopyTask(File parent, File[] toCopy, boolean overwrite, AtomicBoolean cancelSwitch, AtomicInteger result, Consumer<File> eachFile)
	{
		return () -> copyFolder(parent, toCopy, overwrite, cancelSwitch, result, eachFile);
	}
	
	private void copyFolder(File parent, File[] toCopy, boolean overwrite, AtomicBoolean cancelSwitch, AtomicInteger result, Consumer<File> eachFile)
	{
		for (int i = 0; i < toCopy.length; i++) 
		{
			if (cancelSwitch.get())
				return;
			
			File source = toCopy[i];
			File target = new File(parent.getAbsolutePath() + File.separator + source.getName());
			
			if (!source.isDirectory())
			{
				FileUtils.createPathForFile(target);
				if (overwrite || !target.exists())
				{
					try (FileInputStream fis = new FileInputStream(source); FileOutputStream fos = new FileOutputStream(target))
					{
						IOUtils.relay(fis, fos);
					} 
					catch (FileNotFoundException e)
					{
						SwingUtils.error(language.getText("dirtree.newfile.copy.notfound", source));
						target.delete();
					} 
					catch (IOException e)
					{
						SwingUtils.error(language.getText("dirtree.newfile.copy.ioerror", target));
						target.delete();
					}
					catch (SecurityException e)
					{
						SwingUtils.error(language.getText("dirtree.newfile.copy.security"));
						target.delete();
					}
				}
				result.incrementAndGet();
				eachFile.accept(target);
			}
			else
			{
				FileUtils.createPath(target.getAbsolutePath());
				copyFolder(target, source.listFiles(), overwrite, cancelSwitch, result, eachFile);
				result.incrementAndGet();
				eachFile.accept(target);
			}
		}
	}

	/**
	 * Listener interface for tree actions.
	 */
	public interface DirectoryTreeListener
	{
		/**
		 * Called when a file selection changes.
		 * @param selectedFiles the selected files.
		 */
		void onFileSelectionChange(File[] selectedFiles);
		
		/**
		 * Called when a file is confirmed via selection (double-click).
		 * @param confirmedFile the file confirmed.
		 */
		void onFileConfirmed(File confirmedFile);
		
		/**
		 * Called when a new file was already renamed successfully.
		 * @param changedFile the file about to be renamed.
		 * @param newName the new name for the file.
		 */
		void onFileRename(File changedFile, String newName);
		
		/**
		 * Called when a group of files were already copied into the clipboard.
		 * @param copiedFiles the files to copy.
		 */
		void onFilesCopied(File[] copiedFiles);
	
		/**
		 * Called when a group of files have already been deleted.
		 * @param deletedFiles the files deleted.
		 */
		void onFilesDeleted(File[] deletedFiles);

	}


	/**
	 * A File JTree.
	 */
	private class FileTree extends JTree
	{
		private static final long serialVersionUID = 216046587832386980L;

		private FileTree(File rootDirectory, boolean readOnly)
		{
			super();
			setRootDirectory(rootDirectory);
			setEditable(!readOnly);
			setLargeModel(true);
			setInvokesStopCellEditing(false);
			setExpandsSelectedPaths(true);

			setTransferHandler(new FileTransferTreeHandler());
			setDragEnabled(true);
			setDropMode(DropMode.ON);
			
			setCellRenderer(new FileTreeCellRenderer());
		}
		
		/**
		 * Sets the new root directory.
		 * @param rootDirectory the new root directory.
		 */
		public void setRootDirectory(File rootDirectory) 
		{
			setModel(new FileTreeModel(new FileNode(null, rootDirectory)));
		}
		
	}
	
	/**
	 * File Tree Listener
	 */
	private class FileTreeInputListener implements KeyListener, MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e) 
		{
			// Left click.
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				// Double click.
				if (e.getClickCount() == 2)
				{
					if (directoryTreeListener != null)
					{
						TreePath path = fileTree.getClosestPathForLocation(e.getX(), e.getY());
						if (path != null)
						{
							fileTree.setSelectionPath(path);
							directoryTreeListener.onFileConfirmed(((FileNode)path.getLastPathComponent()).file);
						}
					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) 
		{
			if (e.isPopupTrigger())
				doPopupTrigger(e.getComponent(), e.getX(), e.getY());
		}

		@Override
		public void mouseReleased(MouseEvent e) 
		{
			if (e.isPopupTrigger())
				doPopupTrigger(e.getComponent(), e.getX(), e.getY());
		}

		@Override
		public void mouseEntered(MouseEvent e) 
		{
			// Do nothing.
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			// Do nothing.
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			// Do nothing.
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			int selectedCount = fileTree.getSelectionCount();
			if (isKeyStroke(deleteKeyStroke, e))
			{
				if (selectedCount > 0)
					onDeleteSelectedFiles();
			}
			else if (isKeyStroke(copyKeyStroke, e))
			{
				if (selectedCount > 0)
					onCopySelectedFiles();
			}
			else if (isKeyStroke(pasteKeyStroke, e))
			{
				if (selectedCount > 0 && directoryIsSelected())
					onPasteClipboardFilesIntoSelectedDirectory();
			}
			else if (isKeyStroke(refreshKeystroke, e))
			{
				onRefreshSelectedFiles();
			}
			else if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
			{
				Point point = getMousePosition();
				if (point != null)
					doPopupTrigger(e.getComponent(), point.x, point.y);
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			// Do nothing.
		}

		private void doPopupTrigger(Component component, int x, int y)
		{
			TreePath path = fileTree.getClosestPathForLocation(x, y);
			if (path != null)
			{
				int selectedCount = fileTree.getSelectionCount();
				if (selectedCount < 2)
					fileTree.setSelectionPath(path);
				
				if (selectedCount == 1)
				{
					if (directoryIsSelected())
						singleDirectoryPopupMenu.show(component, x, y);
					else
						singleFilePopupMenu.show(component, x, y);
				}
				else if (selectedCount > 1)
				{
					multiFilePopupMenu.show(component, x, y);
				}
			}
		}
	}


	/**
	 * The listener for events that happen on the Tree.
	 */
	private class FileTreeListener implements TreeSelectionListener, TreeExpansionListener, TreeWillExpandListener
	{
		private FileTreeListener() { }
		
		@Override
		public void valueChanged(TreeSelectionEvent event)
		{
			if (directoryTreeListener != null)
				directoryTreeListener.onFileSelectionChange(getSelectedFiles());
		}
		
		@Override
		public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException
		{
			// Do nothing.
		}

		@Override
		public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException
		{
			// Do nothing.
		}

		@Override
		public void treeExpanded(TreeExpansionEvent event) 
		{
			// Do nothing.
		}

		@Override
		public void treeCollapsed(TreeExpansionEvent event) 
		{
			reloadNode((FileNode)event.getPath().getLastPathComponent());
		}

	}

	/**
	 * The model for the tree.
	 */
	private class FileTreeModel extends DefaultTreeModel
	{
		private static final long serialVersionUID = -354549801506916306L;

		private FileTreeModel(FileNode root) 
		{
			super(root);
		}
		
		@Override
		public void reload(TreeNode node) 
		{
			((FileNode)node).clearChildren();
			super.reload(node);
		}
	}

	/**
	 * The cell renderer.
	 * Maybe one day this will have custom icons, but for now this is to make it so that
	 * FileNode.toString() does not affect Tree presentation (because it affects traversal).
	 */
	private static class FileTreeCellRenderer extends DefaultTreeCellRenderer
	{
		private static final long serialVersionUID = -661862086263680971L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) 
		{
			JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if (value instanceof FileNode)
				label.setText(((FileNode)value).file.getName());
			return label;
		}
	}
	
	
	/**
	 * A single node in the file tree.
	 */
	private class FileNode implements MutableTreeNode
	{
		/** The Parent node. */
		private FileNode parent;
		/** The file. */
		private File file;
		/** List of children. */
		private ArrayList<FileNode> children;
		
		/**
		 * Creates a node.
		 * @param parent the parent node.
		 * @param file the file value of this node.
		 */
		private FileNode(FileNode parent, File file)
		{
			this.parent = parent;
			this.file = FileUtils.canonizeFile(file);
			this.children = null;
		}

		/**
		 * Clears the children.
		 */
		public synchronized void clearChildren()
		{
			children = null;
		}
		
		/**
		 * Refreshes the children.
		 */
		public synchronized void refreshChildren()
		{
			if (children != null)
				return;
			
			File[] files = file.listFiles();
			if (files == null)
				return;
			
			Arrays.sort(files, FileUtils.getFileListComparator());
			
			children = new ArrayList<>(files.length);
			for (int i = 0; i < files.length; i++) 
				children.add(new FileNode(this, files[i]));
		}

		@Override
		public TreeNode getChildAt(int childIndex) 
		{
			refreshChildren();
			return children.get(childIndex);
		}

		@Override
		public int getChildCount() 
		{
			refreshChildren();
			return children == null ? 0 : children.size();
		}

		@Override
		public TreeNode getParent() 
		{
			return parent;
		}

		@Override
		public int getIndex(TreeNode node) 
		{
			refreshChildren();
			return children.indexOf(node);
		}

		@Override
		public boolean getAllowsChildren() 
		{
			return file.isDirectory();
		}

		@Override
		public boolean isLeaf() 
		{
			return !file.isDirectory();
		}

		@Override
		public Enumeration<FileNode> children()
		{
			refreshChildren();
			return enumeration(children.iterator());
		}
		
		@Override
		public void insert(MutableTreeNode child, int index)
		{
			if (child instanceof FileNode)
			{
				FileNode childNode = (FileNode)child;
				children.add(index, childNode);
				children.sort(CHILD_COMPARATOR);
			}
		}

		@Override
		public void remove(int index) 
		{
			children.remove(index);
		}

		@Override
		public void remove(MutableTreeNode node)
		{
			if (node instanceof FileNode)
			{
				FileNode fileNode = (FileNode)node;
				children.remove(fileNode);
			}
		}

		@Override
		public void setUserObject(Object object) 
		{
			// Rename occurred.
			if (object instanceof String)
			{
				File oldFile = new File(file.getPath());
				File newFile = new File(oldFile.getParent() + File.separator + String.valueOf(object));
				if (oldFile.renameTo(new File(oldFile.getParent() + File.separator + String.valueOf(object))))
				{
					file = newFile;
					if (directoryTreeListener != null)
						directoryTreeListener.onFileRename(oldFile, String.valueOf(object));
					SwingUtils.invoke(() -> {
						reloadNode(this);
						selectNone();
					});
				}
			}
		}

		@Override
		public void removeFromParent()
		{
			if (parent != null)
				parent.remove(this);
		}

		@Override
		public void setParent(MutableTreeNode newParent) 
		{
			if (newParent instanceof FileNode)
				parent = (FileNode)newParent;
		}

		@Override
		public boolean equals(Object obj) 
		{
			if (obj instanceof FileNode)
			{
				FileNode node = (FileNode)obj;
				return CHILD_COMPARATOR.compare(this, node) == 0;
			}
			else
			{
				return super.equals(obj);
			}
		}
		
		@Override
		public String toString() 
		{
			if (parent == null)
				return file.getPath();
			else
				return file.getName();
		}

	}

	/**
	 * Handle only drag-and-dropped files.
	 */
	private class FileTransferTreeHandler extends TransferHandler
	{
		private static final long serialVersionUID = -2253220534002536924L;

		@Override
		public boolean canImport(TransferSupport support) 
		{
			return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean importData(TransferSupport support) 
		{
			if (!support.isDrop())
				return false;
			
			Transferable transferable = support.getTransferable();
			List<File> files;
			try {
				files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
			} catch (UnsupportedFlavorException | IOException e) {
				LOG.warn("Could not handle DnD import for file drop.");
				return false;
			}
			onDroppedFiles(files.toArray(new File[files.size()]));
			return true;
		}
		
	}

	// =======================================================================

	// Make an Enumeration from an Iterator.
	private static <T> Enumeration<T> enumeration(final Iterator<T> iter)
	{
		return new Enumeration<T>() 
		{
			@Override
			public boolean hasMoreElements() 
			{
				return iter.hasNext();
			}

			@Override
			public T nextElement()
			{
				return iter.next();
			}
		};
	}

	// Tests if a keystroke was performed in an event.
	private static boolean isKeyStroke(KeyStroke keyStroke, KeyEvent event)
	{
		return KeyStroke.getKeyStrokeForEvent(event).equals(keyStroke);
	}
	
}
