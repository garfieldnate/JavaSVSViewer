package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.scene.Geometry;
import edu.umich.soar.svsviewer.scene.GeometryManager;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.List;

public record UpdateGeometryCommand(
    NameMatcher sceneMatcher,
    NameMatcher geometryMatcher,
    List<Double> position,
    List<Double> rotation,
    List<Double> scale,
    List<Double> color,
    List<Vertex> vertices,
    Double radius,
    String text,
    Integer layer,
    Double lineWidth)
    implements Command {

  @Override
  public void interpret(GeometryManager geoManager) {
    for (Geometry geometry : geoManager.findGeometries(sceneMatcher, geometryMatcher)) {
      Box testBox = new Box(5, 5, 5);
      testBox.setMaterial(new PhongMaterial());
      geometry.getGroup().getChildren().add(testBox);
      System.out.println("TODO: interpret " + getClass().getName());
    }
  }

  public record Vertex(double x, double y, double z) {}
}
