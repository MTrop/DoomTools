/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.doommake;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.Properties;

import com.blackrook.json.JSONConversionException;
import com.blackrook.json.JSONObject;

import net.mtrop.doom.tools.DoomMakeMain;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;

/**
 * A fancy-schmancy class for listening for changes to a project
 * and kicking off builds when files change. 
 * @author Matthew Tropiano
 */
public class AutoBuildAgent 
{
	/** The project directory. */
	private File projectDirectory;
	/** Agent listener. */
    private Listener listener;
    
    /** Properties. */
    private Properties mergedProperties;
    /** Local Project properties path. */
    private File propertiesPath;
    /** Project properties path. */
    private File projectPropertiesPath;
    /** Source directory. */
    private File sourceDirectory;
    
    /** "Currently building" internal flag. */
    private volatile boolean currentlyBuilding;
    /** Watcher thread. */
    private volatile WatchThread watchThread;
    /** Watcher thread. */
    private volatile BuildThread buildThread;
    
	/**
	 * Creates an auto-build agent class.
	 * @param projectDirectory the project directory to listen to.
	 * @param listener the listener for this agent.
	 */
	public AutoBuildAgent(File projectDirectory, Listener listener)
	{
		this.projectDirectory = Objects.requireNonNull(projectDirectory);
		this.listener = Objects.requireNonNull(listener);

		this.mergedProperties = createProjectProperties();
		this.propertiesPath = new File(projectDirectory.getPath() + File.separator + "doommake.properties");
		this.projectPropertiesPath = new File(projectDirectory.getPath() + File.separator + "doommake.project.properties");
		this.sourceDirectory = DoomMakeMain.getProjectPropertyPath(projectDirectory, mergedProperties, "doommake.dir.src", "src");
		
		this.currentlyBuilding = false;
		this.watchThread = null;
		this.buildThread = null;
	}
	
	/**
	 * Starts the watcher.
	 * @return true if started successfully, false if not.
	 * @throws IllegalStateException if the watcher is already running.
	 */
	public boolean start()
	{
		if (watchThread != null)
			throw new IllegalStateException("Watcher thread is already running!");
		
		watchThread = new WatchThread(projectDirectory);
		try {
			watchThread.start();
		} catch (Exception e) {
			watchThread = null;
			throw e;
		}
		buildThread = new BuildThread(projectDirectory, 1000L);
		buildThread.start();

		try {
			setAgentLock();
		} catch (JSONConversionException e) {
			fireAgentStartupException("Could not set agent lock on project! JSON Parse error: " + e.getLocalizedMessage(), e);
			stopThreads();
			return false;
		} catch (IOException e) {
			fireAgentStartupException("Could not set agent lock on project!", e);
			stopThreads();
			return false;
		}
		
		fireAgentStarted();
		return true;
	}
	
	private void stopThreads()
	{
		if (watchThread != null)
		{
			watchThread.interrupt();
			watchThread = null;
		}
		if (buildThread != null)
		{
			buildThread.interrupt();
			buildThread = null;
		}
	}
	
	/**
	 * @return true if the agent is running, false if not. 
	 */
	public boolean isRunning()
	{
		return watchThread != null;
	}
	
	/**
	 * Ends the watcher.
	 * Does nothing if the watcher is not running.
	 */
	public void shutDown()
	{
		if (!isRunning())
			return;

		stopThreads();

		try {
			unsetAgentLock();
		} catch (JSONConversionException e) {
			fireAgentStoppedException("Could not unset agent lock on project! JSON Parse error: " + e.getLocalizedMessage(), e);
		} catch (IOException e) {
			fireAgentStoppedException("Could not unset agent lock on project! You will need to clear it manually.", e);
		}

		fireAgentStopped();
	}

