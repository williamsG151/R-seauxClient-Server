import java.io.FileNotFoundException;
import java.io.IOException;

public interface LayersCommunication {

   void send(byte[] buf) throws IOException;
    void receive(byte[] buf);


}
