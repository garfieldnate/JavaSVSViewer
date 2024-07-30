package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.SceneController;
import edu.umich.soar.svsviewer.scene.Geometry;
import edu.umich.soar.svsviewer.scene.GeometryManager;
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.List;

public record UpdateGeometryCommand(
    NameMatcher sceneMatcher,
    NameMatcher geometryMatcher,
    //    TODO: these are stupid. Use a real 3d point record.
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
  public void interpret(GeometryManager geoManager, SceneController sceneController) {
    for (Geometry geometry : geoManager.findGeometries(sceneMatcher, geometryMatcher)) {
      Group group = geometry.getGroup();
      if (position != null) {
        group.setTranslateX(position.get(0));
        group.setTranslateY(position.get(0));
        group.setTranslateZ(position.get(0));
      }
      if (scale != null) {
        group.setScaleX(scale.get(0));
        group.setScaleY(scale.get(1));
        group.setScaleZ(scale.get(2));
      }
      if (rotation != null) {
        System.err.println("TODO: translate viewer's quaternions to JavaFX's 3D rotations");
        //        rotateWithQuaternion(rotation, group);
      }

      Box testBox = new Box(5, 5, 5);
      testBox.setMaterial(new PhongMaterial());
      geometry.getGroup().getChildren().add(testBox);
      System.out.println("TODO: interpret " + getClass().getName());
    }
  }

  //  next: finish this
  //  private void rotateWithQuaternion(List<Double> rotation, Group group) {
  //    // Assuming quaternion is given as (w, x, y, z)
  //    double w = quaternion.getW();
  //    double x = quaternion.getX();
  //    double y = quaternion.getY();
  //    double z = quaternion.getZ();
  //
  //// Convert quaternion to Euler angles (roll, pitch, yaw)
  //    double roll = Math.atan2(2.0 * (w * x + y * z), 1.0 - 2.0 * (x * x + y * y));
  //    double pitch = Math.asin(2.0 * (w * y - z * x));
  //    double yaw = Math.atan2(2.0 * (w * z + x * y), 1.0 - 2.0 * (y * y + z * z));
  //
  //// Convert radians to degrees as JavaFX uses degrees for rotations
  //    double rollDegrees = Math.toDegrees(roll);
  //    double pitchDegrees = Math.toDegrees(pitch);
  //    double yawDegrees = Math.toDegrees(yaw);
  //
  //// Apply rotations to the node
  //    node.getTransforms().addAll(
  //      new Rotate(rollDegrees, Rotate.X_AXIS),
  //      new Rotate(pitchDegrees, Rotate.Y_AXIS),
  //      new Rotate(yawDegrees, Rotate.Z_AXIS)
  //    );
  //
  //
  //  }

  public record Vertex(double x, double y, double z) {}
}
