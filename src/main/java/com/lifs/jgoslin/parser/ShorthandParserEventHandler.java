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

package com.lifs.jgoslin.parser;

import com.lifs.jgoslin.antlr.*;
import com.lifs.jgoslin.domain.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


public class ShorthandParserEventHandler extends Shorthand2020BaseListener implements BaseParserEventHandler<LipidAdduct> {
    public LipidAdduct content;
    public LipidLevel level = LipidLevel.FULL_STRUCTURE;
    public String head_group = "";
    public FattyAcid lcb = null;
    public ArrayList<FattyAcid> fa_list = new ArrayList<>();
    public FattyAcid current_fa = null;
    public Adduct adduct = null;
    public ArrayList<HeadgroupDecorator> headgroup_decorators = new ArrayList<>();
    public boolean use_head_group = false;
    public ExtendedList<FunctionalGroup> current_fas;
    public Dict tmp = new Dict();
    public boolean acer_species = false;
    public static final HashSet<String> special_types = new HashSet<String>(Arrays.asList("acyl", "alkyl", "decorator_acyl", "decorator_alkyl", "cc"));
        
    public static HashSet<String> SP_EXCEPTION_CLASSES = new HashSet<>(Arrays.asList("Cer", "Ceramide", "Sphingosine", "So", "Sphinganine", "Sa", "SPH", "Sph", "LCB"));

    
    public ShorthandParserEventHandler(){
        set_content(null);
    }
    
    @Override
    public void set_content(LipidAdduct l){
        content = l;
    }
    
    @Override
    public LipidAdduct get_content(){
        return content;
    }


    @Override
    public void set_lipid_level(LipidLevel _level){
        level = level.level < _level.level ? level : _level;
    }

    public String FA_I(){
        return "fa" + Integer.toString(current_fas.size());
    }

    public boolean sp_regular_lcb(){
        return Headgroup.get_category(head_group) == LipidCategory.SP && (current_fa.lipid_FA_bond_type == LipidFaBondType.LCB_REGULAR ||current_fa.lipid_FA_bond_type == LipidFaBondType.LCB_EXCEPTION) && !(SP_EXCEPTION_CLASSES.contains(head_group) && headgroup_decorators.isEmpty());
    }
    

    public Headgroup prepare_headgroup_and_checks(){
        Headgroup headgroup = new Headgroup(head_group, headgroup_decorators, use_head_group);
        if (use_head_group) return headgroup;

        int true_fa = 0;
        for (FattyAcid fa : fa_list) {
            true_fa += (fa.num_carbon > 0 || fa.double_bonds.get_num() > 0) ? 1 : 0;
        }
        int poss_fa = (LipidClasses.get_instance().size() > headgroup.lipid_class) ? LipidClasses.get_instance().get(headgroup.lipid_class).possible_num_fa : 0;

        // make lyso
        boolean can_be_lyso = (LipidClasses.get_instance().size() > Headgroup.get_class("L" + head_group)) ? LipidClasses.get_instance().get(Headgroup.get_class("L" + head_group)).special_cases.contains("Lyso") : false;

        if (true_fa + 1 == poss_fa && level != LipidLevel.SPECIES && headgroup.lipid_category == LipidCategory.GP && can_be_lyso){
            head_group = "L" + head_group;
            headgroup = new Headgroup(head_group, headgroup_decorators, use_head_group);
            poss_fa = (LipidClasses.get_instance().size() > headgroup.lipid_class) ? LipidClasses.get_instance().get(headgroup.lipid_class).possible_num_fa : 0;
        }

        else if (true_fa + 2 == poss_fa && level != LipidLevel.SPECIES && headgroup.lipid_category == LipidCategory.GP && head_group.equals("CL")){
            head_group = "DL" + head_group;
            headgroup = new Headgroup(head_group, headgroup_decorators, use_head_group);
            poss_fa = (LipidClasses.get_instance().size() > headgroup.lipid_class) ? LipidClasses.get_instance().get(headgroup.lipid_class).possible_num_fa : 0;
        }

        if (level == LipidLevel.SPECIES){
            if (true_fa == 0 && poss_fa != 0){
                String hg_name = headgroup.headgroup;
                throw new ConstraintViolationException("No fatty acyl information lipid class '" + hg_name + "' provided.");
            }
        }

        else if (true_fa != poss_fa && LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level))
        {
            String hg_name = headgroup.headgroup;
            throw new ConstraintViolationException("Number of described fatty acyl chains (" + Integer.toString(true_fa) + ") not allowed for lipid class '" + hg_name + "' (having " + Integer.toString(poss_fa) + " fatty aycl chains).");
        }

