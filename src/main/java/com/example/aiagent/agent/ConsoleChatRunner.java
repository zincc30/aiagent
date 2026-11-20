package com.example.aiagent.agent;

import com.example.aiagent.command.CommandDispatcher;
import com.example.aiagent.command.ConsoleIO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConditionalOnProperty(prefix = "agent.chat", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ConsoleChatRunner implements CommandLineRunner {
    private final CommandDispatcher commandDispatcher;
    private final ConsoleIO console;

    public ConsoleChatRunner(CommandDispatcher commandDispatcher, ConsoleIO console) {
        this.commandDispatcher = commandDispatcher;
        this.console = console;
    }

    @Override
    public void run(String... args) throws IOException {
        console.println("명령어: " + commandDispatcher.commandSummary(false));
        while (true) {
            String prompt = commandDispatcher.isChatMode() ? "You> " : "Command> ";
            String input = console.readLine(prompt);
            if (input == null) {
                return;
            }
            try {
                if (!commandDispatcher.dispatch(input, console)) {
                    return;
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                console.error("요청이 중단되어 프로그램을 종료합니다.");
                return;
            }
        }
    }
}
