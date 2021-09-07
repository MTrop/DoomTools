/*******************************************************************************
 * Copyright (c) 2020 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.File;
import java.io.FileInputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

/**
 * Stack-based HTML document writer.
 * All calls to {@link Writer} methods convert special characters
 * @author Matthew Tropiano
 */
public class HTMLWriter implements Flushable, AutoCloseable
{
	private static final Attribute[] NO_ATTRIBUTES = new Attribute[0];
	private static final ThreadLocal<char[]> CHARBUFFER = ThreadLocal.withInitial(()->new char[4096]);
	private static final ThreadLocal<String[]> SINGLE_STRING = ThreadLocal.withInitial(()->new String[1]);
	private static final HashMap<Character, String> UNICODE_ENTITY_MAP = new HashMap<Character, String>()
	{
		private static final long serialVersionUID = -8209253780919474200L;
		{
			put('\u00f6',"ouml");
			put('\u03a3',"Sigma");
			put('\u00b0',"deg");
			put('\u00f4',"ocirc");
			put('\u0392',"Beta");
			put('\u00e5',"aring");
			put('\u2660',"spades");
			put('\u03b9',"iota");
			put('\u039b',"Lambda");
			put('\u03a0',"Pi");
			put('\u201a',"sbquo");
			put('\u00d6',"Ouml");
			put('\u03c3',"sigma");
			put('\u2286',"sube");
			put('\u232a',"rang");
			put('\u2044',"frasl");
			put('\u0399',"Iota");
			put('\u00d1',"Ntilde");
			put('\u2261',"equiv");
			put('\u03bb',"lambda");
			put('\u00bb',"raquo");
			put('\u222a',"cup");
			put('\u00fc',"uuml");
			put('\u2282',"sub");
			put('\u00a7',"sect");
			put('\u00b4',"acute");
			put('\u00a1',"iexcl");
			put('\u2211',"sum");
			put('\u2283',"sup");
			put('\u00eb',"euml");
			put('\u201c',"ldquo");
			put('\u21d0',"lArr");
			put('\u00f1',"ntilde");
			put('\u03d2',"upsih");
			put('\u00dc',"Uuml");
			put('\u2033',"Prime");
			put('\u00c0',"Agrave");
			put('\u00cd',"Iacute");
			put('\u00e3',"atilde");
			put('\u2248',"asymp");
			put('\u00cb',"Euml");
			put('\u2212',"minus");
			put('\u00dd',"Yacute");
			put('\u2190',"larr");
			put('\u2020',"dagger");
			put('\u2309',"rceil");
			put('\u2663',"clubs");
			put('\u0398',"Theta");
			put('\u2032',"prime");
			put('\u00c7',"Ccedil");
			put('\u03a7',"Chi");
			put('\u00ed',"iacute");
			put('\u03c5',"upsilon");
			put('\u2295',"oplus");
			put('\u00a3',"pound");
			put('\u21d2',"rArr");
			put('\u00ae',"reg");
			put('\u00fd',"yacute");
			put('\u03b5',"epsilon");
			put('\u03b8',"theta");
			put('\u0192',"fnof");
			put('\u00c1',"Aacute");
			put('\u00cc',"Igrave");
			put('\u03c7',"chi");
			put('\u03a8',"Psi");
			put('\u03bf',"omicron");
			put('\u00d3',"Oacute");
			put('\u2192',"rarr");
			put('\u00df',"szlig");
			put('\u00ad',"shy");
			put('\u00b5',"micro");
			put('\u00ec',"igrave");
			put('\u2207',"nabla");
			put('\u2135',"alefsym");
			put('\u03c8',"psi");
			put('\u221e',"infin");
			put('\u2002',"ensp");
			put('\u00f3',"oacute");
			put('\u200d',"zwj");
			put('\u00ac',"not");
			put('\u00e4',"auml");
			put('\u00a8',"uml");
			put('\u223c',"sim");
			put('\u00c2',"Acirc");
			put('\u03b6',"zeta");
			put('\u03a1',"Rho");
			put('\u21d4',"hArr");
			put('\u00ca',"Ecirc");
			put('\u2026',"hellip");
			put('\u2265',"ge");
			put('\u00c8',"Egrave");
			put('\u00ce',"Icirc");
			put('\u230a',"lfloor");
			put('\u00a5',"yen");
			put('\u203e',"oline");
			put('\u00b8',"cedil");
			put('\u00d2',"Ograve");
			put('\u2019',"rsquo");
			put('\u00db',"Ucirc");
			put('\u003e',"gt");
			put('\u20ac',"euro");
			put('\u00da',"Uacute");
			put('\u03be',"xi");
			put('\u00c4',"Auml");
			put('\u22a5',"perp");
			put('\u0152',"OElig");
			put('\u0153',"oelig");
			put('\u2203',"exist");
			put('\u00e2',"acirc");
			put('\u0396',"Zeta");
			put('\u03c1',"rho");
			put('\u2194',"harr");
			put('\u00ea',"ecirc");
			put('\u00ee',"icirc");
			put('\u2013',"ndash");
			put('\u00e9',"eacute");
			put('\u00af',"macr");
			put('\u2003',"emsp");
			put('\u0022',"quot");
			put('\u00f2',"ograve");
			put('\u00fb',"ucirc");
			put('\u00a0',"nbsp");
			put('\u00fa',"uacute");
			put('\u039e',"Xi");
			put('\u2284',"nsub");
			put('\u2022',"bull");
			put('\u2665',"hearts");
			put('\u02c6',"circ");
			put('\u00c9',"Eacute");
			put('\u03c2',"sigmaf");
			put('\u2666',"diams");
			put('\u2245',"cong");
			put('\u230b',"rfloor");
			put('\u039a',"Kappa");
			put('\u2039',"lsaquo");
			put('\u00d9',"Ugrave");
			put('\u00be',"frac34");
			put('\u00ab',"laquo");
			put('\u2209',"notin");
			put('\u039f',"Omicron");
			put('\u0391',"Alpha");
			put('\u00e8',"egrave");
			put('\u03a9',"Omega");
			put('\u00b1',"plusmn");
			put('\u00d7',"times");
			put('\u201e',"bdquo");
			put('\u0395',"Epsilon");
			put('\u2009',"thinsp");
			put('\u03ba',"kappa");
			put('\u2205',"empty");
			put('\u00f9',"ugrave");
			put('\u03a6',"Phi");
			put('\u2217',"lowast");
			put('\u220f',"prod");
			put('\u25ca',"loz");
			put('\u02dc',"tilde");
			put('\u201d',"rdquo");
			put('\u03a5',"Upsilon");
			put('\u2111',"image");
			put('\u21d3',"dArr");
			put('\u03b1',"alpha");
			put('\u03c9',"omega");
			put('\u221d',"prop");
			put('\u2122',"trade");
			put('\u03d1',"thetasym");
			put('\u00bc',"frac14");
			put('\u00bd',"frac12");
			put('\u03c6',"phi");
			put('\u2308',"lceil");
			put('\u00d8',"Oslash");
			put('\u0397',"Eta");
			put('\u200f',"rlm");
			put('\u00a9',"copy");
			put('\u203a',"rsaquo");
			put('\u00d0',"ETH");
			put('\u2264',"le");
			put('\u00e1',"aacute");
			put('\u2193',"darr");
			put('\u2014',"mdash");
			put('\u00f7',"divide");
			put('\u0393',"Gamma");
			put('\u03a4',"Tau");
			put('\u003c',"lt");
			put('\u00f8',"oslash");
			put('\u03b7',"eta");
			put('\u21b5',"crarr");
			put('\u00d5',"Otilde");
			put('\u0394',"Delta");
			put('\u00f0',"eth");
			put('\u03d6',"piv");
			put('\u00e7',"ccedil");
			put('\u211c',"real");
			put('\u00b9',"sup1");
			put('\u00b2',"sup2");
			put('\u00a4',"curren");
			put('\u00aa',"ordf");
			put('\u00b3',"sup3");
			put('\u03b3',"gamma");
			put('\u200e',"lrm");
			put('\u00a2',"cent");
			put('\u03bc',"mu");
			put('\u03c4',"tau");
			put('\u2021',"Dagger");
			put('\u00ba',"ordm");
			put('\u21d1',"uArr");
			put('\u2234',"there4");
			put('\u00c3',"Atilde");
			put('\u00f5',"otilde");
			put('\u2260',"ne");
			put('\u03b4',"delta");
			put('\u200c',"zwnj");
			put('\u00ff',"yuml");
			put('\u220b',"ni");
			put('\u00e0',"agrave");
			put('\u0160',"Scaron");
			put('\u0026',"amp");
			put('\u03bd',"nu");
			put('\u039c',"Mu");
			put('\u2200',"forall");
			put('\u2297',"otimes");
			put('\u00ef',"iuml");
			put('\u2191',"uarr");
			put('\u2208',"isin");
			put('\u2229',"cap");
			put('\u00a6',"brvbar");
			put('\u00de',"THORN");
			put('\u2227',"and");
			put('\u2287',"supe");
			put('\u2118',"weierp");
			put('\u2220',"ang");
			put('\u0178',"Yuml");
			put('\u00b6',"para");
			put('\u0161',"scaron");
			put('\u2228',"or");
			put('\u2018',"lsquo");
			put('\u00c6',"AElig");
			put('\u00d4',"Ocirc");
			put('\u00e6',"aelig");
			put('\u00bf',"iquest");
			put('\u039d',"Nu");
			put('\u00cf',"Iuml");
			put('\u2030',"permil");
			put('\u2329',"lang");
			put('\u221a',"radic");
			put('\u222b',"int");
			put('\u03b2',"beta");
			put('\u00c5',"Aring");
			put('\u2202',"part");
			put('\u22c5',"sdot");
			put('\u00fe',"thorn");
			put('\u03c0',"pi");
			put('\u00b7',"middot");
		}
	};

