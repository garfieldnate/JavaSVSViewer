package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.SceneController;
import edu.umich.soar.svsviewer.scene.GeometryManager;

public record DeleteSceneCommand(NameMatcher sceneMatcher) implements Command {
  @Override
  public void interpret(GeometryManager geoManager, SceneController sceneController) {
    geoManager.deleteScene(sceneMatcher);
  }
}
