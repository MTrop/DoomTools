/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * A factory that creates models.
 * @author Matthew Tropiano
 */
public final class ComponentFactory
{
	private ComponentFactory() {}
	
	/* ==================================================================== */
	/* ==== Labels                                                     ==== */
	/* ==================================================================== */

	/**
	 * Creates a new label.
	 * @param icon the label icon.
	 * @param horizontalAlignment the horizontal alignment for the label.
	 * @param label the label.
	 * @return a created label.
	 */
	public static JLabel label(int horizontalAlignment, Icon icon, String label)
	{
		return new JLabel(label, icon, horizontalAlignment);
	}

	/**
	 * Creates a new label.
	 * @param icon the label icon.
	 * @param horizontalAlignment the horizontal alignment for the label.
	 * @return a created label.
	 */
	public static JLabel label(int horizontalAlignment, Icon icon)
	{
		return new JLabel(icon, horizontalAlignment);
	}

	/**
	 * Creates a new label.
	 * @param horizontalAlignment the horizontal alignment for the label.
	 * @param label the label.
	 * @return a created label.
	 */
	public static JLabel label(int horizontalAlignment, String label)
	{
		return new JLabel(label, horizontalAlignment);
	}

	/**
	 * Creates a new label.
	 * @param icon the label icon.
	 * @return a created label.
	 */
	public static JLabel label(Icon icon)
	{
		return new JLabel(icon);
	}

	/**
	 * Creates a new label.
	 * @param label the label.
	 * @return a created label.
	 */
	public static JLabel label(String label)
	{
		return new JLabel(label);
	}

	/* ==================================================================== */
	/* ==== Buttons                                                    ==== */
	/* ==================================================================== */

	/**
	 * Creates a button.
	 * @param icon the check box icon.
	 * @param label the check box label.
	 * @param mnemonic the button mnemonic.
	 * @param handler the check box label.
	 * @return a new button.
	 */
	public static JButton button(Icon icon, String label, int mnemonic, ComponentActionHandler<JButton> handler)
	{
		JButton out = new JButton(action(icon, label, handler));
		if (mnemonic > 0)
			out.setMnemonic(mnemonic);
		return out;
	}

	/**
	 * Creates a button.
	 * @param icon the check box icon.
	 * @param label the check box label.
	 * @param handler the check box label.
	 * @return a new button.
	 */
	public static JButton button(Icon icon, String label, ComponentActionHandler<JButton> handler)
	{
		return button(icon, label, 0, handler);
	}
	
	/**
	 * Creates a button.
	 * @param icon the check box icon.
	 * @param mnemonic the button mnemonic.
	 * @param handler the check box label.
	 * @return a new button.
	 */
	public static JButton button(Icon icon, int mnemonic, ComponentActionHandler<JButton> handler)
	{
		return button(icon, null, 0, handler);
	}

	/**
	 * Creates a button.
	 * @param icon the check box icon.
	 * @param handler the check box label.
	 * @return a new button.
	 */
	public static JButton button(Icon icon, ComponentActionHandler<JButton> handler)
	{
		return button(icon, 0, handler);
	}
	
	/**
	 * Creates a button.
	 * @param label the check box label.
	 * @param mnemonic the button mnemonic.
	 * @param handler the check box label.
	 * @return a new button.
	 */
	public static JButton button(String label, int mnemonic, ComponentActionHandler<JButton> handler)
	{
		return button(null, label, 0, handler);
	}

	/**
	 * Creates a button.
	 * @param label the check box label.
	 * @param handler the check box label.
	 * @return a new button.
	 */
	public static JButton button(String label, ComponentActionHandler<JButton> handler)
	{
		return button(label, 0, handler);
	}
	
	/**
	 * Creates a button.
	 * @param mnemonic the button mnemonic.
	 * @param action the action on the button.
	 * @return a new button.
	 */
	public static JButton button(int mnemonic, Action action)
	{
		JButton out = new JButton(action);
		if (mnemonic > 0)
			out.setMnemonic(mnemonic);
		return out;
	}

	/**
	 * Creates a button.
	 * @param action the action on the button.
	 * @return a new button.
	 */
	public static JButton button(Action action)
	{
		return button(0, action);
	}

	/* ==================================================================== */
	/* ==== Checkboxes                                                 ==== */
	/* ==================================================================== */

	/**
	 * Creates a check box.
	 * @param icon the check box icon.
	 * @param label the check box label.
	 * @param selected the selected state.
	 * @param handler the check box label.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(Icon icon, String label, boolean selected, ComponentActionHandler<JCheckBox> handler)
	{
		JCheckBox out = new JCheckBox(action(icon, label, handler));
		out.setSelected(selected);
		return out;
	}

	/**
	 * Creates a check box.
	 * @param label the check box label.
	 * @param selected the selected state.
	 * @param handler the check box label.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(String label, boolean selected, ComponentActionHandler<JCheckBox> handler)
	{
		JCheckBox out = new JCheckBox(action(label, handler));
		out.setSelected(selected);
		return out;
	}

	/**
	 * Creates a check box.
	 * @param icon the check box icon.
	 * @param selected the selected state.
	 * @param handler the check box label.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(Icon icon, boolean selected, ComponentActionHandler<JCheckBox> handler)
	{
		JCheckBox out = new JCheckBox(action(icon, handler));
		out.setSelected(selected);
		return out;
	}

	/**
	 * Creates a checkbox.
	 * @param selected the selected state.
	 * @param action the action for the checkbox.
	 * @return a new checkbox.
	 */
	public static JCheckBox checkBox(boolean selected, Action action)
	{
		JCheckBox out = new JCheckBox(action);
		out.setSelected(selected);
		return out;
	}

	/**
	 * Creates a check box.
	 * @param selected the selected state.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(boolean selected)
	{
		JCheckBox out = new JCheckBox();
		out.setSelected(selected);
		return out;
	}

	/* ==================================================================== */
	/* ==== Sliders                                                    ==== */
	/* ==================================================================== */

