module edu.umich.soar.svsviewer {
  requires javafx.controls;
  requires javafx.fxml;
  requires jakarta.annotation;
  requires java.desktop;
  requires javafx.swing;

  opens edu.umich.soar.svsviewer to
      javafx.fxml;

  exports edu.umich.soar.svsviewer;
  exports edu.umich.soar.svsviewer.command;

  opens edu.umich.soar.svsviewer.command to
      javafx.fxml;

  exports edu.umich.soar.svsviewer.scene;

  opens edu.umich.soar.svsviewer.scene to
      javafx.fxml;
}
