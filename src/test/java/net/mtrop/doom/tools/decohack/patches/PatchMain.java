package net.mtrop.doom.tools.decohack.patches;

import java.util.ArrayList;

public final class PatchMain 
{
	public static void main(String[] args) 
	{
		ArrayList<Integer> list = new ArrayList<>();		
		for (int i = 0; i < PatchConstantsDoom19.DEHSTATE.length; i++)
			if (PatchConstantsDoom19.DEHSTATE[i].getPointerIndex() != null)
				list.add(i);
		System.out.println("{");
		for (int i : list)
			System.out.println("\t" + i + ",");
		System.out.println("}");
	}
}
