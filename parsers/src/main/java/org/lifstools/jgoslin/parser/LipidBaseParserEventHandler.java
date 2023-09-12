/*
 * Copyright 2021 Dominik Kopczynski, Nils Hoffmann.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.lifstools.jgoslin.domain.Cycle;
import org.lifstools.jgoslin.domain.DoubleBonds;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public abstract class LipidBaseParserEventHandler extends BaseParserEventHandler<LipidAdduct> {

    protected LipidLevel level = LipidLevel.COMPLETE_STRUCTURE;
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

    protected void setLipidLevel(LipidLevel _level) {
        level = level.level < _level.level ? level : _level;
    }

    protected boolean spRegularLcb() {
        return Headgroup.getCategory(headGroup) == LipidCategory.SP && (currentFa.getLipidFaBondType() == LipidFaBondType.LCB_REGULAR || currentFa.getLipidFaBondType() == LipidFaBondType.LCB_EXCEPTION) && !(SP_EXCEPTION_CLASSES.contains(headGroup) && headgroupDecorators.isEmpty());
    }

    protected Headgroup prepareHeadgroupAndChecks() {
        Headgroup headgroup = new Headgroup(headGroup, headgroupDecorators, useHeadGroup);
        if (useHeadGroup) {
            return headgroup;
        }
        
        headGroup = headgroup.getClassName();

        int true_fa = 0;
        for (FattyAcid fa : faList) {
            true_fa += (fa.getNumCarbon() > 0 || fa.getDoubleBonds().getNumDoubleBonds() > 0) ? 1 : 0;
        }
        int poss_fa = (LipidClasses.getInstance().size() > headgroup.getLipidClass()) ? LipidClasses.getInstance().get(headgroup.getLipidClass()).possibleNumFa : 0;

        // make lyso
        boolean can_be_lyso = (LipidClasses.getInstance().size() > Headgroup.getClass("L" + headGroup)) ? LipidClasses.getInstance().get(Headgroup.getClass("L" + headGroup)).specialCases.contains("Lyso") : false;
        LipidClassMeta l = LipidClasses.getInstance().get(Headgroup.getClass("LCL"));
        if ((true_fa + 1 == poss_fa || true_fa + 2 == poss_fa) && level != LipidLevel.SPECIES && headgroup.getLipidCategory() == LipidCategory.GP && can_be_lyso) {
            if (true_fa + 1 == poss_fa) headGroup = "L" + headGroup;
            else headGroup = "DL" + headGroup;
            headgroup = new Headgroup(headGroup, headgroupDecorators, useHeadGroup);
            poss_fa = (LipidClasses.getInstance().size() > headgroup.getLipidClass()) ? LipidClasses.getInstance().get(headgroup.getLipidClass()).possibleNumFa : 0;
        }
        
        else if ((true_fa + 1 == poss_fa || true_fa + 2 == poss_fa) && level != LipidLevel.SPECIES && headgroup.getLipidCategory() == LipidCategory.GL && headGroup.equals("TG")) {
            if (true_fa + 1 == poss_fa) headGroup = "DG";
            else headGroup = "MG";
            headgroup = new Headgroup(headGroup, headgroupDecorators, useHeadGroup);
            poss_fa = (LipidClasses.getInstance().size() > headgroup.getLipidClass()) ? LipidClasses.getInstance().get(headgroup.getLipidClass()).possibleNumFa : 0;
        }
        

        if (level == LipidLevel.SPECIES) {
            if (true_fa == 0 && poss_fa != 0) {
                throw new ConstraintViolationException("No fatty acyl information lipid class '" + headgroup.getHeadgroup() + "' provided.");
            }
        } else if (true_fa != poss_fa && LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level)) {
            throw new ConstraintViolationException("Number of described fatty acyl chains (" + Integer.toString(true_fa) + ") not allowed for lipid class '" + headgroup.getHeadgroup() + "' (having " + Integer.toString(poss_fa) + " fatty aycl chains).");
        } else if (LipidClasses.getInstance().get(Headgroup.getClass(headGroup)).specialCases.contains("Lyso") && true_fa > poss_fa){
            throw new ConstraintViolationException("Number of described fatty acyl chains (" + Integer.toString(true_fa) + ") not allowed for lipid class '" + headgroup.getHeadgroup() + "' (having " + Integer.toString(poss_fa) + " fatty aycl chains).");
        }

        if (LipidClasses.getInstance().get(headgroup.getLipidClass()).specialCases.contains("HC")) {
            faList.get(0).setLipidFaBondType(LipidFaBondType.ETHER);
        }

        if (LipidClasses.getInstance().get(headgroup.getLipidClass()).specialCases.contains("Amide")) {
            for (FattyAcid fatty : faList){
                fatty.setLipidFaBondType(LipidFaBondType.AMIDE);
            }
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

    protected LipidSpecies assembleLipid(Headgroup headgroup) {
        for (FattyAcid fa : faList){
            if (fa.stereoInformationMissing()){
                setLipidLevel(LipidLevel.FULL_STRUCTURE);
                break;
            }
        }
        
        LipidSpecies ls = null;
        switch (level) {
            case COMPLETE_STRUCTURE ->
                ls = new LipidCompleteStructure(headgroup, faList, knownFunctionalGroups);
            case FULL_STRUCTURE ->
                ls = new LipidFullStructure(headgroup, faList, knownFunctionalGroups);
            case STRUCTURE_DEFINED ->
                ls = new LipidStructureDefined(headgroup, faList, knownFunctionalGroups);
            case SN_POSITION ->
                ls = new LipidSnPosition(headgroup, faList, knownFunctionalGroups);
            case MOLECULAR_SPECIES ->
                ls = new LipidMolecularSpecies(headgroup, faList, knownFunctionalGroups);
            case SPECIES ->
                ls = new LipidSpecies(headgroup, faList, knownFunctionalGroups);
            default -> {
            }
        }
        return ls;
    }
    
    

    protected FattyAcid resolveFaSynonym(String mediatorName) {

        switch (mediatorName) {
            case "Palmitic acid":
                return new FattyAcid("FA", 16, knownFunctionalGroups);

            case "Linoleic acid":
                return new FattyAcid("FA", 18, new DoubleBonds(2), knownFunctionalGroups);

            case "AA":
                return new FattyAcid("FA", 20, new DoubleBonds(4), knownFunctionalGroups);

            case "ALA":
                return new FattyAcid("FA", 18, new DoubleBonds(3), knownFunctionalGroups);

            case "EPA":
                return new FattyAcid("FA", 20, new DoubleBonds(5), knownFunctionalGroups);

            case "DHA":
                return new FattyAcid("FA", 22, new DoubleBonds(6), knownFunctionalGroups);

            case "LTB4": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                f1.setPosition(5);
                f2.setPosition(12);
                DoubleBonds db = new DoubleBonds(new TreeMap<>(Map.of(6, "Z", 8, "E", 10, "E", 14, "Z")));
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2))));
                return new FattyAcid("FA", 20, db, fg, knownFunctionalGroups);
            }

            case "Resolvin D3": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                f1.setPosition(4);
                f2.setPosition(11);
                f3.setPosition(17);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2, f3))));
                return new FattyAcid("FA", 22, new DoubleBonds(6), fg, knownFunctionalGroups);
            }

            case "Maresin 1": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                f1.setPosition(4);
                f2.setPosition(14);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2))));
                return new FattyAcid("FA", 22, new DoubleBonds(6), fg, knownFunctionalGroups);
            }

            case "Resolvin D2": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                f1.setPosition(4);
                f2.setPosition(16);
                f3.setPosition(17);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2, f3))));
                return new FattyAcid("FA", 22, new DoubleBonds(6), fg, knownFunctionalGroups);
            }

            case "Resolvin D5": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                f1.setPosition(7);
                f2.setPosition(17);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2))));
                return new FattyAcid("FA", 22, new DoubleBonds(6), fg, knownFunctionalGroups);
            }

            case "Resolvin D1": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                f1.setPosition(7);
                f2.setPosition(8);
                f3.setPosition(17);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2, f3))));
                return new FattyAcid("FA", 22, new DoubleBonds(6), fg, knownFunctionalGroups);
            }

            case "TXB1": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                FunctionalGroup f4 = knownFunctionalGroups.get("oxy");
                f1.setPosition(15);
                f2.setPosition(9);
                f3.setPosition(11);
                f4.setPosition(11);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f2, f3)), "oxo", new ArrayList<>(Arrays.asList(f4))));
                Cycle cy = new Cycle(5, 8, 12, fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                return new FattyAcid("FA", 20, new DoubleBonds(1), fg, knownFunctionalGroups);
            }

            case "TXB2": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                FunctionalGroup f4 = knownFunctionalGroups.get("oxy");
                f1.setPosition(15);
                f2.setPosition(9);
                f3.setPosition(11);
                f4.setPosition(11);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f2, f3)), "oxy", new ArrayList<>(Arrays.asList(f4))));
                Cycle cy = new Cycle(5, 8, 12, fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                return new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }

            case "TXB3": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                FunctionalGroup f4 = knownFunctionalGroups.get("oxy");
                f1.setPosition(15);
                f2.setPosition(9);
                f3.setPosition(11);
                f4.setPosition(11);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f2, f3)), "oxy", new ArrayList<>(Arrays.asList(f4))));
                Cycle cy = new Cycle(5, 8, 12, fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                return new FattyAcid("FA", 20, new DoubleBonds(3), fg, knownFunctionalGroups);
            }

            case "PGF2alpha": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                f1.setPosition(15);
                f2.setPosition(9);
                f3.setPosition(11);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f2, f3))));
                Cycle cy = new Cycle(5, 8, 12, fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                return new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }

            case "PGD2": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("oxo");
                f1.setPosition(15);
                f2.setPosition(9);
                f3.setPosition(11);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f2)), "oxo", new ArrayList<>(Arrays.asList(f3))));
                Cycle cy = new Cycle(5, 8, 12, fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                return new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }

            case "PGE2": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("oxo");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                f1.setPosition(15);
                f2.setPosition(9);
                f3.setPosition(11);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f3)), "oxo", new ArrayList<>(Arrays.asList(f2))));
                Cycle cy = new Cycle(5, 8, 12, fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                return new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }

            case "PGB2": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                f1.setPosition(15);
                f2.setPosition(9);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f2))));
                Cycle cy = new Cycle(5, 8, 12, new DoubleBonds(1), fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                return new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }

            case "15d-PGJ2": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("oxo");
                f1.setPosition(15);
                f2.setPosition(11);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("oxo", new ArrayList<>(Arrays.asList(f2))));
                Cycle cy = new Cycle(5, 8, 12, new DoubleBonds(1), fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                return new FattyAcid("FA", 20, new DoubleBonds(3), fg, knownFunctionalGroups);
            }

            case "PGJ2": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("oxo");
                f1.setPosition(15);
                f2.setPosition(11);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f2))));
                Cycle cy = new Cycle(5, 8, 12, new DoubleBonds(1), fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                return new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }
            
            default:
                return null;
        }
    }

}
