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
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Map.entry;
import java.util.Set;
import org.lifstools.jgoslin.domain.ConstraintViolationException;

/**
 * Event handler implementation for the {@link FattyAcidParser}.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public class FattyAcidParserEventHandler extends BaseParserEventHandler<LipidAdduct> {

    private final KnownFunctionalGroups knownFunctionalGroups;
    private LipidLevel level;
    private String headgroup;
    private ArrayDeque<FattyAcid> fattyAcylStack;
    private Dictionary tmp;

    private static final Map<String, Integer> LAST_NUMBERS = Map.ofEntries(
            entry("un", 1),
            entry("hen", 1),
            entry("do", 2),
            entry("di", 2),
            entry("tri", 3),
            entry("buta", 4),
            entry("but", 4),
            entry("tetra", 4),
            entry("penta", 5),
            entry("pent", 5),
            entry("hexa", 6),
            entry("hex", 6),
            entry("hepta", 7),
            entry("hept", 7),
            entry("octa", 8),
            entry("oct", 8),
            entry("nona", 9),
            entry("non", 9)
    );

    private static final Map<String, Integer> SECOND_NUMBERS = Map.ofEntries(
            entry("deca", 10),
            entry("dec", 10),
            entry("eicosa", 20),
            entry("eicos", 20),
            entry("cosa", 20),
            entry("cos", 20),
            entry("triaconta", 30),
            entry("triacont", 30),
            entry("tetraconta", 40),
            entry("tetracont", 40),
            entry("pentaconta", 50),
            entry("pentacont", 50),
            entry("hexaconta", 60),
            entry("hexacont", 60),
            entry("heptaconta", 70),
            entry("heptacont", 70),
            entry("octaconta", 80),
            entry("octacont", 80),
            entry("nonaconta", 90),
            entry("nonacont", 90)
    );

    private static final Map<String, String> FUNC_GROUPS = Map.ofEntries(
            entry("keto", "oxo"),
            entry("ethyl", "Et"),
            entry("hydroxy", "OH"),
            entry("phospho", "Ph"),
            entry("oxo", "oxo"),
            entry("bromo", "Br"),
            entry("methyl", "Me"),
            entry("hydroperoxy", "OOH"),
            entry("homo", ""),
            entry("Epoxy", "Ep"),
            entry("fluro", "F"),
            entry("fluoro", "F"),
            entry("chloro", "Cl"),
            entry("methylene", "My"),
            entry("sulfooxy", "Su"),
            entry("amino", "NH2"),
            entry("sulfanyl", "SH"),
            entry("methoxy", "OMe"),
            entry("iodo", "I"),
            entry("cyano", "CN"),
            entry("nitro", "NO2"),
            entry("OH", "OH"),
            entry("thio", "SH"),
            entry("mercapto", "SH"),
            entry("carboxy", "COOH"),
            entry("acetoxy", "Ac"),
            entry("cysteinyl", "Cys"),
            entry("phenyl", "Phe"),
            entry("s-glutathionyl", "SGlu"),
            entry("s-cysteinyl", "SCys"),
            entry("butylperoxy", "BOO"),
            entry("dimethylarsinoyl", "MMAs"),
            entry("methylsulfanyl", "SMe"),
            entry("imino", "NH"),
            entry("s-cysteinylglycinyl", "SCG")
    );

    private static final Map<String, Integer> ATE = Map.ofEntries(
            entry("formate", 1),
            entry("acetate", 2),
            entry("butyrate", 4),
            entry("propionate", 3),
            entry("valerate", 5),
            entry("isobutyrate", 4)
    );

    private static final Map<String, Integer> SPECIAL_NUMBERS = Map.ofEntries(
            entry("meth", 1),
            entry("etha", 2),
            entry("eth", 2),
            entry("propa", 3),
            entry("isoprop", 3),
            entry("prop", 3),
            entry("propi", 3),
            entry("propio", 3),
            entry("buta", 4),
            entry("but", 4),
            entry("butr", 4),
            entry("furan", 5),
            entry("valer", 5),
            entry("eicosa", 20),
            entry("eicos", 20),
            entry("icosa", 20),
            entry("icos", 20),
            entry("prosta", 20),
            entry("prost", 20),
            entry("prostan", 20)
    );

    private static final Set<String> NOIC_SET = Set.of("noic acid", "nic acid", "dioic_acid");
    private static final Set<String> NAL_SET = Set.of("nal", "dial");
    private static final Set<String> ACETATE_SET = Set.of("acetate", "noate", "nate");

    /**
     * Create a new {@code FattyAcidParserEventHandler}.
     *
     * @param knownFunctionalGroups the known functional groups
     */
    public FattyAcidParserEventHandler(KnownFunctionalGroups knownFunctionalGroups) {
        this.knownFunctionalGroups = knownFunctionalGroups;
        try {
            registeredEvents = Map.ofEntries(
                    entry("lipid_pre_event", this::resetParser),
                    entry("lipid_post_event", this::build_lipid),
                    entry("fatty_acid_post_event", this::set_fatty_acid),
                    entry("fatty_acid_recursion_post_event", this::set_fatty_acid),
                    entry("acid_single_type_pre_event", this::set_fatty_acyl_type),
                    entry("ol_ending_pre_event", this::set_fatty_acyl_type),
                    entry("double_bond_position_pre_event", this::set_double_bond_information),
                    entry("double_bond_position_post_event", this::add_double_bond_information),
                    entry("db_number_post_event", this::set_double_bond_position),
                    entry("cistrans_post_event", this::set_cistrans),
                    entry("acid_type_double_post_event", this::check_db),
                    entry("db_length_pre_event", this::open_db_length),
                    entry("db_length_post_event", this::close_db_length),
                    // lengths
                    entry("functional_length_pre_event", this::reset_length),
                    entry("fatty_length_pre_event", this::reset_length),
                    entry("functional_length_post_event", this::set_functional_length),
                    entry("fatty_length_post_event", this::set_fatty_length),
                    // numbers
                    entry("notation_specials_pre_event", this::special_number),
                    entry("notation_last_digit_pre_event", this::last_number),
                    entry("notation_second_digit_pre_event", this::second_number),
                    // functional groups
                    entry("functional_group_pre_event", this::set_functional_group),
                    entry("functional_group_post_event", this::add_functional_group),
                    entry("functional_pos_pre_event", this::set_functional_pos),
                    entry("functional_position_pre_event", this::set_functional_position),
                    entry("functional_group_type_pre_event", this::set_functional_type),
                    // cyclo / epoxy
                    entry("cyclo_position_pre_event", this::set_functional_group),
                    entry("cyclo_position_post_event", this::rearrange_cycle),
                    entry("epoxy_pre_event", this::set_functional_group),
                    entry("epoxy_post_event", this::add_epoxy),
                    entry("cycle_pre_event", this::set_cycle),
                    entry("methylene_post_event", this::set_methylene),
                    // dioic
                    entry("dioic_pre_event", this::set_functional_group),
                    entry("dioic_post_event", this::set_dioic),
                    entry("dioic_acid_pre_event", this::set_fatty_acyl_type),
                    entry("dial_post_event", this::set_dial),
                    // prosta
                    entry("prosta_pre_event", this::set_prosta),
                    entry("prosta_post_event", this::add_cyclo),
                    entry("reduction_pre_event", this::set_functional_group),
                    entry("reduction_post_event", this::reduction),
                    entry("homo_post_event", this::homo),
                    // recursion
                    entry("recursion_description_pre_event", this::set_recursion),
                    entry("recursion_description_post_event", this::add_recursion),
                    entry("recursion_pos_pre_event", this::set_recursion_pos),
                    entry("yl_ending_pre_event", this::set_yl_ending),
                    entry("acetic_acid_post_event", this::set_acetic_acid),
                    entry("acetic_recursion_pre_event", this::set_recursion),
                    entry("acetic_recursion_post_event", this::add_recursion),
                    entry("hydroxyl_number_pre_event", this::add_hydroxyl),
                    entry("ol_pre_event", this::setup_hydroxyl),
                    entry("ol_post_event", this::add_hydroxyls),
                    entry("ol_pos_post_event", this::set_yl_ending),
                    // wax esters
                    entry("wax_ester_pre_event", this::set_recursion),
                    entry("wax_ester_post_event", this::add_wax_ester),
                    entry("ate_post_event", this::set_ate),
                    entry("isoprop_post_event", this::set_iso),
                    entry("isobut_post_event", this::set_iso),
                    // CoA
                    entry("coa_post_event", this::set_coa),
                    entry("methyl_pre_event", this::set_methyl),
                    // CAR
                    entry("car_pre_event", this::set_car),
                    entry("car_post_event", this::add_car),
                    // furan
                    entry("tetrahydrofuran_pre_event", this::set_tetrahydrofuran),
                    entry("furan_pre_event", this::set_furan),
                    // amine
                    entry("ethanolamine_post_event", this::add_ethanolamine),
                    entry("amine_n_pre_event", this::set_recursion),
                    entry("amine_n_post_event", this::add_amine),
                    entry("amine_post_event", this::add_amine_name),
                    // functional group position summary
                    entry("fg_pos_summary_pre_event", this::set_functional_group),
                    entry("fg_pos_summary_post_event", this::add_summary),
                    entry("func_stereo_pre_event", this::add_func_stereo)
            );
        } catch (Exception e) {
            throw new ConstraintViolationException("Cannot initialize FattyAcidParserEventHandler.", e);
        }
    }

    private String FA_I() {
        return "fa" + Integer.toString(fattyAcylStack.size());
    }

    private void set_lipid_level(LipidLevel _level) {
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

    void build_lipid(TreeNode node) {
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
            curr_fa_p.setNumCarbon(curr_fa_p.getNumCarbon() + s);
            for (int i = 0; i < s; ++i) {
                int pos = (int) ((GenericList) tmp.get("post_adding")).get(i);
                curr_fa_p.addPosition(pos);
                DoubleBonds db = new DoubleBonds(curr_fa_p.getDoubleBonds().getNumDoubleBonds());
                for (Entry<Integer, String> kv : curr_fa_p.getDoubleBonds().getDoubleBondPositions().entrySet()) {
                    db.getDoubleBondPositions().put(kv.getKey() + (kv.getKey() >= pos ? 1 : 0), kv.getValue());
                }
                db.setNumDoubleBonds(db.getDoubleBondPositions().size());
                curr_fa_p.setDoubleBonds(db);
            }
        }

        FattyAcid curr_fa = fattyAcylStack.peekLast();
        if (curr_fa.getDoubleBonds().getDoubleBondPositions().size() > 0) {
            int db_right = 0;
            for (Entry<Integer, String> kv : curr_fa.getDoubleBonds().getDoubleBondPositions().entrySet()) {
                db_right += kv.getValue().length() > 0 ? 1 : 0;
            }
            if (db_right != curr_fa.getDoubleBonds().getDoubleBondPositions().size()) {
                set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
            }
        }

        Headgroup head_group = new Headgroup(headgroup);

        content = new LipidAdduct();

        switch (level) {
            case COMPLETE_STRUCTURE ->
                content.setLipid(new LipidCompleteStructure(head_group, fattyAcylStack, knownFunctionalGroups));
            case FULL_STRUCTURE ->
                content.setLipid(new LipidFullStructure(head_group, fattyAcylStack, knownFunctionalGroups));
            case STRUCTURE_DEFINED ->
                content.setLipid(new LipidStructureDefined(head_group, fattyAcylStack, knownFunctionalGroups));
            case SN_POSITION ->
                content.setLipid(new LipidSnPosition(head_group, fattyAcylStack, knownFunctionalGroups));
            case MOLECULAR_SPECIES ->
                content.setLipid(new LipidMolecularSpecies(head_group, fattyAcylStack, knownFunctionalGroups));
            case SPECIES ->
                content.setLipid(new LipidSpecies(head_group, fattyAcylStack, knownFunctionalGroups));
            default -> {
            }
        }
    }

    void set_fatty_acyl_type(TreeNode node) {
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
            fattyAcylStack.peekLast().setLipidFaBondType(LipidFaBondType.ETHER);
        } else {
            headgroup = t;
        }
    }

    private void add_amine(TreeNode node) {
        FattyAcid fa = fattyAcylStack.pollLast();

        fa.setLipidFaBondType(LipidFaBondType.AMIDE);
        fattyAcylStack.getLast().setLipidFaBondType(LipidFaBondType.AMIDE);
        fattyAcylStack.addFirst(fa);
    }

    private void set_fatty_acid(TreeNode node) {
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
                throw new ConstraintViolationException("Cannot determine fatty acid and double bond length in '" + node.getText() + "'");
            }
            curr_fa.setNumCarbon(curr_fa.getNumCarbon() + l);
            if (curr_fa.getDoubleBonds().getDoubleBondPositions().isEmpty() && d > 0) {
                curr_fa.getDoubleBonds().setNumDoubleBonds(d);
            }
        }

        if (curr_fa.getFunctionalGroupsInternal().containsKey("noyloxy")) {
            if (headgroup.equals("FA")) {
                headgroup = "FAHFA";
            }

            while (curr_fa.getFunctionalGroupsInternal().get("noyloxy").size() > 0) {
                FattyAcid fa = (FattyAcid) curr_fa.getFunctionalGroupsInternal().get("noyloxy").get(curr_fa.getFunctionalGroupsInternal().get("noyloxy").size() - 1);
                curr_fa.getFunctionalGroupsInternal().get("noyloxy").remove(curr_fa.getFunctionalGroupsInternal().get("noyloxy").size() - 1);

                AcylAlkylGroup acyl = new AcylAlkylGroup(fa, knownFunctionalGroups);
                acyl.setPosition(fa.getPosition());

                if (!curr_fa.getFunctionalGroupsInternal().containsKey("acyl")) {
                    curr_fa.getFunctionalGroupsInternal().put("acyl", new ArrayList<>());
                }
                curr_fa.getFunctionalGroupsInternal().get("acyl").add(acyl);
            }
            curr_fa.getFunctionalGroupsInternal().remove("noyloxy");
        } else if (curr_fa.getFunctionalGroupsInternal().containsKey("nyloxy") || curr_fa.getFunctionalGroupsInternal().containsKey("yloxy")) {
            String yloxy = curr_fa.getFunctionalGroupsInternal().containsKey("nyloxy") ? "nyloxy" : "yloxy";
            while (curr_fa.getFunctionalGroupsInternal().get(yloxy).size() > 0) {
                FattyAcid fa = (FattyAcid) curr_fa.getFunctionalGroupsInternal().get(yloxy).get(curr_fa.getFunctionalGroupsInternal().get(yloxy).size() - 1);
                curr_fa.getFunctionalGroupsInternal().get(yloxy).remove(curr_fa.getFunctionalGroupsInternal().get(yloxy).size() - 1);

                AcylAlkylGroup alkyl = new AcylAlkylGroup(fa, -1, 1, true, knownFunctionalGroups);
                alkyl.setPosition(fa.getPosition());

                if (!curr_fa.getFunctionalGroupsInternal().containsKey("alkyl")) {
                    curr_fa.getFunctionalGroupsInternal().put("alkyl", new ArrayList<>());
                }
                curr_fa.getFunctionalGroupsInternal().get("alkyl").add(alkyl);
            }
            curr_fa.getFunctionalGroupsInternal().remove(yloxy);
        } else {
            boolean has_yl = false;
            for (Entry<String, ArrayList<FunctionalGroup>> kv : curr_fa.getFunctionalGroupsInternal().entrySet()) {
                if (kv.getKey().endsWith("yl")) {
                    has_yl = true;
                    break;
                }
            }
            if (has_yl) {
                while (true) {
                    String yl = "";
                    for (Entry<String, ArrayList<FunctionalGroup>> kv : curr_fa.getFunctionalGroupsInternal().entrySet()) {
                        if (kv.getKey().endsWith("yl")) {
                            yl = kv.getKey();
                            break;
                        }
                    }
                    if (yl.length() == 0) {
                        break;
                    }

                    while (curr_fa.getFunctionalGroupsInternal().get(yl).size() > 0) {
                        FattyAcid fa = (FattyAcid) curr_fa.getFunctionalGroupsInternal().get(yl).get(curr_fa.getFunctionalGroupsInternal().get(yl).size() - 1);
                        curr_fa.getFunctionalGroupsInternal().get(yl).remove(curr_fa.getFunctionalGroupsInternal().get(yl).size() - 1);

                        if (tmp.containsKey("cyclo")) {
                            int cyclo_len = curr_fa.getNumCarbon();
                            tmp.put("cyclo_len", cyclo_len);
                            if (fa.getPosition() != cyclo_len && !tmp.containsKey("furan")) {
                                switch_position(curr_fa, 2 + cyclo_len);
                            }
                            fa.shiftPositions(cyclo_len);
                            if (tmp.containsKey("furan")) {
                                curr_fa.shiftPositions(-1);
                            }

                            for (Entry<String, ArrayList<FunctionalGroup>> kv : fa.getFunctionalGroupsInternal().entrySet()) {
                                if (!curr_fa.getFunctionalGroupsInternal().containsKey(kv.getKey())) {
                                    curr_fa.getFunctionalGroupsInternal().put(kv.getKey(), new ArrayList<>());
                                }
                                for (FunctionalGroup func_group : kv.getValue()) {
                                    curr_fa.getFunctionalGroupsInternal().get(kv.getKey()).add(func_group);
                                }
                            }

                            curr_fa.setNumCarbon(cyclo_len + fa.getNumCarbon());

                            for (Entry<Integer, String> kv : fa.getDoubleBonds().getDoubleBondPositions().entrySet()) {
                                curr_fa.getDoubleBonds().getDoubleBondPositions().put(kv.getKey() + cyclo_len, kv.getValue());
                            }
                            curr_fa.getDoubleBonds().setNumDoubleBonds(curr_fa.getDoubleBonds().getDoubleBondPositions().size());

                            if (!tmp.containsKey("tetrahydrofuran") && tmp.containsKey("furan")) {
                                curr_fa.getDoubleBonds().setNumDoubleBonds(curr_fa.getDoubleBonds().getNumDoubleBonds() + 2);
                                if (!curr_fa.getDoubleBonds().getDoubleBondPositions().containsKey(1)) {
                                    curr_fa.getDoubleBonds().getDoubleBondPositions().put(1, "E");
                                }
                                if (!curr_fa.getDoubleBonds().getDoubleBondPositions().containsKey(3)) {
                                    curr_fa.getDoubleBonds().getDoubleBondPositions().put(3, "E");
                                }
                            }

                            tmp.put("cyclo_yl", true);
                        } else {
                            // add carbon chains here here
                            // special chains: i.e. ethyl, methyl
                            String fg_name = "";
                            if (fa.getDoubleBonds().getNumDoubleBonds() == 0 && fa.getFunctionalGroupsInternal().isEmpty()) {
                                FunctionalGroup fg = null;
                                if (fa.getNumCarbon() == 1) {
                                    fg_name = "Me";
                                    fg = knownFunctionalGroups.get(fg_name);
                                } else if (fa.getNumCarbon() == 2) {
                                    fg_name = "Et";
                                    fg = knownFunctionalGroups.get(fg_name);
                                }
                                if (fg != null && fg_name.length() > 0) {
                                    fg.setPosition(fa.getPosition());
                                    if (!curr_fa.getFunctionalGroupsInternal().containsKey(fg_name)) {
                                        curr_fa.getFunctionalGroupsInternal().put(fg_name, new ArrayList<>());
                                    }
                                    curr_fa.getFunctionalGroupsInternal().get(fg_name).add(fg);
                                }
                            }
                            if (fg_name.length() == 0) {
                                CarbonChain cc = new CarbonChain(fa, fa.getPosition(), knownFunctionalGroups);
                                if (!curr_fa.getFunctionalGroupsInternal().containsKey("cc")) {
                                    curr_fa.getFunctionalGroupsInternal().put("cc", new ArrayList<>());
                                }
                                curr_fa.getFunctionalGroupsInternal().get("cc").add(cc);
                            }
                        }
                    }
                    if (tmp.containsKey("cyclo")) {
                        tmp.remove("cyclo");
                    }
                    curr_fa.getFunctionalGroupsInternal().remove(yl);
                }
            }
        }

        if (curr_fa.getFunctionalGroupsInternal().containsKey("cyclo")) {
            FattyAcid fa = (FattyAcid) curr_fa.getFunctionalGroupsInternal().get("cyclo").get(0);
            curr_fa.getFunctionalGroupsInternal().remove("cyclo");
            if (!tmp.containsKey("cyclo_len")) {
                tmp.put("cyclo_len", 5);
            }
            int start_pos = curr_fa.getNumCarbon() + 1;
            int end_pos = curr_fa.getNumCarbon() + (int) tmp.get("cyclo_len");
            fa.shiftPositions(start_pos - 1);

            if (curr_fa.getFunctionalGroupsInternal().containsKey("cy")) {
                for (FunctionalGroup cy : curr_fa.getFunctionalGroupsInternal().get("cy")) {
                    cy.shiftPositions(start_pos - 1);
                }
            }
            for (Entry<String, ArrayList<FunctionalGroup>> kv : fa.getFunctionalGroupsInternal().entrySet()) {
                if (!curr_fa.getFunctionalGroupsInternal().containsKey(kv.getKey())) {
                    curr_fa.getFunctionalGroupsInternal().put(kv.getKey(), new ArrayList<>());
                }
                for (FunctionalGroup func_group : kv.getValue()) {
                    curr_fa.getFunctionalGroupsInternal().get(kv.getKey()).add(func_group);
                }
            }

            for (Entry<Integer, String> kv : fa.getDoubleBonds().getDoubleBondPositions().entrySet()) {
                curr_fa.getDoubleBonds().getDoubleBondPositions().put(kv.getKey() + start_pos - 1, kv.getValue());
            }
            curr_fa.getDoubleBonds().setNumDoubleBonds(curr_fa.getDoubleBonds().getDoubleBondPositions().size());

            if (!tmp.containsKey("tetrahydrofuran") && tmp.containsKey("furan")) {
                curr_fa.getDoubleBonds().setNumDoubleBonds(curr_fa.getDoubleBonds().getNumDoubleBonds() + 2);
                if (!curr_fa.getDoubleBonds().getDoubleBondPositions().containsKey(1 + curr_fa.getNumCarbon())) {
                    curr_fa.getDoubleBonds().getDoubleBondPositions().put(1 + curr_fa.getNumCarbon(), "E");
                }
                if (!curr_fa.getDoubleBonds().getDoubleBondPositions().containsKey(3 + curr_fa.getNumCarbon())) {
                    curr_fa.getDoubleBonds().getDoubleBondPositions().put(3 + curr_fa.getNumCarbon(), "E");
                }
            }

            curr_fa.setNumCarbon(curr_fa.getNumCarbon() + fa.getNumCarbon());

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
            tmp.put("cyclo_len", curr_fa.getNumCarbon());
            tmp.put("fg_pos", new GenericList());
            GenericList l1 = new GenericList();
            l1.add(1);
            l1.add("");
            ((GenericList) tmp.get("fg_pos")).add(l1);
            GenericList l2 = new GenericList();
            l2.add(curr_fa.getNumCarbon());
            l2.add("");
            ((GenericList) tmp.get("fg_pos")).add(l2);

            tmp.remove("cyclo");
        }

        tmp.put("length_pattern", "");
        tmp.put("length_tokens", new GenericList());
        tmp.put("add_lengths", 0);
    }

    private void switch_position(FunctionalGroup func_group, int switch_num) {
        func_group.setPosition(switch_num - func_group.getPosition());
        for (Entry<String, ArrayList<FunctionalGroup>> kv : func_group.getFunctionalGroupsInternal().entrySet()) {
            for (FunctionalGroup fg : kv.getValue()) {
                switch_position(fg, switch_num);
            }
        }
    }

    private void add_cyclo(TreeNode node) {
        int start = (int) ((GenericList) ((GenericList) tmp.get("fg_pos")).get(0)).get(0);
        int end = (int) ((GenericList) ((GenericList) tmp.get("fg_pos")).get(1)).get(0);

        DoubleBonds cyclo_db = new DoubleBonds();
        // check double bonds
        if (fattyAcylStack.peekLast().getDoubleBonds().getDoubleBondPositions().size() > 0) {
            for (Entry<Integer, String> kv : fattyAcylStack.peekLast().getDoubleBonds().getDoubleBondPositions().entrySet()) {
                if (start <= kv.getKey() && kv.getKey() <= end) {
                    cyclo_db.getDoubleBondPositions().put(kv.getKey(), kv.getValue());
                }
            }
            cyclo_db.setNumDoubleBonds(cyclo_db.getDoubleBondPositions().size());

            for (Entry<Integer, String> kv : cyclo_db.getDoubleBondPositions().entrySet()) {
                fattyAcylStack.peekLast().getDoubleBonds().getDoubleBondPositions().remove(kv.getKey());
            }
            fattyAcylStack.peekLast().getDoubleBonds().setNumDoubleBonds(fattyAcylStack.peekLast().getDoubleBonds().getDoubleBondPositions().size());

        }
        // check functionalGroups
        HashMap<String, ArrayList<FunctionalGroup>> cyclo_fg = new HashMap<>();
        HashSet<String> remove_list = new HashSet<>();
        FattyAcid curr_fa = fattyAcylStack.peekLast();

        if (curr_fa.getFunctionalGroupsInternal().containsKey("noyloxy")) {
            ArrayList<Integer> remove_item = new ArrayList<>();
            int i = 0;
            for (FunctionalGroup func_group : curr_fa.getFunctionalGroupsInternal().get("noyloxy")) {
                if (start <= func_group.getPosition() && func_group.getPosition() <= end) {
                    CarbonChain cc = new CarbonChain((FattyAcid) func_group, func_group.getPosition(), knownFunctionalGroups);

                    if (!curr_fa.getFunctionalGroupsInternal().containsKey("cc")) {
                        curr_fa.getFunctionalGroupsInternal().put("cc", new ArrayList<>());
                    }
                    curr_fa.getFunctionalGroupsInternal().get("cc").add(cc);
                    remove_item.add(i);
                }
                ++i;
            }
            for (int ii = remove_item.size() - 1; ii >= 0; --ii) {
                curr_fa.getFunctionalGroupsInternal().get("noyloxy").remove((int) remove_item.get(ii));
            }
            if (curr_fa.getFunctionalGroupsInternal().get("noyloxy").isEmpty()) {
                remove_list.add("noyloxy");
            }
        }

        for (Entry<String, ArrayList<FunctionalGroup>> kv : curr_fa.getFunctionalGroupsInternal().entrySet()) {
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
            curr_fa.getFunctionalGroupsInternal().remove(fg);
        }

        ArrayList<Element> bridge_chain = new ArrayList<>();
        if (tmp.containsKey("furan")) {
            tmp.remove("furan");
            bridge_chain.add(Element.O);
        }

        Cycle cycle = new Cycle(end - start + 1 + bridge_chain.size(), start, end, cyclo_db, cyclo_fg, bridge_chain, knownFunctionalGroups);
        if (!fattyAcylStack.peekLast().getFunctionalGroupsInternal().containsKey("cy")) {
            fattyAcylStack.peekLast().getFunctionalGroupsInternal().put("cy", new ArrayList<>());
        }
        fattyAcylStack.peekLast().getFunctionalGroupsInternal().get("cy").add(cycle);
    }

    private void add_wax_ester(TreeNode node) {
        FattyAcid fa = fattyAcylStack.pollLast();
        fa.setLipidFaBondType(LipidFaBondType.ETHER);
        fattyAcylStack.addFirst(fa);
    }

    private void set_methyl(TreeNode node) {
        fattyAcylStack.peekLast().setNumCarbon(fattyAcylStack.peekLast().getNumCarbon() + 1);
    }

    private void set_acetic_acid(TreeNode node) {
        fattyAcylStack.peekLast().setNumCarbon(fattyAcylStack.peekLast().getNumCarbon() + 2);
        headgroup = "FA";
    }

    private void set_methylene(TreeNode node) {
        tmp.put("fg_type", "methylene");
        GenericList gl = (GenericList) tmp.get("fg_pos");
        if (gl.size() > 1) {
            if ((int) ((GenericList) gl.get(0)).get(0) < (int) ((GenericList) gl.get(1)).get(0)) {
                ((GenericList) gl.get(1)).set(0, (int) ((GenericList) gl.get(1)).get(0) + 1);
            } else if ((int) ((GenericList) gl.get(0)).get(0) > (int) ((GenericList) gl.get(1)).get(0)) {
                ((GenericList) gl.get(0)).set(0, (int) ((GenericList) gl.get(0)).get(0) + 1);
            }
            fattyAcylStack.peekLast().setNumCarbon(fattyAcylStack.peekLast().getNumCarbon() + 1);
            tmp.put("add_methylene", 1);
        }
    }

    private void set_car(TreeNode node) {
        tmp.put("fg_pos", new GenericList());
        tmp.put("fg_type", "");
    }

    private void add_car(TreeNode node) {
        headgroup = "CAR";
    }

    private void add_ethanolamine(TreeNode node) {
        headgroup = "NAE";
    }

    private void add_amine_name(TreeNode node) {
        headgroup = "NA";
    }

    private void reset_length(TreeNode node) {
        tmp.put("length", 0);
        tmp.put("length_pattern", "");
        tmp.put("length_tokens", new GenericList());
        tmp.put("add_lengths", 1);
    }

    private void set_fatty_length(TreeNode node) {
        tmp.put("add_lengths", 0);
    }

    private void last_number(TreeNode node) {
        if ((int) tmp.get("add_lengths") == 1) {
            tmp.put("length", (int) tmp.get("length") + LAST_NUMBERS.get(node.getText()));
            tmp.put("length_pattern", (String) tmp.get("length_pattern") + "L");
            ((GenericList) tmp.get("length_tokens")).add(LAST_NUMBERS.get(node.getText()));
        }
    }

    private void second_number(TreeNode node) {
        if ((int) tmp.get("add_lengths") == 1) {
            tmp.put("length", (int) tmp.get("length") + SECOND_NUMBERS.get(node.getText()));
            tmp.put("length_pattern", (String) tmp.get("length_pattern") + "S");
            ((GenericList) tmp.get("length_tokens")).add(SECOND_NUMBERS.get(node.getText()));
        }
    }

    private void special_number(TreeNode node) {
        if ((int) tmp.get("add_lengths") == 1) {
            tmp.put("length", (int) tmp.get("length") + SPECIAL_NUMBERS.get(node.getText()));
            tmp.put("length_pattern", (String) tmp.get("length_pattern") + "X");
            ((GenericList) tmp.get("length_tokens")).add(SPECIAL_NUMBERS.get(node.getText()));
        }
    }

    private void set_iso(TreeNode node) {
        FattyAcid curr_fa = fattyAcylStack.peekLast();
        curr_fa.setNumCarbon(curr_fa.getNumCarbon() - 1);
        FunctionalGroup fg = knownFunctionalGroups.get("Me");
        fg.setPosition(2);
        if (!curr_fa.getFunctionalGroupsInternal().containsKey("Me")) {
            curr_fa.getFunctionalGroupsInternal().put("Me", new ArrayList<>());
        }
        curr_fa.getFunctionalGroupsInternal().get("Me").add(fg);
    }

    private void set_prosta(TreeNode node) {
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

    private void set_tetrahydrofuran(TreeNode node) {
        tmp.put("furan", 1);
        tmp.put("tetrahydrofuran", 1);
        set_cycle(node);
    }

    private void set_furan(TreeNode node) {
        tmp.put("furan", 1);
        set_cycle(node);
    }

    private void check_db(TreeNode node) {
        String fa_i = FA_I();
        FattyAcid curr_fa = fattyAcylStack.peekLast();
        if (((Dictionary) tmp.get(fa_i)).containsKey("fg_pos_summary")) {
            for (Entry<String, Object> kv : ((Dictionary) ((Dictionary) tmp.get(fa_i)).get("fg_pos_summary")).entrySet()) {
                int k = Integer.valueOf(kv.getKey());
                String v = (String) ((Dictionary) ((Dictionary) tmp.get(fa_i)).get("fg_pos_summary")).get(kv.getKey());
                if (k > 0 && !curr_fa.getDoubleBonds().getDoubleBondPositions().containsKey(k) && (v.equals("E") || v.equals("Z") || v.length() == 0)) {
                    curr_fa.getDoubleBonds().getDoubleBondPositions().put(k, v);
                    curr_fa.getDoubleBonds().setNumDoubleBonds(curr_fa.getDoubleBonds().getDoubleBondPositions().size());
                }
            }
        }
    }

    private void set_coa(TreeNode node) {
        headgroup = "CoA";
    }

    private void set_yl_ending(TreeNode node) {
        int l = Integer.valueOf(node.getText()) - 1;
        if (l == 0) {
            return;
        }

        FattyAcid curr_fa = fattyAcylStack.peekLast();

        if (tmp.containsKey("furan")) {
            curr_fa.setNumCarbon(curr_fa.getNumCarbon() - l);
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
            for (Entry<String, ArrayList<FunctionalGroup>> kv : curr_fa.getFunctionalGroupsInternal().entrySet()) {
                ArrayList<Integer> remove_item = new ArrayList<>();
                int i = 0;
                for (FunctionalGroup func_group : kv.getValue()) {
                    if (func_group.getPosition() <= l) {
                        remove_item.add(i);
                        if (!fa.getFunctionalGroupsInternal().containsKey(kv.getKey())) {
                            fa.getFunctionalGroupsInternal().put(kv.getKey(), new ArrayList<>());
                        }
                        func_group.setPosition(l + 1 - func_group.getPosition());
                        fa.getFunctionalGroupsInternal().get(kv.getKey()).add(func_group);
                    }
                }
                for (int ii = remove_item.size() - 1; ii >= 0; --ii) {
                    curr_fa.getFunctionalGroupsInternal().get(kv.getKey()).remove((int) remove_item.get(ii));
                }
            }
            Map<String, ArrayList<FunctionalGroup>> func_dict = curr_fa.getFunctionalGroupsInternal();
            curr_fa.setFunctionalGroups(new HashMap<>());
            for (Entry<String, ArrayList<FunctionalGroup>> kv : func_dict.entrySet()) {
                if (kv.getValue().size() > 0) {
                    curr_fa.getFunctionalGroupsInternal().put(kv.getKey(), kv.getValue());
                }
            }

            // shift double bonds
            if (curr_fa.getDoubleBonds().getDoubleBondPositions().size() > 0) {
                fa.setDoubleBonds(new DoubleBonds());
                for (Entry<Integer, String> kv : curr_fa.getDoubleBonds().getDoubleBondPositions().entrySet()) {
                    if (kv.getKey() <= l) {
                        fa.getDoubleBonds().getDoubleBondPositions().put(l + 1 - kv.getKey(), kv.getValue());
                    }
                }
                fa.getDoubleBonds().setNumDoubleBonds(fa.getDoubleBonds().getDoubleBondPositions().size());
                for (Entry<Integer, String> kv : fa.getDoubleBonds().getDoubleBondPositions().entrySet()) {
                    curr_fa.getDoubleBonds().getDoubleBondPositions().remove(kv.getKey());
                }
            }
            fname = "cc";
            fg = new CarbonChain(fa, knownFunctionalGroups);
        }
        curr_fa.setNumCarbon(curr_fa.getNumCarbon() - l);
        fg.setPosition(l);
        curr_fa.shiftPositions(-l);
        if (!curr_fa.getFunctionalGroupsInternal().containsKey(fname)) {
            curr_fa.getFunctionalGroupsInternal().put(fname, new ArrayList<>());
        }
        curr_fa.getFunctionalGroupsInternal().get(fname).add(fg);
    }

    private void set_dial(TreeNode node) {
        FattyAcid curr_fa = fattyAcylStack.peekLast();
        int pos = curr_fa.getNumCarbon();
        FunctionalGroup fg = knownFunctionalGroups.get("oxo");
        fg.setPosition(pos);
        if (!curr_fa.getFunctionalGroupsInternal().containsKey("oxo")) {
            curr_fa.getFunctionalGroupsInternal().put("oxo", new ArrayList<>());
        }
        curr_fa.getFunctionalGroupsInternal().get("oxo").add(fg);
    }

    private void open_db_length(TreeNode node) {
        tmp.put("add_lengths", 1);
    }

    private void close_db_length(TreeNode node) {
        tmp.put("add_lengths", 0);
    }

    private void set_dioic(TreeNode node) {
        headgroup = "FA";
        int pos = (((GenericList) tmp.get("fg_pos")).size() == 2) ? (int) ((GenericList) ((GenericList) tmp.get("fg_pos")).get(1)).get(0) : fattyAcylStack.peekLast().getNumCarbon();
        if (tmp.containsKey("reduction")) {
            pos -= ((GenericList) tmp.get("reduction")).size();
        }
        fattyAcylStack.peekLast().setNumCarbon(fattyAcylStack.peekLast().getNumCarbon() - 1);
        FunctionalGroup func_group = knownFunctionalGroups.get("COOH");
        func_group.setPosition(pos - 1);
        if (!fattyAcylStack.peekLast().getFunctionalGroupsInternal().containsKey("COOH")) {
            fattyAcylStack.peekLast().getFunctionalGroupsInternal().put("COOH", new ArrayList<>());
        }
        fattyAcylStack.peekLast().getFunctionalGroupsInternal().get("COOH").add(func_group);
    }

    private void setup_hydroxyl(TreeNode node) {
        tmp.put("hydroxyl_pos", new GenericList());
    }

    private void add_hydroxyls(TreeNode node) {
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
                if (!fattyAcylStack.peekLast().getFunctionalGroupsInternal().containsKey("OH")) {
                    fattyAcylStack.peekLast().getFunctionalGroupsInternal().put("OH", new ArrayList<>());
                }
                fattyAcylStack.peekLast().getFunctionalGroupsInternal().get("OH").add(fg_insert);
            }
        }
    }

    private void set_ate(TreeNode node) {
        fattyAcylStack.peekLast().setNumCarbon(fattyAcylStack.peekLast().getNumCarbon() + ATE.get(node.getText()));
        headgroup = "WE";
    }

    private void add_hydroxyl(TreeNode node) {
        int h = Integer.valueOf(node.getText());
        ((GenericList) tmp.get("hydroxyl_pos")).add(h);
    }

    private void set_functional_group(TreeNode node) {
        tmp.put("fg_pos", new GenericList());
        tmp.put("fg_type", "");
    }

    private void add_functional_group(TreeNode node) {
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
        if (!fa.getFunctionalGroupsInternal().containsKey(t)) {
            fa.getFunctionalGroupsInternal().put(t, new ArrayList<>());
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
            fa.getFunctionalGroupsInternal().get(t).add(fg_insert);
        }
    }

    private void set_double_bond_information(TreeNode node) {
        ((Dictionary) tmp.get(FA_I())).put("db_position", 0);
        ((Dictionary) tmp.get(FA_I())).put("db_cistrans", "");
    }

    private void add_double_bond_information(TreeNode node) {
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
        if (!fattyAcylStack.peekLast().getDoubleBonds().getDoubleBondPositions().containsKey(pos) || fattyAcylStack.peekLast().getDoubleBonds().getDoubleBondPositions().get(pos).length() == 0) {
            fattyAcylStack.peekLast().getDoubleBonds().getDoubleBondPositions().put(pos, cistrans);
            fattyAcylStack.peekLast().getDoubleBonds().setNumDoubleBonds(fattyAcylStack.peekLast().getDoubleBonds().getDoubleBondPositions().size());
        }
    }

    private void set_cistrans(TreeNode node) {
        ((Dictionary) tmp.get(FA_I())).put("db_cistrans", node.getText());
    }

    private void set_double_bond_position(TreeNode node) {
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

    private void add_summary(TreeNode node) {
        String fa_i = FA_I();
        ((Dictionary) tmp.get(fa_i)).put("fg_pos_summary", new Dictionary());
        for (Object o : (GenericList) tmp.get("fg_pos")) {
            GenericList lst = (GenericList) o;
            String k = Integer.toString((int) lst.get(0));
            String v = ((String) lst.get(1)).toUpperCase();
            ((Dictionary) ((Dictionary) tmp.get(fa_i)).get("fg_pos_summary")).put(k, v);
        }
    }

    private void set_functional_length(TreeNode node) {
        if ((int) tmp.get("length") != ((GenericList) tmp.get("fg_pos")).size()) {
            throw new LipidException("Length of functional group '" + Integer.toString((int) tmp.get("length")) + "' does not match with number of its positions '" + Integer.toString(((GenericList) tmp.get("fg_pos")).size()) + "'");
        }
    }

    private void set_functional_type(TreeNode node) {
        tmp.put("fg_type", node.getText());
    }

    private void add_epoxy(TreeNode node) {
        GenericList gl = (GenericList) tmp.get("fg_pos");
        while (gl.size() > 1) {
            gl.remove(gl.size() - 1);
        }
        tmp.put("fg_type", "Epoxy");
    }

    private void set_functional_position(TreeNode node) {
        GenericList gl = new GenericList();
        gl.add(0);
        gl.add("");
        ((GenericList) tmp.get("fg_pos")).add(gl);
    }

    private void set_functional_pos(TreeNode node) {
        GenericList gl = (GenericList) tmp.get("fg_pos");
        ((GenericList) gl.get(gl.size() - 1)).set(0, Integer.valueOf(node.getText()));
    }

    private void add_func_stereo(TreeNode node) {
        int l = ((GenericList) tmp.get("fg_pos")).size();
        ((GenericList) ((GenericList) tmp.get("fg_pos")).get(l - 1)).set(1, node.getText());
    }

    private void reduction(TreeNode node) {
        int shift_len = -((GenericList) tmp.get("fg_pos")).size();
        fattyAcylStack.peekLast().setNumCarbon(fattyAcylStack.peekLast().getNumCarbon() + shift_len);
        for (Entry<String, ArrayList<FunctionalGroup>> kv : fattyAcylStack.peekLast().getFunctionalGroupsInternal().entrySet()) {
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

    private void homo(TreeNode node) {
        tmp.put("post_adding", new GenericList());
        for (Object o : (GenericList) tmp.get("fg_pos")) {
            GenericList lst = (GenericList) o;
            ((GenericList) tmp.get("post_adding")).add((int) lst.get(0));
        }
    }

    private void set_cycle(TreeNode node) {
        tmp.put("cyclo", 1);
    }

    private void rearrange_cycle(TreeNode node) {
        if (tmp.containsKey("post_adding")) {
            fattyAcylStack.peekLast().setNumCarbon(fattyAcylStack.peekLast().getNumCarbon() + ((GenericList) tmp.get("post_adding")).size());
            tmp.remove("post_adding");
        }

        FattyAcid curr_fa = fattyAcylStack.peekLast();
        int start = (int) ((GenericList) ((GenericList) tmp.get("fg_pos")).get(0)).get(0);
        if (curr_fa.getFunctionalGroupsInternal().containsKey("cy")) {
            for (FunctionalGroup cy : curr_fa.getFunctionalGroupsInternal().get("cy")) {
                int shift_val = start - cy.getPosition();
                if (shift_val == 0) {
                    continue;
                }
                ((Cycle) cy).rearrangeFunctionalGroups(curr_fa, shift_val);
            }
        }
    }

    private void set_recursion(TreeNode node) {
        tmp.put("fg_pos", new GenericList());
        tmp.put("fg_type", "");
        fattyAcylStack.add(new FattyAcid("FA", knownFunctionalGroups));
        tmp.put(FA_I(), new Dictionary());
        ((Dictionary) tmp.get(FA_I())).put("recursion_pos", 0);
    }

    private void add_recursion(TreeNode node) {
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
        if (!curr_fa.getFunctionalGroupsInternal().containsKey(fname)) {
            curr_fa.getFunctionalGroupsInternal().put(fname, new ArrayList<>());
        }
        curr_fa.getFunctionalGroupsInternal().get(fname).add(fa);
        tmp.put("added_func_group", 1);
    }

    private void set_recursion_pos(TreeNode node) {
        ((Dictionary) tmp.get(FA_I())).put("recursion_pos", Integer.valueOf(node.getText()));
    }

}
