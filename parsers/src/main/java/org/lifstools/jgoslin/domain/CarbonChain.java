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
public final class CarbonChain extends FunctionalGroup {

    public CarbonChain(FattyAcid _fa, KnownFunctionalGroups knownFunctionalGroups) {
        this(_fa, -1, 1, knownFunctionalGroups);
    }

    public CarbonChain(FattyAcid _fa, int _position, KnownFunctionalGroups knownFunctionalGroups) {
        this(_fa, _position, 1, knownFunctionalGroups);
    }

    public CarbonChain(FattyAcid _fa, int _position, int _count, KnownFunctionalGroups knownFunctionalGroups) {
        super("cc", _position, _count, knownFunctionalGroups);
        if (_fa != null) {
            functionalGroups.put("cc", new ArrayList<>());
            functionalGroups.get("cc").add(_fa);
        }

        elements.put(Element.H, 1);
        elements.put(Element.O, -1);
    }

    @Override
    public FunctionalGroup copy() {
        return new CarbonChain((FattyAcid) functionalGroups.get("cc").get(0).copy(), getPosition(), getCount(), knownFunctionalGroups);
    }

    @Override
    public String toString(LipidLevel level) {
        return (LipidLevel.isLevel(level, LipidLevel.COMPLETE_STRUCTURE.level | LipidLevel.FULL_STRUCTURE.level) ? Integer.toString(getPosition()) : "") + "(" + ((FattyAcid) functionalGroups.get("cc").get(0)).toString(level) + ")";
    }
    
}
