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

import org.testng.annotations.Test;

import it.jnrpe.utils.thresholds.BracketStage.ClosedBracketStage;
import it.jnrpe.utils.thresholds.BracketStage.OpenBracketStage;
import static org.testng.Assert.*;

/**
 */
public class BracketStageTest {

    /**
     * Method testCanParseNull.
     */
    @Test
    public void testCanParseNull() {
        OpenBracketStage stage = new OpenBracketStage();
        assertFalse(stage.canParse(null));
    }

    /**
     * Method testCanParseOpenForOpen.
     */
    @Test
    public void testCanParseOpenForOpen() {
        OpenBracketStage stage = new OpenBracketStage();
        assertTrue(stage.canParse("("));
    }

    /**
     * Method testCanParseOpenForClose.
     */
    @Test
    public void testCanParseOpenForClose() {
        ClosedBracketStage stage = new ClosedBracketStage();
        assertFalse(stage.canParse("("));
    }

    /**
     * Method testCanParseCloseForClose.
     */
    @Test
    public void testCanParseCloseForClose() {
        ClosedBracketStage stage = new ClosedBracketStage();
        assertTrue(stage.canParse(")"));
    }

    /**
     * Method testCanCloseForOpen.
     */
    @Test
    public void testCanCloseForOpen() {
        OpenBracketStage stage = new OpenBracketStage();
        assertFalse(stage.canParse(")"));
    }

    /**
     * Method testExpectsOpen.
     */
    @Test
    public void testExpectsOpen() {
        OpenBracketStage stage = new OpenBracketStage();
        assertEquals(stage.expects(), "(");
    }

    /**
     * Method testExpectsClosed.
     */
    @Test
    public void testExpectsClosed() {
        ClosedBracketStage stage = new ClosedBracketStage();
        assertEquals(stage.expects(), ")");
    }

    /**
     * Method testParseOk.
     */
    @Test
    public void testParseOk() {
        ClosedBracketStage stage = new ClosedBracketStage();

        assertEquals(stage.parse(")", new RangeConfig()), "");
    }

    /**
     * Method testParseKo.
     */
    @Test
    public void testParseKo() {
        ClosedBracketStage stage = new ClosedBracketStage();
        assertFalse(stage.canParse("a"));
    }

}
