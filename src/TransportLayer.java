import com.sun.net.httpserver.Headers;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.sql.CallableStatement;
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
    LinkLayer downLayer;
    LayersCommunication upwardLayer;
    private List<byte[]> packets = new ArrayList<>();
    private static final int PACKET_NUMBER_SIZE = 10;
    private static final int MAX_PACKET_SIZE = 200;
    private static final int HEADER_LENGTH = 2* PACKET_NUMBER_SIZE;
    private int ack = 0;
    private int packetLength = 0;
    private static final String RETRANSMISSION = "NOT COOL:(";
    private static final String ACKNOWLEDGEMENT = "COOOOOL!:)";
    private static final String TRANSMISSION_ERROR = "TRANSERROR";
    private static final int MESSAGE_TYPE_LENGTH = 10;

    private int transmissionErrorCounter = 0;


    public TransportLayer(int myPort,boolean withErrorGenerator, LayersCommunication upwardLayer) {
        this.upwardLayer= upwardLayer;
        downLayer = new LinkLayer(myPort, withErrorGenerator,this);
    }

    /**
     * Cette fonction prend les données trnsmie par a couche application
     * et les transmet en paquet vers la couche liaison de données
     * @param portDestinataire le numéro du port du destinataire
     * @param IPdestinataire l'adress IP ou on envoie lespacket
     * @param buf les données transmise par la couche application
     * @throws IOException
     */

    @Override
    public void send(int portDestinataire,byte[] IPdestinataire,byte[] buf) throws IOException, TransmissionErrorException {



        int quantity = (int) (Math.ceil(buf.length/MAX_PACKET_SIZE));

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
            if (i==quantity-1){
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
        for (ack =0;ack<packets.size();ack++) {
           downLayer.send(portDestinataire, IPdestinataire,packets.get(ack));
           downLayer.setTimerOn(true);
           downLayer.listen();
        }

    }

    /**
     * Cette fonction sert de resevoir un accuser de reception
     * des paquet envoyer par la couche de communication des donnée
     * et aussi si le message reçu n'est pas un accuser de réception
     * cette fonction renvoie les paquet vers la fonction receiveDta
     * @param portSource it is a integer that pass the number of the port that we use
     * @param IPsource it is a byte array that pass the ip adress of the source
     * @param buf It is a byte arrays that contain the packet send from Link Layer
     */

    @Override
    public void receive(int portSource, byte[] IPsource,byte[] buf) throws IOException, TransmissionErrorException {
            if(buf.length==HEADER_LENGTH){
                //accusé
                byte[] messageType = Arrays.copyOfRange(buf,0,MESSAGE_TYPE_LENGTH);
                byte[] packetNumber = Arrays.copyOfRange(buf,buf.length-PACKET_NUMBER_SIZE,buf.length);
                if ((new String(messageType)).equals(RETRANSMISSION)){
                    //Retransmit
                    retransmit( portSource,  IPsource, Integer.parseInt(new String(packetNumber)) );
                }else if ((new String(messageType)).equals(ACKNOWLEDGEMENT)){
                    acknowledgementReceived(Integer.parseInt(new String(packetNumber)));
                }else if ((new String(messageType).equals(TRANSMISSION_ERROR))){
                    throw (new TransmissionErrorException());
                }
            }else{
                receiveData( portSource, IPsource, buf);
            }
    }
    private void acknowledgementReceived(int packetNumber) throws IOException {
        if (packetNumber == packets.size()){
            packets.clear();
            //End of transfer
        }
    }
    private void retransmit(int destinationPort, byte[] IPDestination, int packetNumber) throws IOException, TransmissionErrorException {
        // Send the paquet that have been requested
        byte[] packet=packets.get(packetNumber-1);
        //Adjust the ack #
        ack = packetNumber-1;
        downLayer.send(destinationPort, IPDestination, packet);
        downLayer.setTimerOn(true);
        downLayer.listen();
    }


    /**
     * Cette fonction permet de prendre les paquet envoyer par la couche
     * liaison de données et les renvoye en un array de byte bien ordonnée dans
     * la couche suppérieur(couche application)
     * @param portSource it is a integer that pass the number of the port that we use
     * @param IPsource it is a byte array that pass the ip adress of the source
     * @param buf It is a byte arrays that contain the packet send from Link Layer
     */
    private void receiveData(int portSource, byte[] IPsource,byte[] buf) throws IOException, TransmissionErrorException {
        byte[] data = Arrays.copyOfRange(buf,HEADER_LENGTH, buf.length);
        byte[] packetNumber = Arrays.copyOfRange(buf,0,PACKET_NUMBER_SIZE);
        byte[] packetQuantity = Arrays.copyOfRange(buf,PACKET_NUMBER_SIZE,2*PACKET_NUMBER_SIZE);

            int packetNum = Integer.valueOf(new String(packetNumber));
            int packetQuantityNum = Integer.valueOf(new String(packetQuantity));
            // check if the packet received is the one expected
            if (packetNum != ack + 1) {
                if(++transmissionErrorCounter==3){ //End connection
                    sendEndOfTransmission(portSource,IPsource);
                    reset();
                }else { //Ask for the retransmission of the good package
                    sendRetransmissionRequest(portSource, IPsource, ack + 1);
                }
            } else {
                //Stock the good packet
                ack++;
                packets.add(data);
                packetLength += data.length;
                sendAcknowledgement(portSource, IPsource, ack);
                if (packetNum == packetQuantityNum) {
                    //Send the Data to Application Layer
                    upwardLayer.receive(portSource, IPsource, reconstructData());
                    //On reset, prêt à commencer une nouvelle transmission
                    reset();
                }
            }
    }

    private void sendEndOfTransmission(int portDestination, byte[] IPDestination) throws IOException {
        //Pad the error code so it has the header length
        String message = String.format("%-" + HEADER_LENGTH + "s", TRANSMISSION_ERROR);
        downLayer.send(portDestination,IPDestination,message.getBytes());
    }

    private void reset() {
        packets.clear();
        packetLength = 0;
        ack = 0;
        transmissionErrorCounter=0;
    }

    private void sendAcknowledgement(int portSource, byte[] IPsource, int ack) throws IOException {
        byte[] buf = new byte[HEADER_LENGTH];
        ByteBuffer bf = ByteBuffer.wrap(buf);
        bf.put(ACKNOWLEDGEMENT.getBytes()).put(numberToString(ack,PACKET_NUMBER_SIZE).getBytes());
        downLayer.send(portSource,IPsource,buf);
    }

    private void sendRetransmissionRequest(int portSource, byte[] IPsource,int packetNumber) throws IOException {
        byte[] buf = new byte[HEADER_LENGTH];
        ByteBuffer bf = ByteBuffer.wrap(buf);
        bf.put(RETRANSMISSION.getBytes()).put(numberToString(packetNumber,PACKET_NUMBER_SIZE).getBytes());
        downLayer.send(portSource,IPsource,buf);
    }

    /**
     * Assemble les tableaux de bytes en un seul
     * @return le tableau de byte contenant l'ensemble des données
     */
    private byte[] reconstructData(){
        byte[] totalPackets =new byte[packetLength];
        ByteBuffer bf = ByteBuffer.wrap(totalPackets);
        for (byte[] data: packets) {
            bf.put(data);
        }
        return totalPackets;
    }

    /**
     * transforme un entier en une string d'une certaine longueur en ajoutant des zero au début au besoin
     * @param number l'entier que l'on veut transfromer en string
     * @param stringLength le nombre de caractère de la string souhaité
     * @return l'entier transformé en string
     */
    private String numberToString(int number, int stringLength){
        String s = String.valueOf(number);
        while(s.length()<stringLength){
            s = "0"+s;
        }
        return s;
    }

    @Override
    public void listen() throws IOException, TransmissionErrorException {

        downLayer.listen();
    }

}
