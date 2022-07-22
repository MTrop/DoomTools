/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import net.mtrop.doom.tools.struct.swing.ComponentFactory;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.ListSelectionMode;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

/**
 * A DoomMake panel for a single directory.
 * @author Matthew Tropiano
 */
public class DoomMakeProjectTargetListPanel extends JPanel
{
	private static final long serialVersionUID = 3576857310145843126L;

	// =======================================================================

	/** Target list model. */
	private DefaultListModel<String> targetListModel;
	/** Target list. */
	private JList<String> targetList;
	
	// =======================================================================
	
	/**
	 * Creates a new project directory.
	 * @param targetNames initial set of target names.
	 * @param selectListener the function to call on target selection. 
	 * @param doubleClickListener the function to call on target double-click.
	 */
	public DoomMakeProjectTargetListPanel(Collection<String> targetNames, final Consumer<String> selectListener, final Consumer<String> doubleClickListener)
	{
		Objects.requireNonNull(selectListener, "must provide a selectListener");
		Objects.requireNonNull(doubleClickListener, "must provide a doubleClickListener");
		
		this.targetListModel = new DefaultListModel<>();

		this.targetList = ComponentFactory.list(this.targetListModel, ListSelectionMode.SINGLE, (selected, adjusting) -> 
		{
			if (!adjusting)
			{
				String value = selected.isEmpty() ? null : selected.get(0);
				selectListener.accept(value);
			}
		});
		
		this.targetList.addMouseListener(new MouseInputAdapter() 
		{
			@Override
			@SuppressWarnings("unchecked")
			public void mouseClicked(MouseEvent e) 
			{
				JList<String> self = (JList<String>)e.getComponent();
				// is double-click.
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) 
				{
					doubleClickListener.accept(self.getSelectedValue());
				}
			}
		});
		refreshTargets(targetNames);
		containerOf(this, node(BorderLayout.CENTER, targetList));
	}
	
	@Override
	public void setEnabled(boolean enabled) 
	{
		super.setEnabled(enabled);
		targetList.setEnabled(enabled);
	}
	
	/**
	 * Sets the targets on this panel.
	 * @param targetNames the target names to add.
	 */
	public void refreshTargets(Collection<String> targetNames)
	{
		SwingUtils.invoke(() -> {
			targetListModel.clear();
			for (String s : targetNames)
				targetListModel.addElement(s);
			targetList.setSelectedIndex(0);
		});
	}
	
}
