package metodos;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

public class  ServerBroadcast {
    private int puerto;
    private KeyPair parLlave;
    private SecretKey claveAleatoria;
    private HashSet<ClienteHandler> clientes = new HashSet<>();

    public ServerBroadcast(int puerto, HashSet<ClienteHandler> clientes) throws NoSuchAlgorithmException {
        this.puerto = puerto;
        this.claveAleatoria= LlaveAleatoria.generarLlave();
        this.parLlave= LlavePubPriv.generarYRetornarParLlaves();
        this.clientes = clientes;
    }

    public ServerBroadcast(int puerto) {
        this.puerto = puerto;

    }

    public void agregarClientes(Socket puertoCliente) throws NoSuchAlgorithmException {
        ClienteHandler handler = new ClienteHandler(puertoCliente, clientes);
        clientes.add(handler);
        new Thread(handler).start();
        System.out.println("Nuevo cliente conectado: " + puertoCliente.getInetAddress());
    }

    public void iniciarPuerto() {
        System.out.println("Servidor iniciado en el puerto " + puerto);

        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            while (true) {
                Socket socketCliente = serverSocket.accept();
                //hilo para controlar al cliente cuando se conecta
                agregarClientes(socketCliente);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ServerBroadcast server = new ServerBroadcast(5000);
        server.iniciarPuerto();
    }

}
