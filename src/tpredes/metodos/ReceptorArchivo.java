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
        //tipoCifrado.init(Cipher.DECRYPT_MODE,"faltalallaveserver");
    }

    public void leerArchivo() throws IOException {
        String tipo = in.readUTF();
        String nombreArchivo = in.readUTF();
        long tamanioArchivo = in.readLong();
        byte[] dataArchivo = new byte[(int) tamanioArchivo];
        in.readFully(dataArchivo);
       determinarArchivos(tipo, nombreArchivo, tamanioArchivo, dataArchivo);
    }

    public void determinarArchivos(String tipo, String nombre, Long tamaño, byte[] datos) {
        if (tipo.equals("SIM")) {
            System.out.println("Recibido archivo cifrado simetrica: " + nombre);
            desencriptarSim(dataArchivo);
        } else if (tipo.equals("ASIM")) {
            System.out.println("Recibido archivo cifrado asimetrica: " + nombre);
            verificarFirmaAsim(dataArchivo);
        } else {
            System.out.println("Tipo de archivo desconocido: " + tipo);
        }

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
      //  try {
        //    while (true) {
               // desencriptar();
          //  }
       // } catch (IOException e) {
           // System.out.println("Conexión cerrada");
    //    }
    }

}
