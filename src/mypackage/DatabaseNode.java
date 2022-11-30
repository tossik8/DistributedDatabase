package mypackage;

import mypackage.threads.ClientServerThread;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class DatabaseNode implements Serializable {
    public static void main(String[] args) {
        if(args.length < 3 || ((!args[0].equals("-tcpport") || !args[2].equals("-record")))){
            System.out.println("Wrong argument names\nExample of execution: java mypackage.DatabaseNode -tcpport 9991 -record 17:256 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989");
            return;
        }
        try{
            int port = Integer.parseInt(args[1]);
            String[] arr = args[3].split(":");
            int key = Integer.parseInt(arr[0]);
            int value = Integer.parseInt(arr[1]);
            List<Integer> addresses = new LinkedList<>();
            DatabaseNode node = new DatabaseNode(port, "localhost", key, value, addresses);
            for(int i = 5; i < args.length; i+=2){
                if(args[i-1].equals("-connect")){
                    String[] address = args[i].split(":");
                    if(connectNode(Integer.parseInt(address[1]), node.port)){
                        addresses.add(Integer.parseInt(address[1]));
                    }
                }
                else{
                    System.out.println("Wrong argument\nExpected -connect. Received " + args[i-1]);
                }
            }
            node.listen();

        }catch (NumberFormatException e){
            System.out.println("""
                    Couldn't create a DatabaseNode. Make sure values are passed properly and in the correct order
                    java mypackage.DatabaseNode -tcpport 9991 -record 17:256 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989""");
        }
    }
    private int port;
    private String ip;
    private int key;
    private int value;

    private static final long serialVersionUID = 6529685098267757690L; //ensures serialization and deserialization
    private List<Integer> connectedNodes;


    private transient ServerSocket serverSocket; //transient for not serializing the server

    public DatabaseNode(int port, String ip, int key, int value, List<Integer> addresses) {
        this.ip = ip;
        this.key = key;
        this.value = value;
        setPort(port);
        this.connectedNodes = addresses;
    }



    public void listen() {
        System.out.println("The new node listens on port " + port + ", contains the value of " + value + " under the key " + key);
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
        do{
            try{
                serverSocket = new ServerSocket(port);
                break;
            } catch (BindException e){
                ++port;
            }
            catch (IOException e) {
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

    public List<Integer> getConnectedNodes() {
        return connectedNodes;
    }

    public static boolean connectNode(int port, int newPort){
        try(Socket socket = new Socket("localhost", port)) {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println("Connect node");
            pw.println(newPort);
            pw.close();
        }catch (ConnectException e){
            return false;
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }

    public void iterateOverNetwork(List<Integer> visitedNodes) {
        PrintWriter printWriter;
//        for (int portNode : this.getConnectedNodes()) {
//            if (!visitedNodes.contains(portNode)) {
//                try (Socket socket = new Socket("localhost", portNode)) {
//                    printWriter = new PrintWriter(socket.getOutputStream(), true);
//                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    printWriter.println("Serve node");
//                    System.out.println(bufferedReader.readLine());
//                    visitedNodes.add(portNode);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
        for (int portNode : this.connectedNodes) {
            if(!visitedNodes.contains(portNode)) {
                try (Socket socket = new Socket("localhost", portNode)) {
                    {
                        printWriter = new PrintWriter(socket.getOutputStream(), true);
                        printWriter.println("Provide node");

                        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                        DatabaseNode node = (DatabaseNode) objectInputStream.readObject();
                        objectInputStream.close();
                        System.out.println("Received object " + node.port);
                        visitedNodes.add(portNode);
                        node.iterateOverNetwork(visitedNodes);
                    }

                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
