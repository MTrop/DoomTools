package net.mtrop.doom.tools.doommake.gui.swing;

import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;

/**
 * Text output panel.
 * @author Matthew Tropiano
 */
public class TextOutputPanel extends JTextArea
{
	private static final long serialVersionUID = -1405465151452714437L;

	/**
	 * Creates a new output panel.
	 */
	public TextOutputPanel()
	{
		super(25, 80);
		setFont(new Font("Lucida Console", Font.PLAIN, 16));
		setEditable(false);
	}

	/**
	 * @return a print stream to use for printing to the text area.
	 */
	public PrintStream getPrintStream()
	{
		return new PrintStream(new Printer());
	}
	
	public class Printer extends OutputStream
	{
		@Override
		public void close() throws IOException
		{
			// Do nothing.
		}
		
		@Override
		public void flush() throws IOException 
		{
			// Do nothing.
		}
		
		@Override
		public void write(int b) throws IOException 
		{
			append(String.valueOf((char)b));
			setCaretPosition(getDocument().getLength());
		}
		
	}
	
}
