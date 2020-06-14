import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ApplicationLayer implements LayersCommunication {
    LayersCommunication downLayer;

    public ApplicationLayer(int myPort) {
        downLayer= new TransportLayer( myPort,this);
    }



    @Override
    public void send(int portDestinataire,byte[] IPdestinataire,byte[] buf) throws IOException {



        //On convertie le tableau de byte en string
        String filePath = new String(buf);

        //Extraction du nom du fichier et de sa longueur
        File file = new File(filePath);
        String fileName = file.getName();
        byte[] name = fileName.getBytes();
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

    @Override
    public void receive(int portSource, byte[] IPsource,byte[] buf) {

    }

    @Override
    public void listen() throws IOException {
        downLayer.listen();
    }


    private static int getFileNameLength(byte[] buf){
        return Byte.valueOf(buf[0]).intValue();
    }

    private static byte[] getFileName(byte[] buf){
        int fileNameLength = getFileNameLength(buf);
        return Arrays.copyOfRange(buf,1,1+fileNameLength);
    }

    private static byte[] getData(byte[] buf) {
        int fileNameLength = getFileNameLength(buf);
        return Arrays.copyOfRange(buf,1+fileNameLength,buf.length);
    }

}
