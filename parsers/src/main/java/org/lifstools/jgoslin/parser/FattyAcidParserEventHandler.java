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
package org.lifstools.jgoslin.parser;

import java.util.ArrayDeque;
import org.lifstools.jgoslin.domain.LipidMolecularSpecies;
import org.lifstools.jgoslin.domain.GenericList;
import org.lifstools.jgoslin.domain.LipidFaBondType;
import org.lifstools.jgoslin.domain.LipidStructureDefined;
import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.KnownFunctionalGroups;
import org.lifstools.jgoslin.domain.Element;
import org.lifstools.jgoslin.domain.LipidSnPosition;
import org.lifstools.jgoslin.domain.Headgroup;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.LipidCompleteStructure;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.AcylAlkylGroup;
import org.lifstools.jgoslin.domain.DoubleBonds;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.Cycle;
import org.lifstools.jgoslin.domain.CarbonChain;
import org.lifstools.jgoslin.domain.FattyAcid;
import org.lifstools.jgoslin.domain.LipidSpecies;
import org.lifstools.jgoslin.domain.LipidFullStructure;
import org.lifstools.jgoslin.domain.Dictionary;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author dominik
 */
public class FattyAcidParserEventHandler extends BaseParserEventHandler<LipidAdduct> {

    private final KnownFunctionalGroups knownFunctionalGroups;
    private LipidLevel level;
    private String headgroup;
    private ArrayDeque<FattyAcid> fattyAcylStack;
    private Dictionary tmp;

    private static final HashMap<String, Integer> LAST_NUMBERS = new HashMap<>() {
        {
            put("un", 1);
            put("hen", 1);
            put("do", 2);
            put("di", 2);
            put("tri", 3);
            put("buta", 4);
            put("but", 4);
            put("tetra", 4);
            put("penta", 5);
            put("pent", 5);
            put("hexa", 6);
            put("hex", 6);
            put("hepta", 7);
            put("hept", 7);
            put("octa", 8);
            put("oct", 8);
            put("nona", 9);
            put("non", 9);
        }
    };

    private static final HashMap<String, Integer> SECOND_NUMBERS = new HashMap<>() {
        {
            put("deca", 10);
            put("dec", 10);
            put("eicosa", 20);
            put("eicos", 20);
            put("cosa", 20);
            put("cos", 20);
            put("triaconta", 30);
            put("triacont", 30);
            put("tetraconta", 40);
            put("tetracont", 40);
            put("pentaconta", 50);
            put("pentacont", 50);
            put("hexaconta", 60);
            put("hexacont", 60);
            put("heptaconta", 70);
            put("heptacont", 70);
            put("octaconta", 80);
            put("octacont", 80);
            put("nonaconta", 90);
            put("nonacont", 90);
        }
    };

    private static final HashMap<String, String> FUNC_GROUPS = new HashMap<>() {
        {
            put("keto", "oxo");
            put("ethyl", "Et");
            put("hydroxy", "OH");
            put("phospho", "Ph");
            put("oxo", "oxo");
            put("bromo", "Br");
            put("methyl", "Me");
            put("hydroperoxy", "OOH");
            put("homo", "");
            put("Epoxy", "Ep");
            put("fluro", "F");
            put("fluoro", "F");
            put("chloro", "Cl");
            put("methylene", "My");
            put("sulfooxy", "Su");
            put("amino", "NH2");
            put("sulfanyl", "SH");
            put("methoxy", "OMe");
            put("iodo", "I");
            put("cyano", "CN");
            put("nitro", "NO2");
            put("OH", "OH");
            put("thio", "SH");
            put("mercapto", "SH");
            put("carboxy", "COOH");
            put("acetoxy", "Ac");
            put("cysteinyl", "Cys");
            put("phenyl", "Phe");
            put("s-glutathionyl", "SGlu");
            put("s-cysteinyl", "SCys");
            put("butylperoxy", "BOO");
            put("dimethylarsinoyl", "MMAs");
            put("methylsulfanyl", "SMe");
            put("imino", "NH");
            put("s-cysteinylglycinyl", "SCG");
        }
    };

    private static final HashMap<String, Integer> ATE = new HashMap<>() {
        {
            put("formate", 1);
            put("acetate", 2);
            put("butyrate", 4);
            put("propionate", 3);
            put("valerate", 5);
            put("isobutyrate", 4);
        }
    };

    private static final HashMap<String, Integer> SPECIAL_NUMBERS = new HashMap<>() {
        {
            put("meth", 1);
            put("etha", 2);
            put("eth", 2);
            put("propa", 3);
            put("isoprop", 3);
            put("prop", 3);
            put("propi", 3);
            put("propio", 3);
            put("buta", 4);
            put("but", 4);
            put("butr", 4);
            put("furan", 5);
            put("valer", 5);
            put("eicosa", 20);
            put("eicos", 20);
            put("icosa", 20);
            put("icos", 20);
            put("prosta", 20);
            put("prost", 20);
            put("prostan", 20);
        }
    };

    private static final HashSet<String> NOIC_SET = new HashSet<>(Arrays.asList("noic acid", "nic acid", "dioic_acid"));
    private static final HashSet<String> NAL_SET = new HashSet<>(Arrays.asList("nal", "dial"));
    private static final HashSet<String> ACETATE_SET = new HashSet<>(Arrays.asList("acetate", "noate", "nate"));

    public FattyAcidParserEventHandler() {
        this(new KnownFunctionalGroups());
    }

