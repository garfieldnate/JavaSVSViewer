package edu.umich.soar.svsviewer.scene;

import javafx.scene.Group;

public class Geometry {
  // This group may have 0 or 1 children; the 1 child would be a Shape3d. We use a Group because we
  // need to be able to
  // specify position/rotation/scale without any shape.
  private final Group group;

  public Geometry(Group group) {
    this.group = group;
  }

  public Group getGroup() {
    return group;
  }

  public static class Builder {
    public Geometry build(Group group) {
      return new Geometry(group);
    }
  }
}
