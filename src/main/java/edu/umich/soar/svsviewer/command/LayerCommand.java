package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.SceneController;
import edu.umich.soar.svsviewer.scene.GeometryManager;

import java.util.EnumMap;

public record LayerCommand(int layerNumber, EnumMap<LayerOption, Integer> options)
    implements Command {

  public enum LayerOption {
    LIGHTING,
    FLAT,
    CLEAR_DEPTH,
    DRAW_NAMES,
    WIREFRAME
  }

  @Override
  public void interpret(GeometryManager geoManager, SceneController sceneController) {
    System.out.println("TODO: interpret " + getClass().getName());
  }
}
