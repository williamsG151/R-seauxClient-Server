import com.sun.net.httpserver.Headers;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cette couche permet de prendre les données envoyer
 * de la couche application et les convertir en plusieur paquet
 * vers la couche communication de donné et peu aussi envoyer des données
 * vers la couche application
 */

public class TransportLayer implements LayersCommunication {
    LayersCommunication downLayer;
    LayersCommunication upwardLayer;
    private List<byte[]> packets = new ArrayList<>();
    private static final int PACKET_NUMBER_SIZE = 5;
    private static final int MAX_PACKET_SIZE = 200;
    private static final int HEADER_LENGTH = 2* PACKET_NUMBER_SIZE;
    private int ack = 0;
    private int packetLength = 0;

    public TransportLayer(int myPort, LayersCommunication upwardLayer) {
        this.upwardLayer= upwardLayer;
        downLayer = new LinkLayer(myPort,this);
    }

    /**
     * Cette fonction prend les données trnsmie par a couche application
     * et les transmet en paquet vers la couche liaison de données
     * @param portDestinataire
     * @param IPdestinataire
     * @param buf
     * @throws IOException
     */

    @Override
    public void send(int portDestinataire,byte[] IPdestinataire,byte[] buf) throws IOException {



        int quantity = (int) (Math.ceil(buf.length/MAX_PACKET_SIZE)+1);

        byte[] packetQuantity = numberToString(quantity, PACKET_NUMBER_SIZE).getBytes();
        byte[] header, body, allBytes;
        ByteBuffer byteBuffer;

        for (int i = 0 ; i<quantity;i++){
            byte[] packetNumber = numberToString(i+1, PACKET_NUMBER_SIZE).getBytes();

            //Création du header
            header =  new byte[HEADER_LENGTH];
            byteBuffer = ByteBuffer.wrap(header);
            byteBuffer.put(packetNumber).put(packetQuantity);

            //Création du body
            if (i==quantity){
                body = Arrays.copyOfRange(buf,i*MAX_PACKET_SIZE, buf.length);
            }else{
                body = Arrays.copyOfRange(buf,i*MAX_PACKET_SIZE, i*MAX_PACKET_SIZE+MAX_PACKET_SIZE);
            }

            //On join les deux ensemble et on les stock dans la liste
            allBytes = new byte[header.length+body.length];
            byteBuffer = ByteBuffer.wrap(allBytes);
            byteBuffer.put(header).put(body);
            packets.add(allBytes);

        }
        for (byte[] bytes:packets) {
           downLayer.send(portDestinataire, IPdestinataire,bytes);
        }
    }

    /**
     * Cette fonction sert de resevoir un accuser de reception
     * des paquet envoyer par la couche de communication des donnée
     * et aussi si le message reçu n'est pas un accuser de réception
     * cette fonction renvoie les paquet vers la fonction receiveDta
     * @param portSource
     * @param IPsource
     * @param buf
     */

    @Override
    public void receive(int portSource, byte[] IPsource,byte[] buf) {
        System.out.println("Receive trans");
            if(buf.length==HEADER_LENGTH){
                //accusé
            }else{
                receiveData( portSource, IPsource, buf);
            }
    }

    /**
     * Cette fonction permet de prendre les paquet envoyer par la couche
     * liaison de données et les renvoye en un array de byte bien ordonnée dans
     * la couche suppérieur(couche application)
     * @param portSource
     * @param IPsource
     * @param buf
     */

    private void receiveData(int portSource, byte[] IPsource,byte[] buf){
        System.out.println("Receive data");
        byte[] data = Arrays.copyOfRange(buf,HEADER_LENGTH, buf.length);
        byte[] packetNumber = Arrays.copyOfRange(buf,0,PACKET_NUMBER_SIZE);
        byte[] packetQuantity = Arrays.copyOfRange(buf,PACKET_NUMBER_SIZE,2*PACKET_NUMBER_SIZE);

        int packetNum = Integer.valueOf(new String(packetNumber));
        int packetQuantityNum = Integer.valueOf(new String(packetQuantity));
        if (packetNum!=ack+1){
            //plz send packet
        }else{
            ack++;
            packets.add(data);
            packetLength+=data.length;
            if(packetNum==packetQuantityNum){
                upwardLayer.receive(portSource,IPsource,reconstructData());
            }else{
                try {
                    downLayer.listen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private byte[] reconstructData(){
        byte[] totalPackets =new byte[packetLength];
        ByteBuffer bf = ByteBuffer.wrap(totalPackets);
        for (byte[] data: packets) {
            bf.put(data);
        }
        return totalPackets;
    }

    private String numberToString(int number, int stringLength){
        String s = String.valueOf(number);
        while(s.length()<stringLength){
            s = "0"+s;
        }
        return s;
    }

    @Override
    public void listen() throws IOException {
        packets.clear();
        packetLength=0;
        ack=0;
        downLayer.listen();
    }

}
