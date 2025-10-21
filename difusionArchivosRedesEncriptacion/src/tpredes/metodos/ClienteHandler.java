package metodos;

import javax.crypto.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.HashSet;

class ClienteHandler implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private static DataOutputStream out;
    private static HashSet<ClienteHandler> clientes;
    private SecretKey claveAleatoria;
    private KeyPair parLlave;

    public ClienteHandler(Socket socket, HashSet<ClienteHandler> clientes) throws NoSuchAlgorithmException {
        this.socket = socket;
        this.clientes = clientes;
        this.claveAleatoria= LlaveAleatoria.generarLlave();
        this.parLlave= LlavePubPriv.generarYRetornarParLlaves();
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // para hacer que la comunicacion con el server sea broadcast
    public void mandarArchivosCLientes(ClienteHandler emisor) throws Exception {
        for (ClienteHandler cliente : clientes) {
            if (cliente != emisor) {
                cliente.asignarDatos();
            }
        }
    }

    public void asignarDatos() throws Exception{
        String nombreArchivo = in.readUTF();
        byte[] datosNombre= nombreArchivo.getBytes();

        long tamanioArchivo = in.readLong();
        byte[] dataArchivo = new byte[(int) tamanioArchivo];
        in.readFully(dataArchivo);

        EncriptacionSimetrica.encriptarClaveAleatoria(datosNombre, dataArchivo, claveAleatoria, parLlave);
    }

    public static void mandarArchivo(byte[] nombreEncPub, byte[] datosEncPub, byte[] llaveAlEncPub) {
        try {
            out.writeUTF(Arrays.toString(nombreEncPub));
            out.writeLong(datosEncPub.length);
            out.write(datosEncPub);
            out.write(llaveAlEncPub);
            out.flush();
        } catch (IOException e) {
            clientes.remove(this);
        }
    }

    public void run() {
        try {
            while (true) {
                mandarArchivosCLientes(this);
                //no se si dejar esto
                //System.out.println("Archivo recibido: " + nombreArchivo + " (" + tamanioArchivo + " bytes)");
                //mandarArchivosACLientes(nombreArchivo, dataArchivo, this);
            }
        } catch (Exception e) {
            //no se si dejar esto tampoco
            //System.out.println("metodos.Cliente desconectado: " + socket.getInetAddress());
            clientes.remove(this);
        }
    }
}