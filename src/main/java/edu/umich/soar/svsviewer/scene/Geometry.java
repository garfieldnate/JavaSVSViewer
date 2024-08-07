package edu.umich.soar.svsviewer.scene;

import static edu.umich.soar.svsviewer.util.Labels.createLabel;

import edu.umich.soar.svsviewer.util.DrawingMode;
import java.util.function.Consumer;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class Geometry {
  private final SVSScene parent;
  private final String name;
  // May have 0 or 1 children; the 1 child would be a Shape3d.
  // We use a Group because we need to be able to specify position/rotation/scale
  // without any particular shape yet specified.
  private final Group group;
  // holds an exact copy of group, but with DrawMode always set to LINE. This is to
  // support DrawingMode.FILL_AND_LINE display.
  private final Group lineGroup;

  private final Node label;

  private final Scale scale;
  private final Rotate rotation;
  private final Translate translation;

  // The ordering here is significant (transform matrices are not commutative)
  private static final int TRANSLATE_TRANSFORM_INDEX = 0;
  private static final int ROTATE_TRANSFORM_INDEX = 1;
  private static final int SCALE_TRANSFORM_INDEX = 2;

  public Geometry(String name, SVSScene parent) {
    this.name = name;
    this.parent = parent;
    group = new Group();
    lineGroup = new Group();
    label = createLabel(name);

    rotation = new Rotate();
    translation = new Translate();
    scale = new Scale();
    modifyGroups(
        g -> {
          ObservableList<Transform> transforms = g.getTransforms();
          transforms.add(TRANSLATE_TRANSFORM_INDEX, translation);
          transforms.add(ROTATE_TRANSFORM_INDEX, rotation);
          transforms.add(SCALE_TRANSFORM_INDEX, scale);
        });
  }

  public Group getGroup() {
    return group;
  }

  public Group getLineGroup() {
    return lineGroup;
  }

  /**
   * If the client doesn't need the underlying main Group or line Group, it's recommended to operate
   * on them via this method to ensure that they stay in sync.
   */
  public void modifyGroups(Consumer<Group> modifier) {
    modifier.accept(getGroup());
    modifier.accept(getLineGroup());
  }

  public void clear() {
    modifyGroups(g -> g.getChildren().clear());
  }

  public void setTranslation(Translate t) {
    modifyGroups(g -> g.getTransforms().set(TRANSLATE_TRANSFORM_INDEX, t));
  }

  public void setRotation(Rotate r) {
    modifyGroups(g -> g.getTransforms().set(ROTATE_TRANSFORM_INDEX, r));
  }

  public void setScale(Scale s) {
    modifyGroups(g -> g.getTransforms().set(SCALE_TRANSFORM_INDEX, s));
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

  void setDrawingMode(DrawingMode mode) {
    if (mode == DrawingMode.LINE) {
      getGroup().setVisible(false);
      getLineGroup().setVisible(true);
    } else if (mode == DrawingMode.FILL) {
      getGroup().setVisible(true);
      getLineGroup().setVisible(false);
    } else if (mode == DrawingMode.FILL_AND_LINE) {
      getGroup().setVisible(true);
      getLineGroup().setVisible(true);
    }
  }
}
