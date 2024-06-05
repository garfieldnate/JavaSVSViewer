module edu.umich.soar.svsviewer {
	requires javafx.controls;
	requires javafx.fxml;


	opens edu.umich.soar.svsviewer to javafx.fxml;
	exports edu.umich.soar.svsviewer;
}
