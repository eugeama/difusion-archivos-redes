package metodos;

import javax.crypto.Cipher;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

public class DesencriptacionAsimetrica {
    private LlavePubPriv parLlaves;
    private ReceptorArchivo receptorArchivo;

    public void desencriptarAsimetricamenteArchivo(byte[]dataArchivo, byte[]nombreArchivo, PublicKey publicaEmisor) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicaEmisor);

        byte[] nombreArchivoDesencriptado = cipher.doFinal(nombreArchivo);
        byte[] dataArchivoDesencriptado = cipher.doFinal(dataArchivo);

        String nombreArchivoStr = new String(nombreArchivoDesencriptado, StandardCharsets.UTF_8);

        receptorArchivo.guardarArchivo(nombreArchivoStr, dataArchivoDesencriptado, "ASIM");
    }

}
