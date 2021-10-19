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
import java.util.Map.Entry;

/**
 *
 * @author dominik
 */
public class LipidSpeciesInfo extends FattyAcid {
    public LipidLevel level;
    public int num_ethers;
    public int num_specified_fa;
    public int total_fa;
    public LipidFaBondType extended_class;
    public static final String[] ether_prefix = {"", "O-", "dO-", "tO-", "eO-"};
    public int lipid_class;

    public LipidSpeciesInfo (int _lipid_class, KnownFunctionalGroups knownFunctionalGroups){
        super("info", knownFunctionalGroups);
        lipid_class = _lipid_class;
        level = LipidLevel.NO_LEVEL;
        num_ethers = 0;
        num_specified_fa = 0;
        extended_class = LipidFaBondType.ESTER;
        total_fa = (LipidClasses.get_instance().size() > lipid_class) ? LipidClasses.get_instance().get(lipid_class).max_num_fa : 0;
    }

    @Override
    public LipidSpeciesInfo copy(){
        LipidSpeciesInfo lsi = new LipidSpeciesInfo(lipid_class, knownFunctionalGroups);
        lsi.level = level;
        lsi.num_ethers = num_ethers;
        lsi.num_specified_fa = num_specified_fa;
        lsi.position = position;
        lsi.total_fa = total_fa;
        lsi.extended_class = extended_class;
        lsi.num_carbon = num_carbon;
        lsi.double_bonds = double_bonds.copy();
        lsi.lipid_FA_bond_type = lipid_FA_bond_type;

        functional_groups.entrySet().stream().map(kv -> {
            lsi.functional_groups.put(kv.getKey(), new ArrayList<>());
            return kv;
        }).forEachOrdered(kv -> {
            kv.getValue().forEach(func_group -> {
                lsi.functional_groups.get(kv.getKey()).add(func_group.copy());
            });
        });
        return lsi;
    }


    @Override
    public ElementTable get_elements(){
        ElementTable elements = super.get_elements();
        if (lipid_FA_bond_type != LipidFaBondType.LCB_EXCEPTION) elements.put(Element.O, elements.get(Element.O) - ((num_ethers == 0) ? 1 : 0));
        elements.put(Element.H, elements.get(Element.H) + (num_ethers == 0 ? 1 : -1));

        return elements;
    }


    public void add(FattyAcid _fa){
        if (_fa.lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMENYL || _fa.lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMANYL){
            num_ethers += 1;
            lipid_FA_bond_type = LipidFaBondType.ETHER_PLASMANYL;
            extended_class = _fa.lipid_FA_bond_type;
        }
        else if (_fa.lipid_FA_bond_type == LipidFaBondType.LCB_EXCEPTION || _fa.lipid_FA_bond_type == LipidFaBondType.LCB_REGULAR)
        {
            lipid_FA_bond_type = _fa.lipid_FA_bond_type;
        }

        else
        {
            num_specified_fa += 1;
        }
        for (Entry<String, ArrayList<FunctionalGroup> > kv : _fa.functional_groups.entrySet()){
            if (!functional_groups.containsKey(kv.getKey())) functional_groups.put(kv.getKey(), new ArrayList<FunctionalGroup>());
            for (FunctionalGroup func_group : kv.getValue()){
                functional_groups.get(kv.getKey()).add(func_group.copy());
            }
        }

        ElementTable e = _fa.get_elements();
        num_carbon += e.get(Element.C);
        double_bonds.num_double_bonds += _fa.get_double_bonds();

    }


    public String to_string(){
        StringBuilder info_string = new StringBuilder();
        info_string.append(ether_prefix[num_ethers]);
        info_string.append(num_carbon).append(":").append(double_bonds.get_num());

        ElementTable fg_elements = get_functional_group_elements();
        for (int i = 2; i < Elements.element_order.size(); ++i){
            Element e = Elements.element_order.get(i);
            if (fg_elements.get(e) > 0){
                info_string.append(";").append(Elements.element_shortcut.get(e));
                if (fg_elements.get(e) > 1){
                    info_string.append(fg_elements.get(e));
                }
            }
        }

        return info_string.toString();
    }
}
