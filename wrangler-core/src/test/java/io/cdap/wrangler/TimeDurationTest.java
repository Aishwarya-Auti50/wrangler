/*
 * Copyright © 2025 xAI
 * Licensed under the Apache License, Version 2.0
 */
package io.cdap.wrangler;

import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for TimeDuration parsing.
 */
public class TimeDurationTest {
    @Test
    public void testTimeDurationParsing() {
        TimeDuration t1 = new TimeDuration("150ms");
        Assert.assertEquals(150 * 1000 * 1000, t1.getNanos());
        TimeDuration t2 = new TimeDuration("2.1s");
        Assert.assertEquals((long) (2.1 * 1000 * 1000 * 1000), t2.getNanos());
        TimeDuration t3 = new TimeDuration("10ns");
        Assert.assertEquals(10L, t3.getNanos());
        TimeDuration t4 = new TimeDuration("1min");
        Assert.assertEquals(60L * 1000 * 1000 * 1000, t4.getNanos());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeDuration() {
        new TimeDuration("10xm");
    }
}