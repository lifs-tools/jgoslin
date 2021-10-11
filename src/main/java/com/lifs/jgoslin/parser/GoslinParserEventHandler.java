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

import com.lifs.jgoslin.domain.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


public class GoslinParserEventHandler extends LipidBaseParserEventHandler {    
    public ExtendedList<FunctionalGroup> current_fas;
    public Dictionary tmp = new Dictionary();
    public boolean acer_species = false;
    public static final HashSet<String> special_types = new HashSet<String>(Arrays.asList("acyl", "alkyl", "decorator_acyl", "decorator_alkyl", "cc"));
        
    
    public GoslinParserEventHandler() {
        try {
            
            
        }
        catch(Exception e){
            throw new RuntimeException("Cannot initialize ShorthandParserEventHandler.");
        }
    }   
        
    public void reset_parser(TreeNode node){
        
    }    

    
    
    
    public void build_lipid(TreeNode node){
        
    }
    
}
