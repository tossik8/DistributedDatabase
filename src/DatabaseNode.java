import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class DatabaseNode {
    public static void main(String[] args) {
        if(args.length < 3 || ((!args[0].equals("-tcpport") || !args[2].equals("-record")))){
            System.err.println("Wrong argument names\nExample of execution: java DatabaseNode -tcpport 9991 -record 17:256 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989");
            return;
        }
        try{
            int port = Integer.parseInt(args[1]);
            String[] arr = args[3].split(":");
            int key, value;
            try{
                key = Integer.parseInt(arr[0]);
                value = Integer.parseInt(arr[1]);
            }catch (ArrayIndexOutOfBoundsException e){
                System.err.println("Arguments for -record were not provided in a proper format");
                return;
            }
            List<String> addresses = new LinkedList<>();
            Node node = new Node(port, "192.168.0.94", key, value, addresses);
            for(int i = 5; i < args.length; i+=2){
                if(args[i-1].equals("-connect")){
                    String[] address = args[i].split(":");
                    try {
                        if ((!node.getIp().equals(address[0]) || node.getPort() != Integer.parseInt(address[1])) &&
                                node.connectNode(address[0], Integer.parseInt(address[1]))) {
                            addresses.add(args[i]);
                        }
                    }catch (ArrayIndexOutOfBoundsException e){
                        System.err.println("Arguments for -connect were not provided in a proper format");
                    }
                }
                else{
                    System.err.println("Wrong argument\nExpected -connect. Received " + args[i-1]);
                }
            }
            System.out.print("The new node listens on " + node.getIp() + ":" + node.getPort() + ", contains the value of " + node.getValue() + " under the key " + node.getKey()+"\nConnected to nodes: ");
            for(String neighbour:node.getConnectedNodes()){
                System.out.print(neighbour + " ");
            }
            System.out.println();
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try (Socket socket = new Socket(node.getIp(), node.getPort())) {
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println("new-record " + node.getKey() +" " +  node.getValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
            node.listen();
        } catch (NumberFormatException e){
            System.err.println("Couldn't create a DatabaseNode. Make sure values are passed properly and in the correct order\njava DatabaseNode -tcpport 9991 -record 17:256 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989");
        }
    }
}
