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

import org.lifstools.jgoslin.domain.Adduct;
import org.lifstools.jgoslin.domain.LipidFaBondType;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.FattyAcid;
import org.lifstools.jgoslin.domain.Headgroup;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.LipidAdduct;
import java.util.ArrayList;

public class GoslinParserEventHandler extends LipidBaseParserEventHandler {

    private int dbPosition;
    private String dbCistrans;
    private char plasmalogen;

//    public GoslinParserEventHandler() {
//        this(new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), SumFormulaParser.getInstance()));
//    }

    public GoslinParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        super(knownFunctionalGroups);
        try {
            registeredEvents.put("lipid_pre_event", this::resetParser);
            registeredEvents.put("lipid_post_event", this::buildLipid);

            registeredEvents.put("hg_cl_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_mlcl_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_pl_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_lpl_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_lsl_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_dsl_pre_event", this::setHeadGroupName);
            registeredEvents.put("st_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_ste_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_stes_pre_event", this::setHeadGroupName);
            registeredEvents.put("mediator_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_mgl_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_dgl_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_sgl_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_tgl_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_dlcl_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_sac_di_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_sac_f_pre_event", this::setHeadGroupName);
            registeredEvents.put("hg_tpl_pre_event", this::setHeadGroupName);

            registeredEvents.put("gl_species_pre_event", this::setSpeciesLevel);
            registeredEvents.put("pl_species_pre_event", this::setSpeciesLevel);
            registeredEvents.put("sl_species_pre_event", this::setSpeciesLevel);
            registeredEvents.put("fa2_unsorted_pre_event", this::setMolecularSubspeciesLevel);
            registeredEvents.put("fa3_unsorted_pre_event", this::setMolecularSubspeciesLevel);
            registeredEvents.put("fa4_unsorted_pre_event", this::setMolecularSubspeciesLevel);
            registeredEvents.put("slbpa_pre_event", this::setMolecularSubspeciesLevel);
            registeredEvents.put("dlcl_pre_event", this::setMolecularSubspeciesLevel);
            registeredEvents.put("mlcl_pre_event", this::setMolecularSubspeciesLevel);

            registeredEvents.put("lcb_pre_event", this::newLcb);
            registeredEvents.put("lcb_post_event", this::cleanLcb);
            registeredEvents.put("fa_pre_event", this::newFa);
            registeredEvents.put("fa_post_event", this::appendFa);

            registeredEvents.put("db_single_position_pre_event", this::setIsomericLevel);
            registeredEvents.put("db_single_position_post_event", this::addDbPosition);
            registeredEvents.put("db_position_number_pre_event", this::addDbPositionNumber);
            registeredEvents.put("cistrans_pre_event", this::addCistrans);

            registeredEvents.put("ether_pre_event", this::addEther);
            registeredEvents.put("old_hydroxyl_pre_event", this::addOldHydroxyl);
            registeredEvents.put("db_count_pre_event", this::addDoubleBonds);
            registeredEvents.put("carbon_pre_event", this::addCarbon);
            registeredEvents.put("hydroxyl_pre_event", this::addHydroxyl);

            registeredEvents.put("adduct_info_pre_event", this::newAdduct);
            registeredEvents.put("adduct_pre_event", this::addAdduct);
            registeredEvents.put("charge_pre_event", this::addCharge);
            registeredEvents.put("charge_sign_pre_event", this::addChargeSign);

            registeredEvents.put("lpl_pre_event", this::setMolecularSubspeciesLevel);
            registeredEvents.put("plasmalogen_pre_event", this::setPlasmalogen);

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
    }

    public void setHeadGroupName(TreeNode node) {
        headGroup = node.getText();
    }

    public void setSpeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.SPECIES);
    }

    public void setIsomericLevel(TreeNode node) {
        dbPosition = 0;
        dbCistrans = "";
    }

    public void addDbPosition(TreeNode node) {
        if (currentFa != null) {
            currentFa.getDoubleBonds().getDoubleBondPositions().put(dbPosition, dbCistrans);
            if (!dbCistrans.equals("E") && !dbCistrans.equals("Z")) {
                setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
            }
        }
    }
    
    public void setPlasmalogen(TreeNode node) {
        plasmalogen = node.getText().toUpperCase().charAt(0);
    }

    public void addDbPositionNumber(TreeNode node) {
        dbPosition = Integer.valueOf(node.getText());
    }

    public void addCistrans(TreeNode node) {
        dbCistrans = node.getText();
    }

    public void setMolecularSubspeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.MOLECULAR_SPECIES);
    }

    public void newFa(TreeNode node) {
        LipidFaBondType lipid_FA_bond_type = LipidFaBondType.ESTER;
        currentFa = new FattyAcid("FA" + (faList.size() + 1), 2, null, null, lipid_FA_bond_type, knownFunctionalGroups);
    }

    public void newLcb(TreeNode node) {
        lcb = new FattyAcid("LCB", knownFunctionalGroups);
        lcb.setType(LipidFaBondType.LCB_REGULAR);
        currentFa = lcb;
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
    }

    public void cleanLcb(TreeNode node) {
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }
        currentFa = null;
    }

    public void appendFa(TreeNode node) {
        if (currentFa.getLipidFaBondType() == LipidFaBondType.ETHER_UNSPECIFIED) {
            throw new LipidException("Lipid with unspecified ether bond cannot be treated properly.");
        }
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }

        if (currentFa.getDoubleBonds().getNumDoubleBonds() < 0) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
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
            lcb = null;
            currentFa = null;
        }
        
        
        if (plasmalogen != '\0' && faList.size() > 0 && lcb == null){
            faList.get(0).setLipidFaBondType(plasmalogen == 'O' ? LipidFaBondType.ETHER_PLASMANYL : LipidFaBondType.ETHER_PLASMENYL);
        }

        Headgroup headgroup = prepareHeadgroupAndChecks();

        LipidAdduct lipid = new LipidAdduct(assembleLipid(headgroup), adduct);
        content = lipid;

    }

    public void addEther(TreeNode node) {
        String ether = node.getText();
        if (ether.equals("a")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMANYL);
        } else if (ether.equals("p")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMENYL);
            currentFa.getDoubleBonds().setNumDoubleBonds(Math.max(0, currentFa.getDoubleBonds().getNumDoubleBonds() - 1));
        }
        plasmalogen = '\0';
    }

    public void addOldHydroxyl(TreeNode node) {
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

    public void addDoubleBonds(TreeNode node) {
        currentFa.getDoubleBonds().setNumDoubleBonds(Integer.valueOf(node.getText()));
    }

    public void addCarbon(TreeNode node) {
        currentFa.setNumCarbon(Integer.valueOf(node.getText()));
    }

    public void addHydroxyl(TreeNode node) {
        int num_h = Integer.valueOf(node.getText());

        if (spRegularLcb()) {
            num_h -= 1;
        }

        FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
        functional_group.setCount(num_h);
        if (!currentFa.getFunctionalGroups().containsKey("OH")) {
            currentFa.getFunctionalGroups().put("OH", new ArrayList<>());
        }
        currentFa.getFunctionalGroups().get("OH").add(functional_group);
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
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