    public FattyAcidParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        this.knownFunctionalGroups = knownFunctionalGroups;
        try {
            registeredEvents.put("lipid_pre_event", this::resetParser);
            registeredEvents.put("lipid_post_event", this::build_lipid);
            registeredEvents.put("fatty_acid_post_event", this::set_fatty_acid);
            registeredEvents.put("fatty_acid_recursion_post_event", this::set_fatty_acid);

            registeredEvents.put("acid_single_type_pre_event", this::set_fatty_acyl_type);
            registeredEvents.put("ol_ending_pre_event", this::set_fatty_acyl_type);
            registeredEvents.put("double_bond_position_pre_event", this::set_double_bond_information);
            registeredEvents.put("double_bond_position_post_event", this::add_double_bond_information);
            registeredEvents.put("db_number_post_event", this::set_double_bond_position);
            registeredEvents.put("cistrans_post_event", this::set_cistrans);
            registeredEvents.put("acid_type_double_post_event", this::check_db);
            registeredEvents.put("db_length_pre_event", this::open_db_length);
            registeredEvents.put("db_length_post_event", this::close_db_length);

            // lengths
            registeredEvents.put("functional_length_pre_event", this::reset_length);
            registeredEvents.put("fatty_length_pre_event", this::reset_length);
            registeredEvents.put("functional_length_post_event", this::set_functional_length);
            registeredEvents.put("fatty_length_post_event", this::set_fatty_length);

            // numbers
            registeredEvents.put("notation_specials_pre_event", this::special_number);
            registeredEvents.put("notation_last_digit_pre_event", this::last_number);
            registeredEvents.put("notation_second_digit_pre_event", this::second_number);

            // functional groups
            registeredEvents.put("functional_group_pre_event", this::set_functional_group);
            registeredEvents.put("functional_group_post_event", this::add_functional_group);
            registeredEvents.put("functional_pos_pre_event", this::set_functional_pos);
            registeredEvents.put("functional_position_pre_event", this::set_functional_position);
            registeredEvents.put("functional_group_type_pre_event", this::set_functional_type);

            // cyclo / epoxy
            registeredEvents.put("cyclo_position_pre_event", this::set_functional_group);
            registeredEvents.put("cyclo_position_post_event", this::rearrange_cycle);
            registeredEvents.put("epoxy_pre_event", this::set_functional_group);
            registeredEvents.put("epoxy_post_event", this::add_epoxy);
            registeredEvents.put("cycle_pre_event", this::set_cycle);
            registeredEvents.put("methylene_post_event", this::set_methylene);

            // dioic
            registeredEvents.put("dioic_pre_event", this::set_functional_group);
            registeredEvents.put("dioic_post_event", this::set_dioic);
            registeredEvents.put("dioic_acid_pre_event", this::set_fatty_acyl_type);
            registeredEvents.put("dial_post_event", this::set_dial);

            // prosta
            registeredEvents.put("prosta_pre_event", this::set_prosta);
            registeredEvents.put("prosta_post_event", this::add_cyclo);
            registeredEvents.put("reduction_pre_event", this::set_functional_group);
            registeredEvents.put("reduction_post_event", this::reduction);
            registeredEvents.put("homo_post_event", this::homo);

            // recursion
            registeredEvents.put("recursion_description_pre_event", this::set_recursion);
            registeredEvents.put("recursion_description_post_event", this::add_recursion);
            registeredEvents.put("recursion_pos_pre_event", this::set_recursion_pos);
            registeredEvents.put("yl_ending_pre_event", this::set_yl_ending);
            registeredEvents.put("acetic_acid_post_event", this::set_acetic_acid);
            registeredEvents.put("acetic_recursion_pre_event", this::set_recursion);
            registeredEvents.put("acetic_recursion_post_event", this::add_recursion);
            registeredEvents.put("hydroxyl_number_pre_event", this::add_hydroxyl);
            registeredEvents.put("ol_pre_event", this::setup_hydroxyl);
            registeredEvents.put("ol_post_event", this::add_hydroxyls);
            registeredEvents.put("ol_pos_post_event", this::set_yl_ending);

            // wax esters
            registeredEvents.put("wax_ester_pre_event", this::set_recursion);
            registeredEvents.put("wax_ester_post_event", this::add_wax_ester);
            registeredEvents.put("ate_post_event", this::set_ate);
            registeredEvents.put("isoprop_post_event", this::set_iso);
            registeredEvents.put("isobut_post_event", this::set_iso);

            // CoA
            registeredEvents.put("coa_post_event", this::set_coa);
            registeredEvents.put("methyl_pre_event", this::set_methyl);

            // CAR
            registeredEvents.put("car_pre_event", this::set_car);
            registeredEvents.put("car_post_event", this::add_car);

            // furan
            registeredEvents.put("tetrahydrofuran_pre_event", this::set_tetrahydrofuran);
            registeredEvents.put("furan_pre_event", this::set_furan);

            // amine
            registeredEvents.put("ethanolamine_post_event", this::add_ethanolamine);
            registeredEvents.put("amine_n_pre_event", this::set_recursion);
            registeredEvents.put("amine_n_post_event", this::add_amine);
            registeredEvents.put("amine_post_event", this::add_amine_name);

            // functional group position summary
            registeredEvents.put("fg_pos_summary_pre_event", this::set_functional_group);
            registeredEvents.put("fg_pos_summary_post_event", this::add_summary);
            registeredEvents.put("func_stereo_pre_event", this::add_func_stereo);
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize FattyAcidParserEventHandler.", e);
        }
    }

    public String FA_I() {
        return "fa" + Integer.toString(fattyAcylStack.size());
    }

    public void set_lipid_level(LipidLevel _level) {
        level = level.level < _level.level ? level : _level;
    }

    @Override
    protected void resetParser(TreeNode node) {
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        headgroup = "";
        fattyAcylStack = new ArrayDeque<>();
        fattyAcylStack.add(new FattyAcid("FA", knownFunctionalGroups));
        tmp = new Dictionary();
        tmp.put("fa1", new Dictionary());
    }

    public void build_lipid(TreeNode node) {
        if (tmp.containsKey("cyclo_yl")) {
            tmp.put("fg_pos", new GenericList());

            GenericList l1 = new GenericList();
            l1.add(1);
            l1.add("");
            ((GenericList) tmp.get("fg_pos")).add(l1);

            GenericList l2 = new GenericList();
            l2.add((int) tmp.get("cyclo_len"));
            l2.add("");
            ((GenericList) tmp.get("fg_pos")).add(l2);

            add_cyclo(node);
            tmp.remove("cyclo_yl");
            tmp.remove("cyclo_len");
        }

        if (tmp.containsKey("post_adding")) {
            FattyAcid curr_fa_p = fattyAcylStack.peekLast();
            int s = ((GenericList) tmp.get("post_adding")).size();
            curr_fa_p.numCarbon += s;
            for (int i = 0; i < s; ++i) {
                int pos = (int) ((GenericList) tmp.get("post_adding")).get(i);
                curr_fa_p.addPosition(pos);
                DoubleBonds db = new DoubleBonds(curr_fa_p.getDoubleBonds().getNumDoubleBonds());
                for (Entry<Integer, String> kv : curr_fa_p.getDoubleBonds().doubleBondPositions.entrySet()) {
                    db.doubleBondPositions.put(kv.getKey() + (kv.getKey() >= pos ? 1 : 0), kv.getValue());
                }
                db.setNumDoubleBonds(db.doubleBondPositions.size());
                curr_fa_p.setDoubleBonds(db);
            }
        }

        FattyAcid curr_fa = fattyAcylStack.peekLast();
        if (curr_fa.getDoubleBonds().doubleBondPositions.size() > 0) {
            int db_right = 0;
            for (Entry<Integer, String> kv : curr_fa.getDoubleBonds().doubleBondPositions.entrySet()) {
                db_right += kv.getValue().length() > 0 ? 1 : 0;
            }
            if (db_right != curr_fa.getDoubleBonds().doubleBondPositions.size()) {
                set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
            }
        }

        Headgroup head_group = new Headgroup(headgroup);

        content = new LipidAdduct();

        switch (level) {

            case COMPLETE_STRUCTURE:
                content.lipid = new LipidCompleteStructure(head_group, fattyAcylStack, knownFunctionalGroups);
                break;
            case FULL_STRUCTURE:
                content.lipid = new LipidFullStructure(head_group, fattyAcylStack, knownFunctionalGroups);
                break;
            case STRUCTURE_DEFINED:
                content.lipid = new LipidStructureDefined(head_group, fattyAcylStack, knownFunctionalGroups);
                break;
            case SN_POSITION:
                content.lipid = new LipidSnPosition(head_group, fattyAcylStack, knownFunctionalGroups);
                break;
            case MOLECULAR_SPECIES:
                content.lipid = new LipidMolecularSpecies(head_group, fattyAcylStack, knownFunctionalGroups);
                break;
            case SPECIES:
                content.lipid = new LipidSpecies(head_group, fattyAcylStack, knownFunctionalGroups);
                break;
            default:
                break;
        }
    }

    public void set_fatty_acyl_type(TreeNode node) {
        String t = node.getText();

        if (t.endsWith("ol")) {
            headgroup = "FOH";
        } else if (NOIC_SET.contains(t)) {
            headgroup = "FA";
        } else if (NAL_SET.contains(t)) {
            headgroup = "FAL";
        } else if (ACETATE_SET.contains(t)) {
            headgroup = "WE";
        } else if (t.equals("ne")) {
            headgroup = "HC";
            fattyAcylStack.peekLast().lipidFaBondType = LipidFaBondType.AMINE;
        } else {
            headgroup = t;
        }
    }

    public void add_amine(TreeNode node) {
        FattyAcid fa = fattyAcylStack.pollLast();

        fa.setName(fa.getName() + "1");
        fattyAcylStack.peekLast().setName(fa.getName() + "2");
        fa.lipidFaBondType = LipidFaBondType.AMINE;
        fattyAcylStack.addFirst(fa);
    }

    public void set_fatty_acid(TreeNode node) {
        FattyAcid curr_fa = fattyAcylStack.peekLast();
        if (tmp.containsKey("length_pattern")) {

            String length_pattern = (String) tmp.get("length_pattern");
            int[] num = new int[((GenericList) tmp.get("length_tokens")).size()];
            for (int i = 0; i < ((GenericList) tmp.get("length_tokens")).size(); ++i) {
                num[i] = (int) ((GenericList) tmp.get("length_tokens")).get(i);
            }

            int l = 0, d = 0;
            if (length_pattern.equals("L") || length_pattern.equals("S")) {
                l += num[0];
            } else if (length_pattern.equals("LS")) {
                l += num[0] + num[1];
            } else if (length_pattern.equals("LL") || length_pattern.equals("SL") || length_pattern.equals("SS")) {
                l += num[0];
                d += num[1];
            } else if (length_pattern.equals("LSL") || length_pattern.equals("LSS")) {
                l += num[0] + num[1];
                d += num[2];
            } else if (length_pattern.equals("LSLS")) {
                l += num[0] + num[1];
                d += num[2] + num[3];
            } else if (length_pattern.equals("SLS")) {
                l += num[0];
                d += num[1] + num[2];
            } else if (length_pattern.length() > 0 && length_pattern.charAt(0) == 'X') {
                l += num[0];
                for (int i = 1; i < ((GenericList) tmp.get("length_tokens")).size(); ++i) {
                    d += num[i];
                }
            } else if (length_pattern.equals("LLS")) { // false
                throw new RuntimeException("Cannot determine fatty acid and double bond length in '" + node.getText() + "'");
            }
            curr_fa.numCarbon += l;
            if (curr_fa.getDoubleBonds().doubleBondPositions.isEmpty() && d > 0) {
                curr_fa.getDoubleBonds().setNumDoubleBonds(d);
            }
        }

        if (curr_fa.getFunctionalGroups().containsKey("noyloxy")) {
            if (headgroup.equals("FA")) {
                headgroup = "FAHFA";
            }

            while (curr_fa.getFunctionalGroups().get("noyloxy").size() > 0) {
                FattyAcid fa = (FattyAcid) curr_fa.getFunctionalGroups().get("noyloxy").get(curr_fa.getFunctionalGroups().get("noyloxy").size() - 1);
                curr_fa.getFunctionalGroups().get("noyloxy").remove(curr_fa.getFunctionalGroups().get("noyloxy").size() - 1);

                AcylAlkylGroup acyl = new AcylAlkylGroup(fa, knownFunctionalGroups);
                acyl.setPosition(fa.getPosition());

                if (!curr_fa.getFunctionalGroups().containsKey("acyl")) {
                    curr_fa.getFunctionalGroups().put("acyl", new ArrayList<>());
                }
                curr_fa.getFunctionalGroups().get("acyl").add(acyl);
            }
            curr_fa.getFunctionalGroups().remove("noyloxy");
        } else if (curr_fa.getFunctionalGroups().containsKey("nyloxy") || curr_fa.getFunctionalGroups().containsKey("yloxy")) {
            String yloxy = curr_fa.getFunctionalGroups().containsKey("nyloxy") ? "nyloxy" : "yloxy";
            while (curr_fa.getFunctionalGroups().get(yloxy).size() > 0) {
                FattyAcid fa = (FattyAcid) curr_fa.getFunctionalGroups().get(yloxy).get(curr_fa.getFunctionalGroups().get(yloxy).size() - 1);
                curr_fa.getFunctionalGroups().get(yloxy).remove(curr_fa.getFunctionalGroups().get(yloxy).size() - 1);

                AcylAlkylGroup alkyl = new AcylAlkylGroup(fa, -1, 1, true, knownFunctionalGroups);
                alkyl.setPosition(fa.getPosition());

                if (!curr_fa.getFunctionalGroups().containsKey("alkyl")) {
                    curr_fa.getFunctionalGroups().put("alkyl", new ArrayList<>());
                }
                curr_fa.getFunctionalGroups().get("alkyl").add(alkyl);
            }
            curr_fa.getFunctionalGroups().remove(yloxy);
        } else {
            boolean has_yl = false;
            for (Entry<String, ArrayList<FunctionalGroup>> kv : curr_fa.getFunctionalGroups().entrySet()) {
                if (kv.getKey().endsWith("yl")) {
                    has_yl = true;
                    break;
                }
            }
            if (has_yl) {
                while (true) {
                    String yl = "";
                    for (Entry<String, ArrayList<FunctionalGroup>> kv : curr_fa.getFunctionalGroups().entrySet()) {
                        if (kv.getKey().endsWith("yl")) {
                            yl = kv.getKey();
                            break;
                        }
                    }
                    if (yl.length() == 0) {
                        break;
                    }

                    while (curr_fa.getFunctionalGroups().get(yl).size() > 0) {
                        FattyAcid fa = (FattyAcid) curr_fa.getFunctionalGroups().get(yl).get(curr_fa.getFunctionalGroups().get(yl).size() - 1);
                        curr_fa.getFunctionalGroups().get(yl).remove(curr_fa.getFunctionalGroups().get(yl).size() - 1);

                        if (tmp.containsKey("cyclo")) {
                            int cyclo_len = curr_fa.numCarbon;
                            tmp.put("cyclo_len", cyclo_len);
                            if (fa.getPosition() != cyclo_len && !tmp.containsKey("furan")) {
                                switch_position(curr_fa, 2 + cyclo_len);
                            }
                            fa.shiftPositions(cyclo_len);
                            if (tmp.containsKey("furan")) {
                                curr_fa.shiftPositions(-1);
                            }

                            for (Entry<String, ArrayList<FunctionalGroup>> kv : fa.getFunctionalGroups().entrySet()) {
                                if (!curr_fa.getFunctionalGroups().containsKey(kv.getKey())) {
                                    curr_fa.getFunctionalGroups().put(kv.getKey(), new ArrayList<>());
                                }
                                for (FunctionalGroup func_group : kv.getValue()) {
                                    curr_fa.getFunctionalGroups().get(kv.getKey()).add(func_group);
                                }
                            }

                            curr_fa.numCarbon = cyclo_len + fa.numCarbon;

                            for (Entry<Integer, String> kv : fa.getDoubleBonds().doubleBondPositions.entrySet()) {
                                curr_fa.getDoubleBonds().doubleBondPositions.put(kv.getKey() + cyclo_len, kv.getValue());
                            }
                            curr_fa.getDoubleBonds().setNumDoubleBonds(curr_fa.getDoubleBonds().doubleBondPositions.size());

                            if (!tmp.containsKey("tetrahydrofuran") && tmp.containsKey("furan")) {
                                curr_fa.getDoubleBonds().setNumDoubleBonds(curr_fa.getDoubleBonds().getNumDoubleBonds() + 2);
                                if (!curr_fa.getDoubleBonds().doubleBondPositions.containsKey(1)) {
                                    curr_fa.getDoubleBonds().doubleBondPositions.put(1, "E");
                                }
                                if (!curr_fa.getDoubleBonds().doubleBondPositions.containsKey(3)) {
                                    curr_fa.getDoubleBonds().doubleBondPositions.put(3, "E");
                                }
                            }

                            tmp.put("cyclo_yl", true);
                        } else {
                            // add carbon chains here here
                            // special chains: i.e. ethyl, methyl
                            String fg_name = "";
                            if (fa.getDoubleBonds().getNumDoubleBonds() == 0 && fa.getFunctionalGroups().isEmpty()) {
                                FunctionalGroup fg = null;
                                if (fa.numCarbon == 1) {
                                    fg_name = "Me";
                                    fg = knownFunctionalGroups.get(fg_name);
                                } else if (fa.numCarbon == 2) {
                                    fg_name = "Et";
                                    fg = knownFunctionalGroups.get(fg_name);
                                }
                                if (fg != null && fg_name.length() > 0) {
                                    fg.setPosition(fa.getPosition());
                                    if (!curr_fa.getFunctionalGroups().containsKey(fg_name)) {
                                        curr_fa.getFunctionalGroups().put(fg_name, new ArrayList<>());
                                    }
                                    curr_fa.getFunctionalGroups().get(fg_name).add(fg);
                                }
                            }
                            if (fg_name.length() == 0) {
                                CarbonChain cc = new CarbonChain(fa, fa.getPosition(), knownFunctionalGroups);
                                if (!curr_fa.getFunctionalGroups().containsKey("cc")) {
                                    curr_fa.getFunctionalGroups().put("cc", new ArrayList<>());
                                }
                                curr_fa.getFunctionalGroups().get("cc").add(cc);
                            }
                        }
                    }
                    if (tmp.containsKey("cyclo")) {
                        tmp.remove("cyclo");
                    }
                    curr_fa.getFunctionalGroups().remove(yl);
                }
            }
        }

        if (curr_fa.getFunctionalGroups().containsKey("cyclo")) {
            FattyAcid fa = (FattyAcid) curr_fa.getFunctionalGroups().get("cyclo").get(0);
            curr_fa.getFunctionalGroups().remove("cyclo");
            if (!tmp.containsKey("cyclo_len")) {
                tmp.put("cyclo_len", 5);
            }
            int start_pos = curr_fa.numCarbon + 1;
            int end_pos = curr_fa.numCarbon + (int) tmp.get("cyclo_len");
            fa.shiftPositions(start_pos - 1);

            if (curr_fa.getFunctionalGroups().containsKey("cy")) {
                for (FunctionalGroup cy : curr_fa.getFunctionalGroups().get("cy")) {
                    cy.shiftPositions(start_pos - 1);
                }
            }
            for (Entry<String, ArrayList<FunctionalGroup>> kv : fa.getFunctionalGroups().entrySet()) {
                if (!curr_fa.getFunctionalGroups().containsKey(kv.getKey())) {
                    curr_fa.getFunctionalGroups().put(kv.getKey(), new ArrayList<>());
                }
                for (FunctionalGroup func_group : kv.getValue()) {
                    curr_fa.getFunctionalGroups().get(kv.getKey()).add(func_group);
                }
            }

            for (Entry<Integer, String> kv : fa.getDoubleBonds().doubleBondPositions.entrySet()) {
                curr_fa.getDoubleBonds().doubleBondPositions.put(kv.getKey() + start_pos - 1, kv.getValue());
            }
            curr_fa.getDoubleBonds().setNumDoubleBonds(curr_fa.getDoubleBonds().doubleBondPositions.size());

            if (!tmp.containsKey("tetrahydrofuran") && tmp.containsKey("furan")) {
                curr_fa.getDoubleBonds().setNumDoubleBonds(curr_fa.getDoubleBonds().getNumDoubleBonds() + 2);
                if (!curr_fa.getDoubleBonds().doubleBondPositions.containsKey(1 + curr_fa.numCarbon)) {
                    curr_fa.getDoubleBonds().doubleBondPositions.put(1 + curr_fa.numCarbon, "E");
                }
                if (!curr_fa.getDoubleBonds().doubleBondPositions.containsKey(3 + curr_fa.numCarbon)) {
                    curr_fa.getDoubleBonds().doubleBondPositions.put(3 + curr_fa.numCarbon, "E");
                }
            }

            curr_fa.numCarbon += fa.numCarbon;

            tmp.put("fg_pos", new GenericList());
            GenericList l1 = new GenericList();
            l1.add(start_pos);
            l1.add("");
            ((GenericList) tmp.get("fg_pos")).add(l1);
            GenericList l2 = new GenericList();
            l2.add(end_pos);
            l2.add("");
            ((GenericList) tmp.get("fg_pos")).add(l2);

            add_cyclo(node);

            if (tmp.containsKey("cyclo_len")) {
                tmp.remove("cyclo_len");
            }
            if (tmp.containsKey("cyclo")) {
                tmp.remove("cyclo");
            }
        } else if (tmp.containsKey("cyclo")) {
            tmp.put("cyclo_yl", 1);
            tmp.put("cyclo_len", curr_fa.numCarbon);
            tmp.put("fg_pos", new GenericList());
            GenericList l1 = new GenericList();
            l1.add(1);
            l1.add("");
            ((GenericList) tmp.get("fg_pos")).add(l1);
            GenericList l2 = new GenericList();
            l2.add(curr_fa.numCarbon);
            l2.add("");
            ((GenericList) tmp.get("fg_pos")).add(l2);

            tmp.remove("cyclo");
        }

        tmp.put("length_pattern", "");
        tmp.put("length_tokens", new GenericList());
        tmp.put("add_lengths", 0);
    }

    public void switch_position(FunctionalGroup func_group, int switch_num) {
        func_group.setPosition(switch_num - func_group.getPosition());
        for (Entry<String, ArrayList<FunctionalGroup>> kv : func_group.getFunctionalGroups().entrySet()) {
            for (FunctionalGroup fg : kv.getValue()) {
                switch_position(fg, switch_num);
            }
        }
    }

    public void add_cyclo(TreeNode node) {
        int start = (int) ((GenericList) ((GenericList) tmp.get("fg_pos")).get(0)).get(0);
        int end = (int) ((GenericList) ((GenericList) tmp.get("fg_pos")).get(1)).get(0);

        DoubleBonds cyclo_db = new DoubleBonds();
        // check double bonds
        if (fattyAcylStack.peekLast().getDoubleBonds().doubleBondPositions.size() > 0) {
            for (Entry<Integer, String> kv : fattyAcylStack.peekLast().getDoubleBonds().doubleBondPositions.entrySet()) {
                if (start <= kv.getKey() && kv.getKey() <= end) {
                    cyclo_db.doubleBondPositions.put(kv.getKey(), kv.getValue());
                }
            }
            cyclo_db.setNumDoubleBonds(cyclo_db.doubleBondPositions.size());

            for (Entry<Integer, String> kv : cyclo_db.doubleBondPositions.entrySet()) {
                fattyAcylStack.peekLast().getDoubleBonds().doubleBondPositions.remove(kv.getKey());
            }
            fattyAcylStack.peekLast().getDoubleBonds().setNumDoubleBonds(fattyAcylStack.peekLast().getDoubleBonds().doubleBondPositions.size());

        }
        // check functionalGroups
        HashMap<String, ArrayList<FunctionalGroup>> cyclo_fg = new HashMap<>();
        HashSet<String> remove_list = new HashSet<>();
        FattyAcid curr_fa = fattyAcylStack.peekLast();

        if (curr_fa.getFunctionalGroups().containsKey("noyloxy")) {
            ArrayList<Integer> remove_item = new ArrayList<>();
            int i = 0;
            for (FunctionalGroup func_group : curr_fa.getFunctionalGroups().get("noyloxy")) {
                if (start <= func_group.getPosition() && func_group.getPosition() <= end) {
                    CarbonChain cc = new CarbonChain((FattyAcid) func_group, func_group.getPosition(), knownFunctionalGroups);

                    if (!curr_fa.getFunctionalGroups().containsKey("cc")) {
                        curr_fa.getFunctionalGroups().put("cc", new ArrayList<>());
                    }
                    curr_fa.getFunctionalGroups().get("cc").add(cc);
                    remove_item.add(i);
                }
                ++i;
            }
            for (int ii = remove_item.size() - 1; ii >= 0; --ii) {
                curr_fa.getFunctionalGroups().get("noyloxy").remove((int) remove_item.get(ii));
            }
            if (curr_fa.getFunctionalGroups().get("noyloxy").isEmpty()) {
                remove_list.add("noyloxy");
            }
        }

        for (Entry<String, ArrayList<FunctionalGroup>> kv : curr_fa.getFunctionalGroups().entrySet()) {
            ArrayList<Integer> remove_item = new ArrayList<>();
            int i = 0;
            for (FunctionalGroup func_group : kv.getValue()) {
                if (start <= func_group.getPosition() && func_group.getPosition() <= end) {
                    if (!cyclo_fg.containsKey(kv.getKey())) {
                        cyclo_fg.put(kv.getKey(), new ArrayList<>());
                    }
                    cyclo_fg.get(kv.getKey()).add(func_group);
                    remove_item.add(i);
                }
                ++i;
            }
            for (int ii = remove_item.size() - 1; ii >= 0; --ii) {
                kv.getValue().remove((int) remove_item.get(ii));
            }
            if (kv.getValue().isEmpty()) {
                remove_list.add(kv.getKey());
            }
        }
        for (String fg : remove_list) {
            curr_fa.getFunctionalGroups().remove(fg);
        }

        ArrayList<Element> bridge_chain = new ArrayList<>();
        if (tmp.containsKey("furan")) {
            tmp.remove("furan");
            bridge_chain.add(Element.O);
        }

        Cycle cycle = new Cycle(end - start + 1 + bridge_chain.size(), start, end, cyclo_db, cyclo_fg, bridge_chain, knownFunctionalGroups);
        if (!fattyAcylStack.peekLast().getFunctionalGroups().containsKey("cy")) {
            fattyAcylStack.peekLast().getFunctionalGroups().put("cy", new ArrayList<>());
        }
        fattyAcylStack.peekLast().getFunctionalGroups().get("cy").add(cycle);
    }

    public void add_wax_ester(TreeNode node) {
        FattyAcid fa = fattyAcylStack.pollLast();
        fa.setName(fa.getName() + "1");
        fa.lipidFaBondType = LipidFaBondType.AMINE;
        fattyAcylStack.peekLast().setName(fa.getName() + "2");
        fattyAcylStack.addFirst(fa);
    }

    public void set_methyl(TreeNode node) {
        fattyAcylStack.peekLast().numCarbon += 1;
    }

    public void set_acetic_acid(TreeNode node) {
        fattyAcylStack.peekLast().numCarbon += 2;
        headgroup = "FA";
    }

    public void set_methylene(TreeNode node) {
        tmp.put("fg_type", "methylene");
        GenericList gl = (GenericList) tmp.get("fg_pos");
        if (gl.size() > 1) {
            if ((int) ((GenericList) gl.get(0)).get(0) < (int) ((GenericList) gl.get(1)).get(0)) {
                ((GenericList) gl.get(1)).set(0, (int) ((GenericList) gl.get(1)).get(0) + 1);
            } else if ((int) ((GenericList) gl.get(0)).get(0) > (int) ((GenericList) gl.get(1)).get(0)) {
                ((GenericList) gl.get(0)).set(0, (int) ((GenericList) gl.get(0)).get(0) + 1);
            }
            fattyAcylStack.peekLast().numCarbon += 1;
            tmp.put("add_methylene", 1);
        }
    }

    public void set_car(TreeNode node) {
        tmp.put("fg_pos", new GenericList());
        tmp.put("fg_type", "");
    }

    public void add_car(TreeNode node) {
        headgroup = "CAR";
    }

    public void add_ethanolamine(TreeNode node) {
        headgroup = "NAE";
    }

    public void add_amine_name(TreeNode node) {
        headgroup = "NA";
    }

    public void reset_length(TreeNode node) {
        tmp.put("length", 0);
        tmp.put("length_pattern", "");
        tmp.put("length_tokens", new GenericList());
        tmp.put("add_lengths", 1);
    }

    public void set_fatty_length(TreeNode node) {
        tmp.put("add_lengths", 0);
    }

    public void last_number(TreeNode node) {
        if ((int) tmp.get("add_lengths") == 1) {
            tmp.put("length", (int) tmp.get("length") + LAST_NUMBERS.get(node.getText()));
            tmp.put("length_pattern", (String) tmp.get("length_pattern") + "L");
            ((GenericList) tmp.get("length_tokens")).add(LAST_NUMBERS.get(node.getText()));
        }
    }

    public void second_number(TreeNode node) {
        if ((int) tmp.get("add_lengths") == 1) {
            tmp.put("length", (int) tmp.get("length") + SECOND_NUMBERS.get(node.getText()));
            tmp.put("length_pattern", (String) tmp.get("length_pattern") + "S");
            ((GenericList) tmp.get("length_tokens")).add(SECOND_NUMBERS.get(node.getText()));
        }
    }

    public void special_number(TreeNode node) {
        if ((int) tmp.get("add_lengths") == 1) {
            tmp.put("length", (int) tmp.get("length") + SPECIAL_NUMBERS.get(node.getText()));
            tmp.put("length_pattern", (String) tmp.get("length_pattern") + "X");
            ((GenericList) tmp.get("length_tokens")).add(SPECIAL_NUMBERS.get(node.getText()));
        }
    }

    public void set_iso(TreeNode node) {
        FattyAcid curr_fa = fattyAcylStack.peekLast();
        curr_fa.numCarbon -= 1;
        FunctionalGroup fg = knownFunctionalGroups.get("Me");
        fg.setPosition(2);
        if (!curr_fa.getFunctionalGroups().containsKey("Me")) {
            curr_fa.getFunctionalGroups().put("Me", new ArrayList<>());
        }
        curr_fa.getFunctionalGroups().get("Me").add(fg);
    }

    public void set_prosta(TreeNode node) {
        int minus_pos = 0;
        if (tmp.containsKey("reduction")) {
            for (Object o : (GenericList) tmp.get("reduction")) {
                int i = (int) o;
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

        ((GenericList) tmp.get("fg_pos")).add(l1);
        ((GenericList) tmp.get("fg_pos")).add(l2);
        tmp.put("fg_type", "cy");
    }

    public void set_tetrahydrofuran(TreeNode node) {
        tmp.put("furan", 1);
        tmp.put("tetrahydrofuran", 1);
        set_cycle(node);
    }

    public void set_furan(TreeNode node) {
        tmp.put("furan", 1);
        set_cycle(node);
    }

    public void check_db(TreeNode node) {
        String fa_i = FA_I();
        FattyAcid curr_fa = fattyAcylStack.peekLast();
        if (((Dictionary) tmp.get(fa_i)).containsKey("fg_pos_summary")) {
            for (Entry<String, Object> kv : ((Dictionary) ((Dictionary) tmp.get(fa_i)).get("fg_pos_summary")).entrySet()) {
                int k = Integer.valueOf(kv.getKey());
                String v = (String) ((Dictionary) ((Dictionary) tmp.get(fa_i)).get("fg_pos_summary")).get(kv.getKey());
                if (k > 0 && !curr_fa.getDoubleBonds().doubleBondPositions.containsKey(k) && (v.equals("E") || v.equals("Z") || v.length() == 0)) {
                    curr_fa.getDoubleBonds().doubleBondPositions.put(k, v);
                    curr_fa.getDoubleBonds().setNumDoubleBonds(curr_fa.getDoubleBonds().doubleBondPositions.size());
                }
            }
        }
    }

    public void set_coa(TreeNode node) {
        headgroup = "CoA";
    }

    public void set_yl_ending(TreeNode node) {
        int l = Integer.valueOf(node.getText()) - 1;
        if (l == 0) {
            return;
        }

        FattyAcid curr_fa = fattyAcylStack.peekLast();

        if (tmp.containsKey("furan")) {
            curr_fa.numCarbon -= l;
            return;
        }

        String fname = "";
        FunctionalGroup fg = null;
        if (l == 1) {
            fname = "Me";
            fg = knownFunctionalGroups.get(fname);
        } else if (l == 2) {
            fname = "Et";
            fg = knownFunctionalGroups.get(fname);
        } else {
            FattyAcid fa = new FattyAcid("FA", l, knownFunctionalGroups);
            // shift functional groups
            for (Entry<String, ArrayList<FunctionalGroup>> kv : curr_fa.getFunctionalGroups().entrySet()) {
                ArrayList<Integer> remove_item = new ArrayList<>();
                int i = 0;
                for (FunctionalGroup func_group : kv.getValue()) {
                    if (func_group.getPosition() <= l) {
                        remove_item.add(i);
                        if (!fa.getFunctionalGroups().containsKey(kv.getKey())) {
                            fa.getFunctionalGroups().put(kv.getKey(), new ArrayList<>());
                        }
                        func_group.setPosition(l + 1 - func_group.getPosition());
                        fa.getFunctionalGroups().get(kv.getKey()).add(func_group);
                    }
                }
                for (int ii = remove_item.size() - 1; ii >= 0; --ii) {
                    curr_fa.getFunctionalGroups().get(kv.getKey()).remove((int) remove_item.get(ii));
                }
            }
            Map<String, ArrayList<FunctionalGroup>> func_dict = curr_fa.getFunctionalGroups();
            curr_fa.setFunctionalGroups(new HashMap<>());
            for (Entry<String, ArrayList<FunctionalGroup>> kv : func_dict.entrySet()) {
                if (kv.getValue().size() > 0) {
                    curr_fa.getFunctionalGroups().put(kv.getKey(), kv.getValue());
                }
            }

            // shift double bonds
            if (curr_fa.getDoubleBonds().doubleBondPositions.size() > 0) {
                fa.setDoubleBonds(new DoubleBonds());
                for (Entry<Integer, String> kv : curr_fa.getDoubleBonds().doubleBondPositions.entrySet()) {
                    if (kv.getKey() <= l) {
                        fa.getDoubleBonds().doubleBondPositions.put(l + 1 - kv.getKey(), kv.getValue());
                    }
                }
                fa.getDoubleBonds().setNumDoubleBonds(fa.getDoubleBonds().doubleBondPositions.size());
                for (Entry<Integer, String> kv : fa.getDoubleBonds().doubleBondPositions.entrySet()) {
                    curr_fa.getDoubleBonds().doubleBondPositions.remove(kv.getKey());
                }
            }
            fname = "cc";
            fg = new CarbonChain(fa, knownFunctionalGroups);
        }
        curr_fa.numCarbon -= l;
        fg.setPosition(l);
        curr_fa.shiftPositions(-l);
        if (!curr_fa.getFunctionalGroups().containsKey(fname)) {
            curr_fa.getFunctionalGroups().put(fname, new ArrayList<>());
        }
        curr_fa.getFunctionalGroups().get(fname).add(fg);
    }

    public void set_dial(TreeNode node) {
        FattyAcid curr_fa = fattyAcylStack.peekLast();
        int pos = curr_fa.numCarbon;
        FunctionalGroup fg = knownFunctionalGroups.get("oxo");
        fg.setPosition(pos);
        if (!curr_fa.getFunctionalGroups().containsKey("oxo")) {
            curr_fa.getFunctionalGroups().put("oxo", new ArrayList<>());
        }
        curr_fa.getFunctionalGroups().get("oxo").add(fg);
    }

    public void open_db_length(TreeNode node) {
        tmp.put("add_lengths", 1);
    }

    public void close_db_length(TreeNode node) {
        tmp.put("add_lengths", 0);
    }

    public void set_dioic(TreeNode node) {
        headgroup = "FA";
        int pos = (((GenericList) tmp.get("fg_pos")).size() == 2) ? (int) ((GenericList) ((GenericList) tmp.get("fg_pos")).get(1)).get(0) : fattyAcylStack.peekLast().numCarbon;
        if (tmp.containsKey("reduction")) {
            pos -= ((GenericList) tmp.get("reduction")).size();
        }
        fattyAcylStack.peekLast().numCarbon -= 1;
        FunctionalGroup func_group = knownFunctionalGroups.get("COOH");
        func_group.setPosition(pos - 1);
        if (!fattyAcylStack.peekLast().getFunctionalGroups().containsKey("COOH")) {
            fattyAcylStack.peekLast().getFunctionalGroups().put("COOH", new ArrayList<>());
        }
        fattyAcylStack.peekLast().getFunctionalGroups().get("COOH").add(func_group);
    }

    public void setup_hydroxyl(TreeNode node) {
        tmp.put("hydroxyl_pos", new GenericList());
    }

    public void add_hydroxyls(TreeNode node) {
        if (((GenericList) tmp.get("hydroxyl_pos")).size() > 1) {
            FunctionalGroup fg_oh = knownFunctionalGroups.get("OH");
            ArrayList<Integer> sorted_pos = new ArrayList<>();
            for (Object o : (GenericList) tmp.get("hydroxyl_pos")) {
                int i = (int) o;
                sorted_pos.add(i);
            }
            Collections.sort(sorted_pos, (Integer a, Integer b) -> b.compareTo(a));
            for (int i = 0; i < sorted_pos.size() - 1; ++i) {
                int pos = sorted_pos.get(i);
                FunctionalGroup fg_insert = fg_oh.copy();
                fg_insert.setPosition(pos);
                if (!fattyAcylStack.peekLast().getFunctionalGroups().containsKey("OH")) {
                    fattyAcylStack.peekLast().getFunctionalGroups().put("OH", new ArrayList<>());
                }
                fattyAcylStack.peekLast().getFunctionalGroups().get("OH").add(fg_insert);
            }
        }
    }

    public void set_ate(TreeNode node) {
        fattyAcylStack.peekLast().numCarbon += ATE.get(node.getText());
        headgroup = "WE";
    }

    public void add_hydroxyl(TreeNode node) {
        int h = Integer.valueOf(node.getText());
        ((GenericList) tmp.get("hydroxyl_pos")).add(h);
    }

    public void set_functional_group(TreeNode node) {
        tmp.put("fg_pos", new GenericList());
        tmp.put("fg_type", "");
    }

    public void add_functional_group(TreeNode node) {
        if (tmp.containsKey("added_func_group")) {
            tmp.remove("added_func_group");
            return;
        } else if (tmp.containsKey("add_methylene")) {
            tmp.remove("add_methylene");
            add_cyclo(node);
            return;
        }

        String t = (String) tmp.get("fg_type");

        FunctionalGroup fg = null;
        if (!t.equals("acetoxy")) {
            if (!FUNC_GROUPS.containsKey(t)) {
                throw new LipidException("Unknown functional group: '" + t + "'");
            }
            t = FUNC_GROUPS.get(t);
            if (t.length() == 0) {
                return;
            }
            fg = knownFunctionalGroups.get(t);
        } else {
            fg = new AcylAlkylGroup(new FattyAcid("O", 2, knownFunctionalGroups), knownFunctionalGroups);
        }

        FattyAcid fa = fattyAcylStack.peekLast();
        if (!fa.getFunctionalGroups().containsKey(t)) {
            fa.getFunctionalGroups().put(t, new ArrayList<>());
        }
        int l = ((GenericList) tmp.get("fg_pos")).size();
        for (Object o : (GenericList) tmp.get("fg_pos")) {
            GenericList lst = (GenericList) o;
            int pos = (int) lst.get(0);

            int num_pos = 0;
            if (tmp.containsKey("reduction")) {
                for (Object oo : (GenericList) tmp.get("reduction")) {
                    int i = (int) oo;
                    num_pos += i < pos ? 1 : 0;
                }
            }
            FunctionalGroup fg_insert = fg.copy();
            fg_insert.setPosition(pos - num_pos);
            fa.getFunctionalGroups().get(t).add(fg_insert);
        }
    }

    public void set_double_bond_information(TreeNode node) {
        ((Dictionary) tmp.get(FA_I())).put("db_position", 0);
        ((Dictionary) tmp.get(FA_I())).put("db_cistrans", "");
    }

    public void add_double_bond_information(TreeNode node) {
        int pos = (int) ((Dictionary) tmp.get(FA_I())).get("db_position");
        String str_pos = Integer.toString(pos);
        String cistrans = (String) ((Dictionary) tmp.get(FA_I())).get("db_cistrans");
        if (cistrans.length() == 0 && ((Dictionary) tmp.get(FA_I())).containsKey("fg_pos_summary") && ((Dictionary) ((Dictionary) tmp.get(FA_I())).get("fg_pos_summary")).containsKey(str_pos)) {
            cistrans = (String) ((Dictionary) ((Dictionary) tmp.get(FA_I())).get("fg_pos_summary")).get(str_pos);
        }
        if (pos == 0) {
            return;
        }

        cistrans = cistrans.toUpperCase();

        ((Dictionary) tmp.get(FA_I())).remove("db_position");
        ((Dictionary) tmp.get(FA_I())).remove("db_cistrans");

        if (!cistrans.equals("E") && !cistrans.equals("Z")) {
            cistrans = "";
        }
        if (!fattyAcylStack.peekLast().getDoubleBonds().doubleBondPositions.containsKey(pos) || fattyAcylStack.peekLast().getDoubleBonds().doubleBondPositions.get(pos).length() == 0) {
            fattyAcylStack.peekLast().getDoubleBonds().doubleBondPositions.put(pos, cistrans);
            fattyAcylStack.peekLast().getDoubleBonds().setNumDoubleBonds(fattyAcylStack.peekLast().getDoubleBonds().doubleBondPositions.size());
        }
    }

    public void set_cistrans(TreeNode node) {
        ((Dictionary) tmp.get(FA_I())).put("db_cistrans", node.getText());
    }

    public void set_double_bond_position(TreeNode node) {
        int pos = Integer.valueOf(node.getText());
        int num_db = 0;
        if (tmp.containsKey("reduction")) {
            GenericList gl = (GenericList) tmp.get("reduction");
            int l = gl.size();
            for (int i = 0; i < l; ++i) {
                num_db += ((int) gl.get(i) < pos) ? 1 : 0;
            }
        }

        ((Dictionary) tmp.get(FA_I())).put("db_position", pos - num_db);
    }

    public void add_summary(TreeNode node) {
        String fa_i = FA_I();
        ((Dictionary) tmp.get(fa_i)).put("fg_pos_summary", new Dictionary());
        for (Object o : (GenericList) tmp.get("fg_pos")) {
            GenericList lst = (GenericList) o;
            String k = Integer.toString((int) lst.get(0));
            String v = ((String) lst.get(1)).toUpperCase();
            ((Dictionary) ((Dictionary) tmp.get(fa_i)).get("fg_pos_summary")).put(k, v);
        }
    }

    public void set_functional_length(TreeNode node) {
        if ((int) tmp.get("length") != ((GenericList) tmp.get("fg_pos")).size()) {
            throw new LipidException("Length of functional group '" + Integer.toString((int) tmp.get("length")) + "' does not match with number of its positions '" + Integer.toString(((GenericList) tmp.get("fg_pos")).size()) + "'");
        }
    }

    public void set_functional_type(TreeNode node) {
        tmp.put("fg_type", node.getText());
    }

    public void add_epoxy(TreeNode node) {
        GenericList gl = (GenericList) tmp.get("fg_pos");
        while (gl.size() > 1) {
            gl.remove(gl.size() - 1);
        }
        tmp.put("fg_type", "Epoxy");
    }

    public void set_functional_position(TreeNode node) {
        GenericList gl = new GenericList();
        gl.add(0);
        gl.add("");
        ((GenericList) tmp.get("fg_pos")).add(gl);
    }

    public void set_functional_pos(TreeNode node) {
        GenericList gl = (GenericList) tmp.get("fg_pos");
        ((GenericList) gl.get(gl.size() - 1)).set(0, Integer.valueOf(node.getText()));
    }

    public void add_func_stereo(TreeNode node) {
        int l = ((GenericList) tmp.get("fg_pos")).size();
        ((GenericList) ((GenericList) tmp.get("fg_pos")).get(l - 1)).set(1, node.getText());
    }

    public void reduction(TreeNode node) {
        int shift_len = -((GenericList) tmp.get("fg_pos")).size();
        fattyAcylStack.peekLast().numCarbon += shift_len;
        for (Entry<String, ArrayList<FunctionalGroup>> kv : fattyAcylStack.peekLast().getFunctionalGroups().entrySet()) {
            for (FunctionalGroup func_group : kv.getValue()) {
                func_group.shiftPositions(shift_len);
            }
        }

        tmp.put("reduction", new GenericList());
        for (Object o : (GenericList) tmp.get("fg_pos")) {
            GenericList lst = (GenericList) o;
            ((GenericList) tmp.get("reduction")).add((int) lst.get(0));
        }
    }

    public void homo(TreeNode node) {
        tmp.put("post_adding", new GenericList());
        for (Object o : (GenericList) tmp.get("fg_pos")) {
            GenericList lst = (GenericList) o;
            ((GenericList) tmp.get("post_adding")).add((int) lst.get(0));
        }
    }

    void set_cycle(TreeNode node) {
        tmp.put("cyclo", 1);
    }

    public void rearrange_cycle(TreeNode node) {
        if (tmp.containsKey("post_adding")) {
            fattyAcylStack.peekLast().numCarbon += ((GenericList) tmp.get("post_adding")).size();
            tmp.remove("post_adding");
        }

        FattyAcid curr_fa = fattyAcylStack.peekLast();
        int start = (int) ((GenericList) ((GenericList) tmp.get("fg_pos")).get(0)).get(0);
        if (curr_fa.getFunctionalGroups().containsKey("cy")) {
            for (FunctionalGroup cy : curr_fa.getFunctionalGroups().get("cy")) {
                int shift_val = start - cy.getPosition();
                if (shift_val == 0) {
                    continue;
                }
                ((Cycle) cy).rearrangeFunctionalGroups(curr_fa, shift_val);
            }
        }
    }

    public void set_recursion(TreeNode node) {
        tmp.put("fg_pos", new GenericList());
        tmp.put("fg_type", "");
        fattyAcylStack.add(new FattyAcid("FA", knownFunctionalGroups));
        tmp.put(FA_I(), new Dictionary());
        ((Dictionary) tmp.get(FA_I())).put("recursion_pos", 0);
    }

    public void add_recursion(TreeNode node) {
        int pos = (int) ((Dictionary) tmp.get(FA_I())).get("recursion_pos");
        FattyAcid fa = fattyAcylStack.pollLast();

        fa.setPosition(pos);
        FattyAcid curr_fa = fattyAcylStack.peekLast();

        String fname = "";
        if (tmp.containsKey("cyclo_yl")) {
            fname = "cyclo";
            tmp.remove("cyclo_yl");
        } else {
            fname = headgroup;
        }
        if (!curr_fa.getFunctionalGroups().containsKey(fname)) {
            curr_fa.getFunctionalGroups().put(fname, new ArrayList<>());
        }
        curr_fa.getFunctionalGroups().get(fname).add(fa);
        tmp.put("added_func_group", 1);
    }

    public void set_recursion_pos(TreeNode node) {
        ((Dictionary) tmp.get(FA_I())).put("recursion_pos", Integer.valueOf(node.getText()));
    }

}
