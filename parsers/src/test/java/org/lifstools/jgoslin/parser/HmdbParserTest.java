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
import org.lifstools.jgoslin.domain.LipidLevel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.StringFunctions;
import static org.lifstools.jgoslin.parser.Parser.DEFAULT_QUOTE;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class HmdbParserTest {

    private static HmdbParser parser;
    private static HmdbParserEventHandler handler;

    @BeforeAll
    public static void setupParser() {
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        parser = new HmdbParser(knownFunctionalGroups, StringFunctions.getResourceAsString("HMDB.g4"), DEFAULT_QUOTE);
        handler = parser.newEventHandler();
    }

    @Test
    public void testDefaultConstructorTest() {
        HmdbParser gp = new HmdbParser();
        assertNotNull(gp);
    }

    @Test
    public void testHmdbParserTest() {
        LipidAdduct l = parser.parse("Cer(d18:1(8Z)/24:0)", handler);
        assertEquals("Cer 18:1(8);(OH)2/24:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("Cer 18:1;O2/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("Cer 18:1;O2/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", l.getSumFormula());

        l = parser.parse("GalCer(d18:1(5Z)/24:0)", handler);
        assertEquals("GalCer 18:1(5);OH/24:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("GalCer 18:1;O2/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("GalCer 18:1;O2/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("GalCer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", l.getSumFormula());

        l = parser.parse("LysoSM(d17:1(4E))", handler);
        assertEquals("LSM 17:1(4);OH", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C22H47N2O5P", l.getSumFormula());

        l = parser.parse("PE-Cer(d14:1(4E)/20:1(11Z))", handler);
        assertEquals("EPC 14:1(4);OH/20:1(11)", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 14:1;O2/20:1", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("EPC 14:1;O2/20:1", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 34:2;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", l.getSumFormula());

        l = parser.parse("MIPC(t18:0/24:0)", handler);
        assertEquals("MIPC 18:0;(OH)2/24:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("MIPC 18:0;O3/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("MIPC 18:0;O3/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("MIPC 42:0;O3", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C54H106NO17P", l.getSumFormula());

        l = parser.parse("PE-Cer(d16:2(4E,6E)/22:1(13Z)(2OH))", handler);
        assertEquals("EPC 16:2(4,6);OH/22:1(13);OH", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 16:2;O2/22:1;O", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("EPC 16:2;O2/22:1;O", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 38:3;O3", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C40H77N2O7P", l.getSumFormula());
        
        l = parser.parse("DG(a-21:0/20:5(5Z,8Z,10E,14Z,17Z)+=O(12S)/0:0)", handler);
        assertEquals("DG 20:0;Me/20:5(5Z,8Z,10E,14Z,17Z);12oxo/0:0", l.getLipidString(LipidLevel.FULL_STRUCTURE));
        assertEquals("DG 20:0;Me/20:5(5,8,10,14,17);oxo/0:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("DG 21:0/20:6;O/0:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("DG 21:0_20:6;O", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("DG 41:6;O", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C44H74O6", l.getSumFormula());

        l = parser.parse("PIP(16:2(9Z,12Z)/PGJ2)", handler);
        assertEquals("PIP 16:2/20:5;O2", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("PIP 16:2_20:5;O2", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("PIP 36:7;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C45H74O18P2", l.getSumFormula());

        l = parser.parse("PC(14:0/20:5(7Z,9Z,11E,13E,17Z)-3OH(5,6,15))", handler);
        assertEquals("PC 14:0/20:5(7Z,9Z,11E,13E,17Z);5OH,6OH,15OH", l.getLipidString(LipidLevel.FULL_STRUCTURE));
        assertEquals("PC 14:0/20:5(7,9,11,13,17);(OH)3", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("PC 14:0/20:5;O3", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("PC 14:0_20:5;O3", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("PC 34:5;O3", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C42H74NO11P", l.getSumFormula());
        
        

        l = parser.parse("CL(i-12:0/i-13:0/18:2(9Z,11Z)/a-13:0)[rac]", handler);
        assertTrue(l != null);
    }

   
    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/hmdb-test.csv", numLinesToSkip = 0, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void testHmdbParserFromFileTest(String lipid_name) {
        LipidAdduct lipid = parser.parse(lipid_name, handler);
        assertTrue(lipid != null);
    }
}
