package edu.umich.soar.svsviewer.command;

import javafx.scene.Group;

public interface Command {
  default void interpret(Group root) {
    System.err.println("TODO: interpret " + getClass().getName());
  }
}
