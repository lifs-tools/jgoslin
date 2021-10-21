/*
 * Copyright 2021 dominik.
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
import org.lifstools.jgoslin.domain.Cycle;
import org.lifstools.jgoslin.domain.Dictionary;
import org.lifstools.jgoslin.domain.DoubleBonds;
import org.lifstools.jgoslin.domain.Element;
import org.lifstools.jgoslin.domain.FattyAcid;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.Headgroup;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.LipidFaBondType;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.UnsupportedLipidException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author dominik
 */
public class HmdbParserEventHandler extends LipidBaseParserEventHandler {

    private int dbPositions;
    private String dbCistrans;
    private Dictionary furan = null;

    public HmdbParserEventHandler() {
        this(new KnownFunctionalGroups());
    }

    public HmdbParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        this.knownFunctionalGroups = knownFunctionalGroups;
        try {
            registeredEvents.put("lipid_pre_event", this::resetParser);
            registeredEvents.put("lipid_post_event", this::buildLipid);
            // set adduct events
            registeredEvents.put("adduct_info_pre_event", this::newAdduct);
            registeredEvents.put("adduct_pre_event", this::addAdduct);
            registeredEvents.put("charge_pre_event", this::addCharge);
            registeredEvents.put("charge_sign_pre_event", this::addChargeSign);

            registeredEvents.put("fa_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("gl_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("gl_molecular_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("mediator_pre_event", this::mediatorEvent);
            registeredEvents.put("gl_mono_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("pl_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("pl_three_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("pl_four_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("sl_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("st_species_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("st_sub1_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("st_sub2_hg_pre_event", this::setHeadGroupName);
            registeredEvents.put("ganglioside_names_pre_event", this::setHeadGroupName);
            registeredEvents.put("fa_species_pre_event", this::setSpeciesLevel);
            registeredEvents.put("gl_molecular_pre_event", this::setMolecularLevel);
            registeredEvents.put("unsorted_fa_separator_pre_event", this::setMolecularLevel);
            registeredEvents.put("fa2_unsorted_pre_event", this::setMolecularLevel);
            registeredEvents.put("fa3_unsorted_pre_event", this::setMolecularLevel);
            registeredEvents.put("fa4_unsorted_pre_event", this::setMolecularLevel);
            registeredEvents.put("db_single_position_pre_event", this::setIsomericLevel);
            registeredEvents.put("db_single_position_post_event", this::addDbPosition);
            registeredEvents.put("db_position_number_pre_event", this::addDbPositionNumber);
            registeredEvents.put("cistrans_pre_event", this::addCistrans);
            registeredEvents.put("lcb_pre_event", this::newLcb);
            registeredEvents.put("lcb_post_event", this::cleanLcb);
            registeredEvents.put("fa_pre_event", this::newFa);
            registeredEvents.put("fa_post_event", this::appendFa);
            registeredEvents.put("ether_pre_event", this::addEther);
            registeredEvents.put("hydroxyl_pre_event", this::addHydroxyl);
            registeredEvents.put("db_count_pre_event", this::addDoubleBonds);
            registeredEvents.put("carbon_pre_event", this::addCarbon);
            registeredEvents.put("fa_lcb_suffix_type_pre_event", this::addOneHydroxyl);
            registeredEvents.put("interlink_fa_pre_event", this::interlinkFa);
            registeredEvents.put("lipid_suffix_pre_event", this::lipidSuffix);
            registeredEvents.put("methyl_pre_event", this::addMethyl);
            registeredEvents.put("furan_fa_pre_event", this::furanFa);
            registeredEvents.put("furan_fa_post_event", this::furanFaPost);
            registeredEvents.put("furan_fa_mono_pre_event", this::furanFaMono);
            registeredEvents.put("furan_fa_di_pre_event", this::furanFaDi);
            registeredEvents.put("furan_first_number_pre_event", this::furanFaFirstNumber);
            registeredEvents.put("furan_second_number_pre_event", this::furanFaSecondNumber);
        } catch (Exception e) {
            throw new LipidParsingException("Cannot initialize LipidMapsParserEventHandler.");
        }
    }

    public void resetParser(TreeNode node) {
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        headGroup = "";
        lcb = null;
        adduct = null;
        faList.clear();
        currentFa = null;
        useHeadGroup = false;
        dbPositions = 0;
        dbCistrans = "";
        furan = new Dictionary();
    }

    public void setIsomericLevel(TreeNode node) {
        dbPositions = 0;
        dbCistrans = "";
    }

    public void addDbPosition(TreeNode node) {
        if (currentFa != null) {
            currentFa.doubleBonds.doubleBondPositions.put(dbPositions, dbCistrans);
            if (!dbCistrans.equals("E") && !dbCistrans.equals("Z")) {
                setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
            }
        }
    }

    public void addDbPositionNumber(TreeNode node) {
        dbPositions = node.getInt();
    }

    public void addCistrans(TreeNode node) {
        dbCistrans = node.getText();
    }

    public void setHeadGroupName(TreeNode node) {
        headGroup = node.getText();
    }

    public void setSpeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.SPECIES);
    }

    public void setMolecularLevel(TreeNode node) {
        setLipidLevel(LipidLevel.MOLECULAR_SPECIES);
    }

    public void mediatorEvent(TreeNode node) {
        useHeadGroup = true;
        headGroup = node.getText();
    }

    public void newFa(TreeNode node) {
        currentFa = new FattyAcid("FA" + (faList.size() + 1), knownFunctionalGroups);
    }

    public void newLcb(TreeNode node) {
        lcb = new FattyAcid("LCB", knownFunctionalGroups);
        lcb.setType(LipidFaBondType.LCB_REGULAR);
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        currentFa = lcb;
    }

    public void cleanLcb(TreeNode node) {
        if (currentFa.doubleBonds.doubleBondPositions.isEmpty() && currentFa.doubleBonds.getNum() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }
        currentFa = null;
    }

    public void appendFa(TreeNode node) {
        if (currentFa.doubleBonds.getNum() < 0) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }
        if (currentFa.doubleBonds.doubleBondPositions.isEmpty() && currentFa.doubleBonds.getNum() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }

        if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level | LipidLevel.SN_POSITION.level)) {
            currentFa.position = faList.size() + 1;
        }

        faList.add(currentFa);
        currentFa = null;
    }

