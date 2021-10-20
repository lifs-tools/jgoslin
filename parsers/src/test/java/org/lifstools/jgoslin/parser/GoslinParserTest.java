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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AssertionsKt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 *
 * @author dominik
 */
public class GoslinParserTest {

    private static GoslinParser parser;
    
    @BeforeAll
    public static void setupParser() {
        parser = GoslinParser.newInstance();
    }
    
    @Test
    public void testGoslinParserTest() {
        LipidAdduct l = parser.parse("Cer 18:1(8Z);2/24:0");
        assertEquals("Cer 18:1(8);(OH)2/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", l.get_sum_formula());
        assertEquals("LCB", l.lipid.get_fa_list().get(0).name);
        assertEquals(0, l.lipid.get_fa_list().get(0).position);
        assertEquals("FA1", l.lipid.get_fa_list().get(1).name);
        assertEquals(2, l.lipid.get_fa_list().get(1).position);

        l = parser.parse("HexCer 18:1(5Z);2/24:0");
        assertEquals("HexCer 18:1(5);OH/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("HexCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("HexCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("HexCer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", l.get_sum_formula());

        l = parser.parse("LSM 17:1(4E);2");
        assertEquals("LSM 17:1(4);OH", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C22H47N2O5P", l.get_sum_formula());

        l = parser.parse("LCB 18:1(4E);2");
        assertEquals("SPB 18:1(4);(OH)2", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C18H37NO2", l.get_sum_formula());

        l = parser.parse("EPC 14:1(4E);2/20:1(11Z)");
        assertEquals("EPC 14:1(4);OH/20:1(11)", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 34:2;O2", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", l.get_sum_formula());

        l = parser.parse("MIPC 18:0;3/24:0");
        assertEquals("MIPC 18:0;(OH)2/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("MIPC 18:0;O3/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("MIPC 18:0;O3/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("MIPC 42:0;O3", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C54H106NO17P", l.get_sum_formula());

        l = parser.parse("EPC 16:2(4E,6E);2/22:1(13Z);1");
        assertEquals("EPC 16:2(4,6);OH/22:1(13);OH", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 16:2;O2/22:1;O", l.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("EPC 16:2;O2/22:1;O", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 38:3;O3", l.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C40H77N2O7P", l.get_sum_formula());
        
        l = parser.parse("BMP 18:1-18:1");
        assertEquals("C42H79O10P", l.get_sum_formula());
        assertEquals(4, l.lipid.get_fa_list().size());
        assertEquals("FA1", l.lipid.get_fa_list().get(0).name);
        assertEquals(0, l.lipid.get_fa_list().get(0).position);
        assertEquals("FA2", l.lipid.get_fa_list().get(1).name);
        assertEquals(0, l.lipid.get_fa_list().get(1).position);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testfiles/goslin-test.csv", numLinesToSkip = 0, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void testGoslinParserFromFileTest(String lipid_name) {
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////
//        GoslinParser parser = new GoslinParser();
        try {
            LipidAdduct lipid = parser.parse(lipid_name);
            assertTrue(lipid != null);
        } catch (RuntimeException re) {
            AssertionsKt.fail("Parsing failed for " + lipid_name, re);
        }
    }
}
