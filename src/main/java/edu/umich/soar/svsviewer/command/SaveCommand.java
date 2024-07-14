package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.scene.GeometryManager;

public record SaveCommand(String path) implements Command {
  @Override
  public void interpret(GeometryManager geoManager) {
    System.out.println("TODO: interpret " + getClass().getName());
  }
}
