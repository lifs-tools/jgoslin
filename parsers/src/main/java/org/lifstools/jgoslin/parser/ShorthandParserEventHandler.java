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
    
    public ArrayDeque<FunctionalGroup> current_fas;
    public Dictionary tmp = new Dictionary();
    public boolean acer_species = false;
    public static final HashSet<String> special_types = new HashSet<String>(Arrays.asList("acyl", "alkyl", "decorator_acyl", "decorator_alkyl", "cc"));
        
    public ShorthandParserEventHandler() {
        this(new KnownFunctionalGroups());
    }
    
    public ShorthandParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        this.knownFunctionalGroups = knownFunctionalGroups;
        try {
            registered_events.put("lipid_pre_event", this::reset_parser);
            registered_events.put("lipid_post_event", this::build_lipid);

            // set categories
            registered_events.put("sl_pre_event", this::pre_sphingolipid);
            registered_events.put("sl_post_event", this::post_sphingolipid);
            registered_events.put("sl_hydroxyl_pre_event", this::set_hydroxyl);

            // set adduct events
            registered_events.put("adduct_info_pre_event", this::new_adduct);
            registered_events.put("adduct_pre_event", this::add_adduct);
            registered_events.put("charge_pre_event", this::add_charge);
            registered_events.put("charge_sign_pre_event", this::add_charge_sign);

            // set species events
            registered_events.put("med_species_pre_event", this::set_species_level);
            registered_events.put("gl_species_pre_event", this::set_species_level);
            registered_events.put("gl_molecular_species_pre_event", this::set_molecular_level);
            registered_events.put("pl_species_pre_event", this::set_species_level);
            registered_events.put("pl_molecular_species_pre_event", this::set_molecular_level);
            registered_events.put("sl_species_pre_event", this::set_species_level);
            registered_events.put("pl_single_pre_event", this::set_molecular_level);
            registered_events.put("unsorted_fa_separator_pre_event", this::set_molecular_level);
            registered_events.put("ether_num_pre_event", this::set_ether_num);

            // set head groups events
            registered_events.put("med_hg_single_pre_event", this::set_headgroup_name);
            registered_events.put("med_hg_double_pre_event", this::set_headgroup_name);
            registered_events.put("med_hg_triple_pre_event", this::set_headgroup_name);
            registered_events.put("gl_hg_single_pre_event", this::set_headgroup_name);
            registered_events.put("gl_hg_double_pre_event", this::set_headgroup_name);
            registered_events.put("gl_hg_true_double_pre_event", this::set_headgroup_name);
            registered_events.put("gl_hg_triple_pre_event", this::set_headgroup_name);
            registered_events.put("pl_hg_single_pre_event", this::set_headgroup_name);
            registered_events.put("pl_hg_double_pre_event", this::set_headgroup_name);
            registered_events.put("pl_hg_quadro_pre_event", this::set_headgroup_name);
            registered_events.put("sl_hg_single_pre_event", this::set_headgroup_name);
            registered_events.put("pl_hg_double_fa_hg_pre_event", this::set_headgroup_name);
            registered_events.put("sl_hg_double_name_pre_event", this::set_headgroup_name);
            registered_events.put("st_hg_pre_event", this::set_headgroup_name);
            registered_events.put("st_hg_ester_pre_event", this::set_headgroup_name);
            registered_events.put("hg_pip_pure_m_pre_event", this::set_headgroup_name);
            registered_events.put("hg_pip_pure_d_pre_event", this::set_headgroup_name);
            registered_events.put("hg_pip_pure_t_pre_event", this::set_headgroup_name);
            registered_events.put("hg_PE_PS_pre_event", this::set_headgroup_name);

            // set head group headgroup_decorators
            registered_events.put("carbohydrate_pre_event", this::set_carbohydrate);
            registered_events.put("carbohydrate_structural_pre_event", this::set_carbohydrate_structural);
            registered_events.put("carbohydrate_isomeric_pre_event", this::set_carbohydrate_isomeric);

            // fatty acyl events
            registered_events.put("lcb_post_event", this::set_lcb);
            registered_events.put("fatty_acyl_chain_pre_event", this::new_fatty_acyl_chain);
            registered_events.put("fatty_acyl_chain_post_event", this::add_fatty_acyl_chain);
            registered_events.put("carbon_pre_event", this::set_carbon);
            registered_events.put("db_count_pre_event", this::set_double_bond_count);
            registered_events.put("db_position_number_pre_event", this::set_double_bond_position);
            registered_events.put("db_single_position_pre_event", this::set_double_bond_information);
            registered_events.put("db_single_position_post_event", this::add_double_bond_information);
            registered_events.put("cistrans_pre_event", this::set_cistrans);
            registered_events.put("ether_type_pre_event", this::set_ether_type);

            // set functional group events
            registered_events.put("func_group_data_pre_event", this::set_functional_group);
            registered_events.put("func_group_data_post_event", this::add_functional_group);
            registered_events.put("func_group_pos_number_pre_event", this::set_functional_group_position);
            registered_events.put("func_group_name_pre_event", this::set_functional_group_name);
            registered_events.put("func_group_count_pre_event", this::set_functional_group_count);
            registered_events.put("stereo_type_pre_event", this::set_functional_group_stereo);
            registered_events.put("molecular_func_group_name_pre_event", this::set_molecular_func_group);

            // set cycle events
            registered_events.put("func_group_cycle_pre_event", this::set_cycle);
            registered_events.put("func_group_cycle_post_event", this::add_cycle);
            registered_events.put("cycle_start_pre_event", this::set_cycle_start);
            registered_events.put("cycle_end_pre_event", this::set_cycle_end);
            registered_events.put("cycle_number_pre_event", this::set_cycle_number);
            registered_events.put("cycle_db_cnt_pre_event", this::set_cycle_db_count);
            registered_events.put("cycle_db_positions_pre_event", this::set_cycle_db_positions);
            registered_events.put("cycle_db_positions_post_event", this::check_cycle_db_positions);
            registered_events.put("cycle_db_position_number_pre_event", this::set_cycle_db_position);
            registered_events.put("cycle_db_position_cis_trans_pre_event", this::set_cycle_db_position_cistrans);
            registered_events.put("cylce_element_pre_event", this::add_cycle_element);

            // set linkage events
            registered_events.put("fatty_acyl_linkage_pre_event", this::set_acyl_linkage);
            registered_events.put("fatty_acyl_linkage_post_event", this::add_acyl_linkage);
            registered_events.put("fatty_alkyl_linkage_pre_event", this::set_alkyl_linkage);
            registered_events.put("fatty_alkyl_linkage_post_event", this::add_alkyl_linkage);
            registered_events.put("fatty_linkage_number_pre_event", this::set_fatty_linkage_number);
            registered_events.put("fatty_acyl_linkage_sign_pre_event", this::set_linkage_type);
            registered_events.put("hydrocarbon_chain_pre_event", this::set_hydrocarbon_chain);
            registered_events.put("hydrocarbon_chain_post_event", this::add_hydrocarbon_chain);
            registered_events.put("hydrocarbon_number_pre_event", this::set_fatty_linkage_number);

            // set remaining events
            registered_events.put("ring_stereo_pre_event", this::set_ring_stereo);
            registered_events.put("pl_hg_fa_pre_event", this::set_hg_acyl);
            registered_events.put("pl_hg_fa_post_event", this::add_hg_acyl);
            registered_events.put("pl_hg_alk_pre_event", this::set_hg_alkyl);
            registered_events.put("pl_hg_alk_post_event", this::add_hg_alkyl);
            registered_events.put("pl_hg_species_pre_event", this::add_pl_species_data);
            registered_events.put("hg_pip_m_pre_event", this::suffix_decorator_molecular);
            registered_events.put("hg_pip_d_pre_event", this::suffix_decorator_molecular);
            registered_events.put("hg_pip_t_pre_event", this::suffix_decorator_molecular);
            registered_events.put("hg_PE_PS_type_pre_event", this::suffix_decorator_species);
            registered_events.put("acer_hg_post_event", this::set_acer);
            registered_events.put("acer_species_post_event", this::set_acer_species);
            
            
        }
        catch(Exception e){
            throw new LipidParsingException("Cannot initialize ShorthandParserEventHandler.");
        }
    }   
        
    public void reset_parser(TreeNode node){
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        adduct = null;
        head_group = "";
        fa_list = new ArrayList<>();
        current_fas = new ArrayDeque<>();
        headgroup_decorators = new ArrayList<>();
        tmp = new Dictionary();
        acer_species = false;
    }    


    public String FA_I(){
        return "fa" + Integer.toString(current_fas.size());
    }
    
    
    public void build_lipid(TreeNode node){
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
        lipid.adduct = adduct;

        if (tmp.containsKey("num_ethers")) lipid.lipid.info.num_ethers = (int)tmp.get("num_ethers");

        content = lipid;
    }
    
    
    
    public void pre_sphingolipid(TreeNode node){
        tmp.put("sl_hydroxyl", 0);
    }
    
    
    
    public void post_sphingolipid(TreeNode node){
        if (((int)tmp.get("sl_hydroxyl")) == 0 && !head_group.equals("Cer") && !head_group.equals("SPB")){
            set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        }
    }
    
    
    
    public void set_hydroxyl(TreeNode node){
        tmp.put("sl_hydroxyl", 1);
    }
    
            
    
    public void new_adduct(TreeNode node){
        adduct = new Adduct("", "", 0, 0);
    }


    
    public void add_adduct(TreeNode node){
        adduct.adduct_string = node.get_text();
    }


    
    public void add_charge(TreeNode node){
        adduct.charge = Integer.valueOf(node.get_text());
    }


    
    public void add_charge_sign(TreeNode node){
        String sign = node.get_text();
        if (sign.equals("+")) adduct.set_charge_sign(1);
        else adduct.set_charge_sign(-1);
    }
    
    
    
    public void set_species_level(TreeNode node){
        set_lipid_level(LipidLevel.SPECIES);
    }
    
    
    public void set_molecular_level(TreeNode node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }
    
    
    public void set_ether_num(TreeNode node){
        int num_ethers = 0;
        String ether = node.get_text();
        switch (ether) {
            case "d": num_ethers = 2; break;
            case "t": num_ethers = 3; break;
            case "e": num_ethers = 4; break;
            default: break;
        }
        tmp.put("num_ethers", num_ethers);
    }
    
    
    public void set_headgroup_name(TreeNode node){
        if (head_group.length() == 0) head_group = node.get_text();
    }
    
    
    public void set_carbohydrate(TreeNode node){
        String carbohydrate = node.get_text();
        FunctionalGroup functional_group = null;
        try
        {
            functional_group = knownFunctionalGroups.get(carbohydrate);
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
            if (!current_fas.peekLast().functional_groups.containsKey(carbohydrate))
            {
                current_fas.peekLast().functional_groups.put(carbohydrate, new ArrayList<>());
            }
            current_fas.peekLast().functional_groups.get(carbohydrate).add(functional_group);
        }
    }
    
    
    public void set_carbohydrate_structural(TreeNode node){
        set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        tmp.put("func_group_head", 1);
    }
    
    
    public void set_carbohydrate_isomeric(TreeNode node){
        tmp.put("func_group_head", 1);
    }
    
    
    public void set_lcb(TreeNode node){
        FattyAcid fa = fa_list.get(fa_list.size() - 1);
        fa.name = "LCB";
        fa.set_type(LipidFaBondType.LCB_REGULAR);
    }

    
    public void new_fatty_acyl_chain(TreeNode node){
        current_fas.add(new FattyAcid("FA", knownFunctionalGroups));
        tmp.put(FA_I(), new Dictionary());
    }

    
    public void add_fatty_acyl_chain(TreeNode node){
        String fg_i = "fa" + Integer.toString(current_fas.size() - 2);
        String special_type = "";
        if (current_fas.size() >= 2 && tmp.containsKey(fg_i) && ((Dictionary)tmp.get(fg_i)).containsKey("fg_name")){
            String fg_name = (String)((Dictionary)tmp.get(fg_i)).get("fg_name");
            if (special_types.contains(fg_name)){
                special_type = fg_name;
            }
        }

        String fa_i = FA_I();
        if (current_fas.peekLast().double_bonds.get_num() != (int)((Dictionary)tmp.get(fa_i)).get("db_count")){
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }
        else if (current_fas.peekLast().double_bonds.get_num() > 0 && current_fas.peekLast().double_bonds.double_bond_positions.isEmpty()){
            set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        }
        tmp.remove(fa_i);

        FattyAcid fa = (FattyAcid)current_fas.pollLast();
        if (special_type.length() > 0){
            fa.name = special_type;
            if (!current_fas.peekLast().functional_groups.containsKey(special_type)){
                current_fas.peekLast().functional_groups.put(special_type, new ArrayList<>());
            }
            current_fas.peekLast().functional_groups.get(special_type).add(fa);
        }
        else{
            fa_list.add(fa);
        }
    }

    
    public void set_carbon(TreeNode node){
        ((FattyAcid)current_fas.peekLast()).num_carbon = Integer.valueOf(node.get_text());
    }

    
    public void set_double_bond_count(TreeNode node){
        int db_cnt = Integer.valueOf(node.get_text());
        ((Dictionary)tmp.get(FA_I())).put("db_count", db_cnt);
        ((FattyAcid)current_fas.peekLast()).double_bonds.num_double_bonds = db_cnt;
    }

    
    public void set_double_bond_position(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("db_position", Integer.valueOf(node.get_text()));
    }

    
    public void set_double_bond_information(TreeNode node){
        String fa_i = FA_I();
        ((Dictionary)tmp.get(fa_i)).put("db_position", 0);
        ((Dictionary)tmp.get(fa_i)).put("db_cistrans", "");
    }

    
    public void add_double_bond_information(TreeNode node){
        String fa_i = FA_I();
        Dictionary d = (Dictionary)tmp.get(fa_i);
        int pos = (int)d.get("db_position");
        String cistrans = (String)d.get("db_cistrans");

        if (cistrans.equals("")){
            set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        }

        d.remove("db_position");
        d.remove("db_cistrans");
        current_fas.peekLast().double_bonds.double_bond_positions.put(pos, cistrans);
    }

    
    public void set_cistrans(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("db_cistrans", node.get_text());
    }
    

    
    public void set_ether_type(TreeNode node){
        String ether_type = node.get_text();
        if (ether_type.equals("O-")) ((FattyAcid)current_fas.peekLast()).lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMANYL;
        else if (ether_type.equals("P-")) ((FattyAcid)current_fas.peekLast()).lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMENYL;
    }
    
    

    
    public void set_functional_group(TreeNode node){
        String fa_i = FA_I();
        Dictionary gd = (Dictionary)tmp.get(fa_i);
        gd.put("fg_pos", -1);
        gd.put("fg_name", "0");
        gd.put("fg_cnt", 1);
        gd.put("fg_stereo", "");
        gd.put("fg_ring_stereo", "");
    }

    
    public void add_functional_group(TreeNode node){
        Dictionary gd = (Dictionary)tmp.get(FA_I());
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
            // FIXME please do not use static singletons, this usually does not pay off in Java and introduces memory leaks
            functional_group = knownFunctionalGroups.get(fg_name);
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

        if (!current_fas.peekLast().functional_groups.containsKey(fg_name)) current_fas.peekLast().functional_groups.put(fg_name, new ArrayList<>());
        current_fas.peekLast().functional_groups.get(fg_name).add(functional_group);
    }

    
    public void set_functional_group_position(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("fg_pos", Integer.valueOf(node.get_text()));
    }

    
    public void set_functional_group_name(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("fg_name", node.get_text());
    }

    
    public void set_functional_group_count(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("fg_cnt", Integer.valueOf(node.get_text()));
    }

    
    public void set_functional_group_stereo(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("fg_stereo", node.get_text());
    }

    
    public void set_molecular_func_group(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("fg_name", node.get_text());
    }
    
    
    public void set_cycle(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("fg_name", "cy");
        current_fas.add(new Cycle(0, knownFunctionalGroups));

        String fa_i = FA_I();
        tmp.put(fa_i, new Dictionary());
        ((Dictionary)tmp.get(fa_i)).put("cycle_elements", new GenericList());
    }

    
    public void add_cycle(TreeNode node){
        String fa_i = FA_I();
        GenericList cycle_elements = (GenericList)((Dictionary)tmp.get(fa_i)).get("cycle_elements");
        Cycle cycle = (Cycle)current_fas.pollLast();
        for (int i = 0; i < cycle_elements.size(); ++i){
            cycle.bridge_chain.add((Element)cycle_elements.get(i));
        }
        ((Dictionary)tmp.get(fa_i)).remove("cycle_elements");

        if (cycle.start > -1 && cycle.end > -1 && cycle.end - cycle.start + 1 + cycle.bridge_chain.size() < cycle.cycle){
            throw new ConstraintViolationException("Cycle length '" + Integer.toString(cycle.cycle) + "' does not match with cycle description.");
        }
        if (!current_fas.peekLast().functional_groups.containsKey("cy")){
            current_fas.peekLast().functional_groups.put("cy", new ArrayList<FunctionalGroup>());
        }
        current_fas.peekLast().functional_groups.get("cy").add(cycle);
    }

    
    public void set_cycle_start(TreeNode node){
        ((Cycle)current_fas.peekLast()).start = Integer.valueOf(node.get_text());
    }

    
    public void set_cycle_end(TreeNode node){
        ((Cycle)current_fas.peekLast()).end = Integer.valueOf(node.get_text());
    }

    
    public void set_cycle_number(TreeNode node){
        ((Cycle)current_fas.peekLast()).cycle = Integer.valueOf(node.get_text());
    }

    
    public void set_cycle_db_count(TreeNode node){
        ((Cycle)current_fas.peekLast()).double_bonds.num_double_bonds = Integer.valueOf(node.get_text());
    }

    
    public void set_cycle_db_positions(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("cycle_db", ((Cycle)current_fas.peekLast()).double_bonds.get_num());
    }

    
    public void check_cycle_db_positions(TreeNode node){
        if (((Cycle)current_fas.peekLast()).double_bonds.get_num() != (int)((Dictionary)tmp.get(FA_I())).get("cycle_db")){
            throw new LipidException("Double bond number in cycle does not correspond to number of double bond positions.");
        }
    }

    
    public void set_cycle_db_position(TreeNode node){
        int pos = Integer.valueOf(node.get_text());
        ((Cycle)current_fas.peekLast()).double_bonds.double_bond_positions.put(pos, "");
        ((Dictionary)tmp.get(FA_I())).put("last_db_pos", pos);
    }

    
    public void set_cycle_db_position_cistrans(TreeNode node){
        int pos = (int)((Dictionary)tmp.get(FA_I())).get("last_db_pos");
        ((Cycle)current_fas.peekLast()).double_bonds.double_bond_positions.put(pos, node.get_text());
    }

    
    public void add_cycle_element(TreeNode node){
        String element = node.get_text();
            
        if (!Elements.element_positions.containsKey(element)){
            throw new LipidParsingException("Element '" + element + "' unknown");
        }

        ((GenericList)((Dictionary)tmp.get(FA_I())).get("cycle_elements")).add(Elements.element_positions.get(element));
    }
    
    
    public void set_acyl_linkage(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("fg_name", "acyl");
        current_fas.add(new AcylAlkylGroup((FattyAcid)null, knownFunctionalGroups));
        tmp.put(FA_I(), new Dictionary());
        ((Dictionary)tmp.get(FA_I())).put("linkage_pos", -1);
    }

    
    public void add_acyl_linkage(TreeNode node){
        boolean linkage_type = (int)((Dictionary)tmp.get(FA_I())).get("linkage_type") == 1;
        int linkage_pos = (int)((Dictionary)tmp.get(FA_I())).get("linkage_pos");

        tmp.remove(FA_I());
        AcylAlkylGroup acyl = (AcylAlkylGroup)current_fas.pollLast();

        acyl.position = linkage_pos;
        acyl.set_N_bond_type(linkage_type);
        if (linkage_pos == -1) set_lipid_level(LipidLevel.STRUCTURE_DEFINED);

        if (!current_fas.peekLast().functional_groups.containsKey("acyl")) current_fas.peekLast().functional_groups.put("acyl", new ArrayList<FunctionalGroup>());
        current_fas.peekLast().functional_groups.get("acyl").add(acyl);
    }

    
    public void set_alkyl_linkage(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("fg_name", "alkyl");
        current_fas.add(new AcylAlkylGroup(null, -1, 1, true, knownFunctionalGroups));
        tmp.put(FA_I(), new Dictionary());
        ((Dictionary)tmp.get(FA_I())).put("linkage_pos", -1);
    }

    
    public void add_alkyl_linkage(TreeNode node){
        int linkage_pos = (int)((Dictionary)tmp.get(FA_I())).get("linkage_pos");
        tmp.remove(FA_I());
        AcylAlkylGroup alkyl = (AcylAlkylGroup)current_fas.pollLast();

        alkyl.position = linkage_pos;
        if (linkage_pos == -1) set_lipid_level(LipidLevel.STRUCTURE_DEFINED);

        if (!current_fas.peekLast().functional_groups.containsKey("alkyl")) current_fas.peekLast().functional_groups.put("alkyl", new ArrayList<FunctionalGroup>());
        current_fas.peekLast().functional_groups.get("alkyl").add(alkyl);
    }

    
    public void set_fatty_linkage_number(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("linkage_pos", Integer.valueOf(node.get_text()));
    }

    
    public void set_linkage_type(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("linkage_type", node.get_text().equals("N") ? 1 : 0);
    }

    
    public void set_hydrocarbon_chain(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("fg_name", "cc");
        current_fas.add(new CarbonChain((FattyAcid)null, knownFunctionalGroups));
        tmp.put(FA_I(), new Dictionary());
        ((Dictionary)tmp.get(FA_I())).put("linkage_pos", -1);
    }

    
    public void add_hydrocarbon_chain(TreeNode node){
        int linkage_pos = (int)((Dictionary)tmp.get(FA_I())).get("linkage_pos");
        tmp.remove(FA_I());
        CarbonChain cc = (CarbonChain)current_fas.pollLast();
        cc.position = linkage_pos;
        if (linkage_pos == -1) set_lipid_level(LipidLevel.STRUCTURE_DEFINED);

        if (!current_fas.peekLast().functional_groups.containsKey("cc")) current_fas.peekLast().functional_groups.put("cc", new ArrayList<FunctionalGroup>());
        current_fas.peekLast().functional_groups.get("cc").add(cc);
    }

    
    public void set_ring_stereo(TreeNode node){
        ((Dictionary)tmp.get(FA_I())).put("fg_ring_stereo", node.get_text());
    }

    
    public void set_hg_acyl(TreeNode node){
        String fa_i = FA_I();
        tmp.put(fa_i, new Dictionary());
        ((Dictionary)tmp.get(fa_i)).put("fg_name", "decorator_acyl");
        current_fas.add(new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups));
        tmp.put(FA_I(), new Dictionary());
    }

    
    public void add_hg_acyl(TreeNode node){
        tmp.remove(FA_I());
        headgroup_decorators.add((HeadgroupDecorator)current_fas.pollLast());
        tmp.remove(FA_I());
    }

    
    public void set_hg_alkyl(TreeNode node){
        tmp.put(FA_I(), new Dictionary());
        ((Dictionary)tmp.get(FA_I())).put("fg_name", "decorator_alkyl");
        current_fas.add(new HeadgroupDecorator("decorator_alkyl", -1, 1, null, true, knownFunctionalGroups));
        tmp.put(FA_I(), new Dictionary());
    }

    
    public void add_hg_alkyl(TreeNode node){
        tmp.remove(FA_I());
        headgroup_decorators.add((HeadgroupDecorator)current_fas.pollLast());
        tmp.remove(FA_I());
    }

    
    public void add_pl_species_data(TreeNode node){
        set_lipid_level(LipidLevel.SPECIES);
        HeadgroupDecorator hgd = new HeadgroupDecorator("", knownFunctionalGroups);
        hgd.elements.put(Element.O, hgd.elements.get(Element.O) + 1);
        hgd.elements.put(Element.H, hgd.elements.get(Element.H) - 1);
        headgroup_decorators.add(hgd);
    }

    
    public void suffix_decorator_molecular(TreeNode node){
        headgroup_decorators.add(new HeadgroupDecorator(node.get_text(), -1, 1, null, true, LipidLevel.MOLECULAR_SPECIES, knownFunctionalGroups));
    }

    
    public void suffix_decorator_species(TreeNode node){
        headgroup_decorators.add(new HeadgroupDecorator(node.get_text(), -1, 1, null, true, LipidLevel.SPECIES, knownFunctionalGroups));
    }

    
    public void set_acer(TreeNode node){
        head_group = "ACer";
        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups);
        hgd.functional_groups.put("decorator_acyl", new ArrayList<>());
        hgd.functional_groups.get("decorator_acyl").add(fa_list.get(fa_list.size() - 1));
        fa_list.remove(fa_list.size() - 1);
        headgroup_decorators.add(hgd);
    }

    
    public void set_acer_species(TreeNode node){
        head_group = "ACer";
        set_lipid_level(LipidLevel.SPECIES);
        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups);
        hgd.functional_groups.put("decorator_acyl", new ArrayList<>());
        hgd.functional_groups.get("decorator_acyl").add(new FattyAcid("FA", 2, knownFunctionalGroups));
        headgroup_decorators.add(hgd);
        acer_species = true;
    }
}