
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReceptorArchivo implements Runnable {
    private DataInputStream in;
    private File carpetaEnviado;

    @Override
    public void run() {
        try {
                    while (true) {
                        String fileName = in.readUTF();
                        long fileSize = in.readLong();

                        byte[] fileData = new byte[(int) fileSize];
                        in.readFully(fileData);

                        // Guardar archivo en la carpeta "enviado" indicada
                        File file = new File(carpetaEnviado, fileName);
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(fileData);
                        }
                        System.out.println("Archivo recibido y guardado en: " + file.getAbsolutePath());
                    }
                } catch (IOException e) {
                    System.out.println("Conexión cerrada");
                }
    }

    public ReceptorArchivo(DataInputStream in, File carpetaEnviado) {
        this.in = in;
        this.carpetaEnviado = carpetaEnviado;
    }
    
}
