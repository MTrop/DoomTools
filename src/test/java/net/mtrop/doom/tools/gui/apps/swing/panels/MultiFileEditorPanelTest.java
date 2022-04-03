package net.mtrop.doom.tools.gui.apps.swing.panels;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.frame;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.swing.JFrame;

import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.apps.swing.editors.MultiFileEditorPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

public final class MultiFileEditorPanelTest 
{
	public static void main(String[] args) 
	{
		DoomToolsGUIMain.setLAF();
		SwingUtils.apply(frame("Test", SwingUtils.apply(new MultiFileEditorPanel(null), (ep) -> {
			ep.newEditor("Stuff", "Hello, content!");
			ep.newEditor("Stuff 2", "Hello, more content!");
			try {
				ep.openFileEditor(new File("src/test/java/net/mtrop/doom/tools/gui/MakeICNSMain.java"), Charset.defaultCharset());
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
