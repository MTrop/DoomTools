package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.io.PrintStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

/**
 * A Swing panel that tracks a process or a call to DoomTools.
 * @author Matthew Tropiano
 */
public class DoomToolsProcessPanel extends JPanel
{
	private static final long serialVersionUID = 3116234535666493956L;
	
	/** Text output panel. */
	private TextOutputPanel textOutputPanel;
	/** Message label. */
	private StatusPanel statusPanel;

	public DoomToolsProcessPanel()
	{
		this.textOutputPanel = new TextOutputPanel();
		this.statusPanel = new StatusPanel();
		
		containerOf(this, new BorderLayout(0, 4), 
			node(BorderLayout.CENTER, scroll(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, textOutputPanel)),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}
	
	/**
	 * @return the print stream for printing stuff to the text panel.
	 */
	public PrintStream getPrintStream()
	{
		return textOutputPanel.getPrintStream();
	}
	
	/**
	 * Sets the success icon and a message.
	 * @param message the message to set.
	 */
	public void setSuccessMessage(String message)
	{
		statusPanel.setSuccessMessage(message);
	}
	
	/**
	 * Sets the activity icon and a message.
	 * @param message the message to set.
	 */
	public void setActivityMessage(String message)
	{
		statusPanel.setActivityMessage(message);
	}
	
	/**
	 * Sets the error icon and a message.
	 * @param message the message to set.
	 */
	public void setErrorMessage(String message)
	{
		statusPanel.setErrorMessage(message);
	}
	
}
