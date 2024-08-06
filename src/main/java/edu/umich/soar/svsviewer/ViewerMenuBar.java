package edu.umich.soar.svsviewer;

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

    CheckMenuItem showMessagesItem = new CheckMenuItem("Show Messages");
    showMessagesItem.selectedProperty().bindBidirectional(preferences.messagesVisibleProperty());
    Menu viewMenu = new Menu("View");
    viewMenu.getItems().add(showMessagesItem);
    menuBar.getMenus().add(viewMenu);

    Menu helpMenu = new Menu("Help");
    MenuItem tutorialItem = new MenuItem("Tutorial");
    helpMenu.getItems().add(tutorialItem);
    menuBar.getMenus().add(helpMenu);

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

    parentPane.getChildren().add(menuBar);
  }
}
