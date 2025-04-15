/*
 * Copyright © 2017-2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.parser;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.CompileStatus;
import io.cdap.wrangler.api.Compiler;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.RecipeParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests {@link GrammarBasedParser}
 */
public class GrammarBasedParserTest {

    @Test
    public void testBasic() throws Exception {
        String[] recipe = new String[] {
            "#pragma version 2.0;",
            "rename :col1 :col2",
            "parse-as-csv :body ',' true;",
            "#pragma load-directives text-reverse, text-exchange;",
            "${macro} ${macro_2}",
            "${macro_${test}}"
        };

        RecipeParser parser = TestingRig.parse(recipe);
        List<Directive> directives = parser.parse();
        Assert.assertEquals(2, directives.size());
    }

    @Test
    public void testLoadableDirectives() throws Exception {
        String[] recipe = new String[] {
            "#pragma version 2.0;",
            "#pragma load-directives text-reverse, text-exchange;",
            "rename col1 col2",
            "parse-as-csv body , true",
            "text-reverse :body;",
            "test prop: { a='b', b=1.0, c=true};",
            "#pragma load-directives test-change,text-exchange, test1,test2,test3,test4;"
        };

        Compiler compiler = new RecipeCompiler();
        CompileStatus status = compiler.compile(new MigrateToV2(recipe).migrate());
        Assert.assertEquals(7, status.getSymbols().getLoadableDirectives().size());
    }

    @Test
    public void testCommentOnlyRecipe() throws Exception {
        String[] recipe = new String[] {
            "// test"
        };

        RecipeParser parser = TestingRig.parse(recipe);
        List<Directive> directives = parser.parse();
        Assert.assertEquals(0, directives.size());
    }

    /**
     * Tests parsing of recipes with the aggregate-stats directive, ensuring
     * column names and unit strings (supporting BYTE_SIZE and TIME_DURATION grammar)
     * are handled correctly, including edge cases like missing or invalid units.
     */
    @Test
    public void testByteSizeAndTimeDurationParsing() throws Exception {
        // Test 1: Valid aggregate-stats directive with units
        String[] recipe1 = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec mb s"
        };
        RecipeParser parser1 = TestingRig.parse(recipe1);
        List<Directive> directives1 = parser1.parse();
        Assert.assertEquals(1, directives1.size());
        Assert.assertEquals("aggregate-stats", directives1.get(0).getName());

        // Test 2: Partial directive (missing units)
        String[] recipe2 = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };
        RecipeParser parser2 = TestingRig.parse(recipe2);
        List<Directive> directives2 = parser2.parse();
        Assert.assertEquals(1, directives2.size());
        Assert.assertEquals("aggregate-stats", directives2.get(0).getName());

        // Test 3: Invalid unit (parser should not crash)
        String[] recipe3 = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec xb sec"
        };
        RecipeParser parser3 = TestingRig.parse(recipe3);
        List<Directive> directives3 = parser3.parse();
        Assert.assertEquals(1, directives3.size());
        Assert.assertEquals("aggregate-stats", directives3.get(0).getName());

        // Test 4: Multiple directives including aggregate-stats
        String[] recipe4 = new String[] {
            "rename :col1 :data_transfer_size",
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec kb ms",
            "parse-as-csv :body ',' true"
        };
        RecipeParser parser4 = TestingRig.parse(recipe4);
        List<Directive> directives4 = parser4.parse();
        Assert.assertEquals(3, directives4.size());
        Assert.assertEquals("aggregate-stats", directives4.get(1).getName());
    }
}