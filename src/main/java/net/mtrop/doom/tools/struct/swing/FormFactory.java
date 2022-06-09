/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import static javax.swing.BorderFactory.*;


/**
 * A field factory that creates form fields.
 * @author Matthew Tropiano
 */
public final class FormFactory
{
	// Don't instantiate.
	private FormFactory() {}
	
	/**
	 * Creates a new text field that stores a custom value type, with a "browse" 
	 * button that calls an outside function to set a value.
	 * @param <T> the type that this field stores.
	 * @param initialValue the field's initial value.
	 * @param browseText the text to put in the browse button.
	 * @param browseFunction the function to call when the browse button is clicked (the parameter is the current value).
	 * @param converter the converter interface for text to value and back.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static <T> JFormField<T> valueBrowseTextField(T initialValue, String browseText, Function<T, T> browseFunction, JValueConverter<T> converter, JValueChangeListener<T> changeListener)
	{
		return new JValueBrowseField<>(initialValue, browseText, browseFunction, converter, changeListener);
	}

	/**
	 * Creates a new text field that stores a custom value type, with a "browse" 
	 * button that calls an outside function to set a value.
	 * @param <T> the type that this field stores.
	 * @param initialValue the field's initial value.
	 * @param browseFunction the function to call when the browse button is clicked (the parameter is the current value).
	 * @param converter the converter interface for text to value and back.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static <T> JFormField<T> valueBrowseTextField(T initialValue, Function<T, T> browseFunction, JValueConverter<T> converter, JValueChangeListener<T> changeListener)
	{
		return valueBrowseTextField(initialValue, "...", browseFunction, converter, changeListener);
	}

	/**
	 * Creates a new text field that stores a custom value type, with a "browse" 
	 * button that calls an outside function to set a value.
	 * @param <T> the type that this field stores.
	 * @param initialValue the field's initial value.
	 * @param browseText the text to put in the browse button.
	 * @param browseFunction the function to call when the browse button is clicked (the parameter is the current value).
	 * @param converter the converter interface for text to value and back.
	 * @return the generated field.
	 */
	public static <T> JFormField<T> valueBrowseTextField(T initialValue, String browseText, Function<T, T> browseFunction, JValueConverter<T> converter)
	{
		return valueBrowseTextField(initialValue, browseText, browseFunction, converter, null);
	}

	/**
	 * Creates a new text field that stores a custom value type, with a "browse" 
	 * button that calls an outside function to set a value.
	 * @param <T> the type that this field stores.
	 * @param initialValue the field's initial value.
	 * @param browseFunction the function to call when the browse button is clicked (the parameter is the current value).
	 * @param converter the converter interface for text to value and back.
	 * @return the generated field.
	 */
	public static <T> JFormField<T> valueBrowseTextField(T initialValue, Function<T, T> browseFunction, JValueConverter<T> converter)
	{
		return valueBrowseTextField(initialValue, "...", browseFunction, converter, null);
	}

	/**
	 * Creates a new text field that stores a custom value type, with a "browse" 
	 * button that calls an outside function to set a value.
	 * @param <T> the type that this field stores.
	 * @param browseText the text to put in the browse button.
	 * @param browseFunction the function to call when the browse button is clicked (the parameter is the current value).
	 * @param converter the converter interface for text to value and back.
	 * @return the generated field.
	 */
	public static <T> JFormField<T> valueBrowseTextField(String browseText, Function<T, T> browseFunction, JValueConverter<T> converter)
	{
		return valueBrowseTextField(null, browseText, browseFunction, converter, null);
	}

	/**
	 * Creates a new text field that stores a custom value type, with a "browse" 
	 * button that calls an outside function to set a value.
	 * @param <T> the type that this field stores.
	 * @param browseFunction the function to call when the browse button is clicked (the parameter is the current value).
	 * @param converter the converter interface for text to value and back.
	 * @return the generated field.
	 */
	public static <T> JFormField<T> valueBrowseTextField(Function<T, T> browseFunction, JValueConverter<T> converter)
	{
		return valueBrowseTextField(null, "...", browseFunction, converter, null);
	}

	/**
	 * Creates a new text field that stores a custom value type.
	 * @param <T> the type that this field stores.
	 * @param initialValue the field's initial value.
	 * @param converter the converter interface for text to value and back.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static <T> JFormField<T> valueTextField(T initialValue, JValueConverter<T> converter, JValueChangeListener<T> changeListener)
	{
		return new JValueTextField<>(initialValue, converter, changeListener);
	}

	/**
	 * Creates a new text field that stores a custom value type.
	 * @param <T> the type that this field stores.
	 * @param converter the converter interface for text to value and back.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static <T> JFormField<T> valueTextField(JValueConverter<T> converter, JValueChangeListener<T> changeListener)
	{
		return valueTextField(null, converter, changeListener);
	}

	/**
	 * Creates a new text field that stores a custom value type.
	 * @param <T> the type that this field stores.
	 * @param initialValue the field's initial value.
	 * @param converter the converter interface for text to value and back.
	 * @return the generated field.
	 */
	public static <T> JFormField<T> valueTextField(T initialValue, JValueConverter<T> converter)
	{
		return valueTextField(initialValue, converter, null);
	}

	/**
	 * Creates a new text field that stores a custom value type.
	 * @param <T> the type that this field stores.
	 * @param converter the converter interface for text to value and back.
	 * @return the generated field.
	 */
	public static <T> JFormField<T> valueTextField(JValueConverter<T> converter)
	{
		return valueTextField(null, converter);
	}

	/**
	 * Creates a new text field that stores a string type.
	 * Empty strings are considered null if this field is nullable.
	 * Nulls are converted to empty string.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<String> stringField(String initialValue, final boolean nullable, JValueChangeListener<String> changeListener)
	{
		return valueTextField(initialValue, converter(
			(text) -> {
				text = text.trim();
				return text.length() == 0 ? (nullable ? null : "") : text;
			},
			(value) -> (
				value != null ? String.valueOf(value) : ""
			)
		), changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a string type.
	 * @param initialValue the field's initial value.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<String> stringField(String initialValue, JValueChangeListener<String> changeListener)
	{
		return stringField(initialValue, false, changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a string type.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<String> stringField(JValueChangeListener<String> changeListener)
	{
		return stringField("", false, changeListener); 
	}

	/**
	 * Creates a new text field that stores a string type.
	 * Empty strings are considered null if this field is nullable.
	 * Nulls are converted to empty string.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<String> stringField(String initialValue, boolean nullable)
	{
		return stringField(initialValue, nullable, null); 
	}

	/**
	 * Creates a new non-nullable text field that stores a string type.
	 * @param initialValue the field's initial value.
	 * @return the generated field.
	 */
	public static JFormField<String> stringField(String initialValue)
	{
		return stringField(initialValue, false, null); 
	}

