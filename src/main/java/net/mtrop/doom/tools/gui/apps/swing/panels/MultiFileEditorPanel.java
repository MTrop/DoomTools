package net.mtrop.doom.tools.gui.apps.swing.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.SwingUtils.*;

/**
 * The editor panel for editing many files at once.
 * @author Matthew Tropiano
 */
public class MultiFileEditorPanel extends JPanel
{
	private static final long serialVersionUID = -3208735521175265227L;

	private DoomToolsIconManager icons;
	private DoomToolsGUIUtils utils;
	
	public interface ActionNames
	{
		String ACTION_SAVE = "save";
		String ACTION_SAVE_AS = "save-as";
		String ACTION_SAVE_ALL = "save-all";
		String ACTION_CLOSE = "close";
		String ACTION_CLOSE_ALL = "close-all";
		String ACTION_CLOSE_ALL_BUT_CURRENT = "close-all-but-current";
		String ACTION_CUT = "cut";
		String ACTION_COPY = "copy";
		String ACTION_PASTE = "paste";
		String ACTION_DELETE = "delete";
		String ACTION_SELECT_ALL = "select-all";
		String ACTION_UNDO = "undo";
		String ACTION_REDO = "redo";
	}
	
	// ======================================================================
	
	/** The main editor tabs. */
	private JTabbedPane mainEditorTabs;
	/** The action map that is a mapping of {@link RTextArea} actions to redirected ones for this panel. */
	private Map<String, Action> unifiedActionMap;
	
	/** Save Action */
	private Action saveAction;
	/** Save As Action */
	private Action saveAsAction;
	/** Save All Action */
	private Action saveAllAction;
	/** Close Action */
	private Action closeAction;
	/** Close All Action */
	private Action closeAllAction;
	/** Close All But Current Action */
	private Action closeAllButCurrentAction;
	/** Cut Action */
	private Action cutAction;
	/** Copy Action */
	private Action copyAction;
	/** Paste Action */
	private Action pasteAction;
	/** Delete Action */
	private Action deleteAction;
	/** Select All Action */
	private Action selectAllAction;
	/** Undo Action */
	private Action undoAction;
	/** Redo Action */
	private Action redoAction;
	
	// ======================================================================
	
	/** All editors. */
	private Map<Component, EditorHandle> allEditors;
	/** The currently selected editor. */
	private EditorHandle currentEditor;

	/**
	 * Creates a new multi-file editor panel.
	 */
	public MultiFileEditorPanel()
	{
		this.icons = DoomToolsIconManager.get();
		this.utils = DoomToolsGUIUtils.get();
		
		this.allEditors = new HashMap<>();
		this.currentEditor = null;
		
		this.mainEditorTabs = apply(tabs(TabPlacement.TOP, TabLayoutPolicy.SCROLL), (tabs) -> {
			tabs.addChangeListener((event) -> {
				int index = tabs.getSelectedIndex();
				if (index >= 0)
					currentEditor = allEditors.get(tabs.getTabComponentAt(tabs.getSelectedIndex()));
				else
					currentEditor = null;
				updateActionStates();
			});
		});
		
		// TODO: Add missing actions.
		this.cutAction = utils.createActionFromLanguageKey("texteditor.action.cut", (event) -> currentEditor.editorPanel.textArea.cut());
		this.copyAction = utils.createActionFromLanguageKey("texteditor.action.copy", (event) -> currentEditor.editorPanel.textArea.copy());
		this.pasteAction = utils.createActionFromLanguageKey("texteditor.action.paste", (event) -> currentEditor.editorPanel.textArea.paste());
		this.deleteAction = utils.createActionFromLanguageKey("texteditor.action.delete", (event) -> currentEditor.editorPanel.textArea.replaceSelection(""));
		this.selectAllAction = utils.createActionFromLanguageKey("texteditor.action.selectall", (event) -> currentEditor.editorPanel.textArea.selectAll());
		this.undoAction = utils.createActionFromLanguageKey("texteditor.action.undo", (event) -> currentEditor.editorPanel.textArea.undoLastAction());
		this.redoAction = utils.createActionFromLanguageKey("texteditor.action.redo", (event) -> currentEditor.editorPanel.textArea.redoLastAction());
		
		this.unifiedActionMap = apply(new HashMap<>(), (map) -> {
			// TODO: Add missing actions.
			map.put(ActionNames.ACTION_CUT, cutAction);
			map.put(ActionNames.ACTION_COPY, copyAction);
			map.put(ActionNames.ACTION_PASTE, pasteAction);
			map.put(ActionNames.ACTION_DELETE, deleteAction);
			map.put(ActionNames.ACTION_SELECT_ALL, selectAllAction);
			map.put(ActionNames.ACTION_UNDO, undoAction);
			map.put(ActionNames.ACTION_REDO, redoAction);
		});
		
		containerOf(this, 
			node(BorderLayout.CENTER, this.mainEditorTabs)
		);
	}
	
	/**
	 * Creates a new editor, returning the editor tab.
	 * @param icon the tab icon.
	 * @param title the tab title.
	 * @param content the content.
	 * @param attachedFile the content source file (if any, can be null).
	 * @return the editor handle created.
	 */
	public EditorHandle createNewTab(Icon icon, String title, File attachedFile, String content)
	{
		RSyntaxTextArea textArea = createTextArea();
		textArea.setText(content);
		
		EditorHandle component = attachedFile != null 
			? new EditorHandle(icon, attachedFile, textArea) 
			: new EditorHandle(icon, title, textArea)
		;
		mainEditorTabs.addTab(null, component.editorPanel);
		
		// The tab just added will be at the end.
		int tabIndex = mainEditorTabs.getTabCount() - 1;
		mainEditorTabs.setTabComponentAt(tabIndex, component.editorTab);
		
		allEditors.put(component.editorTab, component);
		
		return component;
	}
	
