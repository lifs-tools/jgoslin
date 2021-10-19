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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


public class LipidMapsParserEventHandler extends LipidBaseParserEventHandler {
   
    public boolean omit_fa;
    public int db_numbers;
    public int db_position;
    public String db_cistrans;
    public String mod_text;
    public int mod_pos;
    public int mod_num;
    public boolean add_omega_linoleoyloxy_Cer;

    public static final HashSet<String> head_group_exceptions = new HashSet<>(Arrays.asList("PA", "PC", "PE", "PG", "PI", "PS"));
    public static final HashMap<String, Integer> acer_heads = new HashMap<>(){{
        put("1-O-myristoyl", 14);
        put("1-O-palmitoyl", 16);
        put("1-O-stearoyl", 18);
        put("1-O-eicosanoyl", 20);
        put("1-O-behenoyl", 22);
        put("1-O-lignoceroyl", 24);
        put("1-O-cerotoyl", 26);
        put("1-O-pentacosanoyl", 25);
        put("1-O-carboceroyl", 28);
        put("1-O-tricosanoyl", 30);
        put("1-O-lignoceroyl-omega-linoleoyloxy", 24);
        put("1-O-stearoyl-omega-linoleoyloxy", 18);
    }};
    
    public LipidMapsParserEventHandler() {
        this(new KnownFunctionalGroups());
    }

