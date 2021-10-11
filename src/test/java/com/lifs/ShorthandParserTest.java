package com.lifs;

import com.lifs.jgoslin.domain.*;
import com.lifs.jgoslin.parser.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * This class tests the spectrum annotation.
 *
 * @author Marc Vaudel
 */
public class ShorthandParserTest extends TestCase {
    public void testShorthandParserTest() {
        ShorthandParser parser = new ShorthandParser();
        LipidAdduct l = parser.parse("PE 18:1(8Z);1OH,3OH/24:0");
        Assert.assertEquals("PE 18:1(8Z);1OH,3OH/24:0", l.get_lipid_string());
        Assert.assertEquals("PE 18:1(8);(OH)2/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("PE 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("PE 18:1;O2_24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("PE 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        
        
        l = parser.parse("Cer 18:1(8Z);1OH,3OH/24:0");
        Assert.assertEquals("Cer 18:1(8Z);1OH,3OH/24:0", l.get_lipid_string());
        Assert.assertEquals("Cer 18:1(8);(OH)2/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("Cer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C42H83NO3", l.get_sum_formula());
        
        
        l = parser.parse("Cer 18:1(8);(OH)2/24:0");
        Assert.assertEquals("Cer 18:1(8);(OH)2/24:0", l.get_lipid_string());
        Assert.assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("Cer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C42H83NO3", l.get_sum_formula());
        
        
        l = parser.parse("Cer 18:1;O2/24:0");
        Assert.assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string());
        Assert.assertEquals("Cer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("Cer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C42H83NO3", l.get_sum_formula());
        
        
        l = parser.parse("Cer 42:1;O2");
        Assert.assertEquals("Cer 42:1;O2", l.get_lipid_string());
        Assert.assertEquals("C42H83NO3", l.get_sum_formula());
        
        
        
        
        l = parser.parse("Gal-Cer(1) 18:1(5Z);3OH/24:0");
        Assert.assertEquals("Gal-Cer(1) 18:1(5Z);3OH/24:0", l.get_lipid_string());
        Assert.assertEquals("Gal-Cer 18:1(5);OH/24:0", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("GalCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("GalCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("GalCer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C48H93NO8", l.get_sum_formula());
        
        
        l = parser.parse("Gal-Cer 18:1(5);OH/24:0");
        Assert.assertEquals("Gal-Cer 18:1(5);OH/24:0", l.get_lipid_string());
        Assert.assertEquals("GalCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("GalCer 18:1;O2/24:0", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("GalCer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C48H93NO8", l.get_sum_formula());
        
        
        
        l = parser.parse("GalCer 18:1;O2/24:0");
        Assert.assertEquals("GalCer 18:1;O2/24:0", l.get_lipid_string());
        Assert.assertEquals("GalCer 42:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C48H93NO8", l.get_sum_formula());
        
        
        
        l = parser.parse("GalCer 42:1;O2");
        Assert.assertEquals("GalCer 42:1;O2", l.get_lipid_string());
        Assert.assertEquals("C48H93NO8", l.get_sum_formula());
        
        
        l = parser.parse("SPB 18:1(4Z);1OH,3OH");
        Assert.assertEquals("SPB 18:1(4Z);1OH,3OH", l.get_lipid_string());
        Assert.assertEquals("SPB 18:1(4);(OH)2", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C18H37NO2", l.get_sum_formula());
        
        
        l = parser.parse("SPB 18:1(4);(OH)2");
        Assert.assertEquals("SPB 18:1(4);(OH)2", l.get_lipid_string());
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C18H37NO2", l.get_sum_formula());
        
        
        l = parser.parse("SPB 18:1;O2");
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string());
        Assert.assertEquals("SPB 18:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C18H37NO2", l.get_sum_formula());
        
        

        
        l = parser.parse("LSM(1) 17:1(4E);3OH");
        Assert.assertEquals("LSM(1) 17:1(4E);3OH", l.get_lipid_string());
        Assert.assertEquals("LSM 17:1(4);OH", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C22H47N2O5P", l.get_sum_formula());
        
        
        l = parser.parse("LSM 17:1(4);OH");
        Assert.assertEquals("LSM 17:1(4);OH", l.get_lipid_string());
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C22H47N2O5P", l.get_sum_formula());
        
        
        l = parser.parse("LSM 17:1;O2");
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string());
        Assert.assertEquals("LSM 17:1;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C22H47N2O5P", l.get_sum_formula());
        
        

        
        l = parser.parse("EPC(1) 14:1(4E);3OH/20:1(11Z)");
        Assert.assertEquals("EPC(1) 14:1(4E);3OH/20:1(11Z)", l.get_lipid_string());
        Assert.assertEquals("EPC 14:1(4);OH/20:1(11)", l.get_lipid_string(LipidLevel.STRUCTURE_DEFINED));
        Assert.assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("EPC 34:2;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C36H71N2O6P", l.get_sum_formula());
        
        
        l = parser.parse("EPC 14:1(4);OH/20:1(11)");
        Assert.assertEquals("EPC 14:1(4);OH/20:1(11)", l.get_lipid_string());
        Assert.assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.SN_POSITION));
        Assert.assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string(LipidLevel.MOLECULAR_SPECIES));
        Assert.assertEquals("EPC 34:2;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C36H71N2O6P", l.get_sum_formula());
        
        
        l = parser.parse("EPC 14:1;O2/20:1");
        Assert.assertEquals("EPC 14:1;O2/20:1", l.get_lipid_string());
        Assert.assertEquals("EPC 34:2;O2", l.get_lipid_string(LipidLevel.SPECIES));
        Assert.assertEquals("C36H71N2O6P", l.get_sum_formula());
        
        
        l = parser.parse("EPC 34:2;O2");
        Assert.assertEquals("EPC 34:2;O2", l.get_lipid_string());
        Assert.assertEquals("C36H71N2O6P", l.get_sum_formula());

        
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of("src/main/goslin/testfiles/shorthand-test.csv"));
        }
        catch(IOException e){
            throw new RuntimeException("File shorthand-test.csv cannot be read.");
        }
        
        ArrayList<LipidLevel> levels = new ArrayList<>(Arrays.asList(LipidLevel.FULL_STRUCTURE, LipidLevel.STRUCTURE_DEFINED, LipidLevel.SN_POSITION, LipidLevel.MOLECULAR_SPECIES, LipidLevel.SPECIES));

        
        for (String lipid_row : lines){
            ArrayList<String> results = StringFunctions.split_string(lipid_row, ',', '"', true);
            for (int i = 0; i < results.size(); ++i) results.set(i, StringFunctions.strip(results.get(i), '"'));
            String lipid_name = results.get(0);

            int col_num = levels.size();
            LipidAdduct lipid = parser.parse(lipid_name);
            String formula = (results.size() > col_num && results.get(col_num).length() > 0) ? results.get(col_num) : lipid.get_sum_formula();

            if (results.size() > col_num){
                Assert.assertTrue(formula.equals(lipid.get_sum_formula()));
            }

            for (int lev = 0; lev < levels.size(); ++lev){
                LipidLevel lipid_level = levels.get(lev);
                String n = lipid.get_lipid_string(lipid_level);
                Assert.assertTrue(results.get(lev).equals(n));
                Assert.assertTrue(formula.equals(lipid.get_sum_formula()));


                LipidAdduct lipid2 = parser.parse(n);
                for (int ll = lev; ll < col_num; ++ll)
                {   
                    Assert.assertEquals("input " + n + " compare " + results.get(ll) + " vs. " + lipid2.get_lipid_string(levels.get(ll)), results.get(ll), lipid2.get_lipid_string(levels.get(ll)));
                    Assert.assertEquals(formula, lipid2.get_sum_formula());
                }
            }
        }

        System.out.println("Shorthand Test: All tests passed without any problem");
    }
}