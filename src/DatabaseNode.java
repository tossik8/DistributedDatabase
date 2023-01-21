import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DatabaseNode {
    public static void main(String[] args) {
        if(args.length < 3 || ((!args[0].equals("-tcpport")))){
            System.err.println("Wrong argument names\nExample of execution: java DatabaseNode -tcpport 9991 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989 -record 17:256");
            return;
        }
        try{
            int port = Integer.parseInt(args[1]);
            if(port <= 0){
                System.err.println("Port numbers must be positive natural numbers. Instead received " + port);
                return;
            }
            List<String> addresses = new CopyOnWriteArrayList<>();
            if(!args[args.length-2].equals("-record")){
                System.err.println("Wrong argument. Expected -record, received: " + args[args.length-2]);
                return;
            }
            String[] arr = args[args.length - 1].split(":");
            int key, value;
            try{
                key = Integer.parseInt(arr[0]);
                value = Integer.parseInt(arr[1]);
            }catch (ArrayIndexOutOfBoundsException e){
                System.err.println("Arguments for -record were not provided in a proper format");
                return;
            }
            Node node = new Node(port, obtainIP(), key, value, addresses);
            int i = 3;
            for(; i < args.length - 1; i+=2){
                if(args[i-1].equals("-connect")){
                    String[] address = args[i].split(":");
                    if(address[0].equals("localhost") || address[0].equals("127.0.0.1")){
                        address[0] = obtainIP();
                    }
                    try {
                        if ((!node.getIp().equals(address[0]) || node.getPort() != Integer.parseInt(address[1])) &&
                                node.connectNode(address[0], Integer.parseInt(address[1]))) {
                            addresses.add(address[0] + ":" + address[1]);
                        }
                    }catch (ArrayIndexOutOfBoundsException e){
                        System.err.println("Arguments for -connect were not provided in a proper format");
                    }
                }
            }
            System.out.println("The new node listens on " + node.getIp() + ":" + node.getPort());
            System.out.println("Contains a key-value pair " + node.getKey() + ":" + node.getValue());
            System.out.print("Connected to nodes: ");
            for(String neighbour:node.getConnectedNodes()){
                System.out.print(neighbour + " ");
            }
            System.out.println();
            try (Socket socket = new Socket(node.getIp(), node.getPort())) {
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                printWriter.println("new-record " + node.getKey() + ":" + node.getValue() + " [" + node.getIp() + ":" + node.getPort() + "]");
                printWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            node.listen();
        } catch (NumberFormatException e){
            System.err.println("Couldn't create a DatabaseNode. Make sure values are passed properly and in the correct order\njava DatabaseNode -tcpport 9991 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989 -record 17:256");
        }
    }
    public static String obtainIP(){
        Enumeration<NetworkInterface> en;
        try {
            en = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while (en.hasMoreElements()){
            NetworkInterface networkInterface = en.nextElement();
            Enumeration<InetAddress> en2 = networkInterface.getInetAddresses();
            while(en2.hasMoreElements()){
                InetAddress address = en2.nextElement();
                if(networkInterface.toString().startsWith("name:wlan2")){
                    return address.toString().substring(1);
                }
            }
        }
        return "localhost";
    }
}
