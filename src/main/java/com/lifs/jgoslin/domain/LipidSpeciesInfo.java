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

    public LipidSpeciesInfo (LipidClass _lipid_class) : base("info")
    {
        lipid_class = _lipid_class;
        level = LipidLevel.NO_LEVEL;
        num_ethers = 0;
        num_specified_fa = 0;
        extended_class = LipidFaBondType.ESTER;
        ClassMap lipid_classes = LipidClasses.lipid_classes;
        total_fa = lipid_classes.ContainsKey(lipid_class) ? lipid_classes[lipid_class].max_num_fa : 0;
    }


    public LipidSpeciesInfo copy()
    {
        LipidSpeciesInfo lsi = new LipidSpeciesInfo(lipid_class);
        lsi.level = level;
        lsi.num_ethers = num_ethers;
        lsi.num_specified_fa = num_specified_fa;
        lsi.position = position;
        lsi.total_fa = total_fa;
        lsi.extended_class = extended_class;
        lsi.num_carbon = num_carbon;
        lsi.double_bonds = double_bonds.copy();
        lsi.lipid_FA_bond_type = lipid_FA_bond_type;

        foreach (KeyValuePair<string, List<FunctionalGroup>> kv in functional_groups)
        {
            lsi.functional_groups.Add(kv.Key, new List<FunctionalGroup>());
            foreach (FunctionalGroup func_group in kv.Value)
            {
                lsi.functional_groups[kv.Key].Add(func_group.copy());
            }
        }
        return lsi;
    }


    public override ElementTable get_elements()
    {
        ElementTable elements = base.get_elements();
        if (lipid_FA_bond_type != LipidFaBondType.LCB_EXCEPTION) elements[Element.O] -= (num_ethers == 0) ? 1 : 0;
        elements[Element.H] += num_ethers == 0 ? 1 : -1;

        return elements;
    }


    public void add(FattyAcid _fa)
    {
        if (_fa.lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMENYL || _fa.lipid_FA_bond_type == LipidFaBondType.ETHER_PLASMANYL)
        {
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
        foreach (KeyValuePair<string, List<FunctionalGroup> > kv in  _fa.functional_groups)
        {
            if (!functional_groups.ContainsKey(kv.Key)) functional_groups.Add(kv.Key, new List<FunctionalGroup>());
            foreach (FunctionalGroup func_group in kv.Value)
            {
                functional_groups[kv.Key].Add(func_group.copy());
            }
        }

        ElementTable e = _fa.get_elements();
        num_carbon += e[Element.C];
        double_bonds.num_double_bonds += _fa.get_double_bonds();

    }


    public string to_string()
    {
        StringBuilder info_string = new StringBuilder();
        info_string.Append(ether_prefix[num_ethers]);
        info_string.Append(num_carbon).Append(":").Append(double_bonds.get_num());

        ElementTable elements = get_functional_group_elements();
        for (int i = 2; i < Elements.element_order.Count; ++i)
        {
            Element e = Elements.element_order[i];
            if (elements[e] > 0)
            {
                info_string.Append(";").Append(Elements.element_shortcut[e]);
                if (elements[e] > 1)
                {
                    info_string.Append(elements[e]);
                }
            }
        }

        return info_string.ToString();
    }
}