	/** Wrapped output writer. */
	protected Writer writer;
	/** Terminate single tags with a slash? */
	private boolean terminal;
	/** Pretty print? */
	private boolean pretty;
	/** Internal tag stack. */
	private Stack<String> tagStack;
	
	public enum Options
	{
		SLASHES_IN_SINGLE_TAGS,
		PRETTY;
	}
	
	/**
	 * Creates a new HTMLWriter.
	 * @param writer the writer to write out to.
	 * @param options options for output.
	 */
	public HTMLWriter(Writer writer, Options ... options)
	{
		this.writer = writer;
		this.tagStack = new Stack<>();
		this.terminal = false;
		this.pretty = false;
		for (Options opt : options) switch (opt)
		{
			case SLASHES_IN_SINGLE_TAGS:
				this.terminal = true;
				break;
			case PRETTY:
				this.pretty = true;
				break;
		}
	}
	
	/**
	 * Creates a single tag attribute, no value.
	 * @param key the attribute name.
	 * @return the new attribute.
	 */
	public static Attribute attribute(String key)
	{
		return attribute(key, null);
	}

	/**
	 * Creates a single tag attribute.
	 * @param key the attribute name.
	 * @param value the attribute value.
	 * @return the new attribute.
	 */
	public static Attribute attribute(String key, String value)
	{
		Attribute out = new Attribute();
		out.key = key;
		out.value = value;
		return out;
	}

