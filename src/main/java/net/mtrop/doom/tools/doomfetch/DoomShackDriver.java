package net.mtrop.doom.tools.doomfetch;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPReader;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPRequest;

/**
 * Doom Shack fetch driver.
 * @author Matthew Tropiano
 */
public class DoomShackDriver extends FetchDriver 
{
	private static final String ROOT_URL = "https://doomshack.org";
	private static final String WADLIST_URL = ROOT_URL + "/wadlist.php";
	private static final String USER_AGENT = "DoomFetch/" + Version.DOOMFETCH;

	private static final SingletonProvider<SiteCache> CACHE = new SingletonProvider<SiteCache>(() -> (new SiteCache()));
	
	private static class SiteCache
	{
		private Map<String, String> wadToURI;
		
		private SiteCache() 
		{
			this.wadToURI = null;
		}
		
		/**
		 * Checks if the cache has data in it.
		 * @return true if so, false if not.
		 */
		public synchronized boolean hasData()
		{
			return wadToURI != null;
		}
		
		/**
		 * Parses the fetched WAD list.
		 * @param listData the fetched data.
		 * @return itself.
		 * @throws IOException if a parse error occurs.
		 */
		public synchronized SiteCache build(String listData) throws IOException
		{
			this.wadToURI = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			
			Document document = Jsoup.parse(listData);

			for (Element e : document.select("li > a"))
			{
				String uri = e.attr("href");
				String filename = e.html();
				String name = FileUtils.getFileNameWithoutExtension(filename);
				
				wadToURI.put(name, uri);
			}
			
			return this;
		}
		
		/**
		 * Fetches a URI for a filename.
		 * @param name the name of the file (no extension).
		 * @return a URI if found, null if not.
		 */
		public String getURI(String name)
		{
			return wadToURI.get(name);
		}
		
	}
	
	/**
	 * Creates a fetch driver.
	 * @param out the output stream.
	 * @param err the error stream.
	 */
	public DoomShackDriver(PrintStream out, PrintStream err)
	{
		super(out, err);
	}

	@Override
	public Response getStreamFor(String name) throws IOException 
	{
		SiteCache cache = CACHE.get();
		if (!cache.hasData())
		{
			out.println("Pulling DoomShack WAD list...");
			try {
				cache = cache.build(HTTPRequest.get(WADLIST_URL)
					.setHeader("User-Agent", USER_AGENT)
					.send(HTTPReader.createStringReader()));
			} catch (IOException e) {
				err.println("ERROR: Cannot fetch WAD list from DoomShack.org");
				return null;
			}
		}
		
		out.println("Searching DoomShack WAD list for match...");
		String uri = cache.getURI(name);
		
		if (uri == null)
			return null;
		
		HTTPRequest request = HTTPRequest.get(ROOT_URL + uri)
			.setHeader("User-Agent", USER_AGENT)
			.setAutoRedirect(true);
		
		return new Response(uri.substring(uri.lastIndexOf('/') + 1), "", "", request.send());
	}

}
