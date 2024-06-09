module edu.umich.soar.svsviewer {
  requires javafx.controls;
  requires javafx.fxml;
  requires jakarta.annotation;

  opens edu.umich.soar.svsviewer to
      javafx.fxml;

  exports edu.umich.soar.svsviewer;
  exports edu.umich.soar.svsviewer.command;

  opens edu.umich.soar.svsviewer.command to
      javafx.fxml;

  exports edu.umich.soar.svsviewer.geometry;

  opens edu.umich.soar.svsviewer.geometry to
      javafx.fxml;
}
