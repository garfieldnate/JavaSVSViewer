package edu.umich.soar.svsviewer.command;

import edu.umich.soar.svsviewer.SceneController;
import edu.umich.soar.svsviewer.scene.Geometry;
import edu.umich.soar.svsviewer.scene.GeometryManager;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

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
    //    TODO: do the work before the loop and then just apply it in the loop (instead of repeating
    // the work in the loop)
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
        rotateWithQuaternion(rotation, group);
      }
      if (scale != null) {
        group.setScaleX(scale.get(0));
        group.setScaleY(scale.get(1));
        group.setScaleZ(scale.get(2));
      }

      //      Next: can't see anything! Need to correctly specify face coordinates to
      // make the triangles visible. I guess we just count off every 3 and set that to
      // a new face number? We can also specify normals, which is done in svs_viewer (see
      // calc_normals).
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
    //    TODO: NEXT: mesh still not showing. Try specifying normal.
    float[] points = new float[vertices.size() * 3];
    int index = 0;
    for (Float f :
        vertices.stream()
            .flatMap(v -> Stream.of(v.x(), v.y(), v.z()))
            .map(Double::floatValue)
            .toList()) {
      points[index] = f;
      index++;
    }

    float[] textureUVCoordinates = new float[vertices.size() * 2];
    // dummy texture coordinates; we don't support texturing, but we have to specify the coordinates
    // to
    // show the mesh
    for (int i = 0; i < vertices.size() * 2; i += 2) {
      textureUVCoordinates[i] = 0;
      textureUVCoordinates[+1] = 0;
    }

    //    TODO: if we specify normals, will be * 3 * 3
    int[] faces = new int[vertices.size() * 3 * 2];
    int faceIndex = 0;
    for (int i = 0; i < vertices.size() * 3 * 2; i += 3 * 2) {
      faces[i] = faceIndex;
      // dummy texture coordinate
      faces[i + 1] = 0;
      faces[i + 2] = faceIndex;
      // dummy texture coordinate
      faces[i + 3] = 0;
      faces[i + 4] = faceIndex;
      // dummy texture coordinate
      faces[i + 5] = 0;

      faceIndex++;
    }

    TriangleMesh mesh = new TriangleMesh();
    mesh.getPoints().setAll(points);
    mesh.getTexCoords().setAll(textureUVCoordinates);
    mesh.getFaces().setAll(faces);
    return mesh;
  }

  private void rotateWithQuaternion(List<Double> quaternion, Group group) {
    Rotate rotation = quaternionToRotation(quaternion);

    // Override any other existing rotations
    group.getTransforms().removeIf(transform -> transform instanceof Rotate);
    group.getTransforms().add(rotation);
  }

  //  TODO: create dedicated quaternion object instead of using a list
  private Rotate quaternionToRotation(List<Double> quaternion) {
    double x = quaternion.get(0);
    double y = quaternion.get(1);
    double z = quaternion.get(2);
    double w = quaternion.get(3);

    double s = Math.sqrt(1 - w * w);
    double angle = Math.toDegrees(2d * Math.acos(w));

    // normalize, but only if not too small (to avoid stability or divide-by-zero errors)
    // TODO: explain why we do this normalization. Is it really necessary?
    if (s > 0.00001) {
      x /= s;
      y /= s;
      z /= s;
    }

    Point3D rotationAxis = new Point3D(x, y, z);
    return new Rotate(angle, rotationAxis);
  }

  public record Vertex(double x, double y, double z) {}
}
