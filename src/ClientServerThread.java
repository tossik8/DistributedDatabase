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
        try (PrintWriter pw = new PrintWriter(serverSocket.getOutputStream(), true);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()))){
            String firstLine = bufferedReader.readLine();
            String[] arguments = firstLine.split(" ");
            this.executeOperation(pw, firstLine, arguments, arguments[0], new LinkedList<>());
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void executeOperation(PrintWriter pw, String firstLine, String[] arguments, String operation, List<String> visitedNodes) throws IOException {
        switch (operation) {
            case "get-value":
                if (arguments.length > 2) {
                    visitedNodes.addAll(Arrays.asList(arguments[2].substring(1, arguments[2].length() - 1).split(",")));
                }
                pw.println(this.getValue(Integer.parseInt(arguments[1]), visitedNodes));
                node.getRunningProcesses().remove(this);
                break;
            case "set-value":
                if (arguments.length > 2) {
                    visitedNodes.addAll(Arrays.asList(arguments[2].substring(1, arguments[2].length() - 1).split(",")));
                }
                pw.println(this.setValue(Integer.parseInt(arguments[1].split(":")[0]), Integer.parseInt(arguments[1].split(":")[1]), visitedNodes));
                node.getRunningProcesses().remove(this);
                break;
            case "find-key":
                if (arguments.length > 2) {
                    visitedNodes.addAll(Arrays.asList(arguments[2].substring(1, arguments[2].length() - 1).split(",")));
                }
                pw.println(this.findKey(Integer.parseInt(arguments[1]), visitedNodes));
                node.getRunningProcesses().remove(this);
                break;
            case "get-max":
                if (arguments.length > 1) {
                    visitedNodes.addAll(Arrays.asList(arguments[1].substring(1, arguments[1].length() - 1).split(",")));
                }
                pw.println(this.getMax(visitedNodes));
                node.getRunningProcesses().remove(this);
                break;
            case "get-min":
                if (arguments.length > 1) {
                    visitedNodes.addAll(Arrays.asList(arguments[1].substring(1, arguments[1].length() - 1).split(",")));
                }
                pw.println(this.getMin(visitedNodes));
                node.getRunningProcesses().remove(this);
                break;
            case "new-record":
                pw.println(this.newPair(Integer.parseInt(arguments[1].split(":")[0]), Integer.parseInt(arguments[1].split(":")[1]), visitedNodes));
                node.getRunningProcesses().remove(this);
                break;
            case "terminate":
                node.getServerSocket().close();
                node.getRunningProcesses().remove(this);
                while (!node.getRunningProcesses().isEmpty()) {
                    System.out.print("");
                }
                this.terminate();
                pw.println("OK");
                break;
            case "connect-node":
                node.getConnectedNodes().add(arguments[1]);
                this.printConnectedNodes();
                node.getRunningProcesses().remove(this);
                break;
            case "disconnect-node":
                node.getConnectedNodes().remove(arguments[1]);
                System.out.println(arguments[1] + " is no longer connected to " + node.getIp() + ":" + node.getPort());
                List<String> neighbours = new LinkedList<>(Arrays.asList(arguments[2].substring(1, arguments[2].length() - 1).split(",")));
                for (String neighbour : neighbours) {
                    if (!node.getConnectedNodes().contains(neighbour) && !(node.getIp() + ":" + node.getPort()).equals(neighbour))
                        node.getConnectedNodes().add(neighbour);
                }
                this.printConnectedNodes();
                node.getRunningProcesses().remove(this);
                break;
            default:
                pw.println("There is no operation " + firstLine);
                node.getRunningProcesses().remove(this);
                break;
        }
    }

    public void printConnectedNodes(){
        System.out.print(node.getIp() + ":" + node.getPort() + " is connected to: ");
        for (String s : node.getConnectedNodes()) {
            System.out.print(s + " ");
        }
        System.out.println();
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
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    printWriter.println("get-value " + key + " " + visitedNodes.toString().replace(" ", ""));
                    String result = reader.readLine();
                    if(!result.equals("ERROR")) return result;
                    reader.close();
                    printWriter.close();
                } catch (ConnectException e){
                    System.err.println("No connection with " + address);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return "ERROR";
    }
    public String setValue(int key, int value, List<String> visitedNodes){
        String res = "ERROR";
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
                    printWriter.println("set-value " + key + ":" + value + " " + visitedNodes.toString().replace(" ", ""));
                    String result = reader.readLine();
                    if(!result.equals("ERROR")) res = result;
                    reader.close();
                    printWriter.close();
                } catch (ConnectException e){
                    System.err.println("No connection with " + address);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return res;
    }

    public String findKey(int key, List<String> visitedNodes){
        if(node.getKey() == key){
            return node.getIp()+":"+node.getPort();
        }
        visitedNodes.add(node.getIp()+":"+node.getPort());
        for(String address:node.getConnectedNodes()){
            if(!visitedNodes.contains(address)){
                try (Socket socket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]))){
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println("find-key " + key + " " + visitedNodes.toString().replace(" ", ""));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String res= bufferedReader.readLine();
                    if(!res.equals("ERROR")) return res;
                    bufferedReader.close();
                    printWriter.close();
                } catch (ConnectException e){
                    System.err.println("No connection with " + address);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return "ERROR";
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
                    max = compareValues(true, max, bufferedReader.readLine());
                    bufferedReader.close();
                    printWriter.close();
                } catch (ConnectException e){
                    System.err.println("No connection with " + address);
                }
                catch (IOException e) {
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
                    min = compareValues(false, min, bufferedReader.readLine());
                    bufferedReader.close();
                    printWriter.close();
                } catch (ConnectException e){
                    System.err.println("No connection with " + address);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return min;
    }

    public String compareValues(boolean isMax, String value, String value2){
        if(isMax){
            return Integer.parseInt(value.split(":")[1]) > Integer.parseInt(value2.split(":")[1])? value:value2;
        }
        return Integer.parseInt(value2.split(":")[1]) < Integer.parseInt(value.split(":")[1])?value2:value;
    }
    public String newPair(int key, int value, List<String> visitedNodes){
        node.setKey(key);
        node.setValue(value);
        visitedNodes.addAll(node.getConnectedNodes());
        visitedNodes.add(node.getIp() +":" + node.getPort());
        for(String address : node.getConnectedNodes()){
            try (Socket socket = new Socket(address.split(":")[0], Integer.parseInt(address.split(":")[1]))){
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                visitedNodes.remove(address);
                printWriter.println("set-value " + key + ":" + value + " " + visitedNodes.toString().replace(" ", ""));
                visitedNodes.add(address);
                printWriter.close();
            } catch (ConnectException e){
                System.err.println("No connection with " + address);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return "OK";
    }
    public void terminate(){
        for(int i = 0; i < node.getConnectedNodes().size(); ++i){
            String address = node.getConnectedNodes().get(i);
            this.disconnectNode(address, node.getConnectedNodes());
        }
        node.getConnectedNodes().clear();
    }
    public void disconnectNode(String nodeAddress, List<String> addresses){
        try(Socket socket = new Socket(nodeAddress.split(":")[0], Integer.parseInt(nodeAddress.split(":")[1]))) {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println("disconnect-node " + node.getIp()+":"+node.getPort() + " " +addresses.toString().replace(" ", ""));
            pw.close();
        }catch (ConnectException e){
            System.err.println("Failed to connect to " + nodeAddress);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
