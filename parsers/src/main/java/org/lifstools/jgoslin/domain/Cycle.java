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
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author dominik
 */
public final class Cycle extends FunctionalGroup {

    private int cycle;
    private int start;
    private int end;
    private final ArrayList<Element> bridgeChain;

    public Cycle(int _cycle, KnownFunctionalGroups knownFunctionalGroups) {
        this(_cycle, -1, -1, null, null, null, knownFunctionalGroups);
    }

    public Cycle(int _cycle, int _start, int _end, KnownFunctionalGroups knownFunctionalGroups) {
        this(_cycle, _start, _end, null, null, null, knownFunctionalGroups);
    }
    
    public Cycle(int _cycle, int _start, int _end, HashMap<String, ArrayList< FunctionalGroup>> _functional_groups, KnownFunctionalGroups knownFunctionalGroups) {
        this(_cycle, _start, _end, null, _functional_groups, null, knownFunctionalGroups);
    
    }
    
    public Cycle(int _cycle, int _start, int _end, DoubleBonds _double_bonds, HashMap<String, ArrayList< FunctionalGroup>> _functional_groups, KnownFunctionalGroups knownFunctionalGroups) {
        this(_cycle, _start, _end, _double_bonds, _functional_groups, null, knownFunctionalGroups);
    
    }

    public Cycle(int _cycle, int _start, int _end, DoubleBonds _double_bonds, HashMap<String, ArrayList< FunctionalGroup>> _functional_groups, ArrayList< Element> _bridge_chain, KnownFunctionalGroups knownFunctionalGroups) {
        super("cy", _start, 1, _double_bonds, false, "", null, _functional_groups, knownFunctionalGroups);
        cycle = _cycle;
        start = _start;
        end = _end;
        elements.put(Element.H, elements.get(Element.H) - 2);
        bridgeChain = (_bridge_chain == null) ? new ArrayList<>() : _bridge_chain;
    }

    @Override
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
        ArrayList<Element> bc = new ArrayList<>();
        for (Element e : bridgeChain) {
            bc.add(e);
        }

        return new Cycle(cycle, start, end, db, fg, bc, knownFunctionalGroups);
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public ArrayList<Element> getBridgeChain() {
        return bridgeChain;
    }

    @Override
    public int getNDoubleBonds() throws ConstraintViolationException {
        return super.getNDoubleBonds() + 1;
    }

    @Override
    public void addPosition(int pos) {
        start += (start >= pos) ? 1 : 0;
        end += (end >= pos) ? 1 : 0;
        super.addPosition(pos);
    }

    public void rearrangeFunctionalGroups(FunctionalGroup parent, int shift) {
        // put everything back into parent
        for (Entry<Integer, String> kv : doubleBonds.doubleBondPositions.entrySet()) {
            parent.doubleBonds.doubleBondPositions.put(kv.getKey(), kv.getValue());
        }
        doubleBonds = new DoubleBonds();

        for (Entry<String, ArrayList<FunctionalGroup>> kv : functionalGroups.entrySet()) {
            if (!parent.functionalGroups.containsKey(kv.getKey())) {
                parent.functionalGroups.put(kv.getKey(), new ArrayList<>());
            }
            parent.functionalGroups.get(kv.getKey()).addAll(functionalGroups.get(kv.getKey()));
        }
        functionalGroups = new HashMap<>();

        // shift the cycle
        shiftPositions(shift);

        // take back what's mine# check double bonds
        parent.doubleBonds.doubleBondPositions.entrySet().stream().filter(kv -> (start <= kv.getKey() && kv.getKey() <= end)).forEachOrdered(kv -> {
            doubleBonds.doubleBondPositions.put(kv.getKey(), kv.getValue());
        });
        doubleBonds.setNumDoubleBonds(doubleBonds.doubleBondPositions.size());

        doubleBonds.doubleBondPositions.entrySet().forEach(kv -> {
            parent.doubleBonds.doubleBondPositions.remove(kv.getKey());
        });
        parent.doubleBonds.setNumDoubleBonds(parent.doubleBonds.doubleBondPositions.size());

        HashSet<String> remove_list = new HashSet<>();
        for (Entry<String, ArrayList<FunctionalGroup>> kv : parent.functionalGroups.entrySet()) {
            ArrayList<Integer> remove_item = new ArrayList<>();

            int i = 0;
            for (FunctionalGroup func_group : kv.getValue()) {
                if (start <= func_group.getPosition() && func_group.getPosition() <= end && func_group != this) {
                    if (!functionalGroups.containsKey(kv.getKey())) {
                        functionalGroups.put(kv.getKey(), new ArrayList<>());
                    }
                    functionalGroups.get(kv.getKey()).add(func_group);
                    remove_item.add(i);
                }
                ++i;
            }

            while (remove_item.size() > 0) {
                int pos = remove_item.get(remove_item.size() - 1);
                remove_item.remove(remove_item.size() - 1);
                kv.getValue().remove(pos);
            }
            if (kv.getValue().isEmpty()) {
                remove_list.add(kv.getKey());
            }
        }

        for (String fg : remove_list) {
            parent.functionalGroups.remove(fg);
        }
    }

