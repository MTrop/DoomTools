/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.doomtools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.blackrook.json.JSONObject;
import com.blackrook.json.JSONReader;

import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.tools.DoomToolsMain;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPReader;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPRequest;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPResponse;

/**
 * An updater that attempts to download the latest version of DoomTools.
 * The {@link #call()} method will return a {@link DoomToolsMain} result.
 * @author Matthew Tropiano
 */
public class DoomToolsUpdater extends InstancedFuture.Cancellable<Integer>
{
	public static final String UPDATE_GITHUB_API = "https://api.github.com/";
	public static final String UPDATE_REPO_NAME = "DoomTools";
	public static final String UPDATE_REPO_OWNER = "MTrop";

	private static final FileFilter JAR_FILES = (f) -> {
		return FileUtils.getFileExtension(f.getName()).equalsIgnoreCase("jar");
	};

	private static final HTTPReader<JSONObject> JSON_READER = (response, cancel, monitor) -> {
		return JSONReader.readJSON(response.getContentReader());
	};

	/** Root directory of DoomTools. */
	private File toolsRootDirectory;
	/** Root directory of DoomTools. */
	private Listener listener;
	/** Cancel switch. */
	private AtomicBoolean cancelSwitch;
	
	/**
	 * Creates an updater instance.
	 * @param toolsRootDirectory the root directory where DoomTools is installed to.
	 * @param listener the updater listener.
	 * @throws NullPointerException if either parameter is null.
	 */
	public DoomToolsUpdater(File toolsRootDirectory, Listener listener)
	{
		this.toolsRootDirectory = Objects.requireNonNull(toolsRootDirectory);
		this.listener = Objects.requireNonNull(listener);
		this.cancelSwitch = new AtomicBoolean(false);
	}
	
	@Override
	public void cancel() 
	{
		cancelSwitch.set(true);
		super.cancel();
	}
	
