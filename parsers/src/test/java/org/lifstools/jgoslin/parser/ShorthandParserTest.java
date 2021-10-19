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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * This class tests the spectrum annotation.
 *
 * @author Dominik Kopczynski
 */
public class ShorthandParserTest {

    @Test
    public void testShorthandParserTest() {
        ShorthandParser parser = new ShorthandParser();
        LipidAdduct l = parser.parse("PE 18:1(8Z);1OH,3OH/24:0");
        assertEquals("PE 18:1(8Z);1OH,3OH/24:0", l.get_lipid_string());
        assertEquals("PE 18:1(8);(OH)2/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("PE 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("PE 18:1;O2_24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("PE 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));

        l = parser.parse("Cer 18:1(8Z);1OH,3OH/24:0");
        assertEquals("Cer 18:1(8Z);1OH,3OH/24:0", l.get_lipid_string());
        assertEquals("Cer 18:1(8);(OH)2/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", l.get_sum_formula());

        l = parser.parse("Cer 18:1(8);(OH)2/24:0");
        assertEquals("Cer 18:1(8);(OH)2/24:0", l.get_lipid_string());
        assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", l.get_sum_formula());

        l = parser.parse("Cer 18:1;O2/24:0");
        assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string());
        assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", l.get_sum_formula());

        l = parser.parse("Cer 42:1;O2");
        assertEquals("Cer 42:1;O2", l.get_lipid_string());
        assertEquals("C42H83NO3", l.get_sum_formula());

        l = parser.parse("Gal-Cer(1) 18:1(5Z);3OH/24:0");
        assertEquals("Gal-Cer(1) 18:1(5Z);3OH/24:0", l.get_lipid_string());
        assertEquals("Gal-Cer 18:1(5);OH/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("GalCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("GalCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("GalCer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", l.get_sum_formula());

        l = parser.parse("Gal-Cer 18:1(5);OH/24:0");
        assertEquals("Gal-Cer 18:1(5);OH/24:0", l.get_lipid_string());
        assertEquals("GalCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("GalCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("GalCer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", l.get_sum_formula());

        l = parser.parse("GalCer 18:1;O2/24:0");
        assertEquals("GalCer 18:1;O2/24:0", l.get_lipid_string());
        assertEquals("GalCer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", l.get_sum_formula());

        l = parser.parse("GalCer 42:1;O2");
        assertEquals("GalCer 42:1;O2", l.get_lipid_string());
        assertEquals("C48H93NO8", l.get_sum_formula());

        l = parser.parse("SPB 18:1(4Z);1OH,3OH");
        assertEquals("SPB 18:1(4Z);1OH,3OH", l.get_lipid_string());
        assertEquals("SPB 18:1(4);(OH)2", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C18H37NO2", l.get_sum_formula());

        l = parser.parse("SPB 18:1(4);(OH)2");
        assertEquals("SPB 18:1(4);(OH)2", l.get_lipid_string());
        assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C18H37NO2", l.get_sum_formula());

        l = parser.parse("SPB 18:1;O2");
        assertEquals("SPB 18:1;O2", l.get_lipid_string());
        assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C18H37NO2", l.get_sum_formula());

        l = parser.parse("LSM(1) 17:1(4E);3OH");
        assertEquals("LSM(1) 17:1(4E);3OH", l.get_lipid_string());
        assertEquals("LSM 17:1(4);OH", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C22H47N2O5P", l.get_sum_formula());

        l = parser.parse("LSM 17:1(4);OH");
        assertEquals("LSM 17:1(4);OH", l.get_lipid_string());
        assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C22H47N2O5P", l.get_sum_formula());

        l = parser.parse("LSM 17:1;O2");
        assertEquals("LSM 17:1;O2", l.get_lipid_string());
        assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C22H47N2O5P", l.get_sum_formula());

        l = parser.parse("EPC(1) 14:1(4E);3OH/20:1(11Z)");
        assertEquals("EPC(1) 14:1(4E);3OH/20:1(11Z)", l.get_lipid_string());
        assertEquals("EPC 14:1(4);OH/20:1(11)", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 34:2;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", l.get_sum_formula());

        l = parser.parse("EPC 14:1(4);OH/20:1(11)");
        assertEquals("EPC 14:1(4);OH/20:1(11)", l.get_lipid_string());
        assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 34:2;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", l.get_sum_formula());

        l = parser.parse("EPC 14:1;O2/20:1");
        assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string());
        assertEquals("EPC 34:2;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", l.get_sum_formula());

        l = parser.parse("EPC 34:2;O2");
        assertEquals("EPC 34:2;O2", l.get_lipid_string());
        assertEquals("C36H71N2O6P", l.get_sum_formula());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testfiles/shorthand-test.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testShorthandParserFromFilesTest(String fullStructure, String structureDefined, String snPosition, String molecularSpecies, String species, String sumFormula, String noIdea) {
        ShorthandParser parser = new ShorthandParser();
        ArrayList<LipidLevel> levels = new ArrayList<>(Arrays.asList(LipidLevel.FULL_STRUCTURE, LipidLevel.STRUCTURE_DEFINED, LipidLevel.SN_POSITION, LipidLevel.MOLECULAR_SPECIES, LipidLevel.SPECIES));
        int col_num = levels.size();
        LipidAdduct lipid = parser.parse(fullStructure);
        String formula;
        if (sumFormula == null) {
            formula = lipid.get_sum_formula();
        } else {
            formula = sumFormula;
            assertTrue(formula.equals(lipid.get_sum_formula()));
        }

        List<String> lipidNamesOnLevel = Arrays.asList(fullStructure, structureDefined, snPosition, molecularSpecies, species);

        for (int lev = 0; lev < levels.size(); ++lev) {
            LipidLevel lipid_level = levels.get(lev);
            String n = lipid.get_lipid_string(lipid_level);
            assertEquals(lipidNamesOnLevel.get(lev), n);
            assertEquals(formula, lipid.get_sum_formula());

            LipidAdduct lipid2 = parser.parse(n);
            for (int ll = lev; ll < col_num; ++ll) {
                assertEquals(lipidNamesOnLevel.get(ll), lipid2.get_lipid_string(levels.get(ll)), "input " + n + " compare " + lipidNamesOnLevel.get(ll) + " vs. " + lipid2.get_lipid_string(levels.get(ll)));
                assertEquals(formula, lipid2.get_sum_formula());
            }
        }
    }
}
