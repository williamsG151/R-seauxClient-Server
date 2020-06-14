import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {

    public static void main(String[] args) throws IOException {
        ApplicationLayer app = new ApplicationLayer();
        String envoie = "Y";
        while (envoie.toLowerCase().equals("y")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("entrez le nom du fichier: ");
            String nomFichier = br.readLine();
            System.out.println("entrez l'adresse du destinataire: ");
            String adresse = br.readLine();

            System.out.println("Envoie du fichier en cours... veuillez patienter");
            //envoie....
            byte[] buf = (nomFichier).getBytes();
            app.send(adresse,buf);
            System.out.println("Envoie du fichier terminé, voulez vous en envoyer un autre? [Y/N]");
            envoie = br.readLine();
        }
        System.out.println("Merci d'avoir utilisé le protocole maison, bonne journée!");
    }



}
