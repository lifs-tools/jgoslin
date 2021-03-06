/*
 * 
 */
package de.isas.lipidomics.palinom.lipidmaps;

import de.isas.lipidomics.palinom.exceptions.ParsingException;
import de.isas.lipidomics.domain.LipidAdduct;
import de.isas.lipidomics.palinom.LipidMapsLexer;
import de.isas.lipidomics.palinom.LipidMapsParser;
import de.isas.lipidomics.palinom.SyntaxErrorListener;
import de.isas.lipidomics.palinom.VisitorParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * Parser implementation for the LipidMaps grammar.
 *
 * @author nils.hoffmann
 */
@Slf4j
public class LipidMapsVisitorParser implements VisitorParser<LipidAdduct> {

    @Override
    public LipidAdduct parse(String lipidString, SyntaxErrorListener listener) throws ParsingException {
        return parseWithLipidMapsGrammar(lipidString, listener);
    }

    private LipidAdduct parseWithLipidMapsGrammar(String lipidString, SyntaxErrorListener listener) throws ParsingException, RecognitionException {
        CharStream charStream = CharStreams.fromString(lipidString);
        LipidMapsLexer lexer = new LipidMapsLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        log.info("Parsing lipid maps identifier: {}", lipidString);
        LipidMapsParser parser = new LipidMapsParser(tokens);
        prepare(parser, lexer, listener);
        try {
            LipidMapsParser.LipidContext context = parser.lipid();
            if (parser.getNumberOfSyntaxErrors() > 0) {
                throw new ParsingException("Parsing of " + lipidString + " failed with " + parser.getNumberOfSyntaxErrors() + " syntax errors!\n" + listener.getErrorString());
            }
            LipidMapsVisitorImpl lipidVisitor = new LipidMapsVisitorImpl();
            return lipidVisitor.visit(context);
        } catch (ParseCancellationException pce) {
            throw new ParsingException("Parsing of " + lipidString + " failed with " + parser.getNumberOfSyntaxErrors() + " syntax errors!\n" + listener.getErrorString());
        }
    }

}
