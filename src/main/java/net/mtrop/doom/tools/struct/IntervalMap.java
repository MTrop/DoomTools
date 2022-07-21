/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * A sorted map of exclusive intervals used for defining values over a large contiguous range.
 * Size gets larger as intervals get more fragmented.
 * @author Matthew Tropiano
 * @param <V> the value type that this contains.
 */
public class IntervalMap<V>
{
	/** The main interval list. */
	private List<Interval> intervalList;
	
	/**
	 * Creates a new interval map.
	 */
	public IntervalMap()
	{
		this.intervalList = new ArrayList<>(4);
	}

	/**
	 * Creates a new interval map with a default value interval.
	 * @param minIndex the minimum index range (inclusive).
	 * @param maxIndex the maximum index range (inclusive).
	 * @param value the value (can be null).
	 */
	public IntervalMap(long minIndex, long maxIndex, V value)
	{
		this();
		set(minIndex, maxIndex, value);
	}

	/**
	 * Sets a value interval.
	 * @param index the index value.
	 * @param value the value to set (can be null).
	 */
	public void set(long index, V value)
	{
		set(index, index, value);
	}
	
	/**
	 * Sets a value interval.
	 * @param minIndex the min index.
	 * @param maxIndex the max index.
	 * @param value the value to set.
	 */
	public void set(long minIndex, long maxIndex, V value)
	{
		long actualMin = Math.min(minIndex, maxIndex);
		long actualMax = Math.max(minIndex, maxIndex);
		minIndex = actualMin;
		maxIndex = actualMax;
		int minSlot = search(minIndex);
		int maxSlot = search(maxIndex);
		int size = intervalList.size();
		Interval newInterval = new Interval(minIndex, maxIndex, value);

		// empty list.
		if (intervalList.isEmpty())
		{
			intervalList.add(newInterval);
		}
		// interval starts before.
		else if (minSlot < 0)
		{
			// completely before
			if (maxSlot < 0)
			{
				Interval headInterval = intervalList.get(0);
				if (newInterval.max < headInterval.min - 1)
				{
					intervalList.add(0, new Interval(newInterval.max + 1, headInterval.min - 1, null));
					intervalList.add(0, newInterval);
				}
				else if (Objects.equals(newInterval.value, headInterval.value))
				{
					headInterval.min = newInterval.min;
				}
				else // adjacent
				{
					intervalList.add(0, newInterval);
					reduceRight(0);
				}
			}
			else
			{
				while (!intervalList.isEmpty() && newInterval.max >= intervalList.get(0).max)
					intervalList.remove(0);
				
				if (intervalList.isEmpty())
				{
					intervalList.add(newInterval);
				}
				else
				{
					Interval headInterval = intervalList.get(0);
					if (Objects.equals(newInterval.value, headInterval.value))
					{
						headInterval.min = newInterval.min;
					}
					else
					{
						if (newInterval.max >= headInterval.min)
							headInterval.min = newInterval.max + 1;
						intervalList.add(0, newInterval);
					}
				}
			}
		}
		// interval starts after.
		else if (minSlot >= size)
		{
			Interval tailInterval = intervalList.get(size - 1);
			if (newInterval.min > tailInterval.max + 1)
			{
				intervalList.add(new Interval(tailInterval.max + 1, newInterval.min - 1, null));
				intervalList.add(newInterval);
			}
			else if (Objects.equals(newInterval.value, tailInterval.value))
			{
				tailInterval.max = newInterval.max;
			}
			else // adjacent
			{
				intervalList.add(newInterval);
				reduceLeft(intervalList.size() - 1);
			}
		}
		// new interval goes past the end.
		else if (maxSlot >= size)
		{
			while (!intervalList.isEmpty() && intervalList.get(intervalList.size() - 1).min >= newInterval.min)
				intervalList.remove(intervalList.size() - 1);
			
			if (intervalList.isEmpty())
			{
				intervalList.add(newInterval);
			}
			else
			{
				Interval tailInterval = intervalList.get(size - 1);
				if (Objects.equals(newInterval.value, tailInterval.value))
				{
					tailInterval.max = newInterval.max;
				}
				else
				{
					tailInterval.max = newInterval.min - 1;
					intervalList.add(newInterval);
				}
			}
		}
		// inside one interval.
		else if (minSlot == maxSlot)
		{
			Interval middleInterval = intervalList.get(minSlot);
			if (Objects.equals(newInterval.value, middleInterval.value))
			{
				// Do nothing.
			}
			// touches min
			else if (middleInterval.min == newInterval.min)
			{
				// complete overlap
				if (middleInterval.max == newInterval.max)
				{
					middleInterval.value = newInterval.value;
					reduceRight(minSlot);
				}
				// new interval max is less
				else
				{
					middleInterval.min = newInterval.max + 1;
					intervalList.add(minSlot, newInterval);
				}
				reduceLeft(minSlot);
			}
			// touches max
			else if (middleInterval.max == newInterval.max)
			{
				middleInterval.max = newInterval.min - 1;
				intervalList.add(minSlot + 1, newInterval);
				reduceRight(minSlot + 1);
			}
			// touches max
			else
			{
				Interval splitInterval = new Interval(newInterval.max + 1, middleInterval.max, middleInterval.value);
				middleInterval.max = newInterval.min - 1;
				intervalList.add(minSlot + 1, newInterval);
				intervalList.add(minSlot + 2, splitInterval);
			}
		}
		else
		{
			// remove complete overlaps
			int amount = maxSlot - minSlot - 1;
			int removeIndex = minSlot + 1;
			while (amount-- > 0)
				intervalList.remove(removeIndex);
			
			int firstSlot = minSlot;
			int secondSlot = minSlot + 1;

			Interval firstInterval = intervalList.get(firstSlot);
			Interval secondInterval = intervalList.get(secondSlot);
			
			// touches first min
			if (newInterval.min == firstInterval.min)
			{
				// touches second max
				if (newInterval.max == secondInterval.max)
				{
					// delete both and add into the same slot
					intervalList.remove(firstSlot);
					intervalList.remove(firstSlot); // second shuffled down into first
					intervalList.add(firstSlot, newInterval);
					reduceRight(firstSlot);
				}
				// absorb into first interval.
				else if (Objects.equals(newInterval.value, firstInterval.value))
				{
					secondInterval.min = newInterval.max + 1;
				}
				// absorb into second interval.
				else if (Objects.equals(newInterval.value, secondInterval.value))
				{
					// overlaps first completely - delete.
					intervalList.remove(firstSlot);
					secondInterval.min = newInterval.min;
				}
				else
				{
					secondInterval.min = newInterval.max + 1;
					intervalList.remove(firstSlot);
					intervalList.add(firstSlot, newInterval);
				}
				reduceLeft(firstSlot);
			}
			// touches second max
			else if (newInterval.max == secondInterval.max)
			{
				// absorb into first interval.
				if (Objects.equals(newInterval.value, firstInterval.value))
				{
					// overlaps second completely - delete.
					intervalList.remove(secondSlot);
					firstInterval.max = newInterval.max;
				}
				// absorb into second interval.
				else if (Objects.equals(newInterval.value, secondInterval.value))
				{
					firstInterval.max = newInterval.min - 1;
				}
				else
				{
					firstInterval.max = newInterval.min - 1;
					intervalList.remove(secondSlot);
					intervalList.add(secondSlot, newInterval);
				}
				reduceRight(secondSlot);
			}
			// overlap, no touch.
			else
			{
				// absorb into first interval.
				if (Objects.equals(newInterval.value, firstInterval.value))
				{
					secondInterval.min = newInterval.max + 1;
				}
				// absorb into second interval.
				else if (Objects.equals(newInterval.value, secondInterval.value))
				{
					firstInterval.min = newInterval.min - 1;
				}
				// adjust and insert.
				else
				{
					firstInterval.max = newInterval.min - 1;
					secondInterval.min = newInterval.max + 1;
					intervalList.add(secondSlot, newInterval);
				}
			}
		}
		
		// clean up end nulls.
		while (!intervalList.isEmpty() && intervalList.get(intervalList.size() - 1).value == null)
			intervalList.remove(intervalList.size() - 1);
		while (!intervalList.isEmpty() && intervalList.get(0).value == null)
			intervalList.remove(0);
	}
	
