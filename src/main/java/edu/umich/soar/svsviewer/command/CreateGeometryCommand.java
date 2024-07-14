package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.scene.GeometryManager;

public record CreateGeometryCommand(NameMatcher sceneMatcher, String geometryName)
    implements Command {
  @Override
  public void interpret(GeometryManager geoManager) {
    geoManager.addGeometry(sceneMatcher, geometryName);
  }
}
