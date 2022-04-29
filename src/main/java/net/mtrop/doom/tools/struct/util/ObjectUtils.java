/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.util;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Simple utility functions around plain objects.
 * @author Matthew Tropiano
 */
public final class ObjectUtils
{
	private ObjectUtils() {}

	/**
	 * Returns if two objects are equal, performing null checking. 
	 * @param a the first object.
	 * @param b the second object.
	 * @return true if equal, false if not.
	 * @see Object#equals(Object)
	 */
	public static boolean areEqual(Object a, Object b)
	{
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		else
			return a.equals(b);
	}

	/**
	 * Returns the first object if it is not null, otherwise returns the second. 
	 * @param <T> class that extends Object.
	 * @param testObject the first ("tested") object.
	 * @param nullReturn the object to return if testObject is null.
	 * @return testObject if not null, nullReturn otherwise.
	 */
	public static <T> T isNull(T testObject, T nullReturn)
	{
		return testObject != null ? testObject : nullReturn;
	}

	/**
	 * Returns the first object in the supplied list of objects that isn't null. 
	 * @param <T> class that extends Object.
	 * @param objects the list of objects.
	 * @return the first object that isn't null in the list, 
	 * or null if all of the objects are null.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T coalesce(T ... objects)
	{
		for (int i = 0; i < objects.length; i++)
			if (objects[i] != null)
				return objects[i];
		return null;
	}

	/**
	 * Checks if a value is "empty."
	 * The following is considered "empty":
	 * <ul>
	 * <li><i>Null</i> references.
	 * <li>{@link Array} objects that have a length of 0.
	 * <li>{@link Boolean} objects that are false.
	 * <li>{@link Character} objects that are the null character ('\0', '\u0000').
	 * <li>{@link Number} objects that are zero.
	 * <li>{@link String} objects that are the empty string, or are {@link String#trim()}'ed down to the empty string.
	 * <li>{@link Collection} objects where {@link Collection#isEmpty()} returns true.
	 * </ul> 
	 * @param obj the object to check.
	 * @return true if the provided object is considered "empty", false otherwise.
	 */
	public static boolean isEmpty(Object obj)
	{
		if (obj == null)
			return true;
		else if (obj.getClass().isArray())
			return Array.getLength(obj) == 0;
		else if (obj instanceof Boolean)
			return !((Boolean)obj);
		else if (obj instanceof Character)
			return ((Character)obj) == '\0';
		else if (obj instanceof Number)
			return ((Number)obj).doubleValue() == 0.0;
		else if (obj instanceof String)
			return ((String)obj).trim().length() == 0;
		else if (obj instanceof Collection<?>)
			return ((Collection<?>)obj).isEmpty();
		else
			return false;
	}

}
