import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Cette interface permet d'être le Frame des différentes
 * couche
 */

public interface LayersCommunication {

   void send(int portDestinataire, byte[] IPdestinataire, byte[] buf) throws IOException, TransmissionErrorException;
    void receive(int portSource, byte[] IPsource, byte[] buf) throws IOException, TransmissionErrorException;
    void listen() throws IOException, TransmissionErrorException;

}
