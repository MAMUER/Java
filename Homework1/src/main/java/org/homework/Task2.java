package org.homework;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Task2 {
    public static void main(String[] args) {
        runTest("input1.testfile", "checkOutput1.testfile", "Тест файл 1");
    }

    static void runTest(String inputPath, String expectedPath, String label) {
        String input = getInput(inputPath);
        String actual = logic(input);
        String expected = getInput(expectedPath);
        printResult(label, actual, expected, input);
    }

    static String logic(String input) {
        List<List<Integer>> matrix = parseMatrix(input);
        int n = matrix.size();
        int m = matrix.getFirst().size();
        int[][] sum = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int s = 0;
                if (i > 0) {
                    s += matrix.get(i - 1).get(j);
                }
                if (i < n - 1) {
                    s += matrix.get(i + 1).get(j);
                }
                if (j > 0) {
                    s += matrix.get(i).get(j - 1);
                }
                if (j < m - 1) {
                    s += matrix.get(i).get(j + 1);
                }
                sum[i][j] = s;
            }
        }
        return formatMatrix(sum);
    }

    static List<List<Integer>> parseMatrix(String input) {
        List<List<Integer>> matrix = new ArrayList<>();
        for (String line : input.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            List<Integer> row = new ArrayList<>();
            for (String tok : line.split("\\s+")) {
                if (!tok.isEmpty()) {
                    row.add(Integer.parseInt(tok));
                }
            }
            if (!row.isEmpty()) {
                matrix.add(row);
            }
        }
        return matrix;
    }

    static String formatMatrix(int[][] sum) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : sum) {
            for (int v : row) {
                sb.append(String.format("%4d", v));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    static String getInput(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            return "";
        }
    }

    static void printResult(String label, String actual, String expected, String input) {
        boolean ok = checkResult(actual, expected);
        System.out.println(label + ", результат " + (ok ? "ОК" : "Failed") + ":");
        System.out.println("Входной файл:");
        System.out.print(input);
        if (!input.isEmpty() && !input.endsWith("\n")) {
            System.out.println();
        }
        System.out.println();
        System.out.println("Результат:");
        System.out.print(actual);
        if (!ok) {
            System.out.println();
            System.out.println("Ожидали:");
            System.out.print(expected);
            System.out.println();
        }
        System.out.println();
    }

    static boolean checkResult(String actual, String expected) {
        return actual.equals(expected);
    }
}
