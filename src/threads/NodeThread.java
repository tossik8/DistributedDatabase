package threads;


import components.DatabaseNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeThread extends Thread{
    private DatabaseNode node;

    public NodeThread(DatabaseNode node) {
        this.node = node;
    }

    @Override
    public void run() {
        super.run();
        ServerSocket serverSocket;
        do{
            try{
                serverSocket = new ServerSocket(node.getPort());
                break;
            } catch (IOException e) {
                node.setPort(node.getPort()+1);
            }
        }while(true);
        System.out.println("Listens on " + node.getPort());
        while(!currentThread().isInterrupted()){
            try {
                Socket request = serverSocket.accept();
                int i = 0;
                if(i == -1){
                    this.interrupt();
                }
                request.close();
            }

            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
