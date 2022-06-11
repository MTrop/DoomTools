/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Simple utility functions around plain objects.
 * @author Matthew Tropiano
 */
public final class ObjectUtils
{
	private ObjectUtils() {}

	/**
	 * Apply function for objects.
	 * @param input the input object to manipulate.
	 * @param applier the function to pass the input element to.
	 * @param <T> the return/input type.
	 * @return the input object.
	 */
	public static <T> T apply(T input, Consumer<T> applier)
	{
		applier.accept(input);
		return input;
	}

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
	 * Returns the first object if it is not null, otherwise returns the second. 
	 * @param <T> class that extends Object.
	 * @param testObject the first ("tested") object.
	 * @param nullReturn the Supplier to call to return if testObject is null.
	 * @return testObject if not null, nullReturn otherwise.
	 */
	public static <T> T isNull(T testObject, Supplier<T> nullReturn)
	{
		return testObject != null ? testObject : nullReturn.get();
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

	/**
	 * Creates a new immutable set from a list of elements.
	 * The underlying set is a {@link HashSet}.
	 * @param <T> the object type in the Set.
	 * @param elements the list of elements in the set.
	 * @return a new set with the provided elements in it.
	 */
	@SafeVarargs
	public static <T> Set<T> createSet(T ... elements)
	{
		HashSet<T> out = new HashSet<>();
		for (T t : elements)
			out.add(t);
		return Collections.unmodifiableSet(out);
	}

	/**
	 * Creates a new immutable sorted set from a list of elements.
	 * The underlying set is a {@link TreeSet}.
	 * @param <T> the object type in the Set.
	 * @param elements the list of elements in the set.
	 * @return a new set with the provided elements in it.
	 */
	@SafeVarargs
	public static <T> SortedSet<T> createSortedSet(T ... elements)
	{
		TreeSet<T> out = new TreeSet<>();
		for (T t : elements)
			out.add(t);
		return Collections.unmodifiableSortedSet(out);
	}

	/**
	 * Creates a new immutable sorted set from a list of strings, where case-insensitive lookup is possible.
	 * The underlying set is a {@link TreeSet}.
	 * @param elements the list of elements in the set.
	 * @return a new set with the provided elements in it.
	 */
	@SafeVarargs
	public static SortedSet<String> createCaseInsensitiveSortedSet(String ... elements)
	{
		TreeSet<String> out = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		for (String t : elements)
			out.add(t);
		return Collections.unmodifiableSortedSet(out);
	}

	/**
	 * Creates a new immutable map.
	 * The underlying map is a {@link HashMap}.
	 * @param <K> the key type.
	 * @param <V> the value type.
	 * @param entries the entries to add.
	 * @return a new map with the provided key-value pairs in it.
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> createMap(Map.Entry<K, V> ... entries)
	{
		Map<K, V> out = new HashMap<>(entries.length);
		for (Map.Entry<K, V> e : entries)
			out.put(e.getKey(), e.getValue());
		return Collections.unmodifiableMap(out);
	}

	/**
	 * Creates a new immutable sorted map.
	 * The underlying map is a {@link TreeMap}.
	 * @param <K> the key type.
	 * @param <V> the value type.
	 * @param entries the entries to add.
	 * @return a new map with the provided key-value pairs in it.
	 */
	@SafeVarargs
	public static <K extends Comparable<K>, V> SortedMap<K, V> createSortedMap(Map.Entry<K, V> ... entries)
	{
		SortedMap<K, V> out = new TreeMap<>();
		for (Map.Entry<K, V> e : entries)
			out.put(e.getKey(), e.getValue());
		return Collections.unmodifiableSortedMap(out);
	}

	/**
	 * Creates a new immutable sorted map from a keyset of strings, where case-insensitive lookup is possible.
	 * The underlying map is a {@link TreeMap}.
	 * @param <V> the value type.
	 * @param entries the entries to add.
	 * @return a new map with the provided key-value pairs in it.
	 */
	@SafeVarargs
	public static <V> SortedMap<String, V> createCaseInsensitiveSortedMap(Map.Entry<String, V> ... entries)
	{
		SortedMap<String, V> out = new TreeMap<>();
		for (Map.Entry<String, V> e : entries)
			out.put(e.getKey(), e.getValue());
		return Collections.unmodifiableSortedMap(out);
	}

	/**
	 * Creates a new immutable list.
	 * The underlying map is an {@link ArrayList}.
	 * @param <T> the object type in the List.
	 * @param entries the entries to add.
	 * @return a new map with the provided key-value pairs in it.
	 */
	@SafeVarargs
	public static <T> List<T> createList(T ... entries)
	{
		List<T> out = new ArrayList<>(entries.length);
		for (T t : entries)
			out.add(t);
		return Collections.unmodifiableList(out);
	}

	/**
	 * Creates a simple key-value entry.
	 * @param key the key.
	 * @param value the value.
	 * @return a new entry.
	 * @see #createMap(java.util.Map.Entry...)
	 */
	public static <K, V> Map.Entry<K, V> keyValue(K key, V value)
	{
		KeyValue<K, V> out = new KeyValue<>();
		out.key = key;
		out.value = value;
		return out;
	}

	/**
	 * A single replacer for the text replacers.
	 */
	private static class KeyValue<K, V> implements Map.Entry<K, V>
	{
		private K key;
		private V value;
	
		@Override
		public K getKey()
		{
			return key;
		}
		
		@Override
		public V getValue()
		{
			return value;
		}
		
		@Override
		public V setValue(V value)
		{
			V old = this.value;
			this.value = value;
			return old;
		}
	}
	
}
