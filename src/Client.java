import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {

    private static final int SERVER_PORT = 25001;
    private static final int CLIENT_PORT = 25002;

    /**
     * Cette fonction permet de faire un programme client qui
     * envoye un fichier a la couche application
     *
     *
     */
    public static void main(String[] args) throws IOException {
        ApplicationLayer app = new ApplicationLayer(CLIENT_PORT, Boolean.parseBoolean(args[0]));
        String envoie = "Y";
        while (envoie.toLowerCase().equals("y")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("entrez le nom du fichier: ");
            String nomFichier = br.readLine();
            System.out.println("entrez l'adresse du destinataire: ");
            String[] adresseStrings = br.readLine().split("\\.");
            System.out.println("Envoie du fichier en cours... veuillez patienter");
            //envoie....
            byte[] buf = (nomFichier).getBytes();
            //On stock les chiffres de l'adresse dans un array de 4 bytes
            byte[] adress = new byte[4];
            for(int i = 0;i<4; i++){
                adress[i] = Integer.valueOf(adresseStrings[i]).byteValue();
            }
            app.send(SERVER_PORT,adress,buf);
            System.out.println("Envoie du fichier terminé, voulez vous en envoyer un autre? [Y/N]");
            envoie = br.readLine();
        }
        System.out.println("Merci d'avoir utilisé le protocole maison, bonne journée!");
    }



}
