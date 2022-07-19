package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.texture.CommonTextureList;
import net.mtrop.doom.texture.DoomTextureList;
import net.mtrop.doom.texture.PatchNames;
import net.mtrop.doom.texture.StrifeTextureList;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.tools.WADTexMain;
import net.mtrop.doom.tools.common.Utility;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.apps.data.WadTexExportSettings;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.WadTexSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.ActionNames;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorHandle;
import net.mtrop.doom.tools.gui.swing.panels.WadTexExportPanel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.ValueUtils;
import net.mtrop.doom.tools.struct.util.FileUtils.TempFile;
import net.mtrop.doom.util.NameUtils;
import net.mtrop.doom.util.TextureUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;


/**
 * The WadTex application.
 * @author Matthew Tropiano
 */
public class WadTexEditorApp extends DoomToolsApplicationInstance
{
	/** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(WadTexEditorApp.class); 

	private static final AtomicLong NEW_COUNTER = new AtomicLong(1L);

    // Singletons

	private WadTexSettingsManager settings;

	// Referenced Components
	
	private WadTexEditorPanel editorPanel;
	private DoomToolsStatusPanel statusPanel;

	private Action exportAction;
	
	// State
	
	private File fileToOpenFirst;
	private EditorHandle currentHandle;
	private Map<EditorHandle, WadTexExportSettings> handleToSettingsMap;

	// ...

	/**
	 * Create a new WadTex application.
	 */
	public WadTexEditorApp() 
	{
		this(null);
	}
	
