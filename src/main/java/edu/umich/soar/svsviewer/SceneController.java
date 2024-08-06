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

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import javax.imageio.ImageIO;

public class SceneController {
  public static final int MAX_MESSAGES = 20;
  @FXML private Pane rootPane;
  @FXML private Group rootGroup;
  @FXML private Group shapeGroup;
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

  private final Translate cameraTranslation = new Translate(0, 0, -15);

  private final Rotate cameraYaw = new Rotate(0, Rotate.Y_AXIS);
  private final Rotate cameraPitch = new Rotate(0, Rotate.X_AXIS);
  private final Rotate cameraRoll = new Rotate(0, Rotate.Z_AXIS);
  private static final double CAMERA_TRANSLATION_SPEED = 0.1;

  private final VBox messageStack = new VBox();

  @FXML
  public void initialize() {
    //    TODO: This just makes our THOR-Soar setup look nice immediately because we use Z as
    // "up" in 3D space (towards the ceiling)
    rootGroup.getTransforms().add(new Rotate(180, Rotate.X_AXIS));

    // Create and position camera
    PerspectiveCamera camera = new PerspectiveCamera(true);
    camera.getTransforms().addAll(cameraTranslation, cameraYaw, cameraPitch, cameraRoll);

    // Add camera to scene
    viewerScene.setCamera(camera);

    for (Transform t : List.of(cameraYaw, cameraPitch, cameraRoll, cameraTranslation)) {
      t.setOnTransformChanged(
          value ->
              Platform.runLater(
                  () ->
                      Event.fireEvent(
                          viewerScene,
                          new SVSViewerEvent(viewerScene, SVSViewerEvent.SCENE_RERENDERED))));
    }

    // Handle keyboard events
    viewerScene.setOnKeyPressed(
        event -> {
          // Check for Cmd on macOS or Ctrl on other platforms
          boolean isCmdOrCtrlDown = event.isMetaDown() || event.isControlDown();
          switch (event.getCode()) {
            case UP -> {
              if (isCmdOrCtrlDown) {
                cameraPitch.setAngle(cameraPitch.getAngle() + 2);
                showMessage("Cmd/Ctrl+↑: Lean camera forward");
              } else {
                zoomCamera(CAMERA_TRANSLATION_SPEED);
                showMessage("↑: Zoom in");
              }
            }
            case DOWN -> {
              if (isCmdOrCtrlDown) {
                showMessage("Cmd/Ctrl+↑: Lean camera backward");
                cameraPitch.setAngle(cameraPitch.getAngle() - 2);
              } else {
                zoomCamera(-CAMERA_TRANSLATION_SPEED);
                showMessage("↑: Zoom out");
              }
            }
            case LEFT -> {
              if (isCmdOrCtrlDown) {
                cameraYaw.setAngle(cameraYaw.getAngle() - 2); // Rotate left
                showMessage("Cmd/Ctrl+←: Turn camera left");
              }
              // TODO: Not supporting roll for now because a rolled camera has unintuitive yaw/pitch
              // controls. Need more complex calculations for yaw/pitch to support roll.
              //              else if (event.isShiftDown()) {
              //                cameraRoll.setAngle(cameraRoll.getAngle() + 2);
              //              }
              else {
                cameraTranslation.setX(cameraTranslation.getX() - CAMERA_TRANSLATION_SPEED);
                showMessage("Cmd/Ctrl+←: Move camera left");
              }
            }
            case RIGHT -> {
              if (isCmdOrCtrlDown) {
                cameraYaw.setAngle(cameraYaw.getAngle() + 2); // Rotate right
                showMessage("Cmd/Ctrl+→: Turn camera right");
              }
              //              else if (event.isShiftDown()) {
              //                cameraRoll.setAngle(cameraRoll.getAngle() - 2);
              //              }
              else {
                cameraTranslation.setX(cameraTranslation.getX() + CAMERA_TRANSLATION_SPEED);
                showMessage("Cmd/Ctrl+→: Move camera right");
              }
            }
            case L -> {
              toggleSceneLabels();
              showMessage("L: Toggle labels");
            }
            case M -> {
              geometryManager.nextDrawingMode();
              showMessage("M: Change drawing mode");
            }
            case S -> {
              String outfile = saveScreenshot();
              showMessage("S: Save screenshot");
              if (outfile != null) {
                showMessage("Screenshot saved to " + outfile);
              } else {
                showMessage("Failed to save screenshot; see console output.");
              }
            }
            case G -> {
              {
                geometryManager.toggleAxesVisibility();
                showMessage("G: toggle axes");
              }
            }
          }
        });
    viewerScene.setFocusTraversable(true);

    initMouseControls(shapeGroup, viewerScene);
    rootGroup
        .boundsInParentProperty()
        .addListener(
            (observable, oldValue, newValue) ->
                Platform.runLater(
                    () ->
                        Event.fireEvent(
                            viewerScene,
                            new SVSViewerEvent(viewerScene, SVSViewerEvent.SCENE_RERENDERED))));

    this.geometryManager = new GeometryManager(rootPane, shapeGroup, this::showMessage);
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

    //    TODO: factor out or put in constants or something
    AmbientLight globalAmbientLight = new AmbientLight(Color.color(.4, .4, .4));
    rootGroup.getChildren().add(globalAmbientLight);

    PointLight pointLight1 = new PointLight(Color.color(.4, .4, .4));
    pointLight1.setTranslateX(100); // Position the light source
    pointLight1.setTranslateY(100);
    pointLight1.setTranslateZ(100);
    rootGroup.getChildren().add(pointLight1);

    initMenu(rootPane);
    initMessageStack(messageStack);
  }

