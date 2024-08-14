package edu.rpi.cs.csci4963.u24.wangn4.hw05.farmers_market.farmers_market;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import static java.lang.Double.*;

public class DatabaseModel {

    private Connection connection;

    private static String escapeSQL(String value) {
        return value.replace("'", "''");
    }

    public DatabaseModel() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("dbconfig.properties"));

            String jdbcUrl = properties.getProperty("jdbc.url");
            String user = properties.getProperty("jdbc.user");
            String password = properties.getProperty("jdbc.password");

            connection = DriverManager.getConnection(jdbcUrl, user, password);
            System.out.println("db connected");
            generateTables();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void generateTables() {
        try {
            System.out.println("Generating tables");
            generateTableZipCodes();
            System.out.println("Generated table zip codes");
            generateTableFarmersMarket();
            System.out.println("Generated table farmers market");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void generateTableZipCodes() throws Exception {
        URL url = this.getClass().getResource("zip_codes_states.csv");

        // create table
        connection.createStatement().executeUpdate("""
            CREATE TABLE IF NOT EXISTS zip_codes (
                zip_code VARCHAR(10) PRIMARY KEY,
                latitude DECIMAL(9,6),
                longitude DECIMAL(9,6),
                city VARCHAR(100),
                state VARCHAR(2),
                county VARCHAR(100)
            );""");

        // buffer for SQL query
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT IGNORE INTO zip_codes (zip_code, latitude, longitude, city, state, county) VALUES ");

        try (BufferedReader reader = new BufferedReader(new FileReader(url.getFile()))) {
            // header verification
            String line = reader.readLine();
            if (!"\"zip_code\",\"latitude\",\"longitude\",\"city\",\"state\",\"county\"".equals(line)) {
                Exception e = new Exception("Wrong zip code file!!! " + url);
                throw e;
            }

            int i = 1;
            // read and parse line
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(","))
                    line += " ";
                String[] parts = line.replace("\"", "").split(",");
                i++;
                if (parts.length != 6) {
                    Exception e = new Exception("expecting only 6 values split by comma;\tline (" + i + ") got: " + line);
                    throw e;
                }
                parts[5] = parts[5].trim();

                double y;
                if (parts[1].equals("")) {
                    y = 0.0;
                } else {
                    y = parseDouble(parts[1]);
                    if (y == 0.0)
                        System.out.println("Notice failed parsing the latitude as double (could be empty) " + url + "@" + i);
                }

                double x;
                if (parts[2].equals("")) {
                    x = 0.0;
                } else {
                    x = parseDouble(parts[2]);
                    if (x == 0.0)
                        System.out.println("Notice failed parsing the longitude as double (could be empty) " + url + "@" + i);
                }

                // line ok, format in SQL
                builder.append("('")
                        .append(parts[0]).append("', ")
                        .append(x).append(", ")
                        .append(y).append(", '")
                        .append(parts[3].replace("'", "\\'")).append("', '")
                        .append(parts[4].replace("'", "\\'")).append("', '")
                        .append(parts[5].replace("'", "\\'"))
                        .append("'), ");

                // flush buffer every 1000 rows
                if (i % 1000 == 0) {
                    System.out.println("updating through row " + i);
                    builder.delete(builder.length() - 4, builder.length());
                    builder.append("');");
                    try {
                        connection.createStatement().executeUpdate(builder.toString());
                    } catch (SQLException e) {
                        System.out.println(builder.toString());
                        throw new RuntimeException(e);
                    }
                    builder = new StringBuilder();
                    builder.append("INSERT IGNORE INTO zip_codes (zip_code, latitude, longitude, city, state, county) VALUES ");
                }
            }

            // final update
            builder.delete(builder.length() - 4, builder.length());
            builder.append("');");
            connection.createStatement().executeUpdate(builder.toString());
            System.out.println("loaded zip_codes table with " + i + " rows");
        } catch (IOException e) {
            System.out.println("Failed to read the zip code file");
            throw new Exception("Failed to read the zip code file: " + url);
        }
    }

    private void generateTableFarmersMarket() throws Exception {
        URL url = this.getClass().getResource("Export.csv");

        // create table
        connection.createStatement().executeUpdate("""
                CREATE TABLE IF NOT EXISTS farmers_market (
                    FMID INT PRIMARY KEY,
                    MarketName VARCHAR(255),
                    Website VARCHAR(255),
                    Facebook VARCHAR(255),
                    Twitter VARCHAR(255),
                    Youtube VARCHAR(255),
                    OtherMedia VARCHAR(255),
                    street VARCHAR(255),
                    city VARCHAR(100),
                    County VARCHAR(100),
                    State VARCHAR(50),
                    zip VARCHAR(10),
                    Season1Date VARCHAR(1024),
                    Season1Time VARCHAR(1024),
                    Season2Date VARCHAR(1024),
                    Season2Time VARCHAR(1024),
                    Season3Date VARCHAR(1024),
                    Season3Time VARCHAR(1024),
                    Season4Date VARCHAR(1024),
                    Season4Time VARCHAR(1024),
                    x DECIMAL(9,6),
                    y DECIMAL(9,6),
                    Location VARCHAR(255),
                    Credit BOOLEAN,
                    WIC BOOLEAN,
                    WICcash BOOLEAN,
                    SFMNP BOOLEAN,
                    SNAP BOOLEAN,
                    Organic BOOLEAN,
                    Bakedgoods BOOLEAN,
                    Cheese BOOLEAN,
                    Crafts BOOLEAN,
                    Flowers BOOLEAN,
                    Eggs BOOLEAN,
                    Seafood BOOLEAN,
                    Herbs BOOLEAN,
                    Vegetables BOOLEAN,
                    Honey BOOLEAN,
                    Jams BOOLEAN,
                    Maple BOOLEAN,
                    Meat BOOLEAN,
                    Nursery BOOLEAN,
                    Nuts BOOLEAN,
                    Plants BOOLEAN,
                    Poultry BOOLEAN,
                    Prepared BOOLEAN,
                    Soap BOOLEAN,
                    Trees BOOLEAN,
                    Wine BOOLEAN,
                    Coffee BOOLEAN,
                    Beans BOOLEAN,
                    Fruits BOOLEAN,
                    Grains BOOLEAN,
                    Juices BOOLEAN,
                    Mushrooms BOOLEAN,
                    PetFood BOOLEAN,
                    Tofu BOOLEAN,
                    WildHarvested BOOLEAN,
                    updateTime VARCHAR(100)
                );""");


        try (CSVReader reader = new CSVReader(new FileReader(url.getFile()))) {
            // header verification
            String line = Arrays.toString(reader.readNext());
            if (!("[FMID, MarketName, Website, Facebook, Twitter, Youtube, OtherMedia, street, city, County, State, zip, " +
                    "Season1Date, Season1Time, Season2Date, Season2Time, Season3Date, Season3Time, Season4Date, Season4Time, " +
                    "x, y, Location, Credit, WIC, WICcash, SFMNP, SNAP, Organic, Bakedgoods, Cheese, Crafts, Flowers, Eggs, " +
                    "Seafood, Herbs, Vegetables, Honey, Jams, Maple, Meat, Nursery, Nuts, Plants, Poultry, Prepared, Soap, " +
                    "Trees, Wine, Coffee, Beans, Fruits, Grains, Juices, Mushrooms, PetFood, Tofu, WildHarvested, updateTime]"
            ).equals(line)) {
                Exception e = new Exception("Wrong farmers market file!!! " + url);
                throw e;
            }

            String sql = "INSERT IGNORE INTO farmers_market (FMID, MarketName, Website, Facebook, Twitter, Youtube, OtherMedia, " +
                    "street, city, County, State, zip, Season1Date, Season1Time, Season2Date, Season2Time, Season3Date, " +
                    "Season3Time, Season4Date, Season4Time, x, y, Location, Credit, WIC, WICcash, SFMNP, SNAP, Organic, " +
                    "Bakedgoods, Cheese, Crafts, Flowers, Eggs, Seafood, Herbs, Vegetables, Honey, Jams, Maple, Meat," +
                    "Nursery, Nuts, Plants, Poultry, Prepared, Soap, Trees, Wine, Coffee, Beans, Fruits, Grains, Juices, " +
                    "Mushrooms, PetFood, Tofu, WildHarvested, updateTime) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int count = 1;

            // buffer for SQL query
            StringBuilder builder = new StringBuilder();
            builder.append("INSERT IGNORE INTO farmers_market (FMID, MarketName, Website, Facebook, Twitter, Youtube, OtherMedia, ");
            builder.append("street, city, County, State, zip, Season1Date, Season1Time, Season2Date, Season2Time, Season3Date, ");
            builder.append("Season3Time, Season4Date, Season4Time, x, y, Location, Credit, WIC, WICcash, SFMNP, SNAP, Organic, ");
            builder.append("Bakedgoods, Cheese, Crafts, Flowers, Eggs, Seafood, Herbs, Vegetables, Honey, Jams, Maple, Meat,");
            builder.append("Nursery, Nuts, Plants, Poultry, Prepared, Soap, Trees, Wine, Coffee, Beans, Fruits, Grains, Juices, ");
            builder.append("Mushrooms, PetFood, Tofu, WildHarvested, updateTime) VALUES ");


            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            // reads every csv line
            String[] data;
            while ((data = reader.readNext()) != null) {
                count++;
                preparedStatement.setInt(1, Integer.parseInt(data[0]));

                for (int i = 1; i < 20; i++)
                    preparedStatement.setString(i + 1, data[i].replace("'", "\\'"));
                if (data[21].equals("")) {
                    preparedStatement.setBigDecimal(21, BigDecimal.valueOf(0.0));
                } else {
                    preparedStatement.setBigDecimal(21, BigDecimal.valueOf(parseDouble(data[20])));
                }
                if (data[22].equals("")) {
                    preparedStatement.setBigDecimal(22, BigDecimal.valueOf(0.0));
                } else {
                    preparedStatement.setBigDecimal(22, BigDecimal.valueOf(parseDouble(data[21])));
                }
                preparedStatement.setString(23, data[22].replace("'", "\\'"));
                for (int i = 23; i < 58; i++)
                    preparedStatement.setBoolean(i + 1, "Y".equals(data[i]));
                preparedStatement.setString(59, data[58].replace("'", "\\'"));


                // if empty zip code fill in from zip_codes data
                String zipCode = data[11];
                preparedStatement.setString(12, zipCode);

                String query = preparedStatement.toString().substring("com.mysql.cj.jdbc.ClientPreparedStatement: INSERT IGNORE INTO farmers_market (FMID, MarketName, Website, Facebook, Twitter, Youtube, OtherMedia, street, city, County, State, zip, Season1Date, Season1Time, Season2Date, Season2Time, Season3Date, Season3Time, Season4Date, Season4Time, x, y, Location, Credit, WIC, WICcash, SFMNP, SNAP, Organic, Bakedgoods, Cheese, Crafts, Flowers, Eggs, Seafood, Herbs, Vegetables, Honey, Jams, Maple, Meat,Nursery, Nuts, Plants, Poultry, Prepared, Soap, Trees, Wine, Coffee, Beans, Fruits, Grains, Juices, Mushrooms, PetFood, Tofu, WildHarvested, updateTime) VALUES ".length());
                builder.append(query).append(",");
                preparedStatement.addBatch();
            }

            builder.replace(builder.length() - 1, builder.length(), ";");
            connection.setAutoCommit(false);
            connection.createStatement().executeUpdate(builder.toString());
            connection.commit();
            System.out.println("generated table farmers market with " + count + " rows");
        } catch (IOException e) {
            e.printStackTrace();
            connection.rollback();
            System.out.println("Failed to read CSV file: " + e.getMessage());
        } catch (CsvValidationException e) {
            e.printStackTrace();
            connection.rollback();
            System.out.println("Failed to parse CSV properly: " + e.getMessage());
        }
    }
}
