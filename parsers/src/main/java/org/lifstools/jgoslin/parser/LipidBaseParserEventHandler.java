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
package org.lifstools.jgoslin.parser;

import org.lifstools.jgoslin.domain.LipidMolecularSpecies;
import org.lifstools.jgoslin.domain.ConstraintViolationException;
import org.lifstools.jgoslin.domain.LipidStructureDefined;
import org.lifstools.jgoslin.domain.LipidFaBondType;
import org.lifstools.jgoslin.domain.LipidClassMeta;
import org.lifstools.jgoslin.domain.LipidSnPosition;
import org.lifstools.jgoslin.domain.Headgroup;
import org.lifstools.jgoslin.domain.LipidClasses;
import org.lifstools.jgoslin.domain.LipidCompleteStructure;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.Adduct;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidCategory;
import org.lifstools.jgoslin.domain.FattyAcid;
import org.lifstools.jgoslin.domain.LipidSpecies;
import org.lifstools.jgoslin.domain.LipidFullStructure;
import org.lifstools.jgoslin.domain.HeadgroupDecorator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;

/**
 *
 * @author dominik
 */
public class LipidBaseParserEventHandler extends BaseParserEventHandler<LipidAdduct> {

    protected LipidLevel level = LipidLevel.FULL_STRUCTURE;
    protected String headGroup = "";
    protected FattyAcid lcb = null;
    protected List<FattyAcid> faList = new LinkedList<>();
    protected FattyAcid currentFa = null;
    protected Adduct adduct = null;
    protected ArrayList<HeadgroupDecorator> headgroupDecorators = new ArrayList<>();
    protected boolean useHeadGroup = false;
    protected KnownFunctionalGroups knownFunctionalGroups;

    protected static HashSet<String> SP_EXCEPTION_CLASSES = new HashSet<>(Arrays.asList("Cer", "Ceramide", "Sphingosine", "So", "Sphinganine", "Sa", "SPH", "Sph", "LCB"));
    
    public LipidBaseParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        this.knownFunctionalGroups = knownFunctionalGroups;
    }

    public void setLipidLevel(LipidLevel _level) {
        level = level.level < _level.level ? level : _level;
    }

    public boolean spRegularLcb() {
        return Headgroup.getCategory(headGroup) == LipidCategory.SP && (currentFa.getLipidFaBondType() == LipidFaBondType.LCB_REGULAR || currentFa.getLipidFaBondType() == LipidFaBondType.LCB_EXCEPTION) && !(SP_EXCEPTION_CLASSES.contains(headGroup) && headgroupDecorators.isEmpty());
    }

    public Headgroup prepareHeadgroupAndChecks() {
        Headgroup headgroup = new Headgroup(headGroup, headgroupDecorators, useHeadGroup);
        if (useHeadGroup) {
            return headgroup;
        }

        int true_fa = 0;
        for (FattyAcid fa : faList) {
            true_fa += (fa.getNumCarbon() > 0 || fa.getDoubleBonds().getNumDoubleBonds() > 0) ? 1 : 0;
        }
        int poss_fa = (LipidClasses.getInstance().size() > headgroup.getLipidClass()) ? LipidClasses.getInstance().get(headgroup.getLipidClass()).possibleNumFa : 0;

        // make lyso
        boolean can_be_lyso = (LipidClasses.getInstance().size() > Headgroup.getClass("L" + headGroup)) ? LipidClasses.getInstance().get(Headgroup.getClass("L" + headGroup)).specialCases.contains("Lyso") : false;
        LipidClassMeta l = LipidClasses.getInstance().get(Headgroup.getClass("LCL"));
        if (true_fa + 1 == poss_fa && level != LipidLevel.SPECIES && headgroup.getLipidCategory() == LipidCategory.GP && can_be_lyso) {
            headGroup = "L" + headGroup;
            headgroup = new Headgroup(headGroup, headgroupDecorators, useHeadGroup);
            poss_fa = (LipidClasses.getInstance().size() > headgroup.getLipidClass()) ? LipidClasses.getInstance().get(headgroup.getLipidClass()).possibleNumFa : 0;
        } else if (true_fa + 2 == poss_fa && level != LipidLevel.SPECIES && headgroup.getLipidCategory() == LipidCategory.GP && headGroup.equals("CL")) {
            headGroup = "DL" + headGroup;
            headgroup = new Headgroup(headGroup, headgroupDecorators, useHeadGroup);
            poss_fa = (LipidClasses.getInstance().size() > headgroup.getLipidClass()) ? LipidClasses.getInstance().get(headgroup.getLipidClass()).possibleNumFa : 0;
        }

        if (level == LipidLevel.SPECIES) {
            if (true_fa == 0 && poss_fa != 0) {
                String hg_name = headgroup.getHeadgroup();
                throw new ConstraintViolationException("No fatty acyl information lipid class '" + hg_name + "' provided.");
            }
        } else if (true_fa != poss_fa && LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level)) {
            String hg_name = headgroup.getHeadgroup();
            throw new ConstraintViolationException("Number of described fatty acyl chains (" + Integer.toString(true_fa) + ") not allowed for lipid class '" + hg_name + "' (having " + Integer.toString(poss_fa) + " fatty aycl chains).");
        }

        if (LipidClasses.getInstance().get(headgroup.getLipidClass()).specialCases.contains("HC")) {
            faList.get(0).setLipidFaBondType(LipidFaBondType.AMINE);
        }

        int max_num_fa = (LipidClasses.getInstance().size() > headgroup.getLipidClass()) ? LipidClasses.getInstance().get(headgroup.getLipidClass()).maxNumFa : 0;
        if (max_num_fa != faList.size()) {
            setLipidLevel(LipidLevel.MOLECULAR_SPECIES);
        }

        if (faList.size() > 0 && headgroup.isSpException()) {
            faList.get(0).setType(LipidFaBondType.LCB_EXCEPTION);
        }

        return headgroup;
    }

    public LipidSpecies assembleLipid(Headgroup headgroup) {
        LipidSpecies ls = null;
        switch (level) {
            case COMPLETE_STRUCTURE:
                ls = new LipidCompleteStructure(headgroup, faList, knownFunctionalGroups);
                break;
            case FULL_STRUCTURE:
                ls = new LipidFullStructure(headgroup, faList, knownFunctionalGroups);
                break;
            case STRUCTURE_DEFINED:
                ls = new LipidStructureDefined(headgroup, faList, knownFunctionalGroups);
                break;
            case SN_POSITION:
                ls = new LipidSnPosition(headgroup, faList, knownFunctionalGroups);
                break;
            case MOLECULAR_SPECIES:
                ls = new LipidMolecularSpecies(headgroup, faList, knownFunctionalGroups);
                break;
            case SPECIES:
                ls = new LipidSpecies(headgroup, faList, knownFunctionalGroups);
                break;
            default:
                break;
        }
        return ls;
    }

    @Override
    void resetParser(TreeNode node) {
        //
    }
}
