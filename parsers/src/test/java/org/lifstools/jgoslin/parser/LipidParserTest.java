/*
 * Copyright 2021 Dominik Kopczynski, Nils Hoffmann.
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.StringFunctions;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann Kopczynski
 */
public class LipidParserTest {

    private static LipidParser parser;

    @BeforeAll
    public static void setupParsers() {
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        parser = new LipidParser(knownFunctionalGroups);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/lipid-maps-test.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testLM(String lipid_name, String correct_lipid_name) {
        if (correct_lipid_name == null || correct_lipid_name.isEmpty()) {
            //skip test
            assertTrue(true, "Skipping trivial / unsupported name: " + lipid_name);
        } else {
            LipidAdduct lipid = parser.parse(lipid_name);
            assertTrue(lipid != null);
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/swiss-lipids-test.csv", numLinesToSkip = 0, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void testSL(String lipid_name) {
        LipidAdduct lipid = parser.parse(lipid_name);
        assertTrue(lipid != null);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/hmdb-test.csv", numLinesToSkip = 0, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void testHmdb(String lipid_name) {
        LipidAdduct lipid = parser.parse(lipid_name);
        assertTrue(lipid != null);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/goslin-test.csv", numLinesToSkip = 0, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void testGoslin(String lipid_name) {
        LipidAdduct lipid = parser.parse(lipid_name);
        assertTrue(lipid != null);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/shorthand-test.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testShorthand(String fullStructure, String structureDefined, String snPosition, String molecularSpecies, String species, String sumFormula, String noIdea) {
        LipidAdduct lipid = parser.parse(fullStructure);
        assertTrue(lipid != null);
    }

    @Test
    public void testParsingFailed() {
        LipidParsingException lpe = assertThrows(LipidParsingException.class, () -> {
            parser.parse("Cer 189:as7");
        });
        assertTrue(lpe.getMessage().contains("Parsing failed"));
        assertTrue(lpe.getMessage().contains("at or after"));
        assertNull(parser.getLastSuccessfulParser());
    }

    @Test
    public void testLipidParser() {
        LipidParser lp = new LipidParser();
        LipidAdduct la = lp.parse("PE 16:1(6)/16:0;oxo;(OH)2");
        assertEquals(LipidLevel.STRUCTURE_DEFINED, la.getLipidLevel());
    }

    @Test
    public void testHydroxyls() {
        LipidAdduct l = parser.parse("Cer 36:1;2");
        Integer ohCount = l.getLipid().getInfo().getTotalFunctionalGroupCount("OH");
        assertEquals(2, ohCount);
        Integer asdCount = l.getLipid().getInfo().getTotalFunctionalGroupCount("ASD");
        assertEquals(0, asdCount);
        l = parser.parse("Cer d36:1");
        ohCount = l.getLipid().getInfo().getTotalFunctionalGroupCount("OH");
        assertEquals(2, ohCount);
        l = parser.parse("Cer 18:1;2/18:0");
        ohCount = l.getLipid().getInfo().getTotalFunctionalGroupCount("OH");
        assertEquals(2, ohCount);
        l = parser.parse("Cer d18:1/18:0");
        ohCount = l.getLipid().getInfo().getTotalFunctionalGroupCount("OH");
        assertEquals(2, ohCount);
        l = parser.parse("Cer 18:1;(OH)2/18:0");
        ohCount = l.getLipid().getInfo().getTotalFunctionalGroupCount("OH");
        assertEquals(2, ohCount);
    }

    @Test
    public void testSMs() {
        LipidAdduct l = parser.parse("SM 31:1");
        Integer cCount = l.getLipid().getInfo().getNumCarbon();
        assertEquals(31, cCount);
        Integer dbCount = l.getLipid().getInfo().getNDoubleBonds();
        assertEquals(1, dbCount);
    }

    @ParameterizedTest()
    @ValueSource(strings = {
        "CL 18:3(9Z,12Z,15Z)/16:0/22:5(16Z,10Z,19Z,13Z,7Z)/20:0",
        "FA 23:0;3Me,13Me,19Me",
        "LBPA(18:1(11Z)/0:0/32:5(14Z,17Z,20Z,23Z,26Z)/0:0)",
        "MLCL (20:2(11Z,14Z)/20:1(11Z)/20:0/0:0)",
        "N-ethyl-5Z,8Z,11Z,14Z-eicosatetraenoyl amine",
        "PC(21:0/22:6(4Z,7Z,10Z,13Z,16Z,19Z))",
        "PE(16:2(9Z,12Z)/18:1(6Z))",
        "PIP[4'] 6:0/22:5(16Z,10Z,19Z,13Z,7Z)",
        "TG(20:0/22:3(10Z,13Z,16Z)/22:5(7Z,10Z,13Z,16Z,19Z))[iso6]",
        "TG-EST 68:2;O2",
        "TG-EST 18:1_18:1_32:0;O2",
        "TG 16:0;O(FA 16:0)/18:1/18:1",
        "ST 27:1;O;S"
    })
    public void testOKuda(String lipidName) {
        LipidAdduct l = parser.parse(lipidName);
        Assertions.assertNotNull(l);
    }

}
