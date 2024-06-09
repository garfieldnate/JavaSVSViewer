package edu.umich.soar.svsviewer.command;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.Nonnull;

public class Parser {
	public static class ParsingException extends Exception {
		public ParsingException(String message, List<String> commandTokens) {
			super(message + "\nCommand was: " + commandTokens);
		}
	}

	public interface Command {
	}

	public record SaveCommand(String path) implements Command {
	}

	public enum LayerOption {
		LIGHTING, FLAT, CLEAR_DEPTH, DRAW_NAMES, WIREFRAME
	}

	public record LayerCommand(int layerNumber, EnumMap<LayerOption, Integer> options) implements Command {
	}

	public enum NameMatchType {
		// wildcard name patterns may contain "*" to match arbitrary text
		EXACT, WILDCARD
	}

	public record NameMatcher(String namePattern, NameMatchType matchType) {
	}

	public record DeleteSceneCommand(NameMatcher sceneMatcher) implements Command {
	}

	//	"Create" commands are no-ops if the scene/geometries already exist
	public record CreateSceneCommand(String sceneName) implements Command {
	}

	public record DeleteGeometryCommand(NameMatcher sceneMatcher, NameMatcher geometryMatcher) implements Command {
	}

	public record CreateGeometryCommand(NameMatcher sceneMatcher, String geometryName) implements Command {
	}

	public record Vertex(double x, double y, double z) {
	}

	public record UpdateGeometryCommand(NameMatcher sceneMatcher, NameMatcher geometryMatcher, List<Double> position,
										List<Double> rotation, List<Double> scale, List<Double> color,
										List<Vertex> vertices, Double radius, String text, Integer layer,
										Double lineWidth) implements Command {
	}

	private static class Cursor {
		private final List<String> commandTokens;
		private int currentTokenIndex;

		private Cursor(List<String> commandTokens) {

			this.commandTokens = commandTokens;
		}

		String advance() {
			String currentToken = getCurrentToken();
			currentTokenIndex++;
			return currentToken;
		}

		Double advanceIfDouble() {
			String currentToken = getCurrentToken();
			if (currentToken == null) {
				return null;
			}
			double d;
			try {
				d = Double.parseDouble(currentToken);
			} catch (NumberFormatException e) {
				return null;
			}
			currentTokenIndex++;
			return d;
		}

		boolean noRemainingTokens() {
			return currentTokenIndex == commandTokens.size();
		}

		String getCurrentToken() {
			if (noRemainingTokens()) {
				return null;
			}
			return commandTokens.get(currentTokenIndex);
		}

		void assertEmpty(String message) throws ParsingException {
			if (!noRemainingTokens()) {
				throw new ParsingException(message, commandTokens);
			}
		}
	}

	@Nonnull
	public static List<Command> parse(List<String> commandTokens) throws ParsingException {

		if (commandTokens.isEmpty()) {
			throw new ParsingException("cannot parse a command because commandTokens is empty", commandTokens);
		}
		switch (commandTokens.get(0)) {
			case "save" -> {
				if (commandTokens.size() != 2) {
					throw new ParsingException("Expected 1 argument to 'save' command", commandTokens);
				}
				return List.of(new SaveCommand(commandTokens.get(1)));
			}
			case "layer" -> {
				return List.of(parseLayerCommand(commandTokens));
			}
			default -> {
				return parseDrawCommand(commandTokens);
			}
		}
	}

