/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.CommonTextureList;
import net.mtrop.doom.texture.PatchNames;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.texture.TextureSet.Patch;
import net.mtrop.doom.texture.TextureSet.Texture;
import net.mtrop.doom.util.NameUtils;

/**
 * Common shared functions.
 * @author Matthew Tropiano
 */
public final class Utility
{
	private static final String SWANTBLS_HEADER = (new StringBuilder())
		.append("#\n")
		.append("# This file is input for SWANTBLS.EXE, it specifies the switchnames\n")
		.append("# and animated textures and flats usable with BOOM. The output of\n")
		.append("# SWANTBLS is two lumps, SWITCHES.LMP and ANIMATED.LMP that should\n")
		.append("# be inserted in the PWAD as lumps.")
		.append("#\n")
		.append("# Of course, this is also readable by WSWANTBL.\n")
	.toString();

	private static final String SWANTBLS_SWITCHES = (new StringBuilder())
		.append("# switches usable with each IWAD, 1=SW, 2=registered DOOM, 3=DOOM2\n")
		.append("[SWITCHES]\n")
		.append("# epi   texture1        texture2")
	.toString();

	private static final String SWANTBLS_ANIMFLATS = (new StringBuilder())
		.append("# animated flats, spd is number of frames between changes\n")
		.append("# 65536 = warping, in EE\n")
		.append("[FLATS]\n")
		.append("# spd   last        first")
	.toString();

	private static final String SWANTBLS_ANIMTEX = (new StringBuilder())
		.append("# animated textures, spd is number of frames between changes\n")
		.append("[TEXTURES]\n")
		.append("# spd   last        first")
	.toString();

	
	/** Version number. */
	private static Map<String, String> VERSION_MAP = new HashMap<>();
	
	/**
	 * Gets the embedded version string for a tool name.
	 * If there is no embedded version, this returns "SNAPSHOT".
	 * @param name the name of the tool. 
	 * @return the version string or "SNAPSHOT"
	 */
	public static String getVersionString(String name)
	{
		if (VERSION_MAP.containsKey(name))
			return VERSION_MAP.get(name);
		
		String out = null;
		try (InputStream in = Common.openResource("net/mtrop/doom/tools/" + name + ".version")) {
			if (in != null)
				VERSION_MAP.put(name, out = Common.getTextualContents(in, "UTF-8").trim());
		} catch (IOException e) {
			/* Do nothing. */
		}
		
		return out != null ? out : "SNAPSHOT";
	}
	