	/**
	 * Creates a new range model for a slider.
	 * @param value the current value.
	 * @param extent the length of the inner range that begins at the model's value.
	 * @param min the minimum value.
	 * @param max the maximum value.
	 * @return a new range model.
	 */
	public static BoundedRangeModel sliderModel(int value, int extent, int min, int max)
	{
		return new DefaultBoundedRangeModel(value, extent, min, max);
	}

	/**
	 * Creates a value slider.
	 * @param orientation the orientation type.
	 * @param rangeModel the range model for the slider.
	 * @param handler the change handler.
	 * @return a new slider.
	 */
	public static JSlider slider(int orientation, BoundedRangeModel rangeModel, ComponentChangeHandler<JSlider> handler)
	{
		JSlider out = new JSlider(rangeModel);
		out.setOrientation(orientation);
		if (handler != null)
			out.addChangeListener(handler);
		return out;
	}
	
	/**
	 * Creates a horizontal value slider.
	 * @param rangeModel the range model for the slider.
	 * @param handler the change handler.
	 * @return a new slider.
	 */
	public static JSlider slider(BoundedRangeModel rangeModel, ComponentChangeHandler<JSlider> handler)
	{
		return slider(JSlider.HORIZONTAL, rangeModel, handler);
	}
	
	/**
	 * Creates a value slider.
	 * @param orientation the orientation type.
	 * @param rangeModel the range model for the slider.
	 * @return a new slider.
	 */
	public static JSlider slider(int orientation, BoundedRangeModel rangeModel)
	{
		return slider(orientation, rangeModel, null);
	}
	
	/**
	 * Creates a horizontal value slider.
	 * @param rangeModel the range model for the slider.
	 * @return a new slider.
	 */
	public static JSlider slider(BoundedRangeModel rangeModel)
	{
		return slider(rangeModel, null);
	}
	
	/* ==================================================================== */
	/* ==== Documents                                                  ==== */
	/* ==================================================================== */

	/**
	 * Creates a single document handler that uses different functions for each change type.
	 * @param <T> the text component type.
	 * @param insertHandler the handler function for text insertion.
	 * @param removeHandler the handler function for text removal.
	 * @param changeHandler the handler function for changes.
	 * @return a new document handler.
	 */
	public static <T extends JTextComponent> DocumentHandler<T> documentHandler(
			DocumentHandlerFunction<T> insertHandler,
			DocumentHandlerFunction<T> removeHandler,
			DocumentHandlerFunction<T> changeHandler
	){
		return new DocumentHandler<T>(insertHandler, removeHandler, changeHandler);
	}
	
	/**
	 * Creates a single document handler that uses one function for all changes.
	 * @param <T> the text component type.
	 * @param changeHandler the handler function.
	 * @return a new document handler.
	 */
	public static <T extends JTextComponent> DocumentHandler<T> textHandler(TextHandlerFunction<T> changeHandler)
	{
		return new DocumentHandler<T>(changeHandler);
	}

	/* ==================================================================== */
	/* ==== Text Area                                                  ==== */
	/* ==================================================================== */

	/**
	 * Creates a new TextArea.
	 * @param document the backing document model.
	 * @param text the default starting text contained.
	 * @param rows the amount of rows.
	 * @param columns the amount of columns.
	 * @param handler the listener for all document changes.
	 * @return a new text area.
	 */
	public static JTextArea textArea(Document document, String text, int rows, int columns, DocumentHandler<JTextArea> handler)
	{
		JTextArea out = new JTextArea(document, text, rows, columns);
		out.getDocument().addDocumentListener(handler);
		return out;
	}

	/**
	 * Creates a new TextArea.
	 * @param document the backing document model.
	 * @param text the default starting text contained.
	 * @param rows the amount of rows.
	 * @param columns the amount of columns.
	 * @return a new text area.
	 */
	public static JTextArea textArea(Document document, String text, int rows, int columns)
	{
		JTextArea out = new JTextArea(document, text, rows, columns);
		return out;
	}

	/**
	 * Creates a new TextArea.
	 * @param text the default starting text contained.
	 * @param rows the amount of rows.
	 * @param columns the amount of columns.
	 * @param handler the listener for all document changes.
	 * @return a new text area.
	 */
	public static JTextArea textArea(String text, int rows, int columns, DocumentHandler<JTextArea> handler)
	{
		JTextArea out = new JTextArea(text, rows, columns);
		out.getDocument().addDocumentListener(handler);
		return out;
	}

	/**
	 * Creates a new TextArea.
	 * @param text the default starting text contained.
	 * @param rows the amount of rows.
	 * @param columns the amount of columns.
	 * @return a new text area.
	 */
	public static JTextArea textArea(String text, int rows, int columns)
	{
		JTextArea out = new JTextArea(text, rows, columns);
		return out;
	}

	/**
	 * Creates a new TextArea.
	 * @param rows the amount of rows.
	 * @param columns the amount of columns.
	 * @param handler the listener for all document changes.
	 * @return a new text area.
	 */
	public static JTextArea textArea(int rows, int columns, DocumentHandler<JTextArea> handler)
	{
		JTextArea out = new JTextArea(rows, columns);
		out.getDocument().addDocumentListener(handler);
		return out;
	}

	/**
	 * Creates a new TextArea.
	 * @param rows the amount of rows.
	 * @param columns the amount of columns.
	 * @return a new text area.
	 */
	public static JTextArea textArea(int rows, int columns)
	{
		return new JTextArea(rows, columns);
	}

	/**
	 * Creates a new TextField.
	 * @param document the backing document model.
	 * @param text the default starting text contained.
	 * @param columns the amount of columns.
	 * @param handler the listener for all document changes.
	 * @return a new text field.
	 */
	public static JTextField textField(Document document, String text, int columns, DocumentHandler<JTextArea> handler)
	{
		JTextField out = new JTextField(document, text, columns);
		out.getDocument().addDocumentListener(handler);
		return out;
	}

