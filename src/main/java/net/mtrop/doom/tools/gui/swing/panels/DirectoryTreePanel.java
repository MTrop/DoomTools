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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DropMode;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.ClipboardUtils;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;


/**
 * A panel that shows a directory tree.
 * @author Matthew Tropiano
 */
public class DirectoryTreePanel extends JPanel
{
	// TODO: NOT DONE. Finish this!
	
	private static final long serialVersionUID = 5496698746549135647L;
	
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DirectoryTreePanel.class); 

    private static final Comparator<File> CHILD_FILE_COMPARATOR;
	private static final Comparator<FileNode> CHILD_COMPARATOR;

	static
	{
		final Comparator<File> fileNameComparator = (a, b) -> (
			OSUtils.isWindows()
				? String.CASE_INSENSITIVE_ORDER.compare(a.getPath(), b.getPath())
			    : a.getPath().compareTo(b.getPath())
		);
		
		CHILD_FILE_COMPARATOR = (a, b) -> (
			a.isDirectory() 
				? (b.isDirectory() ? fileNameComparator.compare(a, b) : -1)
				: (b.isDirectory() ? 1 : fileNameComparator.compare(a, b))
		);
		
		CHILD_COMPARATOR = (a, b) -> CHILD_FILE_COMPARATOR.compare(a.file, b.file);
	}
	
	// =======================================================================
	
	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	
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

	// State

	private Set<File> currentlySelectedFiles;

	
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
		if (!rootDirectory.isDirectory())
			throw new IllegalArgumentException(rootDirectory.getPath() + " is not a directory.");
		
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		
		FileTree tree = new FileTree(rootDirectory, readOnly);
		FileTreeListener treeListener = new FileTreeListener();
		tree.addTreeSelectionListener(treeListener);
		tree.addTreeWillExpandListener(treeListener);
		tree.addTreeExpansionListener(treeListener);
		
		FileTreeInputListener inputListener = new FileTreeInputListener();
		tree.addKeyListener(inputListener);
		tree.addMouseListener(inputListener);
		
		this.rootDirectory = FileUtils.canonizeFile(rootDirectory);
		this.fileTree = tree;
		this.directoryTreeListener = directoryTreeListener;

		this.singleFilePopupMenu = createSingleFilePopupMenu();
		this.singleDirectoryPopupMenu = createSingleDirectoryPopupMenu();
		this.multiFilePopupMenu = createMultiFilePopupMenu();
		
		this.copyKeyStroke = language.getKeyStroke("texteditor.action.copy.keystroke");
		this.pasteKeyStroke = language.getKeyStroke("texteditor.action.paste.keystroke");
		this.deleteKeyStroke = language.getKeyStroke("texteditor.action.delete.keystroke");

		this.currentlySelectedFiles = Collections.synchronizedSet(new TreeSet<>());
		
		containerOf(this,
			node(BorderLayout.CENTER, scroll(tree))
		);
	}
	
	private JPopupMenu createSingleFilePopupMenu()
	{
		return popupMenu(
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.open", (c, e) -> onOpenFiles()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.rename.file", (c, e) -> onRenameSelectedFile()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.reveal", (c, e) -> onRevealSelectedFileInSystem()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy", (c, e) -> onCopySelectedFiles()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path", (c, e) -> onCopySelectedFilePath()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path.rel", (c, e) -> onCopySelectedRelativeFilePath()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.paste", (c, e) -> onPasteClipboardFilesIntoSelectedDirectory()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.delete", (c, e) -> onDeleteSelectedFiles())
		);
	}
	
	private JPopupMenu createSingleDirectoryPopupMenu()
	{
		return popupMenu(
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.new.file", (c, e) -> onAddNewFile()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.new.directory", (c, e) -> onAddNewDirectory()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.rename.directory", (c, e) -> onRenameSelectedFile()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.reveal", (c, e) -> onRevealSelectedFileInSystem()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.root", (c, e) -> onMakeSelectedDirectoryRoot()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy", (c, e) -> onCopySelectedFiles()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path", (c, e) -> onCopySelectedFilePath()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path.rel", (c, e) -> onCopySelectedRelativeFilePath()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.paste", (c, e) -> onPasteClipboardFilesIntoSelectedDirectory()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.delete", (c, e) -> onDeleteSelectedFiles())
		);
	}
	
	private JPopupMenu createMultiFilePopupMenu()
	{
		return popupMenu(
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.open", (c, e) -> onOpenFiles()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy", (c, e) -> onCopySelectedFiles()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path", (c, e) -> onCopySelectedFilePath()),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.copy.path.rel", (c, e) -> onCopySelectedRelativeFilePath()),
			separator(),
			utils.createItemFromLanguageKey("dirtree.popup.menu.item.delete", (c, e) -> onDeleteSelectedFiles())
		);
	}
	
	/**
	 * Refresh the tree.
	 */
	public void refresh()
	{
		fileTree.setRootDirectory(rootDirectory);
	}
	
	/**
	 * Sets the new root directory (temporarily).
	 * @param rootDirectory
	 */
	public void setRootDirectory(File rootDirectory) 
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
	public File getSelectedFile()
	{
		File out = null;
		for (File file : currentlySelectedFiles)
		{
			out = file;
			break;
		}
		return out;
	}
	
	/**
	 * Gets all currently selected files.
	 * @return the array of currently selected files.
	 */
	public File[] getSelectedFiles()
	{
		return currentlySelectedFiles.toArray(new File[currentlySelectedFiles.size()]);
	}
	
	/**
	 * @return true if one of the currently selected files is a directory.
	 */
	private boolean directoryIsSelected()
	{
		for (File file : currentlySelectedFiles)
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

		FileNode currentNode = (FileNode)fileTree.model().getRoot();
		
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
	
	/**
	 * Adds a new file under a target directory.
	 * @param targetDirectory the parent directory.
	 */
	private void onAddNewFile()
	{
		File targetDirectory = getSelectedFile();

		int i = 1;
		File newFile;
		do {
			newFile = new File(targetDirectory + File.separator + "New File " + (i++));
		} while (newFile.exists());
		
		try {
			if (FileUtils.touch(newFile))
				onRenameFile(newFile);
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
		
		int i = 1;
		File newFile;
		do {
			newFile = new File(targetDirectory + File.separator + "New Folder " + (i++));
		} while (newFile.exists());
		
		try {
			if (newFile.mkdir())
				onRenameFile(newFile);
			else
				SwingUtils.error(this, language.getText("dirtree.newdir.error"));
		} catch (SecurityException e) {
			SwingUtils.error(this, language.getText("dirtree.newdir.error.security", e.getLocalizedMessage()));
		}
	}

	/**
	 * Renames the currently selected file.
	 */
	private void onRenameSelectedFile()
	{
		if (currentlySelectedFiles.size() == 1)
			for (File file : currentlySelectedFiles)
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
	 * Prepares to copy files into the tree.
	 */
	private void onPasteFiles(File parent, File[] filesToPaste)
	{
		// TODO: Move or Copy, Overwrite or No
	}

	/**
	 * Prepares to delete files in the tree.
	 */
	private void onDeleteSelectedFiles()
	{
		
		
		// TODO: Ask to confirm.
		// TODO: Delete files / directories.
		// TODO: For each deleted, remove node from tree.
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
	 * Sets a selected directory as the new root.
	 */
	private void onMakeSelectedDirectoryRoot()
	{
		File selected = getSelectedFile();
		if (selected != null && selected.isDirectory())
			setRootDirectory(selected);
	}

	/**
	 * Prepares to open selected files (confirms all).
	 */
	private void onOpenFiles()
	{
		for (File file : currentlySelectedFiles)
			if (directoryTreeListener != null)
				directoryTreeListener.onFileConfirmed(file);
	}

	/**
	 * Listener interface for tree actions.
	 */
	public interface DirectoryTreeListener
	{
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
		 * Called when a new file was already inserted into the tree.
		 * @param fileName the name of the new file (just name).
		 * @param parentFile its parent file. Must be a directory.
		 * @param directory if the added file is a directory.
		 * @return true if the insert was successful.
		 */
		boolean onFileInsert(String fileName, File parentFile, boolean directory);
		
		/**
		 * Called when a group of files were already dropped into a place in the tree.
		 * @param parentFile the parent node in the tree. Must be a directory.
		 * @param droppedSourceFiles the array of file paths that were dropped in. 
		 * @return true if the files were moved/copied, false otherwise.
		 */
		boolean onFilesDropped(File parentFile, File[] droppedSourceFiles);
	
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
			setDropMode(DropMode.ON_OR_INSERT);
		}
		
		/**
		 * Sets the new root directory.
		 * @param rootDirectory the new root directory.
		 */
		public void setRootDirectory(File rootDirectory) 
		{
			setModel(new FileTreeModel(new FileNode(null, rootDirectory)));
		}
		
		// Get model.
		private FileTreeModel model()
		{
			return (FileTreeModel)getModel();
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
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			if (isKeyStroke(deleteKeyStroke, e))
			{
				if (!currentlySelectedFiles.isEmpty())
					onDeleteSelectedFiles();
			}
			else if (isKeyStroke(copyKeyStroke, e))
			{
				if (!currentlySelectedFiles.isEmpty())
					onCopySelectedFiles();
			}
			else if (isKeyStroke(pasteKeyStroke, e))
			{
				if (!currentlySelectedFiles.isEmpty() && directoryIsSelected())
					onPasteClipboardFilesIntoSelectedDirectory();
			}
			else if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
			{
				Point point = getMousePosition();
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
				if (currentlySelectedFiles.size() < 2)
					fileTree.setSelectionPath(path);
				
				if (currentlySelectedFiles.size() == 1)
				{
					if (directoryIsSelected())
						singleDirectoryPopupMenu.show(component, x, y);
					else
						singleFilePopupMenu.show(component, x, y);
				}
				else if (currentlySelectedFiles.size() > 1)
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
			TreePath[] paths = event.getPaths(); 
			for (int i = 0; i < paths.length; i++)
			{
				if (event.isAddedPath(i))
				{
					File file = ((FileNode)paths[i].getLastPathComponent()).file;
					currentlySelectedFiles.add(file);
				}
				else
				{
					File file = ((FileNode)paths[i].getLastPathComponent()).file;
					currentlySelectedFiles.remove(file);
				}
			}
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
			((FileNode)event.getPath().getLastPathComponent()).free();
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
		
		private synchronized void free()
		{
			children = null;
		}
		
		/**
		 * Refreshes the children.
		 */
		public synchronized void refreshChildren()
		{
			File[] files = file.listFiles();
			if (files == null)
				return;
			
			Arrays.sort(files, CHILD_FILE_COMPARATOR);
			
			children = new ArrayList<>(files.length);
			for (int i = 0; i < files.length; i++) 
				children.add(new FileNode(this, files[i]));
			validate();
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
				validate();
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
		return keyStroke.getKeyChar() == event.getKeyChar()
			&& keyStroke.getKeyCode() == event.getKeyCode()
			&& keyStroke.getModifiers() == event.getModifiers()
		;
	}
	
}
