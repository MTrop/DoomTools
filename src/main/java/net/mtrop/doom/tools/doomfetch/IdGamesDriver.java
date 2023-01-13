package net.mtrop.doom.tools.doomfetch;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import com.blackrook.json.JSONObject;

import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPRequest;

/**
 * A DoomFetch Driver descriptor.
 * @author Matthew Tropiano
 */
public class IdGamesDriver extends FetchDriver 
{
	private static final String ROOT_URL = "https://www.gamers.org/pub/idgames/";
	
	/**
	 * Creates a fetch driver.
	 * @param out the output stream.
	 * @param err the error stream.
	 */
	public IdGamesDriver(PrintStream out, PrintStream err)
	{
		super(out, err);
	}

	@Override
	public Response getStreamFor(String name) throws IOException
	{
		JSONObject json = HTTPRequest.get("https://www.doomworld.com/idgames/api/api.php")
			.parameters(
				HTTPUtils.entry("action", "search"),
				HTTPUtils.entry("type", "filename"),
				HTTPUtils.entry("sort", "name"),
				HTTPUtils.entry("out", "json"),
				HTTPUtils.entry("query", name)
		).send(JSON_READER);
		
		JSONObject content = json.get("content");
		JSONObject meta = json.get("meta");
		
		if (meta.isUndefined())
			return null;
		if (content.isUndefined())
			return null;
		
		JSONObject selectedFile = null;
		JSONObject files = content.get("file");
		
		// Object response.
		if (files.isObject())
		{
			if (!files.hasMember("filename"))
			{
				err.println("Response from idGames is malformed!");
				return null;
			}
			else if (FileUtils.getFileNameWithoutExtension(files.get("filename").getString()).equals(name))
			{
				selectedFile = files;
			}
		}
		// Array response.
		else if (files.isArray())
		{
			JSONObject file;
			for (int i = 0; i < files.length(); i++)
			{
				file = files.get(i);
				if (!file.hasMember("filename"))
				{
					err.println("Response from idGames is malformed!");
					return null;
				}
				else if (FileUtils.getFileNameWithoutExtension(file.get("filename").getString()).equals(name))
				{
					selectedFile = files;
					break;
				}
			}
		}
		else
		{
			err.println("Response from idGames is malformed!");
			return null;
		}

		if (selectedFile == null)
			return null;
		
		String uri = selectedFile.get("dir").getString() + selectedFile.get("filename").getString();
		
		InputStream in = HTTPRequest.get(ROOT_URL + uri).setAutoRedirect(true).send().getContentStream();
		
		return new Response("", "", in);
	}

}
