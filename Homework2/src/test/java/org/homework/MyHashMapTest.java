package org.homework;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MyHashMapTest {

    @Test
    void putAndGet() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        assertNull(map.put("Apple", 1));
        assertEquals(1, map.get("Apple"));
        assertEquals(1, map.size());
    }

    @Test
    void overwriteValue() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("Apple", 1);
        assertEquals(1, map.put("Apple", 10));
        assertEquals(10, map.get("Apple"));
        assertEquals(1, map.size());
    }

    @Test
    void remove() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("Banana", 2);
        assertEquals(2, map.remove("Banana"));
        assertNull(map.get("Banana"));
        assertEquals(0, map.size());
        assertNull(map.remove("Banana"));
    }

    @Test
    void missingKey() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        assertNull(map.get("Nope"));
        assertEquals(0, map.size());
    }

    @Test
    void nullKey() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        assertNull(map.put(null, 5));
        assertEquals(5, map.get(null));
        assertEquals(1, map.size());
        assertEquals(5, map.remove(null));
        assertNull(map.get(null));
    }

    @Test
    void multipleEntries() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("Apple", 1);
        map.put("Banana", 2);
        assertEquals(2, map.size());
        assertEquals(1, map.get("Apple"));
        assertEquals(2, map.get("Banana"));
    }
}
