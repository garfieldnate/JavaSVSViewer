package edu.umich.soar.svsviewer;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.composites.PolyLine3D;
import org.fxyz3d.shapes.composites.PolyLine3D.LineType;

import java.util.List;

// TODO: Code only works just for showing XY plane.
public class Axes3DBuilder {
  private static final LineType lineType = LineType.TRIANGLE;

  private int numberOfGridLines = 10;

  private double gridSize = .5;

  private float gridLineWidth = .05f;
  private float axisLineWidth = .05f;

  private boolean showXAxis = true;
  private boolean showYAxis = true;
  private boolean showZAxis = false;

  private Color xAxisColor = new Color(0.7, 0.0, 0.0, 0.5);
  private Color yAxisColor = new Color(0.0, 0.7, 0.0, 0.5);
  private Color zAxisColor = new Color(0.0, 0.0, 0.7, 0.5);
  private Color gridColor = new Color(0.3, 0.3, 0.3, 0.5);

  public Node build() {
    //    TODO: support showing Z axis
    Group root = new Group();

    // Grid lines parallel to axes
    double g = numberOfGridLines * gridSize;
    for (int i = -numberOfGridLines; i <= numberOfGridLines; i++) {
      double offset = i * gridSize;
      if (showXAxis) {
        PolyLine3D lineX =
            new PolyLine3D(
                List.of(new Point3D(-g, 0, offset), new Point3D(g, 0, offset)),
                gridLineWidth,
                gridColor,
                lineType);
        root.getChildren().add(lineX);
      }

      if (showYAxis) {
        PolyLine3D lineY =
            new PolyLine3D(
                List.of(new Point3D(offset, 0, -g), new Point3D(offset, 0, g)),
                gridLineWidth,
                gridColor,
                lineType);
        root.getChildren().add(lineY);
      }

      if (showZAxis) {
        PolyLine3D lineZ =
            new PolyLine3D(
                List.of(new Point3D(0, -g, offset), new Point3D(0, g, offset)),
                gridLineWidth,
                gridColor,
                lineType);
        root.getChildren().add(lineZ);
      }
    }

    // Axis lines
    if (showXAxis) {
      PolyLine3D xAxis =
          new PolyLine3D(
              List.of(new Point3D(0.0, 0, -g), new Point3D(0.0, 0, g)),
              axisLineWidth,
              xAxisColor,
              lineType);
      root.getChildren().add(xAxis);
    }
    if (showYAxis) {
      PolyLine3D yAxis =
          new PolyLine3D(
              List.of(new Point3D(-g, 0, 0.0), new Point3D(g, 0, 0.0)),
              axisLineWidth,
              yAxisColor,
              lineType);
      root.getChildren().add(yAxis);
    }
    if (showZAxis) {
      PolyLine3D zAxis =
          new PolyLine3D(
              List.of(new Point3D(0.0, -g, 0.0), new Point3D(0.0, g, 0.0)),
              axisLineWidth,
              zAxisColor,
              lineType);
      root.getChildren().add(zAxis);
    }
    AmbientLight ambientLight = new AmbientLight(Color.WHITE);
    ambientLight.getScope().add(root);
    root.getChildren().add(ambientLight);

    return root;
  }

  public void setNumberOfGridLines(int numberOfGridLines) {
    this.numberOfGridLines = numberOfGridLines;
  }

  public void setGridSize(double gridSize) {
    this.gridSize = gridSize;
  }

  public void setGridLineWidth(float gridLineWidth) {
    this.gridLineWidth = gridLineWidth;
  }

  public void setAxisLineWidth(float axisLineWidth) {
    this.axisLineWidth = axisLineWidth;
  }

  public void setShowXAxis(boolean showXAxis) {
    this.showXAxis = showXAxis;
  }

  public void setShowYAxis(boolean showYAxis) {
    this.showYAxis = showYAxis;
  }

  public void setShowZAxis(boolean showZAxis) {
    this.showZAxis = showZAxis;
  }

  public void setXAxisColor(Color xAxisColor) {
    this.xAxisColor = xAxisColor;
  }

  public void setYAxisColor(Color yAxisColor) {
    this.yAxisColor = yAxisColor;
  }

  public void setZAxisColor(Color zAxisColor) {
    this.zAxisColor = zAxisColor;
  }

  public void setGridColor(Color gridColor) {
    this.gridColor = gridColor;
  }
}
