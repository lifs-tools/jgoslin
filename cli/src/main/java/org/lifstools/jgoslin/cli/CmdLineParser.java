/*
 * Copyright 2020  nils.hoffmann.
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
package org.lifstools.jgoslin.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.tuple.Pair;
import org.lifstools.jgoslin.domain.ConstraintViolationException;
import org.lifstools.jgoslin.domain.FattyAcid;
import org.lifstools.jgoslin.domain.FunctionalGroup;
import org.lifstools.jgoslin.domain.LipidAdduct;
import org.lifstools.jgoslin.domain.LipidClassMeta;
import org.lifstools.jgoslin.domain.LipidClasses;
import org.lifstools.jgoslin.domain.LipidLevel;
import org.lifstools.jgoslin.domain.LipidParsingException;
import org.lifstools.jgoslin.domain.LipidSpeciesInfo;
import org.lifstools.jgoslin.parser.FattyAcidParser;
import org.lifstools.jgoslin.parser.GoslinParser;
import org.lifstools.jgoslin.parser.HmdbParser;
import org.lifstools.jgoslin.parser.LipidMapsParser;
import org.lifstools.jgoslin.parser.Parser;
import org.lifstools.jgoslin.parser.ShorthandParser;
import org.lifstools.jgoslin.parser.SwissLipidsParser;

/**
 * Create a new command line parser for parsing of lipid names.
 *
 * @author nils.hoffmann
 */
@Slf4j
public class CmdLineParser {

    public static final String LIPIDMAPS_CLASS_REGEXP = ".+\\[([A-Z0-9]+)\\]";
    private static final Map<ValidationResult.Grammar, Parser<LipidAdduct>> DEFAULT_PARSERS = loadParsers();
    private static final LipidClasses LIPID_CLASSES = LipidClasses.get_instance();

    private static String getAppInfo() throws IOException {
        Properties p = new Properties();
        p.load(CmdLineParser.class.getResourceAsStream(
                "/application.properties"));
        StringBuilder sb = new StringBuilder();
        String buildDate = p.getProperty("app.build.date", "no build date");
        if (!"no build date".equals(buildDate)) {
            Instant instant = Instant.ofEpochMilli(Long.parseLong(buildDate));
            buildDate = instant.toString();
        }
        /*
         *Property keys are in src/main/resources/application.properties
         */
        sb.append("Running ").
                append(p.getProperty("app.name", "undefined app")).
                append("\n\r").
                append(" version: '").
                append(p.getProperty("app.version", "unknown version")).
                append("'").
                append("\n\r").
                append(" build-date: '").
                append(buildDate).
                append("'").
                append("\n\r").
                append(" scm-location: '").
                append(p.getProperty("scm.location", "no scm location")).
                append("'").
                append("\n\r").
                append(" commit: '").
                append(p.getProperty("scm.commit.id", "no commit id")).
                append("'").
                append("\n\r").
                append(" branch: '").
                append(p.getProperty("scm.branch", "no branch")).
                append("'").
                append("\n\r");
        return sb.toString();
    }

