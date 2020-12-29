package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchContext;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchDoom19Context;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHMiscellany;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom.EpisodeMap;

/**
 * Performs exporting.
 * @author Matthew Tropiano
 */
public final class DecoHackExporter
{
	/**
	 * Writes this patch to a DeHackEd file stream.
	 * @param patch the patch.
	 * @param writer the writer to write to.
	 * @param comment the comment line.
	 * @throws IOException if a write error occurs.
	 */
	public static void writePatch(AbstractPatchContext<?> patch, Writer writer, String comment) throws IOException
	{
		if (patch instanceof AbstractPatchDoom19Context)
			writePatch((AbstractPatchDoom19Context)patch, writer, comment);
		else if (patch instanceof AbstractPatchBoomContext)
			writePatch((AbstractPatchBoomContext)patch, writer, comment);
	}
	
	/**
	 * Writes this patch to a DeHackEd file stream.
	 * @param patch the patch.
	 * @param writer the writer to write to.
	 * @param comment the comment line.
	 * @throws IOException if a write error occurs.
	 */
	public static void writePatch(AbstractPatchDoom19Context patch, Writer writer, String comment) throws IOException
	{
		writePatchHeader(writer, comment, 19, 6);
		writePatchBody(patch, writer);

		for (int i = 0; i < patch.getActionPointerCount(); i++)
		{
			DEHActionPointer action = patch.getActionPointer(i);
			DEHActionPointer original = patch.getSourcePatch().getActionPointer(i);
			if (action == null)
				continue;
			if (!action.equals(original))
			{
				writer.append("Pointer ")
					.append(String.valueOf(i))
					.append(" (Frame ")
					.append(String.valueOf(patch.getSourcePatch().getActionPointerFrame(i)))
					.append(")")
					.append("\r\n");
				writer.append("Codep Frame = ").append(String.valueOf(action.getFrame())).append("\r\n");
				writer.append("\r\n");
			}
		}
		writer.flush();

		for (int i = 0; i < patch.getStringCount(); i++)
		{
			String str = patch.getString(i);
			String original = patch.getSourcePatch().getString(i);
			if (str == null)
				continue;
			if (!str.equals(original))
			{
				writer.append("Text ")
					.append(String.valueOf(original.length()))
					.append(" ")
					.append(String.valueOf(str.length()))
					.append("\r\n");
				writer.append(original).append(str);
				if (i < patch.getStringCount() - 1)
					writer.append("\r\n");
				writer.flush();
			}
		}
	}
	
	/**
	 * Writes this patch to a DeHackEd file stream.
	 * @param patch the patch.
	 * @param writer the writer to write to.
	 * @param comment the comment line.
	 * @throws IOException if a write error occurs.
	 */
	public static void writePatch(AbstractPatchBoomContext patch, Writer writer, String comment) throws IOException
	{
		writePatchHeader(writer, comment, 21, 6);
		writePatchBody(patch, writer);

		// CODEPTR
		boolean codeptrHeader = false;
		for (int i = 0; i < patch.getStateCount(); i++)
		{
			DEHActionPointer pointer = patch.getActionPointer(i);
			DEHActionPointer original = patch.getSourcePatch().getActionPointer(i);
			if (pointer == null)
				continue;
			if (!pointer.equals(original))
			{
				if (!codeptrHeader)
				{
					writer.append("[CODEPTR]").append("\r\n");
					codeptrHeader = true;
				}
				writer.append("FRAME ")
					.append(String.valueOf(i))
					.append(" = ")
					.append(pointer.getMnemonic())
					.append("\r\n");
			}
		}
		if (codeptrHeader)
			writer.append("\r\n").flush();
		
		// STRINGS
		boolean stringsHeader = false;
		for (String keys : patch.getStringKeys())
		{
			String value;
			if (!Objects.equals(value = patch.getString(keys), patch.getSourcePatch().getString(keys)))
			{
				if (!stringsHeader)
				{
					writer.append("[STRINGS]").append("\r\n");
					stringsHeader = true;
				}
				writer.append(keys)
					.append(" = ")
					.append(Common.withEscChars(value)).append("\r\n");
			}
		}
		if (stringsHeader)
			writer.append("\r\n").flush();
		
		// PARS
		boolean parsHeader = false;
		for (EpisodeMap em : patch.getParEntries())
		{
			Integer seconds;
			if ((seconds = patch.getParSeconds(em)) != patch.getSourcePatch().getParSeconds(em))
			{
				if (!parsHeader)
				{
					writer.append("[PARS]").append("\r\n");
					parsHeader = true;
				}
				
				writer.append("par ");
				
				if (em.getEpisode() != 0)
					writer.append(String.valueOf(em.getEpisode())).append(' ');
				
				writer.append(String.valueOf(em.getMap()))
					.append(' ')
					.append(String.valueOf(seconds))
					.append("\r\n");
			}
		}
		if (parsHeader)
			writer.append("\r\n").flush();
	}