    public void buildLipid(TreeNode node) {
        if (lcb != null) {
            for (FattyAcid fa : faList) {
                fa.position += 1;
            }
            faList.add(0, lcb);
        }

        Headgroup headgroup = prepareHeadgroupAndChecks();

        LipidAdduct lipid = new LipidAdduct();
        lipid.lipid = assembleLipid(headgroup);
        lipid.adduct = adduct;
        content = lipid;
    }

    public void addEther(TreeNode node) {
        String ether = node.getText();
        if (ether.equals("O-") || ether.equals("o-")) {
            currentFa.lipidFaBondType = LipidFaBondType.ETHER_PLASMANYL;
        } else if (ether.equals("P-")) {
            currentFa.lipidFaBondType = LipidFaBondType.ETHER_PLASMENYL;
        } else {
            throw new UnsupportedLipidException("Fatty acyl chain of type '" + ether + "' is currently not supported");
        }
    }

    public void addHydroxyl(TreeNode node) {
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
        functional_group.count = num_h;
        if (!currentFa.functionalGroups.containsKey("OH")) {
            currentFa.functionalGroups.put("OH", new ArrayList<>());
        }
        currentFa.functionalGroups.get("OH").add(functional_group);
    }

    public void addMethyl(TreeNode node) {
        FunctionalGroup functional_group = knownFunctionalGroups.get("Me");
        functional_group.position = currentFa.numCarbon - (node.getText().equals("i-") ? 1 : 2);
        currentFa.numCarbon -= 1;

        if (!currentFa.functionalGroups.containsKey("Me")) {
            currentFa.functionalGroups.put("Me", new ArrayList<>());
        }
        currentFa.functionalGroups.get("Me").add(functional_group);
    }

