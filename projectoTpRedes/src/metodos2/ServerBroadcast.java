package metodos2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

public class ServerBroadcast {
    private final int puerto;
    private final KeyPair parLlave;           // llaves del servidor para firmar
    private final HashSet<ClienteHandler> clientes = new HashSet<>();

    public ServerBroadcast(int puerto) throws NoSuchAlgorithmException {
        this.puerto = puerto;
        this.parLlave = LlavePubPriv.generarYRetornarParLlaves();
    }

    public void iniciarPuerto() {
        System.out.println("Servidor iniciado en el puerto " + puerto);
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            while (true) {
                Socket socketCliente = serverSocket.accept();
                agregarCliente(socketCliente);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void agregarCliente(Socket socketCliente) throws NoSuchAlgorithmException, IOException {
        ClienteHandler handler = new ClienteHandler(socketCliente, clientes, parLlave);
        clientes.add(handler);
        new Thread(handler).start();
        System.out.println("Nuevo cliente conectado: " + socketCliente.getRemoteSocketAddress());
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        ServerBroadcast server = new ServerBroadcast(5000);
        server.iniciarPuerto();
    }
}
