package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A progress panel that displays a message and a progress bar.
 * @author Matthew Tropiano
 */
public class ProgressPanel extends JPanel 
{
	private static final long serialVersionUID = 4039975713920529279L;
	
	private StatusPanel statusPanel;
	private JProgressBar progressBar;
	private JLabel progressLabel;
	
	/**
	 * Creates a progress panel.
	 * @param progressLabelWidth width of the progress label.
	 */
	public ProgressPanel(int progressLabelWidth)
	{
		this.statusPanel = new StatusPanel();
		this.progressBar = progressBar();
		this.progressLabel = SwingUtils.apply(label(JLabel.TRAILING, ""), 
			(label) -> label.setPreferredSize(new Dimension(progressLabelWidth, 32))
		);
		containerOf(this, new BorderLayout(8, 4),
			node(BorderLayout.NORTH, statusPanel),
			node(BorderLayout.CENTER, progressBar),
			node(BorderLayout.EAST, progressLabel)
		);
	}
	
	/**
	 * Sets a message (no icon).
	 * @param message the message to set.
	 */
	public void setMessage(String message)
	{
		statusPanel.setMessage(message);
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
	
	/**
	 * Sets the progress label value.
	 * @param label the new value.
	 */
	public void setProgressLabel(String label)
	{
		SwingUtils.invoke(() -> {
			progressLabel.setText(label);
		});
	}

	/**
	 * Sets the progress bar values.
	 * @param min the minimum value.
	 * @param current the current value.
	 * @param max the maximum value.
	 */
	public void setProgress(int min, int current, int max)
	{
		SwingUtils.invoke(() -> {
			progressBar.setIndeterminate(false);
			progressBar.setMinimum(min);
			progressBar.setValue(current);
			progressBar.setMaximum(max);
		});
	}
	
	/**
	 * Sets the progress bar to the indeterminate state.
	 */
	public void setIndeterminate()
	{
		SwingUtils.invoke(() -> {
			progressBar.setIndeterminate(true);
		});
	}
	
}
