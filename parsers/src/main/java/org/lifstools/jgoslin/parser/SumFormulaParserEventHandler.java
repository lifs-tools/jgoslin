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

package org.lifstools.jgoslin.parser;

import org.lifstools.jgoslin.domain.LipidException;
import org.lifstools.jgoslin.domain.Element;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.Elements;
import org.lifstools.jgoslin.domain.ElementTable;

/**
 *
 * @author dominik
 */
public class SumFormulaParserEventHandler extends BaseParserEventHandler<ElementTable> {
    public Element element;
    public int count;
    
    public SumFormulaParserEventHandler(){
        try {
            registered_events.put("molecule_pre_event", SumFormulaParserEventHandler.class.getDeclaredMethod("reset_parser", TreeNode.class));
            registered_events.put("element_group_post_event", SumFormulaParserEventHandler.class.getDeclaredMethod("element_group_post_event", TreeNode.class));
            registered_events.put("element_pre_event", SumFormulaParserEventHandler.class.getDeclaredMethod("element_pre_event", TreeNode.class));
            registered_events.put("single_element_pre_event", SumFormulaParserEventHandler.class.getDeclaredMethod("single_element_group_pre_event", TreeNode.class));
            registered_events.put("count_pre_event", SumFormulaParserEventHandler.class.getDeclaredMethod("count_pre_event", TreeNode.class));
        }
        catch(Exception e){
            throw new LipidParsingException("Cannot initialize ShorthandParserEventHandler.");
        }
    }
     
    
    public void reset_parser(TreeNode node){
        content = new ElementTable();
        element = Element.H;
        count = 0;
    }
    
    
    public void element_group_post_event(TreeNode node){
         content.put(element, content.get(element) + count);
    }
    
    
    public void element_pre_event(TreeNode node){
        
        String parsed_element = node.get_text();

        if (Elements.element_positions.containsKey(parsed_element))
        {
            element = Elements.element_positions.get(parsed_element);
        }

        else {
            throw new LipidException("Error: element '" + parsed_element + "' is unknown");
        }
    }
    
    
    public void single_element_group_pre_event(TreeNode node){
        String parsed_element = node.get_text();
        if (Elements.element_positions.containsKey(parsed_element))
        {
            element = Elements.element_positions.get(parsed_element);
            content.put(element, content.get(element) + 1);
        }

        else {
            throw new LipidException("Error: element '" + parsed_element + "' is unknown");
        }
    }
    
    
    public void count_pre_event(TreeNode node){
        count = Integer.valueOf(node.get_text());
    }
}
