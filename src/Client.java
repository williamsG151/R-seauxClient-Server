import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Programme client
 */
public class Client {

    private static final int SERVER_PORT = 25001;
    private static final int CLIENT_PORT = 25002;

    /**
     * Cette fonction permet de faire un programme client qui
     * envoye un fichier a la couche application
     * @param args le premier argument est un boolean permettant de specifier si on veut générer des erreurs de transfert des paquets
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        boolean generateError = false;
        if(args.length>0) {
            generateError = Boolean.parseBoolean(args[0]);
        }

        ApplicationLayer app = new ApplicationLayer(CLIENT_PORT, generateError);
        String envoie = "Y";
        byte[] buf = new byte[0];
        byte[] adress = new byte[4];
        
        while (envoie.toLowerCase().equals("y")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            
            String nomFichier;
            boolean ok;
            do {
                System.out.println("entrez le chemin vers le fichier: ");
                nomFichier = br.readLine();
                if (Files.exists(Paths.get(nomFichier))){
                    ok =true;
                    buf = (nomFichier).getBytes();
                }else{
                    System.out.println("Fichier inexistant");
                    ok = false;
                }
            } while (!ok);
            
        

            do {
                System.out.println("entrez l'adresse du destinataire: (Format: ##.##.##.##)");
                String[] adresseStrings = br.readLine().split("\\.");
                if (adresseStrings.length == 4) {
                    //On stock les chiffres de l'adresse dans un array de 4 bytes
                    try {
                        for (int i = 0; i < 4; i++) {
                            adress[i] = Integer.valueOf(adresseStrings[i]).byteValue();
                        }
                        ok = true;
                    } catch (NumberFormatException e) {
                        System.out.println("Adresse invalide");
                        ok = false;
                    }
                }else{
                    System.out.println("Adresse invalide");
                    ok = false;
                }
            } while (!ok);


            System.out.println("Envoie du fichier en cours... veuillez patienter");

            try {
                app.send(SERVER_PORT, adress, buf);
                System.out.println("Envoie du fichier terminé");
            } catch (TransmissionErrorException e) {
                System.out.println("Mauvaise connection avec le serveur, impossible d'envoyer le fichier");
            }

            System.out.println("Voulez vous en envoyer un autre? [Y/N]");
            envoie = br.readLine();
        }
        System.out.println("Merci d'avoir utilisé le protocole maison, bonne journée!");
    }


}
