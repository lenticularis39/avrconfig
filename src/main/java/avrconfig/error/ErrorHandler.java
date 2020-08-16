package avrconfig.error;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import java.io.PrintWriter;
import java.io.StringWriter;

public interface ErrorHandler {
    static void alert(String header, String text) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("AVRConfig");
        a.setHeaderText(header);
        a.setContentText(text);
        a.getDialogPane().getChildren().stream()
                                       .filter(node -> node instanceof Label)
                                       .forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
        a.showAndWait();
    }

    static void info(String header, String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("AVRConfig");
        a.setHeaderText(header);
        a.setContentText(text);
        a.getDialogPane().getChildren().stream()
                                       .filter(node -> node instanceof Label)
                                       .forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
        a.showAndWait();
    }

    static void alertBug(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        alert("An unexpected error has occured.",
                "Please contact the developer.\nError message:\n" + errors.toString());
    }
}
