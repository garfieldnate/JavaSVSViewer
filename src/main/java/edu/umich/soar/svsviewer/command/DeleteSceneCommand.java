package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.scene.GeometryManager;

public record DeleteSceneCommand(NameMatcher sceneMatcher) implements Command {
  @Override
  public void interpret(GeometryManager geoManager) {
    geoManager.deleteScene(sceneMatcher);
  }
}