	/**
	 * Creates a new TextField.
	 * @param document the backing document model.
	 * @param text the default starting text contained.
	 * @param columns the amount of columns.
	 * @return a new text field.
	 */
	public static JTextField textField(Document document, String text, int columns)
	{
		JTextField out = new JTextField(document, text, columns);
		return out;
	}

	/**
	 * Creates a new TextField.
	 * @param text the default starting text contained.
	 * @param columns the amount of columns.
	 * @param handler the listener for all document changes.
	 * @return a new text field.
	 */
	public static JTextField textField(String text, int columns, DocumentHandler<JTextArea> handler)
	{
		JTextField out = new JTextField(text, columns);
		out.getDocument().addDocumentListener(handler);
		return out;
	}

	/**
	 * Creates a new TextField.
	 * @param text the default starting text contained.
	 * @param handler the listener for all document changes.
	 * @return a new text field.
	 */
	public static JTextField textField(String text, DocumentHandler<JTextArea> handler)
	{
		JTextField out = new JTextField(text);
		out.getDocument().addDocumentListener(handler);
		return out;
	}

	/**
	 * Creates a new TextField.
	 * @param columns the amount of columns.
	 * @param handler the listener for all document changes.
	 * @return a new text field.
	 */
	public static JTextField textField(int columns, DocumentHandler<JTextArea> handler)
	{
		JTextField out = new JTextField(columns);
		out.getDocument().addDocumentListener(handler);
		return out;
	}

	/**
	 * Creates a new TextField.
	 * @param handler the listener for all document changes.
	 * @return a new text field.
	 */
	public static JTextField textField(DocumentHandler<JTextArea> handler)
	{
		JTextField out = new JTextField();
		out.getDocument().addDocumentListener(handler);
		return out;
	}

	/**
	 * Creates a new TextField.
	 * @param text the default starting text contained.
	 * @param columns the amount of columns.
	 * @return a new text field.
	 */
	public static JTextField textField(String text, int columns)
	{
		return new JTextField(text, columns);
	}

	/**
	 * Creates a new TextField.
	 * @param text the default starting text contained.
	 * @return a new text field.
	 */
	public static JTextField textField(String text)
	{
		JTextField out = new JTextField(text);
		return out;
	}

	/**
	 * Creates a new TextField.
	 * @param columns the amount of columns.
	 * @return a new text field.
	 */
	public static JTextField textField(int columns)
	{
		JTextField out = new JTextField(columns);
		return out;
	}

	/**
	 * Creates a new TextField.
	 * @return a new text field.
	 */
	public static JTextField textField()
	{
		return new JTextField();
	}

	/* ==================================================================== */
	/* ==== Spinners                                                   ==== */
	/* ==================================================================== */

	/**
	 * Creates a spinner model for numbers.
	 * @param value the current value.
	 * @param minimum the minimum value.
	 * @param maximum the maximum value.
	 * @param stepSize the step between values.
	 * @return a new spinner model.
	 */
	public static SpinnerNumberModel spinnerModel(int value, int minimum, int maximum, int stepSize)
	{
		return new SpinnerNumberModel(value, minimum, maximum, stepSize);
	}

	/**
	 * Creates a spinner model for numbers.
	 * @param value the current value.
	 * @param minimum the minimum value.
	 * @param maximum the maximum value.
	 * @param stepSize the step between values.
	 * @return a new spinner model.
	 */
	public static SpinnerNumberModel spinnerModel(double value, double minimum, double maximum, double stepSize)
	{
		return new SpinnerNumberModel(value, minimum, maximum, stepSize);
	}

	/**
	 * Creates a spinner model for numbers.
	 * @param value the current value.
	 * @param minimum the minimum value.
	 * @param maximum the maximum value.
	 * @param stepSize the step between values.
	 * @return a new spinner model.
	 */
	public static SpinnerNumberModel spinnerModel(Number value, Comparable<?> minimum, Comparable<?> maximum, Number stepSize)
	{
		return new SpinnerNumberModel(value, minimum, maximum, stepSize);
	}

	/**
	 * Creates a spinner model for objects.
	 * @param selectedIndex the selected index.
	 * @param list the list of values.
	 * @return a new spinner model.
	 */
	public static SpinnerListModel spinnerModel(int selectedIndex, List<?> list)
	{
		SpinnerListModel out = new SpinnerListModel(list);
		out.setValue(list.get(selectedIndex));
		return out;
	}

	/**
	 * Creates a spinner model for objects.
	 * @param selectedIndex the selected index.
	 * @param objects the list of values.
	 * @return a new spinner model.
	 */
	public static SpinnerListModel spinnerModel(int selectedIndex, Object ... objects)
	{
		SpinnerListModel out = new SpinnerListModel(objects);
		out.setValue(objects[selectedIndex]);
		return out;
	}

	/**
	 * Creates a spinner model for dates.
	 * @param value the current value.
	 * @param start the starting date value.
	 * @param end the ending date value.
	 * @param calendarField the stepping between values for each date.
	 * @return a new spinner model.
	 */
	public static SpinnerDateModel spinnerModel(Date value, Comparable<Date> start, Comparable<Date> end, int calendarField)
	{
		return new SpinnerDateModel(value, start, end, calendarField);
	}

	/**
	 * Creates a value spinner with an attached change listener.
	 * @param model the spinner model.
	 * @param handler the change handler.
	 * @return the resultant spinner.
	 */
	public static JSpinner spinner(SpinnerModel model, ComponentChangeHandler<JSpinner> handler)
	{
		JSpinner out = new JSpinner(model);
		out.addChangeListener(handler);
		return out;
	}

	/* ==================================================================== */
	/* ==== Comboboxes                                                 ==== */
	/* ==================================================================== */

	/**
	 * Creates a combo box model.
	 * @param <E> the object type that the model contains.
	 * @param objects the objects to put in the list model.
	 * @param listener the listener to attach to the model (after items are added).
	 * @return the list component.
	 */
	public static <E> ComboBoxModel<E> comboBoxModel(Collection<E> objects, ListDataListener listener)
	{
		DefaultComboBoxModel<E> out = new DefaultComboBoxModel<E>();
		for (E e : objects)
			out.addElement(e);
		out.addListDataListener(listener);
		return out;
	}

