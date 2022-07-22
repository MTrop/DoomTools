/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.swing.TransferHandler;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.EditorSettingsManager;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.EncodingUtils;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.FileUtils.TempFile;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static javax.swing.BorderFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;
import static net.mtrop.doom.tools.struct.swing.SwingUtils.*;
import static net.mtrop.doom.tools.struct.util.ObjectUtils.apply;


/**
 * The editor panel for editing many files at once.
 * @author Matthew Tropiano
 */
public class EditorMultiFilePanel extends JPanel
{
	private static final long serialVersionUID = -3208735521175265227L;
	
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(EditorMultiFilePanel.class); 

    private static final FileFilter[] NO_FILTERS = new FileFilter[0];

	private static final Set<WeakReference<EditorMultiFilePanel>> ACTIVE_PANELS = new HashSet<>(8);

	private static final Charset UTF8 = Charset.forName("UTF-8");

	private static final Options DEFAULT_OPTIONS = new EditorMultiFilePanel.Options()
	{
		@Override
		public boolean hideStyleChangePanel()
		{
			return false;
		}

		@Override
		public boolean hideTreeActions()
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
		String ACTION_REVEAL = "reveal";
		String ACTION_REVEAL_TREE = "revealtree";
		String ACTION_OPEN_DIRECTORY = "opendirectory";
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
	/** Line ending mode. */
	private JLabel lineEndingLabel;
	
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
	/** Reveal Action */
	private Action revealAction;
	/** Reveal in Tree Action */
	private Action revealTreeAction;
	/** Open Directory Action */
	private Action openDirectoryAction;

	/** Editor preferences item. */
	private MenuNode editorPreferencesMenuItem;
	/** Change encoding item. */
	private MenuNode changeEncodingMenuItem;
	/** Change language item. */
	private MenuNode changeLanguageMenuItem;
	/** Change spacing item. */
	private MenuNode changeSpacingMenuItem;
	/** Change line ending item. */
	private MenuNode changeLineEndingMenuItem;
	/** Toggle line wrapping item. */
	private MenuNode toggleLineWrapMenuItem;
	
	// ======================================================================
	
	/** All editors. */
	private Map<Component, EditorHandle> allEditors;
	/** All open files. */
	private Set<File> allOpenFiles;
	
	/** The currently selected editor. */
	private EditorHandle currentEditor;
	/** The panel listener. */
	private Listener listener;
	/** Find replace modal instance. */
	private volatile Modal<Void> findModal;

	/** Editor theme. */
	private Theme currentTheme;
	/** Editor font. */
	private Font currentFont;
	/** Editor coding settings. */
	private EditorViewSettings defaultViewSettings;

	/** Editor coding settings. */
	private EditorCodeSettings codeSettings;
	/** Editor auto-completion settings. */
	private EditorAutoCompleteSettings autoCompleteSettings;

	/**
	 * Creates a new multi-file editor panel with default options.
	 */
	public EditorMultiFilePanel()
	{
		this(DEFAULT_OPTIONS, null);
	}

	/**
	 * Creates a new multi-file editor panel with default options.
	 * @param listener the listener.
	 */
	public EditorMultiFilePanel(Listener listener)
	{
		this(DEFAULT_OPTIONS, listener);
	}
	
	/**
	 * Creates a new multi-file editor panel.
	 * @param options the panel options.
	 * @param listener the listener.
	 */
	public EditorMultiFilePanel(Options options, Listener listener)
	{
		this.editorProvider = DoomToolsEditorProvider.get();
		this.icons = DoomToolsIconManager.get();
		this.language = DoomToolsLanguageManager.get();
		this.utils = DoomToolsGUIUtils.get();
		this.settings = EditorSettingsManager.get();
		
		this.allEditors = new HashMap<>();
		this.allOpenFiles = new HashSet<>();
		
		this.currentEditor = null;
		this.listener = listener;
		
		reloadSettings();
		
		this.mainEditorTabs = apply(tabs(TabPlacement.TOP, TabLayoutPolicy.SCROLL), (tabs) -> {
			tabs.addChangeListener(this::onTabChange);
		});
		
		this.saveAction = utils.createActionFromLanguageKey("texteditor.action.save", (event) -> saveCurrentEditor());
		this.saveAsAction = utils.createActionFromLanguageKey("texteditor.action.saveas", (event) -> saveCurrentEditorAs());
		this.saveAllAction = utils.createActionFromLanguageKey("texteditor.action.saveall", (event) -> saveAllEditors());
		this.closeAction = utils.createActionFromLanguageKey("texteditor.action.close", (event) -> closeCurrentEditor());
		this.closeAllAction = utils.createActionFromLanguageKey("texteditor.action.closeall", (event) -> closeAllEditors(false));
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
		
		this.revealAction = utils.createActionFromLanguageKey("texteditor.action.reveal", (event) -> openCurrentEditorFileInSystem());
		this.revealTreeAction = utils.createActionFromLanguageKey("texteditor.action.reveal.tree", (event) -> openCurrentEditorInTree());
		this.openDirectoryAction = utils.createActionFromLanguageKey("texteditor.action.opendir", (event) -> openCurrentEditorDirectory());
		
		final MenuNode[] encodingNodes = createEditorEncodingMenuItems();
		final MenuNode[] languageNodes = createEditorStyleMenuItems();
		final MenuNode[] spacingNodes = createEditorSpacingMenuItems();
		final MenuNode[] lineEndingNodes = createEditorLineEndingMenuItems();
		final MenuNode[] editorFileNodes = createEditorCurrentFileMenuItems(options.hideTreeActions());
		
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
			map.put(ActionNames.ACTION_REVEAL, revealAction);
			map.put(ActionNames.ACTION_REVEAL_TREE, revealTreeAction);
			map.put(ActionNames.ACTION_OPEN_DIRECTORY, openDirectoryAction);
		});
		
		this.filePathLabel = apply(label(" "), (e) -> {
			e.setComponentPopupMenu(popupMenu(editorFileNodes));
		});
		this.syntaxStyleLabel = apply(label(" "), (e) -> {
			e.setComponentPopupMenu(popupMenu(languageNodes));
		});
		this.encodingModeLabel = apply(label(" "), (e) -> {
			e.setComponentPopupMenu(popupMenu(encodingNodes));
		});
		this.spacingModeLabel = apply(label(" "), (e) -> {
			e.setComponentPopupMenu(popupMenu(spacingNodes));
		});
		this.lineEndingLabel = apply(label(" "), (e) -> {
			e.setComponentPopupMenu(popupMenu(lineEndingNodes));
		});
		this.caretPositionLabel = label(" ");
		this.findReplacePanel = new FindReplacePanel();
		
