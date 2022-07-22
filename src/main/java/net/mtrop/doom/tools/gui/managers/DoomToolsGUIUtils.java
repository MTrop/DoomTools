package net.mtrop.doom.tools.gui.managers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Dialog.ModalityType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.Environment;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.gui.managers.settings.DoomToolsSettingsManager;
import net.mtrop.doom.tools.gui.managers.settings.EditorSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsTextOutputPanel;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.swing.ClipboardUtils;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.swing.ContainerFactory.ScrollPolicy;
import net.mtrop.doom.tools.struct.swing.FileChooserFactory;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormField;
import net.mtrop.doom.tools.struct.swing.FormFactory.JFormPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.fileExtensionFilter;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;


/**
 * DoomTools GUI Utilities and component building.
 * This relies on singletons for info.
 * @author Matthew Tropiano
 */
public final class DoomToolsGUIUtils 
{
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomToolsGUIUtils> INSTANCE = new SingletonProvider<>(() -> new DoomToolsGUIUtils());
	
	/**
	 * @return the singleton instance of this object.
	 */
	public static DoomToolsGUIUtils get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	private DoomToolsLanguageManager language;
	private DoomToolsImageManager images;
	private DoomToolsSettingsManager settings;
	
	private List<Image> windowIcons;
	private Icon windowIcon;
	
