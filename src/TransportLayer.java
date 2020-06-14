import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransportLayer implements LayersCommunication {
    LayersCommunication downLayer;
    LayersCommunication upwardLayer;
    private List<byte[]> packets = new ArrayList<>();
    private static final int PACKET_NUMBER_SIZE = 5;
    private static final int MAX_PACKET_SIZE = 200;

    public TransportLayer() {

    public TransportLayer(int myPort, LayersCommunication upwardLayer) {
        this.upwardLayer= upwardLayer;
        downLayer = new LinkLayer(myPort,this);
    }

    @Override
    public void send(int portDestinataire,byte[] IPdestinataire,byte[] buf) throws IOException {



        int quantity = (int) (Math.ceil(buf.length/MAX_PACKET_SIZE)+1);

        byte[] packetQuantity = numberToString(quantity, PACKET_NUMBER_SIZE).getBytes();
        byte[] header, body, allBytes;
        ByteBuffer byteBuffer;

        for (int i = 0 ; i<quantity;i++){
            byte[] packetNumber = numberToString(i, PACKET_NUMBER_SIZE).getBytes();

            //Création du header
            header =  new byte[2* PACKET_NUMBER_SIZE];
            byteBuffer = ByteBuffer.wrap(header);
            byteBuffer.put(packetNumber).put(packetQuantity);

            //Création du body
            if (i==quantity){
                body = Arrays.copyOfRange(buf,i*MAX_PACKET_SIZE, buf.length);
            }else{
                body = Arrays.copyOfRange(buf,i*MAX_PACKET_SIZE, (i-1)*MAX_PACKET_SIZE+MAX_PACKET_SIZE);
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

    @Override
    public void receive(int portSource, byte[] IPsource,byte[] buf) {

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
        downLayer.listen();
    }

}
