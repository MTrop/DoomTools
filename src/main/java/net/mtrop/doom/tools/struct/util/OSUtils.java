/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.util;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Objects;

/**
 * Simple OS utility functions.
 * @author Matthew Tropiano
 */
public final class OSUtils
{
	private static final File NULL_FILE;
	
	/** Is this running on an x86 architecture? */
	private static final boolean IS_X86;
	/** Is this running on an x64 architecture? */
	private static final boolean IS_X64;
	/** Is this running on a Power PC architecture? */
	private static final boolean IS_PPC;
	/** Is this running on an ARM series architecture? */
	private static final boolean IS_ARM;
	/** Are we using Windows? */
	private static final boolean IS_WINDOWS;
	/** Are we using Windows 95/98? */
	private static final boolean IS_WINDOWS_9X;
	/** Are we using Windows Me (God forbid)?*/
	private static final boolean IS_WINDOWS_ME;
	/** Are we using Windows 2000? */
	private static final boolean IS_WINDOWS_2000;
	/** Are we using Windows XP? */
	private static final boolean IS_WINDOWS_XP;
	/** Are we using Windows XP? */
	private static final boolean IS_WINDOWS_VISTA;
	/** Are we using Windows 7? */
	private static final boolean IS_WINDOWS_7;
	/** Are we using Windows 8? */
	private static final boolean IS_WINDOWS_8;
	/** Are we using Windows 10? */
	private static final boolean IS_WINDOWS_10;
	/** Are we using Windows 11? */
	private static final boolean IS_WINDOWS_11;
	/** Are we using Windows NT? */
	private static final boolean IS_WINDOWS_NT;
	/** Are we using Windows 2003 (Server)? */
	private static final boolean IS_WINDOWS_2003;
	/** Are we using Windows 2008 (Server)? */
	private static final boolean IS_WINDOWS_2008;
	/** Are we using Win32 mode? */
	private static final boolean IS_WIN32;
	/** Are we using Win64 mode? */
	private static final boolean IS_WIN64;
	/** Is this Mac OS X? */
	private static final boolean IS_OSX;
	/** Is this Mac OS X x86 Edition? */
	private static final boolean IS_OSX86;
	/** Is this Mac OS X x64 Edition? */
	private static final boolean IS_OSX64;
	/** Is this Mac OS X Power PC Edition? */
	private static final boolean IS_OSXPPC;
	/** Is this a Linux distro? */
	private static final boolean IS_LINUX;
	/** Is this a Solaris distro? */
	private static final boolean IS_SOLARIS;
	/** Current working directory. */
	private static final String WORK_DIR;
	/** 
	 * Current application settings directory.
	 * In Windows, this is set to whatever the APPDATA environment variable is set to.
	 * On other operating systems, this is set to whatever the HOME environment variable is set to.
	 */
	private static final String APP_DIR;		// set in static method.
	/** Current user's home directory. */
	private static final String HOME_DIR;

	static
	{
		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");
		IS_X86 = osArch.contains("86");
		IS_X64 = osArch.contains("64");
		IS_PPC = osArch.contains("ppc") || osArch.contains("Power PC");
		IS_ARM = osArch.startsWith("arm");
		IS_WINDOWS = osName.contains("Windows");
		if (IS_WINDOWS)
		{
			IS_WINDOWS_9X = osName.contains("95") || osName.contains("98");
			IS_WINDOWS_ME = osName.contains("Me");
			IS_WINDOWS_2000 = osName.contains("2000");
			IS_WINDOWS_XP = osName.contains("XP");
			IS_WINDOWS_NT = osName.contains("NT");
			IS_WINDOWS_2003 = osName.contains("2003");
			IS_WINDOWS_2008 = osName.contains("2008");
			IS_WINDOWS_VISTA = osName.contains("Vista");
			IS_WINDOWS_7 = osName.contains(" 7");
			IS_WINDOWS_8 = osName.contains(" 8");
			IS_WINDOWS_10 = osName.contains(" 10");
			IS_WINDOWS_11 = osName.contains(" 11");
			IS_WIN32 = IS_X86;
			IS_WIN64 = IS_X64;
		}
		else
		{
			IS_WINDOWS_9X = false;
			IS_WINDOWS_ME = false;
			IS_WINDOWS_2000 = false;
			IS_WINDOWS_XP = false;
			IS_WINDOWS_NT = false;
			IS_WINDOWS_2003 = false;
			IS_WINDOWS_2008 = false;
			IS_WINDOWS_VISTA = false;
			IS_WINDOWS_7 = false;
			IS_WINDOWS_8 = false;
			IS_WINDOWS_10 = false;
			IS_WINDOWS_11 = false;
			IS_WIN32 = false;
			IS_WIN64 = false;
		}
		
		IS_OSX = osName.contains("OS X") || osName.contains("macOS");
		if (IS_OSX)
		{
			IS_OSX86 = IS_X86;
			IS_OSX64 = IS_X64;
			IS_OSXPPC = IS_PPC;
		}
		else
		{
			IS_OSX86 = false;
			IS_OSX64 = false;
			IS_OSXPPC = false;
		}
		
		IS_LINUX = osName.contains("Linux");
		if (IS_LINUX)
		{
			
		}
		IS_SOLARIS = osName.contains("Solaris");
		if (IS_SOLARIS)
		{
			
		}
		
		// Application data folder.
		if (IS_WINDOWS)
			APP_DIR = System.getenv("APPDATA");
		else if (IS_OSX)
			APP_DIR = System.getenv("HOME");
		else if (IS_LINUX)
			APP_DIR = System.getenv("HOME");
		else if (IS_SOLARIS)
			APP_DIR = System.getenv("HOME");
		else
			APP_DIR = null;
		
		WORK_DIR = System.getProperty("user.dir");
		HOME_DIR = System.getProperty("user.home");
		NULL_FILE = new File(IS_WINDOWS ? "NUL" : "/dev/null");
	}

