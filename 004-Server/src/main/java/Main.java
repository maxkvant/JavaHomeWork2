import client.Client;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Stage;
import server.ListAnswer;
import server.Server;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {
    private final Client client = new Client();
    private Server server = null;
    {
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
        ListView foldersView = new ListView();
        ListView filesView = new ListView();
        TextField pathField = new TextField(".");
        Button showButton = new Button("Show path");
        hBox.getChildren().addAll(pathField, showButton);

        showButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String path = pathField.getText();
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
            }
        });

        hBox.setStyle("-fx-background-color: #336699;");
        vBox.setSpacing(8);
        hBox.setSpacing(8);
        vBox.getChildren().addAll(hBox, foldersView, filesView);


        Scene scene = new Scene(vBox, 512, 768);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
