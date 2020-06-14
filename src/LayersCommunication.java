import java.io.FileNotFoundException;
import java.io.IOException;

public interface LayersCommunication {

   void send(int portDestinataire, byte[] IPdestinataire, byte[] buf) throws IOException;
    void receive(int portSource, byte[] IPsource, byte[] buf);
    void listen() throws IOException;

}
