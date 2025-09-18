import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class ServerBroadcast {
    private int puerto;
    private HashSet<ClienteHandler> clientes = new HashSet<>();

    public void ejecutar() {
        System.out.println("Servidor iniciado en el puerto " + puerto);

        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            while (true) {
                Socket socketCliente = serverSocket.accept();
                //hilo para controlar al cliente cuando se conecta
                ClienteHandler handler = new ClienteHandler(socketCliente, clientes);
                clientes.add(handler);
                new Thread(handler).start();
                System.out.println("Nuevo cliente conectado: " + socketCliente.getInetAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerBroadcast(int puerto) {
        this.puerto = puerto;
    }

    public static void main(String[] args) {
        ServerBroadcast server = new ServerBroadcast(5000);
        server.ejecutar();
    }

}
