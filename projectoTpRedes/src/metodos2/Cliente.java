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
            byte[] myPub = myPair.getPublic().getEncoded();
            out.writeInt(myPub.length);
            out.write(myPub);
            out.flush();

            // Recibo la clave pública del servidor (handshake)
            int lenServerPub = in.readInt();
            byte[] serverPubBytes = new byte[lenServerPub];
            in.readFully(serverPubBytes);
            PublicKey serverPublicKey = EncriptacionYDesencriptacion.bytesToPublicKey(serverPubBytes);

            // Carpetas donde guardar archivos recibidos (las pedías que se mantengan)
            File carpetaSim = new File("recibidos_sim");
            File carpetaAsim = new File("recibidos_asim");
            if (!carpetaSim.exists()) carpetaSim.mkdirs();
            if (!carpetaAsim.exists()) carpetaAsim.mkdirs();

            // Hilo receptor local para guardar archivos que lleguen del servidor
            ReceptorArchivo receptor = new ReceptorArchivo(in, carpetaSim, carpetaAsim, myPair.getPrivate(), serverPublicKey);
            new Thread(receptor).start();

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
