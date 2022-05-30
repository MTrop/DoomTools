package net.mtrop.doom.tools.gui.managers.parsing;

import org.fife.ui.autocomplete.AbstractCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerDoom19;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF21;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerParamType;

/** 
 * DECOHack Completion Provider.
 * @author Matthew Tropiano
 */
public class DecoHackCompletionProvider extends DefaultCompletionProvider
{
	public DecoHackCompletionProvider()
	{
		super();
		for (DEHActionPointerDoom19 pointer : DEHActionPointerDoom19.values())
			addCompletion(new PointerCompletion(this, pointer));
		for (DEHActionPointerMBF pointer : DEHActionPointerMBF.values())
			addCompletion(new PointerCompletion(this, pointer));
		for (DEHActionPointerMBF21 pointer : DEHActionPointerMBF21.values())
			addCompletion(new PointerCompletion(this, pointer));
		// TODO: Add macros.
	}
	
	// Special completion for WadMerge-based stuff.
	public static class PointerCompletion extends AbstractCompletion
	{
		private final String name;
		private final String paramTypeText; 
		private final String summaryText;
		
		protected PointerCompletion(CompletionProvider parent, DEHActionPointer pointer)
		{
			super(parent);
			this.name = "A_" + pointer.getMnemonic();
			
			boolean first = true;
			StringBuilder sb = new StringBuilder();
			for (DEHActionPointerParamType ptype : pointer.getParams())
			{
				if (!first)
					sb.append(", ");
				sb.append(ptype.name().toLowerCase());
				first = false;
			}
			this.paramTypeText = sb.toString();
			
			// TODO: Write actual docs for each pointer.
			this.summaryText = this.name + "(" + this.paramTypeText + ")";
		}
		
		@Override
		public String getInputText()
		{
			return name;
		}

		@Override
		public String getReplacementText()
		{
			return name + "(" + paramTypeText + ")";
		}

		@Override
		public String getSummary()
		{
			return summaryText;
		}

		@Override
		public String toString() 
		{
			return name + "(" + paramTypeText + ")";
		}
		
	}

}
