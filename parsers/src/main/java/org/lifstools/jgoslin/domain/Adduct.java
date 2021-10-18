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

import java.util.HashMap;

/**
 *
 * @author dominik
 */
public final class Adduct
{
    public String sum_formula;
    public String adduct_string;
    public int charge;
    public int charge_sign;
    
    public static final HashMap<String, ElementTable> adducts = new HashMap<>(){{
        put("+H", new ElementTable(){{put(Element.H, 1);}});
        put("+2H", new ElementTable(){{put(Element.H, 2);}});
        put("+3H", new ElementTable(){{put(Element.H, 3);}});
        put("+4H", new ElementTable(){{put(Element.H, 4);}});
        put("-H", new ElementTable(){{put(Element.H, -1);}});
        put("-2H", new ElementTable(){{put(Element.H, -2);}});
        put("-3H", new ElementTable(){{put(Element.H, -3);}});
        put("-4H", new ElementTable(){{put(Element.H, -4);}});
        
        put("+H-H2O", new ElementTable(){{put(Element.H, -1); put(Element.O, -1);}});
        put("+NH4", new ElementTable(){{put(Element.N, 1); put(Element.H, 4);}});
        put("+Cl", new ElementTable(){{put(Element.Cl, 1);}});
        put("+HCOO", new ElementTable(){{put(Element.H, 1); put(Element.C, 1); put(Element.O, 2);}});
        put("+CH3COO", new ElementTable(){{put(Element.H, 3); put(Element.C, 2); put(Element.O, 2);}});
    }};
    
    
    public static final HashMap<String, Integer> adduct_charges = new HashMap<>(){{
        put("+H", 1); put("+2H", 2); put("+3H", 3); put("+4H", 4);
        put("-H", -1); put("-2H", -2); put("-3H", -3); put("-4H", -4);
        put("+H-H2O", 1); put("+NH4", 1); put("+Cl", -1);
        put("+HCOO", -1); put("+CH3COO", -1);
    }};
    

    public Adduct(String _sum_formula, String _adduct_string){
        this(_sum_formula, _adduct_string, 1, 1);

    }

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
            throw new ConstraintViolationException("Sign can only be -1, 0, or 1");
        }
    }

    public String get_lipid_string(){
        if (charge == 0){
            return "[M]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[M").append(sum_formula).append(adduct_string).append("]").append(charge).append(((charge_sign > 0) ? "+" : "-"));

        return sb.toString();
    }

    public ElementTable get_elements(){
        ElementTable elements = new ElementTable();
        String adduct_name = adduct_string.substring(1);

        if (adducts.containsKey(adduct_string)){
            if (adduct_charges.get(adduct_string) != get_charge()){
                throw new ConstraintViolationException("Provided charge '" + get_charge() + "' in contradiction to adduct '" + adduct_string + "' charge '" + adduct_charges.get(adduct_string) + "'.");
            }    
            elements.add(adducts.get(adduct_string));
        }
        else {
            throw new ConstraintViolationException("Adduct '" + adduct_string + "' is unknown.");
        }

        return elements;
    }



    public int get_charge(){
        return charge * charge_sign;
    }
}