	private OSUtils() {}

	/** @return true if we using a Linux distro. */
	public static boolean isLinux()
	{
		return IS_LINUX;
	}

	/** @return true if we using Mac OS X. */
	public static boolean isOSX()
	{
		return IS_OSX;
	}

	/** @return true if we using 64-bit Mac OS X. */
	public static boolean isOSX64()
	{
		return IS_OSX64;
	}

	/** @return true if we using x86 Mac OS X. */
	public static boolean isOSX86()
	{
		return IS_OSX86;
	}

	/** @return true if we using Power PC Mac OS X. */
	public static boolean isOSXPPC()
	{
		return IS_OSXPPC;
	}

	/** @return true if this is running on an Power PC architecture. */
	public static boolean isPPC()
	{
		return IS_PPC;
	}

	/** @return true if this is running on an ARM architecture. */
	public static boolean isARM()
	{
		return IS_ARM;
	}

	/** @return true if we using 32-bit Windows. */
	public static boolean isWin32()
	{
		return IS_WIN32;
	}

	/** @return true if we using 64-bit Windows. */
	public static boolean isWin64() 
	{
		return IS_WIN64;
	}

	/** @return true if we using Windows. */
	public static boolean isWindows()
	{
		return IS_WINDOWS;
	}

	/** @return true if we using Windows 2000. */
	public static boolean isWindows2000()
	{
		return IS_WINDOWS_2000;
	}

	/** @return true if we using Windows 2003. */
	public static boolean isWindows2003()
	{
		return IS_WINDOWS_2003;
	}

	/** @return true if we using Windows 2008. */
	public static boolean isWindows2008()
	{
		return IS_WINDOWS_2008;
	}

	/** @return true if we using Windows Vista. */
	public static boolean isWindowsVista()
	{
		return IS_WINDOWS_VISTA;
	}

	/** @return true if we using Windows 7. */
	public static boolean isWindows7()
	{
		return IS_WINDOWS_7;
	}

	/** 
	 * @return true if we using Windows 8.
	 */
	public static boolean isWindows8()
	{
		return IS_WINDOWS_8;
	}

	/** 
	 * @return true if we using Windows 10. 
	 */
	public static boolean isWindows10()
	{
		return IS_WINDOWS_10;
	}

	/** 
	 * @return true if we using Windows 11. 
	 */
	public static boolean isWindows11()
	{
		return IS_WINDOWS_11;
	}

	/** @return true if we using Windows 95/98. */
	public static boolean isWindows9X()
	{
		return IS_WINDOWS_9X;
	}

	/** @return true if we are using Windows ME, or better yet, if we should just kill the program now. */
	public static boolean isWindowsME()
	{
		return IS_WINDOWS_ME;
	}

	/** @return true if we using Windows NT. */
	public static boolean isWindowsNT()
	{
		return IS_WINDOWS_NT;
	}

	/** @return true if we using Windows XP. */
	public static boolean isWindowsXP()
	{
		return IS_WINDOWS_XP;
	}

	/** @return true if this is running on an x64 architecture. */
	public static boolean isX64()
	{
		return IS_X64;
	}

	/** @return true if this is running on an x86 architecture. */
	public static boolean isX86()
	{
		return IS_X86;
	}

	/** @return true if this is running on Sun Solaris. */
	public static boolean isSolaris()
	{
		return IS_SOLARIS;
	}
	
	/** 
	 * @return the path to the working directory.
	 */
	public static String getWorkingDirectoryPath()
	{
		return WORK_DIR;
	}

	/** 
	 * Current application settings directory.
	 * In Windows, this is set to whatever the APPDATA environment variable is set to.
	 * On other operating systems, this is set to whatever the HOME environment variable is set to.
	 * @return the path to the common application settings directory.
	 */
	public static String getApplicationSettingsPath()
	{
		return APP_DIR;
	}

	/** 
	 * @return the current user's home directory.
	 */
	public static String getHomeDirectoryPath()
	{
		return HOME_DIR;
	}
	
	/**
	 * @return the "null" file pipe for this operating system.
	 */
	public static File getNullFile()
	{
		return NULL_FILE;
	}

	/**
	 * Attempts to detect if an executable is present on the platform's PATH by name.
	 * On Windows, this is done through the <code>where</code> command. On Unix-like systems, the <code>which</code> command. 
	 * @param imageName the executable name.
	 * @return true if so, false if not.
	 * @throws NullPointerException if imageName is null.
	 * @throws UnsupportedOperationException if a mechanism to search the PATH environment does not exist.
	 */
	public static boolean onPath(String imageName)
	{
		Objects.requireNonNull(imageName);
		
		try {
			ProcessBuilder pb = (new ProcessBuilder())
				.redirectInput(Redirect.from(NULL_FILE))
				.redirectError(Redirect.appendTo(NULL_FILE))
				.redirectOutput(Redirect.appendTo(NULL_FILE))
			;
			if (IS_WINDOWS)
				pb.command("where", imageName);
			else if (IS_OSX || IS_LINUX)
				pb.command("which", imageName);
			else
				throw new UnsupportedOperationException("OS does not have \"where\" or \"which\" available for PATH search!");
			int status = pb.start().waitFor();
			return status == 0;
		} catch (IOException e) {
			throw new UnsupportedOperationException("OS does not have \"where\" or \"which\" available for PATH search!");
		} catch (InterruptedException e) {
			return false; // Shouldn't happen.
		}
	}
	
}
