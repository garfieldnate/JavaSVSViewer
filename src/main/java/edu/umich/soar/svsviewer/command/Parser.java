package edu.umich.soar.svsviewer.command;

import java.util.EnumMap;
import java.util.List;

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
		LIGHTING, FLAT, CLEAR_DEPTH, DRAW_NAMES, WIREFRAME;
	}

	public record LayerCommand(int layerNumber, EnumMap<LayerOption, Integer> options) implements Command {
	}

	public static class DrawCommand implements Command {
	}

	@Nonnull
	public static Command parse(List<String> commandTokens) throws ParsingException {

		if (commandTokens.isEmpty()) {
			throw new ParsingException("cannot parse a command because commandTokens is empty", commandTokens);
		}
		switch (commandTokens.get(0)) {
			case "save" -> {
				if (commandTokens.size() != 2) {
					throw new ParsingException("Expected 1 argument to 'save' command", commandTokens);
				}
				return new SaveCommand(commandTokens.get(1));
			}
			case "layer" -> {
				return parseLayerCommand(commandTokens);
			}
			// TODO: collapse these two
			case "draw" -> {
				return parseDrawCommand(commandTokens.subList(1, commandTokens.size()));
			}
			default -> {
				return parseDrawCommand(commandTokens);
			}
		}
	}

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

	private static Command parseDrawCommand(List<String> commandTokens) {
		// TODO
		return new DrawCommand();
	}
}