    public LipidMapsParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        this.knownFunctionalGroups = knownFunctionalGroups;
        try {
            registered_events.put("lipid_pre_event", this::reset_parser);
            registered_events.put("lipid_post_event", this::build_lipid);

            // set adduct events
            registered_events.put("adduct_info_pre_event", this::new_adduct);
            registered_events.put("adduct_pre_event", this::add_adduct);
            registered_events.put("charge_pre_event", this::add_charge);
            registered_events.put("charge_sign_pre_event", this::add_charge_sign);

            registered_events.put("mediator_pre_event", this::mediator_event);

            registered_events.put("sgl_species_pre_event", this::set_species_level);
            registered_events.put("species_fa_pre_event", this::set_species_level);
            registered_events.put("tgl_species_pre_event", this::set_species_level);
            registered_events.put("dpl_species_pre_event", this::set_species_level);
            registered_events.put("cl_species_pre_event", this::set_species_level);
            registered_events.put("dsl_species_pre_event", this::set_species_level);
            registered_events.put("fa2_unsorted_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("fa3_unsorted_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("fa4_unsorted_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("hg_dg_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("fa_lpl_molecular_pre_event", this::set_molecular_subspecies_level);
            registered_events.put("hg_lbpa_pre_event", this::set_molecular_subspecies_level);

            registered_events.put("fa_no_hg_pre_event", this::pure_fa);

            registered_events.put("hg_sgl_pre_event", this::set_head_group_name);
            registered_events.put("hg_gl_pre_event", this::set_head_group_name);
            registered_events.put("hg_cl_pre_event", this::set_head_group_name);
            registered_events.put("hg_dpl_pre_event", this::set_head_group_name);
            registered_events.put("hg_lpl_pre_event", this::set_head_group_name);
            registered_events.put("hg_threepl_pre_event", this::set_head_group_name);
            registered_events.put("hg_fourpl_pre_event", this::set_head_group_name);
            registered_events.put("hg_dsl_pre_event", this::set_head_group_name);
            registered_events.put("hg_cpa_pre_event", this::set_head_group_name);
            registered_events.put("ch_pre_event", this::set_head_group_name);
            registered_events.put("hg_che_pre_event", this::set_head_group_name);
            registered_events.put("mediator_const_pre_event", this::set_head_group_name);
            registered_events.put("pk_hg_pre_event", this::set_head_group_name);
            registered_events.put("hg_fa_pre_event", this::set_head_group_name);
            registered_events.put("hg_lsl_pre_event", this::set_head_group_name);
            registered_events.put("special_cer_pre_event", this::set_head_group_name);
            registered_events.put("special_cer_hg_pre_event", this::set_head_group_name);
            registered_events.put("omega_linoleoyloxy_Cer_pre_event", this::set_omega_head_group_name);

            registered_events.put("lcb_pre_event", this::new_lcb);
            registered_events.put("lcb_post_event", this::clean_lcb);
            registered_events.put("fa_pre_event", this::new_fa);
            registered_events.put("fa_post_event", this::append_fa);

            registered_events.put("glyco_struct_pre_event", this::add_glyco);

            registered_events.put("db_single_position_pre_event", this::set_isomeric_level);
            registered_events.put("db_single_position_post_event", this::add_db_position);
            registered_events.put("db_position_number_pre_event", this::add_db_position_number);
            registered_events.put("cistrans_pre_event", this::add_cistrans);

            registered_events.put("ether_pre_event", this::add_ether);
            registered_events.put("hydroxyl_pre_event", this::add_hydroxyl);
            registered_events.put("hydroxyl_lcb_pre_event", this::add_hydroxyl_lcb);
            registered_events.put("db_count_pre_event", this::add_double_bonds);
            registered_events.put("carbon_pre_event", this::add_carbon);

            registered_events.put("structural_mod_pre_event", this::set_structural_subspecies_level);
            registered_events.put("single_mod_pre_event", this::set_mod);
            registered_events.put("mod_text_pre_event", this::set_mod_text);
            registered_events.put("mod_pos_pre_event", this::set_mod_pos);
            registered_events.put("mod_num_pre_event", this::set_mod_num);
            registered_events.put("single_mod_post_event", this::add_functional_group);
            registered_events.put("special_cer_prefix_pre_event", this::add_ACer);
            
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
        omit_fa = false;
        db_position = 0;
        db_numbers = -1;
        db_cistrans = "";
        mod_pos = -1;
        mod_num = 1;
        mod_text = "";
        headgroup_decorators = new ArrayList<>();
        add_omega_linoleoyloxy_Cer = false;
    }




    public void add_ACer(TreeNode node){
        String head = node.get_text();
        head_group = "ACer";

        if (!acer_heads.containsKey(head)){
            throw new LipidException("ACer head group '" + head + "' unknown");
        }

        HeadgroupDecorator hgd = new HeadgroupDecorator("decorator_acyl", -1, 1, null, true, knownFunctionalGroups);
        int acer_num = acer_heads.get(head);
        hgd.functional_groups.put("decorator_acyl", new ArrayList<>());
        hgd.functional_groups.get("decorator_acyl").add(new FattyAcid("FA", acer_num, knownFunctionalGroups));
        headgroup_decorators.add(hgd);

        if (head.equals("1-O-lignoceroyl-omega-linoleoyloxy") || head.equals("1-O-stearoyl-omega-linoleoyloxy")){
            add_omega_linoleoyloxy_Cer = true;
        }
    }


    public void set_molecular_subspecies_level(TreeNode node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }


    public void pure_fa(TreeNode node){
        head_group = "FA";
    }


    public void mediator_event(TreeNode node){
        use_head_group = true;
        head_group = node.get_text();
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



    public void set_omega_head_group_name(TreeNode node){
        add_omega_linoleoyloxy_Cer = true;
        set_head_group_name(node);
    }


    public void add_glyco(TreeNode node){
        String glyco_name = node.get_text();
        HeadgroupDecorator functional_group = null;
        try {
            functional_group = (HeadgroupDecorator)knownFunctionalGroups.get(glyco_name);
        }
        catch (Exception e) {
            throw new LipidParsingException("Carbohydrate '" + glyco_name + "' unknown");
        }

        functional_group.elements.put(Element.O, functional_group.elements.get(Element.O) - 1);
        headgroup_decorators.add(functional_group);
    }


    public void add_db_position_number(TreeNode node){
        db_position = Integer.valueOf(node.get_text());
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



    public void set_structural_subspecies_level(TreeNode node){
        set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
    }


    public void set_mod(TreeNode node){
        mod_text = "";
        mod_pos = -1;
        mod_num = 1;
    }


    public void set_mod_text(TreeNode node){
        mod_text = node.get_text();
    }


    public void set_mod_pos(TreeNode node){
        mod_pos = node.get_int();
    }


    public void set_mod_num(TreeNode node) {
        mod_num = node.get_int();
    }   


    public void add_functional_group(TreeNode node) {
        if (!mod_text.equals("Cp")){
            FunctionalGroup functional_group = knownFunctionalGroups.get(mod_text);
            functional_group.position = mod_pos;
            functional_group.count = mod_num;
            String fg_name = functional_group.name;
            if (!current_fa.functional_groups.containsKey(fg_name)) current_fa.functional_groups.put(fg_name, new ArrayList<>());
            current_fa.functional_groups.get(fg_name).add(functional_group);
        }
        else {
            current_fa.num_carbon += 1;
            Cycle cycle = new Cycle(3, mod_pos, mod_pos + 2, knownFunctionalGroups);
            if (!current_fa.functional_groups.containsKey("cy")) current_fa.functional_groups.put("cy", new ArrayList<>());
            current_fa.functional_groups.get("cy").add(cycle);
        }
    }


    public void new_fa(TreeNode node){
        db_numbers = -1;
        current_fa = new FattyAcid("FA" + (fa_list.size() + 1), knownFunctionalGroups);
    }



    public void new_lcb(TreeNode node){
        lcb = new FattyAcid("LCB", knownFunctionalGroups);
        lcb.set_type(LipidFaBondType.LCB_REGULAR);
        set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        current_fa = lcb;
    }



    public void clean_lcb(TreeNode node){
        if (db_numbers > -1 && db_numbers != current_fa.double_bonds.get_num())
        {
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }
        if (current_fa.double_bonds.double_bond_positions.isEmpty() && current_fa.double_bonds.get_num() > 0){
            set_lipid_level(LipidLevel.SN_POSITION);
        }
        current_fa = null;
    }



    public void append_fa(TreeNode node){
        if (db_numbers > -1 && db_numbers != current_fa.double_bonds.get_num()){
            throw new LipidException("Double bond count does not match with number of double bond positions");
        }
        if (current_fa.double_bonds.double_bond_positions.isEmpty() && current_fa.double_bonds.get_num() > 0){
            set_lipid_level(LipidLevel.SN_POSITION);
        }

        if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level | LipidLevel.SN_POSITION.level)){
                current_fa.position = fa_list.size() + 1;
        }

        if (current_fa.num_carbon == 0){
            omit_fa = true;
        }
        fa_list.add(current_fa);
        current_fa = null;
    }


    public void add_ether(TreeNode node){
        String ether = node.get_text();
        if (ether.equals("O-")) current_fa.lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMANYL;
        else if (ether.equals("P-")) current_fa.lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMENYL;
    }



    public void add_hydroxyl(TreeNode node)
    {
        int num_h = node.get_int();

        if (sp_regular_lcb()) num_h -= 1;

        FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
        functional_group.count = num_h;
        if (!current_fa.functional_groups.containsKey("OH")) current_fa.functional_groups.put("OH", new ArrayList<>());
        current_fa.functional_groups.get("OH").add(functional_group);
    }



    public void add_hydroxyl_lcb(TreeNode node){
        String hydroxyl = node.get_text();
        int num_h = 0;
        if (hydroxyl.equals("m")) num_h = 1;
        else if (hydroxyl.equals("d")) num_h = 2;
        else if (hydroxyl.equals("t")) num_h = 3;

        if (sp_regular_lcb()) num_h -= 1;

        FunctionalGroup functional_group = knownFunctionalGroups.get("OH");
        functional_group.count = num_h;
        if (!current_fa.functional_groups.containsKey("OH")) current_fa.functional_groups.put("OH", new ArrayList<>());
        current_fa.functional_groups.get("OH").add(functional_group);
    }


    public void add_double_bonds(TreeNode node){
        current_fa.double_bonds.num_double_bonds += node.get_int();
    }


    public void add_carbon(TreeNode node){
        current_fa.num_carbon = node.get_int();
    }



    public void build_lipid(TreeNode node){
        if (omit_fa && head_group_exceptions.contains(head_group)){
            head_group = "L" + head_group;
        }

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