	/**
	 * Creates a combo box model.
	 * @param <E> the object type that the model contains.
	 * @param objects the objects to put in the list model.
	 * @return the list component.
	 */
	public static <E> ComboBoxModel<E> comboBoxModel(Collection<E> objects)
	{
		DefaultComboBoxModel<E> out = new DefaultComboBoxModel<E>();
		for (E e : objects)
			out.addElement(e);
		return out;
	}

	/**
	 * Creates a combo box (dropdown) with an attached listener.
	 * @param <E> the item type.
	 * @param model the spinner model.
	 * @param listener the change listener.
	 * @return the resultant spinner.
	 */
	public static <E> JComboBox<E> comboBox(ComboBoxModel<E> model, ItemListener listener)
	{
		JComboBox<E> out = new JComboBox<E>(model);
		out.addItemListener(listener);
		return out;
	}

	/**
	 * Creates a combo box (dropdown).
	 * @param <E> the item type.
	 * @param model the spinner model.
	 * @return the resultant spinner.
	 */
	public static <E> JComboBox<E> comboBox(ComboBoxModel<E> model)
	{
		return new JComboBox<E>(model);
	}

	/* ==================================================================== */
	/* ==== Lists                                                      ==== */
	/* ==================================================================== */

	/**
	 * Creates a list model.
	 * @param <M> the list model type.
	 * @param <D> the data type contained in the model. 
	 * @param addedHandler the handler function called for when data is added to the model.
	 * @param removedHandler  the handler function called for when data is removed from the model.
	 * @param changedHandler  the handler function called for when data is changed in the model.
	 * @return the list data handler.
	 */
	public static <M extends ListModel<D>, D> ListDataHandler<M, D> listDataHandler(
			final ListDataHandlerFunction<M, D> addedHandler,
			final ListDataHandlerFunction<M, D> removedHandler,
			final ListDataHandlerFunction<M, D> changedHandler
	){
		return new ListDataHandler<M, D>(addedHandler, removedHandler, changedHandler);
	}

	/**
	 * Creates a list model.
	 * @param <M> the list model type.
	 * @param <E> the object type that the model contains.
	 * @param objects the objects to put in the list model.
	 * @param handler the data handler to attach to the model (after the items are added, so that events are not fired to it).
	 * @return the list component.
	 */
	public static <M extends ListModel<E>, E> ListModel<E> listModel(Collection<E> objects, ListDataHandler<M, E> handler)
	{
		DefaultListModel<E> out = new DefaultListModel<E>();
		for (E e : objects)
			out.addElement(e);
		out.addListDataListener(handler);
		return out;
	}

	/**
	 * Creates a list model.
	 * @param <E> the object type that the model contains.
	 * @param objects the objects to put in the list model.
	 * @return the list component.
	 */
	public static <E> ListModel<E> listModel(Collection<E> objects)
	{
		DefaultListModel<E> out = new DefaultListModel<E>();
		for (E e : objects)
			out.addElement(e);
		return out;
	}

	/**
	 * Creates a list with a specific list model.
	 * @param <E> the object type that the model contains.
	 * @param model the list model.
	 * @param renderer the cell renderer.
	 * @param selectionMode the list selection mode (from ListSelectionModel).
	 * @param handler the listener to use for selection changes.
	 * @return the list component.
	 */
	public static <E> JList<E> list(ListModel<E> model, ListCellRenderer<E> renderer, int selectionMode, ListSelectionHandler<JList<E>> handler)
	{
		JList<E> out = new JList<>(model);
		out.setCellRenderer(renderer);
		out.setSelectionMode(selectionMode);
		out.getSelectionModel().addListSelectionListener(handler);
		return out;
	}

	/**
	 * Creates a list with a specific list model.
	 * @param <E> the object type that the model contains.
	 * @param model the list model.
	 * @param renderer the cell renderer.
	 * @param selectionModel the list selection model.
	 * @return the list component.
	 */
	public static <E> JList<E> list(ListModel<E> model, ListCellRenderer<E> renderer, ListSelectionModel selectionModel)
	{
		JList<E> out = new JList<>(model);
		out.setCellRenderer(renderer);
		out.setSelectionModel(selectionModel);
		return out;
	}
	
	/**
	 * Creates a list with a specific list model.
	 * @param <E> the object type that the model contains.
	 * @param model the list model.
	 * @param selectionMode the list selection mode (from ListSelectionModel).
	 * @param handler the listener to use for selection changes.
	 * @return the list component.
	 */
	public static <E> JList<E> list(ListModel<E> model, int selectionMode, ListSelectionHandler<JList<E>> handler)
	{
		JList<E> out = new JList<>(model);
		out.setSelectionMode(selectionMode);
		out.getSelectionModel().addListSelectionListener(handler);
		return out;
	}

	/**
	 * Creates a list with a specific list model.
	 * @param <E> the object type that the model contains.
	 * @param model the list model.
	 * @param selectionModel the list selection model.
	 * @return the list component.
	 */
	public static <E> JList<E> list(ListModel<E> model, ListSelectionModel selectionModel)
	{
		JList<E> out = new JList<>(model);
		out.setSelectionModel(selectionModel);
		return out;
	}
	
	/**
	 * Creates a list with a specific list model and single selection mode.
	 * @param <E> the object type that the model contains.
	 * @param model the list model.
	 * @return the list component.
	 */
	public static <E> JList<E> list(ListModel<E> model)
	{
		JList<E> out = new JList<>(model);
		out.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return out;
	}

	/* ==================================================================== */
	/* ==== Tables                                                     ==== */
	/* ==================================================================== */

