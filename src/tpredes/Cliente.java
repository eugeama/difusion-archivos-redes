
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    private static final String SERVER = "192.168.0.99"; // Cambiar IP si no es local
    private static final int PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Pedir al usuario la ruta de la carpeta "enviado"
        System.out.print("Ingrese la ruta de la carpeta 'enviado': ");
        String carpetaPath = scanner.nextLine();
        File carpetaEnviado = new File(carpetaPath);

        if (!carpetaEnviado.exists() || !carpetaEnviado.isDirectory()) {
            System.out.println("La carpeta no existe o no es válida");
            System.out.println("Creando la carpeta");
            carpetaEnviado.mkdirs();
        }

        try (Socket socket = new Socket(SERVER, PORT)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Thread para recibir archivos
            ReceptorArchivo receptorArchivo = new ReceptorArchivo(in, carpetaEnviado);
            Thread hiloReceptorArchivo = new Thread(receptorArchivo);
            hiloReceptorArchivo.start();

            // Scanner para enviar archivos manualmente
            while (true) {
                System.out.print("Ingrese la ruta del archivo para enviar: ");
                String path = scanner.nextLine();
                File file = new File(path);

                if (!file.exists()) {
                    System.out.println("El archivo no existe");
                    continue;
                }

                byte[] fileData = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(fileData);
                }

                out.writeUTF(file.getName());
                out.writeLong(fileData.length);
                out.write(fileData);
                out.flush();

                System.out.println("Archivo enviado: " + file.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
