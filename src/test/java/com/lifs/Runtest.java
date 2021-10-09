package com.lifs;

import com.lifs.jgoslin.domain.*;
import com.lifs.jgoslin.parser.*;
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
        ShorthandParser parser = new ShorthandParser();
        
        LipidAdduct lipid = parser.parse("FA 22:4(4,7,10,18);[cy5:0;(OH)2];OH");
        System.out.println(lipid.get_lipid_string());
        //System.out.println(lipid.get_sum_formula());
        //System.out.println(lipid.get_mass());
        
    }
}