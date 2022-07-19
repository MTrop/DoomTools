package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import net.mtrop.doom.tools.common.Common;
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
	 * @param executionPanel the execution panel tied to the controls.
	 * @param ideMode if true, omit the IDE button.
	 */
	public DoomMakeProjectControlPanel(final File projectDirectory, DoomMakeExecutionPanel executionPanel, boolean ideMode)
	{
		this.images = DoomToolsImageManager.get();
		this.helper = DoomMakeProjectHelper.get();
		
		if (!projectDirectory.isDirectory())
			throw new IllegalArgumentException("Provided directory is not a directory.");
		
		LoaderFuture<BufferedImage> folderImage = images.getImageAsync("folder.png");
		LoaderFuture<BufferedImage> vsCodeImage = images.getImageAsync("ide.png");
		LoaderFuture<BufferedImage> sladeImage = images.getImageAsync("slade.png");
		LoaderFuture<BufferedImage> terminalImage = images.getImageAsync("terminal.png");
		LoaderFuture<BufferedImage> refreshImage = images.getImageAsync("refresh.png");
		
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
				SwingUtils.error(this, e.getMessage());
			} catch (FileNotFoundException e) {
				SwingUtils.error(this, e.getMessage());
			}
		};
		
		ButtonClickHandler terminalAction = (b) -> 
		{
			if (!Common.openTerminalAtDirectory(projectDirectory))
				SwingUtils.error(this, "Could not open shell.");
		};
		
		ButtonClickHandler refreshAction = (b) -> 
		{
			executionPanel.refreshTargets();
		};
		
		List<Node> nodes = new LinkedList<>();
		nodes.add(node(button(icon(folderImage.result()), folderAction)));
		if (!ideMode)
			nodes.add(node(button(icon(vsCodeImage.result()), vsCodeAction)));
		nodes.add(node(button(icon(sladeImage.result()), sladeAction)));
		nodes.add(node(button(icon(terminalImage.result()), terminalAction)));
		nodes.add(node(button(icon(refreshImage.result()), refreshAction)));
		
		containerOf(this, flowLayout(Flow.RIGHT),
			nodes.toArray(new Node[nodes.size()])
		);
	}

}