	/**
	 * Creates a new text field that stores a string type.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<String> stringField(boolean nullable)
	{
		return stringField("", nullable, null); 
	}

	/**
	 * Creates a new non-nullable text field that stores a string type.
	 * @return the generated field.
	 */
	public static JFormField<String> stringField()
	{
		return stringField("", false, null); 
	}

	/**
	 * Creates a new text field that stores a double type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Double> doubleField(Double initialValue, final boolean nullable, JValueChangeListener<Double> changeListener)
	{
		return valueTextField(initialValue, converter(
			(text) -> {
				try {
					return Double.parseDouble(text.trim());
				} catch (NumberFormatException e) {
					return nullable ? null : 0.0;
				}
			},
			(value) -> (
				value != null ? String.valueOf(value) : (nullable ? "" : "0.0")
			)
		), changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a double type.
	 * @param initialValue the field's initial value.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Double> doubleField(double initialValue, JValueChangeListener<Double> changeListener)
	{
		return doubleField(initialValue, false, changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a double type.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Double> doubleField(JValueChangeListener<Double> changeListener)
	{
		return doubleField(0.0, false, changeListener); 
	}

	/**
	 * Creates a new text field that stores a double type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<Double> doubleField(Double initialValue, boolean nullable)
	{
		return doubleField(initialValue, nullable, null); 
	}

	/**
	 * Creates a new non-nullable text field that stores a double type.
	 * @param initialValue the field's initial value.
	 * @return the generated field.
	 */
	public static JFormField<Double> doubleField(double initialValue)
	{
		return doubleField(initialValue, false, null); 
	}

	/**
	 * Creates a new text field that stores a double type.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<Double> doubleField(boolean nullable)
	{
		return doubleField(0.0, nullable, null); 
	}

	/**
	 * Creates a new non-nullable text field that stores a double type.
	 * @return the generated field.
	 */
	public static JFormField<Double> doubleField()
	{
		return doubleField(0.0, false, null); 
	}

	/**
	 * Creates a new text field that stores a float type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Float> floatField(Float initialValue, final boolean nullable, JValueChangeListener<Float> changeListener)
	{
		return valueTextField(initialValue, converter(
			(text) -> {
				try {
					return Float.parseFloat(text.trim());
				} catch (NumberFormatException e) {
					return nullable ? null : 0.0f;
				}
			},
			(value) -> (
				value != null ? String.valueOf(value) : (nullable ? "" : "0")
			)
		), changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a float type.
	 * @param initialValue the field's initial value.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Float> floatField(float initialValue, JValueChangeListener<Float> changeListener)
	{
		return floatField(initialValue, false, changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a float type.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Float> floatField(JValueChangeListener<Float> changeListener)
	{
		return floatField(0f, false, changeListener); 
	}

	/**
	 * Creates a new text field that stores a float type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<Float> floatField(Float initialValue, boolean nullable)
	{
		return floatField(initialValue, nullable, null); 
	}

	/**
	 * Creates a new non-nullable text field that stores a float type.
	 * @param initialValue the field's initial value.
	 * @return the generated field.
	 */
	public static JFormField<Float> floatField(float initialValue)
	{
		return floatField(initialValue, false, null); 
	}

	/**
	 * Creates a new text field that stores a float type.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<Float> floatField(boolean nullable)
	{
		return floatField(0f, nullable, null); 
	}

	/**
	 * Creates a new non-nullable text field that stores a float type.
	 * @return the generated field.
	 */
	public static JFormField<Float> floatField()
	{
		return floatField(0f, false, null); 
	}

	/**
	 * Creates a new text field that stores a long integer type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Long> longField(Long initialValue, final boolean nullable, JValueChangeListener<Long> changeListener)
	{
		return valueTextField(initialValue, converter(
			(text) -> {
				try {
					return Long.parseLong(text.trim());
				} catch (NumberFormatException e) {
					return nullable ? null : (long)0;
				}
			},
			(value) -> (
				value != null ? String.valueOf(value) : (nullable ? "" : "0")
			)
		), changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a long integer type.
	 * @param initialValue the field's initial value.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Long> longField(long initialValue, JValueChangeListener<Long> changeListener)
	{
		return longField(initialValue, false, changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a long integer type.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Long> longField(JValueChangeListener<Long> changeListener)
	{
		return longField(0L, false, changeListener); 
	}

	/**
	 * Creates a new text field that stores a long integer type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<Long> longField(Long initialValue, boolean nullable)
	{
		return longField(initialValue, nullable, null); 
	}

	/**
	 * Creates a new non-nullable text field that stores a long integer type.
	 * @param initialValue the field's initial value.
	 * @return the generated field.
	 */
	public static JFormField<Long> longField(long initialValue)
	{
		return longField(initialValue, false, null); 
	}

	/**
	 * Creates a new text field that stores a long integer type.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<Long> longField(boolean nullable)
	{
		return longField(0L, nullable, null); 
	}

	/**
	 * Creates a new non-nullable text field that stores a long integer type.
	 * @return the generated field.
	 */
	public static JFormField<Long> longField()
	{
		return longField(0L, false, null); 
	}

	/**
	 * Creates a new text field that stores an integer type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Integer> integerField(Integer initialValue, final boolean nullable, JValueChangeListener<Integer> changeListener)
	{
		return valueTextField(initialValue, converter(
			(text) -> {
				try {
					return Integer.parseInt(text.trim());
				} catch (NumberFormatException e) {
					return nullable ? null : 0;
				}
			},
			(value) -> (
				value != null ? String.valueOf(value) : (nullable ? "" : "0")
			)
		), changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores an integer type.
	 * @param initialValue the field's initial value.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Integer> integerField(int initialValue, JValueChangeListener<Integer> changeListener)
	{
		return integerField(initialValue, false, changeListener);
	}

	/**
	 * Creates a new non-nullable text field that stores an integer type.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Integer> integerField(JValueChangeListener<Integer> changeListener)
	{
		return integerField(0, false, changeListener);
	}

	/**
	 * Creates a new text field that stores an integer type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<Integer> integerField(Integer initialValue, boolean nullable)
	{
		return integerField(initialValue, nullable, null);
	}

	/**
	 * Creates a new non-nullable text field that stores an integer type.
	 * @param initialValue the field's initial value.
	 * @return the generated field.
	 */
	public static JFormField<Integer> integerField(int initialValue)
	{
		return integerField(initialValue, false, null);
	}

