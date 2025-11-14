package metodos2;

import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.util.HashSet;
import java.util.Set;

public class ClienteHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Set<ClienteHandler> clientes;
    private final KeyPair servidorKeyPair; // llave del servidor (para firmar)
    private PublicKey clientePublicKey;    // pública del cliente (handshake)

    public ClienteHandler(Socket socket, Set<ClienteHandler> clientes, KeyPair servidorKeyPair) throws NoSuchAlgorithmException, IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.clientes = clientes;
        this.servidorKeyPair = servidorKeyPair;

        // Handshake: separamos la lógica en un método
        try {
            performHandshake();
        } catch (Exception e) {
            throw new IOException("Error en handshake con cliente: " + e.getMessage(), e);
        }
    }

    // --- nuevo: handshake extraído ---
    private void performHandshake() throws Exception {
        int len = in.readInt();
        byte[] pubBytes = new byte[len];
        in.readFully(pubBytes);
        this.clientePublicKey = EncriptacionYDesencriptacion.bytesToPublicKey(pubBytes);

        byte[] servidorPub = servidorKeyPair.getPublic().getEncoded();
        out.writeInt(servidorPub.length);
        out.write(servidorPub);
        out.flush();
    }

    public DataOutputStream getOut() {
        return out;
    }

    public PublicKey getClientePublicKey() {
        return clientePublicKey;
    }

    @Override
    public void run() {
        try {
            procesarEntradas();
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + socket.getRemoteSocketAddress());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            clientes.remove(this);
        }
    }

    // --- nuevo: loop principal extraído ---
    private void procesarEntradas() throws Exception {
        while (true) {
            String nombreArchivo = in.readUTF();
            long tamanio = in.readLong();
            if (tamanio > Integer.MAX_VALUE) {
                System.out.println("Archivo demasiado grande, se ignora: " + nombreArchivo);
                // Saltar
                in.skip(tamanio);
                continue;
            }
            byte[] data = new byte[(int) tamanio];
            in.readFully(data);

            broadcastArchivo(nombreArchivo, data);
        }
    }

    // --- nuevo: responsable de enviar a todos los destinos (broadcast) ---
    private void broadcastArchivo(String nombreArchivo, byte[] data) {
        for (ClienteHandler destino : new HashSet<>(clientes)) {
            if (destino == this) continue;
            try {
                enviarAUnDestino(destino, nombreArchivo, data);
            } catch (Exception ex) {
                System.out.println("Error enviando a cliente destino, lo removemos. " + ex.getMessage());
                clientes.remove(destino);
            }
        }
    }

    // --- nuevo: preparar y enviar mensaje SIM a un destino ---
    private void enviarAUnDestino(ClienteHandler destino, String nombreArchivo, byte[] data) throws Exception {
        // generar clave AES nueva para este destino
        SecretKey aes = LlaveAleatoria.generarLlave();

        // cifrar nombre y datos con AES
        byte[] nombreEnc = EncriptacionYDesencriptacion.encriptarConAES(nombreArchivo.getBytes(), aes);
        byte[] datosEnc = EncriptacionYDesencriptacion.encriptarConAES(data, aes);

        // cifrar la clave AES con la pública del destino
        byte[] claveAEScifrada = EncriptacionYDesencriptacion.encriptarClaveRSA(aes, destino.getClientePublicKey());

        // crear hashes y firmarlos con la llave privada del servidor
        byte[] nombreHash = EncriptacionYDesencriptacion.hashear(nombreArchivo.getBytes());
        byte[] datosHash = EncriptacionYDesencriptacion.hashear(data);
        byte[] firmaNombre = EncriptacionYDesencriptacion.firmar(nombreHash, servidorKeyPair.getPrivate());
        byte[] firmaDatos = EncriptacionYDesencriptacion.firmar(datosHash, servidorKeyPair.getPrivate());

        // Enviar mensaje con formato SIM:
        DataOutputStream outDestino = destino.getOut();
        synchronized (outDestino) {
            outDestino.writeUTF("SIM");
            outDestino.writeInt(nombreEnc.length);
            outDestino.write(safeBytes(nombreEnc));
            outDestino.writeLong(datosEnc.length);
            outDestino.write(datosEnc);
            outDestino.writeInt(claveAEScifrada.length);
            outDestino.write(claveAEScifrada);
            outDestino.writeInt(firmaNombre.length);
            outDestino.write(firmaNombre);
            outDestino.writeInt(firmaDatos.length);
            outDestino.write(firmaDatos);
            outDestino.flush();
        }
    }

    // pequeño helper para evitar nulls (antes 'nomeOrBytesSafe')
    private byte[] safeBytes(byte[] b) {
        return b == null ? new byte[0] : b;
    }
}
