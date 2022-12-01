package mypackage.threads;

import mypackage.Node;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
            if (firstLine.equals("Connect node")) {
                    String newNode = bufferedReader.readLine();
                    node.getConnectedNodes().add(newNode);
                    System.out.println(node.getIp() + " " + node.getPort() + " is connected to: ");
                    for (String s : node.getConnectedNodes()) {
                        System.out.print(s + " ");
                    }
                    System.out.println();
            }
            else if(firstLine.equals("Serve client")){
                    pw.println("Connected: " + serverSocket.getLocalPort());
                    String operation = bufferedReader.readLine();
                    int argument = Integer.parseInt(bufferedReader.readLine());
                    String result = this.determineOperation(operation, node, argument);
                    pw.println(result);
            }
            else if(firstLine.equals("Serve node")){
                    pw.println(node.getPort() + " - " + node.getKey() + ":" + node.getValue());
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

    public String determineOperation(String operation, Node node, int... arguments){
        String result = "";
        if(operation.equals("set-value")){
            result = setValue(arguments[0], arguments[1]);
        }
        else if(operation.equals("get-value")){
            result = node.getValueRequest(arguments[0], new LinkedList<>());
        }
        else if(operation.equals("find-key")){
            result = findKey(arguments[0]);
        }
        else if(operation.equals("get-max")){
            result = getMax();
        }
        else if(operation.equals("get-min")){
            result = getMin();
        }
        else if(operation.equals("new-record")){
            result = newPair(arguments[0], arguments[1]);
        }
        else if(operation.equals("terminate")){
            terminate(node);

        }
        else {
            result = "There is no operation " + operation;
        }
        return result;
    }

    public String setValue(int key, int value){
        return "Error, couldn't set the value as there is no record with key " + key;
    }

    public String findKey(int key){
        return "Error, there is not any node containing key " + key;
    }
    public String getMax(){
        return "0";
    }
    public String getMin(){
        return "0";
    }
    public String newPair(int key, int value){
       return "OK";
    }
    public void terminate(Node node){

    }
}
