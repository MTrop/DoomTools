package net.mtrop.doom.tools.doomfetch;

import java.io.IOException;
import java.io.PrintStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPContent;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPReader;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPRequest;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPResponse;

/**
 * Doom Shack fetch driver.
 * @author Matthew Tropiano
 */
public class DogSoftDriver extends FetchDriver 
{
	private static final String ROOT_URL = "https://doom.dogsoft.net";
	private static final String SEARCH_URL = ROOT_URL + "/search.php";
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
		
		out.println("Searching Doom.DogSoft.Net WAD list...");
		
		String data;
		HTTPResponse searchResponse = null;
		
		try {
			searchResponse = HTTPRequest.post(SEARCH_URL).content(HTTPContent.createFormContent(HTTPUtils.parameters(
				HTTPUtils.entry("search", name),
				HTTPUtils.entry("s", "Submit")
			))).send();
			
			if (!searchResponse.isSuccess())
			{
				err.println("ERROR: Received status " + searchResponse.getStatusCode() + " Doom.DogSoft.Net: " + searchResponse.getStatusMessage());
				return null;
			}
			
			data = searchResponse.read(HTTPReader.createStringReader());

		} catch (IOException e) {
			err.println("ERROR: Read error from Doom.DogSoft.Net.");
			return null;
		} finally {
			IOUtils.close(searchResponse);
		}
		
		out.println("Scanning Doom.DogSoft.Net result for match...");

		// Get table to parse. Second table is the results.
		Document document = Jsoup.parse(data);
		Elements tables = document.select("table");
		Element resultTable;
		try {
			resultTable = tables.get(1);
		} catch (IndexOutOfBoundsException e) {
			err.println("ERROR: Unexpected return for Doom.DogSoft.Net.");
			return null;
		}
		
		String uri = null;
		String filename = null;
		
		// Don't select header row, which is not a header.
		for (Element e : resultTable.select("tr:not(:first-child)"))
		{
			Element link = e.selectFirst("td > a");
			
			filename = link.html();
			
			if (FileUtils.getFileNameWithoutExtension(filename).equalsIgnoreCase(name))
			{
				uri = link.attr("href");
				break;
			}
		}
		
		if (uri == null)
			return null;
		
		HTTPRequest request = HTTPRequest.get(ROOT_URL + "/" + uri)
			.setHeader("User-Agent", USER_AGENT)
			.setAutoRedirect(true);
		
		return new Response(filename, "", "", request.send());
	}

}
