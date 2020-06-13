import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ApplicationLayer implements LayersCommunication {
    LayersCommunication downLayer;

    public ApplicationLayer() {
        downLayer= new TransportLayer();
    }


    @Override
    public void send(byte[] buf) throws IOException {
        //On convertie le tableau de byte en string
        String string = new String(buf);
        //On sépare les deux lignes de la string
        String[] lines = string.split("\n");



        //On sépare les chiffres de l'adresse
        String[] adressString = lines[1].split("\\.");

        //On stock les chiffres de l'adresse dans un array de 4 bytes
        byte[] adress = new byte[4];
        for(int i = 0;i<4; i++){
            adress[i] = Integer.valueOf(adressString[i]).byteValue();
        }

        //Extraction du nom du fichier et de sa longueur
        File file = new File(lines[0]);
        String fileName = file.getName();
        byte[] name = fileName.getBytes();
        byte nameLength = Integer.valueOf(name.length).byteValue();

        //On stock les bytes du fichier dont le nom est la première ligne de la string
        byte[] data = Files.readAllBytes(Paths.get(file.toURI()));

        //Combinaison des arrays
        byte[] allByteArray = new byte[adress.length+1+name.length+data.length];
        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(adress).put(nameLength).put(name).put(data);


        //Envoie des bytes en la couche en dessous
        downLayer.send(allByteArray);
    }

    @Override
    public void receive(byte[] buf) {

    }

}
