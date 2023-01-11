/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Filled with Clipboard functions.
 * @author Matthew Tropiano
 */
public final class ClipboardUtils
{
	/** Toolkit instance. */
	private static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();

	private static final ClipboardOwner BLANK_OWNER = (clipboard, contents) -> {}; 
	
	private ClipboardUtils() {}
	
	/**
	 * Sends a string to the system clipboard.
	 * @param data the string to send.
	 * @throws HeadlessException if <code>GraphicsEnvironment.isHeadless()</code> returns true.
	 * @throws IllegalStateException  if the system clipboard is not available.
	 */
	public static void sendStringToClipboard(String data)
	{
		sendStringToClipboard(data, BLANK_OWNER);
	}

	/**
	 * Sends a string to the system clipboard.
	 * @param data the string to send.
	 * @param ownerLossFunction the function to call when this program loses the clipboard content ownership.
	 * @throws HeadlessException if <code>GraphicsEnvironment.isHeadless()</code> returns true.
	 * @throws IllegalStateException  if the system clipboard is not available.
	 */
	public static void sendStringToClipboard(String data, ClipboardOwner ownerLossFunction)
	{
		TOOLKIT.getSystemClipboard().setContents(new StringTransferable(data), ownerLossFunction);
	}

	/**
	 * Sends an image to the system clipboard.
	 * @param image the image to send.
	 * @throws HeadlessException if <code>GraphicsEnvironment.isHeadless()</code> returns true.
	 * @throws IllegalStateException  if the system clipboard is not available.
	 */
	public static void sendImageToClipboard(Image image)
	{
		sendImageToClipboard(image, BLANK_OWNER);
	}

	/**
	 * Sends an image to the system clipboard.
	 * @param image the image to send.
	 * @param ownerLossFunction the function to call when this program loses the clipboard content ownership.
	 * @throws HeadlessException if <code>GraphicsEnvironment.isHeadless()</code> returns true.
	 * @throws IllegalStateException  if the system clipboard is not available.
	 */
	public static void sendImageToClipboard(Image image, ClipboardOwner ownerLossFunction)
	{
		TOOLKIT.getSystemClipboard().setContents(new ImageTransferable(image), ownerLossFunction);
	}

	/**
	 * Sends a list of files to the system clipboard.
	 * @param files the file references to send.
	 * @throws HeadlessException if <code>GraphicsEnvironment.isHeadless()</code> returns true.
	 * @throws IllegalStateException  if the system clipboard is not available.
	 */
	public static void sendFilesToClipboard(File[] files)
	{
		sendFilesToClipboard(files, BLANK_OWNER);
	}

	/**
	 * Sends a list of files to the system clipboard.
	 * @param files the file references to send.
	 * @param ownerLossFunction the function to call when this program loses the clipboard content ownership.
	 * @throws HeadlessException if <code>GraphicsEnvironment.isHeadless()</code> returns true.
	 * @throws IllegalStateException  if the system clipboard is not available.
	 */
	public static void sendFilesToClipboard(File[] files, ClipboardOwner ownerLossFunction)
	{
		TOOLKIT.getSystemClipboard().setContents(new FileListTransferable(Arrays.asList(files)), ownerLossFunction);
	}

	/**
	 * Gets a string from the system clipboard.
	 * If the clipboard does not contain a string, this returns null.
	 * @return the string in the clipboard, or null if not a string.
	 * @throws IllegalStateException  if the system clipboard is not available.
	 */
	public static String getStringFromClipboard()
	{
		Transferable content = TOOLKIT.getSystemClipboard().getContents(null);
		if (content.isDataFlavorSupported(DataFlavor.stringFlavor))
		{
			try {
				return (String)content.getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException | IOException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Gets an image from the system clipboard.
	 * If the clipboard does not contain an image, this returns null.
	 * @return the image in the clipboard, or null if not an image.
	 * @throws IllegalStateException  if the system clipboard is not available.
	 */
	public static Image getImageFromClipboard()
	{
		Transferable content = TOOLKIT.getSystemClipboard().getContents(null);
		if (content.isDataFlavorSupported(DataFlavor.imageFlavor))
		{
			try {
				return (Image)content.getTransferData(DataFlavor.imageFlavor);
			} catch (UnsupportedFlavorException | IOException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Gets a list of files from the system clipboard.
	 * If the clipboard does not contain files, this returns null.
	 * @return the file list in the clipboard, or null if not files.
	 * @throws IllegalStateException  if the system clipboard is not available.
	 */
	public static File[] getFilesFromClipboard()
	{
		Transferable content = TOOLKIT.getSystemClipboard().getContents(null);
		if (content.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			try {
				@SuppressWarnings("unchecked")
				List<File> fileList = (List<File>)content.getTransferData(DataFlavor.javaFileListFlavor);
				return fileList.toArray(new File[fileList.size()]);
			} catch (UnsupportedFlavorException | IOException e) {
				return null;
			}
		}
		return null;
	}

	private static class StringTransferable implements Transferable
	{
		private static final DataFlavor[] FLAVORS = new DataFlavor[]{ DataFlavor.stringFlavor };
		
		private String data;
		
		private StringTransferable(String data)
		{
			this.data = data;
		}
		
		@Override
		public DataFlavor[] getTransferDataFlavors() 
		{
			return FLAVORS;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) 
		{
			return FLAVORS[0].equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException 
		{
			if (DataFlavor.stringFlavor.equals(flavor))
				return data;
			else
				return null;
		}
	}
	
	private static class ImageTransferable implements Transferable
	{
		private static final DataFlavor[] FLAVORS = new DataFlavor[]{ DataFlavor.imageFlavor, DataFlavor.stringFlavor };
		
		private Image data;
		private String imageString;

		private ImageTransferable(Image data)
		{
			this.data = data;
			this.imageString = "Image[width="+ data.getWidth(null) + ", height=" + data.getHeight(null) + "]";
		}
		
		@Override
		public DataFlavor[] getTransferDataFlavors() 
		{
			return FLAVORS;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) 
		{
			return FLAVORS[0].equals(flavor)
				|| FLAVORS[1].equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException 
		{
			if (DataFlavor.imageFlavor.equals(flavor))
				return data;
			else if (DataFlavor.stringFlavor.equals(flavor))
				return imageString;
			else
				return null;
		}
	}
	
	private static class FileListTransferable implements Transferable
	{
		private static final DataFlavor[] FLAVORS = new DataFlavor[]{ DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor };
		
		private List<File> fileList;
		private String fileListString;
		
		private FileListTransferable(List<File> fileList)
		{
			this.fileList = fileList;
			StringBuilder sb = new StringBuilder();
			for (File file : fileList)
				sb.append(file.getPath()).append("\n");
			this.fileListString = sb.toString();
		}
		
		@Override
		public DataFlavor[] getTransferDataFlavors() 
		{
			return FLAVORS;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) 
		{
			return FLAVORS[0].equals(flavor)
				|| FLAVORS[1].equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException 
		{
			if (DataFlavor.javaFileListFlavor.equals(flavor))
				return fileList;
			else if (DataFlavor.stringFlavor.equals(flavor))
				return fileListString;
			else
				return null;
		}
	}
	
}
