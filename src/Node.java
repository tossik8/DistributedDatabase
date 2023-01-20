import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Node {
    private int port;
    private final String ip;
    private int key;
    private int value;
    private final List<String> connectedNodes;
    private ServerSocket serverSocket;
    private final List<Thread> runningProcesses;
    public Node(int port, String ip, int key, int value, List<String> connectedNodes) {
        setPort(port);
        this.ip = ip;
        this.key = key;
        this.value = value;
        this.connectedNodes = connectedNodes;
        runningProcesses = new ArrayList<>();
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
    public ServerSocket getServerSocket() {
        return serverSocket;
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
    public List<Thread> getRunningProcesses() {
        return runningProcesses;
    }
    public boolean connectNode(String ip, int port){
        try(Socket socket = new Socket(ip, port)) {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println("connect-node " + this.ip+":"+this.port);
            pw.close();
        }catch (IOException e){
            System.err.println("Failed to connect to " + ip + ":" + port);
            return false;
        }
        return true;
    }
    public void listen() {
        while(!serverSocket.isClosed()){
            try {
                Socket request = serverSocket.accept();
                Thread thread = new ClientServerThread(request, this);
                runningProcesses.add(thread);
                thread.start();
            }
            catch (IOException e) {
                System.out.println(this.ip + ":" + this.port + " is closed");
            }
        }
    }
}
