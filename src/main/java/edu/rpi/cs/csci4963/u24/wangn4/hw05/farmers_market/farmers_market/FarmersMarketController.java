package edu.rpi.cs.csci4963.u24.wangn4.hw05.farmers_market.farmers_market;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class FarmersMarketController {

    @FXML
    private ListView<String> marketListView;
    @FXML
    private TextField cityField, stateField, zipField;
    @FXML
    private ComboBox<String> distanceComboBox;
    @FXML
    private VBox detailedInfoBox;
    @FXML
    private TextField reviewNameField;
    @FXML
    private TextArea reviewTextField;
    @FXML
    private ComboBox<Integer> reviewRatingComboBox;
    @FXML
    private VBox loadingScreen;

    private final BooleanProperty loading = new SimpleBooleanProperty(false);

    private DatabaseModel databaseModel;

    @FXML
    private void initialize() {
        loadingScreen.visibleProperty().bind(loading);
        loadData();
    }

    private void loadData() {
        loading.set(true);
        new Thread(() -> {
            loadAllMarkets();
            loading.set(false);
        }).start();
    }

    private void loadAllMarkets() {
        databaseModel = new DatabaseModel();
    }

    @FXML
    private void onSearchButtonClick() {
        if(loading.get()) {
            Utils.showAlert("Error", "Please wait for the data to finish loading.");
        }
        String city = cityField.getText();
        String state = stateField.getText();
        String zip = zipField.getText();
        String distance = distanceComboBox.getValue();
    }

    @FXML
    private void onMarketSelected() {
        if(loading.get()) {
            Utils.showAlert("Error", "Please wait for the data to finish loading.");
        }
        String selectedMarket = marketListView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void onSubmitReviewButtonClick() {
        if(loading.get()) {
            Utils.showAlert("Error", "Please wait for the data to finish loading.");
        }
        String name = reviewNameField.getText();
        String reviewText = reviewTextField.getText();
        int rating = reviewRatingComboBox.getValue();
    }
}