/*******************************************************************************
 * Copyright (c) 2007, 2014 Massimiliano Ziccardi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package it.jnrpe.utils.thresholds;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class PositiveInfinityStageTest {
    @Test
    public void testCanParseNull() {
        PositiveInfinityStage stage = new PositiveInfinityStage();
        assertFalse(stage.canParse(null));
    }

    @Test
    public void testCanParseWithoutMinus() {
        PositiveInfinityStage stage = new PositiveInfinityStage();
        assertTrue(stage.canParse("inf"));
    }

    @Test
    public void testCanParseWithSign() {
        PositiveInfinityStage stage = new PositiveInfinityStage();
        assertTrue(stage.canParse("+inf"));
    }

    @Test
    public void testParseOk() throws InvalidRangeSyntaxException {
        PositiveInfinityStage stage = new PositiveInfinityStage();
        assertEquals(stage.parse("+inf..80", new RangeConfig()), "..80");
    }

    @Test
    public void testParseKo() throws InvalidRangeSyntaxException {
        PositiveInfinityStage stage = new PositiveInfinityStage();
        assertFalse(stage.canParse("10..80"));
    }

    @Test
    public void testExpect() {
        PositiveInfinityStage stage = new PositiveInfinityStage();
        assertEquals(stage.expects(), "[+]inf");
    }
}
