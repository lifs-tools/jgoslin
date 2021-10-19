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

    private int db_position;
    private String db_cistrans;
    private boolean unspecified_ether;
    
    public GoslinParserEventHandler() {
        this(new KnownFunctionalGroups());
    }
    
    public GoslinParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        this.knownFunctionalGroups = knownFunctionalGroups;
        try {
            registered_events.put("lipid_pre_event", this::reset_parser);
            registered_events.put("lipid_post_event", this::build_lipid);
            
            registered_events.put("hg_cl_pre_event", this::set_head_group_name);
            registered_events.put("hg_mlcl_pre_event", this::set_head_group_name);
            registered_events.put("hg_pl_pre_event", this::set_head_group_name);
            registered_events.put("hg_lpl_pre_event", this::set_head_group_name);
            registered_events.put("hg_lpl_o_pre_event", this::set_head_group_name);
            registered_events.put("hg_pl_o_pre_event", this::set_head_group_name);
            registered_events.put("hg_lsl_pre_event", this::set_head_group_name);
            registered_events.put("hg_dsl_pre_event", this::set_head_group_name);
            registered_events.put("st_pre_event", this::set_head_group_name);
            registered_events.put("hg_ste_pre_event", this::set_head_group_name);
            registered_events.put("hg_stes_pre_event", this::set_head_group_name);
            registered_events.put("mediator_pre_event", this::set_head_group_name);
            registered_events.put("hg_mgl_pre_event", this::set_head_group_name);
            registered_events.put("hg_dgl_pre_event", this::set_head_group_name);
            registered_events.put("hg_sgl_pre_event", this::set_head_group_name);
            registered_events.put("hg_tgl_pre_event", this::set_head_group_name);
            registered_events.put("hg_dlcl_pre_event", this::set_head_group_name);
            registered_events.put("hg_sac_di_pre_event", this::set_head_group_name);
            registered_events.put("hg_sac_f_pre_event", this::set_head_group_name);
            registered_events.put("hg_tpl_pre_event", this::set_head_group_name);  
            
            registered_events.put("gl_species_pre_event", this::set_species_level);
            registered_events.put("pl_species_pre_event", this::set_species_level);
            registered_events.put("sl_species_pre_event", this::set_species_level);
            registered_events.put("fa2_unsorted_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("fa3_unsorted_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("fa4_unsorted_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("slbpa_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("dlcl_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("mlcl_pre_event", this::set_molecular_subspecies_level);
            
            
            registered_events.put("lcb_pre_event", this::new_lcb);
            registered_events.put("lcb_post_event", this::clean_lcb);
            registered_events.put("fa_pre_event", this::new_fa);
            registered_events.put("fa_post_event", this::append_fa);
            
            registered_events.put("db_single_position_pre_event", this::set_isomeric_level);
            registered_events.put("db_single_position_post_event", this::add_db_position);
            registered_events.put("db_position_number_pre_event", this::add_db_position_number);
            registered_events.put("cistrans_pre_event", this::add_cistrans);
            
            
            registered_events.put("ether_pre_event", this::add_ether);
            registered_events.put("old_hydroxyl_pre_event", this::add_old_hydroxyl);
            registered_events.put("db_count_pre_event", this::add_double_bonds);
            registered_events.put("carbon_pre_event", this::add_carbon);
            registered_events.put("hydroxyl_pre_event", this::add_hydroxyl);
            
            
            registered_events.put("adduct_info_pre_event", this::new_adduct);
            registered_events.put("adduct_pre_event", this::add_adduct);
            registered_events.put("charge_pre_event", this::add_charge);
            registered_events.put("charge_sign_pre_event", this::add_charge_sign);
            
            
            registered_events.put("lpl_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("lpl_o_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("hg_lpl_oc_pre_event", this::set_unspecified_ether);
            registered_events.put("hg_pl_oc_pre_event", this::set_unspecified_ether);
            
        }
        catch(Exception e){
            throw new LipidParsingException("Cannot initialize GoslinParserEventHandler");
        }
    }   
        
    public void reset_parser(TreeNode node){
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        head_group = "";
        lcb = null;
        fa_list.clear();
        current_fa = null;
        adduct = null;
        db_position = 0;
        db_cistrans = "";
        unspecified_ether = false;
    }    

 
    public void set_unspecified_ether(TreeNode node){
        unspecified_ether = true;
    }

    public void set_head_group_name(TreeNode node){
        head_group = node.get_text();
    }


    public void set_species_level(TreeNode node){
        set_lipid_level(LipidLevel.SPECIES);
    }


    public void set_isomeric_level(TreeNode node){
        db_position = 0;
        db_cistrans = "";
    }


    public void add_db_position(TreeNode node){
        if (current_fa != null)
        {
            current_fa.double_bonds.double_bond_positions.put(db_position, db_cistrans);
            if (!db_cistrans.equals("E") && !db_cistrans.equals("Z")) set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        }
    }


    public void add_db_position_number(TreeNode node)
    {
        db_position = Integer.valueOf(node.get_text());
    }


    public void add_cistrans(TreeNode node){
        db_cistrans = node.get_text();
    }



    public void set_molecular_subspecies_level(TreeNode node)
    {
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }


    public void new_fa(TreeNode node)
    {
        LipidFaBondType lipid_FA_bond_type = LipidFaBondType.ESTER;
        if (unspecified_ether)
        {
            unspecified_ether = false;
            lipid_FA_bond_type = LipidFaBondType.ETHER_UNSPECIFIED;
        }
        current_fa = new FattyAcid("FA" + (fa_list.size() + 1), 2, null, null, lipid_FA_bond_type, knownFunctionalGroups);
    }



    public void new_lcb(TreeNode node){
        lcb = new FattyAcid("LCB", knownFunctionalGroups);
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
        if (current_fa.lipid_FA_bond_type == LipidFaBondType.ETHER_UNSPECIFIED){
            throw new LipidException("Lipid with unspecified ether bond cannot be treated properly.");
        }
        if (current_fa.double_bonds.double_bond_positions.isEmpty() && current_fa.double_bonds.get_num() > 0){
            set_lipid_level(LipidLevel.SN_POSITION);
        }


        if (current_fa.double_bonds.get_num() < 0){
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }

        if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level| LipidLevel.STRUCTURE_DEFINED.level | LipidLevel.SN_POSITION.level)){
                current_fa.position = fa_list.size() + 1;
        }


        fa_list.add(current_fa);
        current_fa = null;
    }



    public void build_lipid(TreeNode node){
        if (lcb != null){
            for (FattyAcid fa : fa_list) fa.position += 1;
            fa_list.add(0, lcb);
            lcb = null;
            current_fa = null;
        }

        Headgroup headgroup = prepare_headgroup_and_checks();

        LipidAdduct lipid = new LipidAdduct();
        lipid.lipid = assemble_lipid(headgroup);
        lipid.adduct = adduct;
        content = lipid;

    }



    public void add_ether(TreeNode node){
        String ether = node.get_text();
        if (ether.equals("a")) current_fa.lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMANYL;
        else if (ether.equals("p")){
            current_fa.lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMENYL;
            current_fa.double_bonds.num_double_bonds = Math.max(0, current_fa.double_bonds.num_double_bonds - 1);
        }
    }



    public void add_old_hydroxyl(TreeNode node){
        String old_hydroxyl = node.get_text();
        int num_h = 0;
        if (old_hydroxyl.equals("d")) num_h = 2;
        else if (old_hydroxyl.equals("t")) num_h = 3;


        if (sp_regular_lcb()) num_h -= 1;

        FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
        functional_group.count = num_h;
        if (!current_fa.functional_groups.containsKey("OH")) current_fa.functional_groups.put("OH", new ArrayList<>());
        current_fa.functional_groups.get("OH").add(functional_group);
    }



    public void add_double_bonds(TreeNode node){
        current_fa.double_bonds.num_double_bonds = Integer.valueOf(node.get_text());
    }



    public void add_carbon(TreeNode node){
        current_fa.num_carbon = Integer.valueOf(node.get_text());
    }



    public void add_hydroxyl(TreeNode node){
        int num_h = Integer.valueOf(node.get_text());

        if (sp_regular_lcb()) num_h -= 1;

        FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
        functional_group.count = num_h;
        if (!current_fa.functional_groups.containsKey("OH")) current_fa.functional_groups.put("OH", new ArrayList<>());
        current_fa.functional_groups.get("OH").add(functional_group);
        set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
    }



    public void new_adduct(TreeNode node){
        adduct = new Adduct("", "");
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
        else if (sign.equals("-")) adduct.set_charge_sign(-1);
    }
    
}
