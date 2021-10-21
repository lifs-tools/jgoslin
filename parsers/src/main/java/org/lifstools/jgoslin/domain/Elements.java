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
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author dominik
 */
public class Elements {

    public static final double ELECTRON_REST_MASS = 0.00054857990946;

    public static final HashMap<String, Element> ELEMENT_POSITIONS = new HashMap<String, Element>() {
        {
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
        }
    };

    public static final HashMap<Element, Double> ELEMENT_MASSES = new HashMap<Element, Double>() {
        {
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
        }
    };

    public static final HashMap<Element, String> ELEMENT_SHORTCUT = new HashMap<Element, String>() {
        {
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
        }
    };

    public static final ArrayList<Element> ELEMENT_ORDER = new ArrayList<Element>(List.of(Element.C, Element.H, Element.As, Element.Br, Element.Cl, Element.F, Element.I, Element.N, Element.O, Element.P, Element.S, Element.H2, Element.C13, Element.N15, Element.O17, Element.O18, Element.P32, Element.S33, Element.S34));
}