	/**
	 * Creates a new table.
	 * @param model the table model.
	 * @param columnModel the column model.
	 * @param selectionMode the list selection mode (from ListSelectionModel).
	 * @param handler the listener to use for selection changes.
	 * @return the table created.
	 */
	public static JTable table(TableModel model, TableColumnModel columnModel, int selectionMode, ListSelectionHandler<JTable> handler)
	{
		JTable out = new JTable(model, columnModel);
		out.setSelectionMode(selectionMode);
		out.getSelectionModel().addListSelectionListener(handler);
		return out;
	}

	/**
	 * Creates a new table.
	 * @param model the table model.
	 * @param columnModel the column model.
	 * @param selectionModel the selection model.
	 * @return the table created.
	 */
	public static JTable table(TableModel model, TableColumnModel columnModel, ListSelectionModel selectionModel)
	{
		return new JTable(model, columnModel, selectionModel);
	}
	
	/**
	 * Creates a new table.
	 * @param model the table model.
	 * @param selectionMode the list selection mode (from ListSelectionModel).
	 * @param handler the listener to use for selection changes.
	 * @return the table created.
	 */
	public static JTable table(TableModel model, int selectionMode, ListSelectionHandler<JTable> handler)
	{
		JTable out = new JTable(model, new DefaultTableColumnModel());
		out.setSelectionMode(selectionMode);
		out.getSelectionModel().addListSelectionListener(handler);
		return out;
	}

	/**
	 * Creates a new table.
	 * @param model the table model.
	 * @param selectionModel the selection model.
	 * @return the table created.
	 */
	public static JTable table(TableModel model, ListSelectionModel selectionModel)
	{
		return new JTable(model, new DefaultTableColumnModel(), selectionModel);
	}

	/**
	 * Creates a new table.
	 * @param model the table model.
	 * @param columnModel the column model.
	 * @return the table created.
	 */
	public static JTable table(TableModel model, TableColumnModel columnModel)
	{
		return new JTable(model, columnModel);
	}
	
	/* ==================================================================== */
	/* ==== Handlers                                                   ==== */
	/* ==================================================================== */

	/**
	 * A handler interface for listening for list selection events.
	 * @param <L> the listener type that this handles.
	 */
	@FunctionalInterface
	public interface ListSelectionHandler<L> extends ListSelectionListener
	{
		@Override
		@SuppressWarnings("unchecked")
		default void valueChanged(ListSelectionEvent e) 
		{
			onSelectionChange((L)e.getSource(), e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
		}
		
		/**
		 * Called when a list's selection changes.
		 * @param component the associated component.
		 * @param firstIndex the first index of the selection.
		 * @param lastIndex the last index of the selection.
		 * @param adjusting if true, this is in the middle of adjusting, false if not.
		 */
		void onSelectionChange(L component, int firstIndex, int lastIndex, boolean adjusting);
	}
	
	/**
	 * A handler interface for listening for list selection events.
	 * @param <M> the model type.
	 * @param <D> the data type. 
	 */
	@FunctionalInterface
	public interface ListDataHandlerFunction<M extends ListModel<D>, D>
	{
		/**
		 * Handles list's data model change.
		 * @param model the source model.
		 * @param component the associated component.
		 * @param firstIndex the starting index.
		 * @param lastIndex the ending index.
		 * @param data the data itself.
		 */
		@SuppressWarnings("unchecked")
		void onDataEvent(M model, int firstIndex, int lastIndex, D ... data);
	}
	
	/**
	 * An encapsulation for defining how to handle data changes to a list model.
	 * @param <M> the model type.
	 * @param <D> the data type. 
	 */
	public static class ListDataHandler<M extends ListModel<D>, D> implements ListDataListener
	{
		private ListDataHandlerFunction<M, D> addedHandler;
		private ListDataHandlerFunction<M, D> removedHandler;
		private ListDataHandlerFunction<M, D> changedHandler;
		
		private ListDataHandler(
				ListDataHandlerFunction<M, D> addedHandler,
				ListDataHandlerFunction<M, D> removedHandler,
				ListDataHandlerFunction<M, D> changedHandler
		){
			this.addedHandler = addedHandler;
			this.removedHandler = removedHandler;
			this.changedHandler = changedHandler;
		}
	
		@SuppressWarnings("unchecked")
		private static <M extends ListModel<D>, D> void callHandler(ListDataEvent e, ListDataHandlerFunction<M, D> func)
		{
			M model = (M)e.getSource();
			D obj = model.getElementAt(e.getIndex0());
			D[] data = (D[])Array.newInstance(obj.getClass(), e.getIndex1() - e.getIndex0() + 1);
			for (int i = 0; i < data.length; i++)
				data[i] = model.getElementAt(e.getIndex0() + i);
			func.onDataEvent((M)e.getSource(), e.getIndex0(), e.getIndex1(), data);
		}
		
		@Override
		public void intervalAdded(ListDataEvent e)
		{
			callHandler(e, addedHandler);
		}
	
		@Override
		public void intervalRemoved(ListDataEvent e)
		{
			callHandler(e, removedHandler);
		}
	
		@Override
		public void contentsChanged(ListDataEvent e)
		{
			callHandler(e, changedHandler);
		}
	}

	/**
	 * An encapsulation for defining how to handle changes to a document model.
	 * @param <T> the text component type. 
	 */
	public static class DocumentHandler<T extends JTextComponent> implements DocumentListener
	{
		private DocumentHandlerFunction<T> insertHandler;
		private DocumentHandlerFunction<T> removeHandler;
		private DocumentHandlerFunction<T> changeHandler;
		
		private DocumentHandler(
				DocumentHandlerFunction<T> insertHandler,
				DocumentHandlerFunction<T> removeHandler,
				DocumentHandlerFunction<T> changeHandler
		){
			this.insertHandler = insertHandler;
			this.removeHandler = removeHandler;
			this.changeHandler = changeHandler;
		}

		private DocumentHandler(DocumentHandlerFunction<T> handler)
		{
			this.insertHandler = handler;
			this.removeHandler = handler;
			this.changeHandler = handler;
		}

