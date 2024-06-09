package edu.umich.soar.svsviewer.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

  /*
   * Match either:
   * non-whitespace or quote (unquoted token)
   * or quotes surrounding double backslashes, backslash quotes, or anything not a backslash or quote (space is fine)
   */
  static final Pattern tokenPattern =
      Pattern.compile(
          "(?:^|\\s+)((?:[^\\\"\\s])+|(?:\\\"(?:\\\\\\\\|\\\\\\\"|[^\\\"\\\\])*\\\"))",
          Pattern.MULTILINE);

  private static String cleanField(String field) {
    if (field.startsWith("\"")) {
      field = field.substring(1, field.length() - 1);
    }

    field = field.replaceAll("\\\\\\\\", "\\\\");

    return field;
  }

  /**
   * Commands are split into fields by whitespace; quoted fields can contain spaces, and \ is used
   * for escaping.
   *
   * @return list of fields in given command
   */
  public static List<String> tokenizeCommand(String command) {
    command = command.strip();
    List<String> fields = new ArrayList<>();

    final Matcher matcher = tokenPattern.matcher(command);

    while (matcher.find()) {
      if (matcher.groupCount() != 0) {
        fields.add(cleanField(matcher.group(1)));
      }
    }

    return fields;
  }
}
