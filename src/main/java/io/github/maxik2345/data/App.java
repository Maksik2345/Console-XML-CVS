package io.github.maxik2345.data;

import io.github.maxik2345.data.model.DataRecord;
import io.github.maxik2345.data.parser.CsvDataParser;
import io.github.maxik2345.data.parser.DataParser;
import io.github.maxik2345.data.parser.XmlDataParser;
import io.github.maxik2345.data.statistics.DataStatistics;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class App {
    private final DataStatistics statistics = new DataStatistics();

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Введите путь к CSV или XML (exit — выход): ");
                if (!scanner.hasNextLine()) {
                    return;
                }

                String input = scanner.nextLine().strip();
                if ("exit".equalsIgnoreCase(input)) {
                    return;
                }
                if (input.isEmpty()) {
                    System.out.println("Введите путь к файлу.");
                    continue;
                }

                try {
                    processFile(Path.of(input));
                } catch (NoSuchFileException e) {
                    System.out.println("Файл не найден: " + e.getFile());
                } catch (InvalidPathException e) {
                    System.out.println("Некорректный путь: " + e.getReason());
                } catch (IOException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }
        }
    }

    private void processFile(Path path) throws IOException {
        DataParser parser = selectParser(path);

        long start = System.nanoTime();
        List<DataRecord> records = parser.parse(path);
        Map<DataRecord, Integer> occurrences = statistics.countOccurrences(records);
        Map<String, int[]> buildingsByCity = statistics.countBuildingsByCity(occurrences.keySet());
        double elapsedMillis = (System.nanoTime() - start) / 1_000_000.0;

        printDuplicates(occurrences);
        printBuildingsByCity(buildingsByCity);
        System.out.printf("Время обработки: %.3f мс%n%n", elapsedMillis);
    }

    private static DataParser selectParser(Path path) throws IOException {
        String filename = path.toString().toLowerCase(Locale.ROOT);
        if (filename.endsWith(".csv")) {
            return new CsvDataParser();
        }
        if (filename.endsWith(".xml")) {
            return new XmlDataParser();
        }
        throw new IOException("Поддерживаются файлы с расширением .csv или .xml");
    }

    private static void printDuplicates(Map<DataRecord, Integer> occurrences) {
        System.out.println("Дублирующиеся записи:");
        boolean found = false;
        for (Map.Entry<DataRecord, Integer> entry : occurrences.entrySet()) {
            int count = entry.getValue();
            if (count > 1) {
                DataRecord record = entry.getKey();
                System.out.printf("%s, %s, дом %d, этажей %d — появлений: %d%n",
                        record.city(), record.street(), record.house(), record.floor(), count);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Дубликатов нет.");
        }
    }

    private static void printBuildingsByCity(Map<String, int[]> buildingsByCity) {
        System.out.println("Количество зданий по этажности:");
        for (Map.Entry<String, int[]> entry : buildingsByCity.entrySet()) {
            int[] counts = entry.getValue();
            System.out.printf("%s — 1 эт.: %d, 2 эт.: %d, 3 эт.: %d, 4 эт.: %d, 5 эт.: %d%n",
                    entry.getKey(), counts[0], counts[1], counts[2], counts[3], counts[4]);
        }
    }
}