	/**
	 * Fetches a value at an interval index.
	 * @param index the index.
	 * @return the corresponding value.
	 */
	public V get(long index)
	{
		int idx = search(index);
		if (idx < 0)
			return null;
		else if (idx >= intervalList.size())
			return null;
		else
			return intervalList.get(idx).value;
	}
	
	/**
	 * Fetches a value at an interval index, returning a default value if the value is null.
	 * @param index the index.
	 * @param ifNull if <code>get(index)</code> would return <code>null</code>, return this.
	 * @return the corresponding value, or <code>ifNull</code> if the value would be null.
	 */
	public V getOrDefault(long index, V ifNull)
	{
		V out;
		if ((out = get(index)) == null)
			return ifNull;
		return out;
	}
	
	/**
	 * Gets a set of values across an inclusive interval.
	 * Since this is a set, the order is undefined and there will not be any repeats.
	 * @param minIndex the min index.
	 * @param maxIndex the max index.
	 * @return a set of values. Can be empty.
	 */
	public Set<V> getValueSet(long minIndex, long maxIndex)
	{
		return getValueCollection(minIndex, maxIndex, new TreeSet<V>()); 
	}
	
	/**
	 * Gets a list of values across an inclusive interval.
	 * Since this is a list, the order is in interval order and there may be repeats.
	 * @param minIndex the min index.
	 * @param maxIndex the max index.
	 * @return a list of values. Can be empty.
	 */
	public List<V> getValueList(long minIndex, long maxIndex)
	{
		return getValueCollection(minIndex, maxIndex, new ArrayList<V>()); 
	}
	
