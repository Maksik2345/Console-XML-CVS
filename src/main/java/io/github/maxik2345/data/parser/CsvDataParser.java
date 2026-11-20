package io.github.maxik2345.data.parser;

import io.github.maxik2345.data.model.DataRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.newBufferedReader;

public class CsvDataParser implements DataParser{
    @Override
    public List<DataRecord> parse(Path path) throws IOException {

        CSVFormat format = CSVFormat.DEFAULT.builder().setDelimiter(';').setHeader().get();

        try (BufferedReader file = newBufferedReader(path);
             CSVParser parser = fileParse(format, file, path);) {

            List<String> headers = parser.getHeaderNames();

            for (String required : List.of("city", "street", "house", "floor")) {
                if (!headers.contains(required)) {
                    throw new IOException("В CSV отсутствует колонка " + required);
                }
            }

            List<DataRecord> records = new ArrayList<DataRecord>();

            for (CSVRecord row : parser) {
                records.add(parseRecord(row));
            }

            return records;
        } catch (UncheckedIOException e) {
            throw new IOException("Не удалось прочитать CSV-файл "+ path, e);
        }
    }

    private static CSVParser fileParse(CSVFormat format, BufferedReader file, Path path)
            throws IOException {
        try {
            return format.parse(file);
        } catch (IllegalArgumentException e) {
            throw new IOException("Некорректный заголовок CSV-файла " + path, e);
        }
    }

    private static DataRecord parseRecord(CSVRecord row) throws IOException {

        if (!row.isConsistent()) {
            throw new IOException("Запись " + row.getRecordNumber()
                    + ": количество полей не совпадает с заголовком");
        }

        try {
            return new DataRecord(
                    row.get("city"),
                    row.get("street"),
                    Integer.parseInt(row.get("house")),
                    Integer.parseInt(row.get("floor"))
            );
        } catch (NumberFormatException e) {
            throw new IOException("Запись " + row.getRecordNumber()
                    + ": номер дома и этажность должны быть целыми числами", e);
        }
    }
}
