/*
 * Copyright © 2025 xAI
 * Licensed under the Apache License, Version 2.0
 */
package io.cdap.wrangler.api.parser;

import io.cdap.wrangler.api.Token;

/**
 * Represents a byte size value (e.g., 10KB, 1.5MB).
 */
public class ByteSize implements Token {
    private final long bytes;
    private final String raw;

    public ByteSize(String value) {
        this.raw = value;
        this.bytes = parseByteSize(value);
    }

    private long parseByteSize(String value) {
        String num = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").toLowerCase();
        double number = Double.parseDouble(num);
        switch (unit) {
            case "b": return (long) number;
            case "kb": return (long) (number * 1024);
            case "mb": return (long) (number * 1024 * 1024);
            case "gb": return (long) (number * 1024 * 1024 * 1024);
            case "tb": return (long) (number * 1024 * 1024 * 1024 * 1024);
            default: throw new IllegalArgumentException("Invalid byte unit: " + unit);
        }
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public String getRaw() {
        return raw;
    }
}