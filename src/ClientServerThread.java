import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
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
            String[] arguments = firstLine.split(" ");
            String operation = arguments[0];
            List<String> visitedNodes = new LinkedList<>();
            if(operation.equals("get-value")){
                if(arguments.length > 2){
                    visitedNodes.addAll(Arrays.asList(arguments[2].substring(1, arguments[2].length()-1).split(",")));
                }
                pw.println(this.getValue(Integer.parseInt(arguments[1]), visitedNodes));
            }
            else if(operation.equals("set-value")){
                int argument = Integer.parseInt(arguments[1]);
                int argument1 = Integer.parseInt(arguments[2]);
                if(arguments.length > 3){
                    visitedNodes.addAll(Arrays.asList(arguments[3].substring(1, arguments[3].length()-1).split(",")));
                }
                pw.println(this.setValue(argument, argument1, visitedNodes));
            }

            else if(operation.equals("find-key")){
                int argument = Integer.parseInt(firstLine.split(" ")[1]);
                if(arguments.length > 2){
                    visitedNodes.addAll(Arrays.asList(arguments[2].substring(1, arguments[2].length()-1).split(",")));
                }
                String result = this.findKey(argument, visitedNodes);
                pw.println(result);
            }

            else if(operation.equals("get-max")){
                if(arguments.length > 1){
                    visitedNodes.addAll(Arrays.asList(arguments[1].substring(1, arguments[1].length()-1).split(",")));
                }
                pw.println(this.getMax(visitedNodes));
            }

            else if(operation.equals("get-min")){
                if(arguments.length > 1){
                    visitedNodes.addAll(Arrays.asList(arguments[1].substring(1, arguments[1].length()-1).split(",")));
                }
                pw.println(this.getMin(visitedNodes));
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
            else if (firstLine.equals("Connect node")) {
                    String newNode = bufferedReader.readLine();
                    node.getConnectedNodes().add(newNode);
                    System.out.print(node.getIp() + " " + node.getPort() + " is connected to: ");
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
                System.out.print(node.getIp() + " " + node.getPort() + " is connected to: ");
                for (String s : node.getConnectedNodes()) {
                    System.out.print(s + " ");
                }
                System.out.println();

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

                        printWriter.println("get-value " + key + " " +
                                visitedNodes.toString().replace(" ", ""));
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
                    printWriter.println("set-value " + key + " " + value + " " + visitedNodes.toString().replace(" ", ""));
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
                    printWriter.println("find-key " + key + visitedNodes.toString().replace(" ", ""));
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
                    printWriter.println("get-max " + visitedNodes.toString().replace(" ", ""));
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
                    printWriter.println("get-min " + visitedNodes.toString().replace(" ", ""));
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
                printWriter.println("set-value " + key + " " + value + " " + node.getConnectedNodes().toString().replace(" ", ""));
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