	// Called when a file is created in a watched directory.
	private void processFileCreation(File file)
	{
		// If a build is running, watched directories may have contents changed during it. Ignore these changes!
		if (currentlyBuilding)
			return;
		
		// Add directories not in the project root.
		if (file.isDirectory() && !FileUtils.filePathEquals(file.getParentFile(), projectDirectory))
			watchThread.registerDirectory(file);
		
		// If source directory was created, add it and its subdirectories.
		if (FileUtils.filePathEquals(file, sourceDirectory))
			watchThread.registerSubdirectoriesOf(file);
		
		fireFileCreate(file);
		buildThread.trigger();
	}
	
	// Called when a file is modified in a watched directory.
	private void processFileModify(File file)
	{
		// If a build is running, watched directories may have contents changed during it. Ignore these changes!
		if (currentlyBuilding)
			return;

		if (FileUtils.filePathEquals(file, propertiesPath) || FileUtils.filePathEquals(file, projectPropertiesPath))
		{
			mergedProperties = createProjectProperties();
			File oldSourceDir = new File(sourceDirectory.getPath());
			sourceDirectory = DoomMakeMain.getProjectPropertyPath(projectDirectory, mergedProperties, "doommake.dir.src", "src");
			if (!FileUtils.filePathEquals(oldSourceDir, sourceDirectory))
				watchThread.registerSubdirectoriesOf(sourceDirectory);
		}
		
		fireFileModify(file);
		buildThread.trigger();
	}
	
	// Called when a file is deleted in a watched directory.
	private void processFileDelete(File file)
	{
		// If a build is running, watched directories may have contents changed. Ignore these changes!
		if (currentlyBuilding)
			return;

		fireFileDelete(file);
		buildThread.trigger();
	}
	
	private void fireAgentStarted()
	{
		if (listener != null)
			listener.onAgentStarted();
	}
	
	private void fireAgentStartupException(String message, Exception e)
	{
		if (listener != null)
			listener.onAgentStartupException(message, e);
	}

	private void fireAgentStopped()
	{
		if (listener != null)
			listener.onAgentStopped();
	}
	
	private void fireAgentStoppedException(String message, Exception e)
	{
		if (listener != null)
			listener.onAgentStoppedException(message, e);
	}

	private int fireCallBuild(String target)
	{
		if (listener != null)
			return listener.callBuild(target);
		return 0;
	}
	
	private void fireBuildPrepared()
	{
		if (listener != null)
			listener.onBuildPrepared();		
	}
	
	private void fireBuildStart()
	{
		if (listener != null)
			listener.onBuildStart();		
	}
	
	private void fireBuildEnd(int result) 
	{
		if (listener != null)
			listener.onBuildEnd(result);		
	}
	
	private void fireFileCreate(File file)
	{
		if (listener != null)
			listener.onFileCreate(file);
	}

	private void fireFileModify(File file)
	{
		if (listener != null)
			listener.onFileModify(file);
	}

	private void fireFileDelete(File file)
	{
		if (listener != null)
			listener.onFileDelete(file);		
	}

	private void fireVerboseMessage(String message)
	{
		if (listener != null)
			listener.onVerboseMessage(message);
	}

	private void fireInfoMessage(String message)
	{
		if (listener != null)
			listener.onInfoMessage(message);
	}

	private void fireWarningMessage(String message)
	{
		if (listener != null)
			listener.onWarningMessage(message);
	}

	private void fireErrorMessage(Throwable t, String message)
	{
		if (listener != null)
			listener.onErrorMessage(t, message);
	}

	private Properties createProjectProperties()
	{
		Properties properties = new Properties();
		File projectPropertiesFile = new File(projectDirectory + File.separator + "doommake.project.properties");
		File propertiesFile = new File(projectDirectory + File.separator + "doommake.properties");
		if (projectPropertiesFile.exists())
			mergeProperties(properties, projectPropertiesFile);
		if (propertiesFile.exists())
			mergeProperties(properties, propertiesFile);
		return properties;
	}

	private void mergeProperties(Properties properties, File projectPropertiesFile) 
	{
		if (projectPropertiesFile.exists()) try (FileInputStream fis = new FileInputStream(projectPropertiesFile)) 
		{
			properties.load(fis);
		} 
		catch (Exception e) 
		{
			fireErrorMessage(e, "Project properties could not be refreshed!");
		}
	}

