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

import java.util.ArrayList;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.lifstools.jgoslin.parser.SumFormulaParser;

/**
 *
 * @author Nils Hoffmann
 */
public class FunctionalGroupTest {

    private static KnownFunctionalGroups knownFunctionalGroups;

    @BeforeAll
    public static void init() {
        SumFormulaParser sfp = new SumFormulaParser();
        knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
    }

    @Test
    public void testGetRingStereo() {
        FunctionalGroup fg = knownFunctionalGroups.get("Me").copy();
        assertEquals("", fg.getRingStereo());
    }

    @Test
    public void testSetGetAtomic() {
        FunctionalGroup fg = knownFunctionalGroups.get("Me").copy();
        assertEquals(false, fg.isAtomic());
        fg.setAtomic(true);
        assertEquals(true, fg.isAtomic());
    }

    @Test
    public void testSetElements() {
        FunctionalGroup fg = knownFunctionalGroups.get("Me").copy();
        ElementTable et = new ElementTable();
        et.put(Element.C, 5);
        fg.setElements(et);
        assertEquals(5, fg.getElements().get(Element.C));
    }

    @Test
    public void testGetTotalFunctionalGroupCountX() {
        FunctionalGroup fg = knownFunctionalGroups.get("Me").copy();
        assertEquals(0, fg.getTotalFunctionalGroupCount("[X]"));
    }

    @Test
    public void testGetFunctionalGroups() {
        FunctionalGroup fg = knownFunctionalGroups.get("Me").copy();
        Map<String, ArrayList<FunctionalGroup>> functionalGroups = fg.getFunctionalGroups();
        assertFalse(functionalGroups.containsKey("[X]"));
        assertEquals(0, functionalGroups.size());
    }
}
