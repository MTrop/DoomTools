/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.struct.Loader.LoaderFuture;
import net.mtrop.doom.tools.struct.swing.FileChooserFactory;
import net.mtrop.doom.tools.struct.util.FileUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * A panel that stores file paths.
 * @author Matthew Tropiano
 */
public class FileListPanel extends JPanel 
{
	private static final long serialVersionUID = 5877295175127350109L;
	
	private DoomToolsLanguageManager language;
	
	private JList<File> list;
	private FileListModel model;
	
	private Action addAction;
	private Action removeAction;
	private Action moveUpAction;
	private Action moveDownAction;
	
	private FileFilter fileFilter;
	private Consumer<File[]> addedFilesConsumer;
	private Supplier<File> initialFileSupplier;

	
	/**
	 * Creates a new file list panel.
	 * @param titleLabel the label.
	 * @param selectionMode the selection mode, or null for no selection.
	 * @param allowReordering if true, show reordering buttons. If false, no reordering, and files are sorted on add.
	 * @param mutable if true, allow adding and removing files from the list.
	 * @param addedFilesConsumer the consumer to call on file selection.
	 * @param initialFileSupplier the supplier to call for the initial file path.
	 */
	public FileListPanel(
		String titleLabel, 
		ListSelectionMode selectionMode, 
		boolean allowReordering,
		boolean mutable, 
		Consumer<File[]> addedFilesConsumer, 
		Supplier<File> initialFileSupplier
	){
		this.language = DoomToolsLanguageManager.get();
		
		this.model = new FileListModel();
		this.list = new JList<>(model);
		
		this.model.addListDataListener(new ListDataListener() 
		{
			@Override
			public void intervalRemoved(ListDataEvent e) 
			{
				list.repaint();
			}
			
			@Override
			public void intervalAdded(ListDataEvent e) 
			{
				list.repaint();
			}
			
			@Override
			public void contentsChanged(ListDataEvent e) 
			{
				list.repaint();
			}
		});
		
		this.list.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
					onRemoveSelected();
				else if ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)
				{
					if (e.getKeyCode() == KeyEvent.VK_UP)
						onMoveUp();
					else if (e.getKeyCode() == KeyEvent.VK_DOWN)
						onMoveDown();
				}
			}
		});
		
		if (selectionMode != null)
			this.list.setSelectionMode(selectionMode.getSwingId());
		
		this.list.setTransferHandler(new FileDrop());
		
		List<Node> buttonNodes = new LinkedList<>();
		
		DoomToolsIconManager icons = DoomToolsIconManager.get();

		final LoaderFuture<ImageIcon> addIcon = icons.getImageAsync("add.png");
		final LoaderFuture<ImageIcon> removeIcon = icons.getImageAsync("remove.png");
		final LoaderFuture<ImageIcon> upIcon = icons.getImageAsync("up_arrow.png");
		final LoaderFuture<ImageIcon> downIcon = icons.getImageAsync("down_arrow.png");
		
		this.addAction = actionItem(addIcon.result(), (e) -> onAddFiles());
		this.removeAction = actionItem(removeIcon.result(), (e) -> onRemoveSelected());
		this.moveUpAction = actionItem(upIcon.result(), (e) -> onMoveUp());
		this.moveDownAction = actionItem(downIcon.result(), (e) -> onMoveDown());
		
		this.fileFilter = null;
		this.addedFilesConsumer = addedFilesConsumer;
		this.initialFileSupplier = initialFileSupplier;
		
		if (mutable)
		{
			buttonNodes.add(node(button(addAction)));
			buttonNodes.add(node(button(removeAction)));
		}
		
		if (allowReordering)
		{
			buttonNodes.add(node(button(moveUpAction)));
			buttonNodes.add(node(button(moveDownAction)));
		}
		else
		{
			this.model.comparator = FileUtils.getFileComparator();
		}

		containerOf(this, borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(
				node(BorderLayout.CENTER, label(titleLabel)),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.LEADING, 0, 0), 
					buttonNodes.toArray(new Node[buttonNodes.size()])
				))
			)),
			node(BorderLayout.CENTER, containerOf(
				node(BorderLayout.CENTER, scroll(this.list))
			))
		);
	}
	
	/**
	 * Sets the choose-a-file file filter.
	 * @param filter the filter to use.
	 */
	public void setFileFilter(FileFilter filter)
	{
		this.fileFilter = filter;
	}
	
	/**
	 * Adds a file to the model.
	 * If the file is in the model, this does nothing.
	 * @param file the file to add.
	 */
	public void addFile(File file)
	{
		model.addFileAt(model.getSize(), file);
	}

	/**
	 * Adds a set of files to the model.
	 * If a file is in the model, it is not added.
	 * @param files the files to add.
	 */
	public void addFiles(File ... files)
	{
		model.addFilesAt(model.getSize(), files);
	}

	/**
	 * Adds a file to a specific index in the model.
	 * If the file is in the model, this does nothing.
	 * @param index the target index.
	 * @param file the file to add.
	 */
	public void addFileAt(int index, File file)
	{
		model.addFileAt(index, file);
	}

	/**
	 * Adds a set of files to a specific index in the model.
	 * If a file is in the model, it is not added.
	 * @param index the target index.
	 * @param files the files to add.
	 */
	public void addFilesAt(int index, File ... files)
	{
		model.addFilesAt(index, files);
	}

	/**
	 * Removes the provided indices of files.
	 * @param indices the indices of the files in the list.
	 * @return the amount of files removed.
	 */
	public int removeFiles(int ... indices)
	{
		List<File> out = new LinkedList<>();
		
		for (int i = 0; i < indices.length; i++) 
		{
			int index = indices[i];
			if (index >= 0 && index < count())
				out.add(model.getElementAt(index));
		}
		
		return removeFiles(out.toArray(new File[out.size()]));
	}
	
	/**
	 * Removes the provided files.
	 * @param files the files to remove.
	 * @return the amount of files removed.
	 */
	public int removeFiles(File ... files)
	{
		return model.removeFiles(files);
	}

	/**
	 * Clears the file list.
	 */
	public void clear()
	{
		model.clear();
	}
	
	/**
	 * Gets a specific file in the list.
	 * @param index the 
	 * @return the files, or empty list if no files.
	 */
	public File getFile(int index)
	{
		return model.getElementAt(index);
	}

	/**
	 * Gets all files in the list.
	 * @return the files, or empty list if no files.
	 */
	public File[] getFiles()
	{
		return model.fileSet.toArray(new File[model.fileSet.size()]);
	}

	/**
	 * Sets the files in the list in the order provided.
	 * The old list is erased.
	 * @param files the files to set.
	 */
	public void setFiles(File ... files)
	{
		clear();
		addFiles(files);
	}
	
	/**
	 * Gets the first or only currently selected file.
	 * @return the currently selected file, or null if no file.
	 */
	public File getSelectedFile()
	{
		int index = list.getSelectedIndex();
		return index < 0 || index >= model.getSize() ? null : model.getElementAt(index);
	}

	/**
	 * Gets the currently selected files, in list order.
	 * @return the currently selected files, or empty list if no files.
	 */
	public File[] getSelectedFiles()
	{
		int[] indices = list.getSelectedIndices();
		List<File> out = new LinkedList<>();
		for (int i = 0; i < indices.length; i++)
		{
			int index = indices[i];
			if (index >= 0 && index < model.getSize())
				out.add(model.getElementAt(indices[i]));
		}
		return out.toArray(new File[out.size()]);
	}
	
	/**
	 * Gets an immutable set of files that are in the list.
	 * NOTE: The set may not reflect list order!
	 * @return the set of files, or empty set if no files.
	 */
	public Set<File> getFileSet()
	{
		Set<File> set = new TreeSet<>(FileUtils.getFileComparator());
		set.addAll(getFileSet());
		return Collections.unmodifiableSet(set);
	}
	
	/**
	 * Gets an immutable set of files that are currently selected in the list.
	 * NOTE: The set may not reflect list order!
	 * @return the set of files, or empty set if no files.
	 */
	public Set<File> getSelectedFileSet()
	{
		Set<File> set = new TreeSet<>(FileUtils.getFileComparator());
		set.addAll(getSelectedFileSet());
		return Collections.unmodifiableSet(set);
	}
	
	/**
	 * Gets the index of a specific file in the list.
	 * @param file the file to search for.
	 * @return the index, or -1 if not found.
	 */
	public int indexOf(File file)
	{
		return model.fileList.indexOf(file);
	}
	
	/**
	 * Moves the selected files up one position in the list.
	 */
	public void onMoveUp()
	{
		int firstIndex = list.getMinSelectionIndex();
		if (firstIndex < 1 || firstIndex >= model.getSize())
			return;

		File[] files = getSelectedFiles();
		removeFiles(files);
		
		// Use an anchor file because a lot of ordering may change.
		File anchor = getFile(firstIndex - 1);
		int addIndex = indexOf(anchor);
		
		addFilesAt(addIndex, files);
		list.getSelectionModel().setSelectionInterval(addIndex, addIndex + (files.length - 1));
	}
	
	/**
	 * Moves the selected files up one position in the list.
	 */
	public void onMoveDown()
	{
		int firstIndex = list.getMinSelectionIndex();
		int lastIndex = list.getMaxSelectionIndex();
		if (lastIndex < 0 || lastIndex >= model.getSize() - 1)
			return;

		File[] files = getSelectedFiles();
		removeFiles(files);
		
		int addIndex = firstIndex + 1;
		
		addFilesAt(addIndex, files);
		list.getSelectionModel().setSelectionInterval(addIndex, addIndex + (files.length - 1));
	}

	private void onAddFiles()
	{
		File initPath = initialFileSupplier != null ? initialFileSupplier.get() : null;
		
		File[] files = fileFilter != null ? FileChooserFactory.chooseFilesOrDirectories(
			this,
			language.getText("filelist.add.modal.title"),
			initPath,
			language.getText("filelist.add.modal.choice"),
			fileFilter
		) : FileChooserFactory.chooseFilesOrDirectories(
			this, 
			language.getText("filelist.add.modal.title"),
			initPath,
			language.getText("filelist.add.modal.choice")
		);
		
		if (files != null)
			addFiles(files);
		
		if (addedFilesConsumer != null)
			addedFilesConsumer.accept(files);
	}
	
	private void onRemoveSelected()
	{
		removeFiles(getSelectedFiles());
	}
	
	/**
	 * @return the amount of files in the list.
	 */
	public int count() 
	{
		return model.getSize();
	}
	
	private class FileDrop extends TransferHandler
	{
		private static final long serialVersionUID = -8079777274729667158L;

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
				return false;
			}
			
			addFiles(files.toArray(new File[files.size()]));
			
			return true;
		}
		
	}

	private static class FileListModel implements ListModel<File>
	{
		private final Object MUTEX;
		private final Set<File> fileSet;
		private final List<File> fileList;
		private final List<ListDataListener> listeners;
		
		private Comparator<File> comparator;

		
		private FileListModel() 
		{
			this.MUTEX = new Object();
			this.fileSet = new TreeSet<>();
			this.fileList = new ArrayList<>();
			this.listeners = Collections.synchronizedList(new ArrayList<>(4));
			this.comparator = null;
		}
		
		/**
		 * Adds a file to a specific index in the model.
		 * If the file is in the model, this does nothing.
		 * @param index the target index.
		 * @param file the file to add.
		 */
		public void addFileAt(int index, File file)
		{
			file = FileUtils.canonizeFile(file);
			
			if (fileSet.contains(file))
				return;
			
			synchronized (MUTEX) 
			{
				// Early out.
				if (fileSet.contains(file))
					return;
				
				fileSet.add(file);
				fileList.add(index, file);
			}
			
			if (comparator != null)
				fileList.sort(comparator);
			
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index)
			));
		}

		/**
		 * Adds a set of files to a specific index in the model.
		 * If a file is in the model, it is not added.
		 * @param index the target index.
		 * @param files the files to add.
		 */
		public void addFilesAt(int index, File ... files)
		{
			// Add backwards.
			synchronized (MUTEX) 
			{
				for (int i = files.length - 1; i >= 0; i--)
				{
					File file = FileUtils.canonizeFile(files[i]);
					
					if (fileSet.contains(file))
						return;
		
					fileSet.add(file);
					fileList.add(index, file);
				}
			}

			if (comparator != null)
				fileList.sort(comparator);
			
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0)
			));
		}

		
		
		/**
		 * Removes the provided files.
		 * @param files the files to remove.
		 * @return the amount of files removed.
		 */
		public int removeFiles(File ... files)
		{
			int deleted = 0;

			synchronized (MUTEX) 
			{
				for (int i = 0; i < files.length; i++) 
				{
					File file = files[i];
					if (fileSet.remove(file))
					{
						fileList.remove(file);
						deleted++;
					}
				}
			}
			
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0)
			));
			return deleted;
		}

		/**
		 * Clears the list.
		 */
		public void clear()
		{
			synchronized (MUTEX)
			{
				fileSet.clear();
				fileList.clear();
			}
			
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0)
			));
		}
		
		@Override
		public File getElementAt(int index) 
		{
			return fileList.get(index);
		}
		
		@Override
		public void addListDataListener(ListDataListener l) 
		{
			listeners.add(l);
		}
		
		@Override
		public void removeListDataListener(ListDataListener l) 
		{
			listeners.remove(l);
		}

		@Override
		public int getSize() 
		{
			return fileSet.size();
		}
	}
	
}
