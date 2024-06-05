# SVS Viewer

A Java FX 3D-based rewrite of the SVS viewer.

## Progress

### Design

[X] Hello World
[ ] Place display logic into an XML pane thingy
[ ] JMetro? https://www.pixelduke.com/java-javafx-theme-jmetro/

### Network

[ ] listen on specified port
[ ] receive SVS commands (POC: just print them out)
[ ] connect/disconnect occur gracefully
[ ] graceful error when port is already in use
[ ] parse commands and send to display module

### Display

[ ] boxes from SVS
[ ] meshes from SVS
[ ] labels from SVS
[ ] grid lines

### Controls

[ ] good default camera position
[ ] shortcuts for different angle views
[ ] zoom in/out
[ ] translate camera
[ ] rotate camera
[ ] rotate around point?
[ ] labels on/off
[ ] drawing mode line/solid

ideas:
[ ] 1,2,3,4,5,6 shortcuts to view from different faces
[ ] maybe 1,2,3,4,5,6,7,8 to view from different corners
[ ] arrow keys move camera to sides
[ ] mouse translates camera?
[ ] shift mouse rotates camera around point?
[ ] scroll zooms in and out? Or maybe W/S, with A/D rotating the camera?

### Settings

[ ] save/load settings on user's machine somewhere
[ ] adjust sensitivity of controls (zoom)

## Building/Running

### IntelliJ

Load in IntelliJ and run HelloApplication.java.

### Standalone JAR

Run the Jlink build, then cd build/jlinkbase/jlinkjars and do:

```shell
java --module-path . --module edu.umich.soar.svsviewer/edu.umich.soar.svsviewer.HelloApplication
```

