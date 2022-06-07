/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Simple encoding utility functions.
 * @author Matthew Tropiano
 */
public final class EncodingUtils
{
	private EncodingUtils() {}

	/**
	 * Returns a hash of a set of bytes digested by an encryption algorithm.
	 * Can return null if this Java implementation cannot perform this.
	 * Do not use this if you care if the algorithm is provided or not.
	 * @param bytes the bytes to encode.
	 * @param algorithmName the name to the algorithm to use.
	 * @return the resultant byte digest, or null if the algorithm is not supported.
	 */
	public static byte[] digest(byte[] bytes, String algorithmName)
	{
		try {
			return MessageDigest.getInstance(algorithmName).digest(bytes);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	/**
	 * Returns a 20-byte SHA-1 hash of a set of bytes.
	 * Can return null if this Java implementation cannot perform this,
	 * but it shouldn't, since SHA-1 is mandatorily implemented for all implementations.
	 * @param bytes the input bytes.
	 * @return the resultant 20-byte digest.
	 * @see #digest(byte[], String)
	 */
	public static byte[] sha1(byte[] bytes)
	{
		return digest(bytes, "SHA-1");
	}

	/**
	 * Returns a 16-byte MD5 hash of a set of bytes.
	 * Can return null if this Java implementation cannot perform this,
	 * but it shouldn't, since MD5 is mandatorily implemented for all implementations.
	 * @param bytes the input bytes.
	 * @return the resultant 16-byte digest.
	 * @see #digest(byte[], String)
	 */
	public static byte[] md5(byte[] bytes)
	{
		return digest(bytes, "MD5");
	}

	private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final char BLANK = '=';
	
	/**
	 * Encodes a String as a Base64 encoded string, using the default platform encoding.
	 * Uses + and / as characters 62 and 63.
	 * @param input the input string to convert to Base64.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 */
	public static String asBase64(String input)
	{
		return asBase64(input, Charset.defaultCharset(), '+', '/');
	}

	/**
	 * Encodes a String as a Base64 encoded string.
	 * Uses + and / as characters 62 and 63.
	 * @param input the input string to convert to Base64.
	 * @param charset the Charset to encode the string with before Base64 encode.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 */
	public static String asBase64(String input, Charset charset)
	{
		return asBase64(input, charset, '+', '/');
	}

	/**
	 * Encodes a series of bytes as a Base64 encoded string, using the default platform encoding.
	 * @param input the input string to convert to Base64.
	 * @param sixtyTwo the character to use for character 62 in the Base64 index.
	 * @param sixtyThree the character to use for character 63 in the Base64 index.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 */
	public static String asBase64(String input, char sixtyTwo, char sixtyThree)
	{
		return asBase64(input, Charset.defaultCharset(), sixtyTwo, sixtyThree);
	}
	
	/**
	 * Encodes a series of bytes as a Base64 encoded string.
	 * @param input the input string to convert to Base64.
	 * @param charset the Charset to encode the string with before Base64 encode.
	 * @param sixtyTwo the character to use for character 62 in the Base64 index.
	 * @param sixtyThree the character to use for character 63 in the Base64 index.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 */
	public static String asBase64(String input, Charset charset, char sixtyTwo, char sixtyThree)
	{
		try (InputStream in = new ByteArrayInputStream(input.getBytes(charset)))
		{
			return asBase64(in, sixtyTwo, sixtyThree);
		}
		catch (IOException e)
		{
			return null;
		}
	}
	
	/**
	 * Encodes a series of bytes as a Base64 encoded string.
	 * Uses + and / as characters 62 and 63.
	 * @param data the bytes to read to convert to Base64.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 * @throws IOException if the input stream cannot be read.
	 */
	public static String asBase64(byte[] data) throws IOException
	{
		return asBase64(data, 0, data.length, '+', '/');
	}

	/**
	 * Encodes a series of bytes as a Base64 encoded string.
	 * @param data the bytes to read to convert to Base64.
	 * @param sixtyTwo the character to use for character 62 in the Base64 index.
	 * @param sixtyThree the character to use for character 63 in the Base64 index.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 * @throws IOException if the input stream cannot be read.
	 */
	public static String asBase64(byte[] data, char sixtyTwo, char sixtyThree) throws IOException
	{
		return asBase64(data, 0, data.length, sixtyTwo, sixtyThree);
	}
	
	/**
	 * Encodes a series of bytes as a Base64 encoded string.
	 * Uses + and / as characters 62 and 63.
	 * @param data the bytes to read to convert to Base64.
	 * @param offset the offset into the array to start read.
	 * @param length the maximum amount of bytes to read.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 * @throws IOException if the input stream cannot be read.
	 */
	public static String asBase64(byte[] data, int offset, int length) throws IOException
	{
		return asBase64(data, offset, length, '+', '/');
	}

	/**
	 * Encodes a series of bytes as a Base64 encoded string.
	 * @param data the bytes to read to convert to Base64.
	 * @param offset the offset into the array to start read.
	 * @param length the maximum amount of bytes to read.
	 * @param sixtyTwo the character to use for character 62 in the Base64 index.
	 * @param sixtyThree the character to use for character 63 in the Base64 index.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 * @throws IOException if the input stream cannot be read.
	 */
	public static String asBase64(byte[] data, int offset, int length, char sixtyTwo, char sixtyThree) throws IOException
	{
		try (InputStream in = new ByteArrayInputStream(data, offset, length))
		{
			return asBase64(in, sixtyTwo, sixtyThree);
		}
	}
	
	/**
	 * Encodes a series of bytes as a Base64 encoded string.
	 * The input stream is read to the end, and is not closed after read.
	 * Uses + and / as characters 62 and 63.
	 * @param in the input stream to read to convert to Base64.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 * @throws IOException if the input stream cannot be read.
	 */
	public static String asBase64(InputStream in) throws IOException
	{
		return asBase64(in, '+', '/');
	}

	/**
	 * Encodes a series of bytes as a Base64 encoded string.
	 * The input stream is read to the end, and is not closed after read.
	 * @param in the input stream to read to convert to Base64.
	 * @param sixtyTwo the character to use for character 62 in the Base64 index.
	 * @param sixtyThree the character to use for character 63 in the Base64 index.
	 * @return a String of encoded bytes, or null if the message could not be encoded.
	 * @throws IOException if the input stream cannot be read.
	 */
	public static String asBase64(InputStream in, char sixtyTwo, char sixtyThree) throws IOException
	{
		final String alph = (new StringBuilder(ALPHABET.length() + 2))
			.append(ALPHABET)
			.append(sixtyTwo)
			.append(sixtyThree)
		.toString();
		
		StringBuilder out = new StringBuilder();
		int octetBuffer = 0x00000000;
		int bidx = 0;
		
		byte[] buffer = new byte[16384];
		int buf = 0;
		
		while ((buf = in.read(buffer)) > 0) for (int i = 0; i < buf; i++)
		{
			byte b = buffer[i];
			
			octetBuffer |= ((b & 0x0ff) << ((2 - bidx) * 8));
			bidx++;
			if (bidx == 3)
			{
				out.append(alph.charAt((octetBuffer & (0x3f << 18)) >> 18));
				out.append(alph.charAt((octetBuffer & (0x3f << 12)) >> 12));
				out.append(alph.charAt((octetBuffer & (0x3f << 6)) >> 6));
				out.append(alph.charAt(octetBuffer & 0x3f));
				octetBuffer = 0x00000000;
				bidx = 0;
			}
		}
		
		if (bidx == 2)
		{
			out.append(alph.charAt((octetBuffer & (0x3f << 18)) >> 18));
			out.append(alph.charAt((octetBuffer & (0x3f << 12)) >> 12));
			out.append(alph.charAt((octetBuffer & (0x3f << 6)) >> 6));
			out.append(BLANK);
		}
		else if (bidx == 1)
		{
			out.append(alph.charAt((octetBuffer & (0x3f << 18)) >> 18));
			out.append(alph.charAt((octetBuffer & (0x3f << 12)) >> 12));
			out.append(BLANK);
			out.append(BLANK);
		}
		
		return out.toString();
	}

	/**
	 * Decodes a Base64 message to bytes.
	 * Uses + and / as characters 62 and 63.
	 * @param base64Input the input Base64 message.
	 * @return the output decoded bytes from the string.
	 * @throws IllegalArgumentException if <code>input</code> contains an invalid or unexpected character.
	 */
	public static byte[] fromBase64(String base64Input)
	{
		return fromBase64(base64Input, '+', '/');
	}
	
	/**
	 * Decodes a Base64 message to bytes.
	 * @param base64Input the input Base64 message.
	 * @param sixtyTwo the character to use for character 62 in the Base64 index.
	 * @param sixtyThree the character to use for character 63 in the Base64 index.
	 * @return the output decoded bytes from the string.
	 * @throws IllegalArgumentException if <code>input</code> contains an invalid or unexpected character.
	 */
	public static byte[] fromBase64(String base64Input, char sixtyTwo, char sixtyThree)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(base64Input.length() * 2);
		int bitSet = 0;
		int buffer = 0; // only use 24 bits
		
		for (int i = 0; i < base64Input.length(); i++)
		{
			char c = base64Input.charAt(i);
			int value;
			if (c == sixtyTwo)
				value = 62;
			else if (c == sixtyThree)
				value = 63;
			else if (c == BLANK) // assume end
				break;
			else if ((value = ALPHABET.indexOf(c)) < 0) // TODO: Improve. This is sequential search.
				throw new IllegalArgumentException("input contained invalid character: " + c);
			
			buffer |= (value & 0x0000003f) << (18 - (bitSet * 6));
			if ((++bitSet) == 4) // flush buffer
			{
				bos.write((buffer & 0x00ff0000) >>> 16);
				bos.write((buffer & 0x0000ff00) >>> 8);
				bos.write(buffer  & 0x000000ff);
				buffer = 0;
				bitSet = 0;
			}
		}
		
		if (bitSet == 3)
		{
			bos.write((buffer & 0x00ff0000) >>> 16);
			bos.write((buffer & 0x0000ff00) >>> 8);
		}
		else if (bitSet == 2)
		{
			bos.write((buffer & 0x00ff0000) >>> 16);
		}

		// I don't think I need a "bitSet == 1" check due to the math, but I could be wrong
		
		return bos.toByteArray();
	}
	
	/**
	 * Decodes a Base64 message to a new String, using the default platform encoding and resultant bytes from decode.
	 * Uses + and / as characters 62 and 63.
	 * @param base64Input the input Base64 message.
	 * @return the decoded string.
	 * @throws IllegalArgumentException if <code>input</code> contains an invalid or unexpected character.
	 * @see #fromBase64(String, char, char)
	 * @see String#String(byte[], Charset)
	 */
	public static String fromBase64ToString(String base64Input)
	{
		return fromBase64ToString(base64Input, Charset.defaultCharset(), '+', '/');
	}
	
	/**
	 * Decodes a Base64 message to a new String, using the resultant bytes from decode.
	 * Uses + and / as characters 62 and 63.
	 * @param base64Input the input Base64 message.
	 * @param charset the Charset to decode the Base64-decoded bytes with.
	 * @return the decoded string.
	 * @throws IllegalArgumentException if <code>input</code> contains an invalid or unexpected character.
	 * @see #fromBase64(String, char, char)
	 * @see String#String(byte[], Charset)
	 */
	public static String fromBase64ToString(String base64Input, Charset charset)
	{
		return fromBase64ToString(base64Input, charset, '+', '/');
	}
	
	/**
	 * Decodes a Base64 message to a new String, using the default platform encoding and resultant bytes from decode.
	 * @param base64Input the input Base64 message.
	 * @param sixtyTwo the character to use for character 62 in the Base64 index.
	 * @param sixtyThree the character to use for character 63 in the Base64 index.
	 * @return the decoded string.
	 * @throws IllegalArgumentException if <code>input</code> contains an invalid or unexpected character.
	 * @see #fromBase64(String, char, char)
	 * @see String#String(byte[], Charset)
	 */
	public static String fromBase64ToString(String base64Input, char sixtyTwo, char sixtyThree)
	{
		return fromBase64ToString(base64Input, Charset.defaultCharset(), sixtyTwo, sixtyThree);
	}
	
	/**
	 * Decodes a Base64 message to a new String, using the resultant bytes from decode.
	 * @param base64Input the input Base64 message.
	 * @param charset the Charset to decode the Base64-decoded bytes with.
	 * @param sixtyTwo the character to use for character 62 in the Base64 index.
	 * @param sixtyThree the character to use for character 63 in the Base64 index.
	 * @return the decoded string.
	 * @throws IllegalArgumentException if <code>input</code> contains an invalid or unexpected character.
	 * @see #fromBase64(String, char, char)
	 * @see String#String(byte[], Charset)
	 */
	public static String fromBase64ToString(String base64Input, Charset charset, char sixtyTwo, char sixtyThree)
	{
		return new String(fromBase64(base64Input, sixtyTwo, sixtyThree), charset);
	}
	
	/**
	 * Checks if a string can be encoded as the target charset without loss of data or substitutions.
	 * @param content the content to encode.
	 * @param charset the target charset.
	 * @return true if so, false if not.
	 * @see Charset#newEncoder()
	 * @see CharsetEncoder#canEncode(CharSequence)
	 */
	public static boolean canEncodeAs(String content, Charset charset)
	{
		return charset.newEncoder().canEncode(content);
	}
	
	/**
	 * Gets a GZipped series of bytes from input.
	 * @param data the bytes to read.
	 * @return the resultant GZipped bytes.
	 * @throws IOException if a read error occurs.
	 */
	public static byte[] gzipBytes(byte[] data) throws IOException
	{
		return gzipBytes(data, 0, data.length);
	}
	
	/**
	 * Gets a GZipped series of bytes from input.
	 * @param data the bytes to read.
	 * @param offset the offset into the array to start read.
	 * @param length the maximum amount of bytes to read.
	 * @return the resultant GZipped bytes.
	 * @throws IOException if a read error occurs.
	 */
	public static byte[] gzipBytes(byte[] data, int offset, int length) throws IOException
	{
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data, offset, length))
		{
			return gzipBytes(bis);
		}
	}
	
	/**
	 * Gets a GZipped series of bytes from input.
	 * The input stream is read to the end, and is not closed after read.
	 * @param in the input stream.
	 * @return the resultant GZipped bytes.
	 * @throws IOException if a read error occurs.
	 */
	public static byte[] gzipBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
		try (GZIPOutputStream gzout = new GZIPOutputStream(bos))
		{
			relay(in, gzout, 2048);
		}
		return bos.toByteArray();
	}
	
	/**
	 * Unzips a series of bytes from input assumed to be GZipped.
	 * @param data the bytes to read.
	 * @return the resultant unzipped bytes.
	 * @throws IOException if a read error occurs.
	 */
	public static byte[] gunzipBytes(byte[] data) throws IOException
	{
		return gunzipBytes(data, 0, data.length);
	}
	
	/**
	 * Unzips a series of bytes from input assumed to be GZipped.
	 * @param data the bytes to read.
	 * @param offset the offset into the array to start read.
	 * @param length the maximum amount of bytes to read.
	 * @return the resultant unzipped bytes.
	 * @throws IOException if a read error occurs.
	 */
	public static byte[] gunzipBytes(byte[] data, int offset, int length) throws IOException
	{
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data, offset, length))
		{
			return gunzipBytes(bis);
		}
	}
	
	/**
	 * Unzips a series of bytes from input assumed to be GZipped.
	 * The input stream is read to the end, and is not closed after read.
	 * @param in the input stream.
	 * @return the resultant unzipped bytes.
	 * @throws IOException if a read error occurs.
	 */
	public static byte[] gunzipBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
		try (GZIPInputStream gzin = new GZIPInputStream(in))
		{
			relay(gzin, bos, 2048);
		}
		return bos.toByteArray();
	}
	
	/**
	 * Returns a new byte array, delta-encoded.<br>Use with deltaDecode() to decode.
	 * <p>After the first byte, the change in value from the last byte is stored.
	 * <br>For example, a byte sequence <code>[64,92,-23,33]</code> would be returned
	 * as: <code>[64,28,-115,56]</code>.
	 * @param b the input bytestring.
	 * @return the output bytestring.
	 */
	public static byte[] deltaEncode(byte[] b)
	{
		byte[] delta = new byte[b.length];
		delta[0] = b[0];
		for (int i = 1; i < b.length; i++)
			delta[i] = (byte)(b[i] - b[i-1]);
		
		return delta;
	}

	/**
	 * Returns a new byte array, delta-decoded.<br>Decodes sequences made with deltaEncode().
	 * <p>After the first byte, the change in value from the last byte is used to get the original value.
	 * <br>For example, a byte sequence <code>[64,28,-115,56]</code> would be returned
	 * as: <code>[64,92,-23,33]</code>.
	 * @param b the input bytestring.
	 * @return the output bytestring.
	 */
	public static byte[] deltaDecode(byte[] b)
	{
		byte[] delta = /*decompressBytes(b)*/ b;
		
		byte[] out = new byte[delta.length];
		out[0] = delta[0];
		for (int i = 1; i < b.length; i++)
			out[i] = (byte)(out[i-1] + delta[i]);
		
		return out;
	}

	/**
	 * Returns a new byte array, Carmacized.
	 * Named after John D. Carmack, this will compress a sequence
	 * of bytes known to be alike in contiguous sequences.
	 * @param b the input bytestring.
	 * @return the output bytestring.
	 */
	public static byte[] carmacize(byte[] b)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int seq = 1;
		byte prev = b[0];
	
		for(int i = 1; i < b.length; i++)
		{
			if (b[i] == prev)
				seq++;
			else if (prev == -1 || seq > 3)
			{
				while (seq > 0)
				{
					bos.write(255);
					bos.write(prev);
					bos.write(seq > 255 ? 255 : seq);
					seq -= 255;
				}
				prev = b[i];
				seq = 1;
			}
			else
			{
				for (int x = 0; x < seq; x++)
					bos.write(prev);
				prev = b[i];
				seq = 1;
			}
		}
	
		if (seq > 3)
			while (seq > 0)
			{
				bos.write(255);	
				bos.write(prev);
				bos.write(seq > 255 ? 255 : seq);
				seq -= 255;
			}
		else
			for (int x = 0; x < seq; x++)
				bos.write(prev);
		
		return bos.toByteArray(); 
	}

	/**
	 * Returns a Camacized byte array, de-Carmacized.
	 * Named after John D. Carmack, this will decompress a series of
	 * bytes encoded in the Carmacizing algorithm.
	 * @param b the input bytestring.
	 * @return the output bytestring.
	 */
	public static byte[] decarmacize(byte[] b)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (int i = 0; i < b.length; i++)
		{
			if (b[i] == -1)
			{
				i++;
				byte z = b[i];
				i++;
				int x = b[i] < 0? b[i] + 256 : b[i];
				for (int j = 0; j < x; j++)
					bos.write(z);
			}
			else
				bos.write(b[i]);
		}
		return bos.toByteArray();
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
	 * @return the total amount of bytes relayed.
	 * @throws IOException if a read or write error occurs.
	 */
	private static int relay(InputStream in, OutputStream out, int bufferSize) throws IOException
	{
		int total = 0;
		int buf = 0;
			
		byte[] RELAY_BUFFER = new byte[bufferSize];
		
		while ((buf = in.read(RELAY_BUFFER)) > 0)
		{
			out.write(RELAY_BUFFER, 0, buf);
			total += buf;
		}
		out.flush();
		return total;
	}

}
