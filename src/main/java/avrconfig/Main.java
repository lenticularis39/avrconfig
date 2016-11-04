package avrconfig;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    FXMLLoader fxmlLoader;

    @Override
    public void start(Stage primaryStage) throws Exception{
        fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getClassLoader().getResource("main.fxml"));
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
