package edu.umich.soar.svsviewer;

import edu.umich.soar.svsviewer.command.Command;
import edu.umich.soar.svsviewer.parsing.Parser;
import edu.umich.soar.svsviewer.parsing.Tokenizer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

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
            Platform.runLater(() -> command.interpret(contentGroup));
            command.interpret(contentGroup);
          }
        };

    server = new Server(12122, inputProcessor);
    Thread th = new Thread(server);
    th.setDaemon(true);
    th.start();
  }
}
