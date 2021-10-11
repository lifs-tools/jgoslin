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
package com.lifs.jgoslin.parser;

import com.lifs.jgoslin.domain.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author dominik
 */
public class LipidBaseParserEventHandler extends BaseParserEventHandler<LipidAdduct> {
    public LipidLevel level = LipidLevel.FULL_STRUCTURE;
    public String head_group = "";
    public FattyAcid lcb = null;
    public ArrayList<FattyAcid> fa_list = new ArrayList<>();
    public FattyAcid current_fa = null;
    public Adduct adduct = null;
    public ArrayList<HeadgroupDecorator> headgroup_decorators = new ArrayList<>();
    public boolean use_head_group = false;
    
    public static HashSet<String> SP_EXCEPTION_CLASSES = new HashSet<>(Arrays.asList("Cer", "Ceramide", "Sphingosine", "So", "Sphinganine", "Sa", "SPH", "Sph", "LCB"));

    
    public void set_lipid_level(LipidLevel _level){
        level = level.level < _level.level ? level : _level;
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
}
