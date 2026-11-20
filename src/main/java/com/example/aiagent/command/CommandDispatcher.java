package com.example.aiagent.command;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class CommandDispatcher {
    private final Map<String, Command> commandsByName = new HashMap<>();
    private final Set<String> aliases = new HashSet<>();
    private final List<Command> commands;
    private boolean chatMode;

    public CommandDispatcher(List<Command> commands) {
        this.commands = List.copyOf(commands);
        for (Command command : commands) {
            String name = normalize(command.name());
            if (!name.startsWith("/")) {
                throw new IllegalStateException("Command names must start with '/': " + command.name());
            }
            register(name, command, false);
            for (String alias : command.aliases()) {
                register(normalize(alias), command, true);
            }
        }
    }

    public boolean dispatch(String input, ConsoleIO console)
            throws IOException, InterruptedException {
        if (input == null) {
            return false;
        }

        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return true;
        }

        Command command;
        String commandInput;
        if (trimmed.startsWith("/")) {
            int separator = firstWhitespace(trimmed);
            String name = separator < 0 ? trimmed : trimmed.substring(0, separator);
            command = commandsByName.get(normalize(name));
            commandInput = separator < 0 ? "" : trimmed.substring(separator).trim();
            if (command == null) {
                console.println("등록되지 않은 명령어입니다: " + name);
                return true;
            }
        } else {
            String normalizedInput = normalize(trimmed);
            if (aliases.contains(normalizedInput)) {
                command = commandsByName.get(normalizedInput);
                commandInput = "";
            } else if (chatMode) {
                command = commandsByName.get("/chat");
                commandInput = input;
                if (command == null) {
                    console.println("대화 명령어가 등록되어 있지 않습니다.");
                    return true;
                }
            } else {
                console.println("등록되지 않은 명령어입니다. 명령어 앞에 /를 입력하세요.");
                return true;
            }
        }

        if (chatMode && !command.availableInChat()) {
            console.println("이 명령어는 대화 중 사용할 수 없습니다. /back으로 메뉴에 돌아가세요.");
            return true;
        }
        if (!chatMode && !command.availableInMenu()) {
            console.println("이 명령어는 메뉴에서 사용할 수 없습니다.");
            return true;
        }

        CommandResult result = command.execute(new CommandContext(console, chatMode), commandInput);
        if (result == null) {
            throw new IllegalStateException("Command returned no result: " + command.name());
        }
        return applyResult(result, console);
    }

    public boolean isChatMode() {
        return chatMode;
    }

    public String commandSummary(boolean inChat) {
        List<Command> available = new ArrayList<>();
        for (Command command : commands) {
            if (inChat ? command.availableInChat() : command.availableInMenu()) {
                available.add(command);
            }
        }
        available.sort(Comparator.comparing(Command::name));
        return available.stream()
                .map(command -> command.name() + " - " + command.description())
                .reduce((left, right) -> left + ", " + right)
                .orElse("등록된 명령어가 없습니다.");
    }

    private boolean applyResult(CommandResult result, ConsoleIO console) {
        switch (result) {
            case ENTER_CHAT -> {
                chatMode = true;
                console.println("대화 명령어: " + commandSummary(true));
            }
            case LEAVE_CHAT -> chatMode = false;
            case EXIT -> {
                return false;
            }
            case CONTINUE -> {
                // Keep the current command mode.
            }
        }
        return true;
    }

    private void register(String key, Command command, boolean alias) {
        Command existing = commandsByName.putIfAbsent(key, command);
        if (existing != null) {
            throw new IllegalStateException("Duplicate command name or alias: " + key);
        }
        if (alias) {
            aliases.add(key);
        }
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static int firstWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}
