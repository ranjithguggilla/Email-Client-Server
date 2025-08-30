package com.example.mail;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static void sendLine(Socket s, String line) throws IOException {
        var out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
        out.write(line);
        out.write("\r\n");
        out.flush();
    }
}
