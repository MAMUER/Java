package org.homework;

public class Main {

    public static MyHashMap<String, Integer> buildSampleMap() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("Apple", 1);
        map.put("Banana", 2);
        map.put("Apple", 10);
        map.remove("Banana");
        return map;
    }

    public static void main(String[] ignoredArgs) {
        MyHashMap<String, Integer> map = buildSampleMap();
        System.out.println("Apple: " + map.get("Apple"));
        System.out.println("Banana: " + map.get("Banana"));
        System.out.println("Size: " + map.size());
    }
}
