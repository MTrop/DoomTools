package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.managers.DoomMakeProjectHelper;
import net.mtrop.doom.tools.gui.managers.DoomToolsImageManager;
import net.mtrop.doom.tools.gui.managers.DoomMakeProjectHelper.ProcessCallException;
import net.mtrop.doom.tools.gui.managers.DoomMakeProjectHelper.RequiredSettingException;
import net.mtrop.doom.tools.struct.Loader.LoaderFuture;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * A DoomMake control panel for a single project directory.
 * @author Matthew Tropiano
 */
public class DoomMakeProjectControlPanel extends JPanel
{
	private static final long serialVersionUID = -726632043594260163L;
	
	/** Image manager. */
	private DoomToolsImageManager images;
	/** Project helper. */
	private DoomMakeProjectHelper helper;

	/**
	 * Creates a new new project control panel.
	 * @param projectDirectory the project directory.
	 */
	public DoomMakeProjectControlPanel(final File projectDirectory)
	{
		this.images = DoomToolsImageManager.get();
		this.helper = DoomMakeProjectHelper.get();
		
		if (!projectDirectory.isDirectory())
			throw new IllegalArgumentException("Provided directory is not a directory.");
		
		LoaderFuture<BufferedImage> folderImage = images.getImageAsync("folder.png");
		LoaderFuture<BufferedImage> vsCodeImage = images.getImageAsync("ide.png");
		LoaderFuture<BufferedImage> sladeImage = images.getImageAsync("slade.png");
		
		ButtonClickHandler folderAction = (b) -> 
		{
			try {
				helper.openExplorer(projectDirectory);
			} catch (ProcessCallException e) {
				SwingUtils.error(this, e.getMessage());
			} catch (FileNotFoundException e) {
				SwingUtils.error(this, e.getMessage());
			}
		};
		
		ButtonClickHandler vsCodeAction = (b) -> 
		{
			try {
				helper.openIDE(projectDirectory);
			} catch (ProcessCallException e) {
				SwingUtils.error(this, e.getMessage());
			} catch (RequiredSettingException e) {
				// TODO: Make settings error modal.
				SwingUtils.error(this, e.getMessage());
			} catch (FileNotFoundException e) {
				SwingUtils.error(this, e.getMessage());
			}
		};
		
		ButtonClickHandler sladeAction = (b) -> 
		{
			try {
				helper.openSourceFolderInSlade(projectDirectory);
			} catch (ProcessCallException e) {
				SwingUtils.error(this, e.getMessage());
			} catch (RequiredSettingException e) {
				// TODO: Make settings error modal.
				SwingUtils.error(this, e.getMessage());
			} catch (FileNotFoundException e) {
				SwingUtils.error(this, e.getMessage());
			}
		};
		
		containerOf(this, flowLayout(Flow.RIGHT),
			node(button(icon(folderImage.result()), folderAction)),
			node(button(icon(vsCodeImage.result()), vsCodeAction)),
			node(button(icon(sladeImage.result()), sladeAction))
		);
	}

}
