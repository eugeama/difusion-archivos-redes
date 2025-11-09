package metodos2;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

public class ReceptorArchivo implements Runnable {
    private final DataInputStream in;
    private final File carpetaSim;
    private final File carpetaAsim;
    private final PrivateKey myPrivateKey;   // para descifrar la clave AES enviada por el servidor
    private final PublicKey serverPublicKey; // para verificar firmas

    public ReceptorArchivo(DataInputStream in, File carpetaSim, File carpetaAsim, PrivateKey myPrivateKey, PublicKey serverPublicKey) {
        this.in = in;
        this.carpetaSim = carpetaSim;
        this.carpetaAsim = carpetaAsim;
        this.myPrivateKey = myPrivateKey;
        this.serverPublicKey = serverPublicKey;

        if (!carpetaSim.exists()) carpetaSim.mkdirs();
        if (!carpetaAsim.exists()) carpetaAsim.mkdirs();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String tipo = in.readUTF(); // "SIM" (implementado) u otros
                if ("SIM".equals(tipo)) {
                    // leo nombre cifrado
                    int lenNombreEnc = in.readInt();
                    byte[] nombreEnc = new byte[lenNombreEnc];
                    in.readFully(nombreEnc);

                    long lenDatosEnc = in.readLong();
                    byte[] datosEnc = new byte[(int) lenDatosEnc];
                    in.readFully(datosEnc);

                    int lenClaveAES = in.readInt();
                    byte[] claveAEScifrada = new byte[lenClaveAES];
                    in.readFully(claveAEScifrada);

                    int lenFirmaNombre = in.readInt();
                    byte[] firmaNombre = new byte[lenFirmaNombre];
                    in.readFully(firmaNombre);

                    int lenFirmaDatos = in.readInt();
                    byte[] firmaDatos = new byte[lenFirmaDatos];
                    in.readFully(firmaDatos);

                    // descifrar clave AES con mi privada
                    SecretKey claveAES = EncriptacionYDesencriptacion.desencriptarClaveRSA(claveAEScifrada, myPrivateKey);

                    // descifrar nombre y datos con AES
                    byte[] nombreBytes = EncriptacionYDesencriptacion.desencriptarConAES(nombreEnc, claveAES);
                    byte[] dataBytes = EncriptacionYDesencriptacion.desencriptarConAES(datosEnc, claveAES);
                    String nombreArchivo = new String(nombreBytes, StandardCharsets.UTF_8);

                    // verificar firmas: calculo hashes y verifico con la pub del servidor
                    byte[] hNombre = EncriptacionYDesencriptacion.hashear(nombreBytes);
                    byte[] hDatos = EncriptacionYDesencriptacion.hashear(dataBytes);

                    boolean okNombre = EncriptacionYDesencriptacion.verificarFirma(hNombre, firmaNombre, serverPublicKey);
                    boolean okDatos = EncriptacionYDesencriptacion.verificarFirma(hDatos, firmaDatos, serverPublicKey);

                    if (okNombre && okDatos) {
                        guardarArchivo(nombreArchivo, dataBytes, carpetaSim);
                        System.out.println("Archivo recibido y verificado: " + nombreArchivo);
                    } else {
                        System.out.println("Fallo de verificación de firma para archivo: " + nombreArchivo);
                        // opcional: guardar en carpeta de rechazados
                    }
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

    private void guardarArchivo(String nombreArchivo, byte[] datosArchivo, File carpeta) {
        File archivo = new File(carpeta, nombreArchivo);
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            fos.write(datosArchivo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
