/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.swing;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
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
import javax.swing.UIManager;
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
	/* ==== Enums                                                      ==== */
	/* ==================================================================== */

	/**
	 * List selection mode.
	 */
	public enum ListSelectionMode
	{
		SINGLE(ListSelectionModel.SINGLE_SELECTION),
		SINGLE_INTERVAL(ListSelectionModel.SINGLE_INTERVAL_SELECTION),
		MULTIPLE_INTERVAL(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		private final int swingId;
		
		private ListSelectionMode(int swingId)
		{
			this.swingId = swingId;
		}
	}
	
	/**
	 * Progress bar orientation.
	 */
	public enum ProgressBarOrientation
	{
		HORIZONTAL(JProgressBar.HORIZONTAL),
		VERTICAL(JProgressBar.VERTICAL);
		
		private final int swingId;
		
		private ProgressBarOrientation(int swingId)
		{
			this.swingId = swingId;
		}
	}
	
	/**
	 * Slider orientation.
	 */
	public enum SliderOrientation
	{
		HORIZONTAL(JSlider.HORIZONTAL),
		VERTICAL(JSlider.VERTICAL);
		
		private final int swingId;
		
		private SliderOrientation(int swingId)
		{
			this.swingId = swingId;
		}
	}
	
	/* ==================================================================== */
	/* ==== Handlers                                                   ==== */
	/* ==================================================================== */

	/**
	 * A handler interface for listening for list selection events.
	 * @param <V> the list's value type.
	 */
	@FunctionalInterface
	public interface ListSelectionHandler<V>
	{
		/**
		 * Called when a list's selection changes.
		 * @param selected the selected items.
		 * @param adjusting if true, this is in the middle of adjusting, false if not.
		 */
		void onSelectionChange(List<V> selected, boolean adjusting);
	}
	
	/**
	 * A handler interface for listening for table selection events.
	 */
	@FunctionalInterface
	public interface TableSelectionHandler
	{
		/**
		 * Called when a list's selection changes.
		 * @param rowsSelected the selected rows. 
		 * @param columnsSelected the selected columns. 
		 * @param adjusting true if adjusting, false if not.
		 */
		void onSelectionChange(int[] rowsSelected, int[] columnsSelected, boolean adjusting);
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
	
	/**
	 * A handler interface for listening for change events.
	 * @param <C> the component type that this handles.
	 * @param <T> the item type.
	 */
	@FunctionalInterface
	public interface ComponentItemHandler<C, T> extends ItemListener
	{
		@Override
		@SuppressWarnings("unchecked")
		default void itemStateChanged(ItemEvent event)
		{
			C component = (C)event.getSource();
			onItemChangeEvent(component, (T)event.getItem());
		}
		
		/**
		 * Called when a component emits an item change event.
		 * @param component the associated component.
		 * @param item the item affected.
		 */
		void onItemChangeEvent(C component, T item);
	}
	
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
	
	/**
	 * Creates a new blank label.
	 * @return a created label.
	 */
	public static JLabel label()
	{
		return new JLabel("");
	}
	
	/**
	 * Creates an uneditable text area styled like a label, such that it can display multi-line, wrapped text.
	 * @param label the label.
	 * @return a created text area.
	 */
	public static JTextArea wrappedLabel(String label)
	{
		JTextArea out = textArea();
	    out.setBackground(UIManager.getColor("Label.background"));
	    out.setFont(UIManager.getFont("Label.font"));
	    out.setBorder(UIManager.getBorder("Label.border"));
	    out.setOpaque(false);
	    out.setEditable(false);
	    out.setFocusable(false);
	    out.setWrapStyleWord(true);
	    out.setLineWrap(true);
	    out.setText(label);
	    return out;
	}

	/* ==================================================================== */
	/* ==== Icons                                                      ==== */
	/* ==================================================================== */

	/**
	 * Creates a new icon from an image.
	 * @param image the image to use.
	 * @return a new Icon.
	 */
	public static ImageIcon icon(Image image)
	{
		return new ImageIcon(image);
	}

	/* ==================================================================== */
	/* ==== Buttons                                                    ==== */
	/* ==================================================================== */

	/**
	 * Creates a button.
	 * @param icon the check box icon.
	 * @param label the check box label.
	 * @param mnemonic the button mnemonic.
	 * @param handler the handler for button click.
	 * @return a new button.
	 */
	public static JButton button(Icon icon, String label, int mnemonic, ComponentActionHandler<JButton> handler)
	{
		JButton out = new JButton(actionItem(icon, label, handler));
		if (mnemonic > 0)
			out.setMnemonic(mnemonic);
		return out;
	}

	/**
	 * Creates a button.
	 * @param icon the check box icon.
	 * @param label the check box label.
	 * @param handler the handler for button click.
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
	 * @param handler the handler for button click.
	 * @return a new button.
	 */
	public static JButton button(Icon icon, int mnemonic, ComponentActionHandler<JButton> handler)
	{
		return button(icon, null, 0, handler);
	}

	/**
	 * Creates a button.
	 * @param icon the check box icon.
	 * @param handler the handler for button click.
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
	 * @param handler the handler for button click.
	 * @return a new button.
	 */
	public static JButton button(String label, int mnemonic, ComponentActionHandler<JButton> handler)
	{
		return button(null, label, 0, handler);
	}

	/**
	 * Creates a button.
	 * @param label the check box label.
	 * @param handler the handler for button click.
	 * @return a new button.
	 */
	public static JButton button(String label, ComponentActionHandler<JButton> handler)
	{
		return button(label, 0, handler);
	}
	
	/**
	 * Creates a button.
	 * @param handler the handler for button click.
	 * @return a new button.
	 */
	public static JButton button(ComponentActionHandler<JButton> handler)
	{
		return button((String)null, handler);
	}
	
	/**
	 * Creates a button that shows a pop-up menu on click.
	 * @param icon the check box icon.
	 * @param label the check box label.
	 * @param mnemonic the button mnemonic.
	 * @param menu the menu to display.
	 * @return a new button.
	 */
	public static JButton button(Icon icon, String label, int mnemonic, final JPopupMenu menu)
	{
		return button(icon, label, mnemonic, (b, e) -> {
			menu.show(b, 0, b.getHeight());
		});
	}

	/**
	 * Creates a button that shows a pop-up menu on click.
	 * @param icon the check box icon.
	 * @param label the check box label.
	 * @param menu the menu to display.
	 * @return a new button.
	 */
	public static JButton button(Icon icon, String label, JPopupMenu menu)
	{
		return button(icon, label, 0, menu);
	}
	
	/**
	 * Creates a button that shows a pop-up menu on click.
	 * @param icon the check box icon.
	 * @param mnemonic the button mnemonic.
	 * @param menu the menu to display.
	 * @return a new button.
	 */
	public static JButton button(Icon icon, int mnemonic, JPopupMenu menu)
	{
		return button(icon, null, 0, menu);
	}

	/**
	 * Creates a button that shows a pop-up menu on click.
	 * @param icon the check box icon.
	 * @param menu the menu to display.
	 * @return a new button.
	 */
	public static JButton button(Icon icon, JPopupMenu menu)
	{
		return button(icon, 0, menu);
	}
	
	/**
	 * Creates a button that shows a pop-up menu on click.
	 * @param label the check box label.
	 * @param mnemonic the button mnemonic.
	 * @param menu the menu to display.
	 * @return a new button.
	 */
	public static JButton button(String label, int mnemonic, JPopupMenu menu)
	{
		return button(null, label, 0, menu);
	}

	/**
	 * Creates a button that shows a pop-up menu on click.
	 * @param label the check box label.
	 * @param menu the menu to display.
	 * @return a new button.
	 */
	public static JButton button(String label, JPopupMenu menu)
	{
		return button(label, 0, menu);
	}
	
	/**
	 * Creates a button bound to an action.
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
	 * Creates a button bound to an action.
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
	 * @param checked the checked state.
	 * @param handler the handler for button click.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(Icon icon, String label, boolean checked, ComponentActionHandler<JCheckBox> handler)
	{
		return checkBox(checked, actionItem(icon, label, handler));
	}

	/**
	 * Creates a check box.
	 * @param label the check box label.
	 * @param checked the checked state.
	 * @param handler the handler for button click.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(String label, boolean checked, ComponentActionHandler<JCheckBox> handler)
	{
		return checkBox(checked, actionItem(label, handler));
	}

	/**
	 * Creates a check box.
	 * @param icon the check box icon.
	 * @param checked the checked state.
	 * @param handler the handler for button click.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(Icon icon, boolean checked, ComponentActionHandler<JCheckBox> handler)
	{
		return checkBox(checked, actionItem(icon, handler));
	}

	/**
	 * Creates a check box.
	 * @param checked the checked state.
	 * @param handler the handler for button click.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(boolean checked, ComponentActionHandler<JCheckBox> handler)
	{
		return checkBox(checked, actionItem(handler));
	}

	/**
	 * Creates a check box, unchecked.
	 * @param icon the check box icon.
	 * @param label the check box label.
	 * @param handler the handler for button click.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(Icon icon, String label, ComponentActionHandler<JCheckBox> handler)
	{
		return checkBox(false, actionItem(icon, label, handler));
	}

	/**
	 * Creates a check box, unchecked.
	 * @param label the check box label.
	 * @param handler the handler for button click.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(String label, ComponentActionHandler<JCheckBox> handler)
	{
		return checkBox(false, actionItem(label, handler));
	}

	/**
	 * Creates a check box, unchecked.
	 * @param icon the check box icon.
	 * @param handler the handler for button click.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(Icon icon, ComponentActionHandler<JCheckBox> handler)
	{
		return checkBox(false, actionItem(icon, handler));
	}

	/**
	 * Creates a check box, unchecked.
	 * @param handler the handler for button click.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(ComponentActionHandler<JCheckBox> handler)
	{
		return checkBox(false, actionItem(handler));
	}

	/**
	 * Creates a check box.
	 * @param checked the checked state.
	 * @param action the action for the check box.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(boolean checked, Action action)
	{
		JCheckBox out = new JCheckBox(action);
		out.setSelected(checked);
		return out;
	}

	/**
	 * Creates a check box.
	 * @param checked the checked state.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox(boolean checked)
	{
		JCheckBox out = new JCheckBox();
		out.setSelected(checked);
		return out;
	}

	/**
	 * Creates a check box, unchecked.
	 * @return a new check box.
	 */
	public static JCheckBox checkBox()
	{
		return checkBox(false);
	}

	/* ==================================================================== */
	/* ==== Radio Buttons                                              ==== */
	/* ==================================================================== */
	
	/**
	 * Creates a radio button.
	 * @param icon the radio button icon.
	 * @param label the radio button label.
	 * @param checked the checked state.
	 * @param handler the handler for button click.
	 * @return a new radio button.
	 */
	public static JRadioButton radio(Icon icon, String label, boolean checked, ComponentActionHandler<JRadioButton> handler)
	{
		return radio(checked, actionItem(icon, label, handler));
	}

	/**
	 * Creates a radio button.
	 * @param label the radio button label.
	 * @param checked the checked state.
	 * @param handler the handler for button click.
	 * @return a new radio button.
	 */
	public static JRadioButton radio(String label, boolean checked, ComponentActionHandler<JRadioButton> handler)
	{
		return radio(checked, actionItem(label, handler));
	}

	/**
	 * Creates a radio button.
	 * @param icon the radio button icon.
	 * @param checked the checked state.
	 * @param handler the handler for button click.
	 * @return a new radio button.
	 */
	public static JRadioButton radio(Icon icon, boolean checked, ComponentActionHandler<JRadioButton> handler)
	{
		return radio(checked, actionItem(icon, handler));
	}

	/**
	 * Creates a radio button.
	 * @param checked the checked state.
	 * @param handler the handler for button click.
	 * @return a new radio button.
	 */
	public static JRadioButton radio(boolean checked, ComponentActionHandler<JRadioButton> handler)
	{
		return radio(checked, actionItem(handler));
	}

	/**
	 * Creates a radio button, unchecked.
	 * @param icon the radio button icon.
	 * @param label the radio button label.
	 * @param handler the handler for button click.
	 * @return a new radio button.
	 */
	public static JRadioButton radio(Icon icon, String label, ComponentActionHandler<JRadioButton> handler)
	{
		return radio(false, actionItem(icon, label, handler));
	}

	/**
	 * Creates a radio button, unchecked.
	 * @param label the radio button label.
	 * @param handler the handler for button click.
	 * @return a new radio button.
	 */
	public static JRadioButton radio(String label, ComponentActionHandler<JRadioButton> handler)
	{
		return radio(false, actionItem(label, handler));
	}

	/**
	 * Creates a radio button, unchecked.
	 * @param icon the radio button icon.
	 * @param handler the handler for button click.
	 * @return a new radio button.
	 */
	public static JRadioButton radio(Icon icon, ComponentActionHandler<JRadioButton> handler)
	{
		return radio(false, actionItem(icon, handler));
	}

	/**
	 * Creates a radio button, unchecked.
	 * @param handler the handler for button click.
	 * @return a new radio button.
	 */
	public static JRadioButton radio(ComponentActionHandler<JRadioButton> handler)
	{
		return radio(false, actionItem(handler));
	}

	/**
	 * Creates a radio button.
	 * @param checked the checked state.
	 * @param action the action for the radio button.
	 * @return a new radio button.
	 */
	public static JRadioButton radio(boolean checked, Action action)
	{
		JRadioButton out = new JRadioButton(action);
		out.setSelected(checked);
		return out;
	}

	/**
	 * Creates a radio button.
	 * @param checked the checked state.
	 * @return a new radio button.
	 */
	public static JRadioButton radio(boolean checked)
	{
		JRadioButton out = new JRadioButton();
		out.setSelected(checked);
		return out;
	}

	/**
	 * Creates a radio button, unchecked.
	 * @return a new radio button.
	 */
	public static JRadioButton radio()
	{
		return radio(false);
	}

	/* ==================================================================== */
	/* ==== Button Groups                                              ==== */
	/* ==================================================================== */

	/**
	 * Crates a button group that manages a set of buttons.
	 * @param buttons the buttons to add.
	 * @return the button group.
	 */
	public static ButtonGroup group(AbstractButton ... buttons)
	{
		ButtonGroup out = new ButtonGroup();
		for (AbstractButton button : buttons)
			out.add(button);
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
	public static JSlider slider(SliderOrientation orientation, BoundedRangeModel rangeModel, ComponentChangeHandler<JSlider> handler)
	{
		JSlider out = new JSlider(rangeModel);
		out.setOrientation(orientation.swingId);
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
		return slider(SliderOrientation.HORIZONTAL, rangeModel, handler);
	}
	
	/**
	 * Creates a value slider.
	 * @param orientation the orientation type.
	 * @param rangeModel the range model for the slider.
	 * @return a new slider.
	 */
	public static JSlider slider(SliderOrientation orientation, BoundedRangeModel rangeModel)
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
	 * Creates a new TextArea.
	 * @return a new text area.
	 */
	public static JTextArea textArea()
	{
		return new JTextArea();
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
	/* ==== Progress Bars                                              ==== */
	/* ==================================================================== */

	/**
	 * Creates a new progress bar with a default model.
	 * @param orientation the orientation.
	 * @return the new progress bar.
	 */
	public static JProgressBar progressBar(ProgressBarOrientation orientation)
	{
		return new JProgressBar(orientation.swingId);
	}
	
	/**
	 * Creates a new progress bar with a default model and horizontal orientation.
	 * @return the new progress bar.
	 */
	public static JProgressBar progressBar()
	{
		return progressBar(ProgressBarOrientation.HORIZONTAL);
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
		if (handler != null)
			out.addChangeListener(handler);
		return out;
	}

	/**
	 * Creates a value spinner.
	 * @param model the spinner model.
	 * @return the resultant spinner.
	 */
	public static JSpinner spinner(SpinnerModel model)
	{
		return spinner(model, null);
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
	 * @param handler the item change handler.
	 * @return the resultant spinner.
	 */
	public static <E> JComboBox<E> comboBox(ComboBoxModel<E> model, ComponentItemHandler<JComboBox<E>, E> handler)
	{
		JComboBox<E> out = new JComboBox<E>(model);
		out.addItemListener(handler);
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

	/**
	 * Creates a combo box (dropdown).
	 * @param <E> the item type.
	 * @param objects the objects to put in the list model.
	 * @param handler the item change handler.
	 * @return the resultant spinner.
	 */
	public static <E> JComboBox<E> comboBox(Collection<E> objects, ComponentItemHandler<JComboBox<E>, E> handler)
	{
		return comboBox(comboBoxModel(objects), handler);
	}

	/**
	 * Creates a combo box (dropdown).
	 * @param <E> the item type.
	 * @param objects the objects to put in the list model.
	 * @return the resultant spinner.
	 */
	public static <E> JComboBox<E> comboBox(Collection<E> objects)
	{
		return comboBox(comboBoxModel(objects));
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
	 * @param selectionMode the list selection mode.
	 * @param handler the listener to use for selection changes.
	 * @return the list component.
	 */
	public static <E> JList<E> list(ListModel<E> model, ListCellRenderer<E> renderer, int selectionMode, final ListSelectionHandler<E> handler)
	{
		final JList<E> out = new JList<>(model);
		out.setCellRenderer(renderer);
		out.setSelectionMode(selectionMode);
		out.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
		{
			@Override
			public void valueChanged(ListSelectionEvent e) 
			{
				handler.onSelectionChange(out.getSelectedValuesList(), e.getValueIsAdjusting());
			}
		});
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
	 * @param selectionMode the list selection mode.
	 * @param handler the listener to use for selection changes.
	 * @return the list component.
	 */
	public static <E> JList<E> list(ListModel<E> model, ListSelectionMode selectionMode, final ListSelectionHandler<E> handler)
	{
		JList<E> out = new JList<>(model);
		out.setSelectionMode(selectionMode.swingId);
		out.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
		{
			@Override
			public void valueChanged(ListSelectionEvent e) 
			{
				handler.onSelectionChange(out.getSelectedValuesList(), e.getValueIsAdjusting());
			}
		});
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
	 * Creates a list with a specific list model.
	 * @param <E> the object type that the model contains.
	 * @param model the list model.
	 * @param selectionMode the list selection mode.
	 * @return the list component.
	 */
	public static <E> JList<E> list(ListModel<E> model, ListSelectionMode selectionMode)
	{
		JList<E> out = new JList<>(model);
		out.setSelectionMode(selectionMode.swingId);
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

	/**
	 * Creates a list with a list model made from a collection.
	 * @param <E> the object type that the model contains.
	 * @param objects the objects to put in the list model.
	 * @param selectionMode the list selection mode.
	 * @param handler the listener to use for selection changes.
	 * @return the list component.
	 */
	public static <E> JList<E> list(Collection<E> objects, ListSelectionMode selectionMode, ListSelectionHandler<E> handler)
	{
		return list(listModel(objects), selectionMode, handler);
	}

	/**
	 * Creates a list with a list model made from a collection and single selection mode.
	 * @param <E> the object type that the model contains.
	 * @param objects the objects to put in the list model.
	 * @param handler the listener to use for selection changes.
	 * @return the list component.
	 */
	public static <E> JList<E> list(Collection<E> objects, ListSelectionHandler<E> handler)
	{
		return list(listModel(objects), ListSelectionMode.SINGLE, handler);
	}

	/**
	 * Creates a list with a list model made from a collection.
	 * @param <E> the object type that the model contains.
	 * @param objects the objects to put in the list model.
	 * @param selectionMode the list selection mode.
	 * @return the list component.
	 */
	public static <E> JList<E> list(Collection<E> objects, ListSelectionMode selectionMode)
	{
		return list(listModel(objects), selectionMode);
	}

	/**
	 * Creates a list with a specific list model and single selection mode.
	 * @param <E> the object type that the model contains.
	 * @param objects the objects to put in the list model.
	 * @return the list component.
	 */
	public static <E> JList<E> list(Collection<E> objects)
	{
		return list(listModel(objects), ListSelectionMode.SINGLE);
	}

	/* ==================================================================== */
	/* ==== Tables                                                     ==== */
	/* ==================================================================== */

	/**
	 * Creates a new table.
	 * @param model the table model.
	 * @param columnModel the column model.
	 * @param selectionMode the list selection mode.
	 * @param handler the listener to use for selection changes.
	 * @return the table created.
	 */
	public static JTable table(TableModel model, TableColumnModel columnModel, int selectionMode, final TableSelectionHandler handler)
	{
		JTable out = new JTable(model, columnModel);
		out.setSelectionMode(selectionMode);
		out.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
		{
			@Override
			public void valueChanged(ListSelectionEvent e) 
			{
				handler.onSelectionChange(out.getSelectedRows(), out.getSelectedColumns(), e.getValueIsAdjusting());
			}
		});
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
	 * @param selectionMode the list selection mode.
	 * @param handler the listener to use for selection changes.
	 * @return the table created.
	 */
	public static JTable table(TableModel model, ListSelectionMode selectionMode, final TableSelectionHandler handler)
	{
		JTable out = new JTable(model, new DefaultTableColumnModel());
		out.setSelectionMode(selectionMode.swingId);
		out.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
		{
			@Override
			public void valueChanged(ListSelectionEvent e) 
			{
				handler.onSelectionChange(out.getSelectedRows(), out.getSelectedColumns(), e.getValueIsAdjusting());
			}
		});
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
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for accessing (VK).
	 * @param handler the code called when the action is triggered.
	 * @return a new JMenu.
	 */
	public static JMenu menu(Icon icon, String label, int mnemonic, ComponentActionHandler<JMenu> handler)
	{
		JMenu out = new JMenu(actionItem(icon, label, handler));
		out.setMnemonic(mnemonic);
		return out;
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
	 * Creates a menu item node.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(int mnemonic, KeyStroke accelerator, Action action)
	{
		return new MenuItemNode(mnemonic, accelerator, action);
	}

	/**
	 * Creates a menu item node.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(KeyStroke accelerator, Action action)
	{
		return menuItem(0, accelerator, action);
	}

	/**
	 * Creates a menu item node.
	 * @param mnemonic the key mnemonic for the item.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(int mnemonic, Action action)
	{
		return menuItem(mnemonic, null, action);
	}

	/**
	 * Creates a menu item node.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(Action action)
	{
		return menuItem(0, null, action);
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
	public static MenuNode menuItem(Icon icon, String label, int mnemonic, KeyStroke accelerator, ComponentActionHandler<JMenuItem> handler)
	{
		return menuItem(mnemonic, accelerator, actionItem(icon, label, handler));
	}

	/**
	 * Creates a menu item node.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(String label, int mnemonic, KeyStroke accelerator, ComponentActionHandler<JMenuItem> handler)
	{
		return menuItem(null, label, mnemonic, accelerator, handler);
	}

	/**
	 * Creates a menu item node.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(Icon icon, String label, int mnemonic, ComponentActionHandler<JMenuItem> handler)
	{
		return menuItem(icon, label, mnemonic, null, handler);
	}

	/**
	 * Creates a menu item node.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(String label, int mnemonic, ComponentActionHandler<JMenuItem> handler)
	{
		return menuItem(null, label, mnemonic, null, handler);
	}

	/**
	 * Creates a menu item node.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(Icon icon, String label, KeyStroke accelerator, ComponentActionHandler<JMenuItem> handler)
	{
		return menuItem(icon, label, 0, accelerator, handler);
	}

	/**
	 * Creates a menu item node.
	 * @param label the label for the menu entry.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(String label, KeyStroke accelerator, ComponentActionHandler<JMenuItem> handler)
	{
		return menuItem(null, label, 0, accelerator, handler);
	}

	/**
	 * Creates a menu item node.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(Icon icon, String label, ComponentActionHandler<JMenuItem> handler)
	{
		return menuItem(icon, label, 0, null, handler);
	}

	/**
	 * Creates a menu item node.
	 * @param label the label for the menu entry.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(String label, ComponentActionHandler<JMenuItem> handler)
	{
		return menuItem(null, label, 0, null, handler);
	}

	/**
	 * Creates a menu item node.
	 * @param handler the code called when the action is triggered.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(ComponentActionHandler<JMenuItem> handler)
	{
		return menuItem(null, null, 0, null, handler);
	}

	/**
	 * Creates a menu item submenu.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param mnemonic the key mnemonic for the item.
	 * @param nodes the menu nodes to add.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(Icon icon, String label, int mnemonic, MenuNode ... nodes)
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
	public static MenuNode menuItem(String label, int mnemonic, MenuNode ... nodes)
	{
		return menuItem(null, label, mnemonic, nodes);
	}
	
	/**
	 * Creates a menu item submenu.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param nodes the menu nodes to add.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(Icon icon, String label, MenuNode ... nodes)
	{
		return menuItem(icon, label, 0, nodes);
	}
	
	/**
	 * Creates a menu item submenu.
	 * @param label the label for the menu entry.
	 * @param nodes the menu nodes to add.
	 * @return a menu node.
	 */
	public static MenuNode menuItem(String label, MenuNode ... nodes)
	{
		return menuItem(null, label, 0, nodes);
	}

	/**
	 * Creates a check box menu item node.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param selected the state of the check box.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(int mnemonic, KeyStroke accelerator, boolean selected, Action action)
	{
		return new MenuCheckBoxNode(selected, mnemonic, accelerator, action);
	}

	/**
	 * Creates a check box menu item node.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param selected the state of the check box.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(KeyStroke accelerator, boolean selected, Action action)
	{
		return checkBoxItem(0, accelerator, selected, action);
	}

	/**
	 * Creates a check box menu item node.
	 * @param mnemonic the key mnemonic for the item.
	 * @param selected the state of the check box.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(int mnemonic, boolean selected, Action action)
	{
		return checkBoxItem(mnemonic, null, selected, action);
	}

	/**
	 * Creates a check box menu item node.
	 * @param selected the state of the check box.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(boolean selected, Action action)
	{
		return checkBoxItem(0, null, selected, action);
	}

	/**
	 * Creates a check box menu item node, unchecked.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(Action action)
	{
		return checkBoxItem(0, null, false, action);
	}

	/**
	 * Creates a check box menu item node.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the check box.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(Icon icon, String label, boolean selected, int mnemonic, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return checkBoxItem(mnemonic, accelerator, selected, actionItem(icon, label, handler));
	}

	/**
	 * Creates a check box menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the check box.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(String label, boolean selected, int mnemonic, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return checkBoxItem(mnemonic, accelerator, selected, actionItem(label, handler));
	}

	/**
	 * Creates a check box menu item.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the check box.
	 * @param mnemonic the key mnemonic for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(Icon icon, String label, boolean selected, int mnemonic, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return checkBoxItem(mnemonic, null, selected, actionItem(icon, label, handler));
	}

	/**
	 * Creates a check box menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the check box.
	 * @param mnemonic the key mnemonic for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(String label, boolean selected, int mnemonic, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return checkBoxItem(mnemonic, null, selected, actionItem(label, handler));
	}

	/**
	 * Creates a check box menu item.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the check box.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(Icon icon, String label, boolean selected, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return checkBoxItem(0, accelerator, selected, actionItem(icon, label, handler));
	}

	/**
	 * Creates a check box menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the check box.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(String label, boolean selected, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return checkBoxItem(0, accelerator, selected, actionItem(label, handler));
	}

	/**
	 * Creates a check box menu item.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the check box.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(Icon icon, String label, boolean selected, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return checkBoxItem(0, null, selected, actionItem(icon, label, handler));
	}

	/**
	 * Creates a check box menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the check box.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode checkBoxItem(String label, boolean selected, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return checkBoxItem(0, null, selected, actionItem(label, handler));
	}

	/**
	 * Creates a radio button menu item node.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param selected the state of the radio button.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(int mnemonic, KeyStroke accelerator, boolean selected, Action action)
	{
		return new MenuRadioNode(selected, mnemonic, accelerator, action);
	}

	/**
	 * Creates a radio button menu item node.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param selected the state of the radio button.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(KeyStroke accelerator, boolean selected, Action action)
	{
		return radioItem(0, accelerator, selected, action);
	}

	/**
	 * Creates a radio button menu item node.
	 * @param mnemonic the key mnemonic for the item.
	 * @param selected the state of the radio button.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(int mnemonic, boolean selected, Action action)
	{
		return radioItem(mnemonic, null, selected, action);
	}

	/**
	 * Creates a radio button menu item node.
	 * @param selected the state of the radio button.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(boolean selected, Action action)
	{
		return radioItem(0, null, selected, action);
	}

	/**
	 * Creates a radio button menu item node, unchecked.
	 * @param action the action for the menu item.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(Action action)
	{
		return radioItem(0, null, false, action);
	}

	/**
	 * Creates a radio button menu item node.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the radio button.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(Icon icon, String label, boolean selected, int mnemonic, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return radioItem(mnemonic, accelerator, selected, actionItem(icon, label, handler));
	}

	/**
	 * Creates a radio button menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the radio button.
	 * @param mnemonic the key mnemonic for the item.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(String label, boolean selected, int mnemonic, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return radioItem(mnemonic, accelerator, selected, actionItem(label, handler));
	}

	/**
	 * Creates a radio button menu item.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the radio button.
	 * @param mnemonic the key mnemonic for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(Icon icon, String label, boolean selected, int mnemonic, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return radioItem(mnemonic, null, selected, actionItem(icon, label, handler));
	}

	/**
	 * Creates a radio button menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the radio button.
	 * @param mnemonic the key mnemonic for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(String label, boolean selected, int mnemonic, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return radioItem(mnemonic, null, selected, actionItem(label, handler));
	}

	/**
	 * Creates a radio button menu item.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the radio button.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(Icon icon, String label, boolean selected, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return radioItem(0, accelerator, selected, actionItem(icon, label, handler));
	}

	/**
	 * Creates a radio button menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the radio button.
	 * @param accelerator the keystroke shortcut for the item.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(String label, boolean selected, KeyStroke accelerator, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return radioItem(0, accelerator, selected, actionItem(label, handler));
	}

	/**
	 * Creates a radio button menu item.
	 * @param icon the icon for the menu entry.
	 * @param label the label for the menu entry.
	 * @param selected the state of the radio button.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(Icon icon, String label, boolean selected, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return radioItem(0, null, selected, actionItem(icon, label, handler));
	}

	/**
	 * Creates a radio button menu item.
	 * @param label the label for the menu entry.
	 * @param selected the state of the radio button.
	 * @param handler the change handler to add.
	 * @return a menu node.
	 */
	public static MenuNode radioItem(String label, boolean selected, ComponentActionHandler<JCheckBoxMenuItem> handler)
	{
		return radioItem(0, null, selected, actionItem(label, handler));
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
		
		private MenuItemNode(int mnemonic, KeyStroke accelerator, Action action)
		{
			this.action = action;
			this.mnemonic = mnemonic;
			this.accelerator = accelerator;
		}
	
		private JMenuItem createItem()
		{
			JMenuItem item = new JMenuItem(action);
			if (mnemonic > 0)
				item.setMnemonic(mnemonic);
			item.setAccelerator(accelerator);
			return item;
		}
		
		@Override
		protected void addTo(JMenu menu)
		{
			menu.add(createItem());
		}
	
		@Override
		protected void addTo(JPopupMenu menu)
		{
			menu.add(createItem());
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
		
		private MenuCheckBoxNode(boolean selected, int mnemonic, KeyStroke accelerator, Action action)
		{
			this.action = action;
			this.selected = selected;
			this.mnemonic = mnemonic;
			this.accelerator = accelerator;
		}
	
		private JCheckBoxMenuItem createItem()
		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
			item.setSelected(selected);
			if (mnemonic > 0)
				item.setMnemonic(mnemonic);
			item.setAccelerator(accelerator);
			return item;
		}
		
		@Override
		protected void addTo(JMenu menu)
		{
			menu.add(createItem());
		}
	
		@Override
		protected void addTo(JPopupMenu menu)
		{
			menu.add(createItem());
		}
		
	}

	/** Menu radio button node. */
	private static class MenuRadioNode extends MenuNode
	{
		/** The item action. */
		protected Action action;
		/** Starts selected. */
		protected boolean selected;
		/** The mnemonic for the item. */
		protected int mnemonic;
		/** The accelerator for the item. */
		protected KeyStroke accelerator;
		
		private MenuRadioNode(boolean selected, int mnemonic, KeyStroke accelerator, Action action)
		{
			this.action = action;
			this.selected = selected;
			this.mnemonic = mnemonic;
			this.accelerator = accelerator;
		}
	
		private JCheckBoxMenuItem createItem()
		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
			item.setSelected(selected);
			if (mnemonic > 0)
				item.setMnemonic(mnemonic);
			item.setAccelerator(accelerator);
			return item;
		}
		
		@Override
		protected void addTo(JMenu menu)
		{
			menu.add(createItem());
		}
	
		@Override
		protected void addTo(JPopupMenu menu)
		{
			menu.add(createItem());
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
	
		private JMenu createMenu()
		{
			JMenu next = new JMenu(label);
			next.setIcon(icon);
			if (mnemonic > 0)
				next.setMnemonic(mnemonic);
			
			for (MenuNode mn : nodes)
				mn.addTo(next);
			
			return next;
		}
		
		@Override
		protected void addTo(JMenu menu)
		{
			menu.add(createMenu());
		}
	
		@Override
		protected void addTo(JPopupMenu menu)
		{
			menu.add(createMenu());
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
	public static Action actionItem(Icon icon, String label, ActionEventHandler handler)
	{
		return new HandledAction(icon, label, handler);
	}

	/**
	 * Creates a new action.
	 * @param label the action label.
	 * @param handler the code called when the action is triggered.
	 * @return a new action.
	 */
	public static Action actionItem(String label, ActionEventHandler handler)
	{
		return new HandledAction(null, label, handler);
	}

	/**
	 * Creates a new action.
	 * @param icon the icon associated with the action.
	 * @param handler the code called when the action is triggered.
	 * @return a new action.
	 */
	public static Action actionItem(Icon icon, ActionEventHandler handler)
	{
		return new HandledAction(icon, null, handler);
	}

	/**
	 * Creates a new action.
	 * @param handler the code called when the action is triggered.
	 * @return a new action.
	 */
	public static Action actionItem(ActionEventHandler handler)
	{
		return new HandledAction(null, null, handler);
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
