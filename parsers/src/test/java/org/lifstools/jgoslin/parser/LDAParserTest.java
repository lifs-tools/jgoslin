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
import static org.lifstools.jgoslin.parser.Parser.DEFAULT_QUOTE;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.StringFunctions;

/**
 * Sphingolipid names written by LipidDataAnalyzer (LDA.g4).
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class LDAParserTest {

    private static LDAParser parser;
    private static LDAParserEventHandler handler;
    private static LipidParser lipidParser;

    @BeforeAll
    public static void setupParsers() {
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        parser = new LDAParser(knownFunctionalGroups, StringFunctions.getResourceAsString("LDA.g4"), DEFAULT_QUOTE);
        handler = parser.newEventHandler();
        lipidParser = new LipidParser(knownFunctionalGroups);
    }

    @Test
    public void testDefaultConstructorTest() {
        LDAParser ldap = new LDAParser();
        assertNotNull(ldap);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/lda-test.csv", numLinesToSkip = 1, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testLDAParser(String name, String goslinName, String level, String sumFormula) {
        LipidAdduct lipid = parser.parse(name, handler);
        assertNotNull(lipid, name);
        assertEquals(goslinName, lipid.getLipidString(), name);
        assertEquals(LipidLevel.valueOf(level), lipid.getLipid().getInfo().getLevel(), name);
        assertEquals(sumFormula, lipid.getSumFormula(), name);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/lda-test.csv", numLinesToSkip = 1, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testLipidParser(String name, String goslinName, String level, String sumFormula) {
        // the multi grammar parser reads the name the same way
        LipidAdduct lipid = lipidParser.parse(name);
        assertNotNull(lipid, name);
        assertEquals(goslinName, lipid.getLipidString(), name);
        assertEquals(sumFormula, lipid.getSumFormula(), name);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/lda-invalid.txt", numLinesToSkip = 0, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void testInvalidLDANames(String name) {
        assertNull(parser.parse(name, handler, false), name);
    }

}
