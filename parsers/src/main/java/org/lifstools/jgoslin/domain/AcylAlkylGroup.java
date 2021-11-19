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
package org.lifstools.jgoslin.domain;

import java.util.ArrayList;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
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
