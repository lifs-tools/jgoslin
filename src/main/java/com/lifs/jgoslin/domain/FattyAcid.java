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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.antlr.v4.misc.Utils;

/**
 *
 * @author dominik
 */
public class FattyAcid extends FunctionalGroup {
    public int num_carbon;
    public LipidFaBondType lipid_FA_bond_type;
    public HashSet<String> fg_exceptions = new HashSet<>(Arrays.asList("acyl", "alkyl", "cy", "cc", "acetoxy"));
    
    public FattyAcid(String _name){
        this(_name, 0, null, null, LipidFaBondType.ESTER, 0);
    }

    public FattyAcid(String _name, int _num_carbon, DoubleBonds _double_bonds, HashMap<String, ArrayList<FunctionalGroup> > _functional_groups, LipidFaBondType _lipid_FA_bond_type, int _position) {
        super(_name, _position, 1, _double_bonds, false, "", null, _functional_groups);
        num_carbon = _num_carbon;
        lipid_FA_bond_type = _lipid_FA_bond_type;

        if (lipid_FA_bond_type == LipidFaBondType.LCB_REGULAR)
        {
            functional_groups.put("[X]", new ArrayList<>());
            functional_groups.get("[X]").add(KnownFunctionalGroups.get_instance().get("X"));
        }

        if (num_carbon < 0 || num_carbon == 1)
        {
            throw new ConstraintViolationException("FattyAcid must have at least 2 carbons! Got " + Integer.toString(num_carbon));
        }

        if (position < 0)
        {
            throw new ConstraintViolationException("FattyAcid position must be greater or equal to 0! Got " + Integer.toString(position));
        }

        if (double_bonds.get_num() < 0)
        {
            throw new ConstraintViolationException("FattyAcid must have at least 0 double bonds! Got " + Integer.toString(double_bonds.get_num()));
        }
    }


    @Override
    public FunctionalGroup copy()
    {
        DoubleBonds db = double_bonds.copy();
        HashMap<String, ArrayList<FunctionalGroup> > fg = new HashMap<>();
        for (Entry<String, ArrayList<FunctionalGroup> > kv : functional_groups.entrySet()){
            fg.put(kv.getKey(), new ArrayList<>());
            for (FunctionalGroup func_group : kv.getValue()){
                fg.get(kv.getKey()).add(func_group.copy());
            }
        }

        return new FattyAcid(name, num_carbon, db, fg, lipid_FA_bond_type, position);
    }



    public void set_type(LipidFaBondType _lipid_FA_bond_type)
    {
        lipid_FA_bond_type = _lipid_FA_bond_type;
        if (lipid_FA_bond_type == LipidFaBondType.LCB_REGULAR && !functional_groups.containsKey("[X]"))
        {
            functional_groups.put("[X]", new ArrayList<FunctionalGroup>());
            functional_groups.get("[X]").add(KnownFunctionalGroups.get_instance().get("X"));
        }

        else if (functional_groups.containsKey("[X]"))
        {
            functional_groups.remove("[X]");
        }

        name = (lipid_FA_bond_type != LipidFaBondType.LCB_EXCEPTION && lipid_FA_bond_type != LipidFaBondType.LCB_REGULAR) ? "FA" : "LCB";
    }



    public String get_prefix(LipidFaBondType lipid_FA_bond_type)
    {
        switch(lipid_FA_bond_type){
            case ETHER_PLASMANYL: return "O-";
            case ETHER_PLASMENYL: return "P-";
            default: return "";
        }
    }


    @Override
    public int get_double_bonds(){
        return super.get_double_bonds() + ((lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMENYL) ? 1 : 0);
    }



    public boolean lipid_FA_bond_type_prefix(LipidFaBondType lipid_FA_bond_type){
        return (lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMANYL) || (lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMENYL) || (lipid_FA_bond_type == LipidFaBondType.ETHER_UNSPECIFIED); 
    }


    @Override
    public String to_string(LipidLevel level){
        StringBuilder fa_string = new StringBuilder();
        fa_string.append(get_prefix(lipid_FA_bond_type));
        int num_carbons = num_carbon;
        int num_double_bonds = double_bonds.get_num();

        if (num_carbons == 0 && num_double_bonds == 0 && !LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level | LipidLevel.SN_POSITION.level)){
            return "";
        }

        if (LipidLevel.is_level(level, LipidLevel.SN_POSITION.level | LipidLevel.MOLECULAR_SPECIES.level)){
            ElementTable e = get_elements();
            num_carbons = e.get(Element.C);
            num_double_bonds = get_double_bonds() - ((lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMENYL) ? 1 : 0);
        }


        fa_string.append(num_carbons).append(":").append(num_double_bonds);


