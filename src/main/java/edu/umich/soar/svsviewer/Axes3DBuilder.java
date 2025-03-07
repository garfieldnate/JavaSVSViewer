package edu.umich.soar.svsviewer;

import edu.umich.soar.svsviewer.util.Labels;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.composites.PolyLine3D;
import org.fxyz3d.shapes.composites.PolyLine3D.LineType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static edu.umich.soar.svsviewer.util.Labels.createLabel;

// TODO: Code might not always show a grid, depending on the visible axis choices.
// TODO: Put builder inside main class instead of the other way around
public class Axes3DBuilder {
  private static final LineType lineType = LineType.TRIANGLE;

  private int numberOfGridLines = 10;

  private double gridSize = .5d;

  private float gridLineWidth = .05f;
  private float axisLineWidth = .05f;

  private boolean showXAxis = true;
  private boolean showYAxis = true;
  private boolean showZAxis = true;
  private boolean labelAxes = true;

  private Color xAxisColor = new Color(0.7, 0.0, 0.0, 0.5);
  private Color yAxisColor = new Color(0.0, 0.7, 0.0, 0.5);
  private Color zAxisColor = new Color(0.0, 0.0, 0.7, 0.5);
  private Color gridColor = new Color(0.3, 0.3, 0.3, 0.5);

  public static final class Axes3D {
    private final Group root;
    private final List<Pair<javafx.geometry.Point3D, Node>> labels;
    private boolean visible;

    public Axes3D(Group root, List<Pair<javafx.geometry.Point3D, Node>> labels, boolean visible) {
      this.root = root;
      this.labels = labels;
      this.visible = visible;
      //      System.out.println(labels);
    }

    public void updateLabelLocations(Pane labelsPane) {
      for (Pair<javafx.geometry.Point3D, Node> labelPointPair : labels) {
        Labels.updateLocation(labelsPane, root, labelPointPair.getKey(), labelPointPair.getValue());
      }
    }

    public Node getNode() {
      return root;
    }

    public List<Node> getLabels() {
      return labels.stream().map(Pair::getValue).toList();
    }

    public boolean isVisible() {
      return visible;
    }

    public void setVisible(boolean visible) {
      this.visible = visible;
      root.setVisible(visible);
      labels.forEach(label -> label.getValue().setVisible(visible));
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null || obj.getClass() != this.getClass()) return false;
      var that = (Axes3D) obj;
      return Objects.equals(this.root, that.root)
          && Objects.equals(this.labels, that.labels)
          && this.visible == that.visible;
    }

    @Override
    public int hashCode() {
      return Objects.hash(root, labels, visible);
    }

    @Override
    public String toString() {
      return "Axes3D["
          + "root="
          + root
          + ", "
          + "labels="
          + labels
          + ", "
          + "visible="
          + visible
          + ']';
    }
  }

  public Axes3D build() {
    Group root = new Group();
    List<Pair<javafx.geometry.Point3D, Node>> labels = new ArrayList<>();

    // Grid lines parallel to axes
    double g = numberOfGridLines * gridSize;
    for (int i = -numberOfGridLines; i <= numberOfGridLines; i++) {
      double offset = i * gridSize;
      if (showXAxis) {
        PolyLine3D lineX =
            new PolyLine3D(
                List.of(new Point3D(-g, offset, 0), new Point3D(g, offset, 0)),
                gridLineWidth,
                gridColor,
                lineType);
        root.getChildren().add(lineX);
      }

      if (showYAxis) {
        PolyLine3D lineY =
            new PolyLine3D(
                List.of(new Point3D(offset, -g, 0), new Point3D(offset, g, 0)),
                gridLineWidth,
                gridColor,
                lineType);
        root.getChildren().add(lineY);
      }

      if (showZAxis) {
        if (showXAxis) {
          PolyLine3D lineZ =
              new PolyLine3D(
                  List.of(new Point3D(offset, 0, -g), new Point3D(offset, 0, g)),
                  gridLineWidth,
                  gridColor,
                  lineType);
          PolyLine3D lineX =
              new PolyLine3D(
                  List.of(new Point3D(-g, 0, offset), new Point3D(g, 0, offset)),
                  gridLineWidth * 2,
                  gridColor,
                  lineType);
          root.getChildren().addAll(lineZ, lineX);
        }
        if (showYAxis) {
          PolyLine3D lineZ =
              new PolyLine3D(
                  List.of(new Point3D(0, offset, -g), new Point3D(0, offset, g)),
                  gridLineWidth,
                  gridColor,
                  lineType);
          PolyLine3D lineY =
              new PolyLine3D(
                  List.of(new Point3D(0, -g, offset), new Point3D(0, g, offset)),
                  gridLineWidth,
                  gridColor,
                  lineType);
          root.getChildren().addAll(lineZ, lineY);
        }
      }
    }

    // Axis lines
    if (showXAxis) {
      PolyLine3D xAxis =
          new PolyLine3D(
              List.of(new Point3D(-g, 0, 0), new Point3D(g, 0, 0)),
              axisLineWidth,
              xAxisColor,
              lineType);
      root.getChildren().add(xAxis);
      labels.add(new Pair<>(new javafx.geometry.Point3D(-g, 0, 0), createLabel("-X")));
      labels.add(new Pair<>(new javafx.geometry.Point3D(g, 0, 0), createLabel("+X")));
    }
    if (showYAxis) {
      PolyLine3D yAxis =
          new PolyLine3D(
              List.of(new Point3D(0, -g, 0.0), new Point3D(0, g, 0.0)),
              axisLineWidth,
              yAxisColor,
              lineType);
      root.getChildren().add(yAxis);
      if (labelAxes) {
        labels.add(new Pair<>(new javafx.geometry.Point3D(-0, -g, 0.0), createLabel("-Y")));
        labels.add(new Pair<>(new javafx.geometry.Point3D(0, g, 0.0), createLabel("+Y")));
      }
    }
    if (showZAxis) {
      PolyLine3D zAxis =
          new PolyLine3D(
              List.of(new Point3D(0.0, 0, -g), new Point3D(0.0, 0.0, g)),
              axisLineWidth,
              zAxisColor,
              lineType);
      root.getChildren().add(zAxis);
      if (labelAxes) {
        labels.add(new Pair<>(new javafx.geometry.Point3D(0.0, 0, -g), createLabel("-Z")));
        labels.add(new Pair<>(new javafx.geometry.Point3D(0.0, 0.0, g), createLabel("+Z")));
      }
    }

    return new Axes3D(root, labels, true);
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

  public void setLabelAxes(boolean labelAxes) {
    this.labelAxes = Axes3DBuilder.this.labelAxes;
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
