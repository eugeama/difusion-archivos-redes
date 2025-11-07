package metodos;

import java.security.Signature;

public class EncriptacionAsimetrica {
    private ServerBroadcast server;
    private byte[] hashAsimetrico;

    public byte[] getHashAsimetrico() {
        return hashAsimetrico;
    }

    public ServerBroadcast getServer() {
        return server;
    }

    public void setServer(ServerBroadcast server) {
        this.server = server;
    }

    public void hashearYFirmar(byte[] dataNombre, byte[] dataArchivo) throws Exception {
        Hash hashear = new Hash();
        byte[] nombreHash= hashear.hashearDatos(dataNombre);
        byte[] datosHash= hashear.hashearDatos(dataArchivo);
        this.hashAsimetrico = hashear.hashearDatos(dataArchivo);

        crearFirmaYFirmar(nombreHash, datosHash);
    }

    public void crearFirmaYFirmar(byte[] nombreHash, byte[] datosHash) throws Exception {
        Signature firmaNombre= Signature.getInstance("SHA256WithRSA");
        firmaNombre.initSign(getServer().getParLlave().getPrivate());

        Signature firmaDatos= Signature.getInstance("SHA256WithRSA");
        firmaDatos.initSign(getServer().getParLlave().getPrivate());

        firmaNombre.update(nombreHash);
        firmaDatos.update(datosHash);

        byte[] firmaDigitalNombre= firmaNombre.sign();
        byte[] firmaDigitalDatos= firmaDatos.sign();

        ClienteHandler manejoCliente= new ClienteHandler();
        manejoCliente.mandarMensajeAsimetrico(firmaDigitalNombre, firmaDigitalDatos);
    }
}
