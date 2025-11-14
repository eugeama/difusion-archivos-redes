package metodos2;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Scanner;

public class Cliente {

    public static void crearCarpeta(File carpetaEnviado) {
        if (!carpetaEnviado.exists() || !carpetaEnviado.isDirectory()) {
            carpetaEnviado.mkdirs();
        }
    }

    // --- nuevo: enviar la clave pública del cliente al servidor ---
    public static void enviarPublicKey(DataOutputStream out, KeyPair myPair) throws IOException {
        byte[] myPub = myPair.getPublic().getEncoded();
        out.writeInt(myPub.length);
        out.write(myPub);
        out.flush();
    }

    // --- nuevo: recibe la clave pública del servidor (handshake) ---
    public static PublicKey recibirPublicKeyServidor(DataInputStream in) throws Exception {
        int lenServerPub = in.readInt();
        byte[] serverPubBytes = new byte[lenServerPub];
        in.readFully(serverPubBytes);
        return EncriptacionYDesencriptacion.bytesToPublicKey(serverPubBytes);
    }

    // Mantengo la implementación original de mandarArchivos (no la transformé a streaming)
    public static void mandarArchivos(File archivo, DataOutputStream out) throws IOException {
        byte[] dataArchivo = new byte[(int) archivo.length()];
        try (FileInputStream fis = new FileInputStream(archivo)) {
            int leidos = 0;
            while (leidos < dataArchivo.length) {
                int n = fis.read(dataArchivo, leidos, dataArchivo.length - leidos);
                if (n < 0) break;
                leidos += n;
            }
        }

        // Protocolo simple: nombre, tamaño, bytes
        out.writeUTF(archivo.getName());
        out.writeLong(dataArchivo.length);
        out.write(dataArchivo);
        out.flush();
    }

    // --- nuevo: loop que pide rutas y manda archivos ---
    public static void loopEnviarArchivos(Scanner scanner, DataOutputStream out) throws IOException {
        while (true) {
            System.out.print("Ingrese la ruta del archivo para enviar (o 'salir'): ");
            String ruta = scanner.nextLine();
            if ("salir".equalsIgnoreCase(ruta)) break;

            File archivo = new File(ruta);
            if (!archivo.exists() || !archivo.isFile()) {
                System.out.println("Archivo no encontrado. Intente otra ruta.");
                continue;
            }
            mandarArchivos(archivo, out);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese la ruta de la carpeta 'enviado' (local): ");
        String rutaCarpeta = scanner.nextLine();
        File carpetaEnviado = new File(rutaCarpeta);
        crearCarpeta(carpetaEnviado);

        try (Socket socket = new Socket(args[0], 5000)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Genero par de llaves del cliente y envío la pública al servidor
            KeyPair myPair = LlavePubPriv.generarYRetornarParLlaves();
            enviarPublicKey(out, myPair);

            // Recibo la clave pública del servidor (handshake)
            PublicKey serverPublicKey = recibirPublicKeyServidor(in);

            // Carpetas donde guardar archivos recibidos (las pedías que se mantengan)
            File carpetaSim = new File("recibidos_sim");
            File carpetaAsim = new File("recibidos_asim");
            if (!carpetaSim.exists()) carpetaSim.mkdirs();
            if (!carpetaAsim.exists()) carpetaAsim.mkdirs();

            // Hilo receptor local para guardar archivos que lleguen del servidor
            // Nota: aquí cambiamos sólo la variable local; todavía pasamos carpetaSim y carpetaAsim (comportamiento igual)
            ReceptorArchivo receptor = new ReceptorArchivo(in, carpetaEnviado, carpetaAsim, myPair.getPrivate(), serverPublicKey);
            new Thread(receptor).start();

            // Loop para enviar archivos
            loopEnviarArchivos(scanner, out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
