import java.io.IOException;

/**
 * Thread du serveur
 */

public class ServerThread extends Thread {
    boolean run;
    LayersCommunication app;

    /**
     * Constructeur du thread
     * @param app La couche de base de l'application
     */
    public ServerThread(LayersCommunication app){
        super("ServerThread");
        this.app = app;
    }

    /**
     * Démare le thread et met le socket à l'écoute
     */
    @Override
    public void run() {
        run = true;
        while (run) {
            System.out.println("Serveur à l'écoute");
            try {
                app.listen();
            } catch (IOException | TransmissionErrorException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Permet d'arreter la boucle d'exécution du thread
     */
    public void end(){
        run = false;
    }
}
