package edu.umich.soar.svsviewer.util;

public enum DrawingMode {
  FILL_AND_LINE,
  LINE,
  FILL,
  ;

  public DrawingMode getNextMode() {
    int ordinal = (this.ordinal() + 1) % (DrawingMode.values().length);
    return DrawingMode.values()[ordinal];
  }
}
