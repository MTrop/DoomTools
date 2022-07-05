package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.decohack.DecoHackPatchType;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.apps.data.ExportSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.DecoHackSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DecoHackExportPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel.ActionNames;
import net.mtrop.doom.tools.gui.swing.panels.MultiFileEditorPanel.EditorHandle;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;


/**
 * The DECOHack application.
 * @author Matthew Tropiano
 */
public class DecoHackEditorApp extends DoomToolsApplicationInstance
{
	/** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DecoHackEditorApp.class); 

	private static final AtomicLong NEW_COUNTER = new AtomicLong(1L);

	private static final String EMPTY_PATCH = (new StringBuilder())
		.append("/*****************************************************************************\n")
		.append(" * DECOHack Main Patch\n")
		.append(" *****************************************************************************/\n")
		.append("\n")
		.append("#include <{{PATCH_TYPE}}>\n")
		.append("#include <friendly>\n")
		.append("\n")
	.toString();
	
    // Singletons

	private DoomToolsGUIUtils utils;
	private DoomToolsLanguageManager language;
	private DecoHackSettingsManager settings;
	private AppCommon appCommon;

	// Referenced Components
	
	private DecoHackEditorPanel editorPanel;
	private DoomToolsStatusPanel statusPanel;

	private Action exportAction;
	
	// State
	
	private File fileToOpenFirst;
	private EditorHandle currentHandle;
	private Map<EditorHandle, ExportSettings> handleToSettingsMap;

	// ...

	/**
	 * Create a new DECOHack application.
	 */
	public DecoHackEditorApp() 
	{
		this(null);
	}
	
	/**
	 * Create a new DECOHack application.
	 * @param fileToOpenFirst if not null, open this file on create.
	 */
	public DecoHackEditorApp(File fileToOpenFirst) 
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = DecoHackSettingsManager.get();
		this.appCommon = AppCommon.get();
		
		this.editorPanel = new DecoHackEditorPanel(new MultiFileEditorPanel.Options() 
		{
			@Override
			public boolean hideStyleChangePanel() 
			{
				return true;
			}
		}, 
		new DecoHackEditorPanel.Listener()
		{
			@Override
			public void onCurrentEditorChange(EditorHandle previous, EditorHandle next) 
			{
				currentHandle = next;
				onHandleChange();
			}

			@Override
			public void onSave(EditorHandle handle) 
			{
				File sourceFile = handle.getContentSourceFile();
				statusPanel.setSuccessMessage(language.getText("decohack.status.message.saved", sourceFile.getName()));
				onHandleChange();
			}

			@Override
			public void onOpen(EditorHandle handle) 
			{
				statusPanel.setSuccessMessage(language.getText("decohack.status.message.editor.open", handle.getEditorTabName()));
			}

			@Override
			public void onClose(EditorHandle handle) 
			{
				statusPanel.setSuccessMessage(language.getText("decohack.status.message.editor.close", handle.getEditorTabName()));
				handleToSettingsMap.remove(handle);
			}
		});
		this.statusPanel = new DoomToolsStatusPanel();
		
		this.exportAction = utils.createActionFromLanguageKey("decohack.menu.patch.item.export", (e) -> onExport());
		
