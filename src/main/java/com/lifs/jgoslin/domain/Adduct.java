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
public class Adduct
{
    public String sum_formula;
    public String adduct_string;
    public int charge;
    public int charge_sign;

    public Adduct(String _sum_formula, String _adduct_string, int _charge, int _sign){
        sum_formula = _sum_formula;
        adduct_string = _adduct_string;
        charge = _charge;
        set_charge_sign(_sign);

    }


    public void set_charge_sign(int _sign){
        if (-1 <= _sign && _sign <= 1){
            charge_sign = _sign;
        }

        else {
            throw new IllegalArgumentException("Sign can only be -1, 0, or 1");
        }
    }

    public String get_lipid_string(){
        if (charge == 0)
        {
            return "[M]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[M").append(sum_formula).append(adduct_string).append("]").append(charge).append(((charge_sign > 0) ? "+" : "-"));

        return sb.toString();
    }

    public ElementTable get_elements()
    {
        ElementTable elements = new ElementTable();
        try{

            String adduct_name = adduct_string.substring(1);
            /*
            ElementTable adduct_elements = SumFormulaParser.get_instance().parse(adduct_name);
            foreach (KeyValuePair<Element, int> kvp in adduct_elements)
            {
                elements[kvp.Key] += kvp.Value;
            }
            */

        }
        catch (Exception e)
        {
            return elements;
        }

        if (adduct_string.length() > 0 && adduct_string.charAt(0) == '-')
        {
            for (Element e : Elements.element_order)
            {
                elements.replace(e, elements.get(e) * -1);
            }
        }

        return elements;
    }



    public int get_charge()
    {
        return charge * charge_sign;
    }
}
