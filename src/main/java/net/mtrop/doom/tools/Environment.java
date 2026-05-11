/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.File;

import net.mtrop.doom.tools.struct.util.OSUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

/**
 * One stop shop for environment variable value fetching.
 * @author Matthew Tropiano
 */
public final class Environment 
{
	private static final EnvironmentState ENV_STATE;
	
	static
	{
		if (isPortableEnvironmentPresent())
			ENV_STATE = new PortableEnvironmentState();
		else if (OSUtils.isWindows())
			ENV_STATE = new WindowsEnvironmentState();
		else if (OSUtils.isOSX())
			ENV_STATE = new MacOSEnvironmentState();
		else if (OSUtils.isLinux())
		{
			if (isXDGEnvironmentPresent())
				ENV_STATE = new XDGEnvironmentState();
			else
				ENV_STATE = new LinuxEnvironmentState();
		}
		else
			ENV_STATE = new DefaultEnvironmentState(); 
	}
	
	private static boolean isXDGEnvironmentPresent()
	{
		return System.getenv("XDG_CONFIG_HOME") != null
			|| System.getenv("XDG_DATA_HOME") != null
			|| System.getenv("XDG_STATE_HOME") != null
			|| System.getenv("XDG_DATA_DIRS") != null
			|| System.getenv("XDG_CONFIG_DIRS") != null
		;
	}
	
	private static boolean isPortableEnvironmentPresent()
	{
		return (new File(getDoomToolsPath() + File.separator + "portable.txt")).exists();
	}
	
	/**
	 * Gets the path that DoomTools is running from.
	 * If null, this variable may not have been set - you are running from an IDE.
	 * @return the corresponding value.
	 * @throws SecurityException if the variable could not be retrieved.
	 */
	public static String getDoomToolsPath()
	{
		String path = System.getenv("DOOMTOOLS_PATH");
		if (path == null)
			return null;
		return path.endsWith(File.separator) ? path.substring(0, path.length() - File.separator.length()) : path;
	}
	
	/**
	 * Gets the path of the JAR that DoomTools is running from.
	 * If null, this variable may not have been set - you are running from an IDE.
	 * @return the corresponding value.
	 * @throws SecurityException if the variable could not be retrieved.
	 */
	public static String getDoomToolsJarPath()
	{
		return System.getenv("DOOMTOOLS_JAR");
	}
	
	/** 
	 * @return the path to where configuration gets stored. 
	 */
	public static String getApplicationConfigPath()
	{
		return ENV_STATE.getApplicationConfigPath();
	}
	
	/** 
	 * @return the path to where application data gets stored. 
	 */
	public static String getApplicationDataPath()
	{
		return ENV_STATE.getApplicationDataPath();
	}
	
	/** 
	 * @return the path to where cache data gets stored. 
	 */
	public static String getApplicationCachePath()
	{
		return ENV_STATE.getApplicationCachePath();
	}
	
	/**
	 * @return the path to where system configuration gets stored. 
	 */
	public static String getSystemConfigPath()
	{
		return ENV_STATE.getSystemConfigPath();
	}
	
	/**
	 * @return the path to where system configuration gets stored. 
	 */
	public static String getSystemDataPath()
	{
		return ENV_STATE.getSystemDataPath();
	}
	
	/** 
	 * @return the path to where temporary data gets stored. 
	 */
	public static String getSystemTempPath()
	{
		return ENV_STATE.getSystemTempPath();
	}
	
	private interface EnvironmentState
	{
		/** 
		 * @return the path to where configuration gets stored. 
		 */
		String getApplicationConfigPath();

		/** 
		 * @return the path to where application data gets stored. 
		 */
		String getApplicationDataPath();

		/** 
		 * @return the path to where application data gets stored. 
		 */
		String getApplicationCachePath();

		/** 
		 * @return the path to where system configuration gets stored. 
		 */
		String getSystemConfigPath();
		
		/** 
		 * @return the path to where system data gets stored. 
		 */
		String getSystemDataPath();

		/** 
		 * @return the path to where temporary data gets stored. 
		 */
		String getSystemTempPath();
	}
	
