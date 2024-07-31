package edu.umich.soar.svsviewer;

import edu.umich.soar.svsviewer.command.Command;
import edu.umich.soar.svsviewer.scene.GeometryManager;
import edu.umich.soar.svsviewer.parsing.Parser;
import edu.umich.soar.svsviewer.parsing.Tokenizer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class SceneController {
  @FXML private Group contentGroup;
  @FXML private SubScene viewerScene;

  private Server server;

  double anchorX;
  double anchorY;
  double anchorAngle;

  private static int screenshotCounter = 0;

  @FXML
  public void initialize() {

    // Create and position camera
    PerspectiveCamera camera = new PerspectiveCamera(true);
    camera
        .getTransforms()
        .addAll(
            new Rotate(-20, Rotate.Y_AXIS),
            new Rotate(-20, Rotate.X_AXIS),
            new Translate(0, 0, -15));

    // Add camera to scene
    viewerScene.setCamera(camera);

    // Handle keyboard events
    viewerScene.setOnKeyPressed(
        event -> {
          switch (event.getCode()) {
            case W -> camera.translateZProperty().set(camera.getTranslateZ() - 10);
            case S -> camera.translateZProperty().set(camera.getTranslateZ() + 10);
          }
        });
    viewerScene.setFocusTraversable(true);

    contentGroup.setOnMousePressed(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngle = contentGroup.getRotate();
          }
        });

    contentGroup.setOnMouseDragged(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            contentGroup.setRotate(anchorAngle + anchorX - event.getSceneX());
          }
        });

    GeometryManager geometryManager = new GeometryManager(contentGroup);
    Consumer<String> inputProcessor =
        (String line) -> {
          System.out.println(line);
          List<String> tokens = Tokenizer.tokenizeCommand(line);
          if (tokens.isEmpty()) {
            return;
          }
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

    server = new Server(12122, inputProcessor);
    Thread th = new Thread(server);
    th.setDaemon(true);
    th.start();
  }

  /** Save an image file showing the current viewer scene */
  public void saveScreenshot() {
    File outputFile = getScreenshotFile();
    WritableImage image = viewerScene.snapshot(new SnapshotParameters(), null);
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

    try {
      ImageIO.write(bufferedImage, "png", outputFile);
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
      outputFile = new File(baseFileName + suffix + extension);
      screenshotCounter++;
      suffix = Integer.toString(screenshotCounter);
    } while (outputFile.exists());
    return outputFile;
  }
}
