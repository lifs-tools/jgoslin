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
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.LipidAdduct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;
import java.util.TreeMap;

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
                    entry("mediator_position_pre_event", this::setMediatorFunctionPosition),
                    entry("mediator_functional_group_post_event", this::addMediatorFunction),
                    entry("mediator_suffix_pre_event", this::addMediatorSuffix),
                    entry("mediator_tetranor_pre_event", this::setMediatorTetranor)
            );

        } catch (Exception e) {
            throw new LipidParsingException("Cannot initialize GoslinParserEventHandler");
        }
    }

    @Override
    protected void resetParser(TreeNode node) {
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
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
    }

    private void setHeadGroupName(TreeNode node) {
        headGroup = node.getText();
    }

    private void setSpeciesLevel(TreeNode node) {
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

    private void setMolecularSubspeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.MOLECULAR_SPECIES);
    }

    private void newFa(TreeNode node) {
        LipidFaBondType lipid_FA_bond_type = LipidFaBondType.ESTER;
        currentFa = new FattyAcid("FA", 2, null, null, lipid_FA_bond_type, knownFunctionalGroups);
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
        if (currentFa.getLipidFaBondType() == LipidFaBondType.ETHER_UNSPECIFIED) {
            throw new LipidException("Lipid with unspecified ether bond cannot be treated properly.");
        }
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }

        if (currentFa.getDoubleBonds().getNumDoubleBonds() < 0) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }

        faList.add(currentFa);
        currentFa = null;
    }

    private void buildLipid(TreeNode node) {
        if (lcb != null) {
            faList.add(0, lcb);
            lcb = null;
            currentFa = null;
        }

        if (plasmalogen != '\0' && faList.size() > 0 && lcb == null) {
            faList.get(0).setLipidFaBondType(plasmalogen == 'O' ? LipidFaBondType.ETHER_PLASMANYL : LipidFaBondType.ETHER_PLASMENYL);
        }

        Headgroup headgroup = prepareHeadgroupAndChecks();

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

    private void addDoubleBonds(TreeNode node) {
        currentFa.getDoubleBonds().setNumDoubleBonds(Integer.valueOf(node.getText()));
    }

    private void addCarbon(TreeNode node) {
        currentFa.setNumCarbon(Integer.valueOf(node.getText()));
    }

    private void addHydroxyl(TreeNode node) {
        int num_h = Integer.valueOf(node.getText());

        if (spRegularLcb()) {
            num_h -= 1;
        }

        FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
        functional_group.setCount(num_h);
        if (!currentFa.getFunctionalGroupsInternal().containsKey("OH")) {
            currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<>());
        }
        currentFa.getFunctionalGroupsInternal().get("OH").add(functional_group);
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
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

    private void setMediator(TreeNode node) {
        headGroup = "FA";
        currentFa = new FattyAcid("FA", knownFunctionalGroups);
        faList.add(currentFa);
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
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

    private void addMediatorFunction(TreeNode node) {
        FunctionalGroup functionalGroup = null;
        String fg = "";
        if (mediatorFunction.equals("H")) {
            functionalGroup = knownFunctionalGroups.get("OH");
            fg = "OH";
            if (mediatorFunctionPositions.size() > 0) {
                functionalGroup.setPosition(mediatorFunctionPositions.get(0));
            }
        } else if (mediatorFunction.equals("Oxo")) {
            functionalGroup = knownFunctionalGroups.get("oxo");
            fg = "oxo";
            if (mediatorFunctionPositions.size() > 0) {
                functionalGroup.setPosition(mediatorFunctionPositions.get(0));
            }
        } else if (mediatorFunction.equals("E") || mediatorFunction.equals("Ep")) {
            functionalGroup = knownFunctionalGroups.get("Ep");
            fg = "Ep";
            if (mediatorFunctionPositions.size() > 0) {
                functionalGroup.setPosition(mediatorFunctionPositions.get(0));
            }
        } else if (mediatorFunction.equals("DH") || mediatorFunction.equals("DiH")) {
            functionalGroup = knownFunctionalGroups.get("OH");
            fg = "OH";
            if (mediatorFunctionPositions.size() > 0) {
                functionalGroup.setPosition(mediatorFunctionPositions.get(0));
                FunctionalGroup functionalGroup2 = knownFunctionalGroups.get("OH");
                functionalGroup2.setPosition(mediatorFunctionPositions.get(1));
                currentFa.getFunctionalGroupsInternal().put("OH", new ArrayList<>());
                currentFa.getFunctionalGroupsInternal().get("OH").add(functionalGroup2);
            }
        }

        if (!currentFa.getFunctionalGroupsInternal().containsKey(fg)) {
            currentFa.getFunctionalGroupsInternal().put(fg, new ArrayList<>());
        }
        currentFa.getFunctionalGroupsInternal().get(fg).add(functionalGroup);
    }

    private void setTrivialMediator(TreeNode node) {
        headGroup = "FA";

        switch (node.getText()) {
            case "Palmitic acid":
                currentFa = new FattyAcid("FA", 16, knownFunctionalGroups);
                break;

            case "Linoleic acid":
                currentFa = new FattyAcid("FA", 18, new DoubleBonds(2), knownFunctionalGroups);
                break;

            case "AA":
                currentFa = new FattyAcid("FA", 20, new DoubleBonds(4), knownFunctionalGroups);
                break;

            case "ALA":
                currentFa = new FattyAcid("FA", 18, new DoubleBonds(3), knownFunctionalGroups);
                break;

            case "EPA":
                currentFa = new FattyAcid("FA", 20, new DoubleBonds(5), knownFunctionalGroups);
                break;

            case "DHA":
                currentFa = new FattyAcid("FA", 22, new DoubleBonds(6), knownFunctionalGroups);
                break;

            case "LTB4": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                f1.setPosition(5);
                f2.setPosition(12);
                DoubleBonds db = new DoubleBonds(new TreeMap<>(Map.of(6, "Z", 8, "E", 10, "E", 14, "Z")));
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2))));
                currentFa = new FattyAcid("FA", 20, db, fg, knownFunctionalGroups);
            }
            break;

            case "Resolvin D3": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                f1.setPosition(4);
                f2.setPosition(11);
                f3.setPosition(17);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2, f3))));
                currentFa = new FattyAcid("FA", 22, new DoubleBonds(6), fg, knownFunctionalGroups);
            }
            break;

            case "Maresin 1": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                f1.setPosition(4);
                f2.setPosition(14);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2))));
                currentFa = new FattyAcid("FA", 22, new DoubleBonds(6), fg, knownFunctionalGroups);
            }
            break;

            case "Resolvin D2": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                f1.setPosition(4);
                f2.setPosition(16);
                f3.setPosition(17);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2, f3))));
                currentFa = new FattyAcid("FA", 22, new DoubleBonds(6), fg, knownFunctionalGroups);
            }
            break;

            case "Resolvin D5": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                f1.setPosition(7);
                f2.setPosition(17);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2))));
                currentFa = new FattyAcid("FA", 22, new DoubleBonds(6), fg, knownFunctionalGroups);
            }
            break;

            case "Resolvin D1": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                FunctionalGroup f3 = knownFunctionalGroups.get("OH");
                f1.setPosition(7);
                f2.setPosition(8);
                f3.setPosition(17);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1, f2, f3))));
                currentFa = new FattyAcid("FA", 22, new DoubleBonds(6), fg, knownFunctionalGroups);
            }
            break;

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
                currentFa = new FattyAcid("FA", 20, new DoubleBonds(1), fg, knownFunctionalGroups);
            }
            break;

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
                currentFa = new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }
            break;

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
                currentFa = new FattyAcid("FA", 20, new DoubleBonds(3), fg, knownFunctionalGroups);
            }
            break;

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
                currentFa = new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }
            break;

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
                currentFa = new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }
            break;

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
                currentFa = new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }
            break;

            case "PGB2": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("OH");
                f1.setPosition(15);
                f2.setPosition(9);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f2))));
                Cycle cy = new Cycle(5, 8, 12, new DoubleBonds(1), fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                currentFa = new FattyAcid("FA", 20, new DoubleBonds(2), fg, knownFunctionalGroups);
            }
            break;

            case "15d-PGJ2": {
                FunctionalGroup f1 = knownFunctionalGroups.get("OH");
                FunctionalGroup f2 = knownFunctionalGroups.get("oxo");
                f1.setPosition(15);
                f2.setPosition(11);
                HashMap<String, ArrayList<FunctionalGroup>> fgc = new HashMap<>(Map.of("oxo", new ArrayList<>(Arrays.asList(f2))));
                Cycle cy = new Cycle(5, 8, 12, new DoubleBonds(1), fgc, knownFunctionalGroups);
                HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>(Map.of("OH", new ArrayList<>(Arrays.asList(f1)), "cy", new ArrayList<>(Arrays.asList(cy))));
                currentFa = new FattyAcid("FA", 20, new DoubleBonds(3), fg, knownFunctionalGroups);
            }
            break;
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

}
