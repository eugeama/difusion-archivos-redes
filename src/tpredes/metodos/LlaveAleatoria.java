package metodos;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class LlaveAleatoria {

    public static SecretKey generarLlave() throws NoSuchAlgorithmException {
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
