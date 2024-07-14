package edu.umich.soar.svsviewer.scene;

import edu.umich.soar.svsviewer.command.NameMatcher;
import edu.umich.soar.svsviewer.util.WildcardMap;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all of the 3D objects received over the network. Geometries are in scenes, which
 * correspond to a single Soar state. TODO: SceneController will decide what to actually display.
 */
public class GeometryManager {

  private static class Scene {
    private final WildcardMap<Group> geometries = new WildcardMap<>();
  }

  private final WildcardMap<Scene> scenes = new WildcardMap<>();

  //  TODO: for now we are just using the one root!
  private final Group root;

  public GeometryManager(Group root) {
    this.root = root;
  }

  public void createSceneIfNotExists(String sceneName) {
    scenes.computeIfAbsent(sceneName, (key) -> new Scene());
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
    List<Scene> scenesToDeleteFrom;
    switch (sceneMatcher.matchType()) {
      case EXACT -> {
        Scene scene = scenes.get(sceneMatcher.namePattern());
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
          scenesToDeleteFrom.forEach(s -> s.geometries.remove(geometryMatcher.namePattern()));
      case WILDCARD ->
          scenesToDeleteFrom.forEach(
              s -> s.geometries.removeWithWildcards(geometryMatcher.namePattern()));
      default ->
          throw new UnsupportedOperationException("Unknown match type " + sceneMatcher.matchType());
    }
  }

  // delete scene(s)
  // add geometry/ies
  // delete geometry/ies
  // find scene by match exact/wildcard
  // find geometries by scene/geometry matchers
  public List<Geometry> findGeometries(NameMatcher sceneMatcher, NameMatcher geometryMatcher) {
    List<Scene> scenesToSearch;
    switch (sceneMatcher.matchType()) {
      case EXACT -> {
        Scene scene = scenes.get(sceneMatcher.namePattern());
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

    List<Group> matchedGeometries = new ArrayList<>();
    switch (geometryMatcher.matchType()) {
      case EXACT ->
          scenesToSearch.forEach(
              s -> {
                Group geometry = s.geometries.get(geometryMatcher.namePattern());
                if (geometry != null) {
                  matchedGeometries.add(s.geometries.get(geometryMatcher.namePattern()));
                }
              });
      case WILDCARD ->
          scenesToSearch.forEach(
              s ->
                  s.geometries
                      .getWithWildcards(geometryMatcher.namePattern())
                      .forEach(
                          entry -> {
                            matchedGeometries.add(entry.value());
                          }));
      default ->
          throw new UnsupportedOperationException("Unknown match type " + sceneMatcher.matchType());
    }

    //    TODO: converting to Geometries here is dumb; should be using them internally, if at all.
    return matchedGeometries.stream().map(Geometry::new).toList();
  }

  public void addGeometry(NameMatcher sceneMatcher, String geometryName) {
    List<Scene> scenesToUpdate;
    switch (sceneMatcher.matchType()) {
      case EXACT -> {
        Scene scene = scenes.get(sceneMatcher.namePattern());
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
    for (Scene s : scenesToUpdate) {
      // TODO: is it correct to create new geometries only if they don't exist already?
      s.geometries.computeIfAbsent(geometryName, n -> new Group());
    }
  }
}
