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

import org.lifstools.jgoslin.parser.FattyAcidParser;
import org.lifstools.jgoslin.parser.SumFormulaParser;
import org.lifstools.jgoslin.parser.ShorthandParser;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.StringFunctions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 *
 * @author dominik
 */
public class FattyAcidParserTest {

    private static SumFormulaParser sfp;
    private static FattyAcidParser fatty_acid_parser;
    private static ShorthandParser shorthand_parser;

    @BeforeAll
    public static void setupParsers() {
        sfp = new SumFormulaParser();
        fatty_acid_parser = new FattyAcidParser();
        shorthand_parser = new ShorthandParser();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testfiles/fatty-acids-test.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testFattyAcidParserTest(String lmid, String lipid_name, String formula, String expected_lipid_name) {
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////
        String computed_formula = StringFunctions.compute_sum_formula(sfp.parse(formula));
        LipidAdduct lipid = fatty_acid_parser.parse(lipid_name);
        String lipid_formula = lipid.get_sum_formula();

        assertEquals(expected_lipid_name, lipid.get_lipid_string(), lmid + " '" + lipid_name + "': " + expected_lipid_name + " != " + lipid.get_lipid_string() + " (computed)");
        assertEquals(computed_formula, lipid_formula, "formula " + lmid + " '" + lipid_name + "': " + computed_formula + " != " + lipid_formula + " (computed)");

        if (lipid_name.toLowerCase().contains("cyano")) {
            LipidAdduct lipid2 = shorthand_parser.parse(lipid.get_lipid_string());
            lipid_formula = lipid2.get_sum_formula();

            assertEquals(computed_formula, lipid_formula, "lipid " + lmid + " '" + lipid_name + "': " + computed_formula + " != " + lipid_formula + " (computed)");

            lipid2 = shorthand_parser.parse(lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
            lipid_formula = lipid2.get_sum_formula();

            assertEquals(computed_formula, lipid_formula, "molecular " + lmid + " '" + lipid_name + "': " + computed_formula + " != " + lipid_formula + " (computed)");

            lipid2 = shorthand_parser.parse(lipid.get_lipid_string(LipidLevel.SPECIES));
            lipid_formula = lipid2.get_sum_formula();

            assertEquals(computed_formula, lipid_formula, "species " + lmid + " '" + lipid_name + "': " + computed_formula + " != " + lipid_formula + " (computed)");
        }
    }
}
