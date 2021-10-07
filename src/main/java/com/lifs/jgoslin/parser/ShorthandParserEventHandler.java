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
import java.util.HashMap;
import java.util.HashSet;


public class ShorthandParserEventHandler extends Shorthand2020BaseListener implements BaseParserEventHandler<LipidAdduct> {
    public LipidAdduct content;
    public LipidLevel level = LipidLevel.FULL_STRUCTURE;
    public String head_group = "";
    public FattyAcid lcb = null;
    public ArrayList<FattyAcid> fa_list = new ArrayList<>();
    public FattyAcid current_fa = null;
    public Adduct adduct = null;
    public ArrayList<HeadgroupDecorator> headgroup_decorators = new ArrayList<>();
    public boolean use_head_group = false;
    public ExtendedList<FunctionalGroup> current_fas;
    public HashMap<String, Object> tmp = new HashMap<>();
    public boolean acer_species = false;
    public static final HashSet<String> special_types = new HashSet<String>(Arrays.asList("acyl", "alkyl", "decorator_acyl", "decorator_alkyl", "cc"));
        
    public static HashSet<String> SP_EXCEPTION_CLASSES = new HashSet<>(Arrays.asList("Cer", "Ceramide", "Sphingosine", "So", "Sphinganine", "Sa", "SPH", "Sph", "LCB"));

    
    public ShorthandParserEventHandler(){
        set_content(null);
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



    public boolean sp_regular_lcb(){
        return Headgroup.get_category(head_group) == LipidCategory.SP && (current_fa.lipid_FA_bond_type == LipidFaBondType.LCB_REGULAR ||current_fa.lipid_FA_bond_type == LipidFaBondType.LCB_EXCEPTION) && !(SP_EXCEPTION_CLASSES.contains(head_group) && headgroup_decorators.isEmpty());
    }
    

    public Headgroup prepare_headgroup_and_checks(){
        Headgroup headgroup = new Headgroup(head_group, headgroup_decorators, use_head_group);
        if (use_head_group) return headgroup;

        int true_fa = 0;
        for (FattyAcid fa : fa_list) {
            true_fa += (fa.num_carbon > 0 || fa.double_bonds.get_num() > 0) ? 1 : 0;
        }
        int poss_fa = (LipidClasses.get_instance().size() > headgroup.lipid_class) ? LipidClasses.get_instance().get(headgroup.lipid_class).possible_num_fa : 0;

        // make lyso
        boolean can_be_lyso = (LipidClasses.get_instance().size() > Headgroup.get_class("L" + head_group)) ? LipidClasses.get_instance().get(Headgroup.get_class("L" + head_group)).special_cases.contains("Lyso") : false;

        if (true_fa + 1 == poss_fa && level != LipidLevel.SPECIES && headgroup.lipid_category == LipidCategory.GP && can_be_lyso){
            head_group = "L" + head_group;
            headgroup = new Headgroup(head_group, headgroup_decorators, use_head_group);
            poss_fa = (LipidClasses.get_instance().size() > headgroup.lipid_class) ? LipidClasses.get_instance().get(headgroup.lipid_class).possible_num_fa : 0;
        }

        else if (true_fa + 2 == poss_fa && level != LipidLevel.SPECIES && headgroup.lipid_category == LipidCategory.GP && head_group.equals("CL")){
            head_group = "DL" + head_group;
            headgroup = new Headgroup(head_group, headgroup_decorators, use_head_group);
            poss_fa = (LipidClasses.get_instance().size() > headgroup.lipid_class) ? LipidClasses.get_instance().get(headgroup.lipid_class).possible_num_fa : 0;
        }

        if (level == LipidLevel.SPECIES){
            if (true_fa == 0 && poss_fa != 0){
                String hg_name = headgroup.headgroup;
                throw new ConstraintViolationException("No fatty acyl information lipid class '" + hg_name + "' provided.");
            }
        }

        else if (true_fa != poss_fa && LipidLevel.is_level(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level | LipidLevel.STRUCTURE_DEFINED.level))
        {
            String hg_name = headgroup.headgroup;
            throw new ConstraintViolationException("Number of described fatty acyl chains (" + Integer.toString(true_fa) + ") not allowed for lipid class '" + hg_name + "' (having " + Integer.toString(poss_fa) + " fatty aycl chains).");
        }

        if (LipidClasses.get_instance().get(headgroup.lipid_class).special_cases.contains("HC")){
            fa_list.get(0).lipid_FA_bond_type = LipidFaBondType.AMINE;
        }


        int max_num_fa = (LipidClasses.get_instance().size() > headgroup.lipid_class) ? LipidClasses.get_instance().get(headgroup.lipid_class).max_num_fa : 0;
        if (max_num_fa != fa_list.size()) set_lipid_level(LipidLevel.MOLECULAR_SPECIES);

        if (fa_list.size() > 0 && headgroup.sp_exception) fa_list.get(0).set_type(LipidFaBondType.LCB_EXCEPTION);

        return headgroup;
    }



    public LipidSpecies assemble_lipid(Headgroup headgroup){
        LipidSpecies ls = null;
        switch (level){
            case COMPLETE_STRUCTURE: ls = new LipidCompleteStructure(headgroup, fa_list); break;
            case FULL_STRUCTURE: ls = new LipidFullStructure(headgroup, fa_list); break;
            case STRUCTURE_DEFINED: ls = new LipidStructureDefined(headgroup, fa_list); break;
            case SN_POSITION: ls = new LipidSnPosition(headgroup, fa_list); break;
            case MOLECULAR_SPECIES: ls = new LipidMolecularSpecies(headgroup, fa_list); break;
            case SPECIES: ls = new LipidSpecies(headgroup, fa_list); break;
            default: break;
        }
        return ls;
    }
    
     
    @Override
    public void enterLipid(Shorthand2020Parser.LipidContext node) {
        content = null;
        level = LipidLevel.FULL_STRUCTURE;
        adduct = null;
        head_group = "";
        fa_list = new ArrayList<FattyAcid>();
        current_fas = new ExtendedList<FunctionalGroup>();
        headgroup_decorators = new ArrayList<HeadgroupDecorator>();
        tmp = new HashMap<String, Object>();
        acer_species = false;
    }
    
    @Override
    public void exitLipid(Shorthand2020Parser.LipidContext node){
        if (acer_species) fa_list.get(0).num_carbon -= 2;
        Headgroup headgroup = prepare_headgroup_and_checks();

        // add count numbers for fatty acyl chains
        int fa_it = (fa_list.size() > 0 && (fa_list.get(0).lipid_FA_bond_type == LipidFaBondType.LCB_EXCEPTION || fa_list.get(0).lipid_FA_bond_type == LipidFaBondType.LCB_REGULAR)) ? 1 : 0;
        for (int it = fa_it; it < fa_list.size(); ++it){
            fa_list.get(it).name += Integer.toString(it + 1);
        }

        LipidAdduct lipid = new LipidAdduct();
        lipid.adduct = adduct;
        lipid.lipid = assemble_lipid(headgroup);

        if (tmp.containsKey("num_ethers")) lipid.lipid.info.num_ethers = (int)tmp.get("num_ethers");

        content = lipid;
    }
    
    
    @Override
    public void enterSl(Shorthand2020Parser.SlContext node){
        tmp.put("sl_hydroxyl", 0);
    }
    
    
    @Override
    public void exitSl(Shorthand2020Parser.SlContext node){
        if (((int)tmp.get("sl_hydroxyl")) == 0 && !head_group.equals("Cer") && !head_group.equals("SPB")){
            set_lipid_level(LipidLevel.STRUCTURE_DEFINED);
        }
    }
    
    
    @Override
    public void enterSl_hydroxyl(Shorthand2020Parser.Sl_hydroxylContext node){
        tmp.put("sl_hydroxyl", 1);
    }
    
            
    @Override
    public void enterAdduct_info(Shorthand2020Parser.Adduct_infoContext node){
        adduct = new Adduct("", "", 0, 0);
    }


    @Override
    public void enterAdduct(Shorthand2020Parser.AdductContext node){
        adduct.adduct_string = node.getText();
    }


    @Override
    public void enterCharge(Shorthand2020Parser.ChargeContext node){
        adduct.charge = Integer.valueOf(node.getText());
    }


    @Override
    public void enterCharge_sign(Shorthand2020Parser.Charge_signContext node){
        String sign = node.getText();
        if (sign.equals("+")) adduct.set_charge_sign(1);
        else adduct.set_charge_sign(-1);
    }
    
    
    @Override
    public void enterMed_species(Shorthand2020Parser.Med_speciesContext node){
        set_lipid_level(LipidLevel.SPECIES);
    }
    
    @Override
    public void enterGl_species(Shorthand2020Parser.Gl_speciesContext node){
        set_lipid_level(LipidLevel.SPECIES);
    }
    
    @Override
    public void enterGl_molecular_species(Shorthand2020Parser.Gl_molecular_speciesContext node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }
    
    @Override
    public void enterPl_species(Shorthand2020Parser.Pl_speciesContext node){
        set_lipid_level(LipidLevel.SPECIES);
    }
    
    @Override
    public void enterPl_molecular_species(Shorthand2020Parser.Pl_molecular_speciesContext node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }
    
    @Override
    public void enterSl_species(Shorthand2020Parser.Sl_speciesContext node){
        set_lipid_level(LipidLevel.SPECIES);
    }
    
    @Override
    public void enterPl_single(Shorthand2020Parser.Pl_singleContext node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }
    
    @Override
    public void enterUnsorted_fa_separator(Shorthand2020Parser.Unsorted_fa_separatorContext node){
        set_lipid_level(LipidLevel.MOLECULAR_SPECIES);
    }
    
    @Override
    public void enterEther_num(Shorthand2020Parser.Ether_numContext node){
        int num_ethers = 0;
        String ether = node.getText();
        if (ether.equals("d")) num_ethers = 2;
        else if (ether.equals("t")) num_ethers = 3;
        else if (ether.equals("e")) num_ethers = 4;
        tmp.put("num_ethers", num_ethers);
    }
    
    @Override
    public void enterMed_hg_single(Shorthand2020Parser.Med_hg_singleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterMed_hg_double(Shorthand2020Parser.Med_hg_doubleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterMed_hg_triple(Shorthand2020Parser.Med_hg_tripleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterGl_hg_single(Shorthand2020Parser.Gl_hg_singleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterGl_hg_double(Shorthand2020Parser.Gl_hg_doubleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterGl_hg_true_double(Shorthand2020Parser.Gl_hg_true_doubleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterGl_hg_triple(Shorthand2020Parser.Gl_hg_tripleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterPl_hg_single(Shorthand2020Parser.Pl_hg_singleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterPl_hg_double(Shorthand2020Parser.Pl_hg_doubleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterPl_hg_quadro(Shorthand2020Parser.Pl_hg_quadroContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterSl_hg_single(Shorthand2020Parser.Sl_hg_singleContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterPl_hg_double_fa_hg(Shorthand2020Parser.Pl_hg_double_fa_hgContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterSl_hg_double_name(Shorthand2020Parser.Sl_hg_double_nameContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterSt_hg(Shorthand2020Parser.St_hgContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterSt_hg_ester(Shorthand2020Parser.St_hg_esterContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterHg_pip_pure_m(Shorthand2020Parser.Hg_pip_pure_mContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterHg_pip_pure_d(Shorthand2020Parser.Hg_pip_pure_dContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterHg_pip_pure_t(Shorthand2020Parser.Hg_pip_pure_tContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterHg_PE_PS(Shorthand2020Parser.Hg_PE_PSContext node){
        if (head_group.length() == 0) head_group = node.getText();
    }
    
    @Override
    public void enterCarbohydrate(Shorthand2020Parser.CarbohydrateContext node){ // _pre_event", set_carbohydrate);
        String carbohydrate = node.getText();
        FunctionalGroup functional_group = null;
        try
        {
            functional_group = KnownFunctionalGroups.get_instance().get(carbohydrate);
        }
        catch (Exception e)
        {
            throw new LipidParsingException("Carbohydrate '" + carbohydrate + "' unknown");
        }

        functional_group.elements.put(Element.O, functional_group.elements.get(Element.O) - 1);
        if (tmp.containsKey("func_group_head") && ((int)tmp.get("func_group_head") == 1)){
            headgroup_decorators.add((HeadgroupDecorator)functional_group);
        }
        else {
            if (!current_fas.back().functional_groups.containsKey(carbohydrate))
            {
                current_fas.back().functional_groups.put(carbohydrate, new ArrayList<FunctionalGroup>());
            }
            current_fas.back().functional_groups.get(carbohydrate).add(functional_group);
        }
    }
    
    @Override
    public void enterCarbohydrate_structural(Shorthand2020Parser.Carbohydrate_structuralContext node){ //_pre_event", set_carbohydrate_structural);
    
    }
    
    @Override
    public void enterCarbohydrate_isomeric(Shorthand2020Parser.Carbohydrate_isomericContext node){ //_pre_event", set_carbohydrate_isomeric);
    }
}
