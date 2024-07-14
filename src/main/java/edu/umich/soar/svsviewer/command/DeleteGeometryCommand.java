package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.scene.GeometryManager;

public record DeleteGeometryCommand(NameMatcher sceneMatcher, NameMatcher geometryMatcher)
    implements Command {
  @Override
  public void interpret(GeometryManager geoManager) {
    geoManager.deleteGeometry(sceneMatcher, geometryMatcher);
  }
}
