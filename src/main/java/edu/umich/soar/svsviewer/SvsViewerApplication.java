package edu.umich.soar.svsviewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SvsViewerApplication extends Application {

	@Override
	public void start(Stage primaryStage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(SvsViewerApplication.class.getResource("svs-viewer.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		primaryStage.setTitle("SVS Viewer");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}
