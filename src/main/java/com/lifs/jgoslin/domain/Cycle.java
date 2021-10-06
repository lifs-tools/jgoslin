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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 *
 * @author dominik
 */
public class Cycle extends FunctionalGroup {
    public int cycle;
    public int start;
    public int end;
    public ArrayList<Element> bridge_chain;
    
    public Cycle(int _cycle){
        this(_cycle, -1, -1, null, null, null);
    }

    public Cycle(int _cycle, int _start, int _end, DoubleBonds _double_bonds, HashMap<String, ArrayList< FunctionalGroup > > _functional_groups, ArrayList< Element > _bridge_chain){
        super("cy", _start, 1, _double_bonds, false, "", null, _functional_groups);
        cycle = _cycle;
        start = _start;
        end = _end;
        elements.put(Element.H, elements.get(Element.H) - 2);
        bridge_chain = (_bridge_chain == null) ? new ArrayList< Element >() : _bridge_chain;
    }

    
    @Override
    public FunctionalGroup copy(){
        DoubleBonds db = double_bonds.copy();
        HashMap<String, ArrayList<FunctionalGroup> > fg = new HashMap< >();
        functional_groups.entrySet().stream().map(kv -> {
            fg.put(kv.getKey(), new ArrayList<>());
            return kv;
        }).forEachOrdered(kv -> {
            kv.getValue().forEach(func_group -> {
                fg.get(kv.getKey()).add(func_group.copy());
            });
        });
        ArrayList<Element> bc = new ArrayList<Element>();
        for (Element e : bridge_chain) bc.add(e);

        return new Cycle(cycle, start, end, db, fg, bc);
    }

    @Override
    public int get_double_bonds() throws ConstraintViolationException{
        return super.get_double_bonds() + 1;
    }

    @Override
    public void add_position(int pos){
        start += (start >= pos) ? 1 : 0;
        end += (end >= pos) ? 1 : 0;
        super.add_position(pos);
    }


    public void rearrange_functional_groups(FunctionalGroup parent, int shift)
    {
        // put everything back into parent
        for (Entry<Integer, String> kv : double_bonds.double_bond_positions.entrySet()) {
            parent.double_bonds.double_bond_positions.put(kv.getKey(), kv.getValue());
        }
        double_bonds = new DoubleBonds();

        for (Entry<String, ArrayList<FunctionalGroup> > kv : functional_groups.entrySet()){
            if (!parent.functional_groups.containsKey(kv.getKey())){
                parent.functional_groups.put(kv.getKey(), new ArrayList<FunctionalGroup>());
            }
            parent.functional_groups.get(kv.getKey()).addAll(functional_groups.get(kv.getKey()));
        }
        functional_groups = new HashMap<String, ArrayList<FunctionalGroup> >();


        // shift the cycle
        shift_positions(shift);


        // take back what's mine# check double bonds
        parent.double_bonds.double_bond_positions.entrySet().stream().filter(kv -> (start <= kv.getKey() && kv.getKey() <= end)).forEachOrdered(kv -> {
            double_bonds.double_bond_positions.put(kv.getKey(), kv.getValue());
        });
        double_bonds.num_double_bonds = double_bonds.double_bond_positions.size();

        double_bonds.double_bond_positions.entrySet().forEach(kv -> {
            parent.double_bonds.double_bond_positions.remove(kv.getKey());
        });
        parent.double_bonds.num_double_bonds = parent.double_bonds.double_bond_positions.size();


        HashSet<String> remove_list = new HashSet<>();
        for (Entry<String, ArrayList<FunctionalGroup> > kv : parent.functional_groups.entrySet()){
            ArrayList<Integer> remove_item = new ArrayList<>();

            int i = 0;
            for (FunctionalGroup func_group : kv.getValue()){
                if (start <= func_group.position && func_group.position <= end && func_group != this){
                    if (!functional_groups.containsKey(kv.getKey())){
                        functional_groups.put(kv.getKey(), new ArrayList<>());
                    }
                    functional_groups.get(kv.getKey()).add(func_group);
                    remove_item.add(i);
                }
                ++i;
            }

            while (remove_item.size() > 0){
                int pos = remove_item.get(remove_item.size() - 1);
                remove_item.remove(remove_item.size() - 1);
                kv.getValue().remove(pos);
            }
            if (kv.getValue().size() == 0) remove_list.add(kv.getKey());
        }

        for (String fg : remove_list) parent.functional_groups.remove(fg);
    }

    
    @Override
    public void shift_positions(int shift){
        super.shift_positions(shift);
        start += shift;
        end += shift;
        DoubleBonds db = new DoubleBonds();
        for (Entry<Integer, String> kv : double_bonds.double_bond_positions.entrySet()){
            db.double_bond_positions.put(kv.getKey() + shift, kv.getValue());
        }
        db.num_double_bonds = db.double_bond_positions.size();
        double_bonds = db;
    }

