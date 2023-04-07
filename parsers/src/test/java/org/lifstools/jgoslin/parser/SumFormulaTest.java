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
import org.lifstools.jgoslin.domain.StringFunctions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.lifstools.jgoslin.domain.ElementTable;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import static org.lifstools.jgoslin.parser.Parser.DEFAULT_QUOTE;
import org.springframework.util.Assert;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann Kopczynski
 */
public class SumFormulaTest {

    private static LipidMapsParser lipid_maps_parser;
    private static LipidMapsParserEventHandler lmHandler;
    private static SwissLipidsParser swiss_lipids_parser;
    private static SwissLipidsParserEventHandler slHandler;
    private static GoslinParser goslin_parser;
    private static GoslinParserEventHandler gHandler;

    @BeforeAll
    public static void setupParsers() {
        SumFormulaParser sfp = new SumFormulaParser();
        KnownFunctionalGroups knownFunctionalGroups = new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), sfp);
        lipid_maps_parser = new LipidMapsParser(knownFunctionalGroups, StringFunctions.getResourceAsString("LipidMaps.g4"), DEFAULT_QUOTE);
        lmHandler = lipid_maps_parser.newEventHandler();
        swiss_lipids_parser = new SwissLipidsParser(knownFunctionalGroups, StringFunctions.getResourceAsString("SwissLipids.g4"), DEFAULT_QUOTE);
        slHandler = swiss_lipids_parser.newEventHandler();
        goslin_parser = new GoslinParser(knownFunctionalGroups, StringFunctions.getResourceAsString("Goslin.g4"), DEFAULT_QUOTE);
        gHandler = goslin_parser.newEventHandler();
    }
    
//    @Test
//    public void testDeuteratedLMSumFormulas() {
//        //PC 34:1[M7H2+H]1+
//        String expectedSumFormula = "C42H76D7NO8P";
//        SumFormulaParser sfp = new SumFormulaParser();
//        ElementTable et1 = sfp.parse("C42H83NO8P", sfp.newEventHandler());
//        Assert.notNull(et1, "ElementTable should not be null!");
//        assertEquals("C42H83NO8P", et1.getSumFormula());
//        ElementTable et = sfp.parse(expectedSumFormula, sfp.newEventHandler());
//        Assert.notNull(et, "ElementTable should not be null!");
//        assertEquals("C42H76NO8PH'7", et.getSumFormula());
//        assertEquals(767.6296, et.getMass(), 1.0e-4);
//    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/formulas-lipid-maps.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testSumFormulaLM(String lipid_name, String correct_formula) {
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////
        LipidAdduct lipid = lipid_maps_parser.parse(lipid_name, lmHandler);
        assertTrue(lipid != null);
        assertEquals(correct_formula, lipid.getSumFormula(), "Error for lipid '" + lipid_name + "': " + lipid.getSumFormula() + " != " + correct_formula + " (reference)");
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/formulas-swiss-lipids.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testSumFormulaSL(String lipid_name, String correct_formula) {
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////
        LipidAdduct lipid = swiss_lipids_parser.parse(lipid_name, slHandler);
        assertTrue(lipid != null);
        assertEquals(correct_formula, lipid.getSumFormula(), "Error for lipid '" + lipid_name + "': " + lipid.getSumFormula() + " != " + correct_formula + " (reference)");
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvFileSource(resources = "/testfiles/lipid-masses.csv", numLinesToSkip = 1, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testMasses(String lipid_class, String lipid_name, String lipid_formula, String lipid_adduct, double lipid_mass, int lipid_charge) {
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////
        String full_lipid_name = lipid_name + lipid_adduct;
        LipidAdduct lipid = goslin_parser.parse(full_lipid_name, gHandler);
        assertEquals(lipid_class, lipid.getLipidString(LipidLevel.CLASS), lipid.getLipidString(LipidLevel.CLASS) + " != " + lipid_class + "(reference)");
        assertEquals(lipid_formula, lipid.getLipid().getElements().getSumFormula(), full_lipid_name + ": " + lipid_formula + " (reference) != " + lipid.getLipid().getElements().getSumFormula());
        assertTrue(Math.abs(lipid.getMass() - lipid_mass) < 0.001, "lipid: " + lipid.getMass() + " != " + lipid_mass + " (reference)");
        assertEquals(lipid_charge, lipid.getAdduct().getCharge(), lipid.getAdduct().getCharge() + " != " + lipid_charge + " (reference)");
    }

}
