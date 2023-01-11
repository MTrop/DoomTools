/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsImageManager;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import java.awt.BorderLayout;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

/**
 * A message panel that shows a status icon and a message.
 * @author Matthew Tropiano
 */
public class DoomToolsStatusPanel extends JPanel
{
	private static final long serialVersionUID = -3730984456173494660L;

	/** Icon manager. */
	private DoomToolsIconManager icons;
	/** Image manager. */
	private DoomToolsImageManager images;
	
	/** Message label. */
	private JLabel messageLabel;
	
	/** Success icon. */
	private Icon successIcon;
	/** Activity icon. */
	private Icon activityIcon;
	/** Error icon. */
	private Icon errorIcon;

	public DoomToolsStatusPanel()
	{
		this.icons = DoomToolsIconManager.get();
		this.images = DoomToolsImageManager.get();
		this.messageLabel = label(" ");
		this.successIcon = icon(images.getImage("success.png"));
		this.activityIcon = icons.getImage("activity.gif");
		this.errorIcon = icon(images.getImage("error.png"));
		
		this.setBorder(null);
		containerOf(this, borderLayout(0, 4), 
			node(BorderLayout.CENTER, messageLabel)
		);
	}
	
	/**
	 * Sets a message (no icon).
	 * @param message the message to set.
	 */
	public void setMessage(String message)
	{
		SwingUtils.invoke(() -> {
			messageLabel.setIcon(null);
			messageLabel.setText(message);
		});
	}
	
	/**
	 * Sets the success icon and a message.
	 * @param message the message to set.
	 */
	public void setSuccessMessage(String message)
	{
		SwingUtils.invoke(() -> {
			messageLabel.setIcon(successIcon);
			messageLabel.setText(message);
		});
	}
	
	/**
	 * Sets the activity icon and a message.
	 * @param message the message to set.
	 */
	public void setActivityMessage(String message)
	{
		SwingUtils.invoke(() -> {
			messageLabel.setIcon(activityIcon);
			messageLabel.setText(message);
		});
	}
	
	/**
	 * Sets the error icon and a message.
	 * @param message the message to set.
	 */
	public void setErrorMessage(String message)
	{
		SwingUtils.invoke(() -> {
			messageLabel.setIcon(errorIcon);
			messageLabel.setText(message);
		});
	}
	
}
