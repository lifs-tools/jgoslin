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

import java.util.Map.Entry;

/**
 *
 * @author dominik
 */
public class LipidAdduct {
    public LipidSpecies lipid;
    public Adduct adduct;

    public LipidAdduct() {
        lipid = null;
        adduct = null;
    }

    
    public String get_lipid_string(){
        return get_lipid_string(LipidLevel.NO_LEVEL);
    }
    
    
    public String get_lipid_string(LipidLevel level){
        StringBuilder sb = new StringBuilder();
        if (lipid != null) sb.append(lipid.get_lipid_string(level));
        else return "";


        switch (level){
            case CLASS:
            case CATEGORY:
                break;

            default:
                if (adduct != null) sb.append(adduct.get_lipid_string());
                break;
        }

        return sb.toString();
    }


    public String get_class_name(){
        return (lipid != null) ? lipid.headgroup.get_class_name() : "";
    }


    public boolean is_lyso(){
        return (LipidClasses.get_instance().size() > lipid.headgroup.lipid_class) ? LipidClasses.get_instance().get(lipid.headgroup.lipid_class).special_cases.contains("Lyso") : false;
    }


    public boolean is_cardio_lipin(){
        return (LipidClasses.get_instance().size() >lipid.headgroup.lipid_class) ? LipidClasses.get_instance().get(lipid.headgroup.lipid_class).special_cases.contains("Cardio") : false;
    }


    public boolean contains_sugar(){
        return (LipidClasses.get_instance().size() > lipid.headgroup.lipid_class) ? LipidClasses.get_instance().get(lipid.headgroup.lipid_class).special_cases.contains("Sugar") : false;
    }


    public boolean contains_ester(){
        return (LipidClasses.get_instance().size() > lipid.headgroup.lipid_class) ? LipidClasses.get_instance().get(lipid.headgroup.lipid_class).special_cases.contains("Ester") : false;
    }


    public boolean is_sp_exception(){
        return (LipidClasses.get_instance().size() > lipid.headgroup.lipid_class) ? LipidClasses.get_instance().get(lipid.headgroup.lipid_class).special_cases.contains("SP_Exception") : false;
    }


    public LipidLevel get_lipid_level(){
        return lipid.get_lipid_level();
    }



    public String get_extended_class() {
        return (lipid != null) ? lipid.get_extended_class() : "";
    }



    public double get_mass() {
        ElementTable elements = get_elements();
        int charge = 0;
        double mass = 0;

        if (adduct != null)
        {
            charge = adduct.get_charge();
        }

        for(Entry<Element, Integer> kvp : elements.entrySet()){
            mass += Elements.element_masses.get(kvp.getKey()) * kvp.getValue();
        }

        if (charge != 0) mass = (mass - charge * Elements.ELECTRON_REST_MASS) / Math.abs(charge);

        return mass;
    }


    public ElementTable get_elements(){
        ElementTable elements = new ElementTable();

        if (lipid != null) {
            ElementTable lipid_elements = lipid.get_elements();
            for(Entry<Element, Integer> kv : lipid_elements.entrySet()){
                elements.put(kv.getKey(), elements.get(kv.getKey()) + kv.getValue());
            }
        }

        if (adduct != null){
            ElementTable adduct_elements = adduct.get_elements();
            for(Entry<Element, Integer> kv : adduct_elements.entrySet()){
                elements.put(kv.getKey(), elements.get(kv.getKey()) + kv.getValue());
            }
        }
        return elements;
    }
    
    
    @Override
    public String toString(){
        return get_lipid_string();
    }

    
    public String get_sum_formula(){
        return StringFunctions.compute_sum_formula(get_elements());
    }
}