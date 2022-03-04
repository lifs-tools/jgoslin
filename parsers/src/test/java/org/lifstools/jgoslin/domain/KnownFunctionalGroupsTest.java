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

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.lifstools.jgoslin.parser.SumFormulaParser;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author Nils Hoffmann
 */
public class KnownFunctionalGroupsTest {

    @Test
    public void testKnownFunctionGroupsConstructorTest() {
        var groups = new KnownFunctionalGroups();
        var testGroups = new KnownFunctionalGroups();
        FunctionalGroup functionalGroup = new FunctionalGroup("Me", testGroups);
        assertEquals(groups.get("Me").getName(), functionalGroup.getName());
    }

    @Test
    public void testDuplicateDetection() {
        List<String> lines = StringFunctions.getResourceAsStringList(new ClassPathResource("functional-groups.csv"));
        lines.add(lines.get(1));
        SumFormulaParser sfp = new SumFormulaParser();
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            new KnownFunctionalGroups(lines, sfp);
        });
        Assertions.assertEquals("Error: functional group", exception.getMessage().substring(0, 23));
    }
}
