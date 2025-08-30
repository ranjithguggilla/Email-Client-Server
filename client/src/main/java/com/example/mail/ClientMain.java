package com.example.mail;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 2525;

        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
             Scanner sc = new Scanner(System.in)) {

            System.out.println(in.readLine()); // greeting

            while (true) {
                System.out.println("""                        === SimpleMail Client ===
                        1) Register
                        2) Login
                        3) Send
                        4) Inbox
                        5) Read
                        6) Quit
                        Choose: """);
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> {
                        System.out.print("username: "); String u = sc.nextLine();
                        System.out.print("password: "); String p = sc.nextLine();
                        sendLine(out, "REGISTER " + u + " " + p);
                        System.out.println(in.readLine());
                    }
                    case "2" -> {
                        System.out.print("username: "); String u = sc.nextLine();
                        System.out.print("password: "); String p = sc.nextLine();
                        sendLine(out, "LOGIN " + u + " " + p);
                        System.out.println(in.readLine());
                    }
                    case "3" -> {
                        System.out.print("to: "); String to = sc.nextLine();
                        System.out.print("subject: "); String subject = sc.nextLine();
                        System.out.println("Enter body (end with a single '.' on its own line):");
                        StringBuilder body = new StringBuilder();
                        while (true) {
                            String line = sc.nextLine();
                            if (line.equals(".")) break;
                            body.append(line).append("\n");
                        }
                        String b = body.toString();
                        sendLine(out, "SEND");
                        sendLine(out, "TO: " + to);
                        sendLine(out, "SUBJECT: " + subject);
                        sendLine(out, "BODY-LEN: " + b.length());
                        out.write(b); out.flush();
                        System.out.println(in.readLine());
                    }
                    case "4" -> {
                        sendLine(out, "INBOX");
                        String head = in.readLine();
                        System.out.println(head);
                        String line;
                        while (!(line = in.readLine()).equals(".")) {
                            System.out.println(line);
                        }
                    }
                    case "5" -> {
                        System.out.print("message id: "); String id = sc.nextLine();
                        sendLine(out, "READ " + id);
                        String status = in.readLine();
                        System.out.println(status);
                        if (status.startsWith("OK")) {
                            System.out.println(in.readLine()); // FROM
                            System.out.println(in.readLine()); // SUBJECT
                            System.out.println(in.readLine()); // DATE
                            String lenLine = in.readLine();    // BODY-LEN
                            int len = Integer.parseInt(lenLine.split(":")[1].trim());
                            char[] buf = new char[len];
                            int read = 0;
                            while (read < len) {
                                int r = in.read(buf, read, len - read);
                                if (r == -1) break;
                                read += r;
                            }
                            System.out.println("BODY:\n" + new String(buf, 0, read));
                            System.out.println(in.readLine()); // "."
                        }
                    }
                    case "6" -> {
                        sendLine(out, "QUIT");
                        System.out.println(in.readLine());
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }
        }
    }

    private static void sendLine(BufferedWriter out, String line) throws IOException {
        out.write(line); out.write("\r\n"); out.flush();
    }
}
