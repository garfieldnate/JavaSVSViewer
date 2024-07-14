package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.scene.GeometryManager;

public interface Command {
  void interpret(GeometryManager geoManager);
}
