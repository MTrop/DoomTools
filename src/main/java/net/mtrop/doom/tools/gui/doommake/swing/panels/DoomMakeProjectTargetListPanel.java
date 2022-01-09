package net.mtrop.doom.tools.gui.doommake.swing.panels;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputAdapter;

import net.mtrop.doom.tools.struct.swing.ComponentFactory;

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

		this.targetList = ComponentFactory.list(this.targetListModel, ListSelectionModel.SINGLE_SELECTION, (selected, adjusting) -> {
			if (!adjusting)
				selectListener.accept(selected.get(0));
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
		containerOf(this, new BorderLayout(), node(BorderLayout.CENTER, targetList));
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
		targetListModel.clear();
		for (String s : targetNames)
			targetListModel.addElement(s);
		targetList.setSelectedIndex(0);
	}
	
}