	@Nonnull
	private static Command parseLayerCommand(List<String> commandTokens) throws ParsingException {
		// "layer", layer number, option, val
		if (commandTokens.size() < 4) {
			throw new ParsingException("layer command requires at least 4 arguments", commandTokens);
		}
		if (commandTokens.size() % 2 != 0) {
			throw new ParsingException("layer command has unmatched option/value arguments", commandTokens);
		}

		final int layerNumber;
		try {
			layerNumber = Integer.parseInt(commandTokens.get(1));
		} catch (NumberFormatException e) {
			throw new ParsingException("Could not parse layer number '" + commandTokens.get(1) + "'", commandTokens);
		}

		// parse following pairs of option/value
		EnumMap<LayerOption, Integer> options = new EnumMap<>(LayerOption.class);
		for (int tokenIndex = 2; tokenIndex <= commandTokens.size() - 2; tokenIndex += 2) {
			final int value;
			final String stringValue = commandTokens.get(tokenIndex + 1);
			try {
				value = Integer.parseInt(stringValue);
			} catch (NumberFormatException e) {
				throw new ParsingException("Could not parse integer from option value '" + stringValue + "'", commandTokens);
			}
			String option = commandTokens.get(tokenIndex);
			switch (option.charAt(0)) {
				case 'l' -> options.put(LayerOption.LIGHTING, value);
				case 'f' -> options.put(LayerOption.FLAT, value);
				case 'd' -> options.put(LayerOption.CLEAR_DEPTH, value);
				case 'n' -> options.put(LayerOption.DRAW_NAMES, value);
				case 'w' -> options.put(LayerOption.WIREFRAME, value);
				default -> throw new ParsingException("Unknown layer option '" + option + "'", commandTokens);
			}
		}

		return new LayerCommand(layerNumber, options);
	}

	@Nonnull
	private static List<Command> parseDrawCommand(List<String> commandTokens) throws ParsingException {
		Cursor cursor = new Cursor(commandTokens);
		if ("draw".equals(cursor.getCurrentToken())) {
			cursor.advance();
		}
		if (cursor.noRemainingTokens()) {
			throw new ParsingException("Scene pattern required in draw command", commandTokens);
		}

		boolean exactSceneMatch = false;
		String scenePattern = cursor.advance();
		if (scenePattern.startsWith("-")) {
			cursor.assertEmpty("Extra fields found in scene removal command");
			return List.of(new DeleteSceneCommand(new NameMatcher(scenePattern.substring(1), NameMatchType.WILDCARD)));
		}
		List<Command> commands = new ArrayList<>();
		if (scenePattern.startsWith("+")) {
			scenePattern = scenePattern.substring(1);
			commands.add(new CreateSceneCommand(scenePattern));
			exactSceneMatch = true;
		}

		if (cursor.noRemainingTokens()) {
			return commands;
		}

		final NameMatcher sceneMatcher;
		if (exactSceneMatch) {
			sceneMatcher = new NameMatcher(scenePattern, NameMatchType.EXACT);
		} else {
			sceneMatcher = new NameMatcher(scenePattern, NameMatchType.WILDCARD);
		}

		boolean exactGeometryMatch = false;
		String geometryPattern = cursor.advance();
		if (geometryPattern.startsWith("-")) {
			commands.add(new DeleteGeometryCommand(sceneMatcher, new NameMatcher(geometryPattern.substring(1), NameMatchType.WILDCARD)));
			cursor.assertEmpty("Extra fields found in geometry removal command");
			return commands;
		} else if (geometryPattern.startsWith("+")) {
			geometryPattern = geometryPattern.substring(1);
			commands.add(new CreateGeometryCommand(sceneMatcher, geometryPattern));
			exactGeometryMatch = true;
		}

		final NameMatcher geometryMatcher;
		if (exactGeometryMatch) {
			geometryMatcher = new NameMatcher(geometryPattern, NameMatchType.EXACT);
		} else {
			geometryMatcher = new NameMatcher(geometryPattern, NameMatchType.WILDCARD);
		}

		if (!cursor.noRemainingTokens()) {
			commands.add(parseGeometryCommand(cursor, sceneMatcher, geometryMatcher));
		}
		return commands;
	}

