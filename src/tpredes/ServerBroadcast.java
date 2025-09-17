import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerBroadcast {
    private static final int PORT = 5000;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("Servidor iniciado en el puerto " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
                System.out.println("Nuevo cliente conectado: " + clientSocket.getInetAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para enviar archivo a todos los clientes
    public static void broadcastFile(String fileName, byte[] fileData, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) { // si no querés que se reenvíe al emisor
                client.sendFile(fileName, fileData);
            }
        }
    }

    // Handler para cada cliente
    static class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.in = new DataInputStream(socket.getInputStream());
                this.out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                while (true) {
                    // Recibir nombre del archivo
                    String fileName = in.readUTF();

                    // Recibir tamaño del archivo
                    long fileSize = in.readLong();

                    // Recibir contenido
                    byte[] fileData = new byte[(int) fileSize];
                    in.readFully(fileData);

                    System.out.println("Archivo recibido: " + fileName + " (" + fileSize + " bytes)");

                    // Reenviar a todos los clientes
                    ServerBroadcast.broadcastFile(fileName, fileData, this);
                }
            } catch (IOException e) {
                System.out.println("Cliente desconectado: " + socket.getInetAddress());
                clients.remove(this);
            }
        }

        public void sendFile(String fileName, byte[] fileData) {
            try {
                out.writeUTF(fileName);
                out.writeLong(fileData.length);
                out.write(fileData);
                out.flush();
            } catch (IOException e) {
                System.out.println("Error al enviar archivo a cliente.");
                clients.remove(this);
            }
        }
    }
}
