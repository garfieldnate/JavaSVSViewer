package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.SceneController;
import edu.umich.soar.svsviewer.scene.Geometry;
import edu.umich.soar.svsviewer.scene.GeometryManager;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;

import java.util.Collections;
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

  //  TODO: updating vertices, radius, or text below should clear all existing vertices, radius and
  // text (see calls to free_geom_shape in svs_viewer)

  @Override
  public void interpret(GeometryManager geoManager, SceneController sceneController) {
    for (Geometry geometry : geoManager.findGeometries(sceneMatcher, geometryMatcher)) {
      Group group = geometry.getGroup();

      //      TODO: stop displaying these dummy objects!
      Box testBox = new Box(5, 5, 5);
      geometry.getGroup().getChildren().add(testBox);
      //      TODO: can't see anything. Try out
      //      TriangleMesh testMesh = new TriangleMesh();
      //      testMesh.getPoints().setAll(5f, -5f, 5f, -5f, -5f, 5f, -5f, 5f, 5f);
      //      testMesh.getTexCoords().setAll(1, 1);
      //      MeshView testMeshView = new MeshView(testMesh);
      //      testMeshView.setCullFace(CullFace.NONE);
      //      geometry.getGroup().getChildren().add(testMeshView);

      if (position != null) {
        group.setTranslateX(position.get(0));
        group.setTranslateY(position.get(1));
        group.setTranslateZ(position.get(2));
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

      //      Next: can't see anything! Need to correctly specify texture and face coordinates to
      // make the triangles visible. Texture can be set to (0,0) for all vertices. Faces... I guess
      // we just count off every 3 and set that to a new face number?
      // We can also specify normals, which is done in svs_viewer (see calc_normals).
      if (vertices != null) {
        TriangleMesh mesh = verticesToTriangleMesh(vertices);
        MeshView meshView = new MeshView(mesh);
        meshView.setCullFace(CullFace.NONE);
        geometry.getGroup().getChildren().add(meshView);
      }
      if (radius != null) {
        Sphere s = new Sphere(radius);
        geometry.getGroup().getChildren().add(s);
      }
      if (text != null) {
        // svs_viewer calls draw_text(g->text, 0, 0), which would draw at the origin no matter where
        // the geometry is;
        // that seems wrong. I think maybe it was never implemented properly because it's not
        // actually supported in SGEL.
        // TODO: since it's not in SGEL, we can probably safely remove it.
        System.err.println("TODO: interpret text in " + getClass().getName());
      }
      if (layer != null) {
        // TODO: not supported in SGEL, so we can probably safely remove it.
        System.err.println("TODO: interpret layer in " + getClass().getName());
      }
      if (lineWidth != null) {
        // I assume that line width was meant for use with DrawMode.LINE (wireframes). This is not
        // supported in JavaFX
        // because there's no (or not guaranteed to be any) hardware support for it.
        // svs_viewer used glLineWidth, and the docs
        // (https://registry.khronos.org/OpenGL-Refpages/gl4/html/glLineWidth.xhtml)
        // say that only width 1 is guaranteed to be supported.
        // TODO: This isn't supported in SGEL anyway, so we can probably safely remove it.
        // https://stackoverflow.com/a/59615984/474819
        System.err.println(
            "Setting line width is not supported. Sorry! in " + getClass().getName());
      }

      if (color != null) {
        //        TODO: Soar's SVS doesn't support setting color, even though the viewer supports
        // it. Fix that!
        for (Node child : geometry.getGroup().getChildren()) {
          if (child instanceof Shape3D shape) {
            shape.setMaterial(
                new PhongMaterial(new Color(color.get(0), color.get(1), color.get(2), 1)));
          }
        }
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
    mesh.getPoints().setAll(pointArray);
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
