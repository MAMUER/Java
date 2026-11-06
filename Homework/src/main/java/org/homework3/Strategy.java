package org.homework3;

// Интерфейс стратегии
interface ReadingStrategy {
    void read(Book book);
}

// Конкретная стратегия 1
class SkimReadingStrategy implements ReadingStrategy {
    @Override
    public void read(Book book) {
        System.out.println("[Стратегия] " + book.title() + " прочитана по диагонали за 10 минут.");
    }
}

// Конкретная стратегия 2
class DeepReadingStrategy implements ReadingStrategy {
    @Override
    public void read(Book book) {
        System.out.println("[Стратегия] " + book.title() + " изучена глубоко с конспектом за 5 часов.");
    }
}