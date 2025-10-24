package metodos;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    private static final MessageDigest tipoHash;
    static {
        try {
            tipoHash = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] hashearDatos(byte[] datos) {
        return tipoHash.digest(datos);

    }
}
