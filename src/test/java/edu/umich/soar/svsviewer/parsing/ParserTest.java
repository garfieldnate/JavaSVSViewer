package edu.umich.soar.svsviewer.parsing;

import edu.umich.soar.svsviewer.command.*;
import edu.umich.soar.svsviewer.parsing.Parser.ParsingException;
import edu.umich.soar.svsviewer.command.UpdateGeometryCommand.Vertex;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

  @Test
  public void testParseEmptyTokensInvalid() {
    ParsingException exception =
        assertThrows(ParsingException.class, () -> Parser.parse(Collections.emptyList()));
    assertEquals(
        "cannot parse a command because commandTokens is empty\nCommand was: []",
        exception.getMessage(),
        "Should throw exception for missing argument");
  }

  @Test
  public void testParseSaveMissingArgumentInvalid() {
    ParsingException exception =
        assertThrows(ParsingException.class, () -> Parser.parse(List.of("save")));
    assertEquals(
        "Expected 1 argument to 'save' command\nCommand was: [save]",
        exception.getMessage(),
        "Should throw exception for missing argument");
  }

  @Test
  public void testParseSaveExtraArgumentInvalid() {
    ParsingException exception =
        assertThrows(ParsingException.class, () -> Parser.parse(List.of("save", "file", "foo")));
    assertEquals(
        "Expected 1 argument to 'save' command\nCommand was: [save, file, foo]",
        exception.getMessage(),
        "Should throw exception for too many arguments");
  }

  @Test
  public void testParseSaveValid() throws ParsingException {
    List<Command> actual = Parser.parse(List.of("save", "filename"));
    List<Command> expected = List.of(new SaveCommand("filename"));
    assertEquals(expected, actual);
  }

  @Test
  public void testParseLayerMissingArgsInvalid() {
    ParsingException exception =
        assertThrows(ParsingException.class, () -> Parser.parse(List.of("layer")));
    assertEquals(
        "layer command requires at least 4 arguments\nCommand was: [layer]",
        exception.getMessage());
  }

  @Test
  public void testParseLayerUnmatchedOptionsInvalid() {
    ParsingException exception =
        assertThrows(
            ParsingException.class, () -> Parser.parse(List.of("layer", "1", "l", "2", "d")));
    assertEquals(
        "layer command has unmatched option/value arguments\nCommand was: [layer, 1, l, 2, d]",
        exception.getMessage());
  }

  @Test
  public void testParseLayerMalformedLayerIntInvalid() {
    ParsingException exception =
        assertThrows(ParsingException.class, () -> Parser.parse(List.of("layer", "4l", "d", "2")));
    assertEquals(
        "Could not parse layer number '4l'\nCommand was: [layer, 4l, d, 2]",
        exception.getMessage());
  }

  @Test
  public void testParseLayerMalformedOptionIntInvalid() {
    ParsingException exception =
        assertThrows(ParsingException.class, () -> Parser.parse(List.of("layer", "1", "l", "4l")));
    assertEquals(
        "Could not parse integer from option value '4l'\nCommand was: [layer, 1, l, 4l]",
        exception.getMessage());
  }

  @Test
  public void testParseLayerUnknownOptionInvalid() {
    ParsingException exception =
        assertThrows(ParsingException.class, () -> Parser.parse(List.of("layer", "1", "z", "4")));
    assertEquals("Unknown layer option 'z'\nCommand was: [layer, 1, z, 4]", exception.getMessage());
  }

  @Test
  public void testParseLayerValid() throws ParsingException {
    List<Command> actual =
        Parser.parse(List.of("layer", "42", "l", "4", "d", "3", "n", "0", "w", "4", "f", "20"));
    List<Command> expected =
        List.of(
            new LayerCommand(
                42,
                new EnumMap<>(LayerCommand.LayerOption.class) {
                  {
                    put(LayerCommand.LayerOption.LIGHTING, 4);
                    put(LayerCommand.LayerOption.CLEAR_DEPTH, 3);
                    put(LayerCommand.LayerOption.DRAW_NAMES, 0);
                    put(LayerCommand.LayerOption.WIREFRAME, 4);
                    put(LayerCommand.LayerOption.FLAT, 20);
                  }
                }));

    assertEquals(expected, actual);
  }

  @Test
  public void testDrawRemoveScene() throws ParsingException {
    List<Command> actual = Parser.parse(List.of("draw", "-S1"));
    List<Command> expected =
        List.of(new DeleteSceneCommand(new NameMatcher("S1", NameMatcher.NameMatchType.WILDCARD)));

    assertEquals(expected, actual);
  }

  @Test
  public void testDrawCreateScene() throws ParsingException {
    List<Command> actual = Parser.parse(List.of("draw", "+S1"));
    List<Command> expected = List.of(new CreateSceneCommand("S1"));

    assertEquals(expected, actual);
  }

  @Test
  public void testDrawDeleteGeometry() throws ParsingException {
    List<Command> actual = Parser.parse(List.of("draw", "S1", "-foo"));
    List<Command> expected =
        List.of(
            new DeleteGeometryCommand(
                new NameMatcher("S1", NameMatcher.NameMatchType.WILDCARD),
                new NameMatcher("foo", NameMatcher.NameMatchType.WILDCARD)));

    assertEquals(expected, actual);
  }

  @Test
  public void testCreateGeometry() throws ParsingException {
    List<Command> actual = Parser.parse(List.of("draw", "+S1", "+foo"));
    List<Command> expected =
        List.of(
            new CreateSceneCommand("S1"),
            new CreateGeometryCommand(
                new NameMatcher("S1", NameMatcher.NameMatchType.EXACT), "foo"));

    assertEquals(expected, actual);
  }

  @Test
  public void testUpdateGeometryVertices() throws ParsingException {
    String s =
        "S1 foo v 0.5 0.5 0.5 0.5 0.5 -0.5 0.5 -0.5 0.5 0.5 -0.5 -0.5 -0.5 0.5 0.5 -0.5 0.5 -0.5 -0.5 -0.5 0.5 -0.5 -0.5 -0.5   p -0.465155 0.475745 1.15673 r 0 0 0 1  s 0.106014 0.106025 0.11345 c 1 2 3 l 2 w 4.2";
    List<Command> actual = Parser.parse(List.of(s.split("\\s+")));
    List<Command> expected =
        List.of(
            new UpdateGeometryCommand(
                new NameMatcher("S1", NameMatcher.NameMatchType.WILDCARD),
                new NameMatcher("foo", NameMatcher.NameMatchType.WILDCARD),
                List.of(-0.465155, 0.475745, 1.15673),
                List.of(0.0, 0.0, 0.0, 1.0),
                List.of(0.106014, 0.106025, 0.11345),
                List.of(1d, 2d, 3d),
                List.of(
                    new Vertex(0.5, 0.5, 0.5),
                    new Vertex(0.5, 0.5, -0.5),
                    new Vertex(0.5, -0.5, 0.5),
                    new Vertex(0.5, -0.5, -0.5),
                    new Vertex(-0.5, 0.5, 0.5),
                    new Vertex(-0.5, 0.5, -0.5),
                    new Vertex(-0.5, -0.5, 0.5),
                    new Vertex(-0.5, -0.5, -0.5)),
                null,
                null,
                2,
                4.2));

    assertEquals(expected, actual);
  }

  @Test
  public void testUpdateGeometryRadius() throws ParsingException {
    String s =
        "S1 foo p -0.465155 0.475745 1.15673 r 0 0 0 1  s 0.106014 0.106025 0.11345 c 1 2 3 b 3.4 l 2 w 4.2";
    List<Command> actual = Parser.parse(List.of(s.split("\\s+")));
    List<Command> expected =
        List.of(
            new UpdateGeometryCommand(
                new NameMatcher("S1", NameMatcher.NameMatchType.WILDCARD),
                new NameMatcher("foo", NameMatcher.NameMatchType.WILDCARD),
                List.of(-0.465155, 0.475745, 1.15673),
                List.of(0.0, 0.0, 0.0, 1.0),
                List.of(0.106014, 0.106025, 0.11345),
                List.of(1d, 2d, 3d),
                null,
                3.4,
                null,
                2,
                4.2));

    assertEquals(expected, actual);
  }

  @Test
  public void testUpdateGeometryText() throws ParsingException {
    String s =
        "S1 foo   p -0.465155 0.475745 1.15673 r 0 0 0 1  s 0.106014 0.106025 0.11345 c 1 2 3 l 2 w 4.2 t foo";
    List<Command> actual = Parser.parse(List.of(s.split("\\s+")));
    List<Command> expected =
        List.of(
            new UpdateGeometryCommand(
                new NameMatcher("S1", NameMatcher.NameMatchType.WILDCARD),
                new NameMatcher("foo", NameMatcher.NameMatchType.WILDCARD),
                List.of(-0.465155, 0.475745, 1.15673),
                List.of(0.0, 0.0, 0.0, 1.0),
                List.of(0.106014, 0.106025, 0.11345),
                List.of(1d, 2d, 3d),
                null,
                null,
                "foo",
                2,
                4.2));

    assertEquals(expected, actual);
  }

  @Test
  public void testOnlyOneShapeUpdateAllowed() throws ParsingException {
    String s = "S1 foo v 0.5 0.5 0.5 b 1.0 t hello";

    ParsingException exception =
        assertThrows(ParsingException.class, () -> Parser.parse(List.of(s.split("\\s+"))));
    assertThat(exception.getMessage())
        .contains(
            "Only one of vertices, radius or text can be defined in an update command. Found: [vertices, radius, text]");
  }
}
