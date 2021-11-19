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

/**
 * Defines the taxonomy of lipid levels, including UNDEFINED for lipids where
 * the level can not be inferred or does not apply. The levels are based on
 * <pre>Gerhard Liebisch et al. Update on LIPID MAPS classification, nomenclature, and shorthand notation for MS-derived lipid structures. J. Lipid Res. Volume 61, ISSUE 12, P1539-1555, December 01, 2020.</pre>
 * 
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public enum LipidLevel {
    NO_LEVEL(1),
    UNDEFINED_LEVEL(2),
    CATEGORY(4), // Mediators, Glycerolipids, Glycerophospholipids, Sphingolipids, Steroids, Prenols
    CLASS(8), // Glyerophospholipids -> Glycerophosphoinositols PI
    SPECIES(16), // Phosphatidylinositol (16:0) or PI 16:0;O
    MOLECULAR_SPECIES(32), // Phosphatidylinositol 8:0-8:0;O or PI 8:0-8:0;O
    SN_POSITION(64), // Phosphatidylinositol 8:0/8:0;O or PI 8:0/8:0;O
    STRUCTURE_DEFINED(128), // Phosphatidylinositol 8:0/8:0;OH or PI 8:0/8:0;OH
    FULL_STRUCTURE(256), // e.g. PI 18:0/22:6(4Z,7Z,10Z,13Z,16Z,19Z);5OH
    COMPLETE_STRUCTURE(512); // e.g. PI 18:0/22:6(4Z,7Z,10Z,13Z,16Z,19Z);5OH[R]

    public int level;

    LipidLevel(int l) {
        level = l;
    }

    public static boolean isLevel(LipidLevel l, int pattern) {
        return (l.level & pattern) != 0;
    }
}
