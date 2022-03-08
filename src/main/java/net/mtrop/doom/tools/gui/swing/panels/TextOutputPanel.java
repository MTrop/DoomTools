package net.mtrop.doom.tools.gui.swing.panels;

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

	private static final Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 12);
	
	/**
	 * Creates a new output panel.
	 */
	public TextOutputPanel()
	{
		super(25, 80);
		setFont(DEFAULT_FONT);
		setEditable(false);
	}

	/**
	 * Writes a character to this panel.
	 * @param c the character.
	 */
	public void writeChar(char c)
	{
		append(String.valueOf(c));
		setCaretPosition(getDocument().getLength());
	}
	
	/**
	 * @return an output stream to use for printing to the text area.
	 */
	public OutputStream getOutputStream()
	{
		return new Printer();
	}
	
	/**
	 * @return a print stream to use for printing to the text area.
	 */
	public PrintStream getPrintStream()
	{
		return new PrintStream(new Printer(), true);
	}
	
	public class Printer extends OutputStream
	{
		@Override
		public void close() throws IOException
		{
			// Do nothing.
		}
		
		@Override
		public void write(int b) throws IOException 
		{
			writeChar((char)b);
		}
		
	}
	
}