	/**
	 * Creates a new editor tab with a name.
	 * @param tabName the name of the tab.
	 * @param content the initial content of the new editor.
	 */
	public void newEditor(String tabName, String content)
	{
		createNewTab(icons.getImage("script-run.png"), tabName, null, content);
	}
	
	/**
	 * Opens a file into a new tab.
	 * Does nothing if the file is a directory.
	 * @param file the file to load.
	 * @param encoding the file encoding.
	 * @throws FileNotFoundException if the file could not be found. 
	 * @throws IOException if the file could not be read.
	 * @throws SecurityException if the OS is forbidding the read.
	 */
	public void openFile(File file, Charset encoding) throws FileNotFoundException, IOException
	{
		if (file.isDirectory())
			return;
		
		char[] cbuf = new char[8192];
		StringWriter sw = new StringWriter();
		try (Reader reader = new InputStreamReader(new FileInputStream(file), encoding))
		{
			int c;
			while ((c = reader.read(cbuf)) > 0)
				sw.write(cbuf, 0, c);
		}
		
		createNewTab(icons.getImage("script-run.png"), file.getName(), file, sw.toString());
	}
	
	public void saveCurrentEditor() 
	{
		// TODO: Finish this.
	}

	public void saveCurrentEditorAs() 
	{
		// TODO: Finish this.
	}

	public void saveAllEditors()
	{
		// TODO: Finish this.
	}

	public void closeCurrentEditor() 
	{
		// TODO: Finish this.
	}

	public void closeAllEditors() 
	{
		// TODO: Finish this.
	}

	public void closeAllButCurrentEditor() 
	{
		// TODO: Finish this.
	}

	/**
	 * Gets the action for manipulating the currently selected editor.
	 * @param actionName am action name {@link ActionNames}.
	 * @return the corresponding action, or null if no action.
	 */
	public Action getActionFor(String actionName)
	{
		return unifiedActionMap.get(actionName);
	}
	
	/**
	 * Called to generate a new syntax text area.
	 * By default, this creates a default text area.
	 * @return a new area.
	 */
	protected RSyntaxTextArea createTextArea()
	{
		return new RSyntaxTextArea();
	}
	
	private void removeEditorByTab(Component tabComponent)
	{
		int index;
		if ((index = mainEditorTabs.indexOfTabComponent(tabComponent)) >= 0)
		{
			mainEditorTabs.remove(index);
			allEditors.remove(tabComponent);
		}
	}
	
	private void updateActionStates()
	{
		saveAction.setEnabled(currentEditor != null);
		cutAction.setEnabled(currentEditor != null);
		copyAction.setEnabled(currentEditor != null);
		pasteAction.setEnabled(currentEditor != null);
		deleteAction.setEnabled(currentEditor != null);
		selectAllAction.setEnabled(currentEditor != null);
		undoAction.setEnabled(currentEditor != null && currentEditor.editorPanel.textArea.canUndo());
		redoAction.setEnabled(currentEditor != null && currentEditor.editorPanel.textArea.canRedo());
	}
	
	/**
	 * Editor handle.
	 */
	public class EditorHandle
	{
		/** Connected file. Can be null. */
		private File contentSourceFile;
		/** Timestamp of last change to buffer. */
		private long contentLastModified;
		/** Timestamp of last change to file. */
		private long contentSourceFileLastModified;
		
		/** Tab component panel. */
		private EditorTab editorTab;
		/** Editor panel. */
		private EditorPanel editorPanel;
		
		private EditorHandle(Icon icon, String title, RSyntaxTextArea textArea)
		{
			this.editorTab = new EditorTab(icon, title);
			this.editorPanel = new EditorPanel(textArea);
		}
		
		private EditorHandle(Icon icon, File contentSourceFile, RSyntaxTextArea textArea)
		{
			this(icon, contentSourceFile.getName(), textArea);
			this.contentSourceFile = contentSourceFile;
			this.contentLastModified = System.currentTimeMillis();
			this.contentSourceFileLastModified = contentSourceFile != null ? contentSourceFile.lastModified() : -1L;
		}
		
	}

	/**
	 * A single editor tab.
	 */
	private class EditorTab extends JPanel
	{
		private static final long serialVersionUID = 6056215456163910928L;
		
		private JLabel titlePanel;
		private JButton closeButton;
		
		private EditorTab(Icon icon, String title)
		{
			this.titlePanel = label(JLabel.LEADING, icon, title);
			this.closeButton = apply(button(icons.getImage("close-icon.png"), (c, e) -> removeEditorByTab(this)), (b) -> {
				b.setBorder(null);
				b.setOpaque(false);
			});
			setOpaque(false);
			containerOf(this, new FlowLayout(FlowLayout.LEADING, 16, 0),
				node(titlePanel),
				node(closeButton)
			);
		}
	}

	/**
	 * A single editor panel.
	 */
	private class EditorPanel extends JPanel
	{
		private static final long serialVersionUID = -1623390677113162251L;
		
		private RTextScrollPane scrollPane;
		private RSyntaxTextArea textArea;
		
		private EditorPanel(RSyntaxTextArea textArea)
		{
			this.textArea = textArea;
			this.scrollPane = new RTextScrollPane(textArea);
			containerOf(this, node(BorderLayout.CENTER, scrollPane));
		}
		
		public RSyntaxTextArea getTextArea() 
		{
			return textArea;
		}
		
	}
	
}
