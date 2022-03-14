package net.mtrop.doom.tools.gui;

import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.fileFilter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.struct.util.OSUtils;

public interface DoomToolsConstants 
{
	/** Common paths. */
	interface Paths
	{
		/** DoomTools Config folder base. */
	    String APPDATA_PATH = OSUtils.getApplicationSettingsPath() + File.separator + "DoomTools" + File.separator;
	}
	
	/** Common file filters. */
	interface FileFilters
	{
		FileFilter EXECUTABLES = fileFilter("Executables", (f) -> f.canExecute()); 
		FileFilter DIRECTORIES = fileFilter("Directories", (f) -> f.isDirectory());
		FileFilter WORKSPACES = fileFilter("Workspaces (*.dtw)", (f) -> f.isDirectory());
		FileFilter ALL = fileFilter("All Files", (f) -> true);
	}
}
