package net.mtrop.doom.tools.gui.apps.swing.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.EditorSettingsManager;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;

import static javax.swing.BorderFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.SwingUtils.*;


/**
 * The editor panel for editing many files at once.
 * @author Matthew Tropiano
 */
public class MultiFileEditorPanel extends JPanel
{
	private static final long serialVersionUID = -3208735521175265227L;
	
	private static final FileFilter[] NO_FILTERS = new FileFilter[0];

	private static final Options DEFAULT_OPTIONS = new MultiFileEditorPanel.Options()
	{
		@Override
		public boolean hideStyleChangePanel()
		{
			return false;
		}
	};

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
		String ACTION_GOTO = "goto";
		String ACTION_FIND = "find";
	}
	
	// ======================================================================
	
	private DoomToolsEditorProvider editorProvider;
	private DoomToolsIconManager icons;
	private DoomToolsLanguageManager language;
	private EditorSettingsManager settings;
	private DoomToolsGUIUtils utils;
	
	// ======================================================================

	/** The main editor tabs. */
	private JTabbedPane mainEditorTabs;
	/** File path. */
	private JLabel filePathLabel;
	/** Caret position data. */
	private JLabel caretPositionLabel;
	/** Spacing mode. */
	private JLabel spacingModeLabel;
	/** Encoding mode. */
	private JLabel encodingModeLabel;
	/** Syntax style mode. */
	private JLabel syntaxStyleLabel;
	/** Find/Replace panel. */
	private FindReplacePanel findReplacePanel;
	
	// ======================================================================

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
	/** Goto Action */
	private Action gotoAction;
	/** Find Action */
	private Action findAction;

	/** Change encoding item. */
	private MenuNode changeEncodingMenuItem;
	/** Change language item. */
	private MenuNode changeLanguageMenuItem;
	/** Change spacing item. */
	private MenuNode changeSpacingMenuItem;

	// ======================================================================
	
	/** All editors. */
	private Map<Component, EditorHandle> allEditors;
	/** The currently selected editor. */
	private EditorHandle currentEditor;
	/** The panel listener. */
	private Listener listener;
	/** Find replace modal instance. */
	private volatile Modal<Void> findModal;

	/** Editor coding settings. */
	private EditorCodeSettings codeSettings;
	/** Editor auto-completion settings. */
	private EditorAutoCompleteSettings autoCompleteSettings;

	/**
	 * Creates a new multi-file editor panel with default options.
	 */
	public MultiFileEditorPanel()
	{
		this(DEFAULT_OPTIONS, null);
	}

	/**
	 * Creates a new multi-file editor panel with default options.
	 * @param listener the listener.
	 */
	public MultiFileEditorPanel(Listener listener)
	{
		this(DEFAULT_OPTIONS, listener);
	}
	
	/**
	 * Creates a new multi-file editor panel.
	 * @param options the panel options.
	 * @param listener the listener.
	 */
	public MultiFileEditorPanel(Options options, Listener listener)
	{
		this.editorProvider = DoomToolsEditorProvider.get();
		this.icons = DoomToolsIconManager.get();
		this.language = DoomToolsLanguageManager.get();
		this.utils = DoomToolsGUIUtils.get();
		this.settings = EditorSettingsManager.get();
		
		this.allEditors = new HashMap<>();
		this.currentEditor = null;
		this.listener = listener;
		
		this.codeSettings = settings.getDefaultEditorCodeSettings();
		this.autoCompleteSettings = settings.getDefaultEditorAutoCompleteSettings();
		
		this.mainEditorTabs = apply(tabs(TabPlacement.TOP, TabLayoutPolicy.SCROLL), (tabs) -> {
			tabs.addChangeListener((event) -> {
				int index = tabs.getSelectedIndex();
				if (index >= 0)
				{
					setCurrentEditor(allEditors.get(tabs.getTabComponentAt(tabs.getSelectedIndex())));
					tabs.getComponentAt(index).requestFocus();
				}
				else
				{
					setCurrentEditor(null);
				}
			});
		});
		
		MenuNode[] encodingNodes = createEditorEncodingMenuItems();
		MenuNode[] languageNodes = createEditorStyleMenuItems();
		MenuNode[] spacingNodes = createEditorSpacingMenuItems();
		
		this.filePathLabel = label();
		this.caretPositionLabel = label();
		this.encodingModeLabel = apply(label(), (e) -> {
			e.setComponentPopupMenu(popupMenu(encodingNodes));
		});
		this.spacingModeLabel = apply(label(), (e) -> {
			e.setComponentPopupMenu(popupMenu(spacingNodes));
		});
		this.syntaxStyleLabel = apply(label(), (e) -> {
			e.setComponentPopupMenu(popupMenu(languageNodes));
		});;
		this.findReplacePanel = new FindReplacePanel();
		
		this.saveAction = utils.createActionFromLanguageKey("texteditor.action.save", (event) -> saveCurrentEditor());
		this.saveAsAction = utils.createActionFromLanguageKey("texteditor.action.saveas", (event) -> saveCurrentEditorAs());
		this.saveAllAction = utils.createActionFromLanguageKey("texteditor.action.saveall", (event) -> saveAllEditors());
		this.closeAction = utils.createActionFromLanguageKey("texteditor.action.close", (event) -> closeCurrentEditor());
		this.closeAllAction = utils.createActionFromLanguageKey("texteditor.action.closeall", (event) -> closeAllEditors());
		this.closeAllButCurrentAction = utils.createActionFromLanguageKey("texteditor.action.closeallbutcurrent", (event) -> closeAllButCurrentEditor());
		
		this.cutAction = utils.createActionFromLanguageKey("texteditor.action.cut", (event) -> currentEditor.editorPanel.textArea.cut());
		this.copyAction = utils.createActionFromLanguageKey("texteditor.action.copy", (event) -> currentEditor.editorPanel.textArea.copy());
		this.pasteAction = utils.createActionFromLanguageKey("texteditor.action.paste", (event) -> currentEditor.editorPanel.textArea.paste());
		this.deleteAction = utils.createActionFromLanguageKey("texteditor.action.delete", (event) -> currentEditor.editorPanel.textArea.replaceSelection(""));
		this.selectAllAction = utils.createActionFromLanguageKey("texteditor.action.selectall", (event) -> currentEditor.editorPanel.textArea.selectAll());
		this.undoAction = utils.createActionFromLanguageKey("texteditor.action.undo", (event) -> currentEditor.editorPanel.textArea.undoLastAction());
		this.redoAction = utils.createActionFromLanguageKey("texteditor.action.redo", (event) -> currentEditor.editorPanel.textArea.redoLastAction());
		
		this.gotoAction = utils.createActionFromLanguageKey("texteditor.action.goto", (event) -> goToLine());
		this.findAction = utils.createActionFromLanguageKey("texteditor.action.find", (event) -> openFindDialog());
		
		this.unifiedActionMap = apply(new HashMap<>(), (map) -> {
			map.put(ActionNames.ACTION_SAVE, saveAction);
			map.put(ActionNames.ACTION_SAVE_AS, saveAsAction);
			map.put(ActionNames.ACTION_SAVE_ALL, saveAllAction);
			map.put(ActionNames.ACTION_CLOSE, closeAction);
			map.put(ActionNames.ACTION_CLOSE_ALL, closeAllAction);
			map.put(ActionNames.ACTION_CLOSE_ALL_BUT_CURRENT, closeAllButCurrentAction);
			map.put(ActionNames.ACTION_CUT, cutAction);
			map.put(ActionNames.ACTION_COPY, copyAction);
			map.put(ActionNames.ACTION_PASTE, pasteAction);
			map.put(ActionNames.ACTION_DELETE, deleteAction);
			map.put(ActionNames.ACTION_SELECT_ALL, selectAllAction);
			map.put(ActionNames.ACTION_UNDO, undoAction);
			map.put(ActionNames.ACTION_REDO, redoAction);
			map.put(ActionNames.ACTION_GOTO, gotoAction);
			map.put(ActionNames.ACTION_FIND, findAction);
		});
		
		this.changeEncodingMenuItem = utils.createItemFromLanguageKey("texteditor.action.encodings", encodingNodes);
		this.changeLanguageMenuItem = utils.createItemFromLanguageKey("texteditor.action.languages", languageNodes);
		this.changeSpacingMenuItem = utils.createItemFromLanguageKey("texteditor.action.spacing", spacingNodes);
		
		List<Node> labelNodes = new LinkedList<>();
		if (!options.hideStyleChangePanel())
			labelNodes.add(node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(syntaxStyleLabel))));
		labelNodes.add(node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(encodingModeLabel))));
		labelNodes.add(node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(spacingModeLabel))));
		labelNodes.add(node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(caretPositionLabel))));
		
		containerOf(this, borderLayout(0, 2),
			node(BorderLayout.CENTER, this.mainEditorTabs),
			node(BorderLayout.SOUTH, containerOf(gridLayout(1, 2),
				node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(filePathLabel))),
				node(containerOf(gridLayout(1, 0), labelNodes.toArray(new Node[labelNodes.size()])))
			))
		);
	}
	
	/**
	 * Creates a new editor tab with a name.
	 * @param tabName the name of the tab.
	 * @param content the initial content of the new editor.
	 */
	public void newEditor(String tabName, String content)
	{
		createNewTab(tabName, null, Charset.defaultCharset(), null, content);
	}
	
	/**
	 * Creates a new editor tab with a name.
	 * @param tabName the name of the tab.
	 * @param content the initial content of the new editor.
	 * @param encoding the default encoding.
	 * @param styleName the default style. Can be null to not force a style. 
	 */
	public void newEditor(String tabName, String content, Charset encoding, String styleName)
	{
		createNewTab(tabName, null, encoding, styleName, content);
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
	public void openFileEditor(File file, Charset encoding) throws FileNotFoundException, IOException
	{
		openFileEditor(file, encoding, null);
	}
	
	/**
	 * Opens a file into a new tab.
	 * Does nothing if the file is a directory.
	 * @param file the file to load.
	 * @param encoding the file encoding.
	 * @param styleName the style name to use for syntax highlighting and such. Can be null to autodetect.
	 * @throws FileNotFoundException if the file could not be found. 
	 * @throws IOException if the file could not be read.
	 * @throws SecurityException if the OS is forbidding the read.
	 */
	public void openFileEditor(File file, Charset encoding, String styleName) throws FileNotFoundException, IOException
	{
		if (file.isDirectory())
			return;
		
		StringWriter writer = new StringWriter();
		try (Reader reader = new InputStreamReader(new FileInputStream(file), encoding))
		{
			IOUtils.relay(reader, writer, 8192);
		}
		
		// Handle special case - if the only editor open is a new buffer with no file and no changes, remove it.
		if (getOpenEditorCount() == 1 && !currentEditor.needsToSave() && currentEditor.contentSourceFile == null)
			closeCurrentEditor();
		
		createNewTab(file.getName(), file, encoding, styleName, writer.toString());
	}
	
	/**
	 * Saves the current editor to its current file, or a new file if no current file.
	 * If no current editor, this does nothing.
	 * @return true if a successful save occurred, false otherwise.
	 * @see #saveCurrentEditorAs()
	 */
	public boolean saveCurrentEditor()
	{
		if (currentEditor == null)
			return false;

		return saveEditor(currentEditor);
	}

	/**
	 * Saves the current editor to a new file, prompting for the new file.
	 * If no current editor, this does nothing.
	 * @return true if a successful save occurred, false otherwise.
	 */
	public boolean saveCurrentEditorAs() 
	{
		if (currentEditor == null)
			return false;
		return chooseAndSaveFile(currentEditor);
	}

	/**
	 * Attempts to save all open editors.
	 */
	public void saveAllEditors()
	{
		for (Entry<Component, EditorHandle> editor : allEditors.entrySet())
		{
			EditorHandle handle = editor.getValue();
			if (!handle.needsToSave())
				continue;
			
			File editorFile;
			if ((editorFile = handle.contentSourceFile) == null)
				chooseAndSaveFile(handle);
			else
				saveEditorToFile(handle, editorFile);
		}
	}

	/**
	 * Closes the current selected editor.
	 */
	public void closeCurrentEditor() 
	{
		if (currentEditor == null)
			return;
		
		attemptToCloseEditor(currentEditor);
	}

	/**
	 * Attempts to close all editors.
	 * @return true if it is safe to close all editors, and all editors were closed, false if one editor closing was cancelled.
	 */
	public boolean closeAllEditors() 
	{
		// Find all editors to close.
		Set<Component> tabsToClose = new HashSet<>();
		
		for (Entry<Component, EditorHandle> editor : allEditors.entrySet())
		{
			EditorHandle handle = editor.getValue();
			
			if (!editorCanClose(handle))
				return false;
			
			tabsToClose.add(editor.getKey());
		}
		
		for (Component tab : tabsToClose)
			removeEditorByTab(tab);
		return true;
	}

	/**
	 * Closes all but the current editor.
	 * @return true if it is safe to close all closeable editors, and all closeable editors were closed, false if one editor closing was cancelled.
	 */
	public boolean closeAllButCurrentEditor() 
	{
		if (currentEditor == null)
			return false;
		
		// Find all editors to close.
		Set<Component> tabsToClose = new HashSet<>();
		
		for (Entry<Component, EditorHandle> editor : allEditors.entrySet())
		{
			EditorHandle handle = editor.getValue();
			if (currentEditor == handle)
				continue;
			
			if (!editorCanClose(handle))
				return false;
			
			tabsToClose.add(editor.getKey());
		}
		
		for (Component tab : tabsToClose)
			removeEditorByTab(tab);
		return true;
	}

	/**
	 * Calls a consumer function for the current editor, if any.
	 * @param consumer the consumer to call and pass the current editor handle to.
	 */
	public void forCurrentEditor(Consumer<EditorHandle> consumer)
	{
		if (currentEditor != null)
			consumer.accept(currentEditor);
	}

	/**
	 * Calls a consumer function for each open editor.
	 * @param consumer the consumer to call and pass the editor handle to.
	 */
	public void forEachOpenEditor(Consumer<EditorHandle> consumer)
	{
		allEditors.values().forEach(consumer);
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
	 * @return the change encoding menu item.
	 */
	public MenuNode getChangeEncodingMenuItem() 
	{
		return changeEncodingMenuItem;
	}

	/**
	 * @return the change language menu item.
	 */
	public MenuNode getChangeLanguageMenuItem() 
	{
		return changeLanguageMenuItem;
	}
	
	/**
	 * @return the change spacing menu item.
	 */
	public MenuNode getChangeSpacingMenuItem() 
	{
		return changeSpacingMenuItem;
	}
	
	/**
	 * Gets the amount of open editors.
	 * @return the amount of editors still open.
	 */
	public int getOpenEditorCount()
	{
		return allEditors.size();
	}
	
	/**
	 * Gets the amount of unsaved editors.
	 * @return the amount of editors still unsaved.
	 */
	public int getUnsavedEditorCount()
	{
		int count = 0;
		for (Entry<Component, EditorHandle> editor : allEditors.entrySet())
		{
			if (editor.getValue().needsToSave())
				count++;
		}
		
		return count;
	}
	
	/**
	 * Gets the currently-selected text in the current editor.
	 * @return the currently-selected text, an empty string if no selection, or null if no current editor.
	 */
	public String getCurrentEditorSelectedText()
	{
		if (currentEditor == null)
			return null;
		String text = getCurrentEditorTextArea().getSelectedText();
		if (text == null)
			return "";
		else
			return text;
	}
	
	/**
	 * Creates a new editor, returning the editor tab.
	 * @param title the tab title.
	 * @param attachedFile the content source file (if any, can be null).
	 * @param fileCharset the file's source charset.
	 * @param styleName the default style. Can be null to not force a style. 
	 * @param content the content.
	 * @return the editor handle created.
	 */
	protected final EditorHandle createNewTab(String title, File attachedFile, Charset fileCharset, String styleName, String content)
	{
		RSyntaxTextArea textArea = new RSyntaxTextArea();
		
		if (styleName == null)
		{
			if (attachedFile != null)
				styleName = editorProvider.getStyleByFile(attachedFile);
			else
				styleName = SyntaxConstants.SYNTAX_STYLE_NONE;
		}
		
		textArea.setText(content);
		textArea.setCaretPosition(0);
		
		EditorHandle handle = attachedFile != null 
			? new EditorHandle(attachedFile, fileCharset, styleName, textArea) 
			: new EditorHandle(title, fileCharset, styleName, textArea)
		;

		// ==================================================================
		
		settings.getDefaultEditorViewSettings().applyTo(textArea);
		codeSettings.applyTo(textArea);
		setEditorViewSettingsByContent(textArea, content);
		
		// ==================================================================
		
		allEditors.put(handle.editorTab, handle);

		mainEditorTabs.addTab(null, handle.editorPanel);
		
		// The tab just added will be at the end.
		int tabIndex = mainEditorTabs.getTabCount() - 1;
		mainEditorTabs.setTabComponentAt(tabIndex, handle.editorTab);
		
		mainEditorTabs.setSelectedIndex(tabIndex);
		if (mainEditorTabs.getTabCount() == 1) // workaround for weird implementation.
			setCurrentEditor(handle);
		textArea.requestFocus();
		
		if (listener != null)
			listener.onOpen(handle);
		
		return handle;
	}

	/**
	 * Called to get the last path accessed by the editor for saving or opening a file.
	 * @return the file, or null for no file.
	 */
	protected File getLastPathTouched()
	{
		return null;
	}

	/**
	 * Called to get the last path accessed by the editor for saving or opening a file.
	 * @param saved the file saved.
	 */
	protected void setLastPathTouched(File saved)
	{
		// Do nothing by default.
	}

	/**
	 * Called to get the save file types.
	 * @return the file filters for saving files.
	 */
	protected FileFilter[] getSaveFileTypes()
	{
		return NO_FILTERS;
	}
	
	/**
	 * Called on a file chosen for saving in order to do some kind of operation
	 * like fill in a missing extension or whatever.
	 * By default, this does nothing, returning the selected file as-is.
	 * @param selectedFilter the currently selected file filter (can be used to determine type).
	 * @param selectedFile the selected file (no null)
	 * @return the new file path.
	 */
	protected File transformSaveFile(FileFilter selectedFilter, File selectedFile)
	{
		return selectedFile;
	}
	
	// Opens the save file dialog for saving a file.
	private boolean chooseAndSaveFile(EditorHandle handle) 
	{
		File editorFile;
		editorFile = utils.chooseFile(this,
			language.getText("texteditor.action.save.title", handle.editorTab.getTabTitle()), 
			language.getText("texteditor.action.save.approve"), 
			this::getLastPathTouched,
			this::setLastPathTouched,
			this::transformSaveFile,
			getSaveFileTypes()
		);
	
		if (editorFile == null)
			return false;
		
		if (editorFile.exists() && SwingUtils.noTo(this, language.getText("texteditor.action.save.overwrite", editorFile)))
			return false;
		
		return saveEditorToFile(handle, editorFile);
	}

	private void setCurrentEditor(EditorHandle handle)
	{
		currentEditor = handle;
		updateActionStates();
		updateLabels();
		updateEditorHooks();
		if (listener != null)
			listener.onCurrentEditorChange(handle);
		if (currentEditor != null)
			currentEditor.editorPanel.textArea.requestFocus();
	}
	
	private void removeEditorByTab(Component tabComponent)
	{
		int index;
		if ((index = mainEditorTabs.indexOfTabComponent(tabComponent)) >= 0)
		{
			mainEditorTabs.remove(index);
			EditorHandle handle = allEditors.remove(tabComponent);
			if (handle != null && listener != null)
				listener.onClose(handle);
		}
		
		updateActionStates();
	}

	private void updateEditorHooks()
	{
		if (currentEditor == null)
			return;
		findReplacePanel.setTarget(currentEditor.editorPanel.textArea);
	}
	
	private void updateActionEditorStates()
	{
		if (currentEditor == null)
		{
			saveAction.setEnabled(false);
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
		}
		else
		{
			saveAction.setEnabled(true);
			undoAction.setEnabled(currentEditor.editorPanel.textArea.canUndo());
			redoAction.setEnabled(currentEditor.editorPanel.textArea.canRedo());
		}
	}

	private void updateTextActionStates()
	{
		boolean editorPresent = currentEditor != null;
		if (editorPresent)
		{
			RSyntaxTextArea textArea = currentEditor.editorPanel.textArea;
			boolean hasSelection = (textArea.getSelectionEnd() - textArea.getSelectionStart()) > 0; 
			cutAction.setEnabled(hasSelection);
			copyAction.setEnabled(hasSelection);
			deleteAction.setEnabled(hasSelection);
		}
		else
		{
			cutAction.setEnabled(false);
			copyAction.setEnabled(false);
			deleteAction.setEnabled(false);
		}
	}
	
	private void updateActionStates()
	{
		updateActionEditorStates();
		updateTextActionStates();
		boolean editorPresent = currentEditor != null;
		saveAsAction.setEnabled(editorPresent);
		saveAllAction.setEnabled(getOpenEditorCount() > 0);
		pasteAction.setEnabled(editorPresent);
		selectAllAction.setEnabled(editorPresent);
		gotoAction.setEnabled(editorPresent);
		findAction.setEnabled(editorPresent);
	}
	
	private void updateLabels()
	{
		if (currentEditor != null)
		{
			RSyntaxTextArea textArea = currentEditor.editorPanel.textArea;
			int line = textArea.getCaretLineNumber() + 1;
			int offset = textArea.getCaretOffsetFromLineStart();
			int characterOffset = textArea.getCaretPosition();
			int tabSize = textArea.getTabSize();
			boolean usesSpaces = textArea.getTabsEmulated();
			
			int selection = textArea.getSelectionEnd() - textArea.getSelectionStart(); 
			
			File file = currentEditor.getContentSourceFile();
			filePathLabel.setText(file != null ? file.getAbsolutePath() : "");
			syntaxStyleLabel.setText(currentEditor.currentStyle);
			caretPositionLabel.setText(line + " : " + offset + " : " + characterOffset + (selection > 0 ? " [" + selection + "]" : ""));
			encodingModeLabel.setText(currentEditor.contentCharset.displayName());
			spacingModeLabel.setText((usesSpaces ? "Spaces: " : "Tabs: ") + tabSize);
		}
		else
		{
			filePathLabel.setText(" ");
			syntaxStyleLabel.setText(" ");
			caretPositionLabel.setText(" ");
			encodingModeLabel.setText(" ");
			spacingModeLabel.setText(" ");
		}
	}
	
	private void updateActionsIfCurrent(EditorHandle handle)
	{
		if (handle == currentEditor)
			updateActionEditorStates();
	}

	/**
	 * Attempts to close an editor.
	 */
	private void attemptToCloseEditor(EditorHandle handle)
	{
		if (editorCanClose(handle))
			removeEditorByTab(handle.editorTab);
	}
	
	/**
	 * Decides whether an editor can close or not.
	 * @return true if handled, false to halt closing.
	 */
	private boolean editorCanClose(EditorHandle handle)
	{
		if (!handle.needsToSave())
			return true;
		
		Boolean saveChoice = modal(this, utils.getWindowIcons(), 
			language.getText("texteditor.action.save.modal.title"),
			containerOf(label(language.getText("texteditor.action.save.modal.message", handle.editorTab.getTabTitle()))), 
			utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.save", true),
			utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.nosave", false),
			utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)null)
		).openThenDispose();
		
		if (saveChoice == null)
			return false;
		
		if (saveChoice)
			return saveEditor(handle);
		return true;
	}

	private boolean saveEditor(EditorHandle handle)
	{
		File editorFile;
		if ((editorFile = handle.contentSourceFile) == null)
		{
			return chooseAndSaveFile(handle);
		}
		else if (currentEditor.needsToSave())
		{
			return saveEditorToFile(handle, editorFile);
		}
		else
		{
			return true;
		}
	}

	private boolean saveEditorToFile(EditorHandle handle, File targetFile)
	{
		Charset targetCharset = handle.contentCharset;
		String content = handle.editorPanel.textArea.getText();
	
		StringReader reader = new StringReader(content);
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), targetCharset)) {
			IOUtils.relay(reader, writer, 8192);
		} catch (IOException e) {
			error(this, language.getText("texteditor.action.save.error", targetFile.getAbsolutePath()));
			return false;
		}
		
		handle.onSaveChange(targetFile);
		updateActionStates();
		
		if (listener != null)
			listener.onSave(handle);
		return true;
	}

	/**
	 * Gets the currently-selected text in the current editor.
	 * @return the currently-selected text, an empty string if no selection, or null if no current editor.
	 */
	private RSyntaxTextArea getCurrentEditorTextArea()
	{
		if (currentEditor == null)
			return null;
		return currentEditor.editorPanel.textArea;
	}
	
	/**
	 * Clears all highlights/markers on the every editor.
	 */
	private void clearAllHighlights()
	{
		forEachOpenEditor((editor) -> editor.editorPanel.textArea.getHighlighter().removeAllHighlights());
	}
	
	private MenuNode[] createEditorEncodingMenuItems()
	{
		Set<Charset> charsets = editorProvider.getAvailableCommonCharsets();
		Set<Charset> otherCharsets = editorProvider.getAvailableOtherCharsets();
		
		List<MenuNode> out = new ArrayList<>();
		for (Charset charset : charsets)
			out.add(menuItem(charset.displayName(), (c, e) -> changeCurrentEditorEncoding(charset)));
		out.add(separator());
		
		List<MenuNode> others = new ArrayList<>();
		for (Charset charset : otherCharsets)
			others.add(menuItem(charset.displayName(), (c, e) -> changeCurrentEditorEncoding(charset)));
		out.add(utils.createItemFromLanguageKey("texteditor.action.encodings.other", others.toArray(new MenuNode[others.size()])));
		return out.toArray(new MenuNode[out.size()]);
	}
	
	private MenuNode[] createEditorStyleMenuItems()
	{
		Map<String, String> languages = editorProvider.getAvailableLanguageMap();
		Map<String, String> otherLanguages = editorProvider.getOtherAvailableLanguageMap();
		
		List<MenuNode> out = new ArrayList<>();
		for (Map.Entry<String, String> entry : languages.entrySet())
			out.add(menuItem(entry.getKey(), (c, e) -> changeCurrentEditorStyle(entry.getValue())));
		out.add(separator());
		
		List<MenuNode> others = new ArrayList<>();
		for (Map.Entry<String, String> entry : otherLanguages.entrySet())
			others.add(menuItem(entry.getKey(), (c, e) -> changeCurrentEditorStyle(entry.getValue())));
		out.add(utils.createItemFromLanguageKey("texteditor.action.languages.other", others.toArray(new MenuNode[others.size()])));
		return out.toArray(new MenuNode[out.size()]);
	}
	
	private MenuNode[] createEditorSpacingMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.spacing.spaces",
				menuItem("2", KeyEvent.VK_2, (c, e) -> changeCurrentEditorSpacing(true, 2)),
				menuItem("3", KeyEvent.VK_3, (c, e) -> changeCurrentEditorSpacing(true, 3)),
				menuItem("4", KeyEvent.VK_4, (c, e) -> changeCurrentEditorSpacing(true, 4)),
				menuItem("5", KeyEvent.VK_5, (c, e) -> changeCurrentEditorSpacing(true, 5)),
				menuItem("6", KeyEvent.VK_6, (c, e) -> changeCurrentEditorSpacing(true, 6)),
				menuItem("7", KeyEvent.VK_7, (c, e) -> changeCurrentEditorSpacing(true, 7)),
				menuItem("8", KeyEvent.VK_8, (c, e) -> changeCurrentEditorSpacing(true, 8))
			), utils.createItemFromLanguageKey("texteditor.action.spacing.tabs",
				menuItem("2", KeyEvent.VK_2, (c, e) -> changeCurrentEditorSpacing(false, 2)),
				menuItem("3", KeyEvent.VK_3, (c, e) -> changeCurrentEditorSpacing(false, 3)),
				menuItem("4", KeyEvent.VK_4, (c, e) -> changeCurrentEditorSpacing(false, 4)),
				menuItem("5", KeyEvent.VK_5, (c, e) -> changeCurrentEditorSpacing(false, 5)),
				menuItem("6", KeyEvent.VK_6, (c, e) -> changeCurrentEditorSpacing(false, 6)),
				menuItem("7", KeyEvent.VK_7, (c, e) -> changeCurrentEditorSpacing(false, 7)),
				menuItem("8", KeyEvent.VK_8, (c, e) -> changeCurrentEditorSpacing(false, 8))
			)
		); 
	}
	
	/**
	 * Changes the encoding of the text in the current editor.
	 * @param charset the new charset for the editor.
	 */
	private void changeCurrentEditorEncoding(final Charset charset)
	{
		forCurrentEditor((editor) -> editor.changeEncoding(charset));
		updateLabels();
	}

	/**
	 * Changes the language style in the current editor.
	 * @param styleName the style name.
	 */
	private void changeCurrentEditorStyle(final String styleName)
	{
		forCurrentEditor((editor) -> editor.changeStyleName(styleName));
		updateLabels();
	}

	/**
	 * Changes the spacing style and input in the current editor.
	 */
	private void changeCurrentEditorSpacing(final boolean spaces, final int amount)
	{
		forCurrentEditor((editor) -> editor.changeSpacing(spaces, amount));
		updateLabels();
	}

	// Opens the "Go to Line" dialog. 
	private void goToLine()
	{
		final RSyntaxTextArea textArea = getCurrentEditorTextArea();
		final JFormField<Integer> lineField = integerField(textArea.getCaretLineNumber() + 1);
		int lineMax = textArea.getLineCount();
		
		Integer selected = utils.createSettingsModal(
			language.getText("texteditor.modal.goto.title"),
			containerOf(gridLayout(2, 1, 0, 4),
				node(label(language.getText("texteditor.modal.goto.message", lineMax))),
				node(lineField)
			),
			(panel) -> lineField.getValue(),
			utils.createChoiceFromLanguageKey("texteditor.modal.goto.choice.goto", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", false)
		);
		
		if (selected != null) 
		{
			try {
				textArea.setCaretPosition(textArea.getLineStartOffset(selected - 1));
			} catch (BadLocationException e) {
				error(this, language.getText("texteditor.modal.goto.error"));
			}
		}
	}

	// Opens the find dialog.
	private void openFindDialog()
	{
		findReplacePanel.setFind(getCurrentEditorSelectedText());
		
		if (findModal != null)
		{
			findModal.requestFocus();
			return;
		}
		
		findModal = utils.createModal(
			language.getText("texteditor.modal.find.title"),
			ModalityType.MODELESS,
			containerOf(createEmptyBorder(8, 8, 8, 8), node(findReplacePanel))
		);
		findModal.addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosed(WindowEvent e) 
			{
				clearAllHighlights();
				findModal.dispose();
				findModal = null;
			}
		});
		findModal.open();
	}

	// Sets editor view settings by scanning content.
	private static void setEditorViewSettingsByContent(RSyntaxTextArea textArea, String content)
	{
		try (BufferedReader br = new BufferedReader(new StringReader(content)))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith(" ")) // spaces?
				{
					int i = 1;
					textArea.setTabsEmulated(true);
					while (line.startsWith(" ", i))
						i++;
					if (i > 1)
						textArea.setTabSize(Math.min(i, 8));
					break;
				}
				else if (line.startsWith("\t"))
				{
					textArea.setTabsEmulated(false);
					break;
				}
			}
		}
		catch (IOException e) 
		{
			// Do nothing.
		}
	}
	
	/**
	 * The listener.
	 */
	public interface Listener
	{
		/**
		 * Called on a current editor changing focus.
		 * @param handle the new current handle.
		 */
		void onCurrentEditorChange(EditorHandle handle);
		
		/**
		 * Called when an editor is opened or created.
		 * Assume that the editor was successfully opened.
		 * Can be called multiple times, if many at once were opened.
		 * @param handle the handle opened.
		 */
		void onOpen(EditorHandle handle);
		
		/**
		 * Called when an editor is saved.
		 * Assume that the editor was successfully saved.
		 * Can be called multiple times, if many at once were saved.
		 * @param handle the handle saved.
		 */
		void onSave(EditorHandle handle);
		
		/**
		 * Called when an editor is closed.
		 * Assume that all precautions and checks were taken to ensure that it can be closed.
		 * Can be called multiple times, if many at once were closed.
		 * @param handle the handle closed.
		 */
		void onClose(EditorHandle handle);
	}

	/**
	 * Panel options.
	 */
	public interface Options
	{
		/**
		 * @return true to allow style changing, false to forbid it.
		 */
		boolean hideStyleChangePanel();
	}
	
	/**
	 * Editor handle.
	 */
	public class EditorHandle
	{
		private Icon savedIcon;
		private Icon unsavedIcon;
		
		/** Connected file. Can be null. */
		private File contentSourceFile;
		/** Buffer charset, for saving. */
		private Charset contentCharset;
		/** Timestamp of last change to buffer. */
		private long contentLastModified;
		/** Timestamp of last change to file. */
		private long contentSourceFileLastModified;
		/** Current RSyntaxTextArea style. */
		private String currentStyle;

		/** Current AutoCompletion engine. */
		private AutoCompletion currentAutoCompletion;

		/** Tab component panel. */
		private EditorTab editorTab;
		/** Editor panel. */
		private EditorPanel editorPanel;
		
		private EditorHandle(String title, Charset sourceCharset, String styleName, RSyntaxTextArea textArea)
		{
			this.savedIcon = icons.getImage("script.png");
			this.unsavedIcon = icons.getImage("script-unsaved.png");

			this.contentSourceFile = null;
			this.contentCharset = sourceCharset;
			this.contentLastModified = -1L;
			this.contentSourceFileLastModified = -1L;
			this.currentStyle = styleName;
			
			this.editorTab = new EditorTab(savedIcon, title, (c, e) -> attemptToCloseEditor(this));
			this.editorPanel = new EditorPanel(textArea);
			textArea.setSyntaxEditingStyle(styleName);
			applyAutoComplete(styleName);
			textArea.getDocument().addDocumentListener(new DocumentListener()
			{
				@Override
				public void removeUpdate(DocumentEvent e) 
				{
					onChange();
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) 
				{
					onChange();
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) 
				{
					onChange();
				}
			});
			
		}
		
		private EditorHandle(File contentSourceFile, Charset sourceCharset, String styleName, RSyntaxTextArea textArea)
		{
			this(contentSourceFile.getName(), sourceCharset, styleName, textArea);
			this.contentSourceFile = contentSourceFile;
			this.contentCharset = sourceCharset;
			this.contentLastModified = contentSourceFile.lastModified();
			this.contentSourceFileLastModified = contentSourceFile.lastModified();
			this.editorTab.setTabIcon(savedIcon);
		}

		/**
		 * Changes the encoding of the editor.
		 * @param newCharset the new charset.
		 */
		public void changeEncoding(Charset newCharset)
		{
			contentCharset = newCharset;
			updateLabels();
		}
		
		/**
		 * Changes the syntax style.
		 * @param styleName the new style.
		 */
		public void changeStyleName(String styleName)
		{
			currentStyle = styleName;
			editorPanel.textArea.setSyntaxEditingStyle(styleName);
			applyAutoComplete(styleName);
			updateLabels();
		}
		
		/**
		 * Changes the spacing style.
		 * @param spaces if true, spaces. false is tabs.
		 * @param amount the amount of spaces.
		 */
		public void changeSpacing(boolean spaces, int amount)
		{
			editorPanel.textArea.setTabsEmulated(spaces);
			editorPanel.textArea.setTabSize(amount);
			updateLabels();
		}
		
		/**
		 * @return the editor tab name.
		 */
		public String getEditorTabName()
		{
			return editorTab.getTabTitle();
		}

		/**
		 * @return the source file. Can be null.
		 */
		public File getContentSourceFile() 
		{
			return contentSourceFile;
		}

		/**
		 * @return the editor content.
		 */
		public String getContent()
		{
			return editorPanel.textArea.getText();
		}

		/**
		 * @return true if this editor has unsaved data.
		 */
		public boolean needsToSave()
		{
			return contentLastModified > contentSourceFileLastModified;
		}
		
		// Applies the auto-complete.
		private void applyAutoComplete(String styleName)
		{
			AutoCompletion autoCompletion = editorProvider.createAutoCompletionByStyle(styleName);
			autoCompleteSettings.applyTo(autoCompletion);
			if (currentAutoCompletion != null)
				currentAutoCompletion.uninstall();
			autoCompletion.install(editorPanel.textArea);
			currentAutoCompletion = autoCompletion;
		}
		
		// Should call to update title on tab and timestamps.
		private void onSaveChange(File path)
		{
			editorTab.setTabIcon(savedIcon);
			editorTab.setTabTitle(path.getName());
			contentSourceFile = path;
			contentSourceFileLastModified = path.lastModified();
		}
		
		private void onChange()
		{
			editorTab.setTabIcon(unsavedIcon);
			contentLastModified = System.currentTimeMillis();
			updateActionsIfCurrent(this);
		}

	}

	/**
	 * A single editor tab.
	 */
	protected class EditorTab extends JPanel
	{
		private static final long serialVersionUID = 6056215456163910928L;
		
		private JLabel titleLabel;
		private JButton closeButton;
		
		private EditorTab(Icon icon, String title, ComponentActionHandler<JButton> closeHandler)
		{
			this.titleLabel = label(JLabel.LEADING, icon, title);
			this.closeButton = apply(button(icons.getImage("close-icon.png"), closeHandler), (b) -> {
				b.setBorder(null);
				b.setOpaque(false);
			});
			setOpaque(false);
			containerOf(this, (Border)null, flowLayout(Flow.LEADING, 8, 0),
				node(titleLabel),
				node(closeButton)
			);
		}
		
		private void setTabIcon(Icon icon)
		{
			titleLabel.setIcon(icon);
		}
		
		private void setTabTitle(String title)
		{
			titleLabel.setText(title);
		}
		
		private String getTabTitle()
		{
			return titleLabel.getText();
		}
		
	}

	/**
	 * A single editor panel.
	 */
	protected class EditorPanel extends JPanel
	{
		private static final long serialVersionUID = -1623390677113162251L;
		
		private RTextScrollPane scrollPane;
		private RSyntaxTextArea textArea;
		
		private EditorPanel(RSyntaxTextArea textArea)
		{
			this.textArea = textArea;
			this.scrollPane = new RTextScrollPane(textArea);
			
			textArea.addCaretListener((e) -> {
				updateTextActionStates();
				updateLabels();
			});
			
			textArea.addHyperlinkListener((hevent) -> {
				if (hevent.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
					return;

				try {
					SwingUtils.browse(hevent.getURL().toURI());
				} catch (IOException | URISyntaxException e1) {
					// Do nothing.
				}
			});
			
			containerOf(this, node(BorderLayout.CENTER, scrollPane));
		}
		
		public RSyntaxTextArea getTextArea() 
		{
			return textArea;
		}
		
	}
	
	/**
	 * An encapsulation of a series of editor view settings.  
	 */
	public static class EditorViewSettings
	{
		private int tabSize;
		private boolean tabsEmulated;
		private boolean lineWrap;
		private boolean wrapStyleWord;
		
		public EditorViewSettings()
		{
			setTabSize(4);
			setTabsEmulated(false);
			setLineWrap(false);
			setWrapStyleWord(false);
		}
		
		public int getTabSize() 
		{
			return tabSize;
		}

		public boolean isTabsEmulated() 
		{
			return tabsEmulated;
		}

		public boolean isLineWrap()
		{
			return lineWrap;
		}

		public boolean isWrapStyleWord()
		{
			return wrapStyleWord;
		}

		public void setTabSize(int tabSize)
		{
			this.tabSize = tabSize;
		}

		public void setTabsEmulated(boolean tabsEmulated) 
		{
			this.tabsEmulated = tabsEmulated;
		}

		public void setLineWrap(boolean lineWrap)
		{
			this.lineWrap = lineWrap;
		}

		public void setWrapStyleWord(boolean wrapStyleWord) 
		{
			this.wrapStyleWord = wrapStyleWord;
		}

		public void applyTo(RSyntaxTextArea target)
		{
			target.setTabSize(tabSize);
			target.setTabsEmulated(tabsEmulated);
			target.setLineWrap(lineWrap);
			target.setWrapStyleWord(wrapStyleWord);
		}
	}
	
	/**
	 * An encapsulation of a series of editor settings around code building.  
	 */
	public static class EditorCodeSettings
	{
		private boolean marginLineEnabled;
		private int marginLinePosition;
		
		private boolean roundedSelectionEdges;
		private boolean highlightCurrentLine;
		private boolean animateBracketMatching;
		private boolean autoIndentEnabled;
		private boolean bracketMatchingEnabled;
		private boolean clearWhitespaceLinesEnabled;
		private boolean closeCurlyBraces;
		private boolean closeMarkupTags;
		private boolean codeFoldingEnabled;
		private boolean eolMarkersVisible;
		private boolean highlightSecondaryLanguages;
		private boolean showMatchedBracketPopup;
		private boolean useFocusableTips;
		private boolean whitespaceVisible;
		private boolean paintTabLines;
		private boolean markOccurrences;
		private boolean markAllOnOccurrenceSearches;
		private int markOccurrencesDelay;
		private boolean paintMatchedBracketPair;
		private boolean paintMarkOccurrencesBorder;
		private boolean useSelectedTextColor;
		private int parserDelay;
		
		private boolean hyperlinksEnabled;
		private int linkScanningMask;

		public EditorCodeSettings()
		{
			setMarginLineEnabled(true);
			setMarginLinePosition(80);
			
			setCodeFoldingEnabled(true);
			setCloseCurlyBraces(true);
			setCloseMarkupTags(true);

			setAutoIndentEnabled(true);
			setPaintTabLines(true);
			setWhitespaceVisible(false);
			setEOLMarkersVisible(false);
			setClearWhitespaceLinesEnabled(true);

			setBracketMatchingEnabled(true);
			setShowMatchedBracketPopup(true);
			setPaintMatchedBracketPair(true);
			setAnimateBracketMatching(false);
			
			setMarkOccurrences(true);
			setMarkAllOnOccurrenceSearches(false);
			setMarkOccurrencesDelay(500);
			setPaintMarkOccurrencesBorder(false);

			setUseSelectedTextColor(false);
			setRoundedSelectionEdges(false);
			setHighlightCurrentLine(true);
			setHighlightSecondaryLanguages(true);

			setParserDelay(1000);
			setUseFocusableTips(false);

			setHyperlinksEnabled(true);
			setLinkScanningMask(KeyEvent.CTRL_DOWN_MASK);
		}

		public boolean isMarginLineEnabled()
		{
			return marginLineEnabled;
		}

		public int getMarginLinePosition() 
		{
			return marginLinePosition;
		}
		
		public boolean isRoundedSelectionEdges()
		{
			return roundedSelectionEdges;
		}

		public boolean isHighlightCurrentLine() 
		{
			return highlightCurrentLine;
		}

		public boolean isAnimateBracketMatching()
		{
			return animateBracketMatching;
		}

		public boolean isAutoIndentEnabled()
		{
			return autoIndentEnabled;
		}

		public boolean isBracketMatchingEnabled()
		{
			return bracketMatchingEnabled;
		}

		public boolean isClearWhitespaceLinesEnabled()
		{
			return clearWhitespaceLinesEnabled;
		}

		public boolean isCloseCurlyBraces()
		{
			return closeCurlyBraces;
		}

		public boolean isCloseMarkupTags()
		{
			return closeMarkupTags;
		}

		public boolean isCodeFoldingEnabled()
		{
			return codeFoldingEnabled;
		}

		public boolean isEOLMarkersVisible()
		{
			return eolMarkersVisible;
		}

		public boolean isHighlightSecondaryLanguages()
		{
			return highlightSecondaryLanguages;
		}

		public boolean isShowMatchedBracketPopup()
		{
			return showMatchedBracketPopup;
		}

		public boolean isUseFocusableTips()
		{
			return useFocusableTips;
		}

		public boolean isWhitespaceVisible()
		{
			return whitespaceVisible;
		}

		public boolean isPaintTabLines()
		{
			return paintTabLines;
		}

		public boolean isMarkOccurrences()
		{
			return markOccurrences;
		}

		public boolean isMarkAllOnOccurrenceSearches()
		{
			return markAllOnOccurrenceSearches;
		}

		public int getMarkOccurrencesDelay()
		{
			return markOccurrencesDelay;
		}

		public boolean isPaintMatchedBracketPair() 
		{
			return paintMatchedBracketPair;
		}

		public boolean isPaintMarkOccurrencesBorder()
		{
			return paintMarkOccurrencesBorder;
		}

		public boolean isUseSelectedTextColor()
		{
			return useSelectedTextColor;
		}

		public int getParserDelay()
		{
			return parserDelay;
		}

		public boolean isHyperlinksEnabled()
		{
			return hyperlinksEnabled;
		}

		public int getLinkScanningMask()
		{
			return linkScanningMask;
		}

		public void setMarginLineEnabled(boolean marginLineEnabled)
		{
			this.marginLineEnabled = marginLineEnabled;
		}
		
		public void setMarginLinePosition(int marginLinePosition) 
		{
			this.marginLinePosition = marginLinePosition;
		}

		public void setRoundedSelectionEdges(boolean roundedSelectionEdges)
		{
			this.roundedSelectionEdges = roundedSelectionEdges;
		}

		public void setHighlightCurrentLine(boolean highlightCurrentLine)
		{
			this.highlightCurrentLine = highlightCurrentLine;
		}

		public void setAnimateBracketMatching(boolean animateBracketMatching)
		{
			this.animateBracketMatching = animateBracketMatching;
		}

		public void setAutoIndentEnabled(boolean autoIndentEnabled)
		{
			this.autoIndentEnabled = autoIndentEnabled;
		}

		public void setBracketMatchingEnabled(boolean bracketMatchingEnabled)
		{
			this.bracketMatchingEnabled = bracketMatchingEnabled;
		}

		public void setClearWhitespaceLinesEnabled(boolean clearWhitespaceLinesEnabled)
		{
			this.clearWhitespaceLinesEnabled = clearWhitespaceLinesEnabled;
		}

		public void setCloseCurlyBraces(boolean closeCurlyBraces)
		{
			this.closeCurlyBraces = closeCurlyBraces;
		}

		public void setCloseMarkupTags(boolean closeMarkupTags) 
		{
			this.closeMarkupTags = closeMarkupTags;
		}

		public void setCodeFoldingEnabled(boolean codeFoldingEnabled)
		{
			this.codeFoldingEnabled = codeFoldingEnabled;
		}

		public void setEOLMarkersVisible(boolean eolMarkersVisible)
		{
			this.eolMarkersVisible = eolMarkersVisible;
		}

		public void setHighlightSecondaryLanguages(boolean highlightSecondaryLanguages)
		{
			this.highlightSecondaryLanguages = highlightSecondaryLanguages;
		}

		public void setShowMatchedBracketPopup(boolean showMatchedBracketPopup)
		{
			this.showMatchedBracketPopup = showMatchedBracketPopup;
		}

		public void setUseFocusableTips(boolean useFocusableTips)
		{
			this.useFocusableTips = useFocusableTips;
		}

		public void setWhitespaceVisible(boolean whitespaceVisible)
		{
			this.whitespaceVisible = whitespaceVisible;
		}

		public void setPaintTabLines(boolean paintTabLines) 
		{
			this.paintTabLines = paintTabLines;
		}

		public void setMarkOccurrences(boolean markOccurrences)
		{
			this.markOccurrences = markOccurrences;
		}

		public void setMarkAllOnOccurrenceSearches(boolean markAllOnOccurrenceSearches)
		{
			this.markAllOnOccurrenceSearches = markAllOnOccurrenceSearches;
		}

		public void setMarkOccurrencesDelay(int markOccurrencesDelay)
		{
			this.markOccurrencesDelay = markOccurrencesDelay;
		}

		public void setPaintMatchedBracketPair(boolean paintMatchedBracketPair)
		{
			this.paintMatchedBracketPair = paintMatchedBracketPair;
		}

		public void setPaintMarkOccurrencesBorder(boolean paintMarkOccurrencesBorder)
		{
			this.paintMarkOccurrencesBorder = paintMarkOccurrencesBorder;
		}

		public void setUseSelectedTextColor(boolean useSelectedTextColor)
		{
			this.useSelectedTextColor = useSelectedTextColor;
		}

		public void setParserDelay(int parserDelay)
		{
			this.parserDelay = parserDelay;
		}

		public void setHyperlinksEnabled(boolean hyperlinksEnabled)
		{
			this.hyperlinksEnabled = hyperlinksEnabled;
		}

		public void setLinkScanningMask(int linkScanningMask)
		{
			this.linkScanningMask = linkScanningMask;
		}

		public void applyTo(RSyntaxTextArea target)
		{
			target.setMarginLineEnabled(marginLineEnabled);
			target.setMarginLinePosition(marginLinePosition);
			
			target.setRoundedSelectionEdges(roundedSelectionEdges);
			target.setHighlightCurrentLine(highlightCurrentLine);
			target.setAnimateBracketMatching(animateBracketMatching);
			target.setAutoIndentEnabled(autoIndentEnabled);
			target.setBracketMatchingEnabled(bracketMatchingEnabled);
			target.setClearWhitespaceLinesEnabled(clearWhitespaceLinesEnabled);
			target.setCloseCurlyBraces(closeCurlyBraces);
			target.setCloseMarkupTags(closeMarkupTags);
			target.setCodeFoldingEnabled(codeFoldingEnabled);
			target.setEOLMarkersVisible(eolMarkersVisible);
			target.setHighlightSecondaryLanguages(highlightSecondaryLanguages);
			target.setShowMatchedBracketPopup(showMatchedBracketPopup);
			target.setUseFocusableTips(useFocusableTips);
			target.setWhitespaceVisible(whitespaceVisible);
			target.setPaintTabLines(paintTabLines);
			target.setMarkOccurrences(markOccurrences);
			target.setMarkAllOnOccurrenceSearches(markAllOnOccurrenceSearches);
			target.setMarkOccurrencesDelay(markOccurrencesDelay);
			target.setPaintMatchedBracketPair(paintMatchedBracketPair);
			target.setPaintMarkOccurrencesBorder(paintMarkOccurrencesBorder);
			target.setUseSelectedTextColor(useSelectedTextColor);

			target.setParserDelay(parserDelay);

			target.setHyperlinksEnabled(hyperlinksEnabled);
			target.setLinkScanningMask(linkScanningMask);
		}
	}
	
	/**
	 * Encapsulation of auto-complete settings.
	 */
	public static class EditorAutoCompleteSettings
	{
		private int choicesWindowSizeWidth;
		private int choicesWindowSizeHeight;
		private int descriptionWindowSizeWidth;
		private int descriptionWindowSizeHeight;

		private KeyStroke triggerKey;

		private boolean autoCompleteEnabled;
		private boolean autoCompleteSingleChoices;
		private boolean autoActivationEnabled;

		private boolean showDescWindow;
		private int parameterDescriptionTruncateThreshold;

		public EditorAutoCompleteSettings()
		{
			setAutoCompleteEnabled(true);
			setChoicesWindowSizeWidth(350);
			setChoicesWindowSizeHeight(200);
			setDescriptionWindowSizeWidth(450);
			setDescriptionWindowSizeHeight(300);
			setTriggerKey(AutoCompletion.getDefaultTriggerKey());
			setAutoCompleteSingleChoices(true);
			setAutoActivationEnabled(false);
			setShowDescWindow(true);
			setParameterDescriptionTruncateThreshold(300);
		}
		
		public int getChoicesWindowSizeWidth()
		{
			return choicesWindowSizeWidth;
		}

		public int getChoicesWindowSizeHeight()
		{
			return choicesWindowSizeHeight;
		}

		public int getDescriptionWindowSizeWidth()
		{
			return descriptionWindowSizeWidth;
		}

		public int getDescriptionWindowSizeHeight()
		{
			return descriptionWindowSizeHeight;
		}

		public KeyStroke getTriggerKey()
		{
			return triggerKey;
		}

		public boolean isAutoCompleteEnabled()
		{
			return autoCompleteEnabled;
		}

		public boolean isAutoCompleteSingleChoices() 
		{
			return autoCompleteSingleChoices;
		}

		public boolean isAutoActivationEnabled() 
		{
			return autoActivationEnabled;
		}

		public boolean isShowDescWindow()
		{
			return showDescWindow;
		}

		public int getParameterDescriptionTruncateThreshold()
		{
			return parameterDescriptionTruncateThreshold;
		}

		public void setChoicesWindowSizeWidth(int choicesWindowSizeWidth) 
		{
			this.choicesWindowSizeWidth = choicesWindowSizeWidth;
		}

		public void setChoicesWindowSizeHeight(int choicesWindowSizeHeight)
		{
			this.choicesWindowSizeHeight = choicesWindowSizeHeight;
		}

		public void setDescriptionWindowSizeWidth(int descriptionWindowSizeWidth) 
		{
			this.descriptionWindowSizeWidth = descriptionWindowSizeWidth;
		}

		public void setDescriptionWindowSizeHeight(int descriptionWindowSizeHeight) 
		{
			this.descriptionWindowSizeHeight = descriptionWindowSizeHeight;
		}

		public void setTriggerKey(KeyStroke triggerKey) 
		{
			this.triggerKey = triggerKey;
		}

		public void setAutoCompleteEnabled(boolean autoCompleteEnabled)
		{
			this.autoCompleteEnabled = autoCompleteEnabled;
		}

		public void setAutoCompleteSingleChoices(boolean autoCompleteSingleChoices)
		{
			this.autoCompleteSingleChoices = autoCompleteSingleChoices;
		}

		public void setAutoActivationEnabled(boolean autoActivationEnabled) 
		{
			this.autoActivationEnabled = autoActivationEnabled;
		}

		public void setShowDescWindow(boolean showDescWindow) 
		{
			this.showDescWindow = showDescWindow;
		}

		public void setParameterDescriptionTruncateThreshold(int parameterDescriptionTruncateThreshold)
		{
			this.parameterDescriptionTruncateThreshold = parameterDescriptionTruncateThreshold;
		}

		public void applyTo(AutoCompletion target)
		{
			target.setChoicesWindowSize(choicesWindowSizeWidth, choicesWindowSizeHeight);
			target.setDescriptionWindowSize(descriptionWindowSizeWidth, descriptionWindowSizeHeight);
			target.setTriggerKey(triggerKey);
			target.setAutoCompleteEnabled(autoCompleteEnabled);
			target.setAutoCompleteSingleChoices(autoCompleteSingleChoices);
			target.setAutoActivationEnabled(autoActivationEnabled);
			target.setShowDescWindow(showDescWindow);
			target.setParameterDescriptionTruncateThreshold(parameterDescriptionTruncateThreshold);
		}
	}
	
}
