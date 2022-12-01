package mypackage;

import mypackage.threads.ClientServerThread;

import java.io.*;
import java.net.*;
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
            return false;
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }

    public void iterateOverNetwork(List<String> visitedNodes) {
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
        for (String address : this.connectedNodes) {
            if(!visitedNodes.contains(address)) {
                int portNode = Integer.parseInt(address.split(":")[1]);
                try (Socket socket = new Socket(address.split(":")[0], portNode)) {
                    {
                        printWriter = new PrintWriter(socket.getOutputStream(), true);
                        printWriter.println("Provide node");

                        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                        Node node = (Node) objectInputStream.readObject();
                        objectInputStream.close();
                        System.out.println("Received object " + node.port);
                        visitedNodes.add(address);
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
