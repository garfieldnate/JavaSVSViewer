package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.scene.GeometryManager;

public record CreateSceneCommand(String sceneName) implements Command {
  @Override
  public void interpret(GeometryManager geoManager) {
    geoManager.createSceneIfNotExists(sceneName);
  }
}
