package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.SceneController;
import edu.umich.soar.svsviewer.scene.GeometryManager;

public record SaveCommand(String path) implements Command {
  //  TODO: move code from SceneController to here
  @Override
  public void interpret(GeometryManager geoManager, SceneController sceneController) {
    sceneController.saveScreenshot();
  }
}
