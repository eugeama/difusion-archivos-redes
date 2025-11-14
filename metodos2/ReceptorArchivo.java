package metodos2;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

public class ReceptorArchivo implements Runnable {
    private final DataInputStream in;
    private final File carpetaEnviado; // antes carpetaSim (ahora usamos la carpeta pasada por Cliente)
    private final File carpetaAsim;
    private final PrivateKey myPrivateKey;   // para descifrar la clave AES enviada por el servidor
    private final PublicKey serverPublicKey; // para verificar firmas

    public ReceptorArchivo(DataInputStream in, File carpetaEnviado, File carpetaAsim, PrivateKey myPrivateKey, PublicKey serverPublicKey) {
        this.in = in;
        this.carpetaEnviado = carpetaEnviado;
        this.carpetaAsim = carpetaAsim;
        this.myPrivateKey = myPrivateKey;
        this.serverPublicKey = serverPublicKey;

        if (!carpetaEnviado.exists()) carpetaEnviado.mkdirs();
        if (!carpetaAsim.exists()) carpetaAsim.mkdirs();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String tipo = in.readUTF(); // "SIM" (implementado) u otros
                if ("SIM".equals(tipo)) {
                    MensajeSIM msg = leerMensajeSIM();
                    procesarMensajeSIM(msg);
                } else {
                    System.out.println("Tipo de mensaje desconocido o no implementado: " + tipo);
                }
            }
        } catch (IOException e) {
            System.out.println("Conexión cerrada en ReceptorArchivo.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- nuevo: contenedor de mensaje SIM ---
    private static class MensajeSIM {
        byte[] nombreEnc;
        byte[] datosEnc;
        byte[] claveAEScifrada;
        byte[] firmaNombre;
        byte[] firmaDatos;
    }

    // --- nuevo: leer todos los campos del mensaje SIM y devolver objeto MensajeSIM ---
    private MensajeSIM leerMensajeSIM() throws IOException {
        MensajeSIM msg = new MensajeSIM();

        int lenNombreEnc = in.readInt();
        msg.nombreEnc = new byte[lenNombreEnc];
        in.readFully(msg.nombreEnc);

        long lenDatosEnc = in.readLong();
        msg.datosEnc = new byte[(int) lenDatosEnc];
        in.readFully(msg.datosEnc);

        int lenClaveAES = in.readInt();
        msg.claveAEScifrada = new byte[lenClaveAES];
        in.readFully(msg.claveAEScifrada);

        int lenFirmaNombre = in.readInt();
        msg.firmaNombre = new byte[lenFirmaNombre];
        in.readFully(msg.firmaNombre);

        int lenFirmaDatos = in.readInt();
        msg.firmaDatos = new byte[lenFirmaDatos];
        in.readFully(msg.firmaDatos);

        return msg;
    }

    // --- nuevo: procesar el mensaje SIM (descifrar, verificar, guardar) ---
    private void procesarMensajeSIM(MensajeSIM msg) throws Exception {
        // descifrar clave AES con mi privada
        SecretKey claveAES = EncriptacionYDesencriptacion.desencriptarClaveRSA(msg.claveAEScifrada, myPrivateKey);

        // descifrar nombre y datos con AES
        byte[] nombreBytes = EncriptacionYDesencriptacion.desencriptarConAES(msg.nombreEnc, claveAES);
        byte[] dataBytes = EncriptacionYDesencriptacion.desencriptarConAES(msg.datosEnc, claveAES);
        String nombreArchivo = new String(nombreBytes, StandardCharsets.UTF_8);

        // verificar firmas: calculo hashes y verifico con la pub del servidor
        byte[] hNombre = EncriptacionYDesencriptacion.hashear(nombreBytes);
        byte[] hDatos = EncriptacionYDesencriptacion.hashear(dataBytes);

        boolean okNombre = EncriptacionYDesencriptacion.verificarFirma(hNombre, msg.firmaNombre, serverPublicKey);
        boolean okDatos = EncriptacionYDesencriptacion.verificarFirma(hDatos, msg.firmaDatos, serverPublicKey);

        if (okNombre && okDatos) {
            guardarArchivo(nombreArchivo, dataBytes, carpetaEnviado);
            System.out.println("Archivo recibido y verificado: " + nombreArchivo);
        } else {
            System.out.println("Fallo de verificación de firma para archivo: " + nombreArchivo);
            // opcional: guardar en carpeta de rechazados (carpetaAsim) o similar
        }
    }

    private void guardarArchivo(String nombreArchivo, byte[] datosArchivo, File carpeta) {
        File archivo = new File(carpeta, nombreArchivo);
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            fos.write(datosArchivo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
