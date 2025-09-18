import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerBroadcast {
    private static final int puerto = 5000;
    private static final Set<ClientHandler> clientes = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("Servidor iniciado en el puerto " + puerto);

        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            while (true) {
                Socket socketCliente = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socketCliente);
                clientes.add(handler);
                new Thread(handler).start();
                System.out.println("Nuevo cliente conectado: " + socketCliente.getInetAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // para hacer que la comunicacion con el server sea broadcast
    public static void broadcast(String nombreArchivo, byte[] dataArchivo, ClientHandler emisor) {
        for (ClientHandler cliente : clientes) {
            if (cliente != emisor) {
                cliente.mandarArchivo(nombreArchivo, dataArchivo);
            }
        }
    }


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

                    String nombreArchivo = in.readUTF();
                    long tamanioArchivo = in.readLong();
                    byte[] dataArchivo = new byte[(int) tamanioArchivo];
                    in.readFully(dataArchivo);

                    System.out.println("Archivo recibido: " + nombreArchivo + " (" + tamanioArchivo + " bytes)");
                    ServerBroadcast.broadcast(nombreArchivo, dataArchivo, this);
                }
            } catch (IOException e) {
                System.out.println("Cliente desconectado: " + socket.getInetAddress());
                clientes.remove(this);
            }
        }

        public void mandarArchivo(String nombreArchivo, byte[] dataArchivo) {
            try {
                out.writeUTF(nombreArchivo);
                out.writeLong(dataArchivo.length);
                out.write(dataArchivo);
                out.flush();
            } catch (IOException e) {
                System.out.println("Error al enviar archivo a cliente");
                clientes.remove(this);
            }
        }
    }
}
