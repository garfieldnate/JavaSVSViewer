package edu.umich.soar.svsviewer;

import edu.umich.soar.svsviewer.command.Command;
import edu.umich.soar.svsviewer.parsing.Parser;
import edu.umich.soar.svsviewer.parsing.Tokenizer;
import edu.umich.soar.svsviewer.scene.GeometryManager;
import edu.umich.soar.svsviewer.server.Server;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.binding.Bindings;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
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

import static edu.umich.soar.svsviewer.util.PropertyUtils.toggleBooleanProperty;

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

  private final Translate cameraTranslation = new Translate(0, 0, -15);

  private final Rotate cameraYaw = new Rotate(0, Rotate.Y_AXIS);
  private final Rotate cameraPitch = new Rotate(0, Rotate.X_AXIS);
  private final Rotate cameraRoll = new Rotate(0, Rotate.Z_AXIS);
  private static final double CAMERA_TRANSLATION_SPEED = 0.1;

  private final VBox messageStack = new VBox();

  private final ViewerPreferences preferences = new ViewerPreferences();

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
            case E -> {
              toggleBooleanProperty(preferences.messagesVisibleProperty());
              showMessage("E: toggle show messages");
            }
            case G -> {
              {
                toggleBooleanProperty(preferences.showAxesProperty());
                showMessage("G: toggle axes");
              }
            }
            case L -> {
              toggleBooleanProperty(preferences.showLabelsProperty());
              showMessage("L: Toggle labels");
            }
            case M -> {
              preferences.nextDrawingMode();
              showMessage("M: Change drawing mode");
            }
            case S -> {
              saveScreenshot();
              showMessage("S: Save screenshot");
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

    this.geometryManager =
        new GeometryManager(preferences, rootPane, shapeGroup, this::showMessage);
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

    ViewerMenuBar.attachMenuBar(rootPane, preferences, this::saveScreenshot);
    initMessageStack(messageStack);
  }

  private void initMessageStack(VBox messageStack) {
    messageStack.setLayoutX(10);
    //    under the menu bar. TODO: should use logic to ensure that, rather than hardcoding a value
    messageStack.setLayoutY(30);
    messageStack.visibleProperty().bindBidirectional(preferences.messagesVisibleProperty());
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
    Text welcomeInstructionsText =
        new Text("Waiting for connection at port 12122.\nConnect from Soar with:");
    welcomeInstructionsText.setFont(new Font("Helvetica", 30));
    welcomeInstructionsText.setTextAlignment(TextAlignment.CENTER);
    welcomeInstructionsText.setLineSpacing(6);
    Text connectionInstructionText = new Text("svs connect_viewer 12122");
    connectionInstructionText.setFont(new Font("Courier New", 30));
    connectionInstructionText.setTextAlignment(TextAlignment.CENTER);

    VBox welcomeInstructionsBox = new VBox(welcomeInstructionsText, connectionInstructionText);
    welcomeInstructionsBox.setAlignment(Pos.CENTER);
    rootPane.getChildren().add(welcomeInstructionsBox);
    // Bind vbox's layoutX and layoutY properties to keep it centered
    welcomeInstructionsBox
        .layoutXProperty()
        .bind(rootPane.widthProperty().subtract(welcomeInstructionsBox.widthProperty()).divide(2));
    welcomeInstructionsBox
        .layoutYProperty()
        .bind(
            rootPane.heightProperty().subtract(welcomeInstructionsBox.heightProperty()).divide(2));

    Label disconnectedWarning =
        new Label("No client connected. Connect from Soar with `svs connect_viewer 12122`.");
    rootPane.getChildren().add(disconnectedWarning);
    disconnectedWarning.getStyleClass().add("disconnectedWarningLabel");
    // Place in lower-left corner
    disconnectedWarning.setLayoutX(0);
    disconnectedWarning
        .layoutYProperty()
        .bind(rootPane.heightProperty().subtract(disconnectedWarning.heightProperty()));

    server = new Server(12122, inputProcessor, this::showMessage);
    server
        .clientConnectedProperty()
        .addListener(
            (observer, oldVal, newVal) -> {
              // hide after the first time the server connects
              if (newVal) {
                Platform.runLater(() -> welcomeInstructionsBox.setVisible(false));
              }
            });
    server
        .clientConnectedProperty()
        .addListener(
            (observer, oldVal, newVal) -> {
              // show when not connected
              Platform.runLater(() -> disconnectedWarning.setVisible(!newVal));
            });
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
      showMessage("Screenshot saved to " + outputFile.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
      showMessage("Failed to save screenshot; see console output.");
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
