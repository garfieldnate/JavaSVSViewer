package edu.umich.soar.svsviewer.scene;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.text.Text;

// The contained group may have 0 or 1 children; the 1 child would be a Shape3d.
// We use a Group because we need to be able to specify position/rotation/scale
// without any particular shape yet specified.
public class Geometry {
  private final SVSScene parent;
  private final String name;
  private final Group group;

  private final Group label;

  public Geometry(String name, SVSScene parent) {
    this.name = name;
    this.parent = parent;
    group = new Group();
    // wrap in Group so we can choose the exact location in the pane
    label = new Group(new Text(name));
    //    SAD! This doesn't work. Need bounds on scene or screen, which there is no listener for.
    // These don't update when the root group rotates or the camera moves, etc.
    group
        //        .boundsInLocalProperty()
        .boundsInParentProperty()
        .addListener(
            (observable, oldValue, newValue) ->
                // TODO NEXT: add a label object and update its location here.
                System.out.println(
                    "Geometry "
                        + this.name
                        + " updated; screen location is "
                        + group.localToScreen(0, 0, 0)));
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