        if (!LipidLevel.is_level(level, LipidLevel.SN_POSITION.level | LipidLevel.MOLECULAR_SPECIES.level) && double_bonds.double_bond_positions.size() > 0){
            fa_string.append("(");

            int i = 0;
            ArrayList<Integer> sorted_db = new ArrayList<>();
            double_bonds.double_bond_positions.entrySet().forEach(kv -> {
                sorted_db.add(kv.getKey());
            });
            Collections.sort(sorted_db);
            for (int db_pos : sorted_db){
                if (i++ > 0) fa_string.append(",");
                fa_string.append(db_pos);
                if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level)) fa_string.append(double_bonds.double_bond_positions.get(db_pos));
            }
            fa_string.append(")");
        }


        if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level)){
            ArrayList<String> fg_names = new ArrayList<>();
            for (Entry<String, ArrayList<FunctionalGroup> > kv : functional_groups.entrySet()) fg_names.add(kv.getKey());
            Collections.sort(fg_names, (String a, String b) -> a.toLowerCase().compareTo(b.toLowerCase()));

            for (String fg : fg_names){
                if (fg.equals("[X]")) continue;
                ArrayList<FunctionalGroup> fg_list = functional_groups.get(fg);
                if (fg_list.isEmpty()) continue;

                Collections.sort(fg_list, (FunctionalGroup a, FunctionalGroup b) -> a.position - b.position);
                
                int i = 0;
                fa_string.append(";");
                for (FunctionalGroup func_group : fg_list){
                    if (i++ > 0) fa_string.append(",");
                    fa_string.append(func_group.to_string(level));
                }
            }
        }

        else if (level == LipidLevel.STRUCTURE_DEFINED)
        {
            ArrayList<String> fg_names = new ArrayList<>();
            functional_groups.entrySet().forEach(kv -> {
                fg_names.add(kv.getKey());
            });
            Collections.sort(fg_names, (String a, String b) -> a.toLowerCase().compareTo(b.toLowerCase()));


            for (String fg : fg_names){
                if (fg.equals("[X]")) continue;
                ArrayList<FunctionalGroup> fg_list = functional_groups.get(fg);
                if (fg_list.isEmpty()) continue;

                if (fg_exceptions.contains(fg)){
                    fa_string.append(";");
                    int i = 0;
                    for (FunctionalGroup func_group : fg_list) {
                        if (i++ > 0) fa_string.append(",");
                        fa_string.append(func_group.to_string(level));
                    }
                }

                else {
                    int fg_count = 0;
                    for (FunctionalGroup func_group : fg_list) fg_count += func_group.count;

                    if (fg_count > 1) {
                        fa_string.append(";").append(!fg_list.get(0).is_atomic ? ("(" + fg + ")" + Integer.toString(fg_count)) : (fg + Integer.toString(fg_count)));
                    }
                    else {
                        fa_string.append(";").append(fg);
                    }
                }
            }

        }
        else {
            ElementTable func_elements = get_functional_group_elements();
            for (int i = 2; i < Elements.element_order.size(); ++i){
                Element e = Elements.element_order.get(i);
                if (func_elements.get(e) > 0){
                    fa_string.append(";").append(Elements.element_shortcut.get(e));
                    if (func_elements.get(e) > 1){
                        fa_string.append(func_elements.get(e));
                    }
                }
            } 
        }
        return fa_string.toString();
    }


    @Override
    public ElementTable get_functional_group_elements(){
        ElementTable elements = super.get_functional_group_elements();

        // subtract the invisible [X] functional group for regular LCBs
        if (lipid_FA_bond_type == LipidFaBondType.LCB_REGULAR && functional_groups.containsKey("O"))
        {
            elements.put(Element.O, elements.get(Element.O) - 1);
        }

        return elements;
    }


    @Override
    public void compute_elements()
    {
        elements = new ElementTable();

        int num_double_bonds = double_bonds.num_double_bonds;
        if (lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMENYL) num_double_bonds += 1;

        if (num_carbon == 0 && num_double_bonds == 0)
        {
            elements.put(Element.H, 1);
            return;
        }

        if (lipid_FA_bond_type != LipidFaBondType.LCB_EXCEPTION && lipid_FA_bond_type != LipidFaBondType.LCB_REGULAR)
        {
            elements.put(Element.C, num_carbon); // carbon
            if (lipid_FA_bond_type == LipidFaBondType.ESTER)
            {
                elements.put(Element.H, (2 * num_carbon - 1 - 2 * num_double_bonds)); // hydrogen
                elements.put(Element.O, 1); // oxygen
            }

            else if (lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMENYL) {
                elements.put(Element.H, (2 * num_carbon - 1 - 2 * num_double_bonds + 2)); // hydrogen
            }

            else if (lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMANYL){
                elements.put(Element.H, ((num_carbon + 1) * 2 - 1 - 2 * num_double_bonds)); // hydrogen
            }

            else if (lipid_FA_bond_type == LipidFaBondType.AMINE)
                elements.put(Element.H, (2 * num_carbon + 1 - 2 * num_double_bonds)); // hydrogen

            else {
                throw new LipidException("Mass cannot be computed for fatty acyl chain with this bond type");
            }
        }
        else {
            // long chain base
            elements.put(Element.C, num_carbon); // carbon
            elements.put(Element.H, (2 * (num_carbon - num_double_bonds) + 1)); // hydrogen
            elements.put(Element.N, 1); // nitrogen
        }
    }
}