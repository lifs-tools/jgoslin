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
package com.lifs.jgoslin.domain;

import java.util.ArrayList;
import java.util.Map.Entry;

/**
 *
 * @author dominik
 */
public class LipidMolecularSpecies extends LipidSpecies {
    public LipidMolecularSpecies(Headgroup _headgroup){
        this(_headgroup, null);
    }

    public LipidMolecularSpecies(Headgroup _headgroup, ArrayList<FattyAcid> _fa){
        super(_headgroup, _fa);
        info.level = LipidLevel.MOLECULAR_SPECIES;
        for(FattyAcid fatty_acid : fa_list){
            if (fa.containsKey(fatty_acid.name)){
                throw new ConstraintViolationException("FA names must be unique! FA with name " + fatty_acid.name + " was already added!");
            }
            fa.put(fatty_acid.name, fatty_acid);
        }


        // add 0:0 dummys
        for (int i = _fa.size(); i < info.total_fa; ++i)
        {
            FattyAcid fatty_acid = new FattyAcid("FA" + Integer.toString(i + _fa.size() + 1));
            info.add(fatty_acid);
            fa.put(fatty_acid.name, fatty_acid);
            fa_list.add(fatty_acid);
        }
    }

    public String build_lipid_subspecies_name(){
        return build_lipid_subspecies_name(LipidLevel.NO_LEVEL);
    }
    
    
    public String build_lipid_subspecies_name(LipidLevel level){
        if (level == LipidLevel.NO_LEVEL) level = LipidLevel.MOLECULAR_SPECIES;

        String fa_separator = (level != LipidLevel.MOLECULAR_SPECIES || headgroup.lipid_category == LipidCategory.SP) ? "/" : "_";
        StringBuilder lipid_name = new StringBuilder();
        lipid_name.append(headgroup.get_lipid_string(level));


        String fa_headgroup_separator = (headgroup.lipid_category != LipidCategory.ST) ? " " : "/";

        switch (level)
        {
            case COMPLETE_STRUCTURE:
            case FULL_STRUCTURE:
            case STRUCTURE_DEFINED:
            case SN_POSITION:
                if (fa_list.size() > 0)
                {
                    lipid_name.append(fa_headgroup_separator);
                    int i = 0;

                    for (FattyAcid fatty_acid : fa_list){
                        if (i++ > 0) lipid_name.append(fa_separator);
                        lipid_name.append(fatty_acid.to_string(level));
                    }
                }
                break;

            default:
                boolean go_on = false;
                for (FattyAcid fatty_acid : fa_list){
                    if (fatty_acid.num_carbon > 0){
                        go_on = true;
                        break;
                    }
                }

                if (go_on){
                    lipid_name.append(fa_headgroup_separator);
                    int i = 0;
                    for(FattyAcid fatty_acid : fa_list){
                        if (fatty_acid.num_carbon > 0){
                            if (i++ > 0) lipid_name.append(fa_separator);
                            lipid_name.append(fatty_acid.to_string(level));
                        }
                    }
                }
                break;
        }
        return lipid_name.toString();
    }


    @Override
    public LipidLevel get_lipid_level(){
        return LipidLevel.MOLECULAR_SPECIES;
    }



    @Override
    public ElementTable get_elements(){
        ElementTable elements = new ElementTable();

        ElementTable hg_elements = headgroup.get_elements();
        for (Entry<Element, Integer> kv : hg_elements.entrySet()) elements.put(kv.getKey(), elements.get(kv.getKey()) + kv.getValue());

        // add elements from all fatty acyl chains
        for (FattyAcid fatty_acid : fa_list){
            ElementTable fa_elements = fatty_acid.get_elements();
            for (Entry<Element, Integer> kv : fa_elements.entrySet()) elements.put(kv.getKey(), elements.get(kv.getKey()) + kv.getValue());
        }

        return elements;
    }

    @Override
    public String get_lipid_string(){
        return get_lipid_string(LipidLevel.NO_LEVEL);

    }

    @Override
    public String get_lipid_string(LipidLevel level){
        switch (level)
        {
            case NO_LEVEL:
            case MOLECULAR_SPECIES:
                return build_lipid_subspecies_name(LipidLevel.MOLECULAR_SPECIES);

            case CATEGORY:
            case CLASS:
            case SPECIES:
                return super.get_lipid_string(level);

            default:
                throw new IllegalArgumentException("LipidMolecularSpecies does not know how to create a lipid string for level " + level.toString());
        }
    }

    }