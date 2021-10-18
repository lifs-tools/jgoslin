/*
 * Copyright 2021 dominik.
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

/**
 *
 * @author dominik
 */
public class HmdbParserEventHandler extends LipidBaseParserEventHandler {
    
    public int db_position;
    public String db_cistrans;
    public Dictionary furan = null;
    
    public HmdbParserEventHandler() {
        this(new KnownFunctionalGroups());
    }
        
    public HmdbParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        this.knownFunctionalGroups = knownFunctionalGroups;
        try {
            registered_events.put("lipid_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("reset_parser", TreeNode.class));
            registered_events.put("lipid_post_event", HmdbParserEventHandler.class.getDeclaredMethod("build_lipid", TreeNode.class));
            // set adduct events
            registered_events.put("adduct_info_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("new_adduct", TreeNode.class));
            registered_events.put("adduct_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_adduct", TreeNode.class));
            registered_events.put("charge_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_charge", TreeNode.class));
            registered_events.put("charge_sign_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_charge_sign", TreeNode.class));

            registered_events.put("fa_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("gl_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("gl_molecular_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("mediator_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("mediator_event", TreeNode.class));
            registered_events.put("gl_mono_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("pl_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("pl_three_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("pl_four_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("sl_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("st_species_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("st_sub1_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("st_sub2_hg_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("ganglioside_names_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("fa_species_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_species_level", TreeNode.class));
            registered_events.put("gl_molecular_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_molecular_level", TreeNode.class));
            registered_events.put("unsorted_fa_separator_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_molecular_level", TreeNode.class));
            registered_events.put("fa2_unsorted_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_molecular_level", TreeNode.class));
            registered_events.put("fa3_unsorted_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_molecular_level", TreeNode.class));
            registered_events.put("fa4_unsorted_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_molecular_level", TreeNode.class));
            registered_events.put("db_single_position_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("set_isomeric_level", TreeNode.class));
            registered_events.put("db_single_position_post_event", HmdbParserEventHandler.class.getDeclaredMethod("add_db_position", TreeNode.class));
            registered_events.put("db_position_number_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_db_position_number", TreeNode.class));
            registered_events.put("cistrans_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_cistrans", TreeNode.class));
            registered_events.put("lcb_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("new_lcb", TreeNode.class));
            registered_events.put("lcb_post_event", HmdbParserEventHandler.class.getDeclaredMethod("clean_lcb", TreeNode.class));
            registered_events.put("fa_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("new_fa", TreeNode.class));
            registered_events.put("fa_post_event", HmdbParserEventHandler.class.getDeclaredMethod("append_fa", TreeNode.class));
            registered_events.put("ether_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_ether", TreeNode.class));
            registered_events.put("hydroxyl_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_hydroxyl", TreeNode.class));
            registered_events.put("db_count_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_double_bonds", TreeNode.class));
            registered_events.put("carbon_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_carbon", TreeNode.class));
            registered_events.put("fa_lcb_suffix_type_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_one_hydroxyl", TreeNode.class));
            registered_events.put("interlink_fa_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("interlink_fa", TreeNode.class));
            registered_events.put("lipid_suffix_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("lipid_suffix", TreeNode.class));
            registered_events.put("methyl_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("add_methyl", TreeNode.class));
            registered_events.put("furan_fa_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("furan_fa", TreeNode.class));
            registered_events.put("furan_fa_post_event", HmdbParserEventHandler.class.getDeclaredMethod("furan_fa_post", TreeNode.class));
            registered_events.put("furan_fa_mono_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("furan_fa_mono", TreeNode.class));
            registered_events.put("furan_fa_di_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("furan_fa_di", TreeNode.class));
            registered_events.put("furan_first_number_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("furan_fa_first_number", TreeNode.class));
            registered_events.put("furan_second_number_pre_event", HmdbParserEventHandler.class.getDeclaredMethod("furan_fa_second_number", TreeNode.class));
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
        adduct = null;
        fa_list = new ArrayList<>();
        current_fa = null;
        use_head_group = false;
        db_position = 0;
        db_cistrans = "";
        furan = new Dictionary();
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


    public void add_db_position_number(TreeNode node){
        db_position = node.get_int();
    }


    public void add_cistrans(TreeNode node){
        db_cistrans = node.get_text();
    }


    public void set_head_group_name(TreeNode node){
        head_group = node.get_text();
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
        current_fa = new FattyAcid("FA" + (fa_list.size() + 1), knownFunctionalGroups);
    }



    public void new_lcb(TreeNode node){
        lcb = new FattyAcid("LCB", knownFunctionalGroups);
        lcb.set_type(LipidFaBondType.LCB_REGULAR);
        set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        current_fa = lcb;
    }



    public void clean_lcb(TreeNode node){
        if (current_fa.double_bonds.double_bond_positions.isEmpty() && current_fa.double_bonds.get_num() > 0){
            set_lipid_level(LipidLevel.SN_POSITION);
        }
        current_fa = null;
    }




    public void append_fa(TreeNode node)
    {
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
        if (lcb != null){
            for (FattyAcid fa : fa_list) fa.position += 1;
            fa_list.add(0, lcb);
        }

        Headgroup headgroup = prepare_headgroup_and_checks();

        LipidAdduct lipid = new LipidAdduct();
        lipid.lipid = assemble_lipid(headgroup);
        lipid.adduct = adduct;
        content = lipid;
    }



    public void add_ether(TreeNode node)
    {
        String ether = node.get_text();
        if (ether.equals("O-") || ether.equals("o-")) current_fa.lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMANYL;
        else if (ether.equals("P-")) current_fa.lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMENYL;
        else throw new UnsupportedLipidException("Fatty acyl chain of type '" + ether + "' is currently not supported");
    }



    public void add_hydroxyl(TreeNode node){
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


    public void add_methyl(TreeNode node){
        FunctionalGroup functional_group = knownFunctionalGroups.get("Me");
        functional_group.position = current_fa.num_carbon - (node.get_text().equals("i-") ? 1 : 2);
        current_fa.num_carbon -= 1;

        if (!current_fa.functional_groups.containsKey("Me")) current_fa.functional_groups.put("Me", new ArrayList<>());
        current_fa.functional_groups.get("Me").add(functional_group);
    }


    public void add_one_hydroxyl(TreeNode node){
        if (current_fa.functional_groups.containsKey("OH") && current_fa.functional_groups.get("OH").get(0).position == -1){
            current_fa.functional_groups.get("OH").get(0).count += 1;
        }
        else {
            FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
            if (!current_fa.functional_groups.containsKey("OH")) current_fa.functional_groups.put("OH", new ArrayList<>());
            current_fa.functional_groups.get("OH").add(functional_group);
        }
    }


    public void add_double_bonds(TreeNode node){
        current_fa.double_bonds.num_double_bonds = node.get_int();
    }



    public void add_carbon(TreeNode node){
        current_fa.num_carbon += node.get_int();
    }


    public void furan_fa(TreeNode node){
        furan = new Dictionary();
    }


    public void furan_fa_post(TreeNode node){
        int l = 4 + (int)furan.get("len_first") + (int)furan.get("len_second");
        current_fa.num_carbon = l;

        int start = 1 + (int)furan.get("len_first");
        int end = 3 + start;
        DoubleBonds cyclo_db = new DoubleBonds(2);
        cyclo_db.double_bond_positions.put(start, "E");
        cyclo_db.double_bond_positions.put(2 + start, "E");

        HashMap<String, ArrayList<FunctionalGroup> > cyclo_fg = new HashMap<>();
        cyclo_fg.put("Me", new ArrayList<>());

        if (((String)furan.get("type")).equals("m")){
            FunctionalGroup fg = knownFunctionalGroups.get("Me");
            fg.position = 1 + start;
            cyclo_fg.get("Me").add(fg);
        }

        else if (((String)furan.get("type")).equals("d")){
            FunctionalGroup fg = knownFunctionalGroups.get("Me");
            fg.position = 1 + start;
            cyclo_fg.get("Me").add(fg);
            fg = knownFunctionalGroups.get("Me");
            fg.position = 2 + start;
            cyclo_fg.get("Me").add(fg);
        }

        ArrayList<Element> bridge_chain = new ArrayList<>();
        bridge_chain.add(Element.O);
        Cycle cycle = new Cycle(end - start + 1 + bridge_chain.size(), start, end, cyclo_db, cyclo_fg, bridge_chain, knownFunctionalGroups);
        current_fa.functional_groups.put("cy", new ArrayList<>());
        current_fa.functional_groups.get("cy").add(cycle);
    }


    public void furan_fa_mono(TreeNode node){
        furan.put("type", "m");
    }


    public void furan_fa_di(TreeNode node){
        furan.put("type", "d");
    }


    public void furan_fa_first_number(TreeNode node){
        furan.put("len_first", node.get_int());
    }


    public void furan_fa_second_number(TreeNode node){
        furan.put("len_second", node.get_int());
    }




    public void interlink_fa(TreeNode node){
        throw new UnsupportedLipidException("Interconnected fatty acyl chains are currently not supported");
    }


    public void lipid_suffix(TreeNode node){
        //throw new UnsupportedLipidException("Lipids with suffix '" + node.get_text() + "' are currently not supported");
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
