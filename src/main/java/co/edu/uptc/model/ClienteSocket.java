package co.edu.uptc.model;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.FlowPane;

public class ClienteSocket extends Application {

    private Socket socketCliente;
    private BufferedReader entradaServidor;
    private PrintWriter salidaServidor;
    private TextArea chatArea;
    private TextField messageField;
    private String nombreUsuario;
    private Stage stage;

    private static final String IMAGE_DIRECTORY = "/filesData"; // Directorio donde se guardarán las imágenes

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        stage = primaryStage;
        String ipNet = "192.168.56.1";
        try {
            InetAddress direction = InetAddress.getLocalHost();
            InetAddress[] directions = InetAddress.getAllByName(direction.getHostName());
            for (InetAddress addr : directions) {
                if (addr.getHostAddress().contains(".")) {
                    ipNet = addr.getHostAddress();
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Error al intentar obtener la dirección IP");
        }

        primaryStage.setTitle("Chat Cliente");

        chatArea = new TextArea();
        chatArea.setEditable(false);

        messageField = new TextField();
        messageField.setPromptText("Escribe un mensaje...");

        Button sendButton = new Button("Enviar");
        sendButton.setOnAction(e -> sendMessage());

        Button chooseImageButton = new Button("Seleccionar imagen");
        chooseImageButton.setOnAction(e -> selectAndSendImage());

        Button viewImagesButton = new Button("Ver imágenes");
        viewImagesButton.setOnAction(e -> showImages());

        Label userLabel = new Label("Usuario: ");
        Label usernameLabel = new Label();

        VBox vbox = new VBox();
        vbox.getChildren().addAll(userLabel, usernameLabel, chatArea, messageField, sendButton, chooseImageButton, viewImagesButton);
        Scene scene = new Scene(vbox, 400, 300);
        primaryStage.setScene(scene);

        String servidor = ipNet;
        int puerto = 12345;

        try {
            socketCliente = new Socket(servidor, puerto);
            entradaServidor = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            salidaServidor = new PrintWriter(socketCliente.getOutputStream(), true);

            nombreUsuario = getInput("Por favor, introduce tu nombre de usuario:");
            usernameLabel.setText(nombreUsuario);

            Thread hiloServidor = new Thread(() -> {
                try {
                    String mensajeServidor;
                    while ((mensajeServidor = entradaServidor.readLine()) != null) {
                        chatArea.appendText(mensajeServidor + "\n");
                    }
                } catch (Exception e) {
                    System.out.println("EL SERVIDOR HA CERRADO");
                }
            });
            hiloServidor.start();

            messageField.setOnAction(e -> sendMessage());

            salidaServidor.println(nombreUsuario);
            primaryStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de ingreso al chat");
            alert.setHeaderText("Servidor apagado");
            alert.setContentText("No hay ningun servidor disponible, porfavor enciendalo.");
            alert.showAndWait();
        }

    }

    private void sendMessage() {
        String mensaje = messageField.getText();
        if (!mensaje.isEmpty()) {
            sendMessage(nombreUsuario + ": " + mensaje);
            messageField.clear();
        }
    }

    private void sendMessage(String message) {
        if (!message.equals("cerrar")) {
            chatArea.appendText(message + "\n");
        }
        salidaServidor.println(message);
    }

    private void selectAndSendImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                // Lee la imagen como un array de bytes
                byte[] imageData = Files.readAllBytes(file.toPath());
                // Envía los bytes al servidor
                sendImage(imageData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendImage(byte[] imageData) {
        try {
            // Envía un mensaje indicando que se enviará una imagen
            sendMessage("IMAGEN");

            // Envía el tamaño de la imagen como un entero de 4 bytes
            OutputStream outputStream = socketCliente.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeInt(imageData.length);

            // Envía los bytes de la imagen al servidor
            outputStream.write(imageData);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getInput(String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nombre de Usuario");
        dialog.setHeaderText(prompt);
        dialog.setContentText("Nombre de Usuario:");
        dialog.initOwner(chatArea.getScene().getWindow());

        return dialog.showAndWait().orElse("Usuario");
    }

    private void showImages() {
        Stage galleryStage = new Stage();
        galleryStage.setTitle("Galería de Imágenes");

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(10);
        flowPane.setVgap(10);

        List<File> imageFiles = loadImageFilesFromDirectory(IMAGE_DIRECTORY);
        for (File file : imageFiles) {
            try {
                Image image = new Image(new FileInputStream(file));
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                flowPane.getChildren().add(imageView);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        Scene scene = new Scene(flowPane, 800, 600);
        galleryStage.setScene(scene);
        galleryStage.show();
    }

    private List<File> loadImageFilesFromDirectory(String directoryPath) {
        List<File> imageFiles = new ArrayList<>();
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isImageFile(file)) {
                        imageFiles.add(file);
                    }
                }
            }
        }
        return imageFiles;
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
    }

    public void stop() {
        stage.close();
    }
}
