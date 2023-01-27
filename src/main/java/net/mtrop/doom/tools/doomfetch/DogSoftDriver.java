package net.mtrop.doom.tools.doomfetch;

import java.io.IOException;
import java.io.PrintStream;

import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.struct.util.HTTPUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPContent;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPRequest;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPResponse;

/**
 * DogSoft fetch driver.
 * @author Matthew Tropiano
 */
public class DogSoftDriver extends FetchDriver 
{
	private static final String ROOT_URL = "https://doom.dogsoft.net";
	private static final String GETWAD_URL = ROOT_URL + "/getwad.php";
	private static final String USER_AGENT = "DoomFetch/" + Version.DOOMFETCH;

	/**
	 * Creates a fetch driver.
	 * @param out the output stream.
	 * @param err the error stream.
	 */
	public DogSoftDriver(PrintStream out, PrintStream err)
	{
		super(out, err);
	}

	@Override
	public Response getStreamFor(String name) throws IOException 
	{
		// DogSoft does not accept searches below 4 characters.
		if (name.length() < 4)
			return null;
		
		out.println("Searching Doom.DogSoft.Net...");
		
		HTTPResponse searchResponse = null;
		
		// Incoming name has no extension, search for matching extensions.
		String[] exts = {"wad", "zip", "pk3", "pk7"};
		
		for (int i = 0; i < exts.length; i++)
		{
			String filename = name + "." + exts[i];
			
			try {
				searchResponse = HTTPRequest.post(GETWAD_URL)
					.setHeader("User-Agent", USER_AGENT)
					.content(HTTPContent.createFormContent(HTTPUtils.parameters(
						HTTPUtils.entry("search", filename)
					)))
				.send();
				
				// if HTML, we got a null response. Anything else, probably a file.
				
				if (!searchResponse.getContentType().endsWith("/html"))
					return new Response(filename, "", "", searchResponse);
				
			} catch (IOException e) {
				err.println("ERROR: Read error from Doom.DogSoft.Net.");
			}

			IOUtils.close(searchResponse);

			// Don't flood server.
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// Do nothing.
			}
		}
		
		return null;
	}

}
