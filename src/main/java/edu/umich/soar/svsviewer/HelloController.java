package edu.umich.soar.svsviewer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Box;

public class HelloController {
	@FXML
	private Label welcomeText;

	@FXML
	protected void onHelloButtonClick() {
		welcomeText.setText("Welcome to JavaFX Application!");

		Box myBox = new Box(20, 20, 20);


	}
}
