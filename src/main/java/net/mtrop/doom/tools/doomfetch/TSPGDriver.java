package net.mtrop.doom.tools.doomfetch;

import java.io.IOException;
import java.io.PrintStream;

import com.blackrook.json.JSONObject;

import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.struct.util.HTTPUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPRequest;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPResponse;

/**
 * A DoomFetch Driver for TSPG-based APIs.
 * @author Matthew Tropiano
 */
public abstract class TSPGDriver extends FetchDriver
{
	private static final String WADS_URI = "autocomplete/wads.php";
	private static final String DOWNLOAD_URI = "download.php";
	private static final String USER_AGENT = "DoomFetch/" + Version.DOOMFETCH;
	
	private String serviceName;
	private String baseURL;
	
	/**
	 * Creates a TSPG fetch driver.
	 * @param serviceName the service name (for logs).
	 * @param baseURL the base URL for the API hookups.
	 * @param out the output stream.
	 * @param err the error stream.
	 */
	public TSPGDriver(String serviceName, String baseURL, PrintStream out, PrintStream err) 
	{
		super(out, err);
		this.serviceName = serviceName;
		this.baseURL = baseURL;
	}

	// Returns a file name with no extension.
	private static String noExtension(String filename)
	{
		int idx = filename.lastIndexOf(".");
		return idx >= 0 ? filename.substring(0, idx) : filename;
	}
	
	@Override
	public Response getStreamFor(String name) throws IOException
	{
		out.println("Searching " + serviceName + " via API...");
		
		JSONObject fileListArray;
		HTTPResponse response = null;
		
		try {
			response = HTTPRequest.get(baseURL + WADS_URI)
				.setHeader("User-Agent", USER_AGENT)
				.setHeader("Accept", "application/json")
				.setHeader("Accept-Encoding", "gzip")
				.setHeader("Accept-Language", "en-US,en;q=0.5")
				.parameters(
					HTTPUtils.entry("type", "wad"),
					HTTPUtils.entry("term", name)
				)
			.send();

			if (response.isError())
			{
				err.println("Received status " + response.getStatusCode() + " response from " + serviceName + ".");
				return null;
			}
			
			fileListArray = response.decode().read(JSON_READER);
			
		} finally {
			IOUtils.close(response);
		}
		
		// Should be array.
		if (!fileListArray.isArray())
		{
			err.println("Received unexpected response from " + serviceName + " WAD list.");
			return null;
		}
		
		String fullFilename = null;
		out.println("Searching " + serviceName + " result for match...");

		for (int i = 0; i < fileListArray.length(); i++)
		{
			String fn = fileListArray.get(i).getString();
			if (name.equalsIgnoreCase(noExtension(fn)))
			{
				fullFilename = fn;
				break;
			}
		}
		
		// no match. :(
		if (fullFilename == null)
		{
			return null;
		}
		
		// Download file.
		response = HTTPRequest.get(baseURL + DOWNLOAD_URI)
			.setHeader("User-Agent", USER_AGENT)
			.setParameter("file", fullFilename)
		.send();
		
		return new Response(fullFilename, "", "", response);
	}

}
