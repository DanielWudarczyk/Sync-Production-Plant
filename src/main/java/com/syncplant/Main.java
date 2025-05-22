package com.syncplant;

import com.syncplant.model.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    // GUI components displaying the current state
    private Label warehouseAStock;
    private Label warehouseBStock;
    private Rectangle truck;
    private Label unpackedLabel;
    private Label packagesLabel;
    private Label[] linesStatusLabels;
    private Pane root;
    private Rectangle outboundTruckIcon;

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

        this.root = new Pane();

        // warehouse A icon
        Rectangle warehouseAIcon = new Rectangle(150,100, Color.LIGHTBLUE);
        warehouseAIcon.setX(250);
        warehouseAIcon.setY(100);
        Label warehouseALabel = new Label("Warehouse A");
        warehouseALabel.setLayoutX(285);
        warehouseALabel.setLayoutY(110);
        this.warehouseAStock = new Label("stock: ?");
        warehouseAStock.setLayoutX(285);
        warehouseAStock.setLayoutY(125);

        // warehouse B icon
        Rectangle warehouseBIcon = new Rectangle(150, 100, Color.LIGHTBLUE);
        warehouseBIcon.setX(250);
        warehouseBIcon.setY(300);
        Label warehouseBLabel = new Label("Warehouse B");
        warehouseBLabel.setLayoutX(285);
        warehouseBLabel.setLayoutY(375);
        this.warehouseBStock = new Label("stock: ?");
        warehouseBStock.setLayoutX(285);
        warehouseBStock.setLayoutY(360);

        // supplier icon
        Rectangle supplier = new Rectangle(50, 70, Color.GREY);
        supplier.setX(5);
        supplier.setY(215);
        Text supplierLabel = new Text("Supplier");
        supplierLabel.setX(7);
        supplierLabel.setY(210);

        // supply truck icon
        this.truck = new Rectangle(60, 30, Color.DARKGREY);
        truck.setX(30);
        truck.setY(235);

        // icons of production lines
        Rectangle[] productionLinesIcons = new Rectangle[numOfLines];
        Label[] linesLabels = new Label[numOfLines];
        Label linesLabel = new Label("Production lines:");
        linesLabel.setLayoutX(500);
        linesLabel.setLayoutY(45);
        linesStatusLabels = new Label[numOfLines];
        for (int i=0; i < numOfLines; i++) {
            productionLinesIcons[i] = new Rectangle(120, 80, Color.LIGHTGREEN);
            productionLinesIcons[i].setX(500);
            productionLinesIcons[i].setY(70 + i * 100);

            linesLabels[i] = new Label("" + (i + 1));
            linesLabels[i].setLayoutX(555);
            linesLabels[i].setLayoutY(75 + i * 100);

            linesStatusLabels[i] = new Label("");
            linesStatusLabels[i].setLayoutX(510);
            linesStatusLabels[i].setLayoutY(95 + i * 100);

            root.getChildren().addAll(productionLinesIcons[i], linesLabels[i], linesStatusLabels[i]);
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
        unpackedLabel = new Label("unpacked products: 0");
        unpackedLabel.setLayoutX(715);
        unpackedLabel.setLayoutY(270);
        // label - number of packages
        packagesLabel = new Label("packages: 0");
        packagesLabel.setLayoutX(715);
        packagesLabel.setLayoutY(290);
        // add to root
        root.getChildren().addAll(outboundWarehouseIcon, outboundWarehouseLabel, unpackedLabel, packagesLabel);

        // icon of products receiver
        Rectangle receiverIcon = new Rectangle(50, 70, Color.BROWN);
        receiverIcon.setX(1030);
        receiverIcon.setY(200);
        Label receiverLabel = new Label("Receiver");
        receiverLabel.setLayoutX(1033);
        receiverLabel.setLayoutY(185);

        // icon of outbound truck
        this.outboundTruckIcon = new Rectangle(60, 30, Color.BLACK);
        outboundTruckIcon.setX(1000);
        outboundTruckIcon.setY(220);

        // add to scene
        root.getChildren().addAll(warehouseAIcon, warehouseALabel, warehouseAStock, warehouseBIcon, warehouseBLabel, warehouseBStock, supplier, supplierLabel, truck, linesLabel, receiverIcon, receiverLabel, outboundTruckIcon);

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

    // update warehouse A status on the label
    public void updateWarehouseA(int stock) {
        Platform.runLater(() -> warehouseAStock.setText("stock: " + stock));
    }

    // update warehouse B status on the label
    public void updateWarehouseB(int stock) {
        Platform.runLater(() -> warehouseBStock.setText("stock: " + stock));
    }

    // update line status label
    public void updateLineStatus(int lineNumber, String status) {
        Platform.runLater(() -> {
            linesStatusLabels[lineNumber-1].setText(status);
        });
    }

    // make product icon which goes to outbound warehouse
    public void makeProductIcon(int lineNumber) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Rectangle productIcon = new Rectangle(25, 25, Color.RED);
            productIcon.setX(520);
            productIcon.setY(105 + (lineNumber-1) * 100);
            root.getChildren().add(productIcon);

            // designate parameters needed to shift
            double startX = productIcon.getX();
            double startY = productIcon.getY();
            double endX = 710;
            double endY = 230;

            // animation
            TranslateTransition goToOutboundWarehouse = new TranslateTransition(Duration.seconds(0.75), productIcon);
            goToOutboundWarehouse.setByX(endX - startX);
            goToOutboundWarehouse.setByY(endY - startY);
            goToOutboundWarehouse.setOnFinished(e -> {
                root.getChildren().remove(productIcon);
                latch.countDown();
            });
            goToOutboundWarehouse.play();
        });

        // stop the producer until the animation is finished
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void updateProducts(int numOfProducts) {
        Platform.runLater(() -> {
            unpackedLabel.setText("unpacked products: " + numOfProducts);
        });
    }

    public void updatePackages(int numOfPackages) {
        Platform.runLater(() -> {
            packagesLabel.setText("packages: " + numOfPackages);
        });
    }

    public void takePackages(Runnable onArrived) {
        Platform.runLater(() -> {
            // movement of the truck
            TranslateTransition moveLeft = new TranslateTransition(Duration.seconds(1), outboundTruckIcon);
            moveLeft.setByX(-220);

            // take packages
            moveLeft.setOnFinished(e -> {
                if (onArrived != null) {
                    onArrived.run();
                    packagesLabel.setText("packages: 0");
                }

                // leave with the packages
                TranslateTransition moveRight = new TranslateTransition(Duration.seconds(1), outboundTruckIcon);
                moveRight.setByX(220);
                moveRight.play();

            });

            moveLeft.play();
        });
    }

}
