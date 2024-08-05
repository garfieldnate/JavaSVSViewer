package edu.umich.soar.svsviewer.scene;

import static edu.umich.soar.svsviewer.Axes3DBuilder.*;

import edu.umich.soar.svsviewer.Axes3DBuilder;
import edu.umich.soar.svsviewer.command.NameMatcher;
import edu.umich.soar.svsviewer.util.DrawingMode;
import edu.umich.soar.svsviewer.util.Labels;
import edu.umich.soar.svsviewer.util.WildcardMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Manages all of the 3D objects received over the network. Geometries are in scenes, which
 * correspond to a single Soar state. TODO: SceneController will decide what to actually display.
 */
public class GeometryManager {

  private final WildcardMap<SVSScene> scenes = new WildcardMap<>();

  private final Group geometryRoot;
  private final Axes3D axes;
  private final Pane labelsPane;
  private DrawingMode drawingMode = DrawingMode.FILL_AND_LINE;

  public GeometryManager(Pane rootPane, Group geometryRoot) {
    this.labelsPane = rootPane;
    //    StackPane.setAlignment(labelsPane, Pos.TOP_LEFT);
    //    labelsPane.prefWidthProperty().bind(rootPane.widthProperty());
    //    labelsPane.prefHeightProperty().bind(rootPane.heightProperty());
    //    labelsPane.setStyle("-fx-border-color: red; -fx-border-width: 2;");

    // Add overlayPane on top of whatever else is in rootStackPane
    //    rootPane.getChildren().add(labelsPane);

    this.geometryRoot = geometryRoot;
    axes = new Axes3DBuilder().build();
    axes.setVisible(false);
    geometryRoot.getChildren().add(axes.getNode());
    for (Node label : axes.getLabels()) {
      labelsPane.getChildren().add(label);
    }
    axes.updateLabelLocations(labelsPane);
  }

  public void createSceneIfNotExists(String sceneName) {
    scenes.computeIfAbsent(
        sceneName,
        (key) -> {
          SVSScene s = new SVSScene(sceneName);
          //    TODO: for now, we are always displaying only the S1 scene. Will probably enable
          // showing different scenes on different tabs or something.
          if (sceneName.equals("S1")) {
            geometryRoot.getChildren().add(s.root());
          }
          return s;
        });
  }

  public void deleteScene(NameMatcher sceneMatcher) {
    switch (sceneMatcher.matchType()) {
      case EXACT -> scenes.remove(sceneMatcher.namePattern());
      case WILDCARD -> scenes.removeWithWildcards(sceneMatcher.namePattern());
      default ->
          throw new UnsupportedOperationException("Unknown match type " + sceneMatcher.matchType());
    }
  }

  public void deleteGeometry(NameMatcher sceneMatcher, NameMatcher geometryMatcher) {
    List<SVSScene> scenesToDeleteFrom;
    switch (sceneMatcher.matchType()) {
      case EXACT -> {
        SVSScene scene = scenes.get(sceneMatcher.namePattern());
        if (scene != null) {
          scenesToDeleteFrom = Collections.singletonList(scene);
        } else {
          // no scenes matched, so we can't delete geometries anywhere
          return;
        }
      }
      case WILDCARD ->
          scenesToDeleteFrom =
              scenes.getWithWildcards(sceneMatcher.namePattern()).stream()
                  .map(WildcardMap.Entry::value)
                  .toList();
      default ->
          throw new UnsupportedOperationException("Unknown match type " + sceneMatcher.matchType());
    }

    switch (geometryMatcher.matchType()) {
      case EXACT ->
          scenesToDeleteFrom.forEach(s -> s.geometries().remove(geometryMatcher.namePattern()));
      case WILDCARD ->
          scenesToDeleteFrom.forEach(
              s -> s.geometries().removeWithWildcards(geometryMatcher.namePattern()));
      default ->
          throw new UnsupportedOperationException("Unknown match type " + sceneMatcher.matchType());
    }
    //    TODO: remove the labels for the deleted geometries
  }

