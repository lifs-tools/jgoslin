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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;
import org.lifstools.jgoslin.domain.Element;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidParsingException;

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
    
    private static final Map<String, ArrayList<String>> GLYCO_TABLE = Map.ofEntries(
        entry("ga2", new ArrayList<String>(Arrays.asList("GalNAc", "Gal", "Glc"))),
        entry("gb3", new ArrayList<String>(Arrays.asList("Gal", "Gal", "Glc"))),
        entry("gb4", new ArrayList<String>(Arrays.asList("GalNAc", "Gal", "Gal", "Glc"))),
        entry("gd1", new ArrayList<String>(Arrays.asList("Gal", "GalNAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gd1a", new ArrayList<String>(Arrays.asList("Hex", "Hex", "Hex", "HexNAc", "NeuAc", "NeuAc"))),
        entry("gd2", new ArrayList<String>(Arrays.asList("GalNAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gd3", new ArrayList<String>(Arrays.asList("NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gm1", new ArrayList<String>(Arrays.asList("Gal", "GalNAc", "NeuAc", "Gal", "Glc"))),
        entry("gm2", new ArrayList<String>(Arrays.asList("GalNAc", "NeuAc", "Gal", "Glc"))),
        entry("gm3", new ArrayList<String>(Arrays.asList("NeuAc", "Gal", "Glc"))),
        entry("gm4", new ArrayList<String>(Arrays.asList("NeuAc", "Gal"))),
        entry("gp1", new ArrayList<String>(Arrays.asList("NeuAc", "NeuAc", "Gal", "GalNAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gq1", new ArrayList<String>(Arrays.asList("NeuAc", "Gal", "GalNAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gt1", new ArrayList<String>(Arrays.asList("Gal", "GalNAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gt2", new ArrayList<String>(Arrays.asList("GalNAc", "NeuAc", "NeuAc", "NeuAc", "Gal", "Glc"))),
        entry("gt3", new ArrayList<String>(Arrays.asList("NeuAc", "NeuAc", "NeuAc", "Gal", "Glc")))
    );

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
        
        String hg = headGroup.toLowerCase();
        if (GLYCO_TABLE.containsKey(hg)){
            for (String carbohydrate : GLYCO_TABLE.get(hg)){
                FunctionalGroup functional_group = null;
                try {
                    functional_group = knownFunctionalGroups.get(carbohydrate);
                } catch (Exception e) {
                    throw new LipidParsingException("Carbohydrate '" + carbohydrate + "' unknown");
                }

                functional_group.getElements().put(Element.O, functional_group.getElements().get(Element.O) - 1);
                headgroupDecorators.add((HeadgroupDecorator) functional_group);

            }
            headGroup = "Cer";
        }
        
        
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

}
