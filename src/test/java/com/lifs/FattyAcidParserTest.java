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

import junit.framework.TestCase;
import com.lifs.jgoslin.domain.*;
import com.lifs.jgoslin.parser.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;

/**
 *
 * @author dominik
 */
public class FattyAcidParserTest extends TestCase {
    public void testFattyAcidParserTest(){
        
        List<String> lipid_data;
        try {
            lipid_data = Files.readAllLines(Path.of("src/main/goslin/testfiles/fatty-acids-test.csv"));
        }
        catch(IOException e){
            throw new RuntimeException("File fatty-acids-test.csv cannot be read.");
        }
        
        FattyAcidParser fatty_acid_parser = new FattyAcidParser();
        ShorthandParser shorthand_parser = new ShorthandParser();

        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////

        SumFormulaParser sfp = SumFormulaParser.get_instance();
        for (String lipid_row : lipid_data) {
            ArrayList<String> data = StringFunctions.split_string(lipid_row, ',', '"', true);
            String lmid = StringFunctions.strip(data.get(0), '"');
            String lipid_name = StringFunctions.strip(data.get(1), '"');
            String formula = StringFunctions.strip(data.get(2), '"');
            String expected_lipid_name = StringFunctions.strip(data.get(3), '"');

            formula = StringFunctions.compute_sum_formula(sfp.parse(formula));
            LipidAdduct lipid = fatty_acid_parser.parse(lipid_name);
            String lipid_formula = lipid.get_sum_formula();

            Assert.assertEquals(lmid + " '" + lipid_name + "': " + expected_lipid_name + " != " + lipid.get_lipid_string() + " (computed)", expected_lipid_name, lipid.get_lipid_string());
            Assert.assertEquals("formula " + lmid + " '" + lipid_name + "': " + formula + " != " + lipid_formula + " (computed)", formula, lipid_formula);

            if (lipid_name.toLowerCase().contains("cyano")) continue;


            LipidAdduct lipid2 = shorthand_parser.parse(lipid.get_lipid_string());
            lipid_formula = lipid2.get_sum_formula();


            Assert.assertEquals("lipid " + lmid + " '" + lipid_name + "': " + formula + " != " + lipid_formula + " (computed)", formula, lipid_formula);

            lipid2 = shorthand_parser.parse(lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
            lipid_formula = lipid2.get_sum_formula();

            Assert.assertEquals("molecular " + lmid + " '" + lipid_name + "': " + formula + " != " + lipid_formula + " (computed)", formula, lipid_formula);

            lipid2 = shorthand_parser.parse(lipid.get_lipid_string(LipidLevel.SPECIES));
            lipid_formula = lipid2.get_sum_formula();

            Assert.assertEquals("species " + lmid + " '" + lipid_name + "': " + formula + " != " + lipid_formula + " (computed)", formula, lipid_formula);


        }

        System.out.println("Fatty Acids Test: All tests passed without any problem");
    }
}
