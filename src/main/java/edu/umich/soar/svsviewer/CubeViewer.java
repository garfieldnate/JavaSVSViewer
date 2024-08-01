package edu.umich.soar.svsviewer;

import com.github.quickhull3d.QuickHull3D;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.Arrays;

import com.github.quickhull3d.Point3d;

// drag the mouse over the cube to rotate it.
public class CubeViewer extends Application {

  double anchorX, anchorY, anchorAngle;

  private PerspectiveCamera addCamera(Scene scene) {
    PerspectiveCamera perspectiveCamera = new PerspectiveCamera(false);
    scene.setCamera(perspectiveCamera);
    return perspectiveCamera;
  }

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    TriangleMesh mesh = getCube(200);
    final MeshView rect = new MeshView(mesh);
    rect.setMaterial(new PhongMaterial(Color.DARKGREEN));
    rect.setRotationAxis(Rotate.Y_AXIS);
    rect.setTranslateX(250);
    rect.setTranslateY(250);
    //    This shows the back sides of triangles in black
    rect.setCullFace(CullFace.NONE);

    final Group root = new Group(rect);
    final Scene scene = new Scene(root, 500, 500, true);

    scene.setOnMousePressed(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngle = rect.getRotate();
          }
        });

    scene.setOnMouseDragged(event -> rect.setRotate(anchorAngle + anchorX - event.getSceneX()));

    addCamera(scene);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private TriangleMesh getCube(int screenWidth) {
    int sideLength = screenWidth / 2;
    Point3d[] points = {
      new Point3d(sideLength, sideLength, sideLength),
      new Point3d(sideLength, sideLength, -sideLength),
      new Point3d(sideLength, -sideLength, -sideLength),
      new Point3d(-sideLength, sideLength, -sideLength),
      new Point3d(-sideLength, -sideLength, -sideLength),
      new Point3d(sideLength, -sideLength, sideLength),
      new Point3d(-sideLength, sideLength, sideLength),
      new Point3d(-sideLength, -sideLength, sideLength),
    };

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

    System.out.println("Vertices:");
    Point3d[] vertices = hull.getVertices();
    for (Point3d pnt : vertices) {
      System.out.println(pnt.x + " " + pnt.y + " " + pnt.z);
    }

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
    System.out.println("texture coords:");
    System.out.println(Arrays.toString(dummyTextureCoords));

    System.out.println("Faces:");
    //    [faceIndex][index] = vertexIndex
    int[][] qh3Dfaces = hull.getFaces(); // CLOCKWISE);
    for (int[] array : qh3Dfaces) {
      System.out.println(Arrays.toString(array));
    }
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
    System.out.println(Arrays.toString(jfxFaces));

    TriangleMesh mesh = new TriangleMesh();
    mesh.getPoints().setAll(jfxPoints);
    mesh.getFaces().setAll(jfxFaces);
    mesh.getTexCoords().setAll(dummyTextureCoords);

    return mesh;
  }
}
