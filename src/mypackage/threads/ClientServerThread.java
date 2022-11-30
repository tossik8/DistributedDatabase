package mypackage.threads;

import mypackage.DatabaseNode;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

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
            System.out.println(firstLine);
            if(firstLine.equals("Connect node")){
                node.getConnectedNodes().add(Integer.parseInt(bufferedReader.readLine()));
//                System.out.println("Connected nodes");
//                for(int port : node.getConnectedNodes()){
//                    System.out.println(port);
//                }
            }
            else if(firstLine.equals("Serve client")){
                pw.println("Connected: " + serverSocket.getLocalPort());
                node.iterateOverNetwork(new ArrayList<>());
            }
            else if(firstLine.equals("Serve node")){
                pw.println(node.getPort() + " - " + node.getKey() + ":"+node.getValue());


            }
            else if(firstLine.equals("Provide node")){
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());
                objectOutputStream.writeObject(node);
                objectOutputStream.flush();
                objectOutputStream.close();
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

    public DatabaseNode getNode() {
        return node;
    }
}
