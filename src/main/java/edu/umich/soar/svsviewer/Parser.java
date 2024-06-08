package edu.umich.soar.svsviewer;

import java.util.List;

import jakarta.annotation.Nonnull;

public class Parser {

	public static class ParsingException extends Exception {
		public ParsingException(String message) {
			super(message);
		}
	}

	public interface Command {
	}

	public static class SaveCommand implements Command {
		private final String path;

		public SaveCommand(String path) {
			this.path = path;
		}
	}

	public static class LayerCommand implements Command {
	}

	public static class DrawCommand implements Command {
	}

	public static class DummyCommand implements Command {
	}

	@Nonnull
	public static Command parse(List<String> commandTokens) throws ParsingException {

		if (commandTokens.isEmpty()) {
			return new DummyCommand();
		}
		switch (commandTokens.get(0)) {
			case "save" -> {
				if (commandTokens.size() != 2) {
					throw new ParsingException("Expected 1 argument to 'save' command; args were " + commandTokens.subList(1, commandTokens.size()));
				}
				return new SaveCommand(commandTokens.get(1));
			}
			case "layer" -> {
//				TODO: parse layer command
				return new LayerCommand();
			}
			case "draw" -> {
//				TODO: parse draw command (rest of commandTokens)
				return new DrawCommand();
			}
			default -> {
//				TODO: parse draw command
				return new DrawCommand();
			}
		}
	}
}
