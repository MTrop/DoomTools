/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TimeZone;

/**
 * HTTP Utilities.
 * @author Matthew Tropiano
 */
public final class HTTPUtils
{
	/** Default timeout in milliseconds. */
	public static int DEFAULT_TIMEOUT_MILLIS = 5000; 
	
	/** HTTP Method: GET. */
	public static final String HTTP_METHOD_GET = "GET"; 
	/** HTTP Method: HEAD. */
	public static final String HTTP_METHOD_HEAD = "HEAD";
	/** HTTP Method: DELETE. */
	public static final String HTTP_METHOD_DELETE = "DELETE"; 
	/** HTTP Method: OPTIONS. */
	public static final String HTTP_METHOD_OPTIONS = "OPTIONS"; 
	/** HTTP Method: TRACE. */
	public static final String HTTP_METHOD_TRACE = "TRACE";
	/** HTTP Method: POST. */
	public static final String HTTP_METHOD_POST = "POST";
	/** HTTP Method: PUT. */
	public static final String HTTP_METHOD_PUT = "PUT";

	/** No parameters. */
	private static final HTTPParameters NO_PARAMETERS = parameters();

	
	private HTTPUtils() {}
	
	private static final Charset UTF8 = Charset.forName("utf-8");
	private static final String[] VALID_HTTP = new String[]{"http", "https"};
	private static final byte[] URL_RESERVED = "!#$%&'()*+,/:;=?@[]".getBytes(UTF8);
	private static final byte[] URL_UNRESERVED = "-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~".getBytes(UTF8);
	private static final ThreadLocal<SimpleDateFormat> ISO_DATE = ThreadLocal.withInitial(()->
	{
		SimpleDateFormat out = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		out.setTimeZone(TimeZone.getTimeZone("GMT"));
		return out;
	});
	
	/**
	 * Interface for monitoring change in a data transfer.
	 */
	@FunctionalInterface
	public interface TransferMonitor
	{
		/**
		 * Called when a series of bytes move from one place to another. 
		 * @param current the current amount in bytes.
		 * @param max the maximum amount/target in bytes, if any.
		 */
		void onProgressChange(long current, Long max);
	}
	
	/**
	 * Interface for reading an HTTPResponse from a URL call.
	 * @param <R> the return type.
	 */
	@FunctionalInterface
	public interface HTTPReader<R>
	{
		/**
		 * Called to read the HTTP response from an HTTP call.
		 * @param response the response object.
		 * @return the returned decoded object.
		 * @throws IOException if a read error occurs.
		 */
		R onHTTPResponse(HTTPResponse response) throws IOException;
		
		/**
		 * An HTTP Reader that reads byte content and returns a decoded String.
		 * Gets the string contents of the response, decoded using the response's charset.
		 */
		static HTTPReader<String> STRING_CONTENT_READER = (response) ->
		{
			String charset;
			if ((charset = response.getCharset()) == null)
				throw new UnsupportedEncodingException("No charset specified.");
			
			char[] c = new char[16384];
			StringBuilder sb = new StringBuilder();
			InputStreamReader reader = new InputStreamReader(response.getContentStream(), charset);
	
			int buf = 0;
			while ((buf = reader.read(c)) >= 0) 
				sb.append(c, 0, buf);
			
			return sb.toString();
		};
		
