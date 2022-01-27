package net.mtrop.doom.tools.gui;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.item;

import java.awt.Image;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.ComponentActionHandler;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;

import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;

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
	 * @return the singleton instance of this settings object.
	 */
	public static DoomToolsGUIUtils get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	private DoomToolsImageManager images;
	private DoomToolsLanguageManager language;
	private List<Image> windowIcons;
	private Icon windowIcon;
	
	private DoomToolsGUIUtils()
	{
		this.images = DoomToolsImageManager.get();
		this.language = DoomToolsLanguageManager.get();
		
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
		return item(
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
		return item(
			language.getText(keyPrefix),
			language.getMnemonicValue(keyPrefix + ".mnemonic"),
			language.getKeyStroke(keyPrefix + ".keystroke"),
			handler
		);
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
	
}
