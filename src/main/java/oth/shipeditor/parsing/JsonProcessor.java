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

    private JsonProcessor() {
    }

    @SuppressWarnings("NestedAssignment")
    public static String correctJSON(File malformed) {
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(malformed, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Pattern to match unquoted values (non-numeric and non-boolean) excluding already quoted values.
        // This marvel of Regex magic is made by ChatGPT, my new machine overlord.
        Pattern pattern = Pattern.compile("(?<![\"])\\b(?!true|false|null|\\d+(?:\\.\\d+)?)([a-zA-Z_][\\w]*)\\b(?![\"])(?![^\"]*\"(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        Matcher matcher = pattern.matcher(jsonString);
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

}
