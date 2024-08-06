package edu.umich.soar.svsviewer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.prefs.Preferences;

public class ViewerPreferences {

  private final BooleanProperty messagesVisible;
  private final BooleanProperty showLabels;

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
          System.out.println("Pref set: " + isNowVisible);
          prefs.putBoolean("showLabels", isNowVisible);
        });
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
}
