package metodos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    private static final String servidor = "127.0.0.1";
    private static final int puerto = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // ingreso de la ruta de la carpeta "enviado"
        System.out.print("Ingrese la ruta de la carpeta 'enviado': ");
        String rutaCarpeta = scanner.nextLine();
        File carpetaEnviado = new File(rutaCarpeta);

        
        if (!carpetaEnviado.exists() || !carpetaEnviado.isDirectory()) {
            System.out.println("La carpeta no existe o no es válida");
            System.out.println("Creando la carpeta");
            carpetaEnviado.mkdirs();
        }

        try (Socket socket = new Socket(servidor, puerto)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // hilo para recibir los archivos
            ReceptorArchivo receptorArchivo = new ReceptorArchivo(in, carpetaEnviado);
            Thread hiloReceptorArchivo = new Thread(receptorArchivo);
            hiloReceptorArchivo.start();

            // ingreso de la rutade los archivos para enviarlos
            while (true) {
                System.out.print("Ingrese la ruta del archivo para enviar: ");
                String ruta = scanner.nextLine();
                File archivo = new File(ruta);

                if (!archivo.exists()) {
                    System.out.println("El archivo no existe");
                    continue;
                }

                byte[] dataArchivo = new byte[(int) archivo.length()];
                try (FileInputStream fis = new FileInputStream(archivo)) {
                    fis.read(dataArchivo);
                }

                out.writeUTF(archivo.getName());
                out.writeLong(dataArchivo.length);
                out.write(dataArchivo);
                out.flush();

                System.out.println("Archivo enviado: " + archivo.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
