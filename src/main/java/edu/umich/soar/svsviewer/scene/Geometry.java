package edu.umich.soar.svsviewer.scene;

import javafx.scene.Group;
import javafx.scene.Node;

import static edu.umich.soar.svsviewer.util.Labels.createLabel;

// The contained group may have 0 or 1 children; the 1 child would be a Shape3d.
// We use a Group because we need to be able to specify position/rotation/scale
// without any particular shape yet specified.
public class Geometry {
  private final SVSScene parent;
  private final String name;
  private final Group group;

  private final Node label;

  public Geometry(String name, SVSScene parent) {
    this.name = name;
    this.parent = parent;
    group = new Group();
    label = createLabel(name);
  }

  public Group getGroup() {
    return group;
  }

  public Node getLabel() {
    return label;
  }

  public String getName() {
    return name;
  }

  public SVSScene getParent() {
    return parent;
  }
}