    public void addOneHydroxyl(TreeNode node) {
        if (currentFa.functionalGroups.containsKey("OH") && currentFa.functionalGroups.get("OH").get(0).position == -1) {
            currentFa.functionalGroups.get("OH").get(0).count += 1;
        } else {
            FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
            if (!currentFa.functionalGroups.containsKey("OH")) {
                currentFa.functionalGroups.put("OH", new ArrayList<>());
            }
            currentFa.functionalGroups.get("OH").add(functional_group);
        }
    }

    public void addDoubleBonds(TreeNode node) {
        currentFa.doubleBonds.numDoubleBonds = node.getInt();
    }

    public void addCarbon(TreeNode node) {
        currentFa.numCarbon += node.getInt();
    }

    public void furanFa(TreeNode node) {
        furan = new Dictionary();
    }

    public void furanFaPost(TreeNode node) {
        int l = 4 + (int) furan.get("len_first") + (int) furan.get("len_second");
        currentFa.numCarbon = l;

        int start = 1 + (int) furan.get("len_first");
        int end = 3 + start;
        DoubleBonds cyclo_db = new DoubleBonds(2);
        cyclo_db.doubleBondPositions.put(start, "E");
        cyclo_db.doubleBondPositions.put(2 + start, "E");

        HashMap<String, ArrayList<FunctionalGroup>> cyclo_fg = new HashMap<>();
        cyclo_fg.put("Me", new ArrayList<>());

        if (((String) furan.get("type")).equals("m")) {
            FunctionalGroup fg = knownFunctionalGroups.get("Me");
            fg.position = 1 + start;
            cyclo_fg.get("Me").add(fg);
        } else if (((String) furan.get("type")).equals("d")) {
            FunctionalGroup fg = knownFunctionalGroups.get("Me");
            fg.position = 1 + start;
            cyclo_fg.get("Me").add(fg);
            fg = knownFunctionalGroups.get("Me");
            fg.position = 2 + start;
            cyclo_fg.get("Me").add(fg);
        }

        ArrayList<Element> bridge_chain = new ArrayList<>();
        bridge_chain.add(Element.O);
        Cycle cycle = new Cycle(end - start + 1 + bridge_chain.size(), start, end, cyclo_db, cyclo_fg, bridge_chain, knownFunctionalGroups);
        currentFa.functionalGroups.put("cy", new ArrayList<>());
        currentFa.functionalGroups.get("cy").add(cycle);
    }

    public void furanFaMono(TreeNode node) {
        furan.put("type", "m");
    }

    public void furanFaDi(TreeNode node) {
        furan.put("type", "d");
    }

    public void furanFaFirstNumber(TreeNode node) {
        furan.put("len_first", node.getInt());
    }

    public void furanFaSecondNumber(TreeNode node) {
        furan.put("len_second", node.getInt());
    }

    public void interlinkFa(TreeNode node) {
        throw new UnsupportedLipidException("Interconnected fatty acyl chains are currently not supported");
    }

    public void lipidSuffix(TreeNode node) {
        //throw new UnsupportedLipidException("Lipids with suffix '" + node.getText() + "' are currently not supported");
    }

    public void newAdduct(TreeNode node) {
        adduct = new Adduct("", "");
    }

    public void addAdduct(TreeNode node) {
        adduct.adductString = node.getText();
    }

    public void addCharge(TreeNode node) {
        adduct.charge = Integer.valueOf(node.getText());
    }

    public void addChargeSign(TreeNode node) {
        String sign = node.getText();
        if (sign.equals("+")) {
            adduct.setChargeSign(1);
        } else if (sign.equals("-")) {
            adduct.setChargeSign(-1);
        }
    }
}
