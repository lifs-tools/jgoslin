/*
 * Copyright 2021 Dominik Kopczynski, Nils Hoffmann.
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
package org.lifstools.jgoslin.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A fatty acid with a specific type. This object defines the name, position,
 * number of carbon atoms, hydroxyls and double bonds, as well as the bond type
 * to the head group. A FattyAcid can carry optional modifications / functional groups and can
 * report double bond positions.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class FattyAcid extends FunctionalGroup {
    
    public static final Set<String> fgExceptions = new HashSet<>(Arrays.asList("acyl", "alkyl", "cy", "cc", "acetoxy"));
    public static final Set<LipidFaBondType> LCB_STATES = new HashSet<>(Arrays.asList(LipidFaBondType.LCB_REGULAR, LipidFaBondType.LCB_EXCEPTION));
    protected int numCarbon;
    protected LipidFaBondType lipidFaBondType;
    
    public FattyAcid(String _name, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, 0, null, null, LipidFaBondType.ESTER, 0, knownFunctionalGroups);
    }

    public FattyAcid(String _name, int _num_carbon, DoubleBonds _double_bonds) {
        this(_name, _num_carbon, _double_bonds, null, LipidFaBondType.ESTER, 0, null);
    }

    public FattyAcid(String _name, int _num_carbon, DoubleBonds _double_bonds, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, _num_carbon, _double_bonds, null, LipidFaBondType.ESTER, 0, knownFunctionalGroups);
    }

    public FattyAcid(String _name, int _num_carbon, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, _num_carbon, null, null, LipidFaBondType.ESTER, 0, knownFunctionalGroups);
    }

    public FattyAcid(String _name, int _num_carbon, DoubleBonds _double_bonds, HashMap<String, ArrayList<FunctionalGroup>> _functional_groups, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, _num_carbon, _double_bonds, _functional_groups, LipidFaBondType.ESTER, 0, knownFunctionalGroups);
    }

    public FattyAcid(String _name, int _num_carbon, DoubleBonds _double_bonds, HashMap<String, ArrayList<FunctionalGroup>> _functional_groups, LipidFaBondType _lipid_FA_bond_type, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, _num_carbon, _double_bonds, _functional_groups, _lipid_FA_bond_type, 0, knownFunctionalGroups);
    }

    public FattyAcid(String _name, int _num_carbon, DoubleBonds _double_bonds, HashMap<String, ArrayList<FunctionalGroup>> _functional_groups, LipidFaBondType _lipid_FA_bond_type, int _position, KnownFunctionalGroups knownFunctionalGroups) {
        super(_name, _position, 1, _double_bonds, false, "", null, _functional_groups, knownFunctionalGroups);
        numCarbon = _num_carbon;
        lipidFaBondType = _lipid_FA_bond_type;

        if (lipidFaBondType == LipidFaBondType.LCB_REGULAR) {
            functionalGroups.put("[X]", new ArrayList<>());
            functionalGroups.get("[X]").add(knownFunctionalGroups.get("X"));
        }

        if (numCarbon < 0 || numCarbon == 1) {
            throw new ConstraintViolationException("FattyAcid must have at least 2 carbons! Got " + Integer.toString(numCarbon));
        }

        if (getPosition() < 0) {
            throw new ConstraintViolationException("FattyAcid position must be greater or equal to 0! Got " + Integer.toString(getPosition()));
        }

        if (doubleBonds.getNumDoubleBonds() < 0) {
            throw new ConstraintViolationException("FattyAcid must have at least 0 double bonds! Got " + Integer.toString(doubleBonds.getNumDoubleBonds()));
        }
    }

    @Override
    public FunctionalGroup copy() {
        DoubleBonds db = doubleBonds.copy();
        HashMap<String, ArrayList<FunctionalGroup>> fg = new HashMap<>();
        for (Entry<String, ArrayList<FunctionalGroup>> kv : functionalGroups.entrySet()) {
            fg.put(kv.getKey(), new ArrayList<>());
            for (FunctionalGroup func_group : kv.getValue()) {
                fg.get(kv.getKey()).add(func_group.copy());
            }
        }

        return new FattyAcid(getName(), numCarbon, db, fg, lipidFaBondType, getPosition(), knownFunctionalGroups);
    }

    public void setType(LipidFaBondType _lipid_FA_bond_type) {
        lipidFaBondType = _lipid_FA_bond_type;
        if (lipidFaBondType == LipidFaBondType.LCB_REGULAR && !functionalGroups.containsKey("[X]")) {
            functionalGroups.put("[X]", new ArrayList<>());
            functionalGroups.get("[X]").add(knownFunctionalGroups.get("X"));
        } else if (functionalGroups.containsKey("[X]")) {
            functionalGroups.remove("[X]");
        }

        setName((lipidFaBondType != LipidFaBondType.LCB_EXCEPTION && lipidFaBondType != LipidFaBondType.LCB_REGULAR) ? "FA" : "LCB");
    }

    public String getPrefix(LipidFaBondType lipid_FA_bond_type) {
        switch (lipid_FA_bond_type) {
            case ETHER_PLASMANYL:
                return "O-";
            case ETHER_PLASMENYL:
                return "P-";
            default:
                return "";
        }
    }

    @Override
    public int getNDoubleBonds() {
        return super.getNDoubleBonds() + ((lipidFaBondType == LipidFaBondType.ETHER_PLASMENYL) ? 1 : 0);
    }

    public boolean lipidFaBondTypePrefix(LipidFaBondType lipid_FA_bond_type) {
        return (lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMANYL) || (lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMENYL) || (lipid_FA_bond_type == LipidFaBondType.ETHER_UNSPECIFIED);
    }

    @Override
    public String toString(LipidLevel level) {
        StringBuilder fa_string = new StringBuilder();
        fa_string.append(getPrefix(lipidFaBondType));
        int num_carbons = numCarbon;
        int num_double_bonds = doubleBonds.getNumDoubleBonds();

        if (num_carbons == 0 && num_double_bonds == 0 && !LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level | LipidLevel.SN_POSITION.level)) {
            return "";
        }

        if (LipidLevel.isLevel(level, LipidLevel.SN_POSITION.level | LipidLevel.MOLECULAR_SPECIES.level)) {
            ElementTable e = computeAndCopyElements();
            num_carbons = e.get(Element.C);
            num_double_bonds = getNDoubleBonds() - ((lipidFaBondType == LipidFaBondType.ETHER_PLASMENYL) ? 1 : 0);
        }

        fa_string.append(num_carbons).append(":").append(num_double_bonds);

        if (!LipidLevel.isLevel(level, LipidLevel.SN_POSITION.level | LipidLevel.MOLECULAR_SPECIES.level) && doubleBonds.doubleBondPositions.size() > 0) {
            fa_string.append("(");

            int i = 0;
            ArrayList<Integer> sorted_db = new ArrayList<>(doubleBonds.doubleBondPositions.keySet());
            Collections.sort(sorted_db, (a, b) -> (int) a - (int) b);
            for (int db_pos : sorted_db) {
                if (i++ > 0) {
                    fa_string.append(",");
                }
                fa_string.append(db_pos);
                if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level)) {
                    fa_string.append(doubleBonds.doubleBondPositions.get(db_pos));
                }
            }
            fa_string.append(")");
        }
        
        
        if (level == LipidLevel.COMPLETE_STRUCTURE && stereochemistry.length() > 0){
            fa_string.append("[").append(stereochemistry).append("]");
        }
        

        if (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level)) {
            ArrayList<String> fg_names = new ArrayList<>();
            for (Entry<String, ArrayList<FunctionalGroup>> kv : functionalGroups.entrySet()) {
                fg_names.add(kv.getKey());
            }
            Collections.sort(fg_names, (String a, String b) -> a.toLowerCase().compareTo(b.toLowerCase()));

            for (String fg : fg_names) {
                if (fg.equals("[X]")) {
                    continue;
                }
                ArrayList<FunctionalGroup> fg_list = functionalGroups.get(fg);
                if (fg_list.isEmpty()) {
                    continue;
                }

                Collections.sort(fg_list, (FunctionalGroup a, FunctionalGroup b) -> a.getPosition() - b.getPosition());

                int i = 0;
                fa_string.append(";");
                for (FunctionalGroup func_group : fg_list) {
                    if (i++ > 0) {
                        fa_string.append(",");
                    }
                    fa_string.append(func_group.toString(level));
                }
            }
        } else if (level == LipidLevel.STRUCTURE_DEFINED) {
            ArrayList<String> fg_names = new ArrayList<>();
            functionalGroups.entrySet().forEach(kv -> {
                fg_names.add(kv.getKey());
            });
            Collections.sort(fg_names, (String a, String b) -> a.toLowerCase().compareTo(b.toLowerCase()));

            for (String fg : fg_names) {
                if (fg.equals("[X]")) {
                    continue;
                }
                ArrayList<FunctionalGroup> fg_list = functionalGroups.get(fg);
                if (fg_list.isEmpty()) {
                    continue;
                }

                if (fgExceptions.contains(fg)) {
                    fa_string.append(";");
                    int i = 0;
                    for (FunctionalGroup func_group : fg_list) {
                        if (i++ > 0) {
                            fa_string.append(",");
                        }
                        fa_string.append(func_group.toString(level));
                    }
                } else {
                    int fg_count = 0;
                    for (FunctionalGroup func_group : fg_list) {
                        fg_count += func_group.getCount();
                    }

                    if (fg_count > 1) {
                        fa_string.append(";").append(!fg_list.get(0).atomic ? ("(" + fg + ")" + Integer.toString(fg_count)) : (fg + Integer.toString(fg_count)));
                    } else {
                        fa_string.append(";").append(fg);
                    }
                }
            }

        } else {
            ElementTable func_elements = getFunctionalGroupElements();
            for (int i = 2; i < Elements.ELEMENT_ORDER.size(); ++i) {
                Element e = Elements.ELEMENT_ORDER.get(i);
                if (func_elements.get(e) > 0) {
                    fa_string.append(";").append(Elements.ELEMENT_SHORTCUT.get(e));
                    if (func_elements.get(e) > 1) {
                        fa_string.append(func_elements.get(e));
                    }
                }
            }
        }

        return fa_string.toString();
    }

    @JsonIgnore
    @Override
    public ElementTable getFunctionalGroupElements() {
        ElementTable fgElements = super.getFunctionalGroupElements();

        // subtract the invisible [X] functional group for regular LCBs
        if (lipidFaBondType == LipidFaBondType.LCB_REGULAR && functionalGroups.containsKey("O")) {
            fgElements.put(Element.O, fgElements.get(Element.O) - 1);
        }

        return fgElements;
    }

    @Override
    public void computeElements() {
        elements = new ElementTable();

        int num_double_bonds = doubleBonds.getNumDoubleBonds();
        if (lipidFaBondType == LipidFaBondType.ETHER_PLASMENYL) {
            num_double_bonds += 1;
        }

        if (numCarbon == 0 && num_double_bonds == 0) {
            elements.put(Element.H, 1);
            return;
        }

        if (lipidFaBondType != LipidFaBondType.LCB_EXCEPTION && lipidFaBondType != LipidFaBondType.LCB_REGULAR) {
            elements.put(Element.C, numCarbon); // carbon
            switch (lipidFaBondType) {
                case ESTER:
                    elements.put(Element.H, (2 * numCarbon - 1 - 2 * num_double_bonds)); // hydrogen
                    elements.put(Element.O, 1); // oxygen
                    break;
                case ETHER_PLASMENYL:
                    elements.put(Element.H, (2 * numCarbon - 1 - 2 * num_double_bonds + 2)); // hydrogen
                    break;
                case ETHER, ETHER_PLASMANYL:
                    elements.put(Element.H, ((numCarbon + 1) * 2 - 1 - 2 * num_double_bonds)); // hydrogen
                    break;
                case AMIDE:
                    elements.put(Element.H, (2 * numCarbon + 1 - 2 * num_double_bonds) - 1); // hydrogen
                    break;
                default:
                    throw new LipidException("Mass cannot be computed for fatty acyl chain with this bond type");
            }
        } else {
            // long chain base
            elements.put(Element.C, numCarbon); // carbon
            elements.put(Element.H, (2 * (numCarbon - num_double_bonds) + 1)); // hydrogen
            elements.put(Element.N, 1); // nitrogen
        }
    }

    public int getNumCarbon() {
        return numCarbon;
    }

    public void setNumCarbon(int numCarbon) {
        this.numCarbon = numCarbon;
    }

    public LipidFaBondType getLipidFaBondType() {
        return lipidFaBondType;
    }

    public void setLipidFaBondType(LipidFaBondType lipidFaBondType) {
        this.lipidFaBondType = lipidFaBondType;
    }
    
}
