package metodos;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class EncriptacionSimetrica {

    public byte[] encriptarClavePublica(SecretKey clAleatoria, KeyPair parLlave) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher tipoCifrado=Cipher.getInstance("RSA");
        tipoCifrado.init(Cipher.ENCRYPT_MODE, parLlave.getPublic());

        return tipoCifrado.doFinal(clAleatoria.getEncoded());
    }

    public void encriptarClaveAleatoria(byte[] dataNombre, byte[] dataArchivo, SecretKey claveAleatoria, KeyPair parLlave) throws Exception {
        Cipher tipoCifrado=Cipher.getInstance("AES");

        tipoCifrado.init(Cipher.ENCRYPT_MODE,claveAleatoria);

        byte[] nombreEncriptado= tipoCifrado.doFinal(dataNombre);
        byte[] datosEncriptados= tipoCifrado.doFinal(dataArchivo);
        byte[] claveEncriptada= encriptarClavePublica(claveAleatoria, parLlave);

        ClienteHandler manejoCliente= new ClienteHandler();
        manejoCliente.mandarMensajeSimetrico(nombreEncriptado, datosEncriptados, claveEncriptada);
    }

    public void hashearArchivoSimetrico(byte[]datosArchivo) throws NoSuchAlgorithmException {
        Hash hash = new Hash();
        hash.hashearDatos(datosArchivo);

    }
}