    /**
     * <p>
     * Runs the command line parser for jgoslin, including validation.</p>
     *
     * @param args an array of {@link java.lang.String} lipid names.
     * @throws java.lang.Exception if any unexpected errors occur.
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        String helpOpt = addHelpOption(options);
        String versionOpt = addVersionOption(options);
        String lipidNameOpt = addLipidNameInputOption(options);
        String lipidFileOpt = addLipidFileInputOption(options);
        String outputToFileOpt = addOutputToFileOption(options);
        String grammarOpt = addGrammarOption(options);

        CommandLine line = parser.parse(options, args);
        if (line.getOptions().length == 0 || line.hasOption(helpOpt)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("jgoslin-cli", options);
        } else if (line.hasOption(versionOpt)) {
            log.info(getAppInfo());
        } else {
            boolean toFile = false;
            if (line.hasOption(outputToFileOpt)) {
                toFile = true;
            }
            Stream<String> lipidNames = Stream.empty();
            if (line.hasOption(lipidNameOpt)) {
                lipidNames = Stream.of(line.getOptionValues(lipidNameOpt));
            } else if (line.hasOption(lipidFileOpt)) {
                lipidNames = Files.lines(new File(line.getOptionValue(lipidFileOpt)).toPath()).filter((t) -> {
                    return !t.isEmpty();
                });
            }
            List<Pair<String, List<ValidationResult>>> results = Collections.emptyList();
            if (line.hasOption(grammarOpt)) {
                results = parseNamesWith(lipidNames, ValidationResult.Grammar.valueOf(line.getOptionValue(grammarOpt)));
            } else {
                results = parseNames(lipidNames);
            }
            if (results.isEmpty()) {
                log.info("No results generated. Please check input file or lipid names passed on the cli!");
                System.exit(1);
            }
            if (toFile) {
                log.info("Saving output to 'goslin-out.tsv'.");
                boolean successful = writeToFile(new File("goslin-out.tsv"), results);
                if (!successful) {
                    System.exit(1);
                }
            } else {
                log.info("Echoing output to stdout.");
                boolean successful = writeToStdOut(results);
                if (!successful) {
                    System.exit(1);
                }
            }
        }
    }

    @Data
    private static class ValidationResult {

        public static enum Grammar {
            GOSLIN, GOSLIN_FRAGMENTS, LIPIDMAPS, SWISSLIPIDS, HMDB, SHORTHAND, FATTY_ACID, NONE
        };

        private String lipidName;

        private Grammar grammar;

        private LipidLevel level;

        private List<String> messages = Collections.emptyList();

        private LipidAdduct lipidAdduct;

        private LipidSpeciesInfo lipidSpeciesInfo;

        private String canonicalName;

        private String lipidMapsCategory;

        private String lipidMapsClass;

        private List<FattyAcid> fattyAcids = Collections.emptyList();

    }

    private static boolean writeToStdOut(List<Pair<String, List<ValidationResult>>> results) {

        try (StringWriter sw = new StringWriter()) {
            try (BufferedWriter bw = new BufferedWriter(sw)) {
                writeToWriter(bw, results);
            }
            sw.flush();
            sw.close();
            log.info(sw.toString());
            return true;
        } catch (IOException ex) {
            log.error("Caught exception while trying to write validation results string!", ex);
            return false;
        }
    }

    private static boolean writeToFile(File f, List<Pair<String, List<ValidationResult>>> results) {

        try (BufferedWriter bw = Files.newBufferedWriter(f.toPath())) {
            writeToWriter(bw, results);
            return true;
        } catch (IOException ex) {
            log.error("Caught exception while trying to write validation results to file " + f, ex);
            return false;
        }
    }

    private static String toTable(List<Pair<String, List<ValidationResult>>> results) {
        StringBuilder sb = new StringBuilder();
        HashSet<String> keys = new LinkedHashSet<>();
        List<ValidationResult> validationResults = results.stream().map((t) -> {
            return t.getValue();
        }).flatMap(List::stream).collect(Collectors.toList());
        List<Map<String, String>> entries = validationResults.stream().map((t) -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("Normalized Name", Optional.ofNullable(t.getCanonicalName()).orElse(""));
            m.put("Original Name", t.getLipidName());
            m.put("Grammar", t.getGrammar().name());
            m.put("Message", t.getMessages().stream().collect(Collectors.joining(" | ")));
            if (t.getLipidAdduct() != null) {
                m.put("Adduct", t.getLipidAdduct().adduct == null ? "" : t.getLipidAdduct().adduct.get_lipid_string());
                m.put("Sum Formula", t.getLipidAdduct().get_sum_formula());
                m.put("Mass", String.format(Locale.US, "%.4f", t.getLipidAdduct().get_mass()));
                m.put("Lipid Maps Category", t.getLipidAdduct().lipid.getHeadgroup().lipid_category.getFullName() + " [" + t.getLipidAdduct().lipid.getHeadgroup().lipid_category.name() + "]");
                LipidClassMeta lclass = LIPID_CLASSES.get(t.getLipidAdduct().lipid.getInfo().lipid_class);
// FIXME retrieve lipid maps main class info, functional class abbreviation and synonyms
                m.put("Lipid Maps Main Class", lclass.description);
                String lclassAbbr = getLipidMapsClassAbbreviation(lclass.description);
                m.put("Functional Class Abbr", "[" + lclassAbbr + "]");
                m.put("Functional Class Synonyms", "[" + lclass.synonyms.stream().collect(Collectors.joining(", ")) + "]");
                m.put("Level", t.level.name());
                m.put("Total #C", t.lipidSpeciesInfo.num_carbon + "");
                // FIXME add OH and other functional groups
//                t.getLipidSpeciesInfo().
//                m.put("Total #OH", t.lipidSpeciesInfo.getNHydroxy() + "");
                m.put("Total #DB", t.lipidSpeciesInfo.double_bonds.num_double_bonds + "");
                for (String functionalGroupKey : t.lipidSpeciesInfo.functional_groups.keySet()) {
                    ArrayList<FunctionalGroup> fg = t.lipidSpeciesInfo.functional_groups.get(functionalGroupKey);
                    String fgCounts = fg.stream().map((sfg) -> {
                        return "" + sfg.count;
                    }).collect(Collectors.joining("|"));
                    m.put("Total #" + functionalGroupKey, fgCounts);
                }
                for (FattyAcid fa : t.getFattyAcids()) {
                    m.put(fa.name + " SN Position", fa.position + "");
                    m.put(fa.name + " #C", fa.num_carbon + "");
                    //FIXME add OH and other functional groups
                    m.put(fa.name + " #DB", fa.double_bonds.num_double_bonds + "");
//                    for(fa.functional_groups)
//                    m.put(fa.name + " #OH", fa.getNHydroxy() + "");
                    m.put(fa.name + " Bond Type", fa.lipid_FA_bond_type.name() + "");
                    String dbPositions = fa.double_bonds.double_bond_positions.entrySet().stream().map((entry) -> {
                        return entry.getKey() + "" + entry.getValue();
                    }).collect(Collectors.joining("|"));
                    m.put(fa.name + " DB Positions", dbPositions + "");
                    for (String functionalGroupKey : fa.functional_groups.keySet()) {
                        ArrayList<FunctionalGroup> fg = fa.functional_groups.get(functionalGroupKey);
                        String fgCounts = fg.stream().map((sfg) -> {
                            return "" + sfg.count;
                        }).collect(Collectors.joining("|"));
                        m.put("Total #" + functionalGroupKey, fgCounts);
                    }
                }
            } else {
                m.put("Lipid Maps Category", "");
                m.put("Lipid Maps Main Class", "");
                m.put("Functional Class Abbr", "");
                m.put("Functional Class Synonyms", "");
                m.put("Level", "");
                m.put("Total #C", "");
//                m.put("Total #OH", "");
                m.put("Total #DB", "");
            }
            keys.addAll(m.keySet());
            return m;
        }).collect(Collectors.toList());
        sb.append(keys.stream().collect(Collectors.joining("\t"))).append("\n");
        for (Map<String, String> m : entries) {
            List<String> l = new LinkedList();
            for (String key : keys) {
                l.add(m.getOrDefault(key, ""));
            }
            sb.append(l.stream().collect(Collectors.joining("\t"))).append("\n");
        }
        return sb.toString();
    }

    private static void writeToWriter(BufferedWriter bw, List<Pair<String, List<ValidationResult>>> results) {
        try {
            bw.write(toTable(results));
            bw.newLine();
        } catch (IOException ex) {
            log.error("Caught exception while trying to write validation results to buffered writer.", ex);
        }
    }

    private static List<Pair<String, List<ValidationResult>>> parseNames(Stream<String> lipidNames) {
        return lipidNames.map((t) -> {
            return parseName(t);
        }).collect(Collectors.toList());
    }

    private static List<Pair<String, List<ValidationResult>>> parseNamesWith(Stream<String> lipidNames, ValidationResult.Grammar grammar) {
        return lipidNames.map((t) -> {
            return parseNameWith(t, grammar);
        }).collect(Collectors.toList()).stream().map((t) -> {
            return Pair.of(t.getKey(), Arrays.asList(t.getValue()));
        }).collect(Collectors.toList());
    }

    private static Pair<String, ValidationResult> parseNameWith(String lipidName, ValidationResult.Grammar grammar) {
        Parser<LipidAdduct> parser = loadParsers().get(grammar);
        ValidationResult validationResult = new ValidationResult();
        if (parser == null) {
            throw new ConstraintViolationException("Unsupported grammar: " + grammar);
        }
        try {
            LipidAdduct la = parser.parse(lipidName, false);
            if (la == null) {
                validationResult.setLipidName(lipidName);
                validationResult.setMessages(Arrays.asList(parser.get_error_message()));
                validationResult.setGrammar(grammar);
                log.debug("Could not parse " + lipidName + " with " + grammar + " grammar: " + grammar + ". Message: " + parser.get_error_message());
            } else {
                validationResult.setLipidName(lipidName);
                validationResult.setLipidAdduct(la);
                validationResult.setGrammar(grammar);
                validationResult.setLevel(la.get_lipid_level());
                validationResult.setLipidMapsCategory(LIPID_CLASSES.get(la.lipid.getInfo().lipid_class).lipid_category.name());
                validationResult.setLipidMapsClass(getLipidMapsClassAbbreviation(LIPID_CLASSES.get(la.lipid.getInfo().lipid_class).class_name));
                validationResult.setLipidSpeciesInfo(la.lipid.getInfo());
                try {
                    String normalizedName = la.get_lipid_string();
                    validationResult.setCanonicalName(normalizedName);
                } catch (RuntimeException re) {
                    log.debug("Parsing error for {}!", lipidName);
                }
                extractFas(la, validationResult);
            }
        } catch (LipidParsingException ex) {

        }
        return Pair.of(lipidName, validationResult);
    }

    private static Pair<String, List<ValidationResult>> parseName(String lipidName) {
        List<ValidationResult> results = new ArrayList<>();
        Pair<String, ValidationResult> shorthandResult = parseNameWith(lipidName, ValidationResult.Grammar.SHORTHAND);
        if (shorthandResult.getValue().getMessages().isEmpty()) {
            return Pair.of(shorthandResult.getKey(), Arrays.asList(shorthandResult.getValue()));
        }
        Pair<String, ValidationResult> fattyAcid = parseNameWith(lipidName, ValidationResult.Grammar.FATTY_ACID);
        if (fattyAcid.getValue().getMessages().isEmpty()) {
            return Pair.of(fattyAcid.getKey(), Arrays.asList(fattyAcid.getValue()));
        }
        Pair<String, ValidationResult> goslinResult = parseNameWith(lipidName, ValidationResult.Grammar.GOSLIN);
        if (goslinResult.getValue().getMessages().isEmpty()) {
            return Pair.of(goslinResult.getKey(), Arrays.asList(goslinResult.getValue()));
        }
        Pair<String, ValidationResult> lipidMapsResult = parseNameWith(lipidName, ValidationResult.Grammar.LIPIDMAPS);
        if (lipidMapsResult.getValue().getMessages().isEmpty()) {
            return Pair.of(lipidMapsResult.getKey(), Arrays.asList(lipidMapsResult.getValue()));
        }
        Pair<String, ValidationResult> swissLipidsResult = parseNameWith(lipidName, ValidationResult.Grammar.SWISSLIPIDS);
        if (swissLipidsResult.getValue().getMessages().isEmpty()) {
            return Pair.of(swissLipidsResult.getKey(), Arrays.asList(swissLipidsResult.getValue()));
        }
        Pair<String, ValidationResult> hmdbResult = parseNameWith(lipidName, ValidationResult.Grammar.HMDB);
        if (hmdbResult.getValue().getMessages().isEmpty()) {
            return Pair.of(hmdbResult.getKey(), Arrays.asList(hmdbResult.getValue()));
        }
        ValidationResult r = new ValidationResult();
        r.setCanonicalName("");
        r.setLipidName(lipidName);
        r.setGrammar(ValidationResult.Grammar.NONE);
        List<String> messages = new ArrayList<>(hmdbResult.getValue().getMessages());
        messages.add("Lipid name could not be parsed with any grammar!");
        r.setMessages(messages);
        results.add(r);
        return Pair.of(lipidName, results);
    }

    private static void extractFas(LipidAdduct la, ValidationResult result) {
        result.setFattyAcids(la.lipid.get_fa_list());
    }

    private static String getLipidMapsClassAbbreviation(String lipidMapsClass) {
        Pattern lmcRegexp = Pattern.compile(LIPIDMAPS_CLASS_REGEXP);
        Matcher lmcMatcher = lmcRegexp.matcher(lipidMapsClass);
        if (lmcMatcher.matches() && lmcMatcher.groupCount() == 1) {
            lipidMapsClass = lmcMatcher.group(1);
        } else {
            lipidMapsClass = null;
        }
        return lipidMapsClass;
    }

    protected static String addLipidFileInputOption(Options options) {
        String versionOpt = "file";
        options.addOption("f", versionOpt, true, "Input a file name to read from for lipid name for parsing. Each lipid name must be on a separate line.");
        return versionOpt;
    }

    protected static String addLipidNameInputOption(Options options) {
        String versionOpt = "name";
        options.addOption("n", versionOpt, true, "Input a lipid name for parsing.");
        return versionOpt;
    }

    protected static String addVersionOption(Options options) {
        String versionOpt = "version";
        options.addOption("v", versionOpt, false, "Print version information.");
        return versionOpt;
    }

    protected static String addHelpOption(Options options) {
        String helpOpt = "help";
        options.addOption("h", helpOpt, false, "Print help message.");
        return helpOpt;
    }

    protected static String addOutputToFileOption(Options options) {
        String outputToFileOpt = "outputFile";
        options.addOption("o", outputToFileOpt, false, "Write output to file 'goslin-out.tsv' instead of to std out.");
        return outputToFileOpt;
    }

    protected static String addGrammarOption(Options options) {
        String grammarOpt = "grammar";
        options.addOption("g", grammarOpt, true, "Use the provided grammar explicitly instead of all grammars. Options are: " + Arrays.toString(ValidationResult.Grammar.values()));
        return grammarOpt;
    }

    protected static Map<ValidationResult.Grammar, Parser<LipidAdduct>> loadParsers() {
        if (DEFAULT_PARSERS == null) {
            Map<ValidationResult.Grammar, Parser<LipidAdduct>> parsers = new LinkedHashMap<>();
            for (ValidationResult.Grammar grammar : ValidationResult.Grammar.values()) {
                switch (grammar) {
                    case FATTY_ACID:
                        parsers.put(grammar, new FattyAcidParser());
                        break;
                    case GOSLIN:
                        parsers.put(grammar, new GoslinParser());
                        break;
                    case HMDB:
                        parsers.put(grammar, new HmdbParser());
                        break;
                    case LIPIDMAPS:
                        parsers.put(grammar, new LipidMapsParser());
                        break;
                    case SHORTHAND:
                        parsers.put(grammar, new ShorthandParser());
                        break;
                    case SWISSLIPIDS:
                        parsers.put(grammar, new SwissLipidsParser());
                        break;
                    case GOSLIN_FRAGMENTS:
                        //FIXME skipping for now
                        break;
                    case NONE:
                        break;
                }
            }
            return parsers;
        }
        return DEFAULT_PARSERS;
    }

}
