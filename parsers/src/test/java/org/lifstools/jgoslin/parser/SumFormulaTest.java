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
import org.lifstools.jgoslin.domain.StringFunctions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 *
 * @author Dominik Kopczynski
 */
public class SumFormulaTest {

    private static LipidMapsParser lipid_maps_parser;
    private static SwissLipidsParser swiss_lipids_parser;
    private static GoslinParser goslin_parser;

    @BeforeAll
    public static void setupParsers() {
        lipid_maps_parser = new LipidMapsParser();
        swiss_lipids_parser = new SwissLipidsParser();
        goslin_parser = new GoslinParser();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testfiles/formulas-lipid-maps.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testSumFormulaLM(String lipid_name, String correct_formula) {
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////
        LipidAdduct lipid = lipid_maps_parser.parse(lipid_name);
        assertTrue(lipid != null);
        assertEquals(correct_formula, lipid.get_sum_formula(), "Error for lipid '" + lipid_name + "': " + lipid.get_sum_formula() + " != " + correct_formula + " (reference)");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testfiles/formulas-swiss-lipids.csv", numLinesToSkip = 0, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testSumFormulaSL(String lipid_name, String correct_formula) {
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////
        LipidAdduct lipid = swiss_lipids_parser.parse(lipid_name);
        assertTrue(lipid != null);
        assertEquals(correct_formula, lipid.get_sum_formula(), "Error for lipid '" + lipid_name + "': " + lipid.get_sum_formula() + " != " + correct_formula + " (reference)");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testfiles/lipid-masses.csv", numLinesToSkip = 1, delimiter = ',', encoding = "UTF-8", lineSeparator = "\n")
    public void testMasses(String lipid_class, String lipid_name, String lipid_formula, String lipid_adduct, double lipid_mass, int lipid_charge) {
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////
        String full_lipid_name = lipid_name + lipid_adduct;
        LipidAdduct lipid = goslin_parser.parse(full_lipid_name);
        assertEquals(lipid_class, lipid.get_lipid_string(LipidLevel.CLASS), lipid.get_lipid_string(LipidLevel.CLASS) + " != " + lipid_class + "(reference)");
        assertEquals(lipid_formula, StringFunctions.compute_sum_formula(lipid.lipid.get_elements()), lipid_formula + " (reference) != " + StringFunctions.compute_sum_formula(lipid.lipid.get_elements()));
        assertTrue(Math.abs(lipid.get_mass() - lipid_mass) < 0.001, "lipid: " + lipid.get_mass() + " != " + lipid_mass + " (reference)");
        assertEquals(lipid_charge, lipid.adduct.get_charge(), lipid.adduct.get_charge() + " != " + lipid_charge + " (reference)");
    }

}
