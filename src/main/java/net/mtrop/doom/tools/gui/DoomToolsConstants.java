package net.mtrop.doom.tools.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.struct.util.OSUtils;

import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;

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
		FileFilter WORKSPACES = fileExtensionFilter("DoomTools Workspaces (*.dtw)", "dtw");
		FileFilter ALL = fileFilter("All Files", (f) -> true);
	}
}
