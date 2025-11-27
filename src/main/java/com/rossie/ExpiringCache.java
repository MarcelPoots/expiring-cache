package com.rossie;


import java.util.concurrent.*;
import java.util.*;

public class ExpiringCache<K, V> {
    private final ConcurrentHashMap<K, CacheItem<V>> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public ExpiringCache(long cleanupIntervalSeconds) {
        // Schedule cleanup task
        cleaner.scheduleAtFixedRate(this::removeExpiredItems, cleanupIntervalSeconds,
                cleanupIntervalSeconds, TimeUnit.SECONDS);
    }

    public int size () {
        return cache.size();
    }

    public void put(K key, V value, long ttlSeconds) {
        long expireTime = System.currentTimeMillis() + ttlSeconds * 1000;
        cache.put(key, new CacheItem<>(value, expireTime));
    }

    public V get(K key) {
        CacheItem<V> item = cache.get(key);
        if (item == null || item.isExpired()) {
            cache.remove(key);
            return null;
        }
        return item.value;
    }

    public void remove(K key) {
        cache.remove(key);
    }

    private void removeExpiredItems() {
        long now = System.currentTimeMillis();
        for (Map.Entry<K, CacheItem<V>> entry : cache.entrySet()) {
            if (entry.getValue().expireTime < now) {
                cache.remove(entry.getKey());
            }
        }
    }

    public void shutdown() {
        cleaner.shutdown();
    }

    private static class CacheItem<V> {
        V value;
        long expireTime;

        CacheItem(V value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }


}
