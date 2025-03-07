module edu.umich.soar.svsviewer {
  requires javafx.controls;
  requires javafx.fxml;
  requires jakarta.annotation;
  requires java.desktop;
  requires javafx.swing;
  requires quickhull3d;
  requires org.fxyz3d.core;
  requires java.prefs;

  opens edu.umich.soar.svsviewer to
      javafx.fxml;

  exports edu.umich.soar.svsviewer;
  exports edu.umich.soar.svsviewer.command;
  exports edu.umich.soar.svsviewer.util;

  opens edu.umich.soar.svsviewer.command to
      javafx.fxml;

  exports edu.umich.soar.svsviewer.scene;

  opens edu.umich.soar.svsviewer.scene to
      javafx.fxml;

  exports edu.umich.soar.svsviewer.server;

  opens edu.umich.soar.svsviewer.server to
      javafx.fxml;
}
