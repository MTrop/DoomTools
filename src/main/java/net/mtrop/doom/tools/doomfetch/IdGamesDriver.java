/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.doomfetch;

import java.io.IOException;
import java.io.PrintStream;

import com.blackrook.json.JSONObject;

import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPRequest;

/**
 * A DoomFetch Driver descriptor.
 * @author Matthew Tropiano
 */
public class IdGamesDriver extends FetchDriver 
{
	private static final String API_URL = "https://www.doomworld.com/idgames/api/api.php";
	private static final String ROOT_URL = "https://ftp.fu-berlin.de/pc/games/idgames/";
	private static final String USER_AGENT = "DoomFetch/" + Version.DOOMFETCH;
	
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
		out.println("Searching idGames via API...");
		JSONObject json = HTTPRequest.get(API_URL)
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
		
		out.println("Scanning idGames result for match...");
		// Array response.
		if (files.isArray())
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
				else if (FileUtils.getFileNameWithoutExtension(file.get("filename").getString()).equalsIgnoreCase(name))
				{
					selectedFile = file;
					break;
				}
			}
		}
		// Object response.
		else if (files.isObject())
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
		else
		{
			err.println("Response from idGames is malformed!");
			return null;
		}

		if (selectedFile == null)
			return null;
		
		String filenameStr = selectedFile.get("filename").getString();
		
		String uri = selectedFile.get("dir").getString() + filenameStr;
		
		HTTPRequest request = HTTPRequest.get(ROOT_URL + uri)
			.setHeader("User-Agent", USER_AGENT)
			.setAutoRedirect(true);
		
		return new Response(filenameStr, "", "", request.send());
	}

}
