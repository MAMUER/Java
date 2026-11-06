package org.homework;

import java.util.Objects;

public class MyHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    private Node<K, V>[] table;

    private int size;

    static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    @SuppressWarnings("unchecked")
    public MyHashMap() {
        table = new Node[DEFAULT_CAPACITY];
        size = 0;
    }

    private int hash(K key) {
        if (key == null) return 0;
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    private int indexFor(int hash, int length) {
        return hash & (length - 1);
    }

    public V put(K key, V value) {
        int h = hash(key);
        int index = indexFor(h, table.length);

        if (table[index] == null) {
            table[index] = new Node<>(h, key, value, null);
            size++;
            resizeIfNeeded();
            return null;
        }

        Node<K, V> current = table[index];
        Node<K, V> prev = null;

        while (current != null) {
            if (current.hash == h && (Objects.equals(key, current.key))) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            prev = current;
            current = current.next;
        }

        if (prev == null) {
            table[index] = new Node<>(h, key, value, null);
        } else {
            prev.next = new Node<>(h, key, value, null);
        }
        size++;
        resizeIfNeeded();
        return null;
    }

    public V get(K key) {
        int h = hash(key);
        int index = indexFor(h, table.length);

        Node<K, V> current = table[index];
        while (current != null) {
            if (current.hash == h && (Objects.equals(key, current.key))) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    public V remove(K key) {
        int h = hash(key);
        int index = indexFor(h, table.length);

        Node<K, V> current = table[index];
        Node<K, V> prev = null;

        while (current != null) {
            if (current.hash == h && (Objects.equals(key, current.key))) {
                if (prev == null) {
                    table[index] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return current.value;
            }
            prev = current;
            current = current.next;
        }
        return null;
    }

    private void resizeIfNeeded() {
        if (size < (int) (table.length * LOAD_FACTOR)) {
            return;
        }
        resize();
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTable = table;
        int newCapacity = oldTable.length * 2;
        table = new Node[newCapacity];
        size = 0;
        for (Node<K, V> head : oldTable) {
            Node<K, V> current = head;
            while (current != null) {
                Node<K, V> next = current.next;
                int index = current.hash & (newCapacity - 1);
                current.next = table[index];
                table[index] = current;
                size++;
                current = next;
            }
        }
    }

    public int size() {
        return size;
    }
}
