package mypackage.threads;

import mypackage.Node;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
                    //int argument = Integer.parseInt(bufferedReader.readLine());
                    String result = this.determineOperation(operation);
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

    public String determineOperation(String operation, int... arguments){
        String result = "";
        if(operation.equals("set-value")){
            result = node.setValueRequest(arguments[0], arguments[1]);
        }
        else if(operation.equals("get-value")){
            result = node.getValueRequest(arguments[0], new LinkedList<>());
        }
        else if(operation.equals("find-key")){
            result = node.findKeyRequest(arguments[0], new LinkedList<>());
        }
        else if(operation.equals("get-max")){
            result = findMax(node.getMaxRequest(new HashMap<>(),new LinkedList<>()));
        }
        else if(operation.equals("get-min")){
            result = findMin(node.getMinRequest(new HashMap<>(), new LinkedList<>()));
        }
        else if(operation.equals("new-record")){
            result = node.newPairRequest(arguments[0], arguments[1]);
        }
        else if(operation.equals("terminate")){
            node.terminateRequest();
            result = "Node terminated";
        }
        else {
            result = "There is no operation " + operation;
        }
        return result;
    }

    public static String findMax(HashMap<Integer, Integer> keyValuePairs){
        int key = (int) keyValuePairs.keySet().toArray()[0], max = (int) keyValuePairs.values().toArray()[0];
        for(Map.Entry<Integer, Integer> entry: keyValuePairs.entrySet()){
            if(entry.getValue() > max){
                max = entry.getValue();
                key = entry.getKey();
            }
        }
        return key+":"+max;
    }

    public static String findMin(HashMap<Integer, Integer> keyValuePairs) {
        int key = (int) keyValuePairs.keySet().toArray()[0], min = (int) keyValuePairs.values().toArray()[0];
        for (Map.Entry<Integer, Integer> entry : keyValuePairs.entrySet()) {
            if (entry.getValue() < min) {
                min = entry.getValue();
                key = entry.getKey();
            }
        }
        return key + ":" + min;

    }
}
