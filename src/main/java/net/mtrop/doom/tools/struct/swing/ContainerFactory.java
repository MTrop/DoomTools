/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Function;
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

import static javax.swing.BorderFactory.*;


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

	/* ==================================================================== */
	/* ==== Containers                                                 ==== */
	/* ==================================================================== */
	
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
	 * Starts a layout tree, returns a component.
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
	 * Starts a new branch off of this branch. 
	 * @param constraints the constraints to use for the added branch (using parent layout).
	 * @param preferredSize the dimensions for the preferred size.
	 * @param component the component to add.
	 * @return a new branch node.
	 */
	public static Node node(Object constraints, Dimension preferredSize, Component component)
	{
		return new Node(constraints, preferredSize, component);
	}

	/**
	 * Starts a new branch off of this branch. 
	 * @param preferredSize the dimensions for the preferred size.
	 * @param component the component to add.
	 * @return a new branch node.
	 */
	public static Node node(Dimension preferredSize, Component component)
	{
		return node(null, preferredSize, component);
	}

	/**
	 * Creates a leaf node (no children).
	 * @param constraints the constraints to use for the added leaf (using parent layout).
	 * @param component the component to add.
	 * @return a new leaf node.
	 */
	public static Node node(Object constraints, Component component)
	{
		return node(constraints, null, component);
	}

	/**
	 * Creates a leaf node, no constraints (no children).
	 * @param component the component to add.
	 * @return a new leaf node.
	 */
	public static Node node(Component component)
	{
		return node(null, null, component);
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

	
	/* ==================================================================== */
	/* ==== Special Modals                                             ==== */
	/* ==================================================================== */

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Container owner, List<Image> icons, String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		Boolean out = modal(owner, icons, title, ModalityType.APPLICATION_MODAL, contentPane, choices).openThenDispose();
		if (out == null || out == Boolean.FALSE)
			return null;
		return settingExtractor.apply(contentPane);
	}	
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Container owner, List<Image> icons, String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(owner, icons, title, contentPane, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}	
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Container owner, Image icon, String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		Boolean out = modal(owner, icon, title, ModalityType.APPLICATION_MODAL, contentPane, choices).openThenDispose();
		if (out == null || out == Boolean.FALSE)
			return null;
		return settingExtractor.apply(contentPane);
	}	
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Container owner, Image icon, String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(owner, icon, title, contentPane, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}	

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Container owner, String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(owner, (Image)null, title, contentPane, settingExtractor, choices);
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Container owner, String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(owner, (Image)null, title, contentPane, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(List<Image> icons, String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		Boolean out = modal(null, icons, title, ModalityType.APPLICATION_MODAL, contentPane, choices).openThenDispose();
		if (out == null || out == Boolean.FALSE)
			return null;
		return settingExtractor.apply(contentPane);
	}	
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(List<Image> icons, String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(null, icons, title, contentPane, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}	
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Image icon, String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		Boolean out = modal(null, icon, title, ModalityType.APPLICATION_MODAL, contentPane, choices).openThenDispose();
		if (out == null || out == Boolean.FALSE)
			return null;
		return settingExtractor.apply(contentPane);
	}	
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Image icon, String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(null, icon, title, contentPane, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}	

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(null, (Image)null, title, contentPane, settingExtractor, choices);
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(null, (Image)null, title, contentPane, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}

	
	/* ==================================================================== */
	/* ==== Modals                                                     ==== */
	/* ==================================================================== */

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, List<Image> icons, String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		Modal<T> out = new Modal<>(owner);
		out.setModalityType(modality);
		out.setTitle(title);
		if (icons != null)
			out.setIconImages(icons);
		return modal(out, contentPane, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
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
		return modal(owner, icons, title, ModalityType.APPLICATION_MODAL, contentPane, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, Image icon, String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		Modal<T> out = new Modal<>(owner);
		out.setModalityType(modality);
		out.setTitle(title);
		if (icon != null)
			out.setIconImage(icon);
		return modal(out, contentPane, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
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
		return modal(owner, icon, title, ModalityType.APPLICATION_MODAL, contentPane, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(owner, (Image)null, title, modality, contentPane, choices);
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
		return modal(owner, (Image)null, title, ModalityType.APPLICATION_MODAL, contentPane, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(List<Image> icons, String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(null, icons, title, modality, contentPane, choices);
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
		return modal(null, icons, title, ModalityType.APPLICATION_MODAL, contentPane, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Image icon, String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(null, icon, title, modality, contentPane, choices);
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
		return modal(null, icon, title, ModalityType.APPLICATION_MODAL, contentPane, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal((Container)null, title, modality, contentPane, choices);
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
		return modal((Container)null, title, ModalityType.APPLICATION_MODAL, contentPane, choices);
	}

	@SafeVarargs
	private static <T> Modal<T> modal(final Modal<T> modal, final Container contentPane, final ModalChoice<T> ... choices)
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
		
		Node[] modalSections;
		Node contentNode = node(BorderLayout.CENTER, containerOf(createEmptyBorder(4, 4, 4, 4), 
			node(contentPane)
		));
		
		if (nodes.length > 0) 
		{
			modalSections = new Node[]{
				contentNode,
				node(BorderLayout.SOUTH, containerOf(createEmptyBorder(4, 4, 4, 4), new FlowLayout(FlowLayout.TRAILING, 4, 0), 
					nodes
				))
			};
		}
		else
		{
			modalSections = new Node[]{contentNode};
		}
		
		modal.setContentPane(containerOf(createEmptyBorder(4, 4, 4, 4), modalSections));
		modal.setLocationByPlatform(true);
		modal.pack();
		modal.setMinimumSize(modal.getSize());
		modal.setResizable(false);
		modal.setLocationRelativeTo(modal.getOwner());
		modal.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		return modal;
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
		return choice(null, label, mnemonic, onClick);
	}

	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, Supplier<T> onClick)
	{
		return choice(icon, null, 0, onClick);
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
		return choice(null, label, 0, onClick);
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
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, T result)
	{
		return choice(icon, null, 0, () -> result);
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
		return choice(null, label, mnemonic, () -> null);
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
		return choice(null, label, 0, () -> null);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon)
	{
		return choice(icon, null, 0, () -> null);
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
