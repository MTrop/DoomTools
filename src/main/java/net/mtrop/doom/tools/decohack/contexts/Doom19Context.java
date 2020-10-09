package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.DEHActionPointer;
import net.mtrop.doom.tools.decohack.DEHAmmo;
import net.mtrop.doom.tools.decohack.DEHExporter;
import net.mtrop.doom.tools.decohack.DEHMiscellany;
import net.mtrop.doom.tools.decohack.DEHPatch;
import net.mtrop.doom.tools.decohack.DEHSound;
import net.mtrop.doom.tools.decohack.DEHState;
import net.mtrop.doom.tools.decohack.DEHThing;
import net.mtrop.doom.tools.decohack.DEHWeapon;
import net.mtrop.doom.tools.decohack.patches.Doom19Patch;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Patch context for Doom 1.9.
 * @author Matthew Tropiano
 */
public class Doom19Context implements DEHPatch, DEHExporter
{
	private static final Doom19Patch BASEPATCH = new Doom19Patch();
	
	private String[] strings;
	private DEHAmmo[] ammo;
	private DEHSound[] sounds;
	private DEHWeapon[] weapons;
	private DEHThing[] things;
	private DEHState[] states;
	private DEHActionPointer[] pointers;
	private DEHMiscellany miscellany;

	private Map<String, Integer> soundStringIndex;
	private Map<String, Integer> spriteStringIndex;
	
	private boolean[] freeStates;
	private boolean[] protectedStates;
	
	public Doom19Context()
	{
		this.strings = new String[BASEPATCH.getStringCount()];
		for (int i = 0; i < this.strings.length; i++)
			this.strings[i] = BASEPATCH.getString(i);
		
		this.ammo = new DEHAmmo[BASEPATCH.getAmmoCount()];
		for (int i = 0; i < this.ammo.length; i++)
			this.ammo[i] = (new DEHAmmo()).copyFrom(BASEPATCH.getAmmo(i));
		
		this.sounds = new DEHSound[BASEPATCH.getSoundCount()];
		for (int i = 0; i < this.sounds.length; i++)
			this.sounds[i] = (new DEHSound()).copyFrom(BASEPATCH.getSound(i));
		
		this.weapons = new DEHWeapon[BASEPATCH.getWeaponCount()];
		for (int i = 0; i < this.sounds.length; i++)
			this.weapons[i] = (new DEHWeapon()).copyFrom(BASEPATCH.getWeapon(i));
		
		this.things = new DEHThing[BASEPATCH.getThingCount()];
		for (int i = 0; i < this.things.length; i++)
			this.things[i] = (new DEHThing()).copyFrom(BASEPATCH.getThing(i));
		
		this.states = new DEHState[BASEPATCH.getStateCount()];
		for (int i = 0; i < this.states.length; i++)
			this.states[i] = (new DEHState()).copyFrom(BASEPATCH.getState(i));
		
		this.pointers = new DEHActionPointer[BASEPATCH.getActionPointerCount()];
		for (int i = 0; i < this.pointers.length; i++)
			this.pointers[i] = BASEPATCH.getActionPointer(i);		
		
		this.miscellany = (new DEHMiscellany()).copyFrom(BASEPATCH.getMiscellany());
		
		this.soundStringIndex = new HashMap<>();
		for (int i = 0; i < sounds.length; i++)
			this.soundStringIndex.put(strings[i + Doom19Patch.STRING_INDEX_SOUNDS], i);
		
		this.spriteStringIndex = new HashMap<>();
		for (int i = 0; i < Doom19Patch.STRING_INDEX_SPRITES_COUNT; i++)
			this.spriteStringIndex.put(strings[i + Doom19Patch.STRING_INDEX_SPRITES], i);
		
		this.freeStates = new boolean[states.length];
		this.protectedStates = new boolean[states.length];
		
		// Protect first two states from clear.
		this.protectedStates[0] = true; 
		this.protectedStates[1] = true; 
	}
	
	@Override
	public DEHMiscellany getMiscellany() 
	{
		return miscellany;
	}

	@Override
	public int getAmmoCount() 
	{
		return ammo.length;
	}

	@Override
	public DEHAmmo getAmmo(int index) 
	{
		return ammo[index];
	}

	@Override
	public int getStringCount() 
	{
		return strings.length;
	}

	@Override
	public String getString(int index)
	{
		return strings[index];
	}

	/**
	 * Sets a new string.
	 * @param index the string index to replace.
	 * @param value the string value.
	 * @throw IllegalArgumentException if the string to add is longer than the original string.
	 */
	public void setString(int index, String value)
	{
		// if sprite.
		if (index >= getSoundStringIndex() && index < getSoundStringIndex() + getSoundCount())
		{
			soundStringIndex.remove(strings[index]);
			soundStringIndex.put(value, index - getSoundStringIndex());
		}
		// if sound name.
		else if (index >= getSpriteStringIndex() && index < getSpriteStringIndex() + Doom19Patch.STRING_INDEX_SPRITES_COUNT)
		{
			spriteStringIndex.remove(strings[index]);
			spriteStringIndex.put(value, index - getSpriteStringIndex());
		}
		
		strings[index] = value;
	}
	
