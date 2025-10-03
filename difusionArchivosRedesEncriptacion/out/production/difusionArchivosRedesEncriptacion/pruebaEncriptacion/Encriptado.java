package pruebaEncriptacion;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

public class Encriptado {
    private String textoParaEncriptar;
    private HashSet<Llave> llaves;

    public Encriptado(String textoParaEncriptar) {
        this.textoParaEncriptar = textoParaEncriptar;
    }

    public static void main(String[] args) {
        Llave llave= new Llave();
        Encriptado e = new Encriptado("anasheee");
        System.out.println(llave.randomString(e));
        System.out.println(llave.crearLlave(e));
    }
}
