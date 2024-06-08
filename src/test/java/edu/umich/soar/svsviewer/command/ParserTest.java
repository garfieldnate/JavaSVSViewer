package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.command.Parser.ParsingException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

	@Test
	public void testParseEmptyTokensInvalid() {
		ParsingException exception = assertThrows(ParsingException.class, () -> Parser.parse(Collections.emptyList()));
		assertEquals("cannot parse a command because commandTokens is empty\nCommand was: []", exception.getMessage(), "Should throw exception for missing argument");
	}

	@Test
	public void testParseSaveMissingArgumentInvalid() {
		ParsingException exception = assertThrows(ParsingException.class, () -> Parser.parse(List.of("save")));
		assertEquals("Expected 1 argument to 'save' command\nCommand was: [save]", exception.getMessage(), "Should throw exception for missing argument");
	}

	@Test
	public void testParseSaveExtraArgumentInvalid() {
		ParsingException exception = assertThrows(ParsingException.class, () -> Parser.parse(List.of("save", "file", "foo")));
		assertEquals("Expected 1 argument to 'save' command\nCommand was: [save, file, foo]", exception.getMessage(), "Should throw exception for too many arguments");
	}

	@Test
	public void testParseSaveValid() throws ParsingException {
		Parser.Command actual = Parser.parse(List.of("save", "filename"));
		Parser.Command expected = new Parser.SaveCommand("filename");
		assertEquals(expected, actual);
	}

	@Test
	public void testParseLayerMissingArgsInvalid() {
		ParsingException exception = assertThrows(ParsingException.class, () -> Parser.parse(List.of("layer")));
		assertEquals("layer command requires at least 4 arguments\nCommand was: [layer]", exception.getMessage());
	}

	@Test
	public void testParseLayerUnmatchedOptionsInvalid() {
		ParsingException exception = assertThrows(ParsingException.class, () -> Parser.parse(List.of("layer", "1", "l", "2", "d")));
		assertEquals("layer command has unmatched option/value arguments\nCommand was: [layer, 1, l, 2, d]", exception.getMessage());
	}

	@Test
	public void testParseLayerMalformedLayerIntInvalid() {
		ParsingException exception = assertThrows(ParsingException.class, () -> Parser.parse(List.of("layer", "4l", "d", "2")));
		assertEquals("Could not parse layer number '4l'\nCommand was: [layer, 4l, d, 2]", exception.getMessage());
	}

	@Test
	public void testParseLayerMalformedOptionIntInvalid() {
		ParsingException exception = assertThrows(ParsingException.class, () -> Parser.parse(List.of("layer", "1", "l", "4l")));
		assertEquals("Could not parse integer from option value '4l'\nCommand was: [layer, 1, l, 4l]", exception.getMessage());
	}

	@Test
	public void testParseLayerUnknownOptionInvalid() {
		ParsingException exception = assertThrows(ParsingException.class, () -> Parser.parse(List.of("layer", "1", "z", "4")));
		assertEquals("Unknown layer option 'z'\nCommand was: [layer, 1, z, 4]", exception.getMessage());
	}

	@Test
	public void testParseLayerValid() throws ParsingException {
		Parser.Command actual = Parser.parse(List.of("layer", "42", "l", "4", "d", "3", "n", "0", "w", "4", "f", "20"));
		Parser.Command expected = new Parser.LayerCommand(42, new EnumMap<>(Parser.LayerOption.class) {{
			put(Parser.LayerOption.LIGHTING, 4);
			put(Parser.LayerOption.CLEAR_DEPTH, 3);
			put(Parser.LayerOption.DRAW_NAMES, 0);
			put(Parser.LayerOption.WIREFRAME, 4);
			put(Parser.LayerOption.FLAT, 20);
		}});

		assertEquals(expected, actual);
	}
}
