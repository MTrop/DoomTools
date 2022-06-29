package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
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
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.KeyStroke;
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

import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;


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
				? String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName())
			    : a.getName().compareTo(b.getName())
		);
		
		CHILD_FILE_COMPARATOR = (a, b) -> (
			a.isDirectory() 
				? (b.isDirectory() ? fileNameComparator.compare(a, b) : -1)
				: (b.isDirectory() ? 1 : fileNameComparator.compare(a, b))
		);
		
		CHILD_COMPARATOR = (a, b) -> CHILD_FILE_COMPARATOR.compare(a.file, b.file);
	}
	
	// =======================================================================
	
	private DoomToolsLanguageManager language;
	
	/** The file tree itself. */
	private File rootDirectory;
	/** The file tree itself. */
	private FileTree fileTree;
	/** The file tree listener. */
	private DirectoryTreeListener directoryTreeListener;
	
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
		
		this.language = DoomToolsLanguageManager.get();
		
		FileTree tree = new FileTree(rootDirectory, readOnly);
		FileTreeListener treeListener = new FileTreeListener();
		tree.addTreeSelectionListener(treeListener);
		tree.addTreeWillExpandListener(treeListener);
		tree.addTreeExpansionListener(treeListener);
		
		FileTreeInputListener inputListener = new FileTreeInputListener();
		tree.addKeyListener(inputListener);
		tree.addMouseListener(inputListener);
		
		this.rootDirectory = rootDirectory;
		this.fileTree = tree;
		this.directoryTreeListener = directoryTreeListener;
		
		this.deleteKeyStroke = language.getKeyStroke("texteditor.action.delete.keystroke");
		this.copyKeyStroke = language.getKeyStroke("texteditor.action.copy.keystroke");
		this.pasteKeyStroke = language.getKeyStroke("texteditor.action.paste.keystroke");

		this.currentlySelectedFiles = Collections.synchronizedSet(new TreeSet<>());
		
		containerOf(this,
			node(BorderLayout.CENTER, scroll(tree))
		);
	}
	
	/**
	 * Refresh the tree.
	 */
	public void refresh()
	{
		fileTree.refresh();
	}
	
	/**
	 * Sets the new root directory.
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
		fileTree.setRootDirectory(rootDirectory);
	}
	
	/**
	 * Selects a file in the tree.
	 * @param filePath the file path.
	 */
	public void setSelectedFile(File filePath) 
	{
		filePath = FileUtils.canonizeFile(filePath);
		String rootPath = fileTree.model().getRoot().toString();
		String filePathString = filePath.getAbsolutePath();
		// TODO: Finish this.
	}
	
	/**
	 * Adds a new file under a target directory.
	 * @param targetDirectory the parent directory.
	 */
	public void addNewFile(File targetDirectory)
	{
		int i = 1;
		File newFile;
		do {
			newFile = new File(targetDirectory + File.separator + (i++));
		} while (newFile.exists());
		
		boolean made = false; 
		try {
			FileUtils.touch(newFile);
		} catch (IOException e) {
			
		} catch (SecurityException e) {
			
		}
		
		// TODO: Finish this.
	}
	
	/**
	 * Adds a new directory under a target directory.
	 * @param targetDirectory the parent directory.
	 */
	public void addNewDirectory(File targetDirectory)
	{
		// TODO: Finish this.
	}


	/**
	 * Listener interface for tree actions.
	 */
	public interface DirectoryTreeListener
	{
		/**
		 * Called when a file is confirmed.
		 * @param confirmedFile the file confirmed.
		 */
		void onFileConfirmed(File confirmedFile);
		
		/**
		 * Called when a new file is about to be renamed.
		 * @param changedFile the file about to be renamed.
		 * @param newName the new name for the file.
		 * @return true if the rename was successful.
		 */
		boolean onFileRename(File changedFile, String newName);
		
		/**
		 * Called when a new file is inserted into the tree.
		 * @param fileName the name of the new file (just name).
		 * @param parentFile its parent file. Must be a directory.
		 * @param directory if the added file is a directory.
		 * @return true if the insert was successful.
		 */
		boolean onFileInsert(String fileName, File parentFile, boolean directory);
		
		/**
		 * Called when a group of files are dropped into a place in the tree.
		 * @param parentFile the parent node in the tree. Must be a directory.
		 * @param droppedSourceFiles the array of file paths that were dropped in. 
		 * @return true if the files were moved/copied, false otherwise.
		 */
		boolean onFilesDropped(File parentFile, File[] droppedSourceFiles);
	
		/**
		 * Called when a group of files need copying into the clipboard.
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
		}
		
		/**
		 * Refresh the tree.
		 */
		public void refresh()
		{
			model().reload();
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
			// Double left-click.
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
			{
				if (directoryTreeListener != null)
					for (File file : currentlySelectedFiles)
						directoryTreeListener.onFileConfirmed(file);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) 
		{
			// Do nothing.
		}

		@Override
		public void mouseReleased(MouseEvent e) 
		{
			// Do nothing.
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
			if (isKeyStroke(deleteKeyStroke, e))
			{
				if (directoryTreeListener != null && !currentlySelectedFiles.isEmpty())
				{
					File[] files = currentlySelectedFiles.toArray(new File[currentlySelectedFiles.size()]);
					directoryTreeListener.onFilesDeleted(files);
					fileTree.refresh();
				}
			}
			else if (isKeyStroke(copyKeyStroke, e))
			{
				if (directoryTreeListener != null && !currentlySelectedFiles.isEmpty())
				{
					File[] files = currentlySelectedFiles.toArray(new File[currentlySelectedFiles.size()]);
					directoryTreeListener.onFilesCopied(files);
					fileTree.refresh();
				}
			}
			else if (isKeyStroke(pasteKeyStroke, e))
			{
				// TODO: Start paste.
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			// Do nothing.
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
					System.out.println(paths[i]);
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
			((FileNode)event.getPath().getLastPathComponent()).validate();
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
		
		/**
		 * Ensures that the children exist.
		 */
		private synchronized void validate()
		{
			if (children == null)
				refresh();
		}
		
		/**
		 * Frees the children in the tree.
		 */
		private synchronized void free()
		{
			children = null;
		}

		/**
		 * Refreshes the children.
		 */
		public synchronized void refresh()
		{
			File[] files = file.listFiles();
			
			Arrays.sort(files, CHILD_FILE_COMPARATOR);
			
			children = new ArrayList<>(files.length);
			for (int i = 0; i < files.length; i++) 
				children.add(new FileNode(this, files[i]));
		}

		@Override
		public TreeNode getChildAt(int childIndex) 
		{
			validate();
			return children.get(childIndex);
		}

		@Override
		public int getChildCount() 
		{
			validate();
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
			validate();
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
			validate();
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
			FileNode node = children.remove(index);
			if (node != null)
				deleteFile(node.file);
		}

		@Override
		public void remove(MutableTreeNode node)
		{
			if (node instanceof FileNode)
			{
				FileNode fileNode = (FileNode)node;
				if (children.remove(fileNode))
					deleteFile(fileNode.file);
			}
		}

		// Delete file.
		private void deleteFile(File file)
		{
			// TODO: Maybe remove from Node. Model should drive.
			if (file.delete())
				LOG.infof("Deleted file: %s", file.getAbsolutePath());
			else
				LOG.errorf("Could not delete file: %s", file.getAbsolutePath());
		}
		
		@Override
		public void setUserObject(Object object) 
		{
			// Rename occurred.
			if (object instanceof String)
			{
				if (directoryTreeListener != null)
					directoryTreeListener.onFileRename(file, String.valueOf(object));
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
		public String toString() 
		{
			if (parent == null)
				return file.getPath();
			else
				return file.getName();
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
