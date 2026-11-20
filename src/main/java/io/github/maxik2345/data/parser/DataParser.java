package io.github.maxik2345.data.parser;

import io.github.maxik2345.data.model.DataRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DataParser {

    List<DataRecord> parse(Path path) throws IOException;
}