		private static <T extends JTextComponent> void callHandler(DocumentEvent e, DocumentHandlerFunction<T> func)
		{
			func.onDocumentChange(e.getDocument(), e.getOffset(), e.getLength(), e);
		}
		
		@Override
		public void insertUpdate(DocumentEvent e)
		{
			callHandler(e, insertHandler);
		}

		@Override
		public void removeUpdate(DocumentEvent e)
		{
			callHandler(e, removeHandler);
		}

		@Override
		public void changedUpdate(DocumentEvent e)
		{
			callHandler(e, changeHandler);
		}
	}
	
	/**
	 * A handler interface for listening for document events.
	 * @param <T> the text component type. 
	 */
	@FunctionalInterface
	public interface DocumentHandlerFunction<T extends JTextComponent>
	{
		/**
		 * Called on document change.
		 * @param document the document affected.
		 * @param offset the offset into the document for the start of the change.
		 * @param length the length of the change in characters.
		 * @param event the document event.
		 */
		void onDocumentChange(Document document, int offset, int length, DocumentEvent event);
	}
	
	/**
	 * A handler interface for listening for document events.
	 * @param <T> the text component type. 
	 */
	@FunctionalInterface
	public interface TextHandlerFunction<T extends JTextComponent> extends DocumentHandlerFunction<T>
	{
		@Override
		default void onDocumentChange(Document document, int offset, int length, DocumentEvent event)
		{
			try {
				onTextChange(document.getText(0, document.getLength()));
			} catch (BadLocationException e) {
				// Should not happen.
			}
		}

		/**
		 * Called when the document changes.
		 * @param text the text after the change.
		 */
		void onTextChange(String text);
	}
	
	/**
	 * A handler interface for listening for change events.
	 * @param <C> the component type that this handles.
	 */
	@FunctionalInterface
	public interface ComponentChangeHandler<C> extends ChangeListener
	{
		@Override
		@SuppressWarnings("unchecked")
		default void stateChanged(ChangeEvent event)
		{
			onChangeEvent((C)event.getSource());
		}
		
		/**
		 * Called when a component emits a change event.
		 * @param component the associated component.
		 */
		void onChangeEvent(C component);
	}
	
	/* ==================================================================== */
	/* ==== Menus                                                      ==== */
	/* ==================================================================== */

	/**
	 * Creates a new JMenuBar.
	 * @param menus the menus to add.
	 * @return a new JMenuBar.
	 */
	public static JMenuBar menuBar(JMenu ... menus)
	{
		JMenuBar out = new JMenuBar();
		for (JMenu m : menus)
			out.add(m);
		return out;
	}
	
	/**
	 * Creates a new Pop-up Menu.
	 * @param name the menu heading.
	 * @param nodes the menu nodes to add.
	 * @return a new JPopupMenu.
	 */
	public static JPopupMenu popupMenu(String name, MenuNode ... nodes)
	{
		JPopupMenu out = new JPopupMenu(name);
		for (MenuNode mn : nodes)
			mn.addTo(out);
		return out;
	}
	
	/**
	 * Creates a new Pop-up Menu tree.
	 * @param nodes the menu nodes to add.
	 * @return a new JPopupMenu.
	 */
	public static JPopupMenu popupMenu(MenuNode ... nodes)
	{
		return popupMenu(null, nodes);
	}
	
	/**
	 * Creates a new menu tree.
	 * @param icon the icon for the menu entry.
	 * @param name the name of the menu.
	 * @param mnemonic the key mnemonic for accessing (VK).
	 * @param nodes the menu nodes to add.
	 * @return a new JMenu.
	 */
	public static JMenu menu(Icon icon, String name, int mnemonic, MenuNode ... nodes)
	{
		JMenu out = new JMenu(name);
		out.setMnemonic(mnemonic);
		for (MenuNode mn : nodes)
			mn.addTo(out);
		return out;
	}
	
	/**
	 * Creates a new menu tree.
	 * @param name the name of the menu.
	 * @param mnemonic the key mnemonic for accessing (VK).
	 * @param nodes the menu nodes to add.
	 * @return a new JMenu.
	 */
	public static JMenu menu(String name, int mnemonic, MenuNode ... nodes)
	{
		JMenu out = new JMenu(name);
		out.setMnemonic(mnemonic);
		for (MenuNode mn : nodes)
			mn.addTo(out);
		return out;
	}
	
	/**
	 * Creates a new menu tree.
	 * @param mnemonic the key mnemonic for accessing (VK).
	 * @param action the action for the menu.
	 * @return a new JMenu.
	 */
	public static JMenu menu(int mnemonic, Action action)
	{
		JMenu out = new JMenu(action);
		out.setMnemonic(mnemonic);
		return out;
	}
	
	/**
	 * Creates a new menu tree.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for accessing (VK).
	 * @param handler the code called when the action is triggered.
	 * @return a new JMenu.
	 */
	public static JMenu menu(Icon icon, String label, int mnemonic, ComponentActionHandler<JMenu> handler)
	{
		JMenu out = new JMenu(action(icon, label, handler));
		out.setMnemonic(mnemonic);
		return out;
	}
	
