package edu.umich.soar.svsviewer;

public class Help {
  public static final String DOCS =
      """
Welcome to JavaSVSViewer! You can connect a Soar kernel by running this command: `svs viewer_connect 12122`.

Once objects are displayed, you can rotate the scene by clicking and dragging it with your cursor.

Keyboard controls:

* UP/DOWN: zoom in/out
* CTRL or CMD + UP/DOWN: lean camera forward and backward
* LEFT/RIGHT: move camera side-to-side
* CTRL or CMD + LEFT/RIGHT: rotate camera side-to-side
* G: toggle axes display. The grid lines are spaced at intervals of 0.5.
* L: toggle labels display
* M: cycle the drawing mode (fill, line or line + fill)
* E: toggle the messages display
* S: save a screenshot

NOTE: Only scene `S1` is displayed. If you want to show other scenes, ask Nate to implement it :)

If you have suggestions or issues with the software, please open an issue at the GitHub repository: https://github.com/garfieldnate/JavaSVSViewer/issues/new

To learn more about Soar's Spatial Visual System, see the Soar Manual chapter: https://soar.eecs.umich.edu/soar_manual/08_SpatialVisualSystem.
""";
}