	/**
	 * Create a new WadTex application.
	 * @param fileToOpenFirst if not null, open this file on create.
	 */
	public WadTexEditorApp(File fileToOpenFirst) 
	{
		this.settings = WadTexSettingsManager.get();
		
		this.editorPanel = new WadTexEditorPanel(new EditorMultiFilePanel.Options() 
		{
			@Override
			public boolean hideStyleChangePanel() 
			{
				return true;
			}

			@Override
			public boolean hideTreeActions()
			{
				return true;
			}
		}, 
		new WadTexEditorPanel.Listener()
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
				statusPanel.setSuccessMessage(language.getText("wadtex.status.message.saved", sourceFile.getName()));
				onHandleChange();
			}

			@Override
			public void onOpen(EditorHandle handle) 
			{
				statusPanel.setSuccessMessage(language.getText("wadtex.status.message.editor.open", handle.getEditorTabName()));
			}

			@Override
			public void onClose(EditorHandle handle) 
			{
				statusPanel.setSuccessMessage(language.getText("wadtex.status.message.editor.close", handle.getEditorTabName()));
				handleToSettingsMap.remove(handle);
			}

			@Override
			public void onTreeDirectoryRequest(EditorHandle handle)
			{
				// Do nothing.
			}

			@Override
			public void onTreeRevealRequest(EditorHandle handle)
			{
				// Do nothing.
			}
		});
		this.statusPanel = new DoomToolsStatusPanel();
		
		this.exportAction = utils.createActionFromLanguageKey("wadtex.menu.patch.item.export", (e) -> onExport());
		
		this.currentHandle = null;
		this.handleToSettingsMap = new HashMap<>();
		this.fileToOpenFirst = fileToOpenFirst;
	}
	
	@Override
	public String getTitle() 
	{
		return language.getText("wadtex.editor.title");
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
			utils.createItemFromLanguageKey("wadtex.menu.file.item.new",
				utils.createItemFromLanguageKey("wadtex.menu.file.item.new.item.main", (i) -> onNewEditor()),
				utils.createItemFromLanguageKey("wadtex.menu.file.item.new.item.blank", (i) -> onNewBlankEditor())
			),
			utils.createItemFromLanguageKey("wadtex.menu.file.item.open", (i) -> onOpenEditor()),
			utils.createItemFromLanguageKey("wadtex.menu.file.item.open.wad", (i) -> onOpenEditorFromWAD()),
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
			utils.createItemFromLanguageKey("wadtex.menu.patch.item.export", exportAction)
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
			utils.createMenuFromLanguageKey("wadtex.menu.file", ArrayUtils.joinArrays(
				createCommonFileMenuItems(),
				ArrayUtils.arrayOf(
					separator(),
					utils.createItemFromLanguageKey("wadtex.menu.file.item.exit", (i) -> attemptClose())
				)
			)),
			utils.createMenuFromLanguageKey("wadtex.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("wadtex.menu.patch", createCommonPatchMenuItems()),
			utils.createMenuFromLanguageKey("wadtex.menu.editor", createCommonEditorMenuItems()),
			createHelpMenu()
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("wadtex.menu.file", createCommonFileMenuItems()),
			utils.createMenuFromLanguageKey("wadtex.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("wadtex.menu.patch", createCommonPatchMenuItems()),
			utils.createMenuFromLanguageKey("wadtex.menu.editor", createCommonEditorMenuItems()),
			createHelpMenu()
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
		statusPanel.setSuccessMessage(language.getText("wadtex.status.message.ready"));
		if (editorPanel.getOpenEditorCount() == 0)
		{
			if (fileToOpenFirst != null && fileToOpenFirst.exists() && !fileToOpenFirst.isDirectory())
				onOpenFile(fileToOpenFirst);
			else
				onNewEditor();
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
		editorPanel.saveState("wadtex", state);

		for (int i = 0; i < editorPanel.getEditorCount(); i++)
		{
			EditorHandle handle = editorPanel.getEditorByIndex(i);
			if (currentHandle == handle)
				state.put("editor.selected", String.valueOf(i));
			
			String settingPrefix = "export." + i;
			WadTexExportSettings settings = handleToSettingsMap.get(handle);
			if (settings != null)
			{
				state.put(settingPrefix + ".enabled", String.valueOf(true));
				if (settings.getOutputWAD() != null)
					state.put(settingPrefix + ".outwad", settings.getOutputWAD().getAbsolutePath());
				if (settings.getNameOverride() != null)
					state.put(settingPrefix + ".name", settings.getNameOverride());
				state.put(settingPrefix + ".strife", String.valueOf(settings.getForceStrife()));
				state.put(settingPrefix + ".append", String.valueOf(settings.getAppendMode()));
			}
		}
		
		return state;
	}
	
	@Override
	public void setApplicationState(Map<String, String> state) 
	{
		handleToSettingsMap.clear();
		editorPanel.loadState("wadtex", state);
		
		int selectedIndex = ValueUtils.parseInt(state.get("editor.selected"), 0);
		editorPanel.setEditorByIndex(selectedIndex);

		for (int i = 0; i < editorPanel.getEditorCount(); i++)
		{
			EditorHandle handle = editorPanel.getEditorByIndex(i);
			String settingPrefix = "export." + i;
			
			boolean enabled = ValueUtils.parseBoolean(state.get(settingPrefix + ".enabled"), false);
			if (enabled)
			{
				WadTexExportSettings settings = new WadTexExportSettings();
				Function<String, File> parseFile = (input) -> ObjectUtils.isEmpty(input) ? null : FileUtils.canonizeFile(new File(input));
				settings.setOutputWAD(ValueUtils.parse(state.get(settingPrefix + ".outwad"), parseFile));
				settings.setNameOverride(state.get(settingPrefix + ".name"));
				settings.setForceStrife(ValueUtils.parseBoolean(state.get(settingPrefix + ".strife"), false));
				settings.setAppendMode(ValueUtils.parseBoolean(state.get(settingPrefix + ".append"), false));
				handleToSettingsMap.put(handle, settings);
			}
		}
	}
	
	
	// ====================================================================

	private void onHandleChange()
	{
		exportAction.setEnabled(currentHandle != null);
	}
	
	private void onNewEditor()
	{
		StringWriter writer = new StringWriter();
		try (Reader reader = new InputStreamReader(IOUtils.openResource("gui/apps/wadtex.txt"), StandardCharsets.UTF_8))
		{
			IOUtils.relay(reader, writer);
		} 
		catch (IOException e) 
		{
			LOG.error(e, "Could not get WADTEX data from resources.");
		}
		
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, writer.toString(), Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_DEUTEX, 0);
	}

	private void onNewBlankEditor()
	{
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, "", Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_DEUTEX, 0);
	}

	private void onOpenEditor()
	{
		File file = utils.chooseFile(
			getApplicationContainer(), 
			language.getText("wadtex.open.title"), 
			language.getText("wadtex.open.accept"),
			settings::getLastTouchedFile,
			settings::setLastTouchedFile,
			utils.createTextFileFilter()
		);
		
		if (file != null)
			onOpenFile(file);
	}
	
	private void onOpenEditorFromWAD()
	{
		File file = utils.chooseFile(
			getApplicationContainer(), 
			language.getText("wadtex.open.wad.title"), 
			language.getText("wadtex.open.wad.accept"),
			settings::getLastOpenedWAD,
			settings::setLastOpenedWAD,
			utils.createWADFileFilter()
		);
		
		if (file == null)
			return;
		
		boolean isWad;
		try {
			isWad = Wad.isWAD(file);
		} catch (FileNotFoundException e) {
			SwingUtils.error(language.getText("wadtex.open.wad.error.notfound", file.getAbsolutePath()));
			return;
		} catch (IOException e) {
			SwingUtils.error(language.getText("wadtex.open.wad.error.ioerror", file.getAbsolutePath()));
			return;
		} catch (SecurityException e) {
			SwingUtils.error(language.getText("wadtex.open.wad.error.security", file.getAbsolutePath()));
			return;
		}
	
		if (!isWad)
		{
			SwingUtils.error(language.getText("wadtex.open.wad.error.badwad", file.getAbsolutePath()));
			return;
		}
		
		try (WadFile wad = new WadFile(file))
		{
			int texture1Index = wad.indexOf("TEXTURE1");
			int texture2Index = wad.indexOf("TEXTURE2");
			int pnamesIndex =   wad.indexOf("PNAMES");
			
			if (pnamesIndex < 0 || (pnamesIndex >= 0 && texture1Index < 0 && texture2Index < 0))
			{
				SwingUtils.error(language.getText("wadtex.open.wad.error.nodata", file.getAbsolutePath()));
				return;
			}
			
			PatchNames pnames = wad.getDataAs(pnamesIndex, PatchNames.class);
			byte[] texture1data = texture1Index >= 0 ? wad.getData(texture1Index) : null;
			byte[] texture2data = texture2Index >= 0 ? wad.getData(texture2Index) : null;
			
			CommonTextureList<?> texture1List = null;
			if (texture1data != null)
			{
				if (TextureUtils.isStrifeTextureData(texture1data))
					texture1List = BinaryObject.create(StrifeTextureList.class, texture1data);
				else
					texture1List = BinaryObject.create(DoomTextureList.class, texture1data);
			}

			CommonTextureList<?> texture2List = null;
			if (texture2data != null)
			{
				if (TextureUtils.isStrifeTextureData(texture2data))
					texture2List = BinaryObject.create(StrifeTextureList.class, texture2data);
				else
					texture2List = BinaryObject.create(DoomTextureList.class, texture2data);
			}

			TextureSet textureSet1 = texture1List != null ? new TextureSet(pnames, texture1List) : null;
			TextureSet textureSet2 = texture2List != null ? new TextureSet(pnames, texture2List) : null;
			
			if (textureSet1 != null)
			{
				StringWriter sw = new StringWriter();
				PrintWriter writer = new PrintWriter(sw);
				Utility.writeDEUTEXFile(textureSet1, WADTexMain.WADTEX_OUTPUT_HEADER, writer);
				String content = sw.toString();
				
				String editorName = "Texture1 " + NEW_COUNTER.getAndIncrement();
				editorPanel.newEditor(editorName, content, Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_DEUTEX, 0);
			}

			if (textureSet2 != null)
			{
				StringWriter sw = new StringWriter();
				PrintWriter writer = new PrintWriter(sw);
				Utility.writeDEUTEXFile(textureSet2, WADTexMain.WADTEX_OUTPUT_HEADER, writer);
				String content = sw.toString();
				
				String editorName = "Texture2 " + NEW_COUNTER.getAndIncrement();
				editorPanel.newEditor(editorName, content, Charset.defaultCharset(), DoomToolsEditorProvider.SYNTAX_STYLE_DEUTEX, 0);
			}
		} 
		catch (WadException e) 
		{
			SwingUtils.error(language.getText("wadtex.open.wad.error.badwad", file.getAbsolutePath()));
			return;
		}
		catch (IOException e) 
		{
			SwingUtils.error(language.getText("wadtex.open.wad.error.ioerror", file.getAbsolutePath()));
			return;
		}
		catch (SecurityException e) 
		{
			SwingUtils.error(language.getText("wadtex.open.wad.error.security", file.getAbsolutePath()));
			return;
		}
	}

	private void onOpenFile(File file)
	{
		try {
			editorPanel.openFileEditor(file, Charset.defaultCharset());
		} catch (FileNotFoundException e) {
			LOG.errorf(e, "Selected file could not be found: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wadtex.status.message.editor.error", file.getName()));
			SwingUtils.error(language.getText("wadtex.open.error.notfound", file.getAbsolutePath()));
		} catch (IOException e) {
			LOG.errorf(e, "Selected file could not be read: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wadtex.status.message.editor.error", file.getName()));
			SwingUtils.error(language.getText("wadtex.open.error.ioerror", file.getAbsolutePath()));
		} catch (SecurityException e) {
			LOG.errorf(e, "Selected file could not be read (access denied): %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("wadtex.status.message.editor.error.security", file.getName()));
		}
	}
	
	private boolean saveBeforeExecute()
	{
		if (currentHandle.getContentSourceFile() != null && currentHandle.needsToSave())
		{
			Boolean saveChoice = modal(
				getApplicationContainer(), 
				utils.getWindowIcons(), 
				language.getText("wadtex.save.modal.title"),
				containerOf(label(language.getText("wadtex.save.modal.message", currentHandle.getEditorTabName()))), 
				utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.save", true),
				utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.nosave", false),
				utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)null)
			).openThenDispose();
			
			if (saveChoice == null)
				return false;
			else if (saveChoice == true)
			{
				if (!editorPanel.saveCurrentEditor())
					return false;
			}
			// else, continue on.
		}

		return true;
	}
	
	private void onExport()
	{
		if (!saveBeforeExecute())
		{
			SwingUtils.error(getApplicationContainer(), language.getText("wadtex.error.mustsave"));
			return;
		}
		
		// Should be set if saveBeforeExecute() succeeds.
		// If no file, then make a temporary one for export.

		if (currentHandle.getContentSourceFile() != null)
		{
			File scriptFile = currentHandle.getContentSourceFile();
			exportWithSettings(scriptFile);
		}
		else
		{
			try (TempFile scriptFile = currentHandle.createTempCopy())
			{
				exportWithSettings(scriptFile);
			}
		}
	}

	private void exportWithSettings(final File scriptFile) 
	{
		WadTexExportSettings existingSettings = handleToSettingsMap.get(currentHandle);
		
		final WadTexExportSettings processSettings;
		if (existingSettings != null)
		{
			processSettings = createExportSettings(scriptFile, existingSettings);
		}
		else
		{
			WadTexExportSettings settings = new WadTexExportSettings();
			settings.setNameOverride(NameUtils.toValidEntryName(currentHandle.getEditorTabName()));
			processSettings = createExportSettings(scriptFile, settings);
		}
		
		if (processSettings == null)
			return;
		
		handleToSettingsMap.put(currentHandle, processSettings);
		appCommon.onExecuteWadTex(getApplicationContainer(), statusPanel, scriptFile, processSettings);
	}

	private WadTexExportSettings createExportSettings(File sourceFile, final WadTexExportSettings initSettings) 
	{
		final WadTexExportPanel argsPanel = new WadTexExportPanel(initSettings);
		argsPanel.setPreferredSize(dimension(350, 120));
		WadTexExportSettings settings = utils.createSettingsModal(
			language.getText("wadtex.export.title"),
			argsPanel,
			(panel) -> {
				WadTexExportSettings out = new WadTexExportSettings();
				out.setOutputWAD(panel.getOutputWAD());
				out.setNameOverride(panel.getNameOverride());
				out.setAppendMode(panel.getAppendMode());
				out.setForceStrife(panel.getForceStrife());
				return out;
			},
			utils.createChoiceFromLanguageKey("wadtex.export.choice.export", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel")
		);
		
		return settings;
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog())
		); 
	}

	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-wadtex.md")).open();
	}

	private class WadTexEditorPanel extends EditorMultiFilePanel
	{
		private static final long serialVersionUID = -9024669807749249148L;
		
		private FileFilter[] TYPES = null;
		
		public WadTexEditorPanel(Options options, Listener listener)
		{
			super(options, listener);
		}

		@Override
		protected String getDefaultStyleName() 
		{
			return DoomToolsEditorProvider.SYNTAX_STYLE_DEUTEX;
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
			return TYPES == null ? TYPES = new FileFilter[]{utils.createTextFileFilter()} : TYPES;
		}
	
		@Override
		protected File transformSaveFile(FileFilter selectedFilter, File selectedFile) 
		{
			return selectedFilter == getSaveFileTypes()[0] ? FileUtils.addMissingExtension(selectedFile, "txt") : selectedFile;
		}
		
	}
	
}
