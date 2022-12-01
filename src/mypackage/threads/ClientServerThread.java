package mypackage.threads;

import mypackage.Node;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientServerThread extends Thread{
    private final Socket serverSocket;
    private final Node node;


    public ClientServerThread(Socket serverSocket, Node node) {
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
            switch (firstLine) {
                case "Connect node" -> {
                    String newNode = bufferedReader.readLine();
                    node.getConnectedNodes().add(newNode);
                    System.out.println(node.getIp() + " " + node.getPort() + " is connected to: ");
                    for (String s : node.getConnectedNodes()) {
                        System.out.print(s + " ");
                    }
                    System.out.println();
                }
                case "Serve client" -> {
                    pw.println("Connected: " + serverSocket.getLocalPort());
                    node.iterateOverNetwork(new ArrayList<>());
                }
                case "Serve node" -> {
                    pw.println(node.getPort() + " - " + node.getKey() + ":" + node.getValue());
                }
                case "Provide node" -> {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());
                    objectOutputStream.writeObject(node);
                    objectOutputStream.flush();
                    objectOutputStream.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getValue(){

        return Integer.MIN_VALUE;
    }
    public boolean setValue(int key, int value){
        return false;
    }

    public String findKey(int key){
        return "";
    }
    public int getMax(){
        return 0;
    }
    public int getMin(){
        return 0;
    }
    public void newPair(int key, int value){

    }
    public void terminate(){

    }
}
