package metodos;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ReceptorArchivo implements Runnable {
    private DataInputStream in;
    private File carpetaEnviadoSimetrico;
    private File carpetaEnviadoAsimetrico;

    public ReceptorArchivo(DataInputStream in, File carpetaEnviadoSimetrico, File carpetaEnviadoAsimetrico) {
        this.in = in;
        this.carpetaEnviadoSimetrico = carpetaEnviadoSimetrico;
        this.carpetaEnviadoAsimetrico = carpetaEnviadoAsimetrico;
    }

    public ReceptorArchivo(DataInputStream in, File carpetaEnviado) {
        this.in = in;

    }

    public void desencriptar() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher tipoCifrado = Cipher.getInstance("AES");
        //tipoCifrado.init(Cipher.DECRYPT_MODE,"faltalallaveserver");
    }

    public void leerArchivo() throws IOException {
        String tipo = in.readUTF();
        String nombreArchivo = in.readUTF();
        long tamanioArchivo = in.readLong();
        byte[] dataArchivo = new byte[(int) tamanioArchivo];
        in.readFully(dataArchivo);
       guardarArchivo(nombreArchivo, dataArchivo, tipo);
    }

    public void guardarArchivo(String nombreArchivo, byte[]datosArchivo, String tipo){
        File archivo;
        if(tipo.equals("SIM")){
            archivo = new File(carpetaEnviadoSimetrico, nombreArchivo);
        }
        else {
            archivo = new File(carpetaEnviadoAsimetrico, nombreArchivo);
        }

        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            fos.write(datosArchivo);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

        public boolean hashesSonIguales(byte[] datosSimetrica, byte[] datosAsimetrica) throws NoSuchAlgorithmException {
        boolean verificacion = false;

        if(datosSimetrica==datosAsimetrica){
            verificacion = true;
        }

        return verificacion;
    }


    @Override
    public void run() {
        try {
            while(true) {
                String nombreArchivo = this.in.readUTF();
                long tamanioArchivo = this.in.readLong();
                byte[] dataArchivo = new byte[(int)tamanioArchivo];
                this.in.readFully(dataArchivo);
                File archivo = new File(this.carpetaEnviadoAsimetrico, nombreArchivo);
                try (FileOutputStream fos = new FileOutputStream(archivo)) {
                    fos.write(dataArchivo);
                }
                System.out.println("Archivo recibido y guardado en: " + archivo.getAbsolutePath());
            }
        } catch (IOException var11) {
            System.out.println("Conexión cerrada");
        }
    }
}