package edu.umich.soar.svsviewer.parsing;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

  @Test
  void testTokenizeCommandWithQuotesAndEscapes() {
    final String string =
        "Hello there! These \"are quoted \\\\\\\"words\\\" but \\\\\" not \"that\" one\n";
    List<String> actual = Tokenizer.tokenizeCommand(string);
    List<String> expected =
        Arrays.asList(
            "Hello", "there!", "These", "are quoted \\\\\"words\\\" but \\", "not", "that", "one");
    assert (expected.equals(actual));
  }

  @Test
  void testTokenizeCommandWithLeadingQuotes() {
    final String string = "\"What is this?\\\"\" no no no no";
    List<String> actual = Tokenizer.tokenizeCommand(string);
    List<String> expected = Arrays.asList("What is this?\\\"", "no", "no", "no", "no");
    assertArrayEquals(expected.toArray(), actual.toArray());
  }

  @Test
  void testTokenizeCommandEmptyInput() {
    final String string = "      \n";
    List<String> actual = Tokenizer.tokenizeCommand(string);
    assert (actual.isEmpty());
  }
}
