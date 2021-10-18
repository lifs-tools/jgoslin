/*
MIT License

Copyright (c) the authors (listed in global LICENSE file)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package org.lifstools;

import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.parser.GoslinParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 *
 * @author dominik
 */
public class GoslinParserTest extends TestCase {
    public void testGoslinParserTest() {
        
        GoslinParser parser = new GoslinParser();
            
    
        LipidAdduct l = parser.parse("Cer 18:1(8Z);2/24:0");
        Assert.assertEquals("Cer 18:1(8);(OH)2/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("Cer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C42H83NO3", l.get_sum_formula());

        l = parser.parse("HexCer 18:1(5Z);2/24:0");
        Assert.assertEquals("HexCer 18:1(5);OH/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("HexCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("HexCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("HexCer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C48H93NO8", l.get_sum_formula());

        l = parser.parse("LSM 17:1(4E);2");
        Assert.assertEquals("LSM 17:1(4);OH", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C22H47N2O5P", l.get_sum_formula());

        l = parser.parse("LCB 18:1(4E);2");
        Assert.assertEquals("SPB 18:1(4);(OH)2", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C18H37NO2", l.get_sum_formula());

        l = parser.parse("EPC 14:1(4E);2/20:1(11Z)");
        Assert.assertEquals("EPC 14:1(4);OH/20:1(11)", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("EPC 34:2;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C36H71N2O6P", l.get_sum_formula());

        l = parser.parse("MIPC 18:0;3/24:0");
        Assert.assertEquals("MIPC 18:0;(OH)2/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("MIPC 18:0;O3/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("MIPC 18:0;O3/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("MIPC 42:0;O3", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C54H106NO17P", l.get_sum_formula());

        l = parser.parse("EPC 16:2(4E,6E);2/22:1(13Z);1");
        Assert.assertEquals("EPC 16:2(4,6);OH/22:1(13);OH", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("EPC 16:2;O2/22:1;O", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("EPC 16:2;O2/22:1;O", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("EPC 38:3;O3", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C40H77N2O7P", l.get_sum_formula());

        
        LipidAdduct lipid;
        
        List<String> lipid_data = null;
        try {
            lipid_data = Files.readAllLines(Path.of("src/main/goslin/testfiles/goslin-test.csv"));
        }
        catch(IOException e){
            throw new RuntimeException("File goslin-test.csv cannot be read.");
        }


        
        ////////////////////////////////////////////////////////////////////////////
        // Test for correctness
        ////////////////////////////////////////////////////////////////////////////

        for (String lipid_name : lipid_data) {
            lipid = parser.parse(lipid_name);
            Assert.assertTrue(lipid != null);
        }

        System.out.println("Goslin Test: All tests passed without any problem");
        
    }
}
