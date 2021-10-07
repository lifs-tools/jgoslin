package com.lifs;

import com.lifs.jgoslin.domain.*;
import com.lifs.jgoslin.parser.*;
import java.util.ArrayList;
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
        Headgroup hg = new Headgroup("PC");
        ArrayList<FattyAcid> fa = new ArrayList<>();
        fa.add(new FattyAcid("FA1", 18));
        fa.add(new FattyAcid("FA2", 20));
        LipidAdduct lipid = new LipidAdduct();
        lipid.lipid = new LipidFullStructure(hg, fa);
        System.out.println(lipid.get_lipid_string());
    }
}