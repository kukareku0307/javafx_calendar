package com.example.kursach;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Calendar.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Календарь + Заметки!");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    public static void main(String[] args) {
        launch();
    }
}