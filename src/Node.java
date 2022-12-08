import java.io.*;
import java.net.*;
import java.util.List;

public class Node {
    private int port;
    private final String ip;
    private int key;
    private int value;
    private List<String> connectedNodes;
    private ServerSocket serverSocket;
    public Node(int port, String ip, int key, int value, List<String> connectedNodes) {
        setPort(port);
        this.ip = ip;
        this.key = key;
        this.value = value;
        this.connectedNodes = connectedNodes;
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
    public boolean connectNode(String ip,int port){
        try(Socket socket = new Socket(ip, port)) {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println("connect-node " + this.ip+":"+this.port);
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
}