	/**
	 * Creates a menu item node.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode item(int mnemonic, KeyStroke accelerator, Action action)
	{
		return new MenuItemNode(action, mnemonic, accelerator);
	}
	
	/**
	 * Creates a menu item node.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode item(KeyStroke accelerator, Action action)
	{
		return new MenuItemNode(action, 0, accelerator);
	}
	
	/**
	 * Creates a menu item node.
	 * @param mnemonic the key mnemonic for the item.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode item(int mnemonic, Action action)
	{
		return new MenuItemNode(action, mnemonic, null);
	}
	
	/**
	 * Creates a menu item node.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode item(Action action)
	{
		return new MenuItemNode(action, 0, null);
	}
	
	/**
	 * Creates a menu item node.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode item(Icon icon, String label, int mnemonic, KeyStroke accelerator, ComponentActionHandler<JMenuItem> handler)
	{
		return new MenuItemNode(action(icon, label, handler), mnemonic, accelerator);
	}
	
	/**
	 * Creates a menu item node.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode item(String label, int mnemonic, KeyStroke accelerator, ComponentActionHandler<JMenuItem> handler)
	{
		return new MenuItemNode(action(label, handler), mnemonic, accelerator);
	}
	
	/**
	 * Creates a menu item node.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode item(Icon icon, String label, int mnemonic, ComponentActionHandler<JMenuItem> handler)
	{
		return new MenuItemNode(action(icon, label, handler), mnemonic, null);
	}
	
	/**
	 * Creates a menu item node.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode item(String label, int mnemonic, ComponentActionHandler<JMenuItem> handler)
	{
		return new MenuItemNode(action(label, handler), mnemonic, null);
	}
	
	/**
	 * Creates a menu item node.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode item(Icon icon, String label, KeyStroke accelerator, ComponentActionHandler<JMenuItem> handler)
	{
		return new MenuItemNode(action(icon, label, handler), 0, accelerator);
	}
	
	/**
	 * Creates a menu item node.
	 * @param label the label for the menu entry.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode item(String label, KeyStroke accelerator, ComponentActionHandler<JMenuItem> handler)
	{
		return new MenuItemNode(action(label, handler), 0, accelerator);
	}
	
	/**
	 * Creates a menu item node.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode item(Icon icon, String label, ComponentActionHandler<JMenuItem> handler)
	{
		return new MenuItemNode(action(icon, label, handler), 0, null);
	}
	
	/**
	 * Creates a menu item node.
	 * @param label the label for the menu entry.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode item(String label, ComponentActionHandler<JMenuItem> handler)
	{
		return new MenuItemNode(action(label, handler), 0, null);
	}
	
	/**
	 * Creates a menu item submenu.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for the item.
	 * @param nodes the menu nodes to add.
	 * @return a menu node.
	 */
	public static MenuNode item(Icon icon, String label, int mnemonic, MenuNode ... nodes)
	{
		return new MenuBranchNode(icon, label, mnemonic, nodes);
	}
	
	/**
	 * Creates a menu item submenu.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for the item.
	 * @param nodes the menu nodes to add.
	 * @return a menu node.
	 */
	public static MenuNode item(String label, int mnemonic, MenuNode ... nodes)
	{
		return new MenuBranchNode(null, label, mnemonic, nodes);
	}
	
	/**
	 * Creates a menu item submenu.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param nodes the menu nodes to add.
	 * @return a menu node.
	 */
	public static MenuNode item(Icon icon, String label, MenuNode ... nodes)
	{
		return new MenuBranchNode(icon, label, 0, nodes);
	}
	
	/**
	 * Creates a menu item submenu.
	 * @param label the label for the menu entry.
	 * @param nodes the menu nodes to add.
	 * @return a menu node.
	 */
	public static MenuNode item(String label, MenuNode ... nodes)
	{
		return new MenuBranchNode(null, label, 0, nodes);
	}

	/**
	 * Creates a check box menu item node.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param selected the state of the checkbox.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(int mnemonic, KeyStroke accelerator, boolean selected, Action action)
	{
		return new MenuCheckBoxNode(action, selected, mnemonic, accelerator);
	}
	
	/**
	 * Creates a check box menu item node.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param selected the state of the checkbox.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(KeyStroke accelerator, boolean selected, Action action)
	{
		return new MenuCheckBoxNode(action, selected, 0, accelerator);
	}
	
	/**
	 * Creates a check box menu item node.
	 * @param mnemonic the key mnemonic for the item.
	 * @param selected the state of the checkbox.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(int mnemonic, boolean selected, Action action)
	{
		return new MenuCheckBoxNode(action, selected, mnemonic, null);
	}
	
	/**
	 * Creates a check box menu item node.
	 * @param selected the state of the checkbox.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(boolean selected, Action action)
	{
		return new MenuCheckBoxNode(action, selected, 0, null);
	}
	
	/**
	 * Creates a check box menu item node.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the checkbox.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(Icon icon, String label, boolean selected, int mnemonic, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return new MenuCheckBoxNode(action(icon, label, handler), selected, mnemonic, accelerator);
	}

	/**
	 * Creates a check box menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the checkbox.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(String label, boolean selected, int mnemonic, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return new MenuCheckBoxNode(action(label, handler), selected, mnemonic, accelerator);
	}

	/**
	 * Creates a check box menu item.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the checkbox.
	 * @param mnemonic the key mnemonic for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(Icon icon, String label, boolean selected, int mnemonic, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return new MenuCheckBoxNode(action(icon, label, handler), selected, mnemonic, null);
	}

	/**
	 * Creates a check box menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the checkbox.
	 * @param mnemonic the key mnemonic for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(String label, boolean selected, int mnemonic, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return new MenuCheckBoxNode(action(label, handler), selected, mnemonic, null);
	}

	/**
	 * Creates a check box menu item.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the checkbox.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(Icon icon, String label, boolean selected, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return new MenuCheckBoxNode(action(icon, label, handler), selected, 0, accelerator);
	}

	/**
	 * Creates a check box menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the checkbox.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(String label, boolean selected, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return new MenuCheckBoxNode(action(label, handler), selected, 0, accelerator);
	}

	/**
	 * Creates a check box menu item.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the checkbox.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(Icon icon, String label, boolean selected, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return new MenuCheckBoxNode(action(icon, label, handler), selected, 0, null);
	}

	/**
	 * Creates a check box menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the checkbox.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(String label, boolean selected, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return new MenuCheckBoxNode(action(label, handler), selected, 0, null);
	}

	/**
	 * Creates a menu separator item.
	 * @return a menu node.
	 */
	public static MenuNode separator()
	{
		return new MenuSeparatorNode();
	}

	/**
	 * A single menu node.
	 */
	public static abstract class MenuNode
	{
	    protected abstract void addTo(JMenu menu);
	    protected abstract void addTo(JPopupMenu menu);
	}

