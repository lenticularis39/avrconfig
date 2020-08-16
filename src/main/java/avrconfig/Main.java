package avrconfig;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    FXMLLoader fxmlLoader;
    String configurationFile;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the configuration file.
        // Note: this has to be done before the FXML is loaded.
        configurationFile = System.getProperty("user.home") + "/.config/avrconfig/config.xml";
        if (getParameters().getNamed().get("config") != null) {
            System.out.println("Setting configuration file.");
            configurationFile = getParameters().getNamed().get("config");
        }

        // Determine AVRConfig version.
        // Note: when running outside JAR file (typically during development), the manifest is not present, therefore
        // the version number is unknown.
        String version = "(development version)";
        if (getClass().getPackage().getImplementationVersion() != null)
            version = getClass().getPackage().getImplementationVersion();

        // Load the FXML and show the main window.
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("AVRConfig " + version);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
