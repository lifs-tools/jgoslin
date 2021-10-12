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
package com.lifs;

import com.lifs.jgoslin.domain.LipidAdduct;
import com.lifs.jgoslin.domain.LipidLevel;
import com.lifs.jgoslin.domain.StringFunctions;
import com.lifs.jgoslin.parser.GoslinParser;
import com.lifs.jgoslin.parser.LipidMapsParser;
import com.lifs.jgoslin.parser.SwissLipidsParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 *
 * @author dominik
 */
public class SumFormulaTest extends TestCase {
    public void testSumFormulaLM(){
        LipidMapsParser lipid_maps_parser = new LipidMapsParser();

        // test several more lipid names
        List<String> lipid_data = null;
        try {
            lipid_data = Files.readAllLines(Path.of("src/main/goslin/testfiles/formulas-lipid-maps.csv"));
        }
        catch(IOException e){
            throw new RuntimeException("File formulas-lipid-maps.csv cannot be read.");
        }

        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////

        for (String lipid_row : lipid_data){
            List<String> data = StringFunctions.split_string(lipid_row, ',', '"', true);

            String lipid_name = StringFunctions.strip(data.get(0), '"');
            String correct_formula = StringFunctions.strip(data.get(1), '"');
            LipidAdduct lipid = lipid_maps_parser.parse(lipid_name);

            Assert.assertTrue(lipid != null);
            Assert.assertEquals("Error for lipid '" + lipid_name + "': " + lipid.get_sum_formula() + " != " + correct_formula + " (reference)", correct_formula,  lipid.get_sum_formula());

        }

        System.out.println("Sum Formula (LIPID MAPS) Test: All tests passed without any problem");
    }



    public void testSumFormulaSL(){
        SwissLipidsParser lipid_maps_parser = new SwissLipidsParser();

        // test several more lipid names
        List<String> lipid_data = null;
        try {
            lipid_data = Files.readAllLines(Path.of("src/main/goslin/testfiles/formulas-swiss-lipids.csv"));
        }
        catch(IOException e){
            throw new RuntimeException("File formulas-swiss-lipids.csv cannot be read.");
        }
        

        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////

        for (String lipid_row : lipid_data){
            List<String> data = StringFunctions.split_string(lipid_row, ',', '"', true);

            String lipid_name = StringFunctions.strip(data.get(0), '"');
            String correct_formula = StringFunctions.strip(data.get(1), '"');
            LipidAdduct lipid = lipid_maps_parser.parse(lipid_name);

            Assert.assertTrue(lipid != null);
            Assert.assertEquals("Error for lipid '" + lipid_name + "': " + lipid.get_sum_formula() + " != " + correct_formula + " (reference)", correct_formula,  lipid.get_sum_formula());

        }

        System.out.println("Sum Formula (SwissLipids) Test: All tests passed without any problem");
    }



    public void testMasses(){
        GoslinParser parser = new GoslinParser();

        // test several more lipid names
        List<String> lipid_data = null;
        try {
            lipid_data = Files.readAllLines(Path.of("src/main/goslin/testfiles/lipid-masses.csv"));
        }
        catch(IOException e){
            throw new RuntimeException("File lipid-masses.csv cannot be read.");
        }

        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////

        char[] trims = new char[]{'\"'};
        int i = 0;
        LipidAdduct lipid;
        for (String lipid_row : lipid_data){
            if (i++ == 0) continue;
            
            ArrayList<String> data = StringFunctions.split_string(lipid_row, ',', '"', true);
            String lipid_class = StringFunctions.strip(data.get(0), '"');
            String lipid_name = StringFunctions.strip(data.get(1), '"');
            String lipid_formula = StringFunctions.strip(data.get(2), '"');
            String lipid_adduct = StringFunctions.strip(data.get(3), '"');
            double lipid_mass = Double.valueOf(StringFunctions.strip(data.get(4), '"'));
            int lipid_charge = Integer.valueOf(StringFunctions.strip(data.get(5), '"'));

            String full_lipid_name = lipid_name + lipid_adduct;
            lipid = parser.parse(full_lipid_name);

            Assert.assertEquals(lipid.get_lipid_string(LipidLevel.CLASS) + " != " + lipid_class + "(reference)", lipid_class, lipid.get_lipid_string(LipidLevel.CLASS));
            Assert.assertEquals(lipid_formula + " (reference) != " + StringFunctions.compute_sum_formula(lipid.lipid.get_elements()), lipid_formula, StringFunctions.compute_sum_formula(lipid.lipid.get_elements()));
            Assert.assertTrue("lipid: " + lipid.get_mass() + " != " + lipid_mass + " (reference)", Math.abs(lipid.get_mass() - lipid_mass) < 0.001);
            Assert.assertEquals(lipid.adduct.get_charge() + " != " + lipid_charge + " (reference)", lipid_charge, lipid.adduct.get_charge());
        }

        System.out.println("Masses Test: All tests passed without any problem");
    }
    
}
