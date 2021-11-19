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

import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.StringFunctions;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann Kopczynski
 */
public class LipidParserTest {

    private static LipidParser parser;

    @BeforeAll
    public static void setupParsers() {
        SumFormulaParser sfp = SumFormulaParser.newInstance();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        parser = LipidParser.newInstance(knownFunctionalGroups);
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
}
