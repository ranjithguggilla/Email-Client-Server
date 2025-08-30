package com.example.mail;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Store {
    private final Map<String, String> users = new ConcurrentHashMap<>();
    private final Map<String, List<Message>> mailboxes = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public synchronized boolean register(String user, String pass) {
        if (users.containsKey(user)) return false;
        users.put(user, pass);
        mailboxes.put(user, Collections.synchronizedList(new ArrayList<>()));
        return true;
    }

    public boolean checkLogin(String user, String pass) { return pass.equals(users.get(user)); }
    public boolean userExists(String user) { return users.containsKey(user); }

    public Message send(String from, String to, String subject, String body) {
        String id = "m" + seq.getAndIncrement();
        Message m = new Message(id, from, to, subject, body, Instant.now());
        mailboxes.computeIfAbsent(to, k -> Collections.synchronizedList(new ArrayList<>())).add(m);
        return m;
    }

    public List<Message> inbox(String user) { return new ArrayList<>(mailboxes.getOrDefault(user, List.of())); }

    public Optional<Message> get(String user, String id) {
        return mailboxes.getOrDefault(user, List.of()).stream().filter(m -> m.id.equals(id)).findFirst();
    }
}
