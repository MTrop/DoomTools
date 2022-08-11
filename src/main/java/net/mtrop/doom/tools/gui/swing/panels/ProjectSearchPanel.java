package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
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

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomToolsTaskManager;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * A panel that searches all available files in a project.
 * @author Matthew Tropiano
 */
public class ProjectSearchPanel extends JPanel
{
	private static final long serialVersionUID = 4260115080161777455L;

    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(ProjectSearchPanel.class); 

	private DoomToolsTaskManager tasks;
	private DoomToolsLanguageManager language;
	private DoomToolsGUIUtils utils;
	
	private Set<File> registeredFiles;
	
	private JFormField<String> findField;
	private JFormField<Boolean> caseSensitiveField;
	
	private ResultModel searchResultListModel;
	private JList<SearchResult> searchResultList;
	private DoomToolsStatusPanel statusPanel;
	
	/**
	 * Creates a new search panel.
	 * @param projectDirectory
	 * @param onSearchSelect the function to call when a search result is selected.
	 */
	public ProjectSearchPanel(File projectDirectory, final Consumer<SearchResult> onSearchSelect)
	{
		this.tasks = DoomToolsTaskManager.get();
		this.language = DoomToolsLanguageManager.get();
		this.utils = DoomToolsGUIUtils.get();
		
		this.registeredFiles = Collections.synchronizedSet(new TreeSet<>());
		
		this.statusPanel = new DoomToolsStatusPanel();
		
		tasks.spawn(() -> {
			statusPanel.setActivityMessage(language.getText("doommake.search.prep"));
			for (File file : FileUtils.explodeFiles(projectDirectory))
				registerFile(file);
			statusPanel.setSuccessMessage(language.getText("doommake.search.ready"));
		});

		this.searchResultListModel = new ResultModel();
		this.searchResultList = new JList<>(searchResultListModel);
		this.searchResultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.searchResultList.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				if (e.getClickCount() == 2)
				{
					onSearchSelect.accept(searchResultList.getSelectedValue());
				}
			}
		});
		
		this.findField = stringField(true);
		this.findField.getFormComponent().addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e) 
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					onFindAll();
				}
			}
		});
		
		this.caseSensitiveField = checkBoxField(checkBox(language.getText("doommake.search.field.case"), (v) -> {}));
		
		containerOf(this,
			node(BorderLayout.NORTH, utils.createForm(form(language.getInteger("doommake.search.field.width")),
				utils.formField("doommake.search.field.find", findField),
				utils.formField(panelField(containerOf(flowLayout(Flow.LEADING),
					node(caseSensitiveField)
				))),
				utils.formField(panelField(containerOf(flowLayout(Flow.TRAILING),
					node(button(language.getText("doommake.search.button.find"), (b) -> onFindAll()))
				)))
			)),
			node(BorderLayout.CENTER, scroll(searchResultList)),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}
	
	/**
	 * Registers/re-registers a file in the search.
	 * Should be called when a file changes.
	 * If the file is considered to be a binary file, it is not registered.
	 * @param file the file to register.
	 */
	public void registerFile(File file)
	{
		// detect a change.
		if (Common.isBinaryFile(file) || file.isHidden() || file.isDirectory())
			deregisterFile(file);
		else
			registeredFiles.add(FileUtils.canonizeFile(file));
	}
	
	/**
	 * De-registers a file from search.
	 * @param file the file to de-register.
	 */
	public void deregisterFile(File file)
	{
		registeredFiles.remove(FileUtils.canonizeFile(file));
	}
	
	/**
	 * Searches for a specific phrase.
	 * @param phrase the phrase to search for.
	 * @param caseSensitive if true, case sensitive search.
	 */
	public void search(String phrase, boolean caseSensitive)
	{
		if (ObjectUtils.isEmpty(phrase))
			return;

		final String finalPhrase;
		if (!caseSensitive)
			finalPhrase = phrase.toLowerCase();
		else
			finalPhrase = phrase;
		
		LOG.debug("Started search.");
		statusPanel.setActivityMessage(language.getText("doommake.search.searching"));
		tasks.spawn(() -> 
		{
			int count = 0;
			for (File file : registeredFiles)
			{
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
				{
					int offset = 0;
					int lines = 1;
					String line;
					while ((line = reader.readLine()) != null)
					{
						if (!caseSensitive)
							line = line.toLowerCase();
						
						int index;
						int lineSearch = 0;
						while ((index = line.indexOf(finalPhrase, lineSearch)) >= 0)
						{
							searchResultListModel.addResult(new SearchResult(file, lines, index, index + finalPhrase.length() - 1, offset + index));
							lineSearch = index + finalPhrase.length();
							count++;
						}
						offset += line.length() + 1;
						lines++;
					}
				} 
				catch (FileNotFoundException e) 
				{
					// Do nothing.
				} 
				catch (IOException e) 
				{
					// Do nothing.
				}
			}
			
			if (count == 0)
				statusPanel.setSuccessMessage(language.getText("doommake.search.results.none"));
			else
				statusPanel.setSuccessMessage(language.getText("doommake.search.results.some", count));
			LOG.debug("Finished search.");
		});
	}
	
	public void onFindAll()
	{
		searchResultListModel.clear();
		search(findField.getValue(), caseSensitiveField.getValue());
	}
	
	/**
	 * A single search result.
	 */
	public static class SearchResult
	{
		private File source;
		private long line;
		private long characterStart;
		private long characterEnd;
		private long offset;
		
		private SearchResult(File source, long line, long characterStart, long characterEnd, long offset) 
		{
			this.source = source;
			this.line = line;
			this.characterStart = characterStart;
			this.characterEnd = characterEnd;
			this.offset = offset;
		}
	
		public File getSource()
		{
			return source;
		}
	
		public long getLine()
		{
			return line;
		}
	
		public long getCharacterStart()
		{
			return characterStart;
		}
		
		public long getCharacterEnd() 
		{
			return characterEnd;
		}
	
		public long getOffset()
		{
			return offset;
		}
		
		@Override
		public String toString() 
		{
			return source.getName() + " (Line " + line + ") - " + source.getParent();
		}
		
	}

	private static class ResultModel implements ListModel<SearchResult>
	{
		private List<SearchResult> results;
		private final List<ListDataListener> listeners;

		private ResultModel() 
		{
			this.results = Collections.synchronizedList(new ArrayList<>());
			this.listeners = Collections.synchronizedList(new ArrayList<>(4));
		}

		/**
		 * Clears the model.
		 */
		public void clear()
		{
			results.clear();
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0)
			));
		}
		
		/**
		 * Adds a result to the model.
		 * @param result the result.
		 */
		public void addResult(SearchResult result)
		{
			results.add(result);
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0)
			));
		}

		@Override
		public int getSize() 
		{
			return results.size();
		}

		@Override
		public SearchResult getElementAt(int index) 
		{
			return results.get(index);
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
