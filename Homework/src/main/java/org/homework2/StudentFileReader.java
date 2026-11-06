package org.homework2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentFileReader {

    public static List<Student> readStudentsFromFile(String filePath) throws IOException {
        List<Student> students = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            List<String> currentLines = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (!currentLines.isEmpty()) {
                        Student student = parseStudent(currentLines);
                        if (student != null) {
                            students.add(student);
                        }
                        currentLines.clear();
                    }
                } else {
                    currentLines.add(line.trim());
                }
            }

            if (!currentLines.isEmpty()) {
                Student student = parseStudent(currentLines);
                if (student != null) {
                    students.add(student);
                }
            }
        }

        return students;
    }

    private static Student parseStudent(List<String> lines) {
        if (lines.isEmpty()) {
            return null;
        }

        String[] studentData = lines.getFirst().split("\\|");
        if (studentData.length != 3) {
            throw new IllegalArgumentException("Неверный формат данных студента: " + lines.getFirst());
        }

        String name = studentData[0].trim();
        int age = Integer.parseInt(studentData[1].trim());
        String gender = studentData[2].trim();

        List<Book> books = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] bookData = lines.get(i).split("\\|");
            if (bookData.length == 5) {
                String title = bookData[0].trim();
                String author = bookData[1].trim();
                String publisher = bookData[2].trim();
                int year = Integer.parseInt(bookData[3].trim());
                int pages = Integer.parseInt(bookData[4].trim());

                books.add(new Book(title, author, publisher, year, pages));
            }
        }

        return new Student(books, name, age, gender);
    }
}