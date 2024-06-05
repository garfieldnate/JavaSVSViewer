package edu.umich.soar.svsviewer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
	public Parent createContent() {

		// Box
		Box testBox = new Box(5, 5, 5);
		testBox.setMaterial(new PhongMaterial());
//		testBox.setDrawMode(DrawMode.LINE);

		// Create and position camera
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.getTransforms().addAll (
			new Rotate(-20, Rotate.Y_AXIS),
			new Rotate(-20, Rotate.X_AXIS),
			new Translate(0, 0, -15));

		// Build the Scene Graph
		Group root = new Group();
		root.getChildren().add(camera);
		root.getChildren().add(testBox);

		// Use a SubScene
		SubScene subScene = new SubScene(root, 1400,800);
		subScene.setFill(Color.ALICEBLUE);
		subScene.setCamera(camera);
		Group group = new Group();
		group.getChildren().add(subScene);
		return group;
	}


	@Override
	public void start(Stage primaryStage) throws IOException {
//		FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
//		Scene scene = new Scene(fxmlLoader.load(), 320, 240);
//		primaryStage.setTitle("Hello!");
//		primaryStage.setScene(scene);
//		primaryStage.show();

		// Box
		Box testBox = new Box(5, 5, 5);
		testBox.setMaterial(new PhongMaterial());
//		testBox.setDrawMode(DrawMode.LINE);

		// Create and position camera
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.getTransforms().addAll (
			new Rotate(-20, Rotate.Y_AXIS),
			new Rotate(-20, Rotate.X_AXIS),
			new Translate(0, 0, -15));

		// Build the Scene Graph
		Group root = new Group();
		root.getChildren().add(camera);
		root.getChildren().add(testBox);

		// Use a SubScene
		SubScene subScene = new SubScene(root, 1400,800);
		subScene.setFill(Color.ALICEBLUE);
		subScene.setCamera(camera);
		Group group = new Group();
		group.getChildren().add(subScene);


		primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			switch (event.getCode()) {
				case W -> testBox.translateZProperty().set(testBox.getTranslateZ() + 10);
				case S -> testBox.translateZProperty().set(testBox.getTranslateZ() - 10);
			}
		});


		primaryStage.setResizable(false);
//		Scene scene = new Scene(createContent());
		Scene scene = new Scene(group);
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch();
	}
}
