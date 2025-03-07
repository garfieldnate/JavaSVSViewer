## Bugs

- on Mac: click on menu, don't select anything and click on pane. Registers as a mouse drag and rotates the scene!

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
  [X] add wildcard-trie to GeometryManager so we can retrieve scenes/geometries using wildcards.
  [ ] interpret parsed commands
	- [X] update geometry
		- [X] spike: show a correct box
	- [ ] layer
	- [X] new scene
	- [X] new geometry
	- [X] delete scenes
	- [X] delete geometries
	- [X] save
- [ ] inject Socket?
- [ ] what is controller actually supposed to do?
- [ ] can I really not specify default port in the FXML? Maybe has to be somewhere else? Is DI not possible? Do I not
  understand FXML well enough?
  [X] connect/disconnect occur gracefully
  [ ] graceful error when port is already in use

[ ] testing

- [ ] add an echo mode to server that returns parsed JSON or something
- [ ] instantiate real server and test client
- [ ] turn on echo mode, then send example data to server with test client and expect back proper acknowledgements and
  JSONs
  [ ] log received commands
- [ ] console
- [ ] a Window user can open?
- [ ] https://github.com/TestFX/TestFX

### 3D Display

[X] meshes from SVS
[X] spheres from SVS
[X] labels from SVS
[%] grid lines

- change grid size automatically when shapeGroup size changes

### List Display

[ ] show all objects
[ ] search objects by name
[ ] change color of selected objects (search for belief-* and make them all red)
[ ] reset color changes

### Controls

[ ] good default camera position
[ ] shortcuts for different angle views
[X] zoom in/out
[X] translate camera
[X] rotate camera
[ ] rotate around point?
[X] labels on/off
[X] drawing mode line/solid
[X] screenshot shortcut
[ ] select which scene (S1, S2, etc.) to display
[ ] select port to listen on, then create server (also allow command line specification)
[ ] shift+mouse-drag horizontal to rotate around z-axis
[ ] select object with mouse or in objects list panel

- [ ] highlight it somehow
- [ ] show orientation with arrows
- [ ] show its info in a separate panel
- [ ] might be able to inspect geometries that have no visualization (only position, rotation, no vertices)
- [ ] double-click or something to focus camera on object?
- [ ] notify if object not currently visible (completely obstructed or surrounded)?
  ideas:
- [ ] change camera to FPV from object? (would probably suffice to show viewing direction of robot)

[ ] 1,2,3,4,5,6 shortcuts to view from different faces
[ ] maybe shift 1,2,3,4,5,6,7,8 to view from different corners
[ ] arrow keys move camera to sides
[X] mouse rotation
[ ] regex to show/hide certain labels or objects

[ ] visual camera controls?

- slider for zoom https://www.youtube.com/watch?v=yWsNO9qiYgg
- cube thing for rotation?
- minimap for translation?

other:

- [ ] change package name. I am not associated with UMich.
- [ ] rename to SVSViewerFX (for standalone apps, noone needs to know it's Java!)
- [ ] send a PR to improve JavaFX javadocs
	- TriangleMesh
		- needs to explain texture coordinates
		- needs an example for point, texture, and normal data setting
		- needs to explain when to use normals and when not to
		- validation of faces array needs to be more specific
		- fix typos
		- add getPointElementSize(), getTexCoordElementSize(), etc. in warnings where printed literally
	- Rotate
		- does the rotation axis need to be normalized?

### Settings

[X] save/load settings on user's machine somewhere
[ ] adjust sensitivity of controls (zoom)
