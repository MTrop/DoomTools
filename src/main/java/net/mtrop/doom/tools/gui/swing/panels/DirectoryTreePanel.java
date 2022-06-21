package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTree;
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

import net.mtrop.doom.tools.struct.util.OSUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

/**
 * A panel that shows a directory tree.
 * @author Matthew Tropiano
 */
public class DirectoryTreePanel extends JPanel
{
	private static final long serialVersionUID = 5496698746549135647L;
	
	/** The file tree itself. */
	private FileTree fileTree;
	/** The file tree listener. */
	private FileTreeListener fileTreeListener;
	
	// TODO: Finish this.

	/**
	 * Creates a file tree panel.
	 * @param rootDirectory the root directory.
	 * @param readOnly if the tree is read-only (not editable).
	 */
	public DirectoryTreePanel(File rootDirectory, boolean readOnly)
	{
		if (!rootDirectory.isDirectory())
			throw new IllegalArgumentException(rootDirectory.getPath() + " is not a directory.");
		
		FileTree tree = new FileTree(rootDirectory, readOnly);
		FileTreeListener treeListener = new FileTreeListener();
		tree.addTreeSelectionListener(treeListener);
		tree.addTreeWillExpandListener(treeListener);
		tree.addTreeExpansionListener(treeListener);
		
		this.fileTree = tree;
		this.fileTreeListener = null;
		
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
	 * A File JTree.
	 */
	private static class FileTree extends JTree
	{
		private static final long serialVersionUID = 216046587832386980L;

		/** The root node. */
		private FileNode rootNode;

		private FileTree(File rootDirectory, boolean readOnly)
		{
			super();
			setRootDirectory(rootDirectory);
			setEditable(!readOnly);
		}
		
		/**
		 * Refresh the tree.
		 */
		public void refresh()
		{
			((DefaultTreeModel)getModel()).reload();
		}
		
		/**
		 * Sets the new root directory.
		 * @param rootDirectory
		 */
		public void setRootDirectory(File rootDirectory) 
		{
			setModel(new DefaultTreeModel(rootNode = new FileNode(null, rootDirectory)));
		}
		
	}
	
	
	/**
	 * The listener for events that happen on the Tree.
	 */
	public class FileTreeListener implements TreeSelectionListener, TreeExpansionListener, TreeWillExpandListener
	{
		@Override
		public void valueChanged(TreeSelectionEvent event)
		{
			TreePath[] paths = event.getPaths(); 
			for (int i = 0; i < paths.length; i++)
				System.out.println((event.isAddedPath(i) ? "Added" : "Removed") + " " + Arrays.toString(paths[i].getPath()));
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
	 * A single node in the file tree.
	 */
	private static class FileNode implements MutableTreeNode
	{
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
		public FileNode(FileNode parent, File file)
		{
			this.parent = parent;
			this.file = canonize(file);
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
			return true;
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
		public String toString() 
		{
			return file.getName();
		}

		@Override
		public void insert(MutableTreeNode child, int index)
		{
			if (child instanceof FileNode)
			{
				FileNode childNode = (FileNode)child;
				// TODO: Finish this.
			}
		}

		@Override
		public void remove(int index) 
		{
			System.out.println("Remove index: " + index);
			// TODO: Finish this.
		}

		@Override
		public void remove(MutableTreeNode node)
		{
			System.out.println("Remove node: " + node);
			// TODO: Finish this.
		}

		@Override
		public void setUserObject(Object object) 
		{
			// Rename occurred.
			if (object instanceof String)
			{
				// TODO: Finish this.
				System.out.println("Set user object: " + object);
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
	}

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

	private static File canonize(File source)
	{
		try {
			return source.getCanonicalFile();
		} catch (IOException e) {
			return source.getAbsoluteFile();
		}
	}
	
}
