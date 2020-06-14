import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class LinkLayer implements LayersCommunication {

    LayersCommunication upwardLayer = new TransportLayer();

    private DatagramSocket socket = null;
    private int transmitedFiles = 0;
    private int lostedFiles = 0;
    private int receivedFiles = 0;
    private int corruptedFiles = 0;

    public LinkLayer() {
        try {
            socket = new DatagramSocket(TransportLayer.PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(String IPadress, byte[] buf) throws IOException {
          byte crc=   CRC32.calculateCRC32(buf);
          transmitedFiles++;
          byte[] allBytesArray = new byte[1+buf.length];
            ByteBuffer bf = ByteBuffer.wrap(allBytesArray);
            bf.put(crc,buf);
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
    }

    @Override
    public void receive(byte[] buf) {

    }
}
