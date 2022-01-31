package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.mtrop.doom.tools.gui.DoomToolsImageManager;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.icon;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.containerOf;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.node;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.scroll;

import java.awt.BorderLayout;

/**
 * A message panel that shows a status icon and a message.
 * @author Matthew Tropiano
 */
public class StatusPanel extends JPanel
{
	private static final long serialVersionUID = -3730984456173494660L;

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

	public StatusPanel()
	{
		this.images = DoomToolsImageManager.get();
		this.messageLabel = new JLabel();
		this.successIcon = icon(images.getImage("success.png"));
		this.activityIcon = icon(images.getImage("activity.gif"));
		this.errorIcon = icon(images.getImage("error.png"));
		
		containerOf(this, new BorderLayout(0, 4), 
			node(BorderLayout.CENTER, scroll(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, messageLabel))
		);
	}
	
	/**
	 * Sets the success icon and a message.
	 * @param message the message to set.
	 */
	public void setSuccessMessage(String message)
	{
		messageLabel.setIcon(successIcon);
		messageLabel.setText(message);
	}
	
	/**
	 * Sets the activity icon and a message.
	 * @param message the message to set.
	 */
	public void setActivityMessage(String message)
	{
		messageLabel.setIcon(activityIcon);
		messageLabel.setText(message);
	}
	
	/**
	 * Sets the error icon and a message.
	 * @param message the message to set.
	 */
	public void setErrorMessage(String message)
	{
		messageLabel.setIcon(errorIcon);
		messageLabel.setText(message);
	}
	
}
