/*
 * Copyright 2022 Nils Hoffmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lifstools.jgoslin.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.lifstools.jgoslin.parser.SumFormulaParser;

/**
 *
 * @author Nils Hoffmann
 */
public class LipidSnPositionTest {

    @Test
    public void testConstructor() {
        Headgroup head_group = new Headgroup("FA");
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        LipidSnPosition lfs = new LipidSnPosition(head_group, knownFunctionalGroups);
        assertEquals(LipidLevel.SN_POSITION, lfs.getLipidLevel());
    }

    @Test
    public void testGetLipidString() {
        Headgroup head_group = new Headgroup("FA");
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        LipidSnPosition lfs = new LipidSnPosition(head_group, knownFunctionalGroups);
        assertEquals("FA 0:0", lfs.getLipidString());
    }

    @Test
    public void testGetLipidStringThrowsException() {
        Headgroup head_group = new Headgroup("FA");
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        LipidSnPosition lfs = new LipidSnPosition(head_group, knownFunctionalGroups);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            lfs.getLipidString(LipidLevel.UNDEFINED_LEVEL);
        });
        Assertions.assertEquals("LipidSnPosition does not know how to create a lipid string for level " + LipidLevel.UNDEFINED_LEVEL.toString(), exception.getMessage());
    }

}
