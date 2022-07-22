/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.mtrop.doom.tools.gui.RepositoryHelper.StatusEntry;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsTaskManager;
import net.mtrop.doom.tools.gui.RepositoryHelper.BranchStatus;
import net.mtrop.doom.tools.gui.RepositoryHelper.Git;
import net.mtrop.doom.tools.struct.Loader.LoaderFuture;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

/**
 * Git repository panel.
 * @author Matthew Tropiano
 */
public class GitRepositoryPanel extends JPanel 
{
	private static final long serialVersionUID = 6824090744834896705L;
	
	private final DoomToolsGUIUtils utils;
	private final DoomToolsLanguageManager language;
	private final DoomToolsTaskManager tasks;
	
	private final Git client;
	
	private final JList<StatusEntry> unstagedChanges;
	private final EntryModel unstagedChangesModel;
	private final JList<StatusEntry> stagedChanges;
	private final EntryModel stagedChangesModel;
	
	private final JLabel branchPanel;
	private final JLabel remoteBranchPanel;
	private final JLabel aheadBehindPanel;
	
	private final Action stageAction;
	private final Action stageAllAction;
	private final Action unstageAction;
	private final Action unstageAllAction;
	private final Action commitAction;
	private final Action pullAction;
	private final Action pushAction;
	private final Action refreshAction;
	
	private final JTextArea commitArea;
	
	private DoomToolsStatusPanel statusPanel;

	/**
	 * Creates a Git repository management panel. 
	 * @param directory the directory to use as the repository directory.
	 * @throws UnsupportedOperationException if Git cannot be found on PATH.
	 * @throws IllegalArgumentException if the provided directory is not a Git repository.
	 * @see Git#isGit(File)
	 * @see Git#checkGit()
	 */
	public GitRepositoryPanel(File directory)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.tasks = DoomToolsTaskManager.get();
		
		this.client = new Git(directory);
		
		this.unstagedChangesModel = new EntryModel();
		this.unstagedChanges = new JList<>(unstagedChangesModel);
		this.unstagedChanges.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.unstagedChanges.setCellRenderer(new Renderer());
		
		this.unstagedChangesModel.addListDataListener(new ListDataListener() 
		{
			@Override
			public void intervalRemoved(ListDataEvent e) 
			{
				unstagedChanges.repaint();
			}
			
			@Override
			public void intervalAdded(ListDataEvent e) 
			{
				unstagedChanges.repaint();
			}
			
			@Override
			public void contentsChanged(ListDataEvent e) 
			{
				unstagedChanges.repaint();
			}
		});
		
		this.stagedChangesModel = new EntryModel();
		this.stagedChanges = new JList<>(stagedChangesModel);
		this.stagedChanges.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.stagedChanges.setCellRenderer(new Renderer());
		
		this.stagedChangesModel.addListDataListener(new ListDataListener() 
		{
			@Override
			public void intervalRemoved(ListDataEvent e) 
			{
				stagedChanges.repaint();
			}
			
			@Override
			public void intervalAdded(ListDataEvent e) 
			{
				stagedChanges.repaint();
			}
			
			@Override
			public void contentsChanged(ListDataEvent e) 
			{
				stagedChanges.repaint();
			}
		});
		
		this.branchPanel = label();
		this.remoteBranchPanel = label();
		this.aheadBehindPanel = label();
		
		DoomToolsIconManager icons = DoomToolsIconManager.get();

		final LoaderFuture<ImageIcon> addIcon = icons.getImageAsync("add.png");
		final LoaderFuture<ImageIcon> removeIcon = icons.getImageAsync("remove.png");
		final LoaderFuture<ImageIcon> addAllIcon = icons.getImageAsync("add-add.png");
		final LoaderFuture<ImageIcon> removeAllIcon = icons.getImageAsync("remove-remove.png");
		final LoaderFuture<ImageIcon> refreshIcon = icons.getImageAsync("refresh.png");

		this.stageAction = actionItem(addIcon.result(), (e) -> onStage());
		this.stageAllAction = actionItem(addAllIcon.result(), (e) -> onStageAll());
		this.unstageAction = actionItem(removeIcon.result(), (e) -> onUnstage());
		this.unstageAllAction = actionItem(removeAllIcon.result(), (e) -> onUnstageAll());
		this.commitAction = utils.createActionFromLanguageKey("git.repo.commit", (e) -> onCommit());
		this.pullAction = utils.createActionFromLanguageKey("git.repo.pull", (e) -> onPull());
		this.pushAction = utils.createActionFromLanguageKey("git.repo.push", (e) -> onPush());
		this.refreshAction = actionItem(refreshIcon.result(), (e) -> {
			refreshInfo();
			refreshEntries();
		});
		
		this.commitArea = textArea();
		this.statusPanel = new DoomToolsStatusPanel();
		
		this.statusPanel.setSuccessMessage(language.getText("git.repo.status.ready"));
		
