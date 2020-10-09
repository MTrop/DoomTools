package net.mtrop.doom.tools.decohack.patches;

import net.mtrop.doom.tools.decohack.DEHActionPointer;
import net.mtrop.doom.tools.decohack.DEHState;

/**
 * Combined state plus action pointer data specific to Boom.
 * @author Matthew Tropiano
 */
public class BoomState
{
	private DEHState state;
	private DEHActionPointer pointer;
	
	private BoomState()
	{
		this.state = null;
		this.pointer = null;
	}
	
	public static BoomState create(DEHState state, DEHActionPointer pointer)
	{
		BoomState out = new BoomState();
		out.state = state;
		out.pointer = pointer;
		return out;
	}
	
	public DEHState getState() 
	{
		return state;
	}
	
	public DEHActionPointer getPointer() 
	{
		return pointer;
	}
	
}
