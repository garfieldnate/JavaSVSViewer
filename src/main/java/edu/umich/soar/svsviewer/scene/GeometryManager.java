package edu.umich.soar.svsviewer.scene;

import static edu.umich.soar.svsviewer.Axes3DBuilder.*;

import edu.umich.soar.svsviewer.Axes3DBuilder;
import edu.umich.soar.svsviewer.ViewerPreferences;
import edu.umich.soar.svsviewer.command.NameMatcher;
import edu.umich.soar.svsviewer.util.DrawingMode;
import edu.umich.soar.svsviewer.util.Labels;
import edu.umich.soar.svsviewer.util.WildcardMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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
  private final Consumer<String> showMessage;
  private final Axes3D axes;
  private final ViewerPreferences preferences;
  private final Pane labelsPane;
  private static final String GEO_LABELS_OFF_CLASS = "geo-labels-off";

  public GeometryManager(
      ViewerPreferences preferences,
      Pane rootPane,
      Group geometryRoot,
      Consumer<String> showMessage) {
    this.preferences = preferences;
    this.labelsPane = rootPane;
    //    StackPane.setAlignment(labelsPane, Pos.TOP_LEFT);
    //    labelsPane.prefWidthProperty().bind(rootPane.widthProperty());
    //    labelsPane.prefHeightProperty().bind(rootPane.heightProperty());
    //    labelsPane.setStyle("-fx-border-color: red; -fx-border-width: 2;");

    // Add overlayPane on top of whatever else is in rootStackPane
    //    rootPane.getChildren().add(labelsPane);

    this.geometryRoot = geometryRoot;
    this.showMessage = showMessage;
    axes = new Axes3DBuilder().build();
    axes.setVisible(false);
    geometryRoot.getChildren().add(axes.getNode());
    for (Node label : axes.getLabels()) {
      labelsPane.getChildren().add(label);
    }
    axes.updateLabelLocations(labelsPane);

    axes.setVisible(preferences.isShowAxes());
    preferences
        .showAxesProperty()
        .addListener(
            (_observable, oldVal, newVal) -> {
              axes.setVisible(newVal);
            });

    setLabelVisibility(preferences.isShowLabels());
    preferences
        .showLabelsProperty()
        .addListener(
            (_observable, oldVal, newVal) -> {
              setLabelVisibility(newVal);
            });

    setDrawingMode(preferences.getDrawingMode());
    preferences
        .drawingModeProperty()
        .addListener(
            (observable, oldVal, newVal) -> {
              setDrawingMode(newVal);
            });
  }

  private void setLabelVisibility(boolean visible) {
    if (!visible) {
      labelsPane.getStyleClass().add(GEO_LABELS_OFF_CLASS);
    } else {
      labelsPane.getStyleClass().remove(GEO_LABELS_OFF_CLASS);
    }
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
          showMessage.accept("Created scene " + sceneName);
          return s;
        });
  }

  public void deleteScene(NameMatcher sceneMatcher) {
    Collection<SVSScene> removedScenes;
    switch (sceneMatcher.matchType()) {
      case EXACT -> {
        SVSScene s = scenes.remove(sceneMatcher.namePattern());
        if (s != null) {
          removedScenes = List.of(s);
        } else {
          removedScenes = Collections.emptyList();
        }
      }
      case WILDCARD -> removedScenes = scenes.removeWithWildcards(sceneMatcher.namePattern());
      default ->
          throw new UnsupportedOperationException("Unknown match type " + sceneMatcher.matchType());
    }
    removedScenes.forEach(
        scene -> {
          removeSceneNodes(scene);
          showMessage.accept("Removed scene " + scene.name());
        });
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
          scenesToDeleteFrom.forEach(
              s -> {
                Geometry removedGeometry = s.geometries().remove(geometryMatcher.namePattern());
                if (removedGeometry != null) {
                  removeGeometryNodes(s, removedGeometry);
                  showMessage.accept("Removed geometry " + s.name() + "." + removedGeometry);
                }
              });
      case WILDCARD ->
          scenesToDeleteFrom.forEach(
              s -> {
                Collection<Geometry> removedGeometries =
                    s.geometries().removeWithWildcards(geometryMatcher.namePattern());
                removedGeometries.forEach(
                    g -> {
                      showMessage.accept("Removed geometry " + s.name() + "." + g);
                      removeGeometryNodes(s, g);
                    });
              });
      default ->
          throw new UnsupportedOperationException("Unknown match type " + sceneMatcher.matchType());
    }
  }

  private void removeSceneNodes(SVSScene scene) {
    for (Geometry geo : scene.geometries().values()) {
      removeGeometryNodes(scene, geo);
    }
    geometryRoot.getChildren().remove(scene.root());
  }

  private void removeGeometryNodes(SVSScene scene, Geometry geometry) {
    geometry.modifyGroups(g -> scene.root().getChildren().remove(g));
    labelsPane.getChildren().remove(geometry.getLabel());
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
                geometry.setDrawingMode(preferences.getDrawingMode());

                showMessage.accept("Added geometry " + s.name() + "." + geometry.getName());

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

  private void setDrawingMode(DrawingMode mode) {
    // TODO: just set for active scene
    scenes
        .values()
        .forEach(
            scene ->
                scene.geometries().values().forEach(geometry -> geometry.setDrawingMode(mode)));
  }
}
