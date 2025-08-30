package com.example.mail;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 2525;
        new MailServer(port).start();
    }
}
