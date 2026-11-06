package org.homework3;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Паттерн BUILDER ===");
        // Создаем студента пошагово
        Student student = Student.builder() // Вызываем статический метод
                .setName("Иван Иванов")
                .setFaculty("Информационные технологии")
                .setYearOfStudy(3)
                .setPremiumAccess(true)
                .build();
        System.out.println("Создан студент: " + student.getName());

        System.out.println("\n=== 2. Паттерн DECORATOR ===");
        // Создаем книгу и "навешиваем" на нее декоратор
        Book myBook = new TextBook("Изучаем Java");
        System.out.println("Изначально: " + myBook.getDescription());

        Book decoratedBook = new AudiobookDecorator(myBook);
        System.out.println("После декоратора: " + decoratedBook.getDescription());

        System.out.println("\n=== 3. Паттерн STRATEGY ===");
        // Студент выбирает стратегию чтения
        ReadingStrategy deepReading = new DeepReadingStrategy();
        System.out.print("Действие студента: ");
        deepReading.read(decoratedBook);

        System.out.println("\n=== 4. Паттерн ADAPTER ===");
        // Подключаем старую систему через адаптер
        OldLibrarySystem legacySystem = new OldLibrarySystem();
        LibraryCatalog catalog = new CatalogAdapter(legacySystem);
        System.out.println(catalog.searchBook("Изучаем Java"));

        System.out.println("\n=== 5. Паттерн PROXY ===");
        // Проверяем доступ к профилю. Сначала у студента есть штраф (true)
        StudentProfile proxyWithFine = new StudentProfileProxy(student, true);
        proxyWithFine.showProfile();

        // Штраф оплачен (false)
        StudentProfile proxyNoFine = new StudentProfileProxy(student, false);
        proxyNoFine.showProfile();

        System.out.println("\n=== 6. Паттерн CHAIN OF RESPONSIBILITY ===");
        // Строим цепочку: Библиотекарь -> Зав. Библиотекой -> Декан
        ApprovalHandler librarian = new Librarian();
        ApprovalHandler headLibrarian = new HeadLibrarian();
        ApprovalHandler dean = new Dean();

        librarian.setNext(headLibrarian);
        headLibrarian.setNext(dean);

        // Запрос на 5 дней (обработает Библиотекарь)
        System.out.println("Запрос 1:");
        librarian.handleRequest(new BookRequest("Алгоритмы", 5));

        // Запрос на 20 дней (пройдет мимо Библиотекаря, обработает Зав. Библиотекой)
        System.out.println("Запрос 2:");
        librarian.handleRequest(new BookRequest("Паттерны проектирования", 20));

        // Запрос на 60 дней (дойдет до Декана)
        System.out.println("Запрос 3:");
        librarian.handleRequest(new BookRequest("Искусственный интеллект", 60));
    }
}