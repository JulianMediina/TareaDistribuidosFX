package co.edu.uptc.model;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SocketInterface extends Application{

    private ClienteSocket socketClient;

    @Override
    public void start(Stage stage) throws Exception {
        Button connectButton = new Button("Conectarse al chat");
        Button serverButton = new Button("Encender servidor");
        Button closeServer = new Button("Apagar servidor");
        VBox vbox = new VBox(connectButton, serverButton, closeServer);
        stage.setOnCloseRequest(e -> {
            ServidorSocket.closeServer();
            System.out.println("Servidor cerrado exitosamente");
            stage.close();
            if(socketClient != null){
                socketClient.stop();
            }
        });
        vbox.setStyle("-fx-spacing: 1.5em; -fx-padding: 7em;");
        connectButton.setOnAction(e ->  addUser());
        serverButton.setOnAction(e -> turnOnServer());
        closeServer.setOnAction(e -> turnOffServer(true));
        vbox.setAlignment(Pos.CENTER);
        stage.setTitle("Chat socket");
        stage.setScene(new Scene(vbox));
        stage.show();
    }

    private void addUser(){
        socketClient = new ClienteSocket();
        Platform.runLater(new Runnable(){
            public void run(){
                socketClient.start(new Stage());
                
            }
        });
    }

    private void turnOnServer(){
        Thread thread = new Thread(new Runnable(){
            public void run(){
                if(!ServidorSocket.serverOn()){
                    new ServidorSocket();
                }else{
                    Platform.runLater(new Runnable(){
                        public void run(){
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
    
    public void turnOffServer(boolean onStage){
        if(ServidorSocket.serverOn()){
            ServidorSocket.closeServer();
            if(socketClient != null){
                socketClient.stop();
            }
            System.out.println("Servidor cerrado exitosamente");
        }else if(onStage){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error servidor aun no encendido");
            alert.setHeaderText("Servidor no encendido");
            alert.setContentText("El servidor no esta encendido.");
            alert.showAndWait();
        }
    }public static void main(String[] args) {
            launch(args);
    }
}
