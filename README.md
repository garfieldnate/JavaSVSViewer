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

## Pre-built Binaries

See the [latest release](https://github.com/garfieldnate/JavaSVSViewer/releases/latest) for a pre-built binary for your
platform.

* Standalone applications are built via jlink/jpackage in CI
* Open issues on the packaging: https://github.com/garfieldnate/JavaSVSViewer/issues/2

## Development

### Executing from Source

    ./gradlew run

### Format

We use [google-java-format](https://github.com/google/google-java-format) to format the source files.

For IntelliJ, install and configure the plugin, and enable "Reformat code" under the "Actions on Save" setting.

### Editors

The main class to run is `SvsViewerApplication`.

Running in IntelliJ has been broken since adding a non-module dependency. The error is:

    Error occurred during initialization of boot layer
    java.lang.module.FindException: Module quickhull3d not found, required by edu.umich.soar.svsviewer

### Building a Standalone JAR

Run the Jlink build, then cd build/jlinkbase/jlinkjars and do:

```shell
java --module-path . --module edu.umich.soar.svsviewer/edu.umich.soar.svsviewer.SvsViewerApplication
```

## License

Released under the [MIT](https://opensource.org/license/mit) license.
