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
package com.lifs;

import com.lifs.jgoslin.domain.ElementTable;
import com.lifs.jgoslin.parser.SumFormulaParser;
import junit.framework.TestCase;

/**
 * This class tests the spectrum annotation.
 *
 * @author Marc Vaudel
 */
public class Runtest extends TestCase {
    
    
    /**
     * This test evaluates the SpectrumIndex.
     */
    public void testFindPeak() {
        SumFormulaParser parser = SumFormulaParser.get_instance();
        ElementTable e = parser.parse("C12H22O3PN2");
        System.out.println(e);
        
        
        //ShorthandParser parser = new ShorthandParser();
        //LipidAdduct lipid = parser.parse("PA 12:0/12:1");
        //System.out.println(lipid.get_lipid_string());
       
        //FattyAcidParser parser = new FattyAcidParser();
        
        //LipidAdduct lipid = parser.parse("17-methyl-6Z-octadecenoic acid");
        //System.out.println(lipid.get_lipid_string());
        //System.out.println(lipid.get_sum_formula());
        //System.out.println(lipid.get_mass());
        
    }
}