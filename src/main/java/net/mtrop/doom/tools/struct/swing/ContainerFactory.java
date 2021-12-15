/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.LayoutManager;

import javax.swing.Icon;
import javax.swing.JFrame;
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
	
	/**
	 * Starts a layout tree, returns a container.
	 * @param container the root container.
	 * @param edges the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(Container container, Node ... edges)
	{
		for (Node n : edges)
			n.addTo(container);
		return container;
	}

	/**
	 * Starts a layout tree, returns a component.
	 * @param layout the layout to use for this tree's children.
	 * @param edges the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(LayoutManager layout, Node ... edges)
	{
		JPanel out = new JPanel();
		out.setLayout(layout);
		for (Node n : edges)
			n.addTo(out);
		return out;
	}

	/**
	 * Starts a Container with a BorderLayout.
	 * @param root the root node.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Node root)
	{
		return containerOf(new BorderLayout(), root);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param border the border to add to the panel.
	 * @param preferredSize the dimensions for the preferred size.
	 * @param layout the layout to use for this branch's children.
	 * @param edges the edges on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, Border border, Dimension preferredSize, LayoutManager layout, Node ... edges)
	{
		return new NodeBranch(layout, border, preferredSize, constraints, edges);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param preferredSize the dimensions for the preferred size.
	 * @param layout the layout to use for this branch's children.
	 * @param edges the edges on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, Dimension preferredSize, LayoutManager layout, Node ... edges)
	{
		return new NodeBranch(layout, null, preferredSize, constraints, edges);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param border the border to add to the panel.
	 * @param layout the layout to use for this branch's children.
	 * @param edges the edges on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, Border border, LayoutManager layout, Node ... edges)
	{
		return new NodeBranch(layout, border, null, constraints, edges);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param border the border to add to the panel.
	 * @param preferredSize the dimensions for the preferred size.
	 * @param layout the layout to use for this branch's children.
	 * @param edges the edges on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Border border, Dimension preferredSize, LayoutManager layout, Node ... edges)
	{
		return new NodeBranch(layout, border, preferredSize, null, edges);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param border the border to add to the panel.
	 * @param layout the layout to use for this branch's children.
	 * @param edges the edges on the branch.
	 * @return a new branch.
	 */
	public static Node node(Border border, LayoutManager layout, Node ... edges)
	{
		return new NodeBranch(layout, border, null, null, edges);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param preferredSize the dimensions for the preferred size.
	 * @param layout the layout to use for this branch's children.
	 * @param edges the edges on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Dimension preferredSize, LayoutManager layout, Node ... edges)
	{
		return new NodeBranch(layout, null, preferredSize, null, edges);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param layout the layout to use for this branch's children.
	 * @param edges the edges on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, LayoutManager layout, Node ... edges)
	{
		return new NodeBranch(layout, null, null, constraints, edges);
	}

	/**
	 * Starts a new branch off of this branch.
	 * @param border the border to add to the panel.
	 * @param edges the edges on the branch.
	 * @return a new branch node.
	 */
	public static Node node(Border border, Node ... edges)
	{
		return new NodeBranch(new BorderLayout(), border, null, null, edges);
	}

	/**
	 * Starts a new branch off of this branch.
	 * @param layout the layout to use for this branch's children.
	 * @param edges the edges on the branch.
	 * @return a new branch node.
	 */
	public static Node node(LayoutManager layout, Node ... edges)
	{
		return new NodeBranch(layout, null, null, null, edges);
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
		JTabbedPane out = new JTabbedPane(tabPlacement, tabLayoutPolicy);
		for (Tab t : tabs)
			out.addTab(t.name, t.icon, t.component, t.tooltip);
		return out;
	}
	
	/**
	 * Creates a tabbed pane component (wrapped tabs).
	 * @param tabPlacement the tab placement policy (from JTabbedPane).
	 * @param tabs the tabs to add.
	 * @return a new tabbed pane component.
	 */
	public static JTabbedPane tabs(int tabPlacement, Tab ... tabs)
	{
		JTabbedPane out = new JTabbedPane(tabPlacement);
		for (Tab t : tabs)
			out.addTab(t.name, t.icon, t.component, t.tooltip);
		return out;
	}

	/**
	 * Creates a tabbed pane component (tabs on TOP, wrapped tabs).
	 * @param tabs the tabs to add.
	 * @return a new tabbed pane component.
	 */
	public static JTabbedPane tabs(Tab ... tabs)
	{
		JTabbedPane out = new JTabbedPane();
		for (Tab t : tabs)
			out.addTab(t.name, t.icon, t.component, t.tooltip);
		return out;
	}

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
	 * @param container the container 
	 * @return a scroll pane.
	 */
	public static JScrollPane scroll(int vsbPolicy, Container container)
	{
		return scroll(vsbPolicy, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, container);
	}

	/**
	 * Creates a scrolling pane.
	 * @param container the container 
	 * @return a scroll pane.
	 */
	public static JScrollPane scroll(Container container)
	{
		return scroll(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, container);
	}

	/* ==================================================================== */

	/**
	 * Creates a new frame.
	 * @param image the icon image.
	 * @param title the title of the frame.
	 * @param menuBar the menu bar to add.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JFrame frame(Image image, String title, JMenuBar menuBar, Container content)
	{
		JFrame out = new JFrame(title);
		out.setIconImage(image);
		out.add(menuBar);
		out.setContentPane(content);
		out.pack();
		return out;
	}
	
	/**
	 * Creates a new frame.
	 * @param image the icon image.
	 * @param title the title of the frame.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JFrame frame(Image image, String title, Container content)
	{
		JFrame out = new JFrame(title);
		out.setIconImage(image);
		out.setContentPane(content);
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
		out.pack();
		return out;
	}
	
	/* ==================================================================== */

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
	
}
