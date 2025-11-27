package com.rossie;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpiringCacheTest {

    @Test
    void testExpiringCache() throws InterruptedException {
        ExpiringCache<String, String> cache = new ExpiringCache<>(5); // cleanup every 5 sec
        cache.put("key1", "value1", 3); // expires in 3 sec

        System.out.println("Initial: " + cache.get("key1")); // should print value1
        assertEquals("value1", cache.get("key1"));
        Thread.sleep(4000);
        System.out.println("After 4 sec: " + cache.get("key1")); // should print null
        assertNull(cache.get("key1"));

        cache.put("key2", "value2", 2);
        Thread.sleep(6000); // Cache should be cleaned by the ScheduledExecutorService

        assertEquals(0, cache.size());
        cache.shutdown();
    }

}