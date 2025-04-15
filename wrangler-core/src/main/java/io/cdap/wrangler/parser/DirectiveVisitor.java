/*
 * Copyright © 2025 xAI
 * Licensed under the Apache License, Version 2.0
 */
package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.Bool;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Numeric;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.Token;

/**
 * ANTLR visitor for processing directive arguments, including byte size and time duration.
 */
public class DirectiveVisitor extends DirectivesBaseVisitor<Token> {
    @Override
    public Token visitByteSizeArg(DirectivesParser.ByteSizeArgContext ctx) {
        return new ByteSize(ctx.BYTE_SIZE().getText());
    }

    @Override
    public Token visitTimeDurationArg(DirectivesParser.TimeDurationArgContext ctx) {
        return new TimeDuration(ctx.TIME_DURATION().getText());
    }

    @Override
    public Token visitNumber(DirectivesParser.NumberContext ctx) {
        String text = ctx.Number().getText();
        try {
            return new Numeric(Long.parseLong(text));
        } catch (NumberFormatException e) {
            return new Text(text);
        }
    }

    @Override
    public Token visitText(DirectivesParser.TextContext ctx) {
        String text = ctx.String().getText();
        text = text.substring(1, text.length() - 1); // Remove quotes
        return new Text(text);
    }

    @Override
    public Token visitColumn(DirectivesParser.ColumnContext ctx) {
        String text = ctx.Column().getText();
        text = text.substring(1); // Remove leading colon
        return new ColumnName(text);
    }

    @Override
    public Token visitBool(DirectivesParser.BoolContext ctx) {
        String text = ctx.Bool().getText();
        return new Bool(Boolean.parseBoolean(text));
    }
}