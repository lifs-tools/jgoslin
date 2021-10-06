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

import com.lifs.jgoslin.antlr.*;
import com.lifs.jgoslin.domain.LipidAdduct;


public class ShorthandParserEventHandler extends Shorthand2020BaseListener implements BaseParserEventHandler<LipidAdduct> {
    LipidAdduct content;
    
    public ShorthandParserEventHandler(){
        set_content(null);
    }
    
    @Override
    public void set_content(LipidAdduct l){
        content = l;
    }
    
    @Override
    public LipidAdduct get_content(){
        return content;
    }
    
     
    @Override
    public void enterPl_hg_double(Shorthand2020Parser.Pl_hg_doubleContext ctx) {
        content = new LipidAdduct();
        content.lipid_class = ctx.getText();
    }
}
