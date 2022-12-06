

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ClientServerThread extends Thread{
    private final Socket serverSocket;
    private final Node node;


    public ClientServerThread(Socket serverSocket, Node node) {
        this.serverSocket = serverSocket;
        this.node = node;
    }

    @Override
    public void run() {
        super.run();
        try {
            PrintWriter pw = new PrintWriter(serverSocket.getOutputStream(), true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            String firstLine = bufferedReader.readLine();
            if (firstLine.equals("Connect node")) {
                    String newNode = bufferedReader.readLine();
                    node.getConnectedNodes().add(newNode);
                    System.out.println(node.getIp() + " " + node.getPort() + " is connected to: ");
                    for (String s : node.getConnectedNodes()) {
                        System.out.print(s + " ");
                    }
                    System.out.println();
            }
            else if(firstLine.equals("Disconnect node")){
                String address = bufferedReader.readLine();
                node.getConnectedNodes().remove(address);
                System.out.println(address + " is no longer connected to " + node.getIp() + " " + node.getPort());
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    if(!node.getConnectedNodes().contains(line) && !(node.getIp()+":"+node.getPort()).equals(line))
                        node.getConnectedNodes().add(line);
                }
                System.out.println(node.getIp() + " " + node.getPort() + " is connected to: ");
                for (String s : node.getConnectedNodes()) {
                    System.out.print(s + " ");
                }
                System.out.println();

            }
            else if(firstLine.equals("Provide node")){
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());
                objectOutputStream.writeObject(node);
                objectOutputStream.flush();
                objectOutputStream.close();
            }
            String[] arguments = firstLine.split(" ");
            String operation = arguments[0];
            if(operation.equals("get-value")){
                int argument = Integer.parseInt(arguments[1]);
                String result = this.getValue(argument, new LinkedList<>());
                pw.println(result);
            }
            else if(operation.equals("get-value-node")){
                List<String> neighbours = new LinkedList<>();
                int argument = Integer.parseInt(arguments[1]);

                String neighbour;

                while(!(neighbour = bufferedReader.readLine()).equals("closed")){
                    neighbours.add(neighbour);
                }

                String result = this.getValue(argument, neighbours);
                pw.println(result);
            }
            else if(operation.equals("set-value")){
                int argument = Integer.parseInt(arguments[1]);
                int argument1 = Integer.parseInt(arguments[2]);
                String result = this.setValue(argument, argument1, new LinkedList<>());
                pw.println(result);
            }
            else if(operation.equals("set-value-node")){
                List<String> neighbours = new LinkedList<>();
                int argument = Integer.parseInt(arguments[1]);
                int argument1 = Integer.parseInt(arguments[2]);
                String neighbour;
                while(!(neighbour = bufferedReader.readLine()).equals("closed")){
                    neighbours.add(neighbour);
                }
                String result = this.setValue(argument, argument1, neighbours);
                pw.println(result);
            }
            else if(operation.equals("find-key")){
                int argument = Integer.parseInt(firstLine.split(" ")[1]);
                String result = this.findKey(argument, new LinkedList<>());
                pw.println(result);
            }
            else if(operation.equals("find-key-node")){
                List<String> neighbours = new LinkedList<>();
                int argument = Integer.parseInt(firstLine.split(" ")[1]);
                String neighbour;
                while(!(neighbour = bufferedReader.readLine()).equals("closed")){
                    neighbours.add(neighbour);
                }
                String result = this.findKey(argument, neighbours);
                pw.println(result);
            }
            else if(operation.equals("get-max")){

                String result = this.getMax(new LinkedList<>());
                pw.println(result);
            }
            else if(operation.equals("get-max-node")){
                List<String> neighbours = new LinkedList<>();
                String neighbour;
                while(!(neighbour = bufferedReader.readLine()).equals("closed")){
                    neighbours.add(neighbour);
                }
                String res = this.getMax(neighbours);
                pw.println(res);
            }
            else if(operation.equals("get-min")){
                String result = this.getMin(new LinkedList<>());
                pw.println(result);
            }
            else if(operation.equals("get-min-node")){
                List<String> neighbours = new LinkedList<>();
                String neighbour;
                while(!(neighbour = bufferedReader.readLine()).equals("closed")){
                    neighbours.add(neighbour);
                }
                String res = this.getMin(neighbours);
                pw.println(res);
            }
            else if(operation.equals("new-record")){
                int argument = Integer.parseInt(firstLine.split(" ")[1]);
                int argument1 = Integer.parseInt(firstLine.split(" ")[2]);
                String result = this.newPair(argument, argument1);
                pw.println(result);
            }
            else if(operation.equals("terminate")){
                this.terminate();
                String result = "Node terminated";
                pw.println(result);
                node.getServerSocket().close();
            }
            else {
                pw.println("There is no operation " + firstLine);
            }
            serverSocket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void disconnectNode(String ip, int port, List<String> addresses){
        try(Socket socket = new Socket(ip, port)) {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println("Disconnect node");
            pw.println(node.getIp()+":"+node.getPort());
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
    public String getValue(int key, List<String> visitedNodes) {
        if (node.getKey() == key) {
            return key + ":" + node.getValue();
        }

        visitedNodes.add(node.getIp() + ":" + node.getPort());
        for (String address : node.getConnectedNodes()) {
            if (!visitedNodes.contains(address)) {
                int portNode = Integer.parseInt(address.split(":")[1]);
                try (Socket socket = new Socket(address.split(":")[0], portNode)) {
                    {
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        printWriter.println("get-value-node " + key);

                        for(String address2 : visitedNodes){

                            printWriter.println(address2);

                        }
                        printWriter.println("closed");
                        String result = reader.readLine();
                        if(!result.contains("Error")) return result;

                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return "Error, there is no record with the key of " + key;
    }


    public String setValue(int key, int value, List<String> visitedNodes){

        String res = "Error, couldn't set the value as there is no record with key " + key;
        if(key == node.getKey()){
            node.setValue(value);
            res = key + ":" + value;
        }
        visitedNodes.add(node.getIp()+":"+node.getPort());
        for(String address : node.getConnectedNodes()){
            if (!visitedNodes.contains(address)) {
                try (Socket socket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]))) {
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    printWriter.println("set-value-node " + key + " " + value);

                    for(String address2 : visitedNodes){

                        printWriter.println(address2);

                    }
                    printWriter.println("closed");
                    String result = reader.readLine();
                    if(!result.contains("Error")) res = result;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return res;
    }

    public String findKey(int key, List<String> visitedNodes){
        if(node.getKey() == key){
            return key + " can be found at " + node.getIp()+":"+node.getPort();
        }
        visitedNodes.add(node.getIp()+":"+node.getPort());
        for(String address:node.getConnectedNodes()){
            if(!visitedNodes.contains(address)){
                try (Socket socket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]))){
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println("find-key-node " + key);
                    for(String address2 : visitedNodes){
                        printWriter.println(address2);
                    }
                    printWriter.println("closed");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String res= bufferedReader.readLine();
                    if(!res.contains("Error")) return res;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return "Error, there is not any node containing key " + key;
    }
    public String getMax(List<String> visitedNodes){
        String max = node.getKey() + ":"+node.getValue();
        visitedNodes.add(node.getIp()+":"+node.getPort());
        for(String address:node.getConnectedNodes()){
            if(!visitedNodes.contains(address)){
                try (Socket socket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]))){
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println("get-max-node");
                    for(String address2 : visitedNodes){
                        printWriter.println(address2);
                    }
                    printWriter.println("closed");
                    BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String max2 = bufferedReader.readLine();
                    if(node.getValue() < Integer.parseInt(max2.split(":")[1])){
                        max = max2;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return max;

    }

    public String getMin(List<String> visitedNodes){
        String min = node.getKey() + ":"+node.getValue();
        visitedNodes.add(node.getIp()+":"+node.getPort());
        for(String address:node.getConnectedNodes()){
            if(!visitedNodes.contains(address)){
                try (Socket socket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]))){
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println("get-min-node");
                    for(String address2 : visitedNodes){
                        printWriter.println(address2);
                    }
                    printWriter.println("closed");
                    BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String min2 = bufferedReader.readLine();
                    if(node.getValue() > Integer.parseInt(min2.split(":")[1])){
                        min = min2;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return min;
    }
    public String newPair(int key, int value){
        node.setKey(key);
        node.setValue(value);
        for(String address : node.getConnectedNodes()){
            try (Socket socket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]))){
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                printWriter.println("set-value-node " + key + " " + value);
                for(String address2 : node.getConnectedNodes()){
                    printWriter.println(address2);
                }
                printWriter.println("closed");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return "OK, " + key+":"+value;
    }
    public void terminate(){
        for(int i = 0; i < node.getConnectedNodes().size(); ++i){
            String address = node.getConnectedNodes().get(i);
            this.disconnectNode(address.split(":")[0],Integer.parseInt(address.split(":")[1]), node.getConnectedNodes());
        }
        node.getConnectedNodes().clear();
    }
}
