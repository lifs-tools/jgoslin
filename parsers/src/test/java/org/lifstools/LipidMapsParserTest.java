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
package org.lifstools;

import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.StringFunctions;
import org.lifstools.jgoslin.parser.LipidMapsParser;
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
public class LipidMapsParserTest extends TestCase {
    public void testLipidMapsParserTest() {
            
        LipidMapsParser parser = new LipidMapsParser();

        LipidAdduct lipid = parser.parse("Cer(d18:1(8Z)/24:0)");
        Assert.assertEquals("Cer 18:1(8);(OH)2/24:0", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("Cer 18:1;O2/24:0", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("Cer 18:1;O2/24:0", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("Cer 42:1;O2", lipid.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C42H83NO3", lipid.get_sum_formula());


        lipid = parser.parse("GalCer(d18:1(5Z)/24:0)");
        Assert.assertEquals("GalCer 18:1(5);OH/24:0", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("GalCer 18:1;O2/24:0", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("GalCer 18:1;O2/24:0", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("GalCer 42:1;O2", lipid.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C48H93NO8", lipid.get_sum_formula());


        lipid = parser.parse("LysoSM(d17:1(4E))");
        Assert.assertEquals("LSM 17:1(4);OH", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("LSM 17:1;O2", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("LSM 17:1;O2", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("LSM 17:1;O2", lipid.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C22H47N2O5P", lipid.get_sum_formula());


        lipid = parser.parse("PE-Cer(d14:1(4E)/20:1(11Z))");
        Assert.assertEquals("EPC 14:1(4);OH/20:1(11)", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("EPC 14:1;O2/20:1", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("EPC 14:1;O2/20:1", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("EPC 34:2;O2", lipid.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C36H71N2O6P", lipid.get_sum_formula());


        lipid = parser.parse("MIPC(t18:0/24:0)");
        Assert.assertEquals("MIPC 18:0;(OH)2/24:0", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("MIPC 18:0;O3/24:0", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("MIPC 18:0;O3/24:0", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("MIPC 42:0;O3", lipid.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C54H106NO17P", lipid.get_sum_formula());


        lipid = parser.parse("PE-Cer(d16:2(4E,6E)/22:1(13Z)(2OH))");
        Assert.assertEquals("EPC 16:2(4,6);OH/22:1(13);OH", lipid.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("EPC 16:2;O2/22:1;O", lipid.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("EPC 16:2;O2/22:1;O", lipid.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("EPC 38:3;O3", lipid.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C40H77N2O7P", lipid.get_sum_formula());


        List<String> lipid_data = null;
        try {
            lipid_data = Files.readAllLines(Path.of("src/main/goslin/testfiles/lipid-maps-test.csv"));
        }
        catch(IOException e){
            throw new RuntimeException("File goslin-test.csv cannot be read.");
        }


        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////

        int i = 0;
        for (String lipid_row : lipid_data){
            ArrayList<String> data = StringFunctions.split_string(lipid_row, ',', '"', true);
            String lipid_name = StringFunctions.strip(data.get(0), '"');
            String correct_lipid_name = StringFunctions.strip(data.get(1), '"');

            try {
                //System.out.println(lipid_name);
                lipid = parser.parse(lipid_name);
                if (!correct_lipid_name.equals("Unsupported lipid") && correct_lipid_name.length() > 0){
                    Assert.assertEquals(lipid_name + " . " + lipid.get_lipid_string() + " != " + correct_lipid_name + " (reference)", correct_lipid_name, lipid.get_lipid_string());
                }
            }
            catch (LipidException e){
                if (correct_lipid_name.length() > 0){
                    System.out.println("Exception: " + i + ": " + lipid_name);
                    System.out.println(e);
                    Assert.assertTrue(false);
                }
            }

            ++i;
        }

        System.out.println("LIPID MAPS Test: All tests passed without any problem");
    }
}
