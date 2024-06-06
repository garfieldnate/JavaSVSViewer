package edu.umich.soar.svsviewer;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class SceneController {
	@FXML
	private Group contentGroup;
	@FXML
	private SubScene viewerScene;

	@FXML
	public void initialize() {
		// Create box
		Box testBox = new Box(5, 5, 5);
		testBox.setMaterial(new PhongMaterial());

		// Create and position camera
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.getTransforms().addAll(
			new Rotate(-20, Rotate.Y_AXIS),
			new Rotate(-20, Rotate.X_AXIS),
			new Translate(0, 0, -15)
		);

		// Add camera to scene
		viewerScene.setCamera(camera);

		// Add test box to the content group
		contentGroup.getChildren().add(testBox);

		// Handle keyboard events
		viewerScene.setOnKeyPressed(event -> {
			switch (event.getCode()) {
				case W -> camera.translateZProperty().set(camera.getTranslateZ() - 10);
				case S -> camera.translateZProperty().set(camera.getTranslateZ() + 10);
			}
		});
		viewerScene.setFocusTraversable(true);
	}
}