	/**
	 * Creates an id attribute.
	 * @param value the attribute value.
	 * @return the new attribute.
	 */
	public static Attribute id(String value)
	{
		return attribute("id", value);
	}

	/**
	 * Creates a "class" attribute with a single class.
	 * @param value the class value.
	 * @return the new attribute.
	 */
	public static Attribute classes(String value)
	{
		// work around an unnecessary implicit allocation
		String[] s = SINGLE_STRING.get();
		s[0] = value;
		return classes(s);
	}

	/**
	 * Creates a "class" attribute with multiple classes.
	 * @param values the class list.
	 * @return the new attribute.
	 */
	public static Attribute classes(String ... values)
	{
		return attribute("class", join(" ", values));
	}

	/**
	 * Creates an "href" attribute.
	 * @param value the value.
	 * @return the new attribute.
	 */
	public static Attribute href(String value)
	{
		return attribute("href", value);
	}

	/**
	 * Creates a "src" attribute.
	 * @param value the value.
	 * @return the new attribute.
	 */
	public static Attribute src(String value)
	{
		return attribute("src", value);
	}

	/**
	 * Creates a "type" attribute.
	 * @param value the value.
	 * @return the new attribute.
	 */
	public static Attribute type(String value)
	{
		return attribute("type", value);
	}

