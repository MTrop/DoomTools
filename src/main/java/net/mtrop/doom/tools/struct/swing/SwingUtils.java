/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
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
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

/**
 * Filled with common Swing functions and calls for convenience.
 * @author Matthew Tropiano
 */
public final class SwingUtils
{
	private SwingUtils() {}
	
	/** Desktop instance. */
	private static Desktop desktopInstance;

	static
	{
		if (Desktop.isDesktopSupported());
			desktopInstance = Desktop.getDesktop();
	}

	/**
	 * Apply function for objects.
	 * @param input the input object to manipulate.
	 * @param applier the function to pass the input element to.
	 * @param <T> the return/input type.
	 * @return the input object.
	 */
	public static <T> T apply(T input, Consumer<T> applier)
	{
		applier.accept(input);
		return input;
	}
	
	/**
	 * Sets the system look and feel.
	 * If this is not possible, this returns false. 
	 * Otherwise, this sets the system look and feel and returns true.
	 * @return true if set, false if not.
	 */
	public static boolean setSystemLAF()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			return false;
		} catch (InstantiationException e) {
			return false;
		} catch (IllegalAccessException e) {
			return false;
		} catch (UnsupportedLookAndFeelException e) {
			return false;
		}
		return true;
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
	 * Opens a directory chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param initPath the initial path for the directory chooser.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected directory, or null if no directory was selected for whatever reason.
	 */
	public static File directory(Component parent, String title, File initPath, String approveText, FileFilter ... choosableFilters)
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (initPath != null)
			jfc.setSelectedFile(initPath);
		if (title != null)
			jfc.setDialogTitle(title);
		for (FileFilter filter : choosableFilters)
			jfc.addChoosableFileFilter(filter);
		switch (jfc.showDialog(parent, approveText))
		{
			default:
			case JFileChooser.CANCEL_OPTION: 
			case JFileChooser.ERROR_OPTION:
				return null;
			case JFileChooser.APPROVE_OPTION:
				return jfc.getSelectedFile();
		}
	}

	/**
	 * Opens a directory chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected directory, or null if no directory was selected for whatever reason.
	 */
	public static File directory(Component parent, String title, String approveText, FileFilter ... choosableFilters)
	{
		return directory(parent, title, null, approveText, choosableFilters);
	}

	/**
	 * Opens a directory chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param initPath the initial path for the directory chooser.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected directory, or null if no directory was selected for whatever reason.
	 */
	public static File directory(Component parent, File initPath, String approveText, FileFilter ... choosableFilters)
	{
		return directory(parent, null, initPath, approveText, choosableFilters);
	}

	/**
	 * Opens a directory chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected directory, or null if no directory was selected for whatever reason.
	 */
	public static File directory(Component parent, String approveText, FileFilter ... choosableFilters)
	{
		return directory(parent, null, null, approveText, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File file(Component parent, String title, File initPath, String approveText, FileFilter ... choosableFilters)
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (initPath != null)
			jfc.setSelectedFile(initPath);
		if (title != null)
			jfc.setDialogTitle(title);
		for (FileFilter filter : choosableFilters)
			jfc.addChoosableFileFilter(filter);
		switch (jfc.showDialog(parent, approveText))
		{
			default:
			case JFileChooser.CANCEL_OPTION: 
			case JFileChooser.ERROR_OPTION:
				return null;
			case JFileChooser.APPROVE_OPTION:
				return jfc.getSelectedFile();
		}
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File file(Component parent, String title, String approveText, FileFilter ... choosableFilters)
	{
		return file(parent, title, null, approveText, choosableFilters);
	}
	
	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File file(Component parent, File initPath, String approveText, FileFilter ... choosableFilters)
	{
		return file(parent, null, initPath, approveText, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param parent the parent component for the chooser modal.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File file(Component parent, String approveText, FileFilter ... choosableFilters)
	{
		return file(parent, null, null, approveText, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param title the dialog title.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File file(String title, File initPath, String approveText, FileFilter ... choosableFilters)
	{
		return file(null, title, initPath, approveText, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File file(String title, String approveText, FileFilter ... choosableFilters)
	{
		return file(null, title, null, approveText, choosableFilters);
	}
	
	/**
	 * Opens a file chooser dialog.
	 * @param title the dialog title.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File file(String title, FileFilter ... choosableFilters)
	{
		return file(null, title, null, null, choosableFilters);
	}
	
	/**
	 * Opens a file chooser dialog.
	 * @param initPath the initial path for the file chooser.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File file(File initPath, String approveText, FileFilter ... choosableFilters)
	{
		return file(null, null, initPath, approveText, choosableFilters);
	}

	/**
	 * Opens a file chooser dialog.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public static File file(FileFilter ... choosableFilters)
	{
		return file(null, null, null, null, choosableFilters);
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
		if (desktopInstance != null && desktopInstance.isSupported(Desktop.Action.OPEN))
		{
			desktopInstance.open(file);
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
		if (desktopInstance != null && desktopInstance.isSupported(Desktop.Action.BROWSE))
		{
			desktopInstance.browse(uri);
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
		if (desktopInstance != null && desktopInstance.isSupported(Desktop.Action.MAIL))
		{
			desktopInstance.mail(uri);
			return true;
		}
		return false;
	}
	
}
