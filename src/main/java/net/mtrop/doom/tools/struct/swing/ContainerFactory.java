/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

/**
 * The main ContainerFactory class.
 * ContainerFactory is used for creating complex layouts quickly, without the tedium of creating
 * panels and layouts and borders.
 * @author Matthew Tropiano
 */
public final class ContainerFactory
{
	private ContainerFactory() {}
	
	/* ==================================================================== */
	/* ==== Containers                                                 ==== */
	/* ==================================================================== */

	/**
	 * Starts a layout tree, returning the provided container.
	 * The layout is replaced on it with the provided layout.
	 * @param container the root container.
	 * @param border the border to set on the container.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, Border border, LayoutManager layout, Node ... children)
	{
		container.setBorder(border);
		container.setLayout(layout);
		for (Node n : children)
			n.addTo(container);
		return container;
	}

	/**
	 * Starts a layout tree, returning the provided container.
	 * @param container the root container.
	 * @param border the border to set on the container.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, Border border, Node ... children)
	{
		for (Node n : children)
			n.addTo(container);
		return container;
	}

	/**
	 * Starts a layout tree, returning the provided container.
	 * The layout is replaced on it with the provided layout.
	 * @param container the root container.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, LayoutManager layout, Node ... children)
	{
		return containerOf(container, null, layout, children);
	}

	/**
	 * Starts a layout tree, returning the provided container.
	 * @param container the root container.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, Node ... children)
	{
		return containerOf(container, null, new BorderLayout(), children);
	}

	/**
	 * Starts a layout tree, returns a component.
	 * @param border the border to set on the container.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Border border, LayoutManager layout, Node ... children)
	{
		return containerOf(new JPanel(), border, layout, children);
	}

	/**
	 * Starts a Container with a BorderLayout.
	 * @param border the border to set on the container.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Border border, Node ... children)
	{
		return containerOf(new JPanel(), border, new BorderLayout(), children);
	}

	/**
	 * Starts a layout tree, returns a component.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(LayoutManager layout, Node ... children)
	{
		return containerOf(new JPanel(), layout, children);
	}

	/**
	 * Starts a Container with a BorderLayout.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Node ... children)
	{
		return containerOf(new JPanel(), null, new BorderLayout(), children);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param border the border to add to the panel.
	 * @param preferredSize the dimensions for the preferred size.
	 * @param layout the layout to use for this branch's children.
	 * @param children the children on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, Border border, Dimension preferredSize, LayoutManager layout, Node ... children)
	{
		return new NodeBranch(layout, border, preferredSize, constraints, children);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param preferredSize the dimensions for the preferred size.
	 * @param layout the layout to use for this branch's children.
	 * @param children the children on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, Dimension preferredSize, LayoutManager layout, Node ... children)
	{
		return new NodeBranch(layout, null, preferredSize, constraints, children);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param border the border to add to the panel.
	 * @param layout the layout to use for this branch's children.
	 * @param children the children on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, Border border, LayoutManager layout, Node ... children)
	{
		return new NodeBranch(layout, border, null, constraints, children);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param border the border to add to the panel.
	 * @param children the children on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, Border border, Node ... children)
	{
		return new NodeBranch(new BorderLayout(), border, null, constraints, children);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param border the border to add to the panel.
	 * @param preferredSize the dimensions for the preferred size.
	 * @param layout the layout to use for this branch's children.
	 * @param children the children on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Border border, Dimension preferredSize, LayoutManager layout, Node ... children)
	{
		return new NodeBranch(layout, border, preferredSize, null, children);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param border the border to add to the panel.
	 * @param layout the layout to use for this branch's children.
	 * @param children the children on the branch.
	 * @return a new branch.
	 */
	public static Node node(Border border, LayoutManager layout, Node ... children)
	{
		return new NodeBranch(layout, border, null, null, children);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param preferredSize the dimensions for the preferred size.
	 * @param layout the layout to use for this branch's children.
	 * @param children the children on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Dimension preferredSize, LayoutManager layout, Node ... children)
	{
		return new NodeBranch(layout, null, preferredSize, null, children);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param layout the layout to use for this branch's children.
	 * @param children the children on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, LayoutManager layout, Node ... children)
	{
		return new NodeBranch(layout, null, null, constraints, children);
	}

