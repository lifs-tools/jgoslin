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
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

/**
 * Utility class to look up element names, masses, shortcut names, and ordering.
 *
 * @author Dominik Kopczynski
 * @author Nils Hoffmann
 */
public final class Elements {

    public static final double ELECTRON_REST_MASS = 0.00054857990946;

    public static final Map<String, Element> ELEMENT_POSITIONS = Map.ofEntries(
            entry("C", Element.C),
            entry("H", Element.H),
            entry("N", Element.N),
            entry("O", Element.O),
            entry("P", Element.P),
            entry("P'", Element.P32),
            entry("S", Element.S),
            entry("F", Element.F),
            entry("Cl", Element.Cl),
            entry("Br", Element.Br),
            entry("I", Element.I),
            entry("As", Element.As),
            entry("S'", Element.S34),
            entry("S''", Element.S33),
            entry("H'", Element.H2),
            entry("C'", Element.C13),
            entry("N'", Element.N15),
            entry("O'", Element.O17),
            entry("O''", Element.O18),
            entry("2H", Element.H2),
            entry("13C", Element.C13),
            entry("15N", Element.N15),
            entry("17O", Element.O17),
            entry("18O", Element.O18),
            entry("32P", Element.P32),
            entry("34S", Element.S34),
            entry("33S", Element.S33),
            entry("H2", Element.H2),
            entry("C13", Element.C13),
            entry("N15", Element.N15),
            entry("O17", Element.O17),
            entry("O18", Element.O18),
            entry("P32", Element.P32),
            entry("S34", Element.S34),
            entry("S33", Element.S33)
    );

    public static final Map<Element, Double> ELEMENT_MASSES = Map.ofEntries(
            entry(Element.C, 12.0),
            entry(Element.H, 1.007825035),
            entry(Element.N, 14.0030740),
            entry(Element.O, 15.99491463),
            entry(Element.P, 30.973762),
            entry(Element.S, 31.9720707),
            entry(Element.H2, 2.014101779),
            entry(Element.C13, 13.0033548378),
            entry(Element.N15, 15.0001088984),
            entry(Element.O17, 16.9991315),
            entry(Element.O18, 17.9991604),
            entry(Element.P32, 31.973907274),
            entry(Element.S33, 32.97145876),
            entry(Element.S34, 33.96786690),
            entry(Element.F, 18.9984031),
            entry(Element.Cl, 34.968853),
            entry(Element.Br, 78.918327),
            entry(Element.I, 126.904473),
            entry(Element.As, 74.921595)
    );

    public static final Map<Element, String> ELEMENT_SHORTCUT = Map.ofEntries(
            entry(Element.C, "C"),
            entry(Element.H, "H"),
            entry(Element.N, "N"),
            entry(Element.O, "O"),
            entry(Element.P, "P"),
            entry(Element.S, "S"),
            entry(Element.F, "F"),
            entry(Element.Cl, "Cl"),
            entry(Element.Br, "Br"),
            entry(Element.I, "I"),
            entry(Element.As, "As"),
            entry(Element.H2, "H'"),
            entry(Element.C13, "C'"),
            entry(Element.N15, "N'"),
            entry(Element.O17, "O'"),
            entry(Element.O18, "O''"),
            entry(Element.P32, "P'"),
            entry(Element.S33, "S'"),
            entry(Element.S34, "S''")
    );

    public static final Map<Element, String> HEAVY_SHORTCUT = Map.ofEntries(
            entry(Element.C, "C"),
            entry(Element.H, "H"),
            entry(Element.N, "N"),
            entry(Element.O, "O"),
            entry(Element.P, "P"),
            entry(Element.S, "S"),
            entry(Element.F, "F"),
            entry(Element.Cl, "Cl"),
            entry(Element.Br, "Br"),
            entry(Element.I, "I"),
            entry(Element.As, "As"),
            entry(Element.H2, "H2"),
            entry(Element.C13, "C13"),
            entry(Element.N15, "N15"),
            entry(Element.O17, "O17"),
            entry(Element.O18, "O18"),
            entry(Element.P32, "P32"),
            entry(Element.S33, "S33"),
            entry(Element.S34, "S34")
    );

    public static final Map<Element, Element> HEAVY_TO_REGULAR = Map.ofEntries(
            entry(Element.H2, Element.H),
            entry(Element.C13, Element.C),
            entry(Element.N15, Element.N),
            entry(Element.O17, Element.O),
            entry(Element.O18, Element.O),
            entry(Element.P32, Element.P),
            entry(Element.S33, Element.S),
            entry(Element.S34, Element.S)
    );

    public static final ArrayList<Element> ELEMENT_ORDER = new ArrayList<Element>(List.of(Element.C, Element.H, Element.As, Element.Br, Element.Cl, Element.F, Element.I, Element.N, Element.O, Element.P, Element.S, Element.H2, Element.C13, Element.N15, Element.O17, Element.O18, Element.P32, Element.S33, Element.S34));
}
