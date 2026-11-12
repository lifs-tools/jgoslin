/*
 * Copyright 2026 Dominik Kopczynski, Nils Hoffmann.
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
package org.lifstools.jgoslin.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.StringFunctions;

/**
 * Names that are a lipid class or a lipid category only, read by the
 * {@link ClassLevelParser} and by the {@link LipidParser}, which uses the
 * {@link ClassLevelParser} as its last parser.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class ClassLevelParserTest {

    private static ClassLevelParser classParser;
    private static ClassLevelParserEventHandler handler;
    private static LipidParser lipidParser;

    @BeforeAll
    public static void setupParsers() {
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        classParser = new ClassLevelParser(knownFunctionalGroups);
        handler = classParser.newEventHandler();
        lipidParser = new LipidParser(knownFunctionalGroups);
    }

    @Test
    public void testDefaultConstructorTest() {
        ClassLevelParser clp = new ClassLevelParser();
        assertNotNull(clp);
    }

    private void assertClassLevel(LipidAdduct lipid, String name, String level, String lipidClass, String category) {
        assertNotNull(lipid, name);
        LipidLevel lipidLevel = LipidLevel.valueOf(level);
        assertEquals(lipidLevel, lipid.getLipid().getInfo().getLevel(), name);
        assertEquals(lipidLevel, lipid.getLipidLevel(), name);
        assertEquals(lipidClass, lipid.getLipidString(LipidLevel.CLASS), name);
        assertEquals(category, lipid.getLipidString(LipidLevel.CATEGORY), name);
        String expected = lipidLevel == LipidLevel.CLASS ? lipidClass : category;
        assertEquals(expected, lipid.getLipidString(), name);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/class-level-test.csv", numLinesToSkip = 1, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testClassLevelParser(String name, String level, String lipidClass, String category) {
        assertClassLevel(classParser.parse(name, handler), name, level, lipidClass, category);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/class-level-test.csv", numLinesToSkip = 1, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testLipidParser(String name, String level, String lipidClass, String category) {
        assertClassLevel(lipidParser.parse(name), name, level, lipidClass, category);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/class-level-invalid.txt", numLinesToSkip = 0, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void testInvalidClassLevelNames(String name) {
        assertNull(classParser.parse(name, handler, false), name);
        assertThrows(LipidParsingException.class, () -> {
            classParser.parse(name, handler, true);
        }, name);
        assertThrows(LipidParsingException.class, () -> {
            lipidParser.parse(name);
        }, name);
    }

    @Test
    public void testSpeciesNamesAreNotClassLevel() {
        LipidAdduct lipid = lipidParser.parse("PC 34:1");
        assertEquals(LipidLevel.SPECIES, lipid.getLipid().getInfo().getLevel());
        assertEquals(LipidLevel.SPECIES, lipid.getLipidLevel());
        assertEquals("PC 34:1", lipid.getLipidString());
    }

    @Test
    public void testNoSumFormulaAtClassLevel() {
        LipidAdduct lipid = lipidParser.parse("PC");
        assertThrows(LipidException.class, () -> {
            lipid.getSumFormula();
        });
        assertThrows(LipidException.class, () -> {
            lipid.getMass();
        });
        LipidAdduct category = lipidParser.parse("GP");
        assertThrows(LipidException.class, () -> {
            category.getSumFormula();
        });
    }

    @Test
    public void testSurroundingWhitespace() {
        LipidAdduct lipid = classParser.parse(" PC ", handler);
        assertNotNull(lipid);
        assertEquals("PC", lipid.getLipidString());
    }

}
