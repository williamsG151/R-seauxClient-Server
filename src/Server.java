import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

/**
 * cette classe représente le programme server
 */

public class Server  {

    private static final int SERVER_PORT = 25001;
    private static final int CLIENT_PORT = 25002;

    private static boolean run=true;


    public static void main(String[] args)  throws IOException {
        ApplicationLayer app = new ApplicationLayer(SERVER_PORT);
    while(run) {
        app.listen();
    }
    System.out.println("Server shut down, bonne journée!");
    }


}
