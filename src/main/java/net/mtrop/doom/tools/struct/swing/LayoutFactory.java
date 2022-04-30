/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;


/**
 * A layout-generating class.
 * This is more for code aesthetics than anything else - the "new" keyword gets ugly in the middle of function calls.
 * @author Matthew Tropiano
 */
public final class LayoutFactory
{
	private LayoutFactory() {}
	
	/* ==================================================================== */
	/* ==== Enums                                                      ==== */
	/* ==================================================================== */

	/**
	 * Flow direction enumeration.
	 */
	public enum Flow
	{
		CENTER(FlowLayout.CENTER),
		LEFT(FlowLayout.LEFT),
		RIGHT(FlowLayout.RIGHT),
		LEADING(FlowLayout.LEADING),
		TRAILING(FlowLayout.TRAILING);
		
		private final int swingId;
		
		private Flow(int swingId)
		{
			this.swingId = swingId;
		}
	}
	
	/**
	 * Box axis enumeration.
	 */
	public enum BoxAxis
	{
		X_AXIS(BoxLayout.X_AXIS),
		Y_AXIS(BoxLayout.Y_AXIS),
		LINE_AXIS(BoxLayout.LINE_AXIS),
		PAGE_AXIS(BoxLayout.PAGE_AXIS);
		
		private final int swingId;
		
		private BoxAxis(int swingId)
		{
			this.swingId = swingId;
		}
	}

	
	/* ==================================================================== */
	/* ==== Layouts                                                    ==== */
	/* ==================================================================== */

	/**
	 * Creates a new BorderLayout with pixel gaps in between.
	 * @param hgap the horizontal gap.
	 * @param vgap the vertical gap.
	 * @return the new layout.
	 * @see BorderLayout
	 */
	public static BorderLayout borderLayout(int hgap, int vgap)
	{
		return new BorderLayout(hgap, vgap);
	}
	
	/**
	 * Creates a new BorderLayout with no gaps.
	 * @return the new layout.
	 * @see BorderLayout
	 */
	public static BorderLayout borderLayout()
	{
		return borderLayout(0, 0);
	}

	/**
	 * Creates a new GridLayout with pixel gaps in between each cell.
	 * @param rows the maximum rows.
	 * @param cols the maximum columns. 
	 * @param hgap the horizontal gap.
	 * @param vgap the vertical gap.
	 * @return the new layout.
	 * @see GridLayout
	 */
	public static GridLayout gridLayout(int rows, int cols, int hgap, int vgap)
	{
		return new GridLayout(rows, cols, hgap, vgap);
	}
	
	/**
	 * Creates a new GridLayout with no pixel gaps in between each cell.
	 * @param rows the maximum rows.
	 * @param cols the maximum columns. 
	 * @return the new layout.
	 * @see GridLayout
	 */
	public static GridLayout gridLayout(int rows, int cols)
	{
		return gridLayout(rows, cols, 0, 0);
	}
	
	/**
	 * Creates a new FlowLayout with pixel gaps in between each component.
	 * @param flow the flow direction.
	 * @param hgap the horizontal gap.
	 * @param vgap the vertical gap.
	 * @return the new layout.
	 * @see FlowLayout
	 */
	public static FlowLayout flowLayout(Flow flow, int hgap, int vgap)
	{
		return new FlowLayout(flow.swingId, hgap, vgap);
	}
	
	/**
	 * Creates a new FlowLayout with LEADING flow, and pixel gaps in between each component.
	 * @param hgap the horizontal gap.
	 * @param vgap the vertical gap.
	 * @return the new layout.
	 * @see FlowLayout
	 */
	public static FlowLayout flowLayout(int hgap, int vgap)
	{
		return flowLayout(Flow.LEADING, hgap, vgap);
	}
	
	/**
	 * Creates a new FlowLayout with no pixel gaps in between each component.
	 * @param flow the flow direction.
	 * @return the new layout.
	 * @see FlowLayout
	 */
	public static FlowLayout flowLayout(Flow flow)
	{
		return flowLayout(flow, 0, 0);
	}
	
	/**
	 * Creates a new FlowLayout with LEADING flow, and no pixel gaps in between each component.
	 * @return the new layout.
	 * @see FlowLayout
	 */
	public static FlowLayout flowLayout()
	{
		return flowLayout(Flow.LEADING);
	}
	
	/**
	 * Creates a new CardLayout with pixel gaps on each side.
	 * @param hgap the horizontal gap.
	 * @param vgap the vertical gap.
	 * @return the new layout.
	 * @see CardLayout
	 */
	public static CardLayout cardLayout(int hgap, int vgap)
	{
		return new CardLayout(hgap, vgap);
	}
	
	/**
	 * Creates a new CardLayout with pixel gaps on each side.
	 * @return the new layout.
	 * @see CardLayout
	 */
	public static CardLayout cardLayout()
	{
		return cardLayout(0, 0);
	}
	
	/**
	 * Creates a new GridBagLayout.
	 * @return the new layout.
	 * @see GridBagLayout
	 */
	public static GridBagLayout gridBagLayout()
	{
		return new GridBagLayout();
	}

	/**
	 * Creates a new BoxLayout.
	 * @param parent the parent component using the layout.
	 * @param axis the layout axis.
	 * @return the new layout.
	 * @see BoxLayout
	 */
	public static BoxLayout boxLayout(Container parent, BoxAxis axis)
	{
		return new BoxLayout(parent, axis.swingId);
	}
	
	/**
	 * Creates a new GroupLayout preloaded with groups.
	 * @param parent the parent component using the layout.
	 * @param horizontalGroup the horizontal group.
	 * @param verticalGroup the vertical group.
	 * @return the new layout.
	 * @see GroupLayout
	 */
	public static GroupLayout groupLayout(Container parent, Group horizontalGroup, Group verticalGroup)
	{
		GroupLayout out = new GroupLayout(parent);
		out.setHorizontalGroup(horizontalGroup);
		out.setVerticalGroup(verticalGroup);
		return out;
	}

	/**
	 * Creates a new GroupLayout.
	 * @param parent the parent component using the layout.
	 * @return the new layout.
	 * @see GroupLayout
	 */
	public static GroupLayout groupLayout(Container parent)
	{
		return new GroupLayout(parent);
	}

}
