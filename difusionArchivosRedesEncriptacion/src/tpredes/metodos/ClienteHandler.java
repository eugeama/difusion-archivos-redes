package metodos;

import javax.crypto.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashSet;

class ClienteHandler implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private HashSet<ClienteHandler> clientes;
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

        encriptar(datosNombre, dataArchivo);
    }

    public void encriptar(byte[] dataNombre, byte[] dataArchivo) throws Exception {
        Cipher tipoCifrado=Cipher.getInstance("AES");
        LlaveAleatoria generador = new LlaveAleatoria();
        SecretKey llaveAleatoriaSim = generador.generarLlave();

        tipoCifrado.init(Cipher.ENCRYPT_MODE,llaveAleatoriaSim);

        byte[] nombreEncriptado= tipoCifrado.doFinal(dataNombre);
        byte[] datosEncriptados= tipoCifrado.doFinal(dataArchivo);

        mandarArchivo(nombreEncriptado, datosEncriptados);
    }

    public void mandarArchivo(byte[] nombreEnc, byte[] datosEnc) {
        try {
            out.writeUTF(Arrays.toString(nombreEnc));
            out.writeLong(datosEnc.length);
            out.write(datosEnc);
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