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

import com.lifs.jgoslin.parser.SumFormulaParser;
import java.util.Map.Entry;

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
            
            ElementTable adduct_elements = (SumFormulaParser.get_instance()).parse(adduct_name);
            for (Entry<Element, Integer> kv : adduct_elements.entrySet()){
                elements.put(kv.getKey(), elements.get(kv.getKey())  + kv.getValue());
            }
            

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
