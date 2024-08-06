package edu.umich.soar.svsviewer.util;

import javafx.beans.property.BooleanProperty;

public class PropertyUtils {

  public static void toggleBooleanProperty(BooleanProperty prop) {
    prop.set(!prop.get());
  }
}
