package edu.umich.soar.svsviewer;

import edu.umich.soar.svsviewer.command.Command;
import edu.umich.soar.svsviewer.scene.GeometryManager;
import edu.umich.soar.svsviewer.parsing.Parser;
import edu.umich.soar.svsviewer.parsing.Tokenizer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SnapshotParameters;
import javafx.scene.SubScene;
import javafx.scene.image.WritableImage;
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

    //    TODO: Just testing code here; this should be triggered by SaveCommand
    WritableImage image = viewerScene.snapshot(new SnapshotParameters(), null);
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

    // Specify the file path and name
    File outputFile = new File("svsViewer.png");
    try {
      ImageIO.write(bufferedImage, "png", outputFile);
    } catch (IOException e) {
      e.printStackTrace();
    }

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
            Platform.runLater(() -> command.interpret(geometryManager));
          }
        };

    server = new Server(12122, inputProcessor);
    Thread th = new Thread(server);
    th.setDaemon(true);
    th.start();
  }
}