  // delete scene(s)
  // add geometry/ies
  // delete geometry/ies
  // find scene by match exact/wildcard
  // find geometries by scene/geometry matchers
  public List<Geometry> findGeometries(NameMatcher sceneMatcher, NameMatcher geometryMatcher) {
    List<SVSScene> scenesToSearch;
    switch (sceneMatcher.matchType()) {
      case EXACT -> {
        SVSScene scene = scenes.get(sceneMatcher.namePattern());
        if (scene != null) {
          scenesToSearch = Collections.singletonList(scene);
        } else {
          // no scenes matched
          return Collections.emptyList();
        }
      }
      case WILDCARD ->
          scenesToSearch =
              scenes.getWithWildcards(sceneMatcher.namePattern()).stream()
                  .map(WildcardMap.Entry::value)
                  .toList();
      default ->
          throw new UnsupportedOperationException("Unknown match type " + sceneMatcher.matchType());
    }

    List<Geometry> matchedGeometries = new ArrayList<>();
    switch (geometryMatcher.matchType()) {
      case EXACT ->
          scenesToSearch.forEach(
              s -> {
                Geometry geometry = s.geometries().get(geometryMatcher.namePattern());
                if (geometry != null) {
                  matchedGeometries.add(s.geometries().get(geometryMatcher.namePattern()));
                }
              });
      case WILDCARD ->
          scenesToSearch.forEach(
              s ->
                  s.geometries()
                      .getWithWildcards(geometryMatcher.namePattern())
                      .forEach(
                          entry -> {
                            matchedGeometries.add(entry.value());
                          }));
      default ->
          throw new UnsupportedOperationException("Unknown match type " + sceneMatcher.matchType());
    }

    return matchedGeometries;
  }

  public void addGeometry(NameMatcher sceneMatcher, String geometryName) {
    List<SVSScene> scenesToUpdate;
    switch (sceneMatcher.matchType()) {
      case EXACT -> {
        SVSScene scene = scenes.get(sceneMatcher.namePattern());
        if (scene != null) {
          scenesToUpdate = Collections.singletonList(scene);
        } else {
          // no scenes matched
          return;
        }
      }
      case WILDCARD ->
          scenesToUpdate =
              scenes.getWithWildcards(sceneMatcher.namePattern()).stream()
                  .map(WildcardMap.Entry::value)
                  .toList();
      default ->
          throw new UnsupportedOperationException("Unknown match type " + sceneMatcher.matchType());
    }
    for (SVSScene s : scenesToUpdate) {
      // TODO: is it correct to create new geometries only if they don't exist already?
      s.geometries()
          .computeIfAbsent(
              geometryName,
              n -> {
                Geometry geometry = new Geometry(geometryName, s);
                geometry.modifyGroups(node -> s.root().getChildren().add(node));
                // TODO: unsatisfyingly places label at 0,0; should be invisible until the node is
                // updated with a location
                labelsPane.getChildren().add(geometry.getLabel());
                geometry.setDrawingMode(drawingMode);

                return geometry;
              });
    }
  }

  public void updateLabelPositions() {
    //    System.out.println("Updating label positions...");
    // TODO: should be done only for the updated scene
    scenes
        .values()
        .forEach(
            scene ->
                scene
                    .geometries()
                    .values()
                    .forEach(
                        geometry ->
                            Labels.updateLocation(
                                labelsPane, geometry.getGroup(), geometry.getLabel())));

    axes.updateLabelLocations(labelsPane);
  }

  public void nextDrawingMode() {
    setDrawingMode(drawingMode.getNextMode());
  }

  void setDrawingMode(DrawingMode mode) {
    this.drawingMode = mode;
    // TODO: just set for one scene
    scenes
        .values()
        .forEach(
            scene ->
                scene.geometries().values().forEach(geometry -> geometry.setDrawingMode(mode)));
  }

  public void toggleAxesVisibility() {
    axes.setVisible(!axes.isVisible());
  }
}
