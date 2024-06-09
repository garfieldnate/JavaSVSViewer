package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.command.Command;

public class InterpretationException extends Exception {
  public InterpretationException(String message, Command command) {
    super(message += "\nCommand was:\n" + command);
  }
}
