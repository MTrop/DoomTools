package net.mtrop.doom.tools.doomfetch;

import java.io.PrintStream;


/**
 * A DoomFetch Driver for TSPG: Painkiller (Allfearthesentinel).
 * @author Matthew Tropiano
 */
public class TSPGPainkillerDriver extends TSPGDriver
{
	private static final String SERVICE_NAME = "TSPG: Painkiller";
	private static final String BASE_URL = "https://allfearthesentinel.com/zandronum/";
	
	/**
	 * Creates a TSPG: Painkiller fetch driver.
	 * @param out the output stream.
	 * @param err the error stream.
	 */
	public TSPGPainkillerDriver(PrintStream out, PrintStream err) 
	{
		super(SERVICE_NAME, BASE_URL, out, err);
	}

}
