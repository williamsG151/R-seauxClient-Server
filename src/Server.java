import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * cette classe représente le programme server
 */

public class Server {

    private static final int SERVER_PORT = 25001;
    private static final int CLIENT_PORT = 25002;

    /**
     * Fonction permettant de démarrer le serveur et de l'arrêter à l'aide de la touche "ENTER"
     * @param args le premier argument est un boolean permettant de specifier si on veut générer des erreurs de transfert des paquets
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        boolean generateError = false;
        if (args.length > 0) {
            generateError = Boolean.parseBoolean(args[0]);
        }
        ServerThread thread = new ServerThread(new ApplicationLayer(SERVER_PORT, generateError));
        thread.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        br.readLine();
        thread.end();
        System.out.println("Server shut down, bonne journée! :)");
        System.exit(0);
    }


}
