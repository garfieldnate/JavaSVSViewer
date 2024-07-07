package edu.umich.soar.svsviewer.geometry;

import edu.umich.soar.svsviewer.command.NameMatcher;
import edu.umich.soar.svsviewer.util.WildcardMap;
import javafx.scene.Group;

import java.util.List;

/**
 * Manages all of the 3D objects received over the network. Geometries are in scenes, which
 * correspond to a single Soar state. TODO: SceneController will decide what to actually display.
 */
public class GeometryManager {
  //  TODO: use these
  private final WildcardMap<Group> scenes = new WildcardMap<>();
  //  private final WildcardTrie<Geometry> geometries = new WildcardTrie<>();

  //  TODO: for now we are just using the one root!
  private final Group root;
  // TODO: for now just using one list
  private final List<Geometry> geometries;

  public GeometryManager(Group root) {
    this.root = root;
    this.geometries = List.of(new Geometry.Builder().build(root));
  }

  public void createSceneIfNotExists(String sceneName) {
    scenes.computeIfAbsent(sceneName, (key) -> new Group());
  }

  public void deleteScene(NameMatcher sceneMatcher) {
    switch (sceneMatcher.matchType()) {
        //      NEXT: implement both of these in WildcardMap
      case EXACT -> scenes.remove(sceneMatcher.namePattern());
      case WILDCARD -> throw new UnsupportedOperationException();
    }
  }

  // delete scene(s)
  // add geometry/ies
  // delete geometry/ies
  // find scene by match exact/wildcard
  // find geometries by scene/geometry matchers
  public List<Geometry> findGeometries(NameMatcher sceneMatcher, NameMatcher geometryMatcher) {
    //    TODO: just using the one geometry for now
    return geometries;
  }
}
