package net.mtrop.doom.tools.doommake.gui.swing.panels;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.mtrop.doom.tools.doommake.gui.DoomMakeImageManager;
import net.mtrop.doom.tools.doommake.gui.DoomMakeProjectHelper;
import net.mtrop.doom.tools.doommake.gui.DoomMakeProjectHelper.ProcessCallException;
import net.mtrop.doom.tools.doommake.gui.DoomMakeProjectHelper.RequiredSettingException;
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
	private DoomMakeImageManager imageManager;
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
		
		this.imageManager = DoomMakeImageManager.get();
		this.projectHelper = DoomMakeProjectHelper.get();
		
		LoaderFuture<BufferedImage> folderImage = imageManager.getImage("folder.png");
		LoaderFuture<BufferedImage> vsCodeImage = imageManager.getImage("vscode.png");
		LoaderFuture<BufferedImage> sladeImage = imageManager.getImage("slade.png");
		
		ComponentActionHandler<JButton> folderAction = (component, event) -> 
		{
			try {
				projectHelper.openShell(projectDirectory);
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
