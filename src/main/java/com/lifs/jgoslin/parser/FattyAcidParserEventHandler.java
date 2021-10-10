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

package com.lifs.jgoslin.parser;

import com.lifs.jgoslin.antlr.*;
import com.lifs.jgoslin.domain.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 *
 * @author dominik
 */
public class FattyAcidParserEventHandler extends FattyAcidsBaseListener implements BaseParserEventHandler<LipidAdduct> {
    public LipidAdduct content;
    public LipidLevel level;
    public String headgroup;
    public ExtendedList<FattyAcid> fatty_acyl_stack;
    public Dictionary tmp;

    public static final HashMap<String, Integer> last_numbers = new HashMap<>(){{
        put("un", 1); put("hen", 1);
        put("do", 2); put("di", 2);
        put("tri", 3);
        put("buta", 4); put("but", 4); put("tetra", 4);
        put("penta", 5); put("pent", 5);
        put("hexa", 6); put("hex", 6);
        put("hepta", 7); put("hept", 7);
        put("octa", 8); put("oct", 8);
        put("nona", 9); put("non", 9);
    }};

    public static final HashMap<String, Integer> second_numbers = new HashMap<>(){{
        put("deca", 10); put("dec", 10); put("eicosa", 20); put("eicos", 20);
        put("cosa", 20); put("cos", 20); put("triaconta", 30); put("triacont", 30);
        put("tetraconta", 40); put("tetracont", 40); put("pentaconta", 50);
        put("pentacont", 50); put("hexaconta", 60); put("hexacont", 60);
        put("heptaconta", 70); put("heptacont", 70); put("octaconta", 80);
        put("octacont", 80); put("nonaconta", 90); put("nonacont", 90);
    }};

    public static final HashMap<String, String> func_groups = new HashMap<>(){{
        put("keto", "oxo"); put("ethyl", "Et"); put("hydroxy", "OH"); put("phospho", "Ph");
        put("oxo", "oxo"); put("bromo", "Br"); put("methyl", "Me"); put("hydroperoxy", "OOH");
        put("homo", ""); put("Epoxy", "Ep"); put("fluro", "F"); put("fluoro", "F");
        put("chloro", "Cl"); put("methylene", "My"); put("sulfooxy", "Su");
        put("amino", "NH2"); put("sulfanyl", "SH"); put("methoxy", "OMe");
        put("iodo", "I"); put("cyano", "CN"); put("nitro", "NO2"); put("OH", "OH");
        put("thio", "SH"); put("mercapto", "SH"); put("carboxy", "COOH");
        put("acetoxy", "Ac"); put("cysteinyl", "Cys"); put("phenyl", "Phe");
        put("s-glutathionyl", "SGlu"); put("s-cysteinyl", "SCys");
        put("butylperoxy", "BOO"); put("dimethylarsinoyl", "MMAs"); put("methylsulfanyl", "SMe");
        put("imino", "NH"); put("s-cysteinylglycinyl", "SCG");
    }};

    public static final HashMap<String, Integer> ate = new HashMap<>(){{
        put("formate", 1); put("acetate", 2); put("butyrate", 4); put("propionate", 3);
        put("valerate", 5); put("isobutyrate", 4);
    }};

    public static final HashMap<String, Integer> special_numbers = new HashMap<>(){{
        put("meth", 1); put("etha", 2); put("eth", 2); put("propa", 3); put("isoprop", 3);
        put("prop", 3); put("propi", 3); put("propio", 3); put("buta", 4); put("but", 4);
        put("butr", 4); put("furan", 5); put("valer", 5); put("eicosa", 20); put("eicos", 20);
        put("icosa", 20); put("icos", 20); put("prosta", 20); put("prost", 20); put("prostan", 20);
    }};

    public static final HashSet<String> noic_set = new HashSet<>(Arrays.asList("noic acid", "nic acid", "dioic_acid"));
    public static final HashSet<String> nal_set = new HashSet<>(Arrays.asList("nal", "dial"));
    public static final HashSet<String> acetate_set = new HashSet<>(Arrays.asList("acetate", "noate", "nate"));
    
    public FattyAcidParserEventHandler(){
        content = null;
    }
    
    public String FA_I(){
        return "fa" + Integer.toString(fatty_acyl_stack.size());
    }
    
    @Override
    public void set_content(LipidAdduct l){
        content = l;
    }
    
    @Override
    public LipidAdduct get_content(){
        return content;
    }


    @Override
    public void set_lipid_level(LipidLevel _level){
        level = level.level < _level.level ? level : _level;
    }
    
    
    @Override public void enterLipid(FattyAcidsParser.LipidContext node) {
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        headgroup = "";
        fatty_acyl_stack = new ExtendedList<>();
        fatty_acyl_stack.add(new FattyAcid("FA"));
        tmp = new Dictionary();
        tmp.put("fa1", new Dictionary());
    }
    
    
    @Override public void exitLipid(FattyAcidsParser.LipidContext node) {
        if (tmp.containsKey("cyclo_yl")){
            tmp.put("fg_pos", new GenericList());

            GenericList l1 = new GenericList();
            l1.add(1);
            l1.add("");
            ((GenericList)tmp.get("fg_pos")).add(l1);

            GenericList l2 = new GenericList();
            l2.add((int)tmp.get("cyclo_len"));
            l2.add("");
            ((GenericList)tmp.get("fg_pos")).add(l2);

            add_cyclo(node);
            tmp.remove("cyclo_yl");
            tmp.remove("cyclo_len");
        }



        if (tmp.containsKey("post_adding"))
        {
            FattyAcid curr_fa_p = fatty_acyl_stack.back();
            int s = ((GenericList)tmp.get("post_adding")).size();
            curr_fa_p.num_carbon += s;
            for (int i = 0; i < s; ++i){
                int pos = (int)((GenericList)tmp.get("post_adding")).get(i);
                curr_fa_p.add_position(pos);
                DoubleBonds db = new DoubleBonds(curr_fa_p.double_bonds.num_double_bonds);
                for (Entry<Integer, String> kv : curr_fa_p.double_bonds.double_bond_positions.entrySet()){
                    db.double_bond_positions.put(kv.getKey() + (kv.getKey() >= pos ? 1 : 0), kv.getValue());
                }
                db.num_double_bonds = db.double_bond_positions.size();
                curr_fa_p.double_bonds = db;
            }
        }

        FattyAcid curr_fa = fatty_acyl_stack.back();
        if (curr_fa.double_bonds.double_bond_positions.size() > 0){
            int db_right = 0;
            for (Entry<Integer, String> kv : curr_fa.double_bonds.double_bond_positions.entrySet()) db_right += kv.getValue().length() > 0 ? 1 : 0;
            if (db_right != curr_fa.double_bonds.double_bond_positions.size()){
                set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
            }
        }

        Headgroup head_group = new Headgroup(headgroup);

        content = new LipidAdduct();

        switch(level)
        {

            case COMPLETE_STRUCTURE: content.lipid = new LipidCompleteStructure(head_group, fatty_acyl_stack); break;
            case FULL_STRUCTURE: content.lipid = new LipidFullStructure(head_group, fatty_acyl_stack); break;
            case STRUCTURE_DEFINED: content.lipid = new LipidStructureDefined(head_group, fatty_acyl_stack); break;
            case SN_POSITION: content.lipid = new LipidSnPosition(head_group, fatty_acyl_stack); break;
            case MOLECULAR_SPECIES: content.lipid = new LipidMolecularSpecies(head_group, fatty_acyl_stack); break;
            case SPECIES: content.lipid = new LipidSpecies(head_group, fatty_acyl_stack); break;
            default: break;
        }
    }
    
