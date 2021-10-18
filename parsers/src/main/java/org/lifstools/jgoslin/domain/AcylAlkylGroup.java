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

/**
 *
 * @author dominik
 */
public class AcylAlkylGroup extends FunctionalGroup {
    public boolean alkyl;
    public boolean N_bond;

    public AcylAlkylGroup(FattyAcid _fa){
        this(_fa, -1, 1, false, false);
    }

    public AcylAlkylGroup(FattyAcid _fa, int _position, int _count, boolean _alkyl){
        this(_fa, _position, _count, _alkyl, false);
    }

    public AcylAlkylGroup(FattyAcid _fa, int _position, int _count, boolean _alkyl, boolean _N_bond){
        super("O", _position, _count);
        alkyl = _alkyl;
        if (_fa != null){
            functional_groups.put((alkyl ? "alkyl" : "acyl"), new ArrayList<FunctionalGroup>());
            functional_groups.get(alkyl ? "alkyl" : "acyl").add(_fa);
        }
        double_bonds.num_double_bonds = alkyl ? 0 : 1;
        set_N_bond_type(_N_bond);

    }


    @Override
    public FunctionalGroup copy()
    {
        return new AcylAlkylGroup((FattyAcid)functional_groups.get(alkyl ? "alkyl" : "acyl").get(0).copy(), position, count, alkyl, N_bond);
    }


    public void set_N_bond_type(boolean _N_bond)
    {
        N_bond = _N_bond;

        if (N_bond){
            elements.put(Element.H, (alkyl ? 2 : 0));
            elements.put(Element.O, (alkyl ? -1 : 0));
            elements.put(Element.N, 1);
        }
        else {
            elements.put(Element.H, (alkyl ? 1 : -1));
            elements.put(Element.O, (alkyl ? 0 : 1));
        }
    }


    @Override
    public String to_string(LipidLevel level){
        StringBuilder acyl_alkyl_string = new StringBuilder();
        if (level == LipidLevel.FULL_STRUCTURE) acyl_alkyl_string.append(position);
        acyl_alkyl_string.append(N_bond ? "N" : "O").append("(");
        if (!alkyl) acyl_alkyl_string.append("FA ");
        acyl_alkyl_string.append(((FattyAcid)functional_groups.get(alkyl ? "alkyl" :"acyl").get(0)).to_string(level)).append(")");

        return acyl_alkyl_string.toString();
    }
}
