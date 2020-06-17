import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Cette interface permet représente une couche
 * pour un protocole de communication
 */

public interface LayersCommunication {

    /**
     * Fonction appelée par une couche supérieur pour envoyer des données
     * @param portDestinataire le port du destinataire
     * @param IPdestinataire l'adresse IP du destinataire
     * @param buf les données à transférer
     * @throws IOException
     * @throws TransmissionErrorException Exception lancé si la communication est coupée
     */
   void send(int portDestinataire, byte[] IPdestinataire, byte[] buf) throws IOException, TransmissionErrorException;

    /**
     * Fonction appelé par la couche inférieur lorsque des données sont reçues
     * @param portSource le port de l'envoyeur
     * @param IPsource l'adresse IP de l'envoyeur
     * @param buf les données reçues
     * @throws IOException
     * @throws TransmissionErrorException Exception lancée si la communication est coupée
     */
    void receive(int portSource, byte[] IPsource, byte[] buf) throws IOException, TransmissionErrorException;

    /**
     * Fonction permettant de transmettre le message à la couche inférieur que la couche supérieur souhaite
     * que la couche inférieur se mette à l'écoute des données entrantes
     * @throws IOException
     * @throws TransmissionErrorException Exception lancée si la communication est coupée
     */
    void listen() throws IOException, TransmissionErrorException;

}
