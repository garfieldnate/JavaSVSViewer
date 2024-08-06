package edu.umich.soar.svsviewer;

import edu.umich.soar.svsviewer.util.DrawingMode;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class ViewerMenuBar {

  public static void attachMenuBar(Pane parentPane, ViewerPreferences preferences) {
    MenuBar menuBar = new MenuBar();
    final String os = System.getProperty("os.name");
    final boolean isMac = os != null && os.startsWith("Mac");
    if (isMac) {
      // Mac applications use the system menu bar; no placement on parent pane required
      menuBar.useSystemMenuBarProperty().set(true);
    } else {
      // For other OS's, it will show up in the top of the parent pane. Span the width of
      // the window so it looks like a proper menu bar.
      menuBar.prefWidthProperty().bind(parentPane.widthProperty());
      menuBar.setStyle("-fx-font-family: 'Helvetica'; -fx-font-size: 14px; -fx-font-weight: bold");
    }

    Menu viewMenu = new Menu("View");
    Menu drawMenu = new Menu("Draw");
    Menu helpMenu = new Menu("Help");
    menuBar.getMenus().addAll(helpMenu, viewMenu, drawMenu);

    CheckMenuItem showMessagesItem = new CheckMenuItem("Show Messages");
    showMessagesItem.selectedProperty().bindBidirectional(preferences.messagesVisibleProperty());
    viewMenu.getItems().add(showMessagesItem);

    MenuItem tutorialItem = new MenuItem("Tutorial");
    helpMenu.getItems().add(tutorialItem);

    // Add event handler to the about item
    tutorialItem.setOnAction(
        e -> {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("Tutorial");
          alert.setHeaderText(null);

          // Create a TextArea for selectable text
          TextArea textArea = new TextArea(Help.DOCS);
          textArea.setEditable(false); // Make it non-editable
          textArea.setWrapText(true); // Enable text wrapping
          textArea.setMaxWidth(Double.MAX_VALUE); // Use max width for better layout
          textArea.setMaxHeight(Double.MAX_VALUE); // Use max height for better layout
          GridPane.setVgrow(textArea, Priority.ALWAYS);
          GridPane.setHgrow(textArea, Priority.ALWAYS);

          GridPane content = new GridPane();
          content.setMaxWidth(Double.MAX_VALUE);
          content.add(textArea, 0, 0);

          // Set the dialog pane content to the layout containing the TextArea
          DialogPane dialogPane = alert.getDialogPane();
          dialogPane.setContent(content);

          dialogPane.setStyle(
              "-fx-font-family: 'Helvetica'; -fx-font-size: 14px; -fx-font-weight: bold");
          alert.showAndWait();
        });

    ToggleGroup drawingModeToggle = new ToggleGroup();

    RadioMenuItem fillAndLinesItem = new RadioMenuItem("Fill + Lines");
    fillAndLinesItem.setUserData(DrawingMode.FILL_AND_LINE);
    fillAndLinesItem.setToggleGroup(drawingModeToggle);

    RadioMenuItem linesOnlyItem = new RadioMenuItem("Lines only");
    linesOnlyItem.setUserData(DrawingMode.LINE);
    linesOnlyItem.setToggleGroup(drawingModeToggle);

    RadioMenuItem fillOnlyItem = new RadioMenuItem("Fill only");
    fillOnlyItem.setUserData(DrawingMode.FILL);
    fillOnlyItem.setToggleGroup(drawingModeToggle);

    drawMenu.getItems().addAll(fillAndLinesItem, linesOnlyItem, fillOnlyItem);

    // manual bi-directional binding
    drawingModeToggle
        .selectedToggleProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (newValue != null) {
                preferences.drawingModeProperty().set((DrawingMode) newValue.getUserData());
                // Update your application state based on the selected ViewMode
              }
            });
    preferences
        .drawingModeProperty()
        .addListener(
            (obs, oldMode, newMode) -> {
              if (newMode != null) {
                drawingModeToggle
                    .getToggles()
                    .forEach(
                        toggle -> {
                          if (toggle.getUserData() == newMode) {
                            drawingModeToggle.selectToggle(toggle);
                          }
                        });
              }
            });
    // ensure initial menu value matches property value
    drawingModeToggle
        .getToggles()
        .forEach(
            toggle -> {
              if (toggle.getUserData() == preferences.drawingModeProperty().get()) {
                drawingModeToggle.selectToggle(toggle);
              }
            });

    parentPane.getChildren().add(menuBar);
  }
}
