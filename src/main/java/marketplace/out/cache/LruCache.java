package marketplace.out.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Простой LRU-кэш на базе LinkedHashMap.
 */

public class LruCache<K, V> {
    private final int capacity;
    private final Map<K, V> map;

    public LruCache(int capacity){
        this.capacity = Math.max(1, capacity);
        this.map = new LinkedHashMap<K, V>(capacity,0.75f, true){
            @Override
            protected boolean removeEldestEntry(Map.Entry<K,V> eldest){
                return size() > LruCache.this.capacity;
            }
        };
    }

    public synchronized V get(K key) {
        return map.get(key);
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized void invalidateAll(){
        map.clear();
    }

}