	/**
	 * Creates a new text field that stores an integer type.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<Integer> integerField(boolean nullable)
	{
		return integerField(0, nullable, null);
	}

	/**
	 * Creates a new non-nullable text field that stores an integer type.
	 * @return the generated field.
	 */
	public static JFormField<Integer> integerField()
	{
		return integerField(0, false, null);
	}

	/**
	 * Creates a new text field that stores a short type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Short> shortField(Short initialValue, final boolean nullable, JValueChangeListener<Short> changeListener)
	{
		return valueTextField(initialValue, converter(
			(text) -> {
				try {
					return Short.parseShort(text.trim());
				} catch (NumberFormatException e) {
					return nullable ? null : (short)0;
				}
			},
			(value) -> (
				value != null ? String.valueOf(value) : (nullable ? "" : "0")
			)
		), changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a short type.
	 * @param initialValue the field's initial value.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Short> shortField(short initialValue, JValueChangeListener<Short> changeListener)
	{
		return shortField(initialValue, false, changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a short type.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Short> shortField(JValueChangeListener<Short> changeListener)
	{
		return shortField((short)0, false, changeListener); 
	}

	/**
	 * Creates a new text field that stores a short type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<Short> shortField(Short initialValue, boolean nullable)
	{
		return shortField(initialValue, nullable, null); 
	}

	/**
	 * Creates a new non-nullable text field that stores a short type.
	 * @param initialValue the field's initial value.
	 * @return the generated field.
	 */
	public static JFormField<Short> shortField(short initialValue)
	{
		return shortField(initialValue, false, null); 
	}

	/**
	 * Creates a new text field that stores a byte type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Byte> byteField(Byte initialValue, final boolean nullable, JValueChangeListener<Byte> changeListener)
	{
		return valueTextField(initialValue, converter(
			(text) -> {
				try {
					return Byte.parseByte(text.trim());
				} catch (NumberFormatException e) {
					return nullable ? null : (byte)0;
				}
			},
			(value) -> (
				value != null ? String.valueOf(value) : (nullable ? "" : "0")
			)
		), changeListener);
	}

	/**
	 * Creates a new non-nullable text field that stores a byte type.
	 * @param initialValue the field's initial value.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Byte> byteField(byte initialValue, JValueChangeListener<Byte> changeListener)
	{
		return byteField(initialValue, false, changeListener); 
	}

	/**
	 * Creates a new non-nullable text field that stores a byte type.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the generated field.
	 */
	public static JFormField<Byte> byteField(JValueChangeListener<Byte> changeListener)
	{
		return byteField((byte)0, false, changeListener);
	}

	/**
	 * Creates a new text field that stores a byte type.
	 * A blank value means null.
	 * @param initialValue the field's initial value.
	 * @param nullable if true, this is a nullable field.
	 * @return the generated field.
	 */
	public static JFormField<Byte> byteField(Byte initialValue, boolean nullable)
	{
		return byteField(initialValue, nullable, null); 
	}

	/**
	 * Creates a new non-nullable text field that stores a byte type.
	 * @param initialValue the field's initial value.
	 * @return the generated field.
	 */
	public static JFormField<Byte> byteField(byte initialValue)
	{
		return byteField(initialValue, false, null); 
	}

	/* ==================================================================== */

	/**
	 * Creates a password field.
	 * @param initialValue the initial value of the field.
	 * @param changeListener the change listener.
	 * @return the generated field.
	 */
	public static JFormField<String> passwordField(String initialValue, JValueChangeListener<String> changeListener)
	{
		return new JValuePasswordField<String>(initialValue, converter(
			Function.identity(),
			(value) -> (
				value != null ? String.valueOf(value) : ""
			)
		), changeListener);
	}	
	
	/**
	 * Creates a password field.
	 * @param initialValue the initial value of the field.
	 * @return the generated field.
	 */
	public static JFormField<String> passwordField(String initialValue)
	{
		return passwordField(initialValue, null);
	}	
	
	/* ==================================================================== */

	/**
	 * Creates a keystroke field.
	 * @param initialValue the initial value of the field.
	 * @param transformer the transformer function for altering an incoming keystroke before set.
	 * @param changeListener the change listener.
	 * @return the generated field.
	 */
	public static JFormField<KeyStroke> keyStrokeField(KeyStroke initialValue, Function<KeyStroke, KeyStroke> transformer, JValueChangeListener<KeyStroke> changeListener)
	{
		return new JValueKeystrokeField(initialValue, transformer, changeListener);
	}	
	
	/**
	 * Creates a keystroke field.
	 * @param initialValue the initial value of the field.
	 * @param changeListener the change listener.
	 * @return the generated field.
	 */
	public static JFormField<KeyStroke> keyStrokeField(KeyStroke initialValue, JValueChangeListener<KeyStroke> changeListener)
	{
		return keyStrokeField(initialValue, Function.identity(), changeListener);
	}	
	
	/**
	 * Creates a keystroke field.
	 * @param initialValue the initial value of the field.
	 * @param transformer the transformer function for altering an incoming keystroke before set.
	 * @return the generated field.
	 */
	public static JFormField<KeyStroke> keyStrokeField(KeyStroke initialValue, Function<KeyStroke, KeyStroke> transformer)
	{
		return keyStrokeField(initialValue, transformer, null);
	}	
	
	/**
	 * Creates a keystroke field.
	 * @param initialValue the initial value of the field.
	 * @return the generated field.
	 */
	public static JFormField<KeyStroke> keyStrokeField(KeyStroke initialValue)
	{
		return keyStrokeField(initialValue, Function.identity(), null);
	}	
	
	/* ==================================================================== */

	/**
	 * Creates a new color field with a color picker button.
	 * @param initialValue the field's initial value.
	 * @param alpha if true, support alpha channel.
	 * @param browseText the browse button text.
	 * @param browseTitle the title of the color picker dialog.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the new color selection field.
	 */
	public static JFormField<Color> colorField(Color initialValue, boolean alpha, String browseText, String browseTitle, JValueChangeListener<Color> changeListener)
	{
		return new JValueColorField(initialValue, alpha, browseText, browseTitle, changeListener);
	}
	
	/**
	 * Creates a new color field with a color picker button, no alpha channel support.
	 * @param initialValue the field's initial value.
	 * @param alpha if true, support alpha channel.
	 * @param browseTitle the title of the color picker dialog.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the new color selection field.
	 */
	public static JFormField<Color> colorField(Color initialValue, boolean alpha, String browseTitle, JValueChangeListener<Color> changeListener)
	{
		return colorField(initialValue, alpha, "...", browseTitle, changeListener);
	}
	