  private void initMessageStack(VBox messageStack) {
    messageStack.setLayoutX(10);
    messageStack.setLayoutY(10);
    rootPane.getChildren().add(messageStack);
  }

  // Call this method to add messages
  public void showMessage(String text) {
    addMessage(text, Duration.seconds(5)); // Show each message for 5 seconds
  }

  public void addMessage(String messageText, Duration duration) {
    Text message = new Text(messageText);
    message.getStyleClass().add("message"); // Define this class in your CSS

    if (messageStack.getChildren().size() > MAX_MESSAGES) {
      messageStack.getChildren().removeLast();
    }
    messageStack.getChildren().add(0, message); // Add to the top of the stack

    PauseTransition delay = new PauseTransition(duration);
    delay.setOnFinished(event -> messageStack.getChildren().remove(message));
    delay.play();
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
          showMessage("Mouse drag: rotate scene");
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

    server = new Server(12122, inputProcessor, this::showMessage);
    server.onConnected(() -> vbox.setVisible(false));
    Thread th = new Thread(server);
    th.setDaemon(true);
    th.start();
  }

  private void initMenu(Pane parentPane) {
    //    Everything below just to: show a menu bar with Help->Tutorial that shows a dialog with
    // usage instructions
    MenuBar menuBar = new MenuBar();
    final String os = System.getProperty("os.name");
    if (os != null && os.startsWith("Mac")) {
      // Mac applications use the system menu bar; no placement on parent pane required
      menuBar.useSystemMenuBarProperty().set(true);
    } else {
      // For other OS's, it will show up in the top of the parent pane. Span the width of
      // the window so it looks like a proper menu bar.
      menuBar.prefWidthProperty().bind(rootPane.widthProperty());
      menuBar.setStyle("-fx-font-family: 'Helvetica'; -fx-font-size: 14px; -fx-font-weight: bold");
    }
    Menu helpMenu = new Menu("Help");
    MenuItem tutorialItem = new MenuItem("Tutorial");
    helpMenu.getItems().add(tutorialItem);
    menuBar.getMenus().add(helpMenu);

    // Add event handler to the about item
    tutorialItem.setOnAction(
        e -> {
          Alert alert = new Alert(AlertType.INFORMATION);
          alert.setTitle("Tutorial");
          alert.setHeaderText(null);

          // Create a TextArea for selectable text
          TextArea textArea = new TextArea(Help.DOCS);
          textArea.setEditable(false); // Make it non-editable
          textArea.setWrapText(true); // Enable text wrapping
          textArea.setMaxWidth(Double.MAX_VALUE); // Use max width for better layout
          textArea.setMaxHeight(Double.MAX_VALUE); // Use max height for better layout
          GridPane.setVgrow(textArea, Priority.ALWAYS);
          GridPane.setHgrow(textArea, Priority.ALWAYS);

          GridPane content = new GridPane();
          content.setMaxWidth(Double.MAX_VALUE);
          content.add(textArea, 0, 0);

          // Set the dialog pane content to the layout containing the TextArea
          DialogPane dialogPane = alert.getDialogPane();
          dialogPane.setContent(content);

          dialogPane.setStyle(
              "-fx-font-family: 'Helvetica'; -fx-font-size: 14px; -fx-font-weight: bold");
          alert.showAndWait();
        });
    parentPane.getChildren().add(menuBar);
  }

  /** Save an image file showing the current viewer scene */
  public String saveScreenshot() {
    File outputFile = getScreenshotFile();
    WritableImage image = rootPane.snapshot(new SnapshotParameters(), null);
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

    try {
      ImageIO.write(bufferedImage, "png", outputFile);
      System.out.println("Saved screenshot to " + outputFile.getAbsolutePath());
      return outputFile.getAbsolutePath();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
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

  private void zoomCamera(double speed) {
    // Calculate the direction vector
    double dx = Math.sin(Math.toRadians(cameraYaw.getAngle()));
    double dy = -Math.sin(Math.toRadians(cameraPitch.getAngle()));
    double dz =
        Math.cos(Math.toRadians(cameraYaw.getAngle()))
            * Math.cos(Math.toRadians(cameraPitch.getAngle()));

    // Normalize the direction vector
    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
    dx /= length;
    dy /= length;
    dz /= length;

    // Calculate the new position
    double newX = cameraTranslation.getX() + dx * speed;
    double newY = cameraTranslation.getY() + dy * speed;
    double newZ = cameraTranslation.getZ() + dz * speed;

    cameraTranslation.setX(newX);
    cameraTranslation.setY(newY);
    cameraTranslation.setZ(newZ);
  }
}
