/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import static javax.swing.BorderFactory.*;


/**
 * A set of factory methods that build simple modals.
 * @author Matthew Tropiano
 */
public final class ModalFactory
{
	// Don't instantiate.
	private ModalFactory() {}
	
	/* ==================================================================== */
	/* ==== Settings Modals                                            ==== */
	/* ==================================================================== */

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * An optional validator can be executed if the user selects a choice that is set up to call it before the choice is confirmed.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Container owner, List<Image> icons, String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		Boolean out = modal(owner, icons, title, ModalityType.APPLICATION_MODAL, contentPane, validator, choices).openThenDispose();
		if (out == Boolean.TRUE)
			return settingExtractor.apply(contentPane);
		else
			return null;
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Container owner, List<Image> icons, String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(owner, icons, title, contentPane, settingExtractor, choices);
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Container owner, List<Image> icons, String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor)
	{
		return settingsModal(owner, icons, title, contentPane, validator, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Container owner, List<Image> icons, String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(owner, icons, title, contentPane, null, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Container owner, Image icon, String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		Boolean out = modal(owner, icon, title, ModalityType.APPLICATION_MODAL, contentPane, validator, choices).openThenDispose();
		if (out == Boolean.TRUE)
			return settingExtractor.apply(contentPane);
		else
			return null;
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Container owner, Image icon, String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(owner, icon, title, contentPane, null, settingExtractor, choices);
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Container owner, Image icon, String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor)
	{
		return settingsModal(owner, icon, title, contentPane, validator, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Container owner, Image icon, String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(owner, icon, title, contentPane, null, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Container owner, String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(owner, (Image)null, title, contentPane, validator, settingExtractor, choices);
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Container owner, String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(owner, (Image)null, title, contentPane, null, settingExtractor, choices);
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Container owner, String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor)
	{
		return settingsModal(owner, (Image)null, title, contentPane, validator, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Container owner, String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(owner, (Image)null, title, contentPane, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(List<Image> icons, String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(null, icons, title, contentPane, validator, settingExtractor, choices);
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(List<Image> icons, String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(null, icons, title, contentPane, null, settingExtractor, choices);
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(List<Image> icons, String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor)
	{
		return settingsModal(null, icons, title, contentPane, validator, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icons the modal icons.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(List<Image> icons, String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(null, icons, title, contentPane, null, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Image icon, String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(null, icon, title, contentPane, validator, settingExtractor, choices);
	}	
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @param choices the boolean choices on the modal (one must return <code>true</code> for a non-null response).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	@SafeVarargs
	public static <C extends Container, T> T settingsModal(Image icon, String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(null, icon, title, contentPane, null, settingExtractor, choices);
	}	
	
	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Image icon, String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor)
	{
		return settingsModal(null, icon, title, contentPane, validator, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}	

	/**
	 * Creates a modal intended to display a complex pane that contains a set of fields
	 * or values that a user can change, and gathers those values into an object if the user
	 * confirms those values or selections. 
	 * Supplies only one choice: OK.
	 * @param <C> the container pane type.
	 * @param <T> the return type.
	 * @param icon the modal icon.
	 * @param title the modal title.
	 * @param contentPane the content pane that also contains a way to extract values from it.
	 * @param settingExtractor the function to use to extract settings from the content pane (called if the modal returned <code>true</code>).
	 * @return the fetched settings object, or null if the modal returned <code>false</code> or <code>null</code> on close.
	 */
	public static <C extends Container, T> T settingsModal(Image icon, String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(null, icon, title, contentPane, null, settingExtractor, choice("OK", KeyEvent.VK_O, true));
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
	public static <C extends Container, T> T settingsModal(String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(null, (Image)null, title, contentPane, validator, settingExtractor, choices);
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
	public static <C extends Container, T> T settingsModal(String title, C contentPane, Function<C, T> settingExtractor, ModalChoice<Boolean> ... choices)
	{
		return settingsModal(null, (Image)null, title, contentPane, null, settingExtractor, choices);
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
	public static <C extends Container, T> T settingsModal(String title, C contentPane, Predicate<Boolean> validator, Function<C, T> settingExtractor)
	{
		return settingsModal(null, (Image)null, title, contentPane, validator, settingExtractor, choice("OK", KeyEvent.VK_O, true));
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
	public static <C extends Container, T> T settingsModal(String title, C contentPane, Function<C, T> settingExtractor)
	{
		return settingsModal(null, (Image)null, title, contentPane, null, settingExtractor, choice("OK", KeyEvent.VK_O, true));
	}

	
	/* ==================================================================== */
	/* ==== Modals                                                     ==== */
	/* ==================================================================== */

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, List<Image> icons, String title, ModalityType modality, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		Modal<T> out = new Modal<>(owner);
		out.setModalityType(modality);
		out.setTitle(title);
		if (icons != null)
			out.setIconImages(icons);
		return modal(out, contentPane, validator, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, List<Image> icons, String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(owner, icons, title, modality, contentPane, null, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, List<Image> icons, String title, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		return modal(owner, icons, title, ModalityType.APPLICATION_MODAL, contentPane, validator, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, List<Image> icons, String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(owner, icons, title, ModalityType.APPLICATION_MODAL, contentPane, null, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, Image icon, String title, ModalityType modality, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		Modal<T> out = new Modal<>(owner);
		out.setModalityType(modality);
		out.setTitle(title);
		if (icon != null)
			out.setIconImage(icon);
		return modal(out, contentPane, validator, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, Image icon, String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(owner, icon, title, modality, contentPane, null, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, Image icon, String title, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		return modal(owner, icon, title, ModalityType.APPLICATION_MODAL, contentPane, validator, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore, if the modality type makes it so.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, Image icon, String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(owner, icon, title, ModalityType.APPLICATION_MODAL, contentPane, null, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, String title, ModalityType modality, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		return modal(owner, (Image)null, title, modality, contentPane, validator, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(owner, (Image)null, title, modality, contentPane, null, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, String title, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		return modal(owner, (Image)null, title, ModalityType.APPLICATION_MODAL, contentPane, validator, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param owner the owning component.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Container owner, String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(owner, (Image)null, title, ModalityType.APPLICATION_MODAL, contentPane, null, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(List<Image> icons, String title, ModalityType modality, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		return modal(null, icons, title, modality, contentPane, validator, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(List<Image> icons, String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(null, icons, title, modality, contentPane, null, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(List<Image> icons, String title, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		return modal(null, icons, title, ModalityType.APPLICATION_MODAL, contentPane, validator, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param icons the icon images in different dimensions.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(List<Image> icons, String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(null, icons, title, ModalityType.APPLICATION_MODAL, contentPane, null, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Image icon, String title, ModalityType modality, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		return modal(null, icon, title, modality, contentPane, validator, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Image icon, String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(null, icon, title, modality, contentPane, null, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Image icon, String title, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		return modal(null, icon, title, ModalityType.APPLICATION_MODAL, contentPane, validator, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param icon the icon image.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(Image icon, String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal(null, icon, title, ModalityType.APPLICATION_MODAL, contentPane, null, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param modality the modality type for the modal.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(String title, ModalityType modality, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		return modal((Container)null, title, modality, contentPane, validator, choices);
	}
	
	/**
	 * Creates a new modal window.
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
	public static <T> Modal<T> modal(String title, ModalityType modality, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal((Container)null, title, modality, contentPane, null, choices);
	}
	
	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * An optional validator can be executed if the user selects a choice that requires calling it on selection.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param validator the validator function for the input. Input parameter is the selected choice; returns true if modal can continue, false if not.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(String title, Container contentPane, Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		return modal((Container)null, title, ModalityType.APPLICATION_MODAL, contentPane, validator, choices);
	}

	/**
	 * Creates a new modal window.
	 * Modals hold the thread of execution until it is not visible anymore.
	 * You can get the result of the modal via {@link Modal#getValue()}.
	 * @param <T> the return type.
	 * @param title the modal title.
	 * @param contentPane the content pane for the modal.
	 * @param choices the modal choices.
	 * @return a new modal dialog.
	 */
	@SafeVarargs
	public static <T> Modal<T> modal(String title, Container contentPane, final ModalChoice<T> ... choices)
	{
		return modal((Container)null, title, ModalityType.APPLICATION_MODAL, contentPane, null, choices);
	}

	@SafeVarargs
	private static <T> Modal<T> modal(final Modal<T> modal, final Container contentPane, final Predicate<T> validator, final ModalChoice<T> ... choices)
	{
		JButton[] buttons = new JButton[choices.length];
		for (int i = 0; i < buttons.length; i++)
		{
			final ModalChoice<T> choice = choices[i];
			JButton button = new JButton(new AbstractAction(choice.label, choice.icon)
			{
				private static final long serialVersionUID = 7418011293584407833L;

				@Override
				public void actionPerformed(ActionEvent e) 
				{
					T value = choice.onClick.get();
					if (validator != null && choice.validate)
					{
						if (validator.test(value))
						{
							modal.setValue(value);
							modal.setVisible(false);
						}
						// else, do nothing.
					}
					else
					{
						modal.setValue(value);
						modal.setVisible(false);
					}
				}
			});
			button.setMnemonic(choice.mnemonic);
			buttons[i] = button;
		}
		
		JPanel southContent = null;
		
		if (buttons.length > 0)
		{
			southContent = new JPanel();
			southContent.setLayout(new FlowLayout(FlowLayout.TRAILING, 4, 0));
			for (int i = 0; i < buttons.length; i++) 
				southContent.add(buttons[i]);
		}
		
		JPanel modalPanel = new JPanel();
		modalPanel.setLayout(new BorderLayout(0, 4));
		modalPanel.setBorder(createEmptyBorder(8, 8, 8, 8));
		modalPanel.add(BorderLayout.CENTER, contentPane);
		if (southContent != null)
			modalPanel.add(BorderLayout.SOUTH, southContent);
		
		modal.setContentPane(modalPanel);
		modal.setLocationByPlatform(true);
		modal.pack();
		modal.setMinimumSize(modal.getSize());
		modal.setResizable(false);
		modal.setLocationRelativeTo(modal.getOwner());
		modal.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		return modal;
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param validate if true, this choice will run the modal's validator. Otherwise, it will close normally.
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, String label, int mnemonic, boolean validate, Supplier<T> onClick)
	{
		return new ModalChoice<>(icon, label, mnemonic, validate, onClick);
	}

	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, String label, int mnemonic, Supplier<T> onClick)
	{
		return choice(icon, label, mnemonic, false, onClick);
	}

	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param validate if true, this choice will run the modal's validator. Otherwise, it will close normally.
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, int mnemonic, boolean validate, Supplier<T> onClick)
	{
		return choice(null, label, mnemonic, validate, onClick);
	}

	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, int mnemonic, Supplier<T> onClick)
	{
		return choice(null, label, mnemonic, false, onClick);
	}

	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param validate if true, this choice will run the modal's validator. Otherwise, it will close normally.
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, boolean validate, Supplier<T> onClick)
	{
		return choice(icon, null, 0, validate, onClick);
	}

	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, Supplier<T> onClick)
	{
		return choice(icon, null, 0, false, onClick);
	}

	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param validate if true, this choice will run the modal's validator. Otherwise, it will close normally.
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, boolean validate, Supplier<T> onClick)
	{
		return choice(null, label, 0, validate, onClick);
	}

	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param onClick the object supplier function to call on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, Supplier<T> onClick)
	{
		return choice(null, label, 0, false, onClick);
	}

	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param validate if true, this choice will run the modal's validator. Otherwise, it will close normally.
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, String label, int mnemonic, boolean validate, T result)
	{
		return choice(icon, label, mnemonic, validate, () -> result);
	}

	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, String label, int mnemonic, T result)
	{
		return choice(icon, label, mnemonic, false, () -> result);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param validate if true, this choice will run the modal's validator. Otherwise, it will close normally.
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, int mnemonic, boolean validate, T result)
	{
		return choice(null, label, mnemonic, validate, () -> result);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, int mnemonic, T result)
	{
		return choice(null, label, mnemonic, false, () -> result);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param validate if true, this choice will run the modal's validator. Otherwise, it will close normally.
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, boolean validate, T result)
	{
		return choice(icon, null, 0, validate, () -> result);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * Choosing one of these choices will close the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, T result)
	{
		return choice(icon, null, 0, false, () -> result);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param validate if true, this choice will run the modal's validator. Otherwise, it will close normally.
	 * @param mnemonic the key mnemonic for the button (VK key).
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, boolean validate, int mnemonic)
	{
		return choice(null, label, mnemonic, validate, () -> (T)null);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param result the result object to supply on click.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, T result)
	{
		return choice(null, label, 0, false, () -> result);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @param validate if true, this choice will run the modal's validator. Otherwise, it will close normally.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label, boolean validate)
	{
		return choice(null, label, 0, validate, () -> (T)null);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param label the modal button label.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(String label)
	{
		return choice(null, label, 0, false, () -> (T)null);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @param validate if true, this choice will run the modal's validator. Otherwise, it will close normally.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon, boolean validate)
	{
		return choice(icon, null, 0, validate, () -> (T)null);
	}
	
	/**
	 * Creates a single modal choice that appears as a button in the modal.
	 * @param <T> the object return type.
	 * @param icon the modal button icon.
	 * @return a modal choice to use on a new modal.
	 */
	public static <T> ModalChoice<T> choice(Icon icon)
	{
		return choice(icon, null, 0, false, () -> (T)null);
	}
	
	/**
	 * A single modal choice abstracted as a button. 
	 * @param <T> the return type.
	 */
	public static class ModalChoice<T>
	{
		private Icon icon;
		private String label;
		private int mnemonic;
		private boolean validate;
		private Supplier<T> onClick;
		
		private ModalChoice(Icon icon, String label, int mnemonic, boolean validate, Supplier<T> onClick)
		{
			this.icon = icon;
			this.label = label;
			this.mnemonic = mnemonic;
			this.validate = validate;
			this.onClick = onClick;
		}
	}
	
	/**
	 * The custom modal.
	 * @param <T> the return type. 
	 */
	public static class Modal<T> extends JDialog
	{
		private static final long serialVersionUID = 3962942391178207377L;

		private T value;
		
		private Modal(Container owner)
		{
			super(getWindowForComponent(owner));
		}

		private static Window getWindowForComponent(Component parent) throws HeadlessException 
		{
			if (parent == null)
				return null;
			if (parent instanceof Frame || parent instanceof Dialog)
				return (Window)parent;
			return getWindowForComponent(parent.getParent());
		}

		
		@Override
		public void setVisible(boolean b) 
		{
			if (b)
				value = null;
			super.setVisible(b);
		}
		
		/**
		 * Sets the value for this modal.
		 * @param value the value.
		 */
		public void setValue(T value) 
		{
			this.value = value;
		}

		/**
		 * Gets the value result of this dialog.
		 * If the dialog is made visible and closed without a button clicked,
		 * the value is null.
		 * @return the value result.
		 */
		public T getValue() 
		{
			return value;
		}
		
		/**
		 * Opens the dialog, waits for a choice, and then returns it.
		 * @return the value result.
		 */
		public T open()
		{
			setVisible(true);
			return getValue();
		}

		/**
		 * Opens the dialog, waits for a choice, and then returns it and disposes the modal.
		 * @return the value result.
		 */
		public T openThenDispose()
		{
			T out = open();
			dispose();
			return out;
		}
	}

}