        if (LipidClasses.get_instance().get(headgroup.lipid_class).special_cases.contains("HC")){
            fa_list.get(0).lipid_FA_bond_type = LipidFaBondType.AMINE;
        }


        int max_num_fa = (LipidClasses.get_instance().size() > headgroup.lipid_class) ? LipidClasses.get_instance().get(headgroup.lipid_class).max_num_fa : 0;
        if (max_num_fa != fa_list.size()) set_lipid_level(LipidLevel.MOLECULAR_SPECIES);

        if (fa_list.size() > 0 && headgroup.sp_exception) fa_list.get(0).set_type(LipidFaBondType.LCB_EXCEPTION);

        return headgroup;
    }



    public LipidSpecies assemble_lipid(Headgroup headgroup){
        LipidSpecies ls = null;
        switch (level){
            case COMPLETE_STRUCTURE: ls = new LipidCompleteStructure(headgroup, fa_list); break;
            case FULL_STRUCTURE: ls = new LipidFullStructure(headgroup, fa_list); break;
            case STRUCTURE_DEFINED: ls = new LipidStructureDefined(headgroup, fa_list); break;
            case SN_POSITION: ls = new LipidSnPosition(headgroup, fa_list); break;
            case MOLECULAR_SPECIES: ls = new LipidMolecularSpecies(headgroup, fa_list); break;
            case SPECIES: ls = new LipidSpecies(headgroup, fa_list); break;
            default: break;
        }
        return ls;
    }
    
     
    @Override
    public void enterLipid(Shorthand2020Parser.LipidContext node) {
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        adduct = null;
        head_group = "";
        fa_list = new ArrayList<FattyAcid>();
        current_fas = new ExtendedList<FunctionalGroup>();
        headgroup_decorators = new ArrayList<HeadgroupDecorator>();
        tmp = new Dict();
        acer_species = false;
    }
    
    @Override
    public void exitLipid(Shorthand2020Parser.LipidContext node){
        if (acer_species) fa_list.get(0).num_carbon -= 2;
        Headgroup headgroup = prepare_headgroup_and_checks();

        // add count numbers for fatty acyl chains
        int fa_it = (fa_list.size() > 0 && (fa_list.get(0).lipid_FA_bond_type == LipidFaBondType.LCB_EXCEPTION || fa_list.get(0).lipid_FA_bond_type == LipidFaBondType.LCB_REGULAR)) ? 1 : 0;
        for (int it = fa_it; it < fa_list.size(); ++it){
            fa_list.get(it).name += Integer.toString(it + 1);
        }

        LipidAdduct lipid = new LipidAdduct();
        lipid.adduct = adduct;
        lipid.lipid = assemble_lipid(headgroup);

        if (tmp.containsKey("num_ethers")) lipid.lipid.info.num_ethers = (int)tmp.get("num_ethers");

        content = lipid;
    }
    
    
    @Override
    public void enterSl(Shorthand2020Parser.SlContext node){
        tmp.put("sl_hydroxyl", 0);
    }
    
    
    @Override
    public void exitSl(Shorthand2020Parser.SlContext node){
        if (((int)tmp.get("sl_hydroxyl")) == 0 && !head_group.equals("Cer") && !head_group.equals("SPB")){
            set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        }
    }
    
    
    @Override
    public void enterSl_hydroxyl(Shorthand2020Parser.Sl_hydroxylContext node){
        tmp.put("sl_hydroxyl", 1);
    }
    
            
    @Override
    public void enterAdduct_info(Shorthand2020Parser.Adduct_infoContext node){
        adduct = new Adduct("", "", 0, 0);
    }


    @Override
    public void enterAdduct(Shorthand2020Parser.AdductContext node){
        adduct.adduct_string = node.getText();
    }


    @Override
    public void enterCharge(Shorthand2020Parser.ChargeContext node){
        adduct.charge = Integer.valueOf(node.getText());
    }


    @Override
    public void enterCharge_sign(Shorthand2020Parser.Charge_signContext node){
        String sign = node.getText();
        if (sign.equals("+")) adduct.set_charge_sign(1);
        else adduct.set_charge_sign(-1);
    }
    
    
    @Override
    public void enterMed_species(Shorthand2020Parser.Med_speciesContext node){
        set_lipid_level(LipidLevel.SPECIES);
    }
    
    @Override
    public void enterGl_species(Shorthand2020Parser.Gl_speciesContext node){
        set_lipid_level(LipidLevel.SPECIES);
    }
    
    @Override
    public void enterGl_molecular_species(Shorthand2020Parser.Gl_molecular_speciesContext node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }
    
    @Override
    public void enterPl_species(Shorthand2020Parser.Pl_speciesContext node){
        set_lipid_level(LipidLevel.SPECIES);
    }
    
    @Override
    public void enterPl_molecular_species(Shorthand2020Parser.Pl_molecular_speciesContext node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }
    
    @Override
    public void enterSl_species(Shorthand2020Parser.Sl_speciesContext node){
        set_lipid_level(LipidLevel.SPECIES);
    }
    
    @Override
    public void enterPl_single(Shorthand2020Parser.Pl_singleContext node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }
    
    @Override
    public void enterUnsorted_fa_separator(Shorthand2020Parser.Unsorted_fa_separatorContext node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }
    
    @Override
    public void enterEther_num(Shorthand2020Parser.Ether_numContext node){
        int num_ethers = 0;
        String ether = node.getText();
        if (ether.equals("d")) num_ethers = 2;
        else if (ether.equals("t")) num_ethers = 3;
        else if (ether.equals("e")) num_ethers = 4;
        tmp.put("num_ethers", num_ethers);
    }
    
    @Override
    public void enterMed_hg_single(Shorthand2020Parser.Med_hg_singleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterMed_hg_double(Shorthand2020Parser.Med_hg_doubleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterMed_hg_triple(Shorthand2020Parser.Med_hg_tripleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterGl_hg_single(Shorthand2020Parser.Gl_hg_singleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterGl_hg_double(Shorthand2020Parser.Gl_hg_doubleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterGl_hg_true_double(Shorthand2020Parser.Gl_hg_true_doubleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterGl_hg_triple(Shorthand2020Parser.Gl_hg_tripleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterPl_hg_single(Shorthand2020Parser.Pl_hg_singleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterPl_hg_double(Shorthand2020Parser.Pl_hg_doubleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterPl_hg_quadro(Shorthand2020Parser.Pl_hg_quadroContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterSl_hg_single(Shorthand2020Parser.Sl_hg_singleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterPl_hg_double_fa_hg(Shorthand2020Parser.Pl_hg_double_fa_hgContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterSl_hg_double_name(Shorthand2020Parser.Sl_hg_double_nameContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterSt_hg(Shorthand2020Parser.St_hgContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterSt_hg_ester(Shorthand2020Parser.St_hg_esterContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterHg_pip_pure_m(Shorthand2020Parser.Hg_pip_pure_mContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterHg_pip_pure_d(Shorthand2020Parser.Hg_pip_pure_dContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterHg_pip_pure_t(Shorthand2020Parser.Hg_pip_pure_tContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterHg_PE_PS(Shorthand2020Parser.Hg_PE_PSContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterCarbohydrate(Shorthand2020Parser.CarbohydrateContext node){ // _pre_event", set_carbohydrate);
        String carbohydrate = node.getText();
        FunctionalGroup functional_group = null;
        try
        {
            functional_group = KnownFunctionalGroups.get_instance().get(carbohydrate);
        }
        catch (Exception e)
        {
            throw new LipidParsingException("Carbohydrate '" + carbohydrate + "' unknown");
        }

        functional_group.elements.put(Element.O, functional_group.elements.get(Element.O) - 1);
        if (tmp.containsKey("func_group_head") && ((int)tmp.get("func_group_head") == 1)){
            headgroup_decorators.add((HeadgroupDecorator)functional_group);
        }
        else {
            if (!current_fas.back().functional_groups.containsKey(carbohydrate))
            {
                current_fas.back().functional_groups.put(carbohydrate, new ArrayList<FunctionalGroup>());
            }
            current_fas.back().functional_groups.get(carbohydrate).add(functional_group);
        }
    }
    
    @Override
    public void enterCarbohydrate_structural(Shorthand2020Parser.Carbohydrate_structuralContext node){
        set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        tmp.put("func_group_head", 1);
    }
    
    @Override
    public void enterCarbohydrate_isomeric(Shorthand2020Parser.Carbohydrate_isomericContext node){
        tmp.put("func_group_head", 1);
    }
    
    @Override
    public void exitLcb(Shorthand2020Parser.LcbContext node){
        FattyAcid fa = fa_list.get(fa_list.size() - 1);
        fa.name = "LCB";
        fa.set_type(LipidFaBondType.LCB_REGULAR);
    }

    @Override
    public void enterFatty_acyl_chain(Shorthand2020Parser.Fatty_acyl_chainContext node){
        current_fas.add(new FattyAcid("FA"));
        tmp.put(FA_I(), new Dict());
    }

    @Override
    public void exitFatty_acyl_chain(Shorthand2020Parser.Fatty_acyl_chainContext node){
        String fg_i = "fa" + Integer.toString(current_fas.size() - 2);
        String special_type = "";
        if (current_fas.size() >= 2 && tmp.containsKey(fg_i) && ((Dict)tmp.get(fg_i)).containsKey("fg_name")){
            String fg_name = (String)((Dict)tmp.get(fg_i)).get("fg_name");
            if (special_types.contains(fg_name)){
                special_type = fg_name;
            }
        }

        String fa_i = FA_I();
        if (current_fas.back().double_bonds.get_num() != (int)((Dict)tmp.get(fa_i)).get("db_count")){
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }
        else if (current_fas.back().double_bonds.get_num() > 0 && current_fas.back().double_bonds.double_bond_positions.isEmpty()){
            set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        }
        tmp.remove(fa_i);

        FattyAcid fa = (FattyAcid)current_fas.PopBack();
        if (special_type.length() > 0){
            fa.name = special_type;
            if (!current_fas.back().functional_groups.containsKey(special_type)){
                current_fas.back().functional_groups.put(special_type, new ArrayList<FunctionalGroup>());
            }
            current_fas.back().functional_groups.get(special_type).add(fa);
        }
        else{
            fa_list.add(fa);
        }
    }

    @Override
    public void enterCarbon(Shorthand2020Parser.CarbonContext node){
        ((FattyAcid)current_fas.back()).num_carbon = Integer.valueOf(node.getText());
    }

    @Override
    public void enterDb_count(Shorthand2020Parser.Db_countContext node){
        int db_cnt = Integer.valueOf(node.getText());
        ((Dict)tmp.get(FA_I())).put("db_count", db_cnt);
        ((FattyAcid)current_fas.back()).double_bonds.num_double_bonds = db_cnt;
    }

    @Override
    public void enterDb_position_number(Shorthand2020Parser.Db_position_numberContext node){
        ((Dict)tmp.get(FA_I())).put("db_position", Integer.valueOf(node.getText()));
    }

    @Override
    public void enterDb_single_position(Shorthand2020Parser.Db_single_positionContext node){
        String fa_i = FA_I();
        ((Dict)tmp.get(fa_i)).put("db_position", 0);
        ((Dict)tmp.get(fa_i)).put("db_cistrans", "");
    }

    @Override
    public void exitDb_single_position(Shorthand2020Parser.Db_single_positionContext node){
        String fa_i = FA_I();
        Dict d = (Dict)tmp.get(fa_i);
        int pos = (int)d.get("db_position");
        String cistrans = (String)d.get("db_cistrans");

        if (cistrans.equals("")){
            set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        }

        d.remove("db_position");
        d.remove("db_cistrans");
        current_fas.back().double_bonds.double_bond_positions.put(pos, cistrans);
    }

    @Override
    public void enterCistrans(Shorthand2020Parser.CistransContext node){
        ((Dict)tmp.get(FA_I())).put("db_cistrans", node.getText());
    }

    @Override
    public void enterEther_type(Shorthand2020Parser.Ether_typeContext node){
        String ether_type = node.getText();
        if (ether_type.equals("O-")) ((FattyAcid)current_fas.back()).lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMANYL;
        else if (ether_type.equals("P-")) ((FattyAcid)current_fas.back()).lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMENYL;
    }
    
    

    @Override
    public void enterFunc_group_data(Shorthand2020Parser.Func_group_dataContext node){
        String fa_i = FA_I();
        Dict gd = (Dict)tmp.get(fa_i);
        gd.put("fg_pos", -1);
        gd.put("fg_name", "0");
        gd.put("fg_cnt", 1);
        gd.put("fg_stereo", "");
        gd.put("fg_ring_stereo", "");
    }

    @Override
    public void exitFunc_group_data(Shorthand2020Parser.Func_group_dataContext node){
        String fa_i = FA_I();
        Dict gd = (Dict)tmp.get(FA_I());
        String fg_name = (String)gd.get("fg_name");

        if (special_types.contains(fg_name) || fg_name.equals("cy")) return;

        int fg_pos = (int)gd.get("fg_pos");
        int fg_cnt = (int)gd.get("fg_cnt");
        String fg_stereo = (String)gd.get("fg_stereo");
        String fg_ring_stereo = (String)gd.get("fg_ring_stereo");

        if (fg_pos == -1){
            set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        }

        FunctionalGroup functional_group = null;
        try {
            functional_group = KnownFunctionalGroups.get_instance().get(fg_name);
        }
        catch (Exception e) {
            throw new LipidParsingException("'" + fg_name + "' unknown");
        }

        functional_group.position = fg_pos;
        functional_group.count = fg_cnt;
        functional_group.stereochemistry = fg_stereo;
        functional_group.ring_stereo = fg_ring_stereo;

        gd.remove("fg_pos");
        gd.remove("fg_name");
        gd.remove("fg_cnt");
        gd.remove("fg_stereo");

        if (!current_fas.back().functional_groups.containsKey(fg_name)) current_fas.back().functional_groups.put(fg_name, new ArrayList<FunctionalGroup>());
        current_fas.back().functional_groups.get(fg_name).add(functional_group);
    }

    @Override
    public void enterFunc_group_pos_number(Shorthand2020Parser.Func_group_pos_numberContext node){
        ((Dict)tmp.get(FA_I())).put("fg_pos", Integer.valueOf(node.getText()));
    }

    @Override
    public void enterFunc_group_name(Shorthand2020Parser.Func_group_nameContext node){
        ((Dict)tmp.get(FA_I())).put("fg_name", node.getText());
    }

    @Override
    public void enterFunc_group_count(Shorthand2020Parser.Func_group_countContext node){
        ((Dict)tmp.get(FA_I())).put("fg_cnt", Integer.valueOf(node.getText()));
    }

    @Override
    public void enterStereo_type(Shorthand2020Parser.Stereo_typeContext node){
        ((Dict)tmp.get(FA_I())).put("fg_stereo", node.getText());
    }

    @Override
    public void enterMolecular_func_group_name(Shorthand2020Parser.Molecular_func_group_nameContext node){
        ((Dict)tmp.get(FA_I())).put("fg_name", node.getText());
    }
    
    @Override
    public void enterFunc_group_cycle(Shorthand2020Parser.Func_group_cycleContext node){
        ((Dict)tmp.get(FA_I())).put("fg_name", "cy");
        current_fas.add(new Cycle(0));

        String fa_i = FA_I();
        tmp.put(fa_i, new Dict());
        ((Dict)tmp.get(fa_i)).put("cycle_elements", new Lst());
    }

    @Override
    public void exitFunc_group_cycle(Shorthand2020Parser.Func_group_cycleContext node){
        String fa_i = FA_I();
        Lst cycle_elements = (Lst)((Dict)tmp.get(fa_i)).get("cycle_elements");
        Cycle cycle = (Cycle)current_fas.PopBack();
        for (int i = 0; i < cycle_elements.size(); ++i){
            cycle.bridge_chain.add((Element)cycle_elements.get(i));
        }
        ((Dict)tmp.get(fa_i)).remove("cycle_elements");

        if (cycle.start > -1 && cycle.end > -1 && cycle.end - cycle.start + 1 + cycle.bridge_chain.size() < cycle.cycle){
            throw new ConstraintViolationException("Cycle length '" + Integer.toString(cycle.cycle) + "' does not match with cycle description.");
        }
        if (!current_fas.back().functional_groups.containsKey("cy")){
            current_fas.back().functional_groups.put("cy", new ArrayList<FunctionalGroup>());
        }
        current_fas.back().functional_groups.get("cy").add(cycle);
    }

    @Override
    public void enterCycle_start(Shorthand2020Parser.Cycle_startContext node){
        ((Cycle)current_fas.back()).start = Integer.valueOf(node.getText());
    }

    @Override
    public void enterCycle_end(Shorthand2020Parser.Cycle_endContext node){
        ((Cycle)current_fas.back()).end = Integer.valueOf(node.getText());
    }

    @Override
    public void enterCycle_number(Shorthand2020Parser.Cycle_numberContext node){
        ((Cycle)current_fas.back()).cycle = Integer.valueOf(node.getText());
    }

    @Override
    public void enterCycle_db_cnt(Shorthand2020Parser.Cycle_db_cntContext node){
        ((Cycle)current_fas.back()).double_bonds.num_double_bonds = Integer.valueOf(node.getText());
    }

    @Override
    public void enterCycle_db_positions(Shorthand2020Parser.Cycle_db_positionsContext node){
        ((Dict)tmp.get(FA_I())).put("cycle_db", ((Cycle)current_fas.back()).double_bonds.get_num());
    }

    @Override
    public void exitCycle_db_positions(Shorthand2020Parser.Cycle_db_positionsContext node){
        if (((Cycle)current_fas.back()).double_bonds.get_num() != (int)((Dict)tmp.get(FA_I())).get("cycle_db")){
            throw new LipidException("Double bond number in cycle does not correspond to number of double bond positions.");
        }
    }

    @Override
    public void enterCycle_db_position_number(Shorthand2020Parser.Cycle_db_position_numberContext node){
        int pos = Integer.valueOf(node.getText());
        ((Cycle)current_fas.back()).double_bonds.double_bond_positions.put(pos, "");
        ((Dict)tmp.get(FA_I())).put("last_db_pos", pos);
    }

    @Override
    public void enterCycle_db_position_cis_trans(Shorthand2020Parser.Cycle_db_position_cis_transContext node){
        int pos = (int)((Dict)tmp.get(FA_I())).get("last_db_pos");
        ((Cycle)current_fas.back()).double_bonds.double_bond_positions.put(pos, node.getText());
    }

    @Override
    public void enterCylce_element(Shorthand2020Parser.Cylce_elementContext node){
        String element = node.getText();
            
        if (!Elements.element_positions.containsKey(element)){
            throw new LipidParsingException("Element '" + element + "' unknown");
        }

        ((Lst)((Dict)tmp.get(FA_I())).get("cycle_elements")).add(Elements.element_positions.get(element));
    }
    
    @Override
    public void enterFatty_acyl_linkage(Shorthand2020Parser.Fatty_acyl_linkageContext node){
        ((Dict)tmp.get(FA_I())).put("fg_name", "acyl");
        current_fas.add(new AcylAlkylGroup((FattyAcid)null));
        tmp.put(FA_I(), new Dict());
        ((Dict)tmp.get(FA_I())).put("linkage_pos", -1);
    }

    @Override
    public void exitFatty_acyl_linkage(Shorthand2020Parser.Fatty_acyl_linkageContext node){
        boolean linkage_type = (int)((Dict)tmp.get(FA_I())).get("linkage_type") == 1;
        int linkage_pos = (int)((Dict)tmp.get(FA_I())).get("linkage_pos");

        tmp.remove(FA_I());
        AcylAlkylGroup acyl = (AcylAlkylGroup)current_fas.PopBack();

        acyl.position = linkage_pos;
        acyl.set_N_bond_type(linkage_type);
        if (linkage_pos == -1) set_lipid_level(LipidLevel.STRUCTURE_DEFINED);

        if (!current_fas.back().functional_groups.containsKey("acyl")) current_fas.back().functional_groups.put("acyl", new ArrayList<FunctionalGroup>());
        current_fas.back().functional_groups.get("acyl").add(acyl);
    }

    @Override
    public void enterFatty_alkyl_linkage(Shorthand2020Parser.Fatty_alkyl_linkageContext node){
        ((Dict)tmp.get(FA_I())).put("fg_name", "alkyl");
        current_fas.add(new AcylAlkylGroup(null, -1, 1, true));
        tmp.put(FA_I(), new Dict());
        ((Dict)tmp.get(FA_I())).put("linkage_pos", -1);
    }

    @Override
    public void exitFatty_alkyl_linkage(Shorthand2020Parser.Fatty_alkyl_linkageContext node){
        int linkage_pos = (int)((Dict)tmp.get(FA_I())).get("linkage_pos");
        tmp.remove(FA_I());
        AcylAlkylGroup alkyl = (AcylAlkylGroup)current_fas.PopBack();

        alkyl.position = linkage_pos;
        if (linkage_pos == -1) set_lipid_level(LipidLevel.STRUCTURE_DEFINED);

        if (!current_fas.back().functional_groups.containsKey("alkyl")) current_fas.back().functional_groups.put("alkyl", new ArrayList<FunctionalGroup>());
        current_fas.back().functional_groups.get("alkyl").add(alkyl);
    }

    @Override
    public void enterFatty_linkage_number(Shorthand2020Parser.Fatty_linkage_numberContext node){
        ((Dict)tmp.get(FA_I())).put("linkage_pos", Integer.valueOf(node.getText()));
    }

    @Override
    public void enterFatty_acyl_linkage_sign(Shorthand2020Parser.Fatty_acyl_linkage_signContext node){
        ((Dict)tmp.get(FA_I())).put("linkage_type", node.getText().equals("N") ? 1 : 0);
    }

    @Override
    public void enterHydrocarbon_chain(Shorthand2020Parser.Hydrocarbon_chainContext node){
        ((Dict)tmp.get(FA_I())).put("fg_name", "cc");
        current_fas.add(new CarbonChain((FattyAcid)null));
        tmp.put(FA_I(), new Dict());
        ((Dict)tmp.get(FA_I())).put("linkage_pos", -1);
    }

    @Override
    public void exitHydrocarbon_chain(Shorthand2020Parser.Hydrocarbon_chainContext node){
        int linkage_pos = (int)((Dict)tmp.get(FA_I())).get("linkage_pos");
        tmp.remove(FA_I());
        CarbonChain cc = (CarbonChain)current_fas.PopBack();
        cc.position = linkage_pos;
        if (linkage_pos == -1) set_lipid_level(LipidLevel.STRUCTURE_DEFINED);

        if (!current_fas.back().functional_groups.containsKey("cc")) current_fas.back().functional_groups.put("cc", new ArrayList<FunctionalGroup>());
        current_fas.back().functional_groups.get("cc").add(cc);
    }

    @Override
    public void enterHydrocarbon_number(Shorthand2020Parser.Hydrocarbon_numberContext node){
        ((Dict)tmp.get(FA_I())).put("linkage_pos", Integer.valueOf(node.getText()));
    }

    @Override
    public void enterRing_stereo(Shorthand2020Parser.Ring_stereoContext node){
        ((Dict)tmp.get(FA_I())).put("fg_ring_stereo", node.getText());
    }

    @Override
    public void enterPl_hg_fa(Shorthand2020Parser.Pl_hg_faContext node){
        String fa_i = FA_I();
        tmp.put(fa_i, new Dict());
        ((Dict)tmp.get(fa_i)).put("fg_name", "decorator_acyl");
        current_fas.add(new HeadgroupDecorator("decorator_acyl", -1, 1, null, true));
        tmp.put(FA_I(), new Dict());
    }

    @Override
    public void exitPl_hg_fa(Shorthand2020Parser.Pl_hg_faContext node){
        tmp.remove(FA_I());
        headgroup_decorators.add((HeadgroupDecorator)current_fas.PopBack());
        tmp.remove(FA_I());
    }

    @Override
    public void enterPl_hg_alk(Shorthand2020Parser.Pl_hg_alkContext node){
        tmp.put(FA_I(), new Dict());
        ((Dict)tmp.get(FA_I())).put("fg_name", "decorator_alkyl");
        current_fas.add(new HeadgroupDecorator("decorator_alkyl", -1, 1, null, true));
        tmp.put(FA_I(), new Dict());
    }

    @Override
    public void exitPl_hg_alk(Shorthand2020Parser.Pl_hg_alkContext node){
        tmp.remove(FA_I());
        headgroup_decorators.add((HeadgroupDecorator)current_fas.PopBack());
        tmp.remove(FA_I());
    }

    @Override
    public void enterPl_hg_species(Shorthand2020Parser.Pl_hg_speciesContext node){
        set_lipid_level(LipidLevel.SPECIES);
        HeadgroupDecorator hgd = new HeadgroupDecorator("");
        hgd.elements.put(Element.O, hgd.elements.get(Element.O) + 1);
        hgd.elements.put(Element.H, hgd.elements.get(Element.H) - 1);
        headgroup_decorators.add(hgd);
    }

    @Override
    public void enterHg_pip_m(Shorthand2020Parser.Hg_pip_mContext node){
        headgroup_decorators.add(new HeadgroupDecorator(node.getText(), -1, 1, null, true, LipidLevel.MOLECULAR_SPECIES));
    }

    @Override
    public void enterHg_pip_d(Shorthand2020Parser.Hg_pip_dContext node){
        headgroup_decorators.add(new HeadgroupDecorator(node.getText(), -1, 1, null, true, LipidLevel.MOLECULAR_SPECIES));
    }

    @Override
    public void enterHg_pip_t(Shorthand2020Parser.Hg_pip_tContext node){
        headgroup_decorators.add(new HeadgroupDecorator(node.getText(), -1, 1, null, true, LipidLevel.MOLECULAR_SPECIES));
    }

    @Override
    public void enterHg_PE_PS_type(Shorthand2020Parser.Hg_PE_PS_typeContext node){
        headgroup_decorators.add(new HeadgroupDecorator(node.getText(), -1, 1, null, true, LipidLevel.SPECIES));
    }

    @Override
    public void exitAcer_hg(Shorthand2020Parser.Acer_hgContext node){
        head_group = "ACer";
        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true);
        hgd.functional_groups.put("decorator_acyl", new ArrayList<FunctionalGroup>());
        hgd.functional_groups.get("decorator_acyl").add(fa_list.get(fa_list.size() - 1));
        fa_list.remove(fa_list.size() - 1);
        headgroup_decorators.add(hgd);
    }

    @Override
    public void exitAcer_species(Shorthand2020Parser.Acer_speciesContext node){
        head_group = "ACer";
        set_lipid_level(LipidLevel.SPECIES);
        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true);
        hgd.functional_groups.put("decorator_acyl", new ArrayList<FunctionalGroup>());
        hgd.functional_groups.get("decorator_acyl").add(new FattyAcid("FA", 2));
        headgroup_decorators.add(hgd);
        acer_species = true;
    }

}
