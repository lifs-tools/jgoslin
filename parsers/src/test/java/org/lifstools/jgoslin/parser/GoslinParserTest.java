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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidFaBondType;
import org.lifstools.jgoslin.domain.StringFunctions;
import static org.lifstools.jgoslin.parser.Parser.DEFAULT_QUOTE;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class GoslinParserTest {

    private static GoslinParser parser;
    private static GoslinParserEventHandler handler;

    @BeforeAll
    public static void setupParser() {
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        parser = new GoslinParser(knownFunctionalGroups, StringFunctions.getResourceAsString("Goslin.g4"), DEFAULT_QUOTE);
        handler = parser.newEventHandler();
    }

    @Test
    public void testDefaultConstructorTest() {
        GoslinParser gp = new GoslinParser();
        assertNotNull(gp);
    }

    @Test
    public void testGoslinParserTest() {
        LipidAdduct l = parser.parse("Cer 18:1(8Z);2/24:0", handler);
        assertEquals("Cer 18:1(8);(OH)2/24:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("Cer 18:1;O2/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("Cer 18:1;O2/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", l.getSumFormula());
        assertEquals("LCB", l.getLipid().getFaList().get(0).getName());
        assertEquals(1, l.getLipid().getFaList().get(0).getPosition());
        assertEquals("FA1", l.getLipid().getFaList().get(1).getName());
        assertEquals(2, l.getLipid().getFaList().get(1).getPosition());

        l = parser.parse("HexCer 18:1(5Z);2/24:0", handler);
        assertEquals("HexCer 18:1(5);OH/24:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("HexCer 18:1;O2/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("HexCer 18:1;O2/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("HexCer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", l.getSumFormula());

        l = parser.parse("HexCer d18:1(5Z)/24:0", handler);
        assertEquals("HexCer 18:1(5);OH/24:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("HexCer 18:1;O2/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("HexCer 18:1;O2/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("HexCer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", l.getSumFormula());

        l = parser.parse("LSM 17:1(4E);2", handler);
        assertEquals("LSM 17:1(4);OH", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C22H47N2O5P", l.getSumFormula());

        l = parser.parse("LCB 18:1(4E);2", handler);
        assertEquals("SPB 18:1(4);(OH)2", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("SPB 18:1;O2", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("SPB 18:1;O2", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("SPB 18:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C18H37NO2", l.getSumFormula());

        l = parser.parse("EPC 14:1(4E);2/20:1(11Z)", handler);
        assertEquals("EPC 14:1(4);OH/20:1(11)", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 14:1;O2/20:1", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("EPC 14:1;O2/20:1", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 34:2;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", l.getSumFormula());

        l = parser.parse("MIPC 18:0;3/24:0", handler);
        assertEquals("MIPC 18:0;(OH)2/24:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("MIPC 18:0;O3/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("MIPC 18:0;O3/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("MIPC 42:0;O3", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C54H106NO17P", l.getSumFormula());

        l = parser.parse("EPC 16:2(4E,6E);2/22:1(13Z);1", handler);
        assertEquals("EPC 16:2(4,6);OH/22:1(13);OH", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 16:2;O2/22:1;O", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("EPC 16:2;O2/22:1;O", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 38:3;O3", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C40H77N2O7P", l.getSumFormula());

        l = parser.parse("BMP 18:1-18:1", handler);
        assertEquals("C42H79O10P", l.getSumFormula());
        assertEquals(4, l.getLipid().getFaList().size());
        assertEquals("FA1", l.getLipid().getFaList().get(0).getName());
        assertEquals(-1, l.getLipid().getFaList().get(0).getPosition());
        assertEquals("FA2", l.getLipid().getFaList().get(1).getName());
        assertEquals(-1, l.getLipid().getFaList().get(1).getPosition());

        l = parser.parse("PC O-18:1-18:1", handler);
        assertEquals("C44H86NO7P", l.getSumFormula());
        assertEquals(2, l.getLipid().getFaList().size());
        assertEquals("FA1", l.getLipid().getFaList().get(0).getName());
        assertEquals(-1, l.getLipid().getFaList().get(0).getPosition());
        assertEquals(LipidFaBondType.ETHER_PLASMANYL, l.getLipid().getFaList().get(0).getLipidFaBondType());
        assertEquals("FA2", l.getLipid().getFaList().get(1).getName());
        assertEquals(-1, l.getLipid().getFaList().get(1).getPosition());

        l = parser.parse("PC-O 18:1_18:1", handler);
        assertEquals("C44H86NO7P", l.getSumFormula());
        assertEquals(2, l.getLipid().getFaList().size());
        assertEquals("FA1", l.getLipid().getFaList().get(0).getName());
        assertEquals(-1, l.getLipid().getFaList().get(0).getPosition());
        assertEquals(LipidFaBondType.ETHER_PLASMANYL, l.getLipid().getFaList().get(0).getLipidFaBondType());
        assertEquals("FA2", l.getLipid().getFaList().get(1).getName());
        assertEquals(-1, l.getLipid().getFaList().get(1).getPosition());
    }

    @Test
    public void testMultipleFas() {
        LipidAdduct l = parser.parse("BMP 18:1-18:1", handler);
        assertEquals("C42H79O10P", l.getSumFormula());
        assertEquals(4, l.getLipid().getFaList().size());
        assertEquals("FA1", l.getLipid().getFaList().get(0).getName());
        assertEquals(-1, l.getLipid().getFaList().get(0).getPosition());
        assertEquals(18, l.getLipid().getFaList().get(0).getNumCarbon());
        assertEquals(1, l.getLipid().getFaList().get(0).getNDoubleBonds());
        assertEquals("FA2", l.getLipid().getFaList().get(1).getName());
        assertEquals(-1, l.getLipid().getFaList().get(1).getPosition());
        assertEquals(18, l.getLipid().getFaList().get(1).getNumCarbon());
        assertEquals(1, l.getLipid().getFaList().get(1).getNDoubleBonds());
        assertEquals("FA3", l.getLipid().getFaList().get(2).getName());
        assertEquals(-1, l.getLipid().getFaList().get(2).getPosition());
        assertEquals(0, l.getLipid().getFaList().get(2).getNumCarbon());
        assertEquals(0, l.getLipid().getFaList().get(2).getNDoubleBonds());
        assertEquals("FA4", l.getLipid().getFaList().get(3).getName());
        assertEquals(-1, l.getLipid().getFaList().get(3).getPosition());
        assertEquals(0, l.getLipid().getFaList().get(3).getNumCarbon());
        assertEquals(0, l.getLipid().getFaList().get(3).getNDoubleBonds());

        LipidAdduct l2 = parser.parse("TAG 18:1/0:0/16:0", parser.newEventHandler());
        assertEquals("C37H70O5", l2.getSumFormula());
        assertEquals(3, l2.getLipid().getFaList().size());
        assertEquals("FA1", l2.getLipid().getFaList().get(0).getName());
        assertEquals(1, l2.getLipid().getFaList().get(0).getPosition());
        assertEquals("FA2", l2.getLipid().getFaList().get(1).getName());
        assertEquals(2, l2.getLipid().getFaList().get(1).getPosition());
        assertEquals("FA3", l2.getLipid().getFaList().get(2).getName());
        assertEquals(3, l2.getLipid().getFaList().get(2).getPosition());

        LipidAdduct l3 = parser.parse("Cer d18:1/24:0", parser.newEventHandler());
    }

    @Test
    public void testHete() {
        LipidAdduct lipidAdduct = null;
        
        lipidAdduct = parser.parse("12-HETE", handler);
        assertEquals("FA1", lipidAdduct.getLipid().getFaList().get(0).getName());
    
        lipidAdduct = parser.parse("NO2-OA", handler);
        assertEquals("FA 18:1;NO2", lipidAdduct.getLipid().getLipidString(LipidLevel.STRUCTURE_DEFINED));
    
        lipidAdduct = parser.parse("7(R),14(S)-DiHDHA", handler);
        assertEquals("FA 22:6;(OH)2", lipidAdduct.getLipid().getLipidString(LipidLevel.STRUCTURE_DEFINED));
    
        lipidAdduct = parser.parse("Mar1", handler);
        assertEquals("FA 22:6;(OH)2", lipidAdduct.getLipid().getLipidString(LipidLevel.STRUCTURE_DEFINED));
    
        lipidAdduct = parser.parse("LA", handler);
        assertEquals("FA 18:2", lipidAdduct.getLipid().getLipidString(LipidLevel.STRUCTURE_DEFINED));
    
        lipidAdduct = parser.parse("iPF2alpha-VI", handler);
        assertEquals("FA 20:2;[cy5:0;(OH)2];OH", lipidAdduct.getLipid().getLipidString(LipidLevel.STRUCTURE_DEFINED));
    }
    
    @Test
    public void testLabeledMediators() {
        LipidAdduct lipid = parser.parse("15S-HETE-d8", parser.newEventHandler());
        assertEquals(328.2854d, lipid.getMass(), 1.0e-4);
        assertEquals("C20H24O3H'8", lipid.getSumFormula());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/goslin-test.csv", numLinesToSkip = 0, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void testGoslinParserFromFileTest(String lipid_name) {
        LipidAdduct lipid = parser.parse(lipid_name, handler);
        assertTrue(lipid != null);
    }
}
