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
