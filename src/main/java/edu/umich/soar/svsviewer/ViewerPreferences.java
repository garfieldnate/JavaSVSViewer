package edu.umich.soar.svsviewer;

import edu.umich.soar.svsviewer.util.DrawingMode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.prefs.Preferences;

public class ViewerPreferences {

  private final BooleanProperty messagesVisible;
  private final BooleanProperty showLabels;

  private final BooleanProperty showAxes;
  //  private
  private final ObjectProperty<DrawingMode> drawingMode;

  public ViewerPreferences() {
    Preferences prefs = Preferences.userNodeForPackage(SceneController.class);

    messagesVisible = new SimpleBooleanProperty(prefs.getBoolean("messagesVisible", true));
    messagesVisible.addListener(
        (obs, wasPreviouslyVisible, isNowVisible) -> {
          prefs.putBoolean("messagesVisible", isNowVisible);
        });

    showLabels = new SimpleBooleanProperty(prefs.getBoolean("showLabels", true));
    showLabels.addListener(
        (obs, wasPreviouslyVisible, isNowVisible) -> {
          prefs.putBoolean("showLabels", isNowVisible);
        });

    showAxes = new SimpleBooleanProperty(prefs.getBoolean("showAxes", true));
    showAxes.addListener(
        (obs, wasPreviouslyVisible, isNowVisible) -> {
          prefs.putBoolean("showAxes", isNowVisible);
        });

    DrawingMode loadedDrawingMode;
    try {
      loadedDrawingMode =
          DrawingMode.valueOf(prefs.get("drawingMode", DrawingMode.FILL_AND_LINE.toString()));
    } catch (IllegalArgumentException e) {
      //      string in preferences was mal-formed, so just use the default
      e.printStackTrace();
      loadedDrawingMode = DrawingMode.FILL_AND_LINE;
    }
    drawingMode = new SimpleObjectProperty<>(loadedDrawingMode);
    drawingMode.addListener(
        (observer, oldValue, newValue) -> prefs.put("drawingMode", newValue.toString()));
  }

  public BooleanProperty messagesVisibleProperty() {
    return messagesVisible;
  }

  public boolean isMessagesVisible() {
    return messagesVisible.get();
  }

  public boolean isShowLabels() {
    return showLabels.get();
  }

  public BooleanProperty showLabelsProperty() {
    return showLabels;
  }

  public DrawingMode getDrawingMode() {
    return drawingMode.get();
  }

  public ObjectProperty<DrawingMode> drawingModeProperty() {
    return drawingMode;
  }

  public void nextDrawingMode() {
    drawingModeProperty().set(getDrawingMode().getNextMode());
  }

  public boolean isShowAxes() {
    return showAxes.get();
  }

  public BooleanProperty showAxesProperty() {
    return showAxes;
  }
}
