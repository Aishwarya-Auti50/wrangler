/*
 * Copyright © 2025 xAI
 * Licensed under the Apache License, Version 2.0
 */
package io.cdap.wrangler;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * A directive that aggregates byte sizes and time durations across rows.
 * <p>
 * Usage: aggregate-stats :sizeCol :timeCol :totalSizeCol :totalTimeCol [sizeUnit] [timeUnit]
 * <p>
 * Example: aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec mb s
 */
public class AggregateStats implements Directive {
    private String sizeCol;
    private String timeCol;
    private String totalSizeCol;
    private String totalTimeCol;
    private String sizeUnit;
    private String timeUnit;
    private long totalBytes = 0;
    private long totalNanos = 0;
    private int rowCount = 0;

    @Override
    public UsageDefinition define() {
        return UsageDefinition.builder("aggregate-stats")
                .with("sizeCol", TokenType.COLUMN_NAME)
                .with("timeCol", TokenType.COLUMN_NAME)
                .with("totalSizeCol", TokenType.COLUMN_NAME)
                .with("totalTimeCol", TokenType.COLUMN_NAME)
                .withOptional("sizeUnit", TokenType.STRING)
                .withOptional("timeUnit", TokenType.STRING)
                .build();
    }

    @Override
    public void initialize(Arguments args, ExecutorContext context) throws DirectiveExecutionException {
        sizeCol = ((ColumnName) args.value("sizeCol")).value();
        timeCol = ((ColumnName) args.value("timeCol")).value();
        totalSizeCol = ((ColumnName) args.value("totalSizeCol")).value();
        totalTimeCol = ((ColumnName) args.value("totalTimeCol")).value();
        sizeUnit = args.contains("sizeUnit") ? ((Text) args.value("sizeUnit")).value().toLowerCase() : "mb";
        timeUnit = args.contains("timeUnit") ? ((Text) args.value("timeUnit")).value().toLowerCase() : "s";
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        totalBytes = 0;
        totalNanos = 0;
        rowCount = 0;

        for (Row row : rows) {
            Object sizeValue = row.getValue(sizeCol);
            Object timeValue = row.getValue(timeCol);
            if (sizeValue instanceof String && timeValue instanceof String) {
                try {
                    ByteSize byteSize = new ByteSize((String) sizeValue);
                    TimeDuration timeDuration = new TimeDuration((String) timeValue);
                    totalBytes += byteSize.getBytes();
                    totalNanos += timeDuration.getNanos();
                    rowCount++;
                } catch (IllegalArgumentException e) {
                    // Skip invalid rows
                    continue;
                }
            }
        }

        List<Row> result = new ArrayList<>();
        if (rowCount > 0) {
            Row output = new Row();
            output.add(totalSizeCol, convertBytes(totalBytes, sizeUnit));
            output.add(totalTimeCol, convertNanos(totalNanos, timeUnit));
            result.add(output);
        }
        return result;
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }

    private double convertBytes(long bytes, String unit) throws DirectiveExecutionException {
        switch (unit) {
            case "b": return bytes;
            case "kb": return bytes / 1024.0;
            case "mb": return bytes / (1024.0 * 1024);
            case "gb": return bytes / (1024.0 * 1024 * 1024);
            case "tb": return bytes / (1024.0 * 1024 * 1024 * 1024);
            default:
                throw new DirectiveExecutionException(
                        "Invalid size unit: " + unit + ". Supported units: b, kb, mb, gb, tb");
        }
    }

    private double convertNanos(long nanos, String unit) throws DirectiveExecutionException {
        switch (unit) {
            case "ns": return nanos;
            case "us": return nanos / 1000.0;
            case "ms": return nanos / (1000.0 * 1000);
            case "s": return nanos / (1000.0 * 1000 * 1000);
            case "min": return nanos / (60.0 * 1000 * 1000 * 1000);
            case "h": return nanos / (3600.0 * 1000 * 1000 * 1000);
            default:
                throw new DirectiveExecutionException(
                        "Invalid time unit: " + unit + ". Supported units: ns, us, ms, s, min, h");
        }
    }
}