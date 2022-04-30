package net.mtrop.doom.tools.gui.managers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Dialog.ModalityType;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsTextOutputPanel;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.ComponentActionHandler;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.swing.ContainerFactory.Modal;
import net.mtrop.doom.tools.struct.swing.ContainerFactory.ModalChoice;
import net.mtrop.doom.tools.struct.swing.ContainerFactory.ScrollPolicy;
import net.mtrop.doom.tools.struct.swing.FileChooserFactory;

import static javax.swing.BorderFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


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
	
	private DoomToolsImageManager images;
	private DoomToolsLanguageManager language;
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
	public MenuNode createItemFromLanguageKey(String keyPrefix, ComponentActionHandler<JMenuItem> handler)
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
	public MenuNode createCheckItemFromLanguageKey(String keyPrefix, boolean selected, ComponentActionHandler<JCheckBoxMenuItem> handler)
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
	public JButton createButtonFromLanguageKey(Icon icon, String keyPrefix, ComponentActionHandler<JButton> handler)
	{
		return button(
			icon,
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			handler
		);
	}

	/**
	 * Creates a button from a language key, getting the necessary pieces to assemble it.
	 * @param keyPrefix the key prefix.
	 * @param handler the action to take on selection.
	 * @return the new menu item node.
	 */
	public JButton createButtonFromLanguageKey(String keyPrefix, ComponentActionHandler<JButton> handler)
	{
		return createButtonFromLanguageKey(null, keyPrefix, handler);
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
	 * Brings up the file chooser to select a file, but on successful selection, returns the file
	 * and sets the last file path used in settings. Initial file is also the last file used.
	 * @param parent the parent component for the chooser modal.
	 * @param keyName the last path key.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @param choosableFilters the choosable filters.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseFile(Component parent, String keyName, String title, String approveText, FileFilter ... choosableFilters)
	{
		File selected;
		if ((selected = FileChooserFactory.chooseFile(parent, title, settings.getLastPath(keyName), approveText, choosableFilters)) != null)
			settings.setLastPath(keyName, selected);
		return selected;
	}

	/**
	 * Brings up the file chooser to select a directory, but on successful selection, returns the directory
	 * and sets the last project path used in settings. Initial file is also the last project directory used.
	 * @param parent the parent component for the chooser modal.
	 * @param keyName the last path key.
	 * @param title the dialog title.
	 * @param approveText the text to put on the approval button.
	 * @return the selected file, or null if no file was selected for whatever reason.
	 */
	public File chooseDirectory(Component parent, String keyName, String title, String approveText)
	{
		File selected;
		if ((selected = FileChooserFactory.chooseDirectory(parent, title, settings.getLastPath(keyName), approveText, FileFilters.DIRECTORIES)) != null)
			settings.setLastPath(keyName, selected);
		return selected;
	}

	/**
	 * Creates a process modal, prepped to start and open.
	 * @param parent
	 * @param title
	 * @param activityMessage
	 * @param successMessage
	 * @param errorMessage
	 * @param modalOutFunction
	 * @return a modal handle to start with a task manager.
	 */
	public ProcessModal createProcessModal(final Container parent, final String title, final String activityMessage, final String successMessage, final String errorMessage, final Function<PrintStream, InstancedFuture<Integer>> modalOutFunction) 
	{
		// Show output.
		final DoomToolsTextOutputPanel outputPanel = new DoomToolsTextOutputPanel();
		final DoomToolsStatusPanel status = new DoomToolsStatusPanel();
		
		status.setActivityMessage(activityMessage);
		
		final Modal<Void> outputModal = modal(
			parent, 
			title,
			containerOf(borderLayout(0, 4),
				node(BorderLayout.CENTER, scroll(ScrollPolicy.AS_NEEDED, outputPanel)),
				node(BorderLayout.SOUTH, status)
			)
		);
		
		return new ProcessModal() 
		{
			@Override
			public void start(DoomToolsTaskManager tasks) 
			{
				final PrintStream outStream = outputPanel.getPrintStream();
				tasks.spawn(() -> {
					InstancedFuture<Integer> runInstance = modalOutFunction.apply(outStream);
					Integer result = runInstance.result();
					if (result == 0)
						status.setSuccessMessage(successMessage);
					else
						status.setErrorMessage(errorMessage);
				});
				outputModal.openThenDispose();
			}
		};
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
	 * Process modal.
	 */
	public interface ProcessModal
	{
		/**
		 * Starts the task and opens the modal.
		 * @param tasks
		 */
		void start(DoomToolsTaskManager tasks);
	}
	
}
