package org.homework3;

// Целевой интерфейс новой системы
interface LibraryCatalog {
    String searchBook(String title);
}

// Устаревший класс, который мы не можем менять
class OldLibrarySystem {
    public String findBookLegacy(String bookName) {
        return "Результат из старой базы: книга '" + bookName + "' найдена на полке 4.";
    }
}

// Адаптер
class CatalogAdapter implements LibraryCatalog {
    private final OldLibrarySystem oldSystem;

    public CatalogAdapter(OldLibrarySystem oldSystem) {
        this.oldSystem = oldSystem;
    }

    @Override
    public String searchBook(String title) {
        // Преобразуем вызов нового метода в вызов старого
        return oldSystem.findBookLegacy(title);
    }
}