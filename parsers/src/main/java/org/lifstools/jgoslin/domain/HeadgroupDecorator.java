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

import java.util.Map.Entry;

/**
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class HeadgroupDecorator extends FunctionalGroup {

    private final boolean suffix;
    private final LipidLevel lowestVisibleLevel;

    public HeadgroupDecorator(String _name, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, -1, 1, null, false, LipidLevel.NO_LEVEL, knownFunctionalGroups);
    }

    public HeadgroupDecorator(String _name, int _position, int _count, ElementTable _elements, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, _position, _count, _elements, false, LipidLevel.NO_LEVEL, knownFunctionalGroups);
    }

    public HeadgroupDecorator(String _name, int _position, int _count, ElementTable _elements, boolean _suffix, KnownFunctionalGroups knownFunctionalGroups) {
        this(_name, _position, _count, _elements, _suffix, LipidLevel.NO_LEVEL, knownFunctionalGroups);
    }

    public HeadgroupDecorator(String _name, int _position, int _count, ElementTable _elements, boolean _suffix, LipidLevel _level, KnownFunctionalGroups knownFunctionalGroups) {
        super(_name, _position, _count, null, false, "", _elements, knownFunctionalGroups);
        suffix = _suffix;
        lowestVisibleLevel = _level;
    }

    @Override
    public FunctionalGroup copy() {
        ElementTable e = new ElementTable();
        for (Entry<Element, Integer> kv : computeAndCopyElements().entrySet()) {
            e.put(kv.getKey(), e.get(kv.getKey()) + kv.getValue());
        }
        return new HeadgroupDecorator(getName(), getPosition(), getCount(), e, suffix, lowestVisibleLevel, knownFunctionalGroups);
    }

    @Override
    public String toString(LipidLevel level) {
        if (!suffix) {
            return getName() + (count > 1 ? Integer.toString(count) : "");
        }
        String decorator_string = "";
        if ((lowestVisibleLevel == LipidLevel.NO_LEVEL) || (lowestVisibleLevel.level <= level.level)) {

            if (functionalGroups.containsKey("decorator_alkyl")) {
                if (functionalGroups.get("decorator_alkyl").size() > 0) {
                    decorator_string = (level.level > LipidLevel.SPECIES.level) ? functionalGroups.get("decorator_alkyl").get(0).toString(level) : "Alk";
                } else {
                    decorator_string = "Alk";
                }
            } else if (functionalGroups.containsKey("decorator_acyl")) {
                if (functionalGroups.get("decorator_acyl").size() > 0) {
                    decorator_string = (level.level > LipidLevel.SPECIES.level) ? ("FA " + functionalGroups.get("decorator_acyl").get(0).toString(level))
                            : "FA";
                } else {
                    decorator_string = "FA";
                }
            } else {
                decorator_string = getName();
            }
            decorator_string = "(" + decorator_string + ")";
        }

        return decorator_string;
    }

    public boolean isSuffix() {
        return suffix;
    }

    public LipidLevel getLowestVisibleLevel() {
        return lowestVisibleLevel;
    }

}
