package com.example.aiagent.command;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class ConsoleIO {
    private final BufferedReader input = new BufferedReader(
            new InputStreamReader(System.in, StandardCharsets.UTF_8));

    public String readLine(String prompt) throws IOException {
        System.out.print(prompt);
        System.out.flush();
        return input.readLine();
    }

    public void print(String message) {
        System.out.print(message);
    }

    public void println(String message) {
        System.out.println(message);
    }

    public void printf(String format, Object... arguments) {
        System.out.printf(format, arguments);
    }

    public void error(String message) {
        System.err.println(message);
    }
}
