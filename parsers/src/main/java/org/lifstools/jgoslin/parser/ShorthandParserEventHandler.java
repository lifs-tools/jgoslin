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
import java.util.HashSet;

public class ShorthandParserEventHandler extends LipidBaseParserEventHandler {

    private ArrayDeque<FunctionalGroup> currentFas = new ArrayDeque<>();
    private Dictionary tmp = new Dictionary();
    private boolean acerSpecies = false;
    private static final HashSet<String> SPECIAL_TYPES = new HashSet<String>(Arrays.asList("acyl", "alkyl", "decorator_acyl", "decorator_alkyl", "cc"));

//    public ShorthandParserEventHandler() {
//        this(new KnownFunctionalGroups(StringFunctions.getResourceAsStringList("functional-groups.csv"), SumFormulaParser.getInstance()));
//    }

    public ShorthandParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        super(knownFunctionalGroups);
        try {
            registeredEvents.put("lipid_pre_event", this::resetParser);
            registeredEvents.put("lipid_post_event", this::buildLipid);

            // set categories
            registeredEvents.put("sl_pre_event", this::preSphingolipid);
            registeredEvents.put("sl_post_event", this::postSphingolipid);
            registeredEvents.put("sl_hydroxyl_pre_event", this::setHydroxyl);

            // set adduct events
            registeredEvents.put("adduct_info_pre_event", this::newAdduct);
            registeredEvents.put("adduct_pre_event", this::addAdduct);
            registeredEvents.put("charge_pre_event", this::addCharge);
            registeredEvents.put("charge_sign_pre_event", this::addChargeSign);

            // set species events
            registeredEvents.put("med_species_pre_event", this::setSpeciesLevel);
            registeredEvents.put("gl_species_pre_event", this::setSpeciesLevel);
            registeredEvents.put("gl_molecular_species_pre_event", this::setMolecularLevel);
            registeredEvents.put("pl_species_pre_event", this::setSpeciesLevel);
            registeredEvents.put("pl_molecular_species_pre_event", this::setMolecularLevel);
            registeredEvents.put("sl_species_pre_event", this::setSpeciesLevel);
            registeredEvents.put("pl_single_pre_event", this::setMolecularLevel);
            registeredEvents.put("unsorted_fa_separator_pre_event", this::setMolecularLevel);
            registeredEvents.put("ether_num_pre_event", this::setEtherNum);

            // set head groups events
            registeredEvents.put("med_hg_single_pre_event", this::setHeadgroupName);
            registeredEvents.put("med_hg_double_pre_event", this::setHeadgroupName);
            registeredEvents.put("med_hg_triple_pre_event", this::setHeadgroupName);
            registeredEvents.put("gl_hg_single_pre_event", this::setHeadgroupName);
            registeredEvents.put("gl_hg_double_pre_event", this::setHeadgroupName);
            registeredEvents.put("gl_hg_true_double_pre_event", this::setHeadgroupName);
            registeredEvents.put("gl_hg_triple_pre_event", this::setHeadgroupName);
            registeredEvents.put("pl_hg_single_pre_event", this::setHeadgroupName);
            registeredEvents.put("pl_hg_double_pre_event", this::setHeadgroupName);
            registeredEvents.put("pl_hg_quadro_pre_event", this::setHeadgroupName);
            registeredEvents.put("sl_hg_single_pre_event", this::setHeadgroupName);
            registeredEvents.put("pl_hg_double_fa_hg_pre_event", this::setHeadgroupName);
            registeredEvents.put("sl_hg_double_name_pre_event", this::setHeadgroupName);
            registeredEvents.put("st_hg_pre_event", this::setHeadgroupName);
            registeredEvents.put("st_hg_ester_pre_event", this::setHeadgroupName);
            registeredEvents.put("hg_pip_pure_m_pre_event", this::setHeadgroupName);
            registeredEvents.put("hg_pip_pure_d_pre_event", this::setHeadgroupName);
            registeredEvents.put("hg_pip_pure_t_pre_event", this::setHeadgroupName);
            registeredEvents.put("hg_PE_PS_pre_event", this::setHeadgroupName);

            // set head group headgroupDecorators
            registeredEvents.put("carbohydrate_pre_event", this::setCarbohydrate);
            registeredEvents.put("carbohydrate_structural_pre_event", this::setCarbohydrateStructural);
            registeredEvents.put("carbohydrate_isomeric_pre_event", this::setCarbohydrateIsomeric);

            // fatty acyl events
            registeredEvents.put("lcb_post_event", this::setLcb);
            registeredEvents.put("fatty_acyl_chain_pre_event", this::newFattyAcylChain);
            registeredEvents.put("fatty_acyl_chain_post_event", this::addFattyAcylChain);
            registeredEvents.put("carbon_pre_event", this::setCarbon);
            registeredEvents.put("db_count_pre_event", this::setDoubleBondCount);
            registeredEvents.put("db_position_number_pre_event", this::setDoubleBondPosition);
            registeredEvents.put("db_single_position_pre_event", this::setDoubleBondInformation);
            registeredEvents.put("db_single_position_post_event", this::addDoubleBondInformation);
            registeredEvents.put("cistrans_pre_event", this::setCisTrans);
            registeredEvents.put("ether_type_pre_event", this::setEtherType);

            // set functional group events
            registeredEvents.put("func_group_data_pre_event", this::setFunctionalGroup);
            registeredEvents.put("func_group_data_post_event", this::addFunctionalGroup);
            registeredEvents.put("func_group_pos_number_pre_event", this::setFunctionalGroupPosition);
            registeredEvents.put("func_group_name_pre_event", this::setFunctionalGroupName);
            registeredEvents.put("func_group_count_pre_event", this::setFunctionalGroupCount);
            registeredEvents.put("stereo_type_pre_event", this::setFunctionalGroupStereo);
            registeredEvents.put("molecular_func_group_name_pre_event", this::setMolecularFuncGroup);

            // set cycle events
            registeredEvents.put("func_group_cycle_pre_event", this::setCycle);
            registeredEvents.put("func_group_cycle_post_event", this::addCycle);
            registeredEvents.put("cycle_start_pre_event", this::setCycleStart);
            registeredEvents.put("cycle_end_pre_event", this::setCycleEnd);
            registeredEvents.put("cycle_number_pre_event", this::setCycleNumber);
            registeredEvents.put("cycle_db_cnt_pre_event", this::setCycleDbCount);
            registeredEvents.put("cycle_db_positions_pre_event", this::setCycleDbPositions);
            registeredEvents.put("cycle_db_positions_post_event", this::checkCycleDbPositions);
            registeredEvents.put("cycle_db_position_number_pre_event", this::setCycleDbPosition);
            registeredEvents.put("cycle_db_position_cis_trans_pre_event", this::setCycleDbPositionCistrans);
            registeredEvents.put("cylce_element_pre_event", this::addCycleElement);

            // set linkage events
            registeredEvents.put("fatty_acyl_linkage_pre_event", this::setAcylLinkage);
            registeredEvents.put("fatty_acyl_linkage_post_event", this::addAcylLinkage);
            registeredEvents.put("fatty_alkyl_linkage_pre_event", this::setAlkylLinkage);
            registeredEvents.put("fatty_alkyl_linkage_post_event", this::addAlkylLinkage);
            registeredEvents.put("fatty_linkage_number_pre_event", this::setFattyLinkageNumber);
            registeredEvents.put("fatty_acyl_linkage_sign_pre_event", this::setLinkageType);
            registeredEvents.put("hydrocarbon_chain_pre_event", this::setHydrocarbonChain);
            registeredEvents.put("hydrocarbon_chain_post_event", this::addHydrocarbonChain);
            registeredEvents.put("hydrocarbon_number_pre_event", this::setFattyLinkageNumber);

            // set remaining events
            registeredEvents.put("ring_stereo_pre_event", this::setRingStereo);
            registeredEvents.put("pl_hg_fa_pre_event", this::setHgAcyl);
            registeredEvents.put("pl_hg_fa_post_event", this::addHgAcyl);
            registeredEvents.put("pl_hg_alk_pre_event", this::setHgAlkyl);
            registeredEvents.put("pl_hg_alk_post_event", this::addHgAlkyl);
            registeredEvents.put("pl_hg_species_pre_event", this::addPlSpeciesData);
            registeredEvents.put("hg_pip_m_pre_event", this::suffixDecoratorMolecular);
            registeredEvents.put("hg_pip_d_pre_event", this::suffixDecoratorMolecular);
            registeredEvents.put("hg_pip_t_pre_event", this::suffixDecoratorMolecular);
            registeredEvents.put("hg_PE_PS_type_pre_event", this::suffixDecoratorSpecies);
            registeredEvents.put("acer_hg_post_event", this::setAcer);
            registeredEvents.put("acer_species_post_event", this::setAcerSpecies);
            
            registeredEvents.put("sterol_definition_post_event", this::setSterolDefinition);

        } catch (Exception e) {
            throw new LipidParsingException("Cannot initialize ShorthandParserEventHandler.");
        }
    }

    @Override
    protected void resetParser(TreeNode node) {
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        adduct = null;
        headGroup = "";
        faList.clear();
        currentFas.clear();
        // FIXME
        headgroupDecorators = new ArrayList<>();
        tmp = new Dictionary();
        acerSpecies = false;
    }

    public String faI() {
        return "fa" + Integer.toString(currentFas.size());
    }

    public void buildLipid(TreeNode node) {
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
    
    
    public void setSterolDefinition(TreeNode node){
        headGroup += " " + node.getText();
        faList.remove(0);
        
    }

    public void preSphingolipid(TreeNode node) {
        tmp.put("sl_hydroxyl", 0);
    }

    public void postSphingolipid(TreeNode node) {
        if (((int) tmp.get("sl_hydroxyl")) == 0 && !headGroup.equals("Cer") && !headGroup.equals("SPB")) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }
    }

    public void setHydroxyl(TreeNode node) {
        tmp.put("sl_hydroxyl", 1);
    }

    public void newAdduct(TreeNode node) {
        adduct = new Adduct("", "", 0, 0);
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
        } else {
            adduct.setChargeSign(-1);
        }
    }

    public void setSpeciesLevel(TreeNode node) {
        setLipidLevel(LipidLevel.SPECIES);
    }

    public void setMolecularLevel(TreeNode node) {
        setLipidLevel(LipidLevel.MOLECULAR_SPECIES);
    }

    public void setEtherNum(TreeNode node) {
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

    public void setHeadgroupName(TreeNode node) {
        if (headGroup.length() == 0) {
            headGroup = node.getText();
        }
    }

    public void setCarbohydrate(TreeNode node) {
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
            if (!currentFas.peekLast().getFunctionalGroups().containsKey(carbohydrate)) {
                currentFas.peekLast().getFunctionalGroups().put(carbohydrate, new ArrayList<>());
            }
            currentFas.peekLast().getFunctionalGroups().get(carbohydrate).add(functional_group);
        }
    }

    public void setCarbohydrateStructural(TreeNode node) {
        setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        tmp.put("func_group_head", 1);
    }

    public void setCarbohydrateIsomeric(TreeNode node) {
        tmp.put("func_group_head", 1);
    }

    public void setLcb(TreeNode node) {
        FattyAcid fa = faList.get(faList.size() - 1);
        fa.setName("LCB");
        fa.setType(LipidFaBondType.LCB_REGULAR);
    }

    public void newFattyAcylChain(TreeNode node) {
        currentFas.add(new FattyAcid("FA", knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
    }

    public void addFattyAcylChain(TreeNode node) {
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
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }
        tmp.remove(fa_i);

        FattyAcid fa = (FattyAcid) currentFas.pollLast();
        if (special_type.length() > 0) {
            fa.setName(special_type);
            if (!currentFas.peekLast().getFunctionalGroups().containsKey(special_type)) {
                currentFas.peekLast().getFunctionalGroups().put(special_type, new ArrayList<>());
            }
            currentFas.peekLast().getFunctionalGroups().get(special_type).add(fa);
        } else {
            faList.add(fa);
        }
    }

    public void setCarbon(TreeNode node) {
        ((FattyAcid) currentFas.peekLast()).setNumCarbon(Integer.valueOf(node.getText()));
    }

    public void setDoubleBondCount(TreeNode node) {
        int db_cnt = Integer.valueOf(node.getText());
        ((Dictionary) tmp.get(faI())).put("db_count", db_cnt);
        ((FattyAcid) currentFas.peekLast()).getDoubleBonds().setNumDoubleBonds(db_cnt);
    }

    public void setDoubleBondPosition(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("db_position", Integer.valueOf(node.getText()));
    }

    public void setDoubleBondInformation(TreeNode node) {
        String fa_i = faI();
        ((Dictionary) tmp.get(fa_i)).put("db_position", 0);
        ((Dictionary) tmp.get(fa_i)).put("db_cistrans", "");
    }

    public void addDoubleBondInformation(TreeNode node) {
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

    public void setCisTrans(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("db_cistrans", node.getText());
    }

    public void setEtherType(TreeNode node) {
        String ether_type = node.getText();
        if (ether_type.equals("O-")) {
            ((FattyAcid) currentFas.peekLast()).setLipidFaBondType(LipidFaBondType.ETHER_PLASMANYL);
        } else if (ether_type.equals("P-")) {
            ((FattyAcid) currentFas.peekLast()).setLipidFaBondType(LipidFaBondType.ETHER_PLASMENYL);
        }
    }

    public void setFunctionalGroup(TreeNode node) {
        String fa_i = faI();
        Dictionary gd = (Dictionary) tmp.get(fa_i);
        gd.put("fg_pos", -1);
        gd.put("fg_name", "0");
        gd.put("fg_cnt", 1);
        gd.put("fg_stereo", "");
        gd.put("fg_ring_stereo", "");
    }

    public void addFunctionalGroup(TreeNode node) {
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

        FunctionalGroup functional_group = null;
        try {
            // FIXME please do not use static singletons, this usually does not pay off in Java and introduces memory leaks
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

        if (!currentFas.peekLast().getFunctionalGroups().containsKey(fg_name)) {
            currentFas.peekLast().getFunctionalGroups().put(fg_name, new ArrayList<>());
        }
        currentFas.peekLast().getFunctionalGroups().get(fg_name).add(functional_group);
    }

    public void setFunctionalGroupPosition(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_pos", Integer.valueOf(node.getText()));
    }

    public void setFunctionalGroupName(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", node.getText());
    }

    public void setFunctionalGroupCount(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_cnt", Integer.valueOf(node.getText()));
    }

    public void setFunctionalGroupStereo(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_stereo", node.getText());
    }

    public void setMolecularFuncGroup(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", node.getText());
    }

    public void setCycle(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", "cy");
        currentFas.add(new Cycle(0, knownFunctionalGroups));

        String fa_i = faI();
        tmp.put(fa_i, new Dictionary());
        ((Dictionary) tmp.get(fa_i)).put("cycle_elements", new GenericList());
    }

    public void addCycle(TreeNode node) {
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
        if (!currentFas.peekLast().getFunctionalGroups().containsKey("cy")) {
            currentFas.peekLast().getFunctionalGroups().put("cy", new ArrayList<>());
        }
        currentFas.peekLast().getFunctionalGroups().get("cy").add(cycle);
    }

    public void setCycleStart(TreeNode node) {
        ((Cycle) currentFas.peekLast()).setStart(Integer.valueOf(node.getText()));
    }

    public void setCycleEnd(TreeNode node) {
        ((Cycle) currentFas.peekLast()).setEnd(Integer.valueOf(node.getText()));
    }

    public void setCycleNumber(TreeNode node) {
        ((Cycle) currentFas.peekLast()).setCycle(Integer.valueOf(node.getText()));
    }

    public void setCycleDbCount(TreeNode node) {
        ((Cycle) currentFas.peekLast()).getDoubleBonds().setNumDoubleBonds(Integer.valueOf(node.getText()));
    }

    public void setCycleDbPositions(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("cycle_db", ((Cycle) currentFas.peekLast()).getDoubleBonds().getNumDoubleBonds());
    }

    public void checkCycleDbPositions(TreeNode node) {
        if (((Cycle) currentFas.peekLast()).getDoubleBonds().getNumDoubleBonds() != (int) ((Dictionary) tmp.get(faI())).get("cycle_db")) {
            throw new LipidException("Double bond number in cycle does not correspond to number of double bond positions.");
        }
    }

    public void setCycleDbPosition(TreeNode node) {
        int pos = Integer.valueOf(node.getText());
        ((Cycle) currentFas.peekLast()).getDoubleBonds().getDoubleBondPositions().put(pos, "");
        ((Dictionary) tmp.get(faI())).put("last_db_pos", pos);
    }

    public void setCycleDbPositionCistrans(TreeNode node) {
        int pos = (int) ((Dictionary) tmp.get(faI())).get("last_db_pos");
        ((Cycle) currentFas.peekLast()).getDoubleBonds().getDoubleBondPositions().put(pos, node.getText());
    }

    public void addCycleElement(TreeNode node) {
        String element = node.getText();

        if (!Elements.ELEMENT_POSITIONS.containsKey(element)) {
            throw new LipidParsingException("Element '" + element + "' unknown");
        }

        ((GenericList) ((Dictionary) tmp.get(faI())).get("cycle_elements")).add(Elements.ELEMENT_POSITIONS.get(element));
    }

    public void setAcylLinkage(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", "acyl");
        currentFas.add(new AcylAlkylGroup((FattyAcid) null, knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
        ((Dictionary) tmp.get(faI())).put("linkage_pos", -1);
    }

    public void addAcylLinkage(TreeNode node) {
        boolean linkage_type = (int) ((Dictionary) tmp.get(faI())).get("linkage_type") == 1;
        int linkage_pos = (int) ((Dictionary) tmp.get(faI())).get("linkage_pos");

        tmp.remove(faI());
        AcylAlkylGroup acyl = (AcylAlkylGroup) currentFas.pollLast();

        acyl.setPosition(linkage_pos);
        acyl.setNitrogenBond(linkage_type);
        if (linkage_pos == -1) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }

        if (!currentFas.peekLast().getFunctionalGroups().containsKey("acyl")) {
            currentFas.peekLast().getFunctionalGroups().put("acyl", new ArrayList<FunctionalGroup>());
        }
        currentFas.peekLast().getFunctionalGroups().get("acyl").add(acyl);
    }

    public void setAlkylLinkage(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", "alkyl");
        currentFas.add(new AcylAlkylGroup(null, -1, 1, true, knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
        ((Dictionary) tmp.get(faI())).put("linkage_pos", -1);
    }

    public void addAlkylLinkage(TreeNode node) {
        int linkage_pos = (int) ((Dictionary) tmp.get(faI())).get("linkage_pos");
        tmp.remove(faI());
        AcylAlkylGroup alkyl = (AcylAlkylGroup) currentFas.pollLast();

        alkyl.setPosition(linkage_pos);
        if (linkage_pos == -1) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }

        if (!currentFas.peekLast().getFunctionalGroups().containsKey("alkyl")) {
            currentFas.peekLast().getFunctionalGroups().put("alkyl", new ArrayList<FunctionalGroup>());
        }
        currentFas.peekLast().getFunctionalGroups().get("alkyl").add(alkyl);
    }

    public void setFattyLinkageNumber(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("linkage_pos", Integer.valueOf(node.getText()));
    }

    public void setLinkageType(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("linkage_type", node.getText().equals("N") ? 1 : 0);
    }

    public void setHydrocarbonChain(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_name", "cc");
        currentFas.add(new CarbonChain((FattyAcid) null, knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
        ((Dictionary) tmp.get(faI())).put("linkage_pos", -1);
    }

    public void addHydrocarbonChain(TreeNode node) {
        int linkage_pos = (int) ((Dictionary) tmp.get(faI())).get("linkage_pos");
        tmp.remove(faI());
        CarbonChain cc = (CarbonChain) currentFas.pollLast();
        cc.setPosition(linkage_pos);
        if (linkage_pos == -1) {
            setLipidLevel(LipidLevel.STRUCTURE_DEFINED);
        }

        if (!currentFas.peekLast().getFunctionalGroups().containsKey("cc")) {
            currentFas.peekLast().getFunctionalGroups().put("cc", new ArrayList<FunctionalGroup>());
        }
        currentFas.peekLast().getFunctionalGroups().get("cc").add(cc);
    }

    public void setRingStereo(TreeNode node) {
        ((Dictionary) tmp.get(faI())).put("fg_ring_stereo", node.getText());
    }

    public void setHgAcyl(TreeNode node) {
        String fa_i = faI();
        tmp.put(fa_i, new Dictionary());
        ((Dictionary) tmp.get(fa_i)).put("fg_name", "decorator_acyl");
        currentFas.add(new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
    }

    public void addHgAcyl(TreeNode node) {
        tmp.remove(faI());
        headgroupDecorators.add((HeadgroupDecorator) currentFas.pollLast());
        tmp.remove(faI());
    }

    public void setHgAlkyl(TreeNode node) {
        tmp.put(faI(), new Dictionary());
        ((Dictionary) tmp.get(faI())).put("fg_name", "decorator_alkyl");
        currentFas.add(new HeadgroupDecorator("decorator_alkyl", -1, 1, null, true, knownFunctionalGroups));
        tmp.put(faI(), new Dictionary());
    }

    public void addHgAlkyl(TreeNode node) {
        tmp.remove(faI());
        headgroupDecorators.add((HeadgroupDecorator) currentFas.pollLast());
        tmp.remove(faI());
    }

    public void addPlSpeciesData(TreeNode node) {
        setLipidLevel(LipidLevel.SPECIES);
        HeadgroupDecorator hgd = new HeadgroupDecorator("", knownFunctionalGroups);
        hgd.getElements().put(Element.O, hgd.getElements().get(Element.O) + 1);
        hgd.getElements().put(Element.H, hgd.getElements().get(Element.H) - 1);
        headgroupDecorators.add(hgd);
    }

    public void suffixDecoratorMolecular(TreeNode node) {
        headgroupDecorators.add(new HeadgroupDecorator(node.getText(), -1, 1, null, true, LipidLevel.MOLECULAR_SPECIES, knownFunctionalGroups));
    }

    public void suffixDecoratorSpecies(TreeNode node) {
        headgroupDecorators.add(new HeadgroupDecorator(node.getText(), -1, 1, null, true, LipidLevel.SPECIES, knownFunctionalGroups));
    }

    public void setAcer(TreeNode node) {
        headGroup = "ACer";
        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups);
        hgd.getFunctionalGroups().put("decorator_acyl", new ArrayList<>());
        hgd.getFunctionalGroups().get("decorator_acyl").add(faList.get(faList.size() - 1));
        faList.remove(faList.size() - 1);
        headgroupDecorators.add(hgd);
    }

    public void setAcerSpecies(TreeNode node) {
        headGroup = "ACer";
        setLipidLevel(LipidLevel.SPECIES);
        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups);
        hgd.getFunctionalGroups().put("decorator_acyl", new ArrayList<>());
        hgd.getFunctionalGroups().get("decorator_acyl").add(new FattyAcid("FA", 2, knownFunctionalGroups));
        headgroupDecorators.add(hgd);
        acerSpecies = true;
    }
}