	/**
	 * Creates a new color field with a color picker button, no alpha channel support.
	 * @param initialValue the field's initial value.
	 * @param alpha if true, support alpha channel.
	 * @param browseText the browse button text.
	 * @param browseTitle the title of the color picker dialog.
	 * @return the new color selection field.
	 */
	public static JFormField<Color> colorField(Color initialValue, boolean alpha, String browseText, String browseTitle)
	{
		return colorField(initialValue, alpha, browseText, browseTitle, null);
	}
	
	/**
	 * Creates a new color field with a color picker button, no alpha channel support.
	 * @param initialValue the field's initial value.
	 * @param alpha if true, support alpha channel.
	 * @param browseTitle the title of the color picker dialog.
	 * @return the new color selection field.
	 */
	public static JFormField<Color> colorField(Color initialValue, boolean alpha, String browseTitle)
	{
		return colorField(initialValue, alpha, "...", browseTitle, null);
	}
	
	/**
	 * Creates a new color field with a color picker button, no alpha channel support.
	 * @param initialValue the field's initial value.
	 * @param browseText the browse button text.
	 * @param browseTitle the title of the color picker dialog.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the new color selection field.
	 */
	public static JFormField<Color> colorField(Color initialValue, String browseText, String browseTitle, JValueChangeListener<Color> changeListener)
	{
		return colorField(initialValue, false, browseText, browseTitle, changeListener);
	}
	
	/**
	 * Creates a new color field with a color picker button, no alpha channel support.
	 * @param initialValue the field's initial value.
	 * @param browseTitle the title of the color picker dialog.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the new color selection field.
	 */
	public static JFormField<Color> colorField(Color initialValue, String browseTitle, JValueChangeListener<Color> changeListener)
	{
		return colorField(initialValue, false, "...", browseTitle, changeListener);
	}
	
	/**
	 * Creates a new color field with a color picker button, no alpha channel support.
	 * @param initialValue the field's initial value.
	 * @param browseText the browse button text.
	 * @param browseTitle the title of the color picker dialog.
	 * @return the new color selection field.
	 */
	public static JFormField<Color> colorField(Color initialValue, String browseText, String browseTitle)
	{
		return colorField(initialValue, false, browseText, browseTitle, null);
	}
	
	/**
	 * Creates a new color field with a color picker button, no alpha channel support.
	 * @param initialValue the field's initial value.
	 * @param browseTitle the title of the color picker dialog.
	 * @return the new color selection field.
	 */
	public static JFormField<Color> colorField(Color initialValue, String browseTitle)
	{
		return colorField(initialValue, false, "...", browseTitle, null);
	}
	
	/* ==================================================================== */

	/**
	 * Creates a new file field with a button to browse for a file.
	 * @param initialValue the field's initial value.
	 * @param browseText the browse button text.
	 * @param browseFunction the function to call to browse for a file.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the new file selection field.
	 */
	public static JFormField<File> fileField(File initialValue, String browseText, Function<File, File> browseFunction, JValueChangeListener<File> changeListener)
	{
		return valueBrowseTextField(initialValue, browseText, browseFunction, 
			converter(
				(text) -> {
					text = text.trim();
					if (text.length() == 0)
						return null;
					return new File(text);
				},
				(value) -> value != null ? (value.exists() ? value.getAbsolutePath() : value.getPath()) : ""
			), 
			changeListener
		);
	}
	
	/**
	 * Creates a new file field with a button to browse for a file.
	 * @param initialValue the field's initial value.
	 * @param browseFunction the function to call to browse for a file.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the new file selection field.
	 */
	public static JFormField<File> fileField(File initialValue, Function<File, File> browseFunction, JValueChangeListener<File> changeListener)
	{
		return fileField(initialValue, "...", browseFunction, changeListener);
	}
	
	/**
	 * Creates a new file field with a button to browse for a file.
	 * @param browseFunction the function to call to browse for a file.
	 * @param changeListener the listener to use when a value change occurs.
	 * @return the new file selection field.
	 */
	public static JFormField<File> fileField(Function<File, File> browseFunction, JValueChangeListener<File> changeListener)
	{
		return fileField(null, "...", browseFunction, changeListener);
	}
	
	/**
	 * Creates a new file field with a button to browse for a file.
	 * @param initialValue the field's initial value.
	 * @param browseText the browse button text.
	 * @param browseFunction the function to call to browse for a file.
	 * @return the new file selection field.
	 */
	public static JFormField<File> fileField(File initialValue, String browseText, Function<File, File> browseFunction)
	{
		return fileField(initialValue, browseText, browseFunction, null);
	}
	
	/**
	 * Creates a new file field with a button to browse for a file.
	 * @param browseText the browse button text.
	 * @param browseFunction the function to call to browse for a file.
	 * @return the new file selection field.
	 */
	public static JFormField<File> fileField(String browseText, Function<File, File> browseFunction)
	{
		return fileField(null, browseText, browseFunction, null);
	}
	
	/**
	 * Creates a new file field with a button to browse for a file.
	 * @param initialValue the field's initial value.
	 * @param browseFunction the function to call to browse for a file.
	 * @return the new file selection field.
	 */
	public static JFormField<File> fileField(File initialValue, Function<File, File> browseFunction)
	{
		return fileField(initialValue, "...", browseFunction, null);
	}
	
	/**
	 * Creates a new file field with a button to browse for a file.
	 * @param browseFunction the function to call to browse for a file.
	 * @return the new file selection field.
	 */
	public static JFormField<File> fileField(Function<File, File> browseFunction)
	{
		return fileField(null, "...", browseFunction, null);
	}
	
	/* ==================================================================== */

	/**
	 * Creates a form field from a text area.
	 * @param textArea the text area to encapsulate.
	 * @return a new form field that encapsulates a text area.
	 */
	public static JFormField<String> textAreaField(final JTextArea textArea)
	{
		return new JFormField<String>() 
		{
			private static final long serialVersionUID = 2756507116966376754L;
			
			private JTextArea field;
			
			{
				setLayout(new BorderLayout());
				add(BorderLayout.CENTER, this.field = textArea);
			}
			
			@Override
			public String getValue()
			{
				return field.getText();
			}

			@Override
			public void setValue(String value)
			{
				field.setText(value);
			}

			@Override
			protected Component getFormComponent() 
			{
				return field;
			}
		};
	}

