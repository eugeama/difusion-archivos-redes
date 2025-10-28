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
    private static DataOutputStream outSimetrico;
    private static DataOutputStream outAsimetrico;
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
            this.outSimetrico = new DataOutputStream(socket.getOutputStream());
            this.outAsimetrico = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClienteHandler() throws NoSuchAlgorithmException {
        this.socket = new Socket();
        this.clientes = new HashSet<>();
        this.claveAleatoria= LlaveAleatoria.generarLlave();
        this.parLlave= LlavePubPriv.generarYRetornarParLlaves();
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.outSimetrico = new DataOutputStream(socket.getOutputStream());
            this.outAsimetrico= new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public static DataOutputStream getOutSimetrico() {
        return outSimetrico;
    }

    public static void setOutSimetrico(DataOutputStream outSimetrico) {
        ClienteHandler.outSimetrico = outSimetrico;
    }

    public static DataOutputStream getOutAsimetrico() {
        return outAsimetrico;
    }

    public static void setOutAsimetrico(DataOutputStream outAsimetrico) {
        ClienteHandler.outAsimetrico = outAsimetrico;
    }

    public static HashSet<ClienteHandler> getClientes() {
        return clientes;
    }

    public static void setClientes(HashSet<ClienteHandler> clientes) {
        ClienteHandler.clientes = clientes;
    }

    public SecretKey getClaveAleatoria() {
        return claveAleatoria;
    }

    public void setClaveAleatoria(SecretKey claveAleatoria) {
        this.claveAleatoria = claveAleatoria;
    }

    public KeyPair getParLlave() {
        return parLlave;
    }

    public void setParLlave(KeyPair parLlave) {
        this.parLlave = parLlave;
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

        encriptarDatosSim(datosNombre, dataArchivo);
        encriptarDatosAsimetrico(datosNombre, dataArchivo);
    }

    public void encriptarDatosSim(byte[] datosNombre, byte[] datosArchivo) throws Exception {
        EncriptacionSimetrica encripSim= new EncriptacionSimetrica();
        encripSim.encriptarClaveAleatoria(datosNombre, datosArchivo, getClaveAleatoria(), getParLlave());
    }

    public void mandarMensajeSimetrico(byte[] nombreEncPub, byte[] datosEncPub, byte[] llaveAlEncPub) {
        try {
            outSimetrico.writeUTF("SIM");
            outSimetrico.writeUTF(Arrays.toString(nombreEncPub));
            outSimetrico.writeLong(datosEncPub.length);
            outSimetrico.write(datosEncPub);
            outSimetrico.write(llaveAlEncPub);
            outSimetrico.flush();
        } catch (IOException e) {
            clientes.remove(this);
        }
    }

    public void encriptarDatosAsimetrico(byte[] datosNombre, byte[] datosArchivo) throws Exception{
        EncriptacionAsimetrica encripAsim= new EncriptacionAsimetrica();

        encripAsim.hashearYFirmar(datosNombre, datosArchivo);
    }

    public void mandarMensajeAsimetrico(byte[] nombreFirmado, byte[] datosFirmado) {
        try {
            outAsimetrico.writeUTF("ASIM");
            outAsimetrico.writeUTF(Arrays.toString(nombreFirmado));
            outAsimetrico.writeLong(datosFirmado.length);
            outAsimetrico.write(datosFirmado);
            outAsimetrico.flush();
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