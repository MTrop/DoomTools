package net.mtrop.doom.tools.decohack.data;

import java.util.Map;

/**
 * Describes all DeHackEd objects that are actors.
 * @author Matthew Tropiano
 */
public interface DEHActor
{
	/**
	 * @return the state label map.
	 */
	Map<String, Integer> getStateIndexMap();
		
}
