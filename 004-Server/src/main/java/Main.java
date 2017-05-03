import client.Client;
import com.google.common.io.Files;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import server.ListAnswer;
import server.Server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main extends Application {
    private final Client client = new Client();
    private Server server = null;
    private String lastPath = "";
    static {
        new Thread(() -> {
            try {
                new Server().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("app");

        VBox vBox = new VBox();
        HBox hBox = new HBox();
        ListView<String> foldersView = new ListView<>();
        ListView<String> filesView = new ListView<>();
        TextField pathField = new TextField(".");
        Button showButton = new Button("Show path");
        hBox.getChildren().addAll(pathField, showButton);

        Consumer<String> onShowButton = (path) -> {
            pathField.setText(path);
            lastPath = path;
            try {
                List<ListAnswer.Node> nodes = client.executeList(path);
                List<String> folders = nodes.stream()
                        .filter(node -> node.isDirectory)
                        .map(node -> node.name)
                        .collect(Collectors.toList());
                foldersView.setItems(FXCollections.observableArrayList(folders));

                List<String> files = nodes.stream()
                        .filter(node -> !node.isDirectory)
                        .map(node -> node.name)
                        .collect(Collectors.toList());
                filesView.setItems(FXCollections.observableArrayList(files));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        pathField.setOnAction(event -> onShowButton.accept(pathField.getText()));


        onShowButton.accept(pathField.getText());
        showButton.setOnMouseClicked(event -> onShowButton.accept(pathField.getText()));

        foldersView.setOnMouseClicked(event -> {
            String selectedItem = foldersView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String path = Paths.get(lastPath, selectedItem).toString();
                onShowButton.accept(path);
            }
        });

        filesView.setOnMouseClicked(event -> {
            String fileName = filesView.getSelectionModel().getSelectedItem();
            if (fileName != null) {
                try {
                    String path = Paths.get(lastPath, fileName).toString();
                    byte[] bytes = client.executeGet(path);
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialFileName(fileName);
                    File file = fileChooser.showSaveDialog(primaryStage);
                    if (file != null) {
                        Files.write(bytes, file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        hBox.setStyle("-fx-background-color: #336699;");
        vBox.getChildren().addAll(hBox,
                new Label("Folders:"),
                foldersView,
                new Label("Files (click to download):"),
                filesView);


        Scene scene = new Scene(vBox, 512, 768);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
