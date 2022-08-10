/*******************************************************************************
 * Copyright (c) 2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A daemon watch service thread that calls processing functions when a directory's contents change.
 * @author Matthew Tropiano
 */
public class WatchServiceThread extends Thread
{
	private File directory;
	private Consumer<File> onFileCreated;
	private Consumer<File> onFileModified;
	private Consumer<File> onFileDeleted;
	private Consumer<String> onErrorMessage;

	private WatchService service;
	
	/**
	 * Creates the new Watch Service thread.
	 * @param directory the directory to monitor.
	 * @param recursive if true, monitor all subdirectories as well.
	 * @param onFileCreated the function to call on file creation. Can be null.
	 * @param onFileModified the function to call on file modification. Can be null.
	 * @param onFileDeleted the function to call on file deletion. Can be null.
	 * @param onErrorMessage the function to call when an error message is produced. Can be null.
	 */
	public WatchServiceThread(File directory, boolean recursive, Consumer<File> onFileCreated, Consumer<File> onFileModified, Consumer<File> onFileDeleted, Consumer<String> onErrorMessage)
	{
		super("WatchThread-" + directory.getName());
		setDaemon(true);
		this.directory = Objects.requireNonNull(directory);
		if (!directory.isDirectory())
			throw new IllegalArgumentException("input directory is not a directory.");
		
		this.onFileCreated = onFileCreated;
		this.onFileModified = onFileModified;
		this.onFileDeleted = onFileDeleted;
		this.onErrorMessage = onErrorMessage;
		this.service = null;
	}
	
	private void registerDirectory(File d)
	{
		if (service == null)
			return;
		if (!d.isDirectory())
			return;
		Path dirPath = Paths.get(d.getPath());
		try {
			dirPath.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			fireMessage("Could not register directory: %s: %s", d.getPath(), e.getLocalizedMessage());
		}
	}
	
	private void registerSubdirectoriesOf(File d)
	{
		File[] directories = getSubdirectories(d, true, (f) -> !f.isHidden());
		if (directories != null ) for (File subdir : directories)
			registerDirectory(subdir);
	}
	
	/**
	 * Gets a list of subdirectories from a top directory.
	 * @param startDirectory the starting directory.
	 * @param includeTop if true, the output includes the starting directory.
	 * @param dirFilter additional directory filter.
	 * @return an array of subdirectory paths under the top directory.
	 */
	private static File[] getSubdirectories(File startDirectory, boolean includeTop, FileFilter dirFilter)
	{
		if (!startDirectory.isDirectory())
			return null;
		
		List<File> dirs = new LinkedList<>();
		Deque<File> dirQueue = new LinkedList<>();
		dirQueue.add(startDirectory);
		
		if (includeTop)
			dirs.add(startDirectory);
		
		while (!dirQueue.isEmpty())
		{
			File dir = dirQueue.pollFirst();
			File[] files = dir.listFiles((f) -> f.isDirectory());
			for (int i = 0; i < files.length; i++)
			{
				if (dirFilter.accept(files[i]))
				{
					dirQueue.add(files[i]);
					dirs.add(files[i]);
				}
			}
		}
		
		return dirs.toArray(new File[dirs.size()]);
	}

	@Override
	public void run() 
	{
		try {
			service = FileSystems.getDefault().newWatchService();
			registerDirectory(directory);
			registerSubdirectoriesOf(directory);
		} catch (UnsupportedOperationException e) {
			throw new RuntimeException("Could not start filesystem monitor: unsupported by platform.", e);
		} catch (IOException e) {
			throw new RuntimeException("Could not start filesystem monitor.", e);
		}
		
		try {
			WatchKey key;
			while ((key = service.take()) != null)
			{
				for (WatchEvent<?> event : key.pollEvents())
				{
					Path p = ((Path)key.watchable()).resolve(((Path)event.context()));
					File f = p.toFile();
					switch (event.kind().name())
					{
						default:
							break;
						case "ENTRY_CREATE":
							if (onFileCreated != null)
								onFileCreated.accept(f);
							break;
						case "ENTRY_MODIFY":
							if (onFileModified != null)
								onFileModified.accept(f);
							break;
						case "ENTRY_DELETE":
							if (onFileDeleted != null)
								onFileDeleted.accept(f);
							break;
					}
				}
				key.reset();
			}
		} catch (ClosedWatchServiceException e) {
			fireMessage("Service was closed unexpectedly.");
		} catch (InterruptedException e) {
			fireMessage("Service wait interrupted.");
		} finally {
			if (service != null)
				try {
					service.close();
				} catch (IOException e) {
					// Do nothing.
				}
		}
	}

	private void fireMessage(String format, Object ... args)
	{
		if (onErrorMessage != null)
			onErrorMessage.accept(String.format(format, args));
	}
	
}
