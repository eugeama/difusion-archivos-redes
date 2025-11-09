package metodos2;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class LlavePubPriv {

    public static KeyPair generarYRetornarParLlaves() throws NoSuchAlgorithmException {
        KeyPairGenerator generadorLlaves = KeyPairGenerator.getInstance("RSA");
        generadorLlaves.initialize(2048);
        return generadorLlaves.generateKeyPair();
    }
}
