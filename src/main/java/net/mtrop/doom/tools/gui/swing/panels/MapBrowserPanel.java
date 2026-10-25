package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.mtrop.doom.WadMap;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomToolsTaskManager;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.util.MapUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

/**
 * The map browser panel for DoomMake Studio.
 * @Author Matthew Tropiano
 */
public class MapBrowserPanel extends JPanel 
{
	private static final long serialVersionUID = 4299156680331278471L;

	/** Logger. */
	private static final Logger LOG = DoomToolsLogger.getLogger(MapBrowserPanel.class); 

	private final DoomToolsTaskManager tasks;
	private final DoomToolsLanguageManager language;

	private Set<File> fileRegistry;
	
	private MapListModel mapListModel;
	private JList<MapWadEntry> mapList;
	private DoomToolsStatusPanel statusPanel;
	
	/**
	 * Creates a new search panel for maps.
	 * @param onMapSelect Called when a map is selected from the list.
	 */
	public MapBrowserPanel(final Consumer<MapWadEntry> onMapSelect)
	{
		this.tasks = DoomToolsTaskManager.get();
		this.language = DoomToolsLanguageManager.get();
		
		this.fileRegistry = Collections.synchronizedSet(new TreeSet<>());
		
		this.statusPanel = new DoomToolsStatusPanel();

		this.mapListModel = new MapListModel();
		this.mapList = new JList<>(mapListModel);
		this.mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.mapList.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				if (e.getClickCount() == 2)
				{
					onMapSelect.accept(mapList.getSelectedValue());
				}
			}
		});
		
		containerOf(this, borderLayout(),
			node(BorderLayout.CENTER, scroll(mapList)),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}

	/**
	 * Builds the registry of files asynchronously.
	 * @param files the list of files to register.
	 */
	public void buildRegistry(final File ... files)
	{
		LOG.info("Start registry build.");
		deregisterAllFiles();
		tasks.spawn(() -> {
			statusPanel.setActivityMessage(language.getText("doommake.mapentry.prep"));
			for (File file : files)
				registerFile(file);
			statusPanel.setSuccessMessage(language.getText("doommake.mapentry.ready"));
			LOG.info("Finished registry build.");
		});
	}
	
	private static boolean isValidRegisterableFile(File file)
	{
		return file.exists() && !file.isDirectory() && file.getName().toLowerCase().endsWith(".wad");
	}
	
	/**
	 * Registers/re-registers a file.
	 * Should be called when a file changes.
	 * If the file is considered to be a binary file, it is not registered.
	 * @param file the file to register.
	 */
	public void registerFile(final File file)
	{
		if (!isValidRegisterableFile(file))
			return;
		
		deregisterFile(file);
		if (file.exists())
		{
			mapListModel.addEntries(getEntriesFromFile(file));
			fileRegistry.add(FileUtils.canonizeFile(file));
		}
	}
	
	public void deregisterFile(File file)
	{
		if (!fileRegistry.contains(FileUtils.canonizeFile(file)))
			return;
		deregisterWadEntries(file);
	}

	/**
	 * De-registers all files.
	 */
	public void deregisterAllFiles()
	{
		mapListModel.clear();
	}
	
	private List<MapWadEntry> getEntriesFromFile(final File file)
	{
		List<MapWadEntry> out = new LinkedList<>();
		WadMap wad;
		try {
			wad = new WadMap(file);
			for (String header : MapUtils.getAllMapHeaders(wad))
				out.add(new MapWadEntry(file, header));
		} catch (IOException e) {
			LOG.error(e, "An exception occurred!");
		}
		return out;
	}
	
	private void deregisterWadEntries(final File file)
	{
		mapListModel.removeEntriesByFile(file);
		fileRegistry.remove(FileUtils.canonizeFile(file));
	}
	
	public static class MapWadEntry implements Comparable<MapWadEntry>
	{
		private final File wadFile;
		private final String headerName;
		
		private static Comparator<MapWadEntry> COMPARATOR = (a, b) -> {
			int hc = a.headerName.compareTo(b.headerName);
			return hc != 0 ? hc : a.wadFile.compareTo(b.wadFile);
		};
		
		public MapWadEntry(File wadFile, String headerName)
		{
			this.wadFile = FileUtils.canonizeFile(wadFile);
			this.headerName = headerName;
		}
		
		public File getWadFile()
		{
			return wadFile;
		}

		@Override
		public boolean equals(Object obj) 
		{
			if (obj instanceof MapWadEntry)
				return equals((MapWadEntry)obj);
			return super.equals(obj);
		}
		
		public boolean equals(MapWadEntry entry)
		{
			return wadFile.equals(entry.wadFile) && headerName.equals(entry.headerName);
		}
		
		@Override
		public String toString() 
		{
			return headerName + ": " + wadFile.getName();
		}

		@Override
		public int compareTo(MapWadEntry entry)
		{
			return COMPARATOR.compare(this, entry);
		}
	}
	
	private static class MapListModel implements ListModel<MapWadEntry>
	{
		private List<MapWadEntry> entries;
		private final List<ListDataListener> listeners;

		private MapListModel()
		{
			this.entries = Collections.synchronizedList(new ArrayList<>());
			this.listeners = Collections.synchronizedList(new ArrayList<>(4));
		}
		
		/**
		 * Clears the model.
		 */
		public synchronized void clear()
		{
			int amount = entries.size(); 
			entries.clear();
			if (amount > 0)
			{
				listeners.forEach((listener) -> listener.intervalRemoved(
					new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, amount - 1)
				));
			}
		}
		
		/**
		 * Adds entries to the model.
		 * @param entryCollection the entries.
		 */
		public synchronized void addEntries(Collection<MapWadEntry> entryCollection)
		{
			entries.addAll(entryCollection);
			entries.sort(MapWadEntry.COMPARATOR);
			if (entries.size() > 0)
			{
				listeners.forEach((listener) -> listener.contentsChanged(
					new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, entries.size() - 1)
				));
			}
		}

		/**
		 * Removes all entries by a file.
		 * @param file the file path.
		 */
		public synchronized void removeEntriesByFile(final File file)
		{
			final File canon = FileUtils.canonizeFile(file);
			int amount = entries.size(); 
			
			for (Iterator<MapWadEntry> it = entries.iterator(); it.hasNext();)
			{
				MapWadEntry entry = it.next();
				if (entry.wadFile.equals(canon))
					it.remove();
			}
			
			if (amount != entries.size() && amount > 0)
			{
				listeners.forEach((listener) -> listener.intervalRemoved(
					new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, amount - 1, amount - 1)
				));
				listeners.forEach((listener) -> listener.contentsChanged(
					new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, entries.size() - 1)
				));
			}
		}

		@Override
		public int getSize() 
		{
			return entries.size();
		}

		@Override
		public MapWadEntry getElementAt(int index) 
		{
			return entries.get(index);
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

	}
	
}
