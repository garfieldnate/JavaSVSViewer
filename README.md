# SVS Viewer

A Java FX 3D-based rewrite of the SVS viewer.

## Progress

### Design

[X] Hello World
[X] Place display logic into an XML pane thingy
[ ] JMetro? https://www.pixelduke.com/java-javafx-theme-jmetro/

### Network

[ ] listen on specified port
[ ] receive SVS commands (POC: just print them out)
[ ] connect/disconnect occur gracefully
[ ] graceful error when port is already in use
[ ] parse commands and send to display module

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
