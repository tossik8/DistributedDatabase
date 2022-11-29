package components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class DatabaseClient {
    private int nodePort;
    private String nodeIP;
    private String operation;
    private List<Integer> parameters;

    public DatabaseClient(int nodePort, String nodeIP, String operation, List<Integer> parameters) {
        this.nodePort = nodePort;
        this.nodeIP = nodeIP;
        this.operation = operation;
        this.parameters = parameters;
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost",Integer.parseInt(args[0]));
        System.out.println(socket.isConnected());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println(bufferedReader.readLine());
        socket.close();
    }
}
