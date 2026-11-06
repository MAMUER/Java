package org.homework2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Student {
    private List<Book> books;
    private String name;
    private int age;
    private String gender;

    public Student() {
        this.books = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            this.books.add(new Book("Книга " + i, "Автор", "Издательство", 1990, 100));
        }
    }

    public Student(List<Book> books, String name, int age, String gender) {
        setBooks(books);
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public List<Book> getBooks() {
        return Collections.unmodifiableList(this.books);
    }

    public void setBooks(List<Book> books) {
        if (books == null) throw new IllegalArgumentException("Список книг не может быть null");
        if (books.size() < 5) throw new IllegalArgumentException("Минимум 5 книг. Передано: " + books.size());
        this.books = new ArrayList<>(books);
    }

    public void addBook(Book book) {
        if (book != null) this.books.add(book);
    }

    public boolean removeBook(Book book) {
        if (this.books.size() > 5) return this.books.remove(book);
        throw new IllegalStateException("Невозможно удалить книгу: должно остаться минимум 5 книг.");
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return this.age;
    }

    public String getGender() {
        return this.gender;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "Студент: " + name + ", возраст: " + age + ", пол: " + gender;
    }
}