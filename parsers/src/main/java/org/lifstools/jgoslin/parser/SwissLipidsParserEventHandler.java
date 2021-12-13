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
import org.lifstools.jgoslin.domain.FattyAcid;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.Headgroup;
import org.lifstools.jgoslin.domain.HeadgroupDecorator;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.LipidFaBondType;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidParsingException;
import java.util.ArrayList;
import java.util.Map;
import static java.util.Map.entry;

/**
 * Event handler implementation for the {@link SwissLipidsParser}.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class SwissLipidsParserEventHandler extends LipidBaseParserEventHandler {

    public int dbPosition;
    public String dbCistrans;
    public int suffixNumber;

    /**
     * Create a new {@code SwissLipidsParserEventHandler}.
     *
     * @param knownFunctionalGroups the known functional groups
     */
    public SwissLipidsParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        super(knownFunctionalGroups);
        try {
            registeredEvents = Map.ofEntries(
                    entry("lipid_pre_event", this::resetParser),
                    entry("lipid_post_event", this::buildLipid),
                    // set adduct events
                    entry("adduct_info_pre_event", this::newAdduct),
                    entry("adduct_pre_event", this::addAdduct),
                    entry("charge_pre_event", this::addCharge),
                    entry("charge_sign_pre_event", this::addChargeSign),
                    entry("fa_hg_pre_event", this::setHeadGroupName),
                    entry("gl_hg_pre_event", this::setHeadGroupName),
                    entry("gl_molecular_hg_pre_event", this::setHeadGroupName),
                    entry("mediator_pre_event", this::mediatorEvent),
                    entry("gl_mono_hg_pre_event", this::setHeadGroupName),
                    entry("pl_hg_pre_event", this::setHeadGroupName),
                    entry("pl_three_hg_pre_event", this::setHeadGroupName),
                    entry("pl_four_hg_pre_event", this::setHeadGroupName),
                    entry("sl_hg_pre_event", this::setHeadGroupName),
                    entry("st_species_hg_pre_event", this::setHeadGroupName),
                    entry("st_sub1_hg_pre_event", this::setHeadGroupName),
                    entry("st_sub2_hg_pre_event", this::setHeadGroupNameSe),
                    entry("fa_species_pre_event", this::setSpeciesLevel),
                    entry("gl_molecular_pre_event", this::setMolecularLevel),
                    entry("unsorted_fa_separator_pre_event", this::setMolecularLevel),
                    entry("fa2_unsorted_pre_event", this::setMolecularLevel),
                    entry("fa3_unsorted_pre_event", this::setMolecularLevel),
                    entry("fa4_unsorted_pre_event", this::setMolecularLevel),
                    entry("db_single_position_pre_event", this::setIsomericLevel),
                    entry("db_single_position_post_event", this::addDbPosition),
                    entry("db_position_number_pre_event", this::addDbPositionNumber),
                    entry("cistrans_pre_event", this::addCistrans),
                    entry("lcb_pre_event", this::newLcb),
                    entry("lcb_post_event", this::cleanLcb),
                    entry("fa_pre_event", this::newFa),
                    entry("fa_post_event", this::appendFa),
                    entry("ether_pre_event", this::addEther),
                    entry("hydroxyl_pre_event", this::addHydroxyl),
                    entry("db_count_pre_event", this::addDoubleBonds),
                    entry("carbon_pre_event", this::addCarbon),
                    entry("sl_lcb_species_pre_event", this::setSpeciesLevel),
                    entry("st_species_fa_post_event", this::setSpeciesFa),
                    entry("fa_lcb_suffix_type_pre_event", this::addFaLcbSuffixType),
                    entry("fa_lcb_suffix_number_pre_event", this::addSuffixNumber),
                    entry("pl_three_post_event", this::setNape)
            );
        } catch (Exception e) {
            throw new LipidParsingException("Cannot initialize LipidMapsParserEventHandler.");
        }
    }

    @Override
    protected void resetParser(TreeNode node) {
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        adduct = null;
        headGroup = "";
        lcb = null;
        faList.clear();
        currentFa = null;
        useHeadGroup = false;
        dbPosition = 0;
        dbCistrans = "";
        headgroupDecorators.clear();
        suffixNumber = -1;
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

    private void setNape(TreeNode node) {
        headGroup = "PE-N";
        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups);
        headgroupDecorators.add(hgd);
        hgd.getFunctionalGroupsInternal().put("decorator_acyl", new ArrayList<>());
        hgd.getFunctionalGroupsInternal().get("decorator_acyl").add(faList.get(faList.size() - 1));
        faList.remove(faList.size() - 1);
    }

    private void addDbPositionNumber(TreeNode node) {
        dbPosition = node.getInt();
    }

    private void addCistrans(TreeNode node) {
        dbCistrans = node.getText();
    }

    private void setHeadGroupName(TreeNode node) {
        headGroup = node.getText();
    }

    private void setHeadGroupNameSe(TreeNode node) {
        headGroup = node.getText().replace("(", " ");
    }

    private void setSpeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.SPECIES);
    }

    private void setMolecularLevel(TreeNode node) {
        setLipidLevel(LipidLevel.MOLECULAR_SPECIES);
    }

    private void mediatorEvent(TreeNode node) {
        useHeadGroup = true;
        headGroup = node.getText();
    }

    private void newFa(TreeNode node) {
        currentFa = new FattyAcid("FA", knownFunctionalGroups);
    }

    private void newLcb(TreeNode node) {
        lcb = new FattyAcid("LCB", knownFunctionalGroups);
        lcb.setType(LipidFaBondType.LCB_REGULAR);
        currentFa = lcb;
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
    }

    private void cleanLcb(TreeNode node) {
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }
        currentFa = null;
    }

    private void appendFa(TreeNode node) {
        if (currentFa.getDoubleBonds().getNumDoubleBonds() < 0) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }

        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }

        faList.add(currentFa);
        currentFa = null;
    }

    private void buildLipid(TreeNode node) {
        if (lcb != null) {
            faList.add(0, lcb);
        }

        Headgroup headgroup = prepareHeadgroupAndChecks();
        LipidAdduct lipid = new LipidAdduct(assembleLipid(headgroup), adduct);
        content = lipid;
    }

    private void addEther(TreeNode node) {
        String ether = node.getText();
        if (ether.equals("O-")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMANYL);
        } else if (ether.equals("P-")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMENYL);
        }
    }

    private void addHydroxyl(TreeNode node) {
        String old_hydroxyl = node.getText();
        int num_h = 0;
        if (old_hydroxyl.equals("m")) {
            num_h = 1;
        } else if (old_hydroxyl.equals("d")) {
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

    private void addOneHydroxyl(TreeNode node) {
        if (!currentFa.getFunctionalGroupsInternal().containsKey("OH") && currentFa.getFunctionalGroupsInternal().get("OH").get(0).getPosition() == -1) {
            currentFa.getFunctionalGroupsInternal().get("OH").get(0).setCount(currentFa.getFunctionalGroupsInternal().get("OH").get(0).getCount() + 1);
        } else {
            FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
            if (!currentFa.getFunctionalGroupsInternal().containsKey("OH")) {
                currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<>());
            }
            currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group);
        }
    }

    private void addSuffixNumber(TreeNode node) {
        suffixNumber = node.getInt();
    }

    private void addFaLcbSuffixType(TreeNode node) {
        String suffix_type = node.getText();
        if (suffix_type.equals("me")) {
            suffix_type = "Me";
            currentFa.setNumCarbon(currentFa.getNumCarbon() - 1);
        }

        FunctionalGroup functional_group = knownFunctionalGroups.get(suffix_type);
        functional_group.setPosition(suffixNumber);
        if (functional_group.getPosition() == -1) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }
        if (!currentFa.getFunctionalGroupsInternal().containsKey(suffix_type)) {
            currentFa.getFunctionalGroupsInternal().put(suffix_type, new ArrayList<>());
        }
        currentFa.getFunctionalGroupsInternal().get(suffix_type).add(functional_group);

        suffixNumber = -1;
    }

    private void addDoubleBonds(TreeNode node) {
        currentFa.getDoubleBonds().setNumDoubleBonds(currentFa.getDoubleBonds().getNumDoubleBonds() + node.getInt());
    }

    private void addCarbon(TreeNode node) {
        currentFa.setNumCarbon(node.getInt());
    }

    private void setSpeciesFa(TreeNode node) {
        headGroup += " 27:1";
        faList.get(faList.size() - 1).setNumCarbon(faList.get(faList.size() - 1).getNumCarbon() - 27);
        faList.get(faList.size() - 1).getDoubleBonds().setNumDoubleBonds(faList.get(faList.size() - 1).getDoubleBonds().getNumDoubleBonds() - 1);
    }

    private void newAdduct(TreeNode node) {
        adduct = new Adduct("", "");
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
    }
}
