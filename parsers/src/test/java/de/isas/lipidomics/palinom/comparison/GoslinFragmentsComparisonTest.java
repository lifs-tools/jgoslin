/*
 * Copyright 2019 nils.hoffmann.
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
package de.isas.lipidomics.palinom.comparison;

import de.isas.lipidomics.domain.LipidAdduct;
import de.isas.lipidomics.palinom.GoslinFragmentsLexer;
import de.isas.lipidomics.palinom.GoslinFragmentsParser;
import de.isas.lipidomics.palinom.SyntaxErrorListener;
import de.isas.lipidomics.palinom.exceptions.ParsingException;
import de.isas.lipidomics.palinom.goslinfragments.GoslinFragmentsVisitorParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 *
 * @author nils.hoffmann
 */
@Slf4j
public class GoslinFragmentsComparisonTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/de/isas/lipidomics/palinom/testfiles/goslin-short.csv", numLinesToSkip = 0, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void isValidLipidNameForGoslinShort(String lipidName) throws ParsingException {
        CharStream charStream = CharStreams.fromString(lipidName);
        GoslinFragmentsLexer lexer = new GoslinFragmentsLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        log.info("Parsing Goslin fragments identifier: {}", lipidName);
        GoslinFragmentsParser parser = new GoslinFragmentsParser(tokens);
        SyntaxErrorListener listener = new SyntaxErrorListener();
        parser.addErrorListener(listener);
        parser.setBuildParseTree(true);
        GoslinFragmentsParser.LipidContext context = parser.lipid();
        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new ParsingException("Parsing of " + lipidName + " failed with " + parser.getNumberOfSyntaxErrors() + " syntax errors!\n" + listener.getErrorString());
        }
        GoslinFragmentsVisitorParser visitorParser = new GoslinFragmentsVisitorParser();
        LipidAdduct la = visitorParser.parse(lipidName, listener);
        Assertions.assertNotNull(la);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/de/isas/lipidomics/palinom/testfiles/goslin-test.csv", numLinesToSkip = 0, delimiter = '\t', encoding = "UTF-8", lineSeparator = "\n")
    public void isValidLipidNameForGoslinTest(String lipidName) throws ParsingException {
        CharStream charStream = CharStreams.fromString(lipidName);
        GoslinFragmentsLexer lexer = new GoslinFragmentsLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        log.info("Parsing Goslin identifier: {}", lipidName);
        GoslinFragmentsParser parser = new GoslinFragmentsParser(tokens);
        SyntaxErrorListener listener = new SyntaxErrorListener();
        parser.addErrorListener(listener);
        parser.setBuildParseTree(true);
        GoslinFragmentsParser.LipidContext context = parser.lipid();
        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new ParsingException("Parsing of " + lipidName + " failed with " + parser.getNumberOfSyntaxErrors() + " syntax errors!\n" + listener.getErrorString());
        }
        GoslinFragmentsVisitorParser visitorParser = new GoslinFragmentsVisitorParser();
        LipidAdduct la = visitorParser.parse(lipidName, listener);
        Assertions.assertNotNull(la);
    }

}
