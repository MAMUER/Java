package org.homework2;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filepath = "C:\\Users\\User\\Downloads\\Homework\\src\\main\\java\\org\\homework2\\file.txt";
        try {
            List<Student> students = StudentFileReader.readStudentsFromFile(filepath);

            students.stream()
                    // 1. Вывести в консоль каждого студента (переопределить toString)
                    .peek(System.out::println)

                    // 2. Получить для каждого студента список книг
                    .map(Student::getBooks)

                    // 3. Получить книги
                    .flatMap(List::stream)

                    // 4. Отсортировать книги по количеству страниц
                    // (условие для сравнения: защита от null, если страниц нет, считаем как 0)
                    .sorted(Comparator.comparingInt(book -> book.getPages() == 0 ? 0 : book.getPages()))

                    // 5. Оставить только уникальные книги (переопределены equals/hashCode в Book)
                    .distinct()

                    // 6. Отфильтровать книги, оставив только те, которые были выпущены после 2000 года
                    .filter(book -> book.getYear() > 2000)

                    // 7. Ограничить стрим на 3 элементах
                    .limit(3)

                    // 8. Получить из книг годы выпуска
                    .map(Book::getYear)

                    // 9. Метод короткого замыкания: вернуть Optional от года
                    .findFirst()

                    // 10. При помощи методов получения значения из Optional вывести в консоль год выпуска
                    // найденной книги, либо запись о том, что такая книга отсутствует
                    .ifPresentOrElse(year -> System.out.println("Год выпуска найденной книги: " + year),
                            () -> System.out.println("Такая книга отсутствует"));
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка валидации данных: " + e.getMessage());
        }
    }
}