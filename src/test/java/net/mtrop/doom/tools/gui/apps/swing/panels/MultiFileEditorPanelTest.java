package net.mtrop.doom.tools.gui.apps.swing.panels;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.frame;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

public final class MultiFileEditorPanelTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		ObjectUtils.apply(frame("Test", ObjectUtils.apply(new EditorMultiFilePanel(), (ep) -> {
			ep.newEditor("Stuff", "Hello, content!");
			ep.newEditor("Stuff 2", "Hello, more content!");
			try {
				ep.openFileEditor(new File("src/test/java/net/mtrop/doom/tools/gui/MakeICNSMain.java"), 0, Charset.defaultCharset());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			ep.setPreferredSize(new Dimension(640, 480));
		})), 
		(frame) -> {
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
		
	}

}
