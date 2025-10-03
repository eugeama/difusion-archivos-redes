import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;

class ClienteHandler implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private HashSet<ClienteHandler> clientes;

    public ClienteHandler(Socket socket, HashSet<ClienteHandler> clientes) {
        this.socket = socket;
        this.clientes = clientes;
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // para hacer que la comunicacion con el server sea broadcast
    public void broadcast(String nombreArchivo, byte[] dataArchivo, ClienteHandler emisor) {
        for (ClienteHandler cliente : clientes) {
            if (cliente != emisor) {
                cliente.mandarArchivo(nombreArchivo, dataArchivo);
            }
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
                broadcast(nombreArchivo, dataArchivo, this);
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