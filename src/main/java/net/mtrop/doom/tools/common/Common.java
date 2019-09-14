package net.mtrop.doom.tools.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.util.NameUtils;

/**
 * Common shared functions.
 * @author Matthew Tropiano
 */
public final class Common
{
	private static final String SWANTBLS_HEADER = (new StringBuilder())
		.append("#\n")
		.append("# This file is input for SWANTBLS.EXE, it specifies the switchnames\n")
		.append("# and animated textures and flats usable with BOOM. The output of\n")
		.append("# SWANTBLS is two lumps, SWITCHES.LMP and ANIMATED.LMP that should\n")
		.append("# be inserted in the PWAD as lumps.\n")
		.append("#\n")
		.append("# switches usable with each IWAD, 1=SW, 2=registered DOOM, 3=DOOM2\n")			
	.toString();

	private static final String SWANTBLS_SWITCHES = (new StringBuilder())
		.append("# switches usable with each IWAD, 1=SW, 2=registered DOOM, 3=DOOM2\n")
		.append("[SWITCHES]\n")
		.append("# epi   texture1        texture2\n")
	.toString();

	private static final String SWANTBLS_ANIMFLATS = (new StringBuilder())
		.append("# animated flats, spd is number of frames between changes\n")
		.append("# 65536 = warping, in EE\n")
		.append("[FLATS]\n")
		.append("# spd   last        first\n")
	.toString();

	private static final String SWANTBLS_ANIMTEX = (new StringBuilder())
		.append("#animated textures, spd is number of frames between changes\n")
		.append("[TEXTURES]\n")
		.append("# spd   last        first\n")
	.toString();

	/**
	 * Parses a SWANTBLS file for ANIMATED and SWITCHES data.
	 * @param reader the input reader.
	 * @param animated the output Animated.
	 * @param switches the output Switches.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ParseException on bad parse.
	 */
	public static void parseSwitchAnimatedTables(BufferedReader reader, Animated animated, Switches switches) throws IOException, FileNotFoundException, ParseException
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
	 * @throws IOException if an error occurs uring write.
	 */
	public static void writeSwitchAnimatedTables(Switches switches, Animated animated, String header, PrintWriter writer) throws IOException
	{
		writer.println(header);
		// TODO: Finish this.
	}
	
}
