package edu.umich.soar.svsviewer.command;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

	@Test
	void testSplitCommandWithQuotesAndEscapes() {
		final String string = "Hello there! These \"are quoted \\\\\\\"words\\\" but \\\\\" not \"that\" one\n";
		List<String> actual = Tokenizer.splitCommand(string);
		List<String> expected = Arrays.asList("Hello", "there!", "These", "are quoted \\\\\"words\\\" but \\", "not", "that", "one");
		assert (expected.equals(actual));
	}

	@Test
	void testSplitCommandWithLeadingQuotes() {
		final String string = "\"What is this?\\\"\" no no no no";
		List<String> actual = Tokenizer.splitCommand(string);
		List<String> expected = Arrays.asList("What is this?\\\"", "no", "no", "no", "no");
		assertArrayEquals(expected.toArray(), actual.toArray());
	}

	@Test
	void testSplitCommandEmptyInput() {
		final String string = "      \n";
		List<String> actual = Tokenizer.splitCommand(string);
		assert (actual.isEmpty());
	}
}
