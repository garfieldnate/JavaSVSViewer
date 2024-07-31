package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.SceneController;
import edu.umich.soar.svsviewer.scene.Geometry;
import edu.umich.soar.svsviewer.scene.GeometryManager;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.util.List;
import java.util.stream.Stream;

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

      //      Box testBox = new Box(5, 5, 5);
      //      geometry.getGroup().getChildren().add(testBox);

      if (position != null) {
        group.setTranslateX(position.get(0));
        group.setTranslateY(position.get(0));
        group.setTranslateZ(position.get(0));
      }
      if (rotation != null) {
        System.err.println("TODO: translate viewer's quaternions to JavaFX's 3D rotations");
        //        rotateWithQuaternion(rotation, group);
      }
      if (scale != null) {
        group.setScaleX(scale.get(0));
        group.setScaleY(scale.get(1));
        group.setScaleZ(scale.get(2));
      }

      if (color != null) {
        System.err.println("TODO: interpret color in " + getClass().getName());
      }

      //      Next: can't see anything! Maybe we need to implement rotating so that we can find it?
      //      or maybe this is all wrong, since the vertices actually specify a polyhedron directly.
      if (vertices != null) {
        TriangleMesh mesh = verticesToTriangleMesh(vertices);
        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(new PhongMaterial(Color.FIREBRICK));
        meshView.setCullFace(CullFace.NONE);
        geometry.getGroup().getChildren().add(meshView);
      }
      if (radius != null) {
        System.err.println("TODO: interpret radius in " + getClass().getName());
      }
      if (text != null) {
        System.err.println("TODO: interpret text in " + getClass().getName());
      }
      if (layer != null) {
        System.err.println("TODO: interpret layer in " + getClass().getName());
      }
      if (lineWidth != null) {
        System.err.println("TODO: interpret lineWidth in " + getClass().getName());
      }
    }
  }

  //        https://stackoverflow.com/a/61239299/474819
  private TriangleMesh verticesToTriangleMesh(List<Vertex> vertices) {
    float[] pointArray = new float[vertices.size() * 3];
    int index = 0;
    for (Float f :
        vertices.stream()
            .flatMap(v -> Stream.of(v.x(), v.y(), v.z()))
            .map(Double::floatValue)
            .toList()) {
      pointArray[index] = f;
      index++;
    }
    TriangleMesh mesh = new TriangleMesh();
    mesh.getPoints().addAll(pointArray);
    return mesh;
  }

  //    TODO: finish this
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
