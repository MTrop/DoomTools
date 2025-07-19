/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import javax.swing.JPanel;

import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.struct.swing.ClipboardUtils;

import java.awt.BorderLayout;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;

/**
 * A DoomMake panel for About info.
 * @author Matthew Tropiano
 */
public class DoomToolsAboutPanel extends JPanel
{
	private static final long serialVersionUID = 8389287671690217978L;

	private static final String VERSION_TEXT = (new StringBuilder())
		.append("DoomTools v").append(Version.DOOMTOOLS).append("\n")
		.append("\n")
		.append("DoomStruct v").append(Version.DOOMSTRUCT).append("\n")
		.append("Black Rook JSON v").append(Version.JSON).append("\n")
		.append("Rookscript v").append(Version.ROOKSCRIPT).append("\n")
		.append("Rookscript-Desktop v").append(Version.ROOKSCRIPT_DESKTOP).append("\n")
		.append("FlatLaf v").append(Version.FLATLAF).append("\n")
		.append("RSyntaxTextArea v").append(Version.RSYNTAXTEXTAREA).append("\n")
		.append("AutoComplete v").append(Version.AUTOCOMPLETE).append("\n")
		.append("CommonMark v").append(Version.COMMONMARK).append("\n")
		.append("Jsoup v").append(Version.JSOUP).append("\n")
		.append("\n")
		.append("DECOHack v").append(Version.DECOHACK).append("\n")
		.append("DImgConv v").append(Version.DIMGCONV).append("\n")
		.append("DMXConv v").append(Version.DMXCONV).append("\n")
		.append("DoomFetch v").append(Version.DOOMFETCH).append("\n")
		.append("DoomMake v").append(Version.DOOMMAKE).append("\n")
		.append("WadMerge v").append(Version.WADMERGE).append("\n")
		.append("WadScript v").append(Version.WADSCRIPT).append("\n")
		.append("WADTex v").append(Version.WADTEX).append("\n")
		.append("WSwAnTBL v").append(Version.WSWANTBL).append("\n")
		.append("WTExport v").append(Version.WTEXPORT).append("\n")
		.append("WTexList v").append(Version.WTEXLIST).append("\n")
		.append("WTexScan v").append(Version.WTEXSCAN).append("\n")
	.toString();
	
	/**
	 * Creates the About panel.
	 */
	public DoomToolsAboutPanel()
	{
		DoomToolsGUIUtils utils = DoomToolsGUIUtils.get();
		
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
		sb.append("Using <b>Jsoup v").append(Version.JSOUP).append("</b>").append("<br/>");
		sb.append("<br/>");
		sb.append("Contains <b>DECOHack v").append(Version.DECOHACK).append("</b>").append("<br/>");
		sb.append("Contains <b>DImgConv v").append(Version.DIMGCONV).append("</b>").append("<br/>");
		sb.append("Contains <b>DMXConv v").append(Version.DMXCONV).append("</b>").append("<br/>");
		sb.append("Contains <b>DoomFetch v").append(Version.DOOMFETCH).append("</b>").append("<br/>");
		sb.append("Contains <b>DoomMake v").append(Version.DOOMMAKE).append("</b>").append("<br/>");
		sb.append("Contains <b>WadMerge v").append(Version.WADMERGE).append("</b>").append("<br/>");
		sb.append("Contains <b>WadScript v").append(Version.WADSCRIPT).append("</b>").append("<br/>");
		sb.append("Contains <b>WADTex v").append(Version.WADTEX).append("</b>").append("<br/>");
		sb.append("Contains <b>WSwAnTBL v").append(Version.WSWANTBL).append("</b>").append("<br/>");
		sb.append("Contains <b>WTExport v").append(Version.WTEXPORT).append("</b>").append("<br/>");
		sb.append("Contains <b>WTexList v").append(Version.WTEXLIST).append("</b>").append("<br/>");
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
		
		containerOf(this, 
			node(BorderLayout.CENTER, label(sb.toString())),
			node(BorderLayout.SOUTH, containerOf(flowLayout(Flow.TRAILING),
				node(utils.createButtonFromLanguageKey("doomtools.about.copy", (b) -> {
					ClipboardUtils.sendStringToClipboard(VERSION_TEXT);
				}))
			))
		);
	}
	
}
