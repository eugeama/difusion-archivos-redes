package metodos;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class DesencriptacionSimetrica {

    //desencriptacion clave aleatoria
    public SecretKey desencriptarClaveAleatoria(SecretKey claveAleatoria) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, claveAleatoria);

        return claveAleatoria;
    }

    //desencriptacion del archivo con la clave aleatoria
    public byte[] desencriptarSimetricamenteDataArchivo(byte[] dataArchivo, SecretKey claveAleatoria) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, claveAleatoria);

        return cipher.doFinal(dataArchivo);
    }

    public byte[] desencriptarSimetricamenteNombreArchivo(byte[] nombreArchivo, SecretKey claveAleatoria) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, claveAleatoria);

        return cipher.doFinal(nombreArchivo);
    }
}
