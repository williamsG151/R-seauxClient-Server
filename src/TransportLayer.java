import java.io.IOException;
import java.util.Arrays;

public class TransportLayer implements LayersCommunication {
    @Override
    public void send(byte[] buf) throws IOException {
        byte[] IPadress = ApplicationLayer.getIPAdress(buf);
        byte[] fileName = ApplicationLayer.getFileName(buf);
        byte[] data = ApplicationLayer.getData(buf);

        int quantity = (int) (Math.ceil(data.length/200)+1);
    }

    @Override
    public void receive(byte[] buf) {

    }
}