		/**
		 * An HTTP Reader that reads byte content and returns a decoded String.
		 * Gets the string contents of the response, decoded using the response's charset.
		 */
		static HTTPReader<byte[]> BYTE_CONTENT_READER = (response) ->
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			long length = response.getLength() != null ? response.getLength() : -1L;
			relay(response.getContentStream(), bos, 8192, length, null);
			return bos.toByteArray();
		};
		
	}

	/**
	 * Content body abstraction.
	 */
	public interface HTTPContent
	{
		/**
		 * @return the content MIME-type of this content.
		 */
		String getContentType();
	
		/**
		 * @return the encoded charset of this content (can be null if not text).
		 */
		String getCharset();
	
		/**
		 * @return the encoding type of this content (like GZIP or somesuch).
		 */
		String getEncoding();
	
		/**
		 * @return the length of the content in bytes.
		 */
		long getLength();
	
		/**
		 * @return an input stream for the data.
		 * @throws IOException if the stream can't be opened.
		 * @throws SecurityException if the OS forbids opening it.
		 */
		InputStream getInputStream() throws IOException;
		
	}

	/**
	 * Multipart form data.
	 */
	public static class MultipartFormContent implements HTTPContent
	{
		private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	
		private static final byte[] CRLF;
		
		private static final byte[] DISPOSITION_HEADER;
		private static final byte[] DISPOSITION_NAME;
		private static final byte[] DISPOSITION_NAME_END;
		private static final byte[] DISPOSITION_FILENAME;
		private static final byte[] DISPOSITION_FILENAME_END;

		private static final byte[] TYPE_HEADER;
		private static final byte[] ENCODING_HEADER;

		private static final int BLUEPRINT_BOUNDARY_START = 0;
		private static final int BLUEPRINT_PART = 1;
		private static final int BLUEPRINT_BOUNDARY_MIDDLE = 2;
		private static final int BLUEPRINT_BOUNDARY_END = 3;

		static
		{
			CRLF = "\r\n".getBytes(UTF8);
			DISPOSITION_HEADER = "Content-Disposition: form-data".getBytes(UTF8);
			DISPOSITION_NAME = "; name=\"".getBytes(UTF8);
			DISPOSITION_NAME_END = "\"".getBytes(UTF8);
			DISPOSITION_FILENAME = "; filename=\"".getBytes(UTF8);
			DISPOSITION_FILENAME_END = "\"".getBytes(UTF8);
			TYPE_HEADER = "Content-Type: ".getBytes(UTF8);
			ENCODING_HEADER = "Content-Transfer-Encoding: ".getBytes(UTF8);
		}
		
		/** The form part first boundary. */
		private byte[] boundaryFirst;
		/** The form part middle boundary. */
		private byte[] boundaryMiddle;
		/** The form part ending boundary. */
		private byte[] boundaryEnd;
		
		/** List of Parts. */
		private List<Part> parts;
		/** Total length. */
		private long length;
		
		private MultipartFormContent() 
		{
			this.parts = new LinkedList<>();
			String boundaryText = generateBoundary();
			
			this.boundaryFirst = ("--" + boundaryText + "\r\n").getBytes(UTF8);
			this.boundaryMiddle = ("\r\n--" + boundaryText + "\r\n").getBytes(UTF8);
			this.boundaryEnd = ("\r\n--" + boundaryText + "--").getBytes(UTF8);
			
			// account for start and end boundary at least.
			this.length = boundaryFirst.length + boundaryEnd.length;
		}

		private static String generateBoundary()
		{
			Random r = new Random();
			StringBuilder sb = new StringBuilder();
			int dashes = r.nextInt(15) + 10;
			int letters = r.nextInt(24) + 16;
			while (dashes-- > 0)
				sb.append('-');
			while (letters-- > 0)
				sb.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
			return sb.toString();
		}
		
		// Adds a part and calculates change in length.
		private void addPart(final Part p)
		{
			boolean hadOtherParts = !parts.isEmpty();
			parts.add(p);
			length += p.getLength();
			if (hadOtherParts)
				length += boundaryMiddle.length;
		}
		
		/**
		 * Adds a single field to this multipart form.
		 * @param name the field name.
		 * @param value the value.
		 * @return itself, for chaining.
		 * @throws IOException if the data can't be encoded.
		 */
		public MultipartFormContent addField(String name, String value) throws IOException
		{
			return addTextPart(name, null, value);
		}
		
		/**
		 * Adds a single text part to this multipart form.
		 * @param name the field name.
		 * @param mimeType the mimeType of the text part.
		 * @param text the text data.
		 * @return itself, for chaining.
		 * @throws IOException if the data can't be encoded.
		 */
		public MultipartFormContent addTextPart(String name, final String mimeType, final String text) throws IOException
		{
			return addDataPart(name, mimeType, null, null, text.getBytes(UTF8));
		}
		
		/**
		 * Adds a file part to this multipart form.
		 * The name of the file is passed along.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
		 * @param data the file data.
		 * @return itself, for chaining.
		 * @throws IllegalArgumentException if data is null or the file cannot be found.
		 */
		public MultipartFormContent addFilePart(String name, String mimeType, final File data)
		{
			return addFilePart(name, mimeType, data.getName(), data);
		}
		
		/**
		 * Adds a file part to this multipart form.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
		 * @param fileName the file name to send (overridden).
		 * @param data the file data.
		 * @return itself, for chaining.
		 * @throws IllegalArgumentException if data is null or the file cannot be found.
		 */
		public MultipartFormContent addFilePart(String name, final String mimeType, final String fileName, final File data)
		{
			if (data == null)
				throw new IllegalArgumentException("data cannot be null.");
			if (!data.exists())
				throw new IllegalArgumentException("File " + data.getPath() + " cannot be found.");
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
			try {
				// Content Disposition Line
				bos.write(DISPOSITION_HEADER);
				bos.write(DISPOSITION_NAME);
				bos.write(name.getBytes(UTF8));
				bos.write(DISPOSITION_NAME_END);
				bos.write(DISPOSITION_FILENAME);
				bos.write(fileName.getBytes(UTF8));
				bos.write(DISPOSITION_FILENAME_END);
				bos.write(CRLF);

				// Content Type Line
				bos.write(TYPE_HEADER);
				bos.write(mimeType.getBytes(UTF8));
				bos.write(CRLF);

				// Blank line for header end.
				bos.write(CRLF);
				
				// ... data follows here.
			} catch (IOException e) {
				// should never happen.
				throw new RuntimeException(e);
			}

			final byte[] headerBytes = bos.toByteArray();
			
			addPart(new Part(headerBytes, new PartData() 
			{
				@Override
				public long getDataLength() 
				{
					return data.length();
				}
	
				@Override
				public InputStream getInputStream() throws IOException
				{
					return new FileInputStream(data);
				}
			}));
			return this;
		}
		
		/**
		 * Adds a byte data part to this multipart form.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
		 * @param dataIn the input data.
		 * @return itself, for chaining.
		 */
		public MultipartFormContent addDataPart(String name, String mimeType, byte[] dataIn)
		{
			return addDataPart(name, mimeType, null, null, dataIn);
		}
	
		/**
		 * Adds a byte data part to this multipart form as though it came from a file.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
		 * @param fileName the name of the file, as though this were originating from a file (can be null, for "no file").
		 * @param dataIn the input data.
		 * @return itself, for chaining.
		 */
		public MultipartFormContent addDataPart(String name, String mimeType, String fileName, byte[] dataIn)
		{
			return addDataPart(name, mimeType, null, fileName, dataIn);
		}
		
		/**
		 * Adds a byte data part (translated as text) to this multipart form as though it came from a file.
		 * @param name the field name.
		 * @param mimeType the mimeType of the file part.
		 * @param encoding the encoding type name for the data sent, like 'base64' or 'gzip' or somesuch (can be null to signal no encoding type).
		 * @param fileName the name of the file, as though this were originating from a file (can be null, for "no file").
		 * @param dataIn the input data.
		 * @return itself, for chaining.
		 */
		public MultipartFormContent addDataPart(String name, final String mimeType, final String encoding, final String fileName, final byte[] dataIn)
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
			try {
				// Content Disposition Line
				bos.write(DISPOSITION_HEADER);
				bos.write(DISPOSITION_NAME);
				bos.write(name.getBytes(UTF8));
				bos.write(DISPOSITION_NAME_END);
				if (fileName != null)
				{
					bos.write(DISPOSITION_FILENAME);
					bos.write(fileName.getBytes(UTF8));
					bos.write(DISPOSITION_FILENAME_END);
				}
				bos.write(CRLF);

				// Content Type Line
				if (mimeType != null)
				{
					bos.write(TYPE_HEADER);
					bos.write(mimeType.getBytes(UTF8));
					bos.write(CRLF);
				}

				// Content transfer encoding
				if (encoding != null)
				{
					bos.write(ENCODING_HEADER);
					bos.write(encoding.getBytes(UTF8));
					bos.write(CRLF);
				}
				
				// Blank line for header end.
				bos.write(CRLF);
				
				// ... data follows here.
			} catch (IOException e) {
				// should never happen.
				throw new RuntimeException(e);
			}

			final byte[] headerBytes = bos.toByteArray();
			
			addPart(new Part(headerBytes, new PartData() 
			{
				@Override
				public long getDataLength() 
				{
					return dataIn.length;
				}
	
				@Override
				public InputStream getInputStream()
				{
					return new ByteArrayInputStream(dataIn);
				}
			}));
			return this;
		}

		@Override
		public String getContentType()
		{
			return "multipart/form-data";
		}

		@Override
		public String getCharset()
		{
			return "utf-8";
		}

		@Override
		public String getEncoding()
		{
			return null;
		}

		@Override
		public long getLength()
		{
			return length;
		}

		@Override
		public InputStream getInputStream() throws IOException
		{
			return new MultiformInputStream();
		}
		
		/**
		 * Part data.
		 */
		private interface PartData
		{
			/**
			 * @return the content length of this part in bytes.
			 */
			long getDataLength();
			
			/**
			 * @return an open input stream to read from this part.
			 * @throws IOException if an I/O error occurs on read.
			 */
			InputStream getInputStream() throws IOException;
		}
		
		/**
		 * A single form part.
		 */
		private static class Part
		{
			private byte[] headerbytes;
			private PartData data;
			
			private Part(final byte[] headerbytes, final PartData data)
			{
				this.headerbytes = headerbytes;
				this.data = data;
			}
			
			/**
			 * @return the boundary-plus-header bytes that make up the start of this part.
			 */
			public byte[] getPartHeaderBytes()
			{
				return headerbytes;
			}

			/**
			 * @return the full length of this part, plus headers, in bytes.
			 */
			public long getLength()
			{
				return getPartHeaderBytes().length + data.getDataLength();
			}

			/**
			 * @return an open input stream for reading from this form.
			 * @throws IOException if an input stream could not be opened.
			 */
			public InputStream getInputStream() throws IOException
			{
				return new PartInputStream();
			}

			private class PartInputStream extends InputStream
			{
				private boolean readHeader;
				private InputStream currentStream;
				
				private PartInputStream()
				{
					this.readHeader = false;
					this.currentStream = new ByteArrayInputStream(headerbytes);
				}
				
				@Override
				public int read() throws IOException
				{
					if (currentStream == null)
						return -1;
					
					int out;
					if ((out = currentStream.read()) < 0)
					{
						currentStream.close();
						if (!readHeader)
						{
							currentStream = data.getInputStream();
							readHeader = true;
						}
						else
							currentStream = null;
						return read();
					}
					else
						return out;
				}
				
				@Override
				public void close() throws IOException
				{
					if (currentStream != null)
					{
						currentStream.close();
						currentStream = null;
					}
					super.close();
				}
			}
		}

		private class MultiformInputStream extends InputStream
		{	
			private int[] blueprint;
			private int currentBlueprint;
			private Iterator<Part> streamIterator;
			private Part currentPart;
			private InputStream currentStream;
			
			private MultiformInputStream() throws IOException
			{
				this.streamIterator = parts.iterator();
				this.currentBlueprint = 0;
				this.currentPart = null;
				this.currentStream = null;
				this.blueprint = new int[parts.isEmpty() ? 0 : parts.size() * 2 + 1];
				
				if (blueprint.length > 0)
				{
					this.blueprint[0] = BLUEPRINT_BOUNDARY_START;
					for (int i = 1; i < blueprint.length; i += 2)
					{
						this.blueprint[i] = BLUEPRINT_PART;
						this.blueprint[i + 1] = i + 1 < blueprint.length - 1 ? BLUEPRINT_BOUNDARY_MIDDLE : BLUEPRINT_BOUNDARY_END;
					}
				}
				nextStream();
			}
		
			private void nextStream() throws IOException
			{
				if (currentBlueprint >= blueprint.length)
				{
					currentPart = null;
					currentStream = null;
				}
				else switch (blueprint[currentBlueprint++])
				{
					case BLUEPRINT_BOUNDARY_START:
						currentStream = new ByteArrayInputStream(boundaryFirst);
						break;
					case BLUEPRINT_PART:
						currentPart = streamIterator.hasNext() ? streamIterator.next() : null;
						currentStream = currentPart != null ? currentPart.getInputStream() : null;
						break;
					case BLUEPRINT_BOUNDARY_MIDDLE:
						currentStream = new ByteArrayInputStream(boundaryMiddle);
						break;
					case BLUEPRINT_BOUNDARY_END:
						currentStream = new ByteArrayInputStream(boundaryEnd);
						break;
				}
			}
			
			@Override
			public int read() throws IOException
			{
				if (currentStream == null)
					return -1;
				
				int out;
				if ((out = currentStream.read()) < 0)
				{
					nextStream();
					return read();
				}
				
				return out;
			}
			
			@Override
			public void close() throws IOException
			{
				if (currentStream != null)
					currentStream.close();
				
				currentStream = null;
				currentPart = null;
				streamIterator = null;
				super.close();
			}
		}
	}

	private static class BlobContent implements HTTPContent
	{
		private String contentType;
		private String contentEncoding;
		private byte[] data;
		
		private BlobContent(String contentType, String contentEncoding, byte[] data)
		{
			this.contentType = contentType;
			this.contentEncoding = contentEncoding;
			this.data = data;
		}
		
		@Override
		public String getContentType()
		{
			return contentType;
		}
		
		@Override
		public String getCharset()
		{
			return null;
		}
		
		@Override
		public String getEncoding()
		{
			return contentEncoding;
		}
		
		@Override
		public long getLength()
		{
			return data.length;
		}
		
		@Override
		public InputStream getInputStream() throws IOException
		{
			return new ByteArrayInputStream(data);
		}
		
	}

	private static class TextContent extends BlobContent
	{
		private String contentCharset;
	
		private TextContent(String contentType, String contentCharset, String contentEncoding, byte[] data)
		{
			super(contentType, contentEncoding, data);
			this.contentCharset = contentCharset;
		}
		
		@Override
		public String getCharset()
		{
			return contentCharset;
		}
		
	}

	private static class FileContent implements HTTPContent
	{
		private String contentType;
		private String encodingType;
		private String charsetType;
		private File file;
		
		private FileContent(String contentType, String encodingType, String charsetType, File file)
		{
			if (file == null)
				throw new IllegalArgumentException("file cannot be null.");
			if (!file.exists())
				throw new IllegalArgumentException("File " + file.getPath() + " cannot be found.");
	
			this.contentType = contentType;
			this.encodingType = encodingType;
			this.charsetType = charsetType;
			this.file = file;
		}
		
		@Override
		public String getContentType()
		{
			return contentType;
		}
	
		@Override
		public String getCharset()
		{
			return charsetType;
		}
	
		@Override
		public String getEncoding()
		{
			return encodingType;
		}
	
		@Override
		public long getLength()
		{
			return file.length();
		}
	
		@Override
		public InputStream getInputStream() throws IOException
		{
			return new FileInputStream(file);
		}
		
	}

	private static class FormContent extends TextContent
	{
		private FormContent(HTTPParameters parameters)
		{
			super("x-www-form-urlencoded", Charset.defaultCharset().displayName(), null, parameters.toString().getBytes());
		}
	}

	/**
	 * HTTP Cookie object.
	 */
	public static class HTTPCookie
	{
		public enum SameSiteMode
		{
			STRICT,
			LAX,
			NONE;
		}
		
		private String key;
		private String value;
		private List<String> flags;
		
		private HTTPCookie(String key, String value)
		{
			this.key = key;
			this.value = value;
			this.flags = new LinkedList<>();
		}
		
		/**
		 * Sets the cookie expiry date. 
		 * @param date the expiry date.
		 * @return this, for chaining.
		 */
		public HTTPCookie expires(Date date)
		{
			flags.add("Expires=" + date(date));
			return this;
		}
		
		/**
		 * Sets the cookie expiry date. 
		 * @param dateMillis the expiry date in milliseconds since the Epoch.
		 * @return this, for chaining.
		 */
		public HTTPCookie expires(long dateMillis)
		{
			flags.add("Expires=" + date(dateMillis));
			return this;
		}
		
		/**
		 * Sets the cookie maximum age in seconds. 
		 * @param seconds the time in seconds.
		 * @return this, for chaining.
		 */
		public HTTPCookie maxAge(long seconds)
		{
			flags.add("Max-Age=" + seconds);
			return this;
		}
		
		/**
		 * Sets the cookie's relevant domain. 
		 * @param value the domain value.
		 * @return this, for chaining.
		 */
		public HTTPCookie domain(String value)
		{
			flags.add("Domain=" + value);
			return this;
		}
		
		/**
		 * Sets the cookie's relevant subpath. 
		 * @param value the path value.
		 * @return this, for chaining.
		 */
		public HTTPCookie path(String value)
		{
			flags.add("Path=" + value);
			return this;
		}

		/**
		 * Sets the cookie for only secure travel. 
		 * @return this, for chaining.
		 */
		public HTTPCookie secure()
		{
			flags.add("Secure");
			return this;
		}

		/**
		 * Sets the cookie for only top-level HTTP requests (not JS/AJAX). 
		 * @return this, for chaining.
		 */
		public HTTPCookie httpOnly()
		{
			flags.add("HttpOnly");
			return this;
		}

		/**
		 * Sets the cookie for a Same Site type. 
		 * @param mode the SameSite mode.
		 * @return this, for chaining.
		 */
		public HTTPCookie sameSite(SameSiteMode mode)
		{
			flags.add("SameSite=" + mode.name().charAt(0) + mode.name().substring(1).toLowerCase());
			return this;
		}

		/**
		 * @return the parameter string to add to header values.
		 */
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			sb.append(key).append('=').append(value);
			for (String flag : flags)
				sb.append("; ").append(flag);
			return sb.toString();
		}
	}
	
	/**
	 * HTTP headers object.
	 */
	public static class HTTPHeaders
	{
		private Map<String, String> map;
		
		private HTTPHeaders()
		{
			this.map = new HashMap<>(); 
		}

		/**
		 * Copies this header object.
		 * @return a copy of this header.
		 */
		public HTTPHeaders copy()
		{
			HTTPHeaders out = new HTTPHeaders();
			out.merge(this);
			return out;
		}
		
		/**
		 * Adds the header entries from another set of headers to this one.
		 * Existing names are overwritten.
		 * @param headers the input headers.
		 * @return this, for chaining.
		 */
		public HTTPHeaders merge(HTTPHeaders headers)
		{
			for (Map.Entry<String, String> entry : headers.map.entrySet())
				this.setHeader(entry.getKey(), entry.getValue());
			return this;
		}

		/**
		 * Sets a header.
		 * @param header the header name.
		 * @param value the header value.
		 * @return this, for chaining.
		 */
		public HTTPHeaders setHeader(String header, String value)
		{
			map.put(header, value);
			return this;
		}

	}
	
	/**
	 * HTTP Parameters object.
	 */
	public static class HTTPParameters
	{
		private Map<String, List<String>> map;
		
		private HTTPParameters()
		{
			this.map = new HashMap<>(); 
		}

		/**
		 * Copies this parameters object.
		 * @return a copy of this parameter object.
		 */
		public HTTPParameters copy()
		{
			HTTPParameters out = new HTTPParameters();
			out.merge(this);
			return out;
		}

		/**
		 * Adds the parameter entries from another set of parameters to this one.
		 * Parameters are added, not replaced.
		 * @param parameters the input parameters.
		 * @return this, for chaining.
		 * @see #addParameter(String, Object)
		 */
		public HTTPParameters merge(HTTPParameters parameters)
		{
			for (Map.Entry<String, List<String>> entry : parameters.map.entrySet())
				for (String value : entry.getValue())
					this.addParameter(entry.getKey(), value);
			return this;
		}

		/**
		 * Adds/creates a parameter.
		 * @param key the parameter name.
		 * @param value the parameter value.
		 * @return this, for chaining.
		 */
		public HTTPParameters addParameter(String key, Object value)
		{
			List<String> list;
			if ((list = map.get(key)) == null)
				map.put(key, (list = new LinkedList<>()));
			list.add(String.valueOf(value));
			return this;
		}

		/**
		 * Sets/resets a parameter and its values.
		 * If the parameter is already set, it is replaced.
		 * @param key the parameter name.
		 * @param value the parameter value.
		 * @return this, for chaining.
		 */
		public HTTPParameters setParameter(String key, Object value)
		{
			List<String> list;
			map.put(key, (list = new LinkedList<>()));
			list.add(String.valueOf(value));
			return this;
		}

		/**
		 * Sets/resets a parameter and its values.
		 * If the parameter is already set, it is replaced.
		 * @param key the parameter name.
		 * @param values the parameter values.
		 * @return this, for chaining.
		 */
		public HTTPParameters setParameter(String key, Object... values)
		{
			List<String> list;
			map.put(key, (list = new LinkedList<>()));
			for (Object v : values)
				list.add(String.valueOf(v));
			return this;
		}
		
		/**
		 * @return the parameter string to add to form content or URLs.
		 */
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, List<String>> entry : map.entrySet())
			{
				String key = entry.getKey();
				for (String value : entry.getValue())
				{
					if (sb.length() > 0)
						sb.append('&');
					sb.append(toURLEncoding(key));
					sb.append('=');
					sb.append(toURLEncoding(value));
				}
			}
			return sb.toString();
		}
	}
	
	/**
	 * Response from an HTTP call.
	 */
	public static class HTTPResponse implements AutoCloseable
	{
		private Map<String, List<String>> headers;
		private int statusCode;
		private String statusMessage;
		private Long length;
		private InputStream contentStream;
		private String charset;
		private String contentType;
		private String contentTypeHeader;
		private String encoding;
		private String contentDisposition;
		private String filename;
	
		private HTTPResponse() {}
		
		/**
		 * @return the headers on the response.
		 */
		public Map<String, List<String>> getHeaders()
		{
			return headers;
		}
		
		/**
		 * @return true if and only if the response status code is between 100 and 199, inclusive, false otherwise.
		 */
		public boolean isInformational()
		{
			return statusCode / 100 == 1;
		}
		
		/**
		 * @return true if and only if the response status code is between 200 and 299, inclusive, false otherwise.
		 */
		public boolean isSuccess()
		{
			return statusCode / 100 == 2;
		}
		
		/**
		 * @return true if and only if the response status code is between 300 and 399, inclusive, false otherwise.
		 */
		public boolean isRedirect()
		{
			return statusCode / 100 == 3;
		}
		
		/**
		 * @return true if and only if the response status code is between 400 and 599, inclusive, false otherwise.
		 */
		public boolean isError()
		{
			int range = statusCode / 100;
			return range == 4 || range == 5;
		}
		
		/**
		 * @return the response status code.
		 */
		public int getStatusCode()
		{
			return statusCode;
		}
	
		/**
		 * @return the response status message.
		 */
		public String getStatusMessage()
		{
			return statusMessage;
		}
	
		/**
		 * @return the response's content length (if reported).
		 */
		public Long getLength() 
		{
			return length;
		}
	
		/**
		 * @return an open input stream for reading the response's content.
		 */
		public InputStream getContentStream() 
		{
			return contentStream;
		}
		
		/**
		 * Convenience function for transferring the entirety of the content 
		 * stream to another.
		 * @param out the output stream.
		 * @return the amount of bytes moved.
		 * @throws IOException if an I/O error occurs during transfer.
		 */
		public final long relayContent(OutputStream out) throws IOException
		{
			return relayContent(out, null);
		}
		
		/**
		 * Convenience function for transferring the entirety of the content 
		 * stream to another, monitoring the progress as it goes.
		 * <p>Equivalent to: <code>return relay(getContentStream(), out, 8192, getLength(), monitor);</code>
		 * @param out the output stream.
		 * @param monitor the optional monitor. Can be null.
		 * @return the amount of bytes moved.
		 * @throws IOException if an I/O error occurs during transfer.
		 */
		public final long relayContent(OutputStream out, TransferMonitor monitor) throws IOException
		{
			return relay(getContentStream(), out, 8192, getLength(), monitor);
		}
		
		/**
		 * @return the response's charset. can be null.
		 */
		public String getCharset()
		{
			return charset;
		}
	
		/**
		 * @return the response's content type. can be null.
		 */
		public String getContentType()
		{
			return contentType;
		}
	
		/**
		 * @return the response's content type (full unparsed header). can be null.
		 */
		public String getContentTypeHeader()
		{
			return contentTypeHeader;
		}
	
		/**
		 * @return the response's encoding. can be null.
		 */
		public String getEncoding()
		{
			return encoding;
		}
	
		/**
		 * @return the content disposition. if present, usually "attachment". Can be null.
		 */
		public String getContentDisposition() 
		{
			return contentDisposition;
		}

		/**
		 * @return the content filename. Set if content disposition is "attachment". Can be null.
		 */
		public String getFilename() 
		{
			return filename;
		}

		@Override
		public void close()
		{
			HTTPUtils.close(contentStream);
		}
	}

	/**
	 * A request builder, as an alternative to calling the "http" methods directly.
	 */
	public static class HTTPRequest
	{
		/** HTTP Method. */
		private String method;
		/** Target URL string. */
		private String url;
		/** HTTP Headers. */
		private HTTPHeaders headers;
		/** HTTP Query Parameters. */
		private HTTPParameters parameters;
		/** Request timeout milliseconds. */
		private int timeoutMillis;
		/** Default charset encoding (on response). */
		private String defaultCharsetEncoding;
		/** HTTP Content Body. */
		private HTTPContent content;
		/** Upload Transfer Monitor. */
		private TransferMonitor monitor;
		
		private HTTPRequest()
		{
			this.method = null;
			this.url = null;
			this.headers = HTTPUtils.headers();
			this.parameters = HTTPUtils.parameters();
			this.timeoutMillis = DEFAULT_TIMEOUT_MILLIS;
			this.defaultCharsetEncoding = null;
			this.content = null;
			this.monitor = null;
		}
		
		/**
		 * Starts a GET request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 */
		public static HTTPRequest get(String url)
		{
			HTTPRequest out = new HTTPRequest();
			out.method = HTTP_METHOD_GET;
			out.url = url;
			return out;
		}

		/**
		 * Starts a HEAD request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 */
		public static HTTPRequest head(String url)
		{
			HTTPRequest out = new HTTPRequest();
			out.method = HTTP_METHOD_HEAD;
			out.url = url;
			return out;
		}

		/**
		 * Starts a DELETE request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 */
		public static HTTPRequest delete(String url)
		{
			HTTPRequest out = new HTTPRequest();
			out.method = HTTP_METHOD_DELETE;
			out.url = url;
			return out;
		}

		/**
		 * Starts a OPTIONS request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 */
		public static HTTPRequest options(String url)
		{
			HTTPRequest out = new HTTPRequest();
			out.method = HTTP_METHOD_OPTIONS;
			out.url = url;
			return out;
		}

		/**
		 * Starts a TRACE request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 */
		public static HTTPRequest trace(String url)
		{
			HTTPRequest out = new HTTPRequest();
			out.method = HTTP_METHOD_TRACE;
			out.url = url;
			return out;
		}

		/**
		 * Starts a PUT request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 */
		public static HTTPRequest put(String url)
		{
			HTTPRequest out = new HTTPRequest();
			out.method = HTTP_METHOD_PUT;
			out.url = url;
			return out;
		}

		/**
		 * Starts a POST request builder.
		 * @param url the URL to target.
		 * @return an {@link HTTPRequest}.
		 */
		public static HTTPRequest post(String url)
		{
			HTTPRequest out = new HTTPRequest();
			out.method = HTTP_METHOD_POST;
			out.url = url;
			return out;
		}

		/**
		 * Makes a deep copy of this request, such that
		 * changes to this one do not affect the original (the content and monitor, however, if any, is reference-copied). 
		 * @return a new HTTPRequest that is a copy of this one.
		 */
		public HTTPRequest copy()
		{
			HTTPRequest out = new HTTPRequest();
			out.method = this.method;
			out.url = this.url;
			out.headers = this.headers.copy();
			out.parameters = this.parameters.copy();
			out.timeoutMillis = this.timeoutMillis;
			out.defaultCharsetEncoding = this.defaultCharsetEncoding;
			out.content = this.content;
			out.monitor = this.monitor;
			return out;
		}
		
		/**
		 * Replaces the headers on this request.
		 * @param headers the new headers.
		 * @return this request, for chaining.
		 */
		public HTTPRequest headers(HTTPHeaders headers)
		{
			this.headers = headers;
			return this;
		}
		
		/**
		 * Adds headers to the headers on this request.
		 * @param headers the source header map.
		 * @return this request, for chaining.
		 * @see HTTPHeaders#merge(HTTPHeaders)
		 */
		public HTTPRequest addHeaders(HTTPHeaders headers)
		{
			this.headers.merge(headers);
			return this;
		}
		
		/**
		 * Replaces the parameters on this request.
		 * @param parameters the new parameters.
		 * @return this request, for chaining.
		 */
		public HTTPRequest parameters(HTTPParameters parameters)
		{
			this.parameters = parameters;
			return this;
		}
		
		/**
		 * Adds parameters to the headers on this request.
		 * @param parameters the source parameter map.
		 * @return this request, for chaining.
		 * @see HTTPHeaders#merge(HTTPHeaders)
		 */
		public HTTPRequest addParameters(HTTPParameters parameters)
		{
			this.parameters.merge(parameters);
			return this;
		}
		
		/**
		 * Sets the timeout for this request.
		 * If this is not set, the current default, {@link HTTPUtils#DEFAULT_TIMEOUT_MILLIS}, is the default.
		 * @param timeoutMillis the timeout in milliseconds.
		 * @return this request, for chaining.
		 */
		public HTTPRequest timeout(int timeoutMillis) 
		{
			this.timeoutMillis = timeoutMillis;
			return this;
		}

		/**
		 * Sets the default charset encoding to use on the response if none is sent back
		 * from the server.
		 * @param defaultCharsetEncoding the new encoding name.
		 * @return this request, for chaining.
		 */
		public HTTPRequest defaultCharsetEncoding(String defaultCharsetEncoding) 
		{
			this.defaultCharsetEncoding = defaultCharsetEncoding;
			return this;
		}
		
		/**
		 * Sets the content body for this request.
		 * @param content the content.
		 * @return this request, for chaining.
		 */
		public HTTPRequest content(HTTPContent content) 
		{
			this.content = content;
			return this;
		}
		
		/**
		 * Sets the upload monitor callback for the request.
		 * @param monitor the monitor to call.
		 * @return this request, for chaining.
		 */
		public HTTPRequest monitor(TransferMonitor monitor) 
		{
			this.monitor = monitor;
			return this;
		}
		
		/**
		 * Sends this request and gets a response.
		 * Best used in a try-with-resources block so that the response input stream auto-closes 
		 * (but not the connection, which stays alive if possible), like so:
		 * <pre><code>
		 * try (HTTPResponse response = request.send())
		 * {
		 *     // ... read response ...
		 * }
		 * </code></pre>
		 * @return an HTTPResponse object.
		 * @throws IOException if an error happens during the read/write.
		 * @throws SocketTimeoutException if the socket read times out.
		 * @throws ProtocolException if the request method is incorrect, or not an HTTP URL.
		 */
		public HTTPResponse send() throws IOException
		{
			return httpFetch(method, new URL(urlParams(url, parameters)), headers, content, defaultCharsetEncoding, timeoutMillis, monitor);
		}

		/**
		 * Sends this request and gets a decoded response via an {@link HTTPReader}.
		 * The response input stream auto-closes after read (but not the connection, which stays alive if possible).
		 * @param <T> the return type.
		 * @param reader the reader to use to read the response.
		 * @return the decoded object from the response.
		 * @throws IOException if an error happens during the read/write.
		 * @throws SocketTimeoutException if the socket read times out.
		 * @throws ProtocolException if the requestMethod is incorrect, or not an HTTP URL.
		 */
		public <T> T send(HTTPReader<T> reader) throws IOException
		{
			return httpFetch(method, new URL(urlParams(url, parameters)), headers, content, defaultCharsetEncoding, timeoutMillis, monitor, reader);
		}
	}
	
	
	/**
	 * Makes an HTTP-acceptable ISO date string from a Date.
	 * @param date the date to format.
	 * @return the resultant string.
	 */
	public static String date(Date date)
	{
		return ISO_DATE.get().format(date);
	}
	
	/**
	 * Makes an HTTP-acceptable ISO date string from a Date, represented in milliseconds since the Epoch.
	 * @param dateMillis the millisecond date to format.
	 * @return the resultant string.
	 */
	public static String date(long dateMillis)
	{
		return ISO_DATE.get().format(new Date(dateMillis));
	}
	
	/**
	 * Makes a comma-space-separated list of values.
	 * @param values the values to join together.
	 * @return the resultant string.
	 */
	public static String list(String ... values)
	{
		return join(", ", values);
	}
	
	/**
	 * Joins a list of values into one string, placing a joiner between all of them.
	 * @param joiner the joining string.
	 * @param values the values to join together.
	 * @return the resultant string.
	 */
	public static String join(String joiner, String ... values)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++)
		{
			sb.append(values[i]);
			if (i < values.length - 1)
				sb.append(joiner);
		}
		return sb.toString();
	}
	
	/**
	 * Makes a "Value; Parameter" string.
	 * @param value the value.
	 * @param parameter the value parameter.
	 * @return a string that is equivalent to: <code>String.valueOf(value) + "; " + String.valueOf(parameter)</code>.
	 */
	public static String valueParam(String value, String parameter)
	{
		return String.valueOf(value) + "; " + String.valueOf(parameter);
	}
	
	/**
	 * Makes a "Key=Value" string.
	 * @param key the key.
	 * @param value the value.
	 * @return a string that is equivalent to: <code>String.valueOf(key) + '=' + String.valueOf(value)</code>.
	 */
	public static String keyValue(String key, String value)
	{
		return String.valueOf(key) + '=' + String.valueOf(value);
	}
	
	/**
	 * Starts a new {@link HTTPCookie} object.
	 * @param key the cookie name.
	 * @param value the cookie value. 
	 * @return a new cookie object.
	 */
	public static HTTPCookie cookie(String key, String value)
	{
		return new HTTPCookie(key, value);
	}
	
	/**
	 * Starts a new {@link HTTPHeaders} object.
	 * @return a new header object.
	 */
	public static HTTPHeaders headers()
	{
		return new HTTPHeaders();
	}
	
	/**
	 * Starts a new {@link HTTPParameters} object.
	 * @return a new parameters object.
	 */
	public static HTTPParameters parameters()
	{
		return new HTTPParameters();
	}
	
	/**
	 * Creates a text blob content body for an HTTP request.
	 * @param contentType the data's content type.
	 * @param text the text data.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createTextContent(String contentType, String text)
	{
		try {
			return new TextContent(contentType, "utf-8", null, text.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("JVM does not support the UTF-8 charset [INTERNAL ERROR].");
		}
	}

	/**
	 * Creates a byte blob content body for an HTTP request.
	 * @param contentType the data's content type.
	 * @param bytes the byte data.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createByteContent(String contentType, byte[] bytes)
	{
		return new BlobContent(contentType, null, bytes);
	}

	/**
	 * Creates a byte blob content body for an HTTP request.
	 * @param contentType the data's content type.
	 * @param contentEncoding the data's encoding type (like gzip or what have you, can be null for none).
	 * @param bytes the byte data.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createByteContent(String contentType, String contentEncoding, byte[] bytes)
	{
		return new BlobContent(contentType, contentEncoding, bytes);
	}

	/**
	 * Creates a file-based content body for an HTTP request.
	 * <p>Note: This is NOT form-data content! See {@link MultipartFormContent} for that.
	 * @param contentType the file's content type.
	 * @param file the file to read from on send.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createFileContent(String contentType, File file)
	{
		return new FileContent(contentType, null, null, file);
	}

	/**
	 * Creates a file-based content body for an HTTP request.
	 * <p>Note: This is NOT form-data content! See {@link MultipartFormContent} for that.
	 * @param contentType the file's content type.
	 * @param encodingType the data encoding type for the file's payload (e.g. "gzip" or "base64").
	 * @param file the file to read from on send.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createFileContent(String contentType, String encodingType, File file)
	{
		return new FileContent(contentType, encodingType, null, file);
	}

	/**
	 * Creates a file-based content body for an HTTP request, presumably a text file.
	 * The default system text encoding is presumed.
	 * <p>Note: This is NOT form-data content! See {@link MultipartFormContent} for that.
	 * @param contentType the file's content type.
	 * @param file the file to read from on send.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createTextFileContent(String contentType, File file)
	{
		return new FileContent(contentType, null, Charset.defaultCharset().name(), file);
	}

	/**
	 * Creates file based content body for an HTTP request, presumably a text file encoded
	 * with a specific charset.
	 * <p>Note: This is NOT form-data content! See {@link MultipartFormContent} for that.
	 * @param contentType the file's content type.
	 * @param charsetType the charset name for the file's payload (e.g. "utf-8" or "ascii").
	 * @param file the file to read from on send.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createTextFileContent(String contentType, String charsetType, File file)
	{
		return new FileContent(contentType, null, charsetType, file);
	}

	/**
	 * Creates a WWW form, URL encoded content body for an HTTP request.
	 * <p>Note: This is NOT mulitpart form-data content! 
	 * See {@link MultipartFormContent} for mixed file attachments and fields.
	 * @param keyValueMap the map of key to value.
	 * @return a content object representing the content.
	 */
	public static HTTPContent createFormContent(HTTPParameters keyValueMap)
	{
		return new FormContent(keyValueMap);
	}

	/**
	 * Creates a WWW form, URL encoded content body for an HTTP request.
	 * <p>Note: This is NOT mulitpart form-data content! 
	 * See {@link MultipartFormContent} for mixed file attachments and fields.
	 * @return a content object representing the content.
	 */
	public static MultipartFormContent createMultipartContent()
	{
		return new MultipartFormContent();
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpGet(url))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static HTTPResponse httpGet(String url) throws IOException
	{
		return httpGet(url, null, null, DEFAULT_TIMEOUT_MILLIS);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpGet(url, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static HTTPResponse httpGet(String url, int socketTimeoutMillis) throws IOException
	{
		return httpGet(url, null, null, socketTimeoutMillis);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpGet(url, headers, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 */
	public static HTTPResponse httpGet(String url, HTTPHeaders headers, int socketTimeoutMillis) throws IOException
	{
		return httpGet(url, headers, null, socketTimeoutMillis);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpGet(url, parameters, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpGet(String url, HTTPParameters parameters, int socketTimeoutMillis) throws IOException
	{
		return httpGet(url, null, parameters, socketTimeoutMillis);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpGet(url, headers, parameters, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpGet(String url, HTTPHeaders headers, HTTPParameters parameters, int socketTimeoutMillis) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_GET, new URL(urlParams(url, parameters)), headers, null, null, socketTimeoutMillis, null);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpGet(String url, HTTPReader<R> reader) throws IOException
	{
		return httpGet(url, null, null, DEFAULT_TIMEOUT_MILLIS, reader);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpGet(String url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpGet(url, null, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 */
	public static <R> R httpGet(String url, HTTPHeaders headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpGet(url, headers, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param parameters the mapping of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 */
	public static <R> R httpGet(String url, HTTPParameters parameters, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpGet(url, null, parameters, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a GET request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static <R> R httpGet(String url, HTTPHeaders headers, HTTPParameters parameters, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_GET, new URL(urlParams(url, parameters)), headers, null, null, socketTimeoutMillis, null, reader);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpHead(url))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static HTTPResponse httpHead(String url) throws IOException
	{
		return httpHead(url, null, null, DEFAULT_TIMEOUT_MILLIS);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpHead(url, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static HTTPResponse httpHead(String url, int socketTimeoutMillis) throws IOException
	{
		return httpHead(url, null, null, socketTimeoutMillis);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpHead(url, headers, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 */
	public static HTTPResponse httpHead(String url, HTTPHeaders headers, int socketTimeoutMillis) throws IOException
	{
		return httpHead(url, headers, null, socketTimeoutMillis);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpHead(url, parameters, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpHead(String url, HTTPParameters parameters, int socketTimeoutMillis) throws IOException
	{
		return httpHead(url, null, parameters, socketTimeoutMillis);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpHead(url, headers, parameters, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpHead(String url, HTTPHeaders headers, HTTPParameters parameters, int socketTimeoutMillis) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_HEAD, new URL(urlParams(url, parameters)), headers, null, null, socketTimeoutMillis, null);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpHead(String url, HTTPReader<R> reader) throws IOException
	{
		return httpHead(url, null, null, DEFAULT_TIMEOUT_MILLIS, reader);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpHead(String url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpHead(url, null, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 */
	public static <R> R httpHead(String url, HTTPHeaders headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpHead(url, headers, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 */
	public static <R> R httpHead(String url, HTTPParameters parameters, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpHead(url, null, parameters, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a HEAD request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static <R> R httpHead(String url, HTTPHeaders headers, HTTPParameters parameters, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_HEAD, new URL(urlParams(url, parameters)), headers, null, null, socketTimeoutMillis, null, reader);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpDelete(url))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static HTTPResponse httpDelete(String url) throws IOException
	{
		return httpDelete(url, null, null, DEFAULT_TIMEOUT_MILLIS);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpDelete(url, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static HTTPResponse httpDelete(String url, int socketTimeoutMillis) throws IOException
	{
		return httpDelete(url, null, null, socketTimeoutMillis);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpDelete(url, headers, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 */
	public static HTTPResponse httpDelete(String url, HTTPHeaders headers, int socketTimeoutMillis) throws IOException
	{
		return httpDelete(url, headers, null, socketTimeoutMillis);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpDelete(url, parameters, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpDelete(String url, HTTPParameters parameters, int socketTimeoutMillis) throws IOException
	{
		return httpDelete(url, null, parameters, socketTimeoutMillis);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpDelete(url, headers, parameters, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpDelete(String url, HTTPHeaders headers, HTTPParameters parameters, int socketTimeoutMillis) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_DELETE, new URL(urlParams(url, parameters)), headers, null, null, socketTimeoutMillis, null);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpDelete(String url, HTTPReader<R> reader) throws IOException
	{
		return httpDelete(url, null, null, DEFAULT_TIMEOUT_MILLIS, reader);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpDelete(String url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpDelete(url, null, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 */
	public static <R> R httpDelete(String url, HTTPHeaders headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpDelete(url, headers, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 */
	public static <R> R httpDelete(String url, HTTPParameters parameters, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpDelete(url, null, parameters, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a DELETE request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static <R> R httpDelete(String url, HTTPHeaders headers, HTTPParameters parameters, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_DELETE, new URL(urlParams(url, parameters)), headers, null, null, socketTimeoutMillis, null, reader);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpOptions(url))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static HTTPResponse httpOptions(String url) throws IOException
	{
		return httpOptions(url, null, null, DEFAULT_TIMEOUT_MILLIS);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpOptions(url, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static HTTPResponse httpOptions(String url, int socketTimeoutMillis) throws IOException
	{
		return httpOptions(url, null, null, socketTimeoutMillis);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpOptions(url, headers, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 */
	public static HTTPResponse httpOptions(String url, HTTPHeaders headers, int socketTimeoutMillis) throws IOException
	{
		return httpOptions(url, headers, null, socketTimeoutMillis);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpOptions(url, parameters, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpOptions(String url, HTTPParameters parameters, int socketTimeoutMillis) throws IOException
	{
		return httpOptions(url, null, parameters, socketTimeoutMillis);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpOptions(url, headers, parameters, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpOptions(String url, HTTPHeaders headers, HTTPParameters parameters, int socketTimeoutMillis) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_OPTIONS, new URL(urlParams(url, parameters)), headers, null, null, socketTimeoutMillis, null);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpOptions(String url, HTTPReader<R> reader) throws IOException
	{
		return httpOptions(url, null, null, DEFAULT_TIMEOUT_MILLIS, reader);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpOptions(String url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpOptions(url, null, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 */
	public static <R> R httpOptions(String url, HTTPHeaders headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpOptions(url, headers, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 */
	public static <R> R httpOptions(String url, HTTPParameters parameters, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpOptions(url, null, parameters, socketTimeoutMillis, reader);
	}

	/**
	 * Sends an OPTIONS request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static <R> R httpOptions(String url, HTTPHeaders headers, HTTPParameters parameters, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_OPTIONS, new URL(urlParams(url, parameters)), headers, null, null, socketTimeoutMillis, null, reader);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpTrace(url))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static HTTPResponse httpTrace(String url) throws IOException
	{
		return httpTrace(url, null, null, DEFAULT_TIMEOUT_MILLIS);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpTrace(url, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static HTTPResponse httpTrace(String url, int socketTimeoutMillis) throws IOException
	{
		return httpTrace(url, null, null, socketTimeoutMillis);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpTrace(url, headers, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 */
	public static HTTPResponse httpTrace(String url, HTTPHeaders headers, int socketTimeoutMillis) throws IOException
	{
		return httpTrace(url, headers, null, socketTimeoutMillis);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpTrace(url, parameters, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpTrace(String url, HTTPParameters parameters, int socketTimeoutMillis) throws IOException
	{
		return httpTrace(url, null, parameters, socketTimeoutMillis);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, headers, parameters, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpTrace(String url, HTTPHeaders headers, HTTPParameters parameters, int socketTimeoutMillis) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_TRACE, new URL(urlParams(url, parameters)), headers, null, null, socketTimeoutMillis, null);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpTrace(String url, HTTPReader<R> reader) throws IOException
	{
		return httpTrace(url, null, null, DEFAULT_TIMEOUT_MILLIS, reader);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 */
	public static <R> R httpTrace(String url, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpTrace(url, null, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 */
	public static <R> R httpTrace(String url, HTTPHeaders headers, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpTrace(url, headers, null, socketTimeoutMillis, reader);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 */
	public static <R> R httpTrace(String url, HTTPParameters parameters, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpTrace(url, null, parameters, socketTimeoutMillis, reader);
	}

	/**
	 * Sends an TRACE request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the map of key to values representing parameters to append as a query string to the URL (can be null).
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static <R> R httpTrace(String url, HTTPHeaders headers, HTTPParameters parameters, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_TRACE, new URL(urlParams(url, parameters)), headers, null, null, socketTimeoutMillis, null, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, content))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpPut(String url, HTTPContent content) throws IOException
	{
		return httpPut(url, null, null, content, DEFAULT_TIMEOUT_MILLIS);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpPut(String url, HTTPContent content, int socketTimeoutMillis) throws IOException
	{
		return httpPut(url, null, null, content, socketTimeoutMillis);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, headers, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpPut(String url, HTTPHeaders headers, HTTPContent content, int socketTimeoutMillis) throws IOException
	{
		return httpPut(url, headers, null, content, socketTimeoutMillis);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, parameters, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpPut(String url, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis) throws IOException
	{
		return httpPut(url, null, parameters, content, socketTimeoutMillis);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, headers, parameters, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpPut(String url, HTTPHeaders headers, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_PUT, new URL(urlParams(url, parameters)), headers, content, null, socketTimeoutMillis, null);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPut(String url, HTTPContent content, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, null, null, content, DEFAULT_TIMEOUT_MILLIS, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPut(String url, HTTPContent content, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, null, null, content, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPut(String url, HTTPHeaders headers, HTTPContent content, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, headers, null, content, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPut(String url, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, null, parameters, content, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPut(String url, HTTPHeaders headers, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_PUT, new URL(urlParams(url, parameters)), headers, content, null, socketTimeoutMillis, null, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, content))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpPut(String url, HTTPContent content, TransferMonitor uploadMonitor) throws IOException
	{
		return httpPut(url, null, null, content, DEFAULT_TIMEOUT_MILLIS, uploadMonitor);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpPut(String url, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor) throws IOException
	{
		return httpPut(url, null, null, content, socketTimeoutMillis, uploadMonitor);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, headers, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpPut(String url, HTTPHeaders headers, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor) throws IOException
	{
		return httpPut(url, headers, null, content, socketTimeoutMillis, uploadMonitor);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, parameters, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPParameters
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpPut(String url, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor) throws IOException
	{
		return httpPut(url, null, parameters, content, socketTimeoutMillis, uploadMonitor);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPut(url, headers, parameters, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpPut(String url, HTTPHeaders headers, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_PUT, new URL(urlParams(url, parameters)), headers, content, null, socketTimeoutMillis, uploadMonitor);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPut(String url, HTTPContent content, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, null, null, content, DEFAULT_TIMEOUT_MILLIS, uploadMonitor, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPut(String url, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, null, null, content, socketTimeoutMillis, uploadMonitor, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPut(String url, HTTPHeaders headers, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, headers, null, content, socketTimeoutMillis, uploadMonitor, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPut(String url, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		return httpPut(url, null, parameters, content, socketTimeoutMillis, uploadMonitor, reader);
	}

	/**
	 * Sends a PUT request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPut(String url, HTTPHeaders headers, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_PUT, new URL(urlParams(url, parameters)), headers, content, null, socketTimeoutMillis, uploadMonitor, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPost(url, content))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpPost(String url, HTTPContent content) throws IOException
	{
		return httpPost(url, null, null, content, DEFAULT_TIMEOUT_MILLIS);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPost(url, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpPost(String url, HTTPContent content, int socketTimeoutMillis) throws IOException
	{
		return httpPost(url, null, null, content, socketTimeoutMillis);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPost(url, headers, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpPost(String url, HTTPHeaders headers, HTTPContent content, int socketTimeoutMillis) throws IOException
	{
		return httpPost(url, headers, null, content, socketTimeoutMillis);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPost(url, parameters, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpPost(String url, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis) throws IOException
	{
		return httpPost(url, null, parameters, content, socketTimeoutMillis);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPost(url, headers, parameters, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpPost(String url, HTTPHeaders headers, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_POST, new URL(urlParams(url, parameters)), headers, content, null, socketTimeoutMillis, null);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPost(String url, HTTPContent content, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, null, null, content, DEFAULT_TIMEOUT_MILLIS, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPost(String url, HTTPContent content, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, null, null, content, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 */
	public static <R> R httpPost(String url, HTTPHeaders headers, HTTPContent content, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, headers, null, content, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 */
	public static <R> R httpPost(String url, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, null, parameters, content, socketTimeoutMillis, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static <R> R httpPost(String url, HTTPHeaders headers, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, headers, parameters, content, socketTimeoutMillis, null, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPost(url, content))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpPost(String url, HTTPContent content, TransferMonitor uploadMonitor) throws IOException
	{
		return httpPost(url, null, null, content, DEFAULT_TIMEOUT_MILLIS, uploadMonitor);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPost(url, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpPost(String url, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor) throws IOException
	{
		return httpPost(url, null, null, content, socketTimeoutMillis, uploadMonitor);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPost(url, headers, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpPost(String url, HTTPHeaders headers, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor) throws IOException
	{
		return httpPost(url, headers, null, content, socketTimeoutMillis, uploadMonitor);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPost(url, parameters, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpPost(String url, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor) throws IOException
	{
		return httpPost(url, null, parameters, content, socketTimeoutMillis, uploadMonitor);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = httpPost(url, headers, parameters, content, socketTimeoutMillis))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static HTTPResponse httpPost(String url, HTTPHeaders headers, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_POST, new URL(urlParams(url, parameters)), headers, content, null, socketTimeoutMillis, uploadMonitor);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPost(String url, HTTPContent content, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, null, null, content, DEFAULT_TIMEOUT_MILLIS, uploadMonitor, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpPost(String url, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, null, null, content, socketTimeoutMillis, uploadMonitor, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 */
	public static <R> R httpPost(String url, HTTPHeaders headers, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, headers, null, content, socketTimeoutMillis, uploadMonitor, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 */
	public static <R> R httpPost(String url, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		return httpPost(url, null, parameters, content, socketTimeoutMillis, uploadMonitor, reader);
	}

	/**
	 * Sends a POST request to an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the return type.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param parameters the optional set of parameters to add to the URL (can be null for no parameters).
	 * @param content if not null, add this content to the body. Otherwise, the body will be empty.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the content from opening an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 * @see HTTPHeaders
	 * @see HTTPParameters
	 */
	public static <R> R httpPost(String url, HTTPHeaders headers, HTTPParameters parameters, HTTPContent content, int socketTimeoutMillis, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		parameters = parameters == null ? NO_PARAMETERS : parameters;
		return httpFetch(HTTP_METHOD_POST, new URL(urlParams(url, parameters)), headers, content, null, socketTimeoutMillis, uploadMonitor, reader);
	}

	/**
	 * Gets the content from a opening an HTTP URL.
	 * The response is encapsulated and returned, with an open input stream to read from the body of the return.
	 * If the response/stream is closed afterward, it will not close the connection (which may be pooled).
	 * <p>
	 * Since the {@link HTTPResponse} auto-closes, the best way to use this method is with a try-with-resources call, a la:
	 * <pre><code>
	 * try (HTTPResponse response = getHTTPContent(requestMethod, url, headers, content, defaultResponseCharset, socketTimeoutMillis, uploadMonitor))
	 * {
	 *     // ... read response ...
	 * }
	 * </code></pre>
	 * @param requestMethod the request method.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param content if not null, add this content to the body.
	 * @param defaultResponseCharset if the response charset is not specified, use this one.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @return the response from an HTTP request.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @throws ProtocolException if the requestMethod is incorrect, or not an HTTP URL.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static HTTPResponse httpFetch(String requestMethod, URL url, HTTPHeaders headers, HTTPContent content, String defaultResponseCharset, int socketTimeoutMillis, TransferMonitor uploadMonitor) throws IOException
	{
		Objects.requireNonNull(requestMethod, "request method is null");
		
		if (Arrays.binarySearch(VALID_HTTP, url.getProtocol()) < 0)
			throw new ProtocolException("This is not an HTTP URL.");
	
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setReadTimeout(socketTimeoutMillis);
		conn.setRequestMethod(requestMethod);
		
		if (headers != null) for (Map.Entry<String, String> entry : headers.map.entrySet())
			conn.setRequestProperty(entry.getKey(), entry.getValue());
	
		// set up body data.
		if (content != null)
		{
			long uploadLen = content.getLength();
			conn.setFixedLengthStreamingMode(content.getLength());
			conn.setRequestProperty("Content-Type", content.getContentType() == null ? "application/octet-stream" : content.getContentType());
			if (content.getEncoding() != null)
				conn.setRequestProperty("Content-Encoding", content.getEncoding());
			conn.setDoOutput(true);
			try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream()))
			{
				relay(content.getInputStream(), dos, 8192, uploadLen, uploadMonitor);
			}
		}
	
		HTTPResponse response = new HTTPResponse();
		response.statusCode = conn.getResponseCode();
		response.statusMessage = conn.getResponseMessage();
		int conlen = conn.getContentLength();
		response.length = conlen < 0 ? null : (long)conlen;
		response.encoding = conn.getContentEncoding();
		response.contentTypeHeader = conn.getContentType();

		int mimeEnd = response.contentTypeHeader.indexOf(';');
		
		response.contentType = response.contentTypeHeader.substring(0, mimeEnd >= 0 ? mimeEnd : response.contentTypeHeader.length()).trim();
		
		int charsetindex;
		if ((charsetindex = response.contentTypeHeader.toLowerCase().indexOf("charset=")) >= 0)
		{
			int endIndex = response.contentTypeHeader.indexOf(";", charsetindex);
			if (endIndex >= 0)
				response.charset = response.contentTypeHeader.substring(charsetindex + "charset=".length(), endIndex).trim();
			else
				response.charset = response.contentTypeHeader.substring(charsetindex + "charset=".length()).trim();
			if (response.charset.startsWith("\"")) // remove surrounding quotes
				response.charset = response.charset.substring(1, response.charset.length() - 1); 
		}
		
		if (response.charset == null)
			response.charset = defaultResponseCharset;
		
		response.headers = conn.getHeaderFields();
		
		// content disposition?
		if ((response.contentDisposition = conn.getHeaderField("content-disposition")) != null)
		{
			int fileNameIndex;
			if ((fileNameIndex = response.contentDisposition.toLowerCase().indexOf("filename=")) >= 0)
			{
				int endIndex = response.contentDisposition.indexOf(";", fileNameIndex);
				if (endIndex >= 0)
					response.filename = response.contentDisposition.substring(fileNameIndex + "filename=".length(), endIndex).trim();
				else
					response.filename = response.contentDisposition.substring(fileNameIndex + "filename=".length()).trim();
				if (response.filename.startsWith("\"")) // remove surrounding quotes
					response.filename = response.filename.substring(1, response.filename.length() - 1); 
			}
		}
		
		if (response.statusCode >= 400) 
			response.contentStream = conn.getErrorStream();
		else 
			response.contentStream = conn.getInputStream();
		
		return response;
	}
	
	/**
	 * Gets the content from a opening an HTTP URL.
	 * The stream is closed afterward (but not the connection, which may be pooled).
	 * @param <R> the resultant value read from the response.
	 * @param requestMethod the request method.
	 * @param url the URL to open and read.
	 * @param headers a map of header to header value to add to the request (can be null for no headers).
	 * @param content if not null, add this content to the body.
	 * @param defaultResponseCharset if the response charset is not specified, use this one.
	 * @param socketTimeoutMillis the socket timeout time in milliseconds. 0 is forever.
	 * @param uploadMonitor an optional callback during upload for upload progress. Can be null.
	 * @param reader the reader to use to read the response and return the data in a useful shape.
	 * @return the read content from an HTTP request, via the provided reader.
	 * @throws IOException if an error happens during the read/write.
	 * @throws SocketTimeoutException if the socket read times out.
	 * @throws ProtocolException if the requestMethod is incorrect, or not an HTTP URL.
	 * @see HTTPHeaders
	 * @see #createByteContent(String, byte[])
	 * @see #createByteContent(String, String, byte[])
	 * @see #createTextContent(String, String)
	 * @see #createFileContent(String, File)
	 * @see #createFileContent(String, String, File)
	 * @see #createFormContent(HTTPParameters)
	 * @see #createMultipartContent()
	 */
	public static <R> R httpFetch(String requestMethod, URL url, HTTPHeaders headers, HTTPContent content, String defaultResponseCharset, int socketTimeoutMillis, TransferMonitor uploadMonitor, HTTPReader<R> reader) throws IOException
	{
		try (HTTPResponse response = httpFetch(requestMethod, url, headers, content, defaultResponseCharset, socketTimeoutMillis, uploadMonitor))
		{
			return reader.onHTTPResponse(response);
		}
	}

	private static final char[] HEX_NYBBLE = "0123456789ABCDEF".toCharArray();

	private static void writePercentChar(StringBuilder target, byte b)
	{
		target.append('%');
		target.append(HEX_NYBBLE[(b & 0x0f0) >> 4]);
		target.append(HEX_NYBBLE[b & 0x00f]);
	}
	
	private static String toURLEncoding(String s)
	{
		StringBuilder sb = new StringBuilder();
		byte[] bytes = s.getBytes(UTF8);
		for (int i = 0; i < s.length(); i++)
		{
			byte b = bytes[i];
			if (Arrays.binarySearch(URL_UNRESERVED, b) >= 0)
				sb.append((char)b);
			else if (Arrays.binarySearch(URL_RESERVED, b) >= 0)
				writePercentChar(sb, b);
			else
				writePercentChar(sb, b);
		}
		return sb.toString();
	}
	
	private static String urlParams(String url, HTTPParameters params)
	{
		return url + (params.map.isEmpty() ? "" : (url.indexOf('?') >= 0 ? '&' : '?') + params.toString()); 
	}
	
	/**
	 * Reads from an input stream, reading in a consistent set of data
	 * and writing it to the output stream. The read/write is buffered
	 * so that it does not bog down the OS's other I/O requests.
	 * This method finishes when the end of the source stream is reached.
	 * Note that this may block if the input stream is a type of stream
	 * that will block if the input stream blocks for additional input.
	 * This method is thread-safe.
	 * @param in the input stream to grab data from.
	 * @param out the output stream to write the data to.
	 * @param bufferSize the buffer size for the I/O. Must be &gt; 0.
	 * @param maxLength the maximum amount of bytes to relay, or null for no max.
	 * @param monitor the transfer monitor to call on changes.
	 * @return the total amount of bytes relayed.
	 * @throws IOException if a read or write error occurs.
	 */
	private static long relay(InputStream in, OutputStream out, int bufferSize, Long maxLength, TransferMonitor monitor) throws IOException
	{
		long total = 0;
		int buf = 0;
			
		byte[] RELAY_BUFFER = new byte[bufferSize];
		
		while ((buf = in.read(RELAY_BUFFER, 0, Math.min(maxLength == null ? Integer.MAX_VALUE : (int)Math.min(maxLength, Integer.MAX_VALUE), bufferSize))) > 0)
		{
			out.write(RELAY_BUFFER, 0, buf);
			total += buf;
			if (monitor != null)
				monitor.onProgressChange(total, maxLength);
			if (maxLength != null && maxLength >= 0)
				maxLength -= buf;
		}
		return total;
	}
	
	/**
	 * Attempts to close an {@link AutoCloseable} object.
	 * If the object is null, this does nothing.
	 * @param c the reference to the AutoCloseable object.
	 */
	private static void close(AutoCloseable c)
	{
		if (c == null) return;
		try { c.close(); } catch (Exception e){}
	}

}