    @Override
    public void compute_elements() {
        elements = new ElementTable();
        elements.put(Element.H, -2 - 2 * double_bonds.num_double_bonds);

        for (Element chain_element : bridge_chain){
            try {
                switch(chain_element){
                    case C:
                        elements.put(Element.C, elements.get(Element.C) + 1);
                        elements.put(Element.H, elements.get(Element.H) + 2);
                        break;

                    case N:
                        elements.put(Element.N, elements.get(Element.N) + 1);
                        elements.put(Element.H, elements.get(Element.H) + 1);
                        break;

                    case P:
                        elements.put(Element.P, elements.get(Element.P) + 1);
                        elements.put(Element.H, elements.get(Element.H) + 1);
                        break;

                    case As:
                        elements.put(Element.As, elements.get(Element.As) + 1);
                        elements.put(Element.H, elements.get(Element.H) + 1);
                        break;

                    case O:
                        elements.put(Element.O, elements.get(Element.O) + 1);
                        break;

                    case S:
                        elements.put(Element.S, elements.get(Element.S) + 1);
                        break;

                }
            }
            catch (Exception e){
                throw new RuntimeException("Element '" + Elements.element_shortcut.get(chain_element) + "' cannot be part of a cycle bridge");
            }
        }

        // add all implicit carbon chain elements
        if (start != -1 && end != -1)
        {
            int n = Math.max(0, cycle - (end - start + 1 + bridge_chain.size()));
            elements.put(Element.C, elements.get(Element.C) + n);
            elements.put(Element.H, elements.get(Element.H) + 2 * n);
        }
    }

    @Override
    public String to_string(LipidLevel level){
        StringBuilder cycle_string = new StringBuilder();
        cycle_string.append("[");
        if (start != -1 && LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level))
        {
            cycle_string.append(start).append("-").append(end);
        }

        if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level) && bridge_chain.size() > 0){
            for (Element e : bridge_chain) cycle_string.append(Elements.element_shortcut.get(e));
        }
        cycle_string.append("cy").append(cycle);            
        cycle_string.append(":").append(double_bonds.get_num());

        if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level)){
            if (double_bonds.double_bond_positions.size() > 0)
            {
                int i = 0;
                cycle_string.append("(");
                for (Entry<Integer, String> kv : double_bonds.double_bond_positions.entrySet()){
                    if (i++ > 0) cycle_string.append(",");
                    if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level)) cycle_string.append(kv.getKey()).append(kv.getValue());
                    else cycle_string.append(kv.getKey());
                }
                cycle_string.append(")");
            }
        }

        if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level)){
            ArrayList<String> fg_names = new ArrayList<>();
            for (Entry<String, ArrayList<FunctionalGroup> > kv : functional_groups.entrySet()) fg_names.add(kv.getKey());
            Collections.sort(fg_names, new Comparator<String>(){
                @Override
                public int compare(String a, String b){
                    return a.toLowerCase().compareTo(b.toLowerCase());
                }
            });

            for (String fg : fg_names){
                ArrayList<FunctionalGroup> fg_list = functional_groups.get(fg);
                if (fg_list.size() == 0) continue;
                Collections.sort(fg_list, new Comparator<FunctionalGroup>(){
                    @Override
                    public int compare(FunctionalGroup a, FunctionalGroup b){
                        return a.position - b.position;
                    }
                });
                
                int i = 0;
                cycle_string.append(";");
                for (FunctionalGroup func_group : fg_list){
                    if (i++ > 0) cycle_string.append(",");
                    cycle_string.append(func_group.to_string(level));
                }
            }
        }

        else if (level == LipidLevel.STRUCTURE_DEFINED){
            ArrayList<String> fg_names = new ArrayList<>();
            for (Entry<String, ArrayList<FunctionalGroup> > kv : functional_groups.entrySet()) fg_names.add(kv.getKey());
            Collections.sort(fg_names, new Comparator<String>(){
                @Override
                public int compare(String a, String b){
                    return a.toLowerCase().compareTo(b.toLowerCase());
                }
            });

            for (String fg : fg_names){
                ArrayList<FunctionalGroup> fg_list = functional_groups.get(fg);
                if (fg_list.size() == 0) continue;

                else if (fg_list.size() == 1 && fg_list.get(0).count == 1){
                    cycle_string.append(";").append(fg_list.get(0).to_string(level));
                }
                else {
                    int fg_count = 0;
                    for (FunctionalGroup func_group : fg_list) fg_count += func_group.count;
                    if (fg_count > 1){
                        cycle_string.append(";(").append(fg).append(")").append(fg_count);
                    }
                    else {
                        cycle_string.append(";").append(fg);
                    }
                }
            }
        }

        cycle_string.append("]");
        if (stereochemistry.length() > 0) cycle_string.append("[").append(stereochemistry).append("]");

        return cycle_string.toString();
    }
}
