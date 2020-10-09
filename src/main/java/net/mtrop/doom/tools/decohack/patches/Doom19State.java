package net.mtrop.doom.tools.decohack.patches;

import net.mtrop.doom.tools.decohack.DEHState;

/**
 * Combined state plus action pointer data specific to Doom 1.9 pointer addressing.
 * @author Matthew Tropiano
 */
public class Doom19State
{
	private DEHState state;
	private Integer pointerIndex;
	
	private Doom19State()
	{
		this.state = null;
		this.pointerIndex = null;
	}
	
	public static Doom19State create(DEHState state, Integer pointerIndex)
	{
		Doom19State out = new Doom19State();
		out.state = state;
		out.pointerIndex = pointerIndex;
		return out;
	}
	
	public DEHState getState() 
	{
		return state;
	}
	
	public Integer getPointerIndex() 
	{
		return pointerIndex;
	}
	
}
