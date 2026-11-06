package org.homework3;

// Паттерн Builder
public class Student {
    // Неизменяемость итогового объекта
    private final String name;
    private final int yearOfStudy;
    private final String faculty;
    private final boolean hasPremiumAccess;

    private Student(Builder builder) {
        this.name = builder.name;
        this.faculty = builder.faculty;
        this.yearOfStudy = builder.yearOfStudy;
        this.hasPremiumAccess = builder.hasPremiumAccess;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public boolean hasPremiumAccess() {
        return hasPremiumAccess;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public String getFaculty() {
        return faculty;
    }

    public static class Builder { // Разделение ответственности через вложенный статический класс
        private String name;
        // Обработка значений по умолчанию
        private String faculty = "Не указан"; // Значение по умолчанию
        private int yearOfStudy = 1;
        private boolean hasPremiumAccess = false;

        // Пошаговое конструирование и Цепочка вызовов
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setFaculty(String faculty) {
            this.faculty = faculty;
            return this;
        }

        public Builder setYearOfStudy(int yearOfStudy) {
            this.yearOfStudy = yearOfStudy;
            return this;
        }

        public Builder setPremiumAccess(boolean hasPremiumAccess) {
            this.hasPremiumAccess = hasPremiumAccess;
            return this;
        }

        // Валидация инвариантов при сборке
        public Student build() {
            if (name == null) throw new IllegalStateException("Имя студента обязательно!");
            return new Student(this);
        }
    }
}