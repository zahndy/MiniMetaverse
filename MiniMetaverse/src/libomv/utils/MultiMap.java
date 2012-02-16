package libomv.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A variation of Map which allows multiple values to be stored per key.
 * </p>
 * 
 * <p>
 * Where a collection of values is returned, the collection is unmodifiable. If
 * changes are to be made to the MultiMap, they should be made through one of
 * the MultiMap's methods.
 * </p>
 * 
 * <p>
 * This implementation uses an internal Map to hold Collections of values mapped
 * to the keys. The default is a HashMap but other maps can be supplied to the
 * constructor.
 * </p>
 * 
 * <p>
 * By default, the collections of values are stored in ArrayLists, one ArrayList
 * for each key. The ArrayLists are created from a CollectionFactory.
 * Constructors exist to allow a differentCollectionFactory to be supplied. This
 * allows flexibility in the precise behaviour of the MultiMap. Supplying a
 * factory which creates sets will prevent duplicates from being stored. Using
 * TreeSets will keep the values in order, etc...
 * </p>
 * 
 * <p>
 * </p>
 * 
 * @author chris
 * 
 * @param <K>
 *            Key
 * @param <V>
 *            Value
 */
public class MultiMap<K, V>
{
	private final Map<K, Collection<V>> inner;
	private int valueCount;

	/**
	 * Creates a MultiMap which uses a HashMap as the underlying map and
	 * ArrayLists to store the values.
	 */
	public MultiMap()
	{
		this(new HashMap<K, Collection<V>>());
	}

	/**
	 * Creates a MultiMap which allows the programmer to specify the underlying
	 * map and uses ArrayLists to store the values.
	 * 
	 * @param innerMap
	 */
	public MultiMap(Map<K, Collection<V>> innerMap)
	{
		this.inner = innerMap;
		valueCount = 0;
	}

	/**
	 * Retrieves all the values associated with the supplied key.
	 * 
	 * @param key
	 * @return a Collection containing each of the values associated with the
	 *         key, or null if no values are associated with this key.
	 */
	public Collection<V> get(Object key)
	{
		Collection<V> values = inner.get(key);
		if (values == null)
		{
			return null;
		}
		return Collections.unmodifiableCollection(inner.get(key));
	}

	/**
	 * Returns the number of values stored in this MultiMap. This method is
	 * synonymous with the valueCount() method and was included for consistency
	 * with the standard Java Collections.
	 * 
	 * @return the number of values stored in the MultiMap.
	 */
	public int size()
	{
		return valueCount();
	}

	/**
	 * Returns the number of keys in this MultiMap.
	 * 
	 * @return the number of keys.
	 */
	public int keyCount()
	{
		return inner.size();
	}

	/**
	 * Returns the number of values stored in this MultiMap. This method is
	 * synonymous with the size() method and was included for clarity.
	 * 
	 * @return the number of values stored in the MultiMap.
	 */
	public int valueCount()
	{
		return valueCount;
	}

    /**
     * Gets an iterator for the collection mapped to the specified key.
     * 
     * @param key  the key to get an iterator for
     * @return the iterator of the collection at the key, empty iterator if key not in map
     * @since Commons Collections 3.1
     */
    public Iterator<V> iterator(K key)
    {
        return new MultiMapIterator(key, inner.get(key));
    }

    /**
	 * Associates the supplied value with the supplied key. MultiMaps allow
	 * multiple values per key but duplicates are only allowed if the underlying
	 * Collection supplied to the constructor also allows them. The default
	 * ArrayList does allow duplicates.
	 * 
	 * @param key
	 * @param value
	 */
	public void put(K key, V value)
	{
		Collection<V> values = inner.get(key);
		if (values == null)
		{
			values = new ArrayList<V>();
			inner.put(key, values);
		}
		if (values.add(value))
		{
			valueCount++;
		}
	}

	/**
	 * Removes the supplied key from the MultiMap and returns all of the values
	 * previously associated with it.
	 * 
	 * @param key
	 * @return a collections containing the values previously associated with
	 *         the key, or null if no values were previously associated with it.
	 */
	public Collection<V> remove(Object key)
	{
		final Collection<V> removed = inner.remove(key);
		if (removed == null)
		{
			return null;
		}
		valueCount -= removed.size();
		return Collections.unmodifiableCollection(removed);
	}

	/**
	 * Removes the mapping between the supplied key and value. The return value
	 * reports the success of the operation.
	 * 
	 * @param key
	 * @param value
	 * @return true if the value was successfully removed, false otherwise.
	 */
	public boolean remove(Object key, Object value)
	{

		final Collection<V> values = inner.get(key);

		if (values == null)
		{
			return false;
		}
		if (!values.remove(value))
		{
			return false;
		}

		if (values.isEmpty())
		{
			inner.remove(key);
		}
		valueCount--;
		return true;
	}

