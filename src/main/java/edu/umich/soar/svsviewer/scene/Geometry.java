package edu.umich.soar.svsviewer.scene;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

// The contained group may have 0 or 1 children; the 1 child would be a Shape3d.
// We use a Group because we need to be able to specify position/rotation/scale
// without any particular shape yet specified.
public class Geometry {
  private final SVSScene parent;
  private final String name;
  private final Group group;

  private final TextFlow label;

  public Geometry(String name, SVSScene parent) {
    this.name = name;
    this.parent = parent;
    group = new Group();
    // Text doesn't support background color, so wrap in TextFlow, which does
    label = new TextFlow(new Text(name));
    // black text on white background for readability
    label.setStyle(
        "-fx-background-color: white; -fx-font-weight: bold; -fx-font-family: 'Helvetica'");
    label.getStyleClass().add("geometry-label");
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
