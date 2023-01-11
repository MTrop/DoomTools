/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
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
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import net.mtrop.doom.tools.gui.RepositoryHelper.Mercurial;
import net.mtrop.doom.tools.struct.Loader.LoaderFuture;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;


/**
 * Git repository panel.
 * @author Matthew Tropiano
 */
public class MercurialRepositoryPanel extends JPanel 
{
	private static final long serialVersionUID = 787169143911151094L;

	private static final Pattern BRANCH_REGEX = Pattern.compile("[!\"#$%&'()+,\\-0-9;<=>@A-Z\\]_`a-z{|}]+");
	
	private final DoomToolsGUIUtils utils;
	private final DoomToolsLanguageManager language;
	private final DoomToolsTaskManager tasks;
	
	private final Mercurial client;
	
	private final JList<StatusEntry> unstagedChanges;
	private final EntryModel unstagedChangesModel;
	private final JList<StatusEntry> stagedChanges;
	private final EntryModel stagedChangesModel;
	
	private final JLabel branchPanel;
	private final JLabel remoteBranchPanel;
	private final JLabel aheadBehindPanel;
	
	private JPopupMenu branchMenu;
	
	private final Action stageAction;
	private final Action stageAllAction;
	private final Action revertAction;
	private final Action unstageAction;
	private final Action unstageAllAction;
	private final Action commitAction;
	private final JButton checkoutButton;
	private final Action pullAction;
	private final Action pushAction;
	private final Action refreshAction;
	
	private final JTextArea commitArea;
	
	private DoomToolsStatusPanel statusPanel;

	/**
	 * Creates a Mercurial repository management panel. 
	 * @param directory the directory to use as the repository directory.
	 * @throws UnsupportedOperationException if Mercurial cannot be found on PATH.
	 * @throws IllegalArgumentException if the provided directory is not a Mercurial repository.
	 * @see Mercurial#isMercurial(File)
	 * @see Mercurial#checkMercurial()
	 */
	public MercurialRepositoryPanel(File directory)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.tasks = DoomToolsTaskManager.get();
		
		this.client = new Mercurial(directory);
		
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
		final LoaderFuture<ImageIcon> undoIcon = icons.getImageAsync("undo.png");
		final LoaderFuture<ImageIcon> removeAllIcon = icons.getImageAsync("remove-remove.png");
		final LoaderFuture<ImageIcon> refreshIcon = icons.getImageAsync("refresh.png");

		this.stageAction = actionItem(addIcon.result(), (e) -> onStage());
		this.stageAllAction = actionItem(addAllIcon.result(), (e) -> onStageAll());
		this.revertAction = actionItem(undoIcon.result(), (e) -> onRevert());
		this.unstageAction = actionItem(removeIcon.result(), (e) -> onUnstage());
		this.unstageAllAction = actionItem(removeAllIcon.result(), (e) -> onUnstageAll());
		this.commitAction = utils.createActionFromLanguageKey("hg.repo.commit", (e) -> onCommit());
		this.pullAction = utils.createActionFromLanguageKey("hg.repo.pull", (e) -> onPull());
		this.pushAction = utils.createActionFromLanguageKey("hg.repo.push", (e) -> onPush());
		this.refreshAction = actionItem(refreshIcon.result(), (e) -> {
			refreshInfoSynchronous();
			refreshEntries();
			refreshBranches();
		});
		
		this.commitArea = textArea();
		this.statusPanel = new DoomToolsStatusPanel();
		
		this.statusPanel.setSuccessMessage(language.getText("hg.repo.status.ready"));
		
