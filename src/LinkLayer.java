import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class LinkLayer implements LayersCommunication {

    LayersCommunication upwardLayer;
    private static final int HEADER_LENGTH = 1;

    private DatagramSocket socket = null;
    private int transmitedFiles = 0;
    private int lostedFiles = 0;
    private int receivedFiles = 0;
    private int corruptedFiles = 0;


    public LinkLayer(int myPort, LayersCommunication upwardLayer) {
       this.upwardLayer = upwardLayer;

        try {
            socket = new DatagramSocket(myPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void send(int portDestinataire,byte[] IPadress, byte[] buf) throws IOException {

          byte crc=   CRC32.calculateCRC32(buf);
          transmitedFiles++;
          byte[] allBytesArray = new byte[1+buf.length];
            ByteBuffer bf = ByteBuffer.wrap(allBytesArray);
            bf.put(crc).put(buf);

        InetAddress adress = InetAddress.getByAddress(IPadress);
        DatagramPacket packet = new DatagramPacket(allBytesArray, allBytesArray.length,adress,portDestinataire);
        socket.send(packet);
    }

    @Override
    public void receive(int portSource, byte[] IPsource, byte[] buf) {
        byte crcByte = buf[0];
        byte[] data = Arrays.copyOfRange(buf,HEADER_LENGTH,buf.length);
        byte crc=   CRC32.calculateCRC32(data);

        if(crc==crcByte){
            upwardLayer.receive(portSource,IPsource,data);
        }else{
            System.out.println("Ce packet est vrm pas cool");
        }
    }

    @Override
    public void listen() throws IOException {
        byte[] buf = new byte[1500];
        DatagramPacket packet = new DatagramPacket(buf,buf.length);
       socket.receive(packet);
       receive(packet.getPort(), packet.getAddress().getAddress(), Arrays.copyOfRange(packet.getData(),0,packet.getLength()));
    }

}
