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
import java.util.Map;
import static java.util.Map.entry;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class HmdbParserEventHandler extends LipidBaseParserEventHandler {

    private int dbPositions;
    private String dbCistrans;
    private Dictionary furan = null;

    public HmdbParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
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
                entry("st_sub2_hg_pre_event", this::setHeadGroupName),
                entry("ganglioside_names_pre_event", this::setHeadGroupName),
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
                entry("fa_lcb_suffix_type_pre_event", this::addOneHydroxyl),
                entry("interlink_fa_pre_event", this::interlinkFa),
                entry("lipid_suffix_pre_event", this::lipidSuffix),
                entry("methyl_pre_event", this::addMethyl),
                entry("furan_fa_pre_event", this::furanFa),
                entry("furan_fa_post_event", this::furanFaPost),
                entry("furan_fa_mono_pre_event", this::furanFaMono),
                entry("furan_fa_di_pre_event", this::furanFaDi),
                entry("furan_first_number_pre_event", this::furanFaFirstNumber),
                entry("furan_second_number_pre_event", this::furanFaSecondNumber)
            );
        } catch (Exception e) {
            throw new LipidParsingException("Cannot initialize LipidMapsParserEventHandler.");
        }
    }

    @Override
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
            currentFa.getDoubleBonds().getDoubleBondPositions().put(dbPositions, dbCistrans);
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
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }
        currentFa = null;
    }

    public void appendFa(TreeNode node) {
        if (currentFa.getDoubleBonds().getNumDoubleBonds() < 0) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }

        if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level | LipidLevel.SN_POSITION.level)) {
            currentFa.setPosition(faList.size() + 1);
        }

        faList.add(currentFa);
        currentFa = null;
    }

    public void buildLipid(TreeNode node) {
        if (lcb != null) {
            for (FattyAcid fa : faList) {
                fa.setPosition(fa.getPosition() + 1);
            }
            faList.add(0, lcb);
        }

        Headgroup headgroup = prepareHeadgroupAndChecks();

        LipidAdduct lipid = new LipidAdduct(assembleLipid(headgroup), adduct);
        content = lipid;
    }

    public void addEther(TreeNode node) {
        String ether = node.getText();
        if (ether.equals("O-") || ether.equals("o-")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMANYL);
        } else if (ether.equals("P-")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMENYL);
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
        functional_group.setCount(num_h);
        if (!currentFa.getFunctionalGroups().containsKey("OH")) {
            currentFa.getFunctionalGroups().put("OH", new ArrayList<>());
        }
        currentFa.getFunctionalGroups().get("OH").add(functional_group);
    }

    public void addMethyl(TreeNode node) {
        FunctionalGroup functional_group = knownFunctionalGroups.get("Me");
        functional_group.setPosition(currentFa.getNumCarbon() - (node.getText().equals("i-") ? 1 : 2));
        currentFa.setNumCarbon(currentFa.getNumCarbon() - 1);

        if (!currentFa.getFunctionalGroups().containsKey("Me")) {
            currentFa.getFunctionalGroups().put("Me", new ArrayList<>());
        }
        currentFa.getFunctionalGroups().get("Me").add(functional_group);
    }

    public void addOneHydroxyl(TreeNode node) {
        if (currentFa.getFunctionalGroups().containsKey("OH") && currentFa.getFunctionalGroups().get("OH").get(0).getPosition() == -1) {
            currentFa.getFunctionalGroups().get("OH").get(0).setCount(currentFa.getFunctionalGroups().get("OH").get(0).getCount() + 1);
        } else {
            FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
            if (!currentFa.getFunctionalGroups().containsKey("OH")) {
                currentFa.getFunctionalGroups().put("OH", new ArrayList<>());
            }
            currentFa.getFunctionalGroups().get("OH").add(functional_group);
        }
    }

    public void addDoubleBonds(TreeNode node) {
        currentFa.getDoubleBonds().setNumDoubleBonds(node.getInt());
    }

    public void addCarbon(TreeNode node) {
        currentFa.setNumCarbon(currentFa.getNumCarbon() + node.getInt());
    }

    public void furanFa(TreeNode node) {
        furan = new Dictionary();
    }

    public void furanFaPost(TreeNode node) {
        int l = 4 + (int) furan.get("len_first") + (int) furan.get("len_second");
        currentFa.setNumCarbon(l);

        int start = 1 + (int) furan.get("len_first");
        int end = 3 + start;
        DoubleBonds cyclo_db = new DoubleBonds(2);
        cyclo_db.getDoubleBondPositions().put(start, "E");
        cyclo_db.getDoubleBondPositions().put(2 + start, "E");

        HashMap<String, ArrayList<FunctionalGroup>> cyclo_fg = new HashMap<>();
        cyclo_fg.put("Me", new ArrayList<>());

        if (((String) furan.get("type")).equals("m")) {
            FunctionalGroup fg = knownFunctionalGroups.get("Me");
            fg.setPosition(1 + start);
            cyclo_fg.get("Me").add(fg);
        } else if (((String) furan.get("type")).equals("d")) {
            FunctionalGroup fg = knownFunctionalGroups.get("Me");
            fg.setPosition(1 + start);
            cyclo_fg.get("Me").add(fg);
            fg = knownFunctionalGroups.get("Me");
            fg.setPosition(2 + start);
            cyclo_fg.get("Me").add(fg);
        }

        ArrayList<Element> bridge_chain = new ArrayList<>();
        bridge_chain.add(Element.O);
        Cycle cycle = new Cycle(end - start + 1 + bridge_chain.size(), start, end, cyclo_db, cyclo_fg, bridge_chain, knownFunctionalGroups);
        currentFa.getFunctionalGroups().put("cy", new ArrayList<>());
        currentFa.getFunctionalGroups().get("cy").add(cycle);
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
        adduct.setAdductString(node.getText());
    }

    public void addCharge(TreeNode node) {
        adduct.setCharge(Integer.valueOf(node.getText()));
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
