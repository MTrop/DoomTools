package net.mtrop.doom.tools.decohack;

import java.io.BufferedReader;
import java.io.IOException;

import net.mtrop.doom.tools.common.Common;

public final class JunkMain 
{
	public static void main(String[] args) throws IOException
	{
		try (BufferedReader br = Common.openTextStream(Common.openResource("decohack/constants/boom/strings.dh")))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith("#define") || line.contains(" STR_"))
				{
					String[] s = line.split("\\s+");
					System.out.println(line.replace("?", s[1].substring(4)));
				}
				else
				{
					System.out.println(line);
				}
			}
		}
	}
}
