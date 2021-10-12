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

import com.lifs.jgoslin.domain.LipidParsingException;

/**
 *
 * @author dominik
 */
public class SwissLipidsParserEventHandler extends LipidBaseParserEventHandler {
    public SwissLipidsParserEventHandler() {
        try {
            registered_events.put("lipid_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("reset_parser", TreeNode.class));
            registered_events.put("lipid_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("build_lipid", TreeNode.class));
            
            registered_events.put("mediator_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("mediator_event", TreeNode.class));
            
            registered_events.put("sgl_species_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_species_level", TreeNode.class));
            registered_events.put("species_fa_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_species_level", TreeNode.class));
            registered_events.put("tgl_species_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_species_level", TreeNode.class));
            registered_events.put("dpl_species_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_species_level", TreeNode.class));
            registered_events.put("cl_species_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_species_level", TreeNode.class));
            registered_events.put("dsl_species_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_species_level", TreeNode.class));
            registered_events.put("fa2_unsorted_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_subspecies_level", TreeNode.class));
            registered_events.put("fa3_unsorted_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_subspecies_level", TreeNode.class));
            registered_events.put("fa4_unsorted_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_subspecies_level", TreeNode.class));
            registered_events.put("hg_dg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_subspecies_level", TreeNode.class));
            registered_events.put("fa_lpl_molecular_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_subspecies_level", TreeNode.class));
            registered_events.put("hg_lbpa_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_molecular_subspecies_level", TreeNode.class));
            
            registered_events.put("fa_no_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("pure_fa", TreeNode.class));
            
            registered_events.put("hg_sgl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_gl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_cl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_dpl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_lpl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_threepl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_fourpl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_dsl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_cpa_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("ch_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_che_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("mediator_const_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("pk_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_fa_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("hg_lsl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("special_cer_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("special_cer_hg_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_head_group_name", TreeNode.class));
            registered_events.put("omega_linoleoyloxy_Cer_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_omega_head_group_name", TreeNode.class));
            
            registered_events.put("lcb_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("new_lcb", TreeNode.class));
            registered_events.put("lcb_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("clean_lcb", TreeNode.class));
            registered_events.put("fa_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("new_fa", TreeNode.class));
            registered_events.put("fa_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("append_fa", TreeNode.class));
            
            registered_events.put("glyco_struct_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_glyco", TreeNode.class));
            
            registered_events.put("db_single_position_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_isomeric_level", TreeNode.class));
            registered_events.put("db_single_position_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_db_position", TreeNode.class));
            registered_events.put("db_position_number_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_db_position_number", TreeNode.class));
            registered_events.put("cistrans_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_cistrans", TreeNode.class));
            
            registered_events.put("ether_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_ether", TreeNode.class));
            registered_events.put("hydroxyl_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_hydroxyl", TreeNode.class));
            registered_events.put("hydroxyl_lcb_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_hydroxyl_lcb", TreeNode.class));
            registered_events.put("db_count_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_double_bonds", TreeNode.class));
            registered_events.put("carbon_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_carbon", TreeNode.class));
            
            registered_events.put("structural_mod_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_structural_subspecies_level", TreeNode.class));
            registered_events.put("single_mod_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_mod", TreeNode.class));
            registered_events.put("mod_text_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_mod_text", TreeNode.class));
            registered_events.put("mod_pos_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_mod_pos", TreeNode.class));
            registered_events.put("mod_num_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("set_mod_num", TreeNode.class));
            registered_events.put("single_mod_post_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_functional_group", TreeNode.class));
            registered_events.put("special_cer_prefix_pre_event", SwissLipidsParserEventHandler.class.getDeclaredMethod("add_ACer", TreeNode.class));
            
        }
        catch(Exception e){
            throw new LipidParsingException("Cannot initialize LipidMapsParserEventHandler.");
        }
    }
}
