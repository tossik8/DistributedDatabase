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
            else if(firstLine.equals("Disconnect node")){
                String address = bufferedReader.readLine();
                node.getConnectedNodes().remove(address);
                System.out.println(address + " is no longer connected to " + node.getIp() + " " + node.getPort());
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    if(!node.getConnectedNodes().contains(line) && !(node.getIp()+":"+node.getPort()).equals(line))
                        node.getConnectedNodes().add(line);
                }
                System.out.println(node.getIp() + " " + node.getPort() + " is connected to: ");
                for (String s : node.getConnectedNodes()) {
                    System.out.print(s + " ");
                }
                System.out.println();

            }
            else if(firstLine.equals("Provide node")){
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());
                objectOutputStream.writeObject(node);
                objectOutputStream.flush();
                objectOutputStream.close();
            }
            else if(firstLine.equals("get-value")){
                int argument = Integer.parseInt(bufferedReader.readLine());
                String result = node.getValueRequest(argument, new LinkedList<>());
                pw.println(result);
            }
            else if(firstLine.equals("set-value")){
                int argument = Integer.parseInt(bufferedReader.readLine());
                int argument1 = Integer.parseInt(bufferedReader.readLine());
                String result = node.setValueRequest(argument, argument1);
                pw.println(result);
            }
            else if(firstLine.equals("find-key")){
                int argument = Integer.parseInt(bufferedReader.readLine());
                String result = node.findKeyRequest(argument, new LinkedList<>());
                pw.println(result);
            }
            else if(firstLine.equals("get-max")){
                String result = findMax(node.getMaxRequest(new HashMap<>(),new LinkedList<>()));
                pw.println(result);
            }
            else if(firstLine.equals("get-min")){
                String result = findMin(node.getMinRequest(new HashMap<>(), new LinkedList<>()));
                pw.println(result);
            }
            else if(firstLine.equals("new-record")){
                int argument = Integer.parseInt(bufferedReader.readLine());
                int argument1 = Integer.parseInt(bufferedReader.readLine());
                String result = node.newPairRequest(argument, argument1);
                pw.println(result);
            }
            else if(firstLine.equals("terminate")){
                node.terminateRequest();
                String result = "Node terminated";
                pw.println(result);
                node.getServerSocket().close();
            }
            else {
                pw.println("There is no operation " + firstLine);
            }
            serverSocket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
