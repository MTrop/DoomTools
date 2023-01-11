/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.JPanel;

import net.mtrop.doom.tools.Version;

import java.awt.BorderLayout;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;

/**
 * A DoomMake panel for About info.
 * @author Matthew Tropiano
 */
public class DoomToolsAboutPanel extends JPanel
{
	private static final long serialVersionUID = 8389287671690217978L;

	/**
	 * Creates the About panel.
	 */
	public DoomToolsAboutPanel()
	{
		String versionString = Version.DOOMTOOLS;
		
		StringBuilder sb = new StringBuilder("<html>");
		sb.append("<h2>DoomTools v").append(versionString).append("</h2>");
		sb.append("by Matt Tropiano and friends (see AUTHORS.TXT)").append("<br/>");
		sb.append("<br/>");
		sb.append("Using <b>DoomStruct v").append(Version.DOOMSTRUCT).append("</b>").append("<br/>");
		sb.append("Using <b>Black Rook JSON v").append(Version.JSON).append("</b>").append("<br/>");
		sb.append("Using <b>Rookscript v").append(Version.ROOKSCRIPT).append("</b>").append("<br/>");
		sb.append("Using <b>Rookscript-Desktop v").append(Version.ROOKSCRIPT_DESKTOP).append("</b>").append("<br/>");
		sb.append("Using <b>FlatLaf v").append(Version.FLATLAF).append("</b>").append("<br/>");
		sb.append("Using <b>RSyntaxTextArea v").append(Version.RSYNTAXTEXTAREA).append("</b>").append("<br/>");
		sb.append("Using <b>AutoComplete v").append(Version.AUTOCOMPLETE).append("</b>").append("<br/>");
		sb.append("Using <b>CommonMark v").append(Version.COMMONMARK).append("</b>").append("<br/>");
		sb.append("<br/>");
		sb.append("Contains <b>DECOHack v").append(Version.DECOHACK).append("</b>").append("<br/>");
		sb.append("Contains <b>DImgConv v").append(Version.DIMGCONV).append("</b>").append("<br/>");
		sb.append("Contains <b>DMXConv v").append(Version.DMXCONV).append("</b>").append("<br/>");
		sb.append("Contains <b>DoomMake v").append(Version.DOOMMAKE).append("</b>").append("<br/>");
		sb.append("Contains <b>WadMerge v").append(Version.WADMERGE).append("</b>").append("<br/>");
		sb.append("Contains <b>WadScript v").append(Version.WADSCRIPT).append("</b>").append("<br/>");
		sb.append("Contains <b>WADTex v").append(Version.WADTEX).append("</b>").append("<br/>");
		sb.append("Contains <b>WSwAnTBL v").append(Version.WSWANTBL).append("</b>").append("<br/>");
		sb.append("Contains <b>WTExport v").append(Version.WTEXPORT).append("</b>").append("<br/>");
		sb.append("Contains <b>WTexScan v").append(Version.WTEXSCAN).append("</b>").append("<br/>");
		sb.append("<br/>");
		sb.append("<b>FlatLaf</b> Look And Feel (C) 2003-2022 FormDev Software GmbH").append("<br/>");
		sb.append("<b>RSyntaxTextArea</b> and <b>AutoComplete</b> (C) 2021 Robert Futrell").append("<br/>");
		sb.append("<b>FamFamFam Silk</b> Icons by Mark James").append("<br/>");
		sb.append("<b>SLADE3</b> Icon by Sir Juddington").append("<br/>");
		sb.append("<br/>");
		sb.append("All third-party licenses are available in the \"licenses\" folder in the documentation folder.").append("<br/>");
		sb.append("<br/>");
		sb.append("<b>Thank you for using DoomTools!</b>");
		sb.append("</html>");
		
		containerOf(this, node(BorderLayout.CENTER, label(sb.toString())));
	}
	
}
