package edu.umich.soar.svsviewer.scene;

import edu.umich.soar.svsviewer.util.WildcardMap;
import javafx.scene.Group;
import javafx.scene.control.Label;

public record SVSScene(String name, WildcardMap<Geometry> geometries, Group root) {
  public SVSScene(String name) {
    this(name, new WildcardMap<>(), new Group());
  }
}
