package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.geometry.GeometryManager;
import javafx.scene.Group;

public interface Command {
  void interpret(GeometryManager geoManager);
}
