package pruebaEncriptacion;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

public class Llave {
    private String llave;
    private HashSet<Llave> llaves;

    public Llave(String llave){
        this.llave= llave;
    }

    public Llave(){
        this.llave= "";
    }

    public String getLlave() {
        return llave;
    }

    public void setLlave(String llave) {
        this.llave = llave;
    }

    public String randomString(Encriptado e){
        String randomUUIDString = UUID.randomUUID().toString();
        return randomUUIDString;
    }

    public String crearLlave(Encriptado e){
        String llave = randomString(e) + LocalDateTime.now();
        for(Llave l: llaves){
            if(llave.equals(l)){
                llave= randomString(e) + LocalDateTime.now();
            }
        }
        return llave;

    }
}
