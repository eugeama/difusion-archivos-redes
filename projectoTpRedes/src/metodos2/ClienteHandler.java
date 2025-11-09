package metodos2;

import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.util.HashSet;

/**
 * Handler por cliente. Hace:
 * - Handshake: recibe la clave pública del cliente y devuelve la pública del servidor.
 * - Después: lee archivos enviados por su cliente y los retransmite (broadcast) a los demás.
 *   Para cada receptor: genera una clave AES, cifra nombre+datos, cifra la clave AES con la pública del receptor,
 *   firma hash del nombre y hash de datos con la llave privada del servidor, y envía todo al receptor.
 */
public class ClienteHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final HashSet<ClienteHandler> clientes;
    private final KeyPair servidorKeyPair; // llave del servidor (para firmar)
    private PublicKey clientePublicKey;    // pública del cliente (handshake)

    public ClienteHandler(Socket socket, HashSet<ClienteHandler> clientes, KeyPair servidorKeyPair) throws NoSuchAlgorithmException, IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.clientes = clientes;
        this.servidorKeyPair = servidorKeyPair;

        // Handshake: primero leo la clave pública del cliente
        try {
            int len = in.readInt();
            byte[] pubBytes = new byte[len];
            in.readFully(pubBytes);
            this.clientePublicKey = EncriptacionYDesencriptacion.bytesToPublicKey(pubBytes);

            // Luego envío la pub del servidor de vuelta para que el cliente pueda verificar firmas
            byte[] servidorPub = servidorKeyPair.getPublic().getEncoded();
            out.writeInt(servidorPub.length);
            out.write(servidorPub);
            out.flush();
        } catch (Exception e) {
            throw new IOException("Error en handshake con cliente: " + e.getMessage(), e);
        }
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
            while (true) {
                // leer archivo del cliente conectado (protocolo simple del Cliente.java)
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

                // Broadcast a los demás clientes
                for (ClienteHandler destino : new HashSet<>(clientes)) {
                    if (destino == this) continue;
                    try {
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
                        // "SIM" | int(lenNombreEnc) | nombreEnc | long(lenDatosEnc) | datosEnc
                        // | int(lenClaveAEScifrada) | claveAEScifrada
                        // | int(lenFirmaNombre) | firmaNombre | int(lenFirmaDatos) | firmaDatos
                        DataOutputStream outDestino = destino.getOut();
                        synchronized (outDestino) {
                            outDestino.writeUTF("SIM");
                            outDestino.writeInt(nombreEnc.length);
                            outDestino.write(nomeOrBytesSafe(nombreEnc));
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
                    } catch (Exception ex) {
                        System.out.println("Error enviando a cliente destino, lo removemos. " + ex.getMessage());
                        clientes.remove(destino);
                    }
                }
            }
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

    // remedio para evitar potential NullPointer si nombreEnc es null (no debería)
    private byte[] nomeOrBytesSafe(byte[] b) {
        return b == null ? new byte[0] : b;
    }
}
