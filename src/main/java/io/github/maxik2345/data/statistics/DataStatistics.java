package io.github.maxik2345.data.statistics;

import io.github.maxik2345.data.model.DataRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataStatistics {
    public Map<DataRecord, Integer> countOccurrences(List<DataRecord> records) {
        Map<DataRecord, Integer> counts = new HashMap<>();

        for (DataRecord record : records) {
            counts.merge(record, 1, Integer::sum);
        }

        return counts;
    }

    public Map<String, int[]> countBuildingsByCity(Set<DataRecord> uniqueRecords) {
        Map<String, int[]> countsByCity = new HashMap<>();

        for (DataRecord record : uniqueRecords) {
            int[] counts = countsByCity.computeIfAbsent(record.city(), city -> new int[5]);
            int floor = record.floor();
            if (floor >= 1 && floor <= 5) {
                counts[floor - 1]++;
            }
        }

        return countsByCity;
    }
}