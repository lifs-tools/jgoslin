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

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Nils Hoffmann
 */
public class FattyAcidTest {

    public FattyAcidTest() {
    }

    @Test
    public void testFaConstraints() {
        DoubleBonds db = new DoubleBonds();
        ConstraintViolationException cve = assertThrows(ConstraintViolationException.class, () -> {
            new FattyAcid("SomeName", -1, db);
        });
        assertEquals("FattyAcid must have at least 2 carbons! Got -1", cve.getMessage());
        cve = assertThrows(ConstraintViolationException.class, () -> {
            new FattyAcid("SomeName", 1, db);
        });
        assertEquals("FattyAcid must have at least 2 carbons! Got 1", cve.getMessage());
        cve = assertThrows(ConstraintViolationException.class, () -> {
            new FattyAcid("Some name", 2, db, new HashMap<>(), LipidFaBondType.ESTER, -2, new KnownFunctionalGroups());
        });
        assertEquals("FattyAcid position must be greater or equal to 0! Got -2", cve.getMessage());
        cve = assertThrows(ConstraintViolationException.class, () -> {
            new FattyAcid("Some name", 2, new DoubleBonds(-1), new HashMap<>(), LipidFaBondType.ESTER, 2, new KnownFunctionalGroups());
        });
        assertEquals("FattyAcid must have at least 0 double bonds! Got -1", cve.getMessage());
    }
    
    @Test
    public void testFaBondTypeLCBRegular() {
        FattyAcid fa1 = new FattyAcid("Some name", 2, new DoubleBonds(0), new HashMap<>(), LipidFaBondType.LCB_REGULAR, 2, new KnownFunctionalGroups());
        // [X] is a virtual group, only expose it in functionalGroupsInternal
        assertTrue(fa1.getFunctionalGroupsInternal().containsKey("[X]"));
        assertFalse(fa1.getFunctionalGroups().containsKey("[X]"));
        assertEquals(1, fa1.getFunctionalGroupsInternal().get("[X]").size());
    }

    @Test
    public void testFaBondTypePrefix() {
        FattyAcid fa1 = new FattyAcid("Some name", 2, new DoubleBonds(0), new HashMap<>(), LipidFaBondType.ESTER, 2, new KnownFunctionalGroups());
        assertFalse(fa1.lipidFaBondTypePrefix(fa1.getLipidFaBondType()));
        FattyAcid fa2 = new FattyAcid("Some name", 2, new DoubleBonds(0), new HashMap<>(), LipidFaBondType.ETHER_PLASMANYL, 2, new KnownFunctionalGroups());
        assertTrue(fa2.lipidFaBondTypePrefix(fa2.getLipidFaBondType()));
        FattyAcid fa3 = new FattyAcid("Some name", 2, new DoubleBonds(0), new HashMap<>(), LipidFaBondType.ETHER_PLASMENYL, 2, new KnownFunctionalGroups());
        assertTrue(fa3.lipidFaBondTypePrefix(fa3.getLipidFaBondType()));
        FattyAcid fa4 = new FattyAcid("Some name", 2, new DoubleBonds(0), new HashMap<>(), LipidFaBondType.ETHER_UNSPECIFIED, 2, new KnownFunctionalGroups());
        assertTrue(fa4.lipidFaBondTypePrefix(fa4.getLipidFaBondType()));
    }
}
