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

import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.StringFunctions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import static org.lifstools.jgoslin.parser.Parser.DEFAULT_QUOTE;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class FattyAcidParserTest {

    private static SumFormulaParser sfp;
    private static SumFormulaParserEventHandler sfpHandler;
    private static FattyAcidParser fatty_acid_parser;
    private static FattyAcidParserEventHandler faHandler;
    private static ShorthandParser shorthand_parser;
    private static ShorthandParserEventHandler shHandler;

    @BeforeAll
    public static void setupParsers() {
        sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        sfpHandler = sfp.newEventHandler();
        fatty_acid_parser = new FattyAcidParser(knownFunctionalGroups, StringFunctions.getResourceAsString("FattyAcids.g4"), DEFAULT_QUOTE);
        faHandler = fatty_acid_parser.newEventHandler();
        shorthand_parser = new ShorthandParser(knownFunctionalGroups, StringFunctions.getResourceAsString("Shorthand2020.g4"), DEFAULT_QUOTE);
        shHandler = shorthand_parser.newEventHandler();
    }

    @Test
    public void testSumFormulaFailures() {
        //"molecular LMFA01100037 '4-amino-4-cyano-butanoic acid': C5H8N2O2 != C5H12N2O2 (computed) ==> expected: <C5H8N2O2> but was: <C5H12N2O2>"
        String computed_formula = sfp.parse("C5H8N2O2", sfpHandler).getSumFormula();
        LipidAdduct lipid = fatty_acid_parser.parse("4-amino-4-cyano-butanoic acid", faHandler);
        String lipid_formula = lipid.getSumFormula();
        assertEquals(computed_formula, lipid_formula);
        LipidAdduct lipid2 = shorthand_parser.parse(lipid.getLipidString(), shHandler);
        String lipid_formula2 = lipid2.getSumFormula();
        assertEquals(computed_formula, lipid_formula2);
    }
    
    @Test 
    public void testManuscriptExamples() {
        String LMFA01020216 =  "5-methyl-octadecanoic acid";
        LipidAdduct l = fatty_acid_parser.parse(LMFA01020216, faHandler);
        assertEquals("FA 18:0;5Me", l.getLipidString());
        String LMFA01160100 =  "2-docosyl-3-hydroxy-28,29-epoxy-30-methyl-pentacontanoic acid";
        l = fatty_acid_parser.parse(LMFA01160100, faHandler);
        assertEquals("FA 50:0;2(22:0);28Ep;30Me;3OH", l.getLipidString());
        String LMFA03010032 =  "11R-hydroxy-9,15-dioxo-2,3,4,5-tetranor-prostan-1,20-dioic acid";
        l = fatty_acid_parser.parse(LMFA03010032, faHandler);
        assertEquals("FA 15:0;15COOH;[4-8cy5:0;7OH;5oxo];11oxo", l.getLipidString());
        String LMFA08040030 = "N-((+/-)-8,9-dihydroxy-5Z,11Z,14Z-eicosatrienoyl)-ethanolamine";
        l = fatty_acid_parser.parse(LMFA08040030, faHandler);
        assertEquals("NAE 20:3(5Z,11Z,14Z);8OH,9OH", l.getLipidString());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/fatty-acids-test.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testFattyAcidParserTest(String lmid, String lipid_name, String formula, String expected_lipid_name) {
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////
        String computed_formula = sfp.parse(formula, sfpHandler).getSumFormula();
        LipidAdduct lipid = fatty_acid_parser.parse(lipid_name, faHandler);
        String lipid_formula = lipid.getSumFormula();

        assertEquals(expected_lipid_name, lipid.getLipidString(), lmid + " '" + lipid_name + "': " + expected_lipid_name + " != " + lipid.getLipidString() + " (computed)");
        assertEquals(computed_formula, lipid_formula, "formula " + lmid + " '" + lipid_name + "': " + computed_formula + " != " + lipid_formula + " (computed)");

        if (!lipid_name.toLowerCase().contains("cyano")) {
            LipidAdduct lipid2 = shorthand_parser.parse(lipid.getLipidString(), shHandler);
            lipid_formula = lipid2.getSumFormula();

            assertEquals(computed_formula, lipid_formula, "lipid " + lmid + " '" + lipid_name + "': " + computed_formula + " != " + lipid_formula + " (computed)");

            lipid2 = shorthand_parser.parse(lipid.getLipidString(LipidLevel.MOLECULAR_SPECIES), shHandler);
            lipid_formula = lipid2.getSumFormula();

            assertEquals(computed_formula, lipid_formula, "molecular " + lmid + " '" + lipid_name + "': " + computed_formula + " != " + lipid_formula + " (computed)");

            lipid2 = shorthand_parser.parse(lipid.getLipidString(LipidLevel.SPECIES), shHandler);
            lipid_formula = lipid2.getSumFormula();

            assertEquals(computed_formula, lipid_formula, "species " + lmid + " '" + lipid_name + "': " + computed_formula + " != " + lipid_formula + " (computed)");
        }
    }
}