		this.currentHandle = null;
		this.handleToSettingsMap = new HashMap<>();
		this.fileToOpenFirst = fileToOpenFirst;
	}
	
	@Override
	public String getTitle() 
	{
		return language.getText("decohack.editor.title");
	}

	@Override
	public Container createContentPane() 
	{
		return containerOf(dimension(650, 500), borderLayout(0, 8), 
			node(BorderLayout.CENTER, editorPanel),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}

	private MenuNode[] createCommonFileMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("decohack.menu.file.item.new",
				utils.createItemFromLanguageKey("decohack.menu.file.item.new.item.main", (c, e) -> onNewEditor()),
				utils.createItemFromLanguageKey("decohack.menu.file.item.new.item.blank", (c, e) -> onNewBlankEditor())
			),
			utils.createItemFromLanguageKey("decohack.menu.file.item.open", (c, e) -> onOpenEditor()),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.close", editorPanel.getActionFor(ActionNames.ACTION_CLOSE)),
			utils.createItemFromLanguageKey("texteditor.action.closeallbutcurrent", editorPanel.getActionFor(ActionNames.ACTION_CLOSE_ALL_BUT_CURRENT)),
			utils.createItemFromLanguageKey("texteditor.action.closeall", editorPanel.getActionFor(ActionNames.ACTION_CLOSE_ALL)),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.save", editorPanel.getActionFor(ActionNames.ACTION_SAVE)),
			utils.createItemFromLanguageKey("texteditor.action.saveas", editorPanel.getActionFor(ActionNames.ACTION_SAVE_AS)),
			utils.createItemFromLanguageKey("texteditor.action.saveall", editorPanel.getActionFor(ActionNames.ACTION_SAVE_ALL))
		);
	}
	
	private MenuNode[] createCommonEditMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.undo", editorPanel.getActionFor(ActionNames.ACTION_UNDO)),
			utils.createItemFromLanguageKey("texteditor.action.redo", editorPanel.getActionFor(ActionNames.ACTION_REDO)),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.cut", editorPanel.getActionFor(ActionNames.ACTION_CUT)),
			utils.createItemFromLanguageKey("texteditor.action.copy", editorPanel.getActionFor(ActionNames.ACTION_COPY)),
			utils.createItemFromLanguageKey("texteditor.action.paste", editorPanel.getActionFor(ActionNames.ACTION_PASTE)),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.delete", editorPanel.getActionFor(ActionNames.ACTION_DELETE)),
			utils.createItemFromLanguageKey("texteditor.action.selectall", editorPanel.getActionFor(ActionNames.ACTION_SELECT_ALL))
		);
	}

	private MenuNode[] createCommonPatchMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("decohack.menu.patch.item.export", exportAction)
		);
	}
	
	private MenuNode[] createCommonEditorMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.goto", editorPanel.getActionFor(ActionNames.ACTION_GOTO)),
			utils.createItemFromLanguageKey("texteditor.action.find", editorPanel.getActionFor(ActionNames.ACTION_FIND)),
			separator(),
			editorPanel.getToggleLineWrapMenuItem(),
			editorPanel.getChangeEncodingMenuItem(),
			editorPanel.getChangeSpacingMenuItem(),
			editorPanel.getChangeLineEndingMenuItem(),
			separator(),
			editorPanel.getEditorPreferencesMenuItem()
		);
	}
	
	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("decohack.menu.file", ArrayUtils.joinArrays(
				createCommonFileMenuItems(),
				ArrayUtils.arrayOf(
					separator(),
					utils.createItemFromLanguageKey("decohack.menu.file.item.exit", (c, e) -> attemptClose())
				)
			)),
			utils.createMenuFromLanguageKey("decohack.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("decohack.menu.patch", createCommonPatchMenuItems()),
			utils.createMenuFromLanguageKey("decohack.menu.editor", createCommonEditorMenuItems())
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("decohack.menu.file", createCommonFileMenuItems()),
			utils.createMenuFromLanguageKey("decohack.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("decohack.menu.patch", createCommonPatchMenuItems()),
			utils.createMenuFromLanguageKey("decohack.menu.editor", createCommonEditorMenuItems())
		);
	}

	@Override
	public void onCreate(Object frame) 
	{
		if (frame instanceof JFrame)
		{
			JFrame f = (JFrame)frame;
			Rectangle bounds = settings.getBounds();
			boolean maximized = settings.getBoundsMaximized();
			f.setBounds(bounds);
			if (maximized)
				f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
	}
	
	@Override
	public void onOpen(Object frame) 
	{
		statusPanel.setSuccessMessage(language.getText("decohack.status.message.ready"));
		if (editorPanel.getOpenEditorCount() == 0)
		{
			if (fileToOpenFirst != null && fileToOpenFirst.exists() && !fileToOpenFirst.isDirectory())
				onOpenFile(fileToOpenFirst);
			else
				onNewEditor(DecoHackPatchType.DOOM19.name().toLowerCase());
		}
	}

	@Override
	public void onClose(Object frame) 
	{
		if (frame instanceof JFrame)
		{
			JFrame f = (JFrame)frame;
			settings.setBounds(f);
		}
	}
	
	@Override
	public boolean shouldClose() 
	{
		if (editorPanel.getUnsavedEditorCount() > 0)
			return editorPanel.closeAllEditors(false);
		return true;
	}
	
	@Override
	public Map<String, String> getApplicationState() 
	{
		Map<String, String> state = super.getApplicationState();
		editorPanel.saveState("decohack", state);

		for (int i = 0; i < editorPanel.getEditorCount(); i++)
		{
			EditorHandle handle = editorPanel.getEditorByIndex(i);
			if (currentHandle == handle)
				state.put("editor.selected", String.valueOf(i));
			
			String settingPrefix = "export." + i;
			ExportSettings settings = handleToSettingsMap.get(handle);
			if (settings != null)
			{
				state.put(settingPrefix + ".enabled", String.valueOf(true));
				if (settings.getOutputFile() != null)
					state.put(settingPrefix + ".outfile", settings.getOutputFile().getAbsolutePath());
				if (settings.getSourceOutputFile() != null)
					state.put(settingPrefix + ".srcfile", settings.getSourceOutputFile().getAbsolutePath());
				state.put(settingPrefix + ".budget", String.valueOf(settings.isOutputBudget()));
			}
		}
		
		return state;
	}
	
	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		handleToSettingsMap.clear();
		editorPanel.loadState("decohack", state);
		
		int selectedIndex = ValueUtils.parseInt(state.get("editor.selected"), 0);
		editorPanel.setEditorByIndex(selectedIndex);

		for (int i = 0; i < editorPanel.getEditorCount(); i++)
		{
			EditorHandle handle = editorPanel.getEditorByIndex(i);
			String settingPrefix = "export." + i;
			
			boolean enabled = ValueUtils.parseBoolean(state.get(settingPrefix + ".enabled"), false);
			if (enabled)
			{
				ExportSettings settings = new ExportSettings();
				Function<String, File> parseFile = (input) -> ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input));
				settings.setOutputFile(ValueUtils.parse(state.get(settingPrefix + ".outfile"), parseFile));
				settings.setSourceOutputFile(ValueUtils.parse(state.get(settingPrefix + ".srcfile"), parseFile));
				settings.setOutputBudget(ValueUtils.parseBoolean(state.get(settingPrefix + ".budget"), false));
				handleToSettingsMap.put(handle, settings);
			}
		}
		
	}
	
	
	// ====================================================================

	private void onHandleChange()
	{
		if (currentHandle != null)
		{
			@SuppressWarnings("unused")
			boolean hasFile = currentHandle.getContentSourceFile() != null;
			// Do nothing?
		}
		else
		{
			// Do nothing?
		}

	}
	
	private void onNewEditor()
	{
		final JFormField<DecoHackPatchType> patchField = comboField(comboBox(Arrays.asList(DecoHackPatchType.values())));
		patchField.setValue(DecoHackPatchType.DOOM19);
		
		JPanel panel = new JPanel();
		containerOf(panel, borderLayout(0, 4),
			node(BorderLayout.NORTH, label(language.getText("decohack.new.modal.message"))),
			node(BorderLayout.SOUTH, patchField)
		);
		
		Boolean choice = utils.createModal( 
			language.getText("decohack.new.modal.title"), 
			panel,
			utils.createChoiceFromLanguageKey("doomtools.ok", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel", false)
		).openThenDispose();
		
		if (choice == null || choice == Boolean.FALSE)
			return;
		
		onNewEditor(patchField.getValue().getKeyword());
	}

	private void onNewEditor(String patchType)
	{
		String content = EMPTY_PATCH.replace("{{PATCH_TYPE}}", patchType);
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, EMPTY_PATCH.replace("{{PATCH_TYPE}}", patchType), Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_DECOHACK, content.length());
	}
	
	private void onNewBlankEditor()
	{
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, "", Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_DECOHACK, 0);
	}
	
	private void onOpenEditor()
	{
		File file = utils.chooseFile(
			getApplicationContainer(), 
			language.getText("wadscript.open.title"), 
			language.getText("wadscript.open.accept"),
			settings::getLastTouchedFile,
			settings::setLastTouchedFile,
			utils.getDecoHackFileFilter()
		);
		
		if (file != null)
			onOpenFile(file);
	}
	
	private void onOpenFile(File file)
	{
		try {
			editorPanel.openFileEditor(file, Charset.defaultCharset());
		} catch (FileNotFoundException e) {
			LOG.errorf(e, "Selected file could not be found: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wadscript.status.message.editor.error", file.getName()));
			SwingUtils.error(language.getText("decohack.open.error.notfound", file.getAbsolutePath()));
		} catch (IOException e) {
			LOG.errorf(e, "Selected file could not be read: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wadscript.status.message.editor.error", file.getName()));
			SwingUtils.error(language.getText("decohack.open.error.ioerror", file.getAbsolutePath()));
		} catch (SecurityException e) {
			LOG.errorf(e, "Selected file could not be read (access denied): %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wadscript.status.message.editor.error.security", file.getName()));
			SwingUtils.error(language.getText("decohack.open.error.security", file.getAbsolutePath()));
		}
	}
	
	private boolean saveBeforeExecute()
	{
		if (currentHandle.needsToSave() || currentHandle.getContentSourceFile() == null)
		{
			Boolean saveChoice = modal(
				getApplicationContainer(), 
				utils.getWindowIcons(), 
				language.getText("decohack.save.modal.title"),
				containerOf(label(language.getText("decohack.save.modal.message", currentHandle.getEditorTabName()))), 
				utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.save", true),
				utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.nosave", false),
				utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)null)
			).openThenDispose();
			
			if (saveChoice == null || saveChoice == false)
				return false;
			else if (saveChoice == true)
			{
				if (!editorPanel.saveCurrentEditor())
					return false;
			}
		}

		return true;
	}
	
	private void onExport()
	{
		if (!saveBeforeExecute())
		{
			SwingUtils.error(getApplicationContainer(), language.getText("decohack.error.mustsave"));
			return;
		}
		
		// Should be set if saveBeforeExecute() succeeds.
		final File scriptFile = currentHandle.getContentSourceFile();
		
		ExportSettings existingSettings = handleToSettingsMap.get(currentHandle);
		final ExportSettings processSettings = createExportSettings(scriptFile, existingSettings != null ? existingSettings : new ExportSettings(scriptFile));
		
		if (processSettings == null)
			return;
		
		handleToSettingsMap.put(currentHandle, processSettings);
		appCommon.onExecuteDecoHack(getApplicationContainer(), statusPanel, scriptFile, currentHandle.getContentCharset(), processSettings);
	}

	private ExportSettings createExportSettings(File sourceFile, final ExportSettings initSettings) 
	{
		final DecoHackExportPanel argsPanel = new DecoHackExportPanel(initSettings);
		ExportSettings settings = utils.createSettingsModal(
			language.getText("decohack.export.title"),
			argsPanel,
			(panel) -> {
				ExportSettings out = new ExportSettings(sourceFile);
				out.setOutputFile(panel.getPatchOutput());
				out.setSourceOutputFile(panel.getSourceOutput());
				out.setOutputBudget(panel.getBudget());
				return out;
			},
			utils.createChoiceFromLanguageKey("decohack.export.choice.export", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel")
		);
		
		return settings;
	}

	private class DecoHackEditorPanel extends MultiFileEditorPanel
	{
		private static final long serialVersionUID = -9024669807749249148L;
		
		private FileFilter[] TYPES = null;
		
		public DecoHackEditorPanel(Options options, Listener listener)
		{
			super(options, listener);
		}

		@Override
		protected String getDefaultStyleName() 
		{
			return DoomToolsEditorProvider.SYNTAX_STYLE_DECOHACK;
		}
		
		@Override
		protected File getLastPathTouched() 
		{
			return settings.getLastTouchedFile();
		}
		
		@Override
		protected void setLastPathTouched(File saved) 
		{
			settings.setLastTouchedFile(saved);
		}
		
		@Override
		protected FileFilter[] getSaveFileTypes() 
		{
			return TYPES == null ? TYPES = new FileFilter[]{utils.getDecoHackFileFilter()} : TYPES;
		}
	
		@Override
		protected File transformSaveFile(FileFilter selectedFilter, File selectedFile) 
		{
			return selectedFilter == getSaveFileTypes()[0] ? FileUtils.addMissingExtension(selectedFile, "dh") : selectedFile;
		}
		
	}
	
}
