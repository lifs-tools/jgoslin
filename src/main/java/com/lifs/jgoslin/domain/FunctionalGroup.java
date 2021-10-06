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
import java.util.Map.Entry;

/**
 *
 * @author dominik
 */
public class FunctionalGroup {
    public String name;
    public int position;
    public int count;
    public String stereochemistry;
    public String ring_stereo;
    public DoubleBonds double_bonds;
    public boolean is_atomic;
    public ElementTable elements;
    public HashMap<String, ArrayList<FunctionalGroup> > functional_groups;

    public FunctionalGroup(String _name){
        this(_name, -1, 1, null, false, "", null, null);
    }
    
    
    public FunctionalGroup(String _name, int _position, int _count, DoubleBonds _double_bonds, boolean _is_atomic, String _stereochemistry, ElementTable _elements, HashMap<String, ArrayList<FunctionalGroup> > _functional_groups){
        name = _name;
        position = _position;
        count = _count;
        stereochemistry = _stereochemistry;
        ring_stereo = "";
        double_bonds = (_double_bonds != null) ? _double_bonds : new DoubleBonds(0);
        is_atomic = _is_atomic;
        elements = (_elements != null) ? _elements : new ElementTable();
        functional_groups = (_functional_groups != null) ? _functional_groups : (new HashMap< >());
    }


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
        ElementTable e = new ElementTable();
        elements.entrySet().forEach(kv -> {
            e.put(kv.getKey(), kv.getValue());
        });

        FunctionalGroup func_group_new = new FunctionalGroup(name, position, count, db, is_atomic, stereochemistry, e, fg);
        func_group_new.ring_stereo = ring_stereo;
        return func_group_new;
    }


    public ElementTable get_elements(){
        compute_elements();
        ElementTable _elements = new ElementTable();
        elements.entrySet().forEach(kv -> {
            _elements.put(kv.getKey(), kv.getValue());
        });

        ElementTable fgElements = get_functional_group_elements();
        fgElements.entrySet().forEach(kv -> {
            _elements.put(kv.getKey(), _elements.get(kv.getKey()) + kv.getValue());
        });
        return _elements;
    }


    public void shift_positions(int shift)
    {
        position += shift;
        functional_groups.entrySet().forEach(kv -> {
            kv.getValue().forEach(fg -> {
                fg.shift_positions(shift);
            });
        });
    }


    public ElementTable get_functional_group_elements(){
        ElementTable _elements = new ElementTable();

        functional_groups.entrySet().forEach(kv -> {
            kv.getValue().forEach(func_group -> {
                ElementTable fg_elements = func_group.get_elements();
                fg_elements.entrySet().forEach(el -> {
                    _elements.put(el.getKey(), _elements.get(el.getKey()) + el.getValue() * func_group.count);
                });
            });
        });

        return _elements;
    }


    public void compute_elements()
    {
        functional_groups.entrySet().forEach(kv -> {
            kv.getValue().forEach(func_group -> {
                func_group.compute_elements();
            });
        });
    }




    public String to_string(LipidLevel level)
    {
        String fg_string = "";
        if (LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level))
        {
            if ('0' <= name.charAt(0) && name.charAt(0) <= '9') {
                fg_string = (position > -1) ? (Integer.toString(position) + ring_stereo + "(" + name + ")") : name;
            }
            else {
                fg_string = (position > -1) ? (Integer.toString(position) + ring_stereo + name) : name;
            }
        }
        else {
            fg_string = (count > 1) ? ("(" + name + ")" + Integer.toString(count)) : name;
        }
        if (stereochemistry.length() > 0 && level == LipidLevel.COMPLETE_STRUCTURE)
        {
            fg_string += "[" + stereochemistry + "]";
        }

        return fg_string;
    }


    public int get_double_bonds() throws ConstraintViolationException
    {
        int db = count * double_bonds.get_num();
        for (Entry<String, ArrayList<FunctionalGroup> > kv : functional_groups.entrySet()){
            for (FunctionalGroup func_group : kv.getValue()) {
                db += func_group.get_double_bonds();
            }
        }

        return db;
    }


    public void add_position(int pos) {
        position += (position >= pos) ? 1 : 0;

        functional_groups.entrySet().forEach(kv -> {
            kv.getValue().forEach(fg -> {
                fg.add_position(pos);
            });
        });
    }


    public void add(FunctionalGroup fg){
        for (Enrty<Element, int> kv in fg.elements)
        {
            elements[kv.Key] += kv.Value * fg.count;
        }
    }
}
