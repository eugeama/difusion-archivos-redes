package metodos;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Llave {
    static SecretKey llave;

    public Llave(SecretKey llave) {
        this.llave = llave;
    }

    public Llave() {
    }

    public SecretKey getLlave() {
        return llave;
    }

    public void setLlave(SecretKey llave) {
        this.llave = llave;
    }

    public SecretKey generarLlave() throws NoSuchAlgorithmException {
        try {
            KeyGenerator generadorDeLlave = KeyGenerator.getInstance("AES");
            generadorDeLlave.init(128);
            return generadorDeLlave.generateKey();
        }
        catch(NoSuchAlgorithmException excepcionSinAlgortimo) {
            System.out.println(excepcionSinAlgortimo.getMessage());
            return null;
        }
    }


}
