# SVS Viewer

A Java FX 3D-based rewrite of the SVS viewer.

## Terminology

Scene in SVS vs JavaFX
Geometry in SVS (might not be visible, has color, text, etc.)

## Progress

### Design

[X] Hello World
[X] Place display logic into an XML pane thingy
[ ] JMetro? https://www.pixelduke.com/java-javafx-theme-jmetro/

### Network

Real line processor parses and sends results to UI
Test one just saves lines

Perhaps a Java-style pull architecture? UI updater is pulling from command parser, (which is pulling from line
provider?), which is pulling from Server?

[X] listen on specified port
[X] figure out how to update UI in some way

- [X] JavaFX Service (or possibly Task, Worker)
  [X] receive SVS commands (POC: just print them out)
  [ ] NEXT: add wildcard-trie to GeometryManager so we can retrieve scenes/geometries using wildcards. Needs a license
  first: https://github.com/TeodorDyakov/wildcard-trie/pull/3
  [ ] interpret parsed commands
	- [%] update geometry (assuming one scene)
	- [ ] new scene
	- [NEXT] new geometry
	- [ ] delete scenes
	- [ ] delete geometries
- [ ] inject Socket?
- [ ] what is controller actually supposed to do?
- [ ] can I really not specify default port in the FXML? Maybe has to be somewhere else? Is DI not possible? Do I not
  understand FXML well enough?
  [ ] connect/disconnect occur gracefully
  [ ] graceful error when port is already in use

[X] command parser

- [X] ignore comments
- [X] split on space(s)
- [X] `save <path>`
- [X] `layer <num> <0/1> <num> ...?`
- [X] `draw? <scene_pat> <geom_pat> <args...>`
- `<scene_pat>`
- `-` deletes scene
- `+` finds or creates scene
- `<geom_pat>`
- `-` deletes geometry
- `+` finds or creates geometry
- `<args...>`
- `[prscvbtlw]`. see `proc_geom_cmd`.

[ ] testing

- [ ] add an echo mode to server that returns parsed JSON or something
- [ ] instantiate real server and test client
- [ ] turn on echo mode, then send example data to server with test client and expect back proper acknowledgements and
  JSONs
  [ ] log received commands
- [ ] console
- [ ] a Window user can open?

### 3D Display,

[ ] boxes from SVS
[ ] meshes from SVS
[ ] labels from SVS
[ ] grid lines

### List Display

[ ] show all objects
[ ] search objects by name
[ ] change color of selected objects (search for belief-* and make them all red)
[ ] reset color changes

### Controls

[ ] good default camera position
[ ] shortcuts for different angle views
[ ] zoom in/out
[ ] translate camera
[ ] rotate camera
[ ] rotate around point?
[ ] labels on/off
[ ] drawing mode line/solid
[ ] screenshot shortcut
[ ] select which scene (S1, S2, etc.) to display
[ ] select port to listen on, then create server (also allow command line specification)
[ ] select object with mouse or in objects list panel

- [ ] highlight it somehow
- [ ] show orientation with arrows
- [ ] show its info in a separate panel
- [ ] might be able to inspect geometries that have no visualization (only position, rotation, no vertices)

ideas:
[ ] 1,2,3,4,5,6 shortcuts to view from different faces
[ ] maybe 1,2,3,4,5,6,7,8 to view from different corners
[ ] arrow keys move camera to sides
[ ] mouse rotation

- https://www.youtube.com/watch?v=yinIKzg7duc
  [ ] shift mouse translates camera?
  [ ] scroll zooms in and out?
- https://www.youtube.com/watch?v=SiPfsZA_GeI
  [ ] visual camera controls?
- slider for zoom https://www.youtube.com/watch?v=yWsNO9qiYgg
- cube thing for rotation?
- minimap for translation?

### Settings

[ ] save/load settings on user's machine somewhere
[ ] adjust sensitivity of controls (zoom)

## Format

We use [google-java-format](https://github.com/google/google-java-format) to format the source files.

For IntelliJ, install and configure the plugin, and enable "Reformat code" under the "Actions on Save" setting.

## Editing

You may want SceneBuilder in IntelliJ to edit the fxml file:
https://www.jetbrains.com/help/idea/opening-fxml-files-in-javafx-scene-builder.html#download-scene-builder-from-ide

## Building/Running

### IntelliJ

Load in IntelliJ and run SvsViewerApplication.java.

### Standalone JAR

Run the Jlink build, then cd build/jlinkbase/jlinkjars and do:

```shell
java --module-path . --module edu.umich.soar.svsviewer/edu.umich.soar.svsviewer.SvsViewerApplication
```

## License

Released under the [MIT](https://opensource.org/license/mit) license.
