package mypackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

public class DatabaseClient {
    public static void main(String[] args) {
        try {
            String[] arr = args[1].split(":");
            String nodeIP = arr[0];
            int nodePort = Integer.parseInt(arr[1]);
            String operation = args[3];
            LinkedList<Integer> parameters = new LinkedList<>();
            for (int i = 4; i < args.length; ++i) {
                parameters.add(Integer.parseInt(args[i]));
            }
            try {
                Socket socket = new Socket(nodeIP, nodePort);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                printWriter.println("Client: " + socket.getLocalPort()+ ":"+socket.getLocalAddress().getHostAddress());
                System.out.println(bufferedReader.readLine());
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (NumberFormatException e) {
            System.out.println("Couldn't create a mypackage.DatabaseClient. Make sure values are passed properly and in the correct order\njava mypackage.DatabaseClient -gateway localhost:9991 -operation get-value 17");

        }
    }
}