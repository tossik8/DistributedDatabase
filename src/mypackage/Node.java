package mypackage;

import mypackage.threads.ClientServerThread;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;

public class Node implements Serializable {
    private int port;
    private final String ip;
    private int key;
    private int value;

    private static final long serialVersionUID = 6529685098267757690L; //ensures serialization and deserialization
    private List<String> connectedNodes;

    private transient ServerSocket serverSocket; //transient for not serializing the server

    public Node(int port, String ip, int key, int value, List<String> connectedNodes) {
        setPort(port);
        this.ip = ip;
        this.key = key;
        this.value = value;
        this.connectedNodes = connectedNodes;

    }
    public void listen() {
        while(!serverSocket.isClosed()){
            try {
                Socket request = serverSocket.accept();
                (new ClientServerThread(request, this)).start();
            }
            catch (IOException e) {
                System.out.println(this.ip + ":" + this.port + " is closed");
            }
        }
    }
    public void setValue(int value) {
        this.value = value;
    }

    public void setPort(int port) {
        do{
            try{
                serverSocket = new ServerSocket(port);
                break;
            } catch (IOException e){
                ++port;
            }
        }while(true);
        this.port = port;

    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public int getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }

    public List<String> getConnectedNodes() {
        return connectedNodes;
    }

    public static boolean connectNode(String ip,int port, int newPort){
        try(Socket socket = new Socket(ip, port)) {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println("Connect node");
            pw.println(ip+":"+newPort);
            pw.close();
        }catch (ConnectException e){
            System.err.println("Failed to connect to " + ip + ":" + port);
            return false;
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void disconnectNode(String ip, int port, List<String> addresses){
        try(Socket socket = new Socket(ip, port)) {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println("Disconnect node");
            pw.println(this.ip+":"+this.port);
            for(String address : addresses){
                pw.println(address);
            }
            pw.close();
        }catch (ConnectException e){
            System.err.println("Failed to connect to " + ip + ":" + port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public String getValueRequest(int key, List<String> visitedNodes) {
        if (this.key == key) {
            return key + ":" + this.value;
        }

        visitedNodes.add(this.ip + ":" + this.port);
        for (String address : this.connectedNodes) {
            if (!visitedNodes.contains(address)) {
                int portNode = Integer.parseInt(address.split(":")[1]);
                try (Socket socket = new Socket(address.split(":")[0], portNode)) {
                    {
                        Node node = getNode(socket);
                        String result = node.getValueRequest(key, visitedNodes);
                        if(!result.contains("Error")) return result;

                    }

                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return "Error, there is no record with the key of " + key;
    }

    private static Node getNode(Socket socket) throws IOException, ClassNotFoundException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        printWriter.println("Provide node");

        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        Node node = (Node) objectInputStream.readObject();
        objectInputStream.close();
        return node;
    }

    public String setValueRequest(int key, int value){
        return "Error, couldn't set the value as there is no record with key " + key;
    }

    public String findKeyRequest(int key, List<String> visitedNodes){
        if(this.key == key){
            return key + " can be found at " + this.ip+":"+this.port;
        }
        visitedNodes.add(this.ip+":"+this.port);
        for(String address:this.connectedNodes){
            if(!visitedNodes.contains(address)){
                try (Socket socket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]))){
                    Node node = getNode(socket);
                    String res = node.findKeyRequest(key, visitedNodes);
                    if(!res.contains("Error")) return res;

                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return "Error, there is not any node containing key " + key;
    }
    public HashMap<Integer, Integer> getMaxRequest(HashMap<Integer, Integer> keyValuePairs, List<String> visitedNodes){
        visitedNodes.add(this.ip+":"+this.port);
        keyValuePairs.put(this.key, this.value);
        for(String address:this.connectedNodes){
            if(!visitedNodes.contains(address)){
                try (Socket socket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]))){
                    Node node = getNode(socket);
                    node.getMaxRequest(keyValuePairs, visitedNodes);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return keyValuePairs;
    }


    public HashMap<Integer, Integer> getMinRequest(HashMap<Integer, Integer> keyValuePairs, List<String> visitedNodes){
        visitedNodes.add(this.ip+":"+this.port);
        keyValuePairs.put(this.key, this.value);
        for(String address:this.connectedNodes){
            if(!visitedNodes.contains(address)){
                try (Socket socket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]))){
                    Node node = getNode(socket);
                    node.getMinRequest(keyValuePairs,visitedNodes);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return keyValuePairs;
    }
    public String newPairRequest(int key, int value){
        return "OK";
    }
    public void terminateRequest(){
        for(int i = 0; i < this.connectedNodes.size(); ++i){
            String address = this.connectedNodes.get(i);
            this.disconnectNode(address.split(":")[0],Integer.parseInt(address.split(":")[1]), this.connectedNodes);
        }
        this.connectedNodes.clear();
    }
}
