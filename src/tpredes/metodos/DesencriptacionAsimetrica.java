package metodos;

import javax.crypto.Cipher;
import java.io.File;
import java.security.PublicKey;

public class DesencriptacionAsimetrica {
    private LlavePubPriv parLlaves;

    public static byte[] desencriptarAsimetricamenteDataArchivo(byte[] dataArchivo, PublicKey publicaEmisor) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicaEmisor);

        return cipher.doFinal(dataArchivo);
    }

    public static byte[] desencriptarAsimetricamenteNombreArchivo(byte[]nombreArchivo, PublicKey publicaEmisor) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicaEmisor);

        return cipher.doFinal(nombreArchivo);
    }
}
