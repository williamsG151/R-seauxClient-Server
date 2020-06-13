import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ApplicationLayer implements LayersCommunication {

    public ApplicationLayer() {
    }


    @Override
    public void send(byte[] buf) throws IOException {
        String string = new String(buf);
        String[] lines = string.split("\n");
        byte[] data = Files.readAllBytes(Paths.get(lines[0])); //Lis tout les bytes du fichier
        String[] adressStrings = lines[1].split(".");
        
        byte[] adress = new byte[4];
        for (int i =0;i<4; i++) {
            adress[i] = Integer.valueOf(adressStrings[i]).byteValue();
        }

    }

    @Override
    public void receive(byte[] buf) {

    }
}
