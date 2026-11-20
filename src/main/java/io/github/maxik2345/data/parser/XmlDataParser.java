package io.github.maxik2345.data.parser;

import io.github.maxik2345.data.model.DataRecord;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class XmlDataParser implements DataParser {
    @Override
    public List<DataRecord> parse(Path path) throws IOException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        List<DataRecord> records = new ArrayList<>();

        try (InputStream input = Files.newInputStream(path)) {
            XMLStreamReader reader = factory.createXMLStreamReader(input);
            try {
                reader.nextTag();
                if (!reader.isStartElement() || !"root".equals(reader.getLocalName())) {
                    throw new IOException("Ожидался корневой элемент <root> в файле " + path);
                }

                while (reader.hasNext()) {
                    reader.next();
                    if (reader.isStartElement() && "item".equals(reader.getLocalName())) {
                        records.add(parseRecord(reader));
                    }
                }
            } finally {
                reader.close();
            }
        } catch (XMLStreamException e) {
            throw new IOException("Не удалось прочитать XML-файл " + path, e);
        }

        return records;
    }

    private static DataRecord parseRecord(XMLStreamReader reader) throws IOException {
        String city = requireAttribute(reader, "city");
        String street = requireAttribute(reader, "street");
        int house = readIntAttribute(reader, "house");
        int floor = readIntAttribute(reader, "floor");

        return new DataRecord(city, street, house, floor);
    }

    private static String requireAttribute(XMLStreamReader reader, String name)
            throws IOException {
        String value = reader.getAttributeValue(null, name);
        if (value == null || value.isBlank()) {
            throw new IOException("Элемент item на строке "
                    + reader.getLocation().getLineNumber()
                    + " должен содержать непустой атрибут " + name);
        }
        return value;
    }

    private static int readIntAttribute(XMLStreamReader reader, String name)
            throws IOException {
        String value = requireAttribute(reader, name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IOException("Атрибут " + name + " на строке "
                    + reader.getLocation().getLineNumber()
                    + " должен быть целым числом", e);
        }
    }
}