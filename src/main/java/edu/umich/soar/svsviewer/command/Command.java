package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.geometry.GeometryManager;
import javafx.scene.Group;

public interface Command {
  default void interpret(GeometryManager geoManager) {
    System.err.println("TODO: interpret " + getClass().getName());
  }
}
