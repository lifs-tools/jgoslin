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

package com.lifs.jgoslin.domain;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author dominik
 */
public class LipidSpecies {
    public Headgroup headgroup;
    public LipidSpeciesInfo info;

    public HashMap<String, FattyAcid> fa = new HashMap<>();
    public ArrayList<FattyAcid> fa_list = new ArrayList<>();

    public LipidSpecies(Headgroup _headgroup){
        this(_headgroup, null);
    }
    
    public LipidSpecies(Headgroup _headgroup, ArrayList<FattyAcid> _fa){
        headgroup = _headgroup;

        info = new LipidSpeciesInfo(headgroup.lipid_class);
        info.level = LipidLevel.SPECIES;

        // add fatty acids
        if (_fa != null){
            for (FattyAcid fatty_acid : _fa){
                info.add(fatty_acid);
                fa_list.add(fatty_acid);
            }
        }
    }


    public LipidLevel get_lipid_level(){
        return LipidLevel.SPECIES;
    }

    public String get_lipid_string(){
        return get_lipid_string(LipidLevel.NO_LEVEL);
    }
    
    public String get_lipid_string(LipidLevel level){
        switch (level){

            default:
                throw new RuntimeException("LipidSpecies does not know how to create a lipid string for level " + level.toString());

            case UNDEFINED_LEVEL:
                throw new RuntimeException("LipidSpecies does not know how to create a lipid string for level " + level.toString());

            case CLASS:
            case CATEGORY:
                return headgroup.get_lipid_string(level);

            case NO_LEVEL:
            case SPECIES:
                StringBuilder lipid_string = new StringBuilder();
                lipid_string.append(headgroup.get_lipid_string(level));

                if (info.elements.get(Element.C) > 0 || info.num_carbon > 0){
                    LipidSpeciesInfo lsi = info.copy();
                    for (HeadgroupDecorator decorator : headgroup.decorators){
                        if (decorator.name.equals("decorator_alkyl") || decorator.name.equals("decorator_acyl")){
                            ElementTable e = decorator.get_elements();
                            lsi.num_carbon += e.get(Element.C);
                            lsi.double_bonds.num_double_bonds += decorator.get_double_bonds();
                        }
                    }
                    lipid_string.append(headgroup.lipid_category != LipidCategory.ST ? " " : "/").append(lsi.to_string());
                }
                return lipid_string.toString();
        }
    }



    public String get_extended_class(){
        boolean special_case = (info.num_carbon > 0) ? (headgroup.lipid_category == LipidCategory.GP) : false;
        String class_name = headgroup.get_class_name();
        if (special_case && (info.extended_class == LipidFaBondType.ETHER_PLASMANYL || info.extended_class == LipidFaBondType.ETHER_UNSPECIFIED)){
            return class_name + "-O";
        }

        else if (special_case && info.extended_class == LipidFaBondType.ETHER_PLASMENYL){
            return class_name + "-p";
        }

        return class_name;
    }


    public ArrayList<FattyAcid> get_fa_list(){
        return fa_list;
    }



    public ElementTable get_elements(){
        
        switch(info.level){        
            case COMPLETE_STRUCTURE:
            case FULL_STRUCTURE:
            case STRUCTURE_DEFINED:
            case SN_POSITION:
            case MOLECULAR_SPECIES:
            case SPECIES:
                break;

            default:    
                throw new LipidException("Element table cannot be computed for lipid level " + info.level.toString());
        }

        if (headgroup.use_headgroup)
        {
            throw new LipidException("Element table cannot be computed for lipid level " + info.level.toString());
        }

        ElementTable elements = headgroup.get_elements();
        elements.add(info.get_elements());
        
        
        // since only one FA info is provided, we have to treat this single information as
        // if we would have the complete information about all possible FAs in that lipid
        LipidClassMeta meta = LipidClasses.get_instance().get(headgroup.lipid_class);

        int additional_fa = meta.possible_num_fa;
        int remaining_H = meta.max_num_fa - additional_fa;
        int hydrochain = meta.special_cases.contains("HC") ? 1 : 0;

        elements.put(Element.O, elements.get(Element.O) - (-additional_fa + info.num_ethers + (headgroup.sp_exception ? 1 : 0) + hydrochain));
        elements.put(Element.H, elements.get(Element.H) + (-additional_fa + remaining_H + 2 * info.num_ethers + 2 * hydrochain));
   
        return elements;
    }
}