    public void set_fatty_acyl_type(ParserRuleContext node){
        String t = node.getText();

        if (t.endsWith("ol")) headgroup = "FOH";
        else if (noic_set.contains(t)) headgroup = "FA";
        else if (nal_set.contains(t)) headgroup = "FAL";
        else if (acetate_set.contains(t)) headgroup = "WE";
        else if (t.equals("ne")){
            headgroup = "HC";
            fatty_acyl_stack.back().lipid_FA_bond_type = LipidFaBondType.AMINE;
        }
        else {
            headgroup = t;
        }
    }
    
    public void set_fatty_acid(ParserRuleContext node){
        FattyAcid curr_fa = fatty_acyl_stack.back();
        if (tmp.containsKey("length_pattern")) {

            String length_pattern = (String)tmp.get("length_pattern");
            int[] num = new int[((GenericList)tmp.get("length_tokens")).size()];
            for (int i = 0; i < ((GenericList)tmp.get("length_tokens")).size(); ++i)
                num[i] = (int)((GenericList)tmp.get("length_tokens")).get(i);

            int l = 0, d = 0;
            if (length_pattern.equals("L") || length_pattern.equals("S")){
                l += num[0];
            }

            else if (length_pattern.equals("LS")){
                l += num[0] + num[1];
            }

            else if (length_pattern.equals("LL") || length_pattern.equals("SL") || length_pattern.equals("SS")){
                l += num[0];
                d += num[1];
            }

            else if (length_pattern.equals("LSL") || length_pattern.equals("LSS")){
                l += num[0] + num[1];
                d += num[2];
            }

            else if (length_pattern.equals("LSLS")){
                l += num[0] + num[1];
                d += num[2] + num[3];
            }

            else if (length_pattern.equals("SLS")){
                l += num[0];
                d += num[1] + num[2];
            }

            else if (length_pattern.length() > 0 && length_pattern.charAt(0) == 'X'){
                l += num[0];
                for (int i = 1; i < ((GenericList)tmp.get("length_tokens")).size(); ++i) d += num[i];
            }

            else if (length_pattern.equals("LLS")){ // false
                throw new RuntimeException("Cannot determine fatty acid and double bond length in '" + node.getText() + "'");
            }
            curr_fa.num_carbon += l;
            if (curr_fa.double_bonds.double_bond_positions.isEmpty() && d > 0) curr_fa.double_bonds.num_double_bonds = d;
        }



        if (curr_fa.functional_groups.containsKey("noyloxy")){
            if (headgroup.equals("FA")) headgroup = "FAHFA";

            while (curr_fa.functional_groups.get("noyloxy").size() > 0){
                FattyAcid fa = (FattyAcid)curr_fa.functional_groups.get("noyloxy").get(curr_fa.functional_groups.get("noyloxy").size() - 1);
                curr_fa.functional_groups.get("noyloxy").remove(curr_fa.functional_groups.get("noyloxy").size() - 1);

                AcylAlkylGroup acyl = new AcylAlkylGroup(fa);
                acyl.position = fa.position;

                if (!curr_fa.functional_groups.containsKey("acyl")) curr_fa.functional_groups.put("acyl", new ArrayList<>());
                curr_fa.functional_groups.get("acyl").add(acyl);
            }
            curr_fa.functional_groups.remove("noyloxy");
        }

        else if (curr_fa.functional_groups.containsKey("nyloxy") || curr_fa.functional_groups.containsKey("yloxy")){
            String yloxy = curr_fa.functional_groups.containsKey("nyloxy") ? "nyloxy" : "yloxy";
            while (curr_fa.functional_groups.get(yloxy).size() > 0){
                FattyAcid fa = (FattyAcid)curr_fa.functional_groups.get(yloxy).get(curr_fa.functional_groups.get(yloxy).size() - 1);
                curr_fa.functional_groups.get(yloxy).remove(curr_fa.functional_groups.get(yloxy).size() - 1);

                AcylAlkylGroup alkyl = new AcylAlkylGroup(fa, -1, 1, true);
                alkyl.position = fa.position;

                if (!curr_fa.functional_groups.containsKey("alkyl")) curr_fa.functional_groups.put("alkyl", new ArrayList<>());
                curr_fa.functional_groups.get("alkyl").add(alkyl);
            }
            curr_fa.functional_groups.remove(yloxy);
        }

        else {
            boolean has_yl = false;
            for (Entry<String, ArrayList<FunctionalGroup> > kv : curr_fa.functional_groups.entrySet()){
                if (kv.getKey().endsWith("yl")) {
                    has_yl = true;
                    break;
                }
            }
            if (has_yl){
                while (true){
                    String yl = "";
                    for (Entry<String, ArrayList<FunctionalGroup> > kv : curr_fa.functional_groups.entrySet()){
                        if (kv.getKey().endsWith("yl")){
                            yl = kv.getKey();
                            break;
                        }
                    }
                    if (yl.length() == 0){
                        break;
                    }

                    while (curr_fa.functional_groups.get(yl).size() > 0){
                        FattyAcid fa = (FattyAcid)curr_fa.functional_groups.get(yl).get(curr_fa.functional_groups.get(yl).size() - 1);
                        curr_fa.functional_groups.get(yl).remove(curr_fa.functional_groups.get(yl).size() - 1);

                        if (tmp.containsKey("cyclo")){
                            int cyclo_len = curr_fa.num_carbon;
                            tmp.put("cyclo_len", cyclo_len);
                            if (fa.position != cyclo_len && !tmp.containsKey("furan")){
                                switch_position(curr_fa, 2 + cyclo_len);
                            }
                            fa.shift_positions(cyclo_len);
                            if (tmp.containsKey("furan")) curr_fa.shift_positions(-1);

                            for (Entry<String, ArrayList<FunctionalGroup> > kv : fa.functional_groups.entrySet()){
                                if (!curr_fa.functional_groups.containsKey(kv.getKey())){
                                    curr_fa.functional_groups.put(kv.getKey(), new ArrayList<>());
                                }
                                for(FunctionalGroup func_group : kv.getValue()) curr_fa.functional_groups.get(kv.getKey()).add(func_group);
                            }

                            curr_fa.num_carbon = cyclo_len + fa.num_carbon;

                            for (Entry<Integer, String> kv : fa.double_bonds.double_bond_positions.entrySet()){
                                curr_fa.double_bonds.double_bond_positions.put(kv.getKey() + cyclo_len, kv.getValue());
                            }
                            curr_fa.double_bonds.num_double_bonds = curr_fa.double_bonds.double_bond_positions.size();

                            if (!tmp.containsKey("tetrahydrofuran") && tmp.containsKey("furan")){
                                curr_fa.double_bonds.num_double_bonds += 2;
                                if (!curr_fa.double_bonds.double_bond_positions.containsKey(1)) curr_fa.double_bonds.double_bond_positions.put(1, "E");
                                if (!curr_fa.double_bonds.double_bond_positions.containsKey(3)) curr_fa.double_bonds.double_bond_positions.put(3, "E");
                            }

                            tmp.put("cyclo_yl", true);
                        }
                        else {
                            // add carbon chains here here
                            // special chains: i.e. ethyl, methyl
                            String fg_name = "";
                            if (fa.double_bonds.get_num() == 0 && fa.functional_groups.isEmpty()){
                                FunctionalGroup fg = null;
                                if (fa.num_carbon == 1){
                                    fg_name = "Me";
                                    fg = KnownFunctionalGroups.get_instance().get(fg_name);
                                }
                                else if (fa.num_carbon == 2){
                                    fg_name = "Et";
                                    fg = KnownFunctionalGroups.get_instance().get(fg_name);
                                }
                                if (fg_name.length() > 0){
                                    fg.position = fa.position;
                                    if (!curr_fa.functional_groups.containsKey(fg_name)) curr_fa.functional_groups.put(fg_name, new ArrayList<>());
                                    curr_fa.functional_groups.get(fg_name).add(fg);
                                }
                            }
                            if (fg_name.length() == 0){
                                CarbonChain cc = new CarbonChain(fa, fa.position);
                                if (!curr_fa.functional_groups.containsKey("cc")) curr_fa.functional_groups.put("cc", new ArrayList<>());
                                curr_fa.functional_groups.get("cc").add(cc);
                            }
                        }
                    }
                    if (tmp.containsKey("cyclo")) tmp.remove("cyclo");
                    curr_fa.functional_groups.remove(yl);
                }
            }
        }

        if (curr_fa.functional_groups.containsKey("cyclo")){
            FattyAcid fa = (FattyAcid)curr_fa.functional_groups.get("cyclo").get(0);
            curr_fa.functional_groups.remove("cyclo");
            if (!tmp.containsKey("cyclo_len")) tmp.put("cyclo_len", 5);
            int start_pos = curr_fa.num_carbon + 1;
            int end_pos = curr_fa.num_carbon + (int)tmp.get("cyclo_len");
            fa.shift_positions(start_pos - 1);

            if (curr_fa.functional_groups.containsKey("cy")){
                for (FunctionalGroup cy : curr_fa.functional_groups.get("cy")){
                    cy.shift_positions(start_pos - 1);
                }
            }
            for (Entry<String, ArrayList<FunctionalGroup> > kv : fa.functional_groups.entrySet()){
                if (!curr_fa.functional_groups.containsKey(kv.getKey())){
                    curr_fa.functional_groups.put(kv.getKey(), new ArrayList<>());
                }
                for (FunctionalGroup func_group : kv.getValue()){
                    curr_fa.functional_groups.get(kv.getKey()).add(func_group);
                }
            }

            for (Entry<Integer, String> kv : fa.double_bonds.double_bond_positions.entrySet()) curr_fa.double_bonds.double_bond_positions.put(kv.getKey() + start_pos - 1, kv.getValue());
            curr_fa.double_bonds.num_double_bonds = curr_fa.double_bonds.double_bond_positions.size();

            if (!tmp.containsKey("tetrahydrofuran") && tmp.containsKey("furan")){
                curr_fa.double_bonds.num_double_bonds += 2;
                if (!curr_fa.double_bonds.double_bond_positions.containsKey(1 + curr_fa.num_carbon)) curr_fa.double_bonds.double_bond_positions.put(1 + curr_fa.num_carbon, "E");
                if (!curr_fa.double_bonds.double_bond_positions.containsKey(3 + curr_fa.num_carbon)) curr_fa.double_bonds.double_bond_positions.put(3 + curr_fa.num_carbon, "E");
            }

            curr_fa.num_carbon += fa.num_carbon;

            tmp.put("fg_pos", new GenericList());
            GenericList l1 = new GenericList();
            l1.add(start_pos);
            l1.add("");
            ((GenericList)tmp.get("fg_pos")).add(l1);
            GenericList l2 = new GenericList();
            l2.add(end_pos);
            l2.add("");
            ((GenericList)tmp.get("fg_pos")).add(l2);

            add_cyclo(node);

            if (tmp.containsKey("cyclo_len")) tmp.remove("cyclo_len");
            if (tmp.containsKey("cyclo")) tmp.remove("cyclo");
        }

        else if (tmp.containsKey("cyclo")){
            tmp.put("cyclo_yl", 1);
            tmp.put("cyclo_len", curr_fa.num_carbon);    
            tmp.put("fg_pos", new GenericList());
            GenericList l1 = new GenericList();
            l1.add(1);
            l1.add("");
            ((GenericList)tmp.get("fg_pos")).add(l1);
            GenericList l2 = new GenericList();
            l2.add(curr_fa.num_carbon);
            l2.add("");
            ((GenericList)tmp.get("fg_pos")).add(l2);

            tmp.remove("cyclo");
        }

        tmp.put("length_pattern", "");
        tmp.put("length_tokens", new GenericList());
        tmp.put("add_lengths", 0);
    }
    
    
    public void switch_position(FunctionalGroup func_group, int switch_num){
        func_group.position = switch_num - func_group.position;
        for (Entry<String, ArrayList<FunctionalGroup> > kv : func_group.functional_groups.entrySet()){
            for (FunctionalGroup fg : kv.getValue()){
                switch_position(fg, switch_num);
            }
        }
    }
    
    
    public void add_cyclo(ParserRuleContext node){
        int start = (int)((GenericList)((GenericList)tmp.get("fg_pos")).get(0)).get(0);
        int end = (int)((GenericList)((GenericList)tmp.get("fg_pos")).get(1)).get(0);


        DoubleBonds cyclo_db = new DoubleBonds();
        // check double bonds
        if (fatty_acyl_stack.back().double_bonds.double_bond_positions.size() > 0){
            for (Entry<Integer, String> kv : fatty_acyl_stack.back().double_bonds.double_bond_positions.entrySet()){
                if (start <= kv.getKey() && kv.getKey() <= end){
                    cyclo_db.double_bond_positions.put(kv.getKey(), kv.getValue());
                }
            }
            cyclo_db.num_double_bonds = cyclo_db.double_bond_positions.size();

            for (Entry<Integer, String> kv : cyclo_db.double_bond_positions.entrySet()){
                fatty_acyl_stack.back().double_bonds.double_bond_positions.remove(kv.getKey());
            }
            fatty_acyl_stack.back().double_bonds.num_double_bonds = fatty_acyl_stack.back().double_bonds.double_bond_positions.size();

        }        
        // check functional_groups
        HashMap<String, ArrayList<FunctionalGroup> > cyclo_fg = new HashMap<>();
        HashSet<String> remove_list = new HashSet<>();
        FattyAcid curr_fa = fatty_acyl_stack.back();

        if (curr_fa.functional_groups.containsKey("noyloxy")){
            ArrayList<Integer> remove_item = new ArrayList<>();
            int i = 0;
            for (FunctionalGroup func_group : curr_fa.functional_groups.get("noyloxy")){
                if (start <= func_group.position && func_group.position <= end){
                    CarbonChain cc = new CarbonChain((FattyAcid)func_group, func_group.position);

                    if (!curr_fa.functional_groups.containsKey("cc")) curr_fa.functional_groups.put("cc", new ArrayList<>());
                    curr_fa.functional_groups.get("cc").add(cc);
                    remove_item.add(i);
                }
                ++i;
            }
            for (int ii = remove_item.size() - 1; ii >= 0; --ii){
                curr_fa.functional_groups.get("noyloxy").remove(remove_item.get(ii));
            }
            if (curr_fa.functional_groups.get("noyloxy").isEmpty()) remove_list.add("noyloxy");
        }

        for (Entry<String, ArrayList<FunctionalGroup> > kv : curr_fa.functional_groups.entrySet()){
            ArrayList<Integer> remove_item = new ArrayList<>();
            int i = 0;
            for (FunctionalGroup func_group : kv.getValue()){
                if (start <= func_group.position && func_group.position <= end){
                    if (!cyclo_fg.containsKey(kv.getKey())) cyclo_fg.put(kv.getKey(), new ArrayList<>());
                    cyclo_fg.get(kv.getKey()).add(func_group);
                    remove_item.add(i);
                }
                ++i;    
            }
            for (int ii = remove_item.size() - 1; ii >= 0; --ii){
                kv.getValue().remove(remove_item.get(ii));
            }
            if (kv.getValue().isEmpty()) remove_list.add(kv.getKey());
        }
        for (String fg : remove_list) curr_fa.functional_groups.remove(fg);

        ArrayList<Element> bridge_chain = new ArrayList<>();
        if (tmp.containsKey("furan")){
            tmp.remove("furan");
            bridge_chain.add(Element.O);
        }

        Cycle cycle = new Cycle(end - start + 1 + bridge_chain.size(), start, end, cyclo_db, cyclo_fg, bridge_chain);
        if (!fatty_acyl_stack.back().functional_groups.containsKey("cy")) fatty_acyl_stack.back().functional_groups.put("cy", new ArrayList<>());
        fatty_acyl_stack.back().functional_groups.get("cy").add(cycle);
    }
    
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFatty_acid(FattyAcidsParser.Fatty_acidContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFatty_acid(FattyAcidsParser.Fatty_acidContext node) {
        set_fatty_acid(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFatty_acid_recursion(FattyAcidsParser.Fatty_acid_recursionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFatty_acid_recursion(FattyAcidsParser.Fatty_acid_recursionContext node) {
        set_fatty_acid(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterWax(FattyAcidsParser.WaxContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitWax(FattyAcidsParser.WaxContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterWax_ester(FattyAcidsParser.Wax_esterContext node) {
        set_recursion(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitWax_ester(FattyAcidsParser.Wax_esterContext node) {
        FattyAcid fa = fatty_acyl_stack.PopBack();
        fa.name += "1";
        fa.lipid_FA_bond_type = LipidFaBondType.AMINE;
        fatty_acyl_stack.back().name += "2";
        fatty_acyl_stack.add(0, fa);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterMethyl(FattyAcidsParser.MethylContext node) {
        tmp.put("fg_type", "methylene");
        GenericList gl = (GenericList)tmp.get("fg_pos");
        if (gl.size() > 1){
            if ((int)((GenericList)gl.get(0)).get(0) < (int)((GenericList)gl.get(1)).get(0)){
                ((GenericList)gl.get(1)).set(0, (int)((GenericList)gl.get(1)).get(0) + 1);
            }
            else if ((int)((GenericList)gl.get(0)).get(0) > (int)((GenericList)gl.get(1)).get(0)){
                ((GenericList)gl.get(0)).set(0, (int)((GenericList)gl.get(0)).get(0) + 1);
            }
            fatty_acyl_stack.back().num_carbon += 1;
            tmp.put("add_methylene", 1);
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitMethyl(FattyAcidsParser.MethylContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCar(FattyAcidsParser.CarContext node) {
        tmp.put("fg_pos", new GenericList());
        tmp.put("fg_type", "");
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCar(FattyAcidsParser.CarContext node) {
        headgroup = "CAR";
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCar_fa(FattyAcidsParser.Car_faContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCar_fa(FattyAcidsParser.Car_faContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCar_spec(FattyAcidsParser.Car_specContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCar_spec(FattyAcidsParser.Car_specContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCar_positions(FattyAcidsParser.Car_positionsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCar_positions(FattyAcidsParser.Car_positionsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCar_position(FattyAcidsParser.Car_positionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCar_position(FattyAcidsParser.Car_positionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterEthanolamine(FattyAcidsParser.EthanolamineContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitEthanolamine(FattyAcidsParser.EthanolamineContext node) {
        headgroup = "NAE";
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAmine(FattyAcidsParser.AmineContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAmine(FattyAcidsParser.AmineContext node) {
        headgroup = "NA";
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAmine_prefix(FattyAcidsParser.Amine_prefixContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAmine_prefix(FattyAcidsParser.Amine_prefixContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAmine_n(FattyAcidsParser.Amine_nContext node) {
        set_recursion(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAmine_n(FattyAcidsParser.Amine_nContext node) {
        FattyAcid fa = fatty_acyl_stack.PopBack();
        fa.name += "1";
        fatty_acyl_stack.back().name += "2";
        fa.lipid_FA_bond_type = LipidFaBondType.AMINE;
        fatty_acyl_stack.add(0, fa);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAcetic_acid(FattyAcidsParser.Acetic_acidContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAcetic_acid(FattyAcidsParser.Acetic_acidContext node) {
        fatty_acyl_stack.back().num_carbon += 2;
        headgroup = "FA";
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAcetic_recursion(FattyAcidsParser.Acetic_recursionContext node) {
        set_recursion(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAcetic_recursion(FattyAcidsParser.Acetic_recursionContext node) {
        add_recursion(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterRegular_fatty_acid(FattyAcidsParser.Regular_fatty_acidContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitRegular_fatty_acid(FattyAcidsParser.Regular_fatty_acidContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAdditional_len(FattyAcidsParser.Additional_lenContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAdditional_len(FattyAcidsParser.Additional_lenContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterSum_add(FattyAcidsParser.Sum_addContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitSum_add(FattyAcidsParser.Sum_addContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterOl_position_description(FattyAcidsParser.Ol_position_descriptionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitOl_position_description(FattyAcidsParser.Ol_position_descriptionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterOl_ending(FattyAcidsParser.Ol_endingContext node) {
        set_fatty_acyl_type(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitOl_ending(FattyAcidsParser.Ol_endingContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterOl_position(FattyAcidsParser.Ol_positionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitOl_position(FattyAcidsParser.Ol_positionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterOl_pos(FattyAcidsParser.Ol_posContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitOl_pos(FattyAcidsParser.Ol_posContext node) {
        set_yl_ending(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFatty_length(FattyAcidsParser.Fatty_lengthContext node) {
        tmp.put("length", 0);
        tmp.put("length_pattern", "");
        tmp.put("length_tokens", new GenericList());
        tmp.put("add_lengths", 1);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFatty_length(FattyAcidsParser.Fatty_lengthContext node) {
        tmp.put("add_lengths", 0);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterNotation_regular(FattyAcidsParser.Notation_regularContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitNotation_regular(FattyAcidsParser.Notation_regularContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterNotation_last_digit(FattyAcidsParser.Notation_last_digitContext node) {
        if ((int)tmp.get("add_lengths") == 1){
            tmp.put("length", (int)tmp.get("length") + last_numbers.get(node.getText()));
            tmp.put("length_pattern", (String)tmp.get("length_pattern") + "L");
            ((GenericList)tmp.get("length_tokens")).add(last_numbers.get(node.getText()));
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitNotation_last_digit(FattyAcidsParser.Notation_last_digitContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterNotation_second_digit(FattyAcidsParser.Notation_second_digitContext node) {
        if ((int)tmp.get("add_lengths") == 1){
            tmp.put("length", (int)tmp.get("length") + second_numbers.get(node.getText()));
            tmp.put("length_pattern", (String)tmp.get("length_pattern") + "S");
            ((GenericList)tmp.get("length_tokens")).add(second_numbers.get(node.getText()));
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitNotation_second_digit(FattyAcidsParser.Notation_second_digitContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterNotation_specials(FattyAcidsParser.Notation_specialsContext node) {
        if ((int)tmp.get("add_lengths") == 1){
            tmp.put("length", (int)tmp.get("length") + special_numbers.get(node.getText()));
            tmp.put("length_pattern", (String)tmp.get("length_pattern") + "X");
            ((GenericList)tmp.get("length_tokens")).add(special_numbers.get(node.getText()));
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitNotation_specials(FattyAcidsParser.Notation_specialsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterIsoprop(FattyAcidsParser.IsopropContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitIsoprop(FattyAcidsParser.IsopropContext node) {
        set_iso(node);
    }
    
    public void set_iso(ParserRuleContext node){
        FattyAcid curr_fa = fatty_acyl_stack.back();
        curr_fa.num_carbon -= 1;
        FunctionalGroup fg = KnownFunctionalGroups.get_instance().get("Me");
        fg.position = 2;
        if (!curr_fa.functional_groups.containsKey("Me")) curr_fa.functional_groups.put("Me", new ArrayList<>());
        curr_fa.functional_groups.get("Me").add(fg);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterProsta(FattyAcidsParser.ProstaContext node) {
        int minus_pos = 0;
        if (tmp.containsKey("reduction")){
            for (Object o : (GenericList)tmp.get("reduction")){
                int i = (int)o;
                minus_pos += i < 8 ? 1 : 0;
            }
        }

        tmp.put("fg_pos", new GenericList());
        GenericList l1 = new GenericList();
        l1.add(8 - minus_pos);
        l1.add("");

        GenericList l2 = new GenericList();
        l2.add(12 - minus_pos);
        l2.add("");

        ((GenericList)tmp.get("fg_pos")).add(l1);
        ((GenericList)tmp.get("fg_pos")).add(l2);
        tmp.put("fg_type", "cy");
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitProsta(FattyAcidsParser.ProstaContext node) {
        add_cyclo(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterTetrahydrofuran(FattyAcidsParser.TetrahydrofuranContext node) {
        tmp.put("furan", 1);
        tmp.put("tetrahydrofuran", 1);
        set_cycle(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitTetrahydrofuran(FattyAcidsParser.TetrahydrofuranContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFuran(FattyAcidsParser.FuranContext node) {
        tmp.put("furan", 1);
        set_cycle(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFuran(FattyAcidsParser.FuranContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAcid_type_regular(FattyAcidsParser.Acid_type_regularContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAcid_type_regular(FattyAcidsParser.Acid_type_regularContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAcid_type_double(FattyAcidsParser.Acid_type_doubleContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAcid_type_double(FattyAcidsParser.Acid_type_doubleContext node) {
        String fa_i = FA_I();
        FattyAcid curr_fa = fatty_acyl_stack.back();
        if (((Dictionary)tmp.get(fa_i)).containsKey("fg_pos_summary")){
            for (Entry<String, Object> kv : ((Dictionary)((Dictionary)tmp.get(fa_i)).get("fg_pos_summary")).entrySet()){
                int k = Integer.valueOf(kv.getKey());
                String v = (String)((Dictionary)((Dictionary)tmp.get(fa_i)).get("fg_pos_summary")).get(kv.getKey());
                if (k > 0 && !curr_fa.double_bonds.double_bond_positions.containsKey(k) && (v.equals("E") || v.equals("Z") || v.length() == 0)){
                    curr_fa.double_bonds.double_bond_positions.put(k, v);
                    curr_fa.double_bonds.num_double_bonds = curr_fa.double_bonds.double_bond_positions.size();
                }
            }
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAcid_single_type(FattyAcidsParser.Acid_single_typeContext node) {
        set_fatty_acyl_type(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAcid_single_type(FattyAcidsParser.Acid_single_typeContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCoa(FattyAcidsParser.CoaContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCoa(FattyAcidsParser.CoaContext node) {
        headgroup = "CoA";
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCoa_ending(FattyAcidsParser.Coa_endingContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCoa_ending(FattyAcidsParser.Coa_endingContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterYl(FattyAcidsParser.YlContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitYl(FattyAcidsParser.YlContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterYl_ending(FattyAcidsParser.Yl_endingContext node) {
        set_yl_ending(node);
    }
    
    public void set_yl_ending(ParserRuleContext node){
        int l = Integer.valueOf(node.getText()) - 1;
        if (l == 0) return;

        FattyAcid curr_fa = fatty_acyl_stack.back();

        if (tmp.containsKey("furan")){
            curr_fa.num_carbon -= l;
            return;
        }

        String fname = "";
        FunctionalGroup fg = null;
        if (l == 1){
            fname = "Me";
            fg = KnownFunctionalGroups.get_instance().get(fname);
        }
        else if (l == 2){
            fname = "Et";
            fg = KnownFunctionalGroups.get_instance().get(fname);
        }
        else {
            FattyAcid fa = new FattyAcid("FA", l);
            // shift functional groups
            for (Entry<String, ArrayList<FunctionalGroup> > kv : curr_fa.functional_groups.entrySet()){
                ArrayList<Integer> remove_item = new ArrayList<Integer>();
                int i = 0;
                for (FunctionalGroup func_group : kv.getValue()){
                    if (func_group.position <= l){
                        remove_item.add(i);
                        if (!fa.functional_groups.containsKey(kv.getKey())) fa.functional_groups.put(kv.getKey(), new ArrayList<>());
                        func_group.position = l + 1 - func_group.position;
                        fa.functional_groups.get(kv.getKey()).add(func_group);
                    }
                }
                for (int ii = remove_item.size() - 1; ii >= 0; --ii){
                    curr_fa.functional_groups.get(kv.getKey()).remove(remove_item.get(ii));
                }
            }
            HashMap<String, ArrayList<FunctionalGroup> > func_dict = curr_fa.functional_groups;
            curr_fa.functional_groups = new HashMap<>();
            for (Entry<String, ArrayList<FunctionalGroup> > kv : func_dict.entrySet()){
                if (kv.getValue().size() > 0) curr_fa.functional_groups.put(kv.getKey(), kv.getValue());
            }

            // shift double bonds
            if (curr_fa.double_bonds.double_bond_positions.size() > 0){
                fa.double_bonds = new DoubleBonds();
                for (Entry<Integer, String> kv : curr_fa.double_bonds.double_bond_positions.entrySet()){
                    if (kv.getKey() <= l) fa.double_bonds.double_bond_positions.put(l + 1 - kv.getKey(), kv.getValue());
                }
                fa.double_bonds.num_double_bonds = fa.double_bonds.double_bond_positions.size();
                for (Entry<Integer, String> kv : fa.double_bonds.double_bond_positions.entrySet()){
                    curr_fa.double_bonds.double_bond_positions.remove(kv.getKey());
                }
            }
            fname = "cc";
            fg = new CarbonChain(fa);
        }
        curr_fa.num_carbon -= l;
        fg.position = l;
        curr_fa.shift_positions(-l);
        if (!curr_fa.functional_groups.containsKey(fname)) curr_fa.functional_groups.put(fname, new ArrayList<>());
        curr_fa.functional_groups.get(fname).add(fg);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitYl_ending(FattyAcidsParser.Yl_endingContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDb_num(FattyAcidsParser.Db_numContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDb_num(FattyAcidsParser.Db_numContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDb_suffix(FattyAcidsParser.Db_suffixContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDb_suffix(FattyAcidsParser.Db_suffixContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDial(FattyAcidsParser.DialContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDial(FattyAcidsParser.DialContext node) {
        FattyAcid curr_fa = fatty_acyl_stack.back();
        int pos = curr_fa.num_carbon;
        FunctionalGroup fg = KnownFunctionalGroups.get_instance().get("oxo");
        fg.position = pos;
        if (!curr_fa.functional_groups.containsKey("oxo")) curr_fa.functional_groups.put("oxo", new ArrayList<>());
        curr_fa.functional_groups.get("oxo").add(fg);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDb_length(FattyAcidsParser.Db_lengthContext node) {
        tmp.put("add_lengths", 1);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDb_length(FattyAcidsParser.Db_lengthContext node) {
        tmp.put("add_lengths", 0);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDioic(FattyAcidsParser.DioicContext node) {
        set_functional_group(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDioic(FattyAcidsParser.DioicContext node) {
        headgroup = "FA";
        int pos = (((GenericList)tmp.get("fg_pos")).size() == 2) ? (int)((GenericList)((GenericList)tmp.get("fg_pos")).get(1)).get(0) : fatty_acyl_stack.back().num_carbon;
        if (tmp.containsKey("reduction")){
            pos -= ((GenericList)tmp.get("reduction")).size();
        }
        fatty_acyl_stack.back().num_carbon -= 1;
        FunctionalGroup func_group = KnownFunctionalGroups.get_instance().get("COOH");
        func_group.position = pos - 1;
        if (!fatty_acyl_stack.back().functional_groups.containsKey("COOH")) fatty_acyl_stack.back().functional_groups.put("COOH", new ArrayList<>());
        fatty_acyl_stack.back().functional_groups.get("COOH").add(func_group);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDioic_acid(FattyAcidsParser.Dioic_acidContext node) {
        set_fatty_acyl_type(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDioic_acid(FattyAcidsParser.Dioic_acidContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterOl(FattyAcidsParser.OlContext node) {
        tmp.put("hydroxyl_pos", new GenericList());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitOl(FattyAcidsParser.OlContext node) {
        if (((GenericList)tmp.get("hydroxyl_pos")).size() > 1){
            FunctionalGroup fg_oh = KnownFunctionalGroups.get_instance().get("OH");
            ArrayList<Integer> sorted_pos = new ArrayList<>();
            for (Object o : (GenericList)tmp.get("hydroxyl_pos")){
                int i = (int)o;
                sorted_pos.add(i);
            }
            Collections.sort(sorted_pos, (Integer a, Integer b) -> b.compareTo(a));
            for (int i = 0; i < sorted_pos.size() - 1; ++i){
                int pos = sorted_pos.get(i);
                FunctionalGroup fg_insert = fg_oh.copy();
                fg_insert.position = pos;
                if (!fatty_acyl_stack.back().functional_groups.containsKey("OH")) fatty_acyl_stack.back().functional_groups.put("OH", new ArrayList<>());
                fatty_acyl_stack.back().functional_groups.get("OH").add(fg_insert);
            }
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAte_type(FattyAcidsParser.Ate_typeContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAte_type(FattyAcidsParser.Ate_typeContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAte(FattyAcidsParser.AteContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAte(FattyAcidsParser.AteContext node) {
        fatty_acyl_stack.back().num_carbon += ate.get(node.getText());
        headgroup = "WE";
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterIsobut(FattyAcidsParser.IsobutContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitIsobut(FattyAcidsParser.IsobutContext node) {
        set_iso(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterHydroxyl_positions(FattyAcidsParser.Hydroxyl_positionsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitHydroxyl_positions(FattyAcidsParser.Hydroxyl_positionsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterHydroxyl_position(FattyAcidsParser.Hydroxyl_positionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitHydroxyl_position(FattyAcidsParser.Hydroxyl_positionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterHydroxyl_number(FattyAcidsParser.Hydroxyl_numberContext node) {
        int h = Integer.valueOf(node.getText());
        ((GenericList)tmp.get("hydroxyl_pos")).add(h);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitHydroxyl_number(FattyAcidsParser.Hydroxyl_numberContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAdditional_descriptions(FattyAcidsParser.Additional_descriptionsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAdditional_descriptions(FattyAcidsParser.Additional_descriptionsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAdditional_descriptions_m(FattyAcidsParser.Additional_descriptions_mContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAdditional_descriptions_m(FattyAcidsParser.Additional_descriptions_mContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAdditional_description(FattyAcidsParser.Additional_descriptionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitAdditional_description(FattyAcidsParser.Additional_descriptionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctional_group(FattyAcidsParser.Functional_groupContext node) {
        set_functional_group(node);
    }
    
    
    public void set_functional_group(ParserRuleContext node){
        tmp.put("fg_pos", new GenericList());
        tmp.put("fg_type", "");
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctional_group(FattyAcidsParser.Functional_groupContext node) {
        if (tmp.containsKey("added_func_group")){
            tmp.remove("added_func_group");
            return;
        }

        else if (tmp.containsKey("add_methylene"))
        {
            tmp.remove("add_methylene");
            add_cyclo(node);
            return;
        }

        String t = (String)tmp.get("fg_type");

        FunctionalGroup fg = null;
        if (!t.equals("acetoxy")){
            if (!func_groups.containsKey(t))
            {
                throw new LipidException("Unknown functional group: '" + t + "'");
            }
            t = func_groups.get(t);
            if (t.length() == 0) return;
            fg = KnownFunctionalGroups.get_instance().get(t);
        }
        else
        {
            fg = new AcylAlkylGroup(new FattyAcid("O", 2));
        }

        FattyAcid fa = fatty_acyl_stack.back();
        if (!fa.functional_groups.containsKey(t)) fa.functional_groups.put(t, new ArrayList<>());
        int l = ((GenericList)tmp.get("fg_pos")).size();
        for (Object o : (GenericList)tmp.get("fg_pos")){
            GenericList lst = (GenericList)o;
            int pos = (int)lst.get(0);

            int num_pos = 0;
            if (tmp.containsKey("reduction")){
                for (Object oo : (GenericList)tmp.get("reduction")){
                    int i = (int)oo;
                    num_pos += i < pos ? 1 : 0;
                }
            }
            FunctionalGroup fg_insert = fg.copy();
            fg_insert.position = pos - num_pos;
            fa.functional_groups.get(t).add(fg_insert);
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterPos_neg(FattyAcidsParser.Pos_negContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitPos_neg(FattyAcidsParser.Pos_negContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDouble_bond_positions(FattyAcidsParser.Double_bond_positionsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDouble_bond_positions(FattyAcidsParser.Double_bond_positionsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDouble_bond_positions_pure(FattyAcidsParser.Double_bond_positions_pureContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDouble_bond_positions_pure(FattyAcidsParser.Double_bond_positions_pureContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDouble_bond_position(FattyAcidsParser.Double_bond_positionContext node) {
        ((Dictionary)tmp.get(FA_I())).put("db_position", 0);
        ((Dictionary)tmp.get(FA_I())).put("db_cistrans", "");
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDouble_bond_position(FattyAcidsParser.Double_bond_positionContext node) {
        int pos = (int)((Dictionary)tmp.get(FA_I())).get("db_position");
        String str_pos = Integer.toString(pos);
        String cistrans = (String)((Dictionary)tmp.get(FA_I())).get("db_cistrans");
        if (cistrans.length() == 0 && ((Dictionary)tmp.get(FA_I())).containsKey("fg_pos_summary") && ((Dictionary)((Dictionary)tmp.get(FA_I())).get("fg_pos_summary")).containsKey(str_pos)){
            cistrans = (String)((Dictionary)((Dictionary)tmp.get(FA_I())).get("fg_pos_summary")).get(str_pos);
        }
        if (pos == 0) return;

        cistrans = cistrans.toUpperCase();

        ((Dictionary)tmp.get(FA_I())).remove("db_position");
        ((Dictionary)tmp.get(FA_I())).remove("db_cistrans");


        if (!cistrans.equals("E") && !cistrans.equals("Z")) cistrans = "";
        if (!fatty_acyl_stack.back().double_bonds.double_bond_positions.containsKey(pos) || fatty_acyl_stack.back().double_bonds.double_bond_positions.get(pos).length() == 0){
            fatty_acyl_stack.back().double_bonds.double_bond_positions.put(pos, cistrans);
            fatty_acyl_stack.back().double_bonds.num_double_bonds = fatty_acyl_stack.back().double_bonds.double_bond_positions.size();
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCistrans_b(FattyAcidsParser.Cistrans_bContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCistrans_b(FattyAcidsParser.Cistrans_bContext node) {
        ((Dictionary)tmp.get(FA_I())).put("db_cistrans", node.getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCistrans(FattyAcidsParser.CistransContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCistrans(FattyAcidsParser.CistransContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDb_number(FattyAcidsParser.Db_numberContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDb_number(FattyAcidsParser.Db_numberContext node) {
        int pos = Integer.valueOf(node.getText());
        int num_db = 0;
        if (tmp.containsKey("reduction")){
            GenericList gl = (GenericList)tmp.get("reduction");
            int l = gl.size();
            for (int i = 0; i < l; ++i){
                num_db += ((int)gl.get(i) < pos) ? 1 : 0;
            }
        }

        ((Dictionary)tmp.get(FA_I())).put("db_position", pos - num_db);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFg_pos_summary(FattyAcidsParser.Fg_pos_summaryContext node) {
        set_functional_group(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFg_pos_summary(FattyAcidsParser.Fg_pos_summaryContext node) {
        String fa_i = FA_I();
        ((Dictionary)tmp.get(fa_i)).put("fg_pos_summary", new Dictionary());
        for (Object o : (GenericList)tmp.get("fg_pos")){
            GenericList lst = (GenericList)o;
            String k = Integer.toString((int)lst.get(0));
            String v = ((String)lst.get(1)).toUpperCase();
            ((Dictionary)((Dictionary)tmp.get(fa_i)).get("fg_pos_summary")).put(k, v);
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterMulti_functional_group(FattyAcidsParser.Multi_functional_groupContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitMulti_functional_group(FattyAcidsParser.Multi_functional_groupContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctional_length(FattyAcidsParser.Functional_lengthContext node) {
        tmp.put("length", 0);
        tmp.put("length_pattern", "");
        tmp.put("length_tokens", new GenericList());
        tmp.put("add_lengths", 1);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctional_length(FattyAcidsParser.Functional_lengthContext node) {
        if ((int)tmp.get("length") != ((GenericList)tmp.get("fg_pos")).size()){
            throw new LipidException("Length of functional group '" + Integer.toString((int)tmp.get("length")) + "' does not match with number of its positions '" + Integer.toString(((GenericList)tmp.get("fg_pos")).size()) + "'");
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctional_positions(FattyAcidsParser.Functional_positionsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctional_positions(FattyAcidsParser.Functional_positionsContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctional_positions_pure(FattyAcidsParser.Functional_positions_pureContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctional_positions_pure(FattyAcidsParser.Functional_positions_pureContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterSingle_functional_group(FattyAcidsParser.Single_functional_groupContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitSingle_functional_group(FattyAcidsParser.Single_functional_groupContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctional_group_type_name(FattyAcidsParser.Functional_group_type_nameContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctional_group_type_name(FattyAcidsParser.Functional_group_type_nameContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctional_group_type(FattyAcidsParser.Functional_group_typeContext node) {
        tmp.put("fg_type", node.getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctional_group_type(FattyAcidsParser.Functional_group_typeContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterEpoxy(FattyAcidsParser.EpoxyContext node) {
        set_functional_group(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitEpoxy(FattyAcidsParser.EpoxyContext node) {
        GenericList gl = (GenericList)tmp.get("fg_pos");
        while(gl.size() > 1){
            gl.remove(gl.size() - 1);
        }
        tmp.put("fg_type", "Epoxy");
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterMethylene_group(FattyAcidsParser.Methylene_groupContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitMethylene_group(FattyAcidsParser.Methylene_groupContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterMethylene(FattyAcidsParser.MethyleneContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitMethylene(FattyAcidsParser.MethyleneContext node) {
        tmp.put("fg_type", "methylene");
        GenericList gl = (GenericList)tmp.get("fg_pos");
        if (gl.size() > 1){
            if ((int)((GenericList)gl.get(0)).get(0) < (int)((GenericList)gl.get(1)).get(0)){
                ((GenericList)gl.get(1)).set(0, (int)((GenericList)gl.get(1)).get(0) + 1);
            }
            else if ((int)((GenericList)gl.get(0)).get(0) > (int)((GenericList)gl.get(1)).get(0)){
                ((GenericList)gl.get(0)).set(0, (int)((GenericList)gl.get(0)).get(0) + 1);
            }
            fatty_acyl_stack.back().num_carbon += 1;
            tmp.put("add_methylene", 1);
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctional_position(FattyAcidsParser.Functional_positionContext node) {
        GenericList gl = new GenericList();
        gl.add(0);
        gl.add("");
        ((GenericList)tmp.get("fg_pos")).add(gl);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctional_position(FattyAcidsParser.Functional_positionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctional_position_pure(FattyAcidsParser.Functional_position_pureContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctional_position_pure(FattyAcidsParser.Functional_position_pureContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunctional_pos(FattyAcidsParser.Functional_posContext node) {
        GenericList gl = (GenericList)tmp.get("fg_pos");
        ((GenericList)gl.get(gl.size() - 1)).set(0, Integer.valueOf(node.getText()));
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunctional_pos(FattyAcidsParser.Functional_posContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterFunc_stereo(FattyAcidsParser.Func_stereoContext node) {
        int l = ((GenericList)tmp.get("fg_pos")).size();
        ((GenericList)((GenericList)tmp.get("fg_pos")).get(l - 1)).set(1, node.getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFunc_stereo(FattyAcidsParser.Func_stereoContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterReduction(FattyAcidsParser.ReductionContext node) {
        set_functional_group(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitReduction(FattyAcidsParser.ReductionContext node) {
        int shift_len = -((GenericList)tmp.get("fg_pos")).size();
        fatty_acyl_stack.back().num_carbon += shift_len;
        for(Entry<String, ArrayList<FunctionalGroup>> kv : fatty_acyl_stack.get(fatty_acyl_stack.size() - 1).functional_groups.entrySet()){
            for(FunctionalGroup func_group : kv.getValue()){
                func_group.shift_positions(shift_len);
            }
        }

        tmp.put("reduction", new GenericList());
        for (Object o : (GenericList)tmp.get("fg_pos")){
            GenericList lst = (GenericList)o;
            ((GenericList)tmp.get("reduction")).add((int)lst.get(0));
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterHomo(FattyAcidsParser.HomoContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitHomo(FattyAcidsParser.HomoContext node) {
        tmp.put("post_adding", new GenericList());
        for (Object o : (GenericList)tmp.get("fg_pos")){
            GenericList lst = (GenericList)o;
            ((GenericList)tmp.get("post_adding")).add((int)lst.get(0));
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCycle(FattyAcidsParser.CycleContext node) {
        set_cycle(node);
    }
    
    
    void set_cycle(ParserRuleContext node){
        tmp.put("cyclo", 1);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCycle(FattyAcidsParser.CycleContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterCyclo_position(FattyAcidsParser.Cyclo_positionContext node) {
        set_functional_group(node);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCyclo_position(FattyAcidsParser.Cyclo_positionContext node) {
        if (tmp.containsKey("post_adding")){
            fatty_acyl_stack.back().num_carbon += ((GenericList)tmp.get("post_adding")).size();
            tmp.remove("post_adding");
        }

        FattyAcid curr_fa = fatty_acyl_stack.back();
        int start = (int)((GenericList)((GenericList)tmp.get("fg_pos")).get(0)).get(0);
        if (curr_fa.functional_groups.containsKey("cy")){
            for (FunctionalGroup cy : curr_fa.functional_groups.get("cy")){
                int shift_val = start - cy.position;
                if (shift_val == 0) continue;
                ((Cycle)cy).rearrange_functional_groups(curr_fa, shift_val);
            }
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterRecursion_description(FattyAcidsParser.Recursion_descriptionContext node) {
        set_recursion(node);
    }
    
    
    public void set_recursion(ParserRuleContext node){
        tmp.put("fg_pos", new GenericList());
        tmp.put("fg_type", "");
        fatty_acyl_stack.add(new FattyAcid("FA"));
        tmp.put(FA_I(), new Dictionary());
        ((Dictionary)tmp.get(FA_I())).put("recursion_pos", 0);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitRecursion_description(FattyAcidsParser.Recursion_descriptionContext node) {
        add_recursion(node);
    }
    
    public void add_recursion(ParserRuleContext node){
        int pos = (int)((Dictionary)tmp.get(FA_I())).get("recursion_pos");
        FattyAcid fa = fatty_acyl_stack.PopBack();

        fa.position = pos;
        FattyAcid curr_fa = fatty_acyl_stack.back();

        String fname = "";
        if (tmp.containsKey("cyclo_yl")){
            fname = "cyclo";
            tmp.remove("cyclo_yl");
        }
        else {
            fname = headgroup;
        }
        if (!curr_fa.functional_groups.containsKey(fname)) curr_fa.functional_groups.put(fname, new ArrayList<>());
        curr_fa.functional_groups.get(fname).add(fa);
        tmp.put("added_func_group", 1);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterRecursion(FattyAcidsParser.RecursionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitRecursion(FattyAcidsParser.RecursionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterRecursion_position(FattyAcidsParser.Recursion_positionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitRecursion_position(FattyAcidsParser.Recursion_positionContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterRecursion_pos(FattyAcidsParser.Recursion_posContext node) {
        ((Dictionary)tmp.get(FA_I())).put("recursion_pos", Integer.valueOf(node.getText()));
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitRecursion_pos(FattyAcidsParser.Recursion_posContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterPos_separator(FattyAcidsParser.Pos_separatorContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitPos_separator(FattyAcidsParser.Pos_separatorContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterNumber(FattyAcidsParser.NumberContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitNumber(FattyAcidsParser.NumberContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterDigit(FattyAcidsParser.DigitContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitDigit(FattyAcidsParser.DigitContext node) { }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterEveryRule(ParserRuleContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitEveryRule(ParserRuleContext node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void visitTerminal(TerminalNode node) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void visitErrorNode(ErrorNode node) { }
}
