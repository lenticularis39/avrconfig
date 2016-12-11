package avrconfig;

import avrconfig.data.ConfigurationFile;
import avrconfig.error.ErrorHandler;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main extends Application {

    FXMLLoader fxmlLoader;
    String configurationFile;
    @Override
    public void start(Stage primaryStage) throws Exception{
        fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));

        // Configuration file needs to be set up to load before the FXML is loaded
        configurationFile = System.getProperty("user.home") + "/.config/avrconfig/config.xml";
        if(getParameters().getNamed().get("config") != null) {
            System.out.println("Setting configuration file.");
            configurationFile = getParameters().getNamed().get("config");
        }
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("AVRConfig");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {

    }

    public static void main(String[] args) {
        launch(args);
    }
}
