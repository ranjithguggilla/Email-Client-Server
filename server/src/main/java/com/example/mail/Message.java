package com.example.mail;

import java.time.Instant;

public class Message {
    public final String id;
    public final String from;
    public final String to;
    public final String subject;
    public final String body;
    public final Instant timestamp;
    public boolean read;

    public Message(String id, String from, String to, String subject, String body, Instant timestamp) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.timestamp = timestamp;
        this.read = false;
    }
}
