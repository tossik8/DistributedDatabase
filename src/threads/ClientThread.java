package threads;

import components.DatabaseClient;

public class ClientThread extends Thread{
    private DatabaseClient client;

    public ClientThread(DatabaseClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        super.run();

    }
}
