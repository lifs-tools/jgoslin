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

import org.lifstools.jgoslin.domain.Adduct;
import org.lifstools.jgoslin.domain.LipidFaBondType;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.FattyAcid;
import org.lifstools.jgoslin.domain.DoubleBonds;
import org.lifstools.jgoslin.domain.Headgroup;
import org.lifstools.jgoslin.domain.Cycle;
import org.lifstools.jgoslin.domain.Elements;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.LipidAdduct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Map.entry;
import java.util.TreeMap;
import org.lifstools.jgoslin.domain.Element;
import org.lifstools.jgoslin.domain.HeadgroupDecorator;
import static org.lifstools.jgoslin.parser.Parser.EOF_SIGN;

/**
 * Event handler implementation for the {@link GoslinParser}.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class GoslinParserEventHandler extends LipidBaseParserEventHandler {

    private int dbPosition;
    private String dbCistrans;
    private char plasmalogen;
    private String mediatorFunction;
    private final ArrayList<Integer> mediatorFunctionPositions = new ArrayList<>();
    private boolean mediatorSuffix;
    private Element heavyElement;
    private int heavyElementNumber;
    private boolean trivialMediator;
    private String prostaglandinType;
    private String prostaglandinNumber;

    private final static Map<String, Integer> MEDIATOR_FA = Map.of(
            "H", 17, "O", 18, "E", 20, "Do", 22);
    private final static Map<String, Integer> MEDIATOR_DB = Map.of(
            "M", 1, "D", 2, "Tr", 3, "T", 4, "P", 5, "H", 6);

    /**
     * Create a new {@code GoslinParserEventHandler}.
     *
     * @param knownFunctionalGroups the known functional groups
     */
    public GoslinParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        super(knownFunctionalGroups);
        try {
            registeredEvents = Map.ofEntries(
                    entry("lipid_pre_event", this::resetParser),
                    entry("lipid_post_event", this::buildLipid),
                    entry("hg_cl_pre_event", this::setHeadGroupName),
                    entry("hg_mlcl_pre_event", this::setHeadGroupName),
                    entry("hg_pl_pre_event", this::setHeadGroupName),
                    entry("hg_lpl_pre_event", this::setHeadGroupName),
                    entry("hg_lsl_pre_event", this::setHeadGroupName),
                    entry("hg_so_lsl_pre_event", this::setHeadGroupName),
                    entry("hg_dsl_pre_event", this::setHeadGroupName),
                    entry("st_pre_event", this::setHeadGroupName),
                    entry("hg_ste_pre_event", this::setHeadGroupName),
                    entry("hg_stes_pre_event", this::setHeadGroupName),
                    entry("hg_mgl_pre_event", this::setHeadGroupName),
                    entry("hg_dgl_pre_event", this::setHeadGroupName),
                    entry("hg_sgl_pre_event", this::setHeadGroupName),
                    entry("hg_tgl_pre_event", this::setHeadGroupName),
                    entry("hg_dlcl_pre_event", this::setHeadGroupName),
                    entry("hg_sac_di_pre_event", this::setHeadGroupName),
                    entry("hg_sac_f_pre_event", this::setHeadGroupName),
                    entry("hg_tpl_pre_event", this::setHeadGroupName),
                    entry("gl_species_pre_event", this::setSpeciesLevel),
                    entry("pl_species_pre_event", this::setSpeciesLevel),
                    entry("sl_species_pre_event", this::setSpeciesLevel),
                    entry("fa2_unsorted_pre_event", this::setMolecularSubspeciesLevel),
                    entry("fa3_unsorted_pre_event", this::setMolecularSubspeciesLevel),
                    entry("fa4_unsorted_pre_event", this::setMolecularSubspeciesLevel),
                    entry("slbpa_pre_event", this::setMolecularSubspeciesLevel),
                    entry("dlcl_pre_event", this::setMolecularSubspeciesLevel),
                    entry("mlcl_pre_event", this::setMolecularSubspeciesLevel),
                    entry("lcb_pre_event", this::newLcb),
                    entry("lcb_post_event", this::cleanLcb),
                    entry("fa_pre_event", this::newFa),
                    entry("fa_post_event", this::appendFa),
                    entry("db_single_position_pre_event", this::setIsomericLevel),
                    entry("db_single_position_post_event", this::addDbPosition),
                    entry("db_position_number_pre_event", this::addDbPositionNumber),
                    entry("cistrans_pre_event", this::addCistrans),
                    entry("ether_pre_event", this::addEther),
                    entry("old_hydroxyl_pre_event", this::addOldHydroxyl),
                    entry("db_count_pre_event", this::addDoubleBonds),
                    entry("carbon_pre_event", this::addCarbon),
                    entry("hydroxyl_pre_event", this::addHydroxyl),
                    entry("adduct_info_pre_event", this::newAdduct),
                    entry("adduct_pre_event", this::addAdduct),
                    entry("charge_pre_event", this::addCharge),
                    entry("charge_sign_pre_event", this::addChargeSign),
                    entry("lpl_pre_event", this::setMolecularSubspeciesLevel),
                    entry("plasmalogen_pre_event", this::setPlasmalogen),
                    entry("mediator_pre_event", this::setMediator),
                    entry("mediator_post_event", this::addMediator),
                    entry("unstructured_mediator_pre_event", this::setUnstructuredMediator),
                    entry("trivial_mediator_pre_event", this::setTrivialMediator),
                    entry("mediator_carbon_pre_event", this::setMediatorCarbon),
                    entry("mediator_db_pre_event", this::setMediatorDB),
                    entry("mediator_mono_functions_pre_event", this::setMediatorFunction),
                    entry("mediator_di_functions_pre_event", this::setMediatorFunction),
                    entry("mediator_tri_functions_pre_event", this::setMediatorFunction),
                    entry("mediator_position_pre_event", this::setMediatorFunctionPosition),
                    entry("mediator_functional_group_post_event", this::addMediatorFunction),
                    entry("mediator_suffix_pre_event", this::addMediatorSuffix),
                    entry("mediator_tetranor_pre_event", this::setMediatorTetranor),
        
                    entry("isotope_pair_pre_event", this::newAdduct),
                    entry("isotope_element_pre_event", this::setHeavyDElement),
                    entry("isotope_number_pre_event", this::setHeavyDNumber),
                    entry("heavy_pre_event", this::newAdduct),
                    entry("adduct_heavy_element_pre_event", this::setHeavyElement),
                    entry("adduct_heavy_number_pre_event", this::setHeavyNumber),
                    entry("adduct_heavy_component_post_event", this::addHeavyComponent),
    
                    entry("prostaglandin_number_pre_event", this::setProstaglandinNumber),
                    entry("prostaglandin_type_pre_event", this::setProstaglandinType),
                    entry("prostaglandin_post_event", this::addProstaglandin)
            );

        } catch (Exception e) {
            throw new LipidParsingException("Cannot initialize GoslinParserEventHandler");
        }
    }

    @Override
    protected void resetParser(TreeNode node) {
        content = null;
        level = LipidLevel.COMPLETE_STRUCTURE;
        headGroup = "";
        lcb = null;
        faList.clear();
        currentFa = null;
        adduct = null;
        dbPosition = 0;
        dbCistrans = "";
        plasmalogen = '\0';
        mediatorFunction = "";
        mediatorFunctionPositions.clear();
        mediatorSuffix = false;
        useHeadGroup = false;
        importedHeadgroup = null;
        headgroupDecorators.clear();
        heavyElement = Element.C;
        heavyElementNumber = 0;
        trivialMediator = false;
        prostaglandinType = "";
        prostaglandinNumber = "";
    }

    private void setHeadGroupName(TreeNode node) {
        headGroup = node.getText();
    }

    protected void setSpeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.SPECIES);
    }

    private void setIsomericLevel(TreeNode node) {
        dbPosition = 0;
        dbCistrans = "";
    }

    private void addDbPosition(TreeNode node) {
        if (currentFa != null) {
            currentFa.getDoubleBonds().getDoubleBondPositions().put(dbPosition, dbCistrans);
            if (!dbCistrans.equals("E") && !dbCistrans.equals("Z")) {
                setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
            }
        }
    }

    private void setPlasmalogen(TreeNode node) {
        plasmalogen = node.getText().toUpperCase().charAt(0);
    }

    private void addDbPositionNumber(TreeNode node) {
        dbPosition = Integer.valueOf(node.getText());
    }

    private void addCistrans(TreeNode node) {
        dbCistrans = node.getText();
    }

    protected void setMolecularSubspeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.MOLECULAR_SPECIES);
    }

    protected void newFa(TreeNode node) {
        LipidFaBondType lipid_FA_bond_type = LipidFaBondType.ESTER;
        currentFa = new FattyAcid("FA", 2, null, null, lipid_FA_bond_type, knownFunctionalGroups);
    }

    protected void newLcb(TreeNode node) {
        lcb = new FattyAcid("LCB", knownFunctionalGroups);
        lcb.setType(LipidFaBondType.LCB_REGULAR);
        currentFa = lcb;
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
    }

    protected void cleanLcb(TreeNode node) {
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }
        currentFa = null;
    }

    protected void appendFa(TreeNode node) {
        if (currentFa.getLipidFaBondType() == LipidFaBondType.ETHER_UNSPECIFIED) {
            throw new LipidException("Lipid with unspecified ether bond cannot be treated properly.");
        }
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }

        if (currentFa.getDoubleBonds().getNumDoubleBonds() < 0) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }

        removeDeoxy(currentFa.getFunctionalGroupsInternal());
        faList.add(currentFa);
        if (headGroup.compareTo("Sa") == 0 || headGroup.compareTo("So") == 0 || headGroup.compareTo("S1P") == 0 || headGroup.compareTo("Sa1P") == 0){
            FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
            if (headGroup.compareTo("Sa") == 0 || headGroup.compareTo("So") == 0){
                functional_group.setCount(2);
                currentFa.setLipidFaBondType(LipidFaBondType.LCB_EXCEPTION);
            }
            else {
                functional_group.setCount(1);
                currentFa.setLipidFaBondType(LipidFaBondType.LCB_REGULAR);
            }
            if (!currentFa.getFunctionalGroupsInternal().containsKey("OH")) {
                currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<>());
            }
            currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group);
        }
        currentFa = null;
    }

    protected void buildLipid(TreeNode node) {
        
        if (lcb != null) {
            faList.add(0, lcb);
            lcb = null;
            currentFa = null;
        }

        if (plasmalogen != '\0' && faList.size() > 0 && lcb == null) {
            faList.get(0).setLipidFaBondType(plasmalogen == 'O' ? LipidFaBondType.ETHER_PLASMANYL : LipidFaBondType.ETHER_PLASMENYL);
        }

        Headgroup headgroup = prepareHeadgroupAndChecks();
        String lipidName = node.getText();
        if (lipidName.charAt(lipidName.length() - 1) == EOF_SIGN) lipidName = lipidName.substring(0, lipidName.length() - 1);
        HashMap<String, ArrayList<String>> trivialDb = knownFunctionalGroups.getTmDb();
        
        if (trivialMediator && trivialDb.containsKey(lipidName)){
            Map<Integer, String> doubleBondPositions = faList.get(0).getDoubleBonds().getDoubleBondPositions();
            doubleBondPositions.clear();
            
            // positions may carry the cis/trans configuration, e.g. '12Z'
            for (String p : trivialDb.get(lipidName)) {
                if (p.isEmpty()) continue;
                char cistrans = p.charAt(p.length() - 1);
                if (cistrans == 'E' || cistrans == 'Z') {
                    doubleBondPositions.put(Integer.valueOf(p.substring(0, p.length() - 1)), String.valueOf(cistrans));
                } else {
                    doubleBondPositions.put(Integer.valueOf(p), "");
                }
            }
            faList.get(0).getDoubleBonds().setNumDoubleBonds(doubleBondPositions.size());
            level = LipidLevel.FULL_STRUCTURE;
        }

        LipidAdduct lipid = new LipidAdduct(assembleLipid(headgroup), adduct);
        content = lipid;

    }

    private void addEther(TreeNode node) {
        String ether = node.getText();
        if (ether.equals("a")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMANYL);
        } else if (ether.equals("p")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMENYL);
            currentFa.getDoubleBonds().setNumDoubleBonds(Math.max(0, currentFa.getDoubleBonds().getNumDoubleBonds() - 1));
        }
        plasmalogen = '\0';
    }

    private void addOldHydroxyl(TreeNode node) {
        String old_hydroxyl = node.getText();
        int num_h = 0;
        if (old_hydroxyl.equals("d")) {
            num_h = 2;
        } else if (old_hydroxyl.equals("t")) {
            num_h = 3;
        }

        if (spRegularLcb()) {
            num_h -= 1;
        }

        FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
        functional_group.setCount(num_h);
        if (!currentFa.getFunctionalGroupsInternal().containsKey("OH")) {
            currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<>());
        }
        currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group);
    }

    protected void addDoubleBonds(TreeNode node) {
        currentFa.getDoubleBonds().setNumDoubleBonds(Integer.valueOf(node.getText()));
    }

    protected void addCarbon(TreeNode node) {
        currentFa.setNumCarbon(Integer.valueOf(node.getText()));
    }

    private void addHydroxyl(TreeNode node) {
        int num_h = Integer.valueOf(node.getText());

        if (spRegularLcb()) {
            num_h -= 1;
        }
        if (num_h <= 0) return;

        FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
        functional_group.setCount(num_h);
        if (!currentFa.getFunctionalGroupsInternal().containsKey("OH")) {
            currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<>());
        }
        currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group);
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
    }

    private void newAdduct(TreeNode node) {
        if (adduct == null) adduct = new Adduct("", "");
    }

    private void addAdduct(TreeNode node) {
        adduct.setAdductString(node.getText());
    }

    private void addCharge(TreeNode node) {
        adduct.setCharge(Integer.valueOf(node.getText()));
    }

    private void addChargeSign(TreeNode node) {
        String sign = node.getText();
        if (sign.equals("+")) {
            adduct.setChargeSign(1);
        } else if (sign.equals("-")) {
            adduct.setChargeSign(-1);
        }
        if (adduct.getCharge() == 0) adduct.setCharge(1);
    }

    private void setMediator(TreeNode node) {
        headGroup = "FA";
        currentFa = new FattyAcid("FA", knownFunctionalGroups);
        faList.add(currentFa);
        // the level is derived from the parsed structure in prepareHeadgroupAndChecks, as in pygoslin
    }

    private void setUnstructuredMediator(TreeNode node) {
        headGroup = node.getText();
        useHeadGroup = true;
        faList.clear();
    }

    private void setMediatorTetranor(TreeNode node) {
        currentFa.setNumCarbon(currentFa.getNumCarbon() - 4);
    }

    private void setMediatorCarbon(TreeNode node) {
        trivialMediator = true;
        currentFa.setNumCarbon(currentFa.getNumCarbon() + MEDIATOR_FA.get(node.getText()));
    }

    private void setMediatorDB(TreeNode node) {
        currentFa.getDoubleBonds().setNumDoubleBonds(MEDIATOR_DB.get(node.getText()));
    }

    private void setMediatorFunction(TreeNode node) {
        mediatorFunction = node.getText();
    }

    private void setMediatorFunctionPosition(TreeNode node) {
        mediatorFunctionPositions.add(node.getInt());
    }

    private void applyMediatorPosition(FunctionalGroup functionalGroup, int index) {
        if (mediatorFunctionPositions.size() > index) {
            functionalGroup.setPosition(mediatorFunctionPositions.get(index));
        }
    }

    private void addMediatorFunction(TreeNode node) {
        FunctionalGroup functionalGroup = null;
        String fg = "";
        String function = mediatorFunction.toLowerCase();
        if (mediatorFunction.equals("H") || mediatorFunction.equals("hydroxy")) {
            functionalGroup = knownFunctionalGroups.get("OH");
            fg = "OH";
            applyMediatorPosition(functionalGroup, 0);
        } else if (function.equals("d") || function.equals("deoxy")) {
            // removes the functional group at that position from a trivial
            // mediator or prostaglandin, see cleanMediator
            functionalGroup = knownFunctionalGroups.get("d");
            fg = "d";
            applyMediatorPosition(functionalGroup, 0);
        } else if (function.equals("oxo") || function.equals("k") || function.equals("keto")) {
            functionalGroup = knownFunctionalGroups.get("oxo");
            fg = "oxo";
            applyMediatorPosition(functionalGroup, 0);
        } else if (function.equals("hp")) {
            functionalGroup = knownFunctionalGroups.get("OOH");
            fg = "OOH";
            applyMediatorPosition(functionalGroup, 0);
        } else if (mediatorFunction.equals("NO2")) {
            functionalGroup = knownFunctionalGroups.get("NO2");
            fg = "NO2";
            applyMediatorPosition(functionalGroup, 0);
        } else if (mediatorFunction.equals("E") || mediatorFunction.equals("Ep") || mediatorFunction.equals("EP")) {
            functionalGroup = knownFunctionalGroups.get("Ep");
            fg = "Ep";
            applyMediatorPosition(functionalGroup, 0);
        } else if (function.equals("dh") || function.equals("dih") || function.equals("dihydro")) {
            functionalGroup = knownFunctionalGroups.get("OH");
            fg = "OH";
            if (mediatorFunctionPositions.size() >= 2) {
                applyMediatorPosition(functionalGroup, 0);
                FunctionalGroup functionalGroup2 = knownFunctionalGroups.get("OH");
                applyMediatorPosition(functionalGroup2, 1);
                currentFa.getFunctionalGroupsInternal().computeIfAbsent("OH", k -> new ArrayList<>()).add(functionalGroup2);
            } else {
                functionalGroup.setCount(2);
            }
        } else if (function.equals("trih") || function.equals("trihydroxy")) {
            functionalGroup = knownFunctionalGroups.get("OH");
            fg = "OH";
            if (mediatorFunctionPositions.size() >= 3) {
                applyMediatorPosition(functionalGroup, 0);
                FunctionalGroup functionalGroup2 = knownFunctionalGroups.get("OH");
                applyMediatorPosition(functionalGroup2, 1);
                FunctionalGroup functionalGroup3 = knownFunctionalGroups.get("OH");
                applyMediatorPosition(functionalGroup3, 2);
                currentFa.getFunctionalGroupsInternal().computeIfAbsent("OH", k -> new ArrayList<>()).add(functionalGroup2);
                currentFa.getFunctionalGroupsInternal().get("OH").add(functionalGroup3);
            } else {
                functionalGroup.setCount(3);
            }
        }

        // e.g. the stereo descriptors alpha / beta do not add a functional group
        if (functionalGroup != null) {
            currentFa.getFunctionalGroupsInternal().computeIfAbsent(fg, k -> new ArrayList<>()).add(functionalGroup);
        }
        // the enclosing mediator_functional_group nodes raise this event again
        mediatorFunctionPositions.clear();
        mediatorFunction = "";
    }


    // replaces the functional group(s) at the position of fg, if findable
    private boolean recursiveDeletion(String fgName, FunctionalGroup fg, Map<String, ArrayList<FunctionalGroup>> functionalGroups){
        String deletedFgName = null;
        for (Entry<String, ArrayList<FunctionalGroup>> kv : functionalGroups.entrySet()){
            if (kv.getValue().removeIf(fgCurr -> fgCurr.getPosition() == fg.getPosition())){
                deletedFgName = kv.getKey();
                break;
            }
            for (FunctionalGroup fgCurr : kv.getValue()){
                if (recursiveDeletion(fgName, fg, fgCurr.getFunctionalGroupsInternal())) return true;
            }
        }
        if (deletedFgName == null) return false;
        if (functionalGroups.get(deletedFgName).isEmpty()) functionalGroups.remove(deletedFgName);
        functionalGroups.computeIfAbsent(fgName, k -> new ArrayList<>()).add(fg);
        return true;
    }
    
    
    private void cleanMediator(FattyAcid fa){
        if (!fa.getFunctionalGroups().isEmpty()){
            for (Entry<String, ArrayList<FunctionalGroup>> kv : fa.getFunctionalGroupsInternal().entrySet()){
                
                
                String fgName = kv.getKey();
                for (FunctionalGroup fg : kv.getValue()){

                    if (fg.getPosition() >= -1){
                        // erase prevously added functional group, if findable
                        if (!recursiveDeletion(fgName, fg, currentFa.getFunctionalGroupsInternal())){
                            if (!currentFa.getFunctionalGroupsInternal().containsKey(fgName)){
                                currentFa.getFunctionalGroupsInternal().put(fgName, new ArrayList<>());
                            }
                            currentFa.getFunctionalGroupsInternal().get(fgName).add(fg);
                        }
                    }
                    else {
                        if (!currentFa.getFunctionalGroupsInternal().containsKey(fgName)){
                            currentFa.getFunctionalGroupsInternal().put(fgName, new ArrayList<>());
                        }
                        currentFa.getFunctionalGroupsInternal().get(fgName).add(fg);
                    }
                }
            }
            fa.getFunctionalGroupsInternal().clear();
        }
        removeDeoxy(currentFa.getFunctionalGroupsInternal());

    }
        
        
    private void removeDeoxy(Map<String, ArrayList<FunctionalGroup>> functionalGroups){
        if (functionalGroups.containsKey("d")){
            functionalGroups.remove("d");
        }
        for (Entry<String, ArrayList<FunctionalGroup>> kv : functionalGroups.entrySet()){
            for (FunctionalGroup fg : kv.getValue()){
                removeDeoxy(fg.getFunctionalGroupsInternal());
            }
        }
    }
    

    private void setTrivialMediator(TreeNode node) {
        headGroup = "FA";
        
        FattyAcid tmpFa = currentFa;
        currentFa = resolveFaSynonym(node.getText());
        if (tmpFa != null){
            cleanMediator(tmpFa);
        }

        faList.clear();
        faList.add(currentFa);
        mediatorSuffix = true;
    }

    private void addMediatorSuffix(TreeNode node) {
        mediatorSuffix = true;
    }

    private void addMediator(TreeNode node) {
        if (!mediatorSuffix) {
            currentFa.getDoubleBonds().setNumDoubleBonds(currentFa.getDoubleBonds().getNumDoubleBonds() - 1);
        }
    }

    private void setHeavyDElement(TreeNode node) {
        adduct.getHeavyElements().put(Element.H2, 1);
    }

    private void setHeavyDNumber(TreeNode node) {
        adduct.getHeavyElements().put(Element.H2, node.getInt());
    }
        
    private void setHeavyElement(TreeNode node) {
        heavyElement = Elements.HEAVY_ELEMENT_TABLE.get(node.getText());
        heavyElementNumber = 1;
    }
    
    private void setHeavyNumber(TreeNode node) {
        heavyElementNumber = node.getInt();
    }
    
    private void addHeavyComponent(TreeNode node) {
        adduct.getHeavyElements().put(heavyElement, adduct.getHeavyElements().get(heavyElement) + heavyElementNumber);
    }

        
    private void setProstaglandinType(TreeNode node){
        prostaglandinType = node.getText();
    }



    private void setProstaglandinNumber(TreeNode node){
        prostaglandinNumber = node.getText();
    }
          
        
        
    private void addProstaglandin(TreeNode node){
        HashSet<String> pgTypes = new HashSet<>(Arrays.asList("A", "B", "D", "E", "F", "H", "J", "K"));
        HashSet<String> pgNumbers = new HashSet<>(Arrays.asList("1", "2", "3"));
        if (!pgTypes.contains(prostaglandinType) || !pgNumbers.contains(prostaglandinNumber)) return;

        DoubleBonds db = null;
        FattyAcid tmpFa = currentFa;
        
        if (prostaglandinNumber.equals("1")) db = new DoubleBonds(new TreeMap<>(Map.of(13, "E")));
        else if (prostaglandinNumber.equals("2")) db = new DoubleBonds(new TreeMap<>(Map.of(5, "Z", 13, "E")));
        else if (prostaglandinNumber.equals("3")) db = new DoubleBonds(new TreeMap<>(Map.of(5, "Z", 13, "E", 17, "Z")));

        if (prostaglandinType.equals("A")){
            FunctionalGroup f1 = knownFunctionalGroups.get("OH");
            FunctionalGroup f2 = knownFunctionalGroups.get("oxo");
            f1.setPosition(15);
            f2.setPosition(9);
            HashMap<String, ArrayList<FunctionalGroup>> fgCy = new HashMap<>(Map.of("oxo", new ArrayList<>(Arrays.asList(f2))));
            Cycle cy = new Cycle(5, 8, 12, null, fgCy, knownFunctionalGroups);
            HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
            db.getDoubleBondPositions().put(10, "Z");
            db.setNumDoubleBonds(db.getDoubleBondPositions().size());
            currentFa = new FattyAcid("FA", 20, db, fg, knownFunctionalGroups);
        }
        else if (prostaglandinType.equals("B")){
            FunctionalGroup f1 = knownFunctionalGroups.get("OH");
            FunctionalGroup f2 = knownFunctionalGroups.get("oxo");
            f1.setPosition(15);
            f2.setPosition(9);
            HashMap<String, ArrayList<FunctionalGroup>> fgCy = new HashMap<>(Map.of("oxo", new ArrayList<>(Arrays.asList(f2))));
            Cycle cy = new Cycle(5, 8, 12, new DoubleBonds(1), fgCy, knownFunctionalGroups);
            HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
            currentFa = new FattyAcid("FA", 20, db, fg, knownFunctionalGroups);
        }
        else if (prostaglandinType.equals("D")){
            FunctionalGroup f1 = knownFunctionalGroups.get("OH");
            FunctionalGroup f2 = knownFunctionalGroups.get("OH");
            FunctionalGroup f3 = knownFunctionalGroups.get("oxo");
            f1.setPosition(15);
            f2.setPosition(9);
            f3.setPosition(11);
            HashMap<String, ArrayList<FunctionalGroup>> fgCy = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f2)), "oxo", new ArrayList<>(Arrays.asList(f3))));
            Cycle cy = new Cycle(5, 8, 12, null, fgCy, knownFunctionalGroups);
            HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
            currentFa = new FattyAcid("FA", 20, db, fg, knownFunctionalGroups);
        }
        else if (prostaglandinType.equals("E")){
            FunctionalGroup f1 = knownFunctionalGroups.get("OH");
            FunctionalGroup f2 = knownFunctionalGroups.get("oxo");
            FunctionalGroup f3 = knownFunctionalGroups.get("OH");
            f1.setPosition(15);
            f2.setPosition(9);
            f3.setPosition(11);
            HashMap<String, ArrayList<FunctionalGroup>> fgCy = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f3)), "oxo", new ArrayList<>(Arrays.asList(f2))));
            Cycle cy = new Cycle(5, 8, 12, null, fgCy, knownFunctionalGroups);
            HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
            currentFa = new FattyAcid("FA", 20, db, fg, knownFunctionalGroups);
        }
        else if (prostaglandinType.equals("F")){
            FunctionalGroup f1 = knownFunctionalGroups.get("OH");
            FunctionalGroup f2 = knownFunctionalGroups.get("OH");
            FunctionalGroup f3 = knownFunctionalGroups.get("OH");
            f1.setPosition(15);
            f2.setPosition(9);
            f3.setPosition(11);
            HashMap<String, ArrayList<FunctionalGroup>> fgCy = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f2, f3))));
            Cycle cy = new Cycle(5, 8, 12, null, fgCy, knownFunctionalGroups);
            HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
            currentFa = new FattyAcid("FA", 20, db, fg, knownFunctionalGroups);
        }
        else if (prostaglandinType.equals("J")){
            FunctionalGroup f1 = knownFunctionalGroups.get("OH");
            FunctionalGroup f2 = knownFunctionalGroups.get("oxo");
            f1.setPosition(15);
            f2.setPosition(11);
            HashMap<String, ArrayList<FunctionalGroup>> fgCy = new HashMap<>(Map.of("oxo", new ArrayList<>(Arrays.asList(f2))));
            Cycle cy = new Cycle(5, 8, 12, new DoubleBonds(1), fgCy, knownFunctionalGroups);
            HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
            currentFa = new FattyAcid("FA", 20, db, fg, knownFunctionalGroups);
        }
        else if (prostaglandinType.equals("K")){
            FunctionalGroup f1 = knownFunctionalGroups.get("OH");
            FunctionalGroup f2 = knownFunctionalGroups.get("oxo");
            FunctionalGroup f3 = knownFunctionalGroups.get("oxo");
            f1.setPosition(15);
            f2.setPosition(9);
            f3.setPosition(11);
            HashMap<String, ArrayList<FunctionalGroup>> fgCy = new HashMap<>(Map.of("oxo", new ArrayList<>(Arrays.asList(f2, f3))));
            Cycle cy = new Cycle(5, 8, 12, null, fgCy, knownFunctionalGroups);
            HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
            currentFa = new FattyAcid("FA", 20, db, fg, knownFunctionalGroups);
        }
        else {
            return;
        }

        cleanMediator(tmpFa);
        faList.clear();
        faList.add(currentFa);
        mediatorSuffix = true;
        prostaglandinType = "";
        prostaglandinNumber = "";
        
    }
}
