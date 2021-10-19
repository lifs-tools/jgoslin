/*
 * Copyright 2021 dominik.
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
import org.lifstools.jgoslin.domain.LipidLevel;
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
public class LipidMapsParserTest {
    
    private static LipidMapsParser parser;
    
    @BeforeAll
    public static void setupParser() {
        parser = new LipidMapsParser();
    }
    
    @Test
    public void testLipidMapsParserTest() {
        LipidAdduct lipid = parser.parse("Cer(d18:1(8Z)/24:0)");
        assertEquals("Cer 18:1(8);(OH)2/24:0", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("Cer 18:1;O2/24:0", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("Cer 18:1;O2/24:0", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", lipid.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", lipid.get_sum_formula());
        
        lipid = parser.parse("GalCer(d18:1(5Z)/24:0)");
        assertEquals("GalCer 18:1(5);OH/24:0", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("GalCer 18:1;O2/24:0", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("GalCer 18:1;O2/24:0", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("GalCer 42:1;O2", lipid.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", lipid.get_sum_formula());
        
        lipid = parser.parse("LysoSM(d17:1(4E))");
        assertEquals("LSM 17:1(4);OH", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("LSM 17:1;O2", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("LSM 17:1;O2", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("LSM 17:1;O2", lipid.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C22H47N2O5P", lipid.get_sum_formula());
        
        lipid = parser.parse("PE-Cer(d14:1(4E)/20:1(11Z))");
        assertEquals("EPC 14:1(4);OH/20:1(11)", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 14:1;O2/20:1", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("EPC 14:1;O2/20:1", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 34:2;O2", lipid.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", lipid.get_sum_formula());
        
        lipid = parser.parse("MIPC(t18:0/24:0)");
        assertEquals("MIPC 18:0;(OH)2/24:0", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("MIPC 18:0;O3/24:0", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("MIPC 18:0;O3/24:0", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("MIPC 42:0;O3", lipid.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C54H106NO17P", lipid.get_sum_formula());
        
        lipid = parser.parse("PE-Cer(d16:2(4E,6E)/22:1(13Z)(2OH))");
        assertEquals("EPC 16:2(4,6);OH/22:1(13);OH", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 16:2;O2/22:1;O", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        assertEquals("EPC 16:2;O2/22:1;O", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 38:3;O3", lipid.get_lipid_string(LipidLevel.SPECIES));
        assertEquals("C40H77N2O7P", lipid.get_sum_formula());
        
    }
    
    @ParameterizedTest
    @CsvFileSource(resources = "/testfiles/lipid-maps-test.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testLipidMapsParserFromFileTest(String lipid_name, String correct_lipid_name) {
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////
        try {
            if (correct_lipid_name == null || correct_lipid_name.isEmpty()) {
                //skip test
                assertTrue(true, "Skipping trivial / unsupported name: " + lipid_name);
            } else {
                LipidAdduct lipid = parser.parse(lipid_name);
                assertTrue(lipid != null);
                if (!correct_lipid_name.equals("Unsupported lipid") && correct_lipid_name.length() > 0) {
                    assertEquals(correct_lipid_name, lipid.get_lipid_string(), lipid_name + " . " + lipid.get_lipid_string() + " != " + correct_lipid_name + " (reference)");
                }
            }
        } catch (RuntimeException re) {
            AssertionsKt.fail("Parsing failed for " + lipid_name, re);
        }
    }
}