		JComponent unstagedListPanel = containerOf(borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(),
				node(BorderLayout.CENTER, label(language.getText("hg.repo.unstaged.label"))),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.LEADING, 0, 0),
					node(button(stageAction)),
					node(button(stageAllAction))
				))
			)),
			node(BorderLayout.CENTER, scroll(unstagedChanges))
		);
		
		JComponent stagedListPanel = containerOf(borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(),
				node(BorderLayout.CENTER, label(language.getText("hg.repo.staged.label"))),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.LEADING, 0, 0),
					node(button(revertAction)),
					node(button(unstageAction)),
					node(button(unstageAllAction))
				))
			)),
			node(BorderLayout.CENTER, scroll(stagedChanges))
		);
		
		JComponent commitPanel = containerOf(borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(borderLayout(),
				node(BorderLayout.CENTER, label(language.getText("hg.repo.commit.label")))
			)),
			node(BorderLayout.CENTER, scroll(commitArea)),
			node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
				node(BorderLayout.NORTH, containerOf(borderLayout(),
					node(BorderLayout.LINE_START, containerOf(gridLayout(2, 1, 0, 4),
						node(button(commitAction)),
						node(checkoutButton = utils.createButtonFromLanguageKey("hg.repo.checkout", (b) -> {
							branchMenu.show(b, b.getX(), b.getY());
						}))
					)),
					node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.LEADING, 4, 0),
						node(button(pullAction)),
						node(button(pushAction)),
						node(button(refreshAction))
					))
				)),
				node(BorderLayout.SOUTH, containerOf(borderLayout(4, 0),
					node(BorderLayout.LINE_START, branchPanel),
					node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.LEADING, 8, 0),
						node(remoteBranchPanel),
						node(aheadBehindPanel)
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
		
		refreshInfoSynchronous();
		refreshEntries();
		refreshBranches();
	}
	
	public void refreshInfo()
	{
		tasks.spawn(() -> 
		{
			refreshInfoSynchronous();
		});
	}

	public void refreshInfoSynchronous()
	{
		BranchStatus bs = client.fetchBranchStatus();
		branchPanel.setText(bs.getName());
		String remote = bs.getRemoteName();
		remoteBranchPanel.setText(remote != null ? remote : "");
		aheadBehindPanel.setText("+" + bs.getAhead() + ", " + "-" + bs.getBehind());
	}

	public void refreshEntries()
	{
		tasks.spawn(() -> 
		{
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

	public void refreshBranches()
	{
		tasks.spawn(() ->
		{
			List<MenuNode> menuNodes = new LinkedList<>();
			for (String branch : client.fetchBranches())
				menuNodes.add(checkBoxItem(branch, branchPanel.getText().equals(branch), (c) -> onCheckout(branch)));
			menuNodes.add(separator());
			menuNodes.add(utils.createItemFromLanguageKey("hg.repo.branch.create", (i) -> onBranchCreate()));
			branchMenu = popupMenu(ArrayUtils.items(menuNodes, MenuNode.class));
		});
	}
	
	private void onBranchCreate()
	{
		JFormField<String> branchNameField = stringField(true);
		
		Boolean ok = modal(
			language.getText("hg.repo.branch.create.title"), 
			containerOf(dimension(100, 20), borderLayout(),
				node(BorderLayout.CENTER, branchNameField)
			), 
			utils.createChoiceFromLanguageKey("doomtools.ok", (Boolean)true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok == null || ok == false || ObjectUtils.isEmpty(branchNameField.getValue()))
			return;
		
		final String branch = branchNameField.getValue();
		
		if (!BRANCH_REGEX.matcher(branch).matches())
		{
			SwingUtils.error(language.getText("hg.repo.branch.create.badname"));
			return;
		}
		
		setActionsEnabled(false);
		tasks.spawn(() ->
		{
			if (client.branch(branch) == 0)
				client.checkout(branch);
			refreshInfoSynchronous();
			refreshBranches();
			setActionsEnabled(true);
		});
	}

	private void setActionsEnabled(boolean enabled)
	{
		commitAction.setEnabled(enabled);
		checkoutButton.setEnabled(enabled);
		pullAction.setEnabled(enabled);
		pushAction.setEnabled(enabled);
		refreshAction.setEnabled(enabled);
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
			SwingUtils.error(language.getText("hg.repo.staged.error", result));
			statusPanel.setErrorMessage(language.getText("hg.repo.staged.error", result));
			return;
		}
		
		statusPanel.setSuccessMessage(language.getText("hg.repo.status.staged", paths.length));
		
		refreshEntries();
	}

	private void onStageAll()
	{
		int result;
		if ((result = client.stageAll()) != 0)
		{
			SwingUtils.error(language.getText("hg.repo.staged.error", result));
			statusPanel.setErrorMessage(language.getText("hg.repo.staged.error", result));
			return;
		}
		
		statusPanel.setSuccessMessage(language.getText("hg.repo.status.staged.all"));
		refreshEntries();
	}

	private void onRevert()
	{
		StatusEntry[] entries = getSelectedEntries(unstagedChanges);
		if (entries.length == 0)
			return;

		if (SwingUtils.noTo(language.getText("hg.repo.revert.message", entries.length)))
			return;

		String[] paths = new String[entries.length];
		for (int i = 0; i < entries.length; i++) 
		{
			StatusEntry entry = entries[i];
			paths[i] = entry.getPath();
		}
		
		int result;
		if ((result = client.revert(paths)) != 0)
		{
			SwingUtils.error(language.getText("hg.repo.revert.error", result));
			statusPanel.setErrorMessage(language.getText("hg.repo.revert.error", result));
			return;
		}
		
		statusPanel.setSuccessMessage(language.getText("hg.repo.status.reverted", paths.length));
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
			SwingUtils.error(language.getText("hg.repo.unstaged.error", result));
			statusPanel.setErrorMessage(language.getText("hg.repo.unstaged.error", result));
			return;
		}
		
		statusPanel.setSuccessMessage(language.getText("hg.repo.status.unstaged", paths.length));
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
			SwingUtils.error(language.getText("hg.repo.unstaged.error", result));
			statusPanel.setErrorMessage(language.getText("hg.repo.unstaged.error", result));
			return;
		}
		
		statusPanel.setSuccessMessage(language.getText("hg.repo.status.unstaged", paths.length));
		refreshEntries();
	}

	private void onCommit() 
	{
		String content = commitArea.getText();
		if (ObjectUtils.isEmpty(content))
		{
			SwingUtils.error(language.getText("hg.repo.commit.blank"));
			return;
		}		
		
		setActionsEnabled(false);
		String[] paragraphs = content.split("\\n\\n+");
		
		int result;
		if ((result = client.commit(paragraphs)) != 0)
		{
			SwingUtils.error(language.getText("hg.repo.commit.error", result));
			statusPanel.setErrorMessage(language.getText("hg.repo.commit.error", result));
			setActionsEnabled(true);
			return;
		}

		commitArea.setText("");
		
		statusPanel.setSuccessMessage(language.getText("hg.repo.status.commit"));
		refreshInfo();
		refreshEntries();
		setActionsEnabled(true);
	}

	private void onPush()
	{
		if (ObjectUtils.isEmpty(remoteBranchPanel.getText()))
		{
			if (SwingUtils.yesTo(language.getText("hg.repo.push.noremote.ask")))
			{
				onPushBranch(branchPanel.getText());
				return;
			}
			else
			{
				SwingUtils.error(language.getText("hg.repo.push.noremote"));
				return;
			}
		}

		setActionsEnabled(false);
		statusPanel.setActivityMessage(language.getText("hg.repo.status.pushing"));

		tasks.spawn(() -> 
		{
			int result;
			if ((result = client.push()) != 0)
			{
				SwingUtils.error(language.getText("hg.repo.push.error", result));
				statusPanel.setErrorMessage(language.getText("hg.repo.push.error", result));
				setActionsEnabled(true);
				return;
			}

			statusPanel.setSuccessMessage(language.getText("hg.repo.status.push"));
			refreshInfo();
			refreshEntries();
			setActionsEnabled(true);
		});
	}

	private void onPushBranch(final String branchName)
	{
		JFormField<String> remoteNameField = stringField("default", true);
		
		Boolean ok = modal(
			language.getText("hg.repo.branch.remote.title"), 
			containerOf(dimension(100, 20), borderLayout(),
				node(BorderLayout.CENTER, remoteNameField)
			), 
			utils.createChoiceFromLanguageKey("doomtools.ok", (Boolean)true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok == null || ok == false || ObjectUtils.isEmpty(remoteNameField.getValue()))
			return;
		
		final String remote = remoteNameField.getValue();
		
		if (!BRANCH_REGEX.matcher(remote).matches())
		{
			SwingUtils.error(language.getText("hg.repo.branch.remote.badname"));
			return;
		}
		
		setActionsEnabled(false);
		statusPanel.setActivityMessage(language.getText("hg.repo.status.pushing"));

		tasks.spawn(() -> 
		{
			int result;
			if ((result = client.pushNewBranch(branchName)) != 0)
			{
				SwingUtils.error(language.getText("hg.repo.push.error", result));
				statusPanel.setErrorMessage(language.getText("hg.repo.push.error", result));
				setActionsEnabled(true);
				return;
			}

			statusPanel.setSuccessMessage(language.getText("hg.repo.status.push"));
			refreshInfo();
			refreshEntries();
			setActionsEnabled(true);
		});
	}
	
	private void onPull()
	{
		if (ObjectUtils.isEmpty(remoteBranchPanel.getText()))
		{
			SwingUtils.error(language.getText("hg.repo.pull.noremote"));
			return;
		}

		setActionsEnabled(false);
		statusPanel.setActivityMessage(language.getText("hg.repo.status.pulling"));

		tasks.spawn(() -> 
		{
			int result;
			if ((result = client.pull()) != 0)
			{
				SwingUtils.error(language.getText("hg.repo.pull.error", result));
				statusPanel.setErrorMessage(language.getText("hg.repo.pull.error", result));
				setActionsEnabled(true);
				return;
			}

			statusPanel.setSuccessMessage(language.getText("hg.repo.status.pull"));
			refreshInfo();
			refreshEntries();
			setActionsEnabled(true);
		});
	}
	
	private void onCheckout(final String branchName)
	{
		setActionsEnabled(false);
		statusPanel.setActivityMessage(language.getText("hg.repo.status.checkingout", branchName));

		tasks.spawn(() -> 
		{
			int result;
			if ((result = client.checkout(branchName)) != 0)
			{
				SwingUtils.error(language.getText("hg.repo.checkout.error", result));
				statusPanel.setErrorMessage(language.getText("hg.repo.checkout.error", result));
				setActionsEnabled(true);
				return;
			}

			statusPanel.setSuccessMessage(language.getText("hg.repo.status.checkout", branchName));
			refreshInfoSynchronous();
			refreshEntries();
			refreshBranches();
			setActionsEnabled(true);
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
		private static final long serialVersionUID = 7078238620806090004L;

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