		JComponent unstagedListPanel = containerOf(borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(),
				node(BorderLayout.CENTER, label(language.getText("git.repo.unstaged.label"))),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.LEADING, 0, 0),
					node(button(stageAction)),
					node(button(stageAllAction))
				))
			)),
			node(BorderLayout.CENTER, scroll(unstagedChanges))
		);
		
		JComponent stagedListPanel = containerOf(borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(),
				node(BorderLayout.CENTER, label(language.getText("git.repo.staged.label"))),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.LEADING, 0, 0),
					node(button(unstageAction)),
					node(button(unstageAllAction))
				))
			)),
			node(BorderLayout.CENTER, scroll(stagedChanges))
		);
		
		JComponent commitPanel = containerOf(borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(),
				node(BorderLayout.CENTER, label(language.getText("git.repo.commit.label")))
			)),
			node(BorderLayout.CENTER, scroll(commitArea)),
			node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(borderLayout(),
					node(BorderLayout.LINE_START, containerOf(flowLayout(Flow.LEADING, 4, 0),
						node(button(commitAction))
					)),
					node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.LEADING, 4, 0),
						node(button(pullAction)),
						node(button(pushAction)),
						node(button(refreshAction))
					))
				)),
				node(BorderLayout.SOUTH, containerOf(gridLayout(1, 2, 4, 0),
					node(BorderLayout.LINE_START, branchPanel),
					node(BorderLayout.LINE_END, containerOf(borderLayout(),
						node(BorderLayout.LINE_START, remoteBranchPanel),
						node(BorderLayout.LINE_END, aheadBehindPanel)
					))
				))
			))
		);
			
		containerOf(this,
			node(BorderLayout.CENTER, split(SplitOrientation.VERTICAL,
				containerOf(node(split(SplitOrientation.VERTICAL,
					unstagedListPanel,
					stagedListPanel
				))),
				containerOf(borderLayout(0, 4),
					node(BorderLayout.CENTER, commitPanel),
					node(BorderLayout.SOUTH, statusPanel)
				)
			))
		);
		
		refreshInfo();
		refreshEntries();
	}
	
	public void refreshInfo()
	{
		tasks.spawn(() -> {
			BranchStatus bs = client.fetchBranchStatus();
			branchPanel.setText(bs.getName());
			String remote = bs.getRemoteName();
			remoteBranchPanel.setText(remote != null ? remote : "");
			aheadBehindPanel.setText("+" + bs.getAhead() + ", " + "-" + bs.getBehind());
		});
	}

	public void refreshEntries()
	{
		tasks.spawn(() -> {
			List<StatusEntry> staged = new LinkedList<>(); 
			List<StatusEntry> unstaged = new LinkedList<>();
			for (StatusEntry status : client.fetchStatus())
			{
				if (status.isStaged())
					staged.add(status);
				else
					unstaged.add(status);
			}
			stagedChangesModel.setEntries(staged.toArray(new StatusEntry[staged.size()]));
			unstagedChangesModel.setEntries(unstaged.toArray(new StatusEntry[unstaged.size()]));
			
			stageAction.setEnabled(!unstaged.isEmpty());
			stageAllAction.setEnabled(!unstaged.isEmpty());
			unstageAction.setEnabled(!staged.isEmpty());
			unstageAllAction.setEnabled(!staged.isEmpty());
		});
	}

	private void onStage() 
	{
		StatusEntry[] entries = getSelectedEntries(unstagedChanges);
		if (entries.length == 0)
			return;
		String[] paths = new String[entries.length];
		for (int i = 0; i < entries.length; i++) 
		{
			StatusEntry entry = entries[i];
			paths[i] = entry.getPath();
		}
		
		int result;
		if ((result = client.stage(paths)) != 0)
		{
			SwingUtils.error(language.getText("git.repo.staged.error", result));
			statusPanel.setErrorMessage(language.getText("git.repo.staged.error", result));
			return;
		}
		
		statusPanel.setSuccessMessage(language.getText("git.repo.status.staged", paths.length));
		
		refreshEntries();
	}

	private void onStageAll()
	{
		int result;
		if ((result = client.stageAll()) != 0)
		{
			SwingUtils.error(language.getText("git.repo.staged.error", result));
			statusPanel.setErrorMessage(language.getText("git.repo.staged.error", result));
			return;
		}
		
		statusPanel.setSuccessMessage(language.getText("git.repo.status.staged.all"));
		refreshEntries();
	}

	private void onUnstage()
	{
		StatusEntry[] entries = getSelectedEntries(stagedChanges);
		if (entries.length == 0)
			return;
		String[] paths = new String[entries.length];
		for (int i = 0; i < entries.length; i++) 
		{
			StatusEntry entry = entries[i];
			paths[i] = entry.getPath();
		}
		
		int result;
		if ((result = client.unstage(paths)) != 0)
		{
			SwingUtils.error(language.getText("git.repo.unstaged.error", result));
			statusPanel.setErrorMessage(language.getText("git.repo.unstaged.error", result));
			return;
		}
		
		statusPanel.setSuccessMessage(language.getText("git.repo.status.unstaged", paths.length));
		refreshEntries();
	}

	private void onUnstageAll() 
	{
		StatusEntry[] entries = getAllEntries(stagedChanges);
		if (entries.length == 0)
			return;
		String[] paths = new String[entries.length];
		for (int i = 0; i < entries.length; i++) 
		{
			StatusEntry entry = entries[i];
			paths[i] = entry.getPath();
		}

		int result;
		if ((result = client.unstage(paths)) != 0)
		{
			SwingUtils.error(language.getText("git.repo.unstaged.error", result));
			statusPanel.setErrorMessage(language.getText("git.repo.unstaged.error", result));
			return;
		}
		
		statusPanel.setSuccessMessage(language.getText("git.repo.status.unstaged", paths.length));
		refreshEntries();
	}

	private void onCommit() 
	{
		String content = commitArea.getText();
		if (ObjectUtils.isEmpty(content))
		{
			SwingUtils.error(language.getText("git.repo.commit.blank"));
			return;
		}		
		
		String[] paragraphs = content.split("\\n\\n+");
		
		int result;
		if ((result = client.commit(paragraphs)) != 0)
		{
			SwingUtils.error(language.getText("git.repo.commit.error", result));
			statusPanel.setErrorMessage(language.getText("git.repo.commit.error", result));
			return;
		}

		commitArea.setText("");
		
		statusPanel.setSuccessMessage(language.getText("git.repo.status.commit"));
		refreshInfo();
		refreshEntries();
	}

	private void onPush()
	{
		if (ObjectUtils.isEmpty(remoteBranchPanel))
		{
			SwingUtils.error(language.getText("git.repo.push.noremote"));
			return;
		}

		statusPanel.setActivityMessage(language.getText("git.repo.status.pushing"));

		tasks.spawn(() -> {
			int result;
			if ((result = client.push()) != 0)
			{
				SwingUtils.error(language.getText("git.repo.push.error", result));
				statusPanel.setErrorMessage(language.getText("git.repo.push.error", result));
				return;
			}

			statusPanel.setSuccessMessage(language.getText("git.repo.status.push"));
			refreshInfo();
			refreshEntries();
		});
	}
	
	private void onPull()
	{
		if (ObjectUtils.isEmpty(remoteBranchPanel))
		{
			SwingUtils.error(language.getText("git.repo.pull.noremote"));
			return;
		}

		statusPanel.setActivityMessage(language.getText("git.repo.status.pulling"));

		tasks.spawn(() -> {
			int result;
			if ((result = client.pull()) != 0)
			{
				SwingUtils.error(language.getText("git.repo.pull.error", result));
				statusPanel.setErrorMessage(language.getText("git.repo.pull.error", result));
				return;
			}

			statusPanel.setSuccessMessage(language.getText("git.repo.status.pull"));
			refreshInfo();
			refreshEntries();
		});
	}
	
	/**
	 * Gets the currently selected entries, in list order.
	 * @param list the list to pull entries from.
	 * @return the currently selected files, or empty list if no files.
	 */
	private static StatusEntry[] getSelectedEntries(JList<StatusEntry> list)
	{
		int[] indices = list.getSelectedIndices();
		List<StatusEntry> out = new LinkedList<>();
		for (int i = 0; i < indices.length; i++)
		{
			int index = indices[i];
			if (index >= 0 && index < list.getModel().getSize())
				out.add((StatusEntry)(list.getModel().getElementAt(indices[i])));
		}
		return out.toArray(new StatusEntry[out.size()]);
	}
	
	/**
	 * Gets the currently selected entries, in list order.
	 * @param list the list to pull entries from.
	 * @return the currently selected files, or empty list if no files.
	 */
	private static StatusEntry[] getAllEntries(JList<StatusEntry> list)
	{
		List<StatusEntry> out = new LinkedList<>();
		for (int i = 0; i < list.getModel().getSize(); i++)
			out.add((StatusEntry)(list.getModel().getElementAt(i)));
		return out.toArray(new StatusEntry[out.size()]);
	}
	
	private static class EntryModel implements ListModel<StatusEntry>
	{
		private StatusEntry[] entries;
		private final List<ListDataListener> listeners;

		public EntryModel()
		{
			this.entries = new StatusEntry[]{};
			this.listeners = Collections.synchronizedList(new ArrayList<>(4));
		}
		
		/**
		 * Sets the list's entries.
		 * @param entries the new entries.
		 */
		public void setEntries(StatusEntry[] entries)
		{
			this.entries = ArrayUtils.copyAnd(entries, (arr) -> {
				Arrays.sort(arr, (a, b) -> a.toDisplayString().compareTo(b.toDisplayString()));
			});
			
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0)
			));
		}
		
		@Override
		public StatusEntry getElementAt(int index)
		{
			return entries[index];
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
			return entries.length;
		}
	}
	
	private static class Renderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 8103636411274110814L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
		{
			JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof StatusEntry)
				label.setText(((StatusEntry)value).toDisplayString());
			return label;
		}
	}
	
}
