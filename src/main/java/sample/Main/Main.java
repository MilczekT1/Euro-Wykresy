package sample.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.General.Configurator;

public final class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/Main Window.fxml"));
        primaryStage.setTitle("Euro Wykresy");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            Configurator.saveCurrentSettings();
            System.exit(0);
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