	/**
	 * Writes the patch header.
	 * @param writer the output writer.
	 * @param comment a comment line.
	 * @param version the patch version.
	 * @param formatNumber the patch format number.
	 * @throws IOException if a write error occurs.
	 */
	private static void writePatchHeader(Writer writer, String comment, int version, int formatNumber) throws IOException
	{
		// Header
		writer.append("Patch File for DeHackEd v3.0").append("\r\n");
		
		// Comment Blurb
		writer.append("# ").append(comment).append("\r\n");
		writer.append("# Note: Use the pound sign ('#') to start comment lines.").append("\r\n");
		writer.append("\r\n");
	
		// Version
		writer.append("Doom version = " + version).append("\r\n");
		writer.append("Patch format = " + formatNumber).append("\r\n");
		writer.append("\r\n");
		writer.append("\r\n");
		writer.flush();
	}

	/**
	 * Writes the common patch body.
	 * @param patch the patch.
	 * @param writer the output writer.
	 * @throws IOException if a write error occurs.
	 */
	private static void writePatchBody(AbstractPatchContext<?> patch, Writer writer) throws IOException
	{
		for (int i = 1; i < patch.getThingCount(); i++)
		{
			DEHThing thing = patch.getThing(i);
			DEHThing original = patch.getSourcePatch().getThing(i);
			if (thing == null)
				continue;
			if (!thing.equals(original))
			{
				writer.append("Thing ")
					.append(String.valueOf(i))
					.append(" (")
					.append(String.valueOf(thing.getName()))
					.append(")")
					.append("\r\n");
				thing.writeObject(writer, original);
				writer.append("\r\n");
			}
		}
		writer.flush();
	
		for (int i = 0; i < patch.getStateCount(); i++)
		{
			DEHState state = patch.getState(i);
			DEHState original = patch.getSourcePatch().getState(i);
			if (state == null)
				continue;
			if (!state.equals(original))
			{
				writer.append("Frame ").append(String.valueOf(i)).append("\r\n");
				state.writeObject(writer, original);
				writer.append("\r\n");
			}
		}
		writer.flush();
	
		for (int i = 0; i < patch.getSoundCount(); i++)
		{
			DEHSound sound = patch.getSound(i);
			DEHSound original = patch.getSourcePatch().getSound(i);
			if (sound == null)
				continue;
			if (!sound.equals(original))
			{
				writer.append("Sound ").append(String.valueOf(i)).append("\r\n");
				sound.writeObject(writer, original);
				writer.append("\r\n");
			}
		}
		writer.flush();
	
		for (int i = 0; i < patch.getWeaponCount(); i++)
		{
			DEHWeapon weapon = patch.getWeapon(i);
			DEHWeapon original = patch.getSourcePatch().getWeapon(i);
			if (weapon == null)
				continue;
			if (!weapon.equals(original))
			{
				writer.append("Weapon ")
					.append(String.valueOf(i))
					.append(" (")
					.append(String.valueOf(weapon.getName()))
					.append(")")
					.append("\r\n");
				weapon.writeObject(writer, original);
				writer.append("\r\n");
			}
		}
		writer.flush();
	
		for (int i = 0; i < patch.getAmmoCount(); i++)
		{
			DEHAmmo ammo = patch.getAmmo(i);
			DEHAmmo original = patch.getSourcePatch().getAmmo(i);
			if (ammo == null)
				continue;
			if (!ammo.equals(original))
			{
				writer.append("Ammo ")
					.append(String.valueOf(i))
					.append(" (")
					.append(String.valueOf(ammo.getName()))
					.append(")")
					.append("\r\n");
				ammo.writeObject(writer, original);
				writer.append("\r\n");
			}
		}
		writer.flush();
	
		DEHMiscellany misc = patch.getMiscellany();
		DEHMiscellany miscOriginal = patch.getSourcePatch().getMiscellany();
		if (!misc.equals(miscOriginal))
		{
			writer.append("Misc ").append(String.valueOf(0)).append("\r\n");
			misc.writeObject(writer, miscOriginal);
			writer.append("\r\n");
		}
		writer.flush();
	}

}
