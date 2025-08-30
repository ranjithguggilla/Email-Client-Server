package com.example.mail;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Store store;
    private String authedUser = null;

    public ClientHandler(Socket socket, Store store) { this.socket = socket; this.store = store; }

    @Override public void run() {
        try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            Utils.sendLine(socket, "OK SIMPLEMAIL 1.0");
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(" ", 2);
                String cmd = parts[0].toUpperCase();

                switch (cmd) {
                    case "REGISTER" -> handleRegister(parts.length > 1 ? parts[1] : "");
                    case "LOGIN"    -> handleLogin(parts.length > 1 ? parts[1] : "");
                    case "SEND"     -> handleSend();
                    case "INBOX"    -> handleInbox();
                    case "READ"     -> handleRead(parts.length > 1 ? parts[1] : "");
                    case "QUIT"     -> { Utils.sendLine(socket, "BYE"); return; }
                    default         -> Utils.sendLine(socket, "ERR unknown_command");
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }

    private void handleRegister(String rest) throws IOException {
        String[] p = rest.split("\\s+");
        if (p.length != 2) { Utils.sendLine(socket, "ERR usage REGISTER <user> <pass>"); return; }
        boolean ok = store.register(p[0], p[1]);
        Utils.sendLine(socket, ok ? "OK registered" : "ERR user_exists");
    }

    private void handleLogin(String rest) throws IOException {
        String[] p = rest.split("\\s+");
        if (p.length != 2) { Utils.sendLine(socket, "ERR usage LOGIN <user> <pass>"); return; }
        if (store.checkLogin(p[0], p[1])) { authedUser = p[0]; Utils.sendLine(socket, "OK logged_in"); }
        else { Utils.sendLine(socket, "ERR bad_credentials"); }
    }

    private void handleSend() throws IOException {
        if (!requireAuth()) return;
        var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        String toLine = in.readLine(); String subjectLine = in.readLine(); String bodyLenLine = in.readLine();
        if (toLine == null || subjectLine == null || bodyLenLine == null) { Utils.sendLine(socket, "ERR send_format"); return; }
        String to = toLine.startsWith("TO:") ? toLine.substring(3).trim() : "";
        String subject = subjectLine.startsWith("SUBJECT:") ? subjectLine.substring(8).trim() : "";
        int len;
        try { len = Integer.parseInt(bodyLenLine.startsWith("BODY-LEN:") ? bodyLenLine.substring(9).trim() : "-1"); }
        catch (NumberFormatException e) { Utils.sendLine(socket, "ERR body_len"); return; }
        if (to.isEmpty() || len < 0) { Utils.sendLine(socket, "ERR send_format"); return; }
        if (!store.userExists(to)) { Utils.sendLine(socket, "ERR no_such_recipient"); return; }

        char[] buf = new char[len]; int read = 0;
        while (read < len) { int r = in.read(buf, read, len - read); if (r == -1) break; read += r; }
        String body = new String(buf, 0, read);
        var msg = store.send(authedUser, to, subject, body);
        Utils.sendLine(socket, "OK sent id=" + msg.id);
    }

    private void handleInbox() throws IOException {
        if (!requireAuth()) return;
        List<Message> messages = store.inbox(authedUser);
        Utils.sendLine(socket, "OK " + messages.size());
        for (Message m : messages) {
            Utils.sendLine(socket, m.id + " | from=" + m.from + " | subj=" + m.subject + " | " + (m.read ? "READ" : "UNREAD"));
        }
        Utils.sendLine(socket, ".");
    }

    private void handleRead(String id) throws IOException {
        if (!requireAuth()) return;
        if (id.isBlank()) { Utils.sendLine(socket, "ERR usage READ <id>"); return; }
        Optional<Message> om = store.get(authedUser, id.trim());
        if (om.isEmpty()) { Utils.sendLine(socket, "ERR not_found"); return; }
        Message m = om.get(); m.read = true;
        String body = m.body == null ? "" : m.body;
        Utils.sendLine(socket, "OK");
        Utils.sendLine(socket, "FROM: " + m.from);
        Utils.sendLine(socket, "SUBJECT: " + m.subject);
        Utils.sendLine(socket, "DATE: " + m.timestamp);
        Utils.sendLine(socket, "BODY-LEN: " + body.length());
        Utils.sendLine(socket, body);
        Utils.sendLine(socket, ".");
    }

    private boolean requireAuth() throws IOException {
        if (authedUser == null) { Utils.sendLine(socket, "ERR not_logged_in"); return false; }
        return true;
    }
}
