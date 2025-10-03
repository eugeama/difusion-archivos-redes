
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
                String nombreArchivo = in.readUTF();
                long tamanioArchivo = in.readLong();
                byte[] dataArchivo = new byte[(int) tamanioArchivo];
                in.readFully(dataArchivo);

                // Guardar archivo en la carpeta "enviado" indicada
                File archivo = new File(carpetaEnviado, nombreArchivo);
                try (FileOutputStream fos = new FileOutputStream(archivo)) {
                    fos.write(dataArchivo);
                }
                System.out.println("Archivo recibido y guardado en: " + archivo.getAbsolutePath());
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
