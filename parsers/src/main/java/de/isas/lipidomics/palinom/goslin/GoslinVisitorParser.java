/*
 * 
 */
package de.isas.lipidomics.palinom.goslin;

import de.isas.lipidomics.palinom.VisitorParser;
import de.isas.lipidomics.palinom.exceptions.ParsingException;
import de.isas.lipidomics.domain.LipidAdduct;
import de.isas.lipidomics.palinom.GoslinLexer;
import de.isas.lipidomics.palinom.GoslinParser;
import de.isas.lipidomics.palinom.SyntaxErrorListener;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * Parser implementation for the Goslin grammar.
 * @author nils.hoffmann
 */
@Slf4j
public class GoslinVisitorParser implements VisitorParser<LipidAdduct> {

    @Override
    public LipidAdduct parse(String lipidString, SyntaxErrorListener listener) throws ParsingException {
        return parseWithModernGrammar(lipidString, listener);
    }

    private LipidAdduct parseWithModernGrammar(String lipidString, SyntaxErrorListener listener) throws ParsingException, RecognitionException {
        CharStream charStream = CharStreams.fromString(lipidString);
        GoslinLexer lexer = new GoslinLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        log.info("Parsing lipid identifier: {}", lipidString);
        GoslinParser parser = new GoslinParser(tokens);
        prepare(parser, lexer, listener);
        try {
            GoslinParser.LipidContext context = parser.lipid();
            if (parser.getNumberOfSyntaxErrors() > 0) {
                throw new ParsingException("Parsing of " + lipidString + " failed with " + parser.getNumberOfSyntaxErrors() + " syntax errors!\n" + listener.getErrorString());
            }
            GoslinVisitorImpl lipidVisitor = new GoslinVisitorImpl();
            return lipidVisitor.visit(context);
        } catch (ParseCancellationException pce) {
            throw new ParsingException("Parsing of " + lipidString + " failed with " + parser.getNumberOfSyntaxErrors() + " syntax errors!\n" + listener.getErrorString());
        }
    }

}