	/** Menu item node. */
	private static class MenuItemNode extends MenuNode
	{
		/** The item action. */
		protected Action action;
		/** The mnemonic for the item. */
		protected int mnemonic;
		/** The accelerator for the item. */
		protected KeyStroke accelerator;
		
		private MenuItemNode(Action action, int mnemonic, KeyStroke accelerator)
		{
			this.action = action;
			this.mnemonic = mnemonic;
			this.accelerator = accelerator;
		}
	
		@Override
		protected void addTo(JMenu menu)
		{
			JMenuItem item = new JMenuItem(action);
			if (mnemonic > 0)
				item.setMnemonic(mnemonic);
			item.setAccelerator(accelerator);
			menu.add(item);
		}
	
		@Override
		protected void addTo(JPopupMenu menu)
		{
			JMenuItem item = new JMenuItem(action);
			if (mnemonic > 0)
				item.setMnemonic(mnemonic);
			item.setAccelerator(accelerator);
			menu.add(item);
		}
		
	}

	/** Menu checkbox node. */
	private static class MenuCheckBoxNode extends MenuNode
	{
		/** The item action. */
		protected Action action;
		/** Starts selected. */
		protected boolean selected;
		/** The mnemonic for the item. */
		protected int mnemonic;
		/** The accelerator for the item. */
		protected KeyStroke accelerator;
		
		private MenuCheckBoxNode(Action action, boolean selected, int mnemonic, KeyStroke accelerator)
		{
			this.action = action;
			this.selected = selected;
			this.mnemonic = mnemonic;
			this.accelerator = accelerator;
		}
	
		@Override
		protected void addTo(JMenu menu)
		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
			item.setState(selected);
			if (mnemonic > 0)
				item.setMnemonic(mnemonic);
			item.setAccelerator(accelerator);
			menu.add(item);
		}
	
		@Override
		protected void addTo(JPopupMenu menu)
		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
			item.setState(selected);
			if (mnemonic > 0)
				item.setMnemonic(mnemonic);
			item.setAccelerator(accelerator);
			menu.add(item);
		}
		
	}

	/** Menu branch node. */
	private static class MenuBranchNode extends MenuNode
	{
		/** The icon for the item. */
		protected Icon icon;
		/** The label for the item. */
		protected String label;
		/** The mnemonic for the item. */
		protected int mnemonic;
		/** The additional nodes. */
		protected MenuNode[] nodes;
		
		private MenuBranchNode(Icon icon, String label, int mnemonic, MenuNode[] nodes)
		{
			this.icon = icon;
			this.label = label;
			this.mnemonic = mnemonic;
			this.nodes = nodes;
		}
	
		@Override
		protected void addTo(JMenu menu)
		{
			JMenu next = new JMenu(label);
			next.setIcon(icon);
			if (mnemonic > 0)
				next.setMnemonic(mnemonic);
			
			for (MenuNode mn : nodes)
				mn.addTo(next);
			
			menu.add(next);
		}
	
		@Override
		protected void addTo(JPopupMenu menu)
		{
			JMenu next = new JMenu(label);
			next.setIcon(icon);
			if (mnemonic > 0)
				next.setMnemonic(mnemonic);
			
			for (MenuNode mn : nodes)
				mn.addTo(next);
			
			menu.add(next);
		}
		
	}

	/** Menu separator node. */
	private static class MenuSeparatorNode extends MenuNode
	{
		private MenuSeparatorNode() {}
		
		@Override
		protected void addTo(JMenu menu)
		{
			menu.addSeparator();
		}
	
		@Override
		protected void addTo(JPopupMenu menu)
		{
			menu.addSeparator();
		}
		
	}

	/* ==================================================================== */
	/* ==== Actions                                                    ==== */
	/* ==================================================================== */

	/**
	 * Creates a new action.
	 * @param icon the icon associated with the action.
	 * @param label the action label.
	 * @param handler the code called when the action is triggered.
	 * @return a new action.
	 */
	public static Action action(Icon icon, String label, ActionEventHandler handler)
	{
		return new HandledAction(icon, label, handler);
	}

	/**
	 * Creates a new action.
	 * @param label the action label.
	 * @param handler the code called when the action is triggered.
	 * @return a new action.
	 */
	public static Action action(String label, ActionEventHandler handler)
	{
		return new HandledAction(null, label, handler);
	}

	/**
	 * Creates a new action.
	 * @param icon the icon associated with the action.
	 * @param handler the code called when the action is triggered.
	 * @return a new action.
	 */
	public static Action action(Icon icon, ActionEventHandler handler)
	{
		return new HandledAction(icon, null, handler);
	}

	/**
	 * A handler interface for listening for action events.
	 * @param <C> the component type that this handles.
	 */
	@FunctionalInterface
	public interface ComponentActionHandler<C> extends ActionEventHandler
	{
		@Override
		@SuppressWarnings("unchecked")
		default void handleActionEvent(ActionEvent event)
		{
			onActionEvent((C)event.getSource(), event);
		}
		
		/**
		 * Called when the component emits an action.
		 * @param component the component on the event.
		 * @param event the emitted event.
		 */
		void onActionEvent(C component, ActionEvent event);
	}
	
	/**
	 * An action handler that is called when an action is performed.
	 */
	@FunctionalInterface
	public interface ActionEventHandler
	{
		/**
		 * Called when an action event happens.
		 * @param e the ActionEvent.
		 */
	    void handleActionEvent(ActionEvent e);
	}

	/**
	 * The action generated from an action call.
	 */
	private static class HandledAction extends AbstractAction
	{
		private static final long serialVersionUID = 7014730121602528947L;
		
		private ActionEventHandler handler;
	
	    private HandledAction(Icon icon, String label, ActionEventHandler handler)
	    {
	    	super(label, icon);
	    	this.handler = handler;
	    }
	    
		@Override
		public void actionPerformed(ActionEvent e)
	    {
	    	handler.handleActionEvent(e);
		}
		
	}

}