	private DoomToolsGUIUtils()
	{
		this.images = DoomToolsImageManager.get();
		this.language = DoomToolsLanguageManager.get();
		this.settings = DoomToolsSettingsManager.get();
		
		final Image icon16  = images.getImage("doomtools-logo-16.png"); 
		final Image icon32  = images.getImage("doomtools-logo-32.png"); 
		final Image icon48  = images.getImage("doomtools-logo-48.png"); 
		final Image icon64  = images.getImage("doomtools-logo-64.png"); 
		final Image icon96  = images.getImage("doomtools-logo-96.png"); 
		final Image icon128 = images.getImage("doomtools-logo-128.png"); 

		this.windowIcons = Arrays.asList(icon128, icon96, icon64, icon48, icon32, icon16);
		this.windowIcon = new ImageIcon(icon16);
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public final <C extends Container, T> T createSettingsModal(String title, C contentPane, Function<Boolean, Boolean> validator, Function<C, T> settingExtractor, final ModalChoice<Boolean> ... choices)
	{
		return settingsModal(getWindowIcons(), title, contentPane, validator, settingExtractor, choices);
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public final <C extends Container, T> T createSettingsModal(String title, C contentPane, Function<Boolean, Boolean> validator, Function<C, T> settingExtractor)
	{
		return settingsModal(getWindowIcons(), title, contentPane, validator, settingExtractor);
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public final <C extends Container, T> T createSettingsModal(String title, C contentPane, Function<C, T> settingExtractor, final ModalChoice<Boolean> ... choices)
	{
		return settingsModal(getWindowIcons(), title, contentPane, settingExtractor, choices);
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public final <C extends Container, T> T createSettingsModal(String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(getWindowIcons(), title, contentPane, settingExtractor);
	}

	/**
	 * Creates a new modal window with the proper icons set.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public final <T> Modal<T> createModal(String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(getWindowIcons(), title, modality, contentPane, choices);
	}
	
	/**
	 * Creates a new modal window with the proper icons set.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @return a new modal dialog.
	 */
	public final Modal<Void> createModal(String title, ModalityType modality, Container contentPane)
	{
		return modal(getWindowIcons(), title, modality, contentPane);
	}
	
	/**
	 * Creates a new modal window with the proper icons set.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public final <T> Modal<T> createModal(String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		return createModal(title, ModalityType.APPLICATION_MODAL, contentPane, choices);
	}

	/**
	 * Creates a new modal window with the proper icons set.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @return a new modal dialog.
	 */
	public Modal<Void> createModal(String title, Container contentPane)
	{
		return createModal(title, ModalityType.APPLICATION_MODAL, contentPane);
	}

	/**
	 * Creates a menu from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param nodes the additional component nodes.
	 * @return the new menu.
	 */
	public JMenu createMenuFromLanguageKey(String keyPrefix, MenuNode... nodes)
	{
		return menu(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			nodes
		);
	}

	/**
	 * Creates a menu item from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param nodes the additional component nodes.
	 * @return the new menu item node.
	 */
	public MenuNode createItemFromLanguageKey(String keyPrefix, MenuNode... nodes)
	{
		return menuItem(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			nodes
		);
	}
	
	/**
	 * Creates a menu item from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param handler the action to take on selection.
	 * @return the new menu item node.
	 */
	public MenuNode createItemFromLanguageKey(String keyPrefix, MenuItemClickHandler handler)
	{
		return menuItem(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			language.getKeyStroke(keyPrefix + ".keystroke"),
			handler
		);
	}
	
	/**
	 * Creates a menu item from a language key, getting the necessary pieces to assemble it.
	 * Name is taken form the action.
	 * @param keyPrefix the key prefix.
	 * @param action the attached action.
	 * @return the new menu item node.
	 */
	public MenuNode createItemFromLanguageKey(String keyPrefix, Action action)
	{
		return menuItem(
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			language.getKeyStroke(keyPrefix + ".keystroke"),
			action
		);
	}

	/**
	 * Creates a menu item from a language key, getting the necessary pieces to assemble it.
	 * Name is taken form the action.
	 * @param keyPrefix the key prefix.
	 * @param handler the attached action handler. 
	 * @return the new menu item node.
	 */
	public Action createActionFromLanguageKey(String keyPrefix, ActionEventHandler handler)
	{
		return actionItem(language.getText(keyPrefix), handler);
	}

	/**
	 * Creates a menu checkbox item from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param selected the initial selected state.
	 * @param handler the action to take on selection.
	 * @return the new menu item node.
	 */
	public MenuNode createCheckItemFromLanguageKey(String keyPrefix, boolean selected, CheckBoxMenuItemClickHandler handler)
	{
		return checkBoxItem(
			language.getText(keyPrefix),
			selected,
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			language.getKeyStroke(keyPrefix + ".keystroke"),
			handler
		);
	}

	/**
	 * Creates a button from a language key, getting the necessary pieces to assemble it.
	 * @param icon the button icon.
	 * @param keyPrefix the key prefix.
	 * @param handler the action to take on selection.
	 * @return the new menu item node.
	 */
	public JButton createButtonFromLanguageKey(Icon icon, String keyPrefix, ButtonClickHandler handler)
	{
		String tipKey = keyPrefix + ".tip";
		final String tip =  language.hasKey(tipKey) ? language.getText(tipKey) : null;
		return ObjectUtils.apply(button(
			icon,
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			handler
		), (button) -> button.setToolTipText(tip));
	}

	/**
	 * Creates a button from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param handler the action to take on selection.
	 * @return the new menu item node.
	 */
	public JButton createButtonFromLanguageKey(String keyPrefix, ButtonClickHandler handler)
	{
		return createButtonFromLanguageKey(null, keyPrefix, handler);
	}

	/**
	 * Creates a button from a language key, getting the necessary pieces to assemble it.
	 * @param icon the button icon.
	 * @param keyPrefix the key prefix.
	 * @param checked initial checked value.
	 * @param handler the action to take on selection.
	 * @return the new menu item node.
	 */
	public JRadioButton createRadioButtonFromLanguageKey(Icon icon, String keyPrefix, boolean checked, ToggleHandler handler)
	{
		String tipKey = keyPrefix + ".tip";
		final String tip =  language.hasKey(tipKey) ? language.getText(tipKey) : null;
		return ObjectUtils.apply(radio(
			icon,
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			checked,
			handler
		), (button) -> button.setToolTipText(tip));
	}

	/**
	 * Creates a button from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param checked initial checked value.
	 * @param handler the action to take on selection.
	 * @return the new menu item node.
	 */
	public JRadioButton createRadioButtonFromLanguageKey(String keyPrefix, boolean checked, ToggleHandler handler)
	{
		return createRadioButtonFromLanguageKey(null, keyPrefix, checked, handler);
	}

	/**
	 * Creates a button from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param checked initial checked value.
	 * @return the new menu item node.
	 */
	public JRadioButton createRadioButtonFromLanguageKey(String keyPrefix, boolean checked)
	{
		return createRadioButtonFromLanguageKey(null, keyPrefix, checked, null);
	}

	/**
	 * Creates a modal choice from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param result the choice result supplier.
	 * @return the new menu item node.
	 */
	public <T> ModalChoice<T> createChoiceFromLanguageKey(String keyPrefix, Supplier<T> result)
	{
		return choice(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			result
		);
	}

	/**
	 * Creates a modal choice from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param result the choice result.
	 * @return the new menu item node.
	 */
	public <T> ModalChoice<T> createChoiceFromLanguageKey(String keyPrefix, T result)
	{
		return choice(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			result
		);
	}

	/**
	 * Creates a modal choice from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @return the new menu item node.
	 */
	public <T> ModalChoice<T> createChoiceFromLanguageKey(String keyPrefix)
	{
		return createChoiceFromLanguageKey(keyPrefix, (T)null);
	}

	/**
	 * Creates a consistent title panel.
	 * @param title the title.
	 * @param container the enclosed container.
	 * @return a new container wrapped in a titled border.
	 */
	public Container createTitlePanel(String title, Container container)
	{
		Border border = createTitledBorder(
			createLineBorder(Color.GRAY, 1), title, TitledBorder.LEADING, TitledBorder.TOP
		);
		return containerOf(border, 
			node(containerOf(createEmptyBorder(4, 4, 4, 4),
				node(BorderLayout.CENTER, container)
			))
		);
	}

	/**
	 * Adds a form field to a form, attaching a tool tip, if any.
	 * @param formPanel the form panel.
	 * @param formFields the list of fields to add.
	 * @return the form panel passed in.
	 */
	public JFormPanel createForm(JFormPanel formPanel, FormFieldInfo ... formFields)
	{
		for (int i = 0; i < formFields.length; i++) 
		{
			FormFieldInfo formFieldInfo = formFields[i];
			String label = formFieldInfo.languageKey != null ? language.getText(formFieldInfo.languageKey) : "";
			String tipKey = formFieldInfo.languageKey + ".tip";
			String tip =  language.hasKey(tipKey) ? language.getText(tipKey) : null;
			formPanel.addTipField(label, tip, formFieldInfo.field);
		}
		return formPanel;
	}
	
	/**
	 * Brings up the file chooser to select a file, but on successful selection, returns the file
	 * and sets the last file path used in settings. Initial file is also the last file used.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param lastPathSupplier the supplier to call for the last path used.
	 * @param lastPathSaver the consumer to call for saving the chosen path, if valid.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseFile(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		File selected;
		if ((selected = FileChooserFactory.chooseFile(parent, title, lastPathSupplier.get(), approveText, transformFileFunction, choosableFilters)) != null)
			lastPathSaver.accept(selected);
		return selected;
	}

	/**
	 * Brings up the file chooser to select a file, but on successful selection, returns the file
	 * and sets the last file path used in settings. Initial file is also the last file used.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param lastPathSupplier the supplier to call for the last path used.
	 * @param lastPathSaver the consumer to call for saving the chosen path, if valid.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseFile(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver, FileFilter ... choosableFilters)
	{
		return chooseFile(parent, title, approveText, lastPathSupplier, lastPathSaver, (x, file) -> file, choosableFilters);
	}

	/**
	 * Brings up the file chooser to select a directory, but on successful selection, returns the directory
	 * and sets the last project path used in settings. Initial file is also the last project directory used.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param lastPathSupplier the supplier to call for the last path used.
	 * @param lastPathSaver the consumer to call for saving the chosen path, if valid.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseDirectory(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver)
	{
		File selected;
		if ((selected = FileChooserFactory.chooseDirectory(parent, title, lastPathSupplier.get(), approveText, FileFilters.DIRECTORIES)) != null)
			lastPathSaver.accept(selected);
		return selected;
	}

	/**
	 * Brings up the file chooser to select a file, but on successful selection, returns the file
	 * and sets the last file path used in settings. Initial file is also the last file used.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param lastPathSupplier the supplier to call for the last path used.
	 * @param lastPathSaver the consumer to call for saving the chosen path, if valid.
	 * @param transformFileFunction if a file is selected, use this function to set the final file name.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseFileOrDirectory(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver, BiFunction<FileFilter, File, File> transformFileFunction, FileFilter ... choosableFilters)
	{
		File selected;
		if ((selected = FileChooserFactory.chooseFileOrDirectory(parent, title, lastPathSupplier.get(), approveText, transformFileFunction, choosableFilters)) != null)
			lastPathSaver.accept(selected);
		return selected;
	}

	/**
	 * Brings up the file chooser to select a file, but on successful selection, returns the file
	 * and sets the last file path used in settings. Initial file is also the last file used.
	 * @param parent the parent component for the chooser modal.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param lastPathSupplier the supplier to call for the last path used.
	 * @param lastPathSaver the consumer to call for saving the chosen path, if valid.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseFileOrDirectory(Component parent, String title, String approveText, Supplier<File> lastPathSupplier, Consumer<File> lastPathSaver, FileFilter ... choosableFilters)
	{
		return chooseFileOrDirectory(parent, title, approveText, lastPathSupplier, lastPathSaver, (x, file) -> file, choosableFilters);
	}

	/**
	 * Creates a process modal, prepped to start and open.
	 * @param parent the parent owner.
	 * @param title the title of the modal.
	 * @param inFile the input stream file.
	 * @param modalOutFunction function that takes an output stream (STDOUT) and an error output stream (STDERR) and returns an InstancedFuture to start.
	 * @return a modal handle to start with a task manager.
	 */
	public ProcessModal createProcessModal(
		final Container parent, 
		final String title, 
		final File inFile, 
		final TriFunction<PrintStream, PrintStream, InputStream, InstancedFuture<Integer>> modalOutFunction
	){
		return createProcessModal(parent, title, inFile, new DoomToolsTextOutputPanel(), false, modalOutFunction);
	}
	
	/**
	 * Creates a process modal, prepped to start and selectively open.
	 * @param parent the parent owner.
	 * @param title the title of the modal.
	 * @param inFile the input stream file.
	 * @param dontOpen if set, the modal is not opened.
	 * @param modalOutFunction function that takes an output stream (STDOUT) and an error output stream (STDERR) and returns an InstancedFuture to start.
	 * @return a modal handle to start with a task manager.
	 */
	public ProcessModal createProcessModal(
		final Container parent, 
		final String title, 
		final File inFile, 
		final boolean dontOpen,
		final TriFunction<PrintStream, PrintStream, InputStream, InstancedFuture<Integer>> modalOutFunction
	){
		return createProcessModal(parent, title, inFile, new DoomToolsTextOutputPanel(), dontOpen, modalOutFunction);
	}
	
	/**
	 * Creates a process modal, prepped to start and selectively open.
	 * @param parent the parent owner.
	 * @param title the title of the modal.
	 * @param inFile the input stream file.
	 * @param outputPanel the output panel to send out and err output to.
	 * @param dontOpen if set, the modal is not opened.
	 * @param modalOutFunction function that takes an output stream (STDOUT) and an error output stream (STDERR) and returns an InstancedFuture to start.
	 * @return a modal handle to start with a task manager.
	 */
	public ProcessModal createProcessModal(
		final Container parent, 
		final String title, 
		final File inFile, 
		final DoomToolsTextOutputPanel outputPanel,
		final boolean dontOpen,
		final TriFunction<PrintStream, PrintStream, InputStream, InstancedFuture<Integer>> modalOutFunction
	){
		final DoomToolsStatusPanel status = new DoomToolsStatusPanel();
		
		// Show output.
		status.setActivityMessage(language.getText("doomtools.process.activity"));
		
		final Modal<Void> outputModal;
		
		if (!dontOpen)
		{
			outputModal = modal(
				parent, 
				title,
				containerOf(borderLayout(0, 4),
					node(BorderLayout.CENTER, scroll(ScrollPolicy.AS_NEEDED, outputPanel)),
					node(BorderLayout.SOUTH, containerOf(
						node(BorderLayout.WEST, status),
						node(BorderLayout.EAST, containerOf(flowLayout(Flow.RIGHT, 4, 0),
							node(createButtonFromLanguageKey("doomtools.clipboard.copy", (b) -> {
								copyToClipboard(outputPanel.getText());
								status.setSuccessMessage(language.getText("doomtools.clipboard.copy.message"));
							})),
							node(createButtonFromLanguageKey("doomtools.clipboard.save", (b) -> {
								if (saveToFile(outputPanel, outputPanel.getText()))
									status.setSuccessMessage(language.getText("doomtools.clipboard.save.message"));
							}))
						))
					))
				)
			);
		}
		else
		{
			outputModal = null;
		}
		
		return new ProcessModal() 
		{
			@Override
			public void start(DoomToolsTaskManager tasks, final Runnable onStart, final Runnable onEnd) 
			{
				final PrintStream outStream = outputPanel.getPrintStream();
				final PrintStream errorStream = outputPanel.getErrorPrintStream();
				tasks.spawn(() -> 
				{
					if (onStart != null)
						onStart.run();
					
					try (InputStream stdin = inFile != null ? new FileInputStream(inFile) : IOUtils.getNullInputStream()) 
					{
						InstancedFuture<Integer> runInstance = modalOutFunction.apply(outStream, errorStream, stdin);
						Integer result = runInstance.result();
						if (result == 0)
							status.setSuccessMessage(language.getText("doomtools.process.success"));
						else
							status.setErrorMessage(language.getText("doomtools.process.error", String.valueOf(result)));
					} 
					catch (FileNotFoundException e) 
					{
						status.setErrorMessage(language.getText("doomtools.process.error", "Standard In file not found: " + inFile.getPath()));
					} 
					catch (IOException e) 
					{
						status.setErrorMessage(language.getText("doomtools.process.error", e.getLocalizedMessage()));
					}
					
					if (onEnd != null)
						onEnd.run();
				});
				if (!dontOpen)
					outputModal.openThenDispose();
			}
		};
	}
	
	

	/**
	 * Creates a help modal, modeless.
	 * @param sources the help sources to put in the modal.
	 * @return a modal. It starts invisible.
	 */
	public Modal<Void> createHelpModal(HelpSource ... sources)
	{
		return createHelpModal(ModalityType.MODELESS, sources);
	}
	
	/**
	 * Creates a help modal.
	 * @param modalityType the modality type override.
	 * @param sources the help sources to put in the modal.
	 * @return a modal. It starts invisible.
	 */
	public Modal<Void> createHelpModal(ModalityType modalityType, HelpSource ... sources)
	{
		Modal<Void> out;
		if (sources.length > 1)
		{
			Tab[] tabs = new Tab[sources.length];
			for (int i = 0; i < sources.length; i++) 
			{
				HelpSource src = sources[i];
				
				int idx = src.path.lastIndexOf("/");
				String path;
				if (idx < 0)
					path = src.path;
				else
					path = src.path.substring(idx + 1);
				
				JTextArea area = textArea(src.getText(), 25, 80);
				area.setEditable(false);
				area.setFont(EditorSettingsManager.get().getEditorFont());
				tabs[i] = tab(path, src.path, containerOf(borderLayout(), node(BorderLayout.CENTER, scroll(area))));
			}
			
			out = modal(language.getText("doomtools.help.title"), 
				modalityType, 
				containerOf(borderLayout(), 
					node(BorderLayout.CENTER, tabs(TabPlacement.TOP, TabLayoutPolicy.SCROLL, tabs))
				)
			);
		}
		else
		{
			HelpSource src = sources[0];
			
			int idx = src.path.lastIndexOf("/");
			String path;
			if (idx < 0)
				path = src.path;
			else
				path = src.path.substring(idx + 1);
			
			JTextArea area = textArea(src.getText(), 25, 90);
			area.setEditable(false);
			area.setFont(EditorSettingsManager.get().getEditorFont());
			
			out = modal(path, 
				modalityType, 
				containerOf(borderLayout(), 
					node(BorderLayout.CENTER, containerOf(borderLayout(), node(BorderLayout.CENTER, scroll(area))))
				)
			);
		}
		out.setResizable(true);
		return out;
	}
	
	/**
	 * Creates a singular text source for help.
	 * @param name the tab name.
	 * @param text the text.
	 * @return the help source.
	 */
	public HelpSource helpText(String name, String text)
	{
		return new HelpSource(name, text);
	}
	
	/**
	 * Creates a source for help using a path relative to DoomTools's root.
	 * @param path the path.
	 * @return the help source.
	 */
	public HelpSource helpResource(String path)
	{
		return new HelpSource(path, true);
	}
	
	/**
	 * Creates a source for help using a path relative to DoomTools's root.
	 * @param mainClass the main class.
	 * @param helpSwitch the help switch to use. 
	 * @return the help source.
	 */
	public HelpSource helpProcess(Class<?> mainClass, String helpSwitch)
	{
		ProcessCallable callable = Common.spawnJava(mainClass).arg(helpSwitch);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(16 * 1024);
		callable.setOut(bos);
		try {
			callable.call();
			bos.flush();
		} catch (Exception e) {
			e.printStackTrace();
			return new HelpSource(mainClass.getSimpleName(), "[PROCESS ERROR]");
		}
		return new HelpSource(mainClass.getSimpleName(), new String(bos.toByteArray()));
	}
	
	/**
	 * @return the common window icons to use.
	 */
	public List<Image> getWindowIcons() 
	{
		return windowIcons;
	}
	
	/**
	 * @return the single window icon to use.
	 */
	public Icon getWindowIcon() 
	{
		return windowIcon;
	}
	
	/**
	 * @return the DEFSWANI file filter.
	 */
	public FileFilter createDEFSWANIFileFilter()
	{
		return fileExtensionFilter(language.getText("doomtools.filter.defswani.description") + " (*.txt/*.dat)", "txt", "dat");
	}

	/**
	 * @return the DECOHack file filter.
	 */
	public FileFilter createDecoHackFileFilter()
	{
		return fileExtensionFilter(language.getText("doomtools.filter.decohack.description") + " (*.dh)", "dh");
	}

	/**
	 * @return the text file filter.
	 */
	public FileFilter createTextFileFilter()
	{
		return fileExtensionFilter(language.getText("doomtools.filter.textfile.description") + " (*.txt)", "txt");
	}

	/**
	 * @return the WadMerge file filter.
	 */
	public FileFilter createWadMergeFileFilter()
	{
		return fileExtensionFilter(language.getText("doomtools.filter.wadmerge.description") + " (*.wadm/*.wadmerge)", "wadm", "wadmerge");
	}
	
	/**
	 * @return the WadScript file filter.
	 */
	public FileFilter createWadScriptFileFilter()
	{
		return fileExtensionFilter(language.getText("doomtools.filter.wadscript.description") + " (*.wscript/*.wscr/*.wsx)", "wscript", "wscr", "wsx");
	}

	/**
	 * @return the WAD file filter.
	 */
	public FileFilter createWADFileFilter()
	{
		return fileExtensionFilter(language.getText("doomtools.filter.wadfile.description") + " (*.wad)", "wad");
	}
	
	/**
	 * @return the WAD type file filter.
	 */
	public FileFilter createWADTypeFileFilter()
	{
		return fileExtensionFilter(language.getText("doomtools.filter.wadtype.description") + " (*.wad/*.pk3/*.pke)", "wad", "pk3", "pke");
	}
	
	/**
	 * @return the WAD container file filter.
	 */
	public FileFilter createWADContainerFilter()
	{
		return fileExtensionFilter(language.getText("doomtools.filter.container.description") + " (*.wad/*.pk3/*.pke/*.zip)", "wad", "pk3", "pke", "zip");
	}
	
	/**
	 * Creates a {@link FormFieldInfo} object for use with {@link #createForm(JFormPanel, FormFieldInfo...)}.
	 * @param languageKey the language key prefix for the label and tooltip.
	 * @param field the field to add.
	 * @return new info.
	 */
	public FormFieldInfo formField(String languageKey, JFormField<?> field)
	{
		return new FormFieldInfo(languageKey, field);
	}
	
	/**
	 * Creates a {@link FormFieldInfo} object for use with {@link #createForm(JFormPanel, FormFieldInfo...)}.
	 * The label is blank.
	 * @param field the field to add.
	 * @return new info.
	 */
	public FormFieldInfo formField(JFormField<?> field)
	{
		return new FormFieldInfo(null, field);
	}
	
	/* ==================================================================== */

	private void copyToClipboard(String text)
	{
		ClipboardUtils.sendStringToClipboard(text);
	}
	
	private boolean saveToFile(Component parent, String text)
	{
		FileFilter filter = createTextFileFilter();
		File saveFile = chooseFile(parent, 
			language.getText("doomtools.clipboard.save.title"),
			language.getText("doomtools.clipboard.save.choose"),
			settings::getLastFileSave, 
			settings::setLastFileSave,
			(f, input) -> (f == filter ? FileUtils.addMissingExtension(input, "txt") : input),
			filter
		);
		
		if (saveFile == null)
			return false;
		
		try (FileOutputStream fos = new FileOutputStream(saveFile); ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes()))
		{
			IOUtils.relay(bis, fos);
		} 
		catch (IOException e) 
		{
			SwingUtils.error(parent, language.getText("doomtools.clipboard.save.error", e.getLocalizedMessage()));
		}
		catch (SecurityException e) 
		{
			SwingUtils.error(parent, language.getText("doomtools.clipboard.save.security"));
		}
		
		return true;
	}
	
	
	/* ==================================================================== */
	
	/**
	 * Form field info.
	 */
	public static class FormFieldInfo
	{
		private String languageKey;
		private JFormField<?> field;
		
		private FormFieldInfo(String languageKey, JFormField<?> field)
		{
			this.languageKey = languageKey;
			this.field = field;
		}
	}
	
	/**
	 * Help source.
	 */
	public static class HelpSource
	{
		/** Source Relative to DoomTools Path */
		private String path; // 
		/** Is a resource path? */
		private boolean resource;
		/** Text override. */
		private String text;
		
		private HelpSource(String path, String text)
		{
			this.path = path;
			this.text = text;
		}
		
		private HelpSource(String path, boolean resource)
		{
			this.path = path;
			this.resource = resource;
		}
		
		public String getText()
		{
			if (text != null)
			{
				return text;
			}
			else if (resource)
			{
				InputStream in = null;
				try {
					if ((in = IOUtils.openResource(path)) == null)
						return "[NOT FOUND: Resource " + path + "]";
					else
						return IOUtils.getTextualContents(in, StandardCharsets.UTF_8);
				} catch (IOException e) {
					return "[ERROR: Resource " + path + ": " + e.getLocalizedMessage() + "]";
				} finally {
					IOUtils.close(in);
				}
			}
			else
			{
				String root = ObjectUtils.isNull(Environment.getDoomToolsPath(), OSUtils.getWorkingDirectoryPath());
				try (FileInputStream fis = new FileInputStream(new File(root + File.separator + path)))
				{
					return IOUtils.getTextualContents(fis, StandardCharsets.UTF_8);
				} catch (FileNotFoundException e) {
					return "[NOT FOUND: Path " + path + ": " + e.getLocalizedMessage() + "]";
				} catch (IOException e) {
					return "[ERROR: Path " + path + ": " + e.getLocalizedMessage() + "]";
				}
			}
		}
		
	}
	
	/**
	 * Process modal.
	 */
	@FunctionalInterface
	public interface ProcessModal
	{
		/**
		 * Starts the task and opens the modal.
		 * @param tasks
		 * @param onStart called on start.
		 * @param onEnd called on end.
		 */
		default void start(DoomToolsTaskManager tasks)
		{
			start(tasks, null, null);
		}

		/**
		 * Starts the task and opens the modal.
		 * @param tasks
		 * @param onStart called on start.
		 * @param onEnd called on end.
		 */
		void start(DoomToolsTaskManager tasks, Runnable onStart, Runnable onEnd);
	}
	
	/**
	 * Process creation function.
	 * @param <T1>
	 * @param <T2>
	 * @param <T3>
	 * @param <R>
	 */
	@FunctionalInterface
	public interface TriFunction<T1, T2, T3, R>
	{
		/**
		 * Applies this function.
		 * @param t1
		 * @param t2
		 * @param t3
		 * @return the result.
		 */
		R apply(T1 t1, T2 t2, T3 t3);
	}
	
	
}
