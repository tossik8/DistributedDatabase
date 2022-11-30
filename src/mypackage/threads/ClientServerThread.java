package mypackage.threads;

import mypackage.DatabaseNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientServerThread extends Thread{
    private final Socket serverSocket;
    private final DatabaseNode node;


    public ClientServerThread(Socket serverSocket, DatabaseNode node) {
        this.serverSocket = serverSocket;
        this.node = node;
    }

    @Override
    public void run() {
        super.run();
        try {
            PrintWriter pw = new PrintWriter(serverSocket.getOutputStream(), true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            String firstLine = bufferedReader.readLine();
            if(firstLine.equals("Connect node")){
                node.getConnectedNodes().add(Integer.parseInt(bufferedReader.readLine()));
                System.out.println("Connected nodes");
                for(int port : node.getConnectedNodes()){
                    System.out.println(port);
                }
            }
            else if(firstLine.equals("Serve client")){
                pw.println("Connected: " + serverSocket.getPort());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
