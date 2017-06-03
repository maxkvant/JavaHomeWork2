import client.Client;
import com.google.common.io.Files;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.omg.PortableInterceptor.ServerRequestInfo;
import server.ListAnswer;
import server.Server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Simple GUI for Server/Client
 */
public class Main extends Application {
    private final Server server = new Server();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("app");

        String localHost = InetAddress.getLocalHost().getHostName();

        Button serverStop = new Button("Stop");
        Button serverStart = new Button("Start");
        Button clientStart = new Button("Create");
        TextField hostName = new TextField(localHost);

        HBox hBoxServer = new HBox();
        hBoxServer.getChildren().addAll(serverStart, serverStop);

        HBox hBoxClient = new HBox();
        hBoxClient.getChildren().addAll(clientStart, hostName);
        HBox.setHgrow(hostName, Priority.SOMETIMES);

        serverStart.setOnMouseClicked(event -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        serverStop.setOnMouseClicked(event -> {
            try {
                server.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        clientStart.setOnMouseClicked(event -> new ClientGUI(hostName.getText()).getStage().show());

        VBox vBox = new VBox();
        vBox.getChildren().addAll(new Label("Server"),
            hBoxServer,
            new Label(),
            new Label("Client:"),
            clientStart,
            new Label("Client Host:"),
            hostName);

        Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private static class ClientGUI {
        @lombok.Getter
        private final Stage stage;

        private final Scene scene;
        private final TextField pathField = new TextField(".");
        private final VBox vBox = new VBox();
        private final HBox hBox = new HBox();
        private final ListView<String> foldersView = new ListView<>();
        private final ListView<String> filesView = new ListView<>();
        private final Client client;

        private String lastPath = "";

        private ClientGUI(String host) {
            client = new Client(host);
            this.stage = new Stage();
            stage.setTitle("Client");

            HBox.setHgrow(pathField, Priority.SOMETIMES);

            Button showButton = new Button("Show path");
            hBox.getChildren().addAll(pathField, showButton);
            pathField.setOnAction(event -> onShowButton(pathField.getText()));

            onShowButton(pathField.getText());
            showButton.setOnMouseClicked(event -> onShowButton(pathField.getText()));

            foldersView.setOnMouseClicked(event -> {
                String selectedItem = foldersView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    String curPath = Paths.get(lastPath, selectedItem).toString();
                    onShowButton(curPath);
                }
            });

            filesView.setOnMouseClicked(event -> onSaveFile());

            hBox.setStyle("-fx-background-color: #336699;");
            vBox.getChildren().addAll(hBox,
                    new Label("Folders:"),
                    foldersView,
                    new Label("Files (click to download):"),
                    filesView);

            scene = new Scene(vBox, 512, 768);
            stage.setMinWidth(150);
            stage.setMinHeight(150);
            stage.setScene(scene);
        }

        private void onShowButton(String path) {
            pathField.setText(path);
            lastPath = path;

            try {
                List<ListAnswer.Node> nodes = client.executeList(path);
                List<String> folders = new ArrayList<>(nodes.stream()
                        .filter(node -> node.isDirectory)
                        .map(node -> node.name)
                        .sorted()
                        .collect(Collectors.toList()));
                folders.add(0, "..");
                foldersView.setItems(FXCollections.observableArrayList(folders));

                List<String> files = nodes.stream()
                        .filter(node -> !node.isDirectory)
                        .map(node -> node.name)
                        .sorted()
                        .collect(Collectors.toList());
                filesView.setItems(FXCollections.observableArrayList(files));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void onSaveFile() {
            String fileName = filesView.getSelectionModel().getSelectedItem();
            if (fileName != null) {
                try {
                    String path = Paths.get(lastPath, fileName).toString();
                    byte[] bytes = client.executeGet(path);
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialFileName(fileName);
                    File file = fileChooser.showSaveDialog(stage);
                    if (file != null) {
                        Files.write(bytes, file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