	/**
	 * Set agent lock.
	 * @param projectDirectory the project directory root.
	 * @param properties the properties to inspect for the lock file name.
	 * @throws IOException if the file could not be opened or written.
	 */
	private void setAgentLock() throws IOException
	{
		JSONObject lockRoot = DoomMakeMain.readLockObject(projectDirectory, mergedProperties);
		lockRoot.addMember(DoomMakeMain.JSON_AGENT_LOCK_KEY, true);
		DoomMakeMain.writeLockObject(projectDirectory, mergedProperties, lockRoot);
	}
	
	/**
	 * Unset agent lock.
	 * @param projectDirectory the project directory root.
	 * @param properties the properties to inspect for the lock file name.
	 * @throws IOException if the file could not be opened or written.
	 */
	private void unsetAgentLock() throws IOException
	{
		JSONObject lockRoot = DoomMakeMain.readLockObject(projectDirectory, mergedProperties);
		lockRoot.addMember(DoomMakeMain.JSON_AGENT_LOCK_KEY, false);
		DoomMakeMain.writeLockObject(projectDirectory, mergedProperties, lockRoot);
	}

	/**
	 * Listener interface for this agent.
	 */
	public interface Listener
	{
		/**
		 * Called to perform the actual build.
		 * @param target the DoomMake target to call.
		 * @return the result from the call.
		 */
		default int callBuild(String target)
		{
			return 0;
		}
		
		/**
		 * Called when the agent is started and ready.
		 */
		default void onAgentStarted()
		{
			// Do nothing.
		}
		
		/**
		 * Called if an exception occurs on agent start.
		 * If this gets called, the agent will not start.
		 * @param message the error message.
		 * @param exception the exception thrown.
		 */
		default void onAgentStartupException(String message, Exception exception)
		{
			// Do nothing.
		}

		/**
		 * Called when the agent is stopped.
		 */
		default void onAgentStopped()
		{
			// Do nothing.
		}

		/**
		 * Called if an exception occurs on agent stop.
		 * @param message the error message.
		 * @param exception the exception thrown.
		 */
		default void onAgentStoppedException(String message, Exception exception)
		{
			// Do nothing.
		}

		/**
		 * Called when a build is <em>going to</em> start (grace period is set to a nonzero duration from 0).
		 */
		default void onBuildPrepared()
		{
			// Do nothing.
		}
		
		/**
		 * Called when a build starts.
		 */
		default void onBuildStart()
		{
			// Do nothing.
		}
		
		/**
		 * Called when a build terminates.
		 * @param result the result of the build (process result). 0 = success.
		 */
		default void onBuildEnd(int result)
		{
			// Do nothing.
		}
		
		/**
		 * Called when a file was created in a watched tree.
		 * This is called after all internal functions get handled.
		 * @param file the created file.
		 */
		default void onFileCreate(File file)
		{
			// Do nothing.
		}
		
		/**
		 * Called when a file was modified in a watched tree.
		 * This is called after all internal functions get handled.
		 * @param file the modified file.
		 */
		default void onFileModify(File file)
		{
			// Do nothing.
		}
		
		/**
		 * Called when a file was deleted in a watched tree.
		 * This is called after all internal functions get handled.
		 * @param file the deleted file.
		 */
		default void onFileDelete(File file)
		{
			// Do nothing.
		}
		
		/**
		 * Called when an extra-verbose message occurs.
		 * @param message the message to print.
		 */
		default void onVerboseMessage(String message)
		{
			// Do nothing.
		}

		/**
		 * Called when a debug message occurs.
		 * @param message the message to print.
		 */
		default void onInfoMessage(String message)
		{
			// Do nothing.
		}

		/**
		 * Called when a debug message occurs.
		 * @param message the message to print.
		 */
		default void onWarningMessage(String message)
		{
			// Do nothing.
		}