	// Gets a set of values across an interval and returns them in the collection.
	private <U extends Collection<V>> U getValueCollection(long minIndex, long maxIndex, U collection)
	{
		int slotA = search(Math.min(minIndex, maxIndex));
		int slotB = search(Math.max(minIndex, maxIndex));
		
		if (slotB >= 0)
		{
			slotA = slotA < 0 ? 0 : slotA;
			slotB = slotB >= intervalList.size() ? intervalList.size() - 1 : slotB;
			for (int i = slotA; i <= slotB; i++)
			{
				V value = intervalList.get(i).value;
				if (value != null)
					collection.add(value);
			}
		}
		
		return collection;
	}
	
	/**
	 * @return the lowest index in the map (if any).
	 */
	public Long getMinIndex()
	{
		return intervalList.isEmpty() ? null : intervalList.get(0).min;
	}
	
	/**
	 * @return the highest index in the map (if any).
	 */
	public Long getMaxIndex()
	{
		return intervalList.isEmpty() ? null : intervalList.get(intervalList.size() - 1).max;
	}
	
	/**
	 * Gets how many indices are occupied by a value.
	 * @param value the value. Cannot be null.
	 * @return the amount of indices, or 0 if not found.
	 */
	public long getIndexWidth(V value)
	{
		long out = 0L;
		for (int i = 0; i < intervalList.size(); i++)
			if (Objects.equals(intervalList.get(i).value, value))
				out += intervalList.get(i).width();
		return out;
	}
	
	// Attempts to merge intervals by same value going backwards in the list.
	private void reduceLeft(int startSlot)
	{
		if (startSlot <= 0)
			return;
		if (!Objects.equals(intervalList.get(startSlot - 1).value, intervalList.get(startSlot).value))
			return;
		
		Interval newLeft = intervalList.get(startSlot);
		Interval oldLeft = intervalList.get(startSlot - 1);
		
		newLeft.min = oldLeft.min;
		intervalList.remove(startSlot - 1);
		reduceLeft(startSlot - 1);
	}

	// Attempts to merge intervals by same value going forwards in the list.
	private void reduceRight(int startSlot)
	{
		if (startSlot >= intervalList.size() - 1)
			return;
		if (!Objects.equals(intervalList.get(startSlot + 1).value, intervalList.get(startSlot).value))
			return;

		Interval newRight = intervalList.get(startSlot);
		Interval oldRight = intervalList.get(startSlot + 1);
		
		newRight.max = oldRight.max;
		intervalList.remove(startSlot + 1);
		reduceRight(startSlot);
	}

	// Searches for a suitable interval index to fetch or start an insert.
	// Returns index, -1 to insert at the beginning, the list size to add at the end.
	private int search(long index)
	{
		final int size = intervalList.size();
		
		int low = 0;
		int high = size - 1;
		
		int slot = 0;
		while (slot >= 0 && slot < size)
		{
			slot = (low + high) >>> 1;
			Interval currentInterval = intervalList.get(slot);
			if (currentInterval.includes(index))
			{
				return slot;
			}
			else if (index < currentInterval.min)
			{
				if (slot == 0)
					slot = -1;
				else
					high = slot - 1;
			}
			else // index > currentInterval.max
			{
				if (slot == size - 1)
					slot = size;
				else
					low = slot + 1;
			}
		}
		
		return slot;
	}
	
	@Override
	public String toString() 
	{
		return intervalList.toString();
	}
	
	/**
	 * Interval object.
	 * Bounds values are inclusive.
	 */
	private class Interval
	{
		private long min;
		private long max;
		private V value;
		
		private Interval(long min, long max, V value)
		{
			this.min = min;
			this.max = max;
			this.value = value;
		}
		
		/**
		 * Checks if this interval includes an index.
		 * @param index the index.
		 * @return true if the provided index is in this interval or touching a boundary.
		 */
		public boolean includes(long index)
		{
			return min <= index && index <= max;
		}
		
		public long width()
		{
			return max - min + 1;
		}
		
		@Override
		public String toString() 
		{
			return "([" + min + ", " + max + "]: " + String.valueOf(value) + ")";
		}
	}
	
}
