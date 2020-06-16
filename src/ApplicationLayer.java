import javax.swing.*;
import javax.swing.plaf.synth.SynthMenuBarUI;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * La couche application permet de convertir le fichier
 * envoyer par le programme client en donnée qui sont
 * transmit a la couche transport ou de transcrire le information
 * passer par la couche transport
 */

public class ApplicationLayer implements LayersCommunication {
    LayersCommunication downLayer;

    public ApplicationLayer(int myPort, boolean withErrorGenerator) {
        downLayer= new TransportLayer( myPort,  withErrorGenerator, this);
    }

    /**
     * Cette fonction permet passser le fichier reçue
     * par le programmme client en un adress d'un buffer dans
     * la couche inférieur(couche transport)
     *
     * @param portDestinataire numéro du port du destinataire
     * @param IPdestinataire l'adress ip du destinataire
     * @param buf les donné transmie par le programe client
     * @throws IOException
     */

    @Override
    public void send(int portDestinataire,byte[] IPdestinataire,byte[] buf) throws IOException, TransmissionErrorException {

        //On convertie le tableau de byte en string
        String filePath = new String(buf);

        //Extraction du nom du fichier et de sa longueur
        File file = new File(filePath);
        String fileName = file.getName();
        byte[] name = fileName.getBytes();
        //System.out.println(fileName);
        byte nameLength = Integer.valueOf(name.length).byteValue();

        //On stock les bytes du fichier
        byte[] data = Files.readAllBytes(Paths.get(file.toURI()));

        //Combinaison des arrays
        byte[] allByteArray = new byte[1+name.length+data.length];
        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(nameLength).put(name).put(data);

        //Envoie des bytes en la couche en dessous
        downLayer.send(portDestinataire,IPdestinataire,allByteArray);

    }

    /**
     * Cette fonction receoit un arrays de byte qui est transmie
     * par la couche inférieur (couche transport) et cette fonction a pour
     * bus d'écrire les données reçu.
     *
     * @param portSource numéro du port de la source
     * @param IPsource l'adress Ip de la source
     * @param buf les données transmie par lacouche transprt
     */

    @Override
    public void receive(int portSource, byte[] IPsource,byte[] buf) {

        // Split the byte arrays to get name and the data
        byte[] fileName = getFileName(buf);
        byte[] fileData = getData(buf);

        // Change byte in string
        String name = new String(fileName);
        String data = new String(fileData);

        // Write a file
        try {
            File file = new File("data",name);
            // if file doesnt exists, then create it
            file.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(file,false);
            fileOutputStream.write(fileData);
            fileOutputStream.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }



    }

    @Override
    public void listen() throws IOException, TransmissionErrorException {
        downLayer.listen();
    }


    private static int getFileNameLength(byte[] buf){
        // Return the lenght of the Name
        return Byte.valueOf(buf[0]).intValue();
    }

    private static byte[] getFileName(byte[] buf){
        int fileNameLength = getFileNameLength(buf);
        // Return the name of the file
        return Arrays.copyOfRange(buf,1,1+fileNameLength);
    }

    private static byte[] getData(byte[] buf) {
        int fileNameLength = getFileNameLength(buf);
        // Return the data of the file
        return Arrays.copyOfRange(buf,1+fileNameLength,buf.length);
    }

}
