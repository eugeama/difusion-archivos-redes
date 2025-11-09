package metodos2;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class EncriptacionYDesencriptacion {

    // Convierte bytes a PublicKey RSA
    public static PublicKey bytesToPublicKey(byte[] pubBytes) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(pubBytes);
        return keyFactory.generatePublic(spec);
    }

    // Encripta una clave AES con RSA pública (destino)
    public static byte[] encriptarClaveRSA(SecretKey claveAES, PublicKey publicoDestino) throws Exception {
        Cipher rsa = Cipher.getInstance("RSA");
        rsa.init(Cipher.ENCRYPT_MODE, publicoDestino);
        return rsa.doFinal(claveAES.getEncoded());
    }

    // Desencripta la clave AES (bytes) con la privada del receptor y devuelve SecretKey
    public static SecretKey desencriptarClaveRSA(byte[] claveCifrada, PrivateKey privada) throws Exception {
        Cipher rsa = Cipher.getInstance("RSA");
        rsa.init(Cipher.DECRYPT_MODE, privada);
        byte[] keyBytes = rsa.doFinal(claveCifrada);
        return new SecretKeySpec(keyBytes, "AES");
    }

    // Encriptar con AES (usamos AES/ECB/PKCS5Padding por simplicidad, se puede mejorar)
    public static byte[] encriptarConAES(byte[] datos, SecretKey clave) throws Exception {
        Cipher aes = Cipher.getInstance("AES");
        aes.init(Cipher.ENCRYPT_MODE, clave);
        return aes.doFinal(datos);
    }

    public static byte[] desencriptarConAES(byte[] datos, SecretKey clave) throws Exception {
        Cipher aes = Cipher.getInstance("AES");
        aes.init(Cipher.DECRYPT_MODE, clave);
        return aes.doFinal(datos);
    }

    // Firmar bytes (se espera que se pase el hash de los datos)
    public static byte[] firmar(byte[] datos, PrivateKey privada) throws Exception {
        Signature firma = Signature.getInstance("SHA256withRSA");
        firma.initSign(privada);
        firma.update(datos);
        return firma.sign();
    }

    public static boolean verificarFirma(byte[] datos, byte[] firmaBytes, PublicKey publica) throws Exception {
        Signature firma = Signature.getInstance("SHA256withRSA");
        firma.initVerify(publica);
        firma.update(datos);
        return firma.verify(firmaBytes);
    }

    public static byte[] hashear(byte[] datos) throws Exception {
        return Hash.hashearDatos(datos);
    }
}
