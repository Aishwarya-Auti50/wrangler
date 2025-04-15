/*
 * Copyright © 2025 xAI
 * Licensed under the Apache License, Version 2.0
 */
package io.cdap.wrangler.api.parser;

import io.cdap.wrangler.api.Token;

/**
 * Represents a time duration value (e.g., 150ms, 2s).
 */
public class TimeDuration implements Token {
    private final long nanos;
    private final String raw;

    public TimeDuration(String value) {
        this.raw = value;
        this.nanos = parseTimeDuration(value);
    }

    private long parseTimeDuration(String value) {
        String num = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toLowerCase();
        double number = Double.parseDouble(num);
        switch (unit) {
            case "ns": return (long) number;
            case "us": return (long) (number * 1000);
            case "ms": return (long) (number * 1000 * 1000);
            case "s": return (long) (number * 1000 * 1000 * 1000);
            case "min": return (long) (number * 60 * 1000 * 1000 * 1000);
            case "h": return (long) (number * 3600 * 1000 * 1000 * 1000);
            default: throw new IllegalArgumentException("Invalid time unit: " + unit);
        }
    }

    public long getNanos() {
        return nanos;
    }

    @Override
    public String getRaw() {
        return raw;
    }
}