	/**
	 * Starts a new branch off of this branch.
	 * @param border the border to add to the panel.
	 * @param children the children on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Border border, Node ... children)
	{
		return new NodeBranch(new BorderLayout(), border, null, null, children);
	}

	/**
	 * Starts a new branch off of this branch.
	 * @param layout the layout to use for this branch's children.
	 * @param children the children on the branch.
	 * @return a new branch node.
	 */
	public static Node node(LayoutManager layout, Node ... children)
	{
		return new NodeBranch(layout, null, null, null, children);
	}

	/**
	 * Creates a leaf node (no children).
	 * @param constraints the constraints to use for the added leaf (using parent layout).
	 * @param component the component to add.
	 * @return a new leaf node.
	 */
	public static Node node(Object constraints, Component component)
	{
		return new NodeLeaf(constraints, component);
	}

	/**
	 * Creates a leaf node, no constraints (no children).
	 * @param component the component to add.
	 * @return a new leaf node.
	 */
	public static Node node(Component component)
	{
		return new NodeLeaf(null, component);
	}

	/**
	 * Common Swing node.
	 */
	public static abstract class Node
	{
		/** Constraints on the layout. */
		protected Object constraints;
		protected Node(Object constraints)
		{
			this.constraints = constraints;
		}
		
		protected abstract void addTo(Container container); 
	}

	/**
	 * Leaf node in Swing.
	 */
	private static class NodeLeaf extends Node
	{
		private Component component;
		private NodeLeaf(Object constraints, Component component)
		{
			super(constraints);
			this.component = component;
		}
		
		@Override
		protected void addTo(Container parent)
		{
			parent.add(component, constraints);
		}
	}

	/**
	 * Branch node in Swing.
	 */
	private static class NodeBranch extends Node
	{
		private Border border;
		private Dimension preferredSize;
		private LayoutManager layout; 
		private Node[] edges;
		
		private NodeBranch(LayoutManager layout, Border border, Dimension preferredSize, Object constraints, Node[] edges)
		{
			super(constraints);
			this.border = border;
			this.preferredSize = preferredSize;
			this.layout = layout;
			this.edges = edges;
		}
		
		@Override
		protected void addTo(Container container)
		{
			JPanel branchPanel = new JPanel();
			
			if (layout != null)
				branchPanel.setLayout(layout);
	
			if (preferredSize != null)
				branchPanel.setPreferredSize(preferredSize);
			
			if (border != null)
				branchPanel.setBorder(border);
			
			for (Node edge : edges)
				edge.addTo(branchPanel);
			
			container.add(branchPanel, constraints);
		}
		
	}

	/* ==================================================================== */
	/* ==== Tabs                                                       ==== */
	/* ==================================================================== */

	/**
	 * Creates a tab.
	 * @param icon the tab icon.
	 * @param name the name of the tab.
	 * @param tooltip the tool tip on the tab.
	 * @param component the component attached to the tab.
	 * @return a new Tab.
	 */
	public static Tab tab(Icon icon, String name, String tooltip, Component component)
	{
		return new Tab(icon, name, tooltip, component);
	}
	
	/**
	 * Creates a tab.
	 * @param name the name of the tab.
	 * @param tooltip the tool tip on the tab.
	 * @param component the component attached to the tab.
	 * @return a new Tab.
	 */
	public static Tab tab(String name, String tooltip, Component component)
	{
		return new Tab(null, name, tooltip, component);
	}
	
	/**
	 * Creates a tab.
	 * @param icon the tab icon.
	 * @param name the name of the tab.
	 * @param component the component attached to the tab.
	 * @return a new Tab.
	 */
	public static Tab tab(Icon icon, String name, Component component)
	{
		return new Tab(icon, name, null, component);
	}
	
