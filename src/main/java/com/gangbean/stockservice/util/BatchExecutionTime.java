package com.gangbean.stockservice.util;

import com.gangbean.stockservice.exception.BatchNameNotExistsException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class BatchExecutionTime {

    public static final LocalDateTime INITIAL_EXECUTION_TIME =
            LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS);
    public static final Map<String, LocalDateTime> BATCH_LIST = Map.of(
            "Reservation", INITIAL_EXECUTION_TIME
            , "Stock", INITIAL_EXECUTION_TIME);
    private final static Map<String, LocalDateTime> NEXT_EXECUTION = new HashMap<>(BATCH_LIST);

    public static void write(String batchName, LocalDateTime endAt) {
        if (!NEXT_EXECUTION.containsKey(batchName)) {
            throw new BatchNameNotExistsException("존재하지 않는 배치입니다: " + batchName);
        }
        NEXT_EXECUTION.put(batchName, endAt);
    }

    public static boolean isExecutionImpossibleAt(String batchName, LocalDateTime executeAt) {
        return executeAt.isBefore(NEXT_EXECUTION.getOrDefault(batchName, LocalDateTime.MIN));
    }

    public static LocalDateTime nextExecutionTime(String batchName) {
        if (!NEXT_EXECUTION.containsKey(batchName)) {
            throw new BatchNameNotExistsException("존재하지 않는 배치입니다: " + batchName);
        }
        return NEXT_EXECUTION.get(batchName);
    }
}
