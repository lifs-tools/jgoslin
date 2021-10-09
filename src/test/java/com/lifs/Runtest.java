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
        
        LipidAdduct lipid = parser.parse("PE-N(FA) 56:5");
        System.out.println(lipid.get_lipid_string());
        System.out.println(lipid.get_sum_formula());
        //System.out.println(lipid.get_mass());
        
    }
}