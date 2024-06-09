package edu.umich.soar.svsviewer.command;

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
}
