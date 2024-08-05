package edu.umich.soar.svsviewer.util;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class Labels {

  public static Node createLabel(String text) {
    Node label = new TextFlow(new Text(text));
    label.getStyleClass().add("geometry-label");
    return label;
  }

  public static void updateLocation(Pane labelsPane, Node labeled, Node label) {
    updateLocation(labelsPane, labeled, new Point3D(0, 0, 0), label);
  }

  public static void updateLocation(
      Pane labelsPane, Node labeled, Point3D labeledCoordinate, Node label) {
    //    System.out.println(labelsPane + ", " + labeled + ", " + labeledCoordinate + ", " + label);
    Point2D screenLocation = labeled.localToScreen(labeledCoordinate);
    if (screenLocation == null) {
      return;
    }
    Point2D paneLocation = labelsPane.screenToLocal(screenLocation);
    label.setLayoutX(paneLocation.getX());
    label.setLayoutY(paneLocation.getY());
  }
}