	@Override
	public Integer getSoundStringIndex()
	{
		return Doom19Patch.STRING_INDEX_SOUNDS;
	}

	@Override
	public Integer getSoundIndex(String name)
	{
		return soundStringIndex.get(name.toUpperCase());
	}

	@Override
	public Integer getSpriteStringIndex()
	{
		return Doom19Patch.STRING_INDEX_SPRITES;
	}

	@Override
	public Integer getSpriteIndex(String name)
	{
		return spriteStringIndex.get(name.toUpperCase());
	}

	@Override
	public int getSoundCount() 
	{
		return sounds.length;
	}

	@Override
	public DEHSound getSound(int index)
	{
		return sounds[index];
	}

	@Override
	public int getThingCount() 
	{
		return things.length;
	}

	@Override
	public DEHThing getThing(int index)
	{
		return things[index];
	}

	// TODO: Write free state.
	// TODO: Write free thing states.
	// TODO: Write free weapon states.
	// TODO: Write protect state.
	
	@Override
	public int getWeaponCount()
	{
		return weapons.length;
	}

	@Override
	public DEHWeapon getWeapon(int index)
	{
		return weapons[index];
	}

	@Override
	public int getStateCount()
	{
		return states.length;
	}

	@Override
	public DEHState getState(int index) 
	{
		return states[index];
	}

	@Override
	public Integer getStateActionPointerIndex(int stateIndex) 
	{
		return BASEPATCH.getStateActionPointerIndex(stateIndex);
	}

	@Override
	public int getActionPointerCount() 
	{
		return pointers.length;
	}

	@Override
	public DEHActionPointer getActionPointer(int index)
	{
		return pointers[index];
	}

	public int getActionPointerFrame(int index)
	{
		return BASEPATCH.getActionPointerFrame(index);
	}

	@Override
	public void writePatch(Writer writer, String comment) throws IOException
	{
		// Header
		writer.append("Patch File for DeHackEd v3.0").append("\r\n");
		
		// Comment Blurb
		writer.append("# ").append(comment).append("\r\n");
		writer.append("# Note: Use the pound sign ('#') to start comment lines.").append("\r\n");
		writer.append("\r\n");

		// Version
		writer.append("Doom version = 19").append("\r\n");
		writer.append("Patch format = 6").append("\r\n");
		writer.append("\r\n");
		writer.append("\r\n");
		writer.flush();
		
		for (int i = 0; i < getThingCount(); i++)
		{
			DEHThing thing = getThing(i);
			DEHThing original = BASEPATCH.getThing(i);
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

		for (int i = 0; i < getStateCount(); i++)
		{
			DEHState state = getState(i);
			DEHState original = BASEPATCH.getState(i);
			if (!state.equals(original))
			{
				writer.append("Frame ").append(String.valueOf(i)).append("\r\n");
				state.writeObject(writer, original);
				writer.append("\r\n");
			}
		}
		writer.flush();

		for (int i = 0; i < getSoundCount(); i++)
		{
			DEHSound sound = getSound(i);
			DEHSound original = BASEPATCH.getSound(i);
			if (!sound.equals(original))
			{
				writer.append("Sound ").append(String.valueOf(i)).append("\r\n");
				sound.writeObject(writer, original);
				writer.append("\r\n");
			}
		}
		writer.flush();

		for (int i = 0; i < getWeaponCount(); i++)
		{
			DEHWeapon weapon = getWeapon(i);
			DEHWeapon original = BASEPATCH.getWeapon(i);
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

		for (int i = 0; i < getAmmoCount(); i++)
		{
			DEHAmmo ammo = getAmmo(i);
			DEHAmmo original = BASEPATCH.getAmmo(i);
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

		DEHMiscellany misc = getMiscellany();
		DEHMiscellany miscOriginal = BASEPATCH.getMiscellany();
		if (!misc.equals(miscOriginal))
		{
			writer.append("Misc ").append(String.valueOf(0)).append("\r\n");
			misc.writeObject(writer, miscOriginal);
			writer.append("\r\n");
		}
		writer.flush();

		for (int i = 0; i < getActionPointerCount(); i++)
		{
			DEHActionPointer action = getActionPointer(i);
			DEHActionPointer original = BASEPATCH.getActionPointer(i);
			if (!action.equals(original))
			{
				writer.append("Pointer ")
					.append(String.valueOf(i))
					.append(" (Frame ")
					.append(String.valueOf(BASEPATCH.getActionPointerFrame(i)))
					.append(")")
					.append("\r\n");
				writer.append("Codep Frame = ").append(String.valueOf(action.getFrame())).append("\r\n");
				writer.append("\r\n");
			}
		}
		writer.flush();

		for (int i = 0; i < getStringCount(); i++)
		{
			String str = getString(i);
			String original = BASEPATCH.getString(i);
			if (!str.equals(original))
			{
				writer.append("Text ")
					.append(String.valueOf(original.length()))
					.append(" ")
					.append(String.valueOf(str.length()))
					.append("\r\n");
				writer.append(original).append(str);
				if (i < getStringCount() - 1)
					writer.append("\r\n");
				writer.flush();
			}
		}
	}
	
}
