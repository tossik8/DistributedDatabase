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
            System.out.println(bufferedReader.readLine());
            pw.println("Connected: " + serverSocket.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