	@Nonnull
	private static Command parseGeometryCommand(Cursor cursor, NameMatcher sceneMatcher, NameMatcher geometryMatcher) throws ParsingException {
		List<Double> position = null;
		List<Double> rotation = null;
		List<Double> scale = null;
		List<Double> color = null;
		List<Vertex> vertices = null;
		Double radius = null;
		String text = null;
		Integer layer = null;
		Double lineWidth = null;
		while (!cursor.noRemainingTokens()) {
			switch (cursor.advance()) {
				case "p" -> position = parseDoubles(cursor, 3, "position (p)");
				case "r" -> rotation = parseDoubles(cursor, 4, "rotation (r)");
				case "s" -> scale = parseDoubles(cursor, 3, "scale (s)");
				case "c" -> color = parseDoubles(cursor, 3, "color (c)");
				case "v" -> {
					if (vertices != null) {
						throw new ParsingException("Found more than one set of vertices", cursor.commandTokens);
					}
					List<Double> flatVertices = parseDoubles(cursor, "vertices (v)");
					if (flatVertices.size() % 3 != 0) {
						throw new ParsingException("Found " + flatVertices.size() + " vertices (must be multiple of 3)", cursor.commandTokens);
					}
					vertices = new ArrayList<>();
					for (int i = 0; i < flatVertices.size(); i += 3) {
						vertices.add(new Vertex(flatVertices.get(i), flatVertices.get(i + 1), flatVertices.get(i + 2)));
					}
				}
				case "b" -> radius = parseDouble(cursor, "ball radius (b)");
				case "t" -> {
					text = cursor.advance();
					if (Objects.isNull(text)) {
						throw new ParsingException("t (text) argument missing", cursor.commandTokens);
					}
				}
				case "l" -> {
					layer = parseInt(cursor, "layer (l)");
					if (layer < 0) {
						throw new ParsingException("Layer (l) argument must be non-negative", cursor.commandTokens);
					}
				}
				case "w" -> lineWidth = parseDouble(cursor, "line width (w)");
				default ->
					throw new ParsingException("Unknown geometry argument " + cursor.getCurrentToken(), cursor.commandTokens);

			}
		}
		return new UpdateGeometryCommand(sceneMatcher, geometryMatcher, position, rotation, scale, color, vertices, radius, text, layer, lineWidth);
	}

	/**
	 * Parse list of doubles of at least length 1
	 */
	static List<Double> parseDoubles(Cursor cursor, String sectionName) throws ParsingException {
		List<Double> valuesList = new ArrayList<>();

		while (!cursor.noRemainingTokens()) {
			Double arg = cursor.advanceIfDouble();
			if (arg == null) {
				break;
			}
			valuesList.add(arg);
		}

		if (valuesList.isEmpty()) {
			throw new ParsingException("No number arguments provided to " + sectionName, cursor.commandTokens);
		}

		return valuesList;
	}

	static List<Double> parseDoubles(Cursor cursor, int n, String sectionName) throws ParsingException {
		List<Double> valuesList = new ArrayList<>();
		for (int i = 0; i != n && !cursor.noRemainingTokens(); i++) {
			double arg = parseDouble(cursor, sectionName);
			valuesList.add(arg);
		}
		if (n > 0 && valuesList.size() != n) {
			throw new ParsingException("Expected " + n + " args to " + sectionName + " but found only " + valuesList.size(), cursor.commandTokens);
		}
		return valuesList;
	}

	static double[] toPrimitiveArray(List<Double> original) {
		double[] valuesArray = new double[original.size()];
		for (int i = 0; i < valuesArray.length; i++) {
			valuesArray[i] = original.get(i);
		}
		return valuesArray;
	}

	static double parseDouble(Cursor cursor, String sectionName) throws ParsingException {
		double arg;
		try {
			arg = Double.parseDouble(cursor.advance());
		} catch (NullPointerException | NumberFormatException e) {
			throw new ParsingException(sectionName + " argument missing or not a valid floating point number", cursor.commandTokens);
		}
		if (Double.isNaN(arg) || Double.isInfinite(arg)) {
			throw new ParsingException(sectionName + " argument " + arg + " is invalid", cursor.commandTokens);
		}
		return arg;
	}

	static int parseInt(Cursor cursor, String sectionName) throws ParsingException {
		int arg;
		try {
			arg = Integer.parseInt(cursor.advance());
		} catch (NullPointerException | NumberFormatException e) {
			throw new ParsingException(sectionName + " argument missing or not a valid integer", cursor.commandTokens);
		}
		return arg;
	}
}
