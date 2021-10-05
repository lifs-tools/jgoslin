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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author dominik
 */
    
public class Elements
{
    public static double ELECTRON_REST_MASS = 0.00054857990946;
    public enum Element {C, C13, H, H2, N, N15, O, O17, O18, P, P32, S, S34, S33, F, Cl, Br, I, As};

    public static HashMap<String, Element> element_positions = new HashMap<String, Element>(){{
        put("C", Element.C);
        put("H", Element.H);
        put("N", Element.N);
        put("O", Element.O);
        put("P", Element.P);
        put("P'", Element.P32);
        put("S", Element.S);
        put("F", Element.F);
        put("Cl", Element.Cl);
        put("Br", Element.Br);
        put("I", Element.I);
        put("As", Element.As);
        put("S'", Element.S34);
        put("S''", Element.S33);
        put("H'", Element.H2);
        put("C'", Element.C13);
        put("N'", Element.N15);
        put("O'", Element.O17);
        put("O''", Element.O18);
        put("2H", Element.H2);
        put("13C", Element.C13);
        put("15N", Element.N15);
        put("17O", Element.O17);
        put("18O", Element.O18);
        put("32P", Element.P32);
        put("34S", Element.S34);
        put("33S", Element.S33);
        put("H2", Element.H2);
        put("C13", Element.C13);
        put("N15", Element.N15);
        put("O17", Element.O17);
        put("O18", Element.O18);
        put("P32", Element.P32);
        put("S34", Element.S34);
        put("S33", Element.S33);
    }};


    public static HashMap<Element, Double> element_masses = new HashMap<Element, Double>(){{
        put(Element.C, 12.0);
        put(Element.H, 1.007825035);
        put(Element.N, 14.0030740);
        put(Element.O, 15.99491463);
        put(Element.P, 30.973762);
        put(Element.S, 31.9720707);
        put(Element.H2, 2.014101779);
        put(Element.C13, 13.0033548378);
        put(Element.N15, 15.0001088984);
        put(Element.O17, 16.9991315);
        put(Element.O18, 17.9991604);
        put(Element.P32, 31.973907274);
        put(Element.S33, 32.97145876);
        put(Element.S34, 33.96786690);
        put(Element.F, 18.9984031);
        put(Element.Cl, 34.968853);
        put(Element.Br, 78.918327);
        put(Element.I, 126.904473);
        put(Element.As, 74.921595);
    }};



    public static  HashMap<Element, String> element_shortcut = new HashMap<Element, String>(){{
        put(Element.C, "C");
        put(Element.H, "H");
        put(Element.N, "N");
        put(Element.O, "O");
        put(Element.P, "P");
        put(Element.S, "S");
        put(Element.F, "F");
        put(Element.Cl, "Cl");
        put(Element.Br, "Br");
        put(Element.I, "I");
        put(Element.As, "As");
        put(Element.H2, "H'");
        put(Element.C13, "C'");
        put(Element.N15, "N'");
        put(Element.O17, "O'");
        put(Element.O18, "O''");
        put(Element.P32, "P'");
        put(Element.S33, "S'");
        put(Element.S34, "S''");
    }};

    public static ArrayList<Element> element_order = new ArrayList<Element>(List.of(Element.C, Element.H, Element.As, Element.Br, Element.Cl, Element.F, Element.I, Element.N, Element.O, Element.P, Element.S, Element.H2, Element.C13, Element.N15, Element.O17, Element.O18, Element.P32, Element.S33, Element.S34));
}
