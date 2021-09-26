package net.mtrop.doom.tools.struct;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	 * Creates a new interval map with a default value interval.
	 * @param min the minimum index range (inclusive).
	 * @param max the maximum index range (inclusive).
	 * @param value the value (can be null).
	 */
	public IntervalMap(int min, int max, V value)
	{
		this.intervalList = new ArrayList<>(4);
		this.intervalList.add(new Interval(min, max, value));
	}

	/**
	 * Sets a value interval.
	 * @param index the index value.
	 * @param value the value to set (can be null).
	 */
	public void set(int index, V value)
	{
		set(index, index, value);
	}
	
	/**
	 * Sets a value interval.
	 * @param value the value to set.
	 * @param min the min index.
	 * @param max the max index.
	 */
	public void set(int min, int max, V value)
	{
		int actualMin = Math.min(min, max);
		int actualMax = Math.max(min, max);
		min = actualMin;
		max = actualMax;
		int minSlot = search(min);
		int maxSlot = search(max);
		int size = intervalList.size();
		Interval newInterval = new Interval(min, max, value);
		
		if (minSlot < 0)
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
				}
			}
			else
			{
				while (!intervalList.isEmpty() && newInterval.max >= intervalList.get(0).max)
					intervalList.remove(0);
				
				if (intervalList.isEmpty())
				{
					intervalList.add(newInterval);
					return;
				}
				
				Interval headInterval = intervalList.get(0);
				if (Objects.equals(newInterval.value, headInterval.value))
				{
					headInterval.min = newInterval.min;
					return;
				}
				
				if (newInterval.max >= headInterval.min)
					headInterval.min = newInterval.max + 1;
				intervalList.add(0, newInterval);
			}
		}
		// completely after
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
			else
			{
				intervalList.add(newInterval);
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
				return;
			}
			
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
		// inside one interval.
		else if (minSlot == maxSlot)
		{
			Interval middleInterval = intervalList.get(minSlot);
			if (Objects.equals(newInterval.value, middleInterval.value))
			{
				return;
			}
			else if (middleInterval.min == newInterval.min)
			{
				if (middleInterval.max == newInterval.max)
				{
					middleInterval.value = newInterval.value;
				}
				else // new interval max is less
				{
					middleInterval.min = newInterval.max + 1;
					intervalList.add(minSlot, newInterval);
				}
			}
			else if (middleInterval.max == newInterval.max)
			{
				middleInterval.max = newInterval.min - 1;
				intervalList.add(minSlot + 1, newInterval);
			}
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
			
			Interval firstInterval = intervalList.get(minSlot);
			Interval secondInterval = intervalList.get(minSlot + 1);
			
			if (newInterval.min == firstInterval.min)
			{
				if (newInterval.max == secondInterval.max)
				{
					// delete both
					intervalList.get(minSlot);
					intervalList.get(minSlot);
					intervalList.add(minSlot, newInterval);
				}
				else if (Objects.equals(newInterval.value, firstInterval.value))
				{
					secondInterval.min = newInterval.max + 1;
				}
				else if (Objects.equals(newInterval.value, secondInterval.value))
				{
					secondInterval.min = newInterval.min;
					intervalList.get(minSlot);
				}
				else
				{
					secondInterval.min = newInterval.max + 1;
					intervalList.remove(minSlot);
					intervalList.add(minSlot, newInterval);
				}
			}
			else if (newInterval.max == secondInterval.max)
			{
				
			}
			
			// new.mix = second.max
				// equal value
			
			
			// TODO: Finish. 
		}
	}
	
	/**
	 * Fetches a value at an interval index.
	 * @param index the index.
	 * @return the corresponding value.
	 */
	public V get(int index)
	{
		int idx = search(index);
		if (idx < 0)
			return null;
		else if (idx >= intervalList.size())
			return null;
		else
			return intervalList.get(idx).value;
	}
	
	// Attempts to merge intervals by same value going backwards in the list.
	private void attemptLeftMerge(int startSlot)
	{
		// TODO: Finish.
	}
	
	// Searches for a suitable interval index to fetch or start an insert.
	// Returns index, -1 to insert at the beginning, the list size to add at the end.
	private int search(int index)
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
	
	/**
	 * Interval object.
	 * Bounds values are inclusive.
	 */
	private class Interval
	{
		private int min;
		private int max;
		private V value;
		
		private Interval(int min, int max, V value)
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
		public boolean includes(int index)
		{
			return min >= index && index <= max;
		}
		
		@Override
		public String toString() 
		{
			return "[" + min + ", " + max + "]: " + String.valueOf(value);
		}
	}
	
}
