package edu.umich.soar.svsviewer;

import edu.umich.soar.svsviewer.command.Command;
import edu.umich.soar.svsviewer.parsing.Parser;
import edu.umich.soar.svsviewer.parsing.Tokenizer;
import edu.umich.soar.svsviewer.scene.GeometryManager;
import edu.umich.soar.svsviewer.server.Server;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javax.imageio.ImageIO;

public class SceneController {
  @FXML private Pane rootPane;
  @FXML private Group contentGroup;
  @FXML private SubScene viewerScene;

  private Server server;

  private GeometryManager geometryManager;

  double anchorX;
  double anchorY;
  double anchorAngleX;
  double anchorAngleY;

  private final DoubleProperty angleX = new SimpleDoubleProperty(0);
  private final DoubleProperty angleY = new SimpleDoubleProperty(0);

  private static int screenshotCounter = 0;

  private boolean geoLabelsHidden = false;
  private static final String GEO_LABELS_OFF_CLASS = "geo-labels-off";

  private DrawMode drawMode = DrawMode.FILL;

  @FXML
  public void initialize() {
    //    rootPane
    //        .getStylesheets()
    //        .add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
    //    TODO: Either SVS and JavaFX have different ideas of axes, or the camera
    // location is off. For now we just rotate the scene here to make FloorPlan1
    // appear nice by default. Probably need to display axes first to figure out the
    // correct answer.
    contentGroup.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

    // Create and position camera
    PerspectiveCamera camera = new PerspectiveCamera(true);
    camera
        .getTransforms()
        .addAll(
            //            new Rotate(-20, Rotate.Y_AXIS),
            //            new Rotate(-20, Rotate.X_AXIS),
            new Translate(0, 0, -15));

    // Add camera to scene
    viewerScene.setCamera(camera);

    camera
        .boundsInParentProperty()
        .addListener(
            (observable, oldValue, newValue) ->
                Platform.runLater(
                    () ->
                        Event.fireEvent(
                            viewerScene,
                            new SVSViewerEvent(viewerScene, SVSViewerEvent.SCENE_RERENDERED))));

    // Handle keyboard events
    viewerScene.setOnKeyPressed(
        event -> {
          switch (event.getCode()) {
            case UP -> camera.translateZProperty().set(camera.getTranslateZ() - 10);
            case DOWN -> camera.translateZProperty().set(camera.getTranslateZ() + 10);
            case L -> toggleSceneLabels();
            case M -> toggleSceneDrawMode();
            case S -> saveScreenshot();
          }
        });
    viewerScene.setFocusTraversable(true);

    initMouseControls(contentGroup, viewerScene);
    contentGroup
        .boundsInParentProperty()
        .addListener(
            (observable, oldValue, newValue) ->
                Platform.runLater(
                    () ->
                        Event.fireEvent(
                            viewerScene,
                            new SVSViewerEvent(viewerScene, SVSViewerEvent.SCENE_RERENDERED))));

    this.geometryManager = new GeometryManager(rootPane, contentGroup);
    viewerScene.addEventFilter(
        SVSViewerEvent.SCENE_RERENDERED, e -> geometryManager.updateLabelPositions());

    Consumer<String> inputProcessor =
        (String line) -> {
          List<String> tokens = Tokenizer.tokenizeCommand(line);
          if (tokens.isEmpty()) {
            System.err.println("No tokens found in line: " + line);
            return;
          }
          System.out.println("COMMAND: " + line);
          List<Command> parsed;
          try {
            parsed = Parser.parse(tokens);
          } catch (Parser.ParsingException e) {
            // TODO: anything better we can do here?
            System.err.println("Ignoring unparseable line: " + line);
            System.err.println(e.getMessage());
            return;
          }
          for (Command command : parsed) {
            // we're on the server thread, but the UI must be updated on the main thread; runLater()
            // takes care of that
            Platform.runLater(() -> command.interpret(geometryManager, this));
          }
        };

    initServer(inputProcessor);
  }

  private void toggleSceneLabels() {
    geoLabelsHidden = !geoLabelsHidden;
    if (geoLabelsHidden) {
      //                System.out.println("Hiding labels...");
      rootPane.getStyleClass().add(GEO_LABELS_OFF_CLASS);
    } else {
      //                System.out.println("Showing labels");
      rootPane.getStyleClass().remove(GEO_LABELS_OFF_CLASS);
    }
  }

  private void toggleSceneDrawMode() {
    if (drawMode == DrawMode.FILL) {
      drawMode = DrawMode.LINE;
    } else {
      drawMode = DrawMode.FILL;
    }
    geometryManager.setDrawMode(drawMode);
  }

  private void initMouseControls(Group group, SubScene scene) {
    Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
    Rotate yRotate = new Rotate(0, Rotate.Z_AXIS);

    xRotate.angleProperty().bind(angleX);
    yRotate.angleProperty().bind(angleY);

    group.getTransforms().addAll(xRotate, yRotate);

    scene.setOnMousePressed(
        event -> {
          anchorX = event.getSceneX();
          anchorY = event.getSceneY();
          anchorAngleX = angleX.get();
          anchorAngleY = angleY.get();
        });

    scene.setOnMouseDragged(
        event -> {
          angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
          angleY.set(anchorAngleY - (anchorX - event.getSceneX()));
        });
  }

  private void initServer(Consumer<String> inputProcessor) {
    // While the server is not connected, show a simple status/instruction message
    // at the center of the screen
    Text connectionStatusText =
        new Text("Waiting for connection at port 12122.\nConnect from Soar with:");
    connectionStatusText.setFont(new Font("Helvetica", 30));
    connectionStatusText.setTextAlignment(TextAlignment.CENTER);
    connectionStatusText.setLineSpacing(6);
    Text connectionInstructionText = new Text("svs connect_viewer 12122");
    connectionInstructionText.setFont(new Font("Courier New", 30));
    connectionInstructionText.setTextAlignment(TextAlignment.CENTER);

    VBox vbox = new VBox(connectionStatusText, connectionInstructionText);
    vbox.setAlignment(Pos.CENTER);

    rootPane.getChildren().add(vbox);
    // Bind vbox's layoutX and layoutY properties to keep it centered
    vbox.layoutXProperty().bind(rootPane.widthProperty().subtract(vbox.widthProperty()).divide(2));
    vbox.layoutYProperty()
        .bind(rootPane.heightProperty().subtract(vbox.heightProperty()).divide(2));

    server = new Server(12122, inputProcessor);
    server.onConnected(() -> vbox.setVisible(false));
    Thread th = new Thread(server);
    th.setDaemon(true);
    th.start();
  }

  /** Save an image file showing the current viewer scene */
  public void saveScreenshot() {
    File outputFile = getScreenshotFile();
    WritableImage image = rootPane.snapshot(new SnapshotParameters(), null);
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

    try {
      ImageIO.write(bufferedImage, "png", outputFile);
      System.out.println("Saved screenshot to " + outputFile.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * @return svs_screenshot{suffix}.png, where {suffix} is either empty or a number chosen
   *     sequentially to avoid overwriting any existing files.
   */
  private File getScreenshotFile() {
    String suffix;
    if (screenshotCounter == 0) {
      suffix = "";
    } else {
      suffix = Integer.toString(screenshotCounter);
    }
    screenshotCounter++;
    String baseFileName = "svs_screenshot";
    String extension = "png";

    File outputFile;
    do {
      outputFile = new File(baseFileName + suffix + "." + extension);
      screenshotCounter++;
      suffix = Integer.toString(screenshotCounter);
    } while (outputFile.exists());
    return outputFile;
  }
}