	/**
	 * Creates a tab.
	 * @param name the name of the tab.
	 * @param component the component attached to the tab.
	 * @return a new Tab.
	 */
	public static Tab tab(String name, Component component)
	{
		return new Tab(null, name, null, component);
	}
	
	/**
	 * Creates a tabbed pane component.
	 * @param tabPlacement the tab placement policy (from JTabbedPane).
	 * @param tabLayoutPolicy the tab layout policy (from JTabbedPane).
	 * @param tabs the tabs to add.
	 * @return a new tabbed pane component.
	 */
	public static JTabbedPane tabs(int tabPlacement, int tabLayoutPolicy, Tab ... tabs)
	{
		return attachTabs(new JTabbedPane(tabPlacement, tabLayoutPolicy), tabs);
	}
	
	/**
	 * Creates a tabbed pane component (wrapped tabs).
	 * @param tabPlacement the tab placement policy (from JTabbedPane).
	 * @param tabs the tabs to add.
	 * @return a new tabbed pane component.
	 */
	public static JTabbedPane tabs(int tabPlacement, Tab ... tabs)
	{
		return attachTabs(new JTabbedPane(tabPlacement), tabs);
	}

	/**
	 * Creates a tabbed pane component (tabs on TOP, wrapped tabs).
	 * @param tabs the tabs to add.
	 * @return a new tabbed pane component.
	 */
	public static JTabbedPane tabs(Tab ... tabs)
	{
		return attachTabs(new JTabbedPane(), tabs);
	}

	/**
	 * Attaches a series of tabs to a JTabbedPane, returning the TabbedPane.
	 * @param tabbedPane the tabbed pane to add to.
	 * @param tabs the tabs to add.
	 * @return the tabbed pane component passed in.
	 */
	public static JTabbedPane attachTabs(JTabbedPane tabbedPane, Tab ... tabs)
	{
		for (Tab t : tabs)
			tabbedPane.addTab(t.name, t.icon, t.component, t.tooltip);
		return tabbedPane;
	}

	/**
	 * A single tab entry.
	 */
	public static class Tab
	{
		private Icon icon;
		private String name;
		private Component component;
		private String tooltip;
		private Tab(Icon icon, String name, String tooltip, Component component)
		{
			this.icon = icon;
			this.name = name;
			this.component = component;
			this.tooltip = tooltip;
		}
	}

	/* ==================================================================== */
	/* ==== Split Panes                                                ==== */
	/* ==================================================================== */

	/**
	 * Creates a split pane.
	 * @param orientation the split orientation (from JSplitPane).
	 * @param continuousLayout if true, this renders as the size is adjusted.
	 * @param first the first component.
	 * @param second the second component.
	 * @return a new split pane.
	 */
	public static JSplitPane split(int orientation, boolean continuousLayout, Component first, Component second)
	{
		return new JSplitPane(orientation, continuousLayout, first, second);
	}

	/**
	 * Creates a split pane.
	 * @param orientation the split orientation (from JSplitPane).
	 * @param first the first component.
	 * @param second the second component.
	 * @return a new split pane.
	 */
	public static JSplitPane split(int orientation, Component first, Component second)
	{
		return new JSplitPane(orientation, true, first, second);
	}

	/* ==================================================================== */
	/* ==== Scroll Panes                                               ==== */
	/* ==================================================================== */

	/**
	 * Creates a scrolling pane.
	 * @param vsbPolicy the vertical scroll policy (from JScrollPane).
	 * @param hsbPolicy the horizontal scroll policy (from JScrollPane).
	 * @param component the component to add to the scroller. 
	 * @return a scroll pane.
	 */
	public static JScrollPane scroll(int vsbPolicy, int hsbPolicy, Component component)
	{
		return new JScrollPane(component, vsbPolicy, hsbPolicy);
	}

	/**
	 * Creates a scrolling pane.
	 * @param vsbPolicy the vertical scroll policy (from JScrollPane).
	 * @param component the component 
	 * @return a scroll pane.
	 */
	public static JScrollPane scroll(int vsbPolicy, Component component)
	{
		return scroll(vsbPolicy, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, component);
	}

