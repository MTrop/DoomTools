package net.mtrop.doom.tools.doomfetch;

import java.io.PrintStream;


/**
 * A DoomFetch Driver for Austral Doom Realms.
 * Uses the TSPG API/setup.
 * @author Matthew Tropiano
 */
public class TSPGAustralDriver extends TSPGDriver
{
	private static final String SERVICE_NAME = "Austral Doom Realms";
	private static final String BASE_URL = "https://audrealms.org/zandronum/";
	
	/**
	 * Creates a Austral Doom Realms fetch driver.
	 * @param out the output stream.
	 * @param err the error stream.
	 */
	public TSPGAustralDriver(PrintStream out, PrintStream err) 
	{
		super(SERVICE_NAME, BASE_URL, out, err);
	}

}
