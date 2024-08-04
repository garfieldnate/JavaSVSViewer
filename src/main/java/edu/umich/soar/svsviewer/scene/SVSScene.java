package edu.umich.soar.svsviewer.scene;

import edu.umich.soar.svsviewer.util.WildcardMap;
import javafx.scene.Group;
import javafx.scene.control.Label;

public record SVSScene(String name, WildcardMap<Geometry> geometries, Group root, Group labels) {
  public SVSScene(String name) {
    this(name, new WildcardMap<>(), new Group(), new Group());
    //    NEXT: figure out how to show this label properly in an overlay.
    //      https://stackoverflow.com/questions/46011515/static-2d-text-over-3d-scene-in-javafx-java
    labels().getChildren().add(new Label("hello"));
  }
}
