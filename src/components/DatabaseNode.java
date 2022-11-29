package components;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class DatabaseNode {
    private int port;
    private String ip;
    private int key;
    private int value;


    public DatabaseNode(int port, String ip, int key, int value, Map<Integer, String> addresses) {
        this.ip = ip;
        this.key = key;
        this.value = value;
        this.port = port;
    }

    public void listen() {

    }
    public void setValue(int value) {
        this.value = value;
    }

    public void setPort(int port) {
        this.port = port;
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

}