	/**
	 * Parses a DEUTEX texture file contents.
	 * @param reader the reader to read from.
	 * @param startingPatches the starting PNAMES.
	 * @param startingTextureList the starting TEXTUREX.
	 * @return a combined texture set.
	 * @throws IOException if an I/O Error occurs during read.
	 * @throws ParseException if an error occurs during parse.
	 */
	public static TextureSet readDEUTEXFile(BufferedReader reader, PatchNames startingPatches, CommonTextureList<?> startingTextureList) throws IOException, ParseException
	{
		Texture currentTexture = null;
		TextureSet textureSet = new TextureSet(startingPatches, startingTextureList);
		String line;
		int linenum = 0;
		Pattern whitespacePattern = Pattern.compile("\\s+"); 
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			linenum++;
			if (line.isEmpty() || line.startsWith(";"))
				continue;
			
			if (line.startsWith("*")) // is patch.
			{
				if (currentTexture == null)
					throw new ParseException("Line " + linenum + ": Patch line before texture line.");
				
				String elementName = "";
				try (Scanner scanner = new Scanner(line)) 
				{
					scanner.useDelimiter(whitespacePattern);
					elementName = "star";
					if (!scanner.next().equals("*"))
						throw new ParseException("Line " + linenum + ": Malformed patch: missing star prefix.");
					elementName = "name";
					Patch p = currentTexture.createPatch(NameUtils.toValidEntryName(scanner.next()));
					elementName = "origin X";
					p.setOriginX(scanner.nextInt());
					elementName = "origin Y";
					p.setOriginY(scanner.nextInt());
				} 
				catch (NoSuchElementException e) 
				{
					throw new ParseException("Line " + linenum + ":  Malformed patch: missing " + elementName + ".");
				}
			}
			else // is new texture.
			{
				String elementName = "";
				try (Scanner scanner = new Scanner(line)) 
				{
					scanner.useDelimiter(whitespacePattern);
					elementName = "name";
					currentTexture = textureSet.createTexture(NameUtils.toValidTextureName(scanner.next()));
					elementName = "width";
					currentTexture.setWidth(scanner.nextInt());
					elementName = "height";
					currentTexture.setHeight(scanner.nextInt());
				} 
				catch (NoSuchElementException e) 
				{
					throw new ParseException("Line " + linenum + ":  Malformed patch: missing " + elementName + ".");
				}
			}
		}
		return textureSet;
	}
	
	/**
	 * Writes DEUTEX data to a print writer.
	 * @param textureSet the texture set to export. 
	 * @param header the header blurb to write first.
	 * @param writer the writer to write out to.
	 * @throws IOException if an I/O Error occurs during write.
	 */
	public static void writeDEUTEXFile(TextureSet textureSet, String header, PrintWriter writer) throws IOException
	{
		writer.println(header);
		writer.println();

		for (TextureSet.Texture t : textureSet)
		{
			writer.println(t.getName() + " " + t.getWidth() + " " + t.getHeight());
			for (TextureSet.Patch p : t)
				writer.println("*\t" + p.getName() + " " + p.getOriginX() + " " + p.getOriginY());
			writer.println();
		}
	}
	
	/**
	 * Parses a SWANTBLS file for ANIMATED and SWITCHES data.
	 * @param reader the input reader.
	 * @param animated the output Animated.
	 * @param switches the output Switches.
	 * @throws IOException if an I/O Error occurs during read.
	 * @throws ParseException if an error occurs during parse.
	 */
	public static void readSwitchAnimatedTables(BufferedReader reader, Animated animated, Switches switches) throws IOException, ParseException
	{
		final int STATE_NONE = 0;
		final int STATE_SWITCHES = 1;
		final int STATE_FLATS = 2;
		final int STATE_TEXTURES = 3;
		Pattern whitespacePattern = Pattern.compile("\\s+"); 

		String line;
		int linenum = 0;
		int state = STATE_NONE;
		
		Switches.Game[] SWITCH_GAMES = Switches.Game.values();
		
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			linenum++;
			if (line.isEmpty() || line.startsWith("#"))
				continue;
			
			if (line.startsWith("[") && line.endsWith("]"))
			{
				String type = line.substring(1, line.length() - 1);
				if (type.equals("SWITCHES"))
					state = STATE_SWITCHES;
				else if (type.equals("FLATS"))
					state = STATE_FLATS;
				else if (type.equals("TEXTURES"))
					state = STATE_TEXTURES;
				else
					throw new ParseException(String.format("Line %d: Malformed patch: bad type header: %s. Expected SWITCHES, FLATS, or TEXTURES.", linenum, type));
			}
			else switch (state)
			{
				case STATE_NONE:
				{
					throw new ParseException(String.format("Line %d: No type header found. Expected [SWITCHES], [FLATS], or [TEXTURES] before entries.", linenum));
				}
				
				case STATE_SWITCHES:
				{
					String elementName = "";
					try (Scanner scanner = new Scanner(line))
					{
						scanner.useDelimiter(whitespacePattern);
						
						elementName = "game";
						int gameId = scanner.nextInt();
						if (gameId < 1 || gameId >= SWITCH_GAMES.length)
							throw new ParseException(String.format("Line %d: Bad switch game: %d. Expected 1, 2, 3.", linenum, gameId));
						
						elementName = "\"off\" texture";
						String off = NameUtils.toValidTextureName(scanner.next());
						
						elementName = "\"on\" texture";
						String on = NameUtils.toValidTextureName(scanner.next());

						switches.addEntry(off, on, SWITCH_GAMES[gameId]); 
					}
					catch (NoSuchElementException e) 
					{
						throw new ParseException(String.format("Line %d: Malformed switch: missing %s.\n", linenum, elementName));
					}
				}
				break;
				
				case STATE_FLATS:
				{
					String elementName = "";
					try (Scanner scanner = new Scanner(line))
					{
						scanner.useDelimiter(whitespacePattern);
						
						elementName = "tics";
						int tics = scanner.nextInt();
						if (tics < 0)
						{
							throw new ParseException(String.format("Line %d: Bad animated flat. Tic duration is negative.\n", linenum));
						}
						
						elementName = "ending texture";
						String lastName = NameUtils.toValidTextureName(scanner.next());
						
						elementName = "starting texture";
						String firstName = NameUtils.toValidTextureName(scanner.next());

						animated.addEntry(Animated.flat(lastName, firstName, tics)); 
					}
					catch (NoSuchElementException e) 
					{
						throw new ParseException(String.format("Line %d: Malformed flat: missing %s.\n", linenum, elementName));
					}
				}
				break;
				
				case STATE_TEXTURES:
				{
					String elementName = "";
					try (Scanner scanner = new Scanner(line))
					{
						scanner.useDelimiter(whitespacePattern);
						
						elementName = "tics";
						int tics = scanner.nextInt();
						if (tics < 0)
						{
							throw new ParseException(String.format("Line %d: Bad animated texture. Tic duration is negative.", linenum));
						}
						
						elementName = "ending texture";
						String lastName = NameUtils.toValidTextureName(scanner.next());
						
						elementName = "starting texture";
						String firstName = NameUtils.toValidTextureName(scanner.next());

						animated.addEntry(Animated.texture(lastName, firstName, tics)); 
					}
					catch (NoSuchElementException e) 
					{
						throw new ParseException(String.format("Line %d: Malformed texture: missing %s.\n", linenum, elementName));
					}
				}
				break;
			}
		}
	}

	/**
	 * Writes SWANTBLS data to a print writer.
	 * @param switches the switches data.
	 * @param animated the animated data.
	 * @param header the header blurb to write first.
	 * @param writer the writer to write out to.
	 * @throws IOException if an error occurs during write.
	 */
	public static void writeSwitchAnimatedTables(Switches switches, Animated animated, String header, PrintWriter writer) throws IOException
	{
		writer.println(header);
		writer.println(SWANTBLS_HEADER);
		
		Switches.Entry[] swt = new Switches.Entry[switches.getEntryCount()];
		for (int i = 0; i < swt.length; i++)
			swt[i] = switches.get(i);

		Animated.Entry[] anm = new Animated.Entry[animated.getEntryCount()];
		for (int i = 0; i < anm.length; i++)
			anm[i] = animated.get(i);
		
		Arrays.sort(swt, (e1, e2) -> e1.getGame().ordinal() - e2.getGame().ordinal());
		Arrays.sort(anm, (e1, e2) -> e1.getType().ordinal() - e2.getType().ordinal());

		writer.println();
		writer.println(SWANTBLS_SWITCHES);
		Switches.Game game = null;
		for (Switches.Entry entry : swt)
		{
			if (game != entry.getGame())
			{
				writer.println();
				game = entry.getGame();
				switch (entry.getGame())
				{
					case SHAREWARE_DOOM:
						writer.println("# Shareware Doom");
						break;
					case DOOM:
						writer.println("# Registered Doom");
						break;
					case ALL:
						writer.println("# All Versions");
						break;
					default:
						break;
				}
			}
			writer.printf("%-7d %-15s %s\n", entry.getGame().ordinal(), entry.getOffName(), entry.getOnName());
		}

		Animated.TextureType type = null;
		for (Animated.Entry entry : anm)
		{
			if (type != entry.getType())
			{
				type = entry.getType();
				writer.println();
				switch (entry.getType())
				{
					case FLAT:
						writer.println(SWANTBLS_ANIMFLATS);
						break;
					case TEXTURE:
						writer.println(SWANTBLS_ANIMTEX);
						break;
				}
			}
			writer.printf("%-7d %-11s %s\n", entry.getTicks(), entry.getLastName(), entry.getFirstName());
		}

	}

}