	@Override
	public Integer call() throws Exception
	{
		if (isCancelled())
		{
			listener.onUpdateAbort();
			return DoomToolsMain.ERROR_TASK_CANCELLED;
		}
		
		cancelSwitch.set(false);
		
		// grab date from current JAR.
		String currentVersion = getLatestJarFile(new File(toolsRootDirectory.getAbsolutePath() + "/jar")).getName().substring(10, 30);
		listener.onMessage("Current version is: " + currentVersion);
		listener.onMessage("Contacting update site...");

		JSONObject assetJSON = null;

		try {
			JSONObject json;
			json = getJSONResponse(UPDATE_GITHUB_API);
			
			if (isCancelled())
			{
				listener.onUpdateAbort();
				return DoomToolsMain.ERROR_TASK_CANCELLED;
			}
			
			String repoURL;
			if ((repoURL = json.get("repository_url").getString()) == null)
			{
				listener.onError("Unexpected content from update site. Update failed.");
				return DoomToolsMain.ERROR_SITE_ERROR;
			}
			
			// Get latest release assets list.
			json = getJSONResponse(repoURL
				.replace("{owner}", UPDATE_REPO_OWNER)
				.replace("{repo}", UPDATE_REPO_NAME)
				+ "/releases/latest"
			).get("assets");

			if (isCancelled())
			{
				listener.onUpdateAbort();
				return DoomToolsMain.ERROR_TASK_CANCELLED;
			}
			
			if (json == null || !json.isArray())
			{
				listener.onError("Unexpected content from update site. Update failed.");
				return DoomToolsMain.ERROR_SITE_ERROR;
			}
			
			// Get archive from latest release.
			for (int i = 0; i < json.length(); i++)
			{
				JSONObject element = json.get(i);
				if ("application/zip".equals(element.get("content_type").getString()))
				{
					if (element.get("name").getString().contains("-jar"))
					{
						assetJSON = element;
						break;
					}
				}
			}
			
		} catch (SocketTimeoutException e) {
			listener.onError("Timeout occurred contacting update site.");
			return DoomToolsMain.ERROR_TIMEOUT;
		} catch (IOException e) {
			listener.onError("Read error contacting update site: " + e.getLocalizedMessage());
			return DoomToolsMain.ERROR_IOERROR;
		}
		
		if (assetJSON == null)
		{
			listener.onError("Release found, but no suitable archive.");
			return DoomToolsMain.ERROR_SITE_ERROR;
		}

		// should grab date from asset name (CMD version).
		String releaseVersion = assetJSON.get("name").getString().substring(14, 34); 

		if (currentVersion.compareTo(releaseVersion) >= 0)
		{
			listener.onUpToDate();
			return DoomToolsMain.ERROR_NONE;
		}
		
		if (isCancelled())
		{
			listener.onUpdateAbort();
			return DoomToolsMain.ERROR_TASK_CANCELLED;
		}
		
		if (!listener.shouldContinue(releaseVersion))
		{
			listener.onUpdateAbort();
			return DoomToolsMain.ERROR_NONE;
		}
		
		try (HTTPResponse response = HTTPRequest.get(assetJSON.get("browser_download_url").getString()).setHeader("Accept", "*/*").send())
		{
			if (!response.isSuccess())
			{
				listener.onError("Server responded: HTTP " + response.getStatusCode() + " " + response.getStatusMessage());
				return DoomToolsMain.ERROR_SITE_ERROR;
			}
			
			listener.onDownloadStart();
			File tempFile = Files.createTempFile("doomtools-tmp-", ".zip").toFile(); 
			try 
			{
				final AtomicLong currentBytes = new AtomicLong(0L);

				listener.onDownloadTransfer(currentBytes.get(), response.getLength());
				
				final AtomicLong lastDate = new AtomicLong(System.currentTimeMillis());
				try (FileOutputStream fos = new FileOutputStream(tempFile))
				{
					response.relayContent(fos, (cur, max) -> 
					{
						long next = System.currentTimeMillis();
						currentBytes.set(cur);
						if (next > lastDate.get() + 250L)
						{
							listener.onDownloadTransfer(currentBytes.get(), max);
							lastDate.set(next);
						}
					});
				}
				
				listener.onDownloadTransfer(currentBytes.get(), response.getLength());
				listener.onDownloadFinish();
				listener.onMessage("Extracting....");
				
				boolean extracted = false;
				try (ZipFile zf = new ZipFile(tempFile))
				{
					@SuppressWarnings("unchecked")
					Enumeration<ZipEntry> zipenum = (Enumeration<ZipEntry>)zf.entries();
					while (zipenum.hasMoreElements())
					{
						ZipEntry entry = zipenum.nextElement();
						if (entry.getName().endsWith(".jar") && !entry.getName().contains("jar/"))
						{
							File targetJarFile = new File(toolsRootDirectory.getAbsolutePath() + "/jar/" + entry.getName());
							try (InputStream zin = zf.getInputStream(entry); FileOutputStream fos = new FileOutputStream(targetJarFile))
							{
								IOUtils.relay(zin, fos);
							}
							extracted = true;
						}
					}
				}
				
				if (!extracted)
				{
					listener.onError("No JAR entry in archive!");
					return DoomToolsMain.ERROR_IOERROR;
				}
				else
				{
					listener.onMessage("Finished updating!");
				}
			} 
			finally 
			{
				tempFile.delete();
			}
		} 
		catch (SocketTimeoutException e) 
		{
			listener.onError("Timeout occurred downloading update!");
			return DoomToolsMain.ERROR_TIMEOUT;
		} 
		catch (IOException e) 
		{
			listener.onError("Read error downloading update: " + e.getLocalizedMessage());
			return DoomToolsMain.ERROR_IOERROR;
		}
		
		listener.onUpdateSuccessful();
		return DoomToolsMain.ERROR_NONE;
	}
	
	private static File[] getSortedJarList(File dir)
	{
		File[] jars = dir.listFiles(JAR_FILES);
		Arrays.sort(jars, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()));
		return jars;
	}
	
	private static File getLatestJarFile(File dir)
	{
		File[] jars = getSortedJarList(dir);
		return jars.length == 0 ? null : jars[jars.length - 1];
	}
	
	private static JSONObject getJSONResponse(String url) throws IOException
	{
		return HTTPRequest.get(url).send(JSON_READER);
	}
	
	/**
	 * Listener for updater events (progress, messages, etc.).
	 */
	public interface Listener
	{
		/**
		 * Called when a message needs printing.
		 * @param message the message to print.
		 */
		void onMessage(String message);

		/**
		 * Called when an error message needs printing.
		 * @param message the message to print.
		 */
		void onError(String message);

		/**
		 * Called when the download starts.
		 */
		void onDownloadStart();

		/**
		 * Called when bytes are transferred.
		 * @param current the current amount of bytes.
		 * @param max the maximum amount of bytes (can be null if unknown).
		 */
		void onDownloadTransfer(long current, Long max);
		
		/**
		 * Called when the download finishes.
		 */
		void onDownloadFinish();
		
		/**
		 * Called when an update was found and to ask the user to continue.
		 * @param versionString the found version string.
		 * @return true to continue, false to abort.
		 */
		boolean shouldContinue(String versionString);
		
		/**
		 * Called when the updater determines that you are up-to-date.
		 */
		void onUpToDate();
		
		/**
		 * Called when the updater finishes successfully.
		 */
		void onUpdateSuccessful();
		
		/**
		 * Called when the update was aborted.
		 */
		void onUpdateAbort();
		
	}
	
}
