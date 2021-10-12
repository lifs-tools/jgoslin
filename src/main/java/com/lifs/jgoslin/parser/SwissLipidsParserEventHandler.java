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

import com.lifs.jgoslin.domain.Dictionary;
import com.lifs.jgoslin.domain.FattyAcid;
import com.lifs.jgoslin.domain.ExtendedList;
import com.lifs.jgoslin.domain.FunctionalGroup;
import com.lifs.jgoslin.domain.Headgroup;
import com.lifs.jgoslin.domain.HeadgroupDecorator;
import com.lifs.jgoslin.domain.KnownFunctionalGroups;
import com.lifs.jgoslin.domain.LipidAdduct;
import com.lifs.jgoslin.domain.LipidException;
import com.lifs.jgoslin.domain.LipidFaBondType;
import com.lifs.jgoslin.domain.LipidLevel;
import com.lifs.jgoslin.domain.LipidParsingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author dominik
 */
public class SwissLipidsParserEventHandler extends LipidBaseParserEventHandler {
    public int db_position;
    public String db_cistrans;
    public int suffix_number;
        
    public SwissLipidsParserEventHandler() {
        try {
            registered_events.put("lipid_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("reset_parser", TreeNode.class));
            registered_events.put("lipid_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("build_lipid", TreeNode.class));
            registered_events.put("fa_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("gl_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("gl_molecular_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("mediator_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("mediator_event", TreeNode.class));
            registered_events.put("gl_mono_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("pl_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("pl_three_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("pl_four_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("sl_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("st_species_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("st_sub1_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("st_sub2_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name_se", TreeNode.class));
            registered_events.put("fa_species_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_species_level", TreeNode.class));
            registered_events.put("gl_molecular_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_level", TreeNode.class));
            registered_events.put("unsorted_fa_separator_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_level", TreeNode.class));
            registered_events.put("fa2_unsorted_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_level", TreeNode.class));
            registered_events.put("fa3_unsorted_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_level", TreeNode.class));
            registered_events.put("fa4_unsorted_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_level", TreeNode.class));
            registered_events.put("db_single_position_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_isomeric_level", TreeNode.class));
            registered_events.put("db_single_position_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_db_position", TreeNode.class));
            registered_events.put("db_position_number_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_db_position_number", TreeNode.class));
            registered_events.put("cistrans_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_cistrans", TreeNode.class));
            registered_events.put("lcb_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("new_lcb", TreeNode.class));
            registered_events.put("lcb_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("clean_lcb", TreeNode.class));
            registered_events.put("fa_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("new_fa", TreeNode.class));
            registered_events.put("fa_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("append_fa", TreeNode.class));
            registered_events.put("ether_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_ether", TreeNode.class));
            registered_events.put("hydroxyl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_hydroxyl", TreeNode.class));
            registered_events.put("db_count_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_double_bonds", TreeNode.class));
            registered_events.put("carbon_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_carbon", TreeNode.class));
            registered_events.put("sl_lcb_species_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_species_level", TreeNode.class));
            registered_events.put("st_species_fa_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_species_fa", TreeNode.class));
            registered_events.put("fa_lcb_suffix_type_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_fa_lcb_suffix_type", TreeNode.class));
            registered_events.put("fa_lcb_suffix_number_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_suffix_number", TreeNode.class));
            registered_events.put("pl_three_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_nape", TreeNode.class));
            
        }
        catch(Exception e){
            throw new LipidParsingException("Cannot initialize LipidMapsParserEventHandler.");
        }
    }
    
    public void reset_parser(TreeNode node){
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        head_group = "";
        lcb = null;
        fa_list = new ArrayList<>();
        current_fa = null;
        use_head_group = false;
        db_position = 0;
        db_cistrans = "";
        headgroup_decorators = new ArrayList<>();
        suffix_number = -1;
    }


    public void set_isomeric_level(TreeNode node){
        db_position = 0;
        db_cistrans = "";
    }


    public void add_db_position(TreeNode node){
        if (current_fa != null){
            current_fa.double_bonds.double_bond_positions.put(db_position, db_cistrans);
            if (!db_cistrans.equals("E") && !db_cistrans.equals("Z")) set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        }
    }


    public void set_nape(TreeNode node){
        head_group = "PE-N";
        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true);
        headgroup_decorators.add(hgd);
        hgd.functional_groups.put("decorator_acyl", new ArrayList<>());
        hgd.functional_groups.get("decorator_acyl").add(fa_list.get(fa_list.size() - 1));
        fa_list.remove(fa_list.size() - 1);
    }


    public void add_db_position_number(TreeNode node){
        db_position = node.get_int();
    }


    public void add_cistrans(TreeNode node){
        db_cistrans = node.get_text();
    }


    public void set_head_group_name(TreeNode node){
        head_group = node.get_text();
    }


    public void set_head_group_name_se(TreeNode node){
        head_group = node.get_text().replace("(", " ");
    }


    public void set_species_level(TreeNode node){
        set_lipid_level(LipidLevel.SPECIES);
    }



    public void set_molecular_level(TreeNode node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }


    public void mediator_event(TreeNode node){
        use_head_group = true;
        head_group = node.get_text();
    }



    public void new_fa(TreeNode node){
        current_fa = new FattyAcid("FA" + (fa_list.size() + 1));
    }



    public void new_lcb(TreeNode node){
        lcb = new FattyAcid("LCB");
        lcb.set_type(LipidFaBondType.LCB_REGULAR);
        current_fa = lcb;
        set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
    }



    public void clean_lcb(TreeNode node){
        if (current_fa.double_bonds.double_bond_positions.isEmpty() && current_fa.double_bonds.get_num() > 0){
            set_lipid_level(LipidLevel.SN_POSITION);
        }
        current_fa = null;
    }



    public void append_fa(TreeNode node){
        if (current_fa.double_bonds.get_num() < 0){
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }

        if (current_fa.double_bonds.double_bond_positions.isEmpty() && current_fa.double_bonds.get_num() > 0){
            set_lipid_level(LipidLevel.SN_POSITION);
        }

        if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level | LipidLevel.SN_POSITION.level)){
                current_fa.position = fa_list.size() + 1;
        }

        fa_list.add(current_fa);
        current_fa = null;
    }



    public void build_lipid(TreeNode node){
        if (lcb != null)
        {
            for (FattyAcid fa : fa_list) fa.position += 1;
            fa_list.add(0, lcb);
        }

        Headgroup headgroup = prepare_headgroup_and_checks();
        content = new LipidAdduct();
        content.lipid = assemble_lipid(headgroup);
    }



    public void add_ether(TreeNode node){
        String ether = node.get_text();
        if (ether.equals("O-")) current_fa.lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMANYL;
        else if (ether.equals("P-")) current_fa.lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMENYL;
    }



    public void add_hydroxyl(TreeNode node){
        String old_hydroxyl = node.get_text();
        int num_h = 0;
        if (old_hydroxyl.equals("m")) num_h = 1;
        else if (old_hydroxyl.equals("d")) num_h = 2;
        else if (old_hydroxyl.equals("t")) num_h = 3;


        if (sp_regular_lcb()) num_h -= 1;
        FunctionalGroup functional_group = KnownFunctionalGroups.get_instance().get("OH");
        functional_group.count = num_h;
        if (!current_fa.functional_groups.containsKey("OH")) current_fa.functional_groups.put("OH", new ArrayList<>());
        current_fa.functional_groups.get("OH").add(functional_group);
    }


    public void add_one_hydroxyl(TreeNode node){
        if (!current_fa.functional_groups.containsKey("OH") && current_fa.functional_groups.get("OH").get(0).position == -1){
            current_fa.functional_groups.get("OH").get(0).count += 1;
        }
        else {
            FunctionalGroup functional_group = KnownFunctionalGroups.get_instance().get("OH");
            if (!current_fa.functional_groups.containsKey("OH")) current_fa.functional_groups.put("OH", new ArrayList<>());
            current_fa.functional_groups.get("OH").add(functional_group);
        }
    }



    public void add_suffix_number(TreeNode node){
        suffix_number = node.get_int();
    }



    public void add_fa_lcb_suffix_type(TreeNode node){
        String suffix_type = node.get_text();
        if (suffix_type.equals("me")){
            suffix_type = "Me";
            current_fa.num_carbon -= 1;
        }

        FunctionalGroup functional_group = KnownFunctionalGroups.get_instance().get(suffix_type);
        functional_group.position = suffix_number;
        if (functional_group.position == -1) set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        if (!current_fa.functional_groups.containsKey(suffix_type)) current_fa.functional_groups.put(suffix_type, new ArrayList<>());
        current_fa.functional_groups.get(suffix_type).add(functional_group);

        suffix_number = -1;
    }



    public void add_double_bonds(TreeNode node){
        current_fa.double_bonds.num_double_bonds += node.get_int();
    }



    public void add_carbon(TreeNode node){
        current_fa.num_carbon = node.get_int();
    }



    public void set_species_fa(TreeNode node){
        head_group += " 27:1";
        fa_list.get(fa_list.size() - 1).num_carbon -= 27;
        fa_list.get(fa_list.size() - 1).double_bonds.num_double_bonds -= 1;
    }
}