	/**
	 * Creates a form field from a check box.
	 * @param checkBox the checkbox to encapsulate.
	 * @return a new form field that encapsulates a checkbox.
	 */
	public static JFormField<Boolean> checkBoxField(final JCheckBox checkBox)
	{
		return new JFormField<Boolean>() 
		{
			private static final long serialVersionUID = -1477632818725772731L;

			private JCheckBox field;
			
			{
				setLayout(new BorderLayout());
				add(BorderLayout.CENTER, this.field = checkBox);
			}
			
			@Override
			public Boolean getValue()
			{
				return field.isSelected();
			}

			@Override
			public void setValue(Boolean value)
			{
				field.setSelected(value);
			}

			@Override
			protected Component getFormComponent() 
			{
				return field;
			}
		};
	}
	
	/**
	 * Creates a form field from a check box.
	 * @param radio the radio button to encapsulate.
	 * @return a new form field that encapsulates a radio button.
	 */
	public static JFormField<Boolean> radioField(final JRadioButton radio)
	{
		return new JFormField<Boolean>() 
		{
			private static final long serialVersionUID = -9153299653112501886L;
			
			private JRadioButton field;
			
			{
				setLayout(new BorderLayout());
				add(BorderLayout.CENTER, this.field = radio);
			}
			
			@Override
			public Boolean getValue()
			{
				return field.isSelected();
			}

			@Override
			public void setValue(Boolean value)
			{
				field.setSelected(value);
			}

			@Override
			protected Component getFormComponent() 
			{
				return field;
			}
		};
	}
	
	/**
	 * Creates a form field from a slider.
	 * @param minLabel the minimum value label.
	 * @param maxLabel the maximum value label.
	 * @param slider the slider to encapsulate.
	 * @return a new form field that encapsulates a slider.
	 */
	public static JFormField<Integer> sliderField(String minLabel, String maxLabel, final JSlider slider)
	{
		return new JFormField<Integer>() 
		{
			private static final long serialVersionUID = 4363610257614419998L;

			private JSlider field;
			
			{
				setLayout(new BorderLayout());
				if (minLabel != null)
					add(BorderLayout.WEST, new JLabel(minLabel));
				add(BorderLayout.CENTER, this.field = slider);
				if (maxLabel != null)
					add(BorderLayout.EAST, new JLabel(maxLabel));
			}
			
			@Override
			public Integer getValue()
			{
				return field.getValue();
			}

			@Override
			public void setValue(Integer value)
			{
				field.setValue(value);
			}

			@Override
			protected Component getFormComponent() 
			{
				return field;
			}
		};
	}
	
	/**
	 * Creates a form field from a slider.
	 * @param slider the slider to encapsulate.
	 * @return a new form field that encapsulates a slider.
	 */
	public static JFormField<Integer> sliderField(final JSlider slider)
	{
		return sliderField(
			String.valueOf(slider.getModel().getMinimum()),
			String.valueOf(slider.getModel().getMaximum()),
			slider
		);
	}

