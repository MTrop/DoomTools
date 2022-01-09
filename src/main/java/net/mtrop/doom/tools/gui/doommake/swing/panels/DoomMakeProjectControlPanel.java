package net.mtrop.doom.tools.gui.doommake.swing.panels;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.mtrop.doom.tools.gui.DoomToolsImageManager;
import net.mtrop.doom.tools.gui.doommake.DoomMakeProjectHelper;
import net.mtrop.doom.tools.gui.doommake.DoomMakeProjectHelper.ProcessCallException;
import net.mtrop.doom.tools.gui.doommake.DoomMakeProjectHelper.RequiredSettingException;
import net.mtrop.doom.tools.struct.Loader.LoaderFuture;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;

/**
 * A DoomMake control panel for a single project directory.
 * @author Matthew Tropiano
 */
public class DoomMakeProjectControlPanel extends JPanel
{
	private static final long serialVersionUID = -726632043594260163L;
	
	/** Image manager. */
	private DoomToolsImageManager imageManager;
	/** Project helper. */
	private DoomMakeProjectHelper projectHelper;

	/**
	 * Creates a new new project control panel.
	 * @param projectDirectory the project directory.
	 */
	public DoomMakeProjectControlPanel(final File projectDirectory)
	{
		if (!projectDirectory.isDirectory())
			throw new IllegalArgumentException("Provided directory is not a directory.");
		
		this.imageManager = DoomToolsImageManager.get();
		this.projectHelper = DoomMakeProjectHelper.get();
		
		LoaderFuture<BufferedImage> folderImage = imageManager.getImageAsync("folder.png");
		LoaderFuture<BufferedImage> vsCodeImage = imageManager.getImageAsync("vscode.png");
		LoaderFuture<BufferedImage> sladeImage = imageManager.getImageAsync("slade.png");
		
		ComponentActionHandler<JButton> folderAction = (component, event) -> 
		{
			try {
				projectHelper.openExplorer(projectDirectory);
			} catch (ProcessCallException e) {
				SwingUtils.error(component, e.getMessage());
			} catch (FileNotFoundException e) {
				SwingUtils.error(component, e.getMessage());
			}
		};
		
		ComponentActionHandler<JButton> vsCodeAction = (component, event) -> 
		{
			try {
				projectHelper.openVSCode(projectDirectory);
			} catch (ProcessCallException e) {
				SwingUtils.error(component, e.getMessage());
			} catch (RequiredSettingException e) {
				// TODO: Make settings error modal.
				SwingUtils.error(component, e.getMessage());
			} catch (FileNotFoundException e) {
				SwingUtils.error(component, e.getMessage());
			}
		};
		
		ComponentActionHandler<JButton> sladeAction = (component, event) -> 
		{
			try {
				projectHelper.openSourceFolderInSlade(projectDirectory);
			} catch (ProcessCallException e) {
				SwingUtils.error(component, e.getMessage());
			} catch (RequiredSettingException e) {
				// TODO: Make settings error modal.
				SwingUtils.error(component, e.getMessage());
			} catch (FileNotFoundException e) {
				SwingUtils.error(component, e.getMessage());
			}
		};
		
		containerOf(this, new FlowLayout(FlowLayout.RIGHT),
			node(button(icon(folderImage.result()), folderAction)),
			node(button(icon(vsCodeImage.result()), vsCodeAction)),
			node(button(icon(sladeImage.result()), sladeAction))
		);
	}

}
