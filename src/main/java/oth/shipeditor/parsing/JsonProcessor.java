package oth.shipeditor.parsing;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ontheheavens
 * @since 01.07.2023
 */
@Log4j2
public final class JsonProcessor {

    private static final char QUOTES = '"';

    private static final Pattern LETTERS_AFTER_DIGIT = Pattern.compile("(?<=[0-9])f|(?<=[0-9])d");

    /**
     * Pattern to match unquoted values (non-numeric and non-boolean) excluding already quoted values.
     */
    private static final Pattern UNQUOTED_VALUES = Pattern.compile("(?<!\")\\b(?!true|false|null|\\d+(?:\\.\\d+)?)([a-zA-Z_]\\w*)\\b(?!\")(?![^\"]*\"(?:[^\"]*\"[^\"]*\")*[^\"]*$)");


    /**
     * Pattern to match semicolons used for object separation outside quoted string values.
     */
    private static final Pattern SEPARATORS = Pattern.compile("(?<!\")\\s*;\\s*(?![^\"]*\"(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    private JsonProcessor() {
    }

    public static String straightenMalformed(File input) {
        String preprocessed = JsonProcessor.correctJSONUnquotedValues(input);
        preprocessed = JsonProcessor.correctSpuriousSeparators(preprocessed);
        preprocessed = JsonProcessor.correctNumberLetterSignums(preprocessed);
        return preprocessed;
    }

    @SuppressWarnings({"NestedAssignment", "RegExpSimplifiable", "CallToPrintStackTrace"})
    private static String correctJSONUnquotedValues(File malformed) {
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(malformed, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Matcher matcher = UNQUOTED_VALUES.matcher(jsonString);
        StringBuilder preprocessedJson = new StringBuilder();
        int previousEnd = 0;
        while (matcher.find()) {
            // Append the part before the match.
            preprocessedJson.append(jsonString, previousEnd, matcher.start());
            // Append the quoted value.
            preprocessedJson.append(QUOTES).append(matcher.group()).append(QUOTES);
            // Update the previous end position.
            previousEnd = matcher.end();
        }
        preprocessedJson.append(jsonString.substring(previousEnd));

        return preprocessedJson.toString();
    }

    @SuppressWarnings("RegExpSimplifiable")
    private static String correctSpuriousSeparators(String inputJSON) {
        Matcher matcher = SEPARATORS.matcher(inputJSON);
        StringBuilder preprocessedJson = new StringBuilder();
        int previousEnd = 0;
        while (matcher.find()) {
            // Append the part before the match.
            preprocessedJson.append(inputJSON, previousEnd, matcher.start());
            // Append a comma to replace the semicolon.
            preprocessedJson.append(",");
            // Update the previous end position.
            previousEnd = matcher.end();
        }
        preprocessedJson.append(inputJSON.substring(previousEnd));
        return preprocessedJson.toString();
    }

    private static String correctNumberLetterSignums(CharSequence inputJSON) {
        Matcher matcher = LETTERS_AFTER_DIGIT.matcher(inputJSON);
        return matcher.replaceAll("");
    }

}
