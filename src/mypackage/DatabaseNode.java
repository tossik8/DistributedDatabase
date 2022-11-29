package mypackage;

import mypackage.threads.ClientServerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class DatabaseNode {
    public static void main(String[] args) {
        try{
            int port = Integer.parseInt(args[1]);
            String[] arr = args[3].split(":");
            int key = Integer.parseInt(arr[0]);
            int value = Integer.parseInt(arr[1]);
            Map<Integer, String> addresses = new HashMap<>();
            for(int i = 5; i < args.length; i+=2){
                String[] address = args[i].split(":");
                addresses.put(Integer.parseInt(address[1]), address[0]);
            }
            DatabaseNode node = new DatabaseNode(port, "localhost", key, value, addresses);
            node.listen();
        }catch (NumberFormatException e){
            System.out.println("""
                    Couldn't create a DatabaseNode. Make sure values are passed properly and in the correct order
                    java DatabaseNode -tcpport 9991 -record 17:256 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989""");
        }
    }
    private int port;
    private String ip;
    private int key;
    private int value;


    public DatabaseNode(int port, String ip, int key, int value, Map<Integer, String> addresses) {
        this.ip = ip;
        this.key = key;
        this.value = value;
        this.port = port;
    }

    public void listen() {
        ServerSocket serverSocket;
        do{
            try{
                serverSocket = new ServerSocket(port);
                break;
            } catch (IOException e) {
                ++port;
            }
        }while(true);
        System.out.println("Listens on " + port);
        while(true){
            try {
                Socket request = serverSocket.accept();
                (new ClientServerThread(request, this)).start();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void setValue(int value) {
        this.value = value;
    }

    public void setPort(int port) {
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

}
