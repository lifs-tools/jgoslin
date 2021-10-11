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

import com.lifs.jgoslin.domain.LipidAdduct;
import com.lifs.jgoslin.parser.FattyAcidParser;
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
        
        FattyAcidParser parser = new FattyAcidParser();
        parser.parser_event_handler.debug = "a";
        LipidAdduct lipid = parser.parse("N-(1,3-dihydroxypropan-2-yl)-9S,15S-dihydroxy-11-oxo-5Z,13E-prostadienoyl amine");
        System.out.println(lipid.get_lipid_string());
        //System.out.println(lipid.get_sum_formula());
        //System.out.println(lipid.get_mass());
        
    }
}