		this.editorPreferencesMenuItem = utils.createItemFromLanguageKey("texteditor.action.prefs", (i) -> openEditorPreferences());
		this.changeEncodingMenuItem = utils.createItemFromLanguageKey("texteditor.action.encodings", encodingNodes);
		this.changeLanguageMenuItem = utils.createItemFromLanguageKey("texteditor.action.languages", languageNodes);
		this.changeSpacingMenuItem = utils.createItemFromLanguageKey("texteditor.action.spacing", spacingNodes);
		this.changeLineEndingMenuItem = utils.createItemFromLanguageKey("texteditor.action.lineending", lineEndingNodes);
		this.toggleLineWrapMenuItem = utils.createItemFromLanguageKey("texteditor.action.linewrap", (i) -> toggleCurrentEditorLineWrap());

		setTransferHandler(new FileTransferHandler());
		
		List<Node> labelNodes = new LinkedList<>();
		if (!options.hideStyleChangePanel())
			labelNodes.add(node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(syntaxStyleLabel))));
		labelNodes.add(node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(encodingModeLabel))));
		labelNodes.add(node(
			containerOf(gridLayout(1, 0), 
				node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(spacingModeLabel))),
				node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(lineEndingLabel)))
			)
		));
		labelNodes.add(node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(caretPositionLabel))));
		
		containerOf(this, borderLayout(0, 2),
			node(BorderLayout.CENTER, this.mainEditorTabs),
			node(BorderLayout.SOUTH, containerOf(gridLayout(1, 2),
				node(containerOf(createBevelBorder(BevelBorder.LOWERED), node(filePathLabel))),
				node(containerOf(gridLayout(1, 0), labelNodes.toArray(new Node[labelNodes.size()])))
			))
		);
		
		synchronized (ACTIVE_PANELS) 
		{
			ACTIVE_PANELS.add(new WeakReference<EditorMultiFilePanel>(this));
		}
	}
	
	/**
	 * Saves this editor's state to a state map.
	 * @param prefix the key prefix.
	 * @param stateMap the output state map.
	 * @return the amount of editor tabs saved.
	 */
	public int saveState(String prefix, Map<String, String> stateMap)
	{
		stateMap.put(prefix + ".editors", String.valueOf(mainEditorTabs.getTabCount()));
		for (int i = 0; i < mainEditorTabs.getTabCount(); i++)
		{
			String keyPrefix = prefix + ".editor." + String.valueOf(i);
			EditorHandle handle = allEditors.get(mainEditorTabs.getTabComponentAt(i));
			
			stateMap.put(keyPrefix + ".tabTitle", handle.getEditorTabName());
			if (handle.contentSourceFile != null)
				stateMap.put(keyPrefix + ".contentSourceFile", handle.contentSourceFile.getAbsolutePath());
			stateMap.put(keyPrefix + ".contentCharset", handle.contentCharset.displayName());
			stateMap.put(keyPrefix + ".currentStyle", handle.currentStyle);
			try {
				stateMap.put(keyPrefix + ".content", EncodingUtils.asBase64(EncodingUtils.gzipBytes(handle.getContent().getBytes(UTF8))));
			} catch (IOException e) {
				stateMap.put(keyPrefix + ".content", "");
			}
			stateMap.put(keyPrefix + ".caretPosition", String.valueOf(handle.editorPanel.textArea.getCaretPosition()));
			
			stateMap.put(keyPrefix + ".currentLineEnding", handle.currentLineEnding.name());
			stateMap.put(keyPrefix + ".contentLastModified", String.valueOf(handle.contentLastModified));
			stateMap.put(keyPrefix + ".contentSourceFileLastModified", String.valueOf(handle.contentSourceFileLastModified));
		}
		
		return mainEditorTabs.getTabCount();
	}

	/**
	 * Loads this editor's state from a state map and sets its state.
	 * @param prefix the key prefix.
	 * @param stateMap the output state map.
	 * @return the amount of editor tabs loaded.
	 */
	public int loadState(String prefix, Map<String, String> stateMap)
	{
		closeAllEditors(true);
		
		int editorCount = ValueUtils.parseInt(stateMap.get(prefix + ".editors"), 0);
		for (int i = 0; i < editorCount; i++)
		{
			String keyPrefix = prefix + ".editor." + String.valueOf(i);
			
			String title = ValueUtils.parse(stateMap.get(keyPrefix + ".tabTitle"), (input) ->
				ObjectUtils.isEmpty(input) ? "UNNAMED" : input
			);
			
			File attachedFile = ValueUtils.parse(stateMap.get(keyPrefix + ".contentSourceFile"), (input) -> 
				ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input))
			);
			
			Charset fileCharset = ValueUtils.parse(stateMap.get(keyPrefix + ".contentCharset"), (input) -> 
				Charset.forName(input)
			);
			
			String styleName = ValueUtils.parse(stateMap.get(keyPrefix + ".currentStyle"), (input) ->
				ObjectUtils.isEmpty(input) ? "text/plain" : input
			);
			
			String originalContent = ValueUtils.parse(stateMap.get(keyPrefix + ".content"), (input) -> {
				if (!ObjectUtils.isEmpty(input))
				{
					try {
						return new String(EncodingUtils.gunzipBytes(EncodingUtils.fromBase64(input)), UTF8);
					} catch (IOException e) {
						LOG.error(e, "Could not decode content.");
						return "";
					}
				}
				else
				{
					return "";
				}
			});
			
			
			int caretPosition = ValueUtils.parseInt(stateMap.get(keyPrefix + ".caretPosition"), 0);
			
			LineEnding ending = ValueUtils.parse(stateMap.get(keyPrefix + ".currentLineEnding"), (input) ->
				ObjectUtils.isEmpty(input) 
					? (OSUtils.isWindows() ? LineEnding.CRLF : LineEnding.LF) 
					: LineEnding.VALUE_MAP.get(input)
			);
			
			long contentLastModified = ValueUtils.parseLong(stateMap.get(keyPrefix + ".contentLastModified"), -1L);

			long contentSourceFileLastModified = attachedFile != null 
				? attachedFile.lastModified() 
				: ValueUtils.parseLong(stateMap.get(keyPrefix + ".contentSourceFileLastModified"), -1L);
			
			createNewTab(title, attachedFile, fileCharset, styleName, originalContent, caretPosition, ending, contentLastModified, contentSourceFileLastModified);
		}
		
		return editorCount;
	}
	
	/**
	 * Creates a new editor tab with a name.
	 * @param tabName the name of the tab.
	 * @param content the initial content of the new editor.
	 */
	public void newEditor(String tabName, String content)
	{
		newEditor(tabName, content, 0);
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
		newEditor(tabName, content, encoding, styleName, 0);
	}
	
	/**
	 * Creates a new editor tab with a name.
	 * @param tabName the name of the tab.
	 * @param content the initial content of the new editor.
	 * @param caretPosition the starting caret position.
	 */
	public void newEditor(String tabName, String content, int caretPosition)
	{
		createNewTab(tabName, null, defaultViewSettings.getDefaultEncoding(), null, content, caretPosition, null, null, null);
	}
	
	/**
	 * Creates a new editor tab with a name.
	 * @param tabName the name of the tab.
	 * @param content the initial content of the new editor.
	 * @param encoding the default encoding.
	 * @param styleName the default style. Can be null to not force a style. 
	 * @param caretPosition the starting caret position.
	 */
	public void newEditor(String tabName, String content, Charset encoding, String styleName, int caretPosition)
	{
		createNewTab(tabName, null, encoding, styleName, content, caretPosition, null, null, null);
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
		
		createNewTab(file.getName(), file, encoding, getDefaultStyleName(), writer.toString(), 0, null, null, null);
	}
	
	/**
	 * Opens an editor into a new tab.
	 * @param editorName the buffer name.
	 * @param file the associated file.
	 * @param encoding the file encoding.
	 * @param styleName the syntax style name.
	 * @param content the editor content.
	 */
	public void openEditor(String editorName, File file, Charset encoding, String styleName, String content)
	{
		createNewTab(editorName, file, encoding, styleName, content, 0, null, null, null);
	}
	
	/**
	 * Opens a file relative to the parent of the current editor's file.
	 * @param filePath the path to open.
	 */
	public void openFilePathRelativeToCurrentEditor(String filePath) 
	{
		if (currentEditor == null)
			return;
		
		if (currentEditor.contentSourceFile == null)
			return;

		File editorParentFile = currentEditor.contentSourceFile.getParentFile();
		File lookupFile = new File(editorParentFile + File.separator + filePath);
		if (lookupFile.exists())
		{
			try {
				openFileEditor(lookupFile, currentEditor.contentCharset);
				LOG.infof("Opened \"include\" path: %s", lookupFile.getAbsolutePath());
			} catch (FileNotFoundException e) {
				SwingUtils.error(language.getText("texteditor.action.include.error.notfound", lookupFile.getAbsolutePath()));
			} catch (IOException e) {
				SwingUtils.error(language.getText("texteditor.action.include.error.ioerror", lookupFile.getAbsolutePath()));
			} catch (SecurityException e) {
				SwingUtils.error(language.getText("texteditor.action.include.error.security", lookupFile.getAbsolutePath()));
			}
		}
		else
		{
			SwingUtils.error(language.getText("texteditor.action.include.error.notfound", lookupFile.getAbsolutePath()));
		}
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
				saveEditorToFile(handle, editorFile, false);
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
	 * @param force if true, force all closed without prompting.
	 * @return true if it is safe to close all editors, and all editors were closed, false if one editor closing was cancelled.
	 */
	public boolean closeAllEditors(boolean force) 
	{
		// Find all editors to close.
		Set<Component> tabsToClose = new HashSet<>();
		
		for (Entry<Component, EditorHandle> editor : allEditors.entrySet())
		{
			EditorHandle handle = editor.getValue();
			
			if (!force && !editorCanClose(handle))
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
	 * Checks if this panel has any unsaved editors.
	 * @return true if this editor has unsaved data, false if not.
	 */
	public boolean hasUnsavedData()
	{
		for (Map.Entry<Component, EditorHandle> entry : allEditors.entrySet())
			if (entry.getValue().needsToSave())
				return true;
		
		return false;
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
	 * @return the editor preferences menu item.
	 */
	public MenuNode getEditorPreferencesMenuItem() 
	{
		return editorPreferencesMenuItem;
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
	 * @return the change line ending menu item.
	 */
	public MenuNode getChangeLineEndingMenuItem() 
	{
		return changeLineEndingMenuItem;
	}
	
	/**
	 * @return the toggle line wrap meni item.
	 */
	public MenuNode getToggleLineWrapMenuItem() 
	{
		return toggleLineWrapMenuItem;
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
	 * @return the number of open editors.
	 */
	public int getEditorCount()
	{
		return mainEditorTabs.getTabCount();
	}

	/**
	 * Gets an editor by its tab index.
	 * @param index the index to fetch.
	 * @return the corresponding editor, or null if index is out of range.
	 */
	public EditorHandle getEditorByIndex(int index)
	{
		if (index < 0 || index >= mainEditorTabs.getTabCount())
			return null;
		return allEditors.get(mainEditorTabs.getTabComponentAt(index));
	}
	
	/**
	 * Gets an editor by an open file.
	 * @param file the file to search for.
	 * @return the corresponding editor, or null if it doesn't exist.
	 */
	public EditorHandle getEditorByFile(File file)
	{
		file = FileUtils.canonizeFile(file);
		
		if (!allOpenFiles.contains(file))
			return null;

		// Search sequentially because this seems to be the only reliable way to do this.
		for (int i = 0; i < mainEditorTabs.getTabCount(); i++)
		{
			EditorHandle handle = getEditorByIndex(i);
			if (handle.contentSourceFile != null && file.getAbsolutePath().equals(handle.contentSourceFile.getAbsolutePath()))
				return handle;
		}

		return null;
	}
	
	/**
	 * Switches to an editor by an editor index.
	 * @param index the index to set.
	 */
	public void setEditorByIndex(int index)
	{
		mainEditorTabs.setSelectedIndex(index);
	}
	
	/**
	 * Reloads the theme from settings and applies it to all open editors.
	 */
	public void reloadSettings()
	{
		defaultViewSettings = settings.getDefaultEditorViewSettings();
		codeSettings = settings.getDefaultEditorCodeSettings();
		autoCompleteSettings = settings.getDefaultEditorAutoCompleteSettings();
		setTheme(EditorThemeType.THEME_MAP.getOrDefault(settings.getEditorThemeName(), EditorThemeType.DEFAULT));
		setEditorFont(settings.getEditorFont());
		forEachOpenEditor((handle) -> codeSettings.apply(handle.editorPanel.textArea));
		forEachOpenEditor((handle) -> autoCompleteSettings.apply(handle.currentAutoCompletion));
		forEachOpenEditor((handle) -> handle.editorPanel.textArea.setWrapStyleWord(defaultViewSettings.isWrapStyleWord()));
	}

	/**
	 * Calls {@link #reloadSettings()} across all un-disposed editors.
	 */
	public static void reloadAllSettings()
	{
		synchronized (ACTIVE_PANELS) 
		{
			Iterator<WeakReference<EditorMultiFilePanel>> it = ACTIVE_PANELS.iterator();
			while (it.hasNext())
			{
				WeakReference<EditorMultiFilePanel> panelRef = it.next();
				EditorMultiFilePanel panel;
				if ((panel = panelRef.get()) != null)
					panel.reloadSettings();
				else
					it.remove();
			}
		}
	}

	/**
	 * Sets a theme across all editors (and future ones).
	 * @param themeType the theme type.
	 */
	public void setTheme(EditorThemeType themeType)
	{
		final Theme theme = loadTheme(themeType);
		currentTheme = theme;
		forEachOpenEditor((handle) -> theme.apply(handle.editorPanel.textArea));
	}
	
	/**
	 * Sets a font across all editors (and future ones).
	 * @param fontType the font type.
	 */
	public void setEditorFont(Font fontType)
	{
		currentFont = fontType;
		forEachOpenEditor((handle) -> handle.editorPanel.textArea.setFont(fontType));
	}
	
	/**
	 * Creates a new editor, returning the editor tab.
	 * @param title the tab title.
	 * @param attachedFile the content source file (if any, can be null).
	 * @param fileCharset the file's source charset.
	 * @param styleName the default style. Can be null to not force a style. 
	 * @param originalContent the incoming content for the editor.
	 * @param caretPosition the starting caret position.
	 * @param ending the line ending. Can be null.
	 * @param contentLastModified the last modified timestamp. Can be null.
	 * @param contentSourceFileLastModified the file last modified timestamp. Can be null.
	 */
	protected final synchronized void createNewTab(
		String title, 
		File attachedFile, 
		Charset fileCharset, 
		String styleName, 
		final String originalContent, 
		int caretPosition, 
		LineEnding ending,
		Long contentLastModified,
		Long contentSourceFileLastModified
	){
		if (attachedFile != null)
			attachedFile = FileUtils.canonizeFile(attachedFile);
		
		if (focusOnFile(attachedFile))
			return;
		
		RSyntaxTextArea textArea = new RSyntaxTextArea();
		
		if (styleName == null)
		{
			if (attachedFile != null)
			{
				styleName = editorProvider.getStyleByFile(attachedFile);
				if (styleName == null)
					styleName = SyntaxConstants.SYNTAX_STYLE_NONE;
			}
			else
			{
				styleName = SyntaxConstants.SYNTAX_STYLE_NONE;
			}
		}
		
		// Remove all CRs for editor, keep LFs.
		final String textAreaContent = originalContent.replace("\r", "");
		textArea.setText(textAreaContent);
		textArea.setCaretPosition(caretPosition);

		EditorHandle handle = attachedFile != null 
			? new EditorHandle(attachedFile, fileCharset, styleName, textArea) 
			: new EditorHandle(title, fileCharset, styleName, textArea)
		;

		if (contentLastModified != null)
			handle.contentLastModified = contentLastModified;
		if (contentSourceFileLastModified != null)
			handle.contentSourceFileLastModified = contentSourceFileLastModified;

		handle.updateIcon();
		
		// ==================================================================
		
		defaultViewSettings.apply(textArea);
		currentTheme.apply(textArea);
		textArea.setFont(currentFont);
		
		setEditorViewSettingsByContent(textArea, originalContent);
		if (attachedFile != null) // only scan for ending if existing file
			setEditorHandleSettingsByContent(handle, originalContent);
		
		if (ending != null)
			handle.currentLineEnding = ending;
		
		// ==================================================================
		
		allEditors.put(handle.editorTab, handle);
		if (attachedFile != null)
			allOpenFiles.add(attachedFile);

		mainEditorTabs.addTab(null, handle.editorPanel);

		// some settings do not apply properly until the text area is added to the layout
		codeSettings.apply(textArea);

		// The tab just added will be at the end.
		int tabIndex = mainEditorTabs.getTabCount() - 1;
		mainEditorTabs.setTabComponentAt(tabIndex, handle.editorTab);
		
		mainEditorTabs.setSelectedIndex(tabIndex);
		if (mainEditorTabs.getTabCount() == 1) // workaround for weird implementation.
			setCurrentEditor(handle);
		SwingUtils.invoke(() -> textArea.requestFocus());
		
		if (listener != null)
			listener.onOpen(handle);
	}

	/**
	 * Called to get the default parser style name opening a file.
	 * @return the default style, or null for auto-detect on open.
	 */
	protected String getDefaultStyleName()
	{
		return null;
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
	
	// Handles a tab change.
	private void onTabChange(ChangeEvent event)
	{
		int index = mainEditorTabs.getSelectedIndex();
		if (index >= 0)
		{
			setCurrentEditor(allEditors.get(mainEditorTabs.getTabComponentAt(mainEditorTabs.getSelectedIndex())));
			mainEditorTabs.getComponentAt(index).requestFocus();
		}
		else
		{
			setCurrentEditor(null);
		}
	}
	
	private boolean focusOnFile(File file)
	{
		if (!allOpenFiles.contains(file))
			return false;

		// Search sequentially because this seems to be the only reliable way to do this.
		for (int i = 0; i < mainEditorTabs.getTabCount(); i++)
		{
			Component component = mainEditorTabs.getTabComponentAt(i);
			final EditorHandle handle = allEditors.get(component);
			if (handle.contentSourceFile != null && file.getAbsolutePath().equals(handle.contentSourceFile.getAbsolutePath()))
			{
				mainEditorTabs.setSelectedIndex(i);
				SwingUtils.invoke(() -> handle.editorPanel.textArea.requestFocus());
				return true;
			}
		}
		
		return false;
	}
	
	private void remapFileTabs(File oldFile, File newFile)
	{
		if (oldFile != null)
			allOpenFiles.remove(oldFile);
		if (allOpenFiles.contains(newFile))
			removeEditorByFile(newFile);
		allOpenFiles.add(newFile);
	}
	
	private void setCurrentEditor(EditorHandle handle)
	{
		EditorHandle previous = currentEditor;
		currentEditor = handle;
		updateActionStates();
		updateLabels();
		updateEditorHooks();
		if (listener != null)
			listener.onCurrentEditorChange(previous, handle);
		if (currentEditor != null)
			currentEditor.editorPanel.textArea.requestFocus();
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
		
		return saveEditorToFile(handle, editorFile, false);
	}

	private void removeEditorByFile(File file)
	{
		for (EditorHandle handle : allEditors.values())
		{
			if (handle.contentSourceFile != null && file.getAbsolutePath().equals(handle.contentSourceFile.getAbsolutePath()))
			{
				removeEditorByTab(handle.editorTab);
				break;
			}
		}
	}

	private void removeEditorByTab(Component tabComponent)
	{
		int index;
		if ((index = mainEditorTabs.indexOfTabComponent(tabComponent)) >= 0)
		{
			mainEditorTabs.remove(index);
			EditorHandle handle = allEditors.remove(tabComponent);
			if (handle != null && listener != null)
			{
				if (handle.contentSourceFile != null)
					allOpenFiles.remove(handle.contentSourceFile);
				listener.onClose(handle);
			}
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
		revealAction.setEnabled(editorPresent && currentEditor.contentSourceFile != null);
		revealTreeAction.setEnabled(editorPresent && currentEditor.contentSourceFile != null);
		openDirectoryAction.setEnabled(editorPresent && currentEditor.contentSourceFile != null);
	}
	
	private void updateEncodingLabel()
	{
		if (currentEditor != null)
			encodingModeLabel.setText(currentEditor.contentCharset.displayName());
		else
			encodingModeLabel.setText(" ");
	}
	
	private void updateStyleLabel() 
	{
		if (currentEditor != null)
			syntaxStyleLabel.setText(currentEditor.currentStyle);
		else
			syntaxStyleLabel.setText(" ");
	}
	
	private void updateSpacingLabel() 
	{
		if (currentEditor != null)
		{
			RSyntaxTextArea textArea = currentEditor.editorPanel.textArea;
			int tabSize = textArea.getTabSize();
			boolean usesSpaces = textArea.getTabsEmulated();
			boolean lineWrap = textArea.getLineWrap();
			spacingModeLabel.setText(
				(usesSpaces ? "SPC " : "TAB ") + tabSize +
				(lineWrap ? ", WRP" : "")
			);
		}
		else
		{
			spacingModeLabel.setText(" ");
		}
	}

	private void updateLineEndingLabel() 
	{
		if (currentEditor != null)
			lineEndingLabel.setText(currentEditor.currentLineEnding.name());
		else
			lineEndingLabel.setText(" ");
	}

	private void updateFilePathLabel()
	{
		if (currentEditor != null)
		{
			File file = currentEditor.getContentSourceFile();
			filePathLabel.setText(file != null ? file.getAbsolutePath() : "");
		}
		else
		{
			filePathLabel.setText(" ");
		}
	}

	private void updateCaratPositionLabel()
	{
		if (currentEditor != null)
		{
			RSyntaxTextArea textArea = currentEditor.editorPanel.textArea;
			boolean twoCharEnding = currentEditor.currentLineEnding.isTwoCharEnding();
			int line = textArea.getCaretLineNumber() + 1;
			int offset = textArea.getCaretOffsetFromLineStart();
			int characterOffset = textArea.getCaretPosition() + (twoCharEnding ? textArea.getCaretLineNumber() : 0);
			
			int selection = textArea.getSelectionEnd() - textArea.getSelectionStart(); 
			String selected = textArea.getSelectedText();
			if (selected != null)
			{
				int selectedLineSpan = selection - textArea.getSelectedText().replace("\n",	"").length();
				selection = selection + (twoCharEnding ? selectedLineSpan : 0);
			}
			
			caretPositionLabel.setText("L " + line + ", C " + offset + ", P " + characterOffset + (selection > 0 ? ", [" + selection + "]" : ""));
		}
		else
		{
			caretPositionLabel.setText(" ");
		}
	}
	
	private void updateLabels()
	{
		updateFilePathLabel();
		updateStyleLabel();
		updateEncodingLabel();
		updateSpacingLabel();
		updateLineEndingLabel();
		updateCaratPositionLabel();
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
			return saveEditorToFile(handle, editorFile, false);
		}
		else
		{
			return true;
		}
	}

	private boolean saveEditorToFile(EditorHandle handle, File targetFile, boolean skipEvent)
	{
		targetFile = FileUtils.canonizeFile(targetFile);
		
		Charset targetCharset = handle.contentCharset;
		String content = handle.editorPanel.textArea.getText();
		content = handle.currentLineEnding.convertContent(content);
	
		if (!checkEncode(targetFile, content, targetCharset))
			return false;
		
		StringReader reader = new StringReader(content);
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), targetCharset)) {
			IOUtils.relay(reader, writer, 8192);
		} catch (IOException e) {
			error(this, language.getText("texteditor.action.save.error", targetFile.getAbsolutePath()));
			return false;
		}
		
		if (!skipEvent)
		{
			handle.onSaveChange(targetFile);
			updateActionStates();
			
			if (listener != null)
				listener.onSave(handle);
		}
		
		return true;
	}

	/**
	 * Check if a file and content can be encoded to the provided charset.
	 * @param targetFile the file about to be saved.
	 * @param content the content to encode.
	 * @param targetCharset the charset to encode to.
	 * @return true if so, false if not.
	 */
	private boolean checkEncode(File targetFile, String content, Charset targetCharset) 
	{
		if (!EncodingUtils.canEncodeAs(content, targetCharset))
		{
			error(this, language.getText("texteditor.action.save.encoding.error", targetFile.getAbsolutePath(), targetCharset.displayName()));
			return false;
		}
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
	 * Opens the current editor's file in system explorer. 
	 */
	private boolean openCurrentEditorFileInSystem()
	{
		if (currentEditor == null)
			return false;
		return openEditorFileInSystem(currentEditor);
	}
	
	/**
	 * Opens the current editor's file in the tree. 
	 */
	private boolean openCurrentEditorInTree()
	{
		if (currentEditor == null)
			return false;
		if (listener != null)
			listener.onTreeRevealRequest(currentEditor);
		return true;
	}
	
	/**
	 * Opens the current editor's directory in the tree. 
	 */
	private boolean openCurrentEditorDirectory()
	{
		if (currentEditor == null)
			return false;
		if (listener != null)
			listener.onTreeDirectoryRequest(currentEditor);
		return true;
	}
	
	/**
	 * Clears all highlights/markers on every editor.
	 */
	private void clearAllHighlights()
	{
		forEachOpenEditor((editor) -> editor.editorPanel.textArea.getHighlighter().removeAllHighlights());
	}
	
	private MenuNode[] createEditorEncodingMenuItems()
	{
		Set<Charset> charsets = editorProvider.getAvailableCommonCharsets();
		
		List<MenuNode> out = new ArrayList<>();
		for (Charset charset : charsets)
			out.add(menuItem(charset.displayName(), (i) -> changeCurrentEditorEncoding(charset)));
		return out.toArray(new MenuNode[out.size()]);
	}
	
	private MenuNode[] createEditorStyleMenuItems()
	{
		Map<String, String> languages = editorProvider.getAvailableLanguageMap();
		Map<String, String> otherLanguages = editorProvider.getOtherAvailableLanguageMap();
		
		List<MenuNode> out = new ArrayList<>();
		for (Map.Entry<String, String> entry : languages.entrySet())
			out.add(menuItem(entry.getKey(), (i) -> changeCurrentEditorStyle(entry.getValue())));
		out.add(separator());
		
		List<MenuNode> others = new ArrayList<>();
		for (Map.Entry<String, String> entry : otherLanguages.entrySet())
			others.add(menuItem(entry.getKey(), (i) -> changeCurrentEditorStyle(entry.getValue())));
		out.add(utils.createItemFromLanguageKey("texteditor.action.languages.other", others.toArray(new MenuNode[others.size()])));
		return out.toArray(new MenuNode[out.size()]);
	}
	
	private MenuNode[] createEditorSpacingMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.spacing.spaces",
				menuItem("2", KeyEvent.VK_2, (i) -> changeCurrentEditorSpacing(true, 2)),
				menuItem("3", KeyEvent.VK_3, (i) -> changeCurrentEditorSpacing(true, 3)),
				menuItem("4", KeyEvent.VK_4, (i) -> changeCurrentEditorSpacing(true, 4)),
				menuItem("5", KeyEvent.VK_5, (i) -> changeCurrentEditorSpacing(true, 5)),
				menuItem("6", KeyEvent.VK_6, (i) -> changeCurrentEditorSpacing(true, 6)),
				menuItem("7", KeyEvent.VK_7, (i) -> changeCurrentEditorSpacing(true, 7)),
				menuItem("8", KeyEvent.VK_8, (i) -> changeCurrentEditorSpacing(true, 8))
			), utils.createItemFromLanguageKey("texteditor.action.spacing.tabs",
				menuItem("2", KeyEvent.VK_2, (i) -> changeCurrentEditorSpacing(false, 2)),
				menuItem("3", KeyEvent.VK_3, (i) -> changeCurrentEditorSpacing(false, 3)),
				menuItem("4", KeyEvent.VK_4, (i) -> changeCurrentEditorSpacing(false, 4)),
				menuItem("5", KeyEvent.VK_5, (i) -> changeCurrentEditorSpacing(false, 5)),
				menuItem("6", KeyEvent.VK_6, (i) -> changeCurrentEditorSpacing(false, 6)),
				menuItem("7", KeyEvent.VK_7, (i) -> changeCurrentEditorSpacing(false, 7)),
				menuItem("8", KeyEvent.VK_8, (i) -> changeCurrentEditorSpacing(false, 8))
			)
		); 
	}

	private MenuNode[] createEditorLineEndingMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.lineending.crlf", (i) -> changeCurrentEditorLineEnding(LineEnding.CRLF)),
			utils.createItemFromLanguageKey("texteditor.action.lineending.lf", (i) -> changeCurrentEditorLineEnding(LineEnding.LF)),
			utils.createItemFromLanguageKey("texteditor.action.lineending.cr", (i) -> changeCurrentEditorLineEnding(LineEnding.CR))
		);
	}
	
	private MenuNode[] createEditorCurrentFileMenuItems(boolean hideTreeActions)
	{
		return hideTreeActions ? ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.reveal", revealAction)
		) : ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.reveal", revealAction),
			utils.createItemFromLanguageKey("texteditor.action.reveal.tree", revealTreeAction),
			utils.createItemFromLanguageKey("texteditor.action.reveal.opendir", openDirectoryAction)
		);
	}
	
	/**
	 * Changes the encoding of the text in the current editor.
	 * @param charset the new charset for the editor.
	 */
	private void changeCurrentEditorEncoding(final Charset charset)
	{
		forCurrentEditor((editor) -> editor.changeEncoding(charset));
	}

	/**
	 * Changes the language style in the current editor.
	 * @param styleName the style name.
	 */
	private void changeCurrentEditorStyle(final String styleName)
	{
		forCurrentEditor((editor) -> editor.changeStyleName(styleName));
	}

	/**
	 * Changes the spacing style and input in the current editor.
	 */
	private void changeCurrentEditorSpacing(final boolean spaces, final int amount)
	{
		forCurrentEditor((editor) -> editor.changeSpacing(spaces, amount));
	}

	/**
	 * Changes the line-ending output style in the current editor.
	 * @param lineEnding the new line ending for the editor.
	 */
	private void changeCurrentEditorLineEnding(final LineEnding lineEnding)
	{
		forCurrentEditor((editor) -> editor.changeLineEnding(lineEnding));
	}

	/**
	 * Changes line wrapping state in the current editor.
	 * @param lineWrap the new line wrap state for the editor.
	 */
	private void toggleCurrentEditorLineWrap()
	{
		forCurrentEditor((editor) -> editor.toggleLineWrap());
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
			findReplacePanel.focusFindField();
			return;
		}
		
		findModal = utils.createModal(
			language.getText("texteditor.modal.find.title"),
			ModalityType.MODELESS,
			containerOf(node(findReplacePanel))
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
		findReplacePanel.focusFindField();
	}

	/**
	 * Opens the editor preferences, then reloads the settings on close.
	 */
	private void openEditorPreferences() 
	{
		EditorSettingsPanel settingsPanel = new EditorSettingsPanel();
		utils.createModal(
			language.getText("texteditor.settings"),
			containerOf(node(dimension(500, 600), settingsPanel))
		).openThenDispose();
		settingsPanel.commitSettings();
		reloadAllSettings();
	}

	/**
	 * Opens the an editor's file in system explorer. 
	 */
	private static boolean openEditorFileInSystem(EditorHandle handle)
	{
		if (handle.contentSourceFile == null)
			return false;
		return Common.openInSystemBrowser(handle.contentSourceFile);
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
					while (line.startsWith(" ", i))
						i++;
					if (i > 1)
					{
						textArea.setTabsEmulated(true);
						textArea.setTabSize(Math.min(i, 8));
					}
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
	
	// Sets editor handle settings by scanning content.
	private static void setEditorHandleSettingsByContent(EditorHandle handle, String content)
	{
		boolean sawCR = false;
		boolean sawLF = false;
		try (Reader reader = new StringReader(content))
		{
			int code;
			while ((code = reader.read()) >= 0)
			{
				char c = (char)code;
				if (c == '\r') // CR
				{
					if (sawCR)
					{
						handle.currentLineEnding = LineEnding.CR;
						return;
					}
					else if (sawLF)
					{
						handle.currentLineEnding = LineEnding.LFCR;
						return;
					}
					
					sawCR = true;
				}
				else if (c == '\n') // LF
				{
					if (sawCR)
					{
						handle.currentLineEnding = LineEnding.CRLF;
						return;
					}
					else if (sawLF)
					{
						handle.currentLineEnding = LineEnding.LF;
						return;
					}
					
					sawLF = true;
				}
				else // not CR nor LF
				{
					if (sawCR)
					{
						handle.currentLineEnding = LineEnding.CR;
						return;
					}
					else if (sawLF)
					{
						handle.currentLineEnding = LineEnding.LF;
						return;
					}
				}
			}
		}
		catch (IOException e) 
		{
			// Do nothing.
		}
		
		// Nothing happened. Keep current setting.
	}
	
	private Theme loadTheme(EditorThemeType themeType) 
	{
		try (InputStream in = IOUtils.openResource(themeType.resourceName))
		{
			return Theme.load(in);
		} 
		catch (IOException e) 
		{
			LOG.errorf(e, "Could not load theme: %s", themeType.resourceName);
			return null;
		}
	}

	/**
	 * The listener.
	 */
	public interface Listener
	{
		/**
		 * Called on a current editor changing focus.
		 * @param previous the previous editor handle selected. Can be null.
		 * @param next the next (and new current) editor handle. Can be null.
		 */
		void onCurrentEditorChange(EditorHandle previous, EditorHandle next);
		
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

		/**
		 * Called when an editor wants its parent directory shown in the directory tree.
		 * @param handle the handle.
		 */
		void onTreeDirectoryRequest(EditorHandle handle);

		/**
		 * Called when an editor wants its file shown in the directory tree.
		 * @param handle the handle.
		 */
		void onTreeRevealRequest(EditorHandle handle);
	}

	/**
	 * Panel options.
	 */
	public interface Options
	{
		/**
		 * @return false to allow tree actions, true to forbid it.
		 */
		boolean hideTreeActions();

		/**
		 * @return false to allow style changing, true to forbid it.
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
		/** Current line ending. */
		private LineEnding currentLineEnding;
		
		/** Current reveal location action. */
		private Action fileRevealAction;
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
			this.currentLineEnding = OSUtils.isWindows() ? LineEnding.CRLF : LineEnding.LF;
	
			this.fileRevealAction = actionItem(language.getText("texteditor.action.reveal"), (e) -> openEditorFileInSystem(this));
			
			this.editorTab = new EditorTab(savedIcon, title, (b) -> attemptToCloseEditor(this));
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
			updateActions();
		}
		
		private EditorHandle(File contentSourceFile, Charset sourceCharset, String styleName, RSyntaxTextArea textArea)
		{
			this(contentSourceFile.getName(), sourceCharset, styleName, textArea);
			
			this.contentSourceFile = contentSourceFile;
			this.contentCharset = sourceCharset;
			this.contentLastModified = contentSourceFile.lastModified();
			this.contentSourceFileLastModified = contentSourceFile.lastModified();
			this.editorTab.setTabIcon(savedIcon);
			this.currentLineEnding = OSUtils.isWindows() ? LineEnding.CRLF : LineEnding.LF;
			updateActions();
		}
	
		/**
		 * Changes the encoding of the editor.
		 * @param newCharset the new charset.
		 */
		public void changeEncoding(Charset newCharset)
		{
			contentCharset = newCharset;
			updateEncodingLabel();
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
			updateStyleLabel();
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
			updateSpacingLabel();
		}
		
		/**
		 * Changes the line ending style.
		 * @param lineEnding if true, spaces. false is tabs.
		 */
		public void changeLineEnding(LineEnding lineEnding)
		{
			currentLineEnding = lineEnding; 
			updateCaratPositionLabel();
			updateLineEndingLabel();
		}
		
		/**
		 * Toggles the line wrapping style.
		 */
		public void toggleLineWrap()
		{
			editorPanel.textArea.setLineWrap(!editorPanel.textArea.getLineWrap());
			updateSpacingLabel();
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
		 * @return the editor charset encoding.
		 */
		public Charset getContentCharset() 
		{
			return contentCharset;
		}
		
		/**
		 * @return the editor style type.
		 */
		public String getCurrentStyleType() 
		{
			return currentStyle;
		}
		
		/**
		 * @return true if this editor has unsaved data.
		 */
		public boolean needsToSave()
		{
			return contentLastModified > contentSourceFileLastModified;
		}
		
		/**
		 * Creates a temp file with the editor contents and returns it.
		 * @return the TempFile with the editor data.
		 */
		public TempFile createTempCopy()
		{
			TempFile out = FileUtils.createTempFile();
			return saveEditorToFile(this, out, true) ? out : null;
		}
		
		private void updateActions()
		{
			fileRevealAction.setEnabled(contentSourceFile != null);
		}
		
		private void updateIcon()
		{
			if (needsToSave())
				editorTab.setTabIcon(unsavedIcon);
			else
				editorTab.setTabIcon(savedIcon);
		}
		
		// Applies the auto-complete.
		private void applyAutoComplete(String styleName)
		{
			AutoCompletion autoCompletion = editorProvider.createAutoCompletionByStyle(styleName);
			if (autoCompletion != null)
			{
				autoCompleteSettings.apply(autoCompletion);
				autoCompletion.setParameterAssistanceEnabled(true);
			}
			
			if (currentAutoCompletion != null)
				currentAutoCompletion.uninstall();

			if (autoCompletion != null)
			{
				autoCompletion.install(editorPanel.textArea);
			}
			currentAutoCompletion = autoCompletion;
		}
		
		// Should call to update title on tab and timestamps.
		private void onSaveChange(File path)
		{
			editorTab.setTabTitle(path.getName());
			remapFileTabs(contentSourceFile, path);
			contentSourceFile = path;
			contentSourceFileLastModified = path.lastModified();
			updateFilePathLabel();
			updateIcon();
			updateActions();
		}
		
		private void onChange()
		{
			contentLastModified = System.currentTimeMillis();
			updateIcon();
			updateActionsIfCurrent(this);
		}

	}

	/**
	 * Handle only drag-and-dropped files.
	 */
	private class FileTransferHandler extends TransferHandler
	{
		private static final long serialVersionUID = 1667964095084519427L;

		@Override
		public boolean canImport(TransferSupport support) 
		{
			return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean importData(TransferSupport support) 
		{
			if (!support.isDrop())
				return false;
			
			Transferable transferable = support.getTransferable();
			List<File> files;
			try {
				files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
			} catch (UnsupportedFlavorException | IOException e) {
				LOG.warn("Could not handle DnD import for file drop.");
				return false;
			} 
			
			for (File f : files)
			{
				try {
					openFileEditor(f, defaultViewSettings.getDefaultEncoding());
				} catch (IOException e) {
					error(language.getText("texteditor.dnd.droperror", f.getAbsolutePath()));
				}
			}
			
			return true;
		}
		
	}

	/**
	 * A single editor tab.
	 */
	private class EditorTab extends JPanel
	{
		private static final long serialVersionUID = 6056215456163910928L;
		
		private JLabel titleLabel;
		private JButton closeButton;
		
		private EditorTab(Icon icon, String title, ButtonClickHandler closeHandler)
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
	private class EditorPanel extends JPanel
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
				updateCaratPositionLabel();
			});
			
			textArea.addHyperlinkListener((hevent) -> {
				if (hevent.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
					return;

				if (hevent.getURL() != null)
				{
					try {
						SwingUtils.browse(hevent.getURL().toURI());
					} catch (IOException | URISyntaxException e1) {
						// Do nothing.
					}
				}
				else
				{
					String path = hevent.getDescription();
					// description is something like "no protocol: whatever".
					int pathIdx = path.indexOf(": ");
					int strIdx = pathIdx >= 0 ? pathIdx + 2 : 0;
					openFilePathRelativeToCurrentEditor(path.substring(strIdx));
				}
				
			});
			
			containerOf(this, node(BorderLayout.CENTER, scrollPane));
		}

	}
	
	/**
	 * An encapsulation of a series of editor view settings.  
	 */
	public static class EditorViewSettings
	{
		private Charset defaultEncoding;
		private int tabSize;
		private boolean tabsEmulated;
		private boolean lineWrap;
		private boolean wrapStyleWord;
		
		public EditorViewSettings()
		{
			setDefaultEncoding(Charset.defaultCharset());
			setTabSize(4);
			setTabsEmulated(false);
			setLineWrap(false);
			setWrapStyleWord(true);
		}
		
		public Charset getDefaultEncoding() 
		{
			return defaultEncoding;
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

		public void setDefaultEncoding(Charset defaultEncoding) 
		{
			this.defaultEncoding = defaultEncoding;
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

		public void apply(RSyntaxTextArea target)
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

		public void apply(RSyntaxTextArea target)
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
	 * Line ending mode.
	 */
	public enum LineEnding
	{
		/** Windows */
		CRLF 
		{
			@Override
			public String convertContent(String content) 
			{
				return content.replace("\n", "\r\n");
			}

			@Override
			public boolean isTwoCharEnding() 
			{
				return true;
			}
		},
		
		/** Unknown! */
		LFCR
		{
			@Override
			public String convertContent(String content) 
			{
				return content.replace("\n", "\n\r");
			}

			@Override
			public boolean isTwoCharEnding() 
			{
				return true;
			}
		},

		/** Unix */
		LF 
		{
			@Override
			public String convertContent(String content) 
			{
				// Do nothing.
				return content;
			}

			@Override
			public boolean isTwoCharEnding()
			{
				return false;
			}
		},

		/** Macintosh */
		CR 
		{
			@Override
			public String convertContent(String content) 
			{
				return content.replace("\n", "\r");
			}

			@Override
			public boolean isTwoCharEnding() 
			{
				return false;
			}
		},

		;
		
		public abstract String convertContent(String content);
		public abstract boolean isTwoCharEnding();
		
		public static final Map<String, LineEnding> VALUE_MAP = EnumUtils.createCaseInsensitiveNameMap(LineEnding.class); 
		
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
		private int autoActivationDelay;

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
			setAutoCompleteSingleChoices(false);
			setAutoActivationEnabled(false);
			setAutoActivationDelay(400);
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
		
		public int getAutoActivationDelay() 
		{
			return autoActivationDelay;
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

		public void setAutoActivationDelay(int autoActivationDelay) 
		{
			this.autoActivationDelay = autoActivationDelay;
		}
		
		public void setShowDescWindow(boolean showDescWindow) 
		{
			this.showDescWindow = showDescWindow;
		}

		public void setParameterDescriptionTruncateThreshold(int parameterDescriptionTruncateThreshold)
		{
			this.parameterDescriptionTruncateThreshold = parameterDescriptionTruncateThreshold;
		}

		public void apply(AutoCompletion target)
		{
			target.setChoicesWindowSize(choicesWindowSizeWidth, choicesWindowSizeHeight);
			target.setDescriptionWindowSize(descriptionWindowSizeWidth, descriptionWindowSizeHeight);
			target.setTriggerKey(triggerKey);
			target.setAutoCompleteEnabled(autoCompleteEnabled);
			target.setAutoCompleteSingleChoices(autoCompleteSingleChoices);
			target.setAutoActivationEnabled(autoActivationEnabled);
			target.setAutoActivationDelay(autoActivationDelay);
			target.setShowDescWindow(showDescWindow);
			target.setParameterDescriptionTruncateThreshold(parameterDescriptionTruncateThreshold);
		}
	}
	
	/**
	 * Theme types.
	 */
	public enum EditorThemeType
	{
		DEFAULT("Default", "org/fife/ui/rsyntaxtextarea/themes/default.xml"),
		DEFAULT_ALT("Default (Alternate)", "org/fife/ui/rsyntaxtextarea/themes/default-alt.xml"),
		DARK("Dark", "org/fife/ui/rsyntaxtextarea/themes/dark.xml"),
		DRUID("Druid", "org/fife/ui/rsyntaxtextarea/themes/druid.xml"),
		ECLIPSE("Eclipse", "org/fife/ui/rsyntaxtextarea/themes/eclipse.xml"),
		IDEA("IntelliJ IDEA", "org/fife/ui/rsyntaxtextarea/themes/idea.xml"),
		MONOKAI("Monokai", "org/fife/ui/rsyntaxtextarea/themes/monokai.xml"),
		VS("Visual Studio", "org/fife/ui/rsyntaxtextarea/themes/vs.xml");
		
		private final String friendlyName;
		private final String resourceName;
		
		public static final Map<String, EditorThemeType> THEME_MAP = EnumUtils.createCaseInsensitiveNameMap(EditorThemeType.class);
		
		private EditorThemeType(String friendlyName, String resourceName)
		{
			this.friendlyName = friendlyName;
			this.resourceName = resourceName;
		}
		
		public String getFriendlyName() 
		{
			return friendlyName;
		}
		
	}

}
