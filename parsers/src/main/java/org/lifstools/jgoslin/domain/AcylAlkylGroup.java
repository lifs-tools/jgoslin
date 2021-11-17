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
public final class AcylAlkylGroup extends FunctionalGroup {

    private boolean alkyl;
    private boolean nitrogenBond;

    public AcylAlkylGroup(FattyAcid _fa, KnownFunctionalGroups knownFunctionalGroups) {
        this(_fa, -1, 1, false, false, knownFunctionalGroups);
    }

    public AcylAlkylGroup(FattyAcid _fa, int _position, int _count, boolean _alkyl, KnownFunctionalGroups knownFunctionalGroups) {
        this(_fa, _position, _count, _alkyl, false, knownFunctionalGroups);
    }

    public AcylAlkylGroup(FattyAcid _fa, int _position, int _count, boolean _alkyl, boolean _N_bond, KnownFunctionalGroups knownFunctionalGroups) {
        super("O", _position, _count, knownFunctionalGroups);
        alkyl = _alkyl;
        if (_fa != null) {
            String key = (alkyl ? "alkyl" : "acyl");
            functionalGroups.put(key, new ArrayList<>());
            functionalGroups.get(key).add(_fa);
        }
        doubleBonds.setNumDoubleBonds(alkyl ? 0 : 1);
        setNitrogenBond(_N_bond);

    }

    @Override
    public FunctionalGroup copy() {
        String key = alkyl ? "alkyl" : "acyl";
        return new AcylAlkylGroup((FattyAcid) functionalGroups.get(key).get(0).copy(), getPosition(), getCount(), alkyl, nitrogenBond, knownFunctionalGroups);
    }

    public void setAlkyl(boolean alkyl) {
        this.alkyl = alkyl;
    }

    public boolean getAlkyl() {
        return this.alkyl;
    }

    public void setNitrogenBond(boolean _N_bond) {
        nitrogenBond = _N_bond;

        if (nitrogenBond) {
            elements.put(Element.H, (alkyl ? 2 : 0));
            elements.put(Element.O, (alkyl ? -1 : 0));
            elements.put(Element.N, 1);
        } else {
            elements.put(Element.H, (alkyl ? 1 : -1));
            elements.put(Element.O, (alkyl ? 0 : 1));
        }
    }

    public boolean getNitrogenBond() {
        return this.nitrogenBond;
    }

    @Override
    public String toString(LipidLevel level) {
        StringBuilder acyl_alkyl_string = new StringBuilder();
        if (level == LipidLevel.FULL_STRUCTURE) {
            acyl_alkyl_string.append(getPosition());
        }
        acyl_alkyl_string.append(nitrogenBond ? "N" : "O").append("(");
        if (!alkyl) {
            acyl_alkyl_string.append("FA ");
        }
        String key = alkyl ? "alkyl" : "acyl";
        acyl_alkyl_string.append(((FattyAcid) functionalGroups.get(key).get(0)).toString(level)).append(")");

        return acyl_alkyl_string.toString();
    }

}
