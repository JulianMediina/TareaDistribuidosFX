package co.edu.uptc.model;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SocketInterface extends Application {

    private ClienteSocket socketClient;
    private TextArea portText, textIp;

    @Override
    public void start(Stage stage) throws Exception {
        Label laberPort = new Label("Ingrese el puerto: ");
        portText = new TextArea();
        portText.setPrefSize(100, 10);
        Label labelIP = new Label("Ingrese la direccion ip: ");
        textIp = new TextArea();
        textIp.setPrefSize(100, 10);
        Button connectButton = new Button("Conectarse al chat");
        Button serverButton = new Button("Encender servidor");
        Button closeServer = new Button("Apagar servidor");
        VBox vbox = new VBox(laberPort, portText, labelIP, textIp, connectButton, serverButton, closeServer);
        stage.setOnCloseRequest(e -> {
            ServidorSocket.closeServer();
            System.out.println("Servidor cerrado exitosamente");
            stage.close();
            if (socketClient != null) {
                socketClient.stop();
            }
        });
        vbox.setStyle("-fx-spacing: 1.5em; -fx-padding: 7em;");
        connectButton.setOnAction(e -> addUser());
        serverButton.setOnAction(e -> turnOnServer());
        closeServer.setOnAction(e -> turnOffServer(true));
        vbox.setAlignment(Pos.CENTER);
        stage.setTitle("Chat socket");
        stage.setScene(new Scene(vbox));
        stage.show();
    }

    private void addUser() {
        socketClient = new ClienteSocket();
        Platform.runLater(new Runnable() {
            public void run() {
                socketClient.startApp(new Stage(), textIp.getText(), Integer.parseInt(portText.getText()));

            }
        });
    }

    private void turnOnServer() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                if (!ServidorSocket.serverOn()) {
                    new ServidorSocket(Integer.parseInt(portText.getText()));
                } else {
                    Platform.runLater(new Runnable() {
                        public void run() {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error sobreposicion de servidores");
                            alert.setHeaderText("Servidor ya encendido");
                            alert.setContentText("Ya hay un servidor abierto.");
                            alert.showAndWait();
                        }
                    });
                }
            }
        });
        thread.start();
    }

    public void turnOffServer(boolean onStage) {
        if (ServidorSocket.serverOn()) {
            ServidorSocket.closeServer();
            if (socketClient != null) {
                socketClient.stop();
            }
            System.out.println("Servidor cerrado exitosamente");
        } else if (onStage) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error servidor aun no encendido");
            alert.setHeaderText("Servidor no encendido");
            alert.setContentText("El servidor no esta encendido.");
            alert.showAndWait();
        }
    }

    //iniciar app
    public static void main(String[] args) {
        launch(args);
    }
}
