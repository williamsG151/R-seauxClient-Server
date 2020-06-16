import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.CRC32;

/**
 * cette couche permet de transmettre et de recevoir des paquet
 * vers/du socket de Berkeley
 */

public class LinkLayer implements LayersCommunication {

    LayersCommunication upwardLayer;
    private static final int HEADER_LENGTH = 1;

    private DatagramSocket socket = null;
    private int transmitedFiles = 0;
    private int lostedFiles = 0;
    private int receivedFiles = 0;
    private int corruptedFiles = 0;
    private final boolean errorGeneratorActivated;
    private final Random r = new Random();
    private final Logger logger = Logger.getLogger("Log");
    FileHandler fileHandler;


    public LinkLayer(int myPort, boolean withErrorGenerator, LayersCommunication upwardLayer) {
        this.upwardLayer = upwardLayer;
        errorGeneratorActivated=withErrorGenerator;
        try {
            socket = new DatagramSocket(myPort);
            logger.setUseParentHandlers(false); //Disable log output in console
            fileHandler = new FileHandler("liasonDeDonnes.log",true);
            logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cette fonction permet de recevoir les paquet transmie par la couche
     * transport et les envoyer les paquet au socket de Berkeley
     *
     * @param portDestinataire le numéro de port du destinataire
     * @param IPadress l'adress IP du destinataire
     * @param buf les pacquet envoyer par la couche transport
     * @throws IOException
     */


    @Override
    public void send(int portDestinataire, byte[] IPadress, byte[] buf) throws IOException {
        logger.log(Level.INFO,"Packet envoyé vers le port " + portDestinataire);
        CRC32 crc32 = new CRC32();
        crc32.update(buf);
        byte crc = (byte) crc32.getValue();
        //byte crc=   CRC32.calculateCRC32(buf);
        transmitedFiles++;

        byte[] allBytesArray = new byte[1 + buf.length];
        ByteBuffer bf = ByteBuffer.wrap(allBytesArray);
        bf.put(crc);
        // generate error in the packets
        if (errorGeneratorActivated && r.nextInt() % 10 == 0) {
            byte[] newBuf = Arrays.copyOf(buf, buf.length);
            byte[] bad = "BAD".getBytes();
            newBuf[0] = bad[0];
            newBuf[1] = bad[1];
            newBuf[2] = bad[2];
            bf.put(newBuf);
        } else {
            bf.put(buf);
        }

        // send packet to Berkeley socket
        InetAddress adress = InetAddress.getByAddress(IPadress);
        DatagramPacket packet = new DatagramPacket(allBytesArray, allBytesArray.length, adress, portDestinataire);
        socket.send(packet);
    }

    /**
     * Receoit les donnée transmie par le socket de Berkeley
     * et les transfert en paquet vers la couche de transport
     *
     * @param portSource le numéro de port de la source
     * @param IPsource l'adress IP de la source
     * @param buf les paquet envoyer du socket de berkeley
     */

    @Override
    public void receive(int portSource, byte[] IPsource, byte[] buf) throws IOException, TransmissionErrorException {
        receivedFiles++;
        byte crcByte = buf[0];
        byte[] data = Arrays.copyOfRange(buf, HEADER_LENGTH, buf.length);
        //byte crc=   CRC32.calculateCRC32(data);
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        byte crc = (byte) crc32.getValue();

        // check if the packet is not corrupt
        if (crc == crcByte) {
            logger.log(Level.INFO,"Packet reçu du port " + portSource);

                upwardLayer.receive(portSource, IPsource, data);

        } else {
            logger.log(Level.INFO,"Packet corrumpu reçu du port " + portSource);
            //System.out.println("Ce packet est vrm pas cool");
            corruptedFiles++;
            listen();
        }
    }

    public void setTimerOn(boolean timerOn) {
        try {
            if (timerOn) {
                socket.setSoTimeout(5000);
            } else {
                socket.setSoTimeout(0);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listen() throws IOException, TransmissionErrorException {
        byte[] buf = new byte[1500];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
            receive(packet.getPort(), packet.getAddress().getAddress(), Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));
        } catch (SocketTimeoutException e) {
            logger.log(Level.INFO,"SocketTimeout");
            lostedFiles++;
        }

    }

}
