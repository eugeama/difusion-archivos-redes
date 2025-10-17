package metodos;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ReceptorArchivo implements Runnable {
    private DataInputStream in;
    private File carpetaEnviado;

    public ReceptorArchivo(DataInputStream in, File carpetaEnviado) {
        this.in = in;
        this.carpetaEnviado = carpetaEnviado;
    }

    public void desencriptar() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher tipoCifrado=Cipher.getInstance("AES");
        tipoCifrado.init(Cipher.DECRYPT_MODE,"faltalallaveserver");
    }

    public void leerArchivo() throws IOException {
        String nombreArchivo = in.readUTF();
        long tamanioArchivo = in.readLong();
        byte[] dataArchivo = new byte[(int) tamanioArchivo];
        in.readFully(dataArchivo);

       guardarArchivo(nombreArchivo, dataArchivo);
    }

    public void guardarArchivo(String nombreArchivo, byte[]datosArchivo ){
        File archivo = new File(carpetaEnviado, nombreArchivo);
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            fos.write(datosArchivo);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                desencriptar();
            }
        } catch (IOException e) {
            System.out.println("Conexión cerrada");
        }
    }

}
