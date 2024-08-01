package edu.umich.soar.svsviewer.command;

import com.github.quickhull3d.Point3d;
import com.github.quickhull3d.QuickHull3D;
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

import java.util.Arrays;
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
    // the work in the loop). Might need to copy, though, because another command might modify only
    // part of this list.
    for (Geometry geometry : geoManager.findGeometries(sceneMatcher, geometryMatcher)) {
      Group group = geometry.getGroup();

      //      TODO: stop displaying these dummy objects!
      //      Box testBox = new Box(5, 5, 5);
      //      geometry.getGroup().getChildren().add(testBox);
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

  // Reference: https://stackoverflow.com/a/61239299/474819
  private TriangleMesh verticesToTriangleMesh(List<Vertex> vertices) {
    Point3d[] points =
        vertices.stream().map(v -> new Point3d(v.x(), v.y(), v.z())).toArray(Point3d[]::new);

    // TODO: draw a point if only one vertex
    // TODO: draw a line segment if only two vertices
    // TODO: draw a triangle if only three vertices
    // NOTE: as in svs_viewer, we don't handle polygons besides triangles
    QuickHull3D hull = new QuickHull3D();
    //    TODO: catch IllegalArgumentException, print it and the name of the geometry
    hull.build(points);
    // we need triangles to use TriangleMesh; generally we have very simple shapes in SVS, so we
    // don't worry about thin triangles or other mentioned potential numerical stability issues
    hull.triangulate();

    Point3d[] qh3dPoints = hull.getVertices();

    float[] jfxPoints = new float[qh3dPoints.length * 3];
    for (int i = 0; i < qh3dPoints.length; i++) {
      int outIndex = i * 3;
      jfxPoints[outIndex] = (float) qh3dPoints[i].x;
      jfxPoints[outIndex + 1] = (float) qh3dPoints[i].y;
      jfxPoints[outIndex + 2] = (float) qh3dPoints[i].z;
    }

    // All 0's; we don't support textures
    float[] dummyTextureCoords = new float[qh3dPoints.length * 2];
    //    System.out.println("texture coords:");
    //    System.out.println(Arrays.toString(dummyTextureCoords));

    //    System.out.println("Faces:");
    //    [faceIndex][index] = vertexIndex
    int[][] qh3Dfaces = hull.getFaces(); // CLOCKWISE);
    //    for (int[] array : qh3Dfaces) {
    //      System.out.println(Arrays.toString(array));
    //    }
    // flatten for Triangle Mesh
    int[] jfxFaces = new int[qh3Dfaces.length * 6];
    for (int i = 0; i < qh3Dfaces.length; i++) {
      int outIndex = i * 6;
      jfxFaces[outIndex] = qh3Dfaces[i][0];
      jfxFaces[outIndex + 1] = 0;
      jfxFaces[outIndex + 2] = qh3Dfaces[i][1];
      jfxFaces[outIndex + 3] = 0;
      jfxFaces[outIndex + 4] = qh3Dfaces[i][2];
      jfxFaces[outIndex + 5] = 0;
    }
    //    System.out.println(Arrays.toString(jfxFaces));

    TriangleMesh mesh = new TriangleMesh();
    mesh.getPoints().setAll(jfxPoints);
    mesh.getFaces().setAll(jfxFaces);
    mesh.getTexCoords().setAll(dummyTextureCoords);

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
