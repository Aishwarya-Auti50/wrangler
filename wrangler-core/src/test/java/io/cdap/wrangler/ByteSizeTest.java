/*
 * Copyright © 2025 xAI
 * Licensed under the Apache License, Version 2.0
 */
package io.cdap.wrangler;

import io.cdap.wrangler.api.parser.ByteSize;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for ByteSize parsing.
 */
public class ByteSizeTest {
    @Test
    public void testByteSizeParsing() {
        ByteSize b1 = new ByteSize("10KB");
        Assert.assertEquals(10 * 1024, b1.getBytes());
        ByteSize b2 = new ByteSize("1.5MB");
        Assert.assertEquals((long) (1.5 * 1024 * 1024), b2.getBytes());
        ByteSize b3 = new ByteSize("100B");
        Assert.assertEquals(100L, b3.getBytes());
        ByteSize b4 = new ByteSize("2GB");
        Assert.assertEquals(2L * 1024 * 1024 * 1024, b4.getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidByteSize() {
        new ByteSize("10XB");
    }
}