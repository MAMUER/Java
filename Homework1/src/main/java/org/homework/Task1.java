package org.homework;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class Task1 {
    public static void main(String[] args) {
        runTest("input1.testfile", "checkOutput1.testfile", "Тест файл 1");
        runTest("input2.testfile", "checkOutput2.testfile", "Тест файл 2");
    }

    static void runTest(String inputPath, String expectedPath, String label) {
        String input = getInput(inputPath);
        String actual = logic(input);
        String expected = getInput(expectedPath);
        printResult(label, actual, expected);
    }

    static String logic(String input) {
        return input;
    }

    static String getInput(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            return "";
        }
    }

    static void printResult(String label, String actual, String expected) {
        boolean ok = checkResult(actual, expected);
        System.out.println(label + " результат " + (ok ? "ОК" : "Failed") + ":");
        if (!ok) {
            System.out.println("Результат:");
            System.out.print(actual);
            if (!actual.isEmpty() && !actual.endsWith("\n")) {
                System.out.println();
            }
            System.out.println();
            System.out.println("Ожидали:");
            System.out.print(expected);
            if (!expected.isEmpty() && !expected.endsWith("\n")) {
                System.out.println();
            }
        } else {
            System.out.print(actual);
            if (!actual.isEmpty() && !actual.endsWith("\n")) {
                System.out.println();
            }
        }
        System.out.println();
    }

    static boolean checkResult(String actual, String expected) {
        return actual.equals(expected);
    }
}
