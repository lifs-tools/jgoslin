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

import java.util.ArrayDeque;
import org.lifstools.jgoslin.domain.ConstraintViolationException;
import org.lifstools.jgoslin.domain.GenericList;
import org.lifstools.jgoslin.domain.LipidFaBondType;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.Element;
import org.lifstools.jgoslin.domain.Headgroup;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.AcylAlkylGroup;
import org.lifstools.jgoslin.domain.Adduct;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.CarbonChain;
import org.lifstools.jgoslin.domain.Elements;
import org.lifstools.jgoslin.domain.Cycle;
import org.lifstools.jgoslin.domain.FattyAcid;
import org.lifstools.jgoslin.domain.HeadgroupDecorator;
import org.lifstools.jgoslin.domain.Dictionary;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Set;

/**
 * Event handler implementation for the {@link ShorthandParser}.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class ShorthandParserEventHandler extends LipidBaseParserEventHandler {

    private ArrayDeque<FunctionalGroup> currentFas = new ArrayDeque<>();
    private Dictionary tmp = new Dictionary();
    private boolean acerSpecies = false;
    private static final Set<String> SPECIAL_TYPES = Set.of("acyl", "alkyl", "decorator_acyl", "decorator_alkyl", "cc");
    private boolean containsStereoInformation = false;

    /**
     * Create a new {@code ShorthandParserEventHandler}.
     *
     * @param knownFunctionalGroups the known functional groups
     */
    public ShorthandParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        super(knownFunctionalGroups);
        try {
            registeredEvents = Map.ofEntries(
                    entry("lipid_pre_event", this::resetParser),
                    entry("lipid_post_event", this::buildLipid),
                    // set categories
                    entry("sl_pre_event", this::preSphingolipid),
                    entry("sl_post_event", this::postSphingolipid),
                    entry("sl_hydroxyl_pre_event", this::setHydroxyl),
                    // set adduct events
                    entry("adduct_info_pre_event", this::newAdduct),
                    entry("adduct_pre_event", this::addAdduct),
                    entry("charge_pre_event", this::addCharge),
                    entry("charge_sign_pre_event", this::addChargeSign),
                    // set species events
                    entry("med_species_pre_event", this::setSpeciesLevel),
                    entry("gl_species_pre_event", this::setSpeciesLevel),
                    entry("gl_molecular_species_pre_event", this::setMolecularLevel),
                    entry("pl_species_pre_event", this::setSpeciesLevel),
                    entry("pl_molecular_species_pre_event", this::setMolecularLevel),
                    entry("sl_species_pre_event", this::setSpeciesLevel),
                    entry("pl_single_pre_event", this::setMolecularLevel),
                    entry("unsorted_fa_separator_pre_event", this::setMolecularLevel),
                    entry("ether_num_pre_event", this::setEtherNum),
                    entry("stereo_type_fa_pre_event", this::setFattyAcylStereo),
                    // set head groups events
                    entry("med_hg_single_pre_event", this::setHeadgroupName),
                    entry("med_hg_double_pre_event", this::setHeadgroupName),
                    entry("med_hg_triple_pre_event", this::setHeadgroupName),
                    entry("gl_hg_single_pre_event", this::setHeadgroupName),
                    entry("gl_hg_double_pre_event", this::setHeadgroupName),
                    entry("gl_hg_glycosyl_single_pre_event", this::setHeadgroupName),
                    entry("gl_hg_glycosyl_double_pre_event", this::setHeadgroupName),
                    entry("gl_hg_triple_pre_event", this::setHeadgroupName),
                    entry("pl_hg_single_pre_event", this::setHeadgroupName),
                    entry("pl_hg_double_pre_event", this::setHeadgroupName),
                    entry("pl_hg_quadro_pre_event", this::setHeadgroupName),
                    entry("sl_hg_single_pre_event", this::setHeadgroupName),
                    entry("pl_hg_double_fa_hg_pre_event", this::setHeadgroupName),
                    entry("sl_hg_double_name_pre_event", this::setHeadgroupName),
                    entry("sl_hg_glyco_pre_event", this::setHeadgroupName),
                    entry("st_hg_pre_event", this::setHeadgroupName),
                    entry("st_hg_ester_pre_event", this::setHeadgroupName),
                    entry("hg_pip_pure_m_pre_event", this::setHeadgroupName),
                    entry("hg_pip_pure_d_pre_event", this::setHeadgroupName),
                    entry("hg_pip_pure_t_pre_event", this::setHeadgroupName),
                    entry("hg_PE_PS_pre_event", this::setHeadgroupName),
                    // set head group headgroupDecorators
                    entry("carbohydrate_pre_event", this::setCarbohydrate),
                    entry("carbohydrate_structural_pre_event", this::setCarbohydrateStructural),
                    entry("carbohydrate_isomeric_pre_event", this::setCarbohydrateIsomeric),
                    entry("glyco_sphingo_lipid_pre_event", this::setGlycoSphingoLipid),
                    entry("carbohydrate_number_pre_event", this::setCarbohydrateNumber),
                    // fatty acyl events
                    entry("lcb_pre_event", this::newLcb),
                    entry("lcb_post_event", this::addFattyAcylChain),
                    entry("fatty_acyl_chain_pre_event", this::newFattyAcylChain),
                    entry("fatty_acyl_chain_post_event", this::addFattyAcylChain),
                    entry("carbon_pre_event", this::setCarbon),
                    entry("db_count_pre_event", this::setDoubleBondCount),
                    entry("db_position_number_pre_event", this::setDoubleBondPosition),
                    entry("db_single_position_pre_event", this::setDoubleBondInformation),
                    entry("db_single_position_post_event", this::addDoubleBondInformation),
                    entry("cistrans_pre_event", this::setCisTrans),
                    entry("ether_type_pre_event", this::setEtherType),
                    // set functional group events
                    entry("func_group_data_pre_event", this::setFunctionalGroup),
                    entry("func_group_data_post_event", this::addFunctionalGroup),
                    entry("func_group_pos_number_pre_event", this::setFunctionalGroupPosition),
                    entry("func_group_name_pre_event", this::setFunctionalGroupName),
                    entry("func_group_count_pre_event", this::setFunctionalGroupCount),
                    entry("stereo_type_fg_pre_event", this::setFunctionalGroupStereo),
                    entry("molecular_func_group_name_pre_event", this::setSnPositionFuncGroup),
                    entry("fa_db_only_post_event", this::addDiHydroxyl),
                    // set cycle events
                    entry("func_group_cycle_pre_event", this::setCycle),
                    entry("func_group_cycle_post_event", this::addCycle),
                    entry("cycle_start_pre_event", this::setCycleStart),
                    entry("cycle_end_pre_event", this::setCycleEnd),
                    entry("cycle_number_pre_event", this::setCycleNumber),
                    entry("cycle_db_cnt_pre_event", this::setCycleDbCount),
                    entry("cycle_db_positions_pre_event", this::setCycleDbPositions),
                    entry("cycle_db_positions_post_event", this::checkCycleDbPositions),
                    entry("cycle_db_position_number_pre_event", this::setCycleDbPosition),
                    entry("cycle_db_position_cis_trans_pre_event", this::setCycleDbPositionCistrans),
                    entry("cylce_element_pre_event", this::addCycleElement),
                    // set linkage events
                    entry("fatty_acyl_linkage_pre_event", this::setAcylLinkage),
                    entry("fatty_acyl_linkage_post_event", this::addAcylLinkage),
                    entry("fatty_alkyl_linkage_pre_event", this::setAlkylLinkage),
                    entry("fatty_alkyl_linkage_post_event", this::addAlkylLinkage),
                    entry("fatty_linkage_number_pre_event", this::setFattyLinkageNumber),
                    entry("fatty_acyl_linkage_sign_pre_event", this::setLinkageType),
                    entry("hydrocarbon_chain_pre_event", this::setHydrocarbonChain),
                    entry("hydrocarbon_chain_post_event", this::addHydrocarbonChain),
                    entry("hydrocarbon_number_pre_event", this::setFattyLinkageNumber),
                    // set remaining events
                    entry("ring_stereo_pre_event", this::setRingStereo),
                    entry("pl_hg_fa_pre_event", this::setHgAcyl),
                    entry("pl_hg_fa_post_event", this::addHgAcyl),
                    entry("pl_hg_alk_pre_event", this::setHgAlkyl),
                    entry("pl_hg_alk_post_event", this::addHgAlkyl),
                    entry("pl_hg_species_pre_event", this::addPlSpeciesData),
                    entry("hg_pip_m_pre_event", this::suffixDecoratorMolecular),
                    entry("hg_pip_d_pre_event", this::suffixDecoratorMolecular),
                    entry("hg_pip_t_pre_event", this::suffixDecoratorMolecular),
                    entry("hg_PE_PS_type_pre_event", this::suffixDecoratorSpecies),
                    entry("acer_hg_post_event", this::setAcer),
                    entry("acer_species_post_event", this::setAcerSpecies),
                    entry("sterol_definition_post_event", this::setSterolDefinition)
            );

        } catch (Exception e) {
            throw new LipidParsingException("Cannot initialize ShorthandParserEventHandler.");
        }
    }

    @Override
    protected void resetParser(TreeNode node) {
        content = null;
        level = LipidLevel.COMPLETE_STRUCTURE;
        adduct = null;
        headGroup = "";
        faList.clear();
        currentFas.clear();
        headgroupDecorators.clear();
        tmp = new Dictionary();
        acerSpecies = false;
        containsStereoInformation = false;
    }

    private String faI() {
        return "fa" + Integer.toString(currentFas.size());
    }
    
    
    
    private void setGlycoSphingoLipid(TreeNode node) {
    }
    
    private void setCarbohydrateNumber(TreeNode node) {
        int carbohydrateNum = node.getInt();
        if (!headgroupDecorators.isEmpty() && carbohydrateNum > 0){
            HeadgroupDecorator lastElement = headgroupDecorators.get(headgroupDecorators.size() - 1);
            int cnt = lastElement.getCount();
            lastElement.setCount(cnt + carbohydrateNum - 1);
        }
    }
    

    private void buildLipid(TreeNode node) {
        if (acerSpecies) {
            faList.get(0).setNumCarbon(faList.get(0).getNumCarbon() - 2);
        }
        Headgroup headgroup = prepareHeadgroupAndChecks();

        // add count numbers for fatty acyl chains
        int fa_it = (faList.size() > 0 && (faList.get(0).getLipidFaBondType() == LipidFaBondType.LCB_EXCEPTION || faList.get(0).getLipidFaBondType() == LipidFaBondType.LCB_REGULAR)) ? 1 : 0;
        for (int it = fa_it; it < faList.size(); ++it) {
            faList.get(it).setName(faList.get(it).getName() + Integer.toString(it + 1));
        }

        LipidAdduct lipid = new LipidAdduct(assembleLipid(headgroup), adduct);

        if (tmp.containsKey("num_ethers")) {
            lipid.getLipid().getInfo().numEthers = (int) tmp.get("num_ethers");
        }

        content = lipid;
    }

    private void setSterolDefinition(TreeNode node) {
        headGroup += " " + node.getText();
        faList.remove(0);

    }

    private void preSphingolipid(TreeNode node) {
        tmp.put("sl_hydroxyl", 0);
    }

    private void postSphingolipid(TreeNode node) {
        if (((int) tmp.get("sl_hydroxyl")) == 0 && !headGroup.equals("Cer") && !headGroup.equals("SPB")) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }
    }

    private void setHydroxyl(TreeNode node) {
        tmp.put("sl_hydroxyl", 1);
    }

    private void newAdduct(TreeNode node) {
        adduct = new Adduct("", "", 0, 0);
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
        } else {
            adduct.setChargeSign(-1);
        }
    }

    private void setSpeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.SPECIES);
    }

    private void setMolecularLevel(TreeNode node) {
        setLipidLevel(LipidLevel.MOLECULAR_SPECIES);
    }

    private void setEtherNum(TreeNode node) {
        int num_ethers = 0;
        String ether = node.getText();
        switch (ether) {
            case "d":
                num_ethers = 2;
                break;
            case "t":
                num_ethers = 3;
                break;
            case "e":
                num_ethers = 4;
                break;
            default:
                break;
        }
        tmp.put("num_ethers", num_ethers);
    }

    private void setHeadgroupName(TreeNode node) {
        if (headGroup.length() == 0) {
            headGroup = node.getText();
        }
    }

    private void setCarbohydrate(TreeNode node) {
        String carbohydrate = node.getText();
        FunctionalGroup functional_group = null;
        try {
            functional_group = knownFunctionalGroups.get(carbohydrate);
        } catch (Exception e) {
            throw new LipidParsingException("Carbohydrate '" + carbohydrate + "' unknown");
        }

        functional_group.getElements().put(Element.O, functional_group.getElements().get(Element.O) - 1);
        if (tmp.containsKey("func_group_head") && ((int) tmp.get("func_group_head") == 1)) {
            headgroupDecorators.add((HeadgroupDecorator) functional_group);
        } else {
            if (!currentFas.peekLast().getFunctionalGroupsInternal().containsKey(carbohydrate)) {
                currentFas.peekLast().getFunctionalGroupsInternal().put(carbohydrate, new ArrayList<>());
            }
            currentFas.peekLast().getFunctionalGroupsInternal().get(carbohydrate).add(functional_group);
        }
    }
    
    private void setFattyAcylStereo(TreeNode node){
        currentFas.getLast().setStereochemistry(node.getText());
        containsStereoInformation = true;
    }
    

    private void setCarbohydrateStructural(TreeNode node) {
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        tmp.put("func_group_head", 1);
    }

    private void setCarbohydrateIsomeric(TreeNode node) {
        tmp.put("func_group_head", 1);
    }

    private void newLcb(TreeNode node) {
        newFattyAcylChain(node);
        currentFas.getLast().setName("LCB");
        ((FattyAcid)currentFas.getLast()).setType(LipidFaBondType.LCB_REGULAR);
    }

    private void newFattyAcylChain(TreeNode node) {
        currentFas.add(new FattyAcid("FA", knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
    }

    private void addFattyAcylChain(TreeNode node) {
        String fg_i = "fa" + Integer.toString(currentFas.size() - 2);
        String special_type = "";
        if (currentFas.size() >= 2 && tmp.containsKey(fg_i) && ((Dictionary) tmp.get(fg_i)).containsKey("fg_name")) {
            String fg_name = (String) ((Dictionary) tmp.get(fg_i)).get("fg_name");
            if (SPECIAL_TYPES.contains(fg_name)) {
                special_type = fg_name;
            }
        }

        String fa_i = faI();
        if (currentFas.peekLast().getDoubleBonds().getNumDoubleBonds() != (int) ((Dictionary) tmp.get(fa_i)).get("db_count")) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        } else if (currentFas.peekLast().getDoubleBonds().getNumDoubleBonds() > 0 && currentFas.peekLast().getDoubleBonds().getDoubleBondPositions().isEmpty()) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }
        tmp.remove(fa_i);

        FattyAcid fa = (FattyAcid) currentFas.pollLast();
        if (special_type.length() > 0) {
            fa.setName(special_type);
            if (!currentFas.peekLast().getFunctionalGroupsInternal().containsKey(special_type)) {
                currentFas.peekLast().getFunctionalGroupsInternal().put(special_type, new ArrayList<>());
            }
            currentFas.peekLast().getFunctionalGroupsInternal().get(special_type).add(fa);
        } else {
            faList.add(fa);
        }
    }

    private void setCarbon(TreeNode node) {
        ((FattyAcid) currentFas.peekLast()).setNumCarbon(Integer.valueOf(node.getText()));
    }

    private void setDoubleBondCount(TreeNode node) {
        int db_cnt = Integer.valueOf(node.getText());
        ((Dictionary) tmp.get(faI())).put("db_count", db_cnt);
        ((FattyAcid) currentFas.peekLast()).getDoubleBonds().setNumDoubleBonds(db_cnt);
    }

    private void setDoubleBondPosition(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("db_position", Integer.valueOf(node.getText()));
    }

    private void setDoubleBondInformation(TreeNode node) {
        String fa_i = faI();
        ((Dictionary) tmp.get(fa_i)).put("db_position", 0);
        ((Dictionary) tmp.get(fa_i)).put("db_cistrans", "");
    }

    private void addDoubleBondInformation(TreeNode node) {
        String fa_i = faI();
        Dictionary d = (Dictionary) tmp.get(fa_i);
        int pos = (int) d.get("db_position");
        String cistrans = (String) d.get("db_cistrans");

        if (cistrans.equals("")) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }

        d.remove("db_position");
        d.remove("db_cistrans");
        currentFas.peekLast().getDoubleBonds().getDoubleBondPositions().put(pos, cistrans);
    }

    private void setCisTrans(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("db_cistrans", node.getText());
    }

    private void setEtherType(TreeNode node) {
        String ether_type = node.getText();
        if (ether_type.equals("O-")) {
            ((FattyAcid) currentFas.peekLast()).setLipidFaBondType(LipidFaBondType.ETHER_PLASMANYL);
        } else if (ether_type.equals("P-")) {
            ((FattyAcid) currentFas.peekLast()).setLipidFaBondType(LipidFaBondType.ETHER_PLASMENYL);
        }
    }

    private void setFunctionalGroup(TreeNode node) {
        String fa_i = faI();
        Dictionary gd = (Dictionary) tmp.get(fa_i);
        gd.put("fg_pos", -1);
        gd.put("fg_name", "0");
        gd.put("fg_cnt", 1);
        gd.put("fg_stereo", "");
        gd.put("fg_ring_stereo", "");
    }

    
    private void addDiHydroxyl(TreeNode node) {
        if (!FattyAcid.LCB_STATES.contains(((FattyAcid)currentFas.getLast()).getLipidFaBondType())) return;
        int num_h = 1;

        if (FattyAcid.fgExceptions.contains(headGroup) && !headgroupDecorators.isEmpty()) {
            num_h -= 1;
        }

        FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
        functional_group.setCount(num_h);
        if (!currentFa.getFunctionalGroupsInternal().containsKey("OH")) {
            currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<>());
        }
        currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group);
    }
    

    private void addFunctionalGroup(TreeNode node) {
        Dictionary gd = (Dictionary) tmp.get(faI());
        String fg_name = (String) gd.get("fg_name");

        if (SPECIAL_TYPES.contains(fg_name) || fg_name.equals("cy")) {
            return;
        }

        int fg_pos = (int) gd.get("fg_pos");
        int fg_cnt = (int) gd.get("fg_cnt");
        String fg_stereo = (String) gd.get("fg_stereo");
        String fg_ring_stereo = (String) gd.get("fg_ring_stereo");

        if (fg_pos == -1) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }
        if (fg_cnt <= 0) return;

        FunctionalGroup functional_group = null;
        try {
            functional_group = knownFunctionalGroups.get(fg_name);
        } catch (Exception e) {
            throw new LipidParsingException("'" + fg_name + "' unknown");
        }

        functional_group.setPosition(fg_pos);
        functional_group.setCount(fg_cnt);
        functional_group.setStereochemistry(fg_stereo);
        functional_group.setRingStereo(fg_ring_stereo);

        gd.remove("fg_pos");
        gd.remove("fg_name");
        gd.remove("fg_cnt");
        gd.remove("fg_stereo");

        if (!currentFas.peekLast().getFunctionalGroupsInternal().containsKey(fg_name)) {
            currentFas.peekLast().getFunctionalGroupsInternal().put(fg_name, new ArrayList<>());
        }
        currentFas.peekLast().getFunctionalGroupsInternal().get(fg_name).add(functional_group);
    }

    private void setFunctionalGroupPosition(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_pos", Integer.valueOf(node.getText()));
    }

    private void setFunctionalGroupName(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", node.getText());
    }

    private void setFunctionalGroupCount(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_cnt", Integer.valueOf(node.getText()));
    }

    private void setFunctionalGroupStereo(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_stereo", node.getText());
        containsStereoInformation = true;
    }

    private void setSnPositionFuncGroup(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", node.getText());
        setLipidLevel(LipidLevel.SN_POSITION);
    }

    private void setCycle(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", "cy");
        currentFas.add(new Cycle(0, knownFunctionalGroups));

        String fa_i = faI();
        tmp.put(fa_i, new Dictionary());
        ((Dictionary) tmp.get(fa_i)).put("cycle_elements", new GenericList());
    }

    private void addCycle(TreeNode node) {
        String fa_i = faI();
        GenericList cycle_elements = (GenericList) ((Dictionary) tmp.get(fa_i)).get("cycle_elements");
        Cycle cycle = (Cycle) currentFas.pollLast();
        for (int i = 0; i < cycle_elements.size(); ++i) {
            cycle.getBridgeChain().add((Element) cycle_elements.get(i));
        }
        ((Dictionary) tmp.get(fa_i)).remove("cycle_elements");

        if (cycle.getStart() > -1 && cycle.getEnd() > -1 && cycle.getEnd() - cycle.getStart() + 1 + cycle.getBridgeChain().size() < cycle.getCycle()) {
            throw new ConstraintViolationException("Cycle length '" + Integer.toString(cycle.getCycle()) + "' does not match with cycle description.");
        }
        if (!currentFas.peekLast().getFunctionalGroupsInternal().containsKey("cy")) {
            currentFas.peekLast().getFunctionalGroupsInternal().put("cy", new ArrayList<>());
        }
        currentFas.peekLast().getFunctionalGroupsInternal().get("cy").add(cycle);
    }

    private void setCycleStart(TreeNode node) {
        ((Cycle) currentFas.peekLast()).setStart(Integer.valueOf(node.getText()));
    }

    private void setCycleEnd(TreeNode node) {
        ((Cycle) currentFas.peekLast()).setEnd(Integer.valueOf(node.getText()));
    }

    private void setCycleNumber(TreeNode node) {
        ((Cycle) currentFas.peekLast()).setCycle(Integer.valueOf(node.getText()));
    }

    private void setCycleDbCount(TreeNode node) {
        ((Cycle) currentFas.peekLast()).getDoubleBonds().setNumDoubleBonds(Integer.valueOf(node.getText()));
    }

    private void setCycleDbPositions(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("cycle_db", ((Cycle) currentFas.peekLast()).getDoubleBonds().getNumDoubleBonds());
    }

    private void checkCycleDbPositions(TreeNode node) {
        if (((Cycle) currentFas.peekLast()).getDoubleBonds().getNumDoubleBonds() != (int) ((Dictionary) tmp.get(faI())).get("cycle_db")) {
            throw new LipidException("Double bond number in cycle does not correspond to number of double bond positions.");
        }
    }

    private void setCycleDbPosition(TreeNode node) {
        int pos = Integer.valueOf(node.getText());
        ((Cycle) currentFas.peekLast()).getDoubleBonds().getDoubleBondPositions().put(pos, "");
        ((Dictionary) tmp.get(faI())).put("last_db_pos", pos);
    }

    private void setCycleDbPositionCistrans(TreeNode node) {
        int pos = (int) ((Dictionary) tmp.get(faI())).get("last_db_pos");
        ((Cycle) currentFas.peekLast()).getDoubleBonds().getDoubleBondPositions().put(pos, node.getText());
    }

    private void addCycleElement(TreeNode node) {
        String element = node.getText();

        if (!Elements.ELEMENT_POSITIONS.containsKey(element)) {
            throw new LipidParsingException("Element '" + element + "' unknown");
        }

        ((GenericList) ((Dictionary) tmp.get(faI())).get("cycle_elements")).add(Elements.ELEMENT_POSITIONS.get(element));
    }

    private void setAcylLinkage(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", "acyl");
        currentFas.add(new AcylAlkylGroup((FattyAcid) null, knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
        ((Dictionary) tmp.get(faI())).put("linkage_pos", -1);
    }

    private void addAcylLinkage(TreeNode node) {
        boolean linkage_type = (int) ((Dictionary) tmp.get(faI())).get("linkage_type") == 1;
        int linkage_pos = (int) ((Dictionary) tmp.get(faI())).get("linkage_pos");

        tmp.remove(faI());
        AcylAlkylGroup acyl = (AcylAlkylGroup) currentFas.pollLast();

        acyl.setPosition(linkage_pos);
        acyl.setNitrogenBond(linkage_type);
        if (linkage_pos == -1) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }

        if (!currentFas.peekLast().getFunctionalGroupsInternal().containsKey("acyl")) {
            currentFas.peekLast().getFunctionalGroupsInternal().put("acyl", new ArrayList<FunctionalGroup>());
        }
        currentFas.peekLast().getFunctionalGroupsInternal().get("acyl").add(acyl);
    }

    private void setAlkylLinkage(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", "alkyl");
        currentFas.add(new AcylAlkylGroup(null, -1, 1, true, knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
        ((Dictionary) tmp.get(faI())).put("linkage_pos", -1);
    }

    private void addAlkylLinkage(TreeNode node) {
        int linkage_pos = (int) ((Dictionary) tmp.get(faI())).get("linkage_pos");
        tmp.remove(faI());
        AcylAlkylGroup alkyl = (AcylAlkylGroup) currentFas.pollLast();

        alkyl.setPosition(linkage_pos);
        if (linkage_pos == -1) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }

        if (!currentFas.peekLast().getFunctionalGroupsInternal().containsKey("alkyl")) {
            currentFas.peekLast().getFunctionalGroupsInternal().put("alkyl", new ArrayList<FunctionalGroup>());
        }
        currentFas.peekLast().getFunctionalGroupsInternal().get("alkyl").add(alkyl);
    }

    private void setFattyLinkageNumber(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("linkage_pos", Integer.valueOf(node.getText()));
    }

    private void setLinkageType(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("linkage_type", node.getText().equals("N") ? 1 : 0);
    }

    private void setHydrocarbonChain(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", "cc");
        currentFas.add(new CarbonChain((FattyAcid) null, knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
        ((Dictionary) tmp.get(faI())).put("linkage_pos", -1);
    }

    private void addHydrocarbonChain(TreeNode node) {
        int linkage_pos = (int) ((Dictionary) tmp.get(faI())).get("linkage_pos");
        tmp.remove(faI());
        CarbonChain cc = (CarbonChain) currentFas.pollLast();
        cc.setPosition(linkage_pos);
        if (linkage_pos == -1) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }

        if (!currentFas.peekLast().getFunctionalGroupsInternal().containsKey("cc")) {
            currentFas.peekLast().getFunctionalGroupsInternal().put("cc", new ArrayList<FunctionalGroup>());
        }
        currentFas.peekLast().getFunctionalGroupsInternal().get("cc").add(cc);
    }

    private void setRingStereo(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_ring_stereo", node.getText());
    }

    private void setHgAcyl(TreeNode node) {
        String fa_i = faI();
        tmp.put(fa_i, new Dictionary());
        ((Dictionary) tmp.get(fa_i)).put("fg_name", "decorator_acyl");
        currentFas.add(new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
    }

    private void addHgAcyl(TreeNode node) {
        tmp.remove(faI());
        headgroupDecorators.add((HeadgroupDecorator) currentFas.pollLast());
        tmp.remove(faI());
    }

    private void setHgAlkyl(TreeNode node) {
        tmp.put(faI(), new Dictionary());
        ((Dictionary) tmp.get(faI())).put("fg_name", "decorator_alkyl");
        currentFas.add(new HeadgroupDecorator("decorator_alkyl", -1, 1, null, true, knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
    }

    private void addHgAlkyl(TreeNode node) {
        tmp.remove(faI());
        headgroupDecorators.add((HeadgroupDecorator) currentFas.pollLast());
        tmp.remove(faI());
    }

    private void addPlSpeciesData(TreeNode node) {
        setLipidLevel(LipidLevel.SPECIES);
        HeadgroupDecorator hgd = new HeadgroupDecorator("", knownFunctionalGroups);
        hgd.getElements().put(Element.O, hgd.getElements().get(Element.O) + 1);
        hgd.getElements().put(Element.H, hgd.getElements().get(Element.H) - 1);
        headgroupDecorators.add(hgd);
    }

    private void suffixDecoratorMolecular(TreeNode node) {
        headgroupDecorators.add(new HeadgroupDecorator(node.getText(), -1, 1, null, true, LipidLevel.MOLECULAR_SPECIES, knownFunctionalGroups));
    }

    private void suffixDecoratorSpecies(TreeNode node) {
        headgroupDecorators.add(new HeadgroupDecorator(node.getText(), -1, 1, null, true, LipidLevel.SPECIES, knownFunctionalGroups));
    }

    private void setAcer(TreeNode node) {
        headGroup = "ACer";
        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups);
        hgd.getFunctionalGroupsInternal().put("decorator_acyl", new ArrayList<>());
        hgd.getFunctionalGroupsInternal().get("decorator_acyl").add(faList.get(faList.size() - 1));
        faList.remove(faList.size() - 1);
        headgroupDecorators.add(hgd);
    }

    private void setAcerSpecies(TreeNode node) {
        headGroup = "ACer";
        setLipidLevel(LipidLevel.SPECIES);
        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups);
        hgd.getFunctionalGroupsInternal().put("decorator_acyl", new ArrayList<>());
        hgd.getFunctionalGroupsInternal().get("decorator_acyl").add(new FattyAcid("FA", 2, knownFunctionalGroups));
        headgroupDecorators.add(hgd);
        acerSpecies = true;
    }
}
