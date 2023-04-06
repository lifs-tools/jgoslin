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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lifstools.jgoslin.parser.Parser.DEFAULT_QUOTE;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.StringFunctions;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class LipidMapsParserTest {

    private static LipidMapsParser parser;
    private static LipidMapsParserEventHandler handler;

    @BeforeAll
    public static void setupParser() {
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        parser = new LipidMapsParser(knownFunctionalGroups, StringFunctions.getResourceAsString("LipidMaps.g4"), DEFAULT_QUOTE);
        handler = parser.newEventHandler();
    }

    @Test
    public void testDefaultConstructorTest() {
        LipidMapsParser gp = new LipidMapsParser();
        assertNotNull(gp);
    }

    @Test
    public void testLipidMapsParserTest() {
        LipidAdduct lipid = parser.parse("Cer(d18:1(8Z)/24:0)", handler);
        assertEquals("Cer 18:1(8);(OH)2/24:0", lipid.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("Cer 18:1;O2/24:0", lipid.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("Cer 18:1;O2/24:0", lipid.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", lipid.getLipidString(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", lipid.getSumFormula());

        lipid = parser.parse("GalCer(d18:1(5Z)/24:0)", handler);
        assertEquals("GalCer 18:1(5);OH/24:0", lipid.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("GalCer 18:1;O2/24:0", lipid.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("GalCer 18:1;O2/24:0", lipid.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("GalCer 42:1;O2", lipid.getLipidString(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", lipid.getSumFormula());

        lipid = parser.parse("LysoSM(d17:1(4E))", handler);
        assertEquals("LSM 17:1(4);OH", lipid.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("LSM 17:1;O2", lipid.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("LSM 17:1;O2", lipid.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("LSM 17:1;O2", lipid.getLipidString(LipidLevel.SPECIES));
        assertEquals("C22H47N2O5P", lipid.getSumFormula());

        lipid = parser.parse("PE-Cer(d14:1(4E)/20:1(11Z))", handler);
        assertEquals("EPC 14:1(4);OH/20:1(11)", lipid.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 14:1;O2/20:1", lipid.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("EPC 14:1;O2/20:1", lipid.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 34:2;O2", lipid.getLipidString(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", lipid.getSumFormula());

        lipid = parser.parse("MIPC(t18:0/24:0)", handler);
        assertEquals("MIPC 18:0;(OH)2/24:0", lipid.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("MIPC 18:0;O3/24:0", lipid.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("MIPC 18:0;O3/24:0", lipid.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("MIPC 42:0;O3", lipid.getLipidString(LipidLevel.SPECIES));
        assertEquals("C54H106NO17P", lipid.getSumFormula());

        lipid = parser.parse("PE-Cer(d16:2(4E,6E)/22:1(13Z)(2OH))", handler);
        final LipidAdduct tl = lipid;
        assertThrows(IllegalArgumentException.class, () -> {
            tl.getLipidString(LipidLevel.COMPLETE_STRUCTURE);
        });
        assertEquals(LipidLevel.FULL_STRUCTURE, lipid.getLipidLevel());
        assertEquals("EPC 16:2(4,6);OH/22:1(13);OH", lipid.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 16:2;O2/22:1;O", lipid.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("EPC 16:2;O2/22:1;O", lipid.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 38:3;O3", lipid.getLipidString(LipidLevel.SPECIES));
        assertEquals("C40H77N2O7P", lipid.getSumFormula());

        lipid = parser.parse("omega-linoleoyloxy-Cer(t17:1(6OH)/29:0)", handler);
        assertEquals(LipidLevel.SN_POSITION, lipid.getLipidLevel());
        assertEquals("Cer 64:3;O4", lipid.getLipidString(LipidLevel.SPECIES));

        lipid = parser.parse("PC(34:1) [M+H]1+", handler);
        assertEquals(LipidLevel.SPECIES, lipid.getLipidLevel());
        String lipidString = lipid.getLipidString(LipidLevel.SPECIES);
        assertEquals("PC 34:1[M+H]1+", lipidString);
        assertEquals("PC 34:1[M+H]1+", lipid.toString());
        assertFalse(lipid.isLyso());
        assertFalse(lipid.isCardioLipin());
        assertFalse(lipid.isContainsSugar());
        assertFalse(lipid.isContainsEster());
        assertFalse(lipid.isSpException());
        assertEquals("PC", lipid.getExtendedClass());
        assertEquals(760.58508d, lipid.getMass(), 1.0e-4);

        //lipid = parser.parse("GalNAcβ1-4(Galβ1-4GlcNAcβ1-3)Galβ1-4Glcβ-Cer(d18:1/24:1(15Z))", handler);
        // Lipid Maps Species name is currently "Hex(3)-HexNAc(2)-Cer 42:2;O2"
        //assertEquals("GalGalGalNAcGlcGlcNAcCer 42:2;O2", lipid.getLipidString(LipidLevel.SPECIES));
        //assertEquals(1539.9388d, lipid.getMass(), 1.0e-4);
    }
    
    @Test
    public void testLabeledLipids() {
        LipidAdduct lipid = parser.parse("PC(34:1)-d7", handler);
        assertEquals("PC 34:1[M7H2]", lipid.getLipidString());
        assertEquals(759.5778d, lipid.getMass(), 1.0e-4);
        lipid = parser.parse("PC(34:1)-d7 [M+H]1+", handler);
        assertEquals("PC 34:1[M7H2+H]1+", lipid.getLipidString());
//        assertEquals(767.6296d, lipid.getMass(), 1.0e-4);
        assertEquals("C42H76NO8PH'7", lipid.getSumFormula());

        lipid = parser.parse("15(S)-HETE-d8", handler);
        assertEquals(328.285361d, lipid.getMass(), 1.0e-4);
        assertEquals("C20H24O3H'8", lipid.getSumFormula());
    }

    
    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/lipid-maps-test.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testLipidMapsParserFromFileTest(String lipid_name, String correct_lipid_name) {
        if (correct_lipid_name == null || correct_lipid_name.isEmpty()) {
            //skip test
            assertTrue(true, "Skipping trivial / unsupported name: " + lipid_name);
        } else {
            LipidAdduct lipid = parser.parse(lipid_name, handler);
            assertTrue(lipid != null);
            if (!correct_lipid_name.equals("Unsupported lipid") && correct_lipid_name.length() > 0) {
                assertEquals(correct_lipid_name, lipid.getLipidString(), lipid_name + " . " + lipid.getLipidString() + " != " + correct_lipid_name + " (reference)");
            }
        }
    }
}
