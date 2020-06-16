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

    private static boolean run = true;



    public static void main(String[] args) throws IOException, TransmissionErrorException {
        boolean generateError = false;
        if (args.length > 0) {
            generateError = Boolean.parseBoolean(args[0]);
        }
        ApplicationLayer app = new ApplicationLayer(SERVER_PORT, generateError);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (run) {
            System.out.println("Serveur à l'écoute");
            app.listen();
        }
        System.out.println("Server shut down, bonne journée!");
    }


}
