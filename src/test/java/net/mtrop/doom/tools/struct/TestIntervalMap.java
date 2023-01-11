/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.IOException;

public final class TestIntervalMap 
{
	public static void main(String[] args) throws IOException 
	{
		IntervalMap<String> map;
		map = new IntervalMap<>(0, 10, "apple");
		System.out.println(map);
		map.set(11, 20, "banana");
		System.out.println(map);
		map.set(-10, -1, "carrot");
		System.out.println(map);
		map.set(5, 15, "durian");
		System.out.println(map);
		map.set(0, 4, "eggplant");
		System.out.println(map);
		map.set(0, 6, "frankfurter");
		System.out.println(map);
		map.set(4, 15, "grape");
		System.out.println(map);
		
		for (long i = map.getMinIndex(); i <= map.getMaxIndex(); i++)
			System.out.println(i + ": " + map.get(i));
		
		map.set(0, 20, "haggis");
		System.out.println(map);
		map.set(0, 20, "carrot");
		System.out.println(map);
		map.set(-20, 30, "icaco");
		System.out.println(map);
		map.set(20, 30, "jello");
		System.out.println(map);
		map.set(-20, -10, "kreblach");
		System.out.println(map);
		map.set(-50, 50, "lavender");
		System.out.println(map);
		map.set(60, 80, "muenster");
		System.out.println(map);
		map.set(-80, -60, "nachos");
		System.out.println(map);
		map.set(60, null);
		System.out.println(map);
		map.set(50, null);
		System.out.println(map);
		map.set(81, 100, "muenster");
		System.out.println(map);
		map.set(-100, 101, "orange");
		System.out.println(map);
		map.set(0, 10, "pistachio");
		System.out.println(map);
		map.set(0, 10, "orange");
		System.out.println(map);
		map.set(0, 200, "quinoa");
		System.out.println(map);
		map.set(-200, -50, "radish");
		System.out.println(map);
		map.set(-200, 200, null);
		System.out.println(map);
	}
}