	/**
	 * Empties the Multimap.
	 */
	public void clear()
	{
		inner.clear();
		valueCount = 0;
	}

	/**
	 * Checks if the MultiMap is empty.
	 * 
	 * @return true if it is empty, false otherwise.
	 */
	public boolean isEmpty()
	{
		return valueCount == 0;
	}

	/**
	 * Checks if any values are associated with the supplied key.<br>
	 * <br>
	 * Note: If the key was previously added to the MultiMap and all of its
	 * values have subsequently been removed, this method will return false.
	 * 
	 * @param key
	 * @return true if values are associated with this key, false otherwise.
	 */
	public boolean containsKey(Object key)
	{
		return inner.containsKey(key);
	}

	/**
	 * Checks if this value is associated with any keys.
	 * 
	 * @param value
	 * @return true if the value is contained in the MultiMap, false otherwise.
	 */
	public boolean containsValue(Object value)
	{
		for (Collection<V> values : inner.values())
		{
			if (values.contains(value))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds all the mappings in the supplied Map to this MultiMap.
	 * 
	 * @param m
	 *            the map containing the key-value pairs to be added.
	 */
	public void putAll(Map<? extends K, ? extends V> m)
	{
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Adds all the mappings in the supplied MultiMap to this MultiMap.
	 * 
	 * @param m
	 *            the MultiMap containing the key-value pairs to be added.
	 */
	public void putAll(MultiMap<? extends K, ? extends V> m)
	{
		for (K key : m.inner.keySet())
		{
			for (V value : m.get(key))
			{
				put(key, value);
			}
		}
	}

	/**
	 * Retrieves all the keys in the MulitMap.
	 * 
	 * @return a Set containing all the keys in the multimap.
	 */
	public Set<K> keySet()
	{
		return inner.keySet();
	}

	/**
	 * Retrieves all the values contained in the MultiMap. This method is
	 * supplied as a convenience but is likely to have poor performance.
	 * 
	 * @return a Collection containing all the values in the MultiMap.
	 */
	public Collection<V> values()
	{
		final Collection<V> allValues = new ArrayList<V>(valueCount());
		for (Collection<V> values : inner.values())
		{
			allValues.addAll(values);
		}
		return Collections.unmodifiableCollection(allValues);
	}

	/**
	 * A standard Map-like entry set. Each key is mapped to a collection
	 * containing all its values.
	 * 
	 * @return the entry set.
	 */
	public Set<Map.Entry<K, Collection<V>>> entrySet()
	{
		return inner.entrySet();
	}

	/**
	 * Returns an entry set where each key is mapped to one value. As a
	 * consequence of this, each key may appear multiple times in the entry set.
	 * This method is supplied as a convenience and may have poor performance.
	 * 
	 * @return
	 */
	public Collection<MultiMapEntry> expandedEntries()
	{
		final Collection<MultiMapEntry> entries = new ArrayList<MultiMapEntry>(valueCount());
		for (Map.Entry<K, Collection<V>> e : entrySet())
		{
			final K key = e.getKey();
			for (V value : e.getValue())
			{
				entries.add(new MultiMapEntry(key, value));
			}
		}
		return Collections.unmodifiableCollection(entries);
	}

	@Override
	public String toString()
	{
		String string = String.format("(%d keys, %d values\n", inner.size(), valueCount);
		for (Map.Entry<K, Collection<V>> e : entrySet())
		{
			string = string + " " + e.getKey().toString() + ", count: " + e.getValue().size() + " " + e.getValue().toString() + "\n";
		}
		return string + ")";
	}
	
	/**
	 * Holds key-value pairs, used as a return type for the expandedEntries()
	 * method.
	 * 
	 * @author chris
	 * 
	 */
	public class MultiMapEntry
	{

		private final K key;
		private final V value;

		private MultiMapEntry(K key, V value)
		{
			this.key = key;
			this.value = value;
		}

		/**
		 * 
		 * @return the key.
		 */
		public K getKey()
		{
			return key;
		}

		/**
		 * 
		 * @return the value.
		 */
		public V getValue()
		{
			return value;
		}

	}
	private class MultiMapIterator implements Iterator<V>
	{
		Iterator<V> iter;
		K key;
		
		public MultiMapIterator(K key, Collection<V> coll)
		{
			this.iter = coll != null ? coll.iterator() : null;
			this.key = key;
		}

		@Override
		public boolean hasNext()
		{
			return iter != null ? iter.hasNext() : false;
		}

		@Override
		public V next()
		{
			return iter != null ? iter.next() : null;
		}

		@Override
		public void remove()
		{
			if (iter != null)
			{
				iter.remove();
				valueCount--;
				if (!iter.hasNext())
					inner.remove(key);
			}
			else
			{
			    throw new UnsupportedOperationException();
			}
		}
	}
}
