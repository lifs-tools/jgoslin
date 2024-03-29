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

import org.lifstools.jgoslin.domain.LipidFaBondType;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.Element;
import org.lifstools.jgoslin.domain.Headgroup;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.Adduct;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.Cycle;
import org.lifstools.jgoslin.domain.FattyAcid;
import org.lifstools.jgoslin.domain.HeadgroupDecorator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Optional;
import java.util.Set;
import org.lifstools.jgoslin.domain.ConstraintViolationException;
import org.lifstools.jgoslin.domain.DoubleBonds;

/**
 * Event handler implementation for the {@link LipidMapsParser}.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class LipidMapsParserEventHandler extends LipidBaseParserEventHandler {

    private boolean omitFa;
    private int dbNumbers;
    private int dbPosition;
    private String dbCistrans;
    private String modText;
    private int modPos;
    private int modNum;
    private boolean addOmegaLinoleoyloxyCer;
    private int heavyNumber;
    private Optional<Element> heavyElement;
    private boolean sphingaPure;
    private int lcbCarbonPreSet;
    private int lcbDbPreSet;
    private ArrayList<FunctionalGroup> lcbHydroPreSet = new ArrayList<>();
    private String sphingaPrefix;
    private String sphingaSuffix;

    private static final Set<String> HEAD_GROUP_EXCEPTIONS = Set.of("PA", "PC", "PE", "PG", "PI", "PS");
    private static final Map<String, Integer> ACER_HEADS = Map.ofEntries(
            entry("1-O-myristoyl", 14),
            entry("1-O-palmitoyl", 16),
            entry("1-O-stearoyl", 18),
            entry("1-O-eicosanoyl", 20),
            entry("1-O-behenoyl", 22),
            entry("1-O-lignoceroyl", 24),
            entry("1-O-cerotoyl", 26),
            entry("1-O-pentacosanoyl", 25),
            entry("1-O-carboceroyl", 28),
            entry("1-O-tricosanoyl", 30),
            entry("1-O-lignoceroyl-omega-linoleoyloxy", 24),
            entry("1-O-stearoyl-omega-linoleoyloxy", 18)
    );

    /**
     * Create a new {@code LipidMapsParserEventHandler}.
     *
     * @param knownFunctionalGroups the known functional groups
     */
    public LipidMapsParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
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
                    entry("mediator_pre_event", this::mediatorEvent),
                    entry("sgl_species_pre_event", this::setSpeciesLevel),
                    entry("species_fa_pre_event", this::setSpeciesLevel),
                    entry("tgl_species_pre_event", this::setSpeciesLevel),
                    entry("dpl_species_pre_event", this::setSpeciesLevel),
                    entry("cl_species_pre_event", this::setSpeciesLevel),
                    entry("dsl_species_pre_event", this::setSpeciesLevel),
                    entry("fa2_unsorted_pre_event", this::setMolecularSubspeciesLevel),
                    entry("fa3_unsorted_pre_event", this::setMolecularSubspeciesLevel),
                    entry("fa4_unsorted_pre_event", this::setMolecularSubspeciesLevel),
                    entry("hg_dg_pre_event", this::setMolecularSubspeciesLevel),
                    entry("fa_lpl_molecular_pre_event", this::setMolecularSubspeciesLevel),
                    entry("hg_lbpa_pre_event", this::setMolecularSubspeciesLevel),
                    entry("fa_no_hg_pre_event", this::pureFa),
                    entry("hg_sgl_pre_event", this::setHeadGroupName),
                    entry("hg_gl_pre_event", this::setHeadGroupName),
                    entry("hg_cl_pre_event", this::setHeadGroupName),
                    entry("hg_dpl_pre_event", this::setHeadGroupName),
                    entry("hg_lpl_pre_event", this::setHeadGroupName),
                    entry("hg_threepl_pre_event", this::setHeadGroupName),
                    entry("hg_fourpl_pre_event", this::setHeadGroupName),
                    entry("hg_dsl_pre_event", this::setHeadGroupName),
                    entry("hg_cpa_pre_event", this::setHeadGroupName),
                    entry("ch_pre_event", this::setHeadGroupName),
                    entry("hg_che_pre_event", this::setHeadGroupName),
                    entry("mediator_const_pre_event", this::setHeadGroupName),
                    entry("pk_hg_pre_event", this::setHeadGroupName),
                    entry("hg_fa_pre_event", this::setHeadGroupName),
                    entry("hg_lsl_pre_event", this::setHeadGroupName),
                    entry("special_cer_pre_event", this::setHeadGroupName),
                    entry("special_cer_hg_pre_event", this::setHeadGroupName),
                    entry("omega_linoleoyloxy_Cer_pre_event", this::setOmegaHeadGroupName),
                    entry("lcb_pre_event", this::newLcb),
                    entry("lcb_post_event", this::cleanLcb),
                    entry("fa_pre_event", this::newFa),
                    entry("fa_post_event", this::appendFa),
                    entry("glyco_struct_pre_event", this::addGlyco),
                    entry("db_single_position_pre_event", this::setIsomericLevel),
                    entry("db_single_position_post_event", this::addDbPosition),
                    entry("db_position_number_pre_event", this::addDbPositionNumber),
                    entry("cistrans_pre_event", this::addCistrans),
                    entry("ether_prefix_pre_event", this::addEther),
                    entry("ether_suffix_pre_event", this::addEther),
                    entry("lcb_pure_fa_pre_event", this::addDiHydroxyl),
                    entry("hydroxyl_pre_event", this::addHydroxyl),
                    entry("hydroxyl_lcb_pre_event", this::addHydroxylLcb),
                    entry("db_count_pre_event", this::addDoubleBonds),
                    entry("carbon_pre_event", this::addCarbon),
                    entry("structural_mod_pre_event", this::setStructuralSubspeciesLevel),
                    entry("single_mod_pre_event", this::setMod),
                    entry("mod_text_pre_event", this::setModText),
                    entry("mod_pos_pre_event", this::setModPos),
                    entry("mod_num_pre_event", this::setModNum),
                    entry("single_mod_post_event", this::addFunctionalGroup),
                    entry("special_cer_prefix_pre_event", this::addAcer),
                    entry("additional_modifier_pre_event", this::addAdditionalModifier),
                    entry("isotope_pair_pre_event", this::newAdduct),
                    entry("isotope_element_pre_event", this::setHeavyElement),
                    entry("isotope_number_pre_event", this::setHeavyNumber),
                    entry("sphinga_pre_event", this::newSphinga),
                    entry("sphinga_phospho_pre_event", this::addPhospho),
                    entry("sphinga_suffix_pre_event", this::sphingaDbSet),
                    entry("sphinga_lcb_len_pre_event", this::addCarbonPreLen),
                    entry("sphinga_prefix_pre_event", this::setHydroPreNum),
                    entry("sphinga_hg_pure_pre_event", this::newSphingaPure),
                    entry("sphinga_hg_pure_post_event", this::cleanLcb)
            );
        } catch (Exception e) {
            throw new LipidParsingException("Cannot initialize LipidMapsParserEventHandler.");
        }
        
    }

    @Override
    protected void resetParser(TreeNode node) {
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        headGroup = "";
        lcb = null;
        adduct = null;
        faList.clear();
        currentFa = null;
        useHeadGroup = false;
        omitFa = false;
        dbPosition = 0;
        dbNumbers = -1;
        dbCistrans = "";
        modPos = -1;
        modNum = 1;
        modText = "";
        headgroupDecorators.clear();
        addOmegaLinoleoyloxyCer = false;
        heavyElement = Optional.empty();
        heavyNumber = 0;
        sphingaPure = false;
        lcbCarbonPreSet = 18;
        lcbDbPreSet = 0;
        lcbHydroPreSet.clear();
        sphingaPrefix = "";
        sphingaSuffix = "";
    }

    private void addAcer(TreeNode node) {
        String head = node.getText();
        headGroup = "ACer";

        if (!ACER_HEADS.containsKey(head)) {
            throw new LipidException("ACer head group '" + head + "' unknown");
        }

        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups);
        int acer_num = ACER_HEADS.get(head);
        hgd.getFunctionalGroupsInternal().put("decorator_acyl", new ArrayList<>());
        hgd.getFunctionalGroupsInternal().get("decorator_acyl").add(new FattyAcid("FA", acer_num, knownFunctionalGroups));
        headgroupDecorators.add(hgd);

        if (head.equals("1-O-lignoceroyl-omega-linoleoyloxy") || head.equals("1-O-stearoyl-omega-linoleoyloxy")) {
            addOmegaLinoleoyloxyCer = true;
        }
    }

    private void setHeavyElement(TreeNode node) {
        String nodeHeavyElement = node.getText();
        switch (nodeHeavyElement) {
            case "d":
            case "D":
                adduct.getHeavyElements().put(Element.H2, 0);
                heavyElement = Optional.of(Element.H2);
                break;
            default:
                throw new LipidParsingException("Heavy Element '" + heavyElement + "' unknown!");
        }
    }

    private void setHeavyNumber(TreeNode node) {
        if (this.heavyElement.isPresent()) {
            this.heavyNumber = node.getInt();
            adduct.getHeavyElements().put(this.heavyElement.get(), node.getInt());
        } else {
            throw new LipidParsingException("Trying to parse Heavy Element number, but no Heavy Element is present!");
        }
    }

    private void addAdditionalModifier(TreeNode node) {
        String modifier = node.getText();
        if (modifier.equals("h")) {
            FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
            String fg_name = functional_group.getName();
            if (!currentFa.getFunctionalGroupsInternal().containsKey(fg_name)) {
                currentFa.getFunctionalGroupsInternal().put(fg_name, new ArrayList<>());
            }
            currentFa.getFunctionalGroupsInternal().get(fg_name).add(functional_group);
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }
    }

    private void addCarbonPreLen(TreeNode node) {
        lcbCarbonPreSet = node.getInt();
    }

    private void sphingaDbSet(TreeNode node) {
        sphingaSuffix = node.getText();

        if (sphingaSuffix.equals("anine")) {
            lcbDbPreSet = 0;
        } else if (sphingaSuffix.equals("osine")) {
            lcbDbPreSet = 1;
        } else if (sphingaSuffix.equals("adienine")) {
            lcbDbPreSet = 2;
        }
    }

    private void newSphinga(TreeNode node) {
        headGroup = "SPB";
    }

    private void newSphingaPure(TreeNode node) {
        sphingaPure = true;
        lcbHydroPreSet.add(knownFunctionalGroups.get("OH"));
        lcbHydroPreSet.add(knownFunctionalGroups.get("OH"));
        lcbHydroPreSet.get(0).setPosition(1);
        lcbHydroPreSet.get(1).setPosition(3);
        newLcb(node);
    }

    private void setHydroPreNum(TreeNode node) {
        lcbHydroPreSet.add(knownFunctionalGroups.get("OH"));
        lcbHydroPreSet.get(lcbHydroPreSet.size() - 1).setPosition(4);
        sphingaPrefix = node.getText();
    }

    private void addPhospho(TreeNode node) {
        String phosphoSuffix = node.getText();
        if (phosphoSuffix.equals("1-phosphate")) {
            headGroup += "P";
        } else if (phosphoSuffix.equals("1-phosphocholine")) {
            headGroup = "LSM";
        }
        lcbHydroPreSet.remove(0);
    }

    private void setMolecularSubspeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.MOLECULAR_SPECIES);
    }

    private void pureFa(TreeNode node) {
        headGroup = "FA";
    }

    private void mediatorEvent(TreeNode node) {
        useHeadGroup = true;
        headGroup = node.getText();
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

    private void setOmegaHeadGroupName(TreeNode node) {
        addOmegaLinoleoyloxyCer = true;
        setHeadGroupName(node);
    }

    private void addGlyco(TreeNode node) {
        String glyco_name = node.getText();
        HeadgroupDecorator functional_group = null;
        try {
            functional_group = (HeadgroupDecorator) knownFunctionalGroups.get(glyco_name);
        } catch (Exception e) {
            throw new LipidParsingException("Carbohydrate '" + glyco_name + "' unknown");
        }

        functional_group.getElements().put(Element.O, functional_group.getElements().get(Element.O) - 1);
        headgroupDecorators.add(functional_group);
    }

    private void addDbPositionNumber(TreeNode node) {
        dbPosition = Integer.valueOf(node.getText());
    }

    private void addCistrans(TreeNode node) {
        dbCistrans = node.getText();
    }

    private void setHeadGroupName(TreeNode node) {
        headGroup = node.getText();
    }

    private void setSpeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.SPECIES);
    }

    private void setStructuralSubspeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
    }

    private void setMod(TreeNode node) {
        modText = "";
        modPos = -1;
        modNum = 1;
    }

    private void setModText(TreeNode node) {
        modText = node.getText();
    }

    private void setModPos(TreeNode node) {
        modPos = node.getInt();
    }

    private void setModNum(TreeNode node) {
        modNum = node.getInt();
    }

    private void addFunctionalGroup(TreeNode node) {
        if (!modText.equals("Cp")) {
            if (FattyAcid.LCB_STATES.contains(currentFa.getLipidFaBondType()) && modText.equals("OH") && currentFa.getFunctionalGroupsInternal().containsKey("OH") && currentFa.getFunctionalGroupsInternal().get("OH").size() > 0) {
                currentFa.getFunctionalGroupsInternal().get("OH").get(currentFa.getFunctionalGroupsInternal().get("OH").size() - 1).setPosition(modPos);
            } else {
                FunctionalGroup functional_group = knownFunctionalGroups.get(modText);
                functional_group.setPosition(modPos);
                functional_group.setCount(modNum);
                String fg_name = functional_group.getName();
                if (!currentFa.getFunctionalGroupsInternal().containsKey(fg_name)) {
                    currentFa.getFunctionalGroupsInternal().put(fg_name, new ArrayList<>());
                }
                currentFa.getFunctionalGroupsInternal().get(fg_name).add(functional_group);
            }
        } else {
            currentFa.setNumCarbon(currentFa.getNumCarbon() + 1);
            Cycle cycle = new Cycle(3, modPos, modPos + 2, knownFunctionalGroups);
            if (!currentFa.getFunctionalGroupsInternal().containsKey("cy")) {
                currentFa.getFunctionalGroupsInternal().put("cy", new ArrayList<>());
            }
            currentFa.getFunctionalGroupsInternal().get("cy").add(cycle);
        }
    }

    private void newFa(TreeNode node) {
        dbNumbers = -1;
        currentFa = new FattyAcid("FA", knownFunctionalGroups);
    }

    private void newLcb(TreeNode node) {
        lcb = new FattyAcid("LCB", knownFunctionalGroups);
        lcb.setType(LipidFaBondType.LCB_REGULAR);
        currentFa = lcb;
    }

    private void cleanLcb(TreeNode node) {
        if (sphingaPure) {
            lcb.setNumCarbon(lcbCarbonPreSet);
            lcb.getDoubleBonds().setNumDoubleBonds(lcbDbPreSet);
            currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<FunctionalGroup>());
            for (FunctionalGroup fg : lcbHydroPreSet) {
                currentFa.getFunctionalGroupsInternal().get("OH").add(fg);
            }
        }

        if (!sphingaSuffix.equals("")) {
            if ((sphingaSuffix.equals("anine") && lcb.getDoubleBonds().getNumDoubleBonds() != 0)
                    || (sphingaSuffix.equals("osine") && lcb.getDoubleBonds().getNumDoubleBonds() != 1)
                    || (sphingaSuffix.equals("adienine") && lcb.getDoubleBonds().getNumDoubleBonds() != 2)) {
                throw new LipidException("Double bond count does not match with head group description");
            }
        }

        if (sphingaPrefix.equals("Phyto") && !sphingaPure) {
            HashSet<Integer> posHydro = new HashSet<Integer>();
            for (FunctionalGroup fg : lcb.getFunctionalGroups().get("OH")) {
                posHydro.add(fg.getPosition());
            }
            if (lcb.getFunctionalGroups().isEmpty() || !lcb.getFunctionalGroups().containsKey("OH") || !posHydro.contains(4)) {
                throw new LipidException("hydroxyl count does not match with head group description");
            }
        }

        if (dbNumbers > -1 && dbNumbers != currentFa.getDoubleBonds().getNumDoubleBonds()) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }
        if (currentFa.getFunctionalGroupsInternal().containsKey("OH")) {
            for (FunctionalGroup fg : currentFa.getFunctionalGroupsInternal().get("OH")) {
                if (fg.getPosition() < 1) {
                    setStructuralSubspeciesLevel(node);
                    break;
                }
            }
        }
        currentFa = null;
    }

    private void appendFa(TreeNode node) {
        if (dbNumbers > -1 && dbNumbers != currentFa.getDoubleBonds().getNumDoubleBonds()) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }

        if (currentFa.getNumCarbon() == 0) {
            omitFa = true;
        }
        faList.add(currentFa);
        currentFa = null;
    }

    private void addEther(TreeNode node) {
        String ether = node.getText();
        if (ether.equals("O-") || ether.equals("e")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMANYL);
        } else if (ether.equals("P-") || ether.equals("p")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMENYL);
        }
    }

    private void addHydroxyl(TreeNode node) {
        int num_h = node.getInt();

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

    private void addDiHydroxyl(TreeNode node) {
        if (!currentFa.getFunctionalGroupsInternal().containsKey("OH")) {
            currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<>());
        }

        FunctionalGroup functional_group_p3 = knownFunctionalGroups.get("OH");
        functional_group_p3.setPosition(3);
        currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group_p3);

        if (!spRegularLcb()) {
            FunctionalGroup functional_group_p1 = knownFunctionalGroups.get("OH");
            functional_group_p1.setPosition(1);
            currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group_p1);
        }
    }

    private void addHydroxylLcb(TreeNode node) {
        if (!currentFa.getFunctionalGroupsInternal().containsKey("OH")) {
            currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<>());
        }

        String hydroxyl = node.getText();
        if (hydroxyl.equals("m")) {
            FunctionalGroup functional_group_p3 = knownFunctionalGroups.get("OH");
            functional_group_p3.setPosition(3);
            currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group_p3);

        } else if (hydroxyl.equals("d")) {
            if (!spRegularLcb()) {
                FunctionalGroup functional_group_p1 = knownFunctionalGroups.get("OH");
                functional_group_p1.setPosition(1);
                currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group_p1);
            }

            FunctionalGroup functional_group_p3 = knownFunctionalGroups.get("OH");
            functional_group_p3.setPosition(3);
            currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group_p3);

        } else if (hydroxyl.equals("t")) {
            if (!spRegularLcb()) {
                FunctionalGroup functional_group_p1 = knownFunctionalGroups.get("OH");
                functional_group_p1.setPosition(1);
                currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group_p1);
            }
            FunctionalGroup functional_group_p3 = knownFunctionalGroups.get("OH");
            functional_group_p3.setPosition(3);
            currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group_p3);

            FunctionalGroup functional_group_t = knownFunctionalGroups.get("OH");
            functional_group_t.setPosition(4);
            currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group_t);

        }
    }

    private void addDoubleBonds(TreeNode node) {
        currentFa.getDoubleBonds().setNumDoubleBonds(currentFa.getDoubleBonds().getNumDoubleBonds() + node.getInt());
    }

    private void addCarbon(TreeNode node) {
        currentFa.setNumCarbon(node.getInt());
    }

    private void buildLipid(TreeNode node) {
        if (omitFa && HEAD_GROUP_EXCEPTIONS.contains(headGroup)) {
            headGroup = "L" + headGroup;
        }

        if (lcb != null) {
            faList.add(0, lcb);
        }

        if (addOmegaLinoleoyloxyCer) {
            if (faList.size() != 2) {
                throw new ConstraintViolationException("omega-linoleoyloxy-Cer with a different combination to one long chain base and one fatty acyl chain unknown");
            }
            Map<String, ArrayList<FunctionalGroup>> fgroups = faList.get(faList.size() - 1).getFunctionalGroupsInternal();
            if (!fgroups.containsKey("acyl")) {
                fgroups.put("acyl", new ArrayList<>());
            }

            DoubleBonds db = new DoubleBonds(2);
            db.getDoubleBondPositions().put(9, "Z");
            db.getDoubleBondPositions().put(12, "Z");
            faList.get(faList.size() - 1).getFunctionalGroupsInternal().get("acyl").add(new FattyAcid("FA", 18, db));
            headGroup = "Cer";
        }

        Headgroup headgroup = prepareHeadgroupAndChecks();
        content = new LipidAdduct(assembleLipid(headgroup), adduct);
    }

    private void newAdduct(TreeNode node) {
        if (adduct == null) {
            adduct = new Adduct("", "");
        }
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
        if (adduct.getCharge() == 0) {
            adduct.setCharge(1);
        }
    }
}
