package metodos;

import java.security.*;

public class LlavePubPriv {

    public static KeyPair generarYRetornarParLlaves() throws NoSuchAlgorithmException {
        KeyPairGenerator generadorLlaves= KeyPairGenerator.getInstance("RSA");
        generadorLlaves.initialize(2048);

        return generadorLlaves.generateKeyPair();
    }

}
