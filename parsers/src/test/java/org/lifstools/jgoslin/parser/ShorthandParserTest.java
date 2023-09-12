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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.StringFunctions;
import static org.lifstools.jgoslin.parser.Parser.DEFAULT_QUOTE;

/**
 * This class tests the shorthand nomenclature parser.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann Kopczynski
 */
public class ShorthandParserTest {

    private static ShorthandParser parser;
    private static ShorthandParserEventHandler handler;

    @BeforeAll
    public static void setupParser() {
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        parser = new ShorthandParser(knownFunctionalGroups, StringFunctions.getResourceAsString("Shorthand2020.g4"), DEFAULT_QUOTE);
        handler = parser.newEventHandler();
    }
    
    @Test
    public void testDefaultConstructorTest() {
        ShorthandParser gp = new ShorthandParser();
        assertNotNull(gp);
    }
    
    @Test
    public void testShorthandParserTest() {
        LipidAdduct l = parser.parse("PE 18:1(8Z);1OH,3OH/24:0", handler);
        assertEquals("PE 18:1(8Z);1OH,3OH/24:0", l.getLipidString());
        assertEquals("PE 18:1(8);(OH)2/24:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("PE 18:1;O2/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("PE 18:1;O2_24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("PE 42:1;O2", l.getLipidString(LipidLevel.SPECIES));

        l = parser.parse("Cer 18:1(8Z);1OH,3OH/24:0", handler);
        assertEquals("Cer 18:1(8Z);1OH,3OH/24:0", l.getLipidString());
        assertEquals("Cer 18:1(8);(OH)2/24:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("Cer 18:1;O2/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("Cer 18:1;O2/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", l.getSumFormula());

        l = parser.parse("Cer 18:1(8);(OH)2/24:0", handler);
        assertEquals("Cer 18:1(8);(OH)2/24:0", l.getLipidString());
        assertEquals("Cer 18:1;O2/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("Cer 18:1;O2/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", l.getSumFormula());

        l = parser.parse("Cer 18:1;O2/24:0", handler);
        assertEquals("Cer 18:1;O2/24:0", l.getLipidString());
        assertEquals("Cer 18:1;O2/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("Cer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C42H83NO3", l.getSumFormula());

        l = parser.parse("Cer 42:1;O2", handler);
        assertEquals("Cer 42:1;O2", l.getLipidString());
        assertEquals("C42H83NO3", l.getSumFormula());

        l = parser.parse("Gal-Cer(1) 18:1(5Z);3OH/24:0", handler);
        assertEquals("Gal-Cer(1) 18:1(5Z);3OH/24:0", l.getLipidString());
        assertEquals("Gal-Cer 18:1(5);OH/24:0", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("HexCer 18:1;O2/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("HexCer 18:1;O2/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("HexCer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", l.getSumFormula());

        l = parser.parse("Gal-Cer 18:1(5);OH/24:0", handler);
        assertEquals("Gal-Cer 18:1(5);OH/24:0", l.getLipidString());
        assertEquals("HexCer 18:1;O2/24:0", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("HexCer 18:1;O2/24:0", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("HexCer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", l.getSumFormula());

        l = parser.parse("GalCer 18:1;O2/24:0", handler);
        assertEquals("HexCer 18:1;O2/24:0", l.getLipidString());
        assertEquals("HexCer 42:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C48H93NO8", l.getSumFormula());

        l = parser.parse("GalCer 42:1;O2", handler);
        assertEquals("HexCer 42:1;O2", l.getLipidString());
        assertEquals("C48H93NO8", l.getSumFormula());

        l = parser.parse("SPB 18:1(4Z);1OH,3OH", handler);
        assertEquals("SPB 18:1(4Z);1OH,3OH", l.getLipidString());
        assertEquals("SPB 18:1(4);(OH)2", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("SPB 18:1;O2", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("SPB 18:1;O2", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("SPB 18:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C18H37NO2", l.getSumFormula());

        l = parser.parse("SPB 18:1(4);(OH)2", handler);
        assertEquals("SPB 18:1(4);(OH)2", l.getLipidString());
        assertEquals("SPB 18:1;O2", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("SPB 18:1;O2", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("SPB 18:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C18H37NO2", l.getSumFormula());

        l = parser.parse("SPB 18:1;O2", handler);
        assertEquals("SPB 18:1;O2", l.getLipidString());
        assertEquals("SPB 18:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C18H37NO2", l.getSumFormula());

        l = parser.parse("LSM(1) 17:1(4E);3OH", handler);
        assertEquals("LSM(1) 17:1(4E);3OH", l.getLipidString());
        assertEquals("LSM 17:1(4);OH", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C22H47N2O5P", l.getSumFormula());

        l = parser.parse("LSM 17:1(4);OH", handler);
        assertEquals("LSM 17:1(4);OH", l.getLipidString());
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C22H47N2O5P", l.getSumFormula());

        l = parser.parse("LSM 17:1;O2", handler);
        assertEquals("LSM 17:1;O2", l.getLipidString());
        assertEquals("LSM 17:1;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("LSM", l.getExtendedClass());
        assertEquals("C22H47N2O5P", l.getSumFormula());

        l = parser.parse("EPC(1) 14:1(4E);3OH/20:1(11Z)", handler);
        assertEquals("EPC(1) 14:1(4E);3OH/20:1(11Z)", l.getLipidString());
        assertEquals("EPC 14:1(4);OH/20:1(11)", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("EPC 14:1;O2/20:1", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("EPC 14:1;O2/20:1", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 34:2;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", l.getSumFormula());

        l = parser.parse("EPC 14:1(4);OH/20:1(11)", handler);
        assertEquals("EPC 14:1(4);OH/20:1(11)", l.getLipidString());
        assertEquals("EPC 14:1;O2/20:1", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("EPC 14:1;O2/20:1", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("EPC 34:2;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", l.getSumFormula());

        l = parser.parse("EPC 14:1;O2/20:1", handler);
        assertEquals("EPC 14:1;O2/20:1", l.getLipidString());
        assertEquals("EPC 34:2;O2", l.getLipidString(LipidLevel.SPECIES));
        assertEquals("C36H71N2O6P", l.getSumFormula());

        l = parser.parse("EPC 34:2;O2", handler);
        assertEquals("EPC 34:2;O2", l.getLipidString());
        assertEquals("C36H71N2O6P", l.getSumFormula());
        
        l = parser.parse("Gal-Glc-Cer(1) 17:1(5E);15Me[R];3OH[R],4OH[S]/22:0;2OH[R]", handler);
        assertEquals(LipidLevel.COMPLETE_STRUCTURE, l.getLipidLevel());
        l = parser.parse("Gal-Glc-Cer(1) 17:1(5E);15Me;3OH,4OH/22:0;2OH", handler);
        assertEquals(LipidLevel.FULL_STRUCTURE, l.getLipidLevel());
        assertEquals("Gal-Glc-Cer(1) 17:1(5E);15Me;3OH,4OH/22:0;2OH", l.getLipidString());
        assertEquals("Gal-Glc-Cer 17:1(5);Me;(OH)2/22:0;OH", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("Hex2Cer 18:1;O3/22:0;O", l.getLipidString(LipidLevel.SN_POSITION));
        
        assertThrows(LipidException.class, () -> {
            parser.parse("SM 21:1(4Z);2O/14:0", handler);
        });
    }
    
    @Test
    public void testManuscriptExamples() {
        LipidAdduct l = parser.parse("PE 16:1(6Z)/16:0;3oxo;5OH[R],8OH[S]", handler);
        assertEquals(LipidLevel.COMPLETE_STRUCTURE, l.getLipidLevel());
        assertEquals("PE 16:1(6Z)/16:0;5OH[R],8OH[S];3oxo", l.getLipidString());
        assertEquals("PE 16:1(6Z)/16:0;5OH,8OH;3oxo", l.getLipidString(LipidLevel.FULL_STRUCTURE));
        assertEquals("PE 16:1(6)/16:0;(OH)2;oxo", l.getLipidString(LipidLevel.STRUCTURE_DEFINED));
        assertEquals("PE 16:1/16:1;O3", l.getLipidString(LipidLevel.SN_POSITION));
        assertEquals("PE 16:1_16:1;O3", l.getLipidString(LipidLevel.MOLECULAR_SPECIES));
        assertEquals("PE 32:2;O3", l.getLipidString(LipidLevel.SPECIES));
        
        l = parser.parse("PE 16:1(6Z)/16:0;3oxo;5OH,8OH", handler);
        assertEquals(LipidLevel.FULL_STRUCTURE, l.getLipidLevel());
        
        l = parser.parse("PE 16:1(6)/16:0;oxo;(OH)2", handler);
        assertEquals(LipidLevel.STRUCTURE_DEFINED, l.getLipidLevel());
        
        l = parser.parse("PE 16:1/16:0;O3", handler);
        assertEquals(LipidLevel.SN_POSITION, l.getLipidLevel());
        
        l = parser.parse("PE 16:1_16:0;O3", handler);
        assertEquals(LipidLevel.MOLECULAR_SPECIES, l.getLipidLevel());
        
        l = parser.parse("PE 32:1;O3", handler);
        assertEquals(LipidLevel.SPECIES, l.getLipidLevel());
        
        l = parser.parse("TG 16:0;5O(FA 16:0)/18:1(9Z)/18:1(9Z)", handler);
        assertEquals(LipidLevel.COMPLETE_STRUCTURE, l.getLipidLevel());
        
        l = parser.parse("FA 35:1(18Z);2(24:0);3OH", handler);
        assertEquals(LipidLevel.FULL_STRUCTURE, l.getLipidLevel());
    }
    
    @Test
    public void testSnPosition() {
        LipidAdduct l = parser.parse("NA 2:0/20:4(5Z,8Z,11Z,14Z)", handler);
        assertEquals(1, l.getLipid().getFaList().get(0).getPosition());
        assertEquals(2, l.getLipid().getFaList().get(1).getPosition());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/shorthand-test.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testShorthandParserFromFilesTest(String fullStructure, String structureDefined, String snPosition, String molecularSpecies, String species, String sumFormula, String noIdea) {
        ArrayList<LipidLevel> levels = new ArrayList<>(Arrays.asList(LipidLevel.FULL_STRUCTURE, LipidLevel.STRUCTURE_DEFINED, LipidLevel.SN_POSITION, LipidLevel.MOLECULAR_SPECIES, LipidLevel.SPECIES));
        int col_num = levels.size();
        LipidAdduct lipid = parser.parse(fullStructure, handler);
        String formula;
        if (sumFormula == null) {
            formula = lipid.getSumFormula();
        } else {
            formula = sumFormula;
            assertTrue(formula.equals(lipid.getSumFormula()));
        }

        List<String> lipidNamesOnLevel = Arrays.asList(fullStructure, structureDefined, snPosition, molecularSpecies, species);

        for (int lev = 0; lev < levels.size(); ++lev) {
            LipidLevel lipid_level = levels.get(lev);
            String n = lipid.getLipidString(lipid_level);
            assertEquals(lipidNamesOnLevel.get(lev), n);
            assertEquals(formula, lipid.getSumFormula());

            LipidAdduct lipid2 = parser.parse(n, handler);
            for (int ll = lev; ll < col_num; ++ll) {
                assertEquals(lipidNamesOnLevel.get(ll), lipid2.getLipidString(levels.get(ll)), "input " + n + " compare " + lipidNamesOnLevel.get(ll) + " vs. " + lipid2.getLipidString(levels.get(ll)));
                assertEquals(formula, lipid2.getSumFormula(), "input " + n + " compare " + lipidNamesOnLevel.get(ll) + " vs. " + lipid2.getLipidString(levels.get(ll)));
            }
        }
    }
}
