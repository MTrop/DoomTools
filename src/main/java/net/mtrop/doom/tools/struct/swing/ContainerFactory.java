/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
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
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
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
	/* ==== Enums                                                      ==== */
	/* ==================================================================== */

	/**
	 * Tab placement enumeration.
	 */
	public enum TabPlacement
	{
		TOP(JTabbedPane.TOP),
		BOTTOM(JTabbedPane.BOTTOM),
		LEFT(JTabbedPane.LEFT),
		RIGHT(JTabbedPane.RIGHT);
		
		private final int swingId;
		
		private TabPlacement(int swingId)
		{
			this.swingId = swingId;
		}
	}
	
	/**
	 * Tab layout enumeration for too many tabs.
	 */
	public enum TabLayoutPolicy
	{
		WRAP(JTabbedPane.WRAP_TAB_LAYOUT),
		SCROLL(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		private final int swingId;
		
		private TabLayoutPolicy(int swingId)
		{
			this.swingId = swingId;
		}
	}
	
	/**
	 * Scrolling policy for scroll panes.
	 */
	public enum ScrollPolicy
	{
		NEVER(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS),
		AS_NEEDED(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED),
		ALWAYS(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER, JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		
		private final int swingIdHorizontal;
		private final int swingIdVertical;
		
		private ScrollPolicy(int swingIdHorizontal, int swingIdVertical)
		{
			this.swingIdHorizontal = swingIdHorizontal;
			this.swingIdVertical = swingIdVertical;
		}
	}
	
	/**
	 * Split orientation for split panes.
	 */
	public enum SplitOrientation
	{
		HORIZONTAL(JSplitPane.HORIZONTAL_SPLIT),
		VERTICAL(JSplitPane.VERTICAL_SPLIT);
		
		private final int swingId;
		
		private SplitOrientation(int swingId)
		{
			this.swingId = swingId;
		}
	}
	
	/* ==================================================================== */
	/* ==== Containers                                                 ==== */
	/* ==================================================================== */

	/**
	 * Starts a layout tree, returning the provided container.
	 * The layout is replaced on it with the provided layout.
	 * @param container the root container.
	 * @param preferredSize the dimensions for the preferred size of this container.
	 * @param border the border to set on the container.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, Dimension preferredSize, Border border, LayoutManager layout, Node ... children)
	{
		container.setBorder(border);
		container.setLayout(layout);
		if (preferredSize != null)
			container.setPreferredSize(preferredSize);
		for (Node n : children)
			n.addTo(container);
		return container;
	}

	/**
	 * Starts a container layout tree, returning the provided container.
	 * The layout is replaced on it with the provided layout.
	 * @param container the root container.
	 * @param border the border to set on the container.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, Border border, LayoutManager layout, Node ... children)
	{
		return containerOf(container, null, border, layout, children);
	}

	/**
	 * Starts a layout tree, returning the provided container.
	 * @param container the root container.
	 * @param preferredSize the dimensions for the preferred size of this container.
	 * @param border the border to set on the container.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, Dimension preferredSize, Border border, Node ... children)
	{
		return containerOf(container, preferredSize, border, null, children);
	}

	/**
	 * Starts a container layout tree, returning the provided container.
	 * @param container the root container.
	 * @param border the border to set on the container.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, Border border, Node ... children)
	{
		return containerOf(container, null, border, new BorderLayout(), children);
	}

	/**
	 * Starts a layout tree, returning the provided container.
	 * The layout is replaced on it with the provided layout.
	 * @param container the root container.
	 * @param preferredSize the dimensions for the preferred size of this container.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, Dimension preferredSize, LayoutManager layout, Node ... children)
	{
		return containerOf(container, preferredSize, null, layout, children);
	}

	/**
	 * Starts a container layout tree, returning the provided container.
	 * The layout is replaced on it with the provided layout.
	 * @param container the root container.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, LayoutManager layout, Node ... children)
	{
		return containerOf(container, null, null, layout, children);
	}

	/**
	 * Starts a layout tree, returning the provided container.
	 * @param container the root container.
	 * @param preferredSize the dimensions for the preferred size of this container.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, Dimension preferredSize, Node ... children)
	{
		return containerOf(container, preferredSize, null, new BorderLayout(), children);
	}

	/**
	 * Starts a container layout tree, returning the provided container.
	 * @param container the root container.
	 * @param children the component's children.
	 * @return the component passed in, with the descendants added.
	 */
	public static Container containerOf(JComponent container, Node ... children)
	{
		return containerOf(container, null, null, new BorderLayout(), children);
	}

	/**
	 * Starts a layout tree, returns a component.
	 * @param preferredSize the dimensions for the preferred size of this container.
	 * @param border the border to set on the container.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Dimension preferredSize, Border border, LayoutManager layout, Node ... children)
	{
		return containerOf(new JPanel(), preferredSize, border, layout, children);
	}

	/**
	 * Starts a Container with a BorderLayout.
	 * @param preferredSize the dimensions for the preferred size of this container.
	 * @param border the border to set on the container.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Dimension preferredSize, Border border, Node ... children)
	{
		return containerOf(new JPanel(), preferredSize, border, new BorderLayout(), children);
	}

	/**
	 * Starts a layout tree, returns a component.
	 * @param preferredSize the dimensions for the preferred size of this container.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Dimension preferredSize, LayoutManager layout, Node ... children)
	{
		return containerOf(new JPanel(), preferredSize, null, layout, children);
	}

	/**
	 * Starts a Container with a BorderLayout.
	 * @param preferredSize the dimensions for the preferred size of this container.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Dimension preferredSize, Node ... children)
	{
		return containerOf(new JPanel(), preferredSize, null, new BorderLayout(), children);
	}

	/**
	 * Starts a container layout tree, returns a component.
	 * @param border the border to set on the container.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Border border, LayoutManager layout, Node ... children)
	{
		return containerOf(new JPanel(), null, border, layout, children);
	}

	/**
	 * Starts a container with a BorderLayout.
	 * @param border the border to set on the container.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Border border, Node ... children)
	{
		return containerOf(new JPanel(), null, border, new BorderLayout(), children);
	}

	/**
	 * Starts a container layout tree, returns a component.
	 * @param layout the layout to use for this tree's children.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(LayoutManager layout, Node ... children)
	{
		return containerOf(new JPanel(), null, null, layout, children);
	}

	/**
	 * Starts a Container with a BorderLayout.
	 * @param children the component's children.
	 * @return a component that is the result of creating the tree.
	 */
	public static Container containerOf(Node ... children)
	{
		return containerOf(new JPanel(), null, null, new BorderLayout(), children);
	}

	/**
	 * Creates a leaf node (no children). 
	 * @param <C> the component type.
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param preferredSize the dimensions for the preferred size.
	 * @param component the component to add.
	 * @param applier an additional function to run to alter the component before adding.
	 * @return a new branch node.
	 */
	public static <C extends Component> Node node(Object constraints, Dimension preferredSize, C component, Consumer<C> applier)
	{
		if (applier != null)
			applier.accept(component);
		return new Node(constraints, preferredSize, component);
	}

	/**
	 * Creates a leaf node (no children).
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param preferredSize the dimensions for the preferred size.
	 * @param component the component to add.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, Dimension preferredSize, Component component)
	{
		return node(constraints, preferredSize, component, null);
	}

	/**
	 * Creates a leaf node (no children). 
	 * @param <C> the component type.
	 * @param preferredSize the dimensions for the preferred size.
	 * @param component the component to add.
	 * @param applier an additional function to run to alter the component before adding.
	 * @return a new branch node.
	 */
	public static <C extends Component> Node node(Dimension preferredSize, C component, Consumer<C> applier)
	{
		return node(null, preferredSize, component, applier);
	}

	/**
	 * Creates a leaf node (no children). 
	 * @param preferredSize the dimensions for the preferred size.
	 * @param component the component to add.
	 * @return a new branch node.
	 */
	public static Node node(Dimension preferredSize, Component component)
	{
		return node(null, preferredSize, component, null);
	}

	/**
	 * Creates a leaf node (no children).
	 * @param <C> the component type.
	 * @param constraints the constraints to use for the added leaf (using parent layout).
	 * @param component the component to add.
	 * @param applier an additional function to run to alter the component before adding.
	 * @return a new leaf node.
	 */
	public static <C extends Component> Node node(Object constraints, C component, Consumer<C> applier)
	{
		return node(constraints, null, component, applier);
	}

	/**
	 * Creates a leaf node (no children).
	 * @param constraints the constraints to use for the added leaf (using parent layout).
	 * @param component the component to add.
	 * @return a new leaf node.
	 */
	public static Node node(Object constraints, Component component)
	{
		return node(constraints, null, component, null);
	}

	/**
	 * Creates a leaf node, no constraints (no children).
	 * @param <C> the component type.
	 * @param component the component to add.
	 * @param applier an additional function to run to alter the component before adding.
	 * @return a new leaf node.
	 */
	public static <C extends Component> Node node(C component, Consumer<C> applier)
	{
		return node(null, null, component, applier);
	}

	/**
	 * Creates a leaf node, no constraints (no children).
	 * @param component the component to add.
	 * @return a new leaf node.
	 */
	public static Node node(Component component)
	{
		return node(null, null, component, null);
	}

	/**
	 * Creates a new {@link Dimension}.
	 * The intent behind this shallow function is to unify the grammar used to build containers via this factory. 
	 * @param width the dimension width.
	 * @param height the dimension height.
	 * @return a new dimension.
	 */
	public static Dimension dimension(int width, int height)
	{
		return new Dimension(width, height);
	}
	
	/**
	 * A component node.
	 */
	public static class Node
	{
		private Object constraints;
		private Dimension preferredSize;
		private Component component;
		
		private Node(Object constraints, Dimension preferredSize, Component component)
		{
			this.constraints = constraints;
			this.component = component;
			this.preferredSize = preferredSize;
		}
		
		protected void addTo(Container parent)
		{
			if (preferredSize != null)
				component.setPreferredSize(preferredSize);
			
			parent.add(component, constraints);
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
	 * @param tabPlacement the tab placement policy.
	 * @param tabLayoutPolicy the tab layout policy.
	 * @param tabs the tabs to add.
	 * @return a new tabbed pane component.
	 */
	public static JTabbedPane tabs(TabPlacement tabPlacement, TabLayoutPolicy tabLayoutPolicy, Tab ... tabs)
	{
		return attachTabs(new JTabbedPane(tabPlacement.swingId, tabLayoutPolicy.swingId), tabs);
	}
	
	/**
	 * Creates a tabbed pane component (scroll as needed).
	 * @param tabLayoutPolicy the tab layout policy (from JTabbedPane).
	 * @param tabs the tabs to add.
	 * @return a new tabbed pane component.
	 */
	public static JTabbedPane tabs(TabLayoutPolicy tabLayoutPolicy, Tab ... tabs)
	{
		return tabs(TabPlacement.TOP, tabLayoutPolicy, tabs);
	}

	/**
	 * Creates a tabbed pane component (wrapped tabs).
	 * @param tabPlacement the tab placement policy (from JTabbedPane).
	 * @param tabs the tabs to add.
	 * @return a new tabbed pane component.
	 */
	public static JTabbedPane tabs(TabPlacement tabPlacement, Tab ... tabs)
	{
		return tabs(tabPlacement, TabLayoutPolicy.WRAP, tabs);
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
	 * Attaches a series of tabs to a JTabbedPane, returning the JTabbedPane.
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
	 * @param orientation the split orientation.
	 * @param continuousLayout if true, this renders as the size is adjusted.
	 * @param first the first component.
	 * @param second the second component.
	 * @return a new split pane.
	 */
	public static JSplitPane split(SplitOrientation orientation, boolean continuousLayout, Component first, Component second)
	{
		return new JSplitPane(orientation.swingId, continuousLayout, first, second);
	}

	/**
	 * Creates a split pane (horizontal orientation).
	 * @param continuousLayout if true, this renders as the size is adjusted.
	 * @param first the first component.
	 * @param second the second component.
	 * @return a new split pane.
	 */
	public static JSplitPane split(boolean continuousLayout, Component first, Component second)
	{
		return split(SplitOrientation.HORIZONTAL, true, first, second);
	}

	/**
	 * Creates a split pane.
	 * @param orientation the split orientation.
	 * @param first the first component.
	 * @param second the second component.
	 * @return a new split pane.
	 */
	public static JSplitPane split(SplitOrientation orientation, Component first, Component second)
	{
		return split(orientation, true, first, second);
	}

	/**
	 * Creates a split pane (horizontal orientation, continuous render).
	 * @param first the first component.
	 * @param second the second component.
	 * @return a new split pane.
	 */
	public static JSplitPane split(Component first, Component second)
	{
		return split(SplitOrientation.HORIZONTAL, true, first, second);
	}

	/* ==================================================================== */
	/* ==== Scroll Panes                                               ==== */
	/* ==================================================================== */

	/**
	 * Creates a scrolling pane.
	 * @param verticalPolicy the vertical scroll policy.
	 * @param horizontalPolicy the horizontal scroll policy.
	 * @param component the component to add to the scroller. 
	 * @return a scroll pane.
	 */
	public static JScrollPane scroll(ScrollPolicy verticalPolicy, ScrollPolicy horizontalPolicy, Component component)
	{
		return new JScrollPane(component, verticalPolicy.swingIdVertical, horizontalPolicy.swingIdHorizontal);
	}

	/**
	 * Creates a scrolling pane.
	 * @param verticalPolicy the vertical scroll policy (from JScrollPane).
	 * @param component the component 
	 * @return a scroll pane.
	 */
	public static JScrollPane scroll(ScrollPolicy verticalPolicy, Component component)
	{
		return scroll(verticalPolicy, ScrollPolicy.AS_NEEDED, component);
	}

	/**
	 * Creates a scrolling pane.
	 * @param component the component 
	 * @return a scroll pane.
	 */
	public static JScrollPane scroll(Component component)
	{
		return scroll(ScrollPolicy.AS_NEEDED, ScrollPolicy.AS_NEEDED, component);
	}

	/* ==================================================================== */
	/* ==== Frames                                                     ==== */
	/* ==================================================================== */

	/**
	 * Creates a new internal frame (for {@link JDesktopPane}s).
	 * @param icon the icon for the frame.
	 * @param title the title of the frame.
	 * @param menuBar the menu bar to add.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JInternalFrame internalFrame(Icon icon, String title, JMenuBar menuBar, Container content)
	{
		JInternalFrame out = new JInternalFrame(title);
		out.setFrameIcon(icon);
		if (menuBar != null)
			out.add(menuBar);
		out.setContentPane(content);
		out.pack();
		return out;
	}
	
	/**
	 * Creates a new internal frame (for {@link JDesktopPane}s).
	 * @param icon the icon for the frame.
	 * @param title the title of the frame.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JInternalFrame internalFrame(Icon icon, String title, Container content)
	{
		return internalFrame(icon, title, null, content);
	}
	
	/**
	 * Creates a new internal frame (for {@link JDesktopPane}s).
	 * @param title the title of the frame.
	 * @param menuBar the menu bar to add.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JInternalFrame internalFrame(String title, JMenuBar menuBar, Container content)
	{
		return internalFrame(null, title, menuBar, content);
	}
	
	/**
	 * Creates a new internal frame (for {@link JDesktopPane}s).
	 * @param title the title of the frame.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JInternalFrame internalFrame(String title, Container content)
	{
		return internalFrame(null, title, null, content);
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
		if (menuBar != null)
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
		return frame(icons, title, null, content);
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
		if (icon != null)
			out.setIconImage(icon);
		if (menuBar != null)
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
		return frame(icon, title, null, content);
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
		return frame((Image)null, title, menuBar, content);
	}
	
	/**
	 * Creates a new frame.
	 * @param title the title of the frame.
	 * @param content the content pane.
	 * @return a new frame.
	 */
	public static JFrame frame(String title, Container content)
	{
		return frame((Image)null, title, null, content);
	}
	
}
