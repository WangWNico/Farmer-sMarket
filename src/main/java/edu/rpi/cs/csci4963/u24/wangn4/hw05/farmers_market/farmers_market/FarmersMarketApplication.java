package edu.rpi.cs.csci4963.u24.wangn4.hw05.farmers_market.farmers_market;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The FarmersMarketApplication class is the main entry point for the JavaFX application.
 * It sets up the primary stage and loads the initial FXML layout.
 */
public class FarmersMarketApplication extends Application {

    /**
     * Starts the JavaFX application.
     * Loads the FXML layout and sets up the primary stage.
     *
     * @param stage The primary stage for this application.
     * @throws IOException If an error occurs during loading the FXML file.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FarmersMarketApplication.class.getResource("farmers-market-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 800);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main method is the entry point of the application.
     * It launches the JavaFX application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch();
    }
}