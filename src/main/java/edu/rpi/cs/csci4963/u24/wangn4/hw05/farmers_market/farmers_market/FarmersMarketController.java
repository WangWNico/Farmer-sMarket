package edu.rpi.cs.csci4963.u24.wangn4.hw05.farmers_market.farmers_market;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FarmersMarketController {

    @FXML
    private ListView<String> marketListView;
    @FXML
    public TextField locationField;
    @FXML
    private ComboBox<String> distanceComboBox;
    @FXML
    private TextField reviewNameField;
    @FXML
    private TextArea reviewTextField;
    @FXML
    private ComboBox<Integer> reviewRatingComboBox;
    @FXML
    private VBox loadingScreen;
    @FXML
    private Label marketDetailsLabel;
    @FXML
    private ListView<String> productListView;

    private final BooleanProperty loading = new SimpleBooleanProperty(false);

    private DatabaseModel databaseModel;

    private String selectedMarket;

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        loadingScreen.visibleProperty().bind(loading);
        loadData();
    }

    /**
     * Loads all market data in a separate thread and updates the loading state.
     */
    private void loadData() {
        loading.set(true);
        new Thread(() -> {
            loadAllMarkets();
            loading.set(false);
        }).start();
    }

    /**
     * Initializes the DatabaseModel and loads all markets.
     */
    private void loadAllMarkets() {
        databaseModel = new DatabaseModel();
    }

    /**
     * Handles the search button click event. Validates the loading state and
     * initiates a market search based on the location field input.
     */
    @FXML
    private void onSearchButtonClick() {
        if(loading.get()) {
            Utils.showAlert("Error", "Please wait for the data to finish loading.");
            return;
        }
        String location = locationField.getText();
        String distance = distanceComboBox.getValue();
        searchMarkets(location);
    }

    /**
     * Searches for markets based on the provided query string in a separate thread.
     *
     * @param query The query string to search for.
     */
    private void searchMarkets(String query) {
        new Thread(() -> {
            try {
                ResultSet resultSet = databaseModel.searchMarkets(query);
                marketListView.getItems().clear();
                while (resultSet.next()) {
                    marketListView.getItems().add(resultSet.getString("MarketName"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Utils.showAlert("Error", "Failed to search markets.");
            }
        }).start();
    }

    /**
     * Handles the market selection event. Validates the loading state and
     * loads the details of the selected market.
     */
    @FXML
    private void onMarketSelected() {
        if (loading.get()) {
            Utils.showAlert("Error", "Please wait for the data to finish loading.");
            return;
        }
        selectedMarket = marketListView.getSelectionModel().getSelectedItem();
        if (selectedMarket != null) {
            loadMarketDetails(selectedMarket);
        }
    }

    /**
     * Loads the details of the specified market in a separate thread and updates the UI.
     *
     * @param marketName The name of the market to load details for.
     */
    private void loadMarketDetails(String marketName) {
        new Thread(() -> {
            try {
                ResultSet resultSet = databaseModel.getMarketDetails(marketName);
                if (resultSet.next()) {
                    int fmid = resultSet.getInt("FMID");
                    StringBuilder details = new StringBuilder();
                    details.append("Market Name: ").append(resultSet.getString("MarketName")).append("\n");
                    appendIfNotEmpty(details, "Website: ", resultSet.getString("Website"));
                    appendIfNotEmpty(details, "Address: ", resultSet.getString("street") + ", " +
                            resultSet.getString("city") + ", " +
                            resultSet.getString("State") + " " +
                            resultSet.getString("zip"));
                    appendIfNotEmpty(details, "Season 1: ", resultSet.getString("Season1Date") + " - " + resultSet.getString("Season1Time"));
                    appendIfNotEmpty(details, "Season 2: ", resultSet.getString("Season2Date") + " - " + resultSet.getString("Season2Time"));
                    appendIfNotEmpty(details, "Season 3: ", resultSet.getString("Season3Date") + " - " + resultSet.getString("Season3Time"));
                    appendIfNotEmpty(details, "Season 4: ", resultSet.getString("Season4Date") + " - " + resultSet.getString("Season4Time"));
                    details.append("Coordinates: (").append(resultSet.getBigDecimal("x")).append(", ").append(resultSet.getBigDecimal("y")).append(")\n");
                    appendIfNotEmpty(details, "Location: ", resultSet.getString("Location"));
                    appendIfNotEmpty(details, "Credit: ", resultSet.getBoolean("Credit"));
                    appendIfNotEmpty(details, "WIC: ", resultSet.getBoolean("WIC"));
                    appendIfNotEmpty(details, "WIC Cash: ", resultSet.getBoolean("WICcash"));
                    appendIfNotEmpty(details, "SFMNP: ", resultSet.getBoolean("SFMNP"));
                    appendIfNotEmpty(details, "SNAP: ", resultSet.getBoolean("SNAP"));
                    appendIfNotEmpty(details, "Organic: ", resultSet.getBoolean("Organic"));
                    // List of products
                    String[] productFields = {
                            "Bakedgoods", "Cheese", "Crafts", "Flowers", "Eggs", "Seafood", "Herbs", "Vegetables", "Honey", "Jams", "Maple", "Meat", "Nursery", "Nuts", "Plants", "Poultry", "Prepared", "Soap", "Trees", "Wine", "Coffee", "Beans", "Fruits", "Grains", "Juices", "Mushrooms", "PetFood", "Tofu", "WildHarvested"
                    };
                    String[] productNames = {
                            "Baked Goods", "Cheese", "Crafts", "Flowers", "Eggs", "Seafood", "Herbs", "Vegetables", "Honey", "Jams", "Maple", "Meat", "Nursery", "Nuts", "Plants", "Poultry", "Prepared", "Soap", "Trees", "Wine", "Coffee", "Beans", "Fruits", "Grains", "Juices", "Mushrooms", "Pet Food", "Tofu", "Wild Harvested"
                    };

                    StringBuilder products = new StringBuilder();
                    for (int i = 0; i < productFields.length; i++) {
                        if (resultSet.getBoolean(productFields[i])) {
                            if (products.length() > 0) {
                                products.append("\n");
                            }
                            products.append(productNames[i]);
                        }
                    }

                    if (products.length() > 0) {
                        details.append("\nProducts: \n").append(products.toString()).append("\n");
                    }

                    appendIfNotEmpty(details, "Update Time: ", resultSet.getString("updateTime"));

                    Platform.runLater(() -> {
                        marketDetailsLabel.setText(details.toString());
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> Utils.showAlert("Error", "Failed to load market details."));
            }
        }).start();
    }

    /**
     * Appends a label and value to the details StringBuilder if the value is not empty.
     *
     * @param details The StringBuilder to append to.
     * @param label The label to prepend to the value.
     * @param value The value to append if not empty.
     */
    private void appendIfNotEmpty(StringBuilder details, String label, String value) {
        if (value != null && !value.isEmpty()) {
            details.append(label).append(value).append("\n");
        }
    }

    /**
     * Appends a label and boolean value to the details StringBuilder.
     *
     * @param details The StringBuilder to append to.
     * @param label The label to prepend to the value.
     * @param value The boolean value to append.
     */
    private void appendIfNotEmpty(StringBuilder details, String label, boolean value) {
        details.append(label).append(value ? "Yes" : "No").append("\n");
    }

    /**
     * Handles the submit review button click event. Validates the loading state and
     * retrieves the review details from the input fields.
     */
    @FXML
    private void onSubmitReviewButtonClick() {
        if(loading.get()) {
            Utils.showAlert("Error", "Please wait for the data to finish loading.");
            return;
        }
        String MarketName = selectedMarket;
        String name = reviewNameField.getText();
        String reviewText = reviewTextField.getText();
        int rating = reviewRatingComboBox.getValue();
    }
}