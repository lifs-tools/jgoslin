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

import com.lifs.jgoslin.domain.Headgroup;
import com.lifs.jgoslin.domain.LipidAdduct;
import com.lifs.jgoslin.domain.LipidClassMeta;
import com.lifs.jgoslin.domain.LipidClasses;
import com.lifs.jgoslin.parser.LipidMapsParser;
import com.lifs.jgoslin.parser.SwissLipidsParser;
import java.util.Map.Entry;
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
        for (Entry<String, Integer> kv : Headgroup.StringClass.entrySet())
            System.out.println(kv);
        int aa = Headgroup.get_class("PIP3[3,4,5]");
        
        
        LipidClassMeta a = LipidClasses.get_instance().get(Headgroup.get_class("PIP3[3,4,5]"));
        
        SwissLipidsParser parser = new SwissLipidsParser();
        parser.parser_event_handler.debug = "a";
        LipidAdduct lipid = parser.parse("PIP3[3,4,5](20:3(11Z,14Z,17Z)/17:0)");
        System.out.println(lipid.get_lipid_string());
        //System.out.println(lipid.get_sum_formula());
        //System.out.println(lipid.get_mass());
        
    }
}