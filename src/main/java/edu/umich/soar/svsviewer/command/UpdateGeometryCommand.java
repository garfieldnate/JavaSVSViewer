package edu.umich.soar.svsviewer.command;

import javafx.scene.Group;
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
  public void interpret(Group root) {
    Box testBox = new Box(5, 5, 5);
    testBox.setMaterial(new PhongMaterial());
    root.getChildren().add(testBox);
  }

  public record Vertex(double x, double y, double z) {}
}
