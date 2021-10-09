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
package com.lifs.jgoslin.parser;

import com.lifs.jgoslin.domain.LipidAdduct;
import com.lifs.jgoslin.domain.LipidException;
import java.util.ArrayList;

/**
 *
 * @author dominik
 */
public class LipidParser{
    public ArrayList< Parser<LipidAdduct> > parser_list = new ArrayList<>();
    public Parser<LipidAdduct> lastSuccessfulParser = null;

    public LipidParser(){
        parser_list.add(new ShorthandParser());
        /*
        parser_list.Add(new GoslinParser());
        parser_list.Add(new FattyAcidParser());
        parser_list.Add(new LipidMapsParser());
        parser_list.Add(new SwissLipidsParser());
        parser_list.Add(new HmdbParser());
        */

        lastSuccessfulParser = null;
    }


    public LipidAdduct parse(String lipid_name)
    {
        lastSuccessfulParser = null;

        for (Parser<LipidAdduct> parser : parser_list){
            LipidAdduct lipid = parser.parse(lipid_name, false);
            if (lipid != null){
                lastSuccessfulParser = parser;
                return lipid;
            }
        }
        throw new LipidException("Lipid not found");
    }
}