    @Override
    public void shiftPositions(int shift) {
        super.shiftPositions(shift);
        start += shift;
        end += shift;
        DoubleBonds db = new DoubleBonds();
        for (Entry<Integer, String> kv : doubleBonds.doubleBondPositions.entrySet()) {
            db.doubleBondPositions.put(kv.getKey() + shift, kv.getValue());
        }
        db.setNumDoubleBonds(db.doubleBondPositions.size());
        doubleBonds = db;
    }

    @Override
    public void computeElements() {
        elements = new ElementTable();
        elements.put(Element.H, -2 - 2 * doubleBonds.getNumDoubleBonds());

        for (Element chain_element : bridgeChain) {
            try {
                switch (chain_element) {
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
            } catch (Exception e) {
                throw new ConstraintViolationException("Element '" + Elements.ELEMENT_SHORTCUT.get(chain_element) + "' cannot be part of a cycle bridge", e);
            }
        }

        // add all implicit carbon chain elements
        if (start != -1 && end != -1) {
            int n = Math.max(0, cycle - (end - start + 1 + bridgeChain.size()));
            elements.put(Element.C, elements.get(Element.C) + n);
            elements.put(Element.H, elements.get(Element.H) + 2 * n);
        }
    }

    @Override
    public String toString(LipidLevel level) {
        StringBuilder cycle_string = new StringBuilder();
        cycle_string.append("[");
        if (start != -1 && LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level)) {
            cycle_string.append(start).append("-").append(end);
        }

        if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level) && bridgeChain.size() > 0) {
            for (Element e : bridgeChain) {
                cycle_string.append(Elements.ELEMENT_SHORTCUT.get(e));
            }
        }
        cycle_string.append("cy").append(cycle);
        cycle_string.append(":").append(doubleBonds.getNumDoubleBonds());

        if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level)) {
            if (doubleBonds.doubleBondPositions.size() > 0) {
                int i = 0;
                cycle_string.append("(");
                ArrayList<Integer> sorted = new ArrayList<>(doubleBonds.doubleBondPositions.keySet());
                Collections.sort(sorted, (a, b) -> (int) a - (int) b);
                for (int key : sorted) {
                    String value = doubleBonds.doubleBondPositions.get(key);
                    if (i++ > 0) {
                        cycle_string.append(",");
                    }
                    if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level)) {
                        cycle_string.append(key).append(value);
                    } else {
                        cycle_string.append(key);
                    }
                }
                cycle_string.append(")");
            }
        }

        if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level)) {
            ArrayList<String> fg_names = new ArrayList<>(functionalGroups.keySet());
            Collections.sort(fg_names, (String a, String b) -> a.toLowerCase().compareTo(b.toLowerCase()));

            for (String fg : fg_names) {
                ArrayList<FunctionalGroup> fg_list = functionalGroups.get(fg);
                if (fg_list.isEmpty()) {
                    continue;
                }

                Collections.sort(fg_list, (FunctionalGroup a, FunctionalGroup b) -> a.getPosition() - b.getPosition());
                int i = 0;
                cycle_string.append(";");
                for (FunctionalGroup func_group : fg_list) {
                    if (i++ > 0) {
                        cycle_string.append(",");
                    }
                    cycle_string.append(func_group.toString(level));
                }
            }
        } else if (level == LipidLevel.STRUCTURE_DEFINED) {
            ArrayList<String> fg_names = new ArrayList<>(functionalGroups.keySet());
            Collections.sort(fg_names, (String a, String b) -> a.toLowerCase().compareTo(b.toLowerCase()));

            for (String fg : fg_names) {
                ArrayList<FunctionalGroup> fg_list = functionalGroups.get(fg);
                if (fg_list.size() > 0) {
                    if (fg_list.size() == 1 && fg_list.get(0).getCount() == 1) {
                        cycle_string.append(";").append(fg_list.get(0).toString(level));
                    } else {
                        int fg_count = 0;
                        for (FunctionalGroup func_group : fg_list) {
                            fg_count += func_group.getCount();
                        }
                        if (fg_count > 1) {
                            cycle_string.append(";(").append(fg).append(")").append(fg_count);
                        } else {
                            cycle_string.append(";").append(fg);
                        }
                    }
                }
            }
        }

        cycle_string.append("]");
        if (getStereochemistry().length() > 0) {
            cycle_string.append("[").append(getStereochemistry()).append("]");
        }

        return cycle_string.toString();
    }
}
