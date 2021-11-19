/*
MIT License

Copyright (c) the authors (listed in global LICENSE file)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
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
        sfp = SumFormulaParser.newInstance();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        sfpHandler = sfp.newEventHandler();
        fatty_acid_parser = FattyAcidParser.newInstance(knownFunctionalGroups, "FattyAcids.g4", DEFAULT_QUOTE);
        faHandler = fatty_acid_parser.newEventHandler();
        shorthand_parser = ShorthandParser.newInstance(knownFunctionalGroups, "Shorthand2020.g4", DEFAULT_QUOTE);
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
