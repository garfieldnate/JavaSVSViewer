# SVS Viewer

This is a 3D explorer for [Soar's](https://soar.eecs.umich.edu/) Spatial Visual System.
It is a JavaFX-based rewrite of Joseph Xu's `svs_viewer`. The application is built as an installer
for all major platforms, so clients do not need to worry about Java configuration.

This application was put together rather quickly, and I was learning JavaFX in the process. I've tested
it on am M2 MacOS, but it still needs to be tested on other platforms.

To learn more about Soar's Spatial Visual System, see the Soar Manual chapter
here: https://soar.eecs.umich.edu/soar_manual/08_SpatialVisualSystem/.

## Features

* 3D rendering of SVS scenes
* Mouse-based scene rotation and keyboard-based camera controls
* toggleable drawing modes (fill, line, or both)
* toggleable 3D axes display
* toggleable label display
* screenshot saving

Screenshots are available on the [wiki](https://github.com/garfieldnate/JavaSVSViewer/wiki).

## Installing

* Standalone applications built via jpackage in CI
* For Mac, I notice that the .pkg works but the .dmg does not

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
