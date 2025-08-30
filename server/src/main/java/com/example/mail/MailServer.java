package com.example.mail;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class MailServer implements AutoCloseable {
    private final int port;
    private final Store store = new Store();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean running = true;

    public MailServer(int port) { this.port = port; }

    public void start() throws Exception {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("SimpleMail server listening on port " + port);
            while (running) {
                Socket client = server.accept();
                pool.submit(new ClientHandler(client, store));
            }
        }
    }

    @Override public void close() { running = false; pool.shutdownNow(); }
}
