package com.syncplant.gui;

import com.syncplant.ConfigLoader;
import com.syncplant.model.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainApp extends Application {

    // GUI components displaying the current state
    private Label warehouseALabel;
    private Label warehouseBLabel;
    private Label outboundUnpackedLabel;
    private Label outboundPackageLabel;
    private TextArea logArea;
    private Rectangle truck;
    private Label unpackedLabel;
    private Label packagesLabel;

    private Warehouse warehouseA;
    private Warehouse warehouseB;
    private OutboundWarehouse outboundWarehouse;
    private SupplyTruck supplyTruck;
    private OutboundTruck outboundTruck;
    private ProductionLine[] productionLines;

    // application start (calls start(Stage stage))
    public static void main(String[] args) {
        launch(args);
    }

    // GUI start
    @Override
    public void start(Stage stage) {
        // load configuration
        Map<String, Object> config = ConfigLoader.loadConfig();

        int numOfLines = (Integer) config.get("number_of_production_lines");

        Pane root = new Pane();

        // warehouse A icon
        Rectangle warehouseAIcon = new Rectangle(150,100, Color.LIGHTBLUE);
        warehouseAIcon.setX(250);
        warehouseAIcon.setY(100);
        this.warehouseALabel = new Label("Warehouse A: ?");
        warehouseALabel.setLayoutX(285);
        warehouseALabel.setLayoutY(110);

        // warehouse B icon
        Rectangle warehouseBIcon = new Rectangle(150, 100, Color.LIGHTBLUE);
        warehouseBIcon.setX(250);
        warehouseBIcon.setY(300);
        this.warehouseBLabel = new Label("Warehouse B: ?");
        warehouseBLabel.setLayoutX(285);
        warehouseBLabel.setLayoutY(375);

        // supplier icon
        Rectangle supplier = new Rectangle(50, 70, Color.GREY);
        supplier.setX(5);
        supplier.setY(215);
        Text supplierLabel = new Text("Supplier");
        supplierLabel.setX(7);
        supplierLabel.setY(210);

        // supply truck icon
        this.truck = new Rectangle(60, 30, Color.DARKGREY);
        truck.setX(40);
        truck.setY(235);

        // icons of production lines
        Rectangle[] productionLinesIcons = new Rectangle[numOfLines];
        Label[] linesLabels = new Label[numOfLines];
        Label linesLabel = new Label("Production lines:");
        linesLabel.setLayoutX(485);
        linesLabel.setLayoutY(75);
        for (int i=0; i < numOfLines; i++) {
            productionLinesIcons[i] = new Rectangle(60, 60, Color.LIGHTGREEN);
            productionLinesIcons[i].setX(500);
            productionLinesIcons[i].setY(100 + i * 80);

            linesLabels[i] = new Label("" + (i + 1));
            linesLabels[i].setLayoutX(525);
            linesLabels[i].setLayoutY(105 + i * 80);

            root.getChildren().addAll(productionLinesIcons[i], linesLabels[i]);
        }

        // icon of outbound warehouse
        Rectangle outboundWarehouseIcon = new Rectangle(150, 150, Color.ORANGE);
        outboundWarehouseIcon.setX(700);
        outboundWarehouseIcon.setY(175);
        // label - name of icon
        Label outboundWarehouseLabel = new Label("Outbound warehouse");
        outboundWarehouseLabel.setLayoutX(715);
        outboundWarehouseLabel.setLayoutY(185);
        // label - number of unpacked products
        unpackedLabel = new Label("Unpacked products: 0");
        unpackedLabel.setLayoutX(715);
        unpackedLabel.setLayoutY(270);
        // label - number of packages
        packagesLabel = new Label("Packages: 0");
        packagesLabel.setLayoutX(715);
        packagesLabel.setLayoutY(290);
        // add to root
        root.getChildren().addAll(outboundWarehouseIcon, outboundWarehouseLabel, unpackedLabel, packagesLabel);


        // add to scene
        root.getChildren().addAll(warehouseAIcon, warehouseALabel, warehouseBIcon, warehouseBLabel, supplier, supplierLabel, truck, linesLabel);

        logArea = new TextArea();
        logArea.setLayoutX(50);
        logArea.setLayoutY(500);
        logArea.setPrefSize(450, 100);
        logArea.setEditable(false);
        root.getChildren().add(logArea);

        Scene scene = new Scene(root);
        stage.setTitle("Manufacturing Plant");
        stage.setScene(scene);
        stage.show();



        // application logic
        // warehouses
        Map<String, Object> warehouseAConfig = (Map<String, Object>) config.get("warehouseA");
        Map<String, Object> warehouseBConfig = (Map<String, Object>) config.get("warehouseB");
        this.warehouseA = new Warehouse("A", (Integer) warehouseAConfig.get("initial_stock"), (Integer) warehouseAConfig.get("threshold"), null, this);
        this.warehouseB = new Warehouse("B", (Integer) warehouseBConfig.get("initial_stock"), (Integer) warehouseBConfig.get("threshold"), null, this);

        // show initial warehouses' stocks in GUI
        updateWarehouseA((Integer) warehouseAConfig.get("initial_stock"));
        updateWarehouseB((Integer) warehouseBConfig.get("initial_stock"));

        // outbound warehouse
        this.outboundWarehouse = new OutboundWarehouse((Integer) config.get("package_capacity"), this);

        // production lines
        this.productionLines = new ProductionLine[numOfLines];
        for (int i = 0; i < numOfLines; i++) {
            productionLines[i] = new ProductionLine(i + 1, warehouseA, warehouseB, (Integer) config.get("required_A_amount"), (Integer) config.get("required_B_amount"), outboundWarehouse, this);
        }

        // supply truck
        this.supplyTruck = new SupplyTruck(new Warehouse[]{warehouseA, warehouseB}, (Integer) config.get("delivery_amount"), productionLines, this);

        // outbound truck
        this.outboundTruck = new OutboundTruck(outboundWarehouse, this);

        // assign supply truck to warehouses
        warehouseA.setTruck(supplyTruck);
        warehouseB.setTruck(supplyTruck);

        // make ExecutorService
        ExecutorService executorService = Executors.newFixedThreadPool(numOfLines);

        // start the production
        for (ProductionLine line : productionLines) {
            executorService.submit(line);
        }

        supplyTruck.start();
        outboundTruck.start();

    }

    public void animateDelivery(String warehouseSymbol, Runnable onDelivered, Runnable onReturned) {
        Platform.runLater(() -> {
            double rightMove = 250;
            double verticalShift = warehouseSymbol.equals("A") ? -85 : 85;

            // go right to warehouses
            TranslateTransition moveRight = new TranslateTransition(Duration.seconds(1), truck);
            moveRight.setByX(rightMove);

            // go up or down to a given warehouse
            TranslateTransition moveUpOrDown = new TranslateTransition(Duration.seconds(1), truck);
            moveUpOrDown.setByY(verticalShift);

            // go back
            TranslateTransition moveBackY = new TranslateTransition(Duration.seconds(0.25), truck);
            moveBackY.setByY(-verticalShift);

            TranslateTransition moveBackX = new TranslateTransition(Duration.seconds(1), truck);
            moveBackX.setByX(-rightMove);

            // when materials delivered refill them
            moveUpOrDown.setOnFinished(e -> {
                if (onDelivered != null) {
                    onDelivered.run();
                }
                moveBackY.play();
            });

            //when truck returned continue its work
            moveBackX.setOnFinished(e -> {
                if (onReturned != null) {
                    onReturned.run();
                }
            });

            // order of execution
            moveRight.setOnFinished(e -> moveUpOrDown.play());
            moveBackY.setOnFinished(e -> moveBackX.play());

            // start the whole animation
            moveRight.play();
        });
    }

    // add text to log
    public void log(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }

    // update warehouse A status on the label
    public void updateWarehouseA(int stock) {
        Platform.runLater(() -> warehouseALabel.setText("Warehouse A: " + stock));
    }

    // update warehouse B status on the label
    public void updateWarehouseB(int stock) {
        Platform.runLater(() -> warehouseBLabel.setText("Warehouse B: " + stock));
    }

    // update outbound warehouse status on the label
    public void updateOutboundWarehouse(int unpacked, int packages) {
        Platform.runLater(() -> {
            unpackedLabel.setText("Unpacked products: " + unpacked);
            packagesLabel.setText("Packages: " + packages);
        });
    }

}
