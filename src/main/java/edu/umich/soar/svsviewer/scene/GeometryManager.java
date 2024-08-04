package edu.umich.soar.svsviewer.scene;

import edu.umich.soar.svsviewer.SVSViewerEvent;
import edu.umich.soar.svsviewer.command.NameMatcher;
import edu.umich.soar.svsviewer.util.WildcardMap;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all of the 3D objects received over the network. Geometries are in scenes, which
 * correspond to a single Soar state. TODO: SceneController will decide what to actually display.
 */
public class GeometryManager {

  private final WildcardMap<SVSScene> scenes = new WildcardMap<>();

  private final Group contentGroup;
  private final Pane labelPane;

  public GeometryManager(StackPane rootPane, Group contentGroup) {
    this.labelPane = new Pane();
    StackPane.setAlignment(labelPane, Pos.TOP_LEFT);
    labelPane.prefWidthProperty().bind(rootPane.widthProperty());
    labelPane.prefHeightProperty().bind(rootPane.heightProperty());
    labelPane.setStyle("-fx-border-color: red; -fx-border-width: 2;");

    // Add overlayPane on top of whatever else is in rootStackPane
    rootPane.getChildren().add(labelPane);

    this.contentGroup = contentGroup;
  }

  public void createSceneIfNotExists(String sceneName) {
    scenes.computeIfAbsent(
        sceneName,
        (key) -> {
          SVSScene s = new SVSScene(sceneName);
          //    TODO: for now, we are always displaying only the S1 scene. Will probably enable
          // showing different scenes on different tabs or something.
          if (sceneName.equals("S1")) {
            contentGroup.getChildren().add(s.root());
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
                s.root().getChildren().add(geometry.getGroup());
                //                TODO: unsatisfyingly places label at 0,0; should be invisible
                // until the node is
                // updated with a location
                labelPane.getChildren().add(geometry.getLabel());
                return geometry;
              });
    }
  }

  public void updateLabelPositions() {
    System.out.println("Updating label positions...");
    // TODO: should be done only for the updated scene
    scenes
        .values()
        .forEach(
            scene ->
                scene
                    .geometries()
                    .values()
                    .forEach(
                        geometry -> {
                          //        TODO: would be better to center in bounds instead of placing at
                          // (0,0,0)
                          Point2D screenLocation = geometry.getGroup().localToScreen(0, 0, 0);
                          Point2D paneLocation = labelPane.screenToLocal(screenLocation);
                          Node label = geometry.getLabel();
                          System.out.println(
                              geometry.getName()
                                  + " label XY: "
                                  + paneLocation.getX()
                                  + ","
                                  + paneLocation.getY());
                          label.setLayoutX(paneLocation.getX());
                          label.setLayoutY(paneLocation.getY());
                        }));
  }
}
