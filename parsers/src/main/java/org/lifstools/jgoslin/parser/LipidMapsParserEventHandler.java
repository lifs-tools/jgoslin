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
import java.util.Map;
import static java.util.Map.entry;
import java.util.Set;

public class LipidMapsParserEventHandler extends LipidBaseParserEventHandler {

    private boolean omitFa;
    private int dbNumbers;
    private int dbPosition;
    private String dbCistrans;
    private String modText;
    private int modPos;
    private int modNum;
    private boolean addOmegaLinoleoyloxyCer;

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

                entry("ether_pre_event", this::addEther),
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
                entry("special_cer_prefix_pre_event", this::addAcer)
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
    }

    public void addAcer(TreeNode node) {
        String head = node.getText();
        headGroup = "ACer";

        if (!ACER_HEADS.containsKey(head)) {
            throw new LipidException("ACer head group '" + head + "' unknown");
        }

        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups);
        int acer_num = ACER_HEADS.get(head);
        hgd.getFunctionalGroups().put("decorator_acyl", new ArrayList<>());
        hgd.getFunctionalGroups().get("decorator_acyl").add(new FattyAcid("FA", acer_num, knownFunctionalGroups));
        headgroupDecorators.add(hgd);

        if (head.equals("1-O-lignoceroyl-omega-linoleoyloxy") || head.equals("1-O-stearoyl-omega-linoleoyloxy")) {
            addOmegaLinoleoyloxyCer = true;
        }
    }

    public void setMolecularSubspeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.MOLECULAR_SPECIES);
    }

    public void pureFa(TreeNode node) {
        headGroup = "FA";
    }

    public void mediatorEvent(TreeNode node) {
        useHeadGroup = true;
        headGroup = node.getText();
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

    public void setOmegaHeadGroupName(TreeNode node) {
        addOmegaLinoleoyloxyCer = true;
        setHeadGroupName(node);
    }

    public void addGlyco(TreeNode node) {
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

    public void addDbPositionNumber(TreeNode node) {
        dbPosition = Integer.valueOf(node.getText());
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

    public void setStructuralSubspeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
    }

    public void setMod(TreeNode node) {
        modText = "";
        modPos = -1;
        modNum = 1;
    }

    public void setModText(TreeNode node) {
        modText = node.getText();
    }

    public void setModPos(TreeNode node) {
        modPos = node.getInt();
    }

    public void setModNum(TreeNode node) {
        modNum = node.getInt();
    }

    public void addFunctionalGroup(TreeNode node) {
        if (!modText.equals("Cp")) {
            FunctionalGroup functional_group = knownFunctionalGroups.get(modText);
            functional_group.setPosition(modPos);
            functional_group.setCount(modNum);
            String fg_name = functional_group.getName();
            if (!currentFa.getFunctionalGroups().containsKey(fg_name)) {
                currentFa.getFunctionalGroups().put(fg_name, new ArrayList<>());
            }
            currentFa.getFunctionalGroups().get(fg_name).add(functional_group);
        } else {
            currentFa.setNumCarbon(currentFa.getNumCarbon() + 1);
            Cycle cycle = new Cycle(3, modPos, modPos + 2, knownFunctionalGroups);
            if (!currentFa.getFunctionalGroups().containsKey("cy")) {
                currentFa.getFunctionalGroups().put("cy", new ArrayList<>());
            }
            currentFa.getFunctionalGroups().get("cy").add(cycle);
        }
    }

    public void newFa(TreeNode node) {
        dbNumbers = -1;
        currentFa = new FattyAcid("FA" + (faList.size() + 1), knownFunctionalGroups);
    }

    public void newLcb(TreeNode node) {
        lcb = new FattyAcid("LCB", knownFunctionalGroups);
        lcb.setType(LipidFaBondType.LCB_REGULAR);
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        currentFa = lcb;
    }

    public void cleanLcb(TreeNode node) {
        if (dbNumbers > -1 && dbNumbers != currentFa.getDoubleBonds().getNumDoubleBonds()) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }
        currentFa = null;
    }

    public void appendFa(TreeNode node) {
        if (dbNumbers > -1 && dbNumbers != currentFa.getDoubleBonds().getNumDoubleBonds()) {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }
        if (currentFa.getDoubleBonds().getDoubleBondPositions().isEmpty() && currentFa.getDoubleBonds().getNumDoubleBonds() > 0) {
            setLipidLevel(LipidLevel.SN_POSITION);
        }

        if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level | LipidLevel.SN_POSITION.level)) {
            currentFa.setPosition(faList.size() + 1);
        }

        if (currentFa.getNumCarbon() == 0) {
            omitFa = true;
        }
        faList.add(currentFa);
        currentFa = null;
    }

    public void addEther(TreeNode node) {
        String ether = node.getText();
        if (ether.equals("O-")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMANYL);
        } else if (ether.equals("P-")) {
            currentFa.setLipidFaBondType(LipidFaBondType.ETHER_PLASMENYL);
        }
    }

    public void addHydroxyl(TreeNode node) {
        int num_h = node.getInt();

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

    public void addHydroxylLcb(TreeNode node) {
        String hydroxyl = node.getText();
        int num_h = 0;
        if (hydroxyl.equals("m")) {
            num_h = 1;
        } else if (hydroxyl.equals("d")) {
            num_h = 2;
        } else if (hydroxyl.equals("t")) {
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
        currentFa.getDoubleBonds().setNumDoubleBonds(currentFa.getDoubleBonds().getNumDoubleBonds() + node.getInt());
    }

    public void addCarbon(TreeNode node) {
        currentFa.setNumCarbon(node.getInt());
    }

    public void buildLipid(TreeNode node) {
        if (omitFa && HEAD_GROUP_EXCEPTIONS.contains(headGroup)) {
            headGroup = "L" + headGroup;
        }

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
