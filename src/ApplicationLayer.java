import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ApplicationLayer implements LayersCommunication {

    public ApplicationLayer() {


    }

    @Override
    public void send(byte[] buf) throws IOException {
        String string = new String(buf);
        String[] lines = buf.toString().split("\n");
        byte[] newBuf = Files.readAllBytes(Paths.get(lines[0]));
        String[] adressString = lines[1].split("\\.");
        byte[] adress = new byte[4];
        for(int i = 0;i<4; i++){
            adress[i] = Integer.valueOf(adressString[i]).byteValue();
        }
        File f = new File(lines[0]);
        String fileName = f.getName();
        byte[] name = fileName.getBytes();
        byte nameLength = Integer.valueOf(name.length).byteValue();

    }

    @Override
    public void receive(byte[] buf) {

    }

}