	/**
	 * Creates a form field from a spinner.
	 * @param <T> the spinner return type.
	 * @param spinner the spinner to encapsulate.
	 * @return a new form field that encapsulates a spinner.
	 */
	public static <T> JFormField<T> spinnerField(final JSpinner spinner)
	{
		return new JFormField<T>() 
		{
			private static final long serialVersionUID = -876324303202896183L;
			
			private JSpinner field;
			
			{
				setLayout(new BorderLayout());
				add(BorderLayout.CENTER, this.field = spinner);
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public T getValue()
			{
				return (T)field.getValue();
			}

			@Override
			public void setValue(Object value)
			{
				field.setValue(value);
			}

			@Override
			protected Component getFormComponent() 
			{
				return field;
			}
		};
	}
	
	/**
	 * Creates a form field from a combo box.
	 * @param <T> the combo box type.
	 * @param comboBox the combo box to encapsulate.
	 * @return a new form field that encapsulates a combo box.
	 */
	public static <T> JFormField<T> comboField(final JComboBox<T> comboBox)
	{
		return new JFormField<T>() 
		{
			private static final long serialVersionUID = -7563041869993609681L;

			private JComboBox<T> field;
			
			{
				setLayout(new BorderLayout());
				add(BorderLayout.CENTER, this.field = comboBox);
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public T getValue()
			{
				return (T)field.getSelectedItem();
			}

			@Override
			public void setValue(Object value)
			{
				field.setSelectedItem(value);
			}

			@Override
			protected Component getFormComponent() 
			{
				return field;
			}
		};
	}
	
	/**
	 * Creates a form field from a list.
	 * @param <T> the list type.
	 * @param list the list to encapsulate.
	 * @return a new form field that encapsulates a list.
	 */
	public static <T> JFormField<T> listField(final JList<T> list)
	{
		return new JFormField<T>() 
		{
			private static final long serialVersionUID = 1064013371765783373L;
			
			private JList<T> field;
			
			{
				setLayout(new BorderLayout());
				add(BorderLayout.CENTER, this.field = list);
			}
			
			@Override
			public T getValue()
			{
				return field.getSelectedValue();
			}

			@Override
			public void setValue(Object value)
			{
				field.setSelectedValue(value, true);
			}
			
			@Override
			protected Component getFormComponent() 
			{
				return field;
			}
		};
	}
	
	/**
	 * Creates a form field that is a series of buttons.
	 * No value is held, nor set.
	 * @param buttons the buttons to encapsulate.
	 * @return a new form field that encapsulates a set of buttons.
	 */
	public static JFormField<Void> buttonField(final JButton ... buttons)
	{
		return new JFormField<Void>()
		{
			private static final long serialVersionUID = -7511474019499338246L;
			
			private JPanel field;
			private List<JButton> buttonList;
			
			{
				setLayout(new BorderLayout());
				JPanel panel = new JPanel();
				panel.setLayout(new FlowLayout(FlowLayout.LEADING));
				for (int i = 0; i < buttons.length; i++)
					panel.add(buttons[i]);
				this.buttonList = Collections.unmodifiableList(Arrays.asList(buttons));
				add(BorderLayout.LINE_START, this.field = panel);
			}
			
			@Override
			public void setEnabled(boolean enabled) 
			{
				super.setEnabled(enabled);
				for (JButton button : buttonList)
					button.setEnabled(enabled);
			}
			
			@Override
			public Void getValue()
			{
				return null;
			}

			@Override
			public void setValue(Void value)
			{
				// Do nothing.
			}
			
			@Override
			protected Component getFormComponent()
			{
				return field;
			}
		};
	}
	
	/**
	 * Creates a form field that is a blank space.
	 * No value is held, nor set.
	 * @return a new form field.
	 */
	public static JFormField<Void> separatorField()
	{
		return new JFormField<Void>()
		{
			private static final long serialVersionUID = -7511474019499338246L;
			
			private JPanel field;
			
			{
				setLayout(new BorderLayout());
				add(BorderLayout.CENTER, this.field = new JPanel());
			}
			
			@Override
			public Void getValue()
			{
				return null;
			}

			@Override
			public void setValue(Void value)
			{
				// Do nothing.
			}
			
			@Override
			protected Component getFormComponent()
			{
				return field;
			}
		};
	}
	
	/* ==================================================================== */
	
	/**
	 * Creates a form panel.
	 * @param labelSide the label side.
	 * @param labelJustification the label justification.
	 * @param labelWidth the label width.
	 * @return a new form panel.
	 */
	public static JFormPanel form(JFormPanel.LabelSide labelSide, JFormPanel.LabelJustification labelJustification, int labelWidth)
	{
		return new JFormPanel(labelSide, labelJustification, labelWidth);
	}
	
	/**
	 * Creates a form panel where the label side matches the justification.
	 * @param labelSide the label side.
	 * @param labelWidth the label width.
	 * @return a new form panel.
	 */
	public static JFormPanel form(JFormPanel.LabelSide labelSide, int labelWidth)
	{
		JFormPanel.LabelJustification justification;
		switch (labelSide)
		{
			default:
				justification = null;
				break;
			case LEADING:
				justification = JFormPanel.LabelJustification.LEADING;
				break;
			case TRAILING:
				justification = JFormPanel.LabelJustification.TRAILING;
				break;
			case LEFT:
				justification = JFormPanel.LabelJustification.LEFT;
				break;
			case RIGHT:
				justification = JFormPanel.LabelJustification.RIGHT;
				break;
		}
		return form(labelSide, justification, labelWidth);
	}

	/**
	 * Creates a form panel where the label side and justification are the leading side.
	 * @param labelWidth the label width.
	 * @return a new form panel.
	 */
	public static JFormPanel form(int labelWidth)
	{
		return form(JFormPanel.LabelSide.LEADING, JFormPanel.LabelJustification.LEADING, labelWidth);
	}

	/* ==================================================================== */
	
	/**
	 * Creates a value converter component for text fields that use a string represent a value.
	 * @param <T> the final field type.
	 * @param valueFromTextFunction the function called to convert to the value from the text input.
	 * @param textFromValueFunction the function called to convert to text from the value input.
	 * @return a value converter to use with a form text field.
	 */
	public static <T> JValueConverter<T> converter(final Function<String, T> valueFromTextFunction, final Function<T, String> textFromValueFunction)
	{
		return new JValueConverter<T>() 
		{
			@Override
			public T getValueFromText(String text) 
			{
				return valueFromTextFunction.apply(text);
			}

			@Override
			public String getTextFromValue(T value) 
			{
				return textFromValueFunction.apply(value);
			}
		};
	}
	
	/* ==================================================================== */

	/**
	 * A single form.
	 */
	public static class JFormPanel extends JPanel
	{
		private static final long serialVersionUID = -3154883143018532725L;
		
		/** 
		 * Parameter for what side the label is on in the form. 
		 */
		public enum LabelSide
		{
			LEFT,
			RIGHT,
			LEADING,
			TRAILING;
		}

		/** 
		 * Parameter for the label justification.
		 */
		public enum LabelJustification
		{
			LEFT(SwingConstants.LEFT),
			CENTER(SwingConstants.CENTER),
			RIGHT(SwingConstants.RIGHT),
			LEADING(SwingConstants.LEADING),
			TRAILING(SwingConstants.TRAILING);
			
			private int alignment;
			
			private LabelJustification(int alignment)
			{
				this.alignment = alignment;
			}
			
		}

		private LabelSide labelSide;
		private LabelJustification labelJustification;
		private int labelWidth;
		private Map<Object, JFormField<?>> fieldValueMap;
		
		private JFormPanel(LabelSide labelSide, LabelJustification labelJustification, int labelWidth)
		{
			this.labelSide = labelSide;
			this.labelJustification = labelJustification;
			this.labelWidth = labelWidth;
			this.fieldValueMap = new HashMap<>();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}

		/**
		 * Adds a field to this form panel with no label.
		 * @param <V> the field value type.
		 * @param field the field to set for the form.
		 * @return this panel.
		 */
		public <V> JFormPanel addField(JFormField<V> field)
		{
			return addField(null, "", field);
		}
		
		/**
		 * Adds a field to this form panel.
		 * @param <V> the field value type.
		 * @param labelText the form label text.
		 * @param field the field to set for the form.
		 * @return this panel.
		 */
		public <V> JFormPanel addField(String labelText, JFormField<V> field)
		{
			return addField(null, labelText, field);
		}
		
		/**
		 * Adds a field to this form panel.
		 * @param <V> the field value type.
		 * @param key the the object key to fetch/set values with (if not null).
		 * @param labelText the form label text.
		 * @param field the field to set for the form.
		 * @return this panel.
		 */
		public <V> JFormPanel addField(Object key, String labelText, JFormField<V> field)
		{
			JFormFieldPanel<V> panel;
			JLabel label = new JLabel(labelText);
			label.setHorizontalAlignment(labelJustification.alignment);
			label.setVerticalAlignment(JLabel.CENTER);
			label.setPreferredSize(new Dimension(labelWidth, 0));
			
			switch (labelSide)
			{
				default:
				case LEFT:
					panel = new JFormFieldPanel<>(label, field);
					break;
				case RIGHT:
					panel = new JFormFieldPanel<>(field, label);
					break;
			}
			if (key != null)
				fieldValueMap.put(key, panel);
			add(panel);
			return this;
		}
		
		/**
		 * Gets a form value by an associated key.
		 * @param key the key to use.
		 * @return the form field value (can be null), or null if it doesn't exist.
		 */
		public Object getValue(Object key)
		{
			JFormField<?> field = fieldValueMap.get(key);
			return field == null ? null : field.getValue();
		}
		
		/**
		 * Sets a form value by an associated key.
		 * If the key does not correspond to a value, this does nothing.
		 * @param <V> the value type.
		 * @param key the key to use.
		 * @param value the value to set.
		 */
		public <V> void setValue(Object key, V value)
		{
			JFormField<?> field;
			if ((field = fieldValueMap.get(key)) != null)
			{
				Method m;
				try {
					m = field.getClass().getMethod("setValue", value.getClass());
					m.invoke(field, value);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new ClassCastException("Could not set form field: " + e.getLocalizedMessage());
				}
			}
		}
		
		/**
		 * Gets a form value by an associated key, cast to a specific type.
		 * @param <T> the return type. 
		 * @param type the class type to cast to.
		 * @param key the key to use.
		 * @return the form field value (can be null), or null if it doesn't exist.
		 */
		public <T> T getValue(Class<T> type, Object key)
		{
			return type.cast(getValue(key));
		}
		
		@Override
		public void setEnabled(boolean enabled) 
		{
			super.setEnabled(enabled);
			for (JFormField<?> field : fieldValueMap.values())
				field.setEnabled(enabled);
		}
		
	}
	
	/**
	 * Input field class used for the Black Rook Swing input components.
	 * @param <V> the type of value stored by this field.
	 */
	public static abstract class JFormField<V> extends JPanel
	{
		private static final long serialVersionUID = 1207550884473493069L;
		
		@Override
		public void setEnabled(boolean enabled)
		{
			super.setEnabled(enabled);
			getFormComponent().setEnabled(enabled);
		}
		
		@Override
		public void requestFocus()
		{
			getFormComponent().requestFocus();
		}
		
		@Override
		public boolean requestFocusInWindow() 
		{
			return getFormComponent().requestFocusInWindow();
		}
		
		/**
		 * @return the field's value. 
		 */
		public abstract V getValue();
		
		/**
		 * Sets the field's value.
		 * @param value the new value. 
		 */
		public abstract void setValue(V value);

		/**
		 * Gets the reference to this field's form component (for state stuff).
		 * @return the component. Cannot be null.
		 */
		protected abstract Component getFormComponent();

	}

	/**
	 * An encapsulated form field with a label. 
	 * @param <T> the value type stored by this panel.
	 */
	public static class JFormFieldPanel<T> extends JFormField<T>
	{
		private static final long serialVersionUID = -7165231538037948972L;
		
		private JLabel label;
		private JFormField<T> formField;
		
		private JFormFieldPanel(JLabel label, JFormField<T> field)
		{
			super();
			setBorder(createEmptyBorder(4, 4, 4, 4));
			setLayout(new BorderLayout(4, 0));
			add(this.label = label, BorderLayout.LINE_START);
			add(this.formField = field, BorderLayout.CENTER);
		}
		
		private JFormFieldPanel(JFormField<T> field, JLabel label)
		{
			super();
			setBorder(createEmptyBorder(2, 2, 2, 2));
			setLayout(new BorderLayout());
			add(this.formField = field, BorderLayout.CENTER);
			add(this.label = label, BorderLayout.LINE_END);
		}
		
		/**
		 * Sets the label text.
		 * @param text the new text.
		 */
		public void setLabel(String text)
		{
			label.setText(text);
		}
		
		/**
		 * @return the label component.
		 */
		public JLabel getLabel() 
		{
			return label;
		}
		
		@Override
		public T getValue()
		{
			return formField.getValue();
		}
	
		@Override
		public void setValue(T value) 
		{
			formField.setValue(value);
		}
		
		@Override
		protected Component getFormComponent() 
		{
			return formField;
		}
	}

	/**
	 * A field with a button for "browsing" for a value to set.
	 * @param <T> the value type.
	 */
	public static class JValueBrowseField<T> extends JValueTextField<T>
	{
		private static final long serialVersionUID = 7171922756771225976L;
		
		private JButton browseButton;
		
		/**
		 * Creates a new browse field.
		 * @param initialValue the initial value.
		 * @param browseText the browse button text.
		 * @param browseFunction the browse value function. 
		 * @param converter the converter for the text field value.
		 * @param changeListener the listener to call when a value changes.
		 */
		protected JValueBrowseField(T initialValue, String browseText, final Function<T, T> browseFunction, JValueConverter<T> converter, JValueChangeListener<T> changeListener)
		{
			super(initialValue, converter, changeListener);
			add(browseButton = new JButton(new AbstractAction(browseText)
			{
				private static final long serialVersionUID = -7785265067430010139L;

				@Override
				public void actionPerformed(ActionEvent e) 
				{
					T value;
					if ((value = browseFunction.apply(JValueBrowseField.this.getValue())) != null)
						setValue(value);
				}
				
			}), BorderLayout.LINE_END);
		}
		
		@Override
		public void setEnabled(boolean enabled)
		{
			super.setEnabled(enabled);
			browseButton.setEnabled(enabled);
		}
		
	}
	
	/**
	 * A password field that is the representation of a greater value.
	 * @param <T> the type that this field stores.
	 */
	public static class JValuePasswordField<T> extends JValueTextField<T>
	{
		private static final long serialVersionUID = -1687010711460237854L;

		protected JValuePasswordField(T initialValue, JValueConverter<T> converter, JValueChangeListener<T> changeListener) 
		{
			super(initialValue, converter, changeListener);
		}

		@Override
		protected JTextField createTextField() 
		{
			return new JPasswordField();
		}
		
	}

	/**
	 * A text field that accepts, listens for, and renders keystrokes.
	 */
	public static class JValueColorField extends JValueTextField<Color>
	{
		private static final long serialVersionUID = 751347919836098377L;
		
		/** Picker button. */
		private JButton browseButton;
		/** Preview panel. */
		private PreviewPanel previewPanel;
		
		protected JValueColorField(Color initialValue, final boolean alpha, final String browseText, final String browseTitle, JValueChangeListener<Color> changeListener)
		{
			super(initialValue, converter(
				(text) -> convertToColor(alpha, text), 
				(color) -> convertToText(alpha, color)
			), changeListener);
			
			final JValueColorField SELF = this;
			
			this.previewPanel = new PreviewPanel();
			this.previewPanel.setPreferredSize(new Dimension(20, 20));
			
			JPanel controls = new JPanel();
			controls.setLayout(new BorderLayout());
			controls.add(previewPanel, BorderLayout.LINE_START);
			controls.add(this.browseButton = new JButton(new AbstractAction(browseText)
			{
				private static final long serialVersionUID = -7921055731899664758L;

				@Override
				public void actionPerformed(ActionEvent e) 
				{
					Color value = JColorChooser.showDialog(SELF, browseTitle, SELF.getValue());
					if (value != null)
						setValue(value);
				}
				
			}), BorderLayout.LINE_END);
			add(controls, BorderLayout.LINE_END);
		}
		
		@Override
		public void setValue(Color value) 
		{
			super.setValue(value);
			if (previewPanel != null)
				previewPanel.repaint();
		}
		
		@Override
		public void setEnabled(boolean enabled)
		{
			super.setEnabled(enabled);			
			browseButton.setEnabled(enabled);
		}
		
		private static Color convertToColor(boolean alpha, String text)
		{
			try {
				long argb = Long.parseLong(text, 16);
				return new Color(
					(int)((argb & 0x000ff0000L) >> 16),
					(int)((argb & 0x00000ff00L) >> 8),
					(int)(argb & 0x0000000ffL),
					(int)((argb & 0x0ff000000L) >> 24)
				);
			} catch (NumberFormatException e) {
				return Color.BLACK;
			}
		}

		private static String convertToText(boolean alpha, Color color)
		{
	        StringBuilder sb = new StringBuilder(Integer.toHexString(color.getRGB() & 0xffffffff));
			while (sb.length() < 8) 
	            sb.insert(0, "0");
			return sb.toString();
		}
		
		private class PreviewPanel extends JComponent
		{
			private static final long serialVersionUID = -7158120389159943477L;

			@Override
			protected void paintComponent(Graphics g) 
			{
				final int width = getWidth();
				final int height = getHeight();
				Color prev = g.getColor();
				Color current = getValue();
				if (current != null)
				{
					g.setColor(getValue());
					g.fillRect(0, 0, width, height);
					g.setColor(prev);
				}
				else
				{
					g.setColor(new Color(0, 0, 0, 0));
					g.fillRect(0, 0, width, height);
					g.setColor(prev);
				}
			}
		}
		
	}
	
	/**
	 * A text field that accepts, listens for, and renders keystrokes.
	 */
	public static class JValueKeystrokeField extends JFormField<KeyStroke>
	{
		private static final long serialVersionUID = 9003830001566335088L;
		
		/** The stored value. */
		private KeyStroke value;
		/** The text field for display. */
		private JTextField textField;
		/** The change listener. */
		private JValueChangeListener<KeyStroke> changeListener;

		/**
		 * Creates a new keystroke field.
		 * @param initialValue the initial value.
		 * @param transformer the transformer function for altering an incoming keystroke before set.
		 * @param changeListener the listener to call when the value changes.
		 */
		protected JValueKeystrokeField(KeyStroke initialValue, Function<KeyStroke, KeyStroke> transformer, JValueChangeListener<KeyStroke> changeListener)
		{
			this.value = null;
			this.textField = new JTextField();
			this.textField.addKeyListener(new KeyListener() 
			{
				@Override
				public void keyTyped(KeyEvent e) 
				{
					e.consume();
				}
				
				@Override
				public void keyReleased(KeyEvent e) 
				{
					e.consume();
				}
				
				@Override
				public void keyPressed(KeyEvent e) 
				{
					e.consume();

					int code = e.getKeyCode();
					int modifiers = e.getModifiers();
					
					switch (code) // alter just meta keys
					{
						case KeyEvent.VK_CONTROL:
						case KeyEvent.VK_ALT:
						case KeyEvent.VK_META:
						case KeyEvent.VK_SHIFT:
							modifiers = 0;
					}
					
					KeyStroke input = transformer.apply(KeyStroke.getKeyStroke(code, modifiers)); 
					if ((value != null && !value.equals(input)) || (value == null && input != null))
						setValue(input);
				}
			});
			setValue(initialValue);
			this.changeListener = changeListener;

			setLayout(new BorderLayout());
			add(BorderLayout.CENTER, this.textField);
		}
		
		@Override
		public KeyStroke getValue() 
		{
			return value;
		}

		@Override
		public void setValue(KeyStroke value) 
		{
			if (value == null)
				this.textField.setText("");
			this.textField.setText(value.toString().replace("pressed ", "")); // remove "pressed" - redundant.
			this.value = value;
			if (changeListener != null)
				changeListener.onChange(value);
		}

		@Override
		protected Component getFormComponent() 
		{
			return textField;
		}
		
	}
	
	/**
	 * A text field that is the representation of a greater value.
	 * @param <T> the type that this field stores.
	 */
	public static class JValueTextField<T> extends JFormField<T>
	{
		private static final long serialVersionUID = -8674796823012708679L;
		
		/** The stored value. */
		private Object value;
		/** The stored value. */
		private JTextField textField;
		/** The converter. */
		private JValueConverter<T> converter;
		/** The change listener. */
		private JValueChangeListener<T> changeListener;
		
		/**
		 * Creates a new text field.
		 * @param initialValue the initial value.
		 * @param converter the converter for the text field value.
		 * @param changeListener the listener to call when a value changes.
		 */
		protected JValueTextField(T initialValue, JValueConverter<T> converter, JValueChangeListener<T> changeListener)
		{
			this.converter = Objects.requireNonNull(converter);
	
			this.textField = createTextField();
			this.textField.addKeyListener(new KeyAdapter() 
			{
				@Override
				public void keyPressed(KeyEvent e) 
				{
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						e.getComponent().transferFocus();
					}
					else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					{
						restoreValue();
						e.getComponent().transferFocus();
					}
				}
			});
			this.textField.addFocusListener(new FocusAdapter()
			{
				@Override
				public void focusGained(FocusEvent e) 
				{
					textField.selectAll();
				}
				
				@Override
				public void focusLost(FocusEvent e) 
				{
					refreshValue();
				}
			});
			setValue(initialValue);
			this.changeListener = changeListener;
			
			setLayout(new BorderLayout());
			add(BorderLayout.CENTER, this.textField);
		}
		
		/**
		 * @return creates the text field.
		 */
		protected JTextField createTextField()
		{
			return new JTextField();
		}
		
		/**
		 * Sets the value from text.
		 * @param text the text to set.
		 */
		public void setText(String text)
		{
			setValue(converter.getValueFromText(text));
		}
	
		@Override
		@SuppressWarnings("unchecked")
		public T getValue()
		{
			return (T)value;
		}
		
		@Override
		public void setValue(T value)
		{
			this.value = value;
			textField.setText(converter.getTextFromValue((T)value));
			if (changeListener != null)
				changeListener.onChange(value);
		}
		
		// Refreshes an entered value.
		private void refreshValue()
		{
			setValue(converter.getValueFromText(textField.getText()));
		}
		
		private void restoreValue()
		{
			setValue(getValue());
		}
		
		@Override
		protected Component getFormComponent() 
		{
			return textField;
		}
		
	}

	/**
	 * A listener that is called when a value changes on a 
	 * @param <T> the field value type.
	 */
	@FunctionalInterface
	public static interface JValueChangeListener<T>
	{
		/**
		 * Called when the value on the form changes.
		 * @param value the new value.
		 */
		void onChange(T value);
	}
	
	/**
	 * A common interface for fields that convert values to and from text.
	 * @param <T> the object type that this converts.
	 */
	public static interface JValueConverter<T>
	{
		/**
		 * Parses text for the value to set on this field.
		 * @param text the value to set.
		 * @return the resultant value.
		 */
		public abstract T getValueFromText(String text);

		/**
		 * Turns the value set on this field into text.
		 * @param value the value to set.
		 * @return the resultant value.
		 */
		public abstract String getTextFromValue(T value);
	}

}
