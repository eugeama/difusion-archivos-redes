package metodos;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class EncriptacionSimetrica {

    public static class EncryptedData {
        public final byte[] nombre;
        public final byte[] datos;
        public final byte[] clave;

        public EncryptedData(byte[] nombre, byte[] datos, byte[] clave) {
            this.nombre = nombre;
            this.datos = datos;
            this.clave = clave;
        }
    }

    public static byte[] encriptarClavePublica(SecretKey clAleatoria, KeyPair parLlave) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher tipoCifrado=Cipher.getInstance("RSA");
        tipoCifrado.init(Cipher.ENCRYPT_MODE, parLlave.getPublic());

        return tipoCifrado.doFinal(clAleatoria.getEncoded());
    }

    public static EncryptedData encriptarClaveAleatoria(byte[] dataNombre, byte[] dataArchivo, SecretKey claveAleatoria, KeyPair parLlave) throws Exception {
        Cipher tipoCifrado=Cipher.getInstance("AES");

        tipoCifrado.init(Cipher.ENCRYPT_MODE,claveAleatoria);

        byte[] nombreEncriptado= tipoCifrado.doFinal(dataNombre);
        byte[] datosEncriptados= tipoCifrado.doFinal(dataArchivo);
        byte[] claveEncriptada= encriptarClavePublica(claveAleatoria, parLlave);

        ClienteHandler.mandarArchivo(nombreEncriptado, datosEncriptados, claveEncriptada);
        return null;
    }
}
