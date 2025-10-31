package metodos;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;

public class DesencriptacionSimetrica {

    //desencriptacion clave aleatoria
    public SecretKey desencriptarClaveAleatoria(SecretKey claveAleatoria) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, claveAleatoria);

        return claveAleatoria;
    }

    

}
