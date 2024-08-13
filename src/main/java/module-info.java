module edu.rpi.cs.csci4963.u24.wangn4.hw05.farmers_market.farmers_market {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.rpi.cs.csci4963.u24.wangn4.hw05.farmers_market.farmers_market to javafx.fxml;
    exports edu.rpi.cs.csci4963.u24.wangn4.hw05.farmers_market.farmers_market;
}