package net.mtrop.doom.tools.doomfetch;

import java.io.PrintStream;


/**
 * A DoomFetch Driver for TSPG: Euroboros.
 * @author Matthew Tropiano
 */
public class TSPGEuroborosDriver extends TSPGDriver
{
	private static final String SERVICE_NAME = "TSPG: Euroboros";
	private static final String BASE_URL = "https://euroboros.net/zandronum/";
	
	/**
	 * Creates a TSPG: Euroboros fetch driver.
	 * @param out the output stream.
	 * @param err the error stream.
	 */
	public TSPGEuroborosDriver(PrintStream out, PrintStream err) 
	{
		super(SERVICE_NAME, BASE_URL, out, err);
	}

}
