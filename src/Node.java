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
