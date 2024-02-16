
package co.edu.uptc.model;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServidorSocket {

    private static List<ClienteHandler> clients = new ArrayList<>();
    private static ServerSocket servidorSocket;
    private static final String IMAGE_DIRECTORY = "/filesData"; // Directorio donde se guardarán las imágenes

    public static void main(String[] args) {
        new ServidorSocket();
    }

    public ServidorSocket() {
        int puerto = 12345;

        try {
            servidorSocket = new ServerSocket(puerto);
            System.out.println("Servidor esperando conexiones en el puerto " + puerto + "...");

            while (true) {
                Socket socketCliente = servidorSocket.accept();
                System.out.println("Nuevo cliente conectado desde la dirección: " + socketCliente.getInetAddress());

                ClienteHandler clienteHandler = new ClienteHandler(socketCliente);
                clients.add(clienteHandler);
                clienteHandler.start();
            }

        } catch(Exception e){
            if(servidorSocket == null || !servidorSocket.isClosed()){
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
    }

    public static boolean serverOn(){
        return servidorSocket != null && !servidorSocket.isClosed();
    }

    public static void closeServer(){
        try {
            servidorSocket.close();
        } catch (Exception e) {
        }
    }

    static class ClienteHandler extends Thread {

        private Socket socketCliente;
        private BufferedReader entradaCliente;
        private PrintWriter salidaCliente;
        private String nombreUsuario;

        public ClienteHandler(Socket socketCliente) {
            this.socketCliente = socketCliente;
            try {
                entradaCliente = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
                salidaCliente = new PrintWriter(socketCliente.getOutputStream(), true);
                nombreUsuario = entradaCliente.readLine();
                System.out.println("Nuevo usuario conectado: " + nombreUsuario);
                broadcast("El usuario " + nombreUsuario + " se ha unido al chat.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String mensajeCliente;
                do {
                    mensajeCliente = entradaCliente.readLine();
                    if (mensajeCliente.equals("IMAGEN")) {
                        receiveImage(); // Método para recibir y guardar la imagen
                    } else {
                        broadcast(mensajeCliente);
                    }
                } while (!mensajeCliente.equals("cerrar"));

                broadcast("El usuario " + nombreUsuario + " ha abandonado el chat.");

                clients.remove(this);
                socketCliente.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void broadcast(String mensaje) {
            for (ClienteHandler cliente : clients) {
                if (cliente != this) {
                    cliente.salidaCliente.println(mensaje);
                }
            }
        }

        private void receiveImage() throws IOException {
            DataInputStream dataInputStream = new DataInputStream(socketCliente.getInputStream());

            // Leer tamaño de la imagen
            int imageSize = dataInputStream.readInt();

            // Leer los bytes de la imagen
            byte[] imageData = new byte[imageSize];
            dataInputStream.readFully(imageData);

            // Guardar la imagen en el servidor
            saveImage(imageData);

            // Notificar a todos los clientes que se ha recibido una imagen
            broadcast("Se ha recibido una nueva imagen de " + nombreUsuario);
        }

        private void saveImage(byte[] imageData) throws IOException {
            File directory = new File(IMAGE_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String imageName = "image_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(directory, imageName);
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            fileOutputStream.write(imageData);
            fileOutputStream.close();
        }
    }
}