	/**
	 * Creates a scrolling pane.
	 * @param component the component 
	 * @return a scroll pane.
	 */
	public static JScrollPane scroll(Component component)
	{
		return scroll(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, component);
	}

	/* ==================================================================== */
	/* ==== Frames                                                     ==== */
	/* ==================================================================== */

	/**
	 * Creates a new internal frame (for {@link JDesktopPane}s).
	 * @param title the title of the frame.
	 * @param menuBar the menu bar to add.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JInternalFrame internalFrame(String title, JMenuBar menuBar, Container content)
	{
		JInternalFrame out = new JInternalFrame(title);
		out.add(menuBar);
		out.setContentPane(content);
		out.pack();
		return out;
	}
	
	/**
	 * Creates a new internal frame (for {@link JDesktopPane}s).
	 * @param title the title of the frame.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JInternalFrame internalFrame(String title, Container content)
	{
		JInternalFrame out = new JInternalFrame(title);
		out.setContentPane(content);
		out.pack();
		return out;
	}
	
	/**
	 * Creates a new frame.
	 * @param icons the icon images in different dimensions.
	 * @param title the title of the frame.
	 * @param menuBar the menu bar to add.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JFrame frame(List<Image> icons, String title, JMenuBar menuBar, Container content)
	{
		JFrame out = new JFrame(title);
		out.setIconImages(icons);
		out.add(menuBar);
		out.setContentPane(content);
		out.setLocationByPlatform(true);
		out.pack();
		return out;
	}
	
	/**
	 * Creates a new frame.
	 * @param icons the icon images in different dimensions.
	 * @param title the title of the frame.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JFrame frame(List<Image> icons, String title, Container content)
	{
		JFrame out = new JFrame(title);
		out.setIconImages(icons);
		out.setContentPane(content);
		out.setLocationByPlatform(true);
		out.pack();
		return out;
	}
	
	/**
	 * Creates a new frame.
	 * @param icon the icon image.
	 * @param title the title of the frame.
	 * @param menuBar the menu bar to add.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JFrame frame(Image icon, String title, JMenuBar menuBar, Container content)
	{
		JFrame out = new JFrame(title);
		out.setIconImage(icon);
		out.add(menuBar);
		out.setContentPane(content);
		out.setLocationByPlatform(true);
		out.pack();
		return out;
	}
	
	/**
	 * Creates a new frame.
	 * @param icon the icon image.
	 * @param title the title of the frame.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JFrame frame(Image icon, String title, Container content)
	{
		JFrame out = new JFrame(title);
		out.setIconImage(icon);
		out.setContentPane(content);
		out.setLocationByPlatform(true);
		out.pack();
		return out;
	}
	
	/**
	 * Creates a new frame.
	 * @param title the title of the frame.
	 * @param menuBar the menu bar to add.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JFrame frame(String title, JMenuBar menuBar, Container content)
	{
		JFrame out = new JFrame(title);
		out.setJMenuBar(menuBar);
		out.setContentPane(content);
		out.setLocationByPlatform(true);
		out.pack();
		return out;
	}
	
	/**
	 * Creates a new frame.
	 * @param title the title of the frame.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JFrame frame(String title, Container content)
	{
		JFrame out = new JFrame(title);
		out.setContentPane(content);
		out.setLocationByPlatform(true);
		out.pack();
		return out;
	}

	/* ==================================================================== */
	/* ==== Modals                                                     ==== */
	/* ==================================================================== */

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, List<Image> icons, String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		Modal<T> out = new Modal<>(owner);
		out.setTitle(title);
		out.setIconImages(icons);
		return modal(out, contentPane, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, Image icon, String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		Modal<T> out = new Modal<>(owner);
		out.setTitle(title);
		out.setIconImage(icon);
		return modal(out, contentPane, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		Modal<T> out = new Modal<>(owner);
		out.setTitle(title);
		return modal(out, contentPane, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(List<Image> icons, String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(null, icons, title, contentPane, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Image icon, String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(null, icon, title, contentPane, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal((Container)null, title, contentPane, choices);
	}
	
	@SafeVarargs
	private static <T> Modal<T> modal(final Modal<T> modal, Container contentPane, final ModalChoice<T> ... choices)
	{
		Node[] nodes = new Node[choices.length];
		for (int i = 0; i < nodes.length; i++)
		{
			final ModalChoice<T> choice = choices[i];
			JButton button = new JButton(new AbstractAction(choice.label, choice.icon)
			{
				private static final long serialVersionUID = 7418011293584407833L;

				@Override
				public void actionPerformed(ActionEvent e) 
				{
					modal.setValue(choice.onClick.get());
					modal.setVisible(false);
				}
			});
			button.setMnemonic(choice.mnemonic);
			nodes[i] = node(button);
		}
		modal.setContentPane(containerOf(
			node(BorderLayout.CENTER, contentPane),
			node(BorderLayout.SOUTH, containerOf(new FlowLayout(FlowLayout.TRAILING, 8, 8), nodes))
		));
		modal.setLocationByPlatform(true);
		modal.pack();
		modal.setMinimumSize(modal.getSize());
		modal.setResizable(false);
		modal.setLocationRelativeTo(modal.getOwner());
		return modal;
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, String label, int mnemonic, T result)
	{
		return choice(icon, label, mnemonic, () -> result);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, int mnemonic, T result)
	{
		return choice(null, label, mnemonic, () -> result);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, int mnemonic)
	{
		return choice(null, label, mnemonic, (T)null);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, T result)
	{
		return choice(null, label, 0, () -> result);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label)
	{
		return choice(null, label, 0, (T)null);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, String label, int mnemonic, Supplier<T> onClick)
	{
		return new ModalChoice<>(icon, label, mnemonic, onClick);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, int mnemonic, Supplier<T> onClick)
	{
		return new ModalChoice<>(null, label, mnemonic, onClick);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, Supplier<T> onClick)
	{
		return new ModalChoice<>(null, label, 0, onClick);
	}
	
	/**
	 * A single modal choice abstracted as a button. 
	 * @param <T> the return type.
	 */
	public static class ModalChoice<T>
	{
		private Icon icon;
		private String label;
		private int mnemonic;
		private Supplier<T> onClick;
		private ModalChoice(Icon icon, String label, int mnemonic, Supplier<T> onClick)
		{
			this.icon = icon;
			this.label = label;
			this.mnemonic = mnemonic;
			this.onClick = onClick;
		}
	}
	
	/**
	 * The custom modal.
	 * @param <T> the return type. 
	 */
	public static class Modal<T> extends JDialog
	{
		private static final long serialVersionUID = 3962942391178207377L;

		private T value;
		
		private Modal(Container owner)
		{
			super(getWindowForComponent(owner));
			setModalityType(ModalityType.APPLICATION_MODAL);
		}

		private static Window getWindowForComponent(Component parent) throws HeadlessException 
		{
	        if (parent == null)
	            return null;
	        if (parent instanceof Frame || parent instanceof Dialog)
	            return (Window)parent;
	        return getWindowForComponent(parent.getParent());
	    }

		
		@Override
		public void setVisible(boolean b) 
		{
			if (b)
				value = null;
			super.setVisible(b);
		}
		
		/**
		 * Sets the value for this modal.
		 * @param value the value.
		 */
		public void setValue(T value) 
		{
			this.value = value;
		}

		/**
		 * Gets the value result of this dialog.
		 * If the dialog is made visible and closed without a button clicked,
		 * the value is null.
		 * @return the value result.
		 */
		public T getValue() 
		{
			return value;
		}
		
		/**
		 * Opens the dialog, waits for a choice, and then returns it.
		 * @return the value result.
		 */
		public T open()
		{
			setVisible(true);
			return getValue();
		}

		/**
		 * Opens the dialog, waits for a choice, and then returns it and disposes the modal.
		 * @return the value result.
		 */
		public T openThenDispose()
		{
			T out = open();
			dispose();
			return out;
		}
	}
	
}