		/**
		 * Called when a debug message occurs.
		 * @param t the throwable that was the cause.
		 * @param message the message to print.
		 */
		default void onErrorMessage(Throwable t, String message)
		{
			// Do nothing.
		}
	}
	
	// Project directory watcher thread.
	private class WatchThread extends Thread
	{
		private WatchService service;
		
		private WatchThread(File directory)
		{
			super("WatchThread-" + directory.getName());
			setDaemon(false);
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
				fireVerboseMessage(String.format("Registered directory: %s", d.getAbsolutePath()));
			} catch (IOException e) {
				fireWarningMessage(String.format("Could not register '%s' for auto-build.", d.getAbsolutePath()));
			}
		}
		
		private void registerSubdirectoriesOf(File d)
		{
			File[] directories = FileUtils.getSubdirectories(d, true, (f) -> !f.isHidden());
			if (directories != null ) for (File subdir : directories)
				registerDirectory(subdir);
		}
		
		@Override
		public void run() 
		{
			try {
				service = FileSystems.getDefault().newWatchService();
				registerDirectory(projectDirectory);
				registerSubdirectoriesOf(sourceDirectory);
			} catch (UnsupportedOperationException e) {
				throw new RuntimeException("Could not start filesystem monitor for auto-build: unsupported by platform.", e);
			} catch (IOException e) {
				throw new RuntimeException("Could not start filesystem monitor for auto-build.", e);
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
								processFileCreation(f);
								break;
							case "ENTRY_MODIFY":
								processFileModify(f);
								break;
							case "ENTRY_DELETE":
								processFileDelete(f);
								break;
						}
					}
					key.reset();
				}
			} catch (ClosedWatchServiceException e) {
				fireWarningMessage(String.format("Watch Service for %s closed unexpectedly. Terminating.", projectDirectory.getAbsolutePath()));
			} catch (InterruptedException e) {
				fireVerboseMessage(String.format("Watcher thread for %s interrupted. Terminating.", projectDirectory.getAbsolutePath()));
			} finally {
				IOUtils.close(service);
				fireVerboseMessage("Watcher service closed.");
				fireInfoMessage(String.format("Closed Watcher service for %s", projectDirectory.getAbsolutePath()));
			}
		}
	}
	
	// Auto-build kickoff thread.
	private class BuildThread extends Thread
	{
		private final Object MUTEX = new Object();

		/** Grace period. */
		private long gracePeriodMillis;
		/** Target time. */
		private long targetTime;
		
		private BuildThread(File directory, long gracePeriodMillis)
		{
			super("AutoBuildThread-" + directory.getName());
			setDaemon(false);
			this.gracePeriodMillis = gracePeriodMillis;
			this.targetTime = -1L;
		}
		
		/**
		 * Trigger a build.
		 * Resets the grace period countdown.
		 */
		public void trigger()
		{
			long systime = System.currentTimeMillis();
			if (systime > targetTime)
				fireBuildPrepared();
			targetTime = gracePeriodMillis + systime;
			
			synchronized (MUTEX)
			{
				MUTEX.notify();
			}
		}
		
		@Override
		public void run()
		{
			try {
				while (true)
				{
					long systime = System.currentTimeMillis();
					if (systime < targetTime)
					{
						Thread.sleep(1);
					}
					else if (!isInterrupted())
					{
						build("make");
						synchronized (MUTEX) 
						{
							MUTEX.wait();
						}
					}
				}
			} catch (InterruptedException e) {
				fireVerboseMessage(String.format("Auto-build trigger thread for %s interrupted. Terminating.", projectDirectory.getAbsolutePath()));
			}
		}
		
		/**
		 * Calls the build.
		 */
		private void build(String target)
		{
			currentlyBuilding = true;
			try {
				fireBuildStart();
				int result = -1;
				try {
					result = fireCallBuild(target); 
				} finally {
					fireBuildEnd(result);
				}
			} finally {
				currentlyBuilding = false;
			}
		}
		
	}
	
}