	/**
	 * Creates a "rel" attribute.
	 * @param value the value.
	 * @return the new attribute.
	 */
	public static Attribute rel(String value)
	{
		return attribute("rel", value);
	}

	/**
	 * Creates a "name" attribute.
	 * @param value the value.
	 * @return the new attribute.
	 */
	public static Attribute name(String value)
	{
		return attribute("name", value);
	}

	/**
	 * Creates an HTMLWriter that just writes to a new String.
	 * The {@link #toString()} method will complete and return the string contents.
	 * @param doctype the document type.
	 * @param options options for output.
	 * @return the new HTMLStringWriter.
	 */
	@SuppressWarnings("resource")
	public static HTMLStringWriter createHTMLString(String doctype, Options ... options)
	{
		try {
			return (HTMLStringWriter)((new HTMLStringWriter(options)).doctype(doctype));
		} catch (IOException e) {
			return null;
		}
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
	 * Reads in all characters from a file and writes them.
	 * Assumes native encoding.
	 * @param file the file to read.
	 * @return this writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public HTMLWriter html(File file) throws IOException
	{
		return html(file, Charset.defaultCharset());
	}
	
	/**
	 * Reads in all characters from a file and writes them.
	 * @param file the file to read.
	 * @param charset the file encoding.
	 * @return this writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public HTMLWriter html(File file, Charset charset) throws IOException
	{
		try (Reader reader = new InputStreamReader(new FileInputStream(file), charset))
		{
			return html(reader);
		}
	}
	
	/**
	 * Reads in all characters from an InputStream and writes them.
	 * @param in the input stream.
	 * @param charset the stream encoding.
	 * @return this writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public HTMLWriter html(InputStream in, Charset charset) throws IOException
	{
		try (Reader reader = new InputStreamReader(in, charset))
		{
			return html(reader);
		}
	}
	
	/**
	 * Reads in all characters from a Reader and writes them.
	 * @param reader the reader.
	 * @return this writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public HTMLWriter html(Reader reader) throws IOException
	{
		writePrettyIndent();
		writeReader(reader);
		writePrettyNewline();
		return this;
	}

	/**
	 * Writes a series of characters, as-is, unconverted.
	 * @param text the string to write.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter html(CharSequence text) throws IOException
	{
		writePrettyIndent();
		writer.append(text);
		writePrettyNewline();
		return this;
	}

	/**
	 * Reads in all characters from a file and writes them, 
	 * converting special characters to HTML entities when possible.
	 * Assumes native encoding.
	 * @param file the file to read.
	 * @return this writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public HTMLWriter text(File file) throws IOException
	{
		return html(file, Charset.defaultCharset());
	}
	
	/**
	 * Reads in all characters from a file and writes them, 
	 * converting special characters to HTML entities when possible.
	 * @param file the file to read.
	 * @param charset the file encoding.
	 * @return this writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public HTMLWriter text(File file, Charset charset) throws IOException
	{
		try (Reader reader = new InputStreamReader(new FileInputStream(file), charset))
		{
			return html(reader);
		}
	}
	
	/**
	 * Reads in all characters from an InputStream and writes them, 
	 * converting special characters to HTML entities when possible.
	 * @param in the input stream.
	 * @param charset the stream encoding.
	 * @return this writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public HTMLWriter text(InputStream in, Charset charset) throws IOException
	{
		try (Reader reader = new InputStreamReader(in, charset))
		{
			return html(reader);
		}
	}
	
	/**
	 * Reads in all characters from a Reader and writes them, 
	 * converting special characters to HTML entities when possible.
	 * @param reader the reader.
	 * @return this writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public HTMLWriter text(Reader reader) throws IOException
	{
		writePrettyIndent();
		writeReaderConverted(reader);
		writePrettyNewline();
		return this;
	}

	/**
	 * Writes a series of characters, converting special characters to HTML entities when possible.
	 * @param text the string to write.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter text(CharSequence text) throws IOException
	{
		writePrettyIndent();
		writeConverted(text);
		writePrettyNewline();
		return this;
	}

	/**
	 * Writes a DOCTYPE tag.
	 * @param type the document type.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter doctype(String type) throws IOException
	{
		html("<!DOCTYPE " + type + ">");
		return this;
	}

	/**
	 * Writes a single, auto-terminated tag.
	 * @param tagName the tag name.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter tag(String tagName) throws IOException
	{
		return tag(tagName, NO_ATTRIBUTES);
	}

	/**
	 * Writes a single, auto-terminated tag with attributes.
	 * @param tagName the tag name.
	 * @param attributes the attributes.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter tag(String tagName, Attribute ... attributes) throws IOException
	{
		writePrettyIndent();
		writeSingleTag(tagName, attributes);
		writePrettyNewline();
		return this;
	}

	/**
	 * Writes a tag with attributes, and text content read from a file, 
	 * converting special characters to HTML entities when possible.
	 * @param tagName the tag name.
	 * @param file the file to read.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter tag(String tagName, File file) throws IOException
	{
		return tag(tagName, file, Charset.defaultCharset(), NO_ATTRIBUTES);
	}

	/**
	 * Writes a tag with attributes, and text content read from a file, 
	 * converting special characters to HTML entities when possible.
	 * @param tagName the tag name.
	 * @param file the file to read.
	 * @param attributes the attributes.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter tag(String tagName, File file, Attribute ... attributes) throws IOException
	{
		return tag(tagName, file, Charset.defaultCharset(), attributes);
	}

	/**
	 * Writes a tag with attributes, and text content read from a file,
	 * converting special characters to HTML entities when possible.
	 * @param tagName the tag name.
	 * @param file the file to read.
	 * @param charset the file encoding.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter tag(String tagName, File file, Charset charset) throws IOException
	{
		return tag(tagName, file, charset, NO_ATTRIBUTES);
	}

	/**
	 * Writes a tag with attributes, and text content read from a file,
	 * converting special characters to HTML entities when possible.
	 * @param tagName the tag name.
	 * @param file the file to read.
	 * @param charset the file encoding.
	 * @param attributes the attributes.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter tag(String tagName, File file, Charset charset, Attribute ... attributes) throws IOException
	{
		try (Reader reader = new InputStreamReader(new FileInputStream(file), charset))
		{
			return tag(tagName, reader, attributes);
		}
	}

	/**
	 * Writes a tag with attributes, and text content read from a stream,
	 * converting special characters to HTML entities when possible.
	 * @param tagName the tag name.
	 * @param in the input stream.
	 * @param charset the stream encoding.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter tag(String tagName, InputStream in, Charset charset) throws IOException
	{
		return tag(tagName, in, charset, NO_ATTRIBUTES);
	}

	/**
	 * Writes a tag with attributes, and text content read from a stream,
	 * converting special characters to HTML entities when possible.
	 * @param tagName the tag name.
	 * @param in the input stream.
	 * @param charset the stream encoding.
	 * @param attributes the attributes.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter tag(String tagName, InputStream in, Charset charset, Attribute ... attributes) throws IOException
	{
		try (Reader reader = new InputStreamReader(in, charset))
		{
			return tag(tagName, reader, attributes);
		}
	}

	/**
	 * Reads in all characters from a Reader and writes them, 
	 * converting special characters to HTML entities when possible.
	 * @param tagName the tag name.
	 * @param reader the reader.
	 * @return this writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public HTMLWriter tag(String tagName, Reader reader) throws IOException
	{
		return tag(tagName, reader, NO_ATTRIBUTES);
	}

	/**
	 * Reads in all characters from a Reader and writes them, 
	 * converting special characters to HTML entities when possible.
	 * @param tagName the tag name.
	 * @param reader the reader.
	 * @param attributes the attributes.
	 * @return this writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public HTMLWriter tag(String tagName, Reader reader, Attribute ... attributes) throws IOException
	{
		writePrettyIndent();
		writeStartTag(tagName, attributes);
		writeReaderConverted(reader);
		writeEndTag(tagName);
		writePrettyNewline();
		return this;
	}

	/**
	 * Writes a tag with attributes, and text content.
	 * @param tagName the tag name.
	 * @param text the string to write.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter tag(String tagName, CharSequence text) throws IOException
	{
		return tag(tagName, text, NO_ATTRIBUTES);
	}

	/**
	 * Writes a tag with attributes, and text content.
	 * @param tagName the tag name.
	 * @param text the string to write.
	 * @param attributes the attributes.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter tag(String tagName, CharSequence text, Attribute ... attributes) throws IOException
	{
		writePrettyIndent();
		writeStartTag(tagName, attributes);
		writeConverted(text);
		writeEndTag(tagName);
		writePrettyNewline();
		return this;
	}

	/**
	 * Writes a single META tag with name and content attributes.
	 * @param name the name attribute.
	 * @param content the content attribute.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 * @see #tag(String, Attribute...)
	 */
	public HTMLWriter meta(String name, String content) throws IOException
	{
		return tag("meta", name(name), attribute("content", content));
	}

	/**
	 * Writes a single LINK tag with "stylesheet" relationship and an HREF to the resource.
	 * @param resourcePath the URI/URL to the style sheet.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 * @see #tag(String, Attribute...)
	 */
	public HTMLWriter css(String resourcePath) throws IOException
	{
		return tag("link", rel("stylesheet"), href(resourcePath));
	}

	/**
	 * Writes an empty script element with type "text/javascript" and a "src" path to the script resource.
	 * If you wish to embed a script, consider using: 
	 * <p><code>tag("script", new File("script-file.js"), type("text/javascript"))</code>
	 * @param resourcePath the URI/URL to the script data.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 * @see #tag(String, String, Attribute...)
	 */
	public HTMLWriter script(String resourcePath) throws IOException
	{
		// push-pop since script cannot be a singleton tag.
		return push("script", type("text/javascript"), src(resourcePath)).pop();
	}

	/**
	 * Writes a tag with attributes, and text content.
	 * @param text the comment data.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter comment(CharSequence text) throws IOException
	{
		writePrettyIndent();
		writer.append("<!-- ");
		writer.append(text);
		writer.append(" -->");
		writePrettyNewline();
		return this;
	}

	/**
	 * Pushes (and writes) an open tag onto the stack. 
	 * @param tagName the tag name.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 * @see #pop()
	 */
	public HTMLWriter push(String tagName) throws IOException
	{
		return push(tagName, NO_ATTRIBUTES);
	}
	
	/**
	 * Pushes (and writes) an open tag onto the stack with attributes. 
	 * @param tagName the tag name.
	 * @param attributes the attributes.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 * @see #pop()
	 */
	public HTMLWriter push(String tagName, Attribute ... attributes) throws IOException
	{
		writePrettyIndent();
		writeStartTag(tagName, attributes);
		writePrettyNewline();
		tagStack.push(tagName);
		return this;
	}

	/**
	 * Pops the last pushed tag and writes the end tag.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 * @throws EmptyStackException if there are no tags on the stack.
	 * @see #push(String)
	 * @see #push(String, Attribute...)
	 */
	public HTMLWriter pop() throws IOException
	{
		String tagName = tagStack.pop();
		writePrettyIndent();
		writeEndTag(tagName);
		writePrettyNewline();
		return this;
	}

	/**
	 * Starts an HTML document.
	 * Convenience method for: <code>doctype("html").push("html")</code>
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter start() throws IOException
	{
		return doctype("html").push("html");
	}
	
	/**
	 * Starts an HTML document with a different DOCTYPE.
	 * Convenience method for: <code>doctype(type).push("html")</code>
	 * @param type the document type.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 */
	public HTMLWriter start(String type) throws IOException
	{
		return doctype(type).push("html");
	}
	
	/**
	 * Pops all pushed tags from the stack and flushes the stream.
	 * Meant to the paired with {@link #start()} or {@link #start(String)}.
	 * @return this writer.
	 * @throws IOException if a write error occurs.
	 * @see #pop()
	 */
	public HTMLWriter end() throws IOException
	{
		while (!tagStack.isEmpty())
			pop();
		writer.flush();
		return this;
	}
	
	@Override
	public void flush() throws IOException
	{
		writer.flush();
	}

	/**
	 * Calls {@link #end()} and closes the underlying writer.
	 */
	@Override
	public void close() throws IOException
	{
		end();
		writer.close();
	}

	private void writeStartTag(String tagName, Attribute... attributes) throws IOException
	{
		writer.append('<').append(tagName);
		for (int i = 0; i < attributes.length; i++)
		{
			writer.write(' ');
			writeAttribute(attributes[i]);
		}
		writer.append('>');
		flush();
	}

	private void writeSingleTag(String tagName, Attribute... attributes) throws IOException
	{
		writer.append('<').append(tagName);
		for (int i = 0; i < attributes.length; i++)
		{
			writer.write(' ');
			writeAttribute(attributes[i]);
		}
		if (terminal)
			writer.append('/');
		writer.append('>');
		flush();
	}

	private void writeEndTag(String tagName, Attribute... attributes) throws IOException
	{
		writer.append('<').append('/').append(tagName).append('>');
		flush();
	}

	private void writePrettyNewline() throws IOException 
	{
		if (!pretty)
			return;
		writer.append('\n');
	}

	private void writePrettyIndent() throws IOException 
	{
		if (!pretty)
			return;
		for (int i = 0; i < tagStack.size(); i++)
			writer.append('\t');
	}

	private void writeReader(Reader reader) throws IOException
	{
		int buf;
		char[] cbuf = CHARBUFFER.get();
		while ((buf = reader.read(cbuf)) > 0)
			writer.write(cbuf, 0, buf);
	}
	
	private void writeConverted(char c) throws IOException 
	{
		String entity;
		if ((entity = UNICODE_ENTITY_MAP.get(c)) != null)
			writer.append('&').append(entity).append(';');
		else
			writer.append(c);
	}

	private void writeConverted(CharSequence seq) throws IOException 
	{
		for (int i = 0; i < seq.length(); i++)
			writeConverted(seq.charAt(i));
	}

	private void writeReaderConverted(Reader reader) throws IOException
	{
		int buf;
		char[] cbuf = CHARBUFFER.get();
		while ((buf = reader.read(cbuf)) > 0)
			for (int i = 0; i < buf; i++)
				writeConverted(cbuf[i]);
	}

	private void writeAttribute(Attribute attribute) throws IOException 
	{
		writer.append(attribute.key);
		if (attribute.value != null)
		{
			writer.append('=').append('"');
			writeConverted(attribute.value);
			writer.append('"');
		}
	}
	
	/**
	 * Convenience string writer for HTMLWriter.
	 * The {@link #toString()} method returns the compiled string.
	 */
	public static class HTMLStringWriter extends HTMLWriter
	{
		private HTMLStringWriter(Options ...options)
		{
			super(new StringWriter(512), options);
		}
		
		@Override
		public String toString() 
		{
			try {
				super.close();
				return writer.toString();
			} catch (IOException e) {
				return null;
			}
		}
	}

	// A single attribute.
	public static class Attribute
	{
		private String key;
		private String value;
		private Attribute() {}
	}

}
