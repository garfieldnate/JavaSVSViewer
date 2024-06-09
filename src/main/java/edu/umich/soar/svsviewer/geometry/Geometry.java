package edu.umich.soar.svsviewer.geometry;

import javafx.scene.Group;

public class Geometry {
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
