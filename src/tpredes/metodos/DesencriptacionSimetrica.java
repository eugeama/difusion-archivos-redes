package metodos;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DesencriptacionSimetrica {
    private ReceptorArchivo receptorArchivo;

    //desencriptacion clave aleatoria
    public SecretKey desencriptarClaveAleatoria(SecretKey claveAleatoria) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, claveAleatoria);

        return claveAleatoria;
    }

    //desencriptacion del archivo con la clave aleatoria
    /*public byte[] desencriptarSimetricamenteDataArchivo(byte[] dataArchivo, SecretKey claveAleatoria) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, claveAleatoria);

        return cipher.doFinal(dataArchivo);
    }

    public byte[] desencriptarSimetricamenteNombreArchivo(byte[] nombreArchivo, SecretKey claveAleatoria) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, claveAleatoria);

        return cipher.doFinal(nombreArchivo);
    }*/

    public void desencriptarSimetricamenteArchivo(byte[] nombreArchivo, byte[]dataArchivo, SecretKey claveAleatoria) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, claveAleatoria);

        byte[] nombreArchivoDesencriptado = cipher.doFinal(nombreArchivo);
        byte[] dataArchivoDesencriptado = cipher.doFinal(dataArchivo);
        String nombreArchivoStr = new String(nombreArchivoDesencriptado, StandardCharsets.UTF_8);

        receptorArchivo.guardarArchivo(nombreArchivoStr, dataArchivoDesencriptado, "SIM");

    }
}
