/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Filled with common Swing functions and calls for convenience.
 * @author Matthew Tropiano
 */
public final class SwingUtils
{
	private SwingUtils() {}
	
	/** Desktop instance. */
	private static final Desktop DESKTOP;

	static
	{
		if (Desktop.isDesktopSupported());
			DESKTOP = Desktop.getDesktop();
	}

	/**
	 * Sets the system look and feel and returns it.
	 * If this is not possible, this returns null. 
	 * Otherwise, this sets the system look and feel and returns <code>UIManager.getLookAndFeel()</code>.
	 * @return the Look and Feel if set, null if not.
	 */
	public static LookAndFeel setSystemLAF()
	{
		return setLAF(UIManager.getSystemLookAndFeelClassName());
	}
	
	/**
	 * Sets the cross-platform default look and feel and returns it.
	 * If this is not possible, this returns null. 
	 * Otherwise, this sets the cross-platform default look and feel and returns <code>UIManager.getLookAndFeel()</code>.
	 * @return the Look and Feel if set, null if not.
	 */
	public static LookAndFeel setCrossPlatformLAF()
	{
		return setLAF(UIManager.getCrossPlatformLookAndFeelClassName());
	}
	
	/**
	 * Sets a look and feel and returns it.
	 * If this is not possible, this returns null. 
	 * Otherwise, this sets the provided look and feel and returns <code>UIManager.getLookAndFeel()</code>.
	 * @param className the Look and Feel classname.
	 * @return the Look and Feel if set, null if not.
	 */
	public static LookAndFeel setLAF(String className)
	{
		try {
			UIManager.setLookAndFeel(className);
		} catch (ClassNotFoundException e) {
			return null;
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (UnsupportedLookAndFeelException e) {
			return null;
		}
		return UIManager.getLookAndFeel();
	}
	
	/**
	 * Sets a look and feel and returns it.
	 * If this is not possible, this returns null. 
	 * Otherwise, this sets the provided look and feel and returns <code>UIManager.getLookAndFeel()</code>.
	 * @param laf the Look and Feel.
	 * @return the Look and Feel if set, null if not.
	 */
	public static LookAndFeel setLAF(LookAndFeel laf)
	{
		try {
			UIManager.setLookAndFeel(laf);
		} catch (UnsupportedLookAndFeelException e) {
			return null;
		}
		return UIManager.getLookAndFeel();
	}
	
	/**
	 * Convenience for {@link SwingUtilities#invokeLater(Runnable)}, so it is only one import.
	 * @param runnable the runnable to call later in the Swing event cue.
	 * @see SwingUtilities#invokeLater(Runnable)
	 */
	public static void invoke(Runnable runnable)
	{
		SwingUtilities.invokeLater(runnable);
	}
	
	/**
	 * Convenience for {@link SwingUtilities#invokeAndWait(Runnable)}, so it is only one import.
	 * This will wait for the runnable to be called.
	 * @param runnable the runnable to call later in the Swing event cue.
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 * @see SwingUtilities#invokeAndWait(Runnable)
	 */
	public static void invokeAndWait(Runnable runnable) throws InvocationTargetException, InterruptedException
	{
		SwingUtilities.invokeAndWait(runnable);
	}
	
	/**
	 * Show an alert window.
	 * @param parent Parent component of this dialog.
	 * @param message The message to show.
	 */
	public static void error(Component parent, String message)
	{
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(
			    parent, message, "Alert",
			    JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Show a warning window.
	 * @param parent Parent component of this dialog.
	 * @param message The message to show.
	 */
	public static void warning(Component parent, String message)
	{
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(
			    parent, message, "Warning",
			    JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Show an info window.
	 * @param message The message to show.
	 * @param parent Parent component of this dialog.
	 */
	public static void info(Component parent, String message)
	{
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(
			    parent, message, "Info",
			    JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Displays a confirmation window asking a user a yes or no question.
	 * @param message the message to show.
	 * @param parent Parent component of this dialog.
	 * @return true if "yes" was clicked. false otherwise.
	 */
	public static boolean yesTo(Component parent, String message)
	{
		int c = JOptionPane.showConfirmDialog(
			    parent, 
			    message,
			    "Confirm",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.QUESTION_MESSAGE);
		boolean out = c == JOptionPane.YES_OPTION;
		return out;
	}

	/**
	 * Displays a confirmation window asking a user a yes or no question.
	 * This is a convenience method for code readability, and is completely
	 * equivalent to <code>!yesTo(message,parent)</code>.
	 * @param message the message to show.
	 * @param parent Parent component of this dialog.
	 * @return true if "no" was clicked. false otherwise.
	 */
	public static boolean noTo(Component parent, String message)
	{
		return !yesTo(parent, message);
	}

	/**
	 * Show an alert window.
	 * @param message The message to show.
	 */
	public static void error(String message)
	{
		error(null, message);
	}

	/**
	 * Show an warning window.
	 * @param message The message to show.
	 */
	public static void warning(String message)
	{
		warning(null, message);
	}

	/**
	 * Show an info window.
	 * @param message The message to show.
	 */
	public static void info(String message)
	{
		info(null, message);
	}

	/**
	 * Displays a confirmation window asking a user a yes or no question.
	 * @param message the message to show.
	 * @return true if "yes" was clicked. false otherwise.
	 */
	public static boolean yesTo(String message)
	{
		return yesTo(null, message);
	}

	/**
	 * Displays a confirmation window asking a user a yes or no question.
	 * This is a convenience method for code readability, and is completely
	 * equivalent to <code>!yesTo(message)</code>.
	 * @param message the message to show.
	 * @return true if "no" was clicked. false otherwise.
	 */
	public static boolean noTo(String message)
	{
		return !yesTo(message);
	}

	/**
	 * Attempts to open a file using the default associated opening program.
	 * Returns false if unsuccessful, true otherwise.
	 * @param file the file to open.
	 * @return true if the file was opened, false if not.
	 * @throws IOException if the file could not be opened for some reason.
	 */
	public static boolean open(File file) throws IOException
	{
		if (DESKTOP != null && DESKTOP.isSupported(Desktop.Action.OPEN))
		{
			DESKTOP.open(file);
			return true;
		}
		return false;
	}

	/**
	 * Attempts to open a location (usually a web browser) for a URI.
	 * Returns false if unsuccessful, true otherwise.
	 * @param uri the URI to browse to.
	 * @return true if the URI was opened, false if not.
	 * @throws IOException if the URI could not be opened for some reason.
	 */
	public static boolean browse(URI uri) throws IOException
	{
		if (DESKTOP != null && DESKTOP.isSupported(Desktop.Action.BROWSE))
		{
			DESKTOP.browse(uri);
			return true;
		}
		return false;
	}

	/**
	 * Attempts to open a mail client for a "mailto" URI.
	 * Returns false if unsuccessful, true otherwise.
	 * @param uri the URI to open for mail.
	 * @return true if the URI was opened, false if not.
	 * @throws IOException if the URI could not be opened for some reason.
	 */
	public static boolean mail(URI uri) throws IOException
	{
		if (DESKTOP != null && DESKTOP.isSupported(Desktop.Action.MAIL))
		{
			DESKTOP.mail(uri);
			return true;
		}
		return false;
	}
	
}
