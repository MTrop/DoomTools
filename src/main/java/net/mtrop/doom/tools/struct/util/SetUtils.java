/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Simple utility functions around Sets.
 * @author Matthew Tropiano
 */
public final class SetUtils
{
	/**
	 * Creates a new instance of a class from a class type.
	 * This essentially calls {@link Class#getDeclaredConstructor(Class...)} with no arguments 
	 * and {@link Class#newInstance()}, but wraps the call in a try/catch block that only throws an exception if something goes wrong.
	 * @param <T> the return object type.
	 * @param clazz the class type to instantiate.
	 * @return a new instance of an object.
	 * @throws RuntimeException if instantiation cannot happen, either due to
	 * a non-existent constructor or a non-visible constructor.
	 */
	private static <T> T create(Class<T> clazz)
	{
		Object out = null;
		try {
			out = clazz.getDeclaredConstructor().newInstance();
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		
		return clazz.cast(out);
	}
	
	/**
	 * Returns a new Set that is the union of the objects in two sets,
	 * i.e. a set with all objects from both sets.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S union(S set1, S set2)
	{
		Set<T> out = create(set1.getClass());
		for (T val : set1)
			out.add(val);
		for (T val : set2)
			out.add(val);
		return (S)out;
	}

	/**
	 * Returns a new Set that is the intersection of the objects in two sets,
	 * i.e. the objects that are present in both sets.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set table that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S intersection(S set1, S set2)
	{
		Set<T> out = create(set1.getClass());
		
		S bigset = set1.size() > set2.size() ? set1 : set2;
		S smallset = bigset == set1 ? set2 : set1;
		
		for (T val : smallset)
		{
			if (bigset.contains(val))
				out.add(val);
		}
		return (S)out;
	}

	/**
	 * Returns a new Set that is the difference of the objects in two sets,
	 * i.e. the objects in the first set minus the objects in the second.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set table that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S difference(S set1, S set2)
	{
		Set<T> out = create(set1.getClass());
		for (T val : set1)
		{
			if (!set2.contains(val))
				out.add(val);
		}
		return (S)out;
	}

	/**
	 * Returns a new Set that is the union minus the intersection of the objects in two sets.
	 * @param <T> the object type in the provided set.
	 * @param <S> the set table that contains type T. 
	 * @param set1 the first set.
	 * @param set2 the second set.
	 * @return a new set.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends Set<T>> S xor(S set1, S set2)
	{
		Set<T> out = create(set1.getClass());
		for (T val : set1)
		{
			if (!set2.contains(val))
				out.add(val);
		}
		for (T val : set2)
		{
			if (!set1.contains(val))
				out.add(val);
		}
		return (S)out;
	}

}
