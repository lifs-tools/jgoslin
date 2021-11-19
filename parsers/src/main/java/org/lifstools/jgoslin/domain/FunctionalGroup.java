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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class FunctionalGroup {

    protected String name;
    protected int position = -1;
    protected int count;
    protected String stereochemistry;
    protected String ringStereo;
    protected DoubleBonds doubleBonds;
    protected boolean atomic;
    protected ElementTable elements;
    protected Map<String, ArrayList<FunctionalGroup>> functionalGroups;
    protected KnownFunctionalGroups knownFunctionalGroups;

    public FunctionalGroup(String _name, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, -1, 1, null, false, "", null, null, knownFunctionalGroups);
    }

    public FunctionalGroup(String _name, int _position, int _count, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, _position, _count, null, false, "", null, null, knownFunctionalGroups);
    }

    public FunctionalGroup(String _name, int _position, int _count, DoubleBonds _double_bonds, boolean _is_atomic, String _stereochemistry, ElementTable _elements, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, _position, _count, _double_bonds, _is_atomic, _stereochemistry, _elements, null, knownFunctionalGroups);
    }

    public FunctionalGroup(String _name, int _position, int _count, DoubleBonds _double_bonds, boolean _is_atomic, String _stereochemistry, ElementTable _elements, HashMap<String, ArrayList<FunctionalGroup>> _functional_groups, KnownFunctionalGroups knownFunctionalGroups) {
        name = _name;
        position = _position;
        count = _count;
        stereochemistry = _stereochemistry;
        ringStereo = "";
        doubleBonds = (_double_bonds != null) ? _double_bonds : new DoubleBonds(0);
        atomic = _is_atomic;
        elements = (_elements != null) ? _elements : new ElementTable();
        functionalGroups = (_functional_groups != null) ? _functional_groups : (new HashMap<>());
        this.knownFunctionalGroups = knownFunctionalGroups;
    }

    public FunctionalGroup copy() {
        DoubleBonds db = doubleBonds.copy();
        HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>();
        functionalGroups.entrySet().stream().map(kv -> {
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

        FunctionalGroup func_group_new = new FunctionalGroup(name, position, count, db, atomic, stereochemistry, e, fg, knownFunctionalGroups);
        func_group_new.ringStereo = ringStereo;
        return func_group_new;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public int getPosition() {
        return this.position;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public int getCount() {
        return this.count;
    }
    
    public String getStereochemistry() {
        return stereochemistry;
    }

    public void setStereochemistry(String stereochemistry) {
        this.stereochemistry = stereochemistry;
    }

    public String getRingStereo() {
        return ringStereo;
    }

    public void setRingStereo(String ringStereo) {
        this.ringStereo = ringStereo;
    }
    
    public DoubleBonds getDoubleBonds() {
        return this.doubleBonds;
    }
    
    public void setDoubleBonds(DoubleBonds doubleBonds) {
        this.doubleBonds = doubleBonds;
    }
    
    public void setAtomic(boolean atomic) {
        this.atomic = atomic;
    }
    
    public boolean isAtomic() {
        return this.atomic;
    }
    
    public ElementTable getElements() {
        return this.elements;
    }
    
    public void setElements(ElementTable elements) {
        this.elements = elements;
    }

    public ElementTable computeAndCopyElements() {
        computeElements();
        ElementTable _elements = elements.copy();
        _elements.add(getFunctionalGroupElements());
        return _elements;
    }

    public void shiftPositions(int shift) {
        position += shift;
        functionalGroups.entrySet().forEach(kv -> {
            kv.getValue().forEach(fg -> {
                fg.shiftPositions(shift);
            });
        });
    }
    
    public Map<String, ArrayList<FunctionalGroup>> getFunctionalGroups() {
        return functionalGroups;
    }
    
    public void setFunctionalGroups(Map<String, ArrayList<FunctionalGroup>> functionalGroups) {
        this.functionalGroups = functionalGroups;
    }

    public ElementTable getFunctionalGroupElements() {
        ElementTable _elements = new ElementTable();

        functionalGroups.entrySet().forEach(kv -> {
            kv.getValue().forEach(func_group -> {
                _elements.add(func_group.computeAndCopyElements(), func_group.count);
            });
        });

        return _elements;
    }

    public void computeElements() {
        functionalGroups.entrySet().forEach(kv -> {
            kv.getValue().forEach(func_group -> {
                func_group.computeElements();
            });
        });
    }

    public String toString(LipidLevel level) {
        String fg_string = "";
        if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level)) {
            if ('0' <= name.charAt(0) && name.charAt(0) <= '9') {
                fg_string = (position > -1) ? (Integer.toString(position) + ringStereo + "(" + name + ")") : name;
            } else {
                fg_string = (position > -1) ? (Integer.toString(position) + ringStereo + name) : name;
            }
        } else {
            fg_string = (count > 1) ? ("(" + name + ")" + Integer.toString(count)) : name;
        }
        if (stereochemistry.length() > 0 && level == LipidLevel.COMPLETE_STRUCTURE) {
            fg_string += "[" + stereochemistry + "]";
        }

        return fg_string;
    }

    public int getNDoubleBonds() throws ConstraintViolationException {
        int db = count * doubleBonds.getNumDoubleBonds();
        for (Entry<String, ArrayList<FunctionalGroup>> kv : functionalGroups.entrySet()) {
            for (FunctionalGroup func_group : kv.getValue()) {
                db += func_group.getNDoubleBonds();
            }
        }

        return db;
    }

    public void addPosition(int pos) {
        position += (position >= pos) ? 1 : 0;

        functionalGroups.entrySet().forEach(kv -> {
            kv.getValue().forEach(fg -> {
                fg.addPosition(pos);
            });
        });
    }

    public void add(FunctionalGroup fg) {
        elements.add(fg.elements);
    }

    public void addFunctionalGroup(FunctionalGroup fg) {

    }
    
}