	private static class DefaultEnvironmentState implements EnvironmentState
	{
		private static final String APPDATA_PATH = OSUtils.getApplicationSettingsPath() + File.separator + "DoomTools";

		@Override
		public String getApplicationConfigPath()
		{
			return APPDATA_PATH;
		}

		@Override
		public String getApplicationDataPath()
		{
			return APPDATA_PATH + File.separator + "data";
		}

		@Override
		public String getApplicationCachePath()
		{
			return APPDATA_PATH + File.separator + "cache";
		}

		@Override
		public String getSystemConfigPath()
		{
			return Environment.getDoomToolsPath() + File.separator + "config";
		}

		@Override
		public String getSystemDataPath()
		{
			return Environment.getDoomToolsPath() + File.separator + "data";
		}

		@Override
		public String getSystemTempPath()
		{
			return OSUtils.getTempDirectoryPath();
		}
	}

	private static class PortableEnvironmentState implements EnvironmentState
	{
		private static final String LOCAL_CONFIG_DIR = Environment.getDoomToolsPath(); 

		@Override
		public String getApplicationConfigPath()
		{
			return LOCAL_CONFIG_DIR + File.separator + "config";
		}

		@Override
		public String getApplicationDataPath()
		{
			return LOCAL_CONFIG_DIR + File.separator + "data";
		}
	
		@Override
		public String getApplicationCachePath()
		{
			return LOCAL_CONFIG_DIR + File.separator + "cache";
		}

		@Override
		public String getSystemConfigPath()
		{
			return getApplicationConfigPath();
		}

		@Override
		public String getSystemDataPath()
		{
			return getApplicationDataPath();
		}

		@Override
		public String getSystemTempPath()
		{
			return LOCAL_CONFIG_DIR + File.separator + "temp";
		}
	}
	
	private static class WindowsEnvironmentState extends DefaultEnvironmentState
	{
		// No changes.
	}
	
	private static class MacOSEnvironmentState extends DefaultEnvironmentState
	{
		// No changes.
	}
	
	private static class LinuxEnvironmentState extends DefaultEnvironmentState
	{
		@Override
		public String getSystemConfigPath()
		{
			return "/usr/share/DoomTools/config";
		}

		@Override
		public String getSystemDataPath()
		{
			return "/usr/share/DoomTools/data";
		}
	}
	
	private static class XDGEnvironmentState implements EnvironmentState
	{
		@Override
		public String getApplicationConfigPath()
		{
			String path = System.getenv("XDG_CONFIG_HOME");
			if (ObjectUtils.isEmpty(path))
				return "~/.config" + File.separator + "DoomTools";
			else
				return path + File.separator + "DoomTools";
		}

		@Override
		public String getApplicationDataPath()
		{
			String path = System.getenv("XDG_DATA_HOME");
			if (ObjectUtils.isEmpty(path))
				return "~/.local/share" + File.separator + "DoomTools";
			else
				return path + File.separator + "DoomTools";
		}
	
		@Override
		public String getApplicationCachePath()
		{
			String path = System.getenv("XDG_CACHE_HOME");
			if (ObjectUtils.isEmpty(path))
				return "~/.cache" + File.separator + "DoomTools";
			else
				return path + File.separator + "DoomTools";
		}
	
		@Override
		public String getSystemConfigPath()
		{
			String path = System.getenv("XDG_CONFIG_DIRS");
			if (ObjectUtils.isEmpty(path))
				return "/etc/xdg/DoomTools";
			else
			{
				String[] paths = path.split(File.pathSeparator);
				return paths[paths.length - 1] + "DoomTools";
			}
		}

		@Override
		public String getSystemDataPath()
		{
			String path = System.getenv("XDG_DATA_DIRS");
			if (ObjectUtils.isEmpty(path))
				return "/usr/share/DoomTools";
			else
			{
				String[] paths = path.split(File.pathSeparator);
				return paths[paths.length - 1] + "DoomTools";
			}
		}

		@Override
		public String getSystemTempPath()
		{
			return OSUtils.getTempDirectoryPath();
		}
	}
	
}
