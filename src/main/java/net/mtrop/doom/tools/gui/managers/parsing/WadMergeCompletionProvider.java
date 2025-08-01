/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers.parsing;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;

import net.mtrop.doom.tools.wadmerge.WadMergeCommand;

/**
 * WadMerge Completion Provider.
 * @author Matthew Tropiano
 */
public class WadMergeCompletionProvider extends CommonCompletionProvider
{
	public WadMergeCompletionProvider()
	{
		super();
		for (WadMergeCommand command : WadMergeCommand.values())
			addCompletion(new CommandCompletion(this, command));
	}
	
	@Override
	public boolean isAutoActivateOkay(JTextComponent tc) 
	{
		if (!super.isAutoActivateOkay(tc))
			return false;
		
		// Only auto-complete at the beginning of a line.
		int offs = tc.getCaretPosition();
		char c;
		do {
			try {
				c = tc.getText(offs - 1, 1).charAt(0);
			} catch (BadLocationException e) {
				// Eat exception
				return false;
			}
		} while (--offs > 0 && c != '\n' && Character.isWhitespace(c));
		
		return c == '\n' || offs <= 0;
	}
	
	/**
	 * Special completion for WadMerge-based stuff.
	 */
	public class CommandCompletion extends TemplateCompletion
	{
		private final String summaryText;
		
		protected CommandCompletion(CompletionProvider parent, WadMergeCommand command) 
		{
			super(parent, 
				command.name().toLowerCase(), 
				command.usage().toLowerCase(), 
				command.usage().toLowerCase().replace("[", "${").replace("]", "}")
			);
			
			final ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
			try (PrintStream textOut = new PrintStream(bos, true))
			{
				command.help(textOut);
			}
			this.summaryText = writeHTML((html) -> html.tag("pre", (new String(bos.toByteArray()))));
		}
		
		@Override
		public String getSummary()
		{
			return summaryText;
		}

	}

}

