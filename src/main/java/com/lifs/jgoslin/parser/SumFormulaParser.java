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
import com.lifs.jgoslin.domain.ElementTable;
import com.lifs.jgoslin.domain.LipidAdduct;
import com.lifs.jgoslin.domain.LipidException;
import com.lifs.jgoslin.domain.LipidParsingException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 *
 * @author dominik
 */
public class SumFormulaParser extends Parser {
    public SumFormulaParser(){
        super(new SumFormulaParserEventHandler());
    }
    
    
    
    @Override
    public ElementTable parse(String s) {
        return parse(s, true);
    }
    
    
    @Override
    public ElementTable parse(String s, boolean throw_exception) {
        parser_event_handler.set_content(null);
        try {
            SumFormulaLexer lexer = new SumFormulaLexer(CharStreams.fromString(s));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            com.lifs.jgoslin.antlr.SumFormulaParser parser = new com.lifs.jgoslin.antlr.SumFormulaParser(tokens);
            ParseTree tree = parser.molecule();
            walker.walk(parser_event_handler, tree);
        }
        catch(Exception e){
            if (throw_exception) throw new LipidParsingException("Lipid '" + s + "' can not be parsed by grammar 'Shorthand2020'");
        }
        
        return (ElementTable)parser_event_handler.get_content();
    }
}
