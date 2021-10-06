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
import com.lifs.jgoslin.domain.*;

/**
 *
 * @author dominik
 */
public class SumFormulaParserEventHandler extends SumFormulaBaseListener implements BaseParserEventHandler<ElementTable> {
    public ElementTable content;
    public Element element;
    public int count;
    
    public SumFormulaParserEventHandler(){
        set_content(null);
    }
    
    @Override
    public void set_content(ElementTable l){
        content = l;
    }
    
    @Override
    public ElementTable get_content(){
        return content;
    }
    
     
    @Override
    public void enterMolecule(com.lifs.jgoslin.antlr.SumFormulaParser.MoleculeContext ctx) {
        content = new ElementTable();
        element = Element.H;
        count = 0;
    }
    
    @Override
    public void exitElement_group(com.lifs.jgoslin.antlr.SumFormulaParser.Element_groupContext ctx){
         content.put(element, content.get(element) + count);
    }
    
    @Override
    public void enterElement(com.lifs.jgoslin.antlr.SumFormulaParser.ElementContext ctx){
        
        String parsed_element = ctx.getText();

        if (Elements.element_positions.containsKey(parsed_element))
        {
            element = Elements.element_positions.get(parsed_element);
        }

        else {
            throw new LipidException("Error: element '" + parsed_element + "' is unknown");
        }
    }
    
    @Override
    public void enterSingle_element(com.lifs.jgoslin.antlr.SumFormulaParser.Single_elementContext ctx){
        String parsed_element = ctx.getText();
        if (Elements.element_positions.containsKey(parsed_element))
        {
            element = Elements.element_positions.get(parsed_element);
            content.put(element, content.get(element) + 1);
        }

        else {
            throw new LipidException("Error: element '" + parsed_element + "' is unknown");
        }
    }
    
    @Override
    public void enterCount(com.lifs.jgoslin.antlr.SumFormulaParser.CountContext ctx){
        count = Integer.valueOf(ctx.getText());
    }
}
