package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.patches.ConstantsID24.*;

import java.util.Map;

import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;

public final class ExtractData 
{
	public static void main(String[] args) 
	{
		int strlen = Integer.MIN_VALUE;
		
		for (Map.Entry<Integer, DEHThing> entry : DEHTHINGID24.entrySet())
		{
			strlen = Math.max(strlen, toFriendlyName(entry.getValue().getName()).length());
		}

		for (Map.Entry<Integer, DEHThing> entry : DEHTHINGID24.entrySet())
		{
			System.out.printf("#define MTF_%-" + strlen + "s %d\n", toFriendlyName(entry.getValue().getName()), entry.getKey());
		}

		strlen = Integer.MIN_VALUE;

		for (Map.Entry<Integer, DEHThing> entry : DEHTHINGID24.entrySet())
		{
			strlen = Math.max(strlen, toAlias(entry.getValue().getName()).length());
		}

		for (Map.Entry<Integer, DEHThing> entry : DEHTHINGID24.entrySet())
		{
			System.out.printf("alias thing %-" + strlen + "s %d\n", toAlias(entry.getValue().getName()), entry.getKey());
		}

		strlen = Integer.MIN_VALUE;

		for (Map.Entry<Integer, DEHWeapon> entry : DEHWEAPONID24.entrySet())
		{
			strlen = Math.max(strlen, toAlias(entry.getValue().getName()).length());
		}

		for (Map.Entry<Integer, DEHWeapon> entry : DEHWEAPONID24.entrySet())
		{
			System.out.printf("alias weapon %-" + strlen + "s %d\n", toAlias(entry.getValue().getName()), entry.getKey());
		}

	}
	
	public static String toFriendlyName(String input)
	{
		return input.toUpperCase()
			.replace(" ", "_")
			.replace(",", "")
			.replace("(", "")
			.replace(")", "")
			;
	}
	
	public static String toAlias(String input)
	{
		String s = input.replace(",", "")
			.replace("(", "")
			.replace(")", "");

		String[] tokens = s.split("\\s+");
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < tokens.length; i++)
			sb.append(Character.toUpperCase(tokens[i].charAt(0))).append(tokens[i].substring(1).toLowerCase());
		
		return sb.toString();
	}

}
