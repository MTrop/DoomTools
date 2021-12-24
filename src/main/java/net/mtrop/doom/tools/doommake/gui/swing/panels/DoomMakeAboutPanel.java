package net.mtrop.doom.tools.doommake.gui.swing.panels;

import javax.swing.JPanel;

import net.mtrop.doom.tools.common.Common;

import java.awt.BorderLayout;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;

/**
 * A DoomMake panel for About info.
 * @author Matthew Tropiano
 */
public class DoomMakeAboutPanel extends JPanel
{
	private static final long serialVersionUID = 8389287671690217978L;

	/**
	 * Creates the About panel.
	 */
	public DoomMakeAboutPanel()
	{
		String versionString = Common.getVersionString("doommake");
		String versionRSString = Common.getVersionString("rookscript");
		String versionWSString = Common.getVersionString("wadscript");
		
		StringBuilder sb = new StringBuilder("<html>");
		sb.append("<b>DoomMake v").append(versionString).append("</b>").append("<br/>");
		sb.append("by Matt Tropiano").append("<br/>");
		sb.append("<br/>");
		sb.append("Running <b>Rookscript v").append(versionRSString).append("</b>").append("<br/>");
		sb.append("Running <b>WadScript v").append(versionWSString).append("</b>").append("<br/>");
		sb.append("<br/>");
		sb.append("<b>FamFamFam Silk</b> Icons by Mark James").append("<br/>");
		sb.append("<b>SLADE3</b> Icon by Sir Juddington").append("<br/>");
		sb.append("Folder and VSCode Icons (C) Microsoft 2021").append("<br/>");
		sb.append("<br/>");
		sb.append("Thank you for using DoomMake!");
		sb.append("</html>");
		
		containerOf(this, node(BorderLayout.CENTER, label(sb.toString())));
	}
	
}
