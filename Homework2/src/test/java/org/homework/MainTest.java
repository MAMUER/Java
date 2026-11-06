package org.homework;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MainTest {

    @Test
    void buildSampleMap() {
        MyHashMap<String, Integer> map = Main.buildSampleMap();
        assertEquals(10, map.get("Apple"));
        assertNull(map.get("Banana"));
        assertEquals(1, map.size());
    }
}
