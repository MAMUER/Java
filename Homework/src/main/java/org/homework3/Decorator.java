package org.homework3;

// Базовый компонент
interface Book {
    String title();

    String getDescription();
}

// Конкретный компонент
record TextBook(String title) implements Book {

    @Override
    public String getDescription() {
        return "Обычная печатная книга: " + title;
    }
}

// Базовый декоратор
abstract class BookDecorator implements Book {
    protected Book book;

    public BookDecorator(Book book) {
        this.book = book;
    }

    @Override
    public String title() {
        return book.title();
    }

    @Override
    public String getDescription() {
        return book.getDescription();
    }
}

// Конкретный декоратор
class AudiobookDecorator extends BookDecorator {
    public AudiobookDecorator(Book book) {
        super(book);
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + Аудио версия (MP3).";
    }
}