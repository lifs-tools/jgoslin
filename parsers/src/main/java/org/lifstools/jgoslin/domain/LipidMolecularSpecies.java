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
package org.lifstools.jgoslin.domain;

import java.util.ArrayList;

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
        fa_list.stream().map(fatty_acid -> {
            if (fa.containsKey(fatty_acid.name)){
                throw new ConstraintViolationException("FA names must be unique! FA with name " + fatty_acid.name + " was already added!");
            }
            return fatty_acid;
        }).forEachOrdered(fatty_acid -> {
            fa.put(fatty_acid.name, fatty_acid);
        });


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
        ElementTable elements = headgroup.get_elements();
        
        // add elements from all fatty acyl chains
        fa_list.forEach(fatty_acid -> {
            elements.add(fatty_acid.get_elements());
        });

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