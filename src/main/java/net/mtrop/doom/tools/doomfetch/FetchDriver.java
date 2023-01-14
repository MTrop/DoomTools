package net.mtrop.doom.tools.doomfetch;

import java.io.IOException;
import java.io.PrintStream;

import com.blackrook.json.JSONObject;
import com.blackrook.json.JSONReader;

import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPReader;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPResponse;

/**
 * A DoomFetch Driver descriptor.
 * @author Matthew Tropiano
 */
public abstract class FetchDriver 
{
	/** JSON reader. */
	public static final HTTPReader<JSONObject> JSON_READER = (response, cancel, monitor) -> {
		return JSONReader.readJSON(response.getContentReader());
	};

	/** Output stream. */
	protected PrintStream out; 
	/** Error stream. */
	protected PrintStream err;
	
	/**
	 * Creates a fetch driver.
	 * @param out the output stream.
	 * @param err the error stream.
	 */
	protected FetchDriver(PrintStream out, PrintStream err)
	{
		this.out = out;
		this.err = err;
	}
	
	/**
	 * Attempts to search for and fetch a file by a specific name.
	 * @param name the name of the file to find.
	 * @return a response if it was found to use for file read, or null if not found by this driver.
	 * @throws IOException the exception if any read errors occur.
	 */
	public abstract Response getStreamFor(String name) throws IOException;
	
	/**
	 * Response class.
	 */
	public static class Response implements AutoCloseable
	{
		private String filename;
		private String etag;
		private String date;
		private HTTPResponse httpResponse;
		
		public Response(String filename, String etag, String date, HTTPResponse httpResponse)
		{
			this.filename = filename;
			this.etag = etag;
			this.date = date;
			this.httpResponse = httpResponse;
		}

		public String getFilename() 
		{
			return filename;
		}
		
		public String getETag() 
		{
			return etag;
		}
		
		public String getDate() 
		{
			return date;
		}
		
		public HTTPResponse getHTTPResponse() 
		{
			return httpResponse;
		}
		
		@Override
		public void close() throws IOException
		{
			httpResponse.close();
		}
		
	}
	
}
