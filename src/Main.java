import components.DatabaseClient;
import components.DatabaseNode;
import threads.ClientThread;
import threads.NodeThread;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String line;
        String[] arguments;
        do{
            line = scanner.nextLine();
            arguments = line.split(" +");
            if(arguments[0].equals("")){
                System.out.println("Quiting...");
                System.exit(-1);
            }
            else if(arguments[1].equals("DatabaseNode")){
                DatabaseNode node = createNode(arguments);
                (new NodeThread(node)).start();
            }
            else if(arguments[1].equals("DatabaseClient")){
                DatabaseClient client = createClient(arguments);
                (new ClientThread(client)).start();
            }
            else{
                System.out.println("Only DatabaseNode or DatabaseClient is accepted");
            }
            System.out.println(Arrays.toString(arguments));
        }while (!arguments[0].equals(""));
    }
    public static DatabaseNode createNode(String [] args){
        try{
            int port = Integer.parseInt(args[3]);
            String[] arr = args[5].split(":");
            int key = Integer.parseInt(arr[0]);
            int value = Integer.parseInt(arr[1]);
            Map<Integer, String> addresses = new HashMap<>();
            for(int i = 7; i < args.length; i+=2){
                String[] address = args[i].split(":");
                addresses.put(Integer.parseInt(address[1]), address[0]);
            }
            return new DatabaseNode(port, "localhost", key, value, addresses);
        }catch (NumberFormatException e){
            System.out.println("""
                    Couldn't create a DatabaseNode. Make sure values are passed properly and in the correct order
                    java DatabaseNode -tcpport 9991 -record 17:256 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989""");
            return null;
        }
    }
    public static DatabaseClient createClient(String[] args){
        try{
            String[] arr = args[3].split(":");
            String nodeIP = arr[0];
            int nodePort = Integer.parseInt(arr[1]);
            String operation = args[5];
            LinkedList<Integer> parameters = new LinkedList<>();
            for(int i = 6; i < args.length; ++i){
                parameters.add(Integer.parseInt(args[i]));
            }
            return new DatabaseClient(nodePort,nodeIP, operation, parameters);
        } catch (NumberFormatException e){
            System.out.println("Couldn't create a DatabaseClient. Make sure values are passed properly and in the correct order\njava DatabaseClient java DatabaseClient -gateway localhost:9991 -operation get-value 17");
            return null;
        }


    }
}
