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

/**
 *
 * @author dominik
 */
public enum LipidLevel
{
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
    LipidLevel(int l){
        level = l;
    }


    public static boolean is_level(LipidLevel l, int pattern)
    {
        return (l.level & pattern) != 0;
    }